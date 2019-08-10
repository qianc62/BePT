package org.processmining.converting.wfnet2bpel.pattern;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.pattern.WellStructuredGraphComponent;
import org.processmining.framework.models.petrinet.pattern.Component;

public class FlowComponent extends WellStructuredGraphComponent {

	public FlowComponent(PetriNet wfnet) {
		super(wfnet);
	}

	/**
	 * @see org.processmining.framework.models.petrinet.pattern.Component#toString()
	 */
	@Override
	public String toString() {
		return "Flow";
	}

	/**
	 * @see org.processmining.framework.models.petrinet.pattern.WellStructuredGraphComponent#cloneComponent()
	 */
	@Override
	public Component cloneComponent() {
		return new FlowComponent(getWfnet());
	}

}
