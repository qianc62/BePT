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
public class EuclidianDistanceMeasure extends DistanceMeasure {
	public EuclidianDistanceMeasure() {
		super("Euclidian Distance");
	}

	public DoubleMatrix2D calculateDistance(DoubleMatrix2D m) {
		int row = m.rows();
		int column = m.columns();
		DoubleMatrix2D D = DoubleFactory2D.dense.make(row, row, 0);

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

		m.columns();
		return D;

	}

	public DoubleMatrix2D calculateDistance(DoubleMatrix2D profile,
			DoubleMatrix2D center) {
		int profile_row = profile.rows();
		int center_row = center.rows();
		int column = profile.columns();

		DoubleMatrix2D D = DoubleFactory2D.dense.make(profile_row, center_row,
				0);

		for (int i = 0; i < profile_row; i++) {
			for (int j = 0; j < center_row; j++) {

				double temp = 0;

				for (int k = 0; k < column; k++) {
					temp += (double) ((profile.get(i, k) - center.get(j, k)) * (profile
							.get(i, k) - center.get(j, k)));
				}

				temp = Math.sqrt(temp);

				D.set(i, j, temp);
			}
		}

		return D;

	}
}
