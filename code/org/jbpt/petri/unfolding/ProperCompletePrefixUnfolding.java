package org.jbpt.petri.unfolding;

import org.jbpt.petri.*;

public class ProperCompletePrefixUnfolding
        extends AbstractProperCompletePrefixUnfolding<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> {

    public ProperCompletePrefixUnfolding() {
        super();
    }

    public ProperCompletePrefixUnfolding(
            INetSystem<Flow, Node, Place, Transition, Marking> sys,
            CompletePrefixUnfoldingSetup setup) {
        super(sys, setup);
    }

    public ProperCompletePrefixUnfolding(
            INetSystem<Flow, Node, Place, Transition, Marking> sys) {
        super(sys);
    }

}
