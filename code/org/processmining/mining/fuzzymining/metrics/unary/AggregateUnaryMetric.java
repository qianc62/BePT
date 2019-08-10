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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class AggregateUnaryMetric extends UnaryMetric {

	protected List<UnaryMetric> metrics;

	/**
	 * @param aName
	 * @param aDescription
	 */
	public AggregateUnaryMetric(String name, String description,
			List<UnaryMetric> componentMetrics) {
		super(name, description, componentMetrics.get(0).size);
		metrics = componentMetrics;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.UnaryMetric#getMeasure(int)
	 */
	public double getMeasure(int index) {
		if (normalized == null) {
			// lazy aggregation and normalization
			ArrayList<UnaryMetric> validMetrics = new ArrayList<UnaryMetric>(
					metrics.size());
			for (UnaryMetric metric : metrics) {
				if (metric.isValid()) {
					validMetrics.add(metric);
				}
			}
			normalized = super.initializeVector(size, 0.0);
			double aggregated;
			double aggregateMax = 0.0;
			for (int i = 0; i < size; i++) {
				aggregated = 0.0;
				for (UnaryMetric metric : validMetrics) {
					aggregated += metric.getMeasure(i);
				}
				normalized[i] = aggregated;
				if (aggregated > aggregateMax) {
					aggregateMax = aggregated;
				}
			}
			double normalizationFactor = normalizationMaximum / aggregateMax;
			for (int i = 0; i < size; i++) {
				normalized[i] *= normalizationFactor;
			}
		}
		return normalized[index];
	}

	protected double calculateNormalizationFactor() {
		double aggregated = 0.0;
		for (UnaryMetric metric : metrics) {
			aggregated += metric.getNormalizationMaximum();
		}
		return (1.0 / aggregated);
	}

}
