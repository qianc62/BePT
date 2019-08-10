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

package org.processmining.mining.geneticmining;

import org.processmining.mining.geneticmining.fitness.duplicates.DTFitnessFactory;

/**
 * @author not attributable
 * @version 1.0
 */

public class GeneticMinerSettings {

	private int populationSize;
	private int initialPopulationType;
	private int maxNumGenerations;
	private double mutationRate;
	private int mutationType;
	private int crossoverType;
	private double crossoverRate;
	private long seed;
	private int fitnessType;
	private int selectionMethodType;
	private double elitismRate;
	private long power; // to be used during the building of the
	// causal/start/end matrices.
	private boolean useGeneticOperators;
	private double[] fitnessParameters = DTFitnessFactory.ALL_FITNESS_PARAMETERS;

	public GeneticMinerSettings(int populationSize, int maxNumGenerations,
			double mutationRate, int crossoverType, double crossoverRate,
			long seed, int fitnessType, int selectionMethodType,
			double elitismRate, int mutationType, long power,
			int initialPopulationType, boolean useGeneticOperators,
			double[] fitnessParameters) {
		this.populationSize = populationSize;
		this.initialPopulationType = initialPopulationType;
		this.maxNumGenerations = maxNumGenerations;
		this.mutationRate = mutationRate;
		this.crossoverRate = crossoverRate;
		this.crossoverType = crossoverType;
		this.seed = seed;
		this.fitnessType = fitnessType;
		this.selectionMethodType = selectionMethodType;
		this.elitismRate = elitismRate;
		this.mutationType = mutationType;
		this.power = power;
		this.useGeneticOperators = useGeneticOperators;
		this.fitnessParameters = fitnessParameters;
	}

	// public GeneticMinerSettings(int populationSize, int maxNumGenerations,
	// double mutationRate,
	// int crossoverType, double crossoverRate, long seed,
	// int fitnessType, int selectionMethodType, double elitismRate,
	// int mutationType, long power, int initialPopulationType, boolean
	// useGeneticOperators) {
	// this.populationSize = populationSize;
	// this.initialPopulationType = initialPopulationType;
	// this.maxNumGenerations = maxNumGenerations;
	// this.mutationRate = mutationRate;
	// this.crossoverRate = crossoverRate;
	// this.crossoverType = crossoverType;
	// this.seed = seed;
	// this.fitnessType = fitnessType;
	// this.selectionMethodType = selectionMethodType;
	// this.elitismRate = elitismRate;
	// this.mutationType = mutationType;
	// this.power = power;
	// this.useGeneticOperators = useGeneticOperators;
	// }

	public double[] getFitnessParameters() {
		return fitnessParameters;
	}

	public int getInitialPopulationType() {
		return initialPopulationType;
	}

	public long getPower() {
		return power;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public int getMaxNumGenerations() {
		return maxNumGenerations;
	}

	public double getElitismRate() {
		return elitismRate;
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public int getMutationType() {
		return mutationType;
	}

	public int getCrossoverType() {
		return crossoverType;
	}

	public double getCrossoverRate() {
		return crossoverRate;
	}

	public int getFitnessType() {
		return fitnessType;

	}

	public long getSeed() {
		return seed;
	}

	public int getSelectionMethodType() {
		return selectionMethodType;
	}

	public boolean getUseGeneticOperators() {
		return useGeneticOperators;
	}

}
