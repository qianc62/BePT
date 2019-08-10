package org.processmining.mining.snamining.miningoperation.handover;

import java.util.Arrays;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.mining.snamining.miningoperation.BasicOperation;
import org.processmining.mining.snamining.miningoperation.UtilOperation;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class Handover_ICCDIM extends BasicOperation {

	// ignore casuality, consider direct succession, ignore multiple appearance

	public Handover_ICCDIM(LogSummary summary, LogReader log) {
		super(summary, log);
	};

	public DoubleMatrix2D calculation() {
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(users.length,
				users.length, 0);

		Iterator it = log.instanceIterator();

		while (it.hasNext()) {

			ProcessInstance pi = (ProcessInstance) it.next();

			Iterator ates = pi.getAuditTrailEntryList().iterator();
			AuditTrailEntry ate, ate2;

			DoubleMatrix2D m = DoubleFactory2D.sparse.make(users.length,
					users.length, 0);

			if (ates.hasNext()) {
				ate = (AuditTrailEntry) ates.next();

				while (ates.hasNext()) {
					int row, column;

					ate2 = (AuditTrailEntry) ates.next();
					if (ate.getOriginator() != null
							&& ate2.getOriginator() != null) {
						row = Arrays.binarySearch(users, ate.getOriginator());
						column = Arrays.binarySearch(users, ate2
								.getOriginator());

						if (row < 0 || column < 0) {
							throw new Error(
									"Implementation error: couldn't find user in the user list: "
											+ ate.getOriginator() + " or "
											+ ate2.getOriginator());
						}

						// we have that ate2 follows ate
						m.set(row, column, 1.0);
					}
					ate = ate2;
				}
			}

			for (int i = 0; i < users.length; i++)
				for (int j = 0; j < users.length; j++)
					D.set(i, j, D.get(i, j) + m.get(i, j));
		}
		return UtilOperation
				.normalize(D, summary.getNumberOfProcessInstances());
	};
}
