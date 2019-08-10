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

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.geneticmining.util.MethodsOverIndividuals;

/**
 * <p>
 * Title: Summary of Structural Properties of a Heuristic Net
 * </p>
 * 
 * <p>
 * Description: This class summarizes basic information about the structure of a
 * Heuristic net. For instance, number of labels, activities, arcs, etc.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class HNStructuralPropertySummary implements AnalysisPlugin {

	public HNStructuralPropertySummary() {
	}

	public String getName() {
		return "HN Property Summary";
	}

	public String getHtmlDescription() {
		return "This plug-ins summarizes how many activities, labels and arcs a Heuristic Net contains.";
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("Individual") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasNet = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof HeuristicsNet) {
						hasNet = true;
					}
				}
				return hasNet;
			}
		} };
		return items;
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		JTextArea text = null;
		Object[] o1 = (inputs[0].getProvidedObjects())[0].getObjects();
		HeuristicsNet hn = null;
		String result;

		for (int i = 0; i < o1.length; i++) {
			if (o1[i] instanceof HeuristicsNet) {
				hn = MethodsOverIndividuals
						.removeDanglingElementReferences((HeuristicsNet) o1[i]);
			}
		}
		result = calculateStructuralPropertySummary(hn);

		text = new JTextArea(result);
		text.setEditable(false);
		return new JScrollPane(text);

	}

	private String calculateStructuralPropertySummary(HeuristicsNet hn) {

		StringBuffer propertiesSummary = new StringBuffer();

		int numberConnectedActivities = 0;

		int numberArcs = 0;

		for (int from = 0; from < hn.getOutputSets().length; from++) {
			if (hn.getInputSet(from).size() > 0
					|| hn.getOutputSet(from).size() > 0) {
				numberConnectedActivities++;
			}

			for (int j = 0; j < hn.getOutputSet(from).size(); j++) {
				for (int k = 0; k < hn.getOutputSet(from).get(j).size(); k++) {
					int to = hn.getOutputSet(from).get(j).get(k);
					numberArcs += hn.getInputSetsWithElement(to, from).size();
				}
			}

		}

		propertiesSummary
				.append("The selected Heuristic net has the following properties: \n\n");
		propertiesSummary.append("Amount of labels: "
				+ hn.getReverseDuplicatesMapping().length + "\n\n");
		propertiesSummary.append("Amount of activities: "
				+ numberConnectedActivities + "\n\n");
		propertiesSummary.append("Amount of arc: " + numberArcs + "\n\n");

		Message.add("<HNPropertySummary numberOfLabels=\""
				+ hn.getReverseDuplicatesMapping().length
				+ "\" numberOfActivities=\"" + numberConnectedActivities
				+ "\" numberOfArcs =\"" + numberArcs + "\">", Message.TEST);

		return propertiesSummary.toString();

	}

}
