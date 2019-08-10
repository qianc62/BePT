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

package org.processmining.analysis.genetic;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * <p>
 * Title: Calculate Fitness
 * </p>
 * <p>
 * Description: Calculates the fitness of an individual to a given log. There is
 * a combo box to select the available fitness measurements.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class CalculateFitness implements AnalysisPlugin {
	public CalculateFitness() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getName() {
		return "Fitness";
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("Individual") {
			public boolean accepts(ProvidedObject object) {
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
			}
		} };
		return items;
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		HeuristicsNet net = null;
		LogReader log = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
			} else if (o[i] instanceof HeuristicsNet) {
				net = ((HeuristicsNet) o[i]).copyNet();
			}
		}

		return new CalculateFitnessUI(log, net);

	}

	public String getHtmlDescription() {
		return "<p> <b>Plug-in: Fitness Calculation </b><p>This plug-in allows "
				+ "the user to calculate the implemented fitness measurements "
				+ "to a given log and heuristic net.";
	}

	private void jbInit() throws Exception {
	}
}
