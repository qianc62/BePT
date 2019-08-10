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
package org.processmining.mining.fuzzymining.metrics.binary;

import org.processmining.mining.fuzzymining.metrics.MatrixUtils;
import org.processmining.mining.fuzzymining.metrics.Metric;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class BinaryMetric extends Metric {

	protected int size;
	protected DoubleMatrix2D relations; // relations matrix (sums of
	// measurements)
	protected DoubleMatrix2D normalized; // 1.0-normalized shadow matrix

	/**
	 * @param aName
	 * @param aDescription
	 */
	public BinaryMetric(String aName, String aDescription, int aSize) {
		super(aName, aDescription);
		size = aSize;
		// use sparse matrices on high event count (exponential matrix size,
		// #cells * 8 bytes)
		if (size < 512) {
			relations = DoubleFactory2D.dense.make(size, size, 0.0);
		} else {
			relations = DoubleFactory2D.sparse.make(size, size, 0.0);
		}
		// initialize normalized shadow matrix as invalid
		normalized = null;
	}

	public BinaryMetric(String aName, String aDescription, BinaryMetric template) {
		this(aName, aDescription, template.size());
		this.setNormalizationMaximum(template.getNormalizationMaximum());
		for (int x = 0; x < template.size(); x++) {
			for (int y = 0; y < template.size(); y++) {
				this.setMeasure(x, y, template.getMeasure(x, y));
			}
		}
	}

	public double getMeasure(int fromIndex, int toIndex) {
		// lazy normalization on the fly
		if (normalized == null) {
			normalized = MatrixUtils.normalize(relations.copy(),
					normalizationMaximum);
		}
		// retrieve result
		double result = normalized.get(fromIndex, toIndex);
		if (invert == true) {
			result = normalizationMaximum - result;
		}
		return result;
	}

	public void setMeasure(int fromIndex, int toIndex, double value) {
		relations.set(fromIndex, toIndex, value);
		normalized = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.Metric#setNormalizationMaximum
	 * (double)
	 */
	public void setNormalizationMaximum(double aNormalizationMaximum) {
		normalizationMaximum = aNormalizationMaximum;
		normalized = null; // reset normalization matrix
	}

	public int size() {
		return size;
	}

	public boolean isValid() {
		return (normalizationMaximum > 0.0 && MatrixUtils
				.getMaxValue(relations) > 0.0);
	}

	public DoubleMatrix2D getNormalizedMatrix() {
		if (normalized == null) {
			normalized = MatrixUtils.normalize(relations.copy(),
					normalizationMaximum);
		}
		return normalized;
	}

	public Object clone() {
		super.clone();
		BinaryMetric clone = (BinaryMetric) super.clone();
		clone.relations = relations.copy();
		clone.normalized = null;
		return clone;
	}

}
