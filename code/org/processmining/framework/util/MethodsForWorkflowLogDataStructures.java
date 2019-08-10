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

package org.processmining.framework.util;

import org.processmining.framework.log.ProcessInstance;

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

public class MethodsForWorkflowLogDataStructures {
	public static final String NUM_SIMILAR_INSTANCES = "numSimilarInstances";

	public static int getNumberSimilarProcessInstances(ProcessInstance pi) {
		int numSimilarPIs = 1;
		if (pi.getAttributes().containsKey(NUM_SIMILAR_INSTANCES)) {
			try {
				numSimilarPIs = Integer.parseInt((String) pi.getAttributes()
						.get(NUM_SIMILAR_INSTANCES));
			} catch (NumberFormatException nfe) {
			}
		}
		return numSimilarPIs;
	}
}
