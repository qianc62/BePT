package org.processmining.analysis.petrinet.petrinetmetrics;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.Message;

public class Density implements ICalculator {
	private PetriNet net;
	private final static String TYPE = "Coupling";
	private final static String NAME = "Density";
	private float places;
	private float transitions;
	// private int connectors;
	private float arcs;

	public Density(PetriNet net) {
		super();
		this.net = net;
		this.places = this.net.getPlaces().size();
		this.transitions = this.net.getTransitions().size();
		this.arcs = this.net.getEdges().size();
	}

	public String Calculate() {
		float divider = this.arcs;
		float dividend = (this.places * this.transitions)
				+ (this.transitions * this.places);
		float result = divider / dividend;
		Message.add("\t<Density value=\"" + Float.toString(result) + "\"/>",
				Message.TEST);
		return Float.toString(result);
	}

	public String getName() {
		return this.NAME;
	}

	public String getType() {
		return this.TYPE;
	}

	public String VerifyBasicRequirements() {
		if (net.getEdges().size() < 1) {
			return "The Petri Net does not contain Arcs. It is not correctly designed";
		} else {
			if (net.getPlaces().size() < 1) {
				return "The Petri Net does not contain Places. It is not correctly designed";
			} else {
				if (net.getTransitions().size() == 0) {
					return "The Petri Net does not contain Transitions. It is not correctly designed";
				}
			}
		}
		return ".";
	}

}
