package org.processmining.mining.snamining.miningoperation.workingtogether;

import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.mining.snamining.miningoperation.BasicOperation;
import org.processmining.mining.snamining.miningoperation.UtilOperation;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class Workingtogether_SAR extends BasicOperation {

	// SIMULTANEOUS_APPEARANCE_RATIO

	public Workingtogether_SAR(LogSummary summary, LogReader log) {
		super(summary, log);
	};

	public DoubleMatrix2D calculation() {
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(users.length,
				users.length, 0);
		int[] number = new int[users.length];

		Iterator it = log.instanceIterator();

		while (it.hasNext()) {

			ProcessInstance pi = (ProcessInstance) it.next();

			for (int i = 0; i < users.length; i++) {
				AuditTrailEntryList ates1 = pi.getAuditTrailEntryList();
				if (UtilOperation.isInCase(ates1, users[i]))
					number[i]++;

				for (int j = 0; j < users.length; j++) {
					AuditTrailEntryList ates = pi.getAuditTrailEntryList();
					if (UtilOperation.isInCase(ates, users[i], users[j]))
						D.set(i, j, D.get(i, j) + 1.0);
				}
			}
		}

		for (int i = 0; i < users.length; i++) {
			for (int j = 0; j < users.length; j++) {
				D.set(i, j, D.get(i, j) / number[i]);
			}
		}

		return D;

	};
}
