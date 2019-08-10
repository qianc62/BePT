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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ClusterNode extends Node {

	protected static long ID_COUNTER = 0;

	protected static synchronized String createId() {
		String id = "cluster_" + ID_COUNTER;
		ID_COUNTER++;
		return id;
	}

	protected Set<Node> nodes;
	protected int[] indices = null;
	protected int minIndex = Integer.MAX_VALUE;
	protected int maxIndex = Integer.MIN_VALUE;
	protected int level;

	public ClusterNode(int level) {
		super(0, createId());
		this.nodes = new HashSet<Node>();
		this.level = level;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.streamscope.cluster.Node#getIndices()
	 */
	@Override
	public int[] getIndices() {
		if (indices == null) {
			maxIndex = Integer.MIN_VALUE;
			minIndex = Integer.MAX_VALUE;
			ArrayList<Integer> indicesList = new ArrayList<Integer>();
			for (Node element : nodes) {
				for (int index : element.getIndices()) {
					indicesList.add(index);
				}
			}
			indices = new int[indicesList.size()];
			for (int i = 0; i < indices.length; i++) {
				indices[i] = indicesList.get(i);
				if (indices[i] > maxIndex) {
					maxIndex = indices[i];
				}
				if (indices[i] < minIndex) {
					minIndex = indices[i];
				}
			}
			Arrays.sort(indices);
			// System.out.println("updated indices for " + name + ": " +
			// minIndex + " > " + maxIndex);
			// System.out.print("[");
			// for(int index : indices) {
			// System.out.print(index + " ");
			// }
			// System.out.println("]");
		}
		return indices;
	}

	public void resetIndex(int index) {
		this.indices = null;
	}

	public int getMinIndex() {
		// trigger derivation
		getIndices();
		return minIndex;
	}

	public int getMaxIndex() {
		// trigger derivation
		getIndices();
		return maxIndex;
	}

	public int getLevel() {
		return level;
	}

	public void add(Node node) {
		nodes.add(node);
	}

	public Set<Node> getElements() {
		return nodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.streamscope.cluster.Node#distance(org.
	 * processmining.analysis.streamscope.cluster.Node,
	 * org.processmining.analysis.streamscope.cluster.Distance)
	 */
	@Override
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
			double minDistance = 0.0;
			for (Node element : nodes) {
				double dist = element.distanceSingleLinkage(other, distance);
				if (dist > minDistance) {
					minDistance = dist;
				}
			}
			return minDistance;
		}
	}

}
