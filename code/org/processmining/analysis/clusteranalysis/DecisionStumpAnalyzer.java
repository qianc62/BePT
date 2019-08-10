package org.processmining.analysis.clusteranalysis;

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

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyFloat;
import org.processmining.framework.util.GUIPropertyInteger;

import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;
import weka.classifiers.Classifier;
import javax.swing.JLabel;
import java.awt.Color;
import weka.classifiers.trees.DecisionStump;
import javax.swing.JCheckBox;

/**
 * A DecisionAnalyzer using the J48 classifier from the weka library. modified
 * from Anne's decision point analysis class
 * 
 * @author Minseok Song (m.s.song@tue.nl)
 */
public class DecisionStumpAnalyzer extends DecisionAnalyzer {

	/*
	 * private GUIPropertyBoolean unprunedTree; private GUIPropertyFloat
	 * confidence; private GUIPropertyInteger minNoInstances; private
	 * GUIPropertyBoolean reducedPruning; private GUIPropertyInteger
	 * numberFolds; private GUIPropertyBoolean binarySplits; private
	 * GUIPropertyBoolean subtreeRaising; private GUIPropertyBoolean
	 * retainInstanceInfo; private GUIPropertyBoolean smoothing; private
	 * GUIPropertyInteger seed;
	 */
	/**
	 * Default constructor.
	 */
	public DecisionStumpAnalyzer() {
		myClassifier = new DecisionStump();
		// create algorithm properties from the default values
		/*
		 * unprunedTree = new GUIPropertyBoolean("Use unpruned tree", ((J48)
		 * myClassifier).unprunedTipText(), ((J48) myClassifier).getUnpruned());
		 * confidence = new GUIPropertyFloat("Confidence treshold for pruning",
		 * ((J48) myClassifier).confidenceFactorTipText(), ((J48)
		 * myClassifier).getConfidenceFactor(), (float) 0.0, (float) 1.0,
		 * (float) 0.01); minNoInstances = new
		 * GUIPropertyInteger("Minimun number of instances in any leaf", ((J48)
		 * myClassifier).minNumObjTipText(), ((J48)
		 * myClassifier).getMinNumObj(), 0, 1000); reducedPruning = new
		 * GUIPropertyBoolean("Use reduced-error pruning", ((J48)
		 * myClassifier).reducedErrorPruningTipText(), ((J48)
		 * myClassifier).getReducedErrorPruning()); numberFolds = new
		 * GUIPropertyInteger("Number of folds for reduced-error pruning",
		 * ((J48) myClassifier).numFoldsTipText(), ((J48)
		 * myClassifier).getNumFolds(), 1, 100); binarySplits = new
		 * GUIPropertyBoolean("Use binary splits only", ((J48)
		 * myClassifier).binarySplitsTipText(), ((J48)
		 * myClassifier).getBinarySplits()); subtreeRaising = new
		 * GUIPropertyBoolean("Perform subtree raising", ((J48)
		 * myClassifier).subtreeRaisingTipText(), ((J48)
		 * myClassifier).getSubtreeRaising()); retainInstanceInfo = new
		 * GUIPropertyBoolean("Retain instance information", ((J48)
		 * myClassifier).saveInstanceDataTipText(), ((J48)
		 * myClassifier).getSaveInstanceData()); smoothing = new
		 * GUIPropertyBoolean(
		 * "Smooth the probability estimates using Laplace smoothing", ((J48)
		 * myClassifier).useLaplaceTipText(), ((J48)
		 * myClassifier).getUseLaplace()); seed = new
		 * GUIPropertyInteger("Seed for shuffling data", ((J48)
		 * myClassifier).seedTipText(), ((J48) myClassifier).getSeed(), 0, 100);
		 */
	}

	public String toString() {
		return "Decision Stump";
	}

	public String getDescription() {
		return "Class for generating an unpruned or a pruned C4.5 decision tree";
	}

	public JPanel getParametersPanel() {
		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
		// add parameter panels
		/*
		 * resultPanel.add(unprunedTree.getPropertyPanel());
		 * resultPanel.add(confidence.getPropertyPanel());
		 * resultPanel.add(minNoInstances.getPropertyPanel());
		 * resultPanel.add(reducedPruning.getPropertyPanel());
		 * resultPanel.add(numberFolds.getPropertyPanel());
		 * resultPanel.add(binarySplits.getPropertyPanel());
		 * resultPanel.add(subtreeRaising.getPropertyPanel());
		 * resultPanel.add(retainInstanceInfo.getPropertyPanel());
		 * resultPanel.add(smoothing.getPropertyPanel());
		 * resultPanel.add(seed.getPropertyPanel());
		 */
		return resultPanel;
	}

