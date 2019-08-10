package org.processmining.framework.models.petrinet.pattern;

import org.processmining.framework.models.petrinet.PetriNet;

public class MarkedGraphComponent extends Component {

	public MarkedGraphComponent(PetriNet wfnet) {
		super(wfnet);
	}

	@Override
	public Component cloneComponent() {
		return new MarkedGraphComponent(getWfnet());
	}

	@Override
	public String toString() {
		return "Marked graph";
	}

}
