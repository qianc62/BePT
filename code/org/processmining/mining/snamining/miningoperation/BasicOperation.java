package org.processmining.mining.snamining.miningoperation;

import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;

import cern.colt.matrix.DoubleMatrix2D;

public abstract class BasicOperation {
	protected LogSummary summary;
	protected String[] users;
	protected LogEvents modelElements;
	protected String[] elements;
	protected LogReader log;

	public BasicOperation(LogSummary summary, LogReader log) {
		this.users = summary.getOriginators();
		this.modelElements = summary.getLogEvents();
		this.elements = summary.getModelElements();
		this.summary = summary;
		this.log = log;
	}

	public DoubleMatrix2D calculation(double beta, int depth) {
		return calculation();
	};

	public DoubleMatrix2D calculation() {
		return null;
	};
}
