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

import java.util.List;

import org.processmining.mining.organizationmining.model.InstancePoint;
import org.processmining.mining.organizationmining.profile.Profile;

/**
 * @author christian
 * 
 */
public class CorrelationCoefficientDistance extends DistanceMetric {

	public CorrelationCoefficientDistance() {
		super("Correlation Coefficient", "Correlation Coefficient distance");
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
				/*
				 * The following algorithm is an adaption from the efficient
				 * algorithm for calculating correlation in a single pass, as
				 * found on Wikipedia.org (05/2007):
				 * http://en.wikipedia.org/wiki/Correlation_coefficient#
				 * Computing_correlation_accurately_in_a_single_pass --
				 * cwguenther
				 */
				double sumSquareX = 0.0;
				double sumSquareY = 0.0;
				double sumCoproduct = 0.0;
				double meanX = profile.getValue(x, 0);
				double meanY = profile.getValue(y, 0);
				double sweep, deltaX, deltaY;
				for (int i = 1; i < profile.numberOfItems(); i++) {
					sweep = (double) i / (double) (i + 1);
					deltaX = profile.getValue(x, i) - meanX;
					deltaY = profile.getValue(y, i) - meanY;
					sumSquareX += deltaX * deltaX * sweep;
					sumSquareY += deltaY * deltaY * sweep;
					sumCoproduct += deltaX * deltaY * sweep;
					meanX += (deltaX / (i + 1));
					meanY += (deltaY / (i + 1));
				}
				double distance;
				if (profile.numberOfItems() == 0) {
					distance = 1.0;
				} else {
					double popSdX = Math.sqrt(sumSquareX
							/ profile.numberOfItems());
					double popSdY = Math.sqrt(sumSquareY
							/ profile.numberOfItems());
					double divisor = popSdX * popSdY;
					if (divisor != 0.0) {
						double covXY = sumCoproduct / profile.numberOfItems();
						double correlation = covXY / (popSdX * popSdY);
						distance = 1.0 - correlation;
					} else {
						distance = 1.0;
					}
				}
				distanceMatrix.set(x, y, distance);
				// superfluous due to symmetric distance matrices:
				// distanceMatrix.set(y, x, distance);
			}
		}
		return distanceMatrix;
	}

	@Override
	public double getDistance(InstancePoint pointA, InstancePoint pointB) {
		List<String> itemKeys = pointA.getItemKeys();
		double sumSquareX = 0.0;
		double sumSquareY = 0.0;
		double sumCoproduct = 0.0;
		double meanX = pointA.get(itemKeys.get(0));
		double meanY = pointB.get(itemKeys.get(0));
		double sweep, deltaX, deltaY;
		for (int i = 1; i < itemKeys.size(); i++) {
			sweep = (double) i / (double) (i + 1);
			deltaX = pointA.get(itemKeys.get(i)) - meanX;
			deltaY = pointB.get(itemKeys.get(i)) - meanY;
			sumSquareX += deltaX * deltaX * sweep;
			sumSquareY += deltaY * deltaY * sweep;
			sumCoproduct += deltaX * deltaY * sweep;
			meanX += (deltaX / (i + 1));
			meanY += (deltaY / (i + 1));
		}
		double distance;
		if (itemKeys.size() == 0) {
			distance = 1.0;
		} else {
			double popSdX = Math.sqrt(sumSquareX / itemKeys.size());
			double popSdY = Math.sqrt(sumSquareY / itemKeys.size());
			double divisor = popSdX * popSdY;
			if (divisor != 0.0) {
				double covXY = sumCoproduct / itemKeys.size();
				double correlation = covXY / (popSdX * popSdY);
				distance = 1.0 - correlation;
			} else {
				distance = 1.0;
			}
		}
		return distance;
	}

}
