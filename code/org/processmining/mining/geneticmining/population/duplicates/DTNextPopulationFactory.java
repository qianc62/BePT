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

import java.util.Random;

import org.processmining.mining.geneticmining.geneticoperations.Crossover;
import org.processmining.mining.geneticmining.geneticoperations.Mutation;
import org.processmining.mining.geneticmining.population.BuildPopulation;
import org.processmining.mining.geneticmining.selection.SelectionMethod;

public class DTNextPopulationFactory {
	public DTNextPopulationFactory() {
	}

	public static BuildPopulation getPopulation(boolean useGeneticOperators,
			SelectionMethod selectionMethod, Random generator,
			double crossoverRate, double mutationRate, double elitismRate,
			Crossover crossover, Mutation mutation,
			BuildPopulation randomPopulationBuilder) {

		BuildPopulation object = null;

		if (useGeneticOperators) {

			object = new DTBuildNextGeneration(selectionMethod, generator,
					crossoverRate, mutationRate, elitismRate, crossover,
					mutation);
		} else {
			object = new DTBuildNextRandomGeneration(elitismRate,
					randomPopulationBuilder);
		}

		return object;
	}

}
