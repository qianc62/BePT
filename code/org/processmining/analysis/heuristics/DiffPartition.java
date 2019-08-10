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

package org.processmining.analysis.heuristics;

import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.ui.Message;
import org.processmining.mining.geneticmining.util.MethodsOverIndividuals;

/**
 * @author Ana Karla A. de Medeiros, Peter van den Brand
 * @version 1.0
 */

public class DiffPartition implements AnalysisPlugin {

	public DiffPartition() {
	}

	public String getName() {
		return "HN diff sets";
	}

	public String getHtmlDescription() {
		return "This plug-in shows differences between the input and output condition functions "
				+ "of activities in a Heuristic net (or causal matrix). The comparison is only possible if the nets have (i) "
				+ "the same amount of activities and (ii) the same set of labels for these activities.";
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = {
				new HeuristicsNetInputItem(HeuristicsNet.FIRST_NET_LABEL),
				new HeuristicsNetInputItem(HeuristicsNet.SECOND_NET_LABEL) };
		return items;
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		JTextArea text = null;
		Object[] o1 = (inputs[0].getProvidedObjects())[0].getObjects();
		Object[] o2 = (inputs[1].getProvidedObjects())[0].getObjects();
		HeuristicsNet one = null, two = null;
		String result;

		for (int i = 0; i < o1.length; i++) {
			if (o1[i] instanceof HeuristicsNet) {
				one = MethodsOverIndividuals
						.removeDanglingElementReferences((HeuristicsNet) o1[i]);
			}
		}
		for (int i = 0; i < o2.length; i++) {
			if (o2[i] instanceof HeuristicsNet) {
				two = MethodsOverIndividuals
						.removeDanglingElementReferences((HeuristicsNet) o2[i]);
			}
		}
		result = one.diffForSets(two);
		StringTokenizer st = new StringTokenizer(result, "\n,[]=+><() ");
		Message.add("<HNDiffSets numbefOfTokensInDiffResult=\""
				+ st.countTokens() + "\">", Message.TEST);

		if (result.equals("")) {
			result = "The nets are identical!";
		}

		text = new JTextArea(result);
		text.setEditable(false);
		return new JScrollPane(text);

	}
}
