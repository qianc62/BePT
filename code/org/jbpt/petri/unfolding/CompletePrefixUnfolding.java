package org.jbpt.petri.unfolding;

import org.jbpt.petri.*;

/**
 * An implementation of a complete prefix unfloding of a net system.<br/><br/>
 *
 * @author Artem Polyvyanyy
 * @see {@link AbstractCompletePrefixUnfolding} for details.
 */
public class CompletePrefixUnfolding extends
        AbstractCompletePrefixUnfolding<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> {

    public CompletePrefixUnfolding(INetSystem<Flow, Node, Place, Transition, Marking> sys) {
        super(sys);
    }

    public CompletePrefixUnfolding(INetSystem<Flow, Node, Place, Transition, Marking> sys, CompletePrefixUnfoldingSetup setup) {
        super(sys, setup);
    }

}
