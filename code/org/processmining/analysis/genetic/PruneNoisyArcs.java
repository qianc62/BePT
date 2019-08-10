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

package org.processmining.analysis.genetic;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.ProvidedObject;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * <p>
 * Title: Prune Noisy Arcs
 * </p>
 * <p>
 * Description: Prune the arcs of an individual. The pruning is based on how
 * often an arc is used while parsing a log. The arcs usage depends on the
 * selected fitness measurement.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class PruneNoisyArcs implements AnalysisPlugin {
	public PruneNoisyArcs() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getName() {
		return "Prune Arcs";
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("Individual") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasLog = false, hasNet = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof LogReader) {
						hasLog = true;
					}
					if (o[i] instanceof HeuristicsNet) {
						hasNet = true;
					}
				}
				return hasLog && hasNet;
			}
		} };
		return items;
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		HeuristicsNet net = null;
		LogReader log = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
			} else if (o[i] instanceof HeuristicsNet) {
				net = ((HeuristicsNet) o[i]).copyNet();
			}
		}

		return new PruneNoisyArcsUI(log, net);

	}

	public String getHtmlDescription() {
		return "<p> <b>Prune Arcs  Analysis Plug-in</b></p><p>This plug-in allows "
				+ "the user to prune arcs from a heuristics net. The pruning is based on "
				+ "a user set percentage. In short, only the arcs that are used above the given percentage "
				+ "are kept in the pruned heuristics net. The percentage is based on the maximum "
				+ "arc usage of the unpruned heuristics net for the selected fitness type.</p>";
	}

	/**
	 * Calculates the actual arc usage number that corresponds to a certain
	 * percentage.
	 * 
	 * @param net
	 *            HeuristicsNet net to be pruned.
	 * @param percentage
	 *            double pruning percentage. (min value = 0 and max value =
	 *            100).
	 * @return double arc usage number.
	 */
	public static double calculatePruningThreshold(HeuristicsNet net,
			double percentage) {

		// identifying the maximum arc usage
		double maxArcUsage = 0.0;
		DoubleMatrix2D arcUsage = net.getArcUsage();
		for (int row = 0; row < arcUsage.rows(); row++) {
			for (int column = 0; column < arcUsage.columns(); column++) {
				if (maxArcUsage < arcUsage.get(row, column)) {
					maxArcUsage = arcUsage.get(row, column);
				}
			}
		}

		return maxArcUsage * percentage / 100;
	}

	private void jbInit() throws Exception {
	}
}
