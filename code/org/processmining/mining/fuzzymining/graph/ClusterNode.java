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

import java.awt.Color;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

import org.deckfour.gantzgraf.model.GGEdge;
import org.deckfour.gantzgraf.model.GGGraph;
import org.deckfour.gantzgraf.model.GGNode;
import org.deckfour.gantzgraf.painter.GGEdgeGenericPainter;
import org.deckfour.gantzgraf.painter.GGNodeOctagonPainter;
import org.deckfour.gantzgraf.painter.GGNodePainter;
import org.deckfour.gantzgraf.painter.GGNodeRectanglePainter;
import org.deckfour.gantzgraf.painter.GGNodeRoundRectanglePainter;
import org.deckfour.gantzgraf.ui.GGGraphView;
import org.processmining.framework.models.DotFileWriter;

public class ClusterNode extends Node implements DotFileWriter {

	protected String elementName;
	protected HashSet<Node> primitives;

	public ClusterNode(MutableFuzzyGraph graph, int index) {
		super(graph, index);
		elementName = "Cluster " + index;
		primitives = new HashSet<Node>();
	}

	public void add(Node node) {
		primitives.add(node);
	}

	public boolean remove(Node node) {
		return primitives.remove(node);
	}

	public Set<Node> getPrimitives() {
		return primitives;
	}

	public int size() {
		return primitives.size();
	}

	public double getSignificance() {
		double sig = 0.0;
		for (Node node : primitives) {
			sig += node.getSignificance();
		}
		return sig / primitives.size();
	}

	public void setSignificance(double significance) {
		throw new AssertionError(
				"Significance of cluster node cannot be modified!");
	}

	public String id() {
		return "cluster_" + index;
	}

	public boolean contains(Node node) {
		return primitives.contains(node);
	}

