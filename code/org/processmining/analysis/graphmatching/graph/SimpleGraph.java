package org.processmining.analysis.graphmatching.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;

/**
 * Efficient implementation of a simple graph: (Vertices, Edges, labels) Only
 * for reading, cannot be modified
 */
public class SimpleGraph {
	private Set<Integer> vertices;
	private Map<Integer, Set<Integer>> outgoingEdges;
	private Map<Integer, Set<Integer>> incomingEdges;
	private Map<Integer, String> labels;

	private Set<Integer> functionVertices;
	private Set<Integer> eventVertices;
	private Set<Integer> connectorVertices;

	public SimpleGraph(Set<Integer> vertices,
			Map<Integer, Set<Integer>> outgoingEdges,
			Map<Integer, Set<Integer>> incomingEdges,
			Map<Integer, String> labels, Set<Integer> functionVertices,
			Set<Integer> eventVertices, Set<Integer> connectorVertices) {
		this.vertices = vertices;
		this.outgoingEdges = outgoingEdges;
		this.incomingEdges = incomingEdges;
		this.labels = labels;

		this.functionVertices = functionVertices;
		this.eventVertices = eventVertices;
		this.connectorVertices = connectorVertices;
	}

	/**
	 * Initializes a simple graph from an EPC.
	 * 
	 */
	public SimpleGraph(ConfigurableEPC epc) {
		Map<ModelGraphVertex, Integer> nodeId2vertex = new HashMap<ModelGraphVertex, Integer>();
		Map<Integer, ModelGraphVertex> vertex2nodeId = new HashMap<Integer, ModelGraphVertex>();

		vertices = new HashSet<Integer>();
		outgoingEdges = new HashMap<Integer, Set<Integer>>();
		incomingEdges = new HashMap<Integer, Set<Integer>>();
		labels = new HashMap<Integer, String>();

		functionVertices = new HashSet<Integer>();
		eventVertices = new HashSet<Integer>();
		connectorVertices = new HashSet<Integer>();

		int vertexId = 0;
		for (ModelGraphVertex n : epc.getVerticeList()) {
			vertices.add(vertexId);
			labels.put(vertexId, n.getIdentifier());
			nodeId2vertex.put(n, vertexId);
			vertex2nodeId.put(vertexId, n);
			if (n instanceof EPCFunction) {
				functionVertices.add(vertexId);
			} else if (n instanceof EPCEvent) {
				eventVertices.add(vertexId);
			} else if (n instanceof EPCConnector) {
				connectorVertices.add(vertexId);
			}
			vertexId++;
		}

		for (Integer v = 0; v < vertexId; v++) {
			ModelGraphVertex n = vertex2nodeId.get(v);

			Set<Integer> incomingCurrent = new HashSet<Integer>();
			for (Object s : n.getPredecessors()) {
				incomingCurrent.add(nodeId2vertex.get((ModelGraphVertex) s));
			}
			incomingEdges.put(v, incomingCurrent);

			Set<Integer> outgoingCurrent = new HashSet<Integer>();
			for (Object s : n.getSuccessors()) {
				outgoingCurrent.add(nodeId2vertex.get((ModelGraphVertex) s));
			}
			outgoingEdges.put(v, outgoingCurrent);
		}
	}

	public Set<Integer> getVertices() {
		return vertices;
	}

	public Set<TwoVertices> getEdges() {
		Set<TwoVertices> result = new HashSet<TwoVertices>();
		for (Integer src : vertices) {
			for (Integer tgt : outgoingEdges.get(src)) {
				result.add(new TwoVertices(src, tgt));
			}
		}
		return result;
	}

	public Set<Integer> getFunctionVertices() {
		return functionVertices;
	}

	public Set<Integer> getEventVertices() {
		return eventVertices;
	}

	public Set<Integer> getConnectorVertices() {
		return connectorVertices;
	}

	public Set<Integer> postSet(int vertex) {
		return outgoingEdges.get(vertex);
	}

	public Set<Integer> preSet(int vertex) {
		return incomingEdges.get(vertex);
	}

	public String getLabel(int vertex) {
		return labels.get(vertex);
	}

	public Set<String> getLabels(Set<Integer> nodes) {
		Set<String> result = new HashSet<String>();

		for (Integer node : nodes) {
			result.add(getLabel(node));
		}

		return result;
	}

	public Integer getVertex(String label) {
		for (Integer v : vertices) {
			if (labels.get(v).equals(label)) {
				return v;
			}
		}
		return Integer.MAX_VALUE;
	}

	/**
	 * @return vertices that do not have an incoming edge.
	 */
	public Set<Integer> sourceVertices() {
		Set<Integer> result = new HashSet<Integer>();
		for (Integer i : vertices) {
			if (incomingEdges.get(i).isEmpty()) {
				result.add(i);
			}
		}
		return result;
	}

