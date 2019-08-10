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

public class DTImprovedContinuousSemanticsFitness implements Fitness {
	private LogReader logReader = null;
	private HeuristicsNet[] population = null;
	private ContinuousSemanticsParser[] parser = null;

	private double[] numPIsWithMissingTokens = null; // PI = process instance
	private double[] numMissingTokens = null; // PI = process instance
	private double[] numPIsWithExtraTokensLeftBehind = null;
	private double[] numExtraTokensLeftBehind = null;
	private double[] numParsedWMEs = null;
	private MapIdenticalIndividuals mapping = null;

	private Random generator = null;

	public DTImprovedContinuousSemanticsFitness(LogReader log) {
		generator = new Random(Long.MAX_VALUE);
		logReader = log;
	}

	public HeuristicsNet[] calculate(HeuristicsNet[] pop) {

		population = pop;
		mapping = new MapIdenticalIndividuals(population);
		createParser();
		resetDuplicatesActualFiringAndArcUsage();
		createFitnessVariables();
		calculatePartialFitness();

		return assignFitness();

	}

	private void resetDuplicatesActualFiringAndArcUsage() {
		for (int i = 0; i < population.length; i++) {
			population[i].resetDuplicatesActualFiring();
			population[i].resetArcUsage();
		}
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

	private void createFitnessVariables() {
		numPIsWithMissingTokens = new double[population.length];
		numMissingTokens = new double[population.length];
		numPIsWithExtraTokensLeftBehind = new double[population.length];
		numExtraTokensLeftBehind = new double[population.length];
		numParsedWMEs = new double[population.length];
	}

	private void calculatePartialFitness() {

		ProcessInstance pi = null;
		int numSimilarPIs = 0;
		int numMissingTokens = 0;
		int numExtraTokensLeftBehind = 0;

		Iterator logReaderInstanceIterator = logReader.instanceIterator();
		while (logReaderInstanceIterator.hasNext()) {
			pi = (ProcessInstance) logReaderInstanceIterator.next();
			numSimilarPIs = MethodsForWorkflowLogDataStructures
					.getNumberSimilarProcessInstances(pi);
			for (int i = 0; i < population.length; i++) {
				if (mapping.getMap(i) < 0) { // we need to compute the partial
					// fitness
					parser[i].parse(pi);
					// partial assignment to variables
					numMissingTokens = parser[i].getNumMissingTokens();
					if (numMissingTokens > 0) {
						this.numPIsWithMissingTokens[i] += numSimilarPIs;
						this.numMissingTokens[i] += (numMissingTokens * numSimilarPIs);
					}

					numExtraTokensLeftBehind = parser[i]
							.getNumExtraTokensLeftBehind();
					if (numExtraTokensLeftBehind > 0) {
						this.numPIsWithExtraTokensLeftBehind[i] += numSimilarPIs;
						this.numExtraTokensLeftBehind[i] += (numExtraTokensLeftBehind * numSimilarPIs);
					}
					numParsedWMEs[i] += (parser[i].getNumParsedElements() * numSimilarPIs);
				}
			}
		}
	}

	private HeuristicsNet[] assignFitness() {

		double fitness = 0;
		double numATEsAtLog = 0;
		double numPIsAtLog = 0;
		double missingTokensDenominator = 0.001;
		double unusedTokensDenominator = 0.001;
		int indexIdenticalIndividual = 0;

		numATEsAtLog = logReader.getLogSummary().getNumberOfAuditTrailEntries();
		numPIsAtLog = logReader.getLogSummary().getNumberOfProcessInstances();

		for (int i = 0; i < population.length; i++) {

			indexIdenticalIndividual = mapping.getMap(i);

			if (indexIdenticalIndividual < 0) {

				missingTokensDenominator = numPIsAtLog
						- numPIsWithMissingTokens[i] + 1;

				unusedTokensDenominator = numPIsAtLog
						- numPIsWithExtraTokensLeftBehind[i] + 1;

				fitness = (numParsedWMEs[i] - ((numMissingTokens[i] / missingTokensDenominator) + (numExtraTokensLeftBehind[i] / unusedTokensDenominator)))
						/ numATEsAtLog;

				population[i].setFitness(fitness);

			} else {

				population[i].setFitness(population[indexIdenticalIndividual]
						.getFitness());
				population[i]
						.setDuplicatesActualFiring(population[indexIdenticalIndividual]
								.getDuplicatesActualFiring());
				population[i].setArcUsage(population[indexIdenticalIndividual]
						.getArcUsage());

			}
		}

		return population;

	}

}
