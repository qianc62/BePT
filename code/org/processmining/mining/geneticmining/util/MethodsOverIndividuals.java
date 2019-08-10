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

package org.processmining.mining.geneticmining.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import org.processmining.framework.models.heuristics.HNSet;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.mining.geneticmining.fitness.Fitness;

import cern.colt.matrix.DoubleMatrix1D;

/**
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class MethodsOverIndividuals {

	/**
	 * Removes the unfired tasks and unused arcs of every individual in the
	 * population.
	 * 
	 * @param localPopulation
	 *            DuplicateTasksHeuristicsNet[] population to be cleaned.
	 * @param fitness
	 *            Fitness fitness to be used to recompute the individual's
	 *            fitness after the cleaning.
	 * @return DuplicateTasksHeuristicsNet[] cleaned population with the updated
	 *         fitness values.
	 */

	public static HeuristicsNet[] removeUnusedElements(
			HeuristicsNet[] localPopulation, Fitness fitness) {
		// removing the unfired elements and the unused arcs - this operation
		// build a cleaner phenotype for
		// the individuals in the population
		for (int i = 0; i < localPopulation.length; i++) {
			localPopulation[i].disconnectUnusedElements();
		}

		return fitness.calculate(localPopulation);

	}

	/**
	 * Updates the net according to the INPUT and OUTPUT sets of "element".
	 * 
	 * @param ind
	 *            individual to update.
	 * @param element
	 *            has the INPUT/OUTPUT sets that are going to guide the update
	 *            of the other related elements.
	 * @param r
	 *            random generator.
	 * @return an updated individual.
	 */

	public static HeuristicsNet updateRelatedElements(HeuristicsNet ind,
			int element, Random r) {

		HeuristicsNet individual = null;

		individual = removeConnections(ind, element);
		individual = addConnections(individual, element, r);

		// //Makes sure that the start/end tasks have an XOR-slit/join semantics
		// individual = updateArtificialStartEndSets(individual);

		return individual;
	}

	private static HeuristicsNet updateArtificialStartEndSets(
			HeuristicsNet individual) {

		int index = 0;
		HNSet singleton = null;

		// making sure that the artificial START tasks have a single output
		// place
		for (int indexStartTask = 0; indexStartTask < individual
				.getStartTasks().size(); indexStartTask++) {
			index = individual.getStartTasks().get(indexStartTask);
			singleton = new HNSet();
			singleton.add(individual.getAllElementsOutputSet(index));
			individual.setOutputSet(index, singleton);
		}
		// making sure that the artificial END tasks have a single input place
		for (int indexEndTask = 0; indexEndTask < individual.getEndTasks()
				.size(); indexEndTask++) {
			index = individual.getEndTasks().get(indexEndTask);
			singleton = new HNSet();
			singleton.add(individual.getAllElementsInputSet(index));
			individual.setInputSet(index, singleton);
		}
		return individual;

	}

	private static HeuristicsNet addConnections(HeuristicsNet ind, int element,
			Random gen) {
		HeuristicsNet individual = ind;

		HNSubSet allElementsInOtherElementSets = null;
		HNSubSet allElementsInElementSets = null;

		// adding element to the INPUT/OUTPUT set of other elements that are
		// respectively in the OUTPUT/INPUT sets of element
		for (int otherElement = 0; otherElement < individual.size(); otherElement++) {
			if (otherElement != element) {

				// checking the otherElement's OUTPUT versus element's INPUT
				allElementsInOtherElementSets = individual
						.getAllElementsOutputSet(otherElement);
				allElementsInElementSets = individual
						.getAllElementsInputSet(element);
				if (!allElementsInOtherElementSets.contains(element)
						&& allElementsInElementSets.contains(otherElement)) {

					individual.setOutputSet(otherElement,
							addConnectionsSet(individual
									.getOutputSet(otherElement), element, gen));

				}

				// checking the otherElement's INPUT versus element's OUTPUT
				allElementsInOtherElementSets = individual
						.getAllElementsInputSet(otherElement);
				allElementsInElementSets = individual
						.getAllElementsOutputSet(element);
				if (!allElementsInOtherElementSets.contains(element)
						&& allElementsInElementSets.contains(otherElement)) {

					individual
							.setInputSet(otherElement, addConnectionsSet(
									individual.getInputSet(otherElement),
									element, gen));
				}

			}
		}

		return individual;

	}

	private static HeuristicsNet removeConnections(HeuristicsNet ind,
			int element) {
		HeuristicsNet individual = ind;

		HNSubSet allElementsInOtherElementSets = null;
		HNSubSet allElementsInElementSets = null;
		HNSet set = null;
		HNSubSet subset = null;

		// removing element from the INPUT/OUTPUT set of other elements that are
		// not pointed
		// by or point to element...
		for (int otherElement = 0; otherElement < individual.size(); otherElement++) {
			if (otherElement != element) {

				// checking the otherElement's OUTPUT versus element's INPUT
				allElementsInOtherElementSets = individual
						.getAllElementsOutputSet(otherElement);
				allElementsInElementSets = individual
						.getAllElementsInputSet(element);
				if (allElementsInOtherElementSets.contains(element)
						&& !allElementsInElementSets.contains(otherElement)) {

					individual.setOutputSet(otherElement, removeConnectionsSet(
							individual.getOutputSet(otherElement), individual
									.getOutputSetsWithElement(otherElement,
											element), element));

					if (individual.getOutputSet(otherElement).size() == 0) {
						set = new HNSet();
						subset = new HNSubSet();
						subset.addAll(individual.getEndTasks());
						set.add(subset);
						individual.setOutputSet(otherElement, set);
					}

				}

				// checking the otherElement's INPUT versus element's OUTPUT
				allElementsInOtherElementSets = individual
						.getAllElementsInputSet(otherElement);
				allElementsInElementSets = individual
						.getAllElementsOutputSet(element);
				if (allElementsInOtherElementSets.contains(element)
						&& !allElementsInElementSets.contains(otherElement)) {

					individual.setInputSet(otherElement, removeConnectionsSet(
							individual.getInputSet(otherElement), individual
									.getInputSetsWithElement(otherElement,
											element), element));
					if (individual.getInputSet(otherElement).size() == 0) {
						set = new HNSet();
						subset = new HNSubSet();
						subset.addAll(individual.getStartTasks());
						set.add(subset);
						individual.setInputSet(otherElement, set);
					}
				}

			}
		}

		return individual;

	}

	private static HNSet removeConnectionsSet(HNSet setOtherElement,
			HNSet subSetsOtherElementThatContainsElement, int element) {
		HNSet newSet = null;
		HNSubSet newSubset = null;

		newSet = setOtherElement;

		newSet.removeAll(subSetsOtherElementThatContainsElement);

		for (int iSubsets = 0; iSubsets < subSetsOtherElementThatContainsElement
				.size(); iSubsets++) {
			newSubset = subSetsOtherElementThatContainsElement.get(iSubsets);
			newSubset.remove(element);
			if (newSubset.size() > 0) {
				newSet.add(newSubset);
			}
		}
		return newSet;

	}

	private static HNSet addConnectionsSet(HNSet setOtherElement, int element,
			Random gen) {
		HNSet newSet = setOtherElement;
		HNSubSet newSubSet = null;
		Iterator iterator = null;
		int size = 0;
		int setPosition = 0;
		boolean addToNewSubset = false;

		addToNewSubset = gen.nextBoolean();

		if (addToNewSubset || (newSet.size() == 0)) {
			// add 'element' to a new subset.
			newSubSet = new HNSubSet();
			newSubSet.add(element);
		} else {
			// randomly select a set and add 'element' to it.
			setPosition = gen.nextInt(newSet.size());
			newSubSet = newSet.get(setPosition);
			newSet.remove(newSubSet);
			newSubSet.add(element);
		}

		newSet.add(newSubSet);

		return newSet;
	}

	public static HeuristicsNet[] removeDuplicates(HeuristicsNet[] population) {

		HeuristicsNet[] cleanedPopulation = null;
		Vector auxVector = null;

		// removing dangling references to elements
		for (int i = 0; i < population.length; i++) {
			population[i] = removeDanglingElementReferences(population[i]);
		}

		// removing duplicate
		for (int i = 0; i < population.length; i++) {
			for (int j = i + 1; j < population.length; j++) {
				if (population[i] == null) {
					break;
				}
				if (population[j] == null) {
					continue;
				}

				if (population[i].toString().equals(population[j].toString())
						|| population[i].equals(population[j])) {
					population[j] = null;
				}
			}
		}

		auxVector = new Vector();
		for (int i = 0; i < population.length; i++) {
			if (population[i] != null) {
				auxVector.add(population[i]);
			}
		}

		cleanedPopulation = new HeuristicsNet[auxVector.size()];
		for (int i = 0; i < auxVector.size(); i++) {
			cleanedPopulation[i] = removeDanglingElementReferences((HeuristicsNet) auxVector
					.get(i));
		}
		return cleanedPopulation;
	}

	/**
	 * This methods matches the INPUT and OUTPUT sets of the individual.
	 * 
	 * @param individual
	 *            to check and correct if necessary
	 * @return individual without dangling element references
	 */
	public static HeuristicsNet removeDanglingElementReferences(
			HeuristicsNet individual) {

		HNSubSet set = null;
		int to = -1;

		// Phase 1 - match output with input set. Direction: Output -> Input
		// Reason: the marking is kept in the output subsets
		for (int from = 0; from < individual.size(); from++) {
			set = individual.getAllElementsOutputSet(from);
			for (int iSet = 0; iSet < set.size(); iSet++) {
				to = set.get(iSet);
				if (!individual.getAllElementsInputSet(to).contains(from)) {
					// remove from "from" outpuset subsets any reference to "to"
					individual.setOutputSet(from, HNSet
							.removeElementFromSubsets(individual
									.getOutputSet(from), to));
				}
			}
		}

		// Phase 2 - match input with output set. Direction: Output <- Input
		// Reason: These input elements are not used

		for (int from = 0; from < individual.size(); from++) {
			set = individual.getAllElementsInputSet(from);
			for (int iSet = 0; iSet < set.size(); iSet++) {
				to = set.get(iSet);
				if (!individual.getAllElementsOutputSet(to).contains(from)) {
					// remove from "iFrom" outpuset subsets any reference to
					// "iTo"
					individual.setInputSet(from, HNSet
							.removeElementFromSubsets(individual
									.getInputSet(from), to));
				}
			}
		}

		return individual;
	}

	public static void decreasinglyOrderPopulation(HeuristicsNet[] population) {
		HeuristicsNet individual = null;

		Arrays.sort(population);

		// ordering the population in descending order
		for (int i = 0, j = population.length - 1; i < (j - i); i++) {
			individual = population[i];
			population[i] = population[j - i];
			population[j - i] = individual;
		}

	}

	/**
	 * Build a HNSet that will be the INPUT or OUTPUT set of an individual's
	 * workflow model element. The INPUT/OUTPUT sets are general because their
	 * subsets do not need to be a partition set of the set of tasks in the log.
	 * 
	 * @param startOrEndEntry
	 * @param inOrOutRowOrColumn
	 * @param generator
	 *            random generator.
	 * @return
	 */
	public static HNSet buildSet(double startOrEndEntry,
			DoubleMatrix1D inOrOutRowOrColumn, Random generator) {
		HNSet hs = null;
		HNSubSet[] arraySubsets = null;

		int numberNonNull = 0;
		int numSubsetsToInsertElement = 0;
		int indexArray = 0;

		hs = new HNSet();

		if (startOrEndEntry <= 0) {
			// element is not connected to the source/end place in the workflow.
			numberNonNull = inOrOutRowOrColumn.cardinality();
			arraySubsets = new HNSubSet[numberNonNull];
			for (int i = 0; i < inOrOutRowOrColumn.size(); i++) {
				if (inOrOutRowOrColumn.get(i) > 0) {
					numSubsetsToInsertElement = generator
							.nextInt(arraySubsets.length) + 1;
					while (numSubsetsToInsertElement > 0) {
						indexArray = generator.nextInt(numberNonNull);
						if (arraySubsets[indexArray] == null) {
							arraySubsets[indexArray] = new HNSubSet();
						}
						arraySubsets[indexArray].add(i);
						numSubsetsToInsertElement--;
					}
				}
			}
			// fill in HashSet
			for (int i = 0; i < numberNonNull; i++) {
				if (arraySubsets[i] != null) {
					hs.add(arraySubsets[i]);
				}
			}

		}

		return hs;

	}

	/**
	 * Build a HNSet that will be the INPUT or OUTPUT set of an individual's
	 * workflow model element. The INPUT/OUTPUT sets are general because their
	 * subsets do not need to be a partition set of the set of tasks in the log.
	 * 
	 * @param startOrEndEntry
	 * @param inOrOutRowOrColumn
	 * @param generator
	 *            random generator.
	 * @return
	 */
	public static HNSet buildHNSet(double startOrEndEntry,
			DoubleMatrix1D inOrOutRowOrColumn, Random generator) {
		HNSet hs = null;
		HNSubSet[] arraySubsets = null;

		int numberNonNull = 0;
		int numSubsetsToInsertElement = 0;
		int indexArray = 0;

		hs = new HNSet();

		if (startOrEndEntry <= 0) {
			// element is not connected to the source/end place in the workflow.
			numberNonNull = inOrOutRowOrColumn.cardinality();
			arraySubsets = new HNSubSet[numberNonNull];
			for (int i = 0; i < inOrOutRowOrColumn.size(); i++) {
				if (inOrOutRowOrColumn.get(i) > 0) {
					numSubsetsToInsertElement = generator
							.nextInt(arraySubsets.length) + 1;
					while (numSubsetsToInsertElement > 0) {
						indexArray = generator.nextInt(numberNonNull);
						if (arraySubsets[indexArray] == null) {
							arraySubsets[indexArray] = new HNSubSet();
						}
						arraySubsets[indexArray].add(i);
						numSubsetsToInsertElement--;
					}
				}
			}
			// fill in HashSet
			for (int i = 0; i < numberNonNull; i++) {
				if (arraySubsets[i] != null) {
					hs.add(arraySubsets[i]);
				}
			}

		}

		return hs;

	}

	public static DoubleMatrix1D randomlyAddEntry(DoubleMatrix1D matrix,
			Random generator) {

		Vector v = new Vector();
		DoubleMatrix1D newMatrix = matrix;
		int positionToAddEntry = 0;

		for (int i = 0; i < matrix.size(); i++) {
			if (matrix.get(i) == 0) {
				v.add(new Integer(i));
			}
		}

		positionToAddEntry = generator.nextInt(v.size());

		newMatrix.set(((Integer) v.get(positionToAddEntry)).intValue(), 1);

		return newMatrix;
	}

	public static DoubleMatrix1D randomlyRemoveEntry(DoubleMatrix1D matrix,
			Random generator) {

		Vector v = new Vector();
		DoubleMatrix1D newMatrix = matrix;
		int positionToRemoveEntry = 0;

		for (int i = 0; i < matrix.size(); i++) {
			if (matrix.get(i) != 0) {
				v.add(new Integer(i));
			}
		}

		positionToRemoveEntry = generator.nextInt(v.size());

		newMatrix.set(((Integer) v.get(positionToRemoveEntry)).intValue(), 0);

		return newMatrix;
	}

	/**
	 * This method randomly selects and returns an element in <i>subsets<i/>
	 * 
	 * @return a randomly selected Integer in <i>subsets</i>. If "subsets" is
	 *         empty, this method returns null;
	 */

	public static Integer selectAnElementInSubset(TreeSet subset,
			Random generator) {

		Integer element = null;
		int size = 0;
		int elementIndex = 0;
		Iterator iElements = null;

		size = subset.size();

		if (size > 0) {
			elementIndex = generator.nextInt(size);
			iElements = subset.iterator();
			for (int i = 0; i < elementIndex; i++) {
				iElements.next();
			}

			element = (Integer) iElements.next();
			iElements = null;
		}

		return element;

	}

	/**
	 * This method randomly selects and returns an element in <i>sets<i/>
	 * 
	 * @return a randomly selected subset in <i>sets</i>. If "sets" is empty,
	 *         this method returns null;
	 */
	public static TreeSet selectSubset(HashSet sets, Random generator) {

		TreeSet subset = null;
		Iterator iSets = null;
		int size = 0;
		int subsetIndex = 0;

		size = sets.size();

		if (size > 0) {

			subsetIndex = generator.nextInt(size);
			iSets = sets.iterator();
			for (int i = 0; i < subsetIndex; i++) {
				iSets.next();
			}

			subset = (TreeSet) iSets.next();
			iSets = null;
		}

		return subset;

	}

	public static HashSet removeElementFromSubsets(HashSet set, Integer element) {

		TreeSet ts = null;
		Iterator i = null;

		ts = new TreeSet();
		ts.add(element);
		// removing singletons (I had to do this way because the set.remove(ts)
		// if ts.size()==1
		// didn't work... neither i.remove();
		ts = new TreeSet();
		ts.add(element);
		set.remove(ts);

		// removing element from the remaining sets...
		i = set.iterator();
		while (i.hasNext()) {
			ts = (TreeSet) i.next();
			if (ts.contains(element)) {
				ts.remove(element);
			}
		}

		return set;
	}

	/**
	 * This method assumes that the population is INCREASINGLY order by the
	 * fitness measure.
	 * 
	 * @return all the individuals that have a fitness X, where X is the best
	 *         fitness in this. This method returns null if the population is
	 *         empty or null. populations.
	 */

	public static HeuristicsNet[] extractBestIndividuals(
			HeuristicsNet[] population) {
		HashSet bestIndividuals = null;
		HeuristicsNet[] bestIndividualsResult = null;
		double fitness = 0;
		int index = 0;
		Iterator iBestIndividuals = null;

		if (population != null && population.length > 0) {
			bestIndividuals = new HashSet();
			index = population.length - 1;
			do {
				fitness = population[index].getFitness();
				bestIndividuals.add(population[index]);
				index--;
			} while (index >= 0 && fitness == population[index].getFitness());

			bestIndividualsResult = new HeuristicsNet[bestIndividuals.size()];

			iBestIndividuals = bestIndividuals.iterator();
			index = 0;
			while (iBestIndividuals.hasNext()) {
				bestIndividualsResult[index] = (HeuristicsNet) iBestIndividuals
						.next();
				index++;
			}
		}

		return bestIndividualsResult;

	}

}
