package org.processmining.analysis.conformance;

import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;
import org.processmining.framework.ui.Message;

import att.grappa.GrappaPanel;

public class StateSpaceExplorationResult extends AnalysisResult {

	public PetriNet inputPetriNet;
	/** The original Petri net passed to the analysis method. */
	public DiagnosticPetriNet exploredPetriNet;
	/** The Petri net enhanced with diagnostic information. */

	// /////////// analysis options (if no configuration is given, all options
	// are treated enabled)
	private boolean improvedBehavioralAppropriatenessOption = false;
	private boolean improvedStructuralAppropriatenessOption = false;
	/** TEMPLATE: add further options here... */

	// / The current way of visualizing the analysis result.
	private DisplayState currentVisualization = DisplayState.BEHAVIORAL;

	// ///////////// result data structures
	/**
	 * Contains the diagnostic data structures for the activities (and their
	 * relations) in the the process model. Non-existing entries in pre- and
	 * post-relations are counted as 0.
	 */
	private DiagnosticLogEventRelation logEvents = new DiagnosticLogEventRelation();

	/**
	 * Contains the number of redundant invisible tasks that have been
	 * encountered during the state space exploration for the improved
	 * structural appropriateness method.
	 */
	private int numberOfRedundantInvisibles = 0;

	/**
	 * Contains the number of alternative duplicate tasks that have been
	 * encountered during the state space exploration for the improved
	 * structural appropriateness method.
	 */
	private int numberOfAlternativeDuplicates = 0;

	/**
	 * Contains the number of sometimes relations (forwards) in the model
	 * (needed for calculating advanced behavioral appropriateness).
	 */
	private int numberOfSometimesRelationForwardElementsInModel = 0;

	/**
	 * Contains the number of sometimes relations (forwards) in the log and in
	 * the model, i.e., represents already the intersection of SFm and SFl
	 * (needed for calculating advanced behavioral appropriateness).
	 */
	private int numberOfSometimesRelationForwardElementsInLog = 0;

	/**
	 * Contains the number of sometimes relations (backwards) in the model
	 * (needed for calculating advanced behavioral appropriateness).
	 */
	private int numberOfSometimesRelationBackwardsElementsInModel = 0;

	/**
	 * Contains the number of sometimes relations (backwards) in the log and in
	 * the model, i.e., represents already the intersection of SFm and SFl
	 * (needed for calculating advanced behavioral appropriateness).
	 */
	private int numberOfSometimesRelationBackwardsElementsInLog = 0;

	/**
	 * 
	 * @param analysisOptions
	 *            AnalysisConfiguration
	 * @param net
	 *            PetriNet
	 * @param method
	 *            StructuralAnalysisMethod
	 */
	public StateSpaceExplorationResult(AnalysisConfiguration analysisOptions,
			PetriNet net, StateSpaceExplorationMethod method) {
		super(analysisOptions);

		inputPetriNet = net;
		// init diagnostic data structures
		exploredPetriNet = new BehAppropriatenessVisualization(inputPetriNet,
				new ArrayList(), null);

		if (analysisOptions != null) {
			// retrieve the options
			ArrayList<AnalysisConfiguration> configurationOptions = analysisOptions
					.getEnabledOptionsForAnalysisMethod(
							new ArrayList<AnalysisConfiguration>(), method
									.getIdentifier());
			Iterator allOptions = configurationOptions.iterator();
			while (allOptions.hasNext()) {
				AnalysisConfiguration currentOption = (AnalysisConfiguration) allOptions
						.next();
				if (currentOption.getName() == "aaB") {
					improvedBehavioralAppropriatenessOption = true;
				}
				if (currentOption.getName() == "aaS") {
					improvedStructuralAppropriatenessOption = true;
				}
				/* TEMPLATE: add further options here... */
			}
		}
		// if configuration object is null --> do all
		else {
			improvedBehavioralAppropriatenessOption = true;
			improvedStructuralAppropriatenessOption = true;
			/* TEMPLATE: add further options here... */
		}
	}

	// ///////// ANALYSIS OPTIONS
	// ///////////////////////////////////////////////

	/**
	 * Indicates whether the "Structural Appropriateness" metric has been
	 * selected by the user.
	 * 
	 * @return <code>true</code> if this option has been chosen,
	 *         <code>false</code> otherwise
	 */
	public boolean calculateImprovedBehavioralAppropriateness() {
		return improvedBehavioralAppropriatenessOption;
	}

	/**
	 * Indicates whether the "Advanced Structural Appropriateness" metric has
	 * been selected by the user.
	 * 
	 * @return <code>true</code> if this option has been chosen,
	 *         <code>false</code> otherwise
	 */
	public boolean calculateImprovedStructuralAppropriateness() {
		return improvedStructuralAppropriatenessOption;
	}

