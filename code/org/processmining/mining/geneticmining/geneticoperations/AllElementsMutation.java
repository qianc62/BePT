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

public class AllElementsMutation implements Mutation {
	private Random generator = null;
	private double mutationRate = 0;

	public AllElementsMutation(Random gen, double mutationRate) {
		this.generator = gen;
		this.mutationRate = mutationRate;
	}

	/**
	 * This method works as follows: <br/>
	 * <ol>
	 * <li>For every workflow model element in the net (individual) do:
	 * <ol>
	 * <li>Randomly choose a double number r. If r <i>less than</i> mutation
	 * rate, do:
	 * <ol>
	 * <li>Randomly choose if add or remove and entry from the INPUT of this
	 * workflow model element.</li>
	 * <li>Randomly choose if add or remove and entry from the OUTPUT of this
	 * workflow model element.</li>
	 * <li>Update related elements.</li></li>
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
		boolean modifyInputMatrix = false;
		boolean modifyOutputMatrix = false;
		boolean addEntry = false;

		DoubleMatrix1D inputMatrix = null;
		DoubleMatrix1D outputMatrix = null;
		HNSet inputHashSet = null;
		HNSet outputHashSet = null;

		for (int i = 0; i < ind.size(); i++) {
			mutate = generator.nextDouble();
			if (mutate < mutationRate) {
				// input set...
				if (ind.getInputSet(i).size() > 0) {
					inputMatrix = HNSubSet.toDoubleMatrix1D(ind
							.getAllElementsInputSet(i), ind.size());

					modifyInputMatrix = generator.nextBoolean();

					if (modifyInputMatrix) {
						addEntry = generator.nextBoolean();
						if (addEntry) {
							MethodsOverIndividuals.randomlyAddEntry(
									inputMatrix, generator);
						} else {
							MethodsOverIndividuals.randomlyRemoveEntry(
									inputMatrix, generator);
						}

					}
				}

				// output set...
				if (ind.getOutputSet(i).size() > 0) {
					outputMatrix = HNSubSet.toDoubleMatrix1D(ind
							.getAllElementsOutputSet(i), ind.size());

					modifyOutputMatrix = generator.nextBoolean();

					if (modifyOutputMatrix) {
						addEntry = generator.nextBoolean();
						if (addEntry) {
							MethodsOverIndividuals.randomlyAddEntry(
									outputMatrix, generator);
						} else {
							MethodsOverIndividuals.randomlyRemoveEntry(
									outputMatrix, generator);
						}
					}
				}

				// rebuild set
				if (ind.getInputSet(i).size() > 0) {
					inputHashSet = MethodsOverIndividuals.buildSet(0,
							inputMatrix, generator);
				} else {
					inputHashSet = ind.getInputSet(i);
				}

				if (ind.getOutputSet(i).size() > 0) {
					outputHashSet = MethodsOverIndividuals.buildSet(0,
							outputMatrix, generator);
				} else {
					outputHashSet = ind.getOutputSet(i);
				}

				// updating sets at workflow model element i
				ind.setInputSet(i, inputHashSet);
				ind.setOutputSet(i, outputHashSet);

				// updating surroundings of wme i
				MethodsOverIndividuals.updateRelatedElements(ind, i, generator);
			}
		}
		return ind;

	}
}
