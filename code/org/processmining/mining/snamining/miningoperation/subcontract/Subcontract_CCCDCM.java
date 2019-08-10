package org.processmining.mining.snamining.miningoperation.subcontract;

import java.util.Arrays;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.mining.logabstraction.LogAbstraction;
import org.processmining.mining.logabstraction.LogAbstractionImpl;
import org.processmining.mining.logabstraction.LogRelations;
import org.processmining.mining.logabstraction.MinValueLogRelationBuilder;
import org.processmining.mining.snamining.miningoperation.BasicOperation;
import org.processmining.mining.snamining.miningoperation.UtilOperation;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class Subcontract_CCCDCM extends BasicOperation {

	// ignore casuality, consider direct succession, consider multiple
	// appearance

	private LogRelations relations = null;

	public Subcontract_CCCDCM(LogSummary summary, LogReader log) {
		super(summary, log);
	};

	public DoubleMatrix2D calculation() {
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(users.length,
				users.length, 0);
		int count = 0;

		LogAbstraction logAbstraction;
		logAbstraction = new LogAbstractionImpl(log);
		relations = (new MinValueLogRelationBuilder(logAbstraction, 0, log
				.getLogSummary().getLogEvents())).getLogRelations();

		DoubleMatrix2D casualFollowerMatrix = relations
				.getCausalFollowerMatrix();

		Iterator it = log.instanceIterator();

		while (it.hasNext()) {
			ProcessInstance pi = (ProcessInstance) it.next();

			Iterator ates = pi.getAuditTrailEntryList().iterator();
			AuditTrailEntry ate, ate2, ate3;

			if (ates.hasNext()) {

				ate = (AuditTrailEntry) ates.next();

				if (ates.hasNext()) {
					ate2 = (AuditTrailEntry) ates.next();
					while (ates.hasNext()) {
						ate3 = (AuditTrailEntry) ates.next();

						int row_from, to_from, column_to;
						row_from = modelElements.findLogEventNumber(ate
								.getElement(), ate.getType());
						to_from = modelElements.findLogEventNumber(ate2
								.getElement(), ate2.getType());
						column_to = modelElements.findLogEventNumber(ate3
								.getElement(), ate3.getType());

						if ((casualFollowerMatrix.get(row_from, to_from) == 1)
								&& (casualFollowerMatrix
										.get(to_from, column_to) == 1)) {

							if (ate.getOriginator() != null
									&& ate2.getOriginator() != null
									&& ate3.getOriginator() != null) {
								if (ate.getOriginator().equals(
										ate3.getOriginator())) {

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

									D
											.set(row, column, D
													.get(row, column) + 1.0);
								}
							}
							count++;
							ate = ate2;
							ate2 = ate3;
						}
					}
				}
			}
		}

		return UtilOperation.normalize(D, count);
	};
}
