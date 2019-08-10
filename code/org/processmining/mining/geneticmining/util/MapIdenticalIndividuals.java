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

package org.processmining.mining.geneticmining.util;

import java.util.Arrays;

import org.processmining.framework.models.heuristics.HeuristicsNet;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: This class maps individuals that are equal. So, if individual n
 * is equal to individual n-2 and n-3, the mapping for individual n returns
 * individual n-3. These mapping can be used to improve performance. For
 * instance, if individual n-3 has a fitness to a given log, we don't need to
 * recalculate it to individual n. So, we save time when assigning the fitness.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros.
 * @version 1.0
 */

public class MapIdenticalIndividuals {

	private int[] mapIndividuals;

	public MapIdenticalIndividuals(HeuristicsNet[] population) {

		createMapIndividuals(population);

	}

	private void createMapIndividuals(HeuristicsNet[] population) {

		mapIndividuals = new int[population.length];

		Arrays.fill(mapIndividuals, -1);

		for (int i = 0; i < population.length; i++) {
			for (int j = 0; j < i; j++) {
				if (population[i].equals(population[j])) {
					mapIndividuals[i] = j;
					break;
				}
			}
		}

	}

	/**
	 * 
	 * @param individual
	 *            index of the individual to find mapping.
	 * @return the index of the first individual in the population that is equal
	 *         to the individual given as parameter.
	 */
	public final int getMap(int individual) {
		return mapIndividuals[individual];

	}
}
