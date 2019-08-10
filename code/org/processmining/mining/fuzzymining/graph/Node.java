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

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.DotFileWriter;

public class Node implements DotFileWriter {

	protected MutableFuzzyGraph graph;
	protected int index;

	public Node(MutableFuzzyGraph graph, int index) {
		this.graph = graph;
		this.index = index;
	}

	public boolean isDirectlyConnectedTo(Node other) {
		if (other instanceof ClusterNode) {
			return other.isDirectlyConnectedTo(this);
		} else {
			return ((graph.getBinarySignificance(index, other.index) > 0.0) || (graph
					.getBinarySignificance(other.index, index) > 0.0));
		}
	}

	public boolean directlyFollows(Node other) {
		if (other instanceof ClusterNode) {
			Set<Node> otherPrimitives = ((ClusterNode) other).getPrimitives();
			for (Node n : otherPrimitives) {
				if (directlyFollows(n)) {
					return true;
				}
			}
			return false;
		} else {
			return (graph.getBinarySignificance(index, other.index) > 0.0);
		}
	}

	public Set<Node> getPredecessors() {
		HashSet<Node> predecessors = new HashSet<Node>();
		for (int x = 0; x < graph.getNumberOfInitialNodes(); x++) {
			if (x == index) {
				continue; // ignore self
			} else if (graph.getBinarySignificance(x, index) > 0.0) {
				Node pre = graph.getNodeMappedTo(x);
				if (pre != null) {
					predecessors.add(pre);
				}
			}
		}
		return predecessors;
	}

	public Set<Node> getSuccessors() {
		HashSet<Node> successors = new HashSet<Node>();
		for (int y = 0; y < graph.getNumberOfInitialNodes(); y++) {
			if (y == index) {
				continue; // ignore self
			} else if (graph.getBinarySignificance(index, y) > 0.0) {
				Node post = graph.getNodeMappedTo(y);
				if (post != null) {
					successors.add(post);
				}
			}
		}
		return successors;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public FuzzyGraph getGraph() {
		return graph;
	}

	public double getSignificance() {
		return graph.getNodeSignificanceMetric().getMeasure(index);
	}

	public void setSignificance(double significance) {
		graph.getNodeSignificanceMetric().setMeasure(index, significance);
	}

	public String id() {
		return "node_" + index;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Node) {
			return (index == ((Node) obj).index);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return index;
	}

	public String toString() {
		return getElementName() + " (" + getEventType() + ")";
	}

	public String getElementName() {
		return graph.getLogEvents().get(index).getModelElementName();
	}

	public void setElementName(String name) {
		LogEvent event = graph.getLogEvents().get(index);
		graph.updateLogEvent(index, new LogEvent(name, event.getEventType(),
				event.getOccurrenceCount()));
	}

	public String getEventType() {
		return graph.getLogEvents().get(index).getEventType();
	}

	public void setEventType(String type) {
		LogEvent event = graph.getLogEvents().get(index);
		graph.updateLogEvent(index, new LogEvent(event.getModelElementName(),
				type, event.getOccurrenceCount()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.DotFileWriter#writeToDot(java.io.Writer
	 * )
	 */
	public void writeToDot(Writer bw) throws IOException {
		LogEvent evt = graph.getLogEvents().get(index);
		bw.write(id() + " [label=\"" + evt.getModelElementName() + "\\n"
				+ evt.getEventType() + "\\n"
				+ MutableFuzzyGraph.format(getSignificance()) + "\"];\n");
	}

	public String getToolTipText() {
		LogEvent evt = graph.getLogEvents().get(index);
		return "<html><table><tr colspan=\"2\"><td>"
				+ evt.getModelElementName() + "</td></tr>"
				+ "<tr><td>Event type:</td><td>" + evt.getEventType()
				+ "</td></tr>" + "<tr><td>Significance:</td><td>"
				+ MutableFuzzyGraph.format(getSignificance()) + "</td></tr>";
	}

}
