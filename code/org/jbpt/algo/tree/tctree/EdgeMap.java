package org.jbpt.algo.tree.tctree;

import org.jbpt.graph.abs.IEdge;
import org.jbpt.hypergraph.abs.IVertex;

import java.util.HashMap;


/**
 * This map is a convenient solution to store values for edges.
 *
 * @author Christian Wiggert
 */
public class EdgeMap<E extends IEdge<V>, V extends IVertex> extends HashMap<E, Object> {

    /**
     *
     */
    private static final long serialVersionUID = -3122883772335954023L;

    public int getInt(E edge) {
        return (Integer) this.get(edge);
    }

    public void setInt(E edge, int i) {
        this.put(edge, i);
    }

    public boolean getBool(E edge) {
        if (this.get(edge) == null)
            return false;
        return (Boolean) this.get(edge);
    }

    public void setBool(E edge, boolean flag) {
        this.put(edge, flag);
    }

    public void initialiseWithFalse() {
        for (E edge : this.keySet()) {
            this.put(edge, false);
        }
    }
}
