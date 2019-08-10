package org.processmining.mining.snamining.miningoperation;

import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class UtilOperation {

	public static DoubleMatrix2D normalize(DoubleMatrix2D m, int n) {
		for (int r = 0; r < m.rows(); r++) {
			for (int c = 0; c < m.columns(); c++) {
				m.set(r, c, m.get(r, c) / n);
			}
		}
		return m;
	}

	public static DoubleMatrix2D normalize(DoubleMatrix2D m, double n) {
		for (int r = 0; r < m.rows(); r++) {
			for (int c = 0; c < m.columns(); c++) {
				m.set(r, c, m.get(r, c) / n);
			}
		}
		return m;
	}

	public static boolean isInCase(AuditTrailEntryList atelists, String user1,
			String user2) {
		boolean bResult1 = false;
		boolean bResult2 = false;

		Iterator ates = atelists.iterator();

		while (ates.hasNext()) {
			AuditTrailEntry ate;
			ate = (AuditTrailEntry) ates.next();

			if (ate.getOriginator() != null
					&& ate.getOriginator().equals(user1))
				bResult1 = true;
			if (ate.getOriginator() != null
					&& ate.getOriginator().equals(user2))
				bResult2 = true;
			if (bResult1 & bResult2)
				break;
		}
		return (bResult1 & bResult2);
	}

	public static boolean isInCase(AuditTrailEntryList atelists, String user1) {
		boolean bResult1 = false;

		Iterator ates = atelists.iterator();

		while (ates.hasNext()) {
			AuditTrailEntry ate;
			ate = (AuditTrailEntry) ates.next();

			if (ate.getOriginator() != null
					&& ate.getOriginator().equals(user1)) {
				bResult1 = true;
				break;
			}
		}

		return (bResult1);
	}

	public static DoubleMatrix2D euclidiandistance(DoubleMatrix2D m) {

		int row = m.rows();
		int column = m.columns();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(row, row, 0);

		for (int i = 0; i < row - 1; i++) {
			for (int j = i + 1; j < row; j++) {

				double temp = 0;

				for (int k = 0; k < column; k++) {
					temp += (double) ((m.get(i, k) - m.get(j, k)) * (m
							.get(i, k) - m.get(j, k)));
				}

				temp = Math.sqrt(temp);

				D.set(i, j, temp);
				D.set(j, i, temp);
			}
		}

		return D;
	}

	public static DoubleMatrix2D hammingdistance(DoubleMatrix2D m) {

		int row = m.rows();
		int column = m.columns();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(row, row, 0);

		m.forEachNonZero(new cern.colt.function.IntIntDoubleFunction() {
			public double apply(int row, int column, double value) {
				value = 1;
				return value;
			}
		});

		for (int i = 0; i < row - 1; i++) {
			for (int j = i + 1; j < row; j++) {

				double temp = 0;

				for (int k = 0; k < column; k++) {
					if (m.get(i, k) != m.get(j, k))
						temp++;
				}

				D.set(i, j, (column - temp) / column);
				D.set(j, i, (column - temp) / column);
			}
		}
		return D;
	}

	public static DoubleMatrix2D similaritycoefficient(DoubleMatrix2D m) {

		int row = m.rows();
		int column = m.columns();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(row, row, 0);

		m.forEachNonZero(new cern.colt.function.IntIntDoubleFunction() {
			public double apply(int row, int column, double value) {
				value = 1;
				return value;
			}
		});

		for (int i = 0; i < row - 1; i++) {
			for (int j = i + 1; j < row; j++) {

				double temp1 = 0;
				double temp2 = 0;

				for (int k = 0; k < column; k++) {
					if (m.get(i, k) == 1 && m.get(j, k) == 1)
						temp1++;
					else
						temp2++;
				}

				if (temp2 != 0) {
					D.set(i, j, temp1 / temp2);
					D.set(j, i, temp1 / temp2);
				} else {
					D.set(i, j, Double.MAX_VALUE);
					D.set(j, i, Double.MAX_VALUE);
				}
			}
		}
		return D;
	}

	public static DoubleMatrix2D correlationcoefficient(DoubleMatrix2D m) {

		int row = m.rows();
		int column = m.columns();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(row, row, 0);

		for (int i = 0; i < row - 1; i++) {
			for (int j = i + 1; j < row; j++) {

				double temp1 = 0.0, temp2 = 0.0;
				double upper = 0.0, below1 = 0.0, below2 = 0.0;

				for (int k = 0; k < column; k++) {
					temp1 += m.get(i, k);
					temp2 += m.get(j, k);
				}

				temp1 = temp1 / column;
				temp2 = temp2 / column;

				for (int k = 0; k < column; k++) {
					upper += (m.get(i, k) - temp1) * (m.get(j, k) - temp2);
					below1 += (m.get(i, k) - temp1) * (m.get(i, k) - temp1);
					below2 += (m.get(j, k) - temp2) * (m.get(j, k) - temp2);
				}

				D.set(i, j, upper / Math.sqrt(below1 * below2));
				D.set(j, i, upper / Math.sqrt(below1 * below2));
			}
		}
		return D;
	}
}
