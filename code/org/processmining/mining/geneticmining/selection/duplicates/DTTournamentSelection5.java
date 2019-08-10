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

package org.processmining.mining.geneticmining.selection.duplicates;

import java.util.Arrays;
import java.util.Random;

import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.mining.geneticmining.selection.SelectionMethod;

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

public class DTTournamentSelection5 implements SelectionMethod {

	private static final int NUM_INDIVIDUALS = 5;
	private Random generator = null;

	public DTTournamentSelection5(Random gen) {
		this.generator = gen;
	}

	public HeuristicsNet[] select(HeuristicsNet[] population) {

		HeuristicsNet[] selectedIndividuals = null;
		HeuristicsNet[] result = null;

		selectedIndividuals = new HeuristicsNet[NUM_INDIVIDUALS];

		for (int i = 0; i < selectedIndividuals.length; i++) {
			selectedIndividuals[i] = population[generator
					.nextInt(population.length)].copyNet();
		}

		Arrays.sort(selectedIndividuals);

		result = new HeuristicsNet[1];

		result[0] = selectedIndividuals[selectedIndividuals.length - 1]
				.copyNet();

		selectedIndividuals = null;

		return result;
	}

}
