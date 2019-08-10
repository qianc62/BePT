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

package org.processmining.mining.geneticmining.population;

import java.util.Random;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HNSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.mining.geneticmining.util.MethodsOverIndividuals;
import org.processmining.framework.models.heuristics.HNSubSet;

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

public class BuildInitialPopulationWithoutDuplicateIndividuals implements
		BuildPopulation {

	private GeneticMiningMatrices geneticMiningMatrices = null;
	private HeuristicsNet[] population = null;
	private Random generator = null;
	private LogReader logReader = null;

	public BuildInitialPopulationWithoutDuplicateIndividuals(Random gen,
			LogReader log, GeneticMiningMatrices genMinMatrices) {
		generator = gen;
		logReader = log;
		geneticMiningMatrices = genMinMatrices;
	}

	/**
	 * Fills in oldPopulation with new individuals.
	 * 
	 * @param oldPopulation
	 *            DuplicateTasksHeuristicsNet[] population to be filled in.
	 * @return DuplicateTasksHeuristicsNet[] population with new individuals.
	 *         There are no duplicated individuals in this population.
	 */
	public HeuristicsNet[] build(HeuristicsNet[] oldPopulation) {

		int size = 0;
		HNSet singleton = null;
		boolean individualAlreadyInPopulation = false;

		population = oldPopulation;

		size = population.length;

		for (int i = 0; i < size; i++) {
			// create an individual
			population[i] = new HeuristicsNet(logReader.getLogSummary()
					.getLogEvents());

			do {
				individualAlreadyInPopulation = false;
				// create its input/output sets
				for (int j = 0; j < population[i].size(); j++) {
					population[i].setInputSet(j, buildInputSet(j));
					population[i].setOutputSet(j, buildOutputSet(j));

					if (geneticMiningMatrices.getStartMatrix().get(j) > 0) {
						// because this is the artificial START tasks....
						if (population[i].getStartTasks() == null) {
							HNSubSet startTasks = new HNSubSet();
							startTasks.add(j);
							population[i].setStartTasks(startTasks);

						} else {
							population[i].getStartTasks().add(j);
						}

						singleton = new HNSet();
						singleton.add(population[i].getAllElementsOutputSet(j));
						population[i].setOutputSet(j, singleton);
					}

					if (geneticMiningMatrices.getEndMatrix().get(j) > 0) {
						// because this is the artificial END tasks....
						if (population[i].getEndTasks() == null) {
							HNSubSet endTasks = new HNSubSet();
							endTasks.add(j);
							population[i].setEndTasks(endTasks);
						} else {
							population[i].getEndTasks().add(j);
						}

						singleton = new HNSet();
						singleton.add(population[i].getAllElementsInputSet(j));
						population[i].setInputSet(j, singleton);
					}
				}

				// generate new matrices for next individual
				geneticMiningMatrices.rebuildAllMatrices();
				for (int k = 0; k < i; k++) {
					if (population[i].equals(population[k])) {
						individualAlreadyInPopulation = true;
						break;
					}
				}
			} while (individualAlreadyInPopulation);
		}

		return population;
	}

	private HNSet buildInputSet(int index) {

		return MethodsOverIndividuals.buildSet(geneticMiningMatrices
				.getStartMatrix().get(index), geneticMiningMatrices
				.getCausalMatrix().viewColumn(index), generator);
	}

	private HNSet buildOutputSet(int index) {
		return MethodsOverIndividuals.buildSet(geneticMiningMatrices
				.getEndMatrix().get(index), geneticMiningMatrices
				.getCausalMatrix().viewRow(index), generator);
	}

}
