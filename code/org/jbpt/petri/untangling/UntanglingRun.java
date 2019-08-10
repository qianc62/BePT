package org.jbpt.petri.untangling;

import org.jbpt.petri.*;

public class UntanglingRun extends AbstractUntanglingRun<Flow, Node, Place, Transition, Marking> {
    private static final long serialVersionUID = 2583533913166868367L;

    public UntanglingRun() {
        super();
    }

    public UntanglingRun(INetSystem<Flow, Node, Place, Transition, Marking> sys) {
        super(sys);
    }
}
