package org.processmining.mining.snamining.miningoperation.workingtogether;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.mining.snamining.SocialNetworkOptions;
import org.processmining.mining.snamining.miningoperation.BasicOperation;
import org.processmining.mining.snamining.miningoperation.OperationFactory;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class Workingtogether_DWTC extends BasicOperation {

	// Consider distance with causality
	public Workingtogether_DWTC(LogSummary summary, LogReader log) {
		super(summary, log);
	};

	public DoubleMatrix2D calculation() {
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(users.length,
				users.length, 0);
		BasicOperation baseObject = OperationFactory
				.getOperation(SocialNetworkOptions.HANDOVER_OF_WORK
						+ SocialNetworkOptions.CONSIDER_CAUSALITY, summary, log);
		DoubleMatrix2D imsiD = baseObject.calculation(0.5, 10);
		for (int i = 0; i < users.length; i++)
			for (int j = i; j < users.length; j++) {
				D.set(i, j, (imsiD.get(i, j) + imsiD.get(j, i)) / 2);
				D.set(j, i, D.get(i, j));
			}
		return D;
	};
}
