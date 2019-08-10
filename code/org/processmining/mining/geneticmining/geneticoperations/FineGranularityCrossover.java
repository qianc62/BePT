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

package org.processmining.mining.geneticmining.geneticoperations;

import java.util.Random;

import org.processmining.framework.models.heuristics.HNSet;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.mining.geneticmining.util.MethodsOverIndividuals;

/**
 * <p>
 * Title: Fine Granularity Crossover
 * </p>
 * <p>
 * Description: This class does crossover over the subsets of the input/output
 * sets.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Ana Karla A. de Medeiros.
 * @version 1.0
 */

public class FineGranularityCrossover implements Crossover {

	private Random generator = null;

	public FineGranularityCrossover(Random generator) {
		this.generator = generator;
	}

	/**
	 * Do crossover over the input and output sets of <b>two</b> individuals in
	 * the population. This crossover works as follows: <br/>
	 * (This procedure is done for both input and output set) <br/>
	 * - Select a random point at the input set of x (x is an individual). <br/>
	 * - Select a random point at the input set of x'.<br/>
	 * - Do crossover for the input sets.<br/>
	 * - If the subsets have common elements, randomly choose if these subsets
	 * are going to be merged or if the common elements are going to be removed
	 * from the subset that wasn't transferred during the crossover. - Check for
	 * dangling arcs and correct them.
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

		int pointToCrossover = 0;

		if (population.length < 2) {
			return population;
		}

		if (population[0].equals(population[1])) {
			return population; // because the parents are equal!
		}

		offsprings = new HeuristicsNet[2];
		offspring1 = population[0].copyNet();
		offspring2 = population[1].copyNet();

		pointToCrossover = generator.nextInt(offspring1.size());
		// swapping parts of the input sets
		sets = new HNSet[2];
		sets[0] = offspring1.getInputSet(pointToCrossover);
		sets[1] = offspring2.getInputSet(pointToCrossover);
		sets = fineGrainCrossover(sets);
		offspring1.setInputSet(pointToCrossover, sets[0]);
		offspring2.setInputSet(pointToCrossover, sets[1]);
		sets = null;

		// swapping parts of the output sets
		sets = new HNSet[2];
		sets[0] = offspring1.getOutputSet(pointToCrossover);
		sets[1] = offspring2.getOutputSet(pointToCrossover);
		sets = fineGrainCrossover(sets);
		offspring1.setOutputSet(pointToCrossover, sets[0]);
		offspring2.setOutputSet(pointToCrossover, sets[1]);
		sets = null;

		MethodsOverIndividuals.updateRelatedElements(offspring1,
				pointToCrossover, generator);
		MethodsOverIndividuals.updateRelatedElements(offspring2,
				pointToCrossover, generator);

		offsprings[0] = offspring1;
		offsprings[1] = offspring2;

		return offsprings;

	}

	private HNSet[] fineGrainCrossover(HNSet[] sets) { // set1, HashSet set2) {
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

		set1 = addSubsets(set1, subsetsToSwap2);
		set2 = addSubsets(set2, subsetsToSwap1);

		sets[0] = set1;
		sets[1] = set2;

		return sets;
	}

	private HNSet extractSubsets(HNSet set, int splitPoint) {
		HNSet returnSet = null;
		HNSubSet subset = null;

		returnSet = new HNSet();

		for (int i = 0; i < set.size(); i++) {
			subset = set.get(i);
			if (i >= splitPoint) {
				returnSet.add(subset);
			}
		}

		return returnSet;
	}

	private HNSet addSubsets(HNSet set, HNSet setsToAdd) {
		HNSet returnSet;
		HNSet setWithoutOverlapping;
		int element;
		HNSubSet currentSubSet;
		HNSubSet currentSubSetToAdd;
		HNSubSet elementsInCommon;
		boolean mergeElements;

		returnSet = set;
		if (returnSet.size() == 0) {
			returnSet = setsToAdd;
		} else if ((setsToAdd.size() > 0) && !set.equals(setsToAdd)) {
			// check if there are overlapping between the subsets in "setsToAdd"
			// and
			// the subsets in "set"
			setWithoutOverlapping = new HNSet();
			for (int isetElements = 0; isetElements < returnSet.size(); isetElements++) {
				currentSubSet = returnSet.get(isetElements);
				// checking "currentSubSet" against all subsets in "setsToAdd"
				for (int isetsToAddElements = 0; isetsToAddElements < setsToAdd
						.size(); isetsToAddElements++) {
					currentSubSetToAdd = setsToAdd.get(isetsToAddElements);
					// checking the elements in common
					elementsInCommon = new HNSubSet();
					for (int ielements = 0; ielements < currentSubSetToAdd
							.size(); ielements++) {
						element = currentSubSetToAdd.get(ielements);
						if (currentSubSet.contains(element)) {
							elementsInCommon.add(element);
						}
					}
					// in case there are elements in common, update subsets
					// either
					// by merging them or by removing the elements in common
					// from set
					if (elementsInCommon.size() > 0) {
						mergeElements = generator.nextBoolean();
						if (mergeElements) {
							// the next two steps make sure we keep the
							// partition restriction
							// when merging the sets
							currentSubSet.removeAll(elementsInCommon);
							currentSubSet.removeAll(findIntersectingElements(
									currentSubSet, setsToAdd));

							currentSubSetToAdd.addAll(currentSubSet);
							currentSubSet = new HNSubSet();
						} else {
							currentSubSet.removeAll(elementsInCommon);
						}
					}
					if (currentSubSet.size() == 0) {
						break;
					}
				}
				if (currentSubSet.size() > 0) {
					setWithoutOverlapping.add(currentSubSet);
				}
			}
			returnSet = setWithoutOverlapping;
			for (int isetsToAddElements = 0; isetsToAddElements < setsToAdd
					.size(); isetsToAddElements++) {
				returnSet.add(setsToAdd.get(isetsToAddElements));
			}
		}

		return returnSet;
	}

	private HNSubSet findIntersectingElements(HNSubSet ts, HNSet sets) {
		HNSubSet intersection;
		int elem;
		HNSubSet subset;

		intersection = new HNSubSet();
		for (int itTsElems = 0; itTsElems < ts.size(); itTsElems++) {
			elem = ts.get(itTsElems);
			for (int itSubset = 0; itSubset < sets.size(); itSubset++) {
				subset = sets.get(itSubset);
				if (subset.contains(elem)) {
					intersection.add(elem);
				}
			}
		}

		return intersection;
	}

}
