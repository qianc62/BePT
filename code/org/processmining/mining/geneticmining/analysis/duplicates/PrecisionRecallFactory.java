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
public class PrecisionRecallFactory {

	public static final int IND_BEHAVIORAL_PR = 0;
	public static final int IND_STRUCTURAL_PR = 1;
	public static final int IND_DUPLICATES_PR = 2;

	public PrecisionRecallFactory() {
	}

	public static String[] getAllPrecisionRecallTypes() {
		return new String[] { "Behavioral Precision/Recall",
				"Structural Precision/Recall", "Duplicates Precision/Recall" };
	}

	public static PrecisionRecall getPrecisionRecall(int indexFitnessType,
			LogReader log, HeuristicsNet baseHN, HeuristicsNet foundHN)
			throws Exception {

		PrecisionRecall object = null;

		switch (indexFitnessType) {
		case IND_BEHAVIORAL_PR:
			object = new TraceParsing(log, baseHN, foundHN);
			break;
		case IND_STRUCTURAL_PR:
			object = new ConnectionEquivalent(baseHN, foundHN);
			break;
		case IND_DUPLICATES_PR:
			object = new DuplicatesEquivalent(baseHN, foundHN);
			break;
		}

		return object;
	}

}
