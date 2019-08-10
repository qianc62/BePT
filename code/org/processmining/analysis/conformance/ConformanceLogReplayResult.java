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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisMethod;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisResult;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;
import org.processmining.framework.ui.Message;

import att.grappa.GrappaPanel;

/**
 * Contains all the results obtained during log replay analysis. Can be used to
 * retrieve values for the implemented metrics and to get diagnostic
 * visualizations.
 * 
 * @see ConformanceMeasurer
 * 
 * @author arozinat
 */
public class ConformanceLogReplayResult extends LogReplayAnalysisResult {

	// /////////// analysis options (if no configuration is given, all options
	// are treated enabled)
	private boolean behavioralAppropriatenessOption = false;
	private boolean fitnessOption = false;
	private boolean successfulExecutionOption = false;
	private boolean properCompletionOption = false;
	private boolean improvedBehavioralAppropriateness = false;
	/** TEMPLATE: add further options here... */

	// The current way of visualizing the analysis result.
	private DisplayState currentVisualization = DisplayState.MODEL;

	// //////////// result data structures
	/**
	 * Contains the diagnostic data structures for the activities (and their
	 * relations) in the current log selection. Non-existing entries in pre- and
	 * post-relations are counted as 0.
	 */
	private DiagnosticLogEventRelation logEvents = new DiagnosticLogEventRelation();

	/**
	 * Points to all log traces that could be replayed perfectly.
	 */
	private HashSet<ReplayedLogTrace> fittingLogTraces = new HashSet<ReplayedLogTrace>();

	/**
	 * Contains the sum of mean numbers of enabled transitions for all process
	 * instances (taking the number of similar instances into account).
	 */
	private float overallMeanNumber = 0;

	/**
	 * Contains the sum of all process instances (taking the number of similar
	 * instances into account).
	 */
	private int overallNumberOfInstances = 0;

	/**
	 * The number of consumed tokens during log replay is used to calculate the
	 * basic fitness measure of being successfully executed based on missing
	 * tokens.
	 */
	private int numberOfConsumedTokens = 0;

	/**
	 * The number of produced tokens during log replay is used to calculate the
	 * basic fitness measure of being properly terminated based on remaining
	 * tokens.
	 */
	private int numberOfProducedTokens = 0;

	/**
	 * The number of successfully executed process instances.
	 */
	int numberOfSuccessfullyExecuted = 0;

	/**
	 * The number of properly completed process instances.
	 */
	int numberOfProperlyTerminated = 0;

