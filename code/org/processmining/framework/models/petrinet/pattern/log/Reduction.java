package org.processmining.framework.models.petrinet.pattern.log;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.pattern.Component;

public class Reduction {

	public final PetriNet petriNet;
	public final Component component;
	public final double smResult;
	public final double unstructuredSmResult;
	public final int cardosoMetric;
	public final int cyclomaticMetric;

	public Reduction(PetriNet pn, Component component, double smResult,
			double unstructuredSmResult, int cardosoMetric, int cyclomaticMetric) {
		super();
		this.petriNet = pn;
		this.component = component;
		this.smResult = smResult;
		this.unstructuredSmResult = unstructuredSmResult;
		this.cardosoMetric = cardosoMetric;
		this.cyclomaticMetric = cyclomaticMetric;
	}

}
