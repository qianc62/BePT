/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.mining.fuzzymining.metrics;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author christian
 * 
 */
public class MatrixUtils {

	public static double getMaxValue(DoubleMatrix2D matrix) {
		double maxFound = 0.0;
		double current;
		for (int x = matrix.columns() - 1; x >= 0; x--) {
			for (int y = matrix.rows() - 1; y >= 0; y--) {
				current = matrix.get(x, y);
				if (current > maxFound) {
					maxFound = current;
				}
			}
		}
		return maxFound;
	}

	public static DoubleMatrix2D multiplyAllFields(DoubleMatrix2D matrix,
			double factor) {
		for (int x = matrix.columns() - 1; x >= 0; x--) {
			for (int y = matrix.rows() - 1; y >= 0; y--) {
				matrix.set(x, y, (matrix.get(x, y) * factor));
			}
		}
		return matrix;
	}

	public static DoubleMatrix2D setAllFields(DoubleMatrix2D matrix,
			double value) {
		for (int x = matrix.columns() - 1; x >= 0; x--) {
			for (int y = matrix.rows() - 1; y >= 0; y--) {
				matrix.set(x, y, value);
			}
		}
		return matrix;
	}

	public static DoubleMatrix2D normalize(DoubleMatrix2D matrix, double maximum) {
		if (maximum == 0.0) {
			return setAllFields(matrix, 0.0); // disabled matrix / metric
		}
		double maxValue = getMaxValue(matrix);
		// do not normalize all-zero matrices
		if (maxValue > 0.0) {
			double factor = maximum / maxValue;
			return multiplyAllFields(matrix, factor);
		} else {
			return matrix;
		}
	}

	public static DoubleMatrix2D normalize(DoubleMatrix2D matrix) {
		return normalize(matrix, 1.0);
	}

	public static boolean verifyMatrix(DoubleMatrix2D matrix) {
		return verifyMatrix(matrix, 0.0, 1.0);
	}

	public static boolean verifyMatrix(DoubleMatrix2D matrix, double min,
			double max) {
		int width = matrix.columns();
		int height = matrix.rows();
		if (width != height) {
			System.err.println("Matrix test failed (unbalanced)!");
			return false;
		}
		double probe;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				probe = matrix.get(x, y);
				if (probe == Double.NaN) {
					System.err.println("Matrix test failed (NaN at " + x + ","
							+ y + ")!");
					return false;
				} else if (probe < min) {
					System.err.println("Matrix test failed (< MIN at " + x
							+ "," + y + ")!");
					return false;
				} else if (probe > max) {
					System.err.println("Matrix test failed (> MAX at " + x
							+ "," + y + ")!");
					return false;
				}
			}
		}
		return true;
	}

}
