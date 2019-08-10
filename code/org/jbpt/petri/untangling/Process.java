package org.jbpt.petri.untangling;

import org.jbpt.petri.*;
import org.jbpt.petri.unfolding.BPNode;
import org.jbpt.petri.unfolding.Condition;
import org.jbpt.petri.unfolding.Event;

/**
 * An implementation of the {@link IProcess} interface.
 *
 * @author Artem Polyvyanyy
 */
public class Process extends AbstractProcess<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> {

    public Process() {
        super();
    }

    /**
     * Construct a process of a net system.
     *
     * @param sys A net system.
     */
    public Process(INetSystem<Flow, Node, Place, Transition, Marking> sys) {
        super(sys);
    }
}
