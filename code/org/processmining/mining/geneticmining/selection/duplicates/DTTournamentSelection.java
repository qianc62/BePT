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

package org.processmining.mining.geneticmining.selection.duplicates;

import java.util.Arrays;
import java.util.Random;

import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.mining.geneticmining.selection.SelectionMethod;

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
 * @author not attributable
 * @version 1.0
 */

public class DTTournamentSelection implements SelectionMethod {

	private double threshold = 0.75;
	private Random generator = null;

	/**
	 * Uses a threshold of 0.75. So, the fittest individual is selected in 75%
	 * of the cases.
	 */
	public DTTournamentSelection(Random gen) {
		this.generator = gen;

	}

	/**
	 * This methods randomly selects two individuals (in population) to play the
	 * tournament. The fittest of these two individuals is returned in
	 * threshold% of the cases.
	 * 
	 * @param population
	 *            Array of individuals to take part in the selection
	 * @return An array containing the selected individual.
	 */
	public HeuristicsNet[] select(HeuristicsNet[] population) {

		HeuristicsNet[] player1 = new HeuristicsNet[1];
		HeuristicsNet[] player2 = new HeuristicsNet[1];
		double play = generator.nextDouble();

		Arrays.fill(player1, population[generator.nextInt(population.length)]);
		Arrays.fill(player2, population[generator.nextInt(population.length)]);

		if (player1[0].getFitness() > player2[0].getFitness()) {
			// player1 is the fittest...
			if (play < threshold) {
				return player1;
			}
			return player2;
		}

		// player2 is the fittest...
		if (play < threshold) {
			return player2;
		}
		return player1;

	}

}
