package org.processmining.framework.models.petrinet.pattern;

import org.processmining.framework.models.petrinet.PetriNet;

public class ExplicitChoiceComponent extends ChoiceComponent {

	public ExplicitChoiceComponent(PetriNet wfnet) {
		super(wfnet);
	}

	@Override
	public Component cloneComponent() {
		return new ExplicitChoiceComponent(getWfnet());
	}

	@Override
	public String toString() {
		return "Explicit choice";
	}

}
