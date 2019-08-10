package org.jbpt.algo.tree.tctree;

import org.jbpt.graph.abs.IEdge;
import org.jbpt.hypergraph.abs.IVertex;
import org.jbpt.hypergraph.abs.Vertex;

/**
 * Implementation of the node of the tree of the triconnected components.
 *
 * @param <E> Edge template.
 * @param <V> Vertex template.
 * @author Artem Polyvyanyy
 */
public class TCTreeNode<E extends IEdge<V>, V extends IVertex> extends Vertex {
    // node type
    protected TCType type = TCType.UNDEFINED;
    // skeleton
    protected TCSkeleton<E, V> skeleton = new TCSkeleton<E, V>();
    // boundary vertices of the fragment

    public TCType getType() {
        return this.type;
    }

    public TCSkeleton<E, V> getSkeleton() {
        return this.skeleton;
    }

    @Override
    public String toString() {
        return this.getName() + " - skeleton: " + this.skeleton + " virtual: " + this.skeleton.virtualEdges;
    }
}
