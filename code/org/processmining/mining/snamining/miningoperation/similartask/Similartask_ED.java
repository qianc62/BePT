package org.processmining.mining.snamining.miningoperation.similartask;

import cern.colt.matrix.DoubleMatrix2D;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.mining.snamining.miningoperation.UtilOperation;

public class Similartask_ED extends SimilartaskBase {

	// consider multiple transfer

	public Similartask_ED(LogSummary summary, LogReader log) {
		super(summary, log);
	};

	public DoubleMatrix2D calculation() {
		DoubleMatrix2D OTMatrix = super.makeOTMatrix();

		return UtilOperation.euclidiandistance(OTMatrix);
	};

}
