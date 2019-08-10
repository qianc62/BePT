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

package org.processmining.mining.geneticmining.population;

import java.util.Random;

import org.processmining.framework.log.LogReader;

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
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class InitialPopulationFactory {

	public InitialPopulationFactory() {
	}

	public static String[] getInitialPopulationTypes() {
		return new String[] { "Possible Duplicates", "No Duplicates",
				"Without Heuristics" };
		// ,"Guided Start/End", "Using follows info",
		// "Using follow/start/end info"};
	}

	public static BuildPopulation getPopulation(int indexPopulationType,
			Random gen, LogReader log, double power) {
		BuildPopulation object = null;
		GeneticMiningMatrices genMinMatrices = null;

		switch (indexPopulationType) {
		case 0:
			genMinMatrices = new GeneticMiningMatricesWithHeuristics(gen, log,
					power);
			object = new BuildInitialPopulation(gen, log, genMinMatrices);
			break;
		case 1:
			genMinMatrices = new GeneticMiningMatricesWithHeuristics(gen, log,
					power);
			object = new BuildInitialPopulationWithoutDuplicateIndividuals(gen,
					log, genMinMatrices);
			break;
		case 2:
			genMinMatrices = new GeneticMiningMatricesWithoutHeuristics(gen,
					log);
			object = new BuildInitialPopulation(gen, log, genMinMatrices);
			break;
		// case 3:
		// genMinMatrices = new GeneticMiningMatricesWithGuidedStartEnd(gen,
		// log, power);
		// object = new BuildInitialPopulation(size, gen, log, genMinMatrices);
		// break;
		// case 4:
		// genMinMatrices = new GeneticMiningMatricesBasedOnFollowsRelation(gen,
		// log);
		// object = new BuildInitialPopulation(size, gen, log, genMinMatrices);
		// break;
		// case 5:
		// genMinMatrices = new
		// GeneticMiningMatricesBasedOnFollowsStartEndRelation(gen, log);
		// object = new BuildInitialPopulation(size, gen, log, genMinMatrices);
		// break;
		}

		return object;
	}

}
