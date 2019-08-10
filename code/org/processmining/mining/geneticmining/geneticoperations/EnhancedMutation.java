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

import java.util.Iterator;
import java.util.Random;

import org.processmining.framework.models.heuristics.HNSet;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.mining.geneticmining.util.MethodsOverIndividuals;

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
 * @author not attributable
 * @version 1.0
 */

public class EnhancedMutation implements Mutation {

	private Random generator = null;
	private double mutationRate = 0;

	public EnhancedMutation(Random gen, double mutationRate) {
		this.generator = gen;
		this.mutationRate = mutationRate;
	}

	/**
	 * This method works as follows: <br/>
	 * <ol>
	 * <li>For every workflow model element in the net (individual) do:
	 * <ol>
	 * <li>Randomly choose a double number r. If r <i>less than</i> mutation
	 * rate, do one of the following operations for its INPUT/OUTPUT sets:
	 * <ol>
	 * <li>Add a task to one of the INPUT/OUTPUT subsets of this workflow model
	 * element.</li>
	 * <li>Remove a task from one of the INPUT/OUTPUT subsets of this workflow
	 * model element.</li>
	 * <li>Redistribute the tasks of the INPUT/OUTPUT sets of this workflow
	 * model element.</li></li>
	 * <li>Update related elements.</li>
	 * </ol>
	 * </li>
	 * </ol>
	 * 
	 * @param ind
	 *            individual to mutate.
	 * @return mutated individual.
	 */

	public HeuristicsNet doMutation(HeuristicsNet ind) {
		HNSubSet mutatedPositions = new HNSubSet();
		Iterator iMutatedPositions = null;

		for (int i = 0; i < ind.size(); i++) {
			// INPUT set
			if (!ind.getStartTasks().contains(i)) {
				if (mutateSet(ind.getInputSet(i), ind.size())) {
					mutatedPositions.add(i);
				}
			}

			// OUTPUT set
			if (!ind.getEndTasks().contains(i)) {
				if (mutateSet(ind.getOutputSet(i), ind.size())) {
					mutatedPositions.add(i);
				}
			}
		}

		// updating surroundings of the mutated wme
		for (int i = 0; i < mutatedPositions.size(); i++) {
			MethodsOverIndividuals.updateRelatedElements(ind, mutatedPositions
					.get(i), generator);
		}

		return ind;
	}

	private boolean mutateSet(HNSet set, int maxTaskId) {

		double mutate = 0.0;
		double mutationType = 0.0;
		boolean mutated = false;

		mutate = generator.nextDouble();

		if (mutate < mutationRate) {
			mutationType = generator.nextDouble();

			if (mutationType < 1.0 / 3.0) { // 0 <= mutationType < 1/3
				mutated = addOneTask(set, maxTaskId);
			} else if (mutationType < 2.0 / 3.0) { // 1/3 <= mutationType < 2/3
				mutated = removeOneTask(set);
			} else { // 2/3 <= mutationType < 1.0
				mutated = redistributeElements(set);
			}
		}

		return mutated;
	}

	private boolean addOneTask(HNSet set, int maxTaskId) {
		HNSubSet subset = null;
		int taskToAdd = 0;

		taskToAdd = generator.nextInt(maxTaskId);

		if (set.size() > 0) {
			subset = set.get(generator.nextInt(set.size()));
			subset.add(taskToAdd);
		} else {
			subset = new HNSubSet();
			subset.add(taskToAdd);
			set.add(subset);
		}

		return true;
	}

	private boolean removeOneTask(HNSet set) {

		HNSubSet subset = null;
		boolean mutated = false;

		if (set.size() > 0) {
			subset = set.get(generator.nextInt(set.size()));
			if ((subset.size() > 0) && (subset.size() > 1 || set.size() > 1)) {
				if (subset.size() == 1) {
					// set has more than one subset, so we can remove this
					// subset directly
					set.remove(subset);
				} else {
					// subset has more than one element
					subset.remove(subset.get(generator.nextInt(subset.size())));
				}
				mutated = true;
			}
		}

		return mutated;
	}

	private boolean redistributeElements(HNSet set) {

		int[] multiset = null;

		HNSubSet[] array = null;
		int setToIncludeTask = 0;
		boolean mutated = false;

		multiset = HeuristicsNet.getElements(set);

		if (multiset.length > 1) {

			// redistributing the elements
			array = new HNSubSet[multiset.length];

			for (int i = 0; i < array.length; i++) {
				setToIncludeTask = generator.nextInt(array.length);
				if (array[setToIncludeTask] == null) {
					array[setToIncludeTask] = new HNSubSet();
				}
				array[setToIncludeTask].add(multiset[i]);
			}

			// filtering out the null entries
			set.removeAll(set);
			for (int i = 0; i < array.length; i++) {
				if (array[i] != null) {
					set.add(array[i]);
				}
			}
			mutated = true;
		}

		return mutated;

	}

}
