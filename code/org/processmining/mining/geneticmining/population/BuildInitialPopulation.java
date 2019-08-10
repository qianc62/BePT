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
 * Title: Build Initial Population
 * </p>
 * <p>
 * Description: Builds an initial population. Every individual is based on
 * causal, start and end matrices that are randomly created whenever an
 * individual is going to be built.<br/>
 * <b>Note:<b/> The created individuals do not have a fitness value.
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

public class BuildInitialPopulation implements BuildPopulation {

	private GeneticMiningMatrices geneticMiningMatrices = null;
	private HeuristicsNet[] population = null;
	private Random generator = null;
	private LogReader logReader = null;

	public BuildInitialPopulation(Random gen, LogReader log,
			GeneticMiningMatrices genMining) {
		generator = gen;
		logReader = log;
		geneticMiningMatrices = genMining;
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

		population = oldPopulation;

		size = population.length;

		for (int i = 0; i < size; i++) {
			// create an individual
			population[i] = new HeuristicsNet(logReader.getLogSummary()
					.getLogEvents());

			// create its input/output sets
			for (int j = 0; j < population[i].size(); j++) {
				population[i].setInputSet(j, buildInputSet(j));
				population[i].setOutputSet(j, buildOutputSet(j));

				// System.out.println("individual number " + i + " "+ j + " = "
				// + population[i].getInputSet(j).toString());
				// System.out.println("individual number " + i + " "+ j + " = "
				// + population[i].getOutputSet(j).toString());
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

			// System.out.println("individual number " + i + " start task  = " +
			// population[i].getStartTask() + " and end task = " +
			// population[i].getEndTask());
			// generate new matrices for next individual
			geneticMiningMatrices.rebuildAllMatrices();
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
