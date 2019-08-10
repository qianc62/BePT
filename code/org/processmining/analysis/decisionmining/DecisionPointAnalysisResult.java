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

package org.processmining.analysis.decisionmining;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.ProcessInstanceVisualization;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.hlprocess.visualization.HLVisualization;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.DoubleClickTable;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GuiPropertyStringTextarea;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.mxml.writing.persistency.LogPersistencyStream;

/**
 * Displays the results of some decision point analysis.
 * 
 * @author Anne Rozinat (a.rozinat@tm.tue.nl)
 */
public class DecisionPointAnalysisResult extends JPanel implements Provider {

	/**
	 * Required for a serializable class (generated quickfix). Not directly
	 * used.
	 */
	private static final long serialVersionUID = -8434962856665878580L;

	/**
	 * If the decision miner is used multiple times, each time the simulation
	 * model to be provided will have an incremented number (in order to
	 * distinguish them later when they e.g., need to be joined)
	 */
	private static int simulationModelCounter = 0;

	// user interface related attributes
	private JTabbedPane tabPane = new JTabbedPane();
	private JPanel myModelViewPanel = new JPanel(new BorderLayout());
	private JPanel myDecisionTreePanel = new JPanel(new BorderLayout());
	private JPanel myEvaluationPanel = new JPanel(new BorderLayout());
	private JPanel myLogViewPanel = new JPanel();
	private JPanel myAttributesViewPanel = new JPanel();
	private JPanel myLogVisualizationPanel = new JPanel();
	private JPanel myAlgorithmPanel = new JPanel();
	private JPanel mySimModelPanel = new JPanel(new BorderLayout());
	private JButton updateResultsButton = new JButton("Update results");
	private JButton updateViewButton = new JButton();
	private JButton splitLoopsButton = new JButton("Split loops");
	private DoubleClickTable decisionPointTable;
	private JTree logTree;

	// technical attributes
	private DecisionPointBuilder decisionPointBuilder;
	private DecisionMiningLogReader decisionMiningLog;
	private DecisionAnalyser analyser = new J48Analyser();
	private HLPetriNet highLevelPN;
	private DecisionPointPlusLog decisionPointAndLog;

	/**
	 * Builds the GUI and registers GUI actions.
	 */
	public DecisionPointAnalysisResult(ModelGraph model, LogReader log) {
		this(new HLPetriNet((PetriNet) model), (PetriNet) model, log);
	}

