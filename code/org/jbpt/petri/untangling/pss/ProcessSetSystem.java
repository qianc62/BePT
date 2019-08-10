package org.jbpt.petri.untangling.pss;

import org.jbpt.petri.*;
import org.jbpt.petri.unfolding.BPNode;
import org.jbpt.petri.unfolding.Condition;
import org.jbpt.petri.unfolding.Event;
import org.jbpt.petri.untangling.IProcess;

import java.util.Collection;

/**
 * An implementation of the {@link IProcessSetSystem} interface.
 *
 * @author Artem Polyvyanyy
 */
public class ProcessSetSystem extends AbstractProcessSetSystem<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> {

    public ProcessSetSystem(INetSystem<Flow, Node, Place, Transition, Marking> sys, Collection<IProcess<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking>> pis) {
        super(sys, pis);
    }

}
