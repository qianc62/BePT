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

public class CrossoverFactory {
	public CrossoverFactory() {
	}

	public static String[] getAllCrossoverTypes() {
		return new String[] { "Local One Point", "Variable Local One Point",
				"Fine Granularity", "Enhanced" };// , "SuperEnhanced"};
	}

	public static Crossover getCrossover(int indexCrossoverType,
			Random generator) {
		Crossover object = null;
		switch (indexCrossoverType) {
		case 0:
			object = new LocalOnePointCrossover(generator);
			break;
		case 1:
			object = new VariableLocalOnePointCrossover(generator);
			break;
		case 2:
			object = new FineGranularityCrossover(generator);
			break;
		case 3:
			object = new EnhancedCrossover(generator);
			break;
		// case 4:
		// object = new SuperEnhancedCrossover(generator);
		// break;

		}
		return object;
	}
}
