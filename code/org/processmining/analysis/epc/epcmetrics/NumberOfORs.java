package org.processmining.analysis.epc.epcmetrics;

import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.ui.Message;

public class NumberOfORs implements ICalculator {

	private ConfigurableEPC epc;
	private final static String TYPE = "Size";
	private final static String NAME = "Num of ORs";

	public NumberOfORs(ConfigurableEPC aEpc) {
		epc = aEpc;
	}

	public String Calculate() {
		int result = 0;
		int numConnectors = this.epc.getConnectors().size();
		for (int i = 0; i < numConnectors; i++) {
			EPCConnector connector = (EPCConnector) this.epc.getConnectors()
					.get(i);
			if (connector.toString().startsWith("OR")) {
				result++;
			}
		}
		String output = "" + result;
		Message.add("\t<NumOfOrs value=\"" + output + "\"/>", Message.TEST);
		return output;
	}

	public String getName() {
		return this.NAME;
	}

	public String getType() {
		return this.TYPE;
	}

	public String VerifyBasicRequirements() {
		return ".";
	}

}
