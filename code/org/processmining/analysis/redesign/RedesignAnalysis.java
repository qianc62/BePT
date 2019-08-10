package org.processmining.analysis.redesign;

/**
 * Provides the possibility to create redesigns for a given high-level Petri net.
 * The redesign is done step by step by transforming a certain process part with
 * a certain redesign type. In this way, gradually a tree of redesign alternatives
 * is created. The performance of the original and the alternative models can be
 * evaluated with simulation.
 *
 * @see HLPetriNet
 *
 * @author Mariska Netjes
 */

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.redesign.ui.RedesignAnalysisUI;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.plugin.ProvidedObject;

public class RedesignAnalysis implements AnalysisPlugin {
	public RedesignAnalysis() {
	}

	/**
	 * Specifies the name of the plug-in. This is used for, e.g., labelling the
	 * corresponding menu item or the user documentation page.
	 * 
	 * @return the name
	 */
	public String getName() {
		return ("Redesign Analysis");
	}

	/**
	 * The RedesignAnalysis accepts a HLPetriNet
	 * 
	 * @return AnalysisInputItem[]
	 */
	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("HLPetriNet model") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof HLPetriNet) {
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
		HLPetriNet net = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof HLPetriNet) {
				net = (HLPetriNet) o[i];
			}
		}

		return new RedesignAnalysisUI(net);
	}

	/**
	 * Provides user documentation for the plugin
	 * 
	 * @return a URL that will be opened in the default browser of the user
	 */
	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/online/redesign";
	}
}
