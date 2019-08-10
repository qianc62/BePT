package org.processmining.analysis.epc.epcmetrics;

import org.processmining.framework.models.epcpack.ConfigurableEPC;

public class NumberOfFunctions implements ICalculator {

	private ConfigurableEPC epc;
	private final static String TYPE = "Size";
	private final static String NAME = "Num of Functions";

	public NumberOfFunctions(ConfigurableEPC aEpc) {
		epc = aEpc;
	}

	public String Calculate() {
		String output = "" + epc.getFunctions().size();
		return output;
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
			if (epc.getEdges().size() < 2) {
				return "The EPC does not contain at least 2 edges, it is not designed properly!";
			}
		}

		return ".";
	}

}
