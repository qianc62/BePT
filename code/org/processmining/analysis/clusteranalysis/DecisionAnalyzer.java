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
import java.awt.Color;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;

/**
 * Class holding the machine learning settings and performing the actual data
 * mining tasks (with the help of the weka library).
 * 
 * @author arozinat (a.rozinat@tm.tue.nl)
 */
public abstract class DecisionAnalyzer {

	/**
	 * The data mining classifier to be used for analysis.
	 */
	protected Classifier myClassifier;
	protected ClusterSet clusters;
	protected AggregateProfile agProfiles;
	protected ClusterDecisionAnalyzer clusterDecisionAnalyzer;

	/**
	 * Analyses the given list of decision points according to the context
	 * specified. Furthermore, the context is provided with some visualization
	 * of the analysis result.
	 * 
	 * @param decisionPoints
	 *            the list of decision points to be analysed
	 * @param log
	 *            the log to be analysed
	 * @param highLevelPN
	 *            the simulation model to export discovered data dependencies
	 */
	public void analyse(ClusterDecisionAnalyzer cda) {
		clusterDecisionAnalyzer = cda;

		// create empty data set with attribute information
		Instances data = cda.getDataInfo();

		// in case no single learning instance can be provided (as decision
		// point is never
		// reached, or decision classes cannot specified properly) --> do not
		// call algorithm
		if (data.numInstances() == 0) {
			System.out.println("No learning instances available");
		}
		// actually solve the classification problem
		else {
			try {
				myClassifier.buildClassifier(data);
				// build up result visualization
				cda.setResultVisualization(createResultVisualization());
				cda
						.setEvaluationVisualization(createEvaluationVisualization(data));
			} catch (Exception ex) {
				ex.printStackTrace();
				cda
						.setResultVisualization(createMessagePanel("Error while solving the classification problem"));
			}
		}

	}

	// //////////////////////////// Helper methods
	// ////////////////////////////////////////////

	/**
	 * Helper method creating an empty panel containing the given feedback
	 * message for the user.
	 * 
	 * @param message
	 *            the message to be displayed for the user
	 * @return the panel to be displayed as analysis result for the current
	 *         decision point
	 */
	public static JPanel createMessagePanel(String message) {
		JPanel messagePanel = new JPanel(new BorderLayout());
		JLabel messageLabel = new JLabel("     " + message + ".");
		messageLabel.setForeground(new Color(100, 100, 100));
		messagePanel.add(messageLabel, BorderLayout.CENTER);
		return messagePanel;
	}

	/**
	 * Creates an evaluation overview of the built classifier.
	 * 
	 * @return the panel to be displayed as result evaluation view for the
	 *         current decision point
	 */
	protected JPanel createEvaluationVisualization(Instances data) {
		// build text field to display evaluation statistics
		JTextPane statistic = new JTextPane();

		try {
			// build evaluation statistics
			Evaluation evaluation = new Evaluation(data);
			evaluation.evaluateModel(myClassifier, data);
			statistic.setText(evaluation.toSummaryString() + "\n\n"
					+ evaluation.toClassDetailsString() + "\n\n"
					+ evaluation.toMatrixString());

		} catch (Exception ex) {
			ex.printStackTrace();
			return createMessagePanel("Error while creating the decision tree evaluation view");
		}

		statistic.setFont(new Font("Courier", Font.PLAIN, 14));
		statistic.setEditable(false);
		statistic.setCaretPosition(0);

		JPanel resultViewPanel = new JPanel();
		resultViewPanel.setLayout(new BoxLayout(resultViewPanel,
				BoxLayout.PAGE_AXIS));
		resultViewPanel.add(new JScrollPane(statistic));

		return resultViewPanel;
	}

	// //////////////////// Methods to be overridden by subclasses
	// ////////////////////////////

	/**
	 * Specify the name of this algorithm to be displayed in the combo box of
	 * the algorithm view.
	 * 
	 * @return the name of this algorithm
	 */
	public abstract String toString();

	/**
	 * Provide a description of this algorithm to be displayed in the algorithm
	 * view.
	 * 
	 * @return the description of this algorithm
	 */
	public abstract String getDescription();

	/**
	 * Creates a GUI panel containing the parameters that are available for this
	 * type of decision analyser (i.e., the used algorithm).
	 * 
	 * @return the parameters panel to be displayed in the algorithm settings of
	 *         the decsion miner
	 */
	public abstract JPanel getParametersPanel();

	/**
	 * Initializes data mining classifier to be used for analysis.
	 */
	protected abstract void initClassifier();

	/**
	 * Creates some result visualization of the result for the current
	 * classification problem.
	 * 
	 * @return the panel to be displayed as analysis result for the current
	 *         decision point
	 */
	protected abstract JPanel createResultVisualization();

	/**
	 * Invokes the redraw of the given result visualization. Note that the
	 * visualization is not re-created but only adjusted, e.g., with respect to
	 * positioning graphical components which cannot be done before the result
	 * panel is actually packed to the frame.
	 * 
	 * @param panel
	 *            the result visualization to be adjusted
	 */
	protected abstract void redrawResultVisualization(JPanel panel);
}
