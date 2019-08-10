package org.processmining.analysis.petrinet.structuredness;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.pattern.Component;

public class UnstructuredComponent extends Component {

	public UnstructuredComponent(PetriNet wfnet) {
		super(wfnet);
	}

	@Override
	public Component cloneComponent() {
		return new UnstructuredComponent(getWfnet());
	}

	@Override
	public String toString() {
		return "Unstructured";
	}

}
