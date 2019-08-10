package org.jbpt.petri.unfolding;

import org.jbpt.petri.*;

import java.util.HashSet;
import java.util.Set;


public abstract class AbstractCondition<BPN extends IBPNode<N>, C extends ICondition<BPN, C, E, F, N, P, T, M>, E extends IEvent<BPN, C, E, F, N, P, T, M>, F extends IFlow<N>, N extends INode, P extends IPlace, T extends ITransition, M extends IMarking<F, N, P, T>>
        extends AbstractBPNode<N>
        implements ICondition<BPN, C, E, F, N, P, T, M> {
    public static int count = 0;

    P s = null;
    E e = null;
    Set<E> postE = new HashSet<E>();

    // used to mark if this a place after a cut-off event
    private boolean cutoffPost = false;
    // if this is cutoffPost, store all the mapping conditions
    // mappingConditions in all the mapping conditions are linked to each other,
    // which is to say, all change when one of them changes
    private Set<C> mappingConditions = null;
    // only the condition with successors is correspondingCondition
    private C correspondingCondition = null;

    protected AbstractCondition() {
        this.ID = ++AbstractCondition.count;
    }

    public AbstractCondition(P place, E event) {
        this.ID = ++AbstractCondition.count;

        this.s = place;
        this.e = event;
    }

    @Override
    public P getPlace() {
        return this.s;
    }

    @Override
    public void setPlace(P place) {
        this.s = place;
    }

    @Override
    public E getPreEvent() {
        return this.e;
    }

    @Override
    public void setPreEvent(E event) {
        this.e = event;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public String getName() {
        return String.format("%s-%s", this.s.getName(), this.ID);
    }

    @Override
    public boolean equals(Object that) {
        if (that == null || !(that instanceof ICondition)) return false;
        if (this == that) return true;

        @SuppressWarnings("unchecked")
        C thatC = (C) that;
        if (this.getPlace().equals(thatC.getPlace())) {
            if (this.getPreEvent() == null) {
                if (thatC.getPreEvent() == null) return true;
                return false;
            } else {
                if (this.getPreEvent().equals(thatC.getPreEvent())) return true;
                return false;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode += this.getPlace() == null ? 0 : 7 * this.getPlace().hashCode();
        hashCode += this.getPreEvent() == null ? 0 : 11 * this.getPreEvent().getTransition().hashCode();

        return hashCode;
    }

    @SuppressWarnings("unchecked")
    @Override
    public N getPetriNetNode() {
        return (N) this.s;
    }

    @Override
    public boolean isEvent() {
        return false;
    }

    @Override
    public boolean isCondition() {
        return true;
    }

    @Override
    public Set<E> getPostE() {
        return postE;
    }

    @Override
    public void setPostE(Set<E> postE) {
        this.postE = postE;
    }

    public boolean isCutoffPost() {
        return cutoffPost;
    }

    public void setCutoffPost(boolean cutoffPost) {
        this.cutoffPost = cutoffPost;
    }

    public Set<C> getMappingConditions() {
        return mappingConditions;
    }

    public void setMappingConditions(Set<C> mappingConditions) {
        this.mappingConditions = mappingConditions;
    }

    public C getCorrespondingCondition() {
        return correspondingCondition;
    }

    public void setCorrespondingCondition(C correspondingCondition) {
        this.correspondingCondition = correspondingCondition;
    }
}
