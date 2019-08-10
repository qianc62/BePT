/*
 * Copyright (c) 2008 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.analysis.streamscope.cluster;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class Node {

	protected String name;
	protected double threshold;
	protected int index;

	public Node(int index, String name) {
		this.threshold = 1.0;
		this.name = name;
		this.index = index;
	}

	public int[] getIndices() {
		return new int[] { index };
	}

	public void resetIndex(int index) {
		this.index = index;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getLevel() {
		return 0;
	}

	public String toString() {
		return name;
	}

	public double distanceSingleLinkage(Node other, Distance distance) {
		if (other instanceof ClusterNode) {
			double minDistance = 0.0;
			for (Node element : ((ClusterNode) other).getElements()) {
				double dist = distanceSingleLinkage(element, distance);
				if (dist > minDistance) {
					minDistance = dist;
				}
			}
			return minDistance;
		} else {
			return distance.distance(index, other.getIndices()[0]);
			// return distance.distanceSingleLinkage(this, other);
		}
	}

	public double distanceAverageLinkage(Node other, Distance distance) {
		if (other instanceof ClusterNode) {
			double meanDistance = 0.0;
			for (Node element : ((ClusterNode) other).getElements()) {
				double dist = distanceSingleLinkage(element, distance);
				if (dist > meanDistance) {
					meanDistance = dist;
				}
			}
			return meanDistance;
		} else {
			return distance.distance(index, other.getIndices()[0]);
			// return distance.distanceSingleLinkage(this, other);
		}
	}

}
