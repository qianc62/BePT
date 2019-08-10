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
package org.processmining.mining.fuzzymining.graph;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.processmining.framework.models.DotFileWriter;

public class Edges implements DotFileWriter {

	protected HashSet<Edge> edges;
	protected FuzzyGraph graph;
	protected double attenuationThreshold;

	public Edges(FuzzyGraph graph) {
		this.graph = graph;
		edges = new HashSet<Edge>();
		attenuationThreshold = 1.0;
	}

	public void setAttenuationThreshold(double attThreshold) {
		attenuationThreshold = attThreshold;
	}

	public void addEdge(Node source, Node target, double significance,
			double correlation) {
		Edge edge = new Edge(source, target, significance, correlation);
		if (edges.contains(edge)) {
			for (Edge oE : edges) {
				if (oE.equals(edge)) {
					// merge to max value of the two merged edges
					if (edge.significance > oE.significance) {
						oE.significance = edge.significance;
					}
					if (edge.correlation > oE.correlation) {
						oE.correlation = edge.correlation;
					}
				}
				break;
			}
		} else {
			// insert new edge
			edges.add(edge);
		}
	}

	public Edge getEdge(Node source, Node target) {
		for (Edge edge : edges) {
			if (edge.source.equals(source) && edge.target.equals(target)) {
				return edge;
			}
		}
		return null;
	}

	public Set<Edge> getEdges() {
		return edges;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.DotFileWriter#writeToDot(java.io.Writer
	 * )
	 */
	public void writeToDot(Writer bw) throws IOException {
		for (Edge edge : edges) {
			edge.setAttenuationThreshold(attenuationThreshold);
			edge.writeToDot(bw);
		}
	}

}
