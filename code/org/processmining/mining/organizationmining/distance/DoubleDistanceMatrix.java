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
package org.processmining.mining.organizationmining.distance;

import java.util.Arrays;

/**
 * An efficient implementation of a symmetric distance matrix for clustering.
 * Note that symmetry is implicitly assumed, i.e. distance(x, y) must yield the
 * same value as distance(y, x).
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class DoubleDistanceMatrix extends DistanceMatrix {

	protected int itemCount;
	protected int size;
	protected double[] values;
	protected double min;
	protected double max;

	public DoubleDistanceMatrix(int numberOfItems) {
		itemCount = numberOfItems;
		size = sumSmallerEqual(itemCount);
		values = new double[size];
		Arrays.fill(values, 0.0);
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
	}

	public int itemSize() {
		return itemCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.traceclustering.distance.DistanceMatrix#get
	 * (int, int)
	 */
	public double get(int x, int y) {
		return values[translateAddress(x, y)];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.traceclustering.distance.DistanceMatrix#set
	 * (int, int, double)
	 */
	public void set(int x, int y, double value) {
		if (value > max) {
			max = value;
		}
		if (value < min) {
			min = value;
		}
		values[translateAddress(x, y)] = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.traceclustering.distance.DistanceMatrix#
	 * getMinValue()
	 */
	public double getMinValue() {
		return min;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.traceclustering.distance.DistanceMatrix#
	 * getMaxValue()
	 */
	public double getMaxValue() {
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.traceclustering.distance.DistanceMatrix#
	 * multiplyAllFieldsWith(double)
	 */
	public void multiplyAllFieldsWith(double factor) {
		for (int i = 0; i < size; i++) {
			values[i] *= factor;
		}
		min *= factor;
		max *= factor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.traceclustering.distance.DistanceMatrix#
	 * normalizeToMaximum(double)
	 */
	public void normalizeToMaximum(double normalizationMaximum) {
		if (max > 0) {
			double normalizationFactor = normalizationMaximum / max;
			multiplyAllFieldsWith(normalizationFactor);
		}
	}

	public void invert() {
		for (int i = 0; i < size; i++) {
			double inverted = values[i] - max;
			if (inverted < 0) {
				inverted = 0;
			}
			values[i] = inverted;
		}
	}

	public void add(DistanceMatrix other) {
		if (itemCount != other.itemSize()) {
			throw new AssertionError(
					"Cannot add two distance matrices of different sizes!");
		} else {
			for (int x = 0; x < itemCount; x++) {
				for (int y = 0; y <= x; y++) {
					double value = get(x, y) + other.get(x, y);
					set(x, y, value);
					if (value > max) {
						max = value;
					}
					if (value < min) {
						min = value;
					}
				}
			}
		}
	}

}
