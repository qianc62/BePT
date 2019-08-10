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

import java.util.ArrayList;
import java.util.List;

import org.processmining.mining.fuzzymining.metrics.MatrixUtils;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class AggregateBinaryMetric extends BinaryMetric {

	protected List<BinaryMetric> metrics;

	/**
	 * @param aName
	 * @param aDescription
	 */
	public AggregateBinaryMetric(String name, String description,
			List<BinaryMetric> componentMetrics) {
		super(name, description, componentMetrics.get(0).size());
		metrics = new ArrayList<BinaryMetric>(componentMetrics.size());
		metrics.addAll(componentMetrics);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.BinaryMetric#getMeasure(int,
	 * int)
	 */
	public double getMeasure(int fromIndex, int toIndex) {
		if (normalized == null) {
			// on-the-fly aggregation and normalization
			aggregateMetrics();
		}
		return normalized.get(fromIndex, toIndex);
	}

	protected void aggregateMetrics() {
		ArrayList<BinaryMetric> validMetrics = new ArrayList<BinaryMetric>(
				metrics.size());
		for (BinaryMetric metric : metrics) {
			if (metric.isValid()) {
				validMetrics.add(metric);
			}
		}
		double aggregateMax = 0.0;
		double aggregate;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				aggregate = 0.0;
				for (BinaryMetric metric : validMetrics) {
					aggregate += metric.getMeasure(x, y);
				}
				relations.set(x, y, aggregate);
				if (aggregate > aggregateMax) {
					aggregateMax = aggregate;
				}
			}
		}
		normalized = MatrixUtils.multiplyAllFields(relations.copy(),
				(normalizationMaximum / aggregateMax));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.binary.BinaryMetric#isValid
	 * ()
	 */
	public boolean isValid() {
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				if (getMeasure(x, y) > 0.0) {
					return true;
				}
			}
		}
		return false;
	}

}
