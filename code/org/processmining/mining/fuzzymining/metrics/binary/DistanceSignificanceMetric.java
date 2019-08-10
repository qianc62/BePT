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

import org.processmining.mining.fuzzymining.metrics.MetricsRepository;
import org.processmining.mining.fuzzymining.metrics.unary.AggregateUnaryMetric;

/**
 * @author christian
 * 
 */
public class DistanceSignificanceMetric extends
		SignificanceBinaryDerivateMetric {

	/**
	 * @param aName
	 * @param aDescription
	 * @param aRepository
	 */
	public DistanceSignificanceMetric(MetricsRepository aRepository) {
		super(
				"Distance Significance",
				"Measures significance by the distance in significance of a link with its endpoints.",
				aRepository);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.BinaryDerivateMetric#measure
	 * ()
	 */
	public void measure() {
		AggregateUnaryMetric aggUnaryMetric = repository
				.getAggregateUnaryLogMetric();
		AggregateBinaryMetric aggBinaryMetric = repository
				.getAggregateSignificanceBinaryLogMetric();
		// calculate metrics from primary unary and secondary significance
		// metrics
		double sigSource, sigTarget, sigLink, distSig;
		for (int x = 0; x < size; x++) {
			sigSource = aggUnaryMetric.getMeasure(x);
			for (int y = 0; y < size; y++) {
				sigTarget = aggUnaryMetric.getMeasure(y);
				sigLink = aggBinaryMetric.getMeasure(x, y);
				// calculate and set distance significance
				distSig = 1.0 - (((sigSource - sigLink) + (sigTarget - sigLink)) / (sigSource + sigTarget));
				relations.set(x, y, distSig);
			}
		}
	}

}
