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
 * Copyright (c) 2005 Eindhoven Technical University of Technology
 * All rights reserved.
 */

package org.processmining.analysis.conformance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisGUI;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GuiNotificationTarget;

import att.grappa.Edge;
import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Node;
import att.grappa.Subgraph;

/**
 * Creates the GUI for the "Fitness" tab in the results frame. For the time
 * being also displays the appropriateness metrics (and therefore appears as
 * "Conformance" tab) but they will be moved to separate main categories as soon
 * as the alternative metrics are implemented.
 * 
 * @author arozinat
 */
public class FitnessAnalysisGUI extends AnalysisGUI implements
		GuiNotificationTarget {

	/**
	 * Required for a serializable class (generated quickfix). Not directly
	 * used.
	 */
	private static final long serialVersionUID = 8725966024645509903L;

	// technical attributes
	private DiagnosticPetriNet diagnosticPetriNet;
	private HashMap mapping;
	private ConformanceLogReplayResult replayResult;
	// contains the "precise" net in the sense that only transitions
	// that were executed at least once are
	private PetriNet minimalPetriNet;

	// user interface related attributes
	private JScrollPane modelContainer;
	private JPanel graphPanel = new JPanel(new BorderLayout());
	private JPanel measurementPanel = new JPanel();
	private JPanel bottomPanel = new JPanel(new BorderLayout());
	private JPanel bottomNorthPanel = new JPanel(new BorderLayout());

	private GrappaPanel grappaPanel;
	private JLabel tokensFitnessMeasureLabel = new JLabel("Fitness: ");
	private JLabel tokensFitnessMeasure = new JLabel();
	private JLabel modelMeasuresHeading = new JLabel("Model-related Measures");
	private JLabel logMeasuresHeading = new JLabel("Log-related Measures");
	private JLabel visualizationHeading = new JLabel("Diagnostic Perspective ");
	private JLabel fractionSuccessfullyExecutedLabel = new JLabel(
			"Successful Execution: ");
	private JLabel fractionProperlyCompletedLabel = new JLabel(
			"Proper Completion: ");
	private JLabel fractionSuccessfullyExecuted = new JLabel();
	private JLabel fractionProperlyCompleted = new JLabel();

	private JPanel graphTypePanel = new JPanel();
	private JPanel vizOptionsPanel = new JPanel();
	private GUIPropertyBoolean tokenCounter;
	private GUIPropertyBoolean failedTransitions;
	private GUIPropertyBoolean remainingTransitions;
	private GUIPropertyBoolean pathCoverage;
	private GUIPropertyBoolean passedEdges;
	private GUIPropertyBoolean failedEvents;
	private JComboBox visualizationType = new JComboBox();
	private boolean visualizationTypeOption;

	/**
	 * Creates Fitness GUI with default visualization options.
	 * 
	 * @param analysisResults
	 *            should contain a {@link ConformanceLogReplayResult
	 *            ConformanceLogReplayResult}
	 */
	public FitnessAnalysisGUI(Set<AnalysisResult> analysisResults) {
		this(analysisResults, true, true, false, false, false, true, true);
	}

	/**
	 * Creates the results view for the Fitness category.
	 * 
	 * @param analysisResults
	 *            should contain a {@link ConformanceLogReplayResult
	 *            ConformanceLogReplayResult}
	 * @param tokencounter
	 *            indicates whether token counter visualization option should be
	 *            selected
	 * @param failedtrans
	 *            indicates whether failed transitions visualization option
	 *            should be selected
	 * @param remainingtrans
	 *            indicates whether remaining transitions visualization option
	 *            should be selected
	 * @param pathcov
	 *            indicates whether path coverage visualization option should be
	 *            selected
	 * @param passededges
	 *            indicates whether passed edges visualization option should be
	 *            selected
	 * @param failedevents
	 *            indicates whether failed events visualization option should be
	 *            selected
	 * @param visualizationtype
	 *            indicates which visualization type should be selected (true =
	 *            model view)
	 */
	public FitnessAnalysisGUI(Set<AnalysisResult> analysisResults,
			boolean tokencounter, boolean failedtrans, boolean remainingtrans,
			boolean pathcov, boolean passededges, boolean failedevents,
			boolean visualizationtype) {
		super(analysisResults);

		// set visualization options
		tokenCounter = new GUIPropertyBoolean(
				"Token Counter",
				"Indicate places that had tokens left or missing during log replay",
				tokencounter, this);
		failedTransitions = new GUIPropertyBoolean(
				"Failed Tasks",
				"Indicate all transitions being fired artificially during log replay",
				failedtrans, this);
		remainingTransitions = new GUIPropertyBoolean("Remaining Tasks",
				"Indicate all transitions remaining enabled after log replay",
				remainingtrans, this);
		pathCoverage = new GUIPropertyBoolean("Path Coverage",
				"Indicate all transitions being fired during log replay",
				pathcov, this);
		passedEdges = new GUIPropertyBoolean(
				"Passed Edges",
				"Indicate how many times each edge has been passed during log replay",
				passededges, this);
		failedEvents = new GUIPropertyBoolean(
				"Failed Log Events",
				"Indicate all log events having failed execution during log replay",
				failedevents, this);
		visualizationTypeOption = visualizationtype;

		Iterator<AnalysisResult> results = analysisResults.iterator();
		while (results.hasNext()) {
			AnalysisResult currentResult = results.next();
			if (currentResult != null) {
				if (currentResult.getClass().getName() == "org.processmining.analysis.conformance.ConformanceLogReplayResult") {
					replayResult = (ConformanceLogReplayResult) currentResult;
				}
			} else {
				// an analysis result can be null if the user aborted the
				// computation process.. --> display empty tab
				return;
			}
		}

		diagnosticPetriNet = (DiagnosticPetriNet) replayResult.replayedPetriNet;
		minimalPetriNet = determineMinimalPetriNet(diagnosticPetriNet);

		try {
			// build GUI
			jbInit();
			// connect functionality to GUI elements
			registerGuiActionListener();
		} catch (Exception ex) {
			Message.add(
					"Program exception while displaying conformance check results:\n"
							+ ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * Retrieves the current selection status of this visualization option.
	 * 
	 * @return <code>true</code> if this option is selected, <code>false</code>
	 *         otherwise
	 */
	public boolean getTokenCounterOption() {
		return tokenCounter.getValue();
	}

	/**
	 * Retrieves the current selection status of this visualization option.
	 * 
	 * @return <code>true</code> if this option is selected, <code>false</code>
	 *         otherwise
	 */
	public boolean getFailedTransitionsOption() {
		return failedTransitions.getValue();
	}

	/**
	 * Retrieves the current selection status of this visualization option.
	 * 
	 * @return <code>true</code> if this option is selected, <code>false</code>
	 *         otherwise
	 */
	public boolean getRemainingTransitionsOption() {
		return remainingTransitions.getValue();
	}

	/**
	 * Retrieves the current selection status of this visualization option.
	 * 
	 * @return <code>true</code> if this option is selected, <code>false</code>
	 *         otherwise
	 */
	public boolean getPathCoverageOption() {
		return pathCoverage.getValue();
	}

	/**
	 * Retrieves the current selection status of this visualization option.
	 * 
	 * @return <code>true</code> if this option is selected, <code>false</code>
	 *         otherwise
	 */
	public boolean getPassedEdgesOption() {
		return passedEdges.getValue();
	}

	/**
	 * Retrieves the current selection status of this visualization option.
	 * 
	 * @return <code>true</code> if this option is selected, <code>false</code>
	 *         otherwise
	 */
	public boolean getFailedEventsOption() {
		return failedEvents.getValue();
	}

	/**
	 * Retrieves the current selection status of this visualization option.
	 * 
	 * @return <code>true</code> if the model option is selected,
	 *         <code>false</code> if the log view is chosen
	 */
	public boolean getVisualizationTypeOption() {
		return visualizationTypeOption;
	}

	// ////////////// Private Methods /////////////////////////////////

	/**
	 * Creates a Petri net that only contains those transitions that were never
	 * executed during the last log replay.
	 * 
	 * @param givenNet
	 *            the diagnostic petri net after log replay
	 * @return the Petri net after every transition that never fired during log
	 *         replay was reduced if possible (according to reduction rules)
	 */
	private PetriNet determineMinimalPetriNet(DiagnosticPetriNet givenNet) {
		try {
			// remove Transitions that never fired during log replay
			DiagnosticPetriNet clonedNet = (DiagnosticPetriNet) givenNet
					.clone();
			ArrayList<Transition> toBeRemoved = new ArrayList<Transition>();
			ArrayList<Transition> allTrans = clonedNet.getTransitions();
			DiagnosticTransition trans;
			Iterator<Transition> transIt = allTrans.iterator();
			while (transIt.hasNext()) {
				trans = (DiagnosticTransition) transIt.next();
				if (trans.hasFired() == false) {
					toBeRemoved.add(trans);
				}
			}
			transIt = toBeRemoved.iterator();
			while (transIt.hasNext()) {
				trans = (DiagnosticTransition) transIt.next();
				clonedNet.delTransition(trans);
			}
			// now clean up isolated places
			ArrayList<Place> isolatedPlaces = new ArrayList<Place>();
			ArrayList<Place> allPlaces = clonedNet.getPlaces();
			Iterator<Place> placeIt = allPlaces.iterator();
			Place place;
			while (placeIt.hasNext()) {
				place = placeIt.next();
				if (place.inDegree() == 0 && place.outDegree() == 0) {
					isolatedPlaces.add(place);
				}
			}
			placeIt = isolatedPlaces.iterator();
			while (placeIt.hasNext()) {
				place = placeIt.next();
				clonedNet.delPlace(place);
			}
			return clonedNet;
		} catch (Exception ex) {
			Message.add(
					"A problem occurred during Petri net reduction for minimal net: "
							+ ex.toString(), 2);
			return givenNet;
		}
	}

	/**
	 * Construct the user interface for the conformance check analysis.
	 * 
	 * @throws Exception
	 *             Any exception is just passed to the caller.
	 */
	private void jbInit() throws Exception {

		// set up initial measurements
		if (replayResult.calculateFitness() == true) { // configurated
			float fitness = replayResult.getFitnessMeasure();
			tokensFitnessMeasure.setText("" + fitness);
			// / PLUGIN TEST START
			Message.add("Fitness: " + fitness, Message.TEST);
			// PLUGIN TEST END
		}
		if (replayResult.calculateSuccessfulExecution() == true) { // configurated
			float succExec = replayResult.getFractionOfSuccessfullyExecuted();
			fractionSuccessfullyExecuted.setText("" + succExec);
			// / PLUGIN TEST START
			Message.add("Successfully executed traces: " + succExec,
					Message.TEST);
			// PLUGIN TEST END
		}
		if (replayResult.calculateProperCompletetion() == true) { // configurated
			float propCompl = replayResult.getFractionOfProperlyTerminated();
			fractionProperlyCompleted.setText("" + propCompl);
			// / PLUGIN TEST START
			Message
					.add("Properly completed traces: " + propCompl,
							Message.TEST);
			// PLUGIN TEST END
		}

		modelMeasuresHeading.setForeground(new Color(100, 100, 100));
		logMeasuresHeading.setForeground(new Color(100, 100, 100));
		visualizationHeading.setForeground(new Color(100, 100, 100));

		// set up all help texts
		tokensFitnessMeasure
				.setToolTipText("Degree of fit based on missing and remaining tokens in the model during log replay.");
		fractionSuccessfullyExecuted
				.setToolTipText("The fraction of successfully executed process instances.");
		fractionProperlyCompleted
				.setToolTipText("The fraction of properly completed process instances.");
		visualizationHeading
				.setToolTipText("Change the type of visualization and the measure perspective.");

		// set up diagnostic perspective
		String[] vizualizationModes = { "Model", "Log" };
		visualizationType = new JComboBox(vizualizationModes);
		if (visualizationTypeOption == true) {
			visualizationType.setSelectedIndex(0);
		} else {
			visualizationType.setSelectedIndex(1);
		}
		graphTypePanel.add(visualizationHeading);
		graphTypePanel.add(visualizationType);
		// log replay must have been carried out in order to show diagnostic
		// visualization
		if (replayResult.performLogReplay() == true) { // configurated
			if (visualizationTypeOption == true) {
				vizOptionsPanel.add(tokenCounter.getPropertyPanel());
				vizOptionsPanel.add(failedTransitions.getPropertyPanel());
				vizOptionsPanel.add(remainingTransitions.getPropertyPanel());
				vizOptionsPanel.add(pathCoverage.getPropertyPanel());
				vizOptionsPanel.add(passedEdges.getPropertyPanel());
			} else {
				vizOptionsPanel.add(failedEvents.getPropertyPanel());
			}
			graphTypePanel.add(vizOptionsPanel);
		}

		DisplayState currentVisualization;
		if (this.visualizationTypeOption == true) {
			currentVisualization = DisplayState.MODEL;
			currentVisualization.tokenCouterOption = tokenCounter.getValue();
			currentVisualization.failedTransitionsOption = failedTransitions
					.getValue();
			currentVisualization.remainingTransitionsOption = remainingTransitions
					.getValue();
			currentVisualization.pathCoverageOption = pathCoverage.getValue();
			currentVisualization.passedEdgesOption = passedEdges.getValue();
		} else {
			currentVisualization = DisplayState.LOG;
			currentVisualization.failedEventsOption = failedEvents.getValue();
		}

		grappaPanel = replayResult.getVisualization(currentVisualization);
		grappaPanel.addGrappaListener(new DiagnosticGrappaAdapter());
		mapping = new HashMap();
		buildGraphMapping(mapping, grappaPanel.getSubgraph());

		// build the layout (only the selected options)
		measurementPanel.setLayout(new BoxLayout(measurementPanel,
				BoxLayout.PAGE_AXIS));
		measurementPanel.add(modelMeasuresHeading);
		measurementPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		if (replayResult.calculateFitness() == true) { // configurated
			measurementPanel.add(tokensFitnessMeasureLabel);
			measurementPanel.add(tokensFitnessMeasure);
			measurementPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		}
		measurementPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
				10));

		modelContainer = new JScrollPane(grappaPanel);
		graphPanel.add(modelContainer, BorderLayout.CENTER);
		JSplitPane resultSplitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, graphPanel, measurementPanel);
		resultSplitPane.setDividerLocation(resultSplitPane.getSize().width
				- resultSplitPane.getInsets().right
				- resultSplitPane.getDividerSize()
				- measurementPanel.getWidth());
		resultSplitPane.setOneTouchExpandable(true);
		resultSplitPane.setDividerSize(3);
		resultSplitPane.setResizeWeight(1.0);

		bottomNorthPanel.add(graphTypePanel, BorderLayout.EAST);
		bottomPanel.add(bottomNorthPanel, BorderLayout.NORTH);

		this.setLayout(new BorderLayout());
		this.add(resultSplitPane, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
		this.validate();
		this.repaint();
	}

	/**
	 * Connect GUI elements like, e.g., buttons with functionality to create
	 * interaction.
	 */
	private void registerGuiActionListener() {
		// specify the action to be taken when changing the visualization type
		visualizationType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String vizType = (String) cb.getSelectedItem();
				updateVisualizationType(vizType);
			}
		});
	}

	/**
	 * Update the options panel for the visualization type (called as soon as
	 * the diagnostic perspective has changed).
	 * 
	 * @param newType
	 *            The newly chosen visualizatin type.
	 */
	private void updateVisualizationType(String newType) {
		// change respective GUI parts
		if (newType == "Model") {
			// update visualization options
			if (replayResult.performLogReplay() == true) { // configurated
				vizOptionsPanel.removeAll();
				vizOptionsPanel.add(tokenCounter.getPropertyPanel());
				vizOptionsPanel.add(failedTransitions.getPropertyPanel());
				vizOptionsPanel.add(remainingTransitions.getPropertyPanel());
				vizOptionsPanel.add(pathCoverage.getPropertyPanel());
				vizOptionsPanel.add(passedEdges.getPropertyPanel());
			}
			// update measure types
			measurementPanel.removeAll();
			measurementPanel.add(modelMeasuresHeading);
			measurementPanel.add(Box.createRigidArea(new Dimension(0, 15)));

			if (replayResult.calculateFitness() == true) { // configurated
				measurementPanel.add(tokensFitnessMeasureLabel);
				measurementPanel.add(tokensFitnessMeasure);
				measurementPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			}
		} else if (newType == "Log") {
			// update visualization options
			if (replayResult.performLogReplay() == true) { // configurated
				vizOptionsPanel.removeAll();
				vizOptionsPanel.add(failedEvents.getPropertyPanel());
			}
			// update measure types
			measurementPanel.removeAll();
			measurementPanel.add(logMeasuresHeading);
			measurementPanel.add(Box.createRigidArea(new Dimension(0, 15)));
			if (replayResult.calculateSuccessfulExecution() == true) { // configurated
				measurementPanel.add(fractionSuccessfullyExecutedLabel);
				measurementPanel.add(fractionSuccessfullyExecuted);
				measurementPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			}
			if (replayResult.calculateProperCompletetion() == true) { // configurated
				measurementPanel.add(fractionProperlyCompletedLabel);
				measurementPanel.add(fractionProperlyCompleted);
			}
		} else {
			Message
					.add(
							"Current visualization state could not be determined.\n",
							2);
		}
		measurementPanel.validate();
		measurementPanel.repaint();
		bottomNorthPanel.validate();
		bottomNorthPanel.repaint();
		// update visualization
		updateGUI();
	}

	/**
	 * Updates both the visualization and the conformance measures by taking
	 * only the selected instances into account. If all instances have been
	 * selected, the result corresponds to the initial state.
	 */
	public void updateGUI() {
		try {
			// update the visualization
			DisplayState currentVisualization = DisplayState.MODEL;
			if (((String) visualizationType.getSelectedItem())
					.equals(new String("Model")) == true) {
				currentVisualization = DisplayState.MODEL;
				this.visualizationTypeOption = true;
				currentVisualization.tokenCouterOption = tokenCounter
						.getValue();
				currentVisualization.failedTransitionsOption = failedTransitions
						.getValue();
				currentVisualization.remainingTransitionsOption = remainingTransitions
						.getValue();
				currentVisualization.pathCoverageOption = pathCoverage
						.getValue();
				currentVisualization.passedEdgesOption = passedEdges.getValue();
			} else if (((String) visualizationType.getSelectedItem())
					.equals(new String("Log")) == true) {
				currentVisualization = DisplayState.LOG;
				this.visualizationTypeOption = false;
				currentVisualization.failedEventsOption = failedEvents
						.getValue();
			} else {
				Message.add("Current display state could not be determined.\n",
						2);
			}
			grappaPanel = replayResult.getVisualization(currentVisualization);
			grappaPanel.addGrappaListener(new DiagnosticGrappaAdapter());
			mapping = new HashMap();
			buildGraphMapping(mapping, grappaPanel.getSubgraph());

			modelContainer = new JScrollPane(grappaPanel);
			graphPanel.removeAll();
			graphPanel.add(modelContainer, BorderLayout.CENTER);
			graphPanel.validate();
			graphPanel.repaint();

			// update the provided object (diagnostic visualization might have
			// changed)
			diagnosticPetriNet = (DiagnosticPetriNet) replayResult.replayedPetriNet;
		} catch (Exception ex) {
			JOptionPane
					.showMessageDialog(this,
							"An internal error occured while\nupdating the visualization.");
			Message.add("Probably not found.\n" + ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	// //////// GRAPPA RELATED METHODS //////////

	/**
	 * Create a mapping from the Petri net graph structure back to the Grappa
	 * nodes. Copied from the template GUI but not used so far.
	 * 
	 * @param mapping
	 * @param subGraph
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

	// //////// INTERFACE IMPLEMENTATION RELATED METHODS //////////

	/**
	 * Specifiy provided objects of the analysis that can be further used to,
	 * e.g., export an item.
	 * 
	 * @return An Array containing a provided object called
	 *         "Diagnostic Petri net"; which contains a Petri net model
	 *         exporting the current diagnostic visualization.
	 */
	public ProvidedObject[] getProvidedObjects() {
		try {
			ProvidedObject[] objects = {
					new ProvidedObject("Diagnostic visualization",
							new Object[] { new DotFileWriter() {
								public void writeToDot(Writer bw)
										throws IOException {
									diagnosticPetriNet.writeToDot(bw);
								}
							} }),
					new ProvidedObject("Covered Petri net",
							new Object[] { minimalPetriNet }) };
			return objects;

		}

		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// //////// INTERNAL CLASS DEFINITIONS //////////

	/**
	 * A custom listener class for the grappa graph panel.
	 */
	public class DiagnosticGrappaAdapter extends GrappaAdapter {

		/**
		 * Create custom diagnostic tool tips for the grappa elements in the
		 * graph panel.
		 * 
		 * @param subg
		 *            Not used.
		 * @param elem
		 *            The element currently being moved over.
		 * @param pt
		 *            Not used.
		 * @param modifiers
		 *            Not used.
		 * @param panel
		 *            Not used.
		 * @return The string to be displayed.
		 */
		public String grappaTip(Subgraph subg, Element elem, GrappaPoint pt,
				int modifiers, GrappaPanel panel) {
			if (elem == null) {
				return "";
			} else if (elem.object == null) {
				return "";
			} else if (elem.object instanceof DiagnosticPlace) {
				DiagnosticPlace p = (DiagnosticPlace) elem.object;
				// only show diagnostics if visualization option has been
				// selected
				if (tokenCounter.getValue() == true) {
					return p.getDiagnosticToolTip(diagnosticPetriNet);
				} else {
					return p.getOrdinaryToolTip();
				}
			} else if (elem.object instanceof DiagnosticTransition) {
				DiagnosticTransition t = (DiagnosticTransition) elem.object;
				// only show diagnostics if visualization option has been
				// selected
				if (failedTransitions.getValue() == true
						|| remainingTransitions.getValue() == true) {
					return t.getDiagnosticToolTip(diagnosticPetriNet);
				} else {
					return t.getOrdinaryToolTip();
				}
			}
			// for anything else
			return "";
		}
	}
}