	/**
	 * Initializes data mining classifier to be used for analysis as a J48
	 * classifier (corresponds to the weka implementation of the C4.5
	 * algorithm).
	 */
	protected void initClassifier() {
		myClassifier = new DecisionStump();
		applyOptionalParameters();
	}

	/**
	 * Creates a decision tree visualization for the current classification
	 * problem.
	 * 
	 * @return the panel to be displayed as analysis result for the current
	 *         decision point
	 */
	protected JPanel createResultVisualization() {
		JPanel resultViewPanel = new JPanel(new BorderLayout());

		try {
			/*
			 * JCheckBox[] checks = clusterDecisionAnalyzer.getChecks(); for
			 * (int i=0; i<checks.length; i++) { if(checks[i].isSelected()){
			 * System.out.println("test=" + ((DecisionStump)
			 * myClassifier).toSource(checks[i].getName())); } }
			 */
			System.out.println("test="
					+ ((DecisionStump) myClassifier).toSource("test"));
			/*
			 * resultViewPanel = new DSResultPanel(); ((DSResultPanel)
			 * resultViewPanel).setTreeVisualizer(new TreeVisualizer(null,
			 * ((REPTree) myClassifier).graph(), new PlaceNode2()));
			 */
			return resultViewPanel;
		} catch (Exception ex) {
			ex.printStackTrace();
			return createMessagePanel("Error while creating the decision tree visualization");
		}
	}

	/**
	 * Invokes the redraw of the given decision tree visualization. This is
	 * necessary as the TreeVisualizer component can only be positioned properly
	 * after being drawn.
	 * 
	 * @param panel
	 *            the result visualization to be adjusted
	 */
	protected void redrawResultVisualization(JPanel panel) {
		// message panels are no J48ResultPanels
		/*
		 * if (panel instanceof J48ResultPanel) { ((J48ResultPanel)
		 * panel).redrawTreeVisualizer(); }
		 */
	}

	/**
	 * The options set by the user need to be applied to the algorithm before it
	 * can be used for classification.
	 */
	private void applyOptionalParameters() {
		/*
		 * ((J48) myClassifier).setUnpruned(unprunedTree.getValue()); ((J48)
		 * myClassifier).setConfidenceFactor(confidence.getValue()); ((J48)
		 * myClassifier).setMinNumObj(minNoInstances.getValue()); ((J48)
		 * myClassifier).setReducedErrorPruning(reducedPruning.getValue());
		 * ((J48) myClassifier).setNumFolds(numberFolds.getValue()); ((J48)
		 * myClassifier).setBinarySplits(binarySplits.getValue()); ((J48)
		 * myClassifier).setSubtreeRaising(subtreeRaising.getValue()); ((J48)
		 * myClassifier).setSaveInstanceData(retainInstanceInfo.getValue());
		 * ((J48) myClassifier).setUseLaplace(smoothing.getValue()); ((J48)
		 * myClassifier).setSeed(seed.getValue());
		 */
	}

	/**
	 * Private class for displaying a decision tree as the analysis result.
	 * 
	 * @author arozinat (a.rozinat@tm.tue.nl)
	 */
	private class DSResultPanel extends JPanel {

		/**
		 * Required for a serializable class (generated quickfix). Not directly
		 * used.
		 */
		private static final long serialVersionUID = 3871405020282172506L;

		private TreeVisualizer tv;

		/** The decision tree visualization */

		/**
		 * Default constructor.
		 */
		public DSResultPanel() {
			this.setLayout(new BorderLayout());
		}

		/**
		 * Adds the given decision tree visualizer to this panel.
		 * 
		 * @param treeViz
		 *            the tree visualizer to be added
		 */
		public void setTreeVisualizer(TreeVisualizer treeViz) {
			tv = treeViz;
			this.removeAll();
			this.add(tv, BorderLayout.CENTER);
			this.validate();
			this.repaint();
		}

		/**
		 * Re-positions the tree visualizer on the screen. Should be called
		 * after this panel has been added to its target location within the GUI
		 * structure.
		 */
		public void redrawTreeVisualizer() {
			tv.fitToScreen();
		}
	}
}
