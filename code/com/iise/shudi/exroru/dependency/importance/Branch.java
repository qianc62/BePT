package com.iise.shudi.exroru.dependency.importance;

import org.apache.commons.math3.linear.RealVector;
import org.jbpt.petri.unfolding.IBPNode;

@SuppressWarnings("rawtypes")
public class Branch {

    private IBPNode cur;
    private RealVector inEdges;
    private boolean waiting;
    private boolean merged;

    public Branch(IBPNode c, RealVector v, boolean w, boolean m) {
        cur = c;
        inEdges = v;
        waiting = w;
        merged = m;
    }

    @Override
    public String toString() {
        return cur.getLabel() + "," + inEdges;
    }

    public IBPNode getCur() {
        return cur;
    }

    public RealVector getInEdges() {
        return inEdges;
    }

    public void setInEdges(RealVector inEdges) {
        this.inEdges = inEdges;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    public boolean isMerged() {
        return merged;
    }
}
