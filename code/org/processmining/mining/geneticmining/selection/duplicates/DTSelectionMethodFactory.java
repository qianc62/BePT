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

import java.util.Random;

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

public class DTSelectionMethodFactory {
	public DTSelectionMethodFactory() {
	}

	public static String[] getAllSelectionMethodsTypes() {
		return new String[] { "Tournament", "Tournament 5" };
	}

	public static SelectionMethod getSelectionMethods(
			int indexSelectionMethodType, Random generator) {
		SelectionMethod object = null;
		switch (indexSelectionMethodType) {
		case 0:
			object = new DTTournamentSelection(generator);
			break;
		case 1:
			object = new DTTournamentSelection5(generator);
			break;
		}

		return object;
	}

}
