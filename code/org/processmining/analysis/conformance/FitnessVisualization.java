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

package org.processmining.analysis.conformance;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.ui.Message;

/**
 * Overrides the default visualization methods to include all the diagnostic
 * information being requested.
 * 
 * @author arozinat
 */
public class FitnessVisualization extends DiagnosticPetriNet {

	/**
	 * Represents the visualization option of indicating the token counter
	 * results in the model. The public attribute is necessary because the
	 * writeToDot() method has a fixed interface.
	 */
	public boolean tokenCouterOption = false;

	/**
	 * Represents the visualization option of indicating the tasks that have
	 * remained enabled in the model. The public attribute is necessary because
	 * the writeToDot() method has a fixed interface.
	 */
	public boolean remainingTransitionsOption = false;

	/**
	 * Represents the visualization option of indicating the tasks that have
	 * failed execution in the model. The public attribute is necessary because
	 * the writeToDot() method has a fixed interface.
	 */
	public boolean failedTransitionsOption = false;

	/**
	 * Represents the visualization option of indicating the tasks that have
	 * been executed in the model. The public attribute is necessary because the
	 * writeToDot() method has a fixed interface.
	 */
	public boolean pathCoverageOption = false;

	/**
	 * Represents the visualization option of indicating the number of times
	 * that an edge has been passed during log replay. The public attribute is
	 * necessary because the writeToDot() method has a fixed interface.
	 */
	public boolean passedEdgesOption = false;

	/**
	 * Create a diagnostic Petri net with path coverage visualization.
	 * 
	 * @param net
	 *            the Petri net to be passed to super
	 * @param caseIDs
	 *            the trace IDs to be passed to super
	 */
	public FitnessVisualization(PetriNet net, ArrayList caseIDs,
			DiagnosticLogReader replayedLog) {
		super(net, caseIDs, replayedLog);
	}

	/**
	 * Create a diagnostic Petri net with path coverage visualization. The copy
	 * constructor is used to change the visualization but to keep all the
	 * diagnostic results.
	 * 
	 * @param copyTemplate
	 *            The Petri net containing all the diagnostic information that
	 *            should be preserved (to be passed to super).
	 */
	public FitnessVisualization(DiagnosticPetriNet copyTemplate) {
		super(copyTemplate);
	}

