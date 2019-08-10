package org.processmining.mining.snamining.miningoperation.handover;

import java.util.ArrayList;
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

public class Handover_ICIDCM extends BasicOperation {

	// ignore casuality, ignore direct succession, consider multiple appearance
	public Handover_ICIDCM(LogSummary summary, LogReader log) {
		super(summary, log);
	};

	public DoubleMatrix2D calculation(double beta, int depth) {
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(users.length,
				users.length, 0);
		double normal = 0;

		Iterator it = log.instanceIterator();

		while (it.hasNext()) {

			ProcessInstance pi = (ProcessInstance) it.next();

			Iterator ates = pi.getAuditTrailEntryList().iterator();
			AuditTrailEntry ate, ate2;

			if (ates.hasNext()) {

				int flag = 0;
				ate = (AuditTrailEntry) ates.next();

				DoubleMatrix2D m = DoubleFactory2D.sparse.make(users.length,
						users.length, 0);
				ArrayList users_list_by_entries = new ArrayList();

				while (ates.hasNext()) {

					users_list_by_entries.add(ate.getOriginator());

					int row, column;

					ate2 = (AuditTrailEntry) ates.next();
					flag++;
					normal++;

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

					if (ate2.getOriginator() != null) {
						column = Arrays.binarySearch(users, ate2
								.getOriginator());

						for (int i = 0; i < depth - 1 && i < flag - 1; i++) {
							if (users_list_by_entries.get(users_list_by_entries
									.size()
									- i - 2) != null) {
								row = Arrays.binarySearch(users,
										users_list_by_entries
												.get(users_list_by_entries
														.size()
														- i - 2));

								D.set(row, column, D.get(row, column)
										+ Math.pow(beta, i + 1));
							}
							normal += Math.pow(beta, i + 1);
						}
					}
					ate = ate2;
				}
			}
		}

		return UtilOperation.normalize(D, normal);
	};
}
