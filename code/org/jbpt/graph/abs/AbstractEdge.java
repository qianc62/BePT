package org.jbpt.graph.abs;

import org.jbpt.hypergraph.abs.AbstractHyperEdge;
import org.jbpt.hypergraph.abs.IDirectedHyperEdge;
import org.jbpt.hypergraph.abs.IVertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * Abstract edge implementation
 * Edge connects two vertices
 *
 * @param <V> Vertex type employed in the edge
 * @author Artem Polyvyanyy
 */
public class AbstractEdge<V extends IVertex> extends AbstractHyperEdge<V> implements IEdge<V> {

    protected V v1;
    protected V v2;

    private AbstractMultiGraph<?, V> graph = null;

    protected AbstractEdge(AbstractMultiGraph<?, V> g, V v1, V v2) {
        super(g);
        this.graph = g;
        setVertices(v1, v2);
    }

    /*
     * (non-Javadoc)
     * @see de.hpi.bpt.graph.abs.IEdge#getOtherVertex(de.hpi.bpt.hypergraph.abs.IVertex)
     */
    public V getOtherVertex(V v) {
        if (v.equals(this.v1)) return this.v2;
        if (v.equals(this.v2)) return this.v1;

        return null;
    }

    /*
     * (non-Javadoc)
     * @see de.hpi.bpt.graph.abs.IEdge#isSelfLoop()
     */
    public boolean isSelfLoop() {
        if (v1.equals(v2) && v1 != null && v2 != null) return true;
        else return false;
    }

    @Override
    public void setVertices(V v1, V v2) {
        if (this.graph == null) return;
        if (v1 == null || v2 == null) return;

        if (!this.graph.isMultiGraph()) {
            Collection<V> vs = new ArrayList<V>();
            vs.add(v1);
            vs.add(v2);

            @SuppressWarnings("unchecked")
            Collection<IDirectedHyperEdge<V>> es = (Collection<IDirectedHyperEdge<V>>) this.graph.getEdges(vs);
            if (es.size() > 0) {
                Iterator<IDirectedHyperEdge<V>> i = es.iterator();
                while (i.hasNext()) {
                    IDirectedHyperEdge<V> e = i.next();
                    if (e.getVertices().size() == 2)
                        return;
                }
            }
        }

        super.removeVertex(v1);
        super.removeVertex(v2);

        Collection<V> vs = new ArrayList<V>();
        vs.add(v1);
        vs.add(v2);
        super.addVertices(vs);
        this.v1 = v1;
        this.v2 = v2;
    }

	/*
     * (non-Javadoc)
	 * @see de.hpi.bpt.hypergraph.abs.AbstractHyperEdge#equals(java.lang.Object)
	 */
	/*@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof AbstractEdge)) return false;
		AbstractEdge<V> e = (AbstractEdge<V>) obj;
		
		return this.compareMultiSets(this.getVertices(), e.getVertices());
	}*/

    /*
     * (non-Javadoc)
     * @see de.hpi.bpt.hypergraph.abs.AbstractHyperEdge#addVertex(de.hpi.bpt.hypergraph.abs.IVertex)
     */
    @Override
    public V addVertex(V v) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see de.hpi.bpt.hypergraph.abs.AbstractHyperEdge#addVertices(java.util.Collection)
     */
    @Override
    public Collection<V> addVertices(Collection<V> vs) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see de.hpi.bpt.hypergraph.abs.AbstractHyperEdge#removeVertex(de.hpi.bpt.hypergraph.abs.IVertex)
     */
    @Override
    public V removeVertex(V v) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see de.hpi.bpt.hypergraph.abs.AbstractHyperEdge#removeVertices(java.util.Collection)
     */
    @Override
    public Collection<V> removeVertices(Collection<V> vs) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see de.hpi.bpt.hypergraph.abs.AbstractHyperEdge#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
        this.graph = null;
    }

    /*
     * (non-Javadoc)
     * @see de.hpi.bpt.graph.abs.IEdge#getV1()
     */
    public V getV1() {
        return this.v1;
    }

    /*
     * (non-Javadoc)
     * @see de.hpi.bpt.graph.abs.IEdge#getV2()
     */
    public V getV2() {
        return this.v2;
    }

    /*
     * (non-Javadoc)
     * @see de.hpi.bpt.graph.abs.IEdge#connectsVertices(de.hpi.bpt.hypergraph.abs.IVertex, de.hpi.bpt.hypergraph.abs.IVertex)
     */
    public boolean connectsVertices(V v1, V v2) {
        return this.connectsVertex(v1) && this.connectsVertex(v2);
    }

    @Override
    public String toString() {
        return String.format("%s-%s", this.v1, this.v2);
    }
}
