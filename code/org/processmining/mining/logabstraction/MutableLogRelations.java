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
 * @author Peter van den Brand
 * @version 1.0
 */

public class MutableLogRelations extends LogRelationsImpl {

	public MutableLogRelations(DoubleMatrix2D causal, DoubleMatrix2D parallel,
			DoubleMatrix1D end, DoubleMatrix1D start, DoubleMatrix1D loops,
			LogEvents events) {
		super(causal.copy(), parallel.copy(), end.copy(), start.copy(), loops
				.copy(), events);
	}

	public MutableLogRelations(LogRelations relations) {
		this(relations.getCausalFollowerMatrix(),
				relations.getParallelMatrix(), relations.getEndInfo(),
				relations.getStartInfo(), relations.getOneLengthLoopsInfo(),
				relations.getLogEvents());
	}

	public void setCausalFollower(int a, int b, double value) {
		causal.set(a, b, value);
	}

	public void setParallel(int a, int b, double value) {
		parallel.set(a, b, value);
	}

	public void setEnd(int element, double value) {
		end.set(element, value);
	}

	public void setStart(int element, double value) {
		start.set(element, value);
	}

	public void setOneLengthLoops(int element, double value) {
		loops.set(element, value);
	}
}
