package org.processmining.framework.models.petrinet.pattern;

import org.processmining.framework.models.petrinet.PetriNet;

public class StateMachineComponent extends Component {

	public StateMachineComponent(PetriNet wfnet) {
		super(wfnet);
	}

	@Override
	public Component cloneComponent() {
		return new StateMachineComponent(getWfnet());
	}

	@Override
	public String toString() {
		return "State machine";
	}

}
