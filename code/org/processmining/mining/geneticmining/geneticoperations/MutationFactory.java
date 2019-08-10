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
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class MutationFactory {
	public MutationFactory() {
	}

	public static String[] getAllMutationTypes() {
		return new String[] { "All Elements", "Partition Redefinition",
				"Enhanced" };
	}

	public static Mutation getMutation(int indexMutationType, Random generator,
			double mutationRate) {
		Mutation object = null;
		switch (indexMutationType) {
		case 0:
			object = new AllElementsMutation(generator, mutationRate);
			break;
		case 1:
			object = new PartitionRedefinitionMutation(generator, mutationRate);
			break;
		case 2:
			object = new EnhancedMutation(generator, mutationRate);
			break;
		}
		return object;
	}

}