/**
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *    Copyright (c) 2003-2006 TU/e Eindhoven
 *    by Eindhoven University of Technology
 *    Department of Information Systems
 *    http://is.tm.tue.nl
 *
 ************************************************************************/
package org.processmining.analysis.clusteranalysis;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.processmining.analysis.petrinet.cpnexport.CpnUtils;
import org.processmining.analysis.petrinet.cpnexport.ManagerLayout;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class holding the machine learning settings and performing the actual data
 * mining tasks (with the help of the weka library).
 * 
 * @author arozinat (a.rozinat@tm.tue.nl)
 */
public class ClusterDecisionAnalyzer {
	// for gui
	protected JPanel resultPanel = null;
	protected JSplitPane resultSplitPanel;
	protected JPanel left = null;
	protected JTabbedPane tabbedPane = null;
	protected JPanel treeViewPanel = null;
	protected JPanel algorithmViewPanel = null;
	protected JPanel evaluationViewPanel = null;

	protected JCheckBox[] checks;

	/**
	 * The data mining classifier to be used for analysis.
	 */
	protected DecisionAnalyzer dAnalyzer;
	protected Classifier myClassifier;
	protected ClusterSet clusters;
	protected AggregateProfile agProfiles;

	private void initPanel() {
		left = new JPanel(new BorderLayout());
		tabbedPane = new JTabbedPane(3);
		treeViewPanel = new JPanel(new BorderLayout());
		algorithmViewPanel = new JPanel(new BorderLayout());
		evaluationViewPanel = new JPanel(new BorderLayout());
		tabbedPane.add(treeViewPanel, "Result", 0);
		tabbedPane.add(algorithmViewPanel, "Algorithm", 1);
		tabbedPane.add(evaluationViewPanel, "Evaluation", 2);
		resultSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left,
				tabbedPane);
		resultSplitPanel.setDividerLocation(150);
		resultSplitPanel.setResizeWeight(0.5);
		resultSplitPanel.setOneTouchExpandable(true);
		resultPanel = new JPanel(new BorderLayout());
		resultPanel.add(resultSplitPanel);

		// left panel
		// button
		JPanel p_top = new JPanel(new BorderLayout());
		JButton startButton = new JButton("analyze");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exec();
			}
		});
		p_top.add(startButton);
		// check box
		JPanel p = new JPanel(new GridLayout(agProfiles.numberOfItems(), 1));
		checks = new JCheckBox[agProfiles.numberOfItems()];
		for (int i = 0; i < agProfiles.numberOfItems(); i++) {
			String name = CpnUtils.replaceSpecialCharacters(agProfiles
					.getItemKey(i));
			checks[i] = new JCheckBox(name);
			checks[i].setSelected(true);
			p.add(checks[i]);
		}
		JScrollPane leftScrollPane = new JScrollPane(p);
		left.add(p_top, BorderLayout.NORTH);
		left.add(leftScrollPane, BorderLayout.CENTER);
	}

	public JCheckBox[] getChecks() {
		return checks;
	}

	public void exec() {
		dAnalyzer.analyse(this);
	}

	public JPanel analyse(ClusterSet aClusters) {
		this.clusters = aClusters;
		agProfiles = clusters.getAGProfiles();
		dAnalyzer = new ClusterJ48Analyzer();
		// dAnalyzer = new DecisionStumpAnalyzer();
		dAnalyzer.initClassifier();
		initPanel();
		setAlgorithmVisualization(dAnalyzer.getParametersPanel());
		return resultPanel;
	}

	/**
	 * Creates a decision tree visualization for the current classification
	 * problem.
	 * 
	 * @return the panel to be displayed as analysis result for the current
	 *         decision point
	 */
	protected void setResultVisualization(JPanel result) {
		treeViewPanel.removeAll();
		treeViewPanel.add(result);
		treeViewPanel.revalidate();
	}

	/**
	 * Creates a decision tree visualization for the current classification
	 * problem.
	 * 
	 * @return the panel to be displayed as analysis result for the current
	 *         decision point
	 */
	protected void setAlgorithmVisualization(JPanel result) {
		algorithmViewPanel.removeAll();
		algorithmViewPanel.add(result);
		algorithmViewPanel.revalidate();
	}

	/**
	 * Creates a decision tree visualization for the current classification
	 * problem.
	 * 
	 * @return the panel to be displayed as analysis result for the current
	 *         decision point
	 */
	protected void setEvaluationVisualization(JPanel result) {
		evaluationViewPanel.removeAll();
		evaluationViewPanel.add(result);
		evaluationViewPanel.revalidate();
	}

	public Instances getDataInfo() {
		// create attribute information
		FastVector attributeInfo = new FastVector();
		// make attribute
		// clean the relevant attribute list and re-fill based on new selection
		// scope
		for (int i = 0; i < agProfiles.numberOfItems(); i++) {
			if (checks[i].isSelected()) {
				String name = CpnUtils.replaceSpecialCharacters(agProfiles
						.getItemKey(i));
				Attribute wekaAtt = new Attribute(name);
				attributeInfo.addElement(wekaAtt);
			}
		}
		// for target concept
		FastVector my_nominal_values = new FastVector(clusters.getClusters()
				.size());
		Attribute targetConcept = null;
		for (Cluster aCluster : clusters.getClusters()) {
			my_nominal_values.addElement(aCluster.getName());
		}
		targetConcept = new Attribute("Cluster", my_nominal_values);
		attributeInfo.addElement(targetConcept);
		attributeInfo.trimToSize();

		// learning
		Instances data = new Instances("Clustering", attributeInfo, 0);
		data.setClassIndex(data.numAttributes() - 1);

		for (Cluster aCluster : clusters.getClusters()) {
			String clusterName = aCluster.getName();
			for (Integer i : aCluster.getTraceIndices()) {
				Instance instance0 = new Instance(attributeInfo.size());
				for (int j = 0; j < agProfiles.numberOfItems(); j++) {
					if (checks[j].isSelected()) {
						String name = CpnUtils
								.replaceSpecialCharacters(agProfiles
										.getItemKey(j));
						Attribute wekaAtt = data.attribute(name);
						if (wekaAtt != null) {
							double doubleAttValue = (new Double(agProfiles
									.getValue(i, j))).doubleValue();
							instance0.setValue(wekaAtt, doubleAttValue);
						} else {
							System.out.println("fail to add");
						}
					}
				}
				instance0.setDataset(data);
				instance0.setClassValue(clusterName);
				data.add(instance0);
			}
		}
		return data;
	}
}
