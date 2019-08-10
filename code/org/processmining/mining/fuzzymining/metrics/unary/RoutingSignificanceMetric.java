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

import org.processmining.mining.fuzzymining.metrics.MetricsRepository;
import org.processmining.mining.fuzzymining.metrics.binary.AggregateBinaryMetric;

/**
 * @author cgunther
 * 
 */
public class RoutingSignificanceMetric extends UnaryDerivateMetric {

	/**
	 * @param aRepository
	 */
	public RoutingSignificanceMetric(MetricsRepository aRepository) {
		super(
				"Routing significance",
				"Measures the significance of a node by weighing incoming against outgoing relations",
				aRepository);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.UnaryDerivateMetric#measure
	 * ()
	 */
	public void measure() {
		int size = repository.getLogReader().getLogSummary().getLogEvents()
				.size();
		double inValue, outValue, quotient, tmpSig, tmpCor;
		AggregateBinaryMetric binMetricCorrelation = repository
				.getAggregateCorrelationBinaryLogMetric();
		AggregateBinaryMetric binMetricSignificance = repository
				.getAggregateSignificanceBinaryLogMetric();
		for (int i = 0; i < size; i++) {
			inValue = 0.0;
			outValue = 0.0;
			// compose incoming and outgoing forces, combined
			for (int x = 0; x < size; x++) {
				if (x == i) {
					continue;
				} // skip self-references
				// compose incoming force
				tmpSig = binMetricSignificance.getMeasure(x, i);
				tmpCor = binMetricCorrelation.getMeasure(x, i);
				inValue += (tmpSig * tmpCor);
				// compose outgoing force
				tmpSig = binMetricSignificance.getMeasure(i, x);
				tmpCor = binMetricCorrelation.getMeasure(i, x);
				outValue += (tmpSig * tmpCor);
			}
			// calculate quotient
			if (inValue == 0.0 && outValue == 0.0) {
				quotient = 0.0;
			} else {
				quotient = ((inValue - outValue) / (inValue + outValue));
				if (quotient < 0.0) {
					quotient = -quotient;
				}
			}
			values[i] = quotient;
		}
		// reset normalization matrix
		normalized = null;
	}

}
