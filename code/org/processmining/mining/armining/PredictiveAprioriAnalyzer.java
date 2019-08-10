package org.processmining.mining.armining;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.filters.GenericMultipleExtFilter;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyInteger;

/**
 * <p>
 * Title: PredictiveAprioriAnalyzer
 * </p>
 * 
 * <p>
 * Description:This class tells the plug-in that the type of Association rule
 * algorithm to be used is the Predictive Apriori algorithm.
 * </p>
 * It also prepares the GUI objects accordingly (specific to the chosen
 * algorithm). </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Shaifali Gupta (s.gupta@student.tue.nl)
 * @version 1.0
 * 
 */

public class PredictiveAprioriAnalyzer extends AssociationAnalyzer {

	protected GUIPropertyInteger p_NumRules;
	protected GUIPropertyBoolean p_eventTypeCare;
	protected GUIPropertyBoolean p_insertNoNameActivity;
	private JCheckBox eventCareChkBox = null;

	private void saveAsARFFBrowseButton_actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();

		chooser.setFileFilter(new GenericMultipleExtFilter(
				new String[] { "ARFF" }, "ARFF file (*.arff)"));
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile().getPath();
			setChosenARFFFile(name);
		}
	}

	private void setChosenARFFFile(String logFileName) {
		locationARFFFile.setText(logFileName);
	}

	public PredictiveAprioriAnalyzer() {
		myAssociator = new MyPredApriori();

		// default is 100, minimum 1 rule and maximum 100 rules
		p_NumRules = new GUIPropertyInteger("Number of Rules: ",
				((MyPredApriori) myAssociator).numRulesTipText(),
				((MyPredApriori) myAssociator).getNumRules(), 1, 100);
	}

	public void resetAssociator() {
		myAssociator = null;
		myAssociator = new MyPredApriori();
	}

	public String toString() {
		return "Predictive Apriori";
	}

	public String getDescription() {
		return "Returns the best n association rules.";
	}

	public JPanel getParametersPanel() {

		eventCareChkBox = new JCheckBox("care about Event Types?");
		eventCareChkBox
				.setToolTipText("If enabled then FIS/Rules are generated with the event type information.");

		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
		resultPanel.add(Box.createRigidArea(new Dimension(5, 0)));

		resultPanel.add(p_NumRules.getPropertyPanel());

		JPanel chkBoxPanel = new JPanel();
		chkBoxPanel.setLayout(new BoxLayout(chkBoxPanel, BoxLayout.X_AXIS));
		JPanel chkBoxPanel1 = new JPanel();
		chkBoxPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		chkBoxPanel1.setLayout(new BoxLayout(chkBoxPanel1, BoxLayout.Y_AXIS));
		chkBoxPanel1.add(Box.createHorizontalGlue());
		chkBoxPanel1.add(eventCareChkBox);
		chkBoxPanel1.add(Box.createVerticalGlue());
		chkBoxPanel.add(chkBoxPanel1);
		resultPanel.add(chkBoxPanel);

		JPanel locationPanel = new JPanel(new BorderLayout());
		locationARFFFile.setMinimumSize(new Dimension(150, 21));
		locationARFFFile.setPreferredSize(new Dimension(350, 21));
		locationARFFFile.setEditable(false);
		locationARFFFile
				.setToolTipText("the location chosen for saving the file is displayed here");
		locationPanel.add(locationARFFFile);

		JPanel browsePanel = new JPanel(new BorderLayout());
		saveAsARFFBrowseButton.setMaximumSize(new Dimension(120, 25));
		saveAsARFFBrowseButton.setMinimumSize(new Dimension(120, 25));
		saveAsARFFBrowseButton.setPreferredSize(new Dimension(120, 25));
		saveAsARFFBrowseButton.setActionCommand("");
		saveAsARFFBrowseButton.setText("Browse...");
		saveAsARFFBrowseButton
				.setToolTipText("Specify the location for saving the ARFF file");
		browsePanel.add(saveAsARFFBrowseButton);

		resultPanel.add(saveAsARFFChecked.getPropertyPanel());
		resultPanel.add(locationPanel);
		resultPanel.add(Box.createVerticalGlue());
		resultPanel.add(browsePanel);
		resultPanel.add(Box.createVerticalGlue());
		saveAsARFFBrowseButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveAsARFFBrowseButton_actionPerformed(e);
					}
				});
		return resultPanel;
	}

	protected void initAssociator() {
		applyOptionalParameters();
	}

	/**
	 * applyOptionalParameters
	 */
	public void applyOptionalParameters() {
		((MyPredApriori) myAssociator).setNumRules(p_NumRules.getValue());

	}

	public void myBuildAssociations() {
		if (data.numInstances() == 0) {
			createMessagePanel("No learning instances available");
		} else {
			try {
				((MyPredApriori) myAssociator).buildAssociations(data);
			} catch (Exception ex) {
				ex.printStackTrace();
				createMessagePanel("Error while solving the association problem");
			}
		}
	}

	public ArrayList<String> getRules() {
		return ((MyPredApriori) myAssociator).get_m_allTheRules();
	}

	public ArrayList<String> showFreqItemSets() {
		return null;
	}

	public boolean getFreqItemSets() {
		return false;
	}

	public boolean check(ProcessInstance pi, int ruleIndex) {
		return ((MyPredApriori) myAssociator).checkForRule(pi, ruleIndex);
	}

	/**
	 *@param
	 *@return
	 */
	public boolean checkWithEC(ProcessInstance pi, int ruleIndex) {
		return ((MyPredApriori) myAssociator).checkRuleWithEventCare(pi,
				ruleIndex);
	}

	public boolean checkFIS(ProcessInstance pi, int ruleIndex) {
		return ((MyPredApriori) myAssociator).checkFISInRule(pi, ruleIndex);
	}

	public boolean checkFISWithEC(ProcessInstance pi, int ruleIndex) {
		return ((MyPredApriori) myAssociator).checkRuleWithEventCare(pi,
				ruleIndex);
	}

	/**
	 *
	 */
	// return the value of the chk box for caring abt event type information
	public boolean getETypeValue() {
		return false;
	}

	/**
	 *
	 */
	// return the value of the chk box for caring abt event type information
	public boolean getNoNameActivity() {
		return false;
	}

	public double getConfValue() {
		return 0;
	}

	public double getUpperBoundMinSup() {
		return 0;
	}

	public double getLowerBoundMinSup() {
		return 0;
	}

	public boolean getSaveARFFValue() {
		return saveAsARFFChecked.getValue();
	}

	public String getlocationARFFFile() {
		return locationARFFFile.getText();
	}

	public boolean isCheckBoxDummySelected() {
		return false;
	}

	public boolean isCheckBoxECSelected() {
		return eventCareChkBox.isSelected();
	}

}
