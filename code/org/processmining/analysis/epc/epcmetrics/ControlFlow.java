/*
 * The Complexity formula implemented
 * on this class was proposed by Jorge Cardoso
 * on his paper "How to Measure the Control-flow Complexity of Web Processes and Workflows" .
 * http://dme.uma.pt/jcardoso/Research/Papers/Control-flow Complexity-WorkflowHandbook.pdf
 * */

package org.processmining.analysis.epc.epcmetrics;

import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.ui.Message;

public class ControlFlow implements ICalculator {
	private final static String TYPE = "Control-Flow";
	private final static String NAME = "Control-Flow";

	private ConfigurableEPC epc;

	public ControlFlow(ConfigurableEPC aEpc) {
		epc = aEpc;
	}

	public String Calculate() {
		Message.add("\t<CFC>", Message.TEST);
		int i = epc.getConnectors().size();
		int result = 0;
		for (int count = 0; count < i; count++) {
			if (epc.getConnectors().get(count).toString().endsWith("join")) {
				continue;
			}
			if (epc.getConnectors().get(count).toString().startsWith("XOR")) {
				EPCObject epcObj = (EPCObject) epc.getConnectors().get(count);
				result = result + fanout(epcObj);
				Message.add("\t\t<XORConnector "
						+ "name=\""
						+ ((EPCConnector) epc.getConnectors().get(count))
								.toString() + "\" " + "value=\""
						+ fanout(epcObj) + "\"/>", Message.TEST);
			} else if (epc.getConnectors().get(count).toString().startsWith(
					"OR")) {
				EPCObject epcObj = (EPCObject) epc.getConnectors().get(count);
				int temp = power(2, fanout(epcObj)) - 1;
				result = result + temp;
				Message.add("\t\t<ORConnector "
						+ "name=\""
						+ ((EPCConnector) epc.getConnectors().get(count))
								.toString() + "\" " + "value=\"" + temp
						+ "\"/>", Message.TEST);
			} else if (epc.getConnectors().get(count).toString().startsWith(
					"AND")) {
				++result;
				Message.add("\t\t<ANDConnector "
						+ "name=\""
						+ ((EPCConnector) epc.getConnectors().get(count))
								.toString() + "\" " + "value=\"" + 1 + "\"/>",
						Message.TEST);
			}
		}
		String output = "" + result;
		Message.add("\t\t<TotalCFC value=\"" + output + "\">", Message.TEST);
		Message.add("\t</CFC>", Message.TEST);
		return output;
	}

	private int fanout(EPCObject aEpcObj) {
		return aEpcObj.getSuccessors().size();
	}

	private int power(int base, int expoente) {
		int resultado = 1;
		for (int i = 0; i < expoente; i++) {
			resultado = resultado * base;
		}
		return resultado;
	}

	/*
	 * The basic requirements for this metric: At least one connector,one
	 * function and one
	 */
	public String VerifyBasicRequirements() {
		if (epc.getConnectors().size() == 0) {
			return "The metric cannot be applied to this EPC because it does not have any connector, maybe it is not designed properly";
		} else {
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
		}
		return ".";
	}

	public String getType() {
		return this.TYPE;
	}

	public String getName() {
		return this.NAME;
	}
}
