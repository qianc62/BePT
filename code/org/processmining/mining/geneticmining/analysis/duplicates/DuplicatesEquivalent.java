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

import org.processmining.framework.models.heuristics.HeuristicsNet;

/**
 * <p>
 * This class calculates the precision and recall of two
 * <code>HeuristicsNet</code> with respect to the number of duplicates that they
 * have in common.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class DuplicatesEquivalent implements PrecisionRecall {

	private double precision;
	private double recall;

	private HeuristicsNet baseHN;
	private HeuristicsNet foundHN;

	/**
	 * Creates an object of <code>DuplicatesEquivalent.</code>
	 * 
	 * @param baseHN
	 *            HeuristicsNet base heuristics net. The precision and recall
	 *            consider this net as the correct solution.
	 * @param foundHN
	 *            HeuristicsNet found heuristis net. The precision and recall
	 *            compare this net to the base heuristics net.
	 */

	public DuplicatesEquivalent(HeuristicsNet baseHN, HeuristicsNet foundHN)
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

		Multiset tasksBaseNetMS = null;
		Multiset taskstFoundNetMS = null;
		Multiset intersectionTasksMS = null;

		tasksBaseNetMS = toMultiSet(baseHN);
		taskstFoundNetMS = toMultiSet(foundHN);
		intersectionTasksMS = tasksBaseNetMS.intersection(taskstFoundNetMS);

		try {
			recall = ((double) intersectionTasksMS.size() / tasksBaseNetMS
					.size());
		} catch (ArithmeticException exc) {
			// division by zero
			recall = 0.0;
		}

		try {
			precision = ((double) intersectionTasksMS.size() / taskstFoundNetMS
					.size());
		} catch (ArithmeticException exc) {
			// division by zero
			precision = 0.0;
		}

	}

	private static Multiset toMultiSet(HeuristicsNet net) {
		Multiset ms = new HashMultiset();
		for (int i = 0; i < net.getDuplicatesMapping().length; i++) {
			if ((net.getInputSet(i).size() > 0)
					|| (net.getOutputSet(i).size() > 0)) {
				ms.add(net.getLogEvents().getEvent(
						net.getDuplicatesMapping()[i]));
			}
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
