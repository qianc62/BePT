package org.jbpt.automaton;

import org.jbpt.petri.*;

/**
 * @author Artem Polyvyanyy
 */
public class State extends AbstractState<Flow, Node, Place, Transition, Marking> {

    public State() {
    }

    public State(Marking marking) {
        super(marking);
    }
}