	// /////// METRICS /////////////////////////////////////////////////////////

	/**
	 * Retrieves the structural appropriateness measure based on the graph size.
	 * 
	 * @return float The value calculated (0 <= value <= 1).
	 */
	public float getImprovedStructuralAppropriatenessMeasure() {
		float totalNumberOfTasks = (float) inputPetriNet.getTransitions()
				.size();
		float redundantInvisibles = (float) numberOfRedundantInvisibles;
		float alternativeDuplicates = (float) numberOfAlternativeDuplicates;

		// TODO: make these figures available in the GUI rather than writing
		// them to the console
		Message.add("Total Number of tasks in model: " + totalNumberOfTasks, 3);
		Message.add("Number of redundant invisible tasks in the model: "
				+ redundantInvisibles, 3);
		Message.add("Number of alternative duplicate tasks in the model: "
				+ numberOfAlternativeDuplicates, 3);

		if (totalNumberOfTasks > 0) {
			// calculate metric
			return (totalNumberOfTasks - (redundantInvisibles + alternativeDuplicates))
					/ totalNumberOfTasks;
		} else {
			Message
					.add("The Improved Structural appropriateness metric aaS can only be calculated for "
							+ "models with at least 1 task.");
			return 0;
		}
	}

	/**
	 * Retrieves the behavioral appropriateness measure based on log-based and
	 * model-based activity relations.
	 * 
	 * @return the metric value (0 <= value <= 1)
	 */
	public float getImprovedBehavioralAppropriatenessMeasure() {
		// make float conversions
		float SmF = (float) numberOfSometimesRelationForwardElementsInModel;
		float SlF = (float) numberOfSometimesRelationForwardElementsInLog;
		float SmB = (float) numberOfSometimesRelationBackwardsElementsInModel;
		float SlB = (float) numberOfSometimesRelationBackwardsElementsInLog;
		float oneHalf = (float) 0.5;
		float forwardsPart;
		float backwardsPart;

		// TODO - make additional views for the model and the log relations that
		// were derived
		Message.add(
				"No. Elements in 'Sometimes Follows' relation of the model: "
						+ numberOfSometimesRelationForwardElementsInModel, 3);
		Message.add(
				"No. Elements in (intersection with) 'Sometimes Follows' relation of the log: "
						+ numberOfSometimesRelationForwardElementsInLog, 3);
		Message.add(
				"No. Elements in 'Sometimes Precedes' relation of the model: "
						+ numberOfSometimesRelationBackwardsElementsInModel, 3);
		Message
				.add(
						"No. Elements in (intersection with) 'Sometimes Precedes' relation of the log: "
								+ numberOfSometimesRelationBackwardsElementsInLog,
						3);

		// check for the trivial case that SmF minimal (e.g., sequential model)
		if (SmF != 0) {
			forwardsPart = (SmF - SlF) / SmF;
		} else {
			forwardsPart = 0;
		}
		// check for the trivial case that SmB minimal (e.g., sequential model)
		if (SmB != 0) {
			backwardsPart = (SmB - SlB) / SmB;
		} else {
			backwardsPart = 0;
		}

		// new measure: only relative to model behavior!!
		return (1 - (oneHalf * forwardsPart + oneHalf * backwardsPart));
	}

	/**
	 * Retrieves the behavioral appropriateness measure based on log-based and
	 * model-based activity relations.
	 * 
	 * @return the metric value (0 <= value <= 1)
	 */
	public float getDegreeOfModelFlexibility() {
		// get |L| from log event relation (is normalized to elements in model
		// relation)
		int L = logEvents.getDiagnosticLogEvents().size();
		// because of artificial start and end certain fields can never be in
		// Sometimes relation
		int maximumSometimes = (L * L) - (3 * L) + 2;

		// make float conversions
		float max = (float) maximumSometimes;
		float SmF = (float) numberOfSometimesRelationForwardElementsInModel;
		float SmB = (float) numberOfSometimesRelationBackwardsElementsInModel;
		float oneHalf = (float) 0.5;

		if (max > 0) {
			// calculate
			return (oneHalf * SmF / max) + (oneHalf * SmB / max);
		} else {
			Message
					.add("The degree of model flexibility can only be calculated for "
							+ "models with at least 1 visible task.");
			return 0;
		}
	}

	/**
	 * Returns the set of activity relations that have been derived from the
	 * state space of the process model. They still need to be matched with the
	 * relations derived for the log events from the log, and their difference
	 * is to be recorded in the diagnostic petri net.
	 * 
	 * @return the table containing the name of the log events and the
	 *         diagnostic log event itself
	 */
	public DiagnosticLogEventRelation getActivityRelations() {
		return logEvents;
	}

	// ///////////////////// methods for improved behavioral appropriateness

