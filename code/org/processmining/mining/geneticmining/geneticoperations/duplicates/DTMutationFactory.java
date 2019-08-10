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

import org.processmining.mining.geneticmining.geneticoperations.Mutation;

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
public class DTMutationFactory {
	public DTMutationFactory() {
	}

	public static String[] getAllMutationTypes() {
		return new String[] { "Enhanced" };
	}

	public static Mutation getMutation(int indexMutationType, Random generator,
			double mutationRate) {
		Mutation object = null;
		switch (indexMutationType) {
		case 0:
			object = new DTEnhancedMutation(generator, mutationRate);
			break;
		}
		return object;
	}

}