	/**
	 * @return vertices that do not have an outgoing edge.
	 */
	public Set<Integer> sinkVertices() {
		Set<Integer> result = new HashSet<Integer>();
		for (Integer i : vertices) {
			if (outgoingEdges.get(i).isEmpty()) {
				result.add(i);
			}
		}
		return result;
	}

	public String toString() {
		String result = "";
		for (Integer i : vertices) {
			result += i + "(" + labels.get(i) + ") {";
			for (Iterator<Integer> j = incomingEdges.get(i).iterator(); j
					.hasNext();) {
				int vertex = j.next();
				result += vertex;// + "(" + labels.get(vertex) + ")";
				result += j.hasNext() ? "," : "";
			}
			result += "} {";
			for (Iterator<Integer> j = outgoingEdges.get(i).iterator(); j
					.hasNext();) {
				int vertex = j.next();
				result += vertex;// + "(" + labels.get(vertex) + ")";
				result += j.hasNext() ? "," : "";
			}
			result += "}\n";
		}
		return result;
	}

	/**
	 * @param vertex
	 *            Vertex to determine the postSet for
	 * @param silent
	 *            Set of vertices that should not be considered
	 * @return the postSet(vertex), in which all v \in silent are (recursively)
	 *         replaced by their postSet(v)
	 */
	public Set<Integer> nonSilentPostSet(Integer vertex, Set<Integer> silent) {
		return nonSilentPostSetHelper(vertex, silent, new HashSet<Integer>());
	}

	private Set<Integer> nonSilentPostSetHelper(Integer vertex,
			Set<Integer> silent, Set<Integer> visited) {
		Set<Integer> result = new HashSet<Integer>();
		Set<Integer> visitedP = new HashSet<Integer>(visited);
		visitedP.add(vertex);

		for (Integer post : postSet(vertex)) {
			if (!visited.contains(post)) {
				if (silent.contains(post)) {
					result
							.addAll(nonSilentPostSetHelper(post, silent,
									visitedP));
				} else {
					result.add(post);
				}
			}
		}
		return result;
	}

	/**
	 * @param vertex
	 *            Vertex to determine the preSet for
	 * @param silent
	 *            Set of vertices that should not be considered
	 * @return the preSet(vertex), in which all v \in silent are (recursively)
	 *         replaced by their preSet(v)
	 */
	public Set<Integer> nonSilentPreSet(Integer vertex, Set<Integer> silent) {
		return nonSilentPreSetHelper(vertex, silent, new HashSet<Integer>());
	}

	private Set<Integer> nonSilentPreSetHelper(Integer vertex,
			Set<Integer> silent, Set<Integer> visited) {
		Set<Integer> result = new HashSet<Integer>();
		Set<Integer> visitedP = new HashSet<Integer>(visited);
		visitedP.add(vertex);

		for (Integer pre : preSet(vertex)) {
			if (!visited.contains(pre)) {
				if (silent.contains(pre)) {
					result.addAll(nonSilentPreSetHelper(pre, silent, visitedP));
				} else {
					result.add(pre);
				}
			}
		}
		return result;
	}

	/**
	 * Returns A COPY OF the graph, such that all vertices from the given set
	 * are removed. All paths (v1,v),(v,v2) via a vertex v from that set are
	 * replaced by direct arcs (v1,v2).
	 * 
	 * Formally: for G = (V, E, l) return (V-vertices, E', l-(vertices x
	 * labels)), where E' = E - ((V x vertices) U (vertices X V)) U {(v1, v2)|v
	 * \in vertices, (v1,v) \in E \land (v,v2) \in E}
	 */
	public SimpleGraph removeVertices(Set<Integer> toRemove) {
		Set<Integer> newVertices = new HashSet<Integer>(vertices);
		newVertices.removeAll(toRemove);

		Map<Integer, Set<Integer>> newOutgoingEdges = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Set<Integer>> newIncomingEdges = new HashMap<Integer, Set<Integer>>();
		;
		Map<Integer, String> newLabels = new HashMap<Integer, String>();

		for (Integer newVertex : newVertices) {
			newOutgoingEdges.put(newVertex, nonSilentPostSet(newVertex,
					toRemove));
			newIncomingEdges.put(newVertex,
					nonSilentPreSet(newVertex, toRemove));
			newLabels.put(newVertex, labels.get(newVertex));
		}

		Set<Integer> newFunctionVertices = new HashSet<Integer>(
				functionVertices);
		newFunctionVertices.removeAll(toRemove);
		Set<Integer> newEventVertices = new HashSet<Integer>(eventVertices);
		newEventVertices.removeAll(toRemove);
		Set<Integer> newConnectorVertices = new HashSet<Integer>(
				connectorVertices);
		newConnectorVertices.removeAll(toRemove);

		return new SimpleGraph(newVertices, newOutgoingEdges, newIncomingEdges,
				newLabels, newFunctionVertices, newEventVertices,
				newConnectorVertices);
	}
}
