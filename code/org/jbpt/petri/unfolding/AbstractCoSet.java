package org.jbpt.petri.unfolding;

import org.jbpt.petri.*;

import java.util.*;


public class AbstractCoSet<BPN extends IBPNode<N>, C extends ICondition<BPN, C, E, F, N, P, T, M>, E extends IEvent<BPN, C, E, F, N, P, T, M>, F extends IFlow<N>, N extends INode, P extends IPlace, T extends ITransition, M extends IMarking<F, N, P, T>>
        extends HashSet<C>
        implements ICoSet<BPN, C, E, F, N, P, T, M> {

    private static final long serialVersionUID = 1L;
    private Map<P, Set<C>> p2cs = new HashMap<P, Set<C>>();

    public AbstractCoSet() {
    }

    @Override
    public int hashCode() {
        int code = 0;
        for (C c : this) {
            code += c.hashCode();
        }

        return code;
    }

    @Override
    public boolean add(C c) {
        if (this.p2cs.get(c.getPlace()) == null) {
            Set<C> cs = new HashSet<C>();
            cs.add(c);
            this.p2cs.put(c.getPlace(), cs);
        } else
            this.p2cs.get(c.getPlace()).add(c);

        return super.add(c);
    }

    @Override
    public boolean addAll(Collection<? extends C> cs) {
        boolean result = false;
        for (C c : cs) result |= this.add(c);
        return result;
    }

    @Override
    public boolean remove(Object c) {
        return super.remove(c);
    }

    @Override
    public boolean removeAll(Collection<?> cs) {
        return super.removeAll(cs);
    }

    @Override
    public Set<C> getConditions(P place) {
        return this.p2cs.get(place);
    }

    @Override
    public Collection<P> getPlaces() {
        Collection<P> result = new ArrayList<P>();

        for (C c : this)
            result.add(c.getPlace());

        return result;
    }
}