	/**
	 * {@inheritDoc} Evaluate the given configuration options, that is,
	 * retrieves the choices made by the user and converts them into analysis
	 * option variables indicating which parts of the analysis needs to be done.
	 */
	public ConformanceLogReplayResult(AnalysisConfiguration analysisOptions,
			PetriNet net, LogReader log, LogReplayAnalysisMethod method) {
		super(analysisOptions, net, log, method);

		if (analysisOptions != null) {
			// retrieve the options
			ArrayList configurationOptions = analysisOptions
					.getEnabledOptionsForAnalysisMethod(new ArrayList(), method
							.getIdentifier());
			Iterator allOptions = configurationOptions.iterator();
			while (allOptions.hasNext()) {
				AnalysisConfiguration currentOption = (AnalysisConfiguration) allOptions
						.next();
				if (currentOption.getName() == "saB") {
					behavioralAppropriatenessOption = true;
				} else if (currentOption.getName() == "f") {
					fitnessOption = true;
				} else if (currentOption.getName() == "pSE") {
					successfulExecutionOption = true;
				} else if (currentOption.getName() == "pPC") {
					properCompletionOption = true;
				} else if (currentOption.getName() == "aaB") {
					improvedBehavioralAppropriateness = true;
				}
				/* TEMPLATE: add further options here... */
			}
		}
		// if configuration object is null --> do all
		else {
			behavioralAppropriatenessOption = true;
			fitnessOption = true;
			successfulExecutionOption = true;
			properCompletionOption = true;
			improvedBehavioralAppropriateness = true;
			/* TEMPLATE: add further options here... */
		}

		// perform log replay as soon as only one of the options has been chosen
		// by the user
		/**
		 * @todo anne: automate (so that not needs to be extended if a new
		 *       option is added)
		 */
		performLogReplay = (calculateFitness() == true
				|| calculateSuccessfulExecution() == true
				|| calculateProperCompletetion() == true
				|| calculateBehavioralAppropriateness() == true || calculateImprovedBehavioralAppropriateness() == true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.models.petrinet.algorithms.logReplay.
	 * LogReplayAnalysisResult#initDiagnosticDataStructures()
	 */
	protected void initDiagnosticDataStructures() {
		replayedLog = new DiagnosticLogReader(inputLogReader);
		replayedPetriNet = new FitnessVisualization(inputPetriNet, replayedLog
				.getLogTraceIDs(), (DiagnosticLogReader) replayedLog);
	}

	// ///////// ANALYSIS OPTIONS
	// ///////////////////////////////////////////////

	/**
	 * Indicates whether the "Behavioral Appropriateness" metric has been
	 * selected by the user.
	 * 
	 * @return <code>true</code> if this option has been chosen,
	 *         <code>false</code> otherwise
	 */
	public boolean calculateBehavioralAppropriateness() {
		return behavioralAppropriatenessOption;
	}

	/**
	 * Indicates whether the "Improved Behavioral Appropriateness" metric has
	 * been selected by the user.
	 * 
	 * @return <code>true</code> if this option has been chosen,
	 *         <code>false</code> otherwise
	 */
	public boolean calculateImprovedBehavioralAppropriateness() {
		return improvedBehavioralAppropriateness;
	}

	/**
	 * Indicates whether the "Fitness" metric has been selected by the user.
	 */
	public boolean calculateFitness() {
		return fitnessOption;
	}

	/**
	 * Indicates whether the "Successful Execution" metric has been selected by
	 * the user.
	 */
	public boolean calculateSuccessfulExecution() {
		return successfulExecutionOption;
	}

	/**
	 * Indicates whether the "Proper Completion" metric has been selected by the
	 * user.
	 */
	public boolean calculateProperCompletetion() {
		return properCompletionOption;
	}

	/* TEMPLATE: add further options here... */

	// /////// CONFORMANCE ORIENTED METRICS
	// /////////////////////////////////////
	/**
	 * Determine the fraction [0,1] of properly terminated process instances
	 * from those log traces specified in the list. Note that the number of
	 * process instances represented by each dignostic log trace is taken into
	 * account, and every log trace is treated as a whole. That means that there
	 * is no possibiltiy to retrieve this measure for only a fraction of similar
	 * instances for a certain logical trace.
	 * 
	 * @return The fraction of properly terminated process instances
	 */
	public float getFractionOfProperlyTerminated() {
		float properlyTerminated = (float) numberOfProperlyTerminated;
		float noOfInstances = (float) overallNumberOfInstances;
		if (noOfInstances > 0) {
			// calculate
			return properlyTerminated / noOfInstances;
		} else {
			Message
					.add("The fraction of properly completed log traces pPC can only be calculated if there were any log traces replayed.");
			return 0;
		}
	}

	/**
	 * Determine the fraction [0,1] of successfully executed process instances
	 * from those log traces specified in the list. Note that the number of
	 * process instances represented by each dignostic log trace is taken into
	 * account, and every log trace is treated as a whole. That means that there
	 * is no possibiltiy to retrieve this measure for only a fraction of similar
	 * instances for a certain logical trace.
	 * 
	 * @param piList
	 *            The list of specified log traces.
	 * @return The fraction of successfully executed process instances.
	 */
	public float getFractionOfSuccessfullyExecuted() {
		float successfullyExecuted = (float) numberOfSuccessfullyExecuted;
		float noOfInstances = (float) overallNumberOfInstances;
		if (noOfInstances > 0) {
			// calculate
			return successfullyExecuted / noOfInstances;
		} else {
			Message
					.add("The fraction of successfully executed log traces pSE can only be calculated if there were any log traces replayed.");
			return 0;
		}
	}

	/**
	 * Retrieves the successful execution measure based on missing tokens (i.e.,
	 * amount of missing / amount of consumed) during log replay.
	 * 
	 * @return The measure for the selected traces.
	 */
	public float getSuccessfulExecutionTokenMeasure() {
		float missing = (float) ((DiagnosticPetriNet) replayedPetriNet)
				.getMissingTokens();
		float consumed = (float) numberOfConsumedTokens;
		if (consumed > 0) {
			// calculate
			return (1 - (missing / consumed));
		} else {
			// if the log is completely empty then one could think of a single
			// empty trace with a
			// start token being produced and a token being consumed from the
			// final place, which leads to 1-1 = 0
			return 0;
		}
	}

	/**
	 * Retrieves the proper completion measure based on remaining tokens (i.e.,
	 * amount of remaining / amount of produced) during log replay.
	 * 
	 * @return The measure for the selected traces.
	 */
	public float getProperCompletionTokenMeasure() {
		float remaining = (float) ((DiagnosticPetriNet) replayedPetriNet)
				.getRemainingTokens();
		float produced = (float) numberOfProducedTokens;
		if (produced > 0) {
			// calculate
			return (1 - (remaining / produced));
		} else {
			// if the log is completely empty then one could think of a single
			// empty trace with a
			// start token being produced and a token being consumed from the
			// final place, which leads to 1-1 = 0
			return 0;
		}
	}

	/**
	 * Retrieves the fitness measure based on the token based proper completion
	 * and the successful execution measures (weighted 50 per cent each).
	 * 
	 * @return The measure for the selected traces.
	 */
	public float getFitnessMeasure() {
		float successfulExecution = getSuccessfulExecutionTokenMeasure();
		float properCompletion = getProperCompletionTokenMeasure();
		// weigh them to 50% each
		return ((float) 0.5) * successfulExecution + ((float) 0.5)
				* properCompletion;
	}

	/**
	 * Retrieves the behavioral appropriateness measure based on the mean number
	 * of enabled transitions during log replay.
	 * 
	 * @param piList
	 *            The list of diagnostic log traces.
	 * @return The measure for the selected traces (0 <= value <= 1).
	 * @throws Exception
	 *             In the case that the entry for one of the traces is missing.
	 */
	public float getBehavioralAppropriatenessMeasure() throws Exception {
		// calculate mean number of enabled transitions
		float meanEnabled = overallMeanNumber
				/ (float) overallNumberOfInstances;
		float numberOfTransitons = replayedPetriNet.getNumberOfVisibleTasks();
		// calculate metric
		if (numberOfTransitons > 1 && overallNumberOfInstances > 0) {
			return (((numberOfTransitons - meanEnabled) / (numberOfTransitons - 1)));
		} else {
			Message
					.add("The Behavioral appropriateness metric aB can only be calculated for "
							+ "models with more than 1 visible task, and if at least one log trace was replayed.");
			return 0;
		}
	}

	/**
	 * Gets all fitting log traces from the log.
	 * 
	 * @return A list of trace IDs referencing the fitting instances in the log.
	 */
	public ArrayList<String> getFittingLogTraces() {
		Iterator<ReplayedLogTrace> allTraces = fittingLogTraces.iterator();
		ArrayList<String> fitting = new ArrayList<String>();
		while (allTraces.hasNext()) {
			ReplayedLogTrace currentTrace = allTraces.next();
			fitting.add(currentTrace.getName());
		}
		return fitting;
	}

	/**
	 * Retrieves the global log event relations gathered during log replay.
	 * 
	 * @return the log event relations for the log
	 */
	public DiagnosticLogEventRelation getLogEventRelations() {
		return logEvents;
	}

	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Adds the given process instance to the set of perfectly fitting log
	 * traces.
	 * 
	 * @param pi
	 *            the fitting log trace to be added
	 */
	public void addFittingInstance(ReplayedLogTrace pi) {
		fittingLogTraces.add(pi);
	}

	/**
	 * TODO: check whether can be obtained from the log directly Adds up the
	 * total number of process instances.
	 * 
	 * @param corresponds
	 *            to the number of similar instances for the updated trace
	 */
	public void updateNumberOfProcessInstances(int value) {
		overallNumberOfInstances = overallNumberOfInstances + value;
	}

	/**
	 * Adds up the global "properly-completed" measurement by the given value.
	 * 
	 * @param corresponds
	 *            to the number of similar instances for the updated trace
	 */
	public void updateProperlyCompletedInstances(int value) {
		numberOfProperlyTerminated = numberOfProperlyTerminated + value;
	}

	/**
	 * Adds up the global "successfully-executed" measurement by the given
	 * value.
	 * 
	 * @param corresponds
	 *            to the number of similar instances for the updated trace
	 */
	public void updateSuccessfullyExecutedInstances(int value) {
		numberOfSuccessfullyExecuted = numberOfSuccessfullyExecuted + value;
	}

	/**
	 * Adds up the given consumed tokens to the global measurement.
	 * 
	 * @param consumed
	 *            the tokens that were consumed for the trace
	 */
	public void updateConsumedTokens(int consumed) {
		numberOfConsumedTokens = numberOfConsumedTokens + consumed;
	}

	/**
	 * Adds up the given produced tokens to the global measurement.
	 * 
	 * @param produced
	 *            the tokens that were consumed for the trace
	 */
	public void updateProducedTokens(int produced) {
		numberOfProducedTokens = numberOfProducedTokens + produced;
	}

	/**
	 * Updates the mean number of enabled transitions measurement for the given
	 * log trace according to the number of similar instances.
	 * 
	 * @param the
	 *            mean number of enabled transitions during replay for the log
	 *            trace
	 * @param the
	 *            number of similar process instances (control-flow perspective)
	 */
	public void updateMeanOfEnabledTransitions(float mean, int similarInst) {
		float similarInstances = (float) similarInst;
		overallMeanNumber = overallMeanNumber + mean * similarInstances;
	}

	// /////////// VISUALIZATION METHODS
	// ////////////////////////////////////////

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
		if (newVisualization.toString().equals(DisplayState.MODEL.toString())) {
			// only change if state is new
			if (getCurrentVisualizationState().equals(
					newVisualization.toString()) == false) {
				replayedPetriNet = new FitnessVisualization(
						(DiagnosticPetriNet) replayedPetriNet);
			}
			// but set always the current options state
			FitnessVisualization currentVisualization = (FitnessVisualization) replayedPetriNet;
			currentVisualization.tokenCouterOption = newVisualization.tokenCouterOption;
			currentVisualization.remainingTransitionsOption = newVisualization.remainingTransitionsOption;
			currentVisualization.failedTransitionsOption = newVisualization.failedTransitionsOption;
			currentVisualization.pathCoverageOption = newVisualization.pathCoverageOption;
			currentVisualization.passedEdgesOption = newVisualization.passedEdgesOption;
		} else if (newVisualization.toString().equals(
				DisplayState.LOG.toString())) {
			// only change if state is new
			if (getCurrentVisualizationState().equals(
					newVisualization.toString()) == false) {
				replayedPetriNet = new FitnessLogTraceVisualization(
						(DiagnosticPetriNet) replayedPetriNet);
			}
			// but set always the current options state
			FitnessLogTraceVisualization currentVisualization = (FitnessLogTraceVisualization) replayedPetriNet;
			currentVisualization.failedEventsOption = newVisualization.failedEventsOption;
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
		myResultVisualization = ((DiagnosticPetriNet) replayedPetriNet)
				.getGrappaVisualization();
		return myResultVisualization;
	}
}
