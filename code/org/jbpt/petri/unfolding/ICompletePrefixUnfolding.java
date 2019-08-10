package org.jbpt.petri.unfolding;

import org.jbpt.petri.*;

import java.util.List;
import java.util.Set;

/**
 * Interface to a complete prefix unfolding.
 *
 * @author Artem Polyvyanyy
 */
public interface ICompletePrefixUnfolding<BPN extends IBPNode<N>, C extends ICondition<BPN, C, E, F, N, P, T, M>, E extends IEvent<BPN, C, E, F, N, P, T, M>, F extends IFlow<N>, N extends INode, P extends IPlace, T extends ITransition, M extends IMarking<F, N, P, T>>
        extends IBranchingProcess<BPN, C, E, F, N, P, T, M> {
    /**
     * Get cutoff events of this complete prefix unfolding.
     *
     * @return Cutoff events of this complete prefix unfolding.
     */
    public Set<E> getCutoffEvents();

    /**
     * Check if a given event is a cutoff event in this complete prefix unfolding.
     *
     * @param event Event of this complete prefix unfolding.
     * @return <tt>true</tt> if 'event' is a cutoff event; otherwise <tt>false</tt>.
     */
    public boolean isCutoffEvent(E event);

    /**
     * Get a corresponding event of a given cutoff event.
     *
     * @param event A cutoff event of this complete prefix unfolding.
     * @return Corresponding event of 'event'; <tt>null</tt> if 'event' is not a cutoff event.
     */
    public E getCorrespondingEvent(E event);

    /**
     * Get total order of transitions used to construct this complete prefix unfolding.
     *
     * @return Ordered list of transitions of the originative system which was used to construct this complete prefix unfolding.
     */
    public List<T> getTotalOrderOfTransitions();

    public boolean isHealthyCutoffEvent(E event);

    public boolean isProper();
}
