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

import org.processmining.mining.organizationmining.model.InstancePoint;
import org.processmining.mining.organizationmining.profile.Profile;

/**
 * @author christian
 * 
 */
public class JaccardIndexDistance extends DistanceMetric {

	public JaccardIndexDistance() {
		super("Jaccard Index", "Jaccard index distance");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.traceclustering.distance.DistanceMetric#
	 * getDistanceMatrix
	 * (org.processmining.analysis.traceclustering.profile.Profile)
	 */
	@Override
	public DistanceMatrix getDistanceMatrix(Profile profile) {
		int numberOfInstances = profile.getNumberOfOriginators();
		DistanceMatrix distanceMatrix;
		if (numberOfInstances < 2000) {
			distanceMatrix = new DoubleDistanceMatrix(numberOfInstances);
		} else {
			distanceMatrix = new FloatDistanceMatrix(numberOfInstances);
		}
		for (int x = 0; x < numberOfInstances; x++) {
			for (int y = x + 1; y < numberOfInstances; y++) {
				double intersection = 0.0;
				double union = 0.0;
				boolean xGreaterMin, yGreaterMin;
				for (int i = 0; i < profile.numberOfItems(); i++) {
					xGreaterMin = (profile.getValue(x, i) > 0.0);
					yGreaterMin = (profile.getValue(y, i) > 0.0);
					if (xGreaterMin || yGreaterMin) {
						union++;
						if (xGreaterMin && yGreaterMin) {
							intersection++;
						}
					}
				}
				double distance;
				if (union > 0.0) {
					distance = intersection / union;
				} else {
					distance = Double.MAX_VALUE;
				}
				distanceMatrix.set(x, y, distance);
				distanceMatrix.set(y, x, distance);
			}
		}
		return distanceMatrix;
	}

	@Override
	public double getDistance(InstancePoint pointA, InstancePoint pointB) {
		double intersection = 0.0;
		double union = 0.0;
		boolean xGreaterMin, yGreaterMin;
		for (String key : pointA.getItemKeys()) {
			xGreaterMin = (pointA.get(key) > 0.0);
			yGreaterMin = (pointB.get(key) > 0.0);
			if (xGreaterMin || yGreaterMin) {
				union++;
				if (xGreaterMin && yGreaterMin) {
					intersection++;
				}
			}
		}
		double distance;
		if (union > 0.0) {
			distance = intersection / union;
		} else {
			distance = Double.MAX_VALUE;
		}
		return distance;
	}

}
