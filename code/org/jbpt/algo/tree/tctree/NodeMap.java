package org.jbpt.algo.tree.tctree;

import org.jbpt.hypergraph.abs.IVertex;

import java.util.HashMap;


/**
 * This map is a convenient solution to store values for edges.
 *
 * @author Christian Wiggert
 */
public class NodeMap<V extends IVertex> extends HashMap<V, Object> {

    /**
     *
     */
    private static final long serialVersionUID = -474286340181229387L;

    public int getInt(V node) {
        return (Integer) this.get(node);
    }

    public void setInt(V node, int i) {
        this.put(node, i);
    }

    public boolean getBool(V node) {
        return (Boolean) this.get(node);
    }

    public void setBool(V node, boolean flag) {
        this.put(node, flag);
    }
}
