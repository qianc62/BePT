/**
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *    Copyright (c) 2003-2006 TU/e Eindhoven
 *    by Eindhoven University of Technology
 *    Department of Information Systems
 *    http://is.tm.tue.nl
 *
 ************************************************************************/

package org.processmining.analysis.decisionmining;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedPetriNet;
import org.processmining.framework.ui.Message;

public class DecisionPointVisualization extends ReplayedPetriNet {

	/**
	 * Holds the place belonging to the decision point currently to be
	 * displayed. The public attribute is necessary because the writeToDot()
	 * method has a fixed interface.
	 */
	public Place toVisualize = null;

	/**
	 * Holds the transitions belonging to the decision class currently to be
	 * displayed. The public attribute is necessary because the writeToDot()
	 * method has a fixed interface.
	 */
	public ArrayList currentDecisionClass = null;

	/**
	 * Holds the transitions belonging to the current attribute selection scope.
	 * The public attribute is necessary because the writeToDot() method has a
	 * fixed interface.
	 */
	public ArrayList attributeSelectionScope = null;

	/**
	 * Create a Petri net with decision point visualization.
	 * 
	 * @param net
	 *            the Petri net to be visualized
	 */
	public DecisionPointVisualization(PetriNet net) {
		super(net, null);
	}

	/**
	 * This is the transition writing part of the writeToDot procedure (refer to
	 * it for further information), which gets affected if the
	 * pathCoverageOption or the failedTransitionsOption evaluate to true.
	 * 
	 * @param bw
	 *            The writer used by the framework to create the temporary dot
	 *            file.
	 * @throws IOException
	 *             If writing to the writer fails.
	 */
	protected void writeTransitionsToDot(Writer bw) throws IOException {
		if (currentDecisionClass == null && attributeSelectionScope == null) {
			// use default
			super.writeTransitionsToDot(bw);
		} else {
			// indicate diagnostics
			Iterator it = this.getTransitions().iterator();
			while (it.hasNext()) {
				Transition currentTrans = (Transition) (it.next());
				String label = currentTrans.getIdentifier();
				try {
					// both fill with color and mark with different line color,
					// if trasition was fired and failed execution
					if ((currentDecisionClass != null)
							&& (currentDecisionClass.contains(currentTrans) == true)
							&& (attributeSelectionScope != null)
							&& (attributeSelectionScope.contains(currentTrans) == true)) {
						bw
								.write("t"
										+ currentTrans.getNumber()
										+ " [shape=\"box\",style=\"filled,bold\",color=\"darkgreen\",fillcolor=\"orange1\",label=\""
										+ label
										+ "\""
										+ (currentTrans.getLogEvent() != null ? ""
												: "style=\"filled\"") + "];\n");
					}
					// mark only with different line color
					else if ((attributeSelectionScope != null)
							&& (attributeSelectionScope.contains(currentTrans) == true)) {
						bw
								.write("t"
										+ currentTrans.getNumber()
										+ " [shape=\"box\",style=\"bold\",color=\"darkgreen\",label=\""
										+ label
										+ "\""
										+ (currentTrans.getLogEvent() != null ? ""
												: "style=\"filled\"") + "];\n");
					}
					// mark only with different fill color
					else if ((currentDecisionClass != null)
							&& (currentDecisionClass.contains(currentTrans) == true)) {
						bw
								.write("t"
										+ currentTrans.getNumber()
										+ " [shape=\"box\",style=\"filled\",fillcolor=\"orange1\",label=\""
										+ label
										+ "\""
										+ (currentTrans.getLogEvent() != null ? ""
												: "style=\"filled\"") + "];\n");
					}
					// write like normally
					else {
						bw.write("t"
								+ currentTrans.getNumber()
								+ " [shape=\"box\",label=\""
								+ label
								+ "\""
								+ (currentTrans.getLogEvent() != null ? ""
										: "style=\"filled\"") + "];\n");
					}
					// connect Petri net nodes to later grappa components
					nodeMapping.put(new String("t" + currentTrans.getNumber()),
							currentTrans);
				} catch (Exception ex) {
					Message.add("Failure while updating the visualization.\n"
							+ ex.toString(), 2);
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * This is the finishing part of the writeToDot procedure (refer to it for
	 * further information), which gets affected if the
	 * remainingTransitionsOption evaluates to true.
	 * 
	 * @param bw
	 *            The writer used by the framework to create the temporary dot
	 *            file.
	 * @throws IOException
	 *             If writing to the writer fails.
	 */
	protected void finishDotWriting(Writer bw) throws IOException {
		// only change visualization if a decision point has been chosen
		if (toVisualize == null) {
			// use default
			super.finishDotWriting(bw);
		} else {
			// indicate decision point via a cluster node containing the place
			// and the succeeding transitions
			String clusterContent = "p" + toVisualize.getNumber() + ";\n";
			Iterator succeedingTransitions = toVisualize.getSuccessors()
					.iterator();
			while (succeedingTransitions.hasNext()) {
				Transition successor = (Transition) succeedingTransitions
						.next();
				clusterContent = clusterContent + "t" + successor.getNumber()
						+ ";\n";
			}
			bw.write("subgraph cluster_Place_" + toVisualize.getNumber()
					+ " {\nstyle=\"filled\";\ncolor=\"lightgrey\";\n");
			// put transitions into subgraph
			bw.write(clusterContent);
			// close the subgraph
			bw.write("}\n");
			// close graph
			bw.write("}\n");
		}
	}
}
