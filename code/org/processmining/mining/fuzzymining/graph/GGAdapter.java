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
import java.util.HashMap;
import java.util.Set;

import org.deckfour.gantzgraf.canvas.GGCanvas;
import org.deckfour.gantzgraf.event.GGGraphAdapter;
import org.deckfour.gantzgraf.model.GGEdge;
import org.deckfour.gantzgraf.model.GGGraph;
import org.deckfour.gantzgraf.model.GGNode;
import org.deckfour.gantzgraf.painter.GGEdgeGenericPainter;
import org.deckfour.gantzgraf.painter.GGEdgePainter;
import org.deckfour.gantzgraf.painter.GGNodeOctagonPainter;
import org.deckfour.gantzgraf.painter.GGNodePainter;
import org.deckfour.gantzgraf.painter.GGNodeRoundRectanglePainter;
import org.processmining.framework.ui.MainUI;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class GGAdapter extends GGGraph {

	public static final String ATTR_FUZZY_GRAPH = "FuzzyGraph";
	public static final String ATTR_FUZZY_GRAPH_ELEMENT = "FuzzyGraphElement";

	public static Color PRIMITIVE_BACKGROUND = new Color(240, 230, 200);
	public static Color PRIMITIVE_BORDER = new Color(20, 20, 20);
	public static Color PRIMITIVE_TEXT = new Color(0, 0, 0, 230);

	public static Color CLUSTER_BACKGROUND = new Color(100, 180, 20);
	public static Color CLUSTER_BORDER = new Color(20, 20, 20);
	public static Color CLUSTER_TEXT = new Color(10, 10, 10, 240);

	protected GGNodePainter primitiveNodePainter;
	protected GGNodePainter clusterNodePainter;

	protected FuzzyGraph fuzzyGraph;
	protected HashMap<Node, GGNode> nodeMap = new HashMap<Node, GGNode>();

	/**
	 * 
	 */
	public GGAdapter(FuzzyGraph fuzzyGraph) {
		this.fuzzyGraph = fuzzyGraph;
		// add nodes to GantzGraf graph
		primitiveNodePainter = new GGNodeRoundRectanglePainter(
				PRIMITIVE_BACKGROUND, PRIMITIVE_BORDER, PRIMITIVE_TEXT);
		clusterNodePainter = new GGNodeOctagonPainter(CLUSTER_BACKGROUND,
				CLUSTER_BORDER, CLUSTER_TEXT);
		Set<Node> fuzzyNodes = fuzzyGraph.getNodes();
		for (Node node : fuzzyNodes) {
			GGNode ggNode = new GGNode(new String[] {});
			if (node instanceof ClusterNode) {
				ClusterNode cluster = (ClusterNode) node;
				ggNode.setPainter(clusterNodePainter);
				ggNode.setLabel(getNodeLabel(cluster));
				ggNode.setGraphListener(new GGFuzzyGraphAdapter(cluster));
			} else {
				ggNode.setPainter(primitiveNodePainter);
				ggNode.setLabel(getNodeLabel(node));
			}
			ggNode.setAttribute(ATTR_FUZZY_GRAPH, fuzzyGraph);
			ggNode.setAttribute(GGAdapter.ATTR_FUZZY_GRAPH_ELEMENT, node);
			nodeMap.put(node, ggNode);
			this.add(ggNode);
		}
		// add edges to GantzGraf graph
		Set<Edge> edges = fuzzyGraph.getEdges().getEdges();
		for (Edge edge : edges) {
			GGNode ggSource = nodeMap.get(edge.getSource());
			GGNode ggTarget = nodeMap.get(edge.getTarget());
			GGEdgePainter painter = createEdgePainter(edge);
			GGEdge ggEdge = new GGEdge(ggSource, ggTarget, getEdgeLabel(edge));
			ggEdge.setPainter(painter);
			ggEdge.setRelativeWidth((float) edge.getSignificance() / 2f);
			this.add(ggEdge);
			ggEdge.setAttribute(ATTR_FUZZY_GRAPH, fuzzyGraph);
			ggEdge.setAttribute(GGAdapter.ATTR_FUZZY_GRAPH_ELEMENT, edge);
		}
	}

	public static String[] getNodeLabel(Node node) {
		if (node instanceof ClusterNode) {
			ClusterNode cluster = (ClusterNode) node;
			return new String[] { cluster.getElementName(),
					cluster.size() + " elements",
					"~" + MutableFuzzyGraph.format(cluster.getSignificance()) };
		} else {
			return new String[] { node.getElementName(), node.getEventType(),
					MutableFuzzyGraph.format(node.getSignificance()) };
		}
	}

	public static String[] getEdgeLabel(Edge edge) {
		return new String[] { MutableFuzzyGraph.format(edge.getSignificance()),
				MutableFuzzyGraph.format(edge.getCorrelation()) };
	}

	public static GGEdgePainter createEdgePainter(Edge edge) {
		int greyVal = (int) (edge.getCorrelation() * 180.0);
		greyVal = 200 - greyVal;
		Color edgeColor = new Color(greyVal, greyVal, greyVal);
		return new GGEdgeGenericPainter(edgeColor, edgeColor);
	}

	protected class GGFuzzyGraphAdapter extends GGGraphAdapter {

		protected ClusterNode cluster;

		protected GGFuzzyGraphAdapter(ClusterNode aCluster) {
			cluster = aCluster;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.processmining.mining.fuzzymining.gantzgraf.util.GGGraphAdapter
		 * #nodeSelected
		 * (org.processmining.mining.fuzzymining.gantzgraf.painter.GGCanvas,
		 * org.processmining.mining.fuzzymining.gantzgraf.model.GGGraph,
		 * org.processmining.mining.fuzzymining.gantzgraf.model.GGNode, float,
		 * float)
		 */
		@Override
		public void nodeSelected(GGCanvas canvs, GGGraph graph, GGNode node,
				float x, float y) {
			try {
				MainUI.getInstance().createFrame(
						"Detailled graph for 'Cluster " + cluster.getIndex()
								+ "'", cluster.getClusterGraphPanel());
			} catch (Exception e) {
				// oops... SNAFU
				e.printStackTrace();
			}
		}

	}

}
