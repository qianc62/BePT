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

package org.processmining.framework.models.petrinet.algorithms.logReplay;

import java.util.*;

import org.processmining.framework.models.petrinet.*;

/**
 * A place involved in the log replay analysis method.
 * 
 * @see ReplayedPetriNet
 * @see ReplayedTransition
 * @see LogReplayAnalysisMethod
 * 
 * @author Anne Rozinat (a.rozinat@tm.tue.nl)
 */
public class ReplayedPlace extends Place {

	/**
	 * The constructor creates a normal place but additionally initializes the
	 * diagnostic data structures.
	 * 
	 * @param template
	 *            place (whose identifier is to be passed to super class)
	 * @param net
	 *            the Petri net it belongs to (to be passed to super class)
	 * @param caseIDs
	 *            a list of strings, containing the IDs of those cases for which
	 *            we want to store diagnostic results in deriving subclasses
	 *            (see {@link #initDiagnosticDataStructures
	 *            initDiagnosticDataStructures})
	 */
	public ReplayedPlace(Place template, PetriNet net, ArrayList caseIDs) {
		super(template.getIdentifier(), net);
		// so deriving subclasses only need to override this init method
		initDiagnosticDataStructures(caseIDs);
	}

	// /////// METHODS TO BE OVERRIDDEN IN SUBCLASSES //////////

	/**
	 * Initializes the diagnostic data structures. Does nothing per default (to
	 * be overridden in subclasses if necessary).
	 * 
	 * @param caseIDs
	 *            a list of strings, containing the IDs of those cases for which
	 *            we want to store diagnostic results in deriving subclasses
	 */
	protected void initDiagnosticDataStructures(ArrayList caseIDs) {
		// do nothing per default
	}

	/**
	 * Records some time-stamp related measurements for this place. Note that
	 * this may happen several times during the replay of one single trace if
	 * there are loops in the process model. Does nothing per default (to be
	 * overridden in subclasses if necessary).
	 * 
	 * @param tokenAvailable
	 *            the time at which the token became available in this place
	 * @param transEnabled
	 *            the time at which the succeeding transition becomes enabled
	 *            (which is only different from tokenAvailable if this place is
	 *            involved in a synchronization)
	 * @param transFired
	 *            the time at which the token was consumed from this place
	 * @param trace
	 *            ReplayedLogTrace Process Instance currently replayed
	 */
	public void recordTimeMeasurement(Date tokenAvailable, Date transEnabled,
			Date transFired, ReplayedLogTrace trace) {
		// do nothing per default
	}

	/**
	 * set the transition that put a token in this place
	 * 
	 * @param trans
	 *            ReplayedTransition
	 */
	public void setEnablingTransition(ReplayedTransition trans) {
		// do nothing per default
	}

	/**
	 * Removes the last transition that put a token in this place.
	 * 
	 * @param endTime
	 *            Date time/date at which it is removed
	 * @param trace
	 *            ReplayedLogTrace Process instance in which it is removed
	 * @param rt
	 *            ReplayedTransition The transition from which this method is
	 *            called
	 */
	public void removeEnablingTransition(Date endTime, ReplayedLogTrace trace,
			ReplayedTransition rt) {
		// do nothing per default
	}
}
