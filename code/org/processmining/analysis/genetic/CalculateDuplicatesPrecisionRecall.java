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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.heuristics.HeuristicsNetInputItem;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.ui.Message;
import org.processmining.mining.geneticmining.analysis.duplicates.DuplicatesEquivalent;
import org.processmining.mining.geneticmining.util.MethodsOverIndividuals;

/**
 * <p>
 * Calculates the duplicates precision and recall of two nets. The nets must
 * have the same task labels (not necessarily the same amount of tasks!).
 * </p>
 * 
 * @author Ana Karla A. de Medeiros.
 * @version 1.0
 */

public class CalculateDuplicatesPrecisionRecall implements AnalysisPlugin {

	public static String FIRST_NET_LABEL = "Base net";
	public static String SECOND_NET_LABEL = "Mined net";

	public CalculateDuplicatesPrecisionRecall() {
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = {
				new HeuristicsNetInputItem(FIRST_NET_LABEL),
				new HeuristicsNetInputItem(SECOND_NET_LABEL) };
		return items;

	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		JTextArea text = null;
		Object[] o1 = (inputs[0].getProvidedObjects())[0].getObjects();
		Object[] o2 = (inputs[1].getProvidedObjects())[0].getObjects();
		HeuristicsNet base = null, mined = null;

		DuplicatesEquivalent metrics;
		String result;

		for (int i = 0; i < o1.length; i++) {
			if (o1[i] instanceof HeuristicsNet) {
				base = MethodsOverIndividuals
						.removeDanglingElementReferences((HeuristicsNet) o1[i]);
			}
		}
		for (int i = 0; i < o2.length; i++) {
			if (o2[i] instanceof HeuristicsNet) {
				mined = MethodsOverIndividuals
						.removeDanglingElementReferences((HeuristicsNet) o2[i]);
			}
		}

		try {
			metrics = new DuplicatesEquivalent(base, mined);
			result = " The values for the duplicates precision (Dp) and recall (Dr) are \n\n"
					+ "Dp = "
					+ metrics.getPrecision()
					+ "\n\n"
					+ "Dr = "
					+ metrics.getRecall();
			Message.add("<DuplicatesPrecisionRecall> duplicatesPrecision=\""
					+ metrics.getPrecision() + "\" duplicatesRecall=\""
					+ metrics.getRecall() + "\">", Message.TEST);

		} catch (Exception ex) {
			result = "The metrics could not be calculated. Please check if both "
					+ "nets contain the same " + "amount of task labels";
		}

		text = new JTextArea(result);
		text.setEditable(false);
		return new JScrollPane(text);

	}

	public String getName() {
		return "Duplicates Precision/Recall";
	}

	public String getHtmlDescription() {
		return "<p> <b>Duplicates Precision and Recall Analysis Plug-in</b></p>"
				+ "<p> Calculates the precision and recall of two nets based on "
				+ "the amount of duplicates that the nets have in common. </p>"
				+ "<p> Note: The nets must have the same task labels "
				+ "(but not necessarily the same amount of tasks!).</p>";
	}

}
