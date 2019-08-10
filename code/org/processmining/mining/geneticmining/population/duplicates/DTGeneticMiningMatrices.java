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

package org.processmining.mining.geneticmining.population.duplicates;

import org.processmining.framework.models.heuristics.HNSubSet;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public interface DTGeneticMiningMatrices {

	public DoubleMatrix2D getCausalMatrix();

	public DoubleMatrix1D getEndMatrix();

	public DoubleMatrix1D getStartMatrix();

	public void rebuildAllMatrices();

	public DoubleMatrix2D rebuildCausalMatrix();

	public DoubleMatrix1D rebuildStartMatrix();

	public DoubleMatrix1D rebuildEndMatrix();

	public int[] getDuplicatesMapping();

	public HNSubSet[] getReverseDuplicatesMapping();
}
