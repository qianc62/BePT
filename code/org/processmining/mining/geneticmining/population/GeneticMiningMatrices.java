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

package org.processmining.mining.geneticmining.population;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

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

public interface GeneticMiningMatrices {

	public DoubleMatrix2D getCausalMatrix();

	public DoubleMatrix1D getEndMatrix();

	public DoubleMatrix1D getStartMatrix();

	public void rebuildAllMatrices();

	public DoubleMatrix2D rebuildCausalMatrix();

	public DoubleMatrix1D rebuildStartMatrix();

	public DoubleMatrix1D rebuildEndMatrix();
}