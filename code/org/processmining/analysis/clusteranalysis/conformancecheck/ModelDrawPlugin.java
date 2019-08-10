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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.SlickTabbedPane;
import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.plugin.ProvidedObject; //import org.processmining.mining.petrinetmining.AlphaPPProcessMiner;
import org.processmining.mining.petrinetmining.AlphaProcessMiner;
import org.processmining.mining.petrinetmining.PetriNetResult;
import org.processmining.mining.regionmining.RegionMiner;
import org.processmining.analysis.traceclustering.model.Cluster;
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
public class ModelDrawPlugin implements AnalysisPlugin {

	protected AggregateProfile agProfiles;
	protected ClusterSet clusters;

	public ModelDrawPlugin() {
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
		return "Draw process models for clustering";
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		clusters = null;

		for (int i = 0; clusters == null; i++) {
			if (o[i] instanceof ClusterSet) {
				clusters = (ClusterSet) o[i];
			}
		}

		// ClusterEvaluationAnalyzer ana = new ClusterEvaluationAnalyzer();
		JPanel resultPanel = new JPanel();
		resultPanel.setBorder(BorderFactory.createEmptyBorder());
		resultPanel.setLayout(new BorderLayout());
		SlickTabbedPane tabbedPane = new SlickTabbedPane();
		// progressPanel.setProgress(0);
		// draw model
		for (Cluster cluster : clusters.getClusters()) {
			try {
				AlphaProcessMiner miningPlugin = new AlphaProcessMiner();
				// RegionMiner miningPlugin = new RegionMiner();
				// AlphaPPProcessMiner miningPlugin = new AlphaPPProcessMiner();
				miningPlugin.getOptionsPanel(cluster.getFilteredLog()
						.getLogSummary());
				PetriNetResult result = (PetriNetResult) miningPlugin
						.mine(cluster.getFilteredLog());
				JScrollPane scrollPane = new JScrollPane(result
						.getVisualization());
				tabbedPane.addTab(cluster.getName(), scrollPane);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			// progressPanel.setProgress(progressPanel.getValue()+1);
		}

		HeaderBar header = new HeaderBar("Clustering Result Analysis");
		header.setHeight(40);
		resultPanel.add(header, BorderLayout.NORTH);
		resultPanel.add(tabbedPane, BorderLayout.CENTER);
		return resultPanel;
		// return ana.analyse(clusters);
	}
}
