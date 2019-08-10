package org.processmining.analysis.conformance;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.Message;

/**
 * Overrides the default Petri net visualization to add structural
 * appropriateness diagnostics.
 * 
 * @author arozinat
 */
public class StrAppropriatenessVisualization extends DiagnosticPetriNet {

	/**
	 * Represents the visualization option of indicating those activities that
	 * always followed each other. The public attribute is necessary because the
	 * writeToDot() method has a fixed interface.
	 */
	public boolean redundantInvisiblesOption = false;

	/**
	 * Represents the visualization option of indicating those activities that
	 * never followed each other. The public attribute is necessary because the
	 * writeToDot() method has a fixed interface.
	 */
	public boolean alternativeDuplicatesOption = false;

	/**
	 * Creates a diagnostic Petri net with successorship relation visualization.
	 * 
	 * @param net
	 *            the Petri net to be passed to super
	 * @param caseIDs
	 *            the trace IDs to be passed to super
	 */
	public StrAppropriatenessVisualization(PetriNet net, ArrayList caseIDs,
			DiagnosticLogReader replayedLog) {
		super(net, caseIDs, replayedLog);
	}

	/**
	 * Creates a diagnostic Petri net with successorship relation visualization.
	 * The copy constructor is used to change the visualization but to keep all
	 * the diagnostic results.
	 * 
	 * @param copyTemplate
	 *            The Petri net containing all the diagnostic information that
	 *            should be preserved (to be passed to super).
	 */
	public StrAppropriatenessVisualization(DiagnosticPetriNet copyTemplate) {
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
		if (redundantInvisiblesOption == false
				&& alternativeDuplicatesOption == false) {
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
					// mark with different fill color
					if (redundantInvisiblesOption == true
							&& myDiagnosticTransition
									.isRedundantInvisibleTask()) {
						bw
								.write("t"
										+ myDiagnosticTransition.getNumber()
										+ " [shape=\"box\",style=\"filled\",color=\"red\",label=\""
										+ label
										+ "\""
										+ (myDiagnosticTransition.getLogEvent() != null ? ""
												: "style=\"filled\"") + "];\n");
					}
					// mark with different fill color
					else if (alternativeDuplicatesOption == true
							&& myDiagnosticTransition
									.isAlternativeDuplicateTask()) {
						bw
								.write("t"
										+ myDiagnosticTransition.getNumber()
										+ " [shape=\"box\",style=\"filled\",fillcolor=\"lightblue\",label=\""
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
}
