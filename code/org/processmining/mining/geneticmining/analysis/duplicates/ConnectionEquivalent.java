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

package org.processmining.mining.geneticmining.analysis.duplicates;

import mathCollection.HashMultiset;
import mathCollection.Multiset;

import org.processmining.framework.log.LogEvents;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;

/**
 * <p>
 * This class calculates the precision and recall of two
 * <code>HeuristicsNet</code> with respect to the connection between the tasks.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class ConnectionEquivalent implements PrecisionRecall {

	private double precision;
	private double recall;

	private HeuristicsNet baseHN;
	private HeuristicsNet foundHN;

	/**
	 * Creates an object of <code>TraceEquivalent.</code>
	 * 
	 * @param baseHN
	 *            HeuristicsNet base heuristics net. The precision and recall
	 *            consider this net as the correct solution.
	 * @param foundHN
	 *            HeuristicsNet found heuristis net. The precision and recall
	 *            compare this net to the base heuristics net.
	 */

	public ConnectionEquivalent(HeuristicsNet baseHN, HeuristicsNet foundHN)
			throws Exception {

		if (baseHN != null) {
			this.baseHN = baseHN;
		} else {
			throw new NullPointerException("Base heuristics net is null!");
		}

		if (foundHN != null) {
			this.foundHN = foundHN;
			// making the foundHN and the baseHN work on the same indeces for
			// the tasks/elements...
			try {
				this.foundHN.setLogEvents(baseHN.getLogEvents());
			} catch (ArrayIndexOutOfBoundsException aioexc) {
				throw new ArrayIndexOutOfBoundsException(
						"The baseHN does not have all the events in the 'foundHN'!");
			}
		} else {
			throw new NullPointerException("Found heuristics net is null!");
		}

		calculatePrecisionAndRecall();

	}

	private void calculatePrecisionAndRecall() {

		Multiset inputBaseNetMS = null;
		Multiset inputFoundNetMS = null;
		Multiset intersectionMS = null;

		precision = 0.0;
		recall = 0.0;

		double numberOfElementsBaseHNWithoutCausalityRelations = 0;
		double numberOfElementsFoundHNWithoutCausalityRelations = 0;

		for (int element = 0; element < baseHN.getLogEvents().size(); element++) {

			// retrieving the input tasks of all the tasks that map to element
			// note that duplicates are possible.
			inputBaseNetMS = retrieveInputTasks(baseHN, element);
			inputFoundNetMS = retrieveInputTasks(foundHN, element);
			intersectionMS = inputBaseNetMS.intersection(inputFoundNetMS);

			if (inputBaseNetMS.size() == 0) { // there are not connections for
				// this element.
				numberOfElementsBaseHNWithoutCausalityRelations++;
			} else if (inputBaseNetMS.size() > 0) {
				recall += ((double) intersectionMS.size() / inputBaseNetMS
						.size());
			} // else, division by zero, no need to increment the recall

			if (inputFoundNetMS.size() == 0) { // there are not connections for
				// this element.
				numberOfElementsFoundHNWithoutCausalityRelations++;
			} else if (inputFoundNetMS.size() > 0) {
				precision += ((double) intersectionMS.size() / inputFoundNetMS
						.size());
			} // else, division by zero, no need to increment the precision

		}

		recall /= (baseHN.getLogEvents().size() - numberOfElementsBaseHNWithoutCausalityRelations);
		precision /= (baseHN.getLogEvents().size() - numberOfElementsFoundHNWithoutCausalityRelations);

	}

	private static Multiset retrieveInputTasks(HeuristicsNet net, int element) {
		Multiset ms = new HashMultiset();

		try {

			// getting the duplicates of "element"
			HNSubSet duplicates = net.getReverseDuplicatesMapping()[element];

			// getting the input elements
			for (int i = 0; i < duplicates.size(); i++) {
				ms.addAll(toMultiSet(net.getAllElementsInputSet(duplicates
						.get(i)), net.getDuplicatesMapping(), net
						.getLogEvents()));
			}

		} catch (ArrayIndexOutOfBoundsException exc) {
			// the net has fewer events than expected

		} catch (NullPointerException exc) {
			// there are no duplicate elements...
		}

		return ms;

	}

	private static Multiset toMultiSet(HNSubSet subset,
			int[] duplicatesMapping, LogEvents le) {
		Multiset ms = new HashMultiset();

		for (int i = 0; i < subset.size(); i++) {
			ms.add(le.getEvent(duplicatesMapping[subset.get(i)]));
		}

		return ms;
	}

	public double getPrecision() {
		return precision;
	}

	public double getRecall() {
		return recall;
	}

}
