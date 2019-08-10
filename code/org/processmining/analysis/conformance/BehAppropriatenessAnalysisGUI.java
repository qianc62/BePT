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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisGUI;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GuiNotificationTarget;

import att.grappa.Edge;
import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Node;
import att.grappa.Subgraph;

/**
 * Creates the GUI for the "Behavioral Appropriateness" tab in the results
 * frame.
 * 
 * @author arozinat
 */
public class BehAppropriatenessAnalysisGUI extends AnalysisGUI implements
		GuiNotificationTarget {

	/**
	 * Required for a serializable class (generated quickfix). Not directly
	 * used.
	 */
	private static final long serialVersionUID = -2734653144103864008L;
	// technical attributes
	private DiagnosticPetriNet stateExplorationPetriNet;
	private HashMap mapping;
	private DisplayState currentVisualization = DisplayState.BEHAVIORAL;

	private ConformanceLogReplayResult replayResult;
	private StateSpaceExplorationResult stateSpaceResult;

	// user interface related attributes
	private JScrollPane modelContainer;
	private JPanel graphPanel = new JPanel(new BorderLayout());
	private JPanel buttonsPanel = new JPanel();
	private JButton printModelRelations = new JButton("Model Relations");
	private JButton printLogRelations = new JButton("Log Relations");
	private JPanel measurementPanel = new JPanel();
	private JPanel bottomPanel = new JPanel(new BorderLayout());
	private JPanel bottomNorthPanel = new JPanel(new BorderLayout());
	private GrappaPanel grappaPanel;
	private JLabel behavioralAppropriatenessMeasureLabel = new JLabel(
			"<html>Simple<br>Behavioral Appropriateness:</html>");
	private JLabel behavioralAppropriatenessMeasure = new JLabel();
	private JLabel advBehavioralAppropriatenessMeasureLabel = new JLabel(
			"<html>Advanced<br>Behavioral Appropriateness:</html>");
	private JLabel advBehavioralAppropriatenessMeasure = new JLabel();
	private JLabel degreeModelFlexibilityMeasureLabel = new JLabel(
			"Degree of Model Flexibility:");
	private JLabel degreeModelFlexibilityMeasure = new JLabel();
	private JLabel measuresHeading = new JLabel("Measures");
	private JPanel graphTypePanel = new JPanel();
	private JPanel vizOptionsPanel = new JPanel();
	private GUIPropertyBoolean alwaysPrecedesOption; // visualization option
	private GUIPropertyBoolean neverPrecedesOption; // visualization option
	private GUIPropertyBoolean alwaysFollowsOption; // visualization option
	private GUIPropertyBoolean neverFollowsOption; // visualization option
	private GUIPropertyInteger threshold; // TODO

	/**
	 * Creates Behavioral Appropriateness GUI with default visualization
	 * options.
	 * 
	 * @param analysisResults
	 *            should contain a {@link ConformanceLogReplayResult
	 *            ConformanceLogReplayResult}
	 */
	public BehAppropriatenessAnalysisGUI(Set<AnalysisResult> analysisResults) {
		this(analysisResults, false, false, true, true);
	}

	/**
	 * Creates the results view for the Behavioral appropriateness category.
	 * 
	 * @param analysisResults
	 *            contains the {@link AnalysisResult AnalysisResult} objects
	 *            that have been requested by this category
	 * @param alwaysPrecedes
	 *            indicates whether this visualization option should be enabled
	 *            at startup
	 * @param neverPrecedes
	 *            indicates whether this visualization option should be enabled
	 *            at startup
	 * @param alwaysFollows
	 *            indicates whether this visualization option should be enabled
	 *            at startup
	 * @param neverFollows
	 *            indicates whether this visualization option should be enabled
	 *            at startup
	 */
	public BehAppropriatenessAnalysisGUI(Set<AnalysisResult> analysisResults,
			boolean alwaysPrecedes, boolean neverPrecedes,
			boolean alwaysFollows, boolean neverFollows) {
		super(analysisResults);

		// initialize the visualization option
		alwaysPrecedesOption = new GUIPropertyBoolean(
				"Always Precedes",
				"Shows pairs of tasks that always preceded each other (in the log)",
				alwaysPrecedes, this);
		neverPrecedesOption = new GUIPropertyBoolean(
				"Never Precedes",
				"Shows pairs of tasks that never preceded each other (in the log)",
				neverPrecedes, this);
		alwaysFollowsOption = new GUIPropertyBoolean(
				"Always Follows",
				"Shows pairs of tasks that always followed each other (in the log)",
				alwaysFollows, this);
		neverFollowsOption = new GUIPropertyBoolean(
				"Never Follows",
				"Shows pairs of tasks that never followed each other (in the log)",
				neverFollows, this);

		// retrieve the result objects
		Iterator<AnalysisResult> results = analysisResults.iterator();
		while (results.hasNext()) {
			AnalysisResult currentResult = results.next();
			if (currentResult != null) {
				if (currentResult instanceof ConformanceLogReplayResult) {
					replayResult = (ConformanceLogReplayResult) currentResult;
				}
				if (currentResult instanceof StateSpaceExplorationResult) {
					stateSpaceResult = (StateSpaceExplorationResult) currentResult;
				}
			} else {
				// an analysis result can be null if the user aborted the
				// computation process.. --> display empty tab
				return;
			}
		}

		// retrieve the diagnostic petri net
		stateExplorationPetriNet = (DiagnosticPetriNet) stateSpaceResult.exploredPetriNet;

		if (stateSpaceResult.calculateImprovedBehavioralAppropriateness() == true) { // configurated
			// clean up previous results first
			cleanLogAndModelRelations(stateSpaceResult, replayResult);
			// match the log-derived relations with the model-derived relations
			// and remember result
			// in the diagnostic Petri net
			matchLogAndModelRelations(stateSpaceResult, replayResult);
		}

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
	public boolean getAlwaysPrecedesOption() {
		return alwaysPrecedesOption.getValue();
	}

	/**
	 * Retrieves the current selection status of this visualization option.
	 * 
	 * @return <code>true</code> if this option is selected, <code>false</code>
	 *         otherwise
	 */
	public boolean getNeverPrecedesOption() {
		return neverPrecedesOption.getValue();
	}

	/**
	 * Retrieves the current selection status of this visualization option.
	 * 
	 * @return <code>true</code> if this option is selected, <code>false</code>
	 *         otherwise
	 */
	public boolean getAlwaysFollowsOption() {
		return alwaysFollowsOption.getValue();
	}

	/**
	 * Retrieves the current selection status of this visualization option.
	 * 
	 * @return <code>true</code> if this option is selected, <code>false</code>
	 *         otherwise
	 */
	public boolean getNeverFollowsCoverageOption() {
		return neverFollowsOption.getValue();
	}

	// /////////////////////// Public static methods

	/**
	 * Clean diagnostic Petri net and state space exploration result object from
	 * previous diagnostic results.
	 */
	public static void cleanLogAndModelRelations(
			StateSpaceExplorationResult stateSpaceResult,
			ConformanceLogReplayResult replayResult) {
		// clean Always and Never relations stored at the Diagnostic transitions
		Iterator<Transition> transitions = stateSpaceResult.exploredPetriNet
				.getTransitions().iterator();
		while (transitions.hasNext()) {
			DiagnosticTransition trans = (DiagnosticTransition) transitions
					.next();
			trans.resetAlwaysAndNeverRelations();
		}
		// clean sometimes relation counter
		stateSpaceResult.resetSometimesRelationCounter();
		// normalize log-based relation with respect to activities contained in
		// model-based relation
		Collection<DiagnosticLogEvent> modelRelationElements = stateSpaceResult
				.getActivityRelations().getDiagnosticLogEvents();
		DiagnosticLogEventRelation logRelation = replayResult
				.getLogEventRelations();
		logRelation
				.completeRelationByExternalZeroEntries(modelRelationElements);
	}

	/**
	 * Matches the log-based relations with the model-based relations. Must be
	 * called every time the threshold changes (as then the sometimes relation
	 * of the log changes as well).
	 */
	public static void matchLogAndModelRelations(
			StateSpaceExplorationResult stateSpaceResult,
			ConformanceLogReplayResult replayResult) {

		DiagnosticLogEventRelation modelRelation = stateSpaceResult
				.getActivityRelations();
		DiagnosticLogEventRelation logRelation = replayResult
				.getLogEventRelations();

		// / match forward relations
		Iterator<DiagnosticLogEvent> modelElements = modelRelation
				.getDiagnosticLogEvents().iterator();
		while (modelElements.hasNext()) {
			DiagnosticLogEvent element = modelElements.next();
			// find all transitions belonging to these two log events (may
			// contain only one transition if
			// no duplicate task)
			ArrayList<Transition> transitions = stateSpaceResult.exploredPetriNet
					.findTransitions(element);

			// / walk through sometimes relation from model
			Iterator<DiagnosticLogEvent> modelSometimesFW = element
					.getSometimesRelationForwards(0).iterator();
			while (modelSometimesFW.hasNext()) {
				DiagnosticLogEvent targetElement = modelSometimesFW.next();
				// find all transitions belonging to this target relation
				// element (log event)
				ArrayList<Transition> targetTransitions = stateSpaceResult.exploredPetriNet
						.findTransitions(targetElement);
				// record sometimes relation counter
				stateSpaceResult.incSFModel();

				// if not found in sometimes relation in log -> must be in
				// always or never
				// TODO - either use toString() method in internal hashMaps or
				// make extra method producing this string!
				if (logRelation.areInSFRelation(element.getModelElementName()
						+ element.getEventType(), targetElement
						.getModelElementName()
						+ targetElement.getEventType()) == true) {
					stateSpaceResult.incSFLog();
				} else if (logRelation.areInAFRelation(element
						.getModelElementName()
						+ element.getEventType(), targetElement
						.getModelElementName()
						+ targetElement.getEventType()) == true) {
					// record transitions for AF relation (for all potential
					// duplicates)
					Iterator<Transition> transitionsIt = transitions.iterator();
					while (transitionsIt.hasNext()) {
						DiagnosticTransition from = (DiagnosticTransition) transitionsIt
								.next();
						Iterator<Transition> targetTransitionsIt = targetTransitions
								.iterator();
						while (targetTransitionsIt.hasNext()) {
							Transition to = targetTransitionsIt.next();
							from.addAlwaysFollows(to);
						}
					}
				} else if (logRelation.areInNFRelation(element
						.getModelElementName()
						+ element.getEventType(), targetElement
						.getModelElementName()
						+ targetElement.getEventType()) == true) {
					// record transitions for NF relation (for all potential
					// duplicates)
					Iterator<Transition> transitionsIt = transitions.iterator();
					while (transitionsIt.hasNext()) {
						DiagnosticTransition from = (DiagnosticTransition) transitionsIt
								.next();
						Iterator<Transition> targetTransitionsIt = targetTransitions
								.iterator();
						while (targetTransitionsIt.hasNext()) {
							Transition to = targetTransitionsIt.next();
							from.addNeverFollows(to);
						}
					}
				}
			}

			// match backward relations
			Iterator<DiagnosticLogEvent> modelSometimesBW = element
					.getSometimesRelationsBackwards(0).iterator();
			while (modelSometimesBW.hasNext()) {
				DiagnosticLogEvent targetElement = modelSometimesBW.next();
				// find all transitions belonging to this target relation
				// element (log event)
				ArrayList<Transition> targetTransitions = stateSpaceResult.exploredPetriNet
						.findTransitions(targetElement);
				// record sometimes relation counter
				stateSpaceResult.incSBModel();

				// / if not found in sometimes relation in log -> must be in
				// always or never
				if (logRelation.areInSBRelation(element.getModelElementName()
						+ element.getEventType(), targetElement
						.getModelElementName()
						+ targetElement.getEventType()) == true) {
					stateSpaceResult.incSBLog();
				} else if (logRelation.areInABRelation(element
						.getModelElementName()
						+ element.getEventType(), targetElement
						.getModelElementName()
						+ targetElement.getEventType()) == true) {
					// record transitions for AB relation (for all potential
					// duplicates)
					Iterator<Transition> transitionsIt = transitions.iterator();
					while (transitionsIt.hasNext()) {
						DiagnosticTransition from = (DiagnosticTransition) transitionsIt
								.next();
						Iterator<Transition> targetTransitionsIt = targetTransitions
								.iterator();
						while (targetTransitionsIt.hasNext()) {
							Transition to = targetTransitionsIt.next();
							from.addAlwaysPrecedes(to);
						}
					}
				} else if (logRelation.areInNBRelation(element
						.getModelElementName()
						+ element.getEventType(), targetElement
						.getModelElementName()
						+ targetElement.getEventType()) == true) {
					// record transitions for NB relation (for all potential
					// duplicates)
					Iterator<Transition> transitionsIt = transitions.iterator();
					while (transitionsIt.hasNext()) {
						DiagnosticTransition from = (DiagnosticTransition) transitionsIt
								.next();
						Iterator<Transition> targetTransitionsIt = targetTransitions
								.iterator();
						while (targetTransitionsIt.hasNext()) {
							Transition to = targetTransitionsIt.next();
							from.addNeverPrecedes(to);
						}
					}
				}
			}
		}
	}

	/**
	 * Construct the user interface for the conformance check analysis.
	 * 
	 * @throws Exception
	 *             Any exception is just passed to the caller.
	 */
	private void jbInit() throws Exception {
		measuresHeading.setForeground(new Color(100, 100, 100));
		// set up all help texts
		behavioralAppropriatenessMeasure
				.setToolTipText("Degree of behavioral appropriateness based on the mean number of enabled transitions");
		advBehavioralAppropriatenessMeasure
				.setToolTipText("Degree of behavioral appropriateness based on the comparison of activity relations in model and log");
		degreeModelFlexibilityMeasure
				.setToolTipText("Degree of flexibility allowed by the model for executing contained activities (alternative and parallel behavior)");
		printModelRelations
				.setToolTipText("Output activity relations derived from the process model to Message Console");
		printLogRelations
				.setToolTipText("Output activity relations derived from the event log to Message Console");

		// / build the layout (only the selected options)
		measurementPanel.setLayout(new BoxLayout(measurementPanel,
				BoxLayout.PAGE_AXIS));
		measurementPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
				10));
		measurementPanel.add(measuresHeading);
		measurementPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		if (replayResult.calculateBehavioralAppropriateness() == true) { // configurated
			float behApp = replayResult.getBehavioralAppropriatenessMeasure();
			behavioralAppropriatenessMeasure.setText("" + behApp);
			measurementPanel.add(behavioralAppropriatenessMeasureLabel);
			measurementPanel.add(behavioralAppropriatenessMeasure);
			measurementPanel.add(Box.createRigidArea(new Dimension(0, 15)));

			// / PLUGIN TEST START
			Message.add("Behavioral Appropriateness: " + behApp, Message.TEST);
			// PLUGIN TEST END
		}

		if (stateSpaceResult.calculateImprovedBehavioralAppropriateness() == true) { // configurated
			float behApp = stateSpaceResult
					.getImprovedBehavioralAppropriatenessMeasure();
			advBehavioralAppropriatenessMeasure.setText("" + behApp);
			measurementPanel.add(advBehavioralAppropriatenessMeasureLabel);
			measurementPanel.add(advBehavioralAppropriatenessMeasure);
			measurementPanel.add(Box.createRigidArea(new Dimension(0, 5)));

			float degreeModelFlex = stateSpaceResult
					.getDegreeOfModelFlexibility();
			degreeModelFlexibilityMeasure.setText("" + degreeModelFlex);
			measurementPanel.add(degreeModelFlexibilityMeasureLabel);
			measurementPanel.add(degreeModelFlexibilityMeasure);

			// / PLUGIN TEST START
			Message.add("Improved Behavioral Appropriateness: " + behApp,
					Message.TEST);
			// PLUGIN TEST END
		}

		// aaB must have been calculated out in order to show diagnostic
		// visualization
		if (stateSpaceResult.calculateImprovedBehavioralAppropriateness() == true) { // configurated
			vizOptionsPanel.add(alwaysPrecedesOption.getPropertyPanel());
			vizOptionsPanel.add(neverPrecedesOption.getPropertyPanel());
			vizOptionsPanel.add(alwaysFollowsOption.getPropertyPanel());
			vizOptionsPanel.add(neverFollowsOption.getPropertyPanel());
			graphTypePanel.add(vizOptionsPanel);
			// / create buttons for output of activity relations
			buttonsPanel.add(printModelRelations);
			buttonsPanel.add(printLogRelations);
		}

		currentVisualization.alwaysFollowsOption = alwaysFollowsOption
				.getValue();
		currentVisualization.alwaysPrecedesOption = alwaysPrecedesOption
				.getValue();
		currentVisualization.neverFollowsOption = neverFollowsOption.getValue();
		currentVisualization.neverPrecedesOption = neverPrecedesOption
				.getValue();

		grappaPanel = stateSpaceResult.getVisualization(currentVisualization);
		if (grappaPanel != null) {
			grappaPanel.addGrappaListener(new DiagnosticGrappaAdapter());
		}
		mapping = new HashMap();
		if (grappaPanel != null) {
			buildGraphMapping(mapping, grappaPanel.getSubgraph());
		}

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
		bottomNorthPanel.add(buttonsPanel, BorderLayout.WEST);
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
		// specify GUI actions
		printModelRelations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DiagnosticLogEventRelation modelRelation = stateSpaceResult
						.getActivityRelations();
				modelRelation.printRelationsToMessageConsole(0);
			}
		});
		printLogRelations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DiagnosticLogEventRelation logRelation = replayResult
						.getLogEventRelations();
				// TODO - consider threshold
				logRelation.printRelationsToMessageConsole(0);
			}
		});
	}

	/**
	 * Updates the visualization for the changed visualization options.
	 */
	public void updateGUI() {
		try {
			// update the result object
			// TODO - will be necessary as soon as threshold is to be
			// incorporated!!
			// replayResult = (ConformanceLogReplayResult)
			// updatedAnalysisResults;

			// update the aB measure for current selection
			// if (replayResult.calculateBehavioralAppropriateness() == true) {
			// // configurated
			// behavioralAppropriatenessMeasure.setText("" +
			// replayResult.getBehavioralAppropriatenessMeasure());
			// }
			// update the aaB measure for current selection
			// if (replayResult.calculateBehavioralAppropriateness() == true) {
			// // configurated
			// advBehavioralAppropriatenessMeasure.setText("" +
			// stateSpaceResult.getImprovedBehavioralAppropriatenessMeasure());
			// }

			currentVisualization.alwaysFollowsOption = alwaysFollowsOption
					.getValue();
			currentVisualization.alwaysPrecedesOption = alwaysPrecedesOption
					.getValue();
			currentVisualization.neverFollowsOption = neverFollowsOption
					.getValue();
			currentVisualization.neverPrecedesOption = neverPrecedesOption
					.getValue();

			// update the visualization
			grappaPanel = stateSpaceResult
					.getVisualization(currentVisualization);
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
			stateExplorationPetriNet = (DiagnosticPetriNet) stateSpaceResult.exploredPetriNet;
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
			ProvidedObject[] objects = { new ProvidedObject(
					"Diagnostic visualization",
					new Object[] { new DotFileWriter() {
						public void writeToDot(Writer bw) throws IOException {
							stateExplorationPetriNet.writeToDot(bw);
						}
					} }) };
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
			}
			if (elem.object == null) {
				return "";
			}
			// if (elem.object instanceof DiagnosticPlace &&
			// (replayResult.performLogReplay() == true)) {
			// DiagnosticPlace p = (DiagnosticPlace) elem.object;
			// return p.getPerformanceToolTip(getSelectedInstanceIDs());
			// }
			// for anything else
			return "";
		}
	}
}
