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

package org.processmining.mining.geneticmining.geneticoperations;

import java.util.Random;

import org.processmining.framework.models.heuristics.HNSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.mining.geneticmining.util.MethodsOverIndividuals;

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

public class VariableLocalOnePointCrossover implements Crossover {

	private Random generator = null;

	public VariableLocalOnePointCrossover(Random generator) {
		this.generator = generator;
	}

	/**
	 * Do crossover over <b>two</b> individuals in population. This crossover
	 * always swaps: <br/>
	 * - both INPUT and OUTPUT sets of a workflow model element, if the randomly
	 * selected double x is between 0 and 0.5 (i.e. 0 <= x < 0.5) <br/>
	 * - INPUT set only, if 0.5 <= x < 0.75 - OUTPUT set only, if 0.75 <= x <
	 * 1.0
	 * 
	 * @param population
	 *            has the two individuals to crossover
	 * @return array with the two offsprings that the crossover produced. If
	 *         population size is less than two, no crossover is done and the
	 *         population itself is returned.
	 */
	public HeuristicsNet[] doCrossover(HeuristicsNet[] population) {
		HeuristicsNet offspring1 = null;
		HeuristicsNet offspring2 = null;
		HNSet setElementOffspring1 = null;
		HeuristicsNet[] offsprings = null;

		int pointToCrossover = 0;
		double setsToCrossover = 0.0;

		if (population.length < 2) {
			return population;
		}

		if (population[0].equals(population[1])) {
			return population; // because the parents are equal!
		}

		offsprings = new HeuristicsNet[2];
		offspring1 = population[0].copyNet();
		offspring2 = population[1].copyNet();

		pointToCrossover = generator.nextInt(offspring1.size());
		setsToCrossover = generator.nextDouble();

		// swapping input sets
		if (setsToCrossover < 0.75) {
			setElementOffspring1 = offspring1.getInputSet(pointToCrossover);
			offspring1.setInputSet(pointToCrossover, offspring2
					.getInputSet(pointToCrossover));
			offspring2.setInputSet(pointToCrossover, setElementOffspring1);
		}

		// swapping output sets
		if (setsToCrossover < 0.5 || setsToCrossover >= 0.75) {
			setElementOffspring1 = offspring1.getOutputSet(pointToCrossover);
			offspring1.setOutputSet(pointToCrossover, offspring2
					.getOutputSet(pointToCrossover));
			offspring2.setOutputSet(pointToCrossover, setElementOffspring1);
		}

		MethodsOverIndividuals.updateRelatedElements(offspring1,
				pointToCrossover, generator);
		MethodsOverIndividuals.updateRelatedElements(offspring2,
				pointToCrossover, generator);

		offsprings[0] = offspring1;
		offsprings[1] = offspring2;

		return offsprings;

	}

	public static void main(String[] args) {
		// VariableLocalOnePointCrossover variableLocalOnePointCrossover1 = new
		// VariableLocalOnePointCrossover();
	}

}
