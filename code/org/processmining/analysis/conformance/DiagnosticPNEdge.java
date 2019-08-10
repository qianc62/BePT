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

import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedEdge;

/**
 * The diagnostic edge can be enhanced by diagnostic information to be stored
 * during log replay and will be used to form a part of the diagnostic Petri
 * net.
 * 
 * @see DiagnosticPetriNet
 * @see DiagnosticTransition
 * @see DiagnosticPlace
 * 
 * @author arozinat
 */
public class DiagnosticPNEdge extends ReplayedEdge {

	/**
	 * Data structure for storage of the number of times this arc has been
	 * followed during log replay.
	 */
	public int numberOfPasses;

	/**
	 * Constructor creates an edge from the given transition to the given place.
	 * 
	 * @param trans
	 *            the transition to be connected to this arc
	 * @param place
	 *            the place to be connected to this arc
	 * @param caseIDs
	 *            the list of IDs for those process instance that want to be
	 *            able to store results of the replay analysis in this
	 *            diagnostic edge
	 */
	public DiagnosticPNEdge(DiagnosticTransition trans, DiagnosticPlace place,
			ArrayList caseIDs) {
		super(trans, place, caseIDs);
	}

	/**
	 * Constructor creates an edge from the given place to the given transition.
	 * 
	 * @param place
	 *            the place to be connected to this arc
	 * @param trans
	 *            the transition to be connected to this arc
	 * @param caseIDs
	 *            the list of IDs for those process instance that want to be
	 *            able to store results of the replay analysis in this
	 *            diagnostic edge
	 */
	public DiagnosticPNEdge(DiagnosticPlace place, DiagnosticTransition trans,
			ArrayList caseIDs) {
		super(place, trans, caseIDs);
	}

	// /////// WRITE ACCESS METHODS FOR DIAGNOSTIC INFORMATION //////////

	/**
	 * Initializes the diagnostic data structures.
	 * 
	 * @param caseIDs
	 *            the list of IDs for those process instance that want to be
	 *            able to store results of the replay analysis in this
	 *            diagnostic edge
	 */
	protected void initDiagnosticDataStructures(ArrayList caseIDs) {
		// numberOfPasses = new HashMap();
		// Iterator checkedInstances = caseIDs.iterator();
		// while (checkedInstances.hasNext()) {
		// // use case id of process instance (pi) for hash map key
		// // (because the log reader always creates a new process instance
		// object)
		// String pi = (String) checkedInstances.next();
		// numberOfPasses.put(pi, new Integer(0));
		// }
	}

	/**
	 * Increment the corresponding numberOfPasses entry by specified value.
	 * 
	 * @param value
	 *            corresponds to the number of similar process instances
	 */
	public void incrementNumberOfPasses(int value) {
		numberOfPasses = numberOfPasses + value;
	}

	// /////// READ ACCESS METHODS FOR DIAGNOSTIC INFORMATION //////////

	/**
	 * Determine the number of passes for the this edge.
	 * 
	 * @return the number of times passing this edge during log replay for this
	 *         edge
	 */
	public int getNumberOfPasses() {
		return numberOfPasses;
	}

	// //////////////// GENERAL PURPOSE METHODS
	// /////////////////////////////////

	/**
	 * Makes a deep copy of the object. Note that this method needs to be
	 * extended as soon as there are attributes added to the class which are not
	 * primitive or immutable.
	 * 
	 * @return Object the cloned object
	 */
	public Object clone() {
		DiagnosticPNEdge o = null;
		o = (DiagnosticPNEdge) super.clone();
		// clone referenced objects to realize deep copy
		// if (numberOfPasses != null) {
		// o.numberOfPasses = (HashMap) numberOfPasses.clone();
		// }
		return o;
	}
}
