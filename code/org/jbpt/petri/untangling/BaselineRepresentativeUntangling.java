package org.jbpt.petri.untangling;

import org.jbpt.petri.*;
import org.jbpt.petri.unfolding.BPNode;
import org.jbpt.petri.unfolding.Condition;
import org.jbpt.petri.unfolding.Event;

/**
 * An implementation of the baseline algorithm for computing representative untanglings of net systems ({@link INetSystem}).
 *
 * @author Artem Polyvyanyy
 */
public class BaselineRepresentativeUntangling extends
        AbstractBaselineRepresentativeUntangling<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> {

    public BaselineRepresentativeUntangling(INetSystem<Flow, Node, Place, Transition, Marking> sys, UntanglingSetup setup) {
        super(sys, setup);
    }

    public BaselineRepresentativeUntangling(INetSystem<Flow, Node, Place, Transition, Marking> sys) {
        super(sys);
    }

}
