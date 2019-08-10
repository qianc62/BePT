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
import org.processmining.analysis.heuristics.HeuristicsNetLogInputItem;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.ui.Message;
import org.processmining.mining.geneticmining.analysis.duplicates.TraceParsing;
import org.processmining.mining.geneticmining.util.MethodsOverIndividuals;

/**
 * <p>
 * Calculates the behavioral precision and recall of two nets. The nets should
 * be associated to a same log.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros.
 * @version 1.0
 */
public class CalculateBehavioralPrecisionRecall implements AnalysisPlugin {
	public static String FIRST_NET_LABEL = "Base net";
	public static String SECOND_NET_LABEL = "Mined net";

	public CalculateBehavioralPrecisionRecall() {

	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = {
				new HeuristicsNetLogInputItem(FIRST_NET_LABEL),
				new HeuristicsNetLogInputItem(SECOND_NET_LABEL) };
		return items;

	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		JTextArea text = null;
		Object[] o1 = (inputs[0].getProvidedObjects())[0].getObjects();
		Object[] o2 = (inputs[1].getProvidedObjects())[0].getObjects();
		HeuristicsNet base = null, mined = null;
		LogReader log = null;

		TraceParsing metrics;
		String result;

		for (int i = 0; i < o1.length; i++) {
			if (o1[i] instanceof HeuristicsNet) {
				base = MethodsOverIndividuals
						.removeDanglingElementReferences(((HeuristicsNet) o1[i])
								.copyNet());
			} else if (o1[i] instanceof LogReader) {
				log = (LogReader) o1[i];
			}
		}
		for (int i = 0; i < o2.length; i++) {
			if (o2[i] instanceof HeuristicsNet) {
				mined = MethodsOverIndividuals
						.removeDanglingElementReferences(((HeuristicsNet) o2[i])
								.copyNet());
			}
		}

		try {
			metrics = new TraceParsing(log, base, mined);
			result = " The values for the behavioral precision (Bp) and recall (Br) are \n\n"
					+ "Bp = "
					+ metrics.getPrecision()
					+ "\n\n"
					+ "Br = "
					+ metrics.getRecall();
			Message.add("<BehavioralPrecisionRecall> behavioralPrecision=\""
					+ metrics.getPrecision() + "\" behavioralRecall=\""
					+ metrics.getRecall() + "\">", Message.TEST);
		} catch (Exception ex) {
			result = "The metrics could not be calculated. Please check if both "
					+ "nets are associated to the same log and if they contain the same "
					+ "amount of tasks";
		}

		text = new JTextArea(result);
		text.setEditable(false);
		return new JScrollPane(text);

	}

	public String getName() {
		return "Behavioral Precision/Recall";
	}

	public String getHtmlDescription() {
		return "<p> <b>Behavioral Precision and Recall Analysis Plug-in</b></p>"
				+ "<p> Calculates the structural precision and recall of two nets. "
				+ "The nets must (i) be associated to the same log and (ii) have the same task labels "
				+ "(but not necessarily the same amount of tasks!).</p>";
	}

}
