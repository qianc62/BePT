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

public class LogRelationsImpl implements LogRelations {

	protected DoubleMatrix2D causal;
	protected DoubleMatrix2D parallel;
	protected DoubleMatrix1D end;
	protected DoubleMatrix1D start;
	protected DoubleMatrix1D loops;
	protected LogEvents events;

	public LogRelationsImpl(DoubleMatrix2D causal, DoubleMatrix2D parallel,
			DoubleMatrix1D end, DoubleMatrix1D start, DoubleMatrix1D loops,
			LogEvents events) {
		this.causal = causal;
		this.parallel = parallel;
		this.end = end;
		this.start = start;
		this.loops = loops;
		this.events = events;
	}

	public LogRelationsImpl(LogRelations relations) {
		this(relations.getCausalFollowerMatrix(),
				relations.getParallelMatrix(), relations.getEndInfo(),
				relations.getStartInfo(), relations.getOneLengthLoopsInfo(),
				relations.getLogEvents());
	}

	public DoubleMatrix2D getCausalFollowerMatrix() {
		return causal;
	}

	public DoubleMatrix2D getParallelMatrix() {
		return parallel;
	}

	public DoubleMatrix1D getEndInfo() {
		return end;
	}

	public DoubleMatrix1D getStartInfo() {
		return start;
	}

	public DoubleMatrix1D getOneLengthLoopsInfo() {
		return loops;
	}

	public int getNumberElements() {
		return causal.columns();
	}

	public LogEvents getLogEvents() {
		return events;
	}
}
