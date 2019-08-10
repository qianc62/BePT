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

/*
 * Copyright (c) 2006 Eindhoven University of Technology
 * All rights reserved.
 */

package org.processmining.analysis.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedEdge;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;

/**
 * This class is used to enhance edges with performance information, e.g.
 * routing probabilities, which are to be stored during log replay and will be
 * displayed in the extended Petri net.
 * 
 * @see ExtendedPetriNet
 * @see ExtendedTransition
 * @see ExtendedPlace
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */

public class ExtendedPNEdge extends ReplayedEdge {

	// used to keep track of the process instances that visit this edge
	private ArrayList visitors = new ArrayList();

	/**
	 * Constructor creates an edge from the given place to the given transition.
	 * 
	 * @param place
	 *            the place to be connected to this arc
	 * @param trans
	 *            the transition to be connected to this arc
	 * @param caseIDs
	 *            the list of IDs for those process instance that want to be
	 *            able to store results of the replay analysis in this extended
	 *            edge
	 */
	public ExtendedPNEdge(ExtendedPlace place, ExtendedTransition trans,
			ArrayList caseIDs) {
		super(place, trans, caseIDs);
	}

	/**
	 * Constructor creates an edge from the given transition to the given place.
	 * 
	 * @param trans
	 *            the transition to be connected to this arc
	 * @param place
	 *            the place to be connected to this arc
	 * @param caseIDs
	 *            the list of IDs for those process instance that want to be
	 *            able to store results of the replay analysis in this extended
	 *            edge
	 */
	public ExtendedPNEdge(ExtendedTransition trans, ExtendedPlace place,
			ArrayList caseIDs) {
		super(trans, place, caseIDs);
	}

	/**
	 * Method to keep track of the number of times this edge is traversed in
	 * each process instance.
	 * 
	 * @param extendedTrace
	 *            ReplayedLogTrace: process instance in which this measurement
	 *            is taken
	 */
	public void recordProbMeasurement(ReplayedLogTrace extendedTrace) {
		String piName = extendedTrace.getName();
		visitors.add(piName);
	}

	/**
	 * Returns the probability that this edge is chosen by a random token
	 * 
	 * @param piList
	 *            ArrayList: process instances on which calculation of
	 *            probability is based
	 * @param totalFrequency
	 *            long: the total frequency of tokens that are counted in
	 *            calculation of probability
	 * @param fitOption
	 *            int: the fit option used (how to deal with non-conformance)
	 * @param failedInstances
	 *            HashSet: instances that have not executed successfully or
	 *            properly
	 * @return double
	 */
	public double getProbability(ArrayList piList, long totalFrequency,
			int fitOption, HashSet failedInstances) {
		double prob = 0;
		long number = getFrequency(piList, fitOption, failedInstances);
		if (totalFrequency != 0) {
			prob = (number * 1.0) / totalFrequency;
		} else {
			prob = 0.0;
		}
		return prob;
	}

	/**
	 * Returns the frequency of tokens that have traversed this edge during log
	 * replay
	 * 
	 * @param piList
	 *            ArrayList: process instances on which calculation of frequency
	 *            is based
	 * @param fitOption
	 *            int: the fit option used (how to deal with non-conformance)
	 * @param failedInstances
	 *            HashSet: set of instances that have not finished execution in
	 *            a regular fashion
	 * @return int
	 */
	public int getFrequency(ArrayList piList, int fitOption,
			HashSet failedInstances) {
		String[] names = (String[]) piList.toArray(new String[0]);
		Arrays.sort(names);

		Iterator it = visitors.iterator();
		int number = 0;
		while (it.hasNext()) {
			String piName = (String) it.next();
			// check whether piName is contained in names
			if (Arrays.binarySearch(names, piName) <= -1) {
				continue;
			}
			if (fitOption == 0) {
				// based on all traces
				number++;
			} else if (fitOption == 1) {
				// based on measurements taken before traces fail
				boolean isUnaffected = true;

				if (this.getDest() instanceof ExtendedTransition) {
					if (((ExtendedTransition) this.getDest())
							.hasFailedBefore(piName)) {
						isUnaffected = false;
					}
				}
				if (isUnaffected) {
					number++;
				}
			} else if (fitOption == 2) {
				// based on measurements taken in traces where no transitions,
				// related to the XOR-split (the input place) fail.
				boolean isUnaffected = true;
				if (this.getSource() instanceof ExtendedPlace) {
					ArrayList transitions = ((ExtendedPlace) this.getSource())
							.getRelatedTransitions();
					ListIterator lit = transitions.listIterator();
					while (lit.hasNext() && isUnaffected) {
						if (((ExtendedTransition) lit.next())
								.hasFailedExecution(piName)) {
							isUnaffected = false;
						}
					}
					if (isUnaffected) {
						number++;
					}
				}
			} else if (fitOption == 3 && !failedInstances.contains(piName)) {
				number++;
			}
		}
		return number;
	}

}
