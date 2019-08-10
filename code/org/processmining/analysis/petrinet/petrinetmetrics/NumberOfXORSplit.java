package org.processmining.analysis.petrinet.petrinetmetrics;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.ui.Message;

public class NumberOfXORSplit implements ICalculator {
	private PetriNet net;
	private final static String TYPE = "Size";
	private final static String NAME = "XOR-Splits";

	public NumberOfXORSplit(PetriNet net) {
		super();
		this.net = net;
	}

	public String Calculate() {
		int i = net.getPlaces().size();
		int result = 0;

		for (int o = 0; o < i; o++) {
			Place place = net.getPlaces().get(o);
			if (place.getSuccessors().size() > 1
					&& place.getPredecessors().size() > 0) {
				result++;
			}
		}
		Message.add("\t<NumOfXorSplits value=\"" + result + "\"/>",
				Message.TEST);
		return "" + result;
	}

	public String getName() {
		return this.NAME;
	}

	public String getType() {
		return this.TYPE;
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

	public String VerifyBasicRequirements() {
		boolean flag = false;
		int transitions = this.net.getTransitions().size();
		for (int i = 0; i < transitions; i++) {
			Transition transition = this.net.getTransitions().get(i);
			if (isAND(transition)) {
				flag = true;
			}
		}
		int places = this.net.getPlaces().size();
		for (int i = 0; i < places; i++) {
			Place place = this.net.getPlaces().get(i);
			if (isXOR(place)) {
				flag = true;
			}
		}
		if (!flag) {
			return "The Net has no connectors";
		}
		return ".";
	}

}
