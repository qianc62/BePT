package org.processmining.analysis.epc.epcmetrics;

import org.processmining.framework.models.epcpack.ConfigurableEPC;

public class NumberOfEvents implements ICalculator {

	private ConfigurableEPC epc;
	private final static String TYPE = "Size";
	private final static String NAME = "Num of Events";

	public NumberOfEvents(ConfigurableEPC aEpc) {
		epc = aEpc;
	}

	public String Calculate() {
		String output = "" + epc.getEvents().size();
		return output;
	}

	public String getName() {
		return this.NAME;
	}

	public String getType() {
		return this.TYPE;
	}

	public String VerifyBasicRequirements() {
		if (epc.getEvents().size() < 2) {
			return "The EPC does not contain at least 2 events, it is not designed properly!";
		} else {
			if (epc.getEdges().size() < 2) {
				return "The EPC does not contain at least 2 edges, it is not designed properly!";
			}
		}
		return ".";
	}

}