	public boolean isDirectlyConnectedTo(Node other) {
		for (Node node : primitives) {
			if (node.isDirectlyConnectedTo(other)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.fuzzymining.graph.Node#directlyFollows(org.
	 * processmining.mining.fuzzymining.graph.Node)
	 */
	public boolean directlyFollows(Node other) {
		for (Node node : primitives) {
			if (node.directlyFollows(other)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.fuzzymining.graph.Node#getPredecessors()
	 */
	public Set<Node> getPredecessors() {
		HashSet<Node> predecessors = new HashSet<Node>();
		for (Node node : primitives) {
			predecessors.addAll(node.getPredecessors());
		}
		predecessors.removeAll(primitives);
		predecessors.remove(this);
		return predecessors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.fuzzymining.graph.Node#getSuccessors()
	 */
	public Set<Node> getSuccessors() {
		HashSet<Node> successors = new HashSet<Node>();
		for (Node node : primitives) {
			successors.addAll(node.getSuccessors());
		}
		successors.removeAll(primitives);
		successors.remove(this);
		return successors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.DotFileWriter#writeToDot(java.io.Writer
	 * )
	 */
	public void writeToDot(Writer bw) throws IOException {
		bw
				.write(id()
						+ " [shape=\"octagon\",style=\"filled\",fillcolor=\"chartreuse3\",label=\"Cluster "
						+ index + "\\n" + primitives.size()
						+ " primitives\\n~ "
						+ MutableFuzzyGraph.format(getSignificance())
						+ "\"];\n");
	}

	public String getToolTipText() {
		return "<html><table><tr colspan=\"2\"><td>" + elementName
				+ "</td></tr>" + "<tr><td>Number of primitives:</td><td>"
				+ primitives.size() + "</td></tr>"
				+ "<tr><td>Mean significance:</td><td>"
				+ MutableFuzzyGraph.format(getSignificance()) + "</td></tr>";
	}

	public JPanel getClusterGraphPanel() throws Exception {
		GGNodePainter externalNodePainter = new GGNodeRectanglePainter(
				new Color(220, 220, 220), new Color(50, 50, 50), new Color(90,
						90, 90));
		GGNodePainter externalClusterPainter = new GGNodeOctagonPainter(
				new Color(180, 220, 180), new Color(50, 50, 50), new Color(70,
						70, 70));
		GGNodePainter internalNodePainter = new GGNodeRoundRectanglePainter(
				new Color(160, 140, 100), new Color(20, 20, 20), new Color(40,
						40, 40));
		GGGraph clusterGraph = new GGGraph();
		HashMap<String, GGNode> ezNodeMap = new HashMap<String, GGNode>();
		// write adjacent predecessor and successor nodes
		Set<Node> predecessors = getPredecessors();
		Set<Node> successors = getSuccessors();
		// unified set, to prevent duplicate nodes (both predecessor and
		// successor)
		HashSet<Node> adjacentNodes = new HashSet<Node>();
		adjacentNodes.addAll(predecessors);
		adjacentNodes.addAll(successors);
		GGNodePainter externalPainter;
		for (Node node : adjacentNodes) {
			if (node instanceof ClusterNode) {
				externalPainter = externalClusterPainter;
			} else {
				externalPainter = externalNodePainter;
			}
			String[] label = new String[] { node.getElementName(),
					node.getEventType(),
					MutableFuzzyGraph.format(getSignificance()) };
			GGNode ezNode = new GGNode(label, externalPainter);
			clusterGraph.add(ezNode);
			ezNodeMap.put(node.id(), ezNode);
			ezNode.setAttribute(GGAdapter.ATTR_FUZZY_GRAPH, graph);
			ezNode.setAttribute(GGAdapter.ATTR_FUZZY_GRAPH_ELEMENT, node);
		}
		// write cluster nodes
		for (Node node : primitives) {
			String[] label = new String[] { node.getElementName(),
					node.getEventType(),
					MutableFuzzyGraph.format(getSignificance()) };
			GGNode ezNode = new GGNode(label);
			ezNode.setPainter(internalNodePainter);
			clusterGraph.add(ezNode);
			ezNodeMap.put(node.id(), ezNode);
			ezNode.setAttribute(GGAdapter.ATTR_FUZZY_GRAPH, graph);
			ezNode.setAttribute(GGAdapter.ATTR_FUZZY_GRAPH_ELEMENT, node);
		}
		// asssemble edges
		Edges clusterEdges = new Edges(graph);
		// write edges within clusters
		addEdgesBetweenSets(primitives, primitives, clusterEdges);
		for (Edge edge : clusterEdges.getEdges()) {
			GGEdge ezEdge = new GGEdge(ezNodeMap.get(edge.getSource().id()),
					ezNodeMap.get(edge.getTarget().id()), null);
			clusterGraph.add(ezEdge);
			ezEdge.setAttribute(GGAdapter.ATTR_FUZZY_GRAPH, graph);
			ezEdge.setAttribute(GGAdapter.ATTR_FUZZY_GRAPH_ELEMENT, edge);
		}
		// create external edges
		Edges externalEdges = new Edges(graph);
		// write edges from predecessors to cluster nodes
		addEdgesBetweenSets(predecessors, primitives, externalEdges);
		// write edges from cluster nodes to successors
		addEdgesBetweenSets(primitives, successors, externalEdges);
		// write edges
		for (Edge edge : externalEdges.getEdges()) {
			GGEdge ezEdge = new GGEdge(ezNodeMap.get(edge.getSource().id()),
					ezNodeMap.get(edge.getTarget().id()), null);
			ezEdge.setPainter(new GGEdgeGenericPainter(
					new Color(150, 150, 150), new Color(120, 120, 120)));
			clusterGraph.add(ezEdge);
			ezEdge.setAttribute(GGAdapter.ATTR_FUZZY_GRAPH, graph);
			ezEdge.setAttribute(GGAdapter.ATTR_FUZZY_GRAPH_ELEMENT, edge);
		}
		return new GGGraphView(clusterGraph);
	}

	public void writeClusterSubgraphToDot(Writer bw) throws IOException {
		// write DOT prelude
		bw
				.write("digraph CLUSTERGRAPH_"
						+ index
						+ " { ranksep=\".3\"; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; ");
		bw.write("fontname=\"Helvetica\";rankdir=\"TB\"; \n");
		bw
				.write("edge [arrowsize=\"0.5\",decorate=true,fontname=\"Helvetica\",fontsize=\"8\"];\n");
		bw
				.write("node [height=\".1\",width=\".2\",fontname=\"Helvetica\",fontsize=\"8\",shape=\"box\",style=\"filled\",fillcolor=\"palegoldenrod\"];\n");
		// write adjacent predecessor and successor nodes
		Set<Node> predecessors = getPredecessors();
		Set<Node> successors = getSuccessors();
		// unified set, to prevent duplicate nodes (both predecessor and
		// successor)
		HashSet<Node> adjacentNodes = new HashSet<Node>();
		adjacentNodes.addAll(predecessors);
		adjacentNodes.addAll(successors);
		for (Node node : adjacentNodes) {
			node.writeToDot(bw);
		}
		// write cluster nodes
		bw.write("subgraph " + id() + " {\n");
		bw.write("node [fillcolor=\"lightgoldenrod1\"];\n");
		bw.write("style=\"filled\";\ncolor=\"chartreuse3\";\n,label=\""
				+ elementName + "\";\n");
		for (Node node : primitives) {
			node.writeToDot(bw);
		}
		// asssemble edges
		Edges clusterEdges = new Edges(graph);
		// write edges within clusters
		addEdgesBetweenSets(primitives, primitives, clusterEdges);
		clusterEdges.writeToDot(bw);
		// close cluster subgraph
		bw.write("}\n");
		// create external edges
		Edges externalEdges = new Edges(graph);
		// write edges from predecessors to cluster nodes
		addEdgesBetweenSets(predecessors, primitives, externalEdges);
		// write edges from cluster nodes to successors
		addEdgesBetweenSets(primitives, successors, externalEdges);
		// write edges
		externalEdges.writeToDot(bw);
		// finish graph
		bw.write("}\n");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.fuzzymining.graph.Node#getElementName()
	 */
	@Override
	public String getElementName() {
		return elementName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.fuzzymining.graph.Node#getEventType()
	 */
	@Override
	public String getEventType() {
		return "complete";
	}

	public void setElementName(String name) {
		elementName = name;
	}

	public void setEventType(String type) {
		throw new AssertionError("type cannot be changed for cluster nodes!");
	}

	protected void addEdgesBetweenSets(Set<Node> sources, Set<Node> targets,
			Edges edges) {
		int x, y;
		double sig, cor;
		for (Node source : sources) {
			x = source.getIndex();
			for (Node target : targets) {
				y = target.getIndex();
				if (x < graph.getNumberOfInitialNodes()
						&& y < graph.getNumberOfInitialNodes()) {
					sig = graph.getBinarySignificance(x, y);
					cor = graph.getBinaryCorrelation(x, y);
				} else {
					sig = 0.5;
					cor = 0.5;
				}
				if ((x == y) && sig < 0.001) {
					continue;
				}
				if (sig > 0.0) {
					edges.addEdge(source, target, sig, cor);
				}
			}
		}
	}

}
