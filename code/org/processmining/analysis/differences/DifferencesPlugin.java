package org.processmining.analysis.differences;

import java.util.ArrayList;
import java.util.List;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.differences.processdifferences.ProcessAutomaton;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.epcpack.EPCHierarchy;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.util.PluginDocumentationLoader;

import javax.swing.*;

public class DifferencesPlugin implements AnalysisPlugin {
	public static String FIRST_NET_LABEL = "Provided Behaviour";
	public static String SECOND_NET_LABEL = "Required Behaviour";

	private boolean debugging = false; // Special flags that triggers debug
	// behaviour; should be set to false
	private static final boolean testingMultiple = false; // Special flags that

	// triggers debug
	// behaviour; should
	// be set to false

	public DifferencesPlugin() {

	}

	public String getName() {
		return ("Differences Analysis");
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = {
				new AnalysisInputItem(FIRST_NET_LABEL, 1, 1) {
					public boolean accepts(ProvidedObject object) {
						boolean acceptedModel = false;
						for (int i = 0; (i < object.getObjects().length)
								&& !acceptedModel; i++) {
							if (object.getObjects()[i] != null) {
								if (ProcessAutomaton.acceptedModelType(object
										.getObjects()[i])) {
									acceptedModel = true;
								}
							}
						}
						return acceptedModel;
					}
				}, new AnalysisInputItem(SECOND_NET_LABEL, 1, 1) {
					public boolean accepts(ProvidedObject object) {
						boolean acceptedModel = false;
						for (int i = 0; (i < object.getObjects().length)
								&& !acceptedModel; i++) {
							if (object.getObjects()[i] != null) {
								if (ProcessAutomaton.acceptedModelType(object
										.getObjects()[i])) {
									acceptedModel = true;
								}
							}
						}
						return acceptedModel;
					}
				} };

		// If testingMultiple: use special code in this if statement
		if (testingMultiple) {
			AnalysisInputItem[] multiItems = { new AnalysisInputItem(
					FIRST_NET_LABEL, 1, 100) {
				public boolean accepts(ProvidedObject object) {
					boolean acceptedModel = false;
					for (int i = 0; (i < object.getObjects().length)
							&& !acceptedModel; i++) {
						if (object.getObjects()[i] != null) {
							if (object.getObjects()[i] instanceof EPCHierarchy) {
								acceptedModel = true;
							}
						}
					}
					return acceptedModel;
				}
			} };
			return multiItems;
		}
		// If debugging: use special code in this if statement
		if (debugging) {
			AnalysisInputItem[] foo = {};
			return foo;
		}
		return items;
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		// If testingMultiple: use special code in this if statement
		if (testingMultiple) {
			List<EPCHierarchy> epcHierarchies = new ArrayList<EPCHierarchy>();
			for (ProvidedObject p : inputs[0].getProvidedObjects()) {
				for (Object o : p.getObjects()) {
					if (o instanceof EPCHierarchy) {
						epcHierarchies.add((EPCHierarchy) o);
					}
				}
			}
			return new MultiDifferencesUI(epcHierarchies);
		}
		DifferencesUI ui = null;
		ModelGraph req = null, prov = null;

		// If debugging: use special code in this if statement
		if (debugging) {
			ui = new DifferencesUI(null, null);
			ui.setSize(1024, 800);
			return ui;
		}

		// get the two selected nets
		for (int i = 0; (i < inputs[0].getProvidedObjects()[0].getObjects().length)
				&& (req == null); i++) {
			if (ProcessAutomaton.acceptedModelType(inputs[0]
					.getProvidedObjects()[0].getObjects()[i])) {
				req = (ModelGraph) inputs[0].getProvidedObjects()[0]
						.getObjects()[i];
			}
		}
		for (int i = 0; (i < inputs[1].getProvidedObjects()[0].getObjects().length)
				&& (prov == null); i++) {
			if (ProcessAutomaton.acceptedModelType(inputs[1]
					.getProvidedObjects()[0].getObjects()[i])) {
				prov = (ModelGraph) inputs[1].getProvidedObjects()[0]
						.getObjects()[i];
			}
		}
		// invoke the GUI
		ui = new DifferencesUI(req, prov);
		ui.setSize(1024, 800);
		return ui;
	}

	public String getHtmlDescription() {
		return PluginDocumentationLoader.load(this);
	}
}