	public DecisionPointAnalysisResult(HLPetriNet hlPetriNet, PetriNet model,
			LogReader log) {
		// increment static sim model counter for this plugin
		simulationModelCounter = simulationModelCounter + 1;
		// initialize the simulation model (assumption: model type is Petri net)
		highLevelPN = hlPetriNet;
		// the highlevelPN covers the data perspective and (some part of) the
		// choices perspective
		highLevelPN.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.DATA_INITIAL_VAL);
		highLevelPN.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.DATA_VALUE_RANGE);
		highLevelPN.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.DATA_AT_TASKS);
		highLevelPN.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.CHOICE_DATA);
		// set the name of the simulation model
		highLevelPN.getHLProcess().getGlobalInfo().setName(
				"Data Simulation Model No." + simulationModelCounter);
		// determine decision points for the given model (assumption: model type
		// is Petri net)
		decisionPointBuilder = new DecisionPointBuilderPetriNet(model, this);
		// classify process instances with respect to their decisions
		decisionMiningLog = new DecisionMiningLogReader(log, highLevelPN);
		decisionMiningLog.classifyLog(decisionPointBuilder.getDecisionPoints());

		// semantically initialize the decision point contexts
		Iterator allDecisionPoints = decisionPointBuilder.getDecisionPoints()
				.iterator();
		while (allDecisionPoints.hasNext()) {
			DecisionPoint currentDP = (DecisionPoint) allDecisionPoints.next();
			// todo: call some init method instead
			currentDP.getContext().getAttributesViewPanel();
		}

		// create data attributes for the simulation model
		decisionMiningLog.addSimulationModelAttributes(highLevelPN);

		// build GUI
		jbInit();
		registerGuiActionListener();
		decisionPointAndLog = new DecisionPointPlusLog(
				getCurrentlySelectedDecisionPoint(), log);

		// / PLUGIN TEST START
		Message.add("<DecisionPointAnalysis>", Message.TEST);
		List<DecisionPoint> decisionPoints = decisionPointBuilder
				.getDecisionPoints();
		Message.add("No. of decision points: " + decisionPoints.size(),
				Message.TEST);
		Iterator<DecisionPoint> it = decisionPoints.iterator();
		while (it.hasNext()) {
			DecisionPoint currentDP = it.next();
			Message.add("Decision point: " + currentDP.myNode.getIdentifier(),
					Message.TEST);
			Iterator<DecisionCategory> branches = currentDP.getTargetConcept()
					.iterator();
			while (branches.hasNext()) {
				DecisionCategory currentBranch = branches.next();
				Message.add("Alternative Branch: "
						+ currentBranch.getBranchSpecification(), Message.TEST);
			}
		}
		Message.add("No. of attributes: "
				+ decisionMiningLog.getAttributesForWholeLog().size(),
				Message.TEST);
		Message.add("</DecisionPointAnalysis>", Message.TEST);
		// PLUGIN TEST END
	}

	/**
	 * Retrieves the decision point aware model representation for this analysis
	 * result.
	 * 
	 * @return the process model being analyzed
	 */
	public DecisionPointBuilder getModelAnalyzer() {
		return decisionPointBuilder;
	}

	/**
	 * Retrieves the decision point aware log representation for this analysis
	 * result.
	 * 
	 * @return the event log being analyzed
	 */
	public DecisionMiningLogReader getLogAnalyzer() {
		return decisionMiningLog;
	}

	/**
	 * Builds the GUI of the result frame.
	 */
	private void jbInit() {
		decisionPointTable = new DoubleClickTable(new DecisionPointTable(),
				updateViewButton);

		// build model tab views
		tabPane.addTab("Model", null, myModelViewPanel,
				"View decision point in process model");

		// treat the case of models not containing any choices
		if (decisionPointTable.getRowCount() == 0) {
			// add user help text at the top of the tab
			String description = new String(
					"No decision points have been found in the model. Therefore no decision point analysis can be carried out. "
							+ "Nevertheless, a simulation model including the found data attributes is provided to the framework.");
			GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
					description);
			myModelViewPanel.add(helpText.getPropertyPanel(),
					BorderLayout.CENTER);
			updateResultsButton.setEnabled(false);
			splitLoopsButton.setEnabled(false);
		} else {
			// select first decision point (such that always one decision point
			// is selected)
			decisionPointTable.getSelectionModel().addSelectionInterval(0, 0);
			// build model view content
			myModelViewPanel.add(getCurrentlySelectedDecisionPoint()
					.getContext().getModelViewPanel());
			// build log view content
			buildLogTreeView();
			// build attributes view content
			myAttributesViewPanel.add(getCurrentlySelectedDecisionPoint()
					.getContext().getAttributesViewPanel());
			// build result view content
			myDecisionTreePanel.add(getCurrentlySelectedDecisionPoint()
					.getContext().getResultViewPanel());
			// build evaluatin view content
			myEvaluationPanel.add(getCurrentlySelectedDecisionPoint()
					.getContext().getEvaluationViewPanel());
			// build algorithm view content
			buildAlgorithmView();

			// only add the tabs if there are decision points in the model
			tabPane.addTab("Attributes", null, new JScrollPane(
					myAttributesViewPanel), "Select attributes to incorporate");
			tabPane.addTab("Log", null, myLogViewPanel,
					"Access log instances according to decisions made");
			tabPane
					.addTab("Algorithm", null, myAlgorithmPanel,
							"Modify the parameters available for the used decision tree algorithm");
			tabPane
					.addTab("Decision Tree / Rules", null, myDecisionTreePanel,
							"View the discovered decision rules/tree for decision point");
			tabPane.addTab("Evaluation", null, myEvaluationPanel,
					"View the evaluation of the result for decision point");
		}

		// build enhanced model view anyway
		buildSimModelView();
		tabPane.addTab("Result", null, mySimModelPanel,
				"View the process model with integrated data perspective");

		updateResultsButton
				.setToolTipText("Restart the analysis for the current settings");
		splitLoopsButton
				.setToolTipText("Split up the log into several loop instances for the current decision point");

		JPanel updateButtonPanel = new JPanel();
		updateButtonPanel.setLayout(new BoxLayout(updateButtonPanel,
				BoxLayout.LINE_AXIS));
		updateButtonPanel.add(updateResultsButton);
		updateButtonPanel.add(Box.createHorizontalGlue());

		JPanel exportButtonPanel = new JPanel();
		exportButtonPanel.setLayout(new BoxLayout(exportButtonPanel,
				BoxLayout.LINE_AXIS));
		exportButtonPanel.add(splitLoopsButton);
		exportButtonPanel.add(Box.createHorizontalGlue());

		// make buttons equal width
		Dimension updateButtonDimension = updateResultsButton
				.getPreferredSize();
		Dimension exportButtonDimension = splitLoopsButton.getPreferredSize();
		exportButtonDimension.width = updateButtonDimension.width;
		splitLoopsButton.setPreferredSize(exportButtonDimension);

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
		leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		leftPanel.add(new JScrollPane(decisionPointTable));
		leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		leftPanel.add(updateButtonPanel);
		leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		leftPanel.add(exportButtonPanel);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPanel, tabPane);
		splitPane.setDividerLocation(150);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(3);

		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(splitPane);
	}

	/**
	 * Helper method building the tree log view (decision point perspective).
	 */
	private void buildLogTreeView() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(
				"Decision Points");
		Iterator allDecisionPoints = decisionPointBuilder.getDecisionPoints()
				.iterator();
		while (allDecisionPoints.hasNext()) {
			DecisionPoint currentDP = (DecisionPoint) allDecisionPoints.next();
			DefaultMutableTreeNode dPNode = new DefaultMutableTreeNode(
					currentDP.getName());
			top.add(dPNode);
			Iterator allCategories = currentDP.getTargetConcept().iterator();
			while (allCategories.hasNext()) {
				DecisionCategory currentBranch = (DecisionCategory) allCategories
						.next();
				// automatically displays toString() result for contained
				// object!
				DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode(
						currentBranch);
				dPNode.add(branchNode);
				Iterator recordedTraces = decisionMiningLog
						.getTracesInCategory(currentBranch).iterator();
				while (recordedTraces.hasNext()) {
					DecisionMiningLogTrace currentTrace = (DecisionMiningLogTrace) recordedTraces
							.next();
					DefaultMutableTreeNode traceNode = new DefaultMutableTreeNode(
							currentTrace);
					branchNode.add(traceNode);
				}
			}
		}
		logTree = new JTree(top);
		// only one node at a time may be selected
		logTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		JScrollPane treeView = new JScrollPane(logTree);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				treeView, myLogVisualizationPanel);
		splitPane.setDividerLocation(150);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(3);
		myLogViewPanel.setLayout(new BorderLayout());
		myLogViewPanel.add(splitPane, BorderLayout.CENTER);
	}

	/**
	 * Helper method building the tree log view (decision point perspective).
	 */
	private void buildAlgorithmView() {

		myAlgorithmPanel.setLayout(new BoxLayout(myAlgorithmPanel,
				BoxLayout.LINE_AXIS));

		JPanel algorithmContentPanel = new JPanel();
		algorithmContentPanel.setLayout(new BoxLayout(algorithmContentPanel,
				BoxLayout.PAGE_AXIS));
		algorithmContentPanel.add(Box.createVerticalGlue());

		// create algorithm selection area
		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new BoxLayout(selectionPanel,
				BoxLayout.LINE_AXIS));
		JLabel algorithmLabel = new JLabel("Use algorithm: ");
		algorithmLabel.setForeground(new Color(100, 100, 100));
		JComboBox algorithmComboBox = new JComboBox();
		algorithmComboBox.addItem(analyser);
		algorithmComboBox.setMaximumSize(algorithmComboBox.getPreferredSize());
		algorithmComboBox.setMinimumSize(algorithmComboBox.getPreferredSize());
		algorithmComboBox.addActionListener(new ActionListener() {
			// specify action when the attribute selection scope is changed
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				DecisionAnalyser newAlgorithm = (DecisionAnalyser) cb
						.getSelectedItem();
				// todo: update description and parameters for the new algoritm
			}
		});
		selectionPanel.add(algorithmLabel);
		selectionPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		selectionPanel.add(algorithmComboBox);
		selectionPanel.add(Box.createHorizontalGlue());
		algorithmContentPanel.add(selectionPanel);
		algorithmContentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		// create algorithm description area
		JPanel descriptionPanel = new JPanel();
		descriptionPanel.setLayout(new BoxLayout(descriptionPanel,
				BoxLayout.LINE_AXIS));
		JLabel descriptionLabel = new JLabel("Algorithm description: ");
		descriptionLabel.setForeground(new Color(100, 100, 100));
		JLabel descriptionContent = new JLabel(analyser.getDescription());
		descriptionPanel.add(descriptionLabel);
		descriptionPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		descriptionPanel.add(descriptionContent);
		descriptionPanel.add(Box.createHorizontalGlue());
		algorithmContentPanel.add(descriptionPanel);
		algorithmContentPanel.add(Box.createVerticalGlue());

		// create algorithm parameters
		JPanel parametersLabelPanel = new JPanel();
		parametersLabelPanel.setLayout(new BoxLayout(parametersLabelPanel,
				BoxLayout.LINE_AXIS));
		JLabel parametersLabel = new JLabel(
				"The following parameters are available for the selected algorithm:");
		parametersLabel.setForeground(new Color(100, 100, 100));
		parametersLabelPanel.add(parametersLabel);
		parametersLabelPanel.add(Box.createHorizontalGlue());
		algorithmContentPanel.add(parametersLabelPanel);
		algorithmContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		algorithmContentPanel.add(analyser.getParametersPanel());
		algorithmContentPanel.add(Box.createVerticalGlue());

		myAlgorithmPanel.add(Box.createHorizontalGlue());
		myAlgorithmPanel.add(algorithmContentPanel);
		myAlgorithmPanel.add(Box.createHorizontalGlue());
		myAlgorithmPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30,
				30));
	}

	/**
	 * Helper method building the simulation model view.
	 */
	private void buildSimModelView() {
		HashSet<HLTypes.Perspective> perspectives = new HashSet<HLTypes.Perspective>();
		perspectives.add(HLTypes.Perspective.CHOICE_DATA);
		perspectives.add(HLTypes.Perspective.DATA_AT_TASKS);
		HLVisualization viz = new HLVisualization(highLevelPN
				.getVisualization(perspectives));
		mySimModelPanel.add(viz.getPanel(), BorderLayout.CENTER);
	}

	/**
	 * Connects GUI elements like, e.g., buttons with functionality to create
	 * interaction.
	 */
	private void registerGuiActionListener() {
		updateResultsButton.addActionListener(new ActionListener() {
			// specify the action to be taken when pressing the "start analysis"
			// button
			public void actionPerformed(ActionEvent e) {
				updateResults();
			}
		});
		updateViewButton.addActionListener(new ActionListener() {
			// specifies the action to be taken when a decision point has been
			// double-clicked
			public void actionPerformed(ActionEvent e) {
				updateViews();
				decisionPointAndLog
						.setDecisionPoint(getCurrentlySelectedDecisionPoint());
			}
		});
		splitLoopsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveLoopSeparatedLog();
			}
		});
		// may be null as it is only built if there is at least one decision
		// point contained in the model
		if (logTree != null) {
			logTree.addTreeSelectionListener(new TreeSelectionListener() {
				// specify the action when a tree node is selected
				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) logTree
							.getLastSelectedPathComponent();

					if (node == null) {
						return;
					}

					Object nodeInfo = node.getUserObject();
					if (node.isLeaf()
							&& nodeInfo.getClass().equals(
									DecisionMiningLogTrace.class)) {
						DecisionMiningLogTrace trace = (DecisionMiningLogTrace) nodeInfo;
						// show visualization of selected trace
						myLogVisualizationPanel.removeAll();
						myLogVisualizationPanel.setLayout(new BorderLayout());
						myLogVisualizationPanel.add(
								(new ProcessInstanceVisualization(trace
										.getProcessInstance()))
										.getGrappaVisualization(),
								BorderLayout.CENTER);
						myLogVisualizationPanel.validate();
						myLogVisualizationPanel.repaint();
					}
					// else do nothing
				}
			});
		}
	}

	private void saveLoopSeparatedLog() {
		String decisionPointName = decisionPointAndLog.getDecisionPoint()
				.getName();
		LogReader splitLog = decisionPointAndLog.getLoopSeparatedLog();
		// actually save to file
		JFileChooser saveDialog = new JFileChooser();
		saveDialog.setSelectedFile(new File(decisionPointName
				+ "_Loop-splitLog.mxml.gz"));
		if (saveDialog.showSaveDialog(MainUI.getInstance()) == JFileChooser.APPROVE_OPTION) {
			try {
				File outputFile = saveDialog.getSelectedFile();
				FileOutputStream output = new FileOutputStream(outputFile);
				BufferedOutputStream out = new BufferedOutputStream(
						new GZIPOutputStream(output));
				LogPersistencyStream persistency = new LogPersistencyStream(
						out, false);
				Process process = splitLog.getProcess(0);
				ProcessInstance instance = null;
				AuditTrailEntryList ateList = null;
				String name = process.getName();
				if (name == null || name.length() == 0) {
					name = "UnnamedProcess";
				}
				String description = process.getDescription();
				if (description == null || description.length() == 0) {
					description = name + " exported by MXMLib @ P-stable";
				}
				String source = splitLog.getLogSummary().getSource().getName();
				if (source == null || source.length() == 0) {
					source = "UnknownSource";
				}
				persistency.startLogfile(name, description, source);
				for (int i = 0; i < splitLog.numberOfProcesses(); i++) {
					process = splitLog.getProcess(i);
					name = process.getName();
					if (name == null || name.length() == 0) {
						name = "UnnamedProcess";
					}
					description = process.getDescription();
					if (description == null || description.length() == 0) {
						description = name + " exported by MXMLib @ P-stable";
					}
					persistency.startProcess(name, description, process
							.getAttributes());
					for (int j = 0; j < process.size(); j++) {
						instance = process.getInstance(j);
						name = instance.getName();
						if (name == null || name.length() == 0) {
							name = "UnnamedProcessInstance";
						}
						description = instance.getDescription();
						if (description == null || description.length() == 0) {
							description = name
									+ " exported by MXMLib @ P-stable";
						}
						ateList = instance.getAuditTrailEntryList();
						persistency.startProcessInstance(name, description,
								instance.getAttributes());
						for (int k = 0; k < ateList.size(); k++) {
							persistency
									.addAuditTrailEntry(promAte2mxmlibAte(ateList
											.get(k)));
						}
						persistency.endProcessInstance();
					}
					persistency.endProcess();
				}
				// clean up
				persistency.endLogfile();
				persistency.finish();
				JOptionPane.showMessageDialog(MainUI.getInstance(),
						"Loop-split log has been saved\nto file!",
						"Loop-split log saved.",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	protected org.processmining.lib.mxml.AuditTrailEntry promAte2mxmlibAte(
			AuditTrailEntry promAte) {
		org.processmining.lib.mxml.AuditTrailEntry mxmlibAte = new org.processmining.lib.mxml.AuditTrailEntry();
		mxmlibAte.setWorkflowModelElement(promAte.getElement());
		mxmlibAte.setEventType(EventType.getType(promAte.getType()));
		mxmlibAte.setOriginator(promAte.getOriginator());
		if (promAte.getTimestamp() != null) {
			mxmlibAte.setTimestamp(promAte.getTimestamp());
		}
		mxmlibAte.setAttributes(promAte.getAttributes());
		return mxmlibAte;
	}

	/**
	 * Updates the view for the double clicked decision point in the list.
	 */
	private void updateViews() {
		// determine current decision point (only one can be selected as it has
		// been doubleclicked)
		DecisionPoint selectedDecisionPoint = getCurrentlySelectedDecisionPoint();

		// display model tab for the new decision point
		myModelViewPanel.removeAll();
		myModelViewPanel.add(selectedDecisionPoint.getContext()
				.getModelViewPanel());
		myModelViewPanel.validate();
		myModelViewPanel.repaint();

		// display attributes tab for the new decision point
		myAttributesViewPanel.removeAll();
		myAttributesViewPanel.add(selectedDecisionPoint.getContext()
				.getAttributesViewPanel());
		myAttributesViewPanel.validate();
		myAttributesViewPanel.repaint();

		// display evaluation of result for the new decision point
		myEvaluationPanel.removeAll();
		myEvaluationPanel.add(selectedDecisionPoint.getContext()
				.getEvaluationViewPanel());
		myEvaluationPanel.validate();
		myEvaluationPanel.repaint();

		// display result tab for the new decision point
		myDecisionTreePanel.removeAll();
		JPanel resultPanel = selectedDecisionPoint.getContext()
				.getResultViewPanel();
		myDecisionTreePanel.add(resultPanel);
		myDecisionTreePanel.validate();
		myDecisionTreePanel.repaint();
		analyser.redrawResultVisualization(resultPanel);
	}

	/**
	 * Invokes a new analysis action according to the current settings (e.g.,
	 * regarding selected attributes for each decision point and the global
	 * algorithm parameters).
	 */
	private void updateResults() {

		analyser.analyse(decisionPointBuilder.getDecisionPoints(),
				decisionMiningLog, highLevelPN);

		// determine current decision point (only one can be selected as it has
		// been doubleclicked)
		DecisionPoint selectedDecisionPoint = getCurrentlySelectedDecisionPoint();

		// update result tab for the current decision point
		myDecisionTreePanel.removeAll();
		JPanel resultPanel = selectedDecisionPoint.getContext()
				.getResultViewPanel();
		myDecisionTreePanel.add(resultPanel);
		myDecisionTreePanel.validate();
		myDecisionTreePanel.repaint();
		analyser.redrawResultVisualization(resultPanel);

		// display evaluation of result for the new decision point
		myEvaluationPanel.removeAll();
		myEvaluationPanel.add(selectedDecisionPoint.getContext()
				.getEvaluationViewPanel());
		myEvaluationPanel.validate();
		myEvaluationPanel.repaint();

		// display enhanced model (simulation model view)
		mySimModelPanel.removeAll();
		buildSimModelView();
		mySimModelPanel.validate();
		mySimModelPanel.repaint();
	}

	/**
	 * Retrieves the decision point which is currently selected. There is only
	 * one decision point which can be selected at one point in time (in the
	 * double click table).
	 * 
	 * @return the current decision point
	 */
	private DecisionPoint getCurrentlySelectedDecisionPoint() {
		// determine current decision point (only one can be selected as it has
		// been doubleclicked)
		int[] indexArray = decisionPointTable.getSelectedRows();
		DecisionPoint selectedDecisionPoint = decisionPointBuilder
				.getDecisionPointAt(indexArray[0]);
		return selectedDecisionPoint;
	}

	// ////////// INTERFACE IMPLEMENTATION RELATED METHODS
	// //////////////////////

	/**
	 * Specifies provided objects of the analysis that can be further used to,
	 * e.g., export an item.
	 * 
	 * @return the provided objects offered by the plugin
	 */
	public ProvidedObject[] getProvidedObjects() {
		if (highLevelPN != null) {
			try {
				ProvidedObject[] objects = { new ProvidedObject(
						"Data Simulation Model No." + simulationModelCounter,
						new Object[] { highLevelPN }) };
				return objects;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return new ProvidedObject[0];
		}
	}

	// ////////// PRIVATE CLASSES
	// ///////////////////////////////////////////////

	/**
	 * Private data structure for the table containing the decision points.
	 */
	private class DecisionPointTable extends AbstractTableModel {

		/**
		 * Required for a serializable class (generated quickfix). Not directly
		 * used.
		 */
		private static final long serialVersionUID = -6165029231436957878L;

		/**
		 * Specify the headings for the columns.
		 * 
		 * @param col
		 *            The column specified.
		 * @return The heading of the respective column.
		 */
		public String getColumnName(int col) {
			// heading of single column
			return "Decision points";
		}

		/**
		 * Specify the number of rows.
		 * 
		 * @return The number of traces in the log.
		 */
		public int getRowCount() {
			return decisionPointBuilder.getNumberOfDecisionPoints();
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
			// fill in name of the decision point
			return decisionPointBuilder.getDecisionPointAt(row).getName();
		}
	}
}
