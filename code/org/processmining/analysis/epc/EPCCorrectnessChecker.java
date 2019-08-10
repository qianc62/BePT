package org.processmining.analysis.epc;

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
import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class EPCCorrectnessChecker implements AnalysisPlugin {
	public EPCCorrectnessChecker() {
	}

	public String getName() {
		return ("EPC Verification plugin");
	}

	public String getHtmlDescription() {
		return "<p> <b>Plug-in: EPC Verification</b>"
				+ "<p>This Plug-in allows the user to verify EPCs. For more "
				+ "details, see "
				+ org.processmining.framework.util.Constants.get_BVD_URLString(
						"EPC_verification", "this paper")
				+ " for a description of the approach."
				+ "<p>An application of this plugin to a real life dataset can "
				+ "be found "
				+ org.processmining.framework.util.Constants.get_BVD_URLString(
						"SAP_reduction", "here");
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

		return new EPCCorrectnessCheckerUI(org);
	}

}
