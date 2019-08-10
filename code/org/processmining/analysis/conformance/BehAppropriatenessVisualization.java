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
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.ui.Message;

/**
 * Overrides the default Petri net visualization to add behavioral
 * appropriateness diagnostics.
 * 
 * @author arozinat
 */
public class BehAppropriatenessVisualization extends DiagnosticPetriNet {

	/**
	 * Represents the visualization option of indicating those activities that
	 * always followed each other. The public attribute is necessary because the
	 * writeToDot() method has a fixed interface.
	 */
	public boolean alwaysFollowsOption = false;

	/**
	 * Represents the visualization option of indicating those activities that
	 * never followed each other. The public attribute is necessary because the
	 * writeToDot() method has a fixed interface.
	 */
	public boolean neverFollowsOption = false;

	/**
	 * Represents the visualization option of indicating those activities that
	 * always preceded each other. The public attribute is necessary because the
	 * writeToDot() method has a fixed interface.
	 */
	public boolean alwaysPrecedesOption = false;

	/**
	 * Represents the visualization option of indicating those activities that
	 * never preceded each other. The public attribute is necessary because the
	 * writeToDot() method has a fixed interface.
	 */
	public boolean neverPrecedesOption = false;

	/**
	 * Creates a diagnostic Petri net with successorship relation visualization.
	 * 
	 * @param net
	 *            the Petri net to be passed to super
	 * @param caseIDs
	 *            the trace IDs to be passed to super
	 */
	public BehAppropriatenessVisualization(PetriNet net, ArrayList caseIDs,
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
	public BehAppropriatenessVisualization(DiagnosticPetriNet copyTemplate) {
		super(copyTemplate);
	}

	// /////// VISUALIZATION SECTION //////////

	/**
	 * Adds diagnostic edges to the model view (indicating the global
	 * successorship and predecessorship relations).
	 */
	protected void finishDotWriting(Writer bw) throws IOException {
		// only token counter option affects the places
		if (alwaysFollowsOption == false && neverFollowsOption == false
				&& alwaysPrecedesOption == false
				&& neverPrecedesOption == false) {
			// use default
			super.finishDotWriting(bw);
		} else {
			// indicate diagnostics
			try {
				Iterator<Transition> it = getTransitions().iterator();
				while (it.hasNext()) {

					DiagnosticTransition currentTrans = (DiagnosticTransition) it
							.next();
					// check for always follows relation
					if (alwaysFollowsOption == true
							&& currentTrans.getAlwaysFollows().size() > 0) {
						// init edge type and write diagnostic edges
						bw.write("edge [color=red,style=bold];");
						Iterator<Transition> alwaysFollower = currentTrans
								.getAlwaysFollows().iterator();
						while (alwaysFollower.hasNext()) {
							Transition current = alwaysFollower.next();
							bw.write("t" + currentTrans.getNumber() + " -> t"
									+ current.getNumber()
									+ " [label=\"Always follows\"];\n");
						}
					}
					// check for never follows relation
					if (neverFollowsOption == true
							&& currentTrans.getNeverFollows().size() > 0) {
						// init edge type and write diagnostic edges
						bw.write("edge [color=blue,style=bold];");
						Iterator<Transition> neverFollower = currentTrans
								.getNeverFollows().iterator();
						while (neverFollower.hasNext()) {
							Transition current = neverFollower.next();
							bw.write("t" + currentTrans.getNumber() + " -> t"
									+ current.getNumber()
									+ " [label=\"Never follows\"];\n");
						}
					}
					// check for always precedes relation
					if (alwaysPrecedesOption == true
							&& currentTrans.getAlwaysPrecedes().size() > 0) {
						// init edge type and write diagnostic edges
						bw.write("edge [color=red,style=dotted];");
						Iterator<Transition> alwaysPrecedent = currentTrans
								.getAlwaysPrecedes().iterator();
						while (alwaysPrecedent.hasNext()) {
							Transition current = alwaysPrecedent.next();
							bw.write("t" + currentTrans.getNumber() + " -> t"
									+ current.getNumber()
									+ " [label=\"Always precedes\"];\n");
						}
					}
					// check for never precedes relation
					if (neverPrecedesOption == true
							&& currentTrans.getNeverPrecedes().size() > 0) {
						// init edge type and write diagnostic edges
						bw.write("edge [color=blue,style=dotted];");
						Iterator<Transition> neverPrecedent = currentTrans
								.getNeverPrecedes().iterator();
						while (neverPrecedent.hasNext()) {
							Transition current = neverPrecedent.next();
							bw.write("t" + currentTrans.getNumber() + " -> t"
									+ current.getNumber()
									+ " [label=\"Never precedes\"];\n");
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
