package org.processmining.analysis.dws;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.util.PluginDocumentationLoader;

/**
 * This is the main class implementing the DWS (Disjunctive Workflow Schema)
 * analysis plugin.
 * 
 * It requires that a logReader object and a HeuristicNet model are available(
 * discovered through the corresponding mining plugin).
 * 
 * @author Gianluigi Greco, Antonella Guzzo, Luigi Pontieri
 * @version 1.0
 * @see org.processmining.framework.models.heuristics.HeuristicsNet
 */

public class DWSAnalysis implements AnalysisPlugin {

	/**
	 * Returns the input items needed by this analysis algorithm. The framework
	 * uses this information to let the user select appropriate inputs.
	 * 
	 * @return The input items accepted by this analysis algorithm, that is a
	 *         logReader and a HeuristicNet.
	 */
	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem(
				"Mined heuristicnet") {
			public boolean accepts(ProvidedObject object) {
				// anto da qui
				Object[] o = object.getObjects();
				boolean hasLog = false, hasNet = false;
				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof LogReader) {
						hasLog = true;
					}
					if (o[i] instanceof HeuristicsNet) {
						hasNet = true;
					}
				}
				return hasLog && hasNet;
				// fin qui
				/*
				 * gianluigi Object[] o = object.getObjects(); if (o.length!=2)
				 * return false; if (o[1] instanceof LogReader && o[0]
				 * instanceof HeuristicsNet) return true; return false;
				 */
			}
		} };
		return items;
	}

	/*
	 * Crea una finestra di input alla quale passa come parametri il LogReader e
	 * l'HeuristicNet ottenuti in automatico nella funzione getInputItems
	 */
	public JComponent analyse(AnalysisInputItem[] arg0) {
		// anto
		Object[] o = (arg0[0].getProvidedObjects())[0].getObjects();
		HeuristicsNet net = null;
		LogReader log = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
			} else if (o[i] instanceof HeuristicsNet) {
				net = (HeuristicsNet) o[i];
			}
		}

		return new DWSInputAnalysisGUI(this, log, net, arg0);
	}

	/**
	 * Gets the name of this plugin. This name will be used in the gui of the
	 * framework to select this plugin.
	 * 
	 * @return The name of this plugin.
	 */
	public String getName() {
		return "DWS analysis plugin";
	}

	/**
	 * Gets a description of this plugin in HTML, used by the framework to be
	 * displayed in the help system.
	 * 
	 * @return A description of this plugin in HTML.
	 */
	public String getHtmlDescription() {
		return PluginDocumentationLoader.load(this);
	}
}
