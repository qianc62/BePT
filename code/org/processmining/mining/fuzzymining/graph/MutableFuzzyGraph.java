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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.deckfour.gantzgraf.ui.GGGraphUI;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.mining.fuzzymining.metrics.MetricsRepository;
import org.processmining.mining.fuzzymining.metrics.binary.BinaryMetric;
import org.processmining.mining.fuzzymining.metrics.unary.UnaryMetric;

public class MutableFuzzyGraph implements FuzzyGraph {

	protected UnaryMetric nodeSignificance;
	protected BinaryMetric edgeSignificance;
	protected BinaryMetric edgeCorrelation;

	protected int numberOfInitialNodes;
	protected LogEvents events;
	protected Node[] primitiveNodes;
	protected ArrayList<ClusterNode> clusterNodes;
	protected double[][] actBinarySignificance;
	protected double[][] actBinaryCorrelation;
	protected Node[] nodeAliasMap;

	protected HashMap<String, String> attributes;

	protected static NumberFormat numberFormat = NumberFormat.getInstance();
	{
		numberFormat.setMinimumFractionDigits(3);
		numberFormat.setMaximumFractionDigits(3);
	}

	public static String format(double number) {
		return numberFormat.format(number);
	}

	public MutableFuzzyGraph(UnaryMetric nodeSignificance,
			BinaryMetric edgeSignificance, BinaryMetric edgeCorrelation,
			LogEvents events) {
		this.nodeSignificance = nodeSignificance;
		this.edgeSignificance = edgeSignificance;
		this.edgeCorrelation = edgeCorrelation;
		this.events = events;
		numberOfInitialNodes = events.size();
		primitiveNodes = new Node[numberOfInitialNodes];
		nodeAliasMap = new Node[numberOfInitialNodes];
		initializeGraph();
	}

	public MutableFuzzyGraph(MetricsRepository metrics) {
		this(metrics.getAggregateUnaryMetric(), metrics
				.getAggregateSignificanceBinaryMetric(), metrics
				.getAggregateCorrelationBinaryMetric(), metrics.getLogReader()
				.getLogSummary().getLogEvents());
	}

