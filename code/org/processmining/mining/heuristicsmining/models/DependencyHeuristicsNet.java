/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.mining.heuristicsmining.models;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;

import org.processmining.framework.log.LogEvents;
import org.processmining.framework.models.heuristics.HNSet;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Ton Weijters
 * @version 1.0
 */

public class DependencyHeuristicsNet extends HeuristicsNet {

	private String helpString;
	private DoubleMatrix2D dependencyMeasures;

	public DependencyHeuristicsNet(LogEvents events,
			DoubleMatrix2D dependencyMeasures,
			DoubleMatrix2D directSuccessionCount) {
		super(events);
		this.dependencyMeasures = dependencyMeasures;

	}

	public String toStringWithEvents() {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < size(); i++) {
			sb.append("\n\n").append(WME_HEADER).append(" ").append(
					WME_NAME_DELIMITER).append(
					getLogEvents().getEvent(getDuplicatesMapping()[i])
							.getModelElementName()).append(" (").append(
					getLogEvents().getEvent(getDuplicatesMapping()[i])
							.getEventType()).append(")").append(
					WME_NAME_DELIMITER).append(":");

			// building IN part....
			sb.append("\n").append(INPUT_SETS_HEADER).append(": ");

			sb.append("[ ");
			buildVisualPresentation(sb, getInputSets()[i], getLogEvents());
			sb.append(" ]");

			// building OUT part....
			sb.append("\n").append(OUTPUT_SETS_HEADER).append(": ");
			sb.append("[ ");
			buildVisualPresentation(sb, getOutputSets()[i], getLogEvents());
			sb.append(" ]");
		}
		return sb.toString();
	}

	private void buildVisualPresentation(StringBuffer sb, HNSet set,
			LogEvents events) {

		HNSubSet subset = null;
		int element = 0;

		if (set != null) {

			for (int i = 0; i < set.size(); i++) {
				subset = set.get(i);
				sb.append("[");
				for (int j = 0; j < subset.size(); j++) {
					element = getDuplicatesMapping()[subset.get(j)];
					sb.append(" ").append(WME_NAME_DELIMITER).append(
							events.getEvent(element).getModelElementName())
							.append(" (").append(
									events.getEvent(element).getEventType())
							.append(")").append(WME_NAME_DELIMITER);
				}
				sb.append(" ]");
			}
		} else {
			sb.append("null");
		}
	}

	public void writeToDotWithoutSplitJoinSemantics(Writer bw)
			throws IOException {
		// correcting individual for visual presentation
		// two individuals with different genotype can have the same phenotype
		// HeuristicsNet phenotype =
		// MethodsOverIndividuals.removeDanglingElementReferences((HeuristicsNet)this.clone());

		DecimalFormat dec = new DecimalFormat("#.###");

		bw.write("digraph G {\n");
		bw.write("  size=\"6,10\"; fontname=\"Arial\"; fontsize=\"12\";\n");
		bw
				.write("  node [shape=\"box\",fontname=\"Arial\",fontsize=\"12\"];\n");
		// bw.write("  node [shape=\"rect\",fontname=\"Arial\",fontsize=\"12\",style=filled, fillcolor=red];\n");

		// write nodes

		for (int i = 0; i < size(); i++) {
			helpString = "E" + i + " [label=\"";
			helpString = helpString
					+ getLogEvents().getEvent(i).getModelElementName().replace(
							'"', '\''); // event name
			helpString = helpString
					+ "\\n("
					+ getLogEvents().getEvent(i).getEventType().replace('"',
							'\'') + ")"; // event type
			helpString = helpString + "\\n"
					+ getLogEvents().getEvent(i).getOccurrenceCount(); // #
			// occurrence
			helpString = helpString + "\"];\n";
			bw.write(helpString);
		}

		// write edges
		for (int from = 0; from < size(); from++) {
			// Iterator set =
			// phenotype.getAllElementsOutputSet(from).iterator();
			HNSubSet set = getAllElementsOutputSet(from);
			for (int iSet = 0; iSet < set.size(); iSet++) {
				int to = set.get(iSet);
				helpString = "E" + from + " -> E" + to
						+ " [style=\"filled\", label=\"";
				helpString = helpString + "\\n "
						+ dec.format(dependencyMeasures.get(from, to));
				// hulpString = hulpString + "\\n " + ((int)
				// directSuccessionCount.get(from, to));
				helpString = helpString + "\\n "
						+ ((int) getArcUsage().get(from, to));
				helpString = helpString + "\"];\n";
				bw.write(helpString);
			}
		}

		bw.write("}\n");
	}

	public void writeToDotWithSplitJoinSemantics(Writer bw) throws IOException {
		// correcting individual for visual presentation
		// two individuals with different genotype can have the same phenotype
		// HeuristicsNet phenotype =
		// MethodsOverIndividuals.removeDanglingElementReferences((HeuristicsNet)this.clone());
		bw.write("digraph G {\n");
		bw.write("  size=\"6,10\"; fontname=\"Arial\"; fontsize=\"12\";\n");
		bw
				.write("  node [shape=\"record\",fontname=\"Arial\",fontsize=\"12\"];\n");
		// bw.write("  node [shape=\"rect\",fontname=\"Arial\",fontsize=\"12\",style=filled, fillcolor=red];\n");
		// write nodes
		for (int i = 0; i < size(); i++) {
			if ((getInputSet(i).size() > 0) || (getOutputSet(i).size() > 0)) {
				bw.write("E" + i + " [label=\"{");
				if (getInputSet(i).size() > 0) {
					bw.write("{");
					for (int j = 0; j < getInputSet(i).size(); j++) {
						if (j > 0) {
							bw.write(" | and | ");
						}
						bw.write("<"
								+ toInputDotName(getInputSet(i).get(j)
										.toString()) + ">  XOR ");
					}
					bw.write("} | ");
				}
				bw.write(getLogEvents().getEvent(getDuplicatesMapping()[i])
						.getModelElementName().replace('"', '\'')
						+ " \\n("
						+ getLogEvents().getEvent(getDuplicatesMapping()[i])
								.getEventType().replace('"', '\'')
						+ ")\\n "
						+ getDuplicatesActualFiring()[i]);
				if (getOutputSet(i).size() > 0) {
					bw.write(" | {");
					for (int j = 0; j < getOutputSet(i).size(); j++) {
						if (j > 0) {
							bw.write(" | and | ");
						}
						bw.write("<"
								+ toOutputDotName(getOutputSet(i).get(j)
										.toString()) + "> XOR ");
					}
					bw.write("}");
				}

				bw.write("}}\"];\n");
			}
		}

		// write edges
		DecimalFormat dec = new DecimalFormat("#.###");

		for (int from = 0; from < size(); from++) {
			// Iterator set =
			// phenotype.getAllElementsOutputSet(from).iterator();
			for (int outSubsetIndex = 0; outSubsetIndex < getOutputSet(from)
					.size(); outSubsetIndex++) {
				HNSubSet outSubset = getOutputSet(from).get(outSubsetIndex);
				for (int outSubsetElementIndex = 0; outSubsetElementIndex < outSubset
						.size(); outSubsetElementIndex++) {
					int to = outSubset.get(outSubsetElementIndex);
					HNSet inputSubsetsElementWithFrom = getInputSetsWithElement(
							to, from);
					for (int k = 0; k < inputSubsetsElementWithFrom.size(); k++) {
						bw.write("E"
								+ from
								+ ":"
								+ toOutputDotName(outSubset.toString())
								+ " -> E"
								+ to
								+ ":"
								+ toInputDotName(inputSubsetsElementWithFrom
										.get(k).toString())
								+ " [style=\"filled\", label=\"  " + "\\n "
								+ dec.format(dependencyMeasures.get(from, to))
								+ "\\n " + (int) getArcUsage().get(from, to)
								+ "\"];\n");
					}
				}
			}
		}
		bw.write("}\n");
	}

}
