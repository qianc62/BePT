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

package org.processmining.mining.geneticmining.geneticoperations.duplicates;

import java.util.Random;

import org.processmining.mining.geneticmining.geneticoperations.Crossover;

/**
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class DTCrossoverFactory {
	public DTCrossoverFactory() {
	}

	public static String[] getAllCrossoverTypes() {
		return new String[] { "Enhanced", "Duplicates Enhanced" };
	}

	public static Crossover getCrossover(int indexCrossoverType,
			Random generator) {
		Crossover object = null;
		switch (indexCrossoverType) {
		case 0:
			object = new DTEnhancedCrossover(generator);
			break;
		case 1:
			object = new DTDuplicatesCrossover(generator);
			break;
		}
		return object;
	}
}
