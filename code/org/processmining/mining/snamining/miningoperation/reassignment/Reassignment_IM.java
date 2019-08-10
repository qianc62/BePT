package org.processmining.mining.snamining.miningoperation.reassignment;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.mining.snamining.miningoperation.BasicOperation;
import org.processmining.mining.snamining.miningoperation.UtilOperation;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class Reassignment_IM extends BasicOperation {

	// ignore multiple transfer

	public Reassignment_IM(LogSummary summary, LogReader log) {
		super(summary, log);
	};

	public DoubleMatrix2D calculation() {
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(users.length,
				users.length, 0);
		int count = 0;

		Iterator it = log.instanceIterator();

		while (it.hasNext()) {

			ProcessInstance pi = (ProcessInstance) it.next();
			AuditTrailEntryList atesl = pi.getAuditTrailEntryList();

			Iterator ates = atesl.iterator();

			AuditTrailEntry ate = null, ate2 = null;

			int index = 0;
			count += atesl.size() - 1;

			if (ates.hasNext()) {
				ate = (AuditTrailEntry) ates.next();
				index++;

				while (ates.hasNext()) {
					int row, column;
					if (ate.getOriginator() != null) {
						if (ate.getType() != null
								&& ate.getType().equals("reassign")) {
							for (int i = index; i < atesl.size(); i++) {
								try {
									ate2 = atesl.get(i);
								} catch (IndexOutOfBoundsException ee) {
								} catch (IOException ee) {
								}
								;
								if (ate.getOriginator() == null)
									continue;
								if (ate.getElement().equals(ate2.getElement())) {
									row = Arrays.binarySearch(users, ate
											.getOriginator());
									column = Arrays.binarySearch(users, ate2
											.getOriginator());

									if (row < 0 || column < 0) {
										throw new Error(
												"Implementation error: couldn't find user in the user list: "
														+ ate.getOriginator()
														+ " or "
														+ ate2.getOriginator());
									}
									D.set(row, column, 1);
									break;
								}
							}
						}
					}

					ate = (AuditTrailEntry) ates.next();
					index++;
				}
			}
		}
		return UtilOperation
				.normalize(D, summary.getNumberOfProcessInstances());
	};
}
