package org.processmining.framework.models.petrinet.pattern;

import org.processmining.framework.models.petrinet.Transition;

public interface PetriNetReducer<A extends Object> {

	public A createAnnotation(Transition t);

}
