package org.jbpt.automaton;

import org.jbpt.petri.*;

/**
 * @author Artem Polyvyanyy
 */
public class Automaton extends AbstractAutomaton<StateTransition, State, Flow, Node, Place, Transition, Marking> {

    public Automaton() {
        super();
    }

    public Automaton(INetSystem<Flow, Node, Place, Transition, Marking> sys, int maxSize) {
        super(sys, maxSize);
    }

    public Automaton(INetSystem<Flow, Node, Place, Transition, Marking> sys) {
        super(sys);
    }

}
