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
 * An edge involved in the log replay analysis method.
 * 
 * @see ReplayedPetriNet
 * @see ReplayedTransition
 * @see ReplayedPlace
 * @see LogReplayAnalysisMethod
 * 
 * @author Anne Rozinat (a.rozinat@tm.tue.nl)
 */
public class ReplayedEdge extends PNEdge {

	/**
	 * The constructor creates a normal PNEdge but additionally initializes the
	 * diagnostic data structures.
	 * 
	 * @param source
	 *            the transition to be connected to this arc as a source node
	 * @param target
	 *            the place to be connected to this arc as a target node
	 * @param caseIDs
	 *            a list of strings, containing the IDs of those cases for which
	 *            we want to store diagnostic results in deriving subclasses
	 *            (see {@link #initDiagnosticDataStructures
	 *            initDiagnosticDataStructures})
	 */
	public ReplayedEdge(Transition source, Place target, ArrayList caseIDs) {
		super(source, target);
		// so deriving subclasses only need to override this init method
		initDiagnosticDataStructures(caseIDs);
	}

	/**
	 * The constructor creates a normal PNEdge but additionally initializes the
	 * diagnostic data structures.
	 * 
	 * @param source
	 *            the place to be connected to this arc as a source node
	 * @param target
	 *            the transition to be connected to this arc as a target node
	 * @param caseIDs
	 *            a list of strings, containing the IDs of those cases for which
	 *            we want to store diagnostic results in deriving subclasses
	 *            (see {@link #initDiagnosticDataStructures
	 *            initDiagnosticDataStructures})
	 */
	public ReplayedEdge(Place source, Transition target, ArrayList caseIDs) {
		super(source, target);
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

	public void recordProbMeasurement(ReplayedLogTrace extendedTrace) {
		// do nothing per default
	}
}
