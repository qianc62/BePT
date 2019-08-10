package org.jbpt.graph.abs;

import org.jbpt.hypergraph.abs.IVertex;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Directed graph implementation
 *
 * @param <E> template for edge (extends IDirectedEdge)
 * @param <V> template for vertex (extends IVertex)
 * @author Artem Polyvyanyy
 */
public class AbstractDirectedGraph<E extends IDirectedEdge<V>, V extends IVertex>
        extends AbstractMultiDirectedGraph<E, V> {

    /*
     * (non-Javadoc)
     * @see de.hpi.bpt.hypergraph.abs.AbstractMultiDirectedHyperGraph#addEdge(de.hpi.bpt.hypergraph.abs.IVertex, de.hpi.bpt.hypergraph.abs.IVertex)
     */
    @SuppressWarnings("unchecked")
    @Override
    public E addEdge(V s, V t) {
        Collection<V> ss = new ArrayList<V>();
        ss.add(s);
        Collection<V> ts = new ArrayList<V>();
        ts.add(t);
        if (!this.checkEdge(ss, ts)) return null;

        return (E) (new AbstractDirectedEdge<V>(this, s, t));
    }

    @Override
    public boolean isMultiGraph() {
        return false;
    }
}
