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
package org.processmining.mining.fuzzymining.graph.transform;

import java.util.ArrayList;

import org.processmining.mining.fuzzymining.graph.ClusterNode;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;
import org.processmining.mining.fuzzymining.graph.Node;

public class ClusterUtils {

	public static double calculateDistance(Node a, Node b) {
		FuzzyGraph g = a.getGraph();
		int indexA = a.getIndex();
		int indexB = b.getIndex();
		double dirCorAB = g.getBinaryCorrelation(indexA, indexB);
		double dirCorBA = g.getBinaryCorrelation(indexB, indexA);
		double dirCor = Math.max(dirCorAB, dirCorBA);
		return 1.0 - dirCor;
	}

	public static double calculateDistance(ClusterNode cluster, Node node) {
		double minDistance = 1.0;
		double distance;
		for (Node other : cluster.getPrimitives()) {
			distance = calculateDistance(node, other);
			if (distance < minDistance) {
				minDistance = distance;
			}
		}
		return minDistance;
	}

	public static double calculateDistance(ClusterNode a, ClusterNode b) {
		double minDistance = 1.0;
		double distance;
		for (Node node : a.getPrimitives()) {
			distance = calculateDistance(b, node);
			if (distance < minDistance) {
				minDistance = distance;
			}
		}
		return minDistance;
	}

	public static double calculateDiameter(ClusterNode cluster) {
		double diameter = 1.0;
		Node[] clusterNodeSet = cluster.getPrimitives().toArray(new Node[0]);
		ArrayList<Node> path;
		double dist;
		for (int x = 0; x < clusterNodeSet.length; x++) {
			for (int y = 0; y < clusterNodeSet.length; y++) {
				if (clusterNodeSet[x].equals(clusterNodeSet[y])) {
					continue;
				}
				path = findPath(clusterNodeSet[x], clusterNodeSet[y],
						clusterNodeSet, clusterNodeSet.length - 1);
				if (path != null) {
					dist = 1.0 - getPathCorrelation(path);
					if (dist < diameter) {
						diameter = dist;
					}
				}
			}
		}
		return diameter;
	}

	protected static ArrayList<Node> findPath(Node a, Node b, Node[] nodeSet,
			int maxDepth) {
		FuzzyGraph graph = a.getGraph();
		ArrayList<Node> path = new ArrayList<Node>();
		int indexA = a.getIndex();
		int indexB = b.getIndex();
		if (graph.getBinarySignificance(indexA, indexB) > 0.0) {
			// direct path; return it
			path.add(a);
			path.add(b);
			return path;
		} else if (maxDepth > 0) {
			// start recursion
			double maxCorrelation = 1.0;
			double currentCorrelation;
			ArrayList<Node> bestPath = null;
			ArrayList<Node> currentPath = null;
			for (int i = 0; i < nodeSet.length; i++) {
				if (nodeSet[i].getIndex() == indexA) {
					continue;
				} // skip current source index
				if (graph.getBinarySignificance(indexA, nodeSet[i].getIndex()) > 0.0) {
					// found successor to node A
					currentPath = findPath(graph.getPrimitiveNode(nodeSet[i]
							.getIndex()), b, nodeSet, maxDepth - 1);
					if (currentPath != null) {
						currentCorrelation = getPathCorrelation(currentPath);
						if (currentCorrelation > maxCorrelation) {
							// found new path with highest correlation so far
							maxCorrelation = currentCorrelation;
							bestPath = currentPath;
						}
					}
				}
			}
			// check if best path is valid
			if (bestPath != null) {
				// yay!
				path.add(a);
				path.addAll(bestPath);
				return path;
			} else {
				// dead end
				return null;
			}
		} else {
			return null;
		}
	}

	protected static double getPathCorrelation(ArrayList<Node> path) {
		FuzzyGraph graph = path.get(0).getGraph();
		double minCor = 1.0;
		double curCor;
		Node pre = path.get(0);
		Node post;
		for (int i = 1; i < path.size(); i++) {
			post = path.get(i);
			curCor = graph
					.getBinaryCorrelation(pre.getIndex(), post.getIndex());
			if (curCor < minCor) {
				// set new correlation minimum
				minCor = curCor;
			}
			pre = post;
		}
		return minCor;
	}
}
