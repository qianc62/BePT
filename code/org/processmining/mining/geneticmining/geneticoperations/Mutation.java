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

import org.processmining.framework.models.heuristics.HeuristicsNet;

/**
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public interface Mutation {
	public HeuristicsNet doMutation(HeuristicsNet ind);
}