	// /////// VISUALIZATION SECTION //////////

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
		// only token counter option affects the places
		if (pathCoverageOption == false && failedTransitionsOption == false) {
			// use default
			super.writeTransitionsToDot(bw);
		} else {
			// indicate diagnostics
			Iterator it = this.getTransitions().iterator();
			while (it.hasNext()) {
				DiagnosticTransition myDiagnosticTransition = (DiagnosticTransition) (it
						.next());
				String label = myDiagnosticTransition.getIdentifier();
				try {
					// both fill with color and mark with different line color,
					// if trasition was fired and failed execution
					if ((pathCoverageOption == true)
							&& (failedTransitionsOption == true)
							&& (myDiagnosticTransition.hasFired() == true)
							&& (myDiagnosticTransition.hasFailedExecution() == true)) {
						bw
								.write("t"
										+ myDiagnosticTransition.getNumber()
										+ " [shape=\"box\",style=\"filled,bold\",color=\"darkgreen\",fillcolor=\"orange1\",label=\""
										+ label
										+ "\""
										+ (myDiagnosticTransition.getLogEvent() != null ? ""
												: "style=\"filled\"") + "];\n");
					}
					// mark only with different line color
					else if ((pathCoverageOption == true)
							&& (myDiagnosticTransition.hasFired() == true)) {
						bw
								.write("t"
										+ myDiagnosticTransition.getNumber()
										+ " [shape=\"box\",style=\"bold\",color=\"darkgreen\",label=\""
										+ label
										+ "\""
										+ (myDiagnosticTransition.getLogEvent() != null ? ""
												: "style=\"filled\"") + "];\n");
					}
					// mark only with different fill color
					else if ((failedTransitionsOption == true)
							&& (myDiagnosticTransition.hasFailedExecution() == true)) {
						bw
								.write("t"
										+ myDiagnosticTransition.getNumber()
										+ " [shape=\"box\",style=\"filled\",fillcolor=\"orange1\",label=\""
										+ label
										+ "\""
										+ (myDiagnosticTransition.getLogEvent() != null ? ""
												: "style=\"filled\"") + "];\n");
					}
					// write like normally
					else {
						bw
								.write("t"
										+ myDiagnosticTransition.getNumber()
										+ " [shape=\"box\",label=\""
										+ label
										+ "\""
										+ (myDiagnosticTransition.getLogEvent() != null ? ""
												: "style=\"filled\"") + "];\n");
					}
					// connect Petri net nodes to later grappa components
					nodeMapping.put(new String("t"
							+ myDiagnosticTransition.getNumber()),
							myDiagnosticTransition);
				} catch (Exception ex) {
					Message.add("Failure while updating the visualization.\n"
							+ ex.toString(), 2);
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * This is the place writing part of the writeToDot procedure (refer to it
	 * for further information), which gets affected if the tokenCounterOption
	 * evaluates to true.
	 * 
	 * @param bw
	 *            The writer used by the framework to create the temporary dot
	 *            file.
	 * @throws IOException
	 *             If writing to the writer fails.
	 */
	protected void writePlacesToDot(Writer bw) throws IOException {
		// only token counter option affects the places
		if (tokenCouterOption == false) {
			// use default
			super.writePlacesToDot(bw);
		} else {
			// indicate diagnostics
			try {
				Iterator it = this.getPlaces().iterator();
				while (it.hasNext()) {
					DiagnosticPlace p = (DiagnosticPlace) (it.next());
					// in the case that something went wrong, paint the place
					// red
					// and indicate the token surplus or shortage by numbers
					if ((p.getMissingTokensOnSemanticLevel() > 0)
							|| (p.getRemainingTokensOnSemanticLevel() > 0)) {
						// in case of missing AND remaining tokens
						if ((p.getMissingTokensOnSemanticLevel() > 0)
								&& (p.getRemainingTokensOnSemanticLevel() > 0)) {
							bw
									.write("p"
											+ p.getNumber()
											+ " [shape=\"circle\",color=\"red\",style=\"bold\",fontcolor=\"red\",label=\"+"
											+ p.getRemainingTokens() + "\\n-"
											+ p.getMissingTokens() + "\"];\n");
						} else if ((p.getMissingTokensOnSemanticLevel() > 0)) {
							// only tokens missing
							bw
									.write("p"
											+ p.getNumber()
											+ " [shape=\"circle\",color=\"red\",style=\"bold\",fontcolor=\"red\",label=\"-"
											+ p.getMissingTokens() + "\"];\n");
						} else {
							// only tokens remaining
							bw
									.write("p"
											+ p.getNumber()
											+ " [shape=\"circle\",color=\"red\",style=\"bold\",fontcolor=\"red\",label=\"+"
											+ p.getRemainingTokens() + "\"];\n");
						}
					} else {
						// paint the place like usually
						bw.write("p" + p.getNumber()
								+ " [shape=\"circle\",label=\"" + "\"];\n");
					}
					nodeMapping.put(new String("p" + p.getNumber()), p);
				}
			} catch (Exception ex) {
				Message.add("Failure while updating the visualization.\n"
						+ ex.toString(), 2);
				ex.printStackTrace();
			}
		}
	}

	/**
	 * This is the edge writing part of the {@link #writeToDot writeToDot}
	 * procedure. This part of the visualization gets affected as soon as the
	 * passedEdgesOption evaluates to true.
	 * 
	 * @param bw
	 *            the writer used by the framework to create the temporary dot
	 *            file
	 * @throws IOException
	 *             if writing to the writer fails
	 */
	protected void writeEdgesToDot(Writer bw) throws IOException {
		// only token counter option affects the places
		if (passedEdgesOption == false) {
			// use default
			super.writeEdgesToDot(bw);
		} else {
			try {
				Iterator it = this.getEdges().iterator();
				while (it.hasNext()) {
					DiagnosticPNEdge e = (DiagnosticPNEdge) (it.next());
					if (e.isPT()) {
						Place p = (Place) e.getSource();
						Transition t = (Transition) e.getDest();
						bw.write("p" + p.getNumber() + " -> t" + t.getNumber()
								+ " [label=" + e.getNumberOfPasses() + "];\n");
					} else {
						Place p = (Place) e.getDest();
						Transition t = (Transition) e.getSource();
						bw.write("t" + t.getNumber() + " -> p" + p.getNumber()
								+ " [label=" + e.getNumberOfPasses() + "];\n");
					}
				}
			} catch (Exception ex) {
				Message.add("Failure while updating the visualization.\n"
						+ ex.toString(), 2);
				ex.printStackTrace();
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
		// only token counter option affects the places
		if (remainingTransitionsOption == false) {
			// use default
			super.finishDotWriting(bw);
		} else {
			// indicate diagnostics
			try {
				Iterator it = this.getPlaces().iterator();
				while (it.hasNext()) {
					DiagnosticPlace p = (DiagnosticPlace) (it.next());
					if (p.getRemainingTokensOnSemanticLevel() > 0) {
						// get the transitions potentially remaining enabled
						Iterator potentiallyEnabled = p.getSuccessors()
								.iterator();
						String transitions = "";
						while (potentiallyEnabled.hasNext()) {
							DiagnosticTransition currentTransition = (DiagnosticTransition) potentiallyEnabled
									.next();
							if (currentTransition.hasRemainedEnabled()) {
								transitions = transitions + "t"
										+ currentTransition.getNumber() + ";\n";
							}
						}
						// create only visualization of remaining enabled
						// transitions, if there are any
						if (transitions != "") {
							bw
									.write("subgraph cluster_Place_"
											+ p.getNumber()
											+ " {\nstyle=\"filled\";\ncolor=\"lightgrey\";\n");
							// put transitions into subgraph
							bw.write(transitions);
							// close the subgraph
							bw.write("}\n");
						}
					}
				}
			} catch (Exception ex) {
				Message.add("Failure while updating the visualization.\n"
						+ ex.toString(), 2);
				ex.printStackTrace();
			}
			// close graph
			bw.write("}\n");
		}
	}
}
