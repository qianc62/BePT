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

package org.processmining.mining.geneticmining.population.duplicates;

import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.mining.geneticmining.population.BuildPopulation;

/**
 * <p>
 * Title: Duplicate Tasks Build Next Random Generation
 * </p>
 * <p>
 * Description: The individuals of the next generation are randomly created. It
 * is possible to keep some individuals of the previous generation by specify an
 * 'elitism rate' bigger than 0.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class DTBuildNextRandomGeneration implements BuildPopulation {

	private double elitismRate = 0;
	private BuildPopulation randomPopulationBuilder = null;

	public DTBuildNextRandomGeneration(double elitismRate,
			BuildPopulation randomPopulationBuilder) {

		this.randomPopulationBuilder = randomPopulationBuilder;
		this.elitismRate = elitismRate;

	}

	/*
	 * Randomly creates a new population, but a percentage of the best
	 * individuals (elite) of the old population is directly copied to the new
	 * population.
	 * 
	 * @param oldPopulation <b>increasingly</b> sorted array.
	 * 
	 * @return new population.
	 */

	public HeuristicsNet[] build(HeuristicsNet[] oldPopulation) {

		double numElite = 0;
		int indexNewPopulation = 0;
		HeuristicsNet[] randomIndividualsPopulation = null;
		HeuristicsNet[] newPopulation = null;

		// copying the best individuals to next generation...
		numElite = oldPopulation.length * elitismRate;
		newPopulation = new HeuristicsNet[oldPopulation.length];

		// NOTE: we assume that the array oldPopulation is increasingly
		// sorted!!!
		indexNewPopulation = 0;
		for (int indexOldPopulation = (oldPopulation.length - 1); indexNewPopulation < numElite; indexNewPopulation++, indexOldPopulation--) {
			newPopulation[indexNewPopulation] = oldPopulation[indexOldPopulation]
					.copyNet();
		}

		// random creation of new individuals...
		randomIndividualsPopulation = randomPopulationBuilder
				.build(new HeuristicsNet[oldPopulation.length
						- indexNewPopulation]);

		for (int indexRandomIndividuals = 0; indexNewPopulation < newPopulation.length; indexNewPopulation++, indexRandomIndividuals++) {
			newPopulation[indexNewPopulation] = randomIndividualsPopulation[indexRandomIndividuals];
		}

		return newPopulation;

	}
}
