package org.processmining.framework.models.petrinet.pattern;

import java.util.Map;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;

public class UnreduciblePetriNetException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7603685831224446780L;
	private final PetriNetReducer<? extends Object> reducer;
	private final PetriNet clone;
	private final Map<Transition, ? extends Object> annotations;

	public UnreduciblePetriNetException(
			PetriNetReducer<? extends Object> reducer, PetriNet clone,
			Map<Transition, ? extends Object> annotations) {
		this.reducer = reducer;
		this.clone = clone;
		this.annotations = annotations;
	}

}
