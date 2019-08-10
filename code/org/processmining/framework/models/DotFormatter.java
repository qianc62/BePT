package org.processmining.framework.models;

import java.util.Map;

public interface DotFormatter {
	/**
	 * Returns the global DOT attributes which should be written to a dot file.
	 * For example:
	 * <code>return "fontname=\"Arial\"; rankdir=\"TB\"; edge [arrowsize=\"0.7\",fontname=\"Arial\",fontsize=\"8\"];\n";</code>
	 * 
	 * @param graph
	 *            The graph being written to DOT
	 * @return The string with DOT attributes for the whole graph
	 */
	Map<String, String> dotFormatHeader(ModelGraph graph);

	/**
	 * Returns the DOT attributes for a specific vertex (node) in the graph. If
	 * label is null, then this method can decide which label to give the DOT
	 * node (for example the vertex identifier). If the label is not null, then
	 * this method should use the given label. For example:
	 * <code>return "shape=\"box\",label=\"" + (label == null ? vertex.getIdentifier() : label) + "\""; </code>
	 * 
	 * @param graph
	 *            The graph being written to DOT
	 * @param vertex
	 *            The vertex begin written to DOT
	 * @param label
	 *            If null, the label of the DOT node can be chosen by this
	 *            method. If not null, the label of the DOT node should be this
	 *            label.
	 * @return The string with DOT attributes for this node
	 */
	Map<String, String> dotFormatVertex(ModelGraph graph,
			ModelGraphVertex vertex);

	/**
	 * Returns the DOT attributes for a specific edge in the graph. If label is
	 * null, then this method can decide which label to give the DOT edge. If
	 * the label is not null, then this method should use the given label. For
	 * example:
	 * <code>return "label=\"" + (label == null ? "" : label) + "\""; </code>
	 * 
	 * @param graph
	 *            The graph being written to DOT
	 * @param edge
	 *            The edge begin written to DOT
	 * @param label
	 *            If null, the label of the DOT edge can be chosen by this
	 *            method. If not null, the label of the DOT edge should be this
	 *            label.
	 * @return The string with DOT attributes for this edge
	 */
	Map<String, String> dotFormatEdge(ModelGraph graph, ModelGraphEdge edge);
}
