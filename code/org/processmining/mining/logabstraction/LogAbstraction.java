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

import java.io.IOException;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author not attributable
 * @version 1.0
 */

public interface LogAbstraction {

	/**
	 * This function returns a matrix with succession relations
	 * 
	 * @param distance
	 *            The distance to look at
	 * @return The resulting matrix D[i][j] gives the number of times the
	 *         ModelElement with number i is followed by j at a given distance.
	 *         If the distance is 1 then the direct successors are given
	 * 
	 */
	public DoubleMatrix2D getFollowerInfo(int distance) throws IOException;

	public DoubleMatrix1D getStartInfo() throws IOException;

	public DoubleMatrix1D getEndInfo() throws IOException;

	public DoubleMatrix2D getCloseInInfo(int distance) throws IOException;

}
