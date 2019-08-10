package org.processmining.mining.armining;

import org.processmining.framework.util.GUIPropertyDouble;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GUIPropertyBoolean;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import java.awt.Dimension;
import javax.swing.Box;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import org.processmining.framework.ui.filters.GenericMultipleExtFilter;
import org.processmining.framework.log.ProcessInstance;
import java.awt.BorderLayout;
import javax.swing.JCheckBox;

/**
 * <p>
 * Title:AprioriAnalyzer
 * </p>
 * 
 * <p>
 * Description: This class tells the plug-in that the type of Association rule
 * algorithm to be used is the Apriori algorithm.
 * </p>
 * It also prepares the GUI objects accordingly (specific to the chosen
 * algorithm).
 * 
 * @author Shaifali Gupta (s.gupta@student.tue.nl)
 * @version 1.0
 */

public class AprioriAnalyzer extends AssociationAnalyzer {

	protected GUIPropertyInteger numRules;
	protected GUIPropertyDouble upperBoundMinSupport;
	protected GUIPropertyDouble lowerBoundMinSupport;
	protected GUIPropertyBoolean outputItemSets;
	protected GUIPropertyDouble minMetric;
	private JCheckBox dummyChkBox = null;
	private JCheckBox eventCareChkBox = null;

	/**
	 *Saves the intermediate learning instances as an ARFF file
	 */
	private void saveAsARFFBrowseButton_actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new GenericMultipleExtFilter(
				new String[] { "ARFF" }, "ARFF file (*.arff)"));
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile().getPath();
			setChosenARFFFile(name);
		}
	}

	/**
	 *Sets the location for saving the learning instances.
	 */
	private void setChosenARFFFile(String logFileName) {
		locationARFFFile.setText(logFileName);
	}

	/**
	 * Constructor
	 */
	public AprioriAnalyzer() {
		myAssociator = new MyApriori();

		// Upper bound Minimum Support- minimum value is 0.1, default value is
		// 1.0, and the max value is also 1.0
		upperBoundMinSupport = new GUIPropertyDouble(
				"Upper bound for Minimum Support (for itemsets): ",
				((MyApriori) myAssociator).upperBoundMinSupportTipText(),
				((MyApriori) myAssociator).getUpperBoundMinSupport(), 0.1, 1.0,
				0.1);

		// Lower bound Minimum Support- minimum value is 0.1, default value is
		// 0.1, and the max value is 1.0
		lowerBoundMinSupport = new GUIPropertyDouble(
				"Lower bound for Minimum Support (for itemsets): ",
				((MyApriori) myAssociator).lowerBoundMinSupportTipText(),
				((MyApriori) myAssociator).getLowerBoundMinSupport(), 0.1, 1.0,
				0.1);

		// minimum 1 rule and maximum 100 rules,default is 10
		numRules = new GUIPropertyInteger("Population size (for Rules): ",
				((MyApriori) myAssociator).myNumRulesTipText(),
				((MyApriori) myAssociator).getNumRules(), 1, 100);

		// Minimum Confidence-min value is 0.1, default is 0.9, max value is 1.0
		minMetric = new GUIPropertyDouble("Minimum Confidence (for a rule): ",
				((MyApriori) myAssociator).minMetricTipText(),
				((MyApriori) myAssociator).getMinMetric(), 0.1, 1.0, 0.1);

		// Default is False
		outputItemSets = new GUIPropertyBoolean("Output Frequent ItemSets?",
				((MyApriori) myAssociator).outputItemSetsTipText(),
				((MyApriori) myAssociator).getOutputItemSets());

	}

	/**
	 *
	 */
	public void resetAssociator() {
		myAssociator = null;
		myAssociator = new MyApriori();
	}

	/**
	 * @return The name of the algorithm.
	 */
	public String toString() {
		return "Apriori";
	}

	/**
	 * @return String description of this algorithm
	 */
	public String getDescription() {
		return "Returns frequent itemsets(if set true) and association rules.";
	}

	/**
	 *Build GUI for various components
	 */
	public JPanel getParametersPanel() {
		dummyChkBox = new JCheckBox("Insert a dummy (noname) activity?");
		dummyChkBox
				.setToolTipText("If enabled then an activity 'noname' with type 'notype' is inserted in the log and the rules and FIS are accordingly derived from this log.");
		eventCareChkBox = new JCheckBox("care about Event Types?");
		eventCareChkBox
				.setToolTipText("If enabled then FIS/Rules are generated with the event type information.");

		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
		// add parameter panels
		resultPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		resultPanel.add(numRules.getPropertyPanel());
		resultPanel.add(minMetric.getPropertyPanel());
		resultPanel.add(lowerBoundMinSupport.getPropertyPanel());
		resultPanel.add(upperBoundMinSupport.getPropertyPanel());
		resultPanel.add(outputItemSets.getPropertyPanel());

		JPanel chkBoxPanel = new JPanel();
		chkBoxPanel.setLayout(new BoxLayout(chkBoxPanel, BoxLayout.X_AXIS));
		JPanel chkBoxPanel1 = new JPanel();
		chkBoxPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		chkBoxPanel1.setLayout(new BoxLayout(chkBoxPanel1, BoxLayout.Y_AXIS));
		chkBoxPanel1.add(Box.createHorizontalGlue());
		chkBoxPanel1.add(eventCareChkBox);
		chkBoxPanel1.add(dummyChkBox);
		chkBoxPanel1.add(Box.createVerticalGlue());
		chkBoxPanel.add(chkBoxPanel1);
		resultPanel.add(chkBoxPanel);

		locationARFFFile.setMinimumSize(new Dimension(150, 21));
		locationARFFFile.setPreferredSize(new Dimension(350, 21));
		locationARFFFile.setEditable(false);
		locationARFFFile
				.setToolTipText("The location chosen for saving the file is displayed here");

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
		resultPanel.add(locationARFFFile);
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

	/**
	 *
	 */
	protected void initAssociator() {
		applyOptionalParameters();
	}

	/**
	 * Communicates the parameter settings selected the user to the algorithm.
	 */
	public void applyOptionalParameters() {
		((MyApriori) myAssociator).setUpperBoundMinSupport(upperBoundMinSupport
				.getValue());
		((MyApriori) myAssociator).setLowerBoundMinSupport(lowerBoundMinSupport
				.getValue());
		((MyApriori) myAssociator).setMinMetric(minMetric.getValue());
		((MyApriori) myAssociator).setNumRules(numRules.getValue());
		((MyApriori) myAssociator).setOutputItemSets(outputItemSets.getValue());

	}

	/**
	 * Executes the actual algorithm. Generates FIS and then association rules
	 * from these FIS.
	 */
	public void myBuildAssociations() {
		if (data.numInstances() == 0) {
			createMessagePanel("No learning instances available");
		} else {
			try {
				((MyApriori) myAssociator).buildAssociations(data);
			} catch (Exception ex) {
				ex.printStackTrace();
				createMessagePanel("Error while solving the association problem");
			}
		}
	}

	/**
	 *Calls the method to display association rules in the format we want (not
	 * in the Weka format)
	 */
	public ArrayList<String> getRules() {
		return ((MyApriori) myAssociator).get_m_allTheRules();
	}

	/**
	 *Call the method to display FIS in the appropriate format.
	 */
	public ArrayList<String> showFreqItemSets() {
		return ((MyApriori) myAssociator).get_m_Ls();
	}

	// return the value of the chk box for frequent item sets
	public boolean getFreqItemSets() {
		return outputItemSets.getValue();
	}

	// return the value of the chk box for caring abt event type information
	public boolean getETypeValue() {
		return false;
	}

	// Return the value of the chk box for caring abt event type information
	public boolean getNoNameActivity() {
		return false;
	}

	public boolean check(ProcessInstance pi, int ruleIndex) {
		return ((MyApriori) myAssociator).checkForRule(pi, ruleIndex);
	}

	public boolean checkWithEC(ProcessInstance pi, int ruleIndex) {
		return ((MyApriori) myAssociator).checkRuleWithEventCare(pi, ruleIndex);
	}

	public boolean checkFIS(ProcessInstance pi, int ruleIndex) {
		return ((MyApriori) myAssociator).checkForFIS(pi, ruleIndex);
	}

	public boolean checkFISWithEC(ProcessInstance pi, int ruleIndex) {
		return ((MyApriori) myAssociator).checkFISWithEventCare(pi, ruleIndex);
	}

	public double getConfValue() {
		return minMetric.getValue();
	}

	public double getUpperBoundMinSup() {
		return upperBoundMinSupport.getValue();
	}

	public double getLowerBoundMinSup() {
		return lowerBoundMinSupport.getValue();
	}

	public boolean getSaveARFFValue() {
		return saveAsARFFChecked.getValue();
	}

	public String getlocationARFFFile() {
		return locationARFFFile.getText();
	}

	public boolean isCheckBoxDummySelected() {
		return dummyChkBox.isSelected();
	}

	public boolean isCheckBoxECSelected() {
		return eventCareChkBox.isSelected();
	}

}
