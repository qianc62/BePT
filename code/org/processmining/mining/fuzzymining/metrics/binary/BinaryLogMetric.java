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

import org.processmining.framework.log.AuditTrailEntry;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author christian
 * 
 */
public abstract class BinaryLogMetric extends BinaryMetric {

	protected DoubleMatrix2D divisors;
	protected boolean compensateFrequency;

	public BinaryLogMetric(String name, String description, int eventCount) {
		super(name, description, eventCount);
		divisors = relations.copy();
		compensateFrequency = true;
	}

	public double getMeasure(int fromIndex, int toIndex) {
		if (compensateFrequency == true && normalized == null) {
			rectifyFrequency();
		}
		return super.getMeasure(fromIndex, toIndex);
	}

	public void measure(AuditTrailEntry reference, AuditTrailEntry follower,
			int referenceIndex, int followerIndex, double attenuationFactor) {
		double attenuated = measure(reference, follower) * attenuationFactor;
		attenuated += relations.get(referenceIndex, followerIndex);
		relations.set(referenceIndex, followerIndex, attenuated);
		double divisor = divisors.get(referenceIndex, followerIndex)
				+ attenuationFactor;
		divisors.set(referenceIndex, followerIndex, divisor);
		normalized = null;
	}

	protected abstract double measure(AuditTrailEntry reference,
			AuditTrailEntry follower);

	protected void rectifyFrequency() {
		double value, divisor;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				value = relations.get(x, y);
				divisor = divisors.get(x, y);
				if (divisor > 0.0) {
					value /= divisor;
					relations.set(x, y, value);
				}
			}
		}
	}

}
