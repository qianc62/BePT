package org.jbpt.graph.abs;

import org.jbpt.hypergraph.abs.IDirectedHyperEdge;
import org.jbpt.hypergraph.abs.IVertex;

/**
 * Interface describing directed binary graph edge behavior (constrained by implementation)
 * Directed binary edge is an edge that connects exactly two vertices and makes a difference between source and target
 *
 * @param <V> Vertex type employed in the edge
 * @author Artem Polyvyanyy
 */
public interface IDirectedEdge<V extends IVertex> extends IDirectedHyperEdge<V>, IEdge<V> {
    /**
     * Get source vertex
     *
     * @return Source vertex
     */
    public V getSource();

    /**
     * Set source vertex
     *
     * @param v Source vertex
     * @return Vertex set as source, <code>null</code> upon failure
     */
    public V setSource(V v);

    /**
     * Get target vertex
     *
     * @return Target vertex
     */
    public V getTarget();

    /**
     * Set target vertex
     *
     * @param v Target vertex
     * @return Vertex set as target, <code>null</code> upon failure
     */
    public V setTarget(V v);

    /**
     * Set directed graph edge vertices.
     *
     * @param source Source vertex.
     * @param target Target vertex.
     */
    public void setVertices(V source, V target);
}
