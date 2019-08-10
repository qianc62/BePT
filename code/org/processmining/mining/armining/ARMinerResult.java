package org.processmining.mining.armining;

import javax.swing.JPanel;
import org.processmining.mining.MiningResult;
import org.processmining.framework.log.LogReader;
import javax.swing.JComponent;
import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import java.util.ArrayList;
import javax.swing.Box;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JScrollPane;
import org.processmining.framework.ui.DoubleClickTable;
import javax.swing.table.AbstractTableModel;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Iterator;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.plugin.ProvidedObject;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import java.awt.FlowLayout;
import java.awt.Color;
import javax.swing.JOptionPane;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title:ARMinerResult
 * </p>
 * 
 * <p>
 * Description: This class builds the output GUI.
 * </p>
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
 */

public class ARMinerResult extends JPanel implements MiningResult, Provider {

	private LogReader logReader;
	private AssociationAnalyzer myAnalyzer;
	private Boolean myECheckBox;

	// GUI variables
	private JTabbedPane myTabPane = new JTabbedPane();
	private JPanel myPanel = new JPanel();
	private JPanel myPanel2 = new JPanel();
	private JPanel myRulesPanel = new JPanel(new BorderLayout());
	private JPanel myFISPanel = new JPanel(new BorderLayout());
	private JPanel rulesInTextPanel = new JPanel();
	private JPanel FISInTextPanel = new JPanel();
	private JPanel clusterPanel = new JPanel();
	private JPanel clusterTablePanel = new JPanel();
	private JPanel clusterRulesPanel = new JPanel();
	private JPanel clusterFISPanel = new JPanel();
	private JPanel clusterFISTablePanel = new JPanel();
	private JPanel clusterFISPanel2 = new JPanel();
	private JButton invertButton = new JButton("Invert Selection");
	private JButton invertFISButton = new JButton("Invert Selection");
	private JLabel numSelectedPI = new JLabel("");
	private JLabel numSelectedPIFIS = new JLabel("");
	private ArrayList<Integer> myRulesPI = null;
	private ArrayList<Integer> myFISPI = null;
	private ArrayList instanceIDs = new ArrayList();
	private ArrayList<String> myRules = null;
	private ArrayList<String> myRules1 = null;

	private ArrayList<String> myRulesWithEType1 = null;
	private ArrayList<String> myRulesWithEType2 = null;
	private ArrayList<String> myFISArrayListWithEType = null;
	private ArrayList<String> myFISArrayList = null;
	private DoubleClickTable processInstanceIDsTable;
	private DoubleClickTable processInstanceIDsTable2;
	private int[] selectedInstanceIndices;
	private JScrollPane tableContainer = null;
	private JScrollPane tableContainer2 = null;
	private GUIPropertyListEnumeration myRulesList = null;
	private GUIPropertyListEnumeration myRulesList1 = null;
	private GUIPropertyListEnumeration myRulesListWithEType1 = null;
	private GUIPropertyListEnumeration myRulesListWithEType2 = null;
	private GUIPropertyListEnumeration myFISListWithEType = null;
	private GUIPropertyListEnumeration myFISList = null;

	/**
	 * Obtains the names of the process instances in the log and stores them in
	 * the instanceIDs list
	 */
	public void obtainInstanceIDs() {
		instanceIDs.clear();
		logReader.reset();
		Iterator allTraces = logReader.instanceIterator();
		while (allTraces.hasNext()) {
			ProcessInstance currentTrace = (ProcessInstance) allTraces.next();
			instanceIDs.add(currentTrace.getName());
		}
	}

	public ProvidedObject[] getProvidedObjects() {
		try {
			if (myTabPane.getTabCount() == 2) {
				if (myTabPane.getSelectedIndex() == 0) { // FIS tab
					try {
						if (processInstanceIDsTable2 != null
								&& processInstanceIDsTable2.getSelectionModel()
										.isSelectionEmpty() == false) {
							ProvidedObject[] objects = {
									new ProvidedObject("Whole Log",
											new Object[] { logReader }),
									new ProvidedObject(
											"Log Selection",
											new Object[] { LogReaderFactory
													.createInstance(
															logReader,
															getSelectionStatus2()) }) };
							return objects;
						} else {
							ProvidedObject[] objects = new ProvidedObject[] { new ProvidedObject(
									"Whole Log", new Object[] { logReader }) };
							return objects;
						}
					} catch (Exception e) {
						System.err
								.println("Fatal error creating new log reader instance:");
						System.err.println("(" + this.getClass() + ")");
						e.printStackTrace();
						return null;
					}

				} else { // Association rules tab
					try {
						if (processInstanceIDsTable != null
								&& processInstanceIDsTable.getSelectionModel()
										.isSelectionEmpty() == false) {
							ProvidedObject[] objects = {
									new ProvidedObject("Whole Log",
											new Object[] { logReader }),
									new ProvidedObject(
											"Log Selection",
											new Object[] { LogReaderFactory
													.createInstance(
															logReader,
															getSelectionStatus()) }) };
							return objects;
						} else {
							ProvidedObject[] objects = new ProvidedObject[] { new ProvidedObject(
									"Whole Log", new Object[] { logReader }) };
							return objects;
						}
					} catch (Exception e) {
						System.err
								.println("Fatal error creating new log reader instance:");
						System.err.println("(" + this.getClass() + ")");
						e.printStackTrace();
						return null;
					}
				}
			} else {
				try {
					if (processInstanceIDsTable != null
							&& processInstanceIDsTable.getSelectionModel()
									.isSelectionEmpty() == false) {
						ProvidedObject[] objects = {
								new ProvidedObject("Whole Log",
										new Object[] { logReader }),
								new ProvidedObject("Log Selection",
										new Object[] { LogReaderFactory
												.createInstance(logReader,
														getSelectionStatus()) }) };
						return objects;
					} else {
						ProvidedObject[] objects = new ProvidedObject[] { new ProvidedObject(
								"Whole Log", new Object[] { logReader }) };
						return objects;
					}
				} catch (Exception e) {
					System.err
							.println("Fatal error creating new log reader instance:");
					System.err.println("(" + this.getClass() + ")");
					e.printStackTrace();
					return null;
				}
			}
		} catch (Exception e) {
			System.err.println("Fatal error creating new log reader instance:");
			System.err.println("(" + this.getClass() + ")");
			e.printStackTrace();
			return null;
		}
	}

	private int[] getSelectionStatus2() {
		return processInstanceIDsTable2.getSelectedRows();
	}

	private int[] getSelectionStatus() {
		return processInstanceIDsTable.getSelectedRows();
	}

