package org.jbpt.petri.unfolding;

import org.jbpt.petri.*;

public class Condition extends AbstractCondition<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> {

    protected Condition() {
    }

    public Condition(Place place, Event event) {
        super(place, event);
    }

}
