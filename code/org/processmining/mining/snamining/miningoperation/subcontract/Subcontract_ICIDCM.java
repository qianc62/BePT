package org.processmining.mining.snamining.miningoperation.subcontract;

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

public class Subcontract_ICIDCM extends BasicOperation {

	// ignore casuality, ignore direct succession, consider multiple appearance

	public Subcontract_ICIDCM(LogSummary summary, LogReader log) {
		super(summary, log);
	};

	public DoubleMatrix2D calculation(double beta, int depth) {
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(users.length,
				users.length, 0);

		double normal = 0;

		Iterator it = log.instanceIterator();

		while (it.hasNext()) {

			ProcessInstance pi = (ProcessInstance) it.next();
			AuditTrailEntryList ates = pi.getAuditTrailEntryList();

			if (ates.size() < 3)
				continue;

			int minK = 0;
			if (ates.size() < depth)
				minK = ates.size();
			else
				minK = depth + 1;

			if (minK < 3)
				minK = 3;

			for (int k = 2; k < minK; k++) {

				normal += Math.pow(beta, k - 2) * (ates.size() - k) * (k - 1);
				DoubleMatrix2D m = DoubleFactory2D.sparse.make(users.length,
						users.length, 0);

				for (int i = 0; i < ates.size() - k; i++) {
					try {
						AuditTrailEntry ate, ate3;
						ate = ates.get(i);
						ate3 = ates.get(i + k);

						if (ate.getOriginator() != null
								&& ate3.getOriginator() != null) {
							if (ate.getOriginator()
									.equals(ate3.getOriginator()))
								for (int j = i + 1; j < i + k; j++) {
									AuditTrailEntry ate2;
									ate2 = ates.get(j);
									if (ate2.getOriginator() == null)
										continue;
									int row, column;
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
									D.set(row, column, D.get(row, column)
											+ Math.pow(beta, k - 2));
								}
						}
					} catch (IndexOutOfBoundsException ee) {
					} catch (IOException ee) {
					}
					;
				}
			}

		}

		return UtilOperation.normalize(D, normal);
	};
}
