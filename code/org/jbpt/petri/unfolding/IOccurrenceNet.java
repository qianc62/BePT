package org.jbpt.petri.unfolding;

import org.jbpt.petri.*;

import java.util.Collection;
import java.util.Set;

public interface IOccurrenceNet<BPN extends IBPNode<N>, C extends ICondition<BPN, C, E, F, N, P, T, M>, E extends IEvent<BPN, C, E, F, N, P, T, M>, F extends IFlow<N>, N extends INode, P extends IPlace, T extends ITransition, M extends IMarking<F, N, P, T>>
        extends IPetriNet<F, N, P, T> {

    public ICompletePrefixUnfolding<BPN, C, E, F, N, P, T, M> getCompletePrefixUnfolding();

    public void setCompletePrefixUnfolding(ICompletePrefixUnfolding<BPN, C, E, F, N, P, T, M> cpu);

    public E getEvent(T t);

    public C getCondition(P p);

    public P getPlace(C c);

    public Collection<P> getPlaces(Collection<C> conditions);

    public T getTransition(E e);

    public Collection<T> getTransitions(Collection<E> events);

    public BPN getUnfoldingNode(N n);

    public OrderingRelationType getOrderingRelation(N n1, N n2);

    public Set<T> getCutoffEvents();

    public T getCorrespondingEvent(T t);

    public boolean isCutoffEvent(T t);

    public Set<P> getCutInducedByLocalConfiguration(T t);

    public void setBranchingProcess(IBranchingProcess<BPN, C, E, F, N, P, T, M> bp);

    public String toDOT(Collection<P> places, Collection<T> transitions);

}