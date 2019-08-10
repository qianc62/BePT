package org.jbpt.algo.tree.tctree;

import org.jbpt.graph.abs.IEdge;
import org.jbpt.hypergraph.abs.IVertex;

import java.util.LinkedList;


/**
 * This EdgeList is an abstraction of the underlying list type, which stores edges.
 *
 * @param <E> Edge class
 * @param <V> Vertex class
 * @author Christian Wiggert
 * @author Artem Polyvyanyy
 */
public class EdgeList<E extends IEdge<V>, V extends IVertex> extends LinkedList<E> {

    private static final long serialVersionUID = 2649534465829537370L;

    public EdgeList(EdgeList<E, V> list) {
        super(list);
    }

    public EdgeList(E edge) {
        super();
        this.add(edge);
    }

    public EdgeList() {
        super();
    }

    @Override
    public EdgeList<E, V> clone() {
        return new EdgeList<E, V>(this);
    }
}
