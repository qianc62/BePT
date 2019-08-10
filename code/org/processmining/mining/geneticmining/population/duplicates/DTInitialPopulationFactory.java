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

import org.processmining.framework.log.LogReader;
import org.processmining.mining.geneticmining.population.BuildPopulation;

/**
 * <p>
 * Title: Duplicate Tasks Initial Population Factory
 * </p>
 * <p>
 * Description: Factory to build an initial population based on different
 * heuristics.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class DTInitialPopulationFactory {

	public DTInitialPopulationFactory() {
	}

	/**
	 * The initial population types are:
	 * <p>
	 * 1) <b>Causal Heuristics (Duplicates+Arcs):</b> Use heuristics to set the
	 * amount of duplicates per task and the arcs among the tasks. The amount of
	 * duplicates is set based on the causal relation. The arcs are set based on
	 * the follows relation.
	 * </p>
	 * <p>
	 * 2) <b>Causal Heuristics (Duplicates):</b> Uses the <i>causal</i> relation
	 * to set the amount of duplicates per task, but the arcs are randomly set.
	 * </p>
	 * <p>
	 * 3) <b>Follows Heuristics (Duplicates+Arcs):</b> Uses the <i>follows</i>
	 * relation to set the amount of duplicates per task and the arcs among the
	 * tasks.
	 * </p>
	 * 
	 * @return String[] With the supported heuristics to build the initial
	 *         population.
	 */
	public static String[] getInitialPopulationTypes() {
		return new String[] { "Causal Heuristics (Duplicates+Arcs)",
				"Causal Heuristics (Duplicates)",
				"Follows Heuristics (Duplicates+Arcs)" };// ,
		// "No Duplicate Tasks"};
	}

	/**
	 * This method provides an object to build an initial population.
	 * 
	 * @param indexPopulationType
	 *            int One of the types returned by method
	 *            <i>getInitialPopulationTypes()</i>
	 * @param gen
	 *            Random The generator to be used.
	 * @param log
	 *            LogReader The log to be used.
	 * @param power
	 *            double The power value to be used.
	 * @return BuildPopulation
	 */
	public static BuildPopulation getPopulation(int indexPopulationType,
			Random gen, LogReader log, double power) {
		BuildPopulation object = null;
		DTGeneticMiningMatrices genMinMatrices = null;

		switch (indexPopulationType) {
		case 0:
			genMinMatrices = new DTGeneticMiningMatricesWithHeuristicsTasksArcs(
					gen, log, power);
			object = new DTBuildInitialPopulation(gen, log, genMinMatrices);
			break;
		case 1:
			genMinMatrices = new DTGeneticMiningMatricesWithHeuristics(gen, log);
			object = new DTBuildInitialPopulation(gen, log, genMinMatrices);
			break;
		case 2:
			genMinMatrices = new DTGeneticMiningMatricesWithMinimalHeuristics(
					gen, log, power);
			object = new DTBuildInitialPopulation(gen, log, genMinMatrices);
			break;
		// case 3:
		// genMinMatrices = new DTGeneticMiningMatricesNoDuplicateTasks(gen,
		// log, power);
		// object = new DTBuildInitialPopulation(gen, log, genMinMatrices);
		// break;

		}

		return object;
	}

}
