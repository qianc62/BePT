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

/*
 * Copyright (c) 2006 Eindhoven University of Technology
 * All rights reserved.
 */

package org.processmining.analysis.performance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.models.Bag;
import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLCondition;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLDistribution;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisGUI;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.DoubleClickTable;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;

import att.grappa.Edge;
import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaBox;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Node;
import att.grappa.Subgraph;

/**
 * This class contains the GUI in which the the results of performance analysis
 * with Petri net is displayed.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class PerformanceAnalysisGUI extends AnalysisGUI implements Provider {

	// technical attributes
	private PetriNet inputPetriNet;
	private LogReader inputLog;
	private ExtendedLogReader extendedLog;
	private ExtendedPetriNet extendedPetriNet;
	private HashMap mapping;

	// petri net with simulation related information
	private HLPetriNet highLevelPN;

	/**
	 * If this plugin is used multiple times, each time the simulation model to
	 * be provided will have an incremented number (in order to distinguish them
	 * later when they e.g., need to be joined)
	 */
	private static int simulationModelCounter = 0;

	// For purpose of keeping track of which elements have been selected on the
	// grappaPanel
	private Element elt1 = null;
	private Element elt2 = null;

	private PerformanceLogReplayResult replayResult;

	// general settings
	private boolean manualSettings = false;
	private long timeDivider = 1000;
	private String timeSort = "seconds";
	private int decimalPlaces = 2;
	private ArrayList bounds;
	private ArrayList levelColors;
	private double fastestProcessPercentage = 25.0;
	private double slowestProcessPercentage = 25.0;
	private double fastestPlacePercentage = 25.0;
	private double slowestPlacePercentage = 25.0;
	private double fastestBetweenPercentage = 25.0;
	private double slowestBetweenPercentage = 25.0;
	private double fastestActivityPercentage = 25.0;
	private double slowestActivityPercentage = 25.0;
	// advanced settings;
	private int[] advancedSettings = { 1, 1, 1, 1, 0 };

	// map containing all transitions and places in selectionBox1
	private HashMap boxMap = new HashMap();
	// set of procss instances that have failed
	private HashSet failedInstances = new HashSet();
	// GUI components
	private JScrollPane tableContainer;
	private JScrollPane modelContainer;
	private JScrollPane metricsBottomContainer;
	private JScrollPane processContainer;
	private JPanel westPanel = new JPanel();
	private JPanel eastPanel = new JPanel();
	private JPanel levelPanel = new JPanel();
	private JPanel highPanel = new JPanel();
	private JPanel mediumPanel = new JPanel();
	private JPanel lowPanel = new JPanel();
	private JPanel metricsBottomPanel = new JPanel();
	private JPanel tablePanel = new JPanel(new BorderLayout());
	private JPanel selectionPanel = new JPanel();
	private JPanel bottomPanel = new JPanel();
	private JPanel centerPanel = new JPanel();
	private JPanel processTablePanel = new JPanel();
	private JPanel piButtonsPanel = new JPanel();
	private ModelGraphPanel grappaPanel;

	// waiting time level labels
	private JLabel waitingLabel = new JLabel("Waiting time:");
	private JLabel highLabel = new JLabel("High");
	private JLabel mediumLabel = new JLabel("Medium");
	private JLabel lowLabel = new JLabel("Low");
	// selection labels
	private JLabel selectLabel = new JLabel("Selected:");
	private JLabel andLabel = new JLabel("and:");
	// place labels
	private JLabel titleLabel = new JLabel(
			"Performance information of the selected" + " place:");
	private JLabel numberObjectLabel = new JLabel("Frequency: ");
	private JLabel freqObjectLabel = new JLabel();
	private JLabel arrivalPlaceLabel = new JLabel("Arrival rate: ");
	private JLabel ratePlaceLabel = new JLabel();

	// process labels
	private JLabel processLabel = new JLabel("Process information:");
	private JLabel numberProcessLabel = new JLabel("Total number selected:");
	private JLabel casesProcessLabel = new JLabel("-");
	private JLabel properLabel = new JLabel("Number fitting:");
	private JLabel completedLabel = new JLabel("-");
	private JLabel arrivalProcessLabel = new JLabel("Arrival rate of selected:");
	private JLabel rateProcessLabel = new JLabel("-");

	// buttons
	private JButton updateButton = new JButton("Update");
	private JButton invertButton = new JButton("Invert Selection");
	private JButton changeSettingsButton = new JButton("Settings");
	private JButton changePercentagesButton = new JButton("Change Percentages");
	private JButton changeProcessPercButton = new JButton("<html>&nbsp &nbsp "
			+ "Change<br> Percentages</html>");
	private JButton exportButton = new JButton("Export Time Metrics");
	private JButton exportProcessButton = new JButton(
			"<html>&nbsp &nbsp &nbsp " + "Export <br> Time-Metrics</html>");
	// tables
	private DoubleClickTable processInstanceIDsTable;
	private JTable placeTable = new JTable();
	private JTable transitionTable = new JTable();
	private JTable processTable = new JTable();
	private JTable activityTable = new JTable();

	// splitPanes
	private JSplitPane leftSplitPane;
	private JSplitPane rightSplitPane;

	// comboboxes
	private JComboBox sb1;
	private JComboBox sb2;

	/**
	 * Calls the constructor of its superclass and initializes variables and the
	 * UI
	 * 
	 * @param analysisResults
	 *            HashSet: contains the results of log replay
	 * @param manual
	 *            boolean: true if the user has manually selected performance
	 *            options. When performance values have been automatically
	 *            calculated it contains false
	 * @param bnds
	 *            ArrayList: the bounds of the bottleneck levels
	 * @param cols
	 *            ArrayList: the bottleneck colors
	 * @param sort
	 *            String: the timesort used
	 * @param divider
	 *            long: the divider belonging to the timesort
	 * @param places
	 *            int: the number of decimal places used
	 * @param advanced
	 *            int[]: contains the advanced settings, containing info on how
	 *            to deal with conformance
	 */
	public PerformanceAnalysisGUI(Set<AnalysisResult> analysisResults,
			boolean manual, ArrayList bnds, ArrayList cols, long divider,
			String sort, int places, int[] advanced) {
		super(analysisResults);

		// increment static sim model counter for this plugin
		simulationModelCounter = simulationModelCounter + 1;

		Iterator results = analysisResults.iterator();
		while (results.hasNext()) {
			AnalysisResult currentResult = (AnalysisResult) results.next();
			if (currentResult != null) {
				replayResult = (PerformanceLogReplayResult) currentResult;
			} else {
				// an analysis result can be null if the user aborted the
				// computation
				return;
			}
		}
		manualSettings = manual;
		timeDivider = divider;
		timeSort = sort;
		decimalPlaces = places;
		bounds = bnds;
		levelColors = cols;
		advancedSettings = advanced;
		inputLog = replayResult.inputLogReader;
		inputPetriNet = replayResult.inputPetriNet;
		// create the highLevelPetriNet
		highLevelPN = new HLPetriNet(inputPetriNet);
		// the highLevelPN covers the timing perspective and (some part of) the
		// choices perspective
		highLevelPN.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.TIMING_EXECTIME);
		highLevelPN.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.TIMING_WAITTIME);
		highLevelPN.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.TIMING_SOJTIME);
		highLevelPN.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.CASE_GEN_SCHEME);
		highLevelPN.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.CHOICE_FREQ);
		highLevelPN.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.CHOICE_PROB);
		// set the name of the simulation model
		highLevelPN.getHLProcess().getGlobalInfo().setName(
				"Performance Simulation Model No." + simulationModelCounter);

		extendedLog = (ExtendedLogReader) replayResult.replayedLog;
		extendedPetriNet = (ExtendedPetriNet) replayResult.replayedPetriNet;
		obtainFailedInstances();
		initializeWaitingTimeLevels();
		displayProcessMetrics(extendedLog.getDiagnosticLogTraces());
		try {
			jbInit();
			registerGuiListener();
		} catch (Exception ex) {
			Message.add(
					"Program exception while displaying performance analysis results:\n"
							+ ex.toString(), 2);
			ex.printStackTrace();
		}
		// calculate the values for the simulation model
		calculateValuesForSimulationModel(extendedLog.getDiagnosticLogTraces(),
				extendedLog.getLogTraceIDs());
		//
		Iterator entries = mapping.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entr = (Map.Entry) entries.next();
			if (entr.getKey() instanceof ExtendedTransition) {
				ExtendedTransition transition = (ExtendedTransition) entr
						.getKey();
				ExtendedActivity act = transition.getAssociatedActivity();
				if (act != null) {
					act.calculateMetrics(getSelectedInstanceIDs(),
							advancedSettings[3], failedInstances);
					act.checkWhichMetricsToUse();
					act.getArrivalRate();
				}
			}
		}
	}

	// /////////////////////INITIALIZATION METHODS//////////////////////////////

	/**
	 * Actually builds the UI
	 * 
	 * @throws Exception
	 */
	private void jbInit() throws Exception {
		// Initialize the table which contains the log traces
		processInstanceIDsTable = new DoubleClickTable(new ExtendedLogTable(),
				updateButton);
		// select all rows as at the beginning the results count for the whole
		// log
		processInstanceIDsTable.getSelectionModel().addSelectionInterval(0,
				extendedLog.getSizeOfLog() - 1);

		tableContainer = new JScrollPane(processInstanceIDsTable);

		// initialize the westPanel (which contains the processInstanceIDsTable)
		westPanel.setPreferredSize(new Dimension(100, 300));
		westPanel.setLayout(new BorderLayout());
		westPanel.add(tableContainer, BorderLayout.CENTER);
		updateButton
				.setToolTipText("Update metrics to the selected process instances");
		westPanel.add(piButtonsPanel, BorderLayout.SOUTH);
		piButtonsPanel.setLayout(new BorderLayout());
		piButtonsPanel.add(updateButton, BorderLayout.CENTER);
		piButtonsPanel.add(invertButton, BorderLayout.SOUTH);

		// initialize the eastpanel (which contains the process-metrics)
		eastPanel.setPreferredSize(new Dimension(240, 390));
		eastPanel.setMinimumSize(new Dimension(240, 390));
		eastPanel.setLayout(new GridBagLayout());
		GridBagConstraints cons = new GridBagConstraints();
		cons.gridx = 0;
		cons.gridy = 0;
		cons.insets = new Insets(0, 5, 5, 0);
		cons.anchor = GridBagConstraints.WEST;
		processLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		eastPanel.add(processLabel, cons);
		cons.gridx = 0;
		cons.gridy = 1;
		cons.insets = new Insets(8, 8, 0, 0);
		eastPanel.add(numberProcessLabel, cons);
		casesProcessLabel.setText(extendedLog.getSizeOfLog() + " cases");
		casesProcessLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
		cons.gridx = 0;
		cons.gridy = 2;
		eastPanel.add(casesProcessLabel, cons);
		cons.gridx = 0;
		cons.gridy = 3;
		eastPanel.add(properLabel, cons);
		completedLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
		cons.gridx = 0;
		cons.gridy = 4;
		eastPanel.add(completedLabel, cons);
		cons.gridx = 0;
		cons.gridy = 5;
		eastPanel.add(arrivalProcessLabel, cons);
		rateProcessLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
		cons.gridx = 0;
		cons.gridy = 6;
		eastPanel.add(rateProcessLabel, cons);
		cons.gridx = 0;
		cons.gridy = 7;
		cons.gridwidth = 2;
		cons.insets = new Insets(15, 5, 0, 2);
		cons.ipadx = 300;
		eastPanel.add(processTablePanel, cons);
		cons.gridx = 0;
		cons.gridy = 8;
		cons.gridwidth = 1;
		cons.insets = new Insets(0, 5, 0, 0);
		cons.weightx = 1;
		cons.ipadx = 0;
		exportProcessButton.setPreferredSize(new Dimension(110, 42));
		exportProcessButton.setMaximumSize(new Dimension(110, 42));
		changeProcessPercButton.setPreferredSize(new Dimension(110, 42));
		changeProcessPercButton.setMaximumSize(new Dimension(110, 42));
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setPreferredSize(new Dimension(240, 42));
		buttonPanel.add(changeProcessPercButton);
		buttonPanel.add(exportProcessButton);
		eastPanel.add(buttonPanel, cons);

		processContainer = new JScrollPane(eastPanel);
		processContainer.setPreferredSize(new Dimension(240, 390));
		// initialize the processTablePanel (which contains the table with
		// throughput times)
		processTablePanel.setLayout(new BorderLayout());
		processTablePanel.setMinimumSize(new Dimension(240, 130));
		processTablePanel.setPreferredSize(new Dimension(240, 130));
		processTable.setRowSelectionAllowed(false);
		processTablePanel.add(processTable.getTableHeader(),
				BorderLayout.PAGE_START);
		processTablePanel.add(processTable, BorderLayout.CENTER);

		// initialize the grappaPanel (which contains the Petri net UI) and
		// place
		// it on the centerPanel
		grappaPanel = replayResult.getVisualization(extendedLog
				.getLogTraceIDs());
		grappaPanel.addGrappaListener(new ExtendedGrappaAdapter());
		mapping = new HashMap();
		buildGraphMapping(mapping, grappaPanel.getSubgraph());
		modelContainer = new JScrollPane(grappaPanel);
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(modelContainer, BorderLayout.CENTER);

		// initialize the colored panels
		Color tCol = (Color) levelColors.get(0);
		lowPanel.setBackground(tCol);
		lowPanel.setBorder(BorderFactory.createEtchedBorder());
		lowPanel.setPreferredSize(new Dimension(25, 12));
		tCol = (Color) levelColors.get(1);
		mediumPanel.setBackground(tCol);
		mediumPanel.setBorder(BorderFactory.createEtchedBorder());
		mediumPanel.setPreferredSize(new Dimension(25, 12));
		tCol = (Color) levelColors.get(2);
		highPanel.setBackground(tCol);
		highPanel.setBorder(BorderFactory.createEtchedBorder());
		highPanel.setPreferredSize(new Dimension(25, 12));

		// initialize the levelPanel (which contains the waiting time settings)
		levelPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		levelPanel.setLayout(new GridBagLayout());
		cons.gridx = 0;
		cons.gridy = 0;
		cons.gridwidth = 2;
		cons.insets = new Insets(2, 2, 0, 5);
		cons.weightx = 0;
		levelPanel.add(waitingLabel, cons);
		cons.gridx = 0;
		cons.gridy = 1;
		cons.anchor = GridBagConstraints.WEST;
		cons.insets = new Insets(2, 2, 0, 5);
		cons.gridwidth = 1;
		levelPanel.add(highPanel, cons);
		cons.gridx = 1;
		cons.gridy = 1;
		levelPanel.add(highLabel, cons);
		cons.gridx = 0;
		cons.gridy = 2;
		cons.insets = new Insets(1, 2, 0, 5);
		levelPanel.add(mediumPanel, cons);
		cons.gridx = 1;
		cons.gridy = 2;
		levelPanel.add(mediumLabel, cons);
		cons.gridx = 0;
		cons.gridy = 3;
		cons.insets = new Insets(2, 2, 2, 5);
		levelPanel.add(lowPanel, cons);
		cons.gridx = 1;
		cons.gridy = 3;
		levelPanel.add(lowLabel, cons);
		cons.gridx = 0;
		cons.gridy = 4;
		cons.gridwidth = 2;
		cons.insets = new Insets(2, 2, 2, 2);
		changeSettingsButton.setMnemonic(KeyEvent.VK_S);
		levelPanel.add(changeSettingsButton, cons);
		levelPanel.setBackground(new Color(220, 220, 220));

		// initialize the selectionPanel (which contains the selected
		// transitions/place)
		initializeSelection();

		// initialize the metricsBottomPanel
		metricsBottomPanel.setLayout(new GridBagLayout());
		cons = new GridBagConstraints();
		cons.gridx = 0;
		cons.gridy = 0;
		cons.gridwidth = 5;
		cons.anchor = GridBagConstraints.WEST;
		cons.insets = new Insets(1, 2, 5, 0);
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
		metricsBottomPanel.add(titleLabel, cons);
		cons.gridx = 0;
		cons.gridy = 1;
		cons.gridwidth = 1;
		cons.insets = new Insets(1, 2, 2, 0);
		metricsBottomPanel.add(numberObjectLabel, cons);
		cons.gridx = 1;
		cons.gridy = 1;
		cons.gridwidth = 1;
		freqObjectLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		metricsBottomPanel.add(freqObjectLabel, cons);
		cons.gridx = 0;
		cons.gridy = 2;
		cons.gridwidth = 1;
		metricsBottomPanel.add(arrivalPlaceLabel, cons);
		cons.gridx = 1;
		cons.gridy = 2;
		cons.gridwidth = 1;
		ratePlaceLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		metricsBottomPanel.add(ratePlaceLabel, cons);
		cons.gridx = 0;
		cons.gridy = 3;
		cons.gridwidth = 5;
		metricsBottomPanel.add(tablePanel, cons);
		cons.gridx = 0;
		cons.gridy = 4;
		cons.gridwidth = 2;
		changePercentagesButton.setPreferredSize(new Dimension(170, 25));
		metricsBottomPanel.add(changePercentagesButton, cons);
		cons.gridx = 2;
		cons.gridy = 4;
		cons.gridwidth = 2;
		metricsBottomPanel.add(exportButton, cons);
		// hide all place/transition metrics
		hideAllMetrics();
		metricsBottomContainer = new JScrollPane(metricsBottomPanel);
		metricsBottomContainer.setPreferredSize(new Dimension(550, 140));
		metricsBottomContainer.setMinimumSize(new Dimension(550, 140));
		metricsBottomContainer.setMaximumSize(new Dimension(550, 800));
		metricsBottomContainer.setBorder(BorderFactory.createEmptyBorder());

		// initialize the bottomPanel
		bottomPanel.setBorder(BorderFactory.createEtchedBorder());
		bottomPanel.setPreferredSize(new Dimension(650, 143));
		bottomPanel.setMinimumSize(new Dimension(650, 143));
		bottomPanel.setLayout(new GridBagLayout());
		GridBagConstraints con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = 0;
		con.gridwidth = 4;
		con.weightx = 2;
		bottomPanel.add(metricsBottomContainer, con);
		con.gridx = 4;
		con.gridy = 0;
		con.gridwidth = 1;
		con.weightx = 1;
		con.anchor = GridBagConstraints.EAST;
		bottomPanel.add(levelPanel, con);
		con.gridx = 5;
		con.gridy = 0; // con.gridwidth = 2;
		con.weightx = 1;
		con.anchor = GridBagConstraints.EAST;
		bottomPanel.add(selectionPanel, con);
		// Divide the upper panels by using splitPanes
		leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westPanel,
				centerPanel);
		leftSplitPane.setDividerLocation(130);
		leftSplitPane.setOneTouchExpandable(true);
		rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftSplitPane, processContainer);
		rightSplitPane.setDividerLocation(rightSplitPane.getSize().width
				- rightSplitPane.getInsets().right
				- rightSplitPane.getDividerSize() - 240); // processContainer.getWidth());
		rightSplitPane.setOneTouchExpandable(true);
		rightSplitPane.setResizeWeight(1.0);
		rightSplitPane.setDividerSize(3);

		// set tooltips
		processTable.setToolTipText("Throughput time of the process, based on"
				+ " the selected cases");
		rateProcessLabel.setToolTipText("Arrival rate of the selected cases");
		arrivalProcessLabel
				.setToolTipText("Arrival rate of the selected cases");
		completedLabel.setToolTipText("Total number of the selected cases that"
				+ "  have completed properly and successfully");
		properLabel.setToolTipText("Total number of the selected cases that"
				+ " have completed properly and successfully");
		casesProcessLabel.setToolTipText("Total number of cases selected in"
				+ " the table on the left");
		numberProcessLabel.setToolTipText("Total number of cases selected in"
				+ " the table on the left");
		changeSettingsButton.setToolTipText("Adjust the current waiting time"
				+ " classifications");
		placeTable
				.setToolTipText("Time-metrics of the selected place, based on"
						+ " the selected cases");
		transitionTable.setToolTipText("Time cases spend in between the"
				+ " selected transitions, based on the selected cases");
		activityTable.setToolTipText("Time-metrics of activity related to the"
				+ " selected transition, based on the selected cases");
		processInstanceIDsTable.setToolTipText("Selected cases");
		changePercentagesButton.setToolTipText("Change percentages of slow,"
				+ " fast and normal");
		changeProcessPercButton.setToolTipText("Change percentages of slow,"
				+ " fast and normal");
		exportButton.setToolTipText("Export the values of all measurements of"
				+ " the metrics in the table above to a comma-seperated"
				+ " text-file");
		exportProcessButton
				.setToolTipText("Export the value of the throughput"
						+ " time of all selected process instances to a comma-seperated"
						+ " text-file");
		// initialize the performanceAnalysisGUI
		this.setLayout(new BorderLayout());
		this.add(bottomPanel, BorderLayout.SOUTH);
		this.add(rightSplitPane, BorderLayout.CENTER);
		this.validate();
		this.repaint();
	}

	/**
	 * Initializes the selectionPanel: Fills one combobox with all transitions
	 * and places of the used Petri net, and one combobox with only transitions.
	 * Initially nothing is selected in both comboboxes and the second combobox
	 * is disabled.
	 */
	private void initializeSelection() {
		sb1 = new JComboBox();
		sb1.setMaximumSize(new Dimension(200, 20));
		// SteppedComboBoxUI allows the pop-up menu to be of different width
		// than the width of the combobox, here sb1, itself
		sb1.setUI(new SteppedComboBoxUI());
		sb1.setBorder(BorderFactory.createEtchedBorder());
		sb2 = new JComboBox();
		sb2.setUI(new SteppedComboBoxUI());
		sb2.setMaximumSize(new Dimension(200, 20));
		sb2.setBorder(BorderFactory.createEtchedBorder());

		// Fill the boxes
		sb1.addItem("-------------");
		sb2.addItem("-------------");
		// add all places of the Petri net to the first selection box
		Iterator it = extendedPetriNet.getPlaces().iterator();
		while (it.hasNext()) {
			Element current = (Element) it.next();
			ExtendedPlace ep = (ExtendedPlace) current;
			String placeString = "Place - " + ep.getIdentifier();
			sb1.addItem(placeString);
			if (boxMap.get(placeString) == null) {
				// and add the place to the boxMap if it is not already in it
				boxMap.put(placeString, current);
			}
		}
		// add all transitions of the Petri net to both selection boxes
		Iterator dit = extendedPetriNet.getTransitions().iterator();
		while (dit.hasNext()) {
			Element current = (Element) dit.next();
			ExtendedTransition et = (ExtendedTransition) current;
			try {
				String transString = "Transition - "
						+ et.getLogEvent().getModelElementName() + " "
						+ et.getLogEvent().getEventType();

				if (boxMap.get(transString) == null) {
					// place transition in sb1 & sb2
					sb1.addItem(transString);
					sb2.addItem(transString);
					// and add the transition to the boxMap
					boxMap.put(transString, current);
				} else {
					// at least one transition with the same name within the
					// boxMap
					// count the number of transitions in the boxMap having this
					// same name
					Iterator keys = boxMap.keySet().iterator();
					int number = 0;
					while (keys.hasNext()) {
						String key = (String) keys.next();
						if (key.startsWith(transString)) {
							number++;
						}
					}
					// add current transition to the boxMap, with between
					// brackets
					// its number (>=1)
					transString = "Transition - "
							+ et.getLogEvent().getModelElementName() + " ("
							+ number + ") " + " "
							+ et.getLogEvent().getEventType();
					// place transition in sb1 & sb2
					sb1.addItem(transString);
					sb2.addItem(transString);
					boxMap.put(transString, current);
				}
			} catch (NullPointerException ne) {
				/*
				 * Invisible transitions do not have a ModelElementName or
				 * EventType, thus an exception occurs. It is caught here
				 */
			}
		}
		// place objects on the selectionPanel
		selectionPanel
				.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
		selectionPanel.add(Box.createRigidArea(new Dimension(15, 10)));
		selectLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
		selectionPanel.add(selectLabel);
		selectionPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		selectionPanel.add(sb1);
		selectionPanel.add(Box.createRigidArea(new Dimension(5, 10)));
		andLabel.setForeground(Color.GRAY);
		selectionPanel.add(andLabel);
		selectionPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		sb1.setBackground(Color.white);
		sb2.setBackground(Color.white);
		sb2.setEnabled(false);
		selectionPanel.add(sb2);
		selectionPanel.setBackground(new Color(220, 220, 220));
		selectLabel.setAlignmentX(LEFT_ALIGNMENT);
		sb1.setAlignmentX(LEFT_ALIGNMENT);
		andLabel.setAlignmentX(LEFT_ALIGNMENT);
		sb2.setAlignmentX(LEFT_ALIGNMENT);
		selectionPanel.setBorder(BorderFactory.createEtchedBorder());
		selectionPanel.setPreferredSize(new Dimension(240, 140));
		selectionPanel.setMinimumSize(new Dimension(240, 140));
	}

	private void obtainFailedInstances() {
		extendedLog.reset();
		while (extendedLog.hasNext()) {
			ExtendedLogTrace pi = (ExtendedLogTrace) extendedLog.next();
			if (!pi.hasSuccessfullyExecuted() || !pi.hasProperlyTerminated()) {
				failedInstances.add(pi.getName());
			}
		}
	}

	/**
	 * Initializes the waiting time levels. If no manual peformance settings are
	 * filled in by the user, standard settings are calculated and used.
	 * Standard settings: approximately 33% low, 33% high, 33% medium level note
	 * that a set is used instead of a list however, so if a time occurs
	 * multiple times (this can happen easily with a waiting time of 0.0s for
	 * instance) of such places only one is used, so the 33-33-33 estimation can
	 * be quite wrong, though this is not considered to be a problem.
	 */
	public void initializeWaitingTimeLevels() {
		if (!manualSettings) {
			// no manual settings are present
			TreeSet waitingTimes = new TreeSet();
			ListIterator it = extendedPetriNet.getPlaces().listIterator();
			while (it.hasNext()) {
				// place the mean waiting time of each place in the tree set
				ExtendedPlace p = (ExtendedPlace) it.next();
				p.calculateMetrics(extendedLog.getLogTraceIDs(),
						advancedSettings[1], failedInstances);
				if (p.getMeanWaitingTime() >= 0) {
					// only add correct times
					double waitTime = p.getMeanWaitingTime() / timeDivider;
					waitingTimes.add(Double.valueOf(waitTime));
				}
			}
			int num = waitingTimes.size() / 3;
			// remove the first 'num' measurements and the last 'num'
			// measurements
			// from waitingTimes
			for (int i = 0; i < num; i++) {
				// there should be at least one waiting time measurement
				// remaining
				if (!(waitingTimes.size() < 2)) {
					waitingTimes.remove(waitingTimes.first());
					waitingTimes.remove(waitingTimes.last());
				}
			}

			// give new values to the bounds and the colors
			if (waitingTimes.size() != 0) {
				Double bnd = (Double) waitingTimes.first();
				bounds.set(0, bnd);
				bnd = (Double) waitingTimes.last();
				bounds.set(1, bnd);
			} else {
				// in case there are no valid waiting times
				waitingTimes.add(Double.valueOf(0));
				Double bnd = (Double) waitingTimes.first();
				bounds.set(0, bnd);
				bounds.set(1, bnd);
			}
			levelColors.set(0, Color.BLUE);
			levelColors.set(1, Color.YELLOW);
			levelColors.set(2, Color.MAGENTA);
		}
		extendedPetriNet.setAdvancedSettings(advancedSettings);
		extendedPetriNet.setBounds(bounds);
		extendedPetriNet.setLevelColors(levelColors);
		extendedPetriNet.setTimeDivider(timeDivider);
		extendedPetriNet.setFailedInstances(failedInstances);
	}

	/**
	 * Connects user-interface objects with Listener methods
	 */
	private void registerGuiListener() {
		// specify button action
		updateButton.addActionListener(new ActionListener() {
			// specify the action to be taken when pressing the button
			public void actionPerformed(ActionEvent e) {
				hideAllMetrics();
				updateResults();
				// update the timing information for the transitions in the hlPN
				Iterator entries = mapping.entrySet().iterator();
				while (entries.hasNext()) {
					Map.Entry entr = (Map.Entry) entries.next();
					if (entr.getKey() instanceof ExtendedTransition) {
						ExtendedTransition transition = (ExtendedTransition) entr
								.getKey();
						ExtendedActivity act = transition
								.getAssociatedActivity();
						if (act != null) {
							act.calculateMetrics(getSelectedInstanceIDs(),
									advancedSettings[3], failedInstances);
							act.checkWhichMetricsToUse();
							act.getArrivalRate();
						}
					}
				}
			}
		});
		invertButton.addActionListener(new ActionListener() {
			// specify the action to be taken when pressing the button
			public void actionPerformed(ActionEvent e) {
				invertSelectionStatus();
			}
		});
		changeSettingsButton.addActionListener(new ActionListener() {
			// specify the action to be taken when pressing the button
			public void actionPerformed(ActionEvent e) {
				// create and show the performance configuration screen
				createPerformanceOptionsScreen();
			}
		});
		changePercentagesButton.addActionListener(new ActionListener() {
			// specify the action to be taken when pressing the button
			public void actionPerformed(ActionEvent e) {
				changeObjectPercentages();
			}
		});
		exportButton.addActionListener(new ActionListener() {
			// specify the action to be taken when pressing the button
			public void actionPerformed(ActionEvent e) {
				exportObjectMetrics();
			}
		});
		exportProcessButton.addActionListener(new ActionListener() {
			// specify the action to be taken when pressing the button
			public void actionPerformed(ActionEvent e) {
				exportProcessMetrics();
			}
		});
		changeProcessPercButton.addActionListener(new ActionListener() {
			// specify the action to be taken when pressing the button
			public void actionPerformed(ActionEvent e) {
				changeProcessPercentages();
			}
		});
		sb1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				adjustToSelectedSb1();
			}
		});
		sb2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				adjustToSelectedSb2();
			}
		});
	}

	// ///////////////////// ADJUSTING METHODS///////////////////
	/**
	 * Updates both the visualization and the performance metrics by taking only
	 * the selected instances into account. If all instances have been selected,
	 * the result corresponds to the initial state.
	 */
	private void updateResults() {
		try {
			// check if selection is empty --> if so select whole log
			ListSelectionModel selectionModel = processInstanceIDsTable
					.getSelectionModel();
			if (selectionModel.isSelectionEmpty()) {
				selectionModel.addSelectionInterval(0, extendedLog
						.getSizeOfLog() - 1);
			}
			// invoke redraw of vizualization for selected process instances
			// only..
			ArrayList selectedInstanceIDs = getSelectedInstanceIDs();
			ArrayList selectedInstances = getSelectedInstances();

			grappaPanel = replayResult.getVisualization(selectedInstanceIDs);
			grappaPanel.addGrappaListener(new ExtendedGrappaAdapter());
			mapping = new HashMap();
			buildGraphMapping(mapping, grappaPanel.getSubgraph());
			modelContainer = new JScrollPane(grappaPanel);
			centerPanel.removeAll();
			centerPanel.add(modelContainer, BorderLayout.CENTER);
			centerPanel.validate();
			centerPanel.repaint();
			// update process metrics and display them
			displayProcessMetrics(selectedInstances);
			// calculate the values for the simulation model
			calculateValuesForSimulationModel(getSelectedInstances(),
					getSelectedInstanceIDs());
			// update the provided object (visualization might have changed)
			extendedPetriNet = (ExtendedPetriNet) replayResult.replayedPetriNet;
			// Make sure that what was selected before the update, is now
			// selected again.
			if (elt1 instanceof ExtendedTransition) {
				// transition was selected, select it again in sb1
				ExtendedTransition trans = (ExtendedTransition) elt1;
				sb1.setSelectedItem("Transition - "
						+ trans.getLogEvent().getModelElementName() + " "
						+ trans.getLogEvent().getEventType());
				if (elt2 instanceof ExtendedTransition) {
					// another transition was selected too, select it again in
					// sb2
					trans = (ExtendedTransition) elt2;
					sb2.setSelectedItem("Transition - "
							+ trans.getLogEvent().getModelElementName() + " "
							+ trans.getLogEvent().getEventType());
				}
			} else if (elt1 instanceof ExtendedPlace) {
				// place was selected, select it in sb1
				ExtendedPlace place = (ExtendedPlace) elt1;
				sb1.setSelectedItem("Place - " + place.getIdentifier());
			} else {
				// nothing selected, set sb1 and sb2 to index 0
				sb1.setSelectedIndex(0);
				sb2.setSelectedIndex(0);
			}
		} catch (Exception ex) {
			JOptionPane
					.showMessageDialog(this,
							"An internal error occured while\nupdating the visualization.");
			Message.add("Probably not found.\n" + ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * Adjusts the PerformanceAnalysisGUI to the set values of the performance
	 * options.
	 */
	private void adjustGUI() {
		tablePanel.removeAll();
		// update legenda
		Color bgColor = (Color) levelColors.get(0);
		lowPanel.setBackground(bgColor);
		bgColor = (Color) levelColors.get(1);
		mediumPanel.setBackground(bgColor);
		bgColor = (Color) levelColors.get(2);
		highPanel.setBackground(bgColor);
		// initialize waiting time levels
		initializeWaitingTimeLevels();
		// update the display results to the settings
		updateResults();
	}

	/**
	 * Creates dialogs in which the user can change the percentages of (place or
	 * time-in-between) measurements to be counted as fast-slow-normal and
	 * adjusts the metrics to these percentages.
	 */
	private void changeObjectPercentages() {
		if (elt1 instanceof ExtendedPlace) {
			// a place was selected
			// Create a dialog in which the user can fill in the percentage of
			// measurements that is counted as 'fast'
			String perc = JOptionPane.showInputDialog(this,
					"Enter the percentage of measurements\n"
							+ " that is to be counted as 'fast': ");
			if (perc != null) {
				// Cancel button was not pressed
				try {
					fastestPlacePercentage = Double.parseDouble(perc);
					// check if the entered value is between 0 and 100
					if (fastestPlacePercentage < 0
							|| fastestPlacePercentage > 100) {
						fastestPlacePercentage = 0;
					}
				} catch (Exception e) {
					// the entered string was not an integer-value
					Message.add("Exception: " + e.toString(), 2);
					fastestPlacePercentage = 0;
				}
				// Create a dialog in which the user can fill in the percentage
				// of
				// measurements that is counted as 'slow'
				String perc2 = JOptionPane.showInputDialog(this,
						"Enter the percentage of measurements\n"
								+ " that is to be counted as 'slow': ");
				if (perc2 != null) {
					// Cancel was not pressed
					try {
						slowestPlacePercentage = Double.parseDouble(perc2);
						// check if the entered value is between 0 and 100 and
						// if
						// fastest + slowpercentage is below or equal to 100
						if (slowestPlacePercentage < 0
								|| slowestPlacePercentage > 100
								|| (slowestPlacePercentage + fastestPlacePercentage) > 100) {
							slowestPlacePercentage = 0;
						}
					} catch (Exception e) {
						Message.add("Exception: " + e.toString(), 2);
						slowestPlacePercentage = 0;
					}
				}
				// display the place metrics
				displayPlaceMetrics((ExtendedPlace) elt1);
			}
		} else {
			if (elt2 instanceof ExtendedTransition) {
				// two transitions selected
				String perc = JOptionPane.showInputDialog(this,
						"Enter the percentage of cases that is to be"
								+ " counted as 'fast': ");
				if (perc != null) {
					try {
						fastestBetweenPercentage = Double.parseDouble(perc);
						if (fastestBetweenPercentage < 0
								|| fastestBetweenPercentage > 100) {
							fastestBetweenPercentage = 0;
						}
					} catch (Exception e) {
						Message.add("Exception: " + e.toString(), 2);
						fastestBetweenPercentage = 0;
					}
					String perc2 = JOptionPane.showInputDialog(this,
							"Enter the percentage of cases that is to be"
									+ " counted as 'slow': ");
					if (perc2 != null) {
						try {
							slowestBetweenPercentage = Double
									.parseDouble(perc2);
							if (slowestBetweenPercentage < 0
									|| slowestBetweenPercentage > 100
									|| (slowestBetweenPercentage + fastestBetweenPercentage) > 100) {
								slowestBetweenPercentage = 0;
							}
						} catch (Exception e) {
							Message.add("Exception: " + e.toString(), 2);
							slowestBetweenPercentage = 0;
						}
					}
					ExtendedTransition otherTrans = (ExtendedTransition) elt1;
					ExtendedTransition lastTrans = (ExtendedTransition) elt2;

					// display the transition metrics
					displayTransitionMetrics(lastTrans, otherTrans);
				}
			} else {
				// one transition selected
				// if not a place selected, then one or two transitions selected
				String perc = JOptionPane.showInputDialog(this,
						"Enter the percentage of measurements that is to be"
								+ " counted as 'fast': ");
				if (perc != null) {
					try {
						fastestActivityPercentage = Double.parseDouble(perc);
						if (fastestActivityPercentage < 0
								|| fastestActivityPercentage > 100) {
							fastestActivityPercentage = 0;
						}
					} catch (Exception e) {
						Message.add("Exception: " + e.toString(), 2);
						fastestActivityPercentage = 0;
					}
					String perc2 = JOptionPane.showInputDialog(this,
							"Enter the percentage of measurements that is"
									+ " to be counted as 'slow': ");
					if (perc2 != null) {
						try {
							slowestActivityPercentage = Double
									.parseDouble(perc2);
							if (slowestActivityPercentage < 0
									|| slowestActivityPercentage > 100
									|| (slowestActivityPercentage + fastestActivityPercentage) > 100) {
								slowestActivityPercentage = 0;
							}
						} catch (Exception e) {
							Message.add("Exception: " + e.toString(), 2);
							slowestActivityPercentage = 0;
						}
					}
					ExtendedTransition trans = (ExtendedTransition) elt1;
					// display the transition metrics
					try {
						displayActivityMetrics(trans.getAssociatedActivity());
					} catch (NullPointerException npe) {
						Message.add(
								"No activity related to the selected transition.\n"
										+ npe.toString(), 2);
					}
				}
			}
		}
	}

	/**
	 * Creates dialogs in which the user can change the percentages of process
	 * instances to be counted as fast-slow-normal baed on their throughput
	 * time. It also adjusts the process metrics to these percentages.
	 */
	private void changeProcessPercentages() {
		// Create a dialog in which the user can fill in the percentage of
		// cases that is to be counted as 'fast'
		String perc = JOptionPane.showInputDialog(this,
				"Enter the percentage of cases that is to be"
						+ " counted as 'fast': ");
		if (perc != null) {
			try {
				fastestProcessPercentage = Double.parseDouble(perc);
				if (fastestProcessPercentage < 0
						|| fastestProcessPercentage > 100) {
					fastestProcessPercentage = 0;
				}
			} catch (Exception e) {
				Message.add("Exception: " + e.toString(), 2);
				fastestProcessPercentage = 0;
			}
			// Create a dialog in which the user can fill in the percentage of
			// cases that is to be counted as 'slow'
			String perc2 = JOptionPane.showInputDialog(this,
					"Enter the percentage of cases that is to be counted"
							+ " as 'slow': ");
			if (perc2 != null) {
				// Cancel button was not pushed
				try {
					slowestProcessPercentage = Double.parseDouble(perc2);
					if (slowestProcessPercentage < 0
							|| slowestProcessPercentage > 100
							|| (slowestProcessPercentage + fastestProcessPercentage) > 100) {
						slowestProcessPercentage = 0;
					}
				} catch (Exception e) {
					Message.add("Exception: " + e.toString(), 2);
					slowestProcessPercentage = 0;
				}
			}
			// display the process metrics (based on the selected instances)
			displayProcessMetrics(getSelectedInstances());
		}
	}

	/**
	 * Creates a filechooser where the user can select the (comma-seperated)
	 * file that he wants to export to. If this file already exists, permission
	 * to overwrite is requested, else a new file is created to which the
	 * time-metrics of the selected place/transitions are exported.
	 * 
	 */
	private void exportObjectMetrics() {
		// create and initialize the file chooser
		final JFileChooser fc = new JFileChooser();
		NameFilter filt1 = new NameFilter(".csv");
		NameFilter filt2 = new NameFilter(".txt");
		fc.addChoosableFileFilter(filt1);
		fc.addChoosableFileFilter(filt2);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(filt1);
		// check whether a place is selected
		int result = fc.showDialog(this, "Export");
		if (result == 0) {
			// Export-button was pushed
			File selectedFile = fc.getSelectedFile();
			if (!(selectedFile.getName().endsWith(fc.getFileFilter()
					.getDescription().substring(1)))) {
				// create new file, with filetype added to it
				selectedFile = new File(selectedFile.getAbsolutePath()
						+ fc.getFileFilter().getDescription().substring(1));
			}

			if (!selectedFile.exists()) {
				if (boxMap.get(sb1.getSelectedItem()) instanceof ExtendedPlace) {
					// actually perform export of place time-metrics
					exportPlaceMetrics(selectedFile);
				} else {
					if (boxMap.get(sb2.getSelectedItem()) instanceof ExtendedTransition) {
						// actually perform export of transition time-metrics
						exportTransitionMetrics(selectedFile);
					} else {
						// actually perform export of activity time-metrics
						exportActivityMetrics(selectedFile);
					}
				}
			} else {
				// file already exist, open a confirm dialog containing a
				// 'Yes' Button as well as a 'No' button
				int overwrite = JOptionPane.showConfirmDialog(this,
						"File already exists! Overwrite?", "Confirm Dialog", 0);
				if (overwrite == 0) {
					// user has selected Yes
					if (boxMap.get(sb1.getSelectedItem()) instanceof ExtendedPlace) {
						// actually perform export of place time-metrics
						exportPlaceMetrics(selectedFile);
					} else {
						if (boxMap.get(sb2.getSelectedItem()) instanceof ExtendedTransition) {
							// actually perform export of transition
							// time-metrics
							exportTransitionMetrics(selectedFile);
						} else {
							// actually perform export of activity time-metrics
							exportActivityMetrics(selectedFile);
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a filechooser where the user can select the (comma-seperated)
	 * file that he wants to export to. If this file already exists, permission
	 * to overwrite is requested, else a new file is created to which the
	 * throughput times of the selected process instances are exported.
	 * 
	 */
	private void exportProcessMetrics() {
		// create and initialize the file chooser
		final JFileChooser fc = new JFileChooser();
		NameFilter filt1 = new NameFilter(".csv");
		NameFilter filt2 = new NameFilter(".txt");
		fc.addChoosableFileFilter(filt1);
		fc.addChoosableFileFilter(filt2);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(filt1);
		// check whether a place is selected
		int result = fc.showDialog(this, "Export");
		if (result == 0) {
			// Export-button was pushed
			File selectedFile = fc.getSelectedFile();
			if (!(selectedFile.getName().endsWith(fc.getFileFilter()
					.getDescription().substring(1)))) {
				// create new file, with filetype added to it
				selectedFile = new File(selectedFile.getAbsolutePath()
						+ fc.getFileFilter().getDescription().substring(1));
			}
			if (!selectedFile.exists()) {
				try {
					// actually export the throughput times to the file
					replayResult.exportToFile(getSelectedInstances(),
							selectedFile, timeDivider, timeSort,
							advancedSettings[0]);
				} catch (IOException ex) {
					Message.add("IO exception: " + ex.toString(), 2);
				}
			} else {
				// file already exist, open a confirm dialog containing a
				// 'Yes' Button as well as a 'No' button
				int overwrite = JOptionPane.showConfirmDialog(this,
						"File already exists! Overwrite?", "Confirm Dialog", 0);
				if (overwrite == 0) {
					// user has selected Yes
					try {
						// actually export the throughput times to the file
						replayResult.exportToFile(getSelectedInstances(),
								selectedFile, timeDivider, timeSort,
								advancedSettings[0]);
					} catch (IOException ex) {
						Message.add("IO exception: " + ex.toString(), 2);
					}
				}
			}
		}
	}

	/**
	 * Exports the values of all measurements of the time-metrics (waiting,
	 * synchronization, sojourn time) of the selected place to the file that is
	 * given as parameter.
	 * 
	 * @param file
	 *            File
	 */
	private void exportPlaceMetrics(File file) {
		ExtendedPlace place = (ExtendedPlace) boxMap.get(sb1.getSelectedItem());
		try {
			place.exportToFile(getSelectedInstanceIDs(), file, timeDivider,
					timeSort, advancedSettings[1]);
		} catch (IOException ex) {
			Message.add("IO exception: " + ex.toString(), 2);
		}
	}

	/**
	 * Exports the values of all measurements of the time-metrics (waiting,
	 * synchronization, sojourn time) of the selected place to the file that is
	 * given as parameter.
	 * 
	 * @param file
	 *            File
	 */
	private void exportTransitionMetrics(File file) {
		ExtendedTransition first = (ExtendedTransition) boxMap.get(sb1
				.getSelectedItem());
		ExtendedTransition second = (ExtendedTransition) boxMap.get(sb2
				.getSelectedItem());
		try {
			TransitionAnalysis ta = new TransitionAnalysis(first, second);
			ta.exportToFile(getSelectedInstanceIDs(), file, timeDivider,
					timeSort, advancedSettings[2]);
		} catch (IOException ex) {
			Message.add("IO exception: " + ex.toString(), 2);
		}
	}

	/**
	 * Exports the values of all measurements of the time-metrics (waiting,
	 * execution, sojourn time) of the selected activity to the file that is
	 * given as parameter.
	 * 
	 * @param file
	 *            File
	 */
	private void exportActivityMetrics(File file) {
		ExtendedTransition trans = (ExtendedTransition) boxMap.get(sb1
				.getSelectedItem());
		try {
			trans.getAssociatedActivity().exportToFile(
					getSelectedInstanceIDs(), file, timeDivider, timeSort,
					advancedSettings[3]);
		} catch (IOException ex) {
			Message.add("IO exception: " + ex.toString(), 2);
		}
	}

	/**
	 * Adjusts the GUI to the object (a place, a transition or null) that is
	 * selected in comboBox sb1
	 * 
	 * @todo Peter: check whether I can change this, so objects are highlighted
	 *       before calculation of metrics starts..
	 */
	private void adjustToSelectedSb1() {
		// remove highlighting of all selected objects on the panel
		grappaPanel.unSelectAll();
		// hide all place/transition metrics
		hideAllMetrics();
		if (boxMap.get((String) sb1.getSelectedItem()) instanceof ExtendedTransition) {
			// a transition was selected in sb1, make sure sb2 is enabled
			sb2.setEnabled(true);
			andLabel.setForeground(Color.black);
			// store the selected transition in elt1
			elt1 = (Element) boxMap.get(sb1.getSelectedItem());
			Collection a = new Bag();
			a.add(elt1);
			if (boxMap.get((String) sb2.getSelectedItem()) instanceof ExtendedTransition) {
				// a transition was selected in sb2 as well, store it in elt2
				elt2 = (Element) boxMap.get(sb2.getSelectedItem());
				if (elt1 != elt2) {
					a.add(elt2);
					// display the transition metrics of the selected
					// transitions
					displayTransitionMetrics((ExtendedTransition) elt1,
							((ExtendedTransition) elt2));
				}
			} else {
				if (sb2.getSelectedIndex() == 0 && elt2 == null) {
					// display activity metrics if just one transition is
					// selected
					displayActivityMetrics(((ExtendedTransition) elt1)
							.getAssociatedActivity());
				}
			}
			// highlight the transition(s) on the grappaPanel
			grappaPanel.selectElements(a);
		} else {
			// anything but a transition selected in sb1, disable sb2
			sb2.setEnabled(false);
			sb2.setSelectedIndex(0);
			andLabel.setForeground(Color.gray);
			if (boxMap.get((String) sb1.getSelectedItem()) instanceof ExtendedPlace) {
				// a place was selected in sb1, store it in elt1
				elt1 = (Element) boxMap.get(sb1.getSelectedItem());
				Collection a = new Bag();
				a.add(elt1);
				// highlight the selected place
				grappaPanel.selectElements(a);
				ExtendedPlace place = (ExtendedPlace) boxMap.get((String) sb1
						.getSelectedItem());
				if (place.getIdentifier() != "pstart"
						&& place.getIdentifier() != "pend"
						&& place.getIdentifier() != "psource"
						&& place.getIdentifier() != "psink") {
					// display the metrics corresponding to the place in elt1
					displayPlaceMetrics((ExtendedPlace) elt1);
				}
			}
		}
		// repaint the graph
		grappaPanel.getSubgraph().getGraph().repaint();
	}

	/**
	 * Adjusts the GUI to the object (a place, a transition or null) that is
	 * selected in comboBox sb2
	 */
	private void adjustToSelectedSb2() {
		// remove all current highlighting
		grappaPanel.unSelectAll();
		// hide all place/transition metrics
		hideAllMetrics();
		// store the transition that is selected in sb1 in elt1
		elt1 = (Element) boxMap.get(sb1.getSelectedItem());
		Collection a = new Bag();
		a.add(elt1);
		if (boxMap.get((String) sb2.getSelectedItem()) instanceof ExtendedTransition) {
			// a transition is selected in sb2, store it in elt2
			elt2 = (Element) boxMap.get(sb2.getSelectedItem());
			a.add(elt2);
			if (elt2 != elt1) {
				// display the transition metrics of elt1 and elt2
				displayTransitionMetrics((ExtendedTransition) elt1,
						((ExtendedTransition) elt2));
			}
		} else {
			elt2 = null;
			if (boxMap.get((String) sb1.getSelectedItem()) instanceof ExtendedTransition) {
				// display activity metrics if just one transition is selected
				displayActivityMetrics(((ExtendedTransition) elt1)
						.getAssociatedActivity());
			}
		}

		// highlight elt1 and (if not null) elt2 in the graph
		grappaPanel.selectElements(a);
		grappaPanel.getSubgraph().getGraph().repaint();
	}

	/**
	 * Creates a new frame, containing the current PerformanceConfiguration. In
	 * the new frame, the user can change the current settings, after which the
	 * PerformanceAnalysisGUI is updated.
	 */
	private void createPerformanceOptionsScreen() {
		// initialize the settings screen
		JPanel firstPanel = new JPanel(new BorderLayout());
		JPanel secondPanel = new JPanel();
		JButton okButton = new JButton("Apply Changes");
		JButton cancelButton = new JButton("Cancel");
		String sel = (String) sb1.getSelectedItem();
		Dimension dim = new Dimension(380, 460);
		// create a new PerformanceConfiguration-object, containing the current
		// settings
		final PerformanceConfiguration performanceOptions = new PerformanceConfiguration(
				levelColors, bounds, timeSort, decimalPlaces, advancedSettings
						.clone());
		if (boxMap.get(sel) instanceof ExtendedPlace) {
			// adjust the performanceOptions to the settings of the selected
			// place
			performanceOptions.changeDisplay((ExtendedPlace) boxMap.get(sel));
			dim = new Dimension(380, 525);
		} else {
			performanceOptions.changeDisplay(null);
		}
		JScrollPane scroll = new JScrollPane(performanceOptions);
		firstPanel.add(scroll, BorderLayout.CENTER);
		firstPanel.add(secondPanel, BorderLayout.SOUTH);
		secondPanel.setLayout(new BoxLayout(secondPanel, BoxLayout.X_AXIS));
		secondPanel.add(Box.createRigidArea(new Dimension(40, 0)));
		secondPanel.add(okButton);
		secondPanel.add(Box.createRigidArea(new Dimension(20, 0)));
		secondPanel.add(cancelButton);
		okButton.addActionListener(new ActionListener() {
			// the action to be taken when the OK-button is pressed
			public void actionPerformed(ActionEvent e) {
				if (performanceOptions.getAllSelected()) {
					// settings should be applied to all places
					timeDivider = performanceOptions.getTimeDivider();
					timeSort = performanceOptions.getTimesort();
					decimalPlaces = performanceOptions.getDecimalPlaces();
					bounds = performanceOptions.getBoundaries();
					levelColors = performanceOptions.getColors();
					advancedSettings = performanceOptions.getAdvancedSettings()
							.clone();
					manualSettings = true;
					ListIterator lit = extendedPetriNet.getPlaces()
							.listIterator();
					while (lit.hasNext()) {
						ExtendedPlace p = (ExtendedPlace) lit.next();
						p.setHasOwnSettings(false);
					}
				} else {
					// apply settings to the selected place only
					performanceOptions.getSelectedPlace().setOwnSettings(
							performanceOptions.getBoundaries(),
							performanceOptions.getColors());
					timeDivider = performanceOptions.getTimeDivider();
					timeSort = performanceOptions.getTimesort();
					decimalPlaces = performanceOptions.getDecimalPlaces();
					advancedSettings = performanceOptions.getAdvancedSettings()
							.clone();
					manualSettings = true;
				}
				// close all frames containing AdvancedSettings opened from
				// performanceOptions
				performanceOptions.closeAdvancedFrames();
				// close the performanceOptions-frame
				MainUI.getInstance().getDesktop().getSelectedFrame()
						.doDefaultCloseAction();
				// adjust the PerformanceAnalysisGUI
				adjustGUI();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			// the action to be taken when the cancel button has been pressed
			public void actionPerformed(ActionEvent e) {
				// close all frames containing AdvancedSettings opened from
				// performanceOptions
				performanceOptions.closeAdvancedFrames();
				// close the PerformanceOptions-frame
				MainUI.getInstance().getDesktop().getSelectedFrame()
						.doDefaultCloseAction();
			}
		});
		// show the panel in a new frame
		MainUI.getInstance().createFrame("Performance analysis options:",
				firstPanel);
		JInternalFrame frame = MainUI.getInstance().getDesktop()
				.getSelectedFrame();
		frame.setSize(dim);
	}

	// ////////////////PROCESS INSTANCE SELECTION RELATED
	// METHODS///////////////////

	/**
	 * Retrieves the current selection status based on table indices.
	 * 
	 * @return int[] an array of indices indicating those instances that are
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
			selectedInstanceIDs.add(extendedLog.getLogTraceIDs().get(
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
			selectedInstances.add(extendedLog.getDiagnosticLogTraces().get(
					indexArray[i]));
		}
		return selectedInstances;
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
		for (int index = 0; index < extendedLog.getSizeOfLog(); index++) {
			if (selectionModel.isSelectedIndex(index) == true) {
				// if entry is currently selected --> deselect
				selectionModel.removeSelectionInterval(index, index);
			} else {
				// if entry is currently not selected --> select
				selectionModel.addSelectionInterval(index, index);
			}
		}
		hideAllMetrics();
		updateResults();
	}

	// ///////////////DISPLAY METRICS METHODS/////////////////////

	/**
	 * Retrieves the values of process-related metrics, based on the process
	 * instances in piList and displays these on the UI.
	 * 
	 * @param piList
	 *            ArrayList: Process instances
	 */
	private void displayProcessMetrics(ArrayList piList) {
		try {
			// calculate the process metrics, based on the selected instances,
			// using the advancedSettings on how to deal with fitting.
			replayResult.calculateMetrics(piList, advancedSettings[0]);
			// initialize the processTable
			OneMetricTableModel ptm = new OneMetricTableModel();
			ptm.setHeadings("", "Throughput time (" + timeSort + ")");
			DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
			dtcr.setBackground(new Color(235, 235, 235));
			processTable.setModel(ptm);
			processTable.setPreferredSize(new Dimension(240, 112));
			processTable.getColumnModel().getColumn(0).setPreferredWidth(50);
			processTable.getColumnModel().getColumn(0).setMaxWidth(150);
			processTable.getTableHeader().setFont(
					new Font("SansSerif", Font.BOLD, 12));
			processTable.getColumnModel().getColumn(0).setCellRenderer(dtcr);
			processTable.setBorder(BorderFactory.createEtchedBorder());
			// initialize labels
			arrivalProcessLabel.setText("Arrival rate:");
			rateProcessLabel.setText(formatString(replayResult.getArrivalRate()
					* timeDivider, decimalPlaces)
					+ " cases per "
					+ timeSort.substring(0, timeSort.length() - 1));
			casesProcessLabel.setText(piList.size() + " cases");
			completedLabel
					.setText(replayResult.getProperFrequency() + " cases");

			// fill the first column of the process table
			processTable.setValueAt("avg", 0, 0);
			processTable.setValueAt("min", 1, 0);
			processTable.setValueAt("max", 2, 0);
			processTable.setValueAt("stdev", 3, 0);
			DecimalFormat df = new DecimalFormat("0.00");
			int[] sizes = replayResult.getSizes(fastestProcessPercentage,
					slowestProcessPercentage);
			processTable.setValueAt("fast "
					+ df.format(fastestProcessPercentage) + "% (" + sizes[0]
					+ ")", 4, 0);
			processTable.setValueAt("slow "
					+ df.format(slowestProcessPercentage) + "%(" + sizes[1]
					+ ")", 5, 0);
			processTable.setValueAt(
					"normal "
							+ df.format(100 - fastestProcessPercentage
									- slowestProcessPercentage) + "%("
							+ sizes[2] + ")", 6, 0);

			// place calculated values in the table
			processTable.setValueAt(formatString(replayResult
					.getMeanThroughputTime()
					/ timeDivider, decimalPlaces), 0, 1);
			processTable.setValueAt(formatString((replayResult
					.getMinThroughputTime() * 1.0)
					/ timeDivider, decimalPlaces), 1, 1);
			processTable.setValueAt(formatString((replayResult
					.getMaxThroughputTime() * 1.0)
					/ timeDivider, decimalPlaces), 2, 1);
			processTable.setValueAt(formatString(replayResult
					.getStdevThroughputTime()
					/ timeDivider, decimalPlaces), 3, 1);
			double[] avgTimes = replayResult.getAverageTimes(
					fastestProcessPercentage, slowestProcessPercentage);
			processTable.setValueAt(formatString(avgTimes[0] / timeDivider,
					decimalPlaces), 4, 1);
			processTable.setValueAt(formatString(avgTimes[1] / timeDivider,
					decimalPlaces), 5, 1);
			processTable.setValueAt(formatString(avgTimes[2] / timeDivider,
					decimalPlaces), 6, 1);
			processTable.revalidate();
		} catch (Exception ex) {
			Message.add(
					"Program exception while calculating process metrics.\n"
							+ ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * Calculates the metrics belonging to a place and displays the results.
	 * 
	 * @param place
	 *            ExtendedPlace: the place of which the values of metrics are
	 *            calculated.
	 */
	private void displayPlaceMetrics(ExtendedPlace place) {
		ArrayList instanceIDList = getSelectedInstanceIDs();
		tablePanel.removeAll();
		tablePanel.add(placeTable.getTableHeader(), BorderLayout.PAGE_START);
		tablePanel.add(placeTable, BorderLayout.CENTER);
		try {
			// calculate place metrics
			place.calculateMetrics(instanceIDList, advancedSettings[1],
					failedInstances);
			// initialize the placeTable
			ThreeMetricsTableModel ptm = new ThreeMetricsTableModel();
			ptm.setHeadings("", "Waiting time (" + timeSort + ")",
					"Synchronization time (" + timeSort + ")", "Sojourn time ("
							+ timeSort + ")");
			placeTable.setModel(ptm);
			placeTable.setBorder(BorderFactory.createEtchedBorder());
			placeTable.setPreferredSize(new Dimension(480, 112));
			placeTable.setMinimumSize(new Dimension(480, 112));
			placeTable.getColumnModel().getColumn(0).setPreferredWidth(60);
			placeTable.getColumnModel().getColumn(0).setMaxWidth(150);
			placeTable.getColumnModel().getColumn(2).setPreferredWidth(100);
			placeTable.setRowSelectionAllowed(false);
			placeTable.getTableHeader().setFont(
					new Font("SansSerif", Font.BOLD, 12));
			DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
			dtcr.setBackground(new Color(235, 235, 235));
			dtcr.setFont(new Font("SansSerif", Font.BOLD, 12));
			placeTable.getColumnModel().getColumn(0).setCellRenderer(dtcr);

			// place time metrics in the table
			placeTable.setValueAt(formatString(place.getMeanWaitingTime()
					/ timeDivider, decimalPlaces), 0, 1);
			placeTable.setValueAt(formatString(place
					.getMeanSynchronizationTime()
					/ timeDivider, decimalPlaces), 0, 2);
			placeTable.setValueAt(formatString(place.getMeanSojournTime()
					/ timeDivider, decimalPlaces), 0, 3);
			placeTable.setValueAt(formatString(
					(place.getMinWaitingTime() * 1.0) / timeDivider,
					decimalPlaces), 1, 1);
			placeTable.setValueAt(formatString((place
					.getMinSynchronizationTime() * 1.0)
					/ timeDivider, decimalPlaces), 1, 2);
			placeTable.setValueAt(formatString(
					(place.getMinSojournTime() * 1.0) / timeDivider,
					decimalPlaces), 1, 3);
			placeTable.setValueAt(formatString(
					(place.getMaxWaitingTime() * 1.0) / timeDivider,
					decimalPlaces), 2, 1);
			placeTable.setValueAt(formatString((place
					.getMaxSynchronizationTime() * 1.0)
					/ timeDivider, decimalPlaces), 2, 2);
			placeTable.setValueAt(formatString(
					(place.getMaxSojournTime() * 1.0) / timeDivider,
					decimalPlaces), 2, 3);
			placeTable.setValueAt(formatString(place.getStdevWaitingTime()
					/ timeDivider, decimalPlaces), 3, 1);
			placeTable.setValueAt(formatString(place
					.getStdevSynchronizationTime()
					/ timeDivider, decimalPlaces), 3, 2);
			placeTable.setValueAt(formatString(place.getStdevSojournTime()
					/ timeDivider, decimalPlaces), 3, 3);
			double[] avgTimes = place.getAvgTimes(fastestPlacePercentage,
					slowestPlacePercentage);
			placeTable.setValueAt(formatString(avgTimes[0] / timeDivider,
					decimalPlaces), 4, 1);
			placeTable.setValueAt(formatString(avgTimes[1] / timeDivider,
					decimalPlaces), 4, 2);
			placeTable.setValueAt(formatString(avgTimes[2] / timeDivider,
					decimalPlaces), 4, 3);
			placeTable.setValueAt(formatString(avgTimes[3] / timeDivider,
					decimalPlaces), 5, 1);
			placeTable.setValueAt(formatString(avgTimes[4] / timeDivider,
					decimalPlaces), 5, 2);
			placeTable.setValueAt(formatString(avgTimes[5] / timeDivider,
					decimalPlaces), 5, 3);
			placeTable.setValueAt(formatString(avgTimes[6] / timeDivider,
					decimalPlaces), 6, 1);
			placeTable.setValueAt(formatString(avgTimes[7] / timeDivider,
					decimalPlaces), 6, 2);
			placeTable.setValueAt(formatString(avgTimes[8] / timeDivider,
					decimalPlaces), 6, 3);
			DecimalFormat df = new DecimalFormat("0.00");
			int[] sizes = place.getSizes(fastestPlacePercentage,
					slowestPlacePercentage);
			placeTable.setValueAt("fast " + df.format(fastestPlacePercentage)
					+ "%(" + sizes[0] + ")", 4, 0);
			placeTable.setValueAt("slow " + df.format(slowestPlacePercentage)
					+ "%(" + sizes[1] + ")", 5, 0);
			placeTable.setValueAt("normal "
					+ df.format(100 - fastestPlacePercentage
							- slowestPlacePercentage) + "%(" + sizes[2] + ")",
					6, 0);
			titleLabel
					.setText("Performance information of the selected place:");
			// display frequency of visits
			freqObjectLabel.setText(place.getFrequency() + " visits");
			// display the arrival rate
			ratePlaceLabel.setText(formatString(place.getArrivalRate()
					* timeDivider, decimalPlaces)
					+ " visits per "
					+ timeSort.substring(0, timeSort.length() - 1));
			placeTable.getTableHeader().setVisible(true);
			titleLabel.setVisible(true);
			numberObjectLabel.setVisible(true);
			freqObjectLabel.setVisible(true);
			arrivalPlaceLabel.setVisible(true);
			ratePlaceLabel.setVisible(true);
			changePercentagesButton.setVisible(true);
			changePercentagesButton.setPreferredSize(new Dimension(170, 25));
			exportButton.setVisible(true);
			placeTable.repaint();
			metricsBottomPanel.revalidate();
		} catch (Exception ex) {
			Message.add("An internal error occured while calculating "
					+ "place metrics.\n" + ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * Calculates and displays values of 'time in between' metrics of two
	 * transitions.
	 * 
	 * @param lastTransition
	 *            ExtendedTransition: one of the two transitions
	 * @param otherTransition
	 *            ExtendedTransition: one of the two transitions
	 */
	private void displayTransitionMetrics(ExtendedTransition lastTransition,
			ExtendedTransition otherTransition) {
		// clear the table Panel
		tablePanel.removeAll();
		// create a transition analysis object
		TransitionAnalysis ta = new TransitionAnalysis(lastTransition,
				otherTransition);
		try {
			// calculate the time-metrics
			ta.calculateMetrics(getSelectedInstanceIDs(), advancedSettings[2],
					failedInstances);
			// initialize the transition time-metrics table
			OneMetricTableModel ttm = new OneMetricTableModel();
			ttm.setHeadings("", "Time in between (" + timeSort + ")");
			transitionTable = new JTable(ttm);
			transitionTable.setBorder(BorderFactory.createEtchedBorder());
			transitionTable.setPreferredSize(new Dimension(300, 112));
			transitionTable.setMinimumSize(new Dimension(280, 112));
			transitionTable.getColumnModel().getColumn(0).setPreferredWidth(80);
			transitionTable.getColumnModel().getColumn(0).setMaxWidth(150);
			transitionTable.setRowSelectionAllowed(false);
			transitionTable.getTableHeader().setFont(
					new Font("SansSerif", Font.BOLD, 12));
			DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
			dtcr.setBackground(new Color(235, 235, 235));
			dtcr.setFont(new Font("SansSerif", Font.BOLD, 12));
			transitionTable.getColumnModel().getColumn(0).setCellRenderer(dtcr);
			// add the table to the tablePanel
			tablePanel.add(transitionTable.getTableHeader(),
					BorderLayout.PAGE_START);
			tablePanel.add(transitionTable, BorderLayout.CENTER);

			// frequency of process instances in which both transitions occur
			freqObjectLabel.setText(ta.getFrequency() + "" + " cases");
			// fill table with 'in-between-times'
			transitionTable.setValueAt(formatString(ta.getMeanTime()
					/ timeDivider, decimalPlaces), 0, 1);
			transitionTable.setValueAt(formatString((ta.getMinTime() * 1.0)
					/ timeDivider, decimalPlaces), 1, 1);
			transitionTable.setValueAt(formatString((ta.getMaxTime() * 1.0)
					/ timeDivider, decimalPlaces), 2, 1);
			transitionTable.setValueAt(formatString(ta.getStdevTimeInBetween()
					/ timeDivider, decimalPlaces), 3, 1);
			// place average of fastest traces in the table
			DecimalFormat df = new DecimalFormat("0.00");
			int[] sizes = ta.getSizes(fastestBetweenPercentage,
					slowestBetweenPercentage);
			transitionTable.setValueAt("fast "
					+ df.format(fastestBetweenPercentage) + "%(" + sizes[0]
					+ ")", 4, 0);
			transitionTable.setValueAt("slow "
					+ df.format(slowestBetweenPercentage) + "%(" + sizes[1]
					+ ")", 5, 0);
			transitionTable.setValueAt(
					"normal "
							+ df.format(100 - fastestBetweenPercentage
									- slowestBetweenPercentage) + "%("
							+ sizes[2] + ")", 6, 0);
			double[] avgTimes = ta.getAverageTimes(fastestBetweenPercentage,
					slowestBetweenPercentage);
			transitionTable.setValueAt(formatString(avgTimes[0] / timeDivider,
					decimalPlaces), 4, 1);
			// place average of slowest traces in the table
			transitionTable.setValueAt(formatString(avgTimes[1] / timeDivider,
					decimalPlaces), 5, 1);
			// place average of other traces in the table
			transitionTable.setValueAt(formatString(avgTimes[2] / timeDivider,
					decimalPlaces), 6, 1);

			// make metrics visible
			changePercentagesButton.setVisible(true);
			exportButton.setVisible(true);
			titleLabel.setVisible(true);
			titleLabel
					.setText("Performance information of the selected transitions:");
			numberObjectLabel.setVisible(true);
			freqObjectLabel.setVisible(true);
			transitionTable.repaint();
			metricsBottomPanel.revalidate();
		} catch (Exception ex) {
			Message.add("An internal error occured while calculating"
					+ " time in between metrics.\n" + ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * Calculates metrics related to activities, such as waiting time, execution
	 * time and throughput time. and displays these on the metricsBottomPanel,
	 * 
	 * @param activity
	 *            ExtendedActivity: the activity involved
	 */
	private void displayActivityMetrics(ExtendedActivity activity) {
		tablePanel.removeAll();
		try {
			// calculate time-metrics of the activity based on the selected
			// instances
			activity.calculateMetrics(getSelectedInstanceIDs(),
					advancedSettings[3], failedInstances);
			activity.checkWhichMetricsToUse();
			// initialize the activityTable
			ThreeMetricsRowExtraTableModel atm = new ThreeMetricsRowExtraTableModel();
			atm.setHeadings("", "Waiting time (" + timeSort + ")", "Execution"
					+ " time (" + timeSort + ")", "Sojourn time (" + timeSort
					+ ")");
			activityTable.setModel(atm);
			activityTable.setBorder(BorderFactory.createEtchedBorder());
			activityTable.setPreferredSize(new Dimension(480, 128));
			activityTable.setMinimumSize(new Dimension(480, 128));
			activityTable.getColumnModel().getColumn(0).setPreferredWidth(60);
			activityTable.getColumnModel().getColumn(0).setMaxWidth(150);
			DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
			dtcr.setBackground(new Color(235, 235, 235));
			activityTable.getColumnModel().getColumn(0).setCellRenderer(dtcr);
			activityTable.setRowSelectionAllowed(false);
			activityTable.getTableHeader().setFont(
					new Font("SansSerif", Font.BOLD, 12));

			/*
			 * Iterator entries = mapping.entrySet().iterator(); while
			 * (entries.hasNext()) { Map.Entry entr = (Map.Entry)
			 * entries.next(); if (entr.getKey() instanceof ExtendedTransition)
			 * { ExtendedTransition transition = (ExtendedTransition)
			 * entr.getKey(); ExtendedActivity act =
			 * transition.getAssociatedActivity(); if (act != null) {
			 * act.calculateMetrics(getSelectedInstanceIDs(),
			 * advancedSettings[3], failedInstances);
			 * act.checkWhichMetricsToUse(); act.getArrivalRate(); } } }
			 */
			// fill table with the calculated activity time-metrics
			TableCellRenderer renderer = new CustomTableCellRenderer(activity
					.getBoundWaitingUsed(), activity.getBoundExecutionUsed(),
					activity.getBoundSojournUsed());
			activityTable.setDefaultRenderer(Class.forName("java.lang.String"),
					renderer);
			activityTable.setValueAt(formatString(activity.getMeanWaitTime()
					/ timeDivider, decimalPlaces), 0, 1);
			activityTable.setValueAt(formatString(activity
					.getMeanExecutionTime()
					/ timeDivider, decimalPlaces), 0, 2);
			activityTable.setValueAt(formatString(activity.getMeanSojournTime()
					/ timeDivider, decimalPlaces), 0, 3);
			activityTable.setValueAt(formatString(activity.getMinWaitTime()
					/ timeDivider, decimalPlaces), 1, 1);
			activityTable.setValueAt(formatString(activity
					.getMinExecutionTime()
					/ timeDivider, decimalPlaces), 1, 2);
			activityTable.setValueAt(formatString(activity.getMinSojournTime()
					/ timeDivider, decimalPlaces), 1, 3);
			activityTable.setValueAt(formatString(activity.getMaxWaitTime()
					/ timeDivider, decimalPlaces), 2, 1);
			activityTable.setValueAt(formatString(activity
					.getMaxExecutionTime()
					/ timeDivider, decimalPlaces), 2, 2);
			activityTable.setValueAt(formatString(activity.getMaxSojournTime()
					/ timeDivider, decimalPlaces), 2, 3);
			activityTable.setValueAt(formatString(activity.getStdevWaitTime()
					/ timeDivider, decimalPlaces), 3, 1);
			activityTable.setValueAt(formatString(activity
					.getStdevExecutionTime()
					/ timeDivider, decimalPlaces), 3, 2);
			activityTable.setValueAt(formatString(activity
					.getStdevSojournTime()
					/ timeDivider, decimalPlaces), 3, 3);
			double[] avgWaitTimes = activity.getAvgWaitTimes(
					fastestActivityPercentage, slowestActivityPercentage);
			double[] avgExecutionTimes = activity.getAvgExecutionTimes(
					fastestActivityPercentage, slowestActivityPercentage);
			double[] avgThroughputTimes = activity.getAvgSojournTimes(
					fastestActivityPercentage, slowestActivityPercentage);
			activityTable.setValueAt(formatString(
					avgWaitTimes[0] / timeDivider, decimalPlaces), 4, 1);
			activityTable.setValueAt(formatString(avgExecutionTimes[0]
					/ timeDivider, decimalPlaces), 4, 2);
			activityTable.setValueAt(formatString(avgThroughputTimes[0]
					/ timeDivider, decimalPlaces), 4, 3);
			activityTable.setValueAt(formatString(
					avgWaitTimes[1] / timeDivider, decimalPlaces), 5, 1);
			activityTable.setValueAt(formatString(avgExecutionTimes[1]
					/ timeDivider, decimalPlaces), 5, 2);
			activityTable.setValueAt(formatString(avgThroughputTimes[1]
					/ timeDivider, decimalPlaces), 5, 3);
			activityTable.setValueAt(formatString(
					avgWaitTimes[2] / timeDivider, decimalPlaces), 6, 1);
			activityTable.setValueAt(formatString(avgExecutionTimes[2]
					/ timeDivider, decimalPlaces), 6, 2);
			activityTable.setValueAt(formatString(avgThroughputTimes[2]
					/ timeDivider, decimalPlaces), 6, 3);
			activityTable.setValueAt(activity.getFrequencyWait() + "", 7, 1);
			activityTable.setValueAt(activity.getFrequencyExecution() + "", 7,
					2);
			activityTable.setValueAt(activity.getFrequencySojourn() + "", 7, 3);

			DecimalFormat df = new DecimalFormat("0.00");
			activityTable.setValueAt("fast "
					+ df.format(fastestActivityPercentage) + "%", 4, 0);
			activityTable.setValueAt("slow "
					+ df.format(slowestActivityPercentage) + "%", 5, 0);
			activityTable.setValueAt("normal "
					+ df.format(100 - fastestActivityPercentage
							- slowestActivityPercentage) + "%", 6, 0);
			ratePlaceLabel.setText(formatString(activity.getArrivalRate()
					* timeDivider, decimalPlaces)
					+ " visits per "
					+ timeSort.substring(0, timeSort.length() - 1));
			// add the table to the tablePanel
			tablePanel.add(activityTable.getTableHeader(),
					BorderLayout.PAGE_START);
			tablePanel.add(activityTable, BorderLayout.CENTER);
			// display the title
			titleLabel.setText("Performance information of activity "
					+ activity.getName() + ":");
			titleLabel.setVisible(true);
			arrivalPlaceLabel.setVisible(true);
			ratePlaceLabel.setVisible(true);
			changePercentagesButton.setVisible(true);
			exportButton.setVisible(true);
			activityTable.repaint();
			metricsBottomPanel.revalidate();
		} catch (Exception ex) {
			Message.add("An internal error occured while calculating "
					+ " activity metrics.\n" + ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * Hides all place/transition/activity-metrics
	 */
	private void hideAllMetrics() {
		tablePanel.removeAll();
		exportButton.setVisible(false);
		changePercentagesButton.setVisible(false);
		titleLabel.setVisible(false);
		numberObjectLabel.setVisible(false);
		freqObjectLabel.setVisible(false);
		arrivalPlaceLabel.setVisible(false);
		ratePlaceLabel.setVisible(false);
		metricsBottomPanel.revalidate();
	}

	/**
	 * Formats double values to display at most the given number of decimal
	 * places.
	 * 
	 * @param val
	 *            double: value that is formatted to String
	 * @param places
	 *            int: number of decimal places displayed
	 * @return String
	 */
	public String formatString(double val, int places) {
		String cur;
		DecimalFormat df;
		double bound = Math.pow(10.0, (0 - places));
		String tempString = "0";
		for (int i = 0; i < places - 1; i++) {
			tempString += "#";
		}
		if ((val != 0.0) && (val < bound)) {
			// display scientific notation
			if (places == 0) {
				df = new DecimalFormat("0E0");
			} else {
				df = new DecimalFormat("0." + tempString + "E0");
			}
			cur = df.format(val);
		} else {
			if (places == 0) {
				df = new DecimalFormat("0");
			} else {
				df = new DecimalFormat("0." + tempString);
			}
			cur = df.format(val);
		}
		return cur;
	}

	// //////// INTERFACE IMPLEMENTATION RELATED METHODS //////////

	/**
	 * Adds the particular time-metrics for the highlevelactivity of a
	 * transition to the simulation model.
	 * 
	 * @param kindTimingInfo
	 *            String <code>"waiting"</code> when the provided time-metrics
	 *            deal about waiting time. <code>execution</code> when the
	 *            provided time-metrics deal about execution time and
	 *            <code>"sojourn"</code> when the provided time-metrics deal
	 *            about sojourn time
	 * @param activity
	 *            HLActivity the highlevelactivity of a transition
	 * @param mean
	 *            double the mean
	 * @param min
	 *            double the minimum
	 * @param max
	 *            double the maximum
	 * @param stDev
	 *            double the standard deviation
	 * @param constVal
	 *            double the constant value
	 * @param arrivalRate
	 *            double the arrival rate
	 */
	private void addTimingInfoForTransition(String kindTimingInfo,
			HLActivity activity, double mean, double min, double max,
			double stDev, double constVal, double arrivalRate) {
		// in case that NaN is provided for either mean, min, max or stDev the
		// default value needs to be used
		double constFinal = 0.0;
		double meanFinal = 0.0;
		double varianceFinal = 0.0;
		double minFinal = 0.0;
		double maxFinal = 0.0;
		double intensityFinal = 0.0;
		if (!(Double.isNaN(mean) || Double.isInfinite(mean))) {
			meanFinal = mean;
		}
		if (!(Double.isNaN(min) || Double.isInfinite(min))) {
			minFinal = min;
		}
		if (!(Double.isNaN(max) || Double.isInfinite(max))) {
			maxFinal = max;
		}
		if (!(Double.isNaN(stDev) || Double.isInfinite(stDev))) {
			varianceFinal = stDev * stDev;
		}
		if (!(Double.isNaN(constVal) || Double.isInfinite(constVal))) {
			constFinal = constVal;
		}
		if (!(Double.isNaN(arrivalRate) || Double.isInfinite(arrivalRate))) {
			intensityFinal = arrivalRate;
		}

		HLGeneralDistribution dist = new HLGeneralDistribution(constFinal,
				meanFinal, varianceFinal, minFinal, maxFinal, intensityFinal,
				HLDistribution.DistributionEnum.NORMAL_DISTRIBUTION);

		if (kindTimingInfo.equals("waiting")) {
			activity.setWaitingTime(dist);
		} else if (kindTimingInfo.equals("execution")) {
			activity.setExecutionTime(dist);
		} else if (kindTimingInfo.equals("sojourn")) {
			activity.setSojournTime(dist);
		}
	}

	/**
	 * Adds the time-metrics for the process and the transitions to the
	 * simulation model. Furthermore, the possibility dependencies and frequency
	 * dependencies for a place with multiple outgoing arcs are added to the
	 * simulation model
	 * 
	 * @param logTraces
	 *            ArrayList list of process instances
	 * @param logTraceIDs
	 *            ArrayList list of IDs of the process instances
	 */
	private void calculateValuesForSimulationModel(ArrayList logTraces,
			ArrayList logTraceIDs) {
		// calculateValuesForSimulationModel(extendedLog.getDiagnosticLogTraces(),
		// extendedLog.getLogTraceIDs());
		// update the simulation model
		fillProcessHighLevelPN(logTraces);
		// fill the transitions in the simulation model with the calculated
		// activity time-metrics
		fillTransitionsHighLevelPN(mapping.entrySet(), logTraceIDs);
		// add frequency and possibility dependencies to the simulation model
		addDependenciesToSimulationModel(mapping.entrySet(), logTraceIDs);
	}

	/**
	 * Adds the frequency and possibility dependencies to the simulation model
	 * 
	 * @param entrySet
	 *            Set the places and transitions in the petri net
	 * @param logTraceIDs
	 *            ArrayList list of IDs of the process instances
	 */
	private void addDependenciesToSimulationModel(Set<Map.Entry> entrySet,
			ArrayList logTraceIDs) {
		// get the places which have more than one outgoing arc
		Iterator entries = entrySet.iterator();
		while (entries.hasNext()) {
			Map.Entry entr = (Map.Entry) entries.next();
			if (entr.getKey() instanceof ExtendedPlace) {
				ExtendedPlace place = (ExtendedPlace) entr.getKey();
				if (place.outDegree() > 1) {
					// check whether in the process a choice exists for 'place'
					HLChoice choice = highLevelPN.findChoice(place);
					if (choice != null) {
						Enumeration outEdges = place.outEdgeElements();
						while (outEdges.hasMoreElements()) {
							ExtendedPNEdge outEdge = (ExtendedPNEdge) outEdges
									.nextElement();

							// TODO: check and remove

							// HLChoice choice = null;
							// boolean choiceCreated = false;
							// Iterator<HLChoice> choicesIt =
							// highLevelPN.getHLProcess().getChoices().iterator();
							// while (choicesIt.hasNext()) {
							// HLChoice choiceIt = choicesIt.next();
							// if (choiceIt.getChoiceNode().equals(place)) {
							// // a highlevelchoice exists for this place in the
							// process.
							// choice = choiceIt;
							// break;
							// }
							// }
							// // the case that choice is still null, a
							// highlevelchoice for 'place' does not exist
							// // so, create a new one
							// if (choice == null) {
							// //choice = new HLChoice(place);
							// choice = new HLChoice(place.getIdentifier(),
							// highLevelPN.getHLProcess());
							// choiceCreated = true;
							// }

							// add the choice based on frequencies
							HLActivity activity = highLevelPN
									.findActivity((Transition) outEdge
											.getDest());
							HLCondition cond = choice.getCondition(activity
									.getID());
							if (outEdge.getFrequency(logTraceIDs,
									advancedSettings[1], failedInstances) > 0) {
								cond.setFrequency(outEdge.getFrequency(
										logTraceIDs, advancedSettings[1],
										failedInstances));
							}
							// add the choice based on probabilities
							cond.setProbability(outEdge.getProbability(
									logTraceIDs, place
											.getTotalOutEdgeFrequency(
													logTraceIDs,
													advancedSettings[1],
													failedInstances),
									advancedSettings[1], failedInstances));

							// // first check whether already a choice based on
							// probabilities exists for this highlevelactivity
							// HighLevelProbabilityDependency prob = null;
							// Iterator<HighLevelProbabilityDependency> probsIt
							// = choice.getProbabilityDependencies().iterator();
							// while (probsIt.hasNext()) {
							// HighLevelProbabilityDependency probIt =
							// probsIt.next();
							// if (probIt.getSourceNode().equals(place) &&
							// probIt.getTargetNode().equals( (Transition)
							// outEdge.getDest())) {
							// prob = probIt;
							// break;
							// }
							// }
							// if (prob == null) {
							// // prob is still null, create a new one
							// prob = new
							// HLProbabilityDependency(outEdge.getProbability(logTraceIDs,
							// place.getTotalOutEdgeFrequency(logTraceIDs,
							// advancedSettings[1], failedInstances),
							// advancedSettings[1], failedInstances), place,
							// (Transition) outEdge.getDest());
							// choice.addProbabilityDependency(prob);
							// }
							// else {
							// // set a new value for the probability
							// prob.setProbability(outEdge.getProbability(logTraceIDs,
							// place.getTotalOutEdgeFrequency(logTraceIDs,
							// advancedSettings[1], failedInstances),
							// advancedSettings[1], failedInstances));
							// }
							// // add the choice to the process
							// if (choiceCreated) {
							// highLevelPN.addChoice(choice);
							// }
						}
					}
				}
			}
		}
	}

	/**
	 * Add the calculated process time-metrics to the simulation model
	 * 
	 * @param logTraces
	 *            ArrayList the process instances
	 */
	private void fillProcessHighLevelPN(ArrayList logTraces) {
		try {
			replayResult.calculateMetrics(logTraces, advancedSettings[0]);
			// add the arrival rate to the HighLevelProcess model
			double arrivalRate = (double) replayResult.getArrivalRate()
					* (double) timeDivider;
			SummaryStatistics arrivalStats = replayResult.getArrivalStats();

			HLGeneralDistribution dist = new HLGeneralDistribution(arrivalStats
					.getMean()
					/ (double) timeDivider, arrivalStats.getMean()
					/ (double) timeDivider, arrivalStats.getVariance()
					/ (double) timeDivider, arrivalStats.getMin()
					/ (double) timeDivider, arrivalStats.getMax()
					/ (double) timeDivider, arrivalRate,
					HLDistribution.DistributionEnum.EXPONENTIAL_DISTRIBUTION);

			highLevelPN.getHLProcess().getGlobalInfo().setCaseGenerationScheme(
					dist);
			// set the time unit of the HighLevelProcess model
			// different time strings: "milliseconds", "seconds", "minutes",
			// "hours", "days", "weeks", "months", "years"
			if (timeSort.equals("milliseconds")) {
				highLevelPN.getHLProcess().getGlobalInfo().setTimeUnit(
						HLTypes.TimeUnit.MILLISECONDS);
			} else if (timeSort.equals("seconds")) {
				highLevelPN.getHLProcess().getGlobalInfo().setTimeUnit(
						HLTypes.TimeUnit.SECONDS);
			} else if (timeSort.equals("minutes")) {
				highLevelPN.getHLProcess().getGlobalInfo().setTimeUnit(
						HLTypes.TimeUnit.MINUTES);
			} else if (timeSort.equals("hours")) {
				highLevelPN.getHLProcess().getGlobalInfo().setTimeUnit(
						HLTypes.TimeUnit.HOURS);
			} else if (timeSort.equals("days")) {
				highLevelPN.getHLProcess().getGlobalInfo().setTimeUnit(
						HLTypes.TimeUnit.DAYS);
			} else if (timeSort.equals("weeks")) {
				highLevelPN.getHLProcess().getGlobalInfo().setTimeUnit(
						HLTypes.TimeUnit.WEEKS);
			} else if (timeSort.equals("months")) {
				highLevelPN.getHLProcess().getGlobalInfo().setTimeUnit(
						HLTypes.TimeUnit.MONTHS);
			} else if (timeSort.equals("years")) {
				highLevelPN.getHLProcess().getGlobalInfo().setTimeUnit(
						HLTypes.TimeUnit.YEARS);
			} else {
				// use default value
				highLevelPN.getHLProcess().getGlobalInfo().setTimeUnit(
						HLTypes.TimeUnit.HOURS);
			}
		} catch (Exception e) {
			Message.add(
					"Program exception while calculating proces performance metrics for the "
							+ "simulation model:\n" + e.toString(), 2);
			e.printStackTrace();

		}
	}

	/**
	 * Add the calculated activity time-metrics to the transitions in the
	 * simulation model
	 * 
	 * @param entrySet
	 *            Set the transitions in the petri net
	 * @param piList
	 *            ArrayList list of process instances
	 */
	private void fillTransitionsHighLevelPN(Set<Map.Entry> entrySet,
			ArrayList piList) {
		Iterator entries = entrySet.iterator();
		while (entries.hasNext()) {
			Map.Entry entr = (Map.Entry) entries.next();
			if (entr.getKey() instanceof ExtendedTransition) {
				ExtendedTransition transition = (ExtendedTransition) entr
						.getKey();
				if (!transition.isInvisibleTask()) {
					ExtendedActivity activity = transition
							.getAssociatedActivity();
					activity.calculateMetrics(piList, advancedSettings[3],
							failedInstances);
					activity.checkWhichMetricsToUse();
					// find the corresponding HLActivity for this transition
					// TODO Anne: double check and remove
					// HLActivity hlActivity =
					// highLevelPN.findHLTransition(transition);
					HLActivity hlActivity = highLevelPN
							.findActivity(transition);
					// add the waiting time (if available)
					addTimingInfoForTransition("waiting", hlActivity, activity
							.getMeanWaitTime()
							/ timeDivider, activity.getMinWaitTime()
							/ timeDivider, activity.getMaxWaitTime()
							/ timeDivider, activity.getStdevWaitTime()
							/ timeDivider, activity.getMeanWaitTime()
							/ timeDivider, ((double) 1 / (activity
							.getMeanWaitTime() / timeDivider)));
					// add the execution time (if available)
					addTimingInfoForTransition(
							"execution",
							hlActivity,
							activity.getMeanExecutionTime() / timeDivider,
							activity.getMinExecutionTime() / timeDivider,
							activity.getMaxExecutionTime() / timeDivider,
							activity.getStdevExecutionTime() / timeDivider,
							activity.getMeanExecutionTime() / timeDivider,
							((double) 1 / (activity.getMeanExecutionTime() / timeDivider)));
					// add the sojourn time (at least the upperbound)
					addTimingInfoForTransition("sojourn", hlActivity, activity
							.getMeanSojournTime()
							/ timeDivider, activity.getMinSojournTime()
							/ timeDivider, activity.getMaxSojournTime()
							/ timeDivider, activity.getStdevSojournTime()
							/ timeDivider, activity.getMeanSojournTime()
							/ timeDivider, ((double) 1 / (activity
							.getMeanSojournTime() / timeDivider)));
				}
			}
		}
	}

	/**
	 * Specifiy provided objects of the analysis that can be further used to,
	 * e.g., export an item.
	 * 
	 * @return An Array containing provided objects
	 */
	public ProvidedObject[] getProvidedObjects() {
		// if provided objects are asked before the GUI is actually created the
		// log selection cannot be provided yet
		// furthermore the log selection is only offered if there is something
		// selected
		try {
			if (processInstanceIDsTable != null
					&& processInstanceIDsTable.getSelectionModel()
							.isSelectionEmpty() == false) {
				// fill the high level PN with simulation information
				// fillTransitionsHighLevelPN();
				ProvidedObject[] objects = {
						new ProvidedObject("Petri net model",
								new Object[] { inputPetriNet }),
						new ProvidedObject("Performance Simulation Model No."
								+ simulationModelCounter,
								new Object[] { highLevelPN }),
						new ProvidedObject("Performance visualization",
								new Object[] { new DotFileWriter() {
									public void writeToDot(Writer bw)
											throws IOException {
										extendedPetriNet.writeToDot(bw);
									}
								} }),
						new ProvidedObject("Whole Log",
								new Object[] { inputLog }),
						new ProvidedObject("Log Selection",
								new Object[] { LogReaderFactory.createInstance(
										inputLog, getSelectionStatus()) }) };
				return objects;
			} else {
				ProvidedObject[] objects = {
						new ProvidedObject("Petri net model",
								new Object[] { inputPetriNet }),
						new ProvidedObject("Performance visualization",
								new Object[] { new DotFileWriter() {
									public void writeToDot(Writer bw)
											throws IOException {
										extendedPetriNet.writeToDot(bw);
									}
								} }),
						new ProvidedObject("Whole Log",
								new Object[] { inputLog }) };
				return objects;
			}
		} catch (Exception e) {
			System.err.println("Fatal error creating new log reader instance:");
			System.err.println("(" + this.getClass() + ")");
			e.printStackTrace();
			return null;
		}
	}

	// //////// GRAPPA RELATED METHODS //////////

	/**
	 * Create a mapping from the Petri net graph structure back to the Grappa
	 * nodes. Copied from the template GUI but not used so far.
	 * 
	 * @param mapping
	 *            Map
	 * @param subGraph
	 *            Subgraph
	 */
	private void buildGraphMapping(Map mapping, Subgraph subGraph) {
		Enumeration e = subGraph.nodeElements();
		while (e.hasMoreElements()) {
			Node n = (Node) e.nextElement();
			mapping.put(n.object, n);
		}
		e = subGraph.edgeElements();
		while (e.hasMoreElements()) {
			Edge n = (Edge) e.nextElement();
			mapping.put(n.object, n);
		}
		e = subGraph.subgraphElements();
		while (e.hasMoreElements()) {
			Subgraph n = (Subgraph) e.nextElement();
			buildGraphMapping(mapping, n);
		}
	}

	// //////// INTERNAL CLASS DEFINITIONS //////////

	/**
	 * A custom listener class for the grappa graph panel.
	 */
	public class ExtendedGrappaAdapter extends GrappaAdapter {

		/**
		 * The method called when a mouse click occurs on an element of the
		 * graph panel. Difference with the same method in the
		 * GrappaAdapter-class is that here certain metrics are displayed after
		 * clicking on a place or a transition within the graph (Petri net),
		 * that is displayed on the panel. Furthermore, here the user can only
		 * have one Element selected at a time, except in case the selected
		 * Elements are transitions, the user can select up to two of these.
		 * 
		 * @param subg
		 *            displayed subgraph where action occurred
		 * @param elem
		 *            subgraph element in which action occurred
		 * @param pt
		 *            the point where the action occurred (graph coordinates)
		 * @param modifiers
		 *            mouse modifiers in effect
		 * @param clickCount
		 *            count of mouse clicks that triggered this action
		 * @param panel
		 *            specific panel where the action occurred
		 * 
		 */
		public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt,
				int modifiers, int clickCount, GrappaPanel panel) {
			if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
				if ((clickCount == 1)
						&& ((modifiers & InputEvent.CTRL_MASK) != InputEvent.CTRL_MASK)) {
					// looks like Java has a single click occur on the way to a
					// multiple click, so this code always executes (which is
					// not necessarily a bad thing)
					if (subg.getGraph().isSelectable()) {
						if (modifiers == InputEvent.BUTTON1_MASK) {
							// select element
							if (elem == null) {
								// called when user clicked below/above/next to
								// Petri net
								sb1.setSelectedIndex(0);
								sb2.setSelectedIndex(0);
								sb2.setEnabled(false);
								andLabel.setForeground(Color.gray);
							} else {
								if (elem.object instanceof ExtendedTransition) {
									// the user has clicked on a transition on
									// the panel
									ExtendedTransition temp = (ExtendedTransition) elem.object;
									if (temp.isInvisibleTask()) {
										JOptionPane
												.showMessageDialog(panel,
														"Invisible transitions cannot be selected!");
									} else {
										// only allow selection of visible tasks
										if (elt1 == null
												|| elt1 instanceof ExtendedPlace) {
											// No other transition was selected
											elt1 = (Element) elem.object;
											elt2 = null;
											ExtendedTransition trans = (ExtendedTransition) elt1;
											String transName = "Transition - "
													+ trans
															.getLogEvent()
															.getModelElementName()
													+ " "
													+ trans.getLogEvent()
															.getEventType();
											sb1.setSelectedItem(transName);
										} else {
											// At least one other transition was
											// selected
											if (elt1 == elem.object) {
												// the clicked transition was
												// already selected
												// in sb1
												if (elt2 == null) {
													sb1.setSelectedIndex(0);
													sb2.setSelectedIndex(0);
													sb2.setEnabled(false);
													andLabel
															.setForeground(Color.gray);
												} else {
													elt1 = elt2;
													ExtendedTransition trans = (ExtendedTransition) elt1;
													String transName = "Transition - "
															+ trans
																	.getLogEvent()
																	.getModelElementName()
															+ " "
															+ trans
																	.getLogEvent()
																	.getEventType();
													sb1
															.setSelectedItem(transName);
													sb2.setSelectedIndex(0);
												}
											} else if (elt2 == elem.object) {
												// the clicked transition was
												// already selected in sb2
												elt2 = null;
												sb2.setSelectedIndex(0);
											} else {
												// add clicked transition to the
												// selection
												if (elt2 != null) {
													JOptionPane
															.showMessageDialog(
																	panel,
																	"Already two transitions selected."
																			+ "\nDeselect one first!");
												} else {
													elt2 = (Element) elem.object;
													ExtendedTransition trans1 = (ExtendedTransition) elt1;
													String transName = "Transition - "
															+ trans1
																	.getLogEvent()
																	.getModelElementName()
															+ " "
															+ trans1
																	.getLogEvent()
																	.getEventType();
													ExtendedTransition trans2 = (ExtendedTransition) elt2;
													sb1
															.setSelectedItem(transName);
													transName = "Transition - "
															+ trans2
																	.getLogEvent()
																	.getModelElementName()
															+ " "
															+ trans2
																	.getLogEvent()
																	.getEventType();
													sb2
															.setSelectedItem(transName);
												}
											}
										}
									}
								} else {
									// called when the user has clicked on
									// anything but a
									// transition -> reset lastSelected and
									// otherSelected
									if (elem.object instanceof ExtendedPlace) {
										// the user has clicked on a place on
										// the panel
										if (elem.object == elt1) {
											// the place was already selected
											elt1 = null;
											elt2 = null;
											sb1.setSelectedIndex(0);
										} else {
											elt1 = (Element) elem.object;
											elt2 = null;
											ExtendedPlace place = (ExtendedPlace) elt1;
											sb1.setSelectedItem("Place - "
													+ place.getIdentifier());
										}
									} else {
										// not a place, nor a transition
										// selected
										elt1 = null;
										elt2 = null;
										sb1.setSelectedIndex(0);
									}
								}
							}
						}
					}
				} else {
					// multiple clicks
					// this code executes for each click beyond the first
				}
			}
		}

		/**
		 * The method called when a mouse release occurs on a displayed
		 * subgraph. Difference with the same method in the GrappaAdapter-class
		 * is that here no elements in the displayed graph are selected after
		 * release.
		 * 
		 * @param subg
		 *            displayed subgraph where action occurred
		 * @param elem
		 *            subgraph element in which action occurred
		 * @param pt
		 *            the point where the action occurred (graph coordinates)
		 * @param modifiers
		 *            mouse modifiers in effect
		 * @param pressedElem
		 *            subgraph element in which the most recent mouse press
		 *            occurred
		 * @param pressedPt
		 *            the point where the most recent mouse press occurred
		 *            (graph coordinates)
		 * @param pressedModifiers
		 *            mouse modifiers in effect when the most recent mouse press
		 *            occurred
		 * @param outline
		 *            enclosing box specification from the previous drag
		 *            position (for XOR reset purposes)
		 * @param panel
		 *            specific panel where the action occurred
		 */
		public void grappaReleased(Subgraph subg, Element elem, GrappaPoint pt,
				int modifiers, Element pressedElem, GrappaPoint pressedPt,
				int pressedModifiers, GrappaBox outline, GrappaPanel panel) {
			if (modifiers == InputEvent.BUTTON1_MASK
					&& subg.getGraph().isSelectable()) {
				if (outline != null) {
					// Outline is supplied in the coordinates of the canvas
					panel.recordDragPoints();
					subg.getGraph().paintImmediately();
				}
			} else if (modifiers == (InputEvent.BUTTON1_MASK | InputEvent.CTRL_MASK)
					&& subg.getGraph().isSelectable()) {
				if (outline != null) {
					Graphics2D g2d = (Graphics2D) (panel.getGraphics());
					AffineTransform orig = g2d.getTransform();
					g2d.setTransform(panel.getTransform());
					g2d.setXORMode(Color.darkGray);
					g2d.draw(outline);
					g2d.setPaintMode();
					g2d.setTransform(orig);
				}
			}
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
			return "Log Traces";
		}

		/**
		 * Specify the number of rows.
		 * 
		 * @return The number of traces in the log.
		 */
		public int getRowCount() {
			return extendedLog.getSizeOfLog();
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
			return extendedLog.getLogTraceIDs().get(row);

		}
	}

	/**
	 * Private data structure for tables containing three columns with metrics.
	 */
	private static class ThreeMetricsTableModel extends AbstractTableModel {

		private String[] columnNames = { "", "Waiting time",
				"Synchronization time", "Sojourn time" };
		private Object[][] data = { { "avg", "", "", "" },
				{ "min", "", "", "" }, { "max", "", "", "" },
				{ "stdev", "", "", "" }, { "fast", "", "", "" },
				{ "slow", "", "", "" }, { "normal", "", "", "" } };

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Cells can't be edited
		 */
		public boolean isCellEditable(int row, int col) {
			return false;
		}

		/*
		 * Set value at field[row, col] in the table data can change.
		 */
		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}

		public void setHeadings(String one, String two, String three,
				String four) {
			columnNames[0] = one;
			columnNames[1] = two;
			columnNames[2] = three;
			columnNames[3] = four;
		}
	}

	/**
	 * Private data structure for tables containing three columns with metrics
	 * and is different as ThreeMetricsTableModel in the sense that it contains
	 * one extra row to store frequencies of measurements.
	 */
	private static class ThreeMetricsRowExtraTableModel extends
			AbstractTableModel {

		private String[] columnNames = { "", "Waiting time", "Execution time",
				"Sojourn time" };
		private Object[][] data = { { "avg", "", "", "" },
				{ "min", "", "", "" }, { "max", "", "", "" },
				{ "stdev", "", "", "" }, { "fast", "", "", "" },
				{ "slow", "", "", "" }, { "normal", "", "", "" },
				{ "frequency", "", "", "" } };

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Cells can't be edited
		 */
		public boolean isCellEditable(int row, int col) {
			return false;
		}

		/*
		 * Set value at field[row, col] in the table data can change.
		 */
		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}

		public void setHeadings(String one, String two, String three,
				String four) {
			columnNames[0] = one;
			columnNames[1] = two;
			columnNames[2] = three;
			columnNames[3] = four;
		}
	}

	/**
	 * Private data structure for tables containing one column with metrics
	 * (e.g. throughput time)
	 */
	private static class OneMetricTableModel extends AbstractTableModel {

		private String[] columnNames = { "", "Throughput time" };
		private Object[][] data = { { "avg", "" }, { "min", "" },
				{ "max", "" }, { "stdev", "" }, { "fast", "" }, { "slow", "" },
				{ "normal", "" } };

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		/*
		 * Set value at field[row, col] in the table data can change.
		 */
		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}

		public void setHeadings(String one, String two) {
			columnNames[0] = one;
			columnNames[1] = two;
		}
	}

	public static class NameFilter extends javax.swing.filechooser.FileFilter {
		private String description = "";

		/**
		 * Constructor, sets description to suffix
		 * 
		 * @param suffix
		 *            String
		 */
		public NameFilter(String suffix) {
			description = suffix;
		}

		/**
		 * Returns true if f is a directory or ends with description
		 * 
		 * @param f
		 *            File
		 * @return boolean
		 */
		public boolean accept(File f) {
			if (f.isDirectory() || f.getName().endsWith(description)) {
				return true;
			} else {
				return false;
			}
		}

		public String getDescription() {
			return "*" + description;
		}

	}

	/**
	 * Cell renderer, to color the font of cells red if they contain bound
	 * values instead of exact values.
	 */
	public static class CustomTableCellRenderer extends
			DefaultTableCellRenderer {
		private boolean firstColumn = false;
		private boolean secondColumn = false;
		private boolean thirdColumn = false;

		public CustomTableCellRenderer(boolean firstColumn,
				boolean secondColumn, boolean thirdColumn) {
			super();
			this.firstColumn = firstColumn;
			this.secondColumn = secondColumn;
			this.thirdColumn = thirdColumn;
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component cell = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
			if (column == 1 && firstColumn) {
				cell.setForeground(Color.red);
			} else if (column == 2 && secondColumn) {
				cell.setForeground(Color.red);
			} else if (column == 3 && thirdColumn) {
				cell.setForeground(Color.red);
			} else {
				cell.setForeground(Color.black);
			}
			return cell;

		}
	}

}
