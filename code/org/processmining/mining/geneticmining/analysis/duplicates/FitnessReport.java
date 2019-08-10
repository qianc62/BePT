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

package org.processmining.mining.geneticmining.analysis.duplicates;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.mining.geneticmining.fitness.FitnessFactory;
import org.processmining.mining.geneticmining.fitness.duplicates.DTFitnessFactory;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class FitnessReport {
	HeuristicsNet[] pop = null;

	public FitnessReport(LogReader log, HeuristicsNet net, int indexFitnessType) {
		pop = new HeuristicsNet[] { net.copyNet() };
		pop = FitnessFactory.getFitness(indexFitnessType, log,
				DTFitnessFactory.ALL_FITNESS_PARAMETERS).calculate(pop);
	}

	public double getFitness() {
		return pop[0].getFitness();
	}

	public HeuristicsNet getNet() {
		return pop[0];
	}
}
