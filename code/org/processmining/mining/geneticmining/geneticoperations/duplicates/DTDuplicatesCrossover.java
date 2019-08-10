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

package org.processmining.mining.geneticmining.geneticoperations.duplicates;

import java.util.Random;

import org.processmining.framework.models.heuristics.HNSet;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.mining.geneticmining.geneticoperations.Crossover;
import org.processmining.mining.geneticmining.util.MethodsOverIndividuals;

/**
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class DTDuplicatesCrossover implements Crossover {

	private Random generator = null;

	public DTDuplicatesCrossover(Random generator) {
		this.generator = generator;
	}

	/**
	 * Do crossover over the input and output sets of <b>two</b> individuals in
	 * the population. The crossover point is a task label. So, if two or more
	 * tasks have the same label, one of them is randomly selected at one
	 * individual and another task is selected at the other individual. This
	 * crossover works as follows: <br/>
	 * (This procedure is done for both input and output set) <br/>
	 * - Select a random task label l to be the crossover point in both
	 * individuals. <br/>
	 * - Select a random point at the input set of t at individual x. t has
	 * label l. <br/>
	 * - Select a random point at the input set of t' at individual x'. t' has
	 * label l.<br/>
	 * - Do crossover for the input sets by performing one of the following
	 * operation for every subset sp to swap:<br/>
	 * (1) Add sp as a new subset in the input set of t (t').<br/>
	 * (2) Join sp with an existing subset in the input set of t (t').<br/>
	 * (3) Remove from an existing subset in the input set of t that contain
	 * elements that are also in sp, and add sp to the individual.<br/>
	 * - Check for dangling arcs and correct them.
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
		HeuristicsNet[] offsprings = null;
		HNSet[] sets = null;

		HNSubSet possibleDuplicates;
		int pointToCrossover1;
		int pointToCrossover2;

		if (population.length < 2) {
			return population;
		}

		offsprings = new HeuristicsNet[2];
		offspring1 = population[0].copyNet();
		offspring2 = population[1].copyNet();

		// here a choose one of the duplicates to individual 1 and to individual
		// 2
		possibleDuplicates = offspring1.getReverseDuplicatesMapping()[generator
				.nextInt(offspring1.getReverseDuplicatesMapping().length)];
		pointToCrossover1 = possibleDuplicates.get(generator
				.nextInt(possibleDuplicates.size()));
		pointToCrossover2 = possibleDuplicates.get(generator
				.nextInt(possibleDuplicates.size()));

		// swapping parts of the input sets
		sets = new HNSet[2];
		sets[0] = offspring1.getInputSet(pointToCrossover1);
		sets[1] = offspring2.getInputSet(pointToCrossover2);
		sets = enhancedCrossover(sets);
		offspring1.setInputSet(pointToCrossover1, sets[0]);
		offspring2.setInputSet(pointToCrossover2, sets[1]);
		sets = null;

		// swapping parts of the output sets
		sets = new HNSet[2];
		sets[0] = offspring1.getOutputSet(pointToCrossover1);
		sets[1] = offspring2.getOutputSet(pointToCrossover2);
		sets = enhancedCrossover(sets);
		offspring1.setOutputSet(pointToCrossover1, sets[0]);
		offspring2.setOutputSet(pointToCrossover2, sets[1]);
		sets = null;

		MethodsOverIndividuals.updateRelatedElements(offspring1,
				pointToCrossover1, generator);
		MethodsOverIndividuals.updateRelatedElements(offspring2,
				pointToCrossover2, generator);

		offsprings[0] = offspring1;
		offsprings[1] = offspring2;

		return offsprings;

	}

	private HNSet[] enhancedCrossover(HNSet[] sets) { // set1, HashSet set2) {
		HNSet subsetsToSwap1 = null;
		HNSet subsetsToSwap2 = null;
		HNSet set1 = null;
		HNSet set2 = null;

		set1 = sets[0];
		set2 = sets[1];

		if (set1.size() == 0) {
			subsetsToSwap1 = new HNSet();
		} else {
			subsetsToSwap1 = extractSubsets(set1, generator
					.nextInt(set1.size()));
		}

		if (set2.size() == 0) {
			subsetsToSwap2 = new HNSet();
		} else {
			subsetsToSwap2 = extractSubsets(set2, generator
					.nextInt(set2.size()));
		}

		set1.removeAll(subsetsToSwap1);
		set2.removeAll(subsetsToSwap2);

		set1 = crossoverSubsets(set1, subsetsToSwap2);
		set2 = crossoverSubsets(set2, subsetsToSwap1);

		sets[0] = set1;
		sets[1] = set2;

		return sets;
	}

	private HNSet extractSubsets(HNSet set, int splitPoint) {
		HNSet returnSet = null;

		returnSet = new HNSet();

		for (int i = splitPoint; i < set.size(); i++) {
			returnSet.add(set.get(i));
		}

		return returnSet;
	}

	private HNSet crossoverSubsets(HNSet set, HNSet setsToAdd) {
		HNSet returnSet = null;
		HNSubSet currentSubsetToAdd = null;
		double randomNumber = 0.0;

		returnSet = set;
		if (returnSet.size() == 0) {
			returnSet = setsToAdd;
		} else if (setsToAdd.size() > 0) {
			for (int iSetsToAdd = 0; iSetsToAdd < setsToAdd.size(); iSetsToAdd++) {
				currentSubsetToAdd = setsToAdd.get(iSetsToAdd);
				randomNumber = generator.nextDouble();
				if (randomNumber < 1.0 / 3.0) { // 0 <= random number < 1/3
					returnSet.add(currentSubsetToAdd);
				} else if (randomNumber < 2.0 / 3.0) { // 1/3 <= random number <
					// 2/3
					returnSet = joinWithOneSet(returnSet, currentSubsetToAdd);
				} else { // 2/3 <= random number < 1.0
					returnSet = addByRemovingIntersectionFromOneSet(returnSet,
							currentSubsetToAdd);
				}
			}
		}

		return returnSet;
	}

	private HNSet joinWithOneSet(HNSet returnSet, HNSubSet subsetToAdd) {
		HNSubSet selectedSet = null;

		if (returnSet.size() > 0) {
			selectedSet = returnSet.get(generator.nextInt(returnSet.size()));
			selectedSet.addAll(subsetToAdd);
		} else {
			returnSet.add(subsetToAdd);
		}

		return returnSet;

	}

	private HNSet addByRemovingIntersectionFromOneSet(HNSet returnSet,
			HNSubSet subsetToAdd) {

		HNSubSet selectedSet = null;

		if (returnSet.size() > 0) {
			selectedSet = returnSet.get(generator.nextInt(returnSet.size()));
			returnSet.remove(selectedSet);
			selectedSet.removeAll(subsetToAdd);
			if (selectedSet.size() > 0) {
				returnSet.add(selectedSet);
			}
		}
		returnSet.add(subsetToAdd);

		return returnSet;

	}

}
