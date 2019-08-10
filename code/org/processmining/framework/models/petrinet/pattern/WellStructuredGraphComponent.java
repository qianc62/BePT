package org.processmining.framework.models.petrinet.pattern;

import org.processmining.framework.models.petrinet.PetriNet;

public class WellStructuredGraphComponent extends Component {

	public WellStructuredGraphComponent(PetriNet wfnet) {
		super(wfnet);
	}

	@Override
	public Component cloneComponent() {
		return new WellStructuredGraphComponent(getWfnet());
	}

	@Override
	public String toString() {
		return "Well-structured";
	}

}
