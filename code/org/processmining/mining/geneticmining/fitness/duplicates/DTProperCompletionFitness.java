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

package org.processmining.mining.geneticmining.fitness.duplicates;

import java.util.Iterator;
import java.util.Random;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.heuristics.ContinuousSemanticsParser;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;
import org.processmining.mining.geneticmining.fitness.Fitness;
import org.processmining.mining.geneticmining.util.MapIdenticalIndividuals;

import cern.colt.matrix.DoubleMatrix2D;

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

public class DTProperCompletionFitness implements Fitness {

	private LogReader logReader = null;
	private double[] numProperlyCompletedPIs = null;
	private HeuristicsNet[] population = null;
	private ContinuousSemanticsParser[] parser = null;
	private MapIdenticalIndividuals mapping = null;

	private Random generator = null;

	public DTProperCompletionFitness(LogReader log) {
		logReader = log;
		generator = new Random(Long.MAX_VALUE);
	}

	public HeuristicsNet[] calculate(HeuristicsNet[] pop) {

		population = pop;
		mapping = new MapIdenticalIndividuals(population);

		createParser();
		createFitnessVariables();
		resetDuplicatesActualFiringAndArcUsage();
		calculatePartialFitness();

		return assignFitness();
	}

	private void resetDuplicatesActualFiringAndArcUsage() {
		for (int i = 0; i < population.length; i++) {
			population[i].resetDuplicatesActualFiring();
			population[i].resetArcUsage();
		}
	}

	private HeuristicsNet[] assignFitness() {

		double fitness = 0;
		double properlyCompleted = 0;
		int indexIdenticalIndividual = 0;

		for (int i = 0; i < population.length; i++) {

			indexIdenticalIndividual = mapping.getMap(i);

			if (indexIdenticalIndividual < 0) {

				properlyCompleted = (numProperlyCompletedPIs[i] / logReader
						.getLogSummary().getNumberOfProcessInstances());
				fitness = properlyCompleted;

				population[i].setFitness(fitness);

			} else {

				population[i].setFitness(population[indexIdenticalIndividual]
						.getFitness());

			}

		}

		return population;

	}

	private void calculatePartialFitness() {

		ProcessInstance pi = null;
		int numSimilarPIs = 0;

		Iterator logReaderInstanceIterator = logReader.instanceIterator();
		while (logReaderInstanceIterator.hasNext()) {
			try {
				pi = (ProcessInstance) logReaderInstanceIterator.next();
				numSimilarPIs = MethodsForWorkflowLogDataStructures
						.getNumberSimilarProcessInstances(pi);
				for (int i = 0; i < population.length; i++) {
					DoubleMatrix2D arcUsage = population[i].getArcUsage()
							.copy();
					int[] taskFiring = new int[population[i]
							.getDuplicatesActualFiring().length];
					System.arraycopy(population[i].getDuplicatesActualFiring(),
							0, taskFiring, 0, taskFiring.length);
					if (mapping.getMap(i) < 0) { // we need to compute the
						// partial fitness
						parser[i].parse(pi);
						// partial assignment to variables
						if (parser[i].getProperlyCompleted()
								&& parser[i].getNumMissingTokens() == 0) {
							numProperlyCompletedPIs[i] += numSimilarPIs;
						} else { // the net didn't proper complete. We need to
							// roll back the arcUsage
							population[i].setArcUsage(arcUsage);
							population[i].setDuplicatesActualFiring(taskFiring);
						}

					}
				}
			} catch (NullPointerException npe) {
				// the net does not contain the element to be parsed
				// proceed to the next process instance
			}
		}
	}

	private void createFitnessVariables() {
		numProperlyCompletedPIs = new double[population.length];
	}

	private void createParser() {
		// creating a parser for every individual
		parser = new ContinuousSemanticsParser[population.length];
		for (int i = 0; i < parser.length; i++) {
			if (mapping.getMap(i) < 0) {
				parser[i] = new ContinuousSemanticsParser(population[i],
						generator);
			}
		}
	}

}
