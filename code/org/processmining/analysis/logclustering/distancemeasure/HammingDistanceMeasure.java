package org.processmining.analysis.logclustering.distancemeasure;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Minseok Song
 * @version 1.0
 */
public class HammingDistanceMeasure extends DistanceMeasure {
	public HammingDistanceMeasure() {
		super("Hamming Distance");
	}

	public DoubleMatrix2D calculateDistance(DoubleMatrix2D m) {
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

	public DoubleMatrix2D calculateDistance(DoubleMatrix2D profile,
			DoubleMatrix2D center) {
		/*
		 * int profile_row = profile.rows(); int center_row = center.rows(); int
		 * column = profile.columns();
		 * 
		 * // int row = m.rows(); // int column = m.columns(); DoubleMatrix2D D
		 * = DoubleFactory2D.sparse.make(profile_row, center_row, 0);
		 * 
		 * profile.forEachNonZero(new cern.colt.function.IntIntDoubleFunction()
		 * { public double apply(int row, int column, double value) { value = 1;
		 * return value; } });
		 * 
		 * for (int i = 0; i < row - 1; i++) { for (int j = i + 1; j < row; j++)
		 * {
		 * 
		 * double temp = 0;
		 * 
		 * for (int k = 0; k < column; k++) { if (m.get(i, k) != m.get(j, k))
		 * temp++; }
		 * 
		 * D.set(i, j, (column - temp) / column); D.set(j, i, (column - temp) /
		 * column); } } return D;
		 */
		return null;
	}

}
