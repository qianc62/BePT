package org.jbpt.petri.unfolding.order;

import org.jbpt.petri.*;
import org.jbpt.petri.unfolding.IBPNode;
import org.jbpt.petri.unfolding.ICondition;
import org.jbpt.petri.unfolding.IEvent;


/**
 * Adequate order (abstract class).
 *
 * @author Artem Polyvyanyy
 */
public abstract class AdequateOrder<BPN extends IBPNode<N>, C extends ICondition<BPN, C, E, F, N, P, T, M>, E extends IEvent<BPN, C, E, F, N, P, T, M>, F extends IFlow<N>, N extends INode, P extends IPlace, T extends ITransition, M extends IMarking<F, N, P, T>>
        implements IAdequateOrder<BPN, C, E, F, N, P, T, M> {

    public boolean isTotal() {
        return false;
    }
}
