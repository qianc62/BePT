package org.processmining.analysis.logclustering.distancemeasure;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class CorrelationCoefficientDistanceMeasure extends DistanceMeasure {
	public CorrelationCoefficientDistanceMeasure() {
		super("Correlation Coefficient");
	}

	public DoubleMatrix2D calculateDistance(DoubleMatrix2D m) {
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

				D.set(i, j, 1 - upper / Math.sqrt(below1 * below2));
				D.set(j, i, 1 - upper / Math.sqrt(below1 * below2));
			}
		}
		return D;
	}

	public DoubleMatrix2D calculateDistance(DoubleMatrix2D profile,
			DoubleMatrix2D center) {
		int profile_row = profile.rows();
		int center_row = center.rows();
		int column = profile.columns();

		DoubleMatrix2D D = DoubleFactory2D.sparse.make(profile_row, center_row,
				0);

		for (int i = 0; i < profile_row; i++) {
			for (int j = 0; j < center_row; j++) {

				double temp1 = 0.0, temp2 = 0.0;
				double upper = 0.0, below1 = 0.0, below2 = 0.0;

				for (int k = 0; k < column; k++) {
					temp1 += profile.get(i, k);
					temp2 += center.get(j, k);
				}

				temp1 = temp1 / column;
				temp2 = temp2 / column;

				for (int k = 0; k < column; k++) {
					upper += (profile.get(i, k) - temp1)
							* (center.get(j, k) - temp2);
					below1 += (profile.get(i, k) - temp1)
							* (profile.get(i, k) - temp1);
					below2 += (center.get(j, k) - temp2)
							* (center.get(j, k) - temp2);
				}

				D.set(i, j, 1 - upper / Math.sqrt(below1 * below2));
			}
		}
		return D;
	}

}
