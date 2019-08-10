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
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Ana Karla A. de Medeiros.
 * @version 1.0
 */

public class DTContinuousSemanticsFitness implements Fitness {
	private LogReader logReader = null;
	private HeuristicsNet[] population = null;
	private ContinuousSemanticsParser[] parser = null;

	private MapIdenticalIndividuals mapping = null;

	private double[] numDisabledWMEs = null; // WME = Workflow Model Element
	private double[] numParsedWMEs = null; // WME = Workflow Model Element
	private double[] numProperlyCompletedPIs = null; // PI = process instance

	private double numEnabledConstant = 0.40;
	private double numProperlyCompletedConstant = 0.60;

	private Random generator = null;

	public DTContinuousSemanticsFitness(LogReader log) {
		logReader = log;
		generator = new Random(Long.MAX_VALUE);
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

	public double getNumProperlyCompletedConstant() {
		return numProperlyCompletedConstant;
	}

	public double getNumEnabledConstant() {
		return numEnabledConstant;
	}

	public void setNumProperlyCompletedConstant(double newValue) {
		numProperlyCompletedConstant = newValue;
	}

	public void setNumEnabledConstant(double newValue) {
		numEnabledConstant = newValue;
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
		numDisabledWMEs = new double[population.length];
		numParsedWMEs = new double[population.length];
		numProperlyCompletedPIs = new double[population.length];
	}

	private void calculatePartialFitness() {

		ProcessInstance pi = null;
		int numSimilarPIs = 0;

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
					numDisabledWMEs[i] += (parser[i].getNumUnparsedElements() * numSimilarPIs);
					numParsedWMEs[i] += (parser[i].getNumParsedElements() * numSimilarPIs);
					if (parser[i].getProperlyCompleted()) {
						numProperlyCompletedPIs[i] += numSimilarPIs;
					}
				}
			}
		}
	}

	private HeuristicsNet[] assignFitness() {

		double fitness = 0;
		double enabled = 0;
		double properlyCompleted = 0;
		int indexIdenticalIndividual = 0;

		for (int i = 0; i < population.length; i++) {

			indexIdenticalIndividual = mapping.getMap(i);

			if (indexIdenticalIndividual < 0) {

				enabled = numParsedWMEs[i]
						/ logReader.getLogSummary()
								.getNumberOfAuditTrailEntries();
				properlyCompleted = (numProperlyCompletedPIs[i] / logReader
						.getLogSummary().getNumberOfProcessInstances());
				fitness = (numEnabledConstant * enabled)
						+ (numProperlyCompletedConstant * properlyCompleted);
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
