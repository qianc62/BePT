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
package org.processmining.mining.organizationmining.util;

import java.util.ArrayList;
import java.util.Collections;

import cern.colt.function.DoubleFunction;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author christian
 * 
 */
public class MatrixUtils {

	public static double getMaximum(DoubleMatrix2D original) {
		double maximum = 0.0;
		for (int x = original.rows() - 1; x >= 0; x--) {
			for (int y = original.columns() - 1; y >= 0; y--) {
				maximum = Math.max(maximum, original.get(x, y));
			}
		}
		return maximum;
	}

	public static double getMinimum(DoubleMatrix2D original) {
		double minimum = 0.0;
		for (int x = original.rows() - 1; x >= 0; x--) {
			for (int y = original.columns() - 1; y >= 0; y--) {
				minimum = Math.min(minimum, original.get(x, y));
			}
		}
		return minimum;
	}

	public static double getMean(DoubleMatrix2D original) {
		double sum = 0.0;
		for (int x = original.rows() - 1; x >= 0; x--) {
			for (int y = original.columns() - 1; y >= 0; y--) {
				sum += original.get(x, y);
			}
		}
		return sum / (original.rows() * original.columns());
	}

	public static double getMedian(DoubleMatrix2D original) {
		// collect all unique values in the matrix
		ArrayList<Double> valueSet = new ArrayList<Double>();
		double current;
		for (int x = original.rows() - 1; x >= 0; x--) {
			for (int y = original.columns() - 1; y >= 0; y--) {
				current = original.get(x, y);
				if (valueSet.contains(current) == false) {
					valueSet.add(current);
				}
			}
		}
		// sort list of unique values
		Collections.sort(valueSet);
		return valueSet.get(valueSet.size() / 2);
	}

	public static DoubleMatrix2D normalize(DoubleMatrix2D original,
			double normalizationMaximum) {
		// calculate normalization factor
		double originalMaximum = getMaximum(original);
		if (originalMaximum != 0.0) {
			double factor = normalizationMaximum / originalMaximum;
			// return normalized copy of original matrix
			if (factor != 1.0) {
				original.assign(cern.jet.math.Mult.mult(factor));
			}
		}
		return original;
	}

	public static DoubleMatrix2D invert(DoubleMatrix2D original) {
		// calculate normalization factor
		final double max = getMaximum(original);
		// return normalized copy of original matrix
		original.assign(new DoubleFunction() {
			public double apply(double value) {
				return Math.max(0.0, (max - value));
			}
		});
		return original;
	}

}
