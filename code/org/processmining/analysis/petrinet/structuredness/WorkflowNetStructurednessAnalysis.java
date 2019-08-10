package org.processmining.analysis.petrinet.structuredness;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;

public class WorkflowNetStructurednessAnalysis implements AnalysisPlugin {

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		PetriNet net = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet) {
				net = (PetriNet) o[i];
			}
		}
		return new WorkflowNetStructurednessAnalysisUI(net);
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("Petri net") {
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

	public String getHtmlDescription() {
		return null;
	}

	public String getName() {
		return "Workflow Net Metrics";
	}

}
