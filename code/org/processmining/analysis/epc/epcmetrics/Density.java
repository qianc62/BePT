/*
 * The Density formula implemented
 * on this class was proposed by Jan Mendling
 * on his paper "Testing Density as a
 * Complexity Metric for EPC's" chapter 3.
 * */

package org.processmining.analysis.epc.epcmetrics;

import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.ui.Message;

public class Density implements ICalculator {
	private final static String TYPE = "Coupling";
	private final static String NAME = "Density";

	private ConfigurableEPC epc;
	private int events;
	private int functions;
	private int connectors;
	private int nodes;
	private int arcs;

	public Density(ConfigurableEPC aEpc) {
		epc = aEpc;
		initializeElements();
	}

	private void initializeElements() {
		events = epc.getEvents().size();
		functions = epc.getFunctions().size();
		connectors = epc.getConnectors().size();
		arcs = epc.getEdges().size();
		nodes = events + functions + connectors;
	}

	public String Calculate() {
		float aMin = nodes - 1;
		float result;
		int cMax;

		Message.add("\t<Density>", Message.TEST);

		if (connectors <= 1) {
			String output = "0";
			return output;
		} else if (connectors % 2 == 0) {
			cMax = power((connectors / 2 + 1), 2);
			Message.add("\t\t<EvenNumConectors/>", Message.TEST);
		} else {
			cMax = power(((connectors - 1) / 2 + 1), 2)
					+ ((connectors - 1) / 2) + 1;
			Message.add("\t\t<EvenNumConectors/>", Message.TEST);
		}
		result = (arcs - aMin) / (cMax + 2 * (events + functions) - aMin);
		Message.add("\t\t<DensityValue value=\"" + Float.toString(result)
				+ "\"/>", Message.TEST);
		String output = Float.toString(result);
		Message.add("\t</Density>", Message.TEST);
		return output;
	}

	private int power(int base, int expoente) {
		int resultado = 1;
		for (int i = 0; i < expoente; i++) {
			resultado = resultado * base;
		}
		return resultado;
	}

	public String getName() {
		return this.NAME;
	}

	public String getType() {
		return this.TYPE;
	}

	public String VerifyBasicRequirements() {
		if (epc.getFunctions().size() == 0) {
			return "The EPC does not contain any function, it is not designed properly!";
		} else {
			if (epc.getEvents().size() < 2) {
				return "The EPC does not contain at least 2 events, it is not designed properly!";
			} else {
				if (epc.getEdges().size() < 2) {
					return "The EPC does not contain at least 2 edges, it is not designed properly!";
				}
			}
		}
		return ".";
	}

}
