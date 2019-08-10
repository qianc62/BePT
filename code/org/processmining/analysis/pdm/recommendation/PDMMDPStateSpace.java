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

package org.processmining.analysis.pdm.recommendation;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.models.pdm.PDMModel;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * <p>
 * Title: PDMAnalysis
 * </p>
 * *
 * <p>
 * Description:
 * </p>
 * *
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * *
 * <p>
 * Company:
 * </p>
 * 
 * @author Irene Vanderfeesten
 * @version 1.0
 */

public class PDMMDPStateSpace implements AnalysisPlugin {

	public PDMMDPStateSpace() {
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("PDM Model") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof PDMModel) {
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
		PDMModel model = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PDMModel) {
				model = (PDMModel) o[i];
			}
		}
		return new PDMMDPStateSpaceUI(model);

	}

	public String getName() {
		return ("PDM MDP Statespace");
	}

	public String getHtmlDescription() {
		return "http://is.tm.tue.nl/staff/ivanderfeesten/ProM/documentation/PDMMDPrecommendations.htm";
	}

}