	/**
	 * Adds up the "number of redundant invisible tasks" measurement by the
	 * given value.
	 */
	public void updateNumberOfRedundantInvisibles(int value) {
		numberOfRedundantInvisibles = numberOfRedundantInvisibles + value;
	}

	/**
	 * Adds up the "number of alternative duplicate tasks" measurement by the
	 * given value.
	 */
	public void updateNumberOfAlternativeDuplicates(int value) {
		numberOfAlternativeDuplicates = numberOfAlternativeDuplicates + value;
	}

	/**
	 * Increments the number of sometimes relations (forwards) in the model
	 * (needed for calculating advanced behavioral appropriateness).
	 */
	public void incSFModel() {
		numberOfSometimesRelationForwardElementsInModel = numberOfSometimesRelationForwardElementsInModel + 1;
	}

	/**
	 * Increments the number of sometimes relations (forwards) in the log and in
	 * the model, i.e., represents already the intersection of SFm and SFl
	 * (needed for calculating advanced behavioral appropriateness).
	 */
	public void incSFLog() {
		numberOfSometimesRelationForwardElementsInLog = numberOfSometimesRelationForwardElementsInLog + 1;
	}

	/**
	 * Increments the number of sometimes relations (backwards) in the model
	 * (needed for calculating advanced behavioral appropriateness).
	 */
	public void incSBModel() {
		numberOfSometimesRelationBackwardsElementsInModel = numberOfSometimesRelationBackwardsElementsInModel + 1;
	}

	/**
	 * Increments the number of sometimes relations (backwards) in the log and
	 * in the model, i.e., represents already the intersection of SFm and SFl
	 * (needed for calculating advanced behavioral appropriateness).
	 */
	public void incSBLog() {
		numberOfSometimesRelationBackwardsElementsInLog = numberOfSometimesRelationBackwardsElementsInLog + 1;
	}

	/**
	 * Resets the sometimes relation element counter to 0, so that it can be
	 * filled with new values (based on the new log selection).
	 */
	public void resetSometimesRelationCounter() {
		numberOfSometimesRelationForwardElementsInLog = 0;
		numberOfSometimesRelationBackwardsElementsInLog = 0;
		numberOfSometimesRelationForwardElementsInModel = 0;
		numberOfSometimesRelationBackwardsElementsInModel = 0;
	}

	// ////////////////////// Visualization methods
	// /////////////////////////////

	/**
	 * Get the current display state.
	 * 
	 * @return The current display state.
	 */
	public String getCurrentVisualizationState() {
		return currentVisualization.toString();
	}

	/**
	 * Set the new visualization state, which leads to a replacement of the
	 * diagnostic Petri net that actually delivers the customized visualization.
	 * 
	 * @param newVisualization
	 *            The new visualization state.
	 */
	private void setVisualizationState(DisplayState newVisualization) {
		// exchange the diagnostic Petri net for delivering the correct
		// visualization
		if (newVisualization.toString().equals(
				DisplayState.BEHAVIORAL.toString())) {
			// only change if state is new
			if (getCurrentVisualizationState().equals(
					newVisualization.toString()) == false) {
				exploredPetriNet = new BehAppropriatenessVisualization(
						(DiagnosticPetriNet) exploredPetriNet);
			}
			// but set always the current options state
			BehAppropriatenessVisualization currentVisualization = (BehAppropriatenessVisualization) exploredPetriNet;
			currentVisualization.alwaysFollowsOption = newVisualization.alwaysFollowsOption;
			currentVisualization.neverFollowsOption = newVisualization.neverFollowsOption;
			currentVisualization.alwaysPrecedesOption = newVisualization.alwaysPrecedesOption;
			currentVisualization.neverPrecedesOption = newVisualization.neverPrecedesOption;
		} else {
			Message
					.add(
							"Correct visualization state could not be determined.\n",
							2);
		}
		currentVisualization = newVisualization;
	}

	/**
	 * Creates a visualization of the conformance check results. Note that a
	 * change of the display state by the user will have no effect before
	 * calling this methods. This is intended to prevent unnecessary cloning of
	 * the diagnostic petri net, which actually delivers the custom
	 * visualization of the conformance analysis results.
	 * 
	 * @param selectedInstances
	 *            The process instances that have been selected for updating the
	 *            visualization.
	 * @param currentVisualization
	 *            The current display state.
	 * @return The visualization wrapped in a GrappaPanel.
	 */
	public GrappaPanel getVisualization(DisplayState currentVisualization) {

		// ensure the display state being up to date
		setVisualizationState(currentVisualization);
		GrappaPanel myResultVisualization;
		myResultVisualization = ((DiagnosticPetriNet) exploredPetriNet)
				.getGrappaVisualization();
		return myResultVisualization;
	}
}
