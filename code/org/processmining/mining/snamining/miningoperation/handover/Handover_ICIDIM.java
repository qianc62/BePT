package org.processmining.mining.snamining.miningoperation.handover;

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

public class Handover_ICIDIM extends BasicOperation {

	// ignore casuality, ignore direct succession, ignore multiple appearance
	public Handover_ICIDIM(LogSummary summary, LogReader log) {
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

			int minK = 0;
			if (ates.size() < depth)
				minK = ates.size();
			else
				minK = depth + 1;
			if (minK < 2)
				minK = 2;

			for (int k = 1; k < minK; k++) {
				normal += Math.pow(beta, k - 1);
				DoubleMatrix2D m = DoubleFactory2D.sparse.make(users.length,
						users.length, 0);

				for (int i = 0; i < ates.size() - k; i++) {
					AuditTrailEntry ate = null, ate2 = null;
					try {
						ate = ates.get(i);
						ate2 = ates.get(i + k);
					} catch (IndexOutOfBoundsException ee) {
					} catch (IOException ee) {
					}
					;

					if (ate.getOriginator() == null
							|| ate2.getOriginator() == null)
						continue;

					int row, column;
					row = Arrays.binarySearch(users, ate.getOriginator());
					column = Arrays.binarySearch(users, ate2.getOriginator());

					if (row < 0 || column < 0) {
						throw new Error(
								"Implementation error: couldn't find user in the user list: "
										+ ate.getOriginator() + " or "
										+ ate2.getOriginator());
					}

					m.set(row, column, 1.0);

				}

				for (int i = 0; i < users.length; i++)
					for (int j = 0; j < users.length; j++)
						D.set(i, j, D.get(i, j) + m.get(i, j)
								* Math.pow(beta, k - 1));
			}

		}
		return UtilOperation.normalize(D, normal);
	};
}
