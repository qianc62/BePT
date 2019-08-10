package org.processmining.mining.snamining.miningoperation.handover;

import java.util.ArrayList;
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

public class Handover_CCIDCM extends BasicOperation {

	// consider casuality, ignore direct succession, consider multiple
	// appearance

	private LogRelations relations = null;

	public Handover_CCIDCM(LogSummary summary, LogReader log) {
		super(summary, log);
	};

	public DoubleMatrix2D calculation(double beta, int depth) {

		DoubleMatrix2D D = DoubleFactory2D.sparse.make(users.length,
				users.length, 0);
		double normal = 0;

		LogAbstraction logAbstraction;
		logAbstraction = new LogAbstractionImpl(log);
		relations = (new MinValueLogRelationBuilder(logAbstraction, 0, log
				.getLogSummary().getLogEvents())).getLogRelations();
		DoubleMatrix2D parallelMatrix = relations.getParallelMatrix();

		Iterator it = log.instanceIterator();

		while (it.hasNext()) {

			ProcessInstance pi = (ProcessInstance) it.next();

			Iterator ates = pi.getAuditTrailEntryList().iterator();
			AuditTrailEntry ate, ate2;

			if (ates.hasNext()) {

				int flag = 0;
				ate = (AuditTrailEntry) ates.next();

				ArrayList users_list_by_entries = new ArrayList();
				ArrayList ates_list_by_entries = new ArrayList();

				while (ates.hasNext()) {
					users_list_by_entries.add(ate.getOriginator());
					ates_list_by_entries.add(ate);

					int row, column, row_from, column_to;

					ate2 = (AuditTrailEntry) ates.next();
					flag++;

					row_from = modelElements.findLogEventNumber(ate
							.getElement(), ate.getType());
					column_to = modelElements.findLogEventNumber(ate2
							.getElement(), ate2.getType());

					if (parallelMatrix.get(row_from, column_to) == 0) {
						normal++;
						if (ate.getOriginator() != null
								&& ate2.getOriginator() != null) {
							row = Arrays.binarySearch(users, ate
									.getOriginator());
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
					}

					if (ate2.getOriginator() != null) {
						column_to = modelElements.findLogEventNumber(ate2
								.getElement(), ate2.getType());
						column = Arrays.binarySearch(users, ate2
								.getOriginator());

						for (int i = 0; i < depth - 1 && i < flag - 1; i++) {
							AuditTrailEntry ateTemp;
							ateTemp = (AuditTrailEntry) ates_list_by_entries
									.get(ates_list_by_entries.size() - i - 2);
							row_from = modelElements.findLogEventNumber(ateTemp
									.getElement(), ateTemp.getType());

							if (parallelMatrix.get(row_from, column_to) == 0) {
								if (users_list_by_entries
										.get(users_list_by_entries.size() - i
												- 2) != null) {
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
					}

					ate = ate2;

				}
			}
		}

		return UtilOperation.normalize(D, normal);
	};
}
