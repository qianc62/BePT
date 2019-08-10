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
import org.processmining.framework.models.fsm.FSM;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * @author not attributable
 * @version 1.0
 */

public class StateSpaceRegionCalculator implements AnalysisPlugin {
	public StateSpaceRegionCalculator() {
	}

	public String getName() {
		return ("Region Calculator");
	}

	/**
	 * The StateSpaceRegionCalculator accepts a StateSpace
	 * 
	 * @return AnalysisInputItem[]
	 */
	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("Automaton") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof FSM) {
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
		FSM statespace = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof FSM) {
				statespace = (FSM) o[i];
			}
		}
		return new StateSpaceCalculatorUI(statespace);
	}

	public String getHtmlDescription() {
		String s = "<html>";
		s += "<head><title>ProM Framework: State Space Region Calculator</title></head>";
		s += "<body>";
		s += "This plugin calculates all regions in a state-space";
		s += "</body></html>";
		return s;
	}
}
