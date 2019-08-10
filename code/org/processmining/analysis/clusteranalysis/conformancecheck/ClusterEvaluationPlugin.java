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

package org.processmining.analysis.clusteranalysis.conformancecheck;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.analysis.traceclustering.model.ClusterSet;

import org.processmining.analysis.traceclustering.profile.AggregateProfile;

/**
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Minseok Song
 * @version 1.0
 */
public class ClusterEvaluationPlugin implements AnalysisPlugin {

	protected AggregateProfile agProfiles;
	protected ClusterSet clusters;

	public ClusterEvaluationPlugin() {
	}

	public String getHtmlDescription() {
		return "<p> This plug-in read trace clustering result and evaluate result.<p> ";
	}

	public AnalysisInputItem[] getInputItems() {
		// needs any instance of LogReader to work
		AnalysisInputItem[] items = { new AnalysisInputItem("Trace cluster") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof ClusterSet) {
						return true;
					}

				}
				return false;
			}
		}

		};
		return items;
	}

	public String getName() {
		return "Conformance Checking for clustering";
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		clusters = null;

		for (int i = 0; clusters == null; i++) {
			if (o[i] instanceof ClusterSet) {
				clusters = (ClusterSet) o[i];
				// agProfiles = clusters.getAGProfiles();
			}
		}

		ClusterEvaluationAnalyzer ana = new ClusterEvaluationAnalyzer();
		return ana.analyse(clusters);
	}
}
