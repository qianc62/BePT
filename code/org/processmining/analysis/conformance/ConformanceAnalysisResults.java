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

package org.processmining.analysis.conformance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisGUI;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisMethod;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.DoubleClickTable;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;

/**
 * Displays the analysis results for the chosen categories in a generic way.
 * 
 * @see ConformanceAnalysisSettings
 * @see AnalysisGUI
 * 
 * @author arozinat
 */
public class ConformanceAnalysisResults extends JPanel implements Provider,
		ThreadNotificationTarget {

	/**
	 * Required for a serializable class (generated quickfix). Not directly
	 * used.
	 */
	private static final long serialVersionUID = -5958221170626249458L;

	// technical attributes
	private PetriNet inputPetriNet;
	private LogReader inputLog;
	private DiagnosticLogReader diagnosticLog;
	private HashMap mapping;
	private AnalysisConfiguration userOptions;
	private ConformanceLogReplayResult replayResult;
	private ArrayList fittingInstances;
	private FitnessAnalysisGUI fitGUI;
	private BehAppropriatenessAnalysisGUI behGUI;
	private int logReplayDepth = -1;

	// GUI attributes
	private JTabbedPane myTabbedPane = new JTabbedPane();
	private DoubleClickTable processInstanceIDsTable;
	private JSplitPane splitPane;
	private JButton updateGraphButton = new JButton("Update Results");
	private JButton selectFittingInstancesButton = new JButton(
			" Select Fitting ");
	private JButton invertSelectionButton = new JButton("Invert Selection");
	private SpinnerListener spinnerListener;
	private JSpinner percentageSelectedInstances = new JSpinner();
	private JLabel percentageSelectedInstancesLabel = new JLabel(
			"   Selected Instances in % ");
	private SpinnerNumberModel model = new SpinnerNumberModel(
			100 /* initial value */, 1 /* min */, 100
			/* max */, 1 /* step */);
	private JPanel bottomPanel = new JPanel(new BorderLayout());
	private JPanel buttonsPanel = new JPanel();
	private JPanel bottomSouthPanel = new JPanel(new BorderLayout());

	/**
	 * Builds the analysis results frame and creates the GUI for each selected
	 * main category in a separate tab.
	 * 
	 * @param analysisOptions
	 *            the configuration options
	 * @param depthValue
	 *            the user-specified value for depth restriction during log
	 *            replay (-1 means "no restriction")
	 */
	public ConformanceAnalysisResults(AnalysisConfiguration analysisOptions,
			int depthValue) {
		// ConformanceLogReplayResult logReplayResult) {
		userOptions = analysisOptions;
		Iterator<AnalysisResult> allResults = userOptions
				.getChildrenResultObjects().iterator();
		while (allResults.hasNext()) {
			AnalysisResult current = allResults.next();
			if (current instanceof ConformanceLogReplayResult) {
				replayResult = (ConformanceLogReplayResult) current;
			}
			/** TODO: retrieve further analysis results here if needed */
		}

		// log replay may have been aborted by user
		if (replayResult != null) {
			diagnosticLog = (DiagnosticLogReader) replayResult.replayedLog;
			// diagnosticPetriNet = (DiagnosticPetriNet)
			// logReplayResult.replayedPetriNet;
			inputLog = replayResult.inputLogReader;
			inputPetriNet = replayResult.inputPetriNet;
			// keep the fitting traces from the first analysis (since
			// subsequently
			// only a subset of the log might be analysed - containing only a
			// subset of
			// the fitting instances)
			fittingInstances = replayResult.getFittingLogTraces();
		}

		logReplayDepth = depthValue;

		// build GUI
		jbInit();
		// connect functionality to GUI elements
		registerGuiActionListener();
	}

	/**
	 * Construct the global user interface for the conformance check analysis.
	 */
	private void jbInit() {
		percentageSelectedInstancesLabel
				.setForeground(new Color(100, 100, 100));

		updateGraphButton
				.setToolTipText("Show all the results only for the process instances currently selected.");
		selectFittingInstancesButton
				.setToolTipText("Select all instances that are 100% compliant with the model.");
		invertSelectionButton
				.setToolTipText("Invert the current selection status.");
		percentageSelectedInstancesLabel
				.setToolTipText("<HTML>Percentage of process instances covered by the latest evaluation.<BR>"
						+ "Adjusting the spinner will select the most frequent traces in the log.</HTML>");

		// user may have aborted the log replay
		if (replayResult != null) {
			processInstanceIDsTable = new DoubleClickTable(
					new DiagnosticLogTable(), updateGraphButton);
			// select all rows as at the beginning the results count for the
			// whole log
			processInstanceIDsTable.getSelectionModel().addSelectionInterval(0,
					diagnosticLog.getSizeOfLog() - 1);

			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					new JScrollPane(processInstanceIDsTable), myTabbedPane);
			splitPane.setDividerLocation(150);
			splitPane.setOneTouchExpandable(true);
			splitPane.setDividerSize(3);

			if (replayResult.calculateFitness() == true) { // configurated
				buttonsPanel.add(selectFittingInstancesButton);
			}

			buttonsPanel.add(invertSelectionButton);
			buttonsPanel.add(percentageSelectedInstancesLabel);
			spinnerListener = new SpinnerListener();
			model.addChangeListener(spinnerListener);
			percentageSelectedInstances = new JSpinner(model);
			buttonsPanel.add(percentageSelectedInstances);
			buttonsPanel.add(updateGraphButton);
			bottomSouthPanel.add(buttonsPanel, BorderLayout.EAST);
			bottomPanel.add(bottomSouthPanel, BorderLayout.SOUTH);
		}

		// make tab for each element in the level-1 list
		ArrayList<AnalysisConfiguration> categories = userOptions
				.getChildConfigurations();
		Iterator<AnalysisConfiguration> it = categories.iterator();
		while (it.hasNext()) {
			AnalysisConfiguration currentCategory = it.next();
			if (currentCategory.isSelected()) {
				JPanel analysisGUI = currentCategory.getResultPanel();
				myTabbedPane.addTab(currentCategory.getName(), null,
						analysisGUI, currentCategory.getToolTip());

				// / TODO: find better solution for storing the analysis GUI
				// objects (needed in order to retrieve
				// current visualization options before recalculating)
				if (currentCategory.getName() == "Fitness") {
					fitGUI = (FitnessAnalysisGUI) analysisGUI;
				} else if (currentCategory.getName() == "Precision") {
					behGUI = (BehAppropriatenessAnalysisGUI) analysisGUI;
				}
			}
		}

		this.setLayout(new BorderLayout());

		// user may have aborted log replay
		if (replayResult != null) {
			this.add(splitPane, BorderLayout.CENTER);
		} else {
			this.add(myTabbedPane, BorderLayout.CENTER);
		}

		this.add(bottomPanel, BorderLayout.SOUTH);
	}

	/**
	 * Connect GUI elements like, e.g., buttons with functionality to create
	 * interaction.
	 */
	private void registerGuiActionListener() {
		// specify button action
		updateGraphButton.addActionListener(new ActionListener() {
			// specify the action to be taken when pressing the button
			public void actionPerformed(ActionEvent e) {
				updateResults();
			}
		});
		selectFittingInstancesButton.addActionListener(new ActionListener() {
			// specifies the action to be taken when pressing the button
			public void actionPerformed(ActionEvent e) {
				ArrayList newSelection = fittingInstances;
				updateSelection(newSelection);
			}
		});
		invertSelectionButton.addActionListener(new ActionListener() {
			// specify the action to be taken when pressing the button
			public void actionPerformed(ActionEvent e) {
				invertSelectionStatus();
			}
		});
	}

	/**
	 * Inverts the current selection status of the table containing the process
	 * instances. This means that instances that have been selected will result
	 * being unselected and the other way round.
	 */
	private void invertSelectionStatus() {
		ListSelectionModel selectionModel = processInstanceIDsTable
				.getSelectionModel();
		// step through the table
		for (int index = 0; index < diagnosticLog.getSizeOfLog(); index++) {
			if (selectionModel.isSelectedIndex(index) == true) {
				// if entry is currently selected --> deselect
				selectionModel.removeSelectionInterval(index, index);
			} else {
				// if entry is currently not selected --> select
				selectionModel.addSelectionInterval(index, index);
			}
		}
	}

	/**
	 * Retrieves the current selection status based on table indices.
	 * 
	 * @return int[] an array of indeces indicating those instances that are
	 *         currently selected
	 */
	private int[] getSelectionStatus() {
		return processInstanceIDsTable.getSelectedRows();
	}

	/**
	 * Retrieves the current selection status in the form of the Process
	 * instance IDs.
	 * 
	 * @return ArrayList a list of Strings containing the IDs of the selected
	 *         process instances
	 */
	private ArrayList getSelectedInstanceIDs() {
		ArrayList selectedInstanceIDs = new ArrayList();
		int[] indexArray = getSelectionStatus();
		for (int i = 0; i < indexArray.length; i++) {
			selectedInstanceIDs.add(diagnosticLog.getLogTraceIDs().get(
					indexArray[i]));
		}
		return selectedInstanceIDs;
	}

	/**
	 * Retrieves the current selection status in the form of the process
	 * instances itselves.
	 * 
	 * @return ArrayList a list of DiagnosticLogTraces
	 */
	private ArrayList getSelectedInstances() {
		ArrayList selectedInstances = new ArrayList();
		int[] indexArray = getSelectionStatus();
		for (int i = 0; i < indexArray.length; i++) {
			selectedInstances.add(diagnosticLog.getDiagnosticLogTraces().get(
					indexArray[i]));
		}
		return selectedInstances;
	}

	/**
	 * This methods updates the selection status of the list of instances. It
	 * is, e.g., called when the spinner state changes, adapting the selection
	 * status of the table to the most frequent traces determined by the given
	 * number.
	 * 
	 * @param newSelection
	 *            a list of Strings containing the IDs of those traces that
	 *            should be selected.
	 */
	private void updateSelection(ArrayList newSelection) {
		Iterator allTracesToSelect = newSelection.iterator();
		ListSelectionModel selectionModel = processInstanceIDsTable
				.getSelectionModel();
		selectionModel.clearSelection();
		while (allTracesToSelect.hasNext()) {
			String currentTraceID = (String) allTracesToSelect.next();
			int index = diagnosticLog.getIndexOfLogTrace(currentTraceID);
			selectionModel.addSelectionInterval(index, index);
		}
	}

	/**
	 * Updates both the visualization and the conformance measures by taking
	 * only the selected instances into account. If all instances have been
	 * selected, the result corresponds to the initial state.
	 */
	private void updateResults() {
		try {
			// check if selection is empty --> if so select whole log
			ListSelectionModel selectionModel = processInstanceIDsTable
					.getSelectionModel();
			if (selectionModel.isSelectionEmpty()) {
				selectionModel.addSelectionInterval(0, diagnosticLog
						.getSizeOfLog() - 1);
			}
			// invoke redraw of vizualization for selected process instances
			// only..
			ArrayList selectedInstanceIDs = getSelectedInstanceIDs();
			// ArrayList selectedInstances = getSelectedInstances();

			// update the percentage of selected process instances displayed in
			// the spinner
			int selectedPercentage = diagnosticLog
					.getPercentage(selectedInstanceIDs);
			model.setValue(new Integer(selectedPercentage));
			/**
			 * @todo anne: Try to display the percentage without triggering
			 *       automatic selection of the most frequent instances (and
			 *       then this updateSelection could be omitted)
			 */
			// reset selection status as it was automatically adapted
			// (workaround)
			spinnerListener.updateSelection(selectedInstanceIDs);

			// create new log reader from selection
			LogReader updatedLog = LogReaderFactory.createInstance(inputLog,
					getSelectionStatus());
			LogReplayAnalysisMethod newLogReplay = new LogReplayAnalysisMethod(
					inputPetriNet, updatedLog, new ConformanceMeasurer(),
					new Progress(""));
			// make sure repeated log replay is executed according to initial
			// user settings
			newLogReplay.setMaxDepth(logReplayDepth);
			AnalysisMethodExecutionThread worker = new AnalysisMethodExecutionThread(
					newLogReplay, this);
			try {
				worker.start();
			} catch (OutOfMemoryError err) {
				/**
				 * @todo anne: check whether this works + deselect category (for
				 *       results frame) since it could not be successfully
				 *       computed
				 */
				Message.add("Out of memory while analyzing");
			}

		} catch (Exception ex) {
			JOptionPane
					.showMessageDialog(this,
							"An internal error occured while\nupdating the visualization.");
			Message.add("Probably not found.\n" + ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.conformance.ThreadNotificationTarget#
	 * getAnalysisConfiguration()
	 */
	public AnalysisConfiguration getAnalysisConfiguration() {
		return userOptions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.conformance.ThreadNotificationTarget#threadDone
	 * (org.processmining.analysis.conformance.AnalysisMethodExecutionThread)
	 */
	public void threadDone(AnalysisMethodExecutionThread thread) {
		// get current tab index
		int tabIndex = myTabbedPane.getSelectedIndex();

		// build UI for every main category
		Iterator<AnalysisConfiguration> tabCategories = getAnalysisConfiguration()
				.getChildConfigurations().iterator();
		while (tabCategories.hasNext()) {
			// TODO - think about how this can be done without this string
			// comparison
			AnalysisConfiguration current = tabCategories.next();
			if (current.getName() == "Fitness" && current.isSelected()) {
				// get current fitness visualization options first
				boolean tokenCounterOption = fitGUI.getTokenCounterOption();
				boolean failedTransitionsOption = fitGUI
						.getFailedTransitionsOption();
				boolean remainingTransitionsOption = fitGUI
						.getRemainingTransitionsOption();
				boolean pathCoverageOption = fitGUI.getPathCoverageOption();
				boolean passedEdgesOption = fitGUI.getPassedEdgesOption();
				boolean failedEventsOption = fitGUI.getFailedEventsOption();
				boolean visualizationTypeOption = fitGUI
						.getVisualizationTypeOption();

				Set<AnalysisResult> fitnessResultObjects = current
						.getResultObjects();
				current.setResultPanel(new FitnessAnalysisGUI(
						fitnessResultObjects, tokenCounterOption,
						failedTransitionsOption, remainingTransitionsOption,
						pathCoverageOption, passedEdgesOption,
						failedEventsOption, visualizationTypeOption));
			}
			if (current.getName() == "Precision" && current.isSelected()) {
				// get current behavioral appropriateness visualization options
				boolean alwaysPrecedes = behGUI.getAlwaysPrecedesOption();
				boolean neverPrecedes = behGUI.getNeverPrecedesOption();
				boolean alwaysFollows = behGUI.getAlwaysFollowsOption();
				boolean neverFollows = behGUI.getNeverFollowsCoverageOption();

				Set<AnalysisResult> behApprResultObjects = current
						.getResultObjects();
				current.setResultPanel(new BehAppropriatenessAnalysisGUI(
						behApprResultObjects, alwaysPrecedes, neverPrecedes,
						alwaysFollows, neverFollows));
			}
			// don't rebuild structural appropriateness tab as
			// only update those GUIs that were affected by log replay need to
			// be reconstructed
		}

		Iterator<AnalysisResult> allResults = userOptions
				.getChildrenResultObjects().iterator();
		while (allResults.hasNext()) {
			AnalysisResult current = allResults.next();
			if (current instanceof ConformanceLogReplayResult) {
				replayResult = (ConformanceLogReplayResult) current;
			}
			/** TODO: retrieve further analysis results here if needed */
		}

		// update the GUI tabs with the new analysis results
		myTabbedPane.removeAll();
		// make tab for each element in the level-1 list
		ArrayList<AnalysisConfiguration> categories = userOptions
				.getChildConfigurations();
		Iterator<AnalysisConfiguration> it = categories.iterator();
		while (it.hasNext()) {
			AnalysisConfiguration currentCategory = it.next();
			if (currentCategory.isSelected()) {
				JPanel analysisGUI = currentCategory.getResultPanel();
				myTabbedPane.addTab(currentCategory.getName(), null,
						analysisGUI, currentCategory.getToolTip());

				// TODO: find better solution for storing the analysis GUI
				// objects (needed in order to retrieve
				// current visualization options before recalculating)
				if (currentCategory.getName() == "Fitness") {
					fitGUI = (FitnessAnalysisGUI) analysisGUI;
				} else if (currentCategory.getName() == "Precision") {
					behGUI = (BehAppropriatenessAnalysisGUI) analysisGUI;
				}
			}
		}

		myTabbedPane.validate();
		myTabbedPane.repaint();
		// open the tab that was selected before the recalculation
		myTabbedPane.setSelectedIndex(tabIndex);
	}

	// ////////// INTERFACE IMPLEMENTATION RELATED METHODS
	// //////////////////////

	/**
	 * Specifies provided objects of the analysis that can be further used to,
	 * e.g., export an item.
	 * 
	 * @return the provided objects offered by the {@link AnalysisGUI
	 *         AnalysisGUI} that is currently visible
	 */
	public ProvidedObject[] getProvidedObjects() {
		try {
			ArrayList<ProvidedObject> objects = new ArrayList<ProvidedObject>();
			// offer input Petri net
			objects.add(new ProvidedObject("Initial Petri net",
					new Object[] { inputPetriNet }));
			// offer input log reader
			objects.add(new ProvidedObject("Whole Log",
					new Object[] { inputLog }));
			// offer selected subset of the log
			if (processInstanceIDsTable != null
					&& processInstanceIDsTable.getSelectionModel()
							.isSelectionEmpty() == false) {
				// if provided objects are asked before the GUI is actually
				// created the log selection cannot be provided yet
				// furthermore the log selection is only offered if there is
				// something selected
				objects.add(new ProvidedObject("Log Selection",
						new Object[] { LogReaderFactory.createInstance(
								inputLog, getSelectionStatus()) }));
			}
			// offer diagnostic visualizations offered by the various analysis
			// tabs
			AnalysisGUI currentTab = (AnalysisGUI) myTabbedPane
					.getSelectedComponent();
			// get the provided objects from the currently selected tab
			// component
			ProvidedObject[] tabObjects = currentTab.getProvidedObjects();
			for (int i = 0; i < tabObjects.length; i++) {
				objects.add(tabObjects[i]);
			}

			int numberOfProvidedObjects = objects.size();
			Iterator<ProvidedObject> it = objects.iterator();
			ProvidedObject[] result = new ProvidedObject[numberOfProvidedObjects];
			for (int i = 0; i < numberOfProvidedObjects; i++) {
				result[i] = it.next();
			}
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// ///////// INTERNAL CLASS DEFINITIONS
	// //////////////////////////////////////

	/**
	 * Change listener for the spinner representing the current percentage of
	 * selected process instances.
	 */
	public class SpinnerListener implements ChangeListener {

		/**
		 * The method automatically invoked when changing the spinner status.
		 * 
		 * @param e
		 *            The passed change event (not used).
		 */
		public void stateChanged(ChangeEvent e) {
			ArrayList newSelection = diagnosticLog.getMostFrequentTraces(model
					.getNumber().intValue());
			updateSelection(newSelection);
		}

		/**
		 * This methods updates the selection status of the list of instances.
		 * It is, e.g., called when the spinner state changes, adapting the
		 * selection status of the table to the most frequent traces determined
		 * by the given number.
		 * 
		 * @param newSelection
		 *            a list of Strings containing the IDs of those traces that
		 *            should be selected.
		 */
		public void updateSelection(ArrayList newSelection) {
			Iterator allTracesToSelect = newSelection.iterator();
			ListSelectionModel selectionModel = processInstanceIDsTable
					.getSelectionModel();
			selectionModel.clearSelection();
			while (allTracesToSelect.hasNext()) {
				String currentTraceID = (String) allTracesToSelect.next();
				int index = diagnosticLog.getIndexOfLogTrace(currentTraceID);
				selectionModel.addSelectionInterval(index, index);
			}
		}
	}

	/**
	 * Private data structure for the table containing the process instance IDs.
	 */
	private class DiagnosticLogTable extends AbstractTableModel {

		/**
		 * Required for a serializable class (generated quickfix). Not directly
		 * used.
		 */
		private static final long serialVersionUID = 2038270325258153706L;

		/**
		 * Specify the headings for the columns.
		 * 
		 * @param col
		 *            The column specified.
		 * @return The heading of the respective column.
		 */
		public String getColumnName(int col) {
			// heading of first column
			if (col == 0) {
				return "#";
			}
			// heading of second column
			else {
				return "Log Traces";
			}
		}

		/**
		 * Specify the number of rows.
		 * 
		 * @return The number of traces in the log.
		 */
		public int getRowCount() {
			return diagnosticLog.getSizeOfLog();
		}

		/**
		 * Specifiy the number of columns.
		 * 
		 * @return Always 2.
		 */
		public int getColumnCount() {
			return 2;
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
			// fill first column
			if (column == 0) {
				// retrieve the trace itself first
				DiagnosticLogTrace trace = (DiagnosticLogTrace) diagnosticLog
						.getLogTrace((String) diagnosticLog.getLogTraceIDs()
								.get(row));
				return String.valueOf(trace.getNumberOfProcessInstances());
			}
			// fill second column
			else {
				return diagnosticLog.getLogTraceIDs().get(row);
			}
		}
	}
}
