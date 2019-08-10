package org.processmining.mining.snamining.miningoperation.similartask;

import java.util.Arrays;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.mining.snamining.miningoperation.BasicOperation;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class SimilartaskBase extends BasicOperation {

	protected String[] elements = null;

	public SimilartaskBase(LogSummary summary, LogReader log) {
		super(summary, log);
		elements = summary.getModelElements();
	};

	public DoubleMatrix2D makeOTMatrix() {

		DoubleMatrix2D OTMatrix = DoubleFactory2D.sparse.make(users.length,
				elements.length, 0);

		while (log.hasNext()) {
			ProcessInstance pi = log.next();
			AuditTrailEntries ates = pi.getAuditTrailEntries();
			AuditTrailEntry ate;

			while (ates.hasNext()) {
				int row, column;

				ate = ates.next();
				if (ate.getOriginator() == null)
					continue;
				row = Arrays.binarySearch(users, ate.getOriginator());
				column = Arrays.binarySearch(elements, ate.getElement());

				OTMatrix.set(row, column, OTMatrix.get(row, column) + 1.0);
			}
		}
		;
		return OTMatrix;
	};
}
