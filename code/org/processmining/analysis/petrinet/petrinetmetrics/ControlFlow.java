package org.processmining.analysis.petrinet.petrinetmetrics;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.ui.Message;

public class ControlFlow implements ICalculator {
	private PetriNet net;
	private final static String TYPE = "Control-Flow";
	private final static String NAME = "Control-Flow";

	public ControlFlow(PetriNet net) {
		super();
		this.net = net;
	}

	public String Calculate() {

		Message.add("\t<CFC>", Message.TEST);
		int i = net.getPlaces().size();
		int result = 0;
		// XOR's
		for (int count = 0; count < i; count++) {
			Place place = (Place) net.getPlaces().get(count);
			if (isXOR(place)) {
				int temp = power(2, place.getSuccessors().size()) - 1;
				result = result + temp;
				Message.add("\t\t<XORConnector " + "name=\"" + place.toString()
						+ "\" " + "value=\"" + temp + "\"/>", Message.TEST);
			}
		}
		// AND's
		i = net.getTransitions().size();
		for (int count = 0; count < i; count++) {
			Transition transition = (Transition) net.getTransitions()
					.get(count);
			if (isAND(transition)) {
				result++;
				Message.add("\t\t<ANDConnector " + "name=\""
						+ transition.toString() + "\" " + "value=\"" + 1
						+ "\"/>", Message.TEST);
			}
		}
		String output = "" + result;
		Message.add("\t\t<TotalCFC value=\"" + output + "\">", Message.TEST);
		Message.add("\t</CFC>", Message.TEST);
		return output;
	}

	private int power(int base, int expoente) {
		int resultado = 1;
		for (int i = 0; i < expoente; i++) {
			resultado = resultado * base;
		}
		return resultado;
	}

	private boolean isAND(Transition transition) {
		if ((transition.getPredecessors().size() > 1)
				&& (transition.getSuccessors().size() > 1)) {
			return true;
		} else {
			if ((transition.getSuccessors().size() == 1)
					&& (transition.getPredecessors().size() > 1)) { // many-to-one
				// case
				return true;
			} else {
				if ((transition.getPredecessors().size() == 1)
						&& (transition.getSuccessors().size() > 1)) {// one-to-many
					// case
					return true;
				}
			}
		}
		return false;
	}

	private boolean isXOR(Place place) {
		if ((place.getPredecessors().size() > 1)
				&& (place.getSuccessors().size() > 1)) {
			return true;
		} else {
			if ((place.getSuccessors().size() == 1)
					&& (place.getPredecessors().size() > 1)) { // many-to-one
				// case
				return true;
			} else {
				if ((place.getPredecessors().size() == 1)
						&& (place.getSuccessors().size() > 1)) {// one-to-many
					// case
					return true;
				}
			}
		}
		return false;
	}

	public String getName() {
		return this.NAME;
	}

	public String getType() {
		return this.TYPE;
	}

	public String VerifyBasicRequirements() {
		boolean hasConnector = false;
		int i = net.getPlaces().size();
		// Verify the existence of XOR's
		for (int count = 0; count < i; count++) {
			Place place = (Place) net.getPlaces().get(count);
			if (place.getSuccessors().size() >= 2) {
				hasConnector = true;
			}
		}
		// if there are no XOR's, then verify the existence of AND's
		i = net.getTransitions().size();
		if (!hasConnector) {
			for (int count = 0; count < i; count++) {
				Transition transition = (Transition) net.getTransitions().get(
						count);
				if ((transition.getSuccessors().size() >= 2)) {
					hasConnector = true;
				}
			}
		}
		if (!hasConnector) {// if there are no connectors the net is not valid.
			return "The Net does not contain a connector, therefore the metric cannot be applied!";
		}// verify if there are at least two places(one for the begining of the
		// net and the other for the end of it),
		// one transition and at least two arcs.
		if (net.getEdges().size() < 2) {
			return "The Petri Net does not contain Arcs. It is not correctly designed";
		} else {
			if (net.getPlaces().size() < 2) {
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
