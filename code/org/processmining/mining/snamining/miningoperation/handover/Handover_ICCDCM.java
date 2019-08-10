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

public class Handover_ICCDCM extends BasicOperation {

	// ignore casuality, consider direct succession, consider multiple
	// appearance

	public Handover_ICCDCM(LogSummary summary, LogReader log) {
		super(summary, log);
	};

	public DoubleMatrix2D calculation() {
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(users.length,
				users.length, 0);
		int count = 0;

		Iterator it = log.instanceIterator();

		while (it.hasNext()) {

			ProcessInstance pi = (ProcessInstance) it.next();

			Iterator ates = pi.getAuditTrailEntryList().iterator();
			AuditTrailEntry ate, ate2;

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

						D.set(row, column, D.get(row, column) + 1.0);
					}

					count++;
					ate = ate2;
				}
			}
		}
		return UtilOperation.normalize(D, count);
	};
}