	/**
	 * initializes the graph structure as found in metrics repository
	 */
	public void initializeGraph() {
		clusterNodes = new ArrayList<ClusterNode>();
		actBinarySignificance = new double[numberOfInitialNodes][numberOfInitialNodes];
		actBinaryCorrelation = new double[numberOfInitialNodes][numberOfInitialNodes];
		for (int x = 0; x < numberOfInitialNodes; x++) {
			for (int y = 0; y < numberOfInitialNodes; y++) {
				actBinarySignificance[x][y] = edgeSignificance.getMeasure(x, y);
				actBinaryCorrelation[x][y] = edgeCorrelation.getMeasure(x, y);
			}
		}
		for (int i = 0; i < numberOfInitialNodes; i++) {
			Node node = new Node(this, i);
			primitiveNodes[i] = node;
			nodeAliasMap[i] = node;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.FuzzyGraph#getNumberOfInitialNodes
	 * ()
	 */
	public int getNumberOfInitialNodes() {
		return numberOfInitialNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.fuzzymining.graph.FuzzyGraph#getLogEvents()
	 */
	public LogEvents getLogEvents() {
		return events;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.FuzzyGraph#getPrimitiveNode
	 * (int)
	 */
	public Node getPrimitiveNode(int index) {
		return primitiveNodes[index];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.FuzzyGraph#getNodeMappedTo
	 * (int)
	 */
	public Node getNodeMappedTo(int index) {
		return nodeAliasMap[index];
	}

	public void setNodeAliasMapping(int index, Node alias) {
		nodeAliasMap[index] = alias;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.FuzzyGraph#getClusterNodes()
	 */
	public List<ClusterNode> getClusterNodes() {
		return clusterNodes;
	}

	public void addClusterNode(ClusterNode cluster) {
		clusterNodes.add(cluster);
	}

	public boolean removeClusterNode(ClusterNode cluster) {
		return clusterNodes.remove(cluster);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.FuzzyGraph#getBinarySignificance
	 * (int, int)
	 */
	public double getBinarySignificance(int fromIndex, int toIndex) {
		return actBinarySignificance[fromIndex][toIndex];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.FuzzyGraph#getBinaryCorrelation
	 * (int, int)
	 */
	public double getBinaryCorrelation(int fromIndex, int toIndex) {
		return actBinaryCorrelation[fromIndex][toIndex];
	}

	public void setBinarySignificance(int fromIndex, int toIndex, double value) {
		actBinarySignificance[fromIndex][toIndex] = value;
	}

	public void setBinaryCorrelation(int fromIndex, int toIndex, double value) {
		actBinaryCorrelation[fromIndex][toIndex] = value;
	}

	public void removePermanently(Edge edge) {
		removeEdgePermanently(edge.getSource(), edge.getTarget());
	}

	public void removeEdgePermanently(Node source, Node target) {
		if (source instanceof ClusterNode) {
			for (Node sourcePrimitive : ((ClusterNode) source).getPrimitives()) {
				removeEdgePermanently(sourcePrimitive, target);
			}
		} else if (target instanceof ClusterNode) {
			for (Node targetPrimitive : ((ClusterNode) target).getPrimitives()) {
				removeEdgePermanently(source, targetPrimitive);
			}
		} else {
			removeEdgePermanently(source.getIndex(), target.getIndex());
		}
	}

	protected void removeEdgePermanently(int from, int to) {
		this.edgeSignificance.setMeasure(from, to, 0.0);
		this.edgeCorrelation.setMeasure(from, to, 0.0);
		this.actBinarySignificance[from][to] = 0.0;
		this.actBinaryCorrelation[from][to] = 0.0;
	}

	public void hidePermanently(Node node) {
		if (node instanceof ClusterNode) {
			// cluster, remove all primitives
			HashSet<Node> primitives = new HashSet<Node>(((ClusterNode) node)
					.getPrimitives());
			for (Node primitive : primitives) {
				hidePermanently(primitive);
			}
			clusterNodes.remove(node);
		} else {
			// primitive node, remove permanently
			int removeIndex = node.getIndex();
			// remove from metrics
			this.nodeSignificance.setMeasure(removeIndex, 0.0);
			for (int x = 0; x < this.numberOfInitialNodes; x++) {
				this.edgeSignificance.setMeasure(x, removeIndex, 0.0);
				this.edgeSignificance.setMeasure(removeIndex, x, 0.0);
				this.edgeCorrelation.setMeasure(x, removeIndex, 0.0);
				this.edgeCorrelation.setMeasure(removeIndex, x, 0.0);
				this.actBinarySignificance[x][removeIndex] = 0.0;
				this.actBinarySignificance[removeIndex][x] = 0.0;
				this.actBinaryCorrelation[x][removeIndex] = 0.0;
				this.actBinaryCorrelation[removeIndex][x] = 0.0;
			}
			// remove from clusters
			for (int i = 0; i < this.clusterNodes.size(); i++) {
				ClusterNode cluster = this.clusterNodes.get(i);
				if (cluster.contains(node)) {
					cluster.remove(node);
					if (cluster.size() == 0) {
						clusterNodes.remove(i);
						i--;
					}
				}
			}
			// mark as deleted in node alias map
			nodeAliasMap[removeIndex] = null;
		}
	}

	public void removePermanently(Node node) {
		if (node instanceof ClusterNode) {
			// cluster, remove all primitives
			HashSet<Node> primitives = new HashSet<Node>(((ClusterNode) node)
					.getPrimitives());
			for (Node primitive : primitives) {
				removePermanently(primitive);
			}
			clusterNodes.remove(node);
		} else {
			// primitive node, remove permanently
			int removeIndex = node.getIndex();
			// remove from metrics
			this.nodeSignificance = removeIndex(this.nodeSignificance,
					removeIndex);
			this.edgeSignificance = removeIndex(this.edgeSignificance,
					removeIndex);
			this.edgeCorrelation = removeIndex(this.edgeCorrelation,
					removeIndex);
			// remove from actual edge mappings
			this.actBinarySignificance = removeIndex(
					this.actBinarySignificance, removeIndex);
			this.actBinaryCorrelation = removeIndex(this.actBinaryCorrelation,
					removeIndex);
			// remove from clusters
			for (int i = 0; i < this.clusterNodes.size(); i++) {
				ClusterNode cluster = this.clusterNodes.get(i);
				if (cluster.contains(node)) {
					cluster.remove(node);
					if (cluster.size() == 0) {
						clusterNodes.remove(i);
						i--;
					}
				}
			}
			// remove from nodex, log events and adjust number of nodes
			numberOfInitialNodes--;
			events.remove(removeIndex);
			// remove from node mappings and set of initial nodes
			Node[] nPrimitiveNodes = new Node[primitiveNodes.length - 1];
			Node[] nNodeAliasMap = new Node[nodeAliasMap.length - 1];
			int nIndex = 0;
			for (int index = 0; index < primitiveNodes.length; index++) {
				if (index != removeIndex) {
					nPrimitiveNodes[nIndex] = primitiveNodes[index];
					nNodeAliasMap[nIndex] = nodeAliasMap[index];
					nIndex++;
				}
			}
			primitiveNodes = nPrimitiveNodes;
			nodeAliasMap = nNodeAliasMap;
			// adjust node indices
			for (Node n : primitiveNodes) {
				if (n.getIndex() >= removeIndex) {
					n.setIndex(n.getIndex() - 1);
				}
			}
		}
	}

	public Node addNode(LogEvent event) {
		events.add(event);
		numberOfInitialNodes++;
		this.nodeSignificance = addIndex(this.nodeSignificance);
		this.edgeSignificance = addIndex(this.edgeSignificance);
		this.edgeCorrelation = addIndex(this.edgeCorrelation);
		this.actBinarySignificance = addIndex(this.actBinarySignificance);
		this.actBinaryCorrelation = addIndex(this.actBinaryCorrelation);
		for (ClusterNode cluster : clusterNodes) {
			cluster.setIndex(cluster.getIndex() + 1);
		}
		Node node = new Node(this, events.size() - 1);
		Node[] nPrimitiveNodes = new Node[numberOfInitialNodes];
		Node[] nNodeAliasMap = new Node[numberOfInitialNodes];
		for (int index = 0; index < (numberOfInitialNodes - 1); index++) {
			nPrimitiveNodes[index] = primitiveNodes[index];
			nNodeAliasMap[index] = nodeAliasMap[index];
		}
		nPrimitiveNodes[numberOfInitialNodes - 1] = node;
		nNodeAliasMap[numberOfInitialNodes - 1] = node;
		primitiveNodes = nPrimitiveNodes;
		nodeAliasMap = nNodeAliasMap;
		return node;
	}

	protected static BinaryMetric removeIndex(BinaryMetric original, int index) {
		BinaryMetric removed = new BinaryMetric(original.getName(), original
				.getDescription(), original.size() - 1);
		int removedX = 0;
		int removedY = 0;
		for (int x = 0; x < original.size(); x++) {
			if (x != index) {
				for (int y = 0; y < original.size(); y++) {
					if (y != index) {
						removed.setMeasure(removedX, removedY, original
								.getMeasure(x, y));
						removedY++;
					}
				}
				removedX++;
			}
			removedY = 0;
		}
		return removed;
	}

	protected static UnaryMetric removeIndex(UnaryMetric original, int index) {
		UnaryMetric removed = new UnaryMetric(original.getName(), original
				.getDescription(), original.size() - 1);
		int removedX = 0;
		for (int x = 0; x < original.size(); x++) {
			if (x != index) {
				removed.setMeasure(removedX, original.getMeasure(x));
				removedX++;
			}
		}
		return removed;
	}

	protected static double[][] removeIndex(double[][] original, int index) {
		int originalSize = original[0].length;
		double[][] removed = new double[originalSize - 1][originalSize - 1];
		int removedX = 0;
		int removedY = 0;
		for (int x = 0; x < originalSize; x++) {
			if (x != index) {
				for (int y = 0; y < originalSize; y++) {
					if (y != index) {
						removed[removedX][removedY] = original[x][y];
						removedY++;
					}
				}
				removedX++;
			}
			removedY = 0;
		}
		return removed;
	}

	protected static BinaryMetric addIndex(BinaryMetric original) {
		BinaryMetric added = new BinaryMetric(original.getName(), original
				.getDescription(), original.size() + 1);
		for (int x = 0; x < original.size(); x++) {
			for (int y = 0; y < original.size(); y++) {
				added.setMeasure(x, y, original.getMeasure(x, y));
			}
		}
		return added;
	}

	protected static UnaryMetric addIndex(UnaryMetric original) {
		UnaryMetric added = new UnaryMetric(original.getName(), original
				.getDescription(), original.size() + 1);
		for (int x = 0; x < original.size(); x++) {
			added.setMeasure(x, original.getMeasure(x));
		}
		return added;
	}

	protected double[][] addIndex(double[][] original) {
		int originalSize = original[0].length;
		double[][] added = new double[originalSize + 1][originalSize + 1];
		for (int x = 0; x < originalSize; x++) {
			for (int y = 0; y < originalSize; y++) {
				added[x][y] = original[x][y];
			}
		}
		for (int x = 0; x < originalSize + 1; x++) {
			added[x][originalSize] = 0.0;
			added[originalSize][x] = 0.0;
		}
		return added;
	}

	public double getThresholdShowingPrimitives(int numberOfPrimitives) {
		double[] significances = getSortedNodeSignificances();
		int index = significances.length - numberOfPrimitives;
		if (index < 0) {
			index = 0;
		}
		return significances[index];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.fuzzymining.graph.FuzzyGraph#
	 * getMinimalNodeSignificance()
	 */
	public double getMinimalNodeSignificance() {
		double[] significances = getSortedNodeSignificances();
		return significances[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.fuzzymining.graph.FuzzyGraph#
	 * getSortedNodeSignificances()
	 */
	public double[] getSortedNodeSignificances() {
		double[] significances = new double[primitiveNodes.length];
		for (int i = 0; i < primitiveNodes.length; i++) {
			significances[i] = primitiveNodes[i].getSignificance();
		}
		Arrays.sort(significances); // in ascending numerical order
		return significances;
	}

	public Set<Node> getNodes() {
		HashSet<Node> activeNodes = new HashSet<Node>();
		for (int i = 0; i < numberOfInitialNodes; i++) {
			if (nodeAliasMap[i] != null) {
				activeNodes.add(nodeAliasMap[i]);
			}
		}
		return activeNodes;
	}

	public Edges getEdges() {
		Edges edges = new Edges(this);
		for (int x = 0; x < numberOfInitialNodes; x++) {
			for (int y = 0; y < numberOfInitialNodes; y++) {
				Node source = nodeAliasMap[x];
				Node target = nodeAliasMap[y];
				if (source == null
						|| target == null
						|| (source.equals(target) && source instanceof ClusterNode)) {
					continue; // do not draw cluster self-references
				}
				if ((x == y) && actBinarySignificance[x][y] < 0.001) {
					continue;
				}
				if (actBinarySignificance[x][y] > 0.0) {
					edges.addEdge(nodeAliasMap[x], nodeAliasMap[y],
							actBinarySignificance[x][y],
							actBinaryCorrelation[x][y]);
				}
			}
		}
		return edges;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.DotFileWriter#writeToDot(java.io.Writer
	 * )
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.FuzzyGraph#writeToDot(java
	 * .io.Writer)
	 */
	public void writeToDot(Writer bw) throws IOException {
		bw
				.write("digraph G { ranksep=\".3\"; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; ");
		bw.write("fontname=\"Helvetica\";rankdir=\"TB\"; \n");
		bw
				.write("edge [arrowsize=\"0.5\",decorate=false,fontname=\"Helvetica\",fontsize=\"8\"];\n");
		bw
				.write("node [height=\".1\",width=\".2\",fontname=\"Helvetica\",fontsize=\"8\",shape=\"box\",style=\"filled\",fillcolor=\"palegoldenrod\"];\n");
		// assemble node set
		Set<Node> activeNodes = getNodes();
		for (Node node : activeNodes) {
			node.writeToDot(bw);
		}
		// write edges
		Edges edges = getEdges();
		edges.writeToDot(bw);
		bw.write("}\n");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.FuzzyGraph#getGraphPanel()
	 */
	public JPanel getGraphPanel() throws Exception {
		GGAdapter ggAdapter = new GGAdapter(this);
		return new GGGraphUI(ggAdapter);
	}

	public int getEventClassIndex(AuditTrailEntry ate) {
		return getEventClassIndex(ate.getElement(), ate.getType());
	}

	public int getEventClassIndex(String element, String type) {
		return events.findLogEventNumber(element, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.fuzzymining.graph.FuzzyGraph#
	 * getEdgeCorrelationMetric()
	 */
	public BinaryMetric getEdgeCorrelationMetric() {
		return this.edgeCorrelation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.fuzzymining.graph.FuzzyGraph#
	 * getEdgeSignificanceMetric()
	 */
	public BinaryMetric getEdgeSignificanceMetric() {
		return this.edgeSignificance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.fuzzymining.graph.FuzzyGraph#
	 * getNodeSignificanceMetric()
	 */
	public UnaryMetric getNodeSignificanceMetric() {
		return this.nodeSignificance;
	}

	public void updateLogEvent(int index, LogEvent event) {
		assert (index >= 0 || index < events.size());
		events.remove(index);
		events.add(index, event);
	}

	public Object clone() {
		MutableFuzzyGraph clone = null;
		try {
			clone = (MutableFuzzyGraph) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		clone.nodeSignificance = new UnaryMetric("Node significance",
				"Node significance", this.nodeSignificance);
		clone.edgeSignificance = new BinaryMetric("Edge significance",
				"Edge significance", this.edgeSignificance);
		clone.edgeCorrelation = new BinaryMetric("Edge correlation",
				"Edge correlation", this.edgeCorrelation);
		clone.events = new LogEvents();
		for (int i = 0; i < this.events.size(); i++) {
			LogEvent event = this.events.get(i);
			clone.events.add(new LogEvent(event.getModelElementName(), event
					.getEventType(), event.getOccurrenceCount()));
		}
		clone.primitiveNodes = new Node[clone.numberOfInitialNodes];
		clone.nodeAliasMap = new Node[clone.numberOfInitialNodes];
		clone.clusterNodes = new ArrayList<ClusterNode>();
		clone.actBinarySignificance = new double[clone.numberOfInitialNodes][clone.numberOfInitialNodes];
		clone.actBinaryCorrelation = new double[clone.numberOfInitialNodes][clone.numberOfInitialNodes];
		for (int x = 0; x < clone.numberOfInitialNodes; x++) {
			for (int y = 0; y < numberOfInitialNodes; y++) {
				clone.actBinarySignificance[x][y] = this.actBinarySignificance[x][y];
				clone.actBinaryCorrelation[x][y] = this.actBinaryCorrelation[x][y];
			}
		}
		HashMap<Node, Node> cloneNodeMap = new HashMap<Node, Node>();
		for (int i = 0; i < clone.numberOfInitialNodes; i++) {
			Node node = new Node(clone, i);
			cloneNodeMap.put(this.getPrimitiveNode(i), node);
			clone.primitiveNodes[i] = node;
			clone.nodeAliasMap[i] = node;
		}
		for (int i = 0; i < this.nodeAliasMap.length; i++) {
			if (this.nodeAliasMap[i] == null) {
				clone.nodeAliasMap[i] = null;
			}
		}
		// restore clustering
		int clusterIndex = numberOfInitialNodes + 1;
		for (ClusterNode cluster : this.clusterNodes) {
			ClusterNode cCluster = new ClusterNode(clone, cluster.getIndex());
			clone.addClusterNode(cCluster);
			clusterIndex++;
			for (Node primitive : cluster.getPrimitives()) {
				Node victim = cloneNodeMap.get(primitive);
				cCluster.add(victim);
				clone.nodeAliasMap[victim.getIndex()] = cCluster;
			}
		}
		// restore attributes
		if (attributes != null) {
			clone.attributes = new HashMap<String, String>(attributes);
		} else {
			clone.attributes = null;
		}
		assert (this.equals(clone));
		return clone;
	}

	public boolean equals(Object o) {
		if (o instanceof MutableFuzzyGraph) {
			MutableFuzzyGraph other = (MutableFuzzyGraph) o;
			if (getNumberOfInitialNodes() != other.getNumberOfInitialNodes()) {
				return false;
			}
			for (int i = 0; i < events.size(); i++) {
				if (events.get(i).equals(other.getLogEvents().get(i)) == false) {
					return false;
				}
				if ((getNodeMappedTo(i) == null) != (other.getNodeMappedTo(i) != null)) {
					return false;
				}
			}
			for (int x = 0; x < numberOfInitialNodes; x++) {
				for (int y = 0; y < numberOfInitialNodes; y++) {
					if (getBinarySignificance(x, y) != other
							.getBinarySignificance(x, y)) {
						return false;
					}
					if (getBinaryCorrelation(x, y) != other
							.getBinaryCorrelation(x, y)) {
						return false;
					}
				}
			}
			List<ClusterNode> myClusters = getClusterNodes();
			List<ClusterNode> otherClusters = other.getClusterNodes();
			if (myClusters.size() != otherClusters.size()) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.FuzzyGraph#getAttribute(java
	 * .lang.String)
	 */
	public String getAttribute(String key) {
		if (attributes != null) {
			return attributes.get(key);
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.FuzzyGraph#getAttributeKeys()
	 */
	public Collection<String> getAttributeKeys() {
		if (attributes != null) {
			return attributes.keySet();
		} else {
			return new HashSet<String>();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.FuzzyGraph#resetAttributes()
	 */
	public void resetAttributes() {
		attributes = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.FuzzyGraph#setAttribute(java
	 * .lang.String, java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		if (attributes == null) {
			attributes = new HashMap<String, String>();
		}
		attributes.put(key, value);
	}

}
