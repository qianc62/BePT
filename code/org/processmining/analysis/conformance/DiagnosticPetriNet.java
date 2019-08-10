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

import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedPetriNet;

/**
 * This class is used to enhance the petri net model with conformance check
 * results. Derive subclasses to create custom visualizations of these.
 * 
 * @author arozinat
 */
public class DiagnosticPetriNet extends ReplayedPetriNet {

	/**
	 * The log reader is necessary as soon as the log perspective is to be
	 * visualized. TODO - think about how this can be done in better way
	 * (writeToDot mechanism must be there)
	 */
	protected DiagnosticLogReader myReplayedLog;

	/**
	 * Constructs a DiagnosticPetriNet out of an ordinary one.
	 * 
	 * @param net
	 *            the Petri net that is re-established in the replayed net
	 * @param caseIDs
	 *            a list of strings containing the IDs of those instances that
	 *            want to store diagnostic information
	 */
	public DiagnosticPetriNet(PetriNet net, ArrayList caseIDs,
			DiagnosticLogReader replayedLog) {
		super(net, caseIDs);
		myReplayedLog = replayedLog;
	}

	/**
	 * Copy Constructor mimics the clone method and will be called by sub
	 * classes when the visualization state has changed in
	 * ConformanceCheckResult. Note that a deep copy with respect to the Petri
	 * net elements is created, i.e., the places, transitions and edges will be
	 * different objects afterwards. Nevertheless, the list of selected
	 * instances and the associated diagnostic log are reassigned and do not get
	 * cloned.
	 * 
	 * @param copyTemplate
	 *            The Petri net containing all the diagnostic information that
	 *            should be preserved.
	 */
	public DiagnosticPetriNet(DiagnosticPetriNet copyTemplate) {
		super(copyTemplate);
		// keep also the selection status
		myReplayedLog = copyTemplate.myReplayedLog;
	}

	/**
	 * {@inheritDoc} Produces a {@link DiagnosticTransition
	 * DiagnosticTransition}.
	 */
	protected Transition makeTransition(Transition template,
			ReplayedPetriNet targetNet, ArrayList caseIDs) {

		return new DiagnosticTransition(template, targetNet, caseIDs);
	}

	/**
	 * {@inheritDoc} Produces a {@link DiagnosticPlace DiagnosticPlace}.
	 */
	protected Place makePlace(Place template, ReplayedPetriNet targetNet,
			ArrayList caseIDs) {

		return new DiagnosticPlace(template, targetNet, caseIDs);
	}

	/**
	 * {@inheritDoc} Produces a {@link DiagnosticPNEdge DiagnosticPNEdge}.
	 */
	protected PNEdge makeEdge(PNEdge template, Place sourceNode,
			Transition targetNode, ReplayedPetriNet targetNet, ArrayList caseIDs) {

		return new DiagnosticPNEdge((DiagnosticPlace) sourceNode,
				(DiagnosticTransition) targetNode, caseIDs);
	}

	/**
	 * {@inheritDoc} Produces a {@link DiagnosticPNEdge DiagnosticPNEdge}.
	 */
	protected PNEdge makeEdge(PNEdge template, Transition sourceNode,
			Place targetNode, ReplayedPetriNet targetNet, ArrayList caseIDs) {

		return new DiagnosticPNEdge((DiagnosticTransition) sourceNode,
				(DiagnosticPlace) targetNode, caseIDs);
	}

	// /////////////////// METRICS RELATED METHODS
	// ///////////////////////////////

	/**
	 * Determine the amount of all missing tokens along all the log replay.
	 * 
	 * @return The amount of missing tokens (sum up for all places in the
	 *         model).
	 */
	public int getMissingTokens() {
		Iterator myPlaces = this.getPlaces().iterator();
		int requestedAmount = 0;
		// step through all places
		while (myPlaces.hasNext()) {
			DiagnosticPlace currentPlace = (DiagnosticPlace) myPlaces.next();
			requestedAmount = requestedAmount + currentPlace.getMissingTokens();
		}
		return requestedAmount;
	}

	/**
	 * Determine the amount of all remaining tokens along all the log replay.
	 * 
	 * @return The amount of remaining tokens for log trace pi (sum up for all
	 *         places in the model).
	 */
	public int getRemainingTokens() {
		Iterator myPlaces = this.getPlaces().iterator();
		int requestedAmount = 0;
		// step through all places
		while (myPlaces.hasNext()) {
			DiagnosticPlace currentPlace = (DiagnosticPlace) myPlaces.next();
			requestedAmount = requestedAmount
					+ currentPlace.getRemainingTokens();
		}
		return requestedAmount;
	}
}
