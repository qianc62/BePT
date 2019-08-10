package org.processmining.framework.models.petrinet.pattern;

import org.processmining.framework.models.petrinet.PetriNet;

public class SequenceComponent extends Component {

	public SequenceComponent(PetriNet wfnet) {
		super(wfnet);
	}

	@Override
	public Component cloneComponent() {
		return new SequenceComponent(getWfnet());
	}

	@Override
	public String toString() {
		return "Sequence";
	}

}
