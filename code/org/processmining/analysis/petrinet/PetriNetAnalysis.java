/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.analysis.petrinet;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * @author not attributable
 * @version 1.0
 */

public class PetriNetAnalysis implements AnalysisPlugin {
	public PetriNetAnalysis() {
	}

	public String getName() {
		return ("Petri net Analysis");
	}

	/**
	 * The PetriNetAnalysis accepts a PetriNet
	 * 
	 * @return AnalysisInputItem[]
	 */
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
		return new PetriNetAnalysisUI(net);
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:analysis:petrinet";
	}
}
