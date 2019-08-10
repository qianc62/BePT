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

package org.processmining.mining.logabstraction;

import org.processmining.framework.log.LogEvents;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author not attributable
 * @version 1.0
 */

public interface LogRelations {

	public DoubleMatrix2D getCausalFollowerMatrix();

	public DoubleMatrix2D getParallelMatrix();

	public DoubleMatrix1D getEndInfo();

	public DoubleMatrix1D getStartInfo();

	public DoubleMatrix1D getOneLengthLoopsInfo();

	public int getNumberElements();

	public LogEvents getLogEvents();
}
