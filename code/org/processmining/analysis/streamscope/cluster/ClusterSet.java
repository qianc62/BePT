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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.processmining.analysis.streamscope.EventClassTable;
import org.processmining.analysis.streamscope.EventClassTable.EventClass;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.Progress;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ClusterSet {

	protected EventClassTable ecTable;
	protected Distance distance;
	protected Node tree;
	protected int level;

	public ClusterSet(LogReader log, Progress progress) throws IOException {
		distance = Distance.create(log, progress);
		progress.setNote("Preparing event class clusters...");
		progress.setProgress(-1);
		List<Node> nodes = new ArrayList<Node>();
		for (EventClass ec : distance.getEventClassTable().getEventClasses()) {
			nodes.add(new Node(ec.getIndex(), ec.name()));
		}
		level = 0;
		while (nodes.size() > 1) {
			level++;
			nodes = clusterStep(nodes, level);
		}
		tree = nodes.get(0);
		// recreate table and re-index
		ecTable = new EventClassTable(null, null);
		registerNodeRecursively(tree, ecTable);
		updateIndicesRecursively(tree);
	}

	public int getNumberOfLevels() {
		return level;
	}

	protected void updateIndicesRecursively(Node node) {
		if (node instanceof ClusterNode) {
			ClusterNode cluster = (ClusterNode) node;
			// reset cluster
			cluster.resetIndex(0);
			// recurse down
			for (Node element : cluster.getElements()) {
				updateIndicesRecursively(element);
			}
		} else {
			node.resetIndex(ecTable.getIndex(node.getName()));
		}
	}

	public List<Node> getOrderedNodesForLevel(int onLevel) {
		if (onLevel < 0 || onLevel > level) {
			return null;
		} else {
			ArrayList<Node> ordered = new ArrayList<Node>();
			orderNodesRecursivelyOnLevel(tree, ordered, onLevel);
			return ordered;
		}

	}

	public EventClassTable getOrderedEventClassTable() {
		return ecTable;
	}

	protected void orderNodesRecursivelyOnLevel(Node node, List<Node> ordered,
			int level) {
		if (node instanceof ClusterNode) {
			ClusterNode cluster = (ClusterNode) node;
			if (cluster.getLevel() > level) {
				// recurse deeper down
				for (Node element : cluster.getElements()) {
					orderNodesRecursivelyOnLevel(element, ordered, level);
				}
			} else {
				// recursion finished here, add self in list
				ordered.add(cluster);
			}
		} else {
			// recursion finished here, add self in list
			ordered.add(node);
		}
	}

	protected void registerNodeRecursively(Node node, EventClassTable table) {
		if (node instanceof ClusterNode) {
			ClusterNode cluster = (ClusterNode) node;
			for (Node element : cluster.getElements()) {
				registerNodeRecursively(element, table);
			}
		} else {
			table.register(node.getName());
		}
	}

	protected List<Node> clusterStep(List<Node> nodes, int level) {
		ArrayList<Node> tmpNodes = new ArrayList<Node>(nodes);
		Node victimA = null;
		Node victimB = null;
		double lastDistance = -1;
		;
		for (Node a : nodes) {
			for (Node b : nodes) {
				if (a == b) {
					continue;
				}
				// double dist = a.distanceSingleLinkage(b, distance);
				// double dist = distance.distanceSingleLinkage(a, b);
				// double dist = distance.distanceAverageLinkage(a, b);
				double dist = distance.distanceCompleteLinkage(a, b);
				if (dist > lastDistance) {
					victimA = a;
					victimB = b;
					lastDistance = dist;
				}
			}
		}
		tmpNodes.remove(victimA);
		tmpNodes.remove(victimB);
		ClusterNode cluster = new ClusterNode(level);
		cluster.add(victimA);
		cluster.add(victimB);
		tmpNodes.add(cluster);
		return tmpNodes;
	}

}
