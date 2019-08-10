package org.processmining.analysis.epc.epcmetrics;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.framework.plugin.ProvidedObject;

public class EPCComplexityCalculator implements AnalysisPlugin {

	public EPCComplexityCalculator() {
	}

	public String getName() {
		return ("EPC Complexity Analysis");
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("EPC") {
			public boolean accepts(ProvidedObject object) {
				int i = 0;
				boolean b = false;
				while (!b && (i < object.getObjects().length)) {
					b |= (object.getObjects()[i] instanceof EPC);
					b |= (object.getObjects()[i] instanceof ConfigurableEPC);
					i++;
				}
				return b;
			}
		} };
		return items;
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		int i = 0;
		boolean b = false;
		while (!b && (i < o.length)) {
			b |= (o[i] instanceof EPC);
			b |= (o[i] instanceof ConfigurableEPC);
			i++;
		}

		Object o2 = o[i - 1];
		ConfigurableEPC org = ((ConfigurableEPC) o2);

		return new EPCComplexityCalculatorUI(org);

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

		String header = "Plugin made by Jo,o Sobrinho and Daniel Teixeira on Univeristy of Madeira (http://www.uma.pt) to analyse Complexity of Business Processes modeled in EPC's.";
		return header + workflow + density + modularity + coupling + general;
	}
}
