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

import cern.colt.matrix.DoubleMatrix1D;

/**
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class PartitionRedefinitionMutation implements Mutation {

	private Random generator = null;
	private double mutationRate = 0;

	public PartitionRedefinitionMutation(Random gen, double mutationRate) {
		this.generator = gen;
		this.mutationRate = mutationRate;
	}

	/**
	 * This method changes the AND/OR-split/join at the individual. It works as
	 * follows: <br/>
	 * <ol>
	 * <li>For every workflow model element in the net (individual) do:
	 * <ol>
	 * <li>Randomly choose a double number r. If r <i>less than</i> mutation
	 * rate, do the following for the INPUT and for the OUTPUT set:
	 * <ol>
	 * <li>Retrieve the union U of all partitions in the set S.</li>
	 * <li>Randomly set a new number of subsets (partitions) for this set S.</li>
	 * <li>Randomly distribute the elements at the union set U among the
	 * partition in S.</li></li>
	 * </ol>
	 * </li>
	 * </ol>
	 * 
	 * @param ind
	 *            individual to mutate.
	 * @return mutated individual.
	 */
	public HeuristicsNet doMutation(HeuristicsNet ind) {
		double mutate = 0;

		HNSet inputHashSet = null;
		HNSet outputHashSet = null;

		for (int i = 0; i < ind.size(); i++) {
			mutate = generator.nextDouble();
			if (mutate < mutationRate) {
				// input set...
				inputHashSet = mutateSet(ind.getInputSet(i), ind
						.getAllElementsInputSet(i), ind.size());
				// output set...
				outputHashSet = mutateSet(ind.getOutputSet(i), ind
						.getAllElementsOutputSet(i), ind.size());

				// updating sets at workflow model element i
				ind.setInputSet(i, inputHashSet);
				ind.setOutputSet(i, outputHashSet);
			}
		}
		return ind;
	}

	private HNSet mutateSet(HNSet set, HNSubSet unionSet,
			int numIndividualWMElements) {
		DoubleMatrix1D matrix = null;
		HNSet mutatedSet = null;

		if (set.size() > 0) {
			// retrieving the union of all elements...
			matrix = HNSubSet.toDoubleMatrix1D(unionSet,
					numIndividualWMElements);
			// rebuilding the set...
			mutatedSet = MethodsOverIndividuals.buildSet(0, matrix, generator);
		} else {
			mutatedSet = set;
		}

		return mutatedSet;

	}

}