	public ARMinerResult(AssociationAnalyzer currentAnalyzer,
			LogReader currentReader) {
		this.logReader = currentReader;
		int number = logReader.getLogSummary().getNumberOfProcessInstances();
		// initially, all instances are selected in the instances table
		selectedInstanceIndices = new int[number];
		for (int i = 0; i < number; i++) {
			selectedInstanceIndices[i] = i;
		}
		obtainInstanceIDs();
		myAnalyzer = currentAnalyzer;

		try {
			jbInit();
			// / messages for testing the Plug-in
			Message.add("<Association Rule Mining>", Message.TEST);
			Message.add("Number of process instances (cases) = "
					+ logReader.getLogSummary().getNumberOfProcessInstances(),
					Message.TEST);
			Message.add("Number of audit trail entries (events) = "
					+ logReader.getLogSummary().getNumberOfAuditTrailEntries()
					+ "\n", Message.TEST);

			Message.add("Algorithm selected:" + myAnalyzer.toString(),
					Message.TEST);
			Message.add("Number of rules: " + myAnalyzer.getRules().size(),
					Message.TEST);

			if (myAnalyzer instanceof AprioriAnalyzer) {
				Message.add("Confidence: " + myAnalyzer.getConfValue(),
						Message.TEST);
				Message.add("Upper Bound for Min Support: "
						+ myAnalyzer.getUpperBoundMinSup(), Message.TEST);
				Message.add("Lower Bound for Min Support:  "
						+ myAnalyzer.getLowerBoundMinSup(), Message.TEST);
				Message.add("Output FIS?" + myAnalyzer.getFreqItemSets(),
						Message.TEST);
			}

			Message.add("Care about Event Type?" + myAnalyzer.getETypeValue(),
					Message.TEST);
			Message.add("Save as ARFF file?: " + myAnalyzer.getSaveARFFValue()
					+ "\n", Message.TEST);

			if (myAnalyzer.getFreqItemSets() == true) {
				Message
						.add(
								myAnalyzer.getRules().size()
										+ "  association rules and frequent itemsets are generated successfully with the "
										+ myAnalyzer.toString() + " algorithm",
								Message.TEST);
			} else {
				Message
						.add(
								myAnalyzer.getRules().size()
										+ " association rules are generated successfully with the "
										+ myAnalyzer.toString() + " algorithm",
								Message.TEST);
			}

			if (myAnalyzer.getETypeValue() == true) {
				Message
						.add(
								"Association rules are generated with event type information about the tasks.",
								Message.TEST);
			}

			if (myAnalyzer.getSaveARFFValue() == true
					&& myAnalyzer.getlocationARFFFile() != "") {
				Message.add(
						"The intermediate input ARFF file has been saved to the location: "
								+ myAnalyzer.getlocationARFFFile().toString()
								+ ".", Message.TEST);
			}
			Message.add("</Association Rule Mining>", Message.TEST);
			Message.add("--------------------------", Message.TEST);
			// Plug-in test ends
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		Boolean myFISValue = false;
		myFISValue = myAnalyzer.getFreqItemSets();

		// if the algorithm chosen in Apriori
		if (myAnalyzer instanceof AprioriAnalyzer) {
			// Display FIS as well as rules & FIS with Event type information
			if (myFISValue == true && myAnalyzer.isCheckBoxECSelected() == true) {
				// if (myFISValue == true) {
				myTabPane.removeAll();
				myRulesPanel.setLayout(new BoxLayout(myRulesPanel,
						BoxLayout.Y_AXIS));
				myRulesPanel.add(Box.createVerticalStrut(15));
				// panel for showing rules in text area
				myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.X_AXIS));
				rulesInTextPanel.setLayout(new BoxLayout(rulesInTextPanel,
						BoxLayout.X_AXIS));
				myRulesWithEType1 = new ArrayList(myAnalyzer.getRules());
				if (myRulesWithEType1.isEmpty() == true) {
					JOptionPane
							.showMessageDialog(
									rulesInTextPanel,
									"No rules can be found for this log using the Apriori algorithm, try using the Pred. Apriori algorithm.",
									"Information ",
									JOptionPane.INFORMATION_MESSAGE);
				}
				rulesInTextPanel.add(MyApriori.newDisplay(myRulesWithEType1));
				rulesInTextPanel.setPreferredSize(new Dimension(1000, 350));
				rulesInTextPanel.setMaximumSize(new Dimension(1000, 350));
				rulesInTextPanel.setMinimumSize(new Dimension(650, 200));

				clusterPanel.setLayout(new BoxLayout(clusterPanel,
						BoxLayout.Y_AXIS));
				processInstanceIDsTable = new DoubleClickTable(
						new ExtendedLogTable(), null);
				tableContainer = new JScrollPane(processInstanceIDsTable);
				tableContainer.setPreferredSize(new Dimension(140, 500));
				tableContainer.setMaximumSize(new Dimension(140, 500));
				tableContainer.setMinimumSize(new Dimension(100, 250));
				clusterTablePanel = new JPanel();
				clusterTablePanel.setLayout(new BoxLayout(clusterTablePanel,
						BoxLayout.Y_AXIS));
				clusterTablePanel.add(tableContainer);

				JPanel invertButtonPanel = new JPanel();
				invertButtonPanel.setLayout(new BoxLayout(invertButtonPanel,
						BoxLayout.X_AXIS));
				invertButton.setAlignmentX(invertButton.RIGHT_ALIGNMENT);
				invertButton.setEnabled(true);
				invertButton.setMnemonic(KeyEvent.VK_I);
				invertButton.setActionCommand("Invert");
				invertButton
						.setToolTipText("Invert the current selection of PIs.");
				invertButtonPanel.add(invertButton);

				clusterPanel.add(clusterTablePanel);
				clusterPanel.add(Box.createRigidArea(new Dimension(0, 10)));
				clusterPanel.add(invertButtonPanel);

				invertButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// invertSelectionStatus();
						if ("Invert".equals(e.getActionCommand())) {
							ListSelectionModel selectionModel = processInstanceIDsTable
									.getSelectionModel();
							// step through the table
							for (int index = 0; index < logReader
									.numberOfInstances(); index++) {
								if (selectionModel.isSelectedIndex(index) == true) {
									// if entry is currently selected -->
									// deselect
									selectionModel.removeSelectionInterval(
											index, index);
								} else {
									// if entry is currently not selected -->
									// select
									selectionModel.addSelectionInterval(index,
											index);
								}
							}
						}
					}
				});
				myPanel.add(rulesInTextPanel);
				myPanel.add(Box.createRigidArea(new Dimension(50, 0)));
				myPanel.add(clusterPanel);
				myPanel.add(Box.createVerticalGlue());
				myRulesPanel.add(myPanel);

				// panel for showing rules in combo box
				clusterRulesPanel.setLayout(new FlowLayout());
				myRulesListWithEType1 = new GUIPropertyListEnumeration(
						"Select Rule: ", null, myRulesWithEType1, null, 500);
				JButton clusterRuleButton = new JButton("Cluster");
				clusterRuleButton.setEnabled(true);
				clusterRuleButton.setMnemonic(KeyEvent.VK_C);
				clusterRuleButton.setActionCommand("Cluster");
				clusterRuleButton
						.setToolTipText("Retrieve the cluster satisfying the selected rule.");
				clusterRulesPanel.add(myRulesListWithEType1.getPropertyPanel());
				clusterRulesPanel.add(clusterRuleButton);

				// if Cluster button is clicked then update the table of PIs.
				clusterRuleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if ("Cluster".equals(e.getActionCommand())) {
							clusterRulesPanel.remove(numSelectedPI);
							myRulesPI = null;
							myRulesPI = new ArrayList<Integer>();
							numSelectedPI = null;
							// get the index of currently selected rule
							int ruleIndex = myRulesWithEType1
									.indexOf(myRulesListWithEType1.getValue());

							for (int j = 0; j < logReader.numberOfInstances(); j++) {
								ProcessInstance pi = logReader.getInstance(j);
								if (myAnalyzer.checkWithEC(pi, ruleIndex) == true) {
									myRulesPI.add(j);
								} else {
								}
							}

							int m2[] = new int[myRulesPI.size()];
							for (int i = 0; i < m2.length; i++) {
								m2[i] = myRulesPI.get(i);
							}
							selectInstances(m2);
						}
						numSelectedPI = new JLabel(
								"Num of PIs satisfying this rule: "
										+ myRulesPI.size());
						clusterRulesPanel.add(numSelectedPI);
					}
				});
				myRulesPanel.add(clusterRulesPanel);
				myRulesPanel.add(Box.createRigidArea(new Dimension(0, 25)));
				myRulesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
						10, 10));
				// -----------------------------------------------------------------------------------------------------------
				// panel for showing FIS in combo box
				myFISPanel
						.setLayout(new BoxLayout(myFISPanel, BoxLayout.Y_AXIS));
				myFISPanel.add(Box.createVerticalStrut(15));

				myPanel2.setLayout(new BoxLayout(myPanel2, BoxLayout.X_AXIS));
				FISInTextPanel.setLayout(new BoxLayout(FISInTextPanel,
						BoxLayout.X_AXIS));
				myFISArrayListWithEType = new ArrayList(myAnalyzer
						.showFreqItemSets());
				FISInTextPanel.add(MyApriori
						.newDisplay(myFISArrayListWithEType));
				FISInTextPanel.setPreferredSize(new Dimension(1000, 350));
				FISInTextPanel.setMaximumSize(new Dimension(1000, 350));
				FISInTextPanel.setMinimumSize(new Dimension(650, 200));

				clusterFISPanel.setLayout(new BoxLayout(clusterFISPanel,
						BoxLayout.Y_AXIS));
				processInstanceIDsTable2 = new DoubleClickTable(
						new ExtendedLogTable(), null);
				tableContainer2 = new JScrollPane(processInstanceIDsTable2);
				tableContainer2.setPreferredSize(new Dimension(140, 500));
				tableContainer2.setMaximumSize(new Dimension(140, 500));
				tableContainer2.setMinimumSize(new Dimension(100, 250));
				clusterFISTablePanel = new JPanel();
				clusterFISTablePanel.setLayout(new BoxLayout(
						clusterFISTablePanel, BoxLayout.Y_AXIS));
				clusterFISTablePanel.add(tableContainer2);

				JPanel invertFISButtonPanel = new JPanel();
				invertFISButtonPanel.setLayout(new BoxLayout(
						invertFISButtonPanel, BoxLayout.X_AXIS));
				invertFISButton.setEnabled(true);
				invertFISButton.setMnemonic(KeyEvent.VK_I);
				invertFISButton.setActionCommand("Invert");
				invertFISButton
						.setToolTipText("Invert the current selection of PIs.");
				invertFISButtonPanel.add(invertFISButton);

				clusterFISPanel.add(clusterFISTablePanel);
				clusterFISPanel.add(Box.createRigidArea(new Dimension(0, 10)));
				clusterFISPanel.add(invertFISButtonPanel);

				invertFISButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// invertSelectionStatus();
						if ("Invert".equals(e.getActionCommand())) {
							ListSelectionModel selectionModel = processInstanceIDsTable2
									.getSelectionModel();
							// step through the table
							for (int index = 0; index < logReader
									.numberOfInstances(); index++) {
								if (selectionModel.isSelectedIndex(index) == true) {
									// if entry is currently selected -->
									// deselect
									selectionModel.removeSelectionInterval(
											index, index);
								} else {
									// if entry is currently not selected -->
									// select
									selectionModel.addSelectionInterval(index,
											index);
								}
							}
						}
					}
				});
				myPanel2.add(FISInTextPanel);
				myPanel2.add(Box.createRigidArea(new Dimension(50, 0)));
				myPanel2.add(clusterFISPanel);
				myPanel2.add(Box.createVerticalGlue());
				myFISPanel.add(myPanel2);

				// panel for showing rules in combo box
				clusterFISPanel2.setLayout(new FlowLayout());
				myFISListWithEType = new GUIPropertyListEnumeration(
						"Select FIS", null, myFISArrayListWithEType, null, 500);
				JButton clusterFISButton = new JButton("Cluster");
				clusterFISButton.setEnabled(true);
				clusterFISButton.setMnemonic(KeyEvent.VK_C);
				clusterFISButton.setActionCommand("Cluster");
				clusterFISButton
						.setToolTipText("Retrieve the cluster satisfying the selected FIS.");

				clusterFISPanel2.add(myFISListWithEType.getPropertyPanel());
				clusterFISPanel2.add(clusterFISButton);

				// if Cluster button is clicked then only show the table of PIs.
				clusterFISButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if ("Cluster".equals(e.getActionCommand())) {
							clusterFISPanel2.remove(numSelectedPIFIS);
							myFISPI = null;
							myFISPI = new ArrayList<Integer>();
							numSelectedPIFIS = new JLabel("");
							// get the index of currently selected rule
							int FISIndex = myFISArrayListWithEType
									.indexOf(myFISListWithEType.getValue());
							for (int j = 0; j < logReader.numberOfInstances(); j++) {
								ProcessInstance pi = logReader.getInstance(j);
								if (myAnalyzer.checkFISWithEC(pi, FISIndex) == true) {
									myFISPI.add(j);
								} else {
								}
							}
							int m2[] = new int[myFISPI.size()];
							for (int i = 0; i < m2.length; i++) {
								m2[i] = myFISPI.get(i);
							}
							selectInstances2(m2);
						}
						numSelectedPIFIS = new JLabel(
								"Num of PIs satisfying this FIS: "
										+ myFISPI.size());
						clusterFISPanel2.add(numSelectedPIFIS);
					}
				});

				myFISPanel.add(clusterFISPanel2);
				myFISPanel.add(Box.createRigidArea(new Dimension(0, 25)));
				myFISPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
						10, 10));
				myTabPane.addTab("Apriori-Frequent Itemsets", null, myFISPanel,
						"view FIS");
				myTabPane.addTab("Apriori- Association Rules", null,
						myRulesPanel, "View association rules");
				topPanel.add(myTabPane);
			}
			// *************************************************************************
			if (myFISValue == false
					&& myAnalyzer.isCheckBoxECSelected() == true) { // show only
				// rules
				// with
				// Event
				// Type
				// information
				myTabPane.removeAll();
				myRulesPanel.setLayout(new BoxLayout(myRulesPanel,
						BoxLayout.Y_AXIS));
				myRulesPanel.add(Box.createVerticalStrut(15));

				// panel for showing rules in text area
				myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.X_AXIS));
				rulesInTextPanel.setLayout(new BoxLayout(rulesInTextPanel,
						BoxLayout.X_AXIS));
				myRulesWithEType2 = new ArrayList(myAnalyzer.getRules());
				if (myRulesWithEType2.isEmpty() == true) {
					JOptionPane
							.showMessageDialog(
									rulesInTextPanel,
									"No rules can be found for this log using the Apriori algorithm, try using the Pred. Apriori algorithm.",
									"Information ",
									JOptionPane.INFORMATION_MESSAGE);
				}

				rulesInTextPanel.add(MyApriori.newDisplay(myRulesWithEType2));
				rulesInTextPanel.setPreferredSize(new Dimension(1000, 350));
				rulesInTextPanel.setMaximumSize(new Dimension(1000, 350));
				rulesInTextPanel.setMinimumSize(new Dimension(650, 200));

				clusterPanel.setLayout(new BoxLayout(clusterPanel,
						BoxLayout.Y_AXIS));
				processInstanceIDsTable = new DoubleClickTable(
						new ExtendedLogTable(), null);
				tableContainer = new JScrollPane(processInstanceIDsTable);
				tableContainer.setPreferredSize(new Dimension(140, 500));
				tableContainer.setMaximumSize(new Dimension(140, 500));
				tableContainer.setMinimumSize(new Dimension(100, 250));
				clusterTablePanel = new JPanel();
				clusterTablePanel.setLayout(new BoxLayout(clusterTablePanel,
						BoxLayout.Y_AXIS));
				clusterTablePanel.add(tableContainer);

				JPanel invertButtonPanel = new JPanel();
				invertButtonPanel.setLayout(new BoxLayout(invertButtonPanel,
						BoxLayout.X_AXIS));
				invertButton.setEnabled(true);
				invertButton.setMnemonic(KeyEvent.VK_I);
				invertButton.setActionCommand("Invert");
				invertButton
						.setToolTipText("Invert the current selection of PIs.");
				invertButtonPanel.add(invertButton);

				clusterPanel.add(clusterTablePanel);
				clusterPanel.add(Box.createRigidArea(new Dimension(0, 10)));
				clusterPanel.add(invertButtonPanel);

				invertButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// invertSelectionStatus();
						if ("Invert".equals(e.getActionCommand())) {
							clusterRulesPanel.remove(numSelectedPI);
							numSelectedPI = new JLabel("");
							ListSelectionModel selectionModel = processInstanceIDsTable
									.getSelectionModel();
							// step through the table
							for (int index = 0; index < logReader
									.numberOfInstances(); index++) {
								if (selectionModel.isSelectedIndex(index) == true) {
									// if entry is currently selected -->
									// deselect
									selectionModel.removeSelectionInterval(
											index, index);
								} else {
									// if entry is currently not selected -->
									// select
									selectionModel.addSelectionInterval(index,
											index);
								}
							}
						}
					}
				});
				myPanel.add(rulesInTextPanel);
				myPanel.add(Box.createRigidArea(new Dimension(50, 0)));
				myPanel.add(clusterPanel);
				myPanel.add(Box.createVerticalGlue());
				myRulesPanel.add(myPanel);

				// panel for showing rules in combo box
				clusterRulesPanel.setLayout(new FlowLayout());
				myRulesListWithEType2 = new GUIPropertyListEnumeration(
						"Select Rule: ", null, myRulesWithEType2, null, 500);
				JButton clusterRuleButton = new JButton("Cluster");
				clusterRuleButton.setEnabled(true);
				clusterRuleButton.setMnemonic(KeyEvent.VK_C);
				clusterRuleButton.setActionCommand("Cluster");
				clusterRuleButton
						.setToolTipText("Click this button to retrieve the cluster satisfying the selected rule.");

				clusterRulesPanel.add(myRulesListWithEType2.getPropertyPanel());
				clusterRulesPanel.add(clusterRuleButton);

				// if Cluster button is clicked then update the table of PIs.
				clusterRuleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if ("Cluster".equals(e.getActionCommand())) {
							clusterRulesPanel.remove(numSelectedPI);
							myRulesPI = null;
							myRulesPI = new ArrayList<Integer>();
							numSelectedPI = new JLabel("");
							// get the index of currently selected rule
							int ruleIndex = myRulesWithEType2
									.indexOf(myRulesListWithEType2.getValue());
							for (int j = 0; j < logReader.numberOfInstances(); j++) {
								ProcessInstance pi = logReader.getInstance(j);
								if (myAnalyzer.checkWithEC(pi, ruleIndex) == true) {
									myRulesPI.add(j);
								} else {
								}
							}

							int m2[] = new int[myRulesPI.size()];
							for (int i = 0; i < m2.length; i++) {
								m2[i] = myRulesPI.get(i);
							}
							selectInstances(m2);
						}
						numSelectedPI = new JLabel(
								"Num of PIs satisfying this rule:"
										+ myRulesPI.size());
						clusterRulesPanel.add(numSelectedPI);
					}
				});
				myRulesPanel.add(clusterRulesPanel);
				myRulesPanel.add(Box.createRigidArea(new Dimension(0, 25)));
				myRulesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
						10, 10));
				myTabPane.addTab("Apriori algorithm-Association Rules", null,
						myRulesPanel, "View association rules");
				topPanel.add(myTabPane);
			}
			// ****************************************************************
			if (myFISValue == true
					&& myAnalyzer.isCheckBoxECSelected() == false) {
				myTabPane.removeAll();
				myRulesPanel.setLayout(new BoxLayout(myRulesPanel,
						BoxLayout.Y_AXIS));
				myRulesPanel.add(Box.createVerticalStrut(15));

				// panel for showing rules in text area
				myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.X_AXIS));
				rulesInTextPanel.setLayout(new BoxLayout(rulesInTextPanel,
						BoxLayout.X_AXIS));
				myRules1 = new ArrayList(myAnalyzer.getRules());
				if (myRules1.isEmpty() == true) {
					JOptionPane
							.showMessageDialog(
									rulesInTextPanel,
									"No rules can be found for this log using the Apriori algorithm, try using the Pred. Apriori algorithm.",
									"Information ",
									JOptionPane.INFORMATION_MESSAGE);
				}
				rulesInTextPanel.add(MyApriori.newDisplay(myRules1));
				rulesInTextPanel.setPreferredSize(new Dimension(1000, 350));
				rulesInTextPanel.setMaximumSize(new Dimension(1000, 350));
				rulesInTextPanel.setMinimumSize(new Dimension(650, 200));

				clusterPanel.setLayout(new BoxLayout(clusterPanel,
						BoxLayout.Y_AXIS));
				processInstanceIDsTable = new DoubleClickTable(
						new ExtendedLogTable(), null);
				tableContainer = new JScrollPane(processInstanceIDsTable);
				tableContainer.setPreferredSize(new Dimension(140, 500));
				tableContainer.setMaximumSize(new Dimension(140, 500));
				tableContainer.setMinimumSize(new Dimension(100, 250));
				clusterTablePanel = new JPanel();
				clusterTablePanel.setLayout(new BoxLayout(clusterTablePanel,
						BoxLayout.Y_AXIS));
				clusterTablePanel.add(tableContainer);

				JPanel invertButtonPanel = new JPanel();
				invertButtonPanel.setLayout(new BoxLayout(invertButtonPanel,
						BoxLayout.X_AXIS));
				invertButton.setAlignmentX(invertButton.RIGHT_ALIGNMENT);
				invertButton.setEnabled(true);
				invertButton.setMnemonic(KeyEvent.VK_I);
				invertButton.setActionCommand("Invert");
				invertButton
						.setToolTipText("Invert the current selection of PIs.");
				invertButtonPanel.add(invertButton);

				clusterPanel.add(clusterTablePanel);
				clusterPanel.add(Box.createRigidArea(new Dimension(0, 10)));
				clusterPanel.add(invertButtonPanel);

				invertButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// invertSelectionStatus();
						if ("Invert".equals(e.getActionCommand())) {
							ListSelectionModel selectionModel = processInstanceIDsTable
									.getSelectionModel();
							// step through the table
							for (int index = 0; index < logReader
									.numberOfInstances(); index++) {
								if (selectionModel.isSelectedIndex(index) == true) {
									// if entry is currently selected -->
									// deselect
									selectionModel.removeSelectionInterval(
											index, index);
								} else {
									// if entry is currently not selected -->
									// select
									selectionModel.addSelectionInterval(index,
											index);
								}
							}
						}
					}
				});
				myPanel.add(rulesInTextPanel);
				myPanel.add(Box.createRigidArea(new Dimension(50, 0)));
				myPanel.add(clusterPanel);
				myPanel.add(Box.createVerticalGlue());
				myRulesPanel.add(myPanel);

				// panel for showing rules in combo box
				clusterRulesPanel.setLayout(new FlowLayout());
				myRulesList1 = new GUIPropertyListEnumeration("Select Rule: ",
						null, myRules1, null, 500);
				JButton clusterRuleButton = new JButton("Cluster");
				clusterRuleButton.setEnabled(true);
				clusterRuleButton.setMnemonic(KeyEvent.VK_C);
				clusterRuleButton.setActionCommand("Cluster");
				clusterRuleButton
						.setToolTipText("Retrieve the cluster satisfying the selected rule.");

				clusterRulesPanel.add(myRulesList1.getPropertyPanel());
				clusterRulesPanel.add(clusterRuleButton);

				// if Cluster button is clicked then update the table of PIs.
				clusterRuleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if ("Cluster".equals(e.getActionCommand())) {
							clusterRulesPanel.remove(numSelectedPI);
							myRulesPI = null;
							myRulesPI = new ArrayList<Integer>();
							numSelectedPI = null;
							// get the index of currently selected rule
							int ruleIndex = myRules1.indexOf(myRulesList1
									.getValue());

							for (int j = 0; j < logReader.numberOfInstances(); j++) {
								ProcessInstance pi = logReader.getInstance(j);
								if (myAnalyzer.check(pi, ruleIndex) == true) {
									myRulesPI.add(j);
								} else {

								}
							}

							int m2[] = new int[myRulesPI.size()];
							for (int i = 0; i < m2.length; i++) {
								m2[i] = myRulesPI.get(i);
							}
							selectInstances(m2);
						}
						numSelectedPI = new JLabel(
								"Num of PIs satisfying this rule: "
										+ myRulesPI.size());
						clusterRulesPanel.add(numSelectedPI);
					}
				});
				myRulesPanel.add(clusterRulesPanel);
				myRulesPanel.add(Box.createRigidArea(new Dimension(0, 25)));
				myRulesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
						10, 10));

				// panel for showing FIS in combo box
				myFISPanel
						.setLayout(new BoxLayout(myFISPanel, BoxLayout.Y_AXIS));
				myFISPanel.add(Box.createVerticalStrut(15));

				myPanel2.setLayout(new BoxLayout(myPanel2, BoxLayout.X_AXIS));
				FISInTextPanel.setLayout(new BoxLayout(FISInTextPanel,
						BoxLayout.X_AXIS));
				myFISArrayList = new ArrayList(myAnalyzer.showFreqItemSets());
				FISInTextPanel.add(MyApriori.newDisplay(myFISArrayList));
				FISInTextPanel.setPreferredSize(new Dimension(1000, 350));
				FISInTextPanel.setMaximumSize(new Dimension(1000, 350));
				FISInTextPanel.setMinimumSize(new Dimension(650, 200));

				clusterFISPanel.setLayout(new BoxLayout(clusterFISPanel,
						BoxLayout.Y_AXIS));
				processInstanceIDsTable2 = new DoubleClickTable(
						new ExtendedLogTable(), null);
				tableContainer2 = new JScrollPane(processInstanceIDsTable2);
				tableContainer2.setPreferredSize(new Dimension(140, 500));
				tableContainer2.setMaximumSize(new Dimension(140, 500));
				tableContainer2.setMinimumSize(new Dimension(100, 250));
				clusterFISTablePanel = new JPanel();
				clusterFISTablePanel.setLayout(new BoxLayout(
						clusterFISTablePanel, BoxLayout.Y_AXIS));
				clusterFISTablePanel.add(tableContainer2);

				JPanel invertFISButtonPanel = new JPanel();
				invertFISButtonPanel.setLayout(new BoxLayout(
						invertFISButtonPanel, BoxLayout.X_AXIS));
				invertFISButton.setEnabled(true);
				invertFISButton.setMnemonic(KeyEvent.VK_I);
				invertFISButton.setActionCommand("Invert");
				invertFISButton
						.setToolTipText("Invert the current selection of PIs.");
				invertFISButtonPanel.add(invertFISButton);

				clusterFISPanel.add(clusterFISTablePanel);
				clusterFISPanel.add(Box.createRigidArea(new Dimension(0, 10)));
				clusterFISPanel.add(invertFISButtonPanel);

				invertFISButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// invertSelectionStatus();
						if ("Invert".equals(e.getActionCommand())) {
							ListSelectionModel selectionModel = processInstanceIDsTable2
									.getSelectionModel();
							// step through the table
							for (int index = 0; index < logReader
									.numberOfInstances(); index++) {
								if (selectionModel.isSelectedIndex(index) == true) {
									// if entry is currently selected -->
									// deselect
									selectionModel.removeSelectionInterval(
											index, index);
								} else {
									// if entry is currently not selected -->
									// select
									selectionModel.addSelectionInterval(index,
											index);
								}
							}
						}
					}
				});
				myPanel2.add(FISInTextPanel);
				myPanel2.add(Box.createRigidArea(new Dimension(50, 0)));
				myPanel2.add(clusterFISPanel);
				myPanel2.add(Box.createVerticalGlue());
				myFISPanel.add(myPanel2);

				// panel for showing rules in combo box
				clusterFISPanel2.setLayout(new FlowLayout());
				myFISList = new GUIPropertyListEnumeration("Select FIS", null,
						myFISArrayList, null, 500);
				JButton clusterFISButton = new JButton("Cluster");
				clusterFISButton.setEnabled(true);
				clusterFISButton.setMnemonic(KeyEvent.VK_C);
				clusterFISButton.setActionCommand("Cluster");
				clusterFISButton
						.setToolTipText("Retrieve the cluster satisfying the selected FIS.");

				clusterFISPanel2.add(myFISList.getPropertyPanel());
				clusterFISPanel2.add(clusterFISButton);

				// if Cluster button is clicked then only show the table of PIs.
				clusterFISButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if ("Cluster".equals(e.getActionCommand())) {
							clusterFISPanel2.remove(numSelectedPIFIS);
							myFISPI = null;
							myFISPI = new ArrayList<Integer>();
							numSelectedPIFIS = new JLabel("");
							// get the index of currently selected rule
							int FISIndex = myFISArrayList.indexOf(myFISList
									.getValue());
							for (int j = 0; j < logReader.numberOfInstances(); j++) {
								ProcessInstance pi = logReader.getInstance(j);
								if (myAnalyzer.checkFIS(pi, FISIndex) == true) {
									myFISPI.add(j);
								} else {
								}
							}
							int m2[] = new int[myFISPI.size()];
							for (int i = 0; i < m2.length; i++) {
								m2[i] = myFISPI.get(i);
							}
							selectInstances2(m2);
						}
						numSelectedPIFIS = new JLabel(
								"Num of PIs satisfying this FIS: "
										+ myFISPI.size());
						clusterFISPanel2.add(numSelectedPIFIS);
					}
				});
				myFISPanel.add(clusterFISPanel2);
				myFISPanel.add(Box.createRigidArea(new Dimension(0, 25)));
				myFISPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
						10, 10));
				myTabPane.addTab("Apriori-Frequent Itemsets", null, myFISPanel,
						"view FIS");
				myTabPane.addTab("Apriori- Association Rules", null,
						myRulesPanel, "View association rules");
				topPanel.add(myTabPane);
			}
			// **************_______________________________________________________________________
			if (myFISValue == false
					&& myAnalyzer.isCheckBoxECSelected() == false) {
				myTabPane.removeAll();
				myRulesPanel.setLayout(new BoxLayout(myRulesPanel,
						BoxLayout.Y_AXIS));
				myRulesPanel.add(Box.createVerticalStrut(15));

				// panel for showing rules in text area
				myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.X_AXIS));
				rulesInTextPanel.setLayout(new BoxLayout(rulesInTextPanel,
						BoxLayout.X_AXIS));
				myRules = new ArrayList(myAnalyzer.getRules());
				if (myRules.isEmpty() == true) {
					JOptionPane
							.showMessageDialog(
									rulesInTextPanel,
									"No rules can be found for this log using the Apriori algorithm, try using the Pred. Apriori algorithm.",
									"Information ",
									JOptionPane.INFORMATION_MESSAGE);
				}

				rulesInTextPanel.add(MyApriori.newDisplay(myRules));
				rulesInTextPanel.setPreferredSize(new Dimension(1000, 350));
				rulesInTextPanel.setMaximumSize(new Dimension(1000, 350));
				rulesInTextPanel.setMinimumSize(new Dimension(650, 200));

				clusterPanel.setLayout(new BoxLayout(clusterPanel,
						BoxLayout.Y_AXIS));
				processInstanceIDsTable = new DoubleClickTable(
						new ExtendedLogTable(), null);
				tableContainer = new JScrollPane(processInstanceIDsTable);
				tableContainer.setPreferredSize(new Dimension(140, 500));
				tableContainer.setMaximumSize(new Dimension(140, 500));
				tableContainer.setMinimumSize(new Dimension(100, 250));
				clusterTablePanel = new JPanel();
				clusterTablePanel.setLayout(new BoxLayout(clusterTablePanel,
						BoxLayout.Y_AXIS));
				clusterTablePanel.add(tableContainer);

				JPanel invertButtonPanel = new JPanel();
				invertButtonPanel.setLayout(new BoxLayout(invertButtonPanel,
						BoxLayout.X_AXIS));
				invertButton.setEnabled(true);
				invertButton.setMnemonic(KeyEvent.VK_I);
				invertButton.setActionCommand("Invert");
				invertButton
						.setToolTipText("Invert the current selection of PIs.");
				invertButtonPanel.add(invertButton);

				clusterPanel.add(clusterTablePanel);
				clusterPanel.add(Box.createRigidArea(new Dimension(0, 10)));
				clusterPanel.add(invertButtonPanel);

				invertButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// invertSelectionStatus();
						if ("Invert".equals(e.getActionCommand())) {
							clusterRulesPanel.remove(numSelectedPI);
							numSelectedPI = new JLabel("");
							ListSelectionModel selectionModel = processInstanceIDsTable
									.getSelectionModel();
							// step through the table
							for (int index = 0; index < logReader
									.numberOfInstances(); index++) {
								if (selectionModel.isSelectedIndex(index) == true) {
									// if entry is currently selected -->
									// deselect
									selectionModel.removeSelectionInterval(
											index, index);
								} else {
									// if entry is currently not selected -->
									// select
									selectionModel.addSelectionInterval(index,
											index);
								}
							}
						}
					}
				});
				myPanel.add(rulesInTextPanel);
				myPanel.add(Box.createRigidArea(new Dimension(50, 0)));
				myPanel.add(clusterPanel);
				myPanel.add(Box.createVerticalGlue());
				myRulesPanel.add(myPanel);

				// panel for showing rules in combo box
				clusterRulesPanel.setLayout(new FlowLayout());
				myRulesList = new GUIPropertyListEnumeration("Select Rule: ",
						null, myRules, null, 500);
				JButton clusterRuleButton = new JButton("Cluster");
				clusterRuleButton.setEnabled(true);
				clusterRuleButton.setMnemonic(KeyEvent.VK_C);
				clusterRuleButton.setActionCommand("Cluster");
				clusterRuleButton
						.setToolTipText("Click this button to retrieve the cluster satisfying the selected rule.");

				clusterRulesPanel.add(myRulesList.getPropertyPanel());
				clusterRulesPanel.add(clusterRuleButton);

				// if Cluster button is clicked then update the table of PIs.
				clusterRuleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if ("Cluster".equals(e.getActionCommand())) {
							clusterRulesPanel.remove(numSelectedPI);
							myRulesPI = null;
							myRulesPI = new ArrayList<Integer>();
							numSelectedPI = new JLabel("");
							// get the index of currently selected rule
							int ruleIndex = myRules.indexOf(myRulesList
									.getValue());
							for (int j = 0; j < logReader.numberOfInstances(); j++) {
								ProcessInstance pi = logReader.getInstance(j);
								if (myAnalyzer.check(pi, ruleIndex) == true) {
									myRulesPI.add(j);
								} else {
								}
							}
							int m2[] = new int[myRulesPI.size()];
							for (int i = 0; i < m2.length; i++) {
								m2[i] = myRulesPI.get(i);
							}
							selectInstances(m2);
						}
						numSelectedPI = new JLabel(
								"Num of PIs satisfying this rule:"
										+ myRulesPI.size());
						clusterRulesPanel.add(numSelectedPI);
					}
				});
				myRulesPanel.add(clusterRulesPanel);
				myRulesPanel.add(Box.createRigidArea(new Dimension(0, 25)));
				myRulesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
						10, 10));

				myTabPane.addTab("Apriori algorithm-Association Rules", null,
						myRulesPanel, "View association rules");
				topPanel.add(myTabPane);
			}
		}
		// -----------------------------------------------
		// Algorithm chosen is Predictive Apriori
		else {
			if (myAnalyzer.isCheckBoxECSelected() == false) {
				myTabPane.removeAll();
				myRulesPanel.setLayout(new BoxLayout(myRulesPanel,
						BoxLayout.Y_AXIS));
				myRulesPanel.add(Box.createVerticalStrut(15));

				// panel for showing rules in text area
				myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.X_AXIS));
				rulesInTextPanel.setLayout(new BoxLayout(rulesInTextPanel,
						BoxLayout.X_AXIS));
				myRules = new ArrayList(myAnalyzer.getRules());
				if (myRules.isEmpty() == true) {
					JOptionPane
							.showMessageDialog(
									rulesInTextPanel,
									"No rules can be found for this log using the Predictive Apriori algorithm, try using the Apriori algorithm.",
									"Information ",
									JOptionPane.INFORMATION_MESSAGE);
				}
				rulesInTextPanel.add(MyPredApriori.newDisplay(myRules));
				rulesInTextPanel.setPreferredSize(new Dimension(1000, 350));
				rulesInTextPanel.setMaximumSize(new Dimension(1000, 350));
				rulesInTextPanel.setMinimumSize(new Dimension(650, 200));

				clusterPanel.setLayout(new BoxLayout(clusterPanel,
						BoxLayout.Y_AXIS));
				processInstanceIDsTable = new DoubleClickTable(
						new ExtendedLogTable(), null);
				tableContainer = new JScrollPane(processInstanceIDsTable);
				tableContainer.setPreferredSize(new Dimension(140, 500));
				tableContainer.setMaximumSize(new Dimension(140, 500));
				tableContainer.setMinimumSize(new Dimension(100, 250));
				clusterTablePanel = new JPanel();
				clusterTablePanel.setLayout(new BoxLayout(clusterTablePanel,
						BoxLayout.Y_AXIS));
				clusterTablePanel.add(tableContainer);

				JPanel invertButtonPanel = new JPanel();
				invertButtonPanel.setLayout(new BoxLayout(invertButtonPanel,
						BoxLayout.X_AXIS));
				invertButton.setAlignmentX(invertButton.RIGHT_ALIGNMENT);
				invertButton.setEnabled(true);
				invertButton.setMnemonic(KeyEvent.VK_I);
				invertButton.setActionCommand("Invert");
				invertButton
						.setToolTipText("Invert the current selection of PIs.");
				invertButtonPanel.add(invertButton);

				clusterPanel.add(clusterTablePanel);
				clusterPanel.add(Box.createRigidArea(new Dimension(0, 10)));
				clusterPanel.add(invertButtonPanel);

				invertButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// invertSelectionStatus();
						if ("Invert".equals(e.getActionCommand())) {
							ListSelectionModel selectionModel = processInstanceIDsTable
									.getSelectionModel();
							// step through the table
							for (int index = 0; index < logReader
									.numberOfInstances(); index++) {
								if (selectionModel.isSelectedIndex(index) == true) {
									// if entry is currently selected -->
									// deselect
									selectionModel.removeSelectionInterval(
											index, index);
								} else {
									// if entry is currently not selected -->
									// select
									selectionModel.addSelectionInterval(index,
											index);
								}
							}
						}
					}
				});

				myPanel.add(rulesInTextPanel);
				myPanel.add(Box.createRigidArea(new Dimension(50, 0)));
				myPanel.add(clusterPanel);
				myPanel.add(Box.createVerticalGlue());
				myRulesPanel.add(myPanel);

				// panel for showing rules in combo box
				clusterRulesPanel.setLayout(new FlowLayout());
				myRulesList = new GUIPropertyListEnumeration("Select Rule: ",
						null, myRules, null, 500);

				JButton clusterRuleButton = new JButton("Cluster");
				clusterRuleButton.setEnabled(true);
				clusterRuleButton.setMnemonic(KeyEvent.VK_C);
				clusterRuleButton.setActionCommand("Cluster");
				clusterRuleButton
						.setToolTipText("Click this button to retrieve the cluster satisfying the selected rule.");

				clusterRulesPanel.add(myRulesList.getPropertyPanel());
				clusterRulesPanel.add(clusterRuleButton);

				// if Cluster button is clicked then update the table of PIs.
				clusterRuleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if ("Cluster".equals(e.getActionCommand())) {
							clusterRulesPanel.remove(numSelectedPI);
							myRulesPI = null;
							myRulesPI = new ArrayList<Integer>();
							numSelectedPI = null;
							// get the index of currently selected rule
							int ruleIndex = myRules.indexOf(myRulesList
									.getValue());
							for (int j = 0; j < logReader.numberOfInstances(); j++) {
								ProcessInstance pi = logReader.getInstance(j);
								if (myAnalyzer.check(pi, ruleIndex) == true) {
									myRulesPI.add(j);
								} else {
								}
							}

							int m2[] = new int[myRulesPI.size()];
							for (int i = 0; i < m2.length; i++) {
								m2[i] = myRulesPI.get(i);
							}
							selectInstances(m2);
						}
						numSelectedPI = new JLabel(
								"Num of PIs satisfying this rule: "
										+ myRulesPI.size());
						clusterRulesPanel.add(numSelectedPI);
					}
				});
				myRulesPanel.add(clusterRulesPanel);
				myRulesPanel.add(Box.createRigidArea(new Dimension(0, 25)));
				myRulesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
						10, 10));

				myTabPane.addTab("Predictive Apriori-Association Rules", null,
						myRulesPanel, "View association rules");
				topPanel.add(myTabPane);
			}

			else { // myETypeValue==true
				myTabPane.removeAll();
				myRulesPanel.setLayout(new BoxLayout(myRulesPanel,
						BoxLayout.Y_AXIS));
				myRulesPanel.add(Box.createVerticalStrut(15));

				// panel for showing rules in text area
				myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.X_AXIS));
				rulesInTextPanel.setLayout(new BoxLayout(rulesInTextPanel,
						BoxLayout.X_AXIS));
				myRules = new ArrayList(myAnalyzer.getRules()); // changed from
				// getRulesWithEType
				// 10th May
				// 1810pm
				if (myRules.isEmpty() == true) {
					JOptionPane
							.showMessageDialog(
									rulesInTextPanel,
									"No rules can be found for this log using the Predictive Apriori algorithm, try using the Apriori algorithm.",
									"Information ",
									JOptionPane.INFORMATION_MESSAGE);
				}
				rulesInTextPanel.add(MyPredApriori.newDisplay(myRules));
				rulesInTextPanel.setPreferredSize(new Dimension(1000, 350));
				rulesInTextPanel.setMaximumSize(new Dimension(1000, 350));
				rulesInTextPanel.setMinimumSize(new Dimension(650, 200));

				clusterPanel.setLayout(new BoxLayout(clusterPanel,
						BoxLayout.Y_AXIS));
				processInstanceIDsTable = new DoubleClickTable(
						new ExtendedLogTable(), null);
				tableContainer = new JScrollPane(processInstanceIDsTable);
				tableContainer.setPreferredSize(new Dimension(140, 500));
				tableContainer.setMaximumSize(new Dimension(140, 500));
				tableContainer.setMinimumSize(new Dimension(100, 250));
				clusterTablePanel = new JPanel();
				clusterTablePanel.setLayout(new BoxLayout(clusterTablePanel,
						BoxLayout.Y_AXIS));
				clusterTablePanel.add(tableContainer);

				JPanel invertButtonPanel = new JPanel();
				invertButtonPanel.setLayout(new BoxLayout(invertButtonPanel,
						BoxLayout.X_AXIS));
				invertButton.setAlignmentX(invertButton.RIGHT_ALIGNMENT);
				invertButton.setEnabled(true);
				invertButton.setMnemonic(KeyEvent.VK_I);
				invertButton.setActionCommand("Invert");
				invertButton
						.setToolTipText("Invert the current selection of PIs.");
				invertButtonPanel.add(invertButton);

				clusterPanel.add(clusterTablePanel);
				clusterPanel.add(Box.createRigidArea(new Dimension(0, 10)));
				clusterPanel.add(invertButtonPanel);

				invertButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// invertSelectionStatus();
						if ("Invert".equals(e.getActionCommand())) {
							ListSelectionModel selectionModel = processInstanceIDsTable
									.getSelectionModel();
							// step through the table
							for (int index = 0; index < logReader
									.numberOfInstances(); index++) {
								if (selectionModel.isSelectedIndex(index) == true) {
									// if entry is currently selected -->
									// deselect
									selectionModel.removeSelectionInterval(
											index, index);
								} else {
									// if entry is currently not selected -->
									// select
									selectionModel.addSelectionInterval(index,
											index);
								}
							}
						}
					}
				});

				myPanel.add(rulesInTextPanel);
				myPanel.add(Box.createRigidArea(new Dimension(50, 0)));
				myPanel.add(clusterPanel);
				myPanel.add(Box.createVerticalGlue());
				myRulesPanel.add(myPanel);

				// panel for showing rules in combo box
				clusterRulesPanel.setLayout(new FlowLayout());
				myRulesList = new GUIPropertyListEnumeration("Select Rule: ",
						null, myRules, null, 500);

				JButton clusterRuleButton = new JButton("Cluster");
				clusterRuleButton.setEnabled(true);
				clusterRuleButton.setMnemonic(KeyEvent.VK_C);
				clusterRuleButton.setActionCommand("Cluster");
				clusterRuleButton
						.setToolTipText("Click this button to retrieve the cluster satisfying the selected rule.");

				clusterRulesPanel.add(myRulesList.getPropertyPanel());
				clusterRulesPanel.add(clusterRuleButton);

				// if Cluster button is clicked then update the table of PIs.
				clusterRuleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if ("Cluster".equals(e.getActionCommand())) {
							clusterRulesPanel.remove(numSelectedPI);
							myRulesPI = null;
							myRulesPI = new ArrayList<Integer>();
							numSelectedPI = null;
							// get the index of currently selected rule
							int ruleIndex = myRules.indexOf(myRulesList
									.getValue());
							for (int j = 0; j < logReader.numberOfInstances(); j++) {
								ProcessInstance pi = logReader.getInstance(j);
								if (myAnalyzer.checkWithEC(pi, ruleIndex) == true) {
									myRulesPI.add(j);
								} else {
								}
							}

							int m2[] = new int[myRulesPI.size()];
							for (int i = 0; i < m2.length; i++) {
								m2[i] = myRulesPI.get(i);
							}
							selectInstances(m2);
						}
						numSelectedPI = new JLabel(
								"Num of PIs satisfying this rule: "
										+ myRulesPI.size());
						clusterRulesPanel.add(numSelectedPI);
					}
				});
				myRulesPanel.add(clusterRulesPanel);
				myRulesPanel.add(Box.createRigidArea(new Dimension(0, 25)));
				myRulesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
						10, 10));

				myTabPane.addTab("Apriori algorithm-Association Rules", null,
						myRulesPanel, "View association rules");
				topPanel.add(myTabPane);

			}
		}
		topPanel.validate();
		topPanel.repaint();
		this.add(topPanel);
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
	}

	public JComponent getVisualization() {
		return this;
	}

	public LogReader getLogReader() {
		return logReader;
	}

	/**
	 * Selects those instances in the process instance table that have an index
	 * that is in the indices list
	 * 
	 * @param indices
	 *            int[]
	 */
	private void selectInstances(int[] indices) {

		processInstanceIDsTable.getSelectionModel().removeSelectionInterval(0,
				processInstanceIDsTable.getRowCount() - 1);
		HashSet intervals = new HashSet();
		if (indices.length > 0) {
			Arrays.sort(indices);
			int firstOfInterval = indices[0];
			int lastOfInterval = firstOfInterval;
			for (int i = 1; i < indices.length; i++) {
				int index = indices[i];
				if (!(lastOfInterval == index - 1)) {
					int[] interval = new int[2];
					interval[0] = firstOfInterval;
					interval[1] = lastOfInterval;
					intervals.add(interval);
					firstOfInterval = index;
				}
				lastOfInterval = index;
			}
			int[] interval = new int[2];
			interval[0] = firstOfInterval;
			interval[1] = lastOfInterval;
			intervals.add(interval);
		}

		Iterator its = intervals.iterator();
		while (its.hasNext()) {
			int[] interval = (int[]) its.next();
			// select interval
			processInstanceIDsTable.getSelectionModel().addSelectionInterval(
					interval[0], interval[1]);
		}
	}

	private void selectInstances2(int[] indices) {

		processInstanceIDsTable2.getSelectionModel().removeSelectionInterval(0,
				processInstanceIDsTable2.getRowCount() - 1);
		HashSet intervals = new HashSet();
		if (indices.length > 0) {
			Arrays.sort(indices);
			int firstOfInterval = indices[0];
			int lastOfInterval = firstOfInterval;
			for (int i = 1; i < indices.length; i++) {
				int index = indices[i];
				if (!(lastOfInterval == index - 1)) {
					int[] interval = new int[2];
					interval[0] = firstOfInterval;
					interval[1] = lastOfInterval;
					intervals.add(interval);
					firstOfInterval = index;
				}
				lastOfInterval = index;
			}
			int[] interval = new int[2];
			interval[0] = firstOfInterval;
			interval[1] = lastOfInterval;
			intervals.add(interval);
		}

		Iterator its = intervals.iterator();
		while (its.hasNext()) {
			int[] interval = (int[]) its.next();
			// select interval
			processInstanceIDsTable2.getSelectionModel().addSelectionInterval(
					interval[0], interval[1]);
		}
	}

	/**
	 * Private data structure for the table containing the process instance IDs.
	 */
	private class ExtendedLogTable extends AbstractTableModel {

		/**
		 * Specify the headings for the columns.
		 * 
		 * @param col
		 *            The column specified.
		 * @return The heading of the respective column.
		 */
		public String getColumnName(int col) {
			// heading of the first column
			return "Process Instances";
		}

		/**
		 * Specify the number of rows.
		 * 
		 * @return The number of traces in the log.
		 */
		public int getRowCount() {
			return logReader.numberOfInstances();
		}

		/**
		 * Specifiy the number of columns.
		 * 
		 * @return Always 1.
		 */
		public int getColumnCount() {
			return 1;
		}

		/**
		 * Method to fill a certain field of the table with contents.
		 * 
		 * @param row
		 *            The specified row.
		 * @param column
		 *            The specified column.
		 * @return The content to display at the table field specified.
		 */
		public Object getValueAt(int row, int column) {
			// fill column with trace IDs
			return instanceIDs.get(row);

		}
	}

	public static JPanel createMessagePanel(String message) {
		JPanel messagePanel = new JPanel(new BorderLayout());
		JLabel messageLabel = new JLabel("     " + message + ".");
		messageLabel.setForeground(new Color(100, 100, 100));
		messagePanel.add(messageLabel, BorderLayout.CENTER);
		return messagePanel;
	}

}
