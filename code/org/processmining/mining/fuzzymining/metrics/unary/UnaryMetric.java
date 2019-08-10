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
package org.processmining.mining.fuzzymining.metrics.unary;

import java.util.Arrays;

import org.processmining.mining.fuzzymining.metrics.Metric;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class UnaryMetric extends Metric {

	protected int size;
	protected double[] values;
	protected double[] normalized;

	/**
	 * @param aName
	 * @param aDescription
	 */
	public UnaryMetric(String aName, String aDescription, int aSize) {
		super(aName, aDescription);
		size = aSize;
		values = initializeVector(size, 0.0);
		normalized = null;
	}

	public UnaryMetric(String aName, String aDescription, UnaryMetric template) {
		this(aName, aDescription, template.size());
		this.setNormalizationMaximum(template.getNormalizationMaximum());
		for (int i = 0; i < template.size(); i++) {
			this.setMeasure(i, template.getMeasure(i));
		}
	}

	public int size() {
		return size;
	}

	public double getMeasure(int index) {
		if (normalized == null) {
			normalized = normalizeVector(values, normalizationMaximum);
		}
		double result = normalized[index];
		if (invert == true) {
			result = normalizationMaximum - result;
		}
		return result;
	}

	public void setMeasure(int index, double value) {
		values[index] = value;
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

	public boolean isValid() {
		for (int i = 0; i < size; i++) {
			if (getMeasure(i) > 0.0) {
				return true;
			}
		}
		return false;
	}

	protected double[] initializeVector(int size, double initialValue) {
		double[] result = new double[size];
		Arrays.fill(result, initialValue);
		return result;
	}

	protected double[] normalizeVector(double[] vector, double maxValue) {
		if (maxValue == 0.0) {
			return initializeVector(vector.length, 0.0); // disabled metric
		}
		double[] normalized = new double[vector.length];
		double foundMax = Double.MIN_VALUE;
		for (double current : vector) {
			foundMax = Math.max(current, foundMax);
		}
		double normFactor = maxValue / foundMax;
		for (int i = 0; i < vector.length; i++) {
			normalized[i] = normFactor * vector[i];
		}
		return normalized;
	}

	public double[] getNormalizedValues() {
		if (normalized == null) {
			normalized = normalizeVector(values, normalizationMaximum);
		}
		return normalized;
	}

	public Object clone() {
		super.clone();
		UnaryMetric clone = (UnaryMetric) super.clone();
		clone.values = (double[]) values.clone();
		clone.normalized = null;
		return clone;
	}
}
