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

package org.processmining.mining.geneticmining.analysis;

import java.util.Iterator;

import mathCollection.Multiset;

import org.processmining.framework.models.heuristics.HeuristicsNet;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class DescriptiveStatistics {

	public static double mean(HeuristicsNet[] population) {
		double mean = 0;

		for (int i = 0; i < population.length; i++) {
			mean += population[i].getFitness();
		}

		mean /= population.length;

		return mean;

	}

	public static double standardDeviation(HeuristicsNet[] population) {
		double standardDeviation = 0;
		double variance = 0;

		variance = variance(population);
		standardDeviation = Math.sqrt(variance);

		return standardDeviation;
	}

	public static double variance(HeuristicsNet[] population) {
		double mean = 0;
		double variance = 0;

		mean = mean(population);

		for (int i = 0; i < population.length; i++) {
			variance += Math.pow(population[i].getFitness() - mean, 2);
		}

		variance /= population.length;

		return variance;
	}

	/**
	 * Calculates the mode (statistics) for a multiset of Integers.
	 * 
	 * @param ms
	 *            multiset
	 * @return the most frequent element. If all of them occurr as often, one of
	 *         them is returned. If the multiset is empty, the MIN_VALUE for an
	 *         Integer is returned.
	 */
	public static int mode(Multiset ms) {
		Integer mode = new Integer(Integer.MIN_VALUE);
		Iterator iMs = null;
		Integer element = null;
		int quantity = Integer.MIN_VALUE;

		iMs = ms.iterator();
		while (iMs.hasNext()) {
			element = (Integer) iMs.next();
			if (quantity < ms.getQuantity(element)) {
				quantity = ms.getQuantity(element);
				mode = element;
			}
		}

		return mode.intValue();
	}

}
