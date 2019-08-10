package org.processmining.analysis.petrinet.petrinetmetrics;

import javax.swing.*;

import org.processmining.analysis.*;
import org.processmining.framework.models.petrinet.*;
import org.processmining.framework.plugin.*;

/**
 * @author not attributable
 * @version 1.0
 */

public class PNComplexityCalculator implements AnalysisPlugin {
	public PNComplexityCalculator() {
	}

	public String getName() {
		return ("Petri Net Complexity Analysis");
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("Petrinet") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof PetriNet) {
						return true;
					}
				}
				return false;
			}
		} };
		return items;
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		PetriNet net = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet) {
				net = (PetriNet) o[i];
			}
		}
		return new PNComplexityCalculatorUI(net);
	}

	public String getHtmlDescription() {
		String workflow = "<h2> Control Flow </h2>"
				+ "		This metric has been developed by Jorge Cardoso (jcardoso@uma.pt) and the paper (How to Measure the Control-flow Complexity of Web Processes and Workflows) about it is available here: http://dme.uma.pt/jcardoso/Research/Papers/Control-flow Complexity-WorkflowHandbook.pdf"
				+ "		<br>Futher informations about this metric can be viewed on the paper mencioned above.";

		String coupling = "<h2>Coupling</h2>"
				+ "		This metric is still being developed and the pespective paper is still a draft provided to us Jorge Cardoso.";

		String density = "<h2>Density</h2>"
				+ "		This metric was developed by Jan Medling (jan.mendling@wu-wien.ac.at) and is presented in the paper intitled \"Testing Density as a Complexity Metric for EPCs\".  ";

		String modularity = "<h2>Modularity</h2>"
				+ "		There is no formula for this metric yet, but as soon as possible will be implemented and available here.";

		String general = "<h2> Other Metrics </h2>"
				+ "		Some size metric are also available. Those metrics are simple that only count the number of elements in the process.";

		String header = "Plugin made by Jo�o Sobrinho and Daniel Teixeira on University of Madeira (http://www.uma.pt) to analyse Complexity of Business Processes modeled in Petri Nets.";
		return header + workflow + density + modularity + coupling + general;
	}
}
