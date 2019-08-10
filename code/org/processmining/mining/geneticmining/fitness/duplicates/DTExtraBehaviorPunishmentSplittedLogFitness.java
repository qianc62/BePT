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
import org.processmining.framework.models.heuristics.ExtraBehaviorParser;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;
import org.processmining.mining.geneticmining.fitness.Fitness;
import org.processmining.mining.geneticmining.util.MapIdenticalIndividuals;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: This class uses the continuous parsing plus a punishment based
 * on the amount of enabled tasks.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class DTExtraBehaviorPunishmentSplittedLogFitness implements Fitness {
	private LogReader logReader = null;
	private HeuristicsNet[] population = null;
	private ExtraBehaviorParser[] parser = null;

	private double[] numPIsWithMissingTokens = null; // PI = process instance
	private double[] numMissingTokens = null; // PI = process instance
	private double[] numPIsWithExtraTokensLeftBehind = null;
	private double[] numExtraTokensLeftBehind = null;
	private double[] numParsedWMEs = null;
	private double[] numEnabledWMEs = null;
	private MapIdenticalIndividuals mapping = null;

	private double actuallyConsideredPIs = 0;
	private double actuallyConsideredWMEs = 0;

	private Random generator = null;

	public DTExtraBehaviorPunishmentSplittedLogFitness(LogReader log) {

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
		parser = new ExtraBehaviorParser[population.length];
		for (int i = 0; i < parser.length; i++) {
			if (mapping.getMap(i) < 0) {
				parser[i] = new ExtraBehaviorParser(population[i], generator);
			}
		}
	}

	private void createFitnessVariables() {
		numPIsWithMissingTokens = new double[population.length];
		numMissingTokens = new double[population.length];
		numPIsWithExtraTokensLeftBehind = new double[population.length];
		numExtraTokensLeftBehind = new double[population.length];
		numParsedWMEs = new double[population.length];
		numEnabledWMEs = new double[population.length];
	}

	private void calculatePartialFitness() {

		ProcessInstance pi = null;
		int numSimilarPIs = 0;
		int numMissingTokens = 0;
		int numExtraTokensLeftBehind = 0;

		Iterator logReaderInstanceIterator = logReader.instanceIterator();
		actuallyConsideredPIs = 0;
		actuallyConsideredWMEs = 0;

		// System.out.println("pis = " + actuallyConsideredPIs + " and wmes = "
		// + actuallyConsideredWMEs);

		while (logReaderInstanceIterator.hasNext()) {
			pi = (ProcessInstance) logReaderInstanceIterator.next();
			if (generator.nextDouble() < 0.25) {
				numSimilarPIs = MethodsForWorkflowLogDataStructures
						.getNumberSimilarProcessInstances(pi);
				actuallyConsideredPIs += numSimilarPIs;
				actuallyConsideredWMEs += (pi.getAuditTrailEntryList().size() * numSimilarPIs);
				for (int i = 0; i < population.length; i++) {
					if (mapping.getMap(i) < 0) { // we need to compute the
						// partial fitness
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
						numEnabledWMEs[i] += (parser[i]
								.getNumTotalEnabledElements() * numSimilarPIs);
					}
				}
			}
		}
		// System.out.println("pis = " + actuallyConsideredPIs + " and wmes = "
		// + actuallyConsideredWMEs);
	}

	private HeuristicsNet[] assignFitness() {

		double fitness = 0;
		double f1 = 0;
		double f2 = 0;
		double maxF2 = 0;
		// double numATEsAtLog = 0;
		// double numPIsAtLog = 0;
		double missingTokensDenominator = 0.001;
		double unusedTokensDenominator = 0.001;
		double maxEnabledWMEs = Double.MIN_VALUE;
		int indexIdenticalIndividual = 0;

		if (actuallyConsideredPIs != 0 && actuallyConsideredWMEs != 0) {

			maxEnabledWMEs = getMaxValue(numEnabledWMEs);
			// maxF2 = maxEnabledWMEs / maxTotalAllowedEnabledWMEs;
			maxF2 = maxEnabledWMEs;

			for (int i = 0; i < population.length; i++) {

				indexIdenticalIndividual = mapping.getMap(i);

				if (indexIdenticalIndividual < 0) {

					missingTokensDenominator = actuallyConsideredPIs
							- numPIsWithMissingTokens[i] + 1;

					unusedTokensDenominator = actuallyConsideredPIs
							- numPIsWithExtraTokensLeftBehind[i] + 1;

					f1 = (numParsedWMEs[i] - ((numMissingTokens[i] / missingTokensDenominator) + (numExtraTokensLeftBehind[i] / unusedTokensDenominator)))
							/ actuallyConsideredWMEs;

					// f2 = numEnabledWMEs[i] / this.maxTotalAllowedEnabledWMEs;
					f2 = numEnabledWMEs[i];

					// if (f2 < 0) {
					// f2 = -f2;
					// }

					f2 /= maxF2; // normalizing...

					// fitness = ((Math.floor(100 * f1)) / 100) - 0.009 * f2;

					fitness = f1 - 0.009 * f2;

					population[i]
							.setFitness((population[i].getFitness() + fitness) / 2);

				} else {

					population[i]
							.setFitness(population[indexIdenticalIndividual]
									.getFitness());
					population[i]
							.setDuplicatesActualFiring(population[indexIdenticalIndividual]
									.getDuplicatesActualFiring());
					population[i]
							.setArcUsage(population[indexIdenticalIndividual]
									.getArcUsage());

				}
			}
		}

		return population;

	}

	private double getMaxValue(double[] array) {
		double max = Double.MIN_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (max < array[i]) {
				max = array[i];
			}
		}
		return max;
	}

}
