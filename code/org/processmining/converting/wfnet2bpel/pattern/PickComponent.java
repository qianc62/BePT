package org.processmining.converting.wfnet2bpel.pattern;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.pattern.ChoiceComponent;
import org.processmining.framework.models.petrinet.pattern.Component;

public class PickComponent extends ChoiceComponent {

	public PickComponent(PetriNet wfnet) {
		super(wfnet);
	}

	/**
	 * @see org.processmining.framework.models.petrinet.pattern.Component#toString()
	 */
	@Override
	public String toString() {
		return "Pick";
	}

	/**
	 * @see org.processmining.framework.models.petrinet.pattern.ChoiceComponent#cloneComponent()
	 */
	@Override
	public Component cloneComponent() {
		return new PickComponent(getWfnet());
	}

}
