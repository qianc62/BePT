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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.State;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.CoverabilityGraphBuilder;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisMethod;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisResult;
import org.processmining.framework.models.petrinet.algorithms.logReplay.Measurer;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedPlace;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedTransition;
import org.processmining.framework.ui.Message;

/**
 * Actually takes those measurements that are needed to calculate the selected
 * conformance metrics.
 * 
 * @author arozinat
 */
public class ConformanceMeasurer extends Measurer {

	// / advanced behavioral appropriateness
	/**
	 * Maps the name of each log event within the current trace on its
	 * diagnostic data structure (which includes their relations to other log
	 * events).
	 */
	private DiagnosticLogEventRelation logEvents = new DiagnosticLogEventRelation();

	// / fitness
	/**
	 * Records every replayed trace for which tokens were missing during log
	 * replay. The total amount is then recorded at that place at the end of the
	 * replay.
	 */
	private HashMap<DiagnosticPlace, Integer> missingTokens = new HashMap<DiagnosticPlace, Integer>();
	/**
	 * Remembers those places that had either tokens missing or remaining in
	 * this trace (used for tool tip in visualization).
	 */
	private HashSet<DiagnosticPlace> wrongTokenPlaces = new HashSet<DiagnosticPlace>();
	/**
	 * Remembers those transitions that have failed execution for this trace
	 * (used for tool tip in visualization).
	 */
	private HashSet<DiagnosticTransition> failedTransitions = new HashSet<DiagnosticTransition>();
	/**
	 * Remembers those transitions that have remained enabled for this trace
	 * (used for tool tip in visualization).
	 */
	private HashSet<DiagnosticTransition> remainedTransitions = new HashSet<DiagnosticTransition>();
	/**
	 * The number of consumed tokens during log replay is used to (a) calculate
	 * the basic fitness measure of being successfully executed based on missing
	 * tokens (b) record the fitness measurement for this trace directly in the
	 * event log itself
	 */
	private int numberOfConsumedTokens = 0;
	/**
	 * The number of produced tokens during log replay of this trace is used to
	 * (a) calculate the basic fitness measure of being properly terminated
	 * based on remaining tokens (b) record the fitness measurement for this
	 * trace directly in the event log itself
	 */
	private int numberOfProducedTokens = 0;
	/**
	 * The number of missing tokens during log replay of this trace is used to
	 * record the fitness measurement for this trace directly in the event log
	 * itself
	 */
	private int numberOfMissingTokens = 0;
	/**
	 * The number of remaining tokens during log replay of this trace is used to
	 * record the fitness measurement for this trace directly in the event log
	 * itself
	 */
	private int numberOfRemainingTokens = 0;

	// / simple behavioral appropriateness
	/**
	 * The sume of enabled transitions during replay of current trace (to be set
	 * in relation with the number of measurements).
	 */
	private int sumOfEnabledTransitions = 0;
	/**
	 * The number of measurements taken so far for the
	 * meanNumberOfEnabledTransitions measure. This is used to allow the
	 * incremental addition of measurements.
	 */
	private int numberOfMeasurements = 0;

	// / proper termination
	/**
	 * Data structure for storage of the proper termination status (i.e., no
	 * tokens are left after replaying the trace) of each process instance. For
	 * every entry the key is the ID of the corresponding process instance and
	 * the value is the bolean value for that case.
	 */
	private boolean properlyTerminated = true;

	// / successful execution
	/**
	 * Data structure for storage of the successful execution status (i.e., no
	 * tokens had been artificially created while replaying the trace) of each
	 * process instance. For every entry the key is the ID of the corresponding
	 * process instance and the value is the bolean value for that case.
	 */
	private boolean successfullyExecuted = true;

	// / for writing back the fitness values
	private ProcessInstance currentInstance; // for writing the actual fitness

	// value

	/**
	 * {@inheritDoc} Creates an analysis result of type
	 * {@link ConformanceLogReplayResult ConformanceLogReplayResult}.
	 */
	protected LogReplayAnalysisResult initLogReplayAnalysisResult(
			AnalysisConfiguration analysisOptions, PetriNet petriNet,
			LogReader log, LogReplayAnalysisMethod analysisMethod) {
		// notify the user that
		if (log instanceof BufferedLogReader == false) {
			Message
					.add("This Log-reader is READ-ONLY, so the fitness results will not be stored."
							+ " Please select another Log-Reader under 'Help'.");
		}
		// initialize the result object and the analysis options
		return new ConformanceLogReplayResult(analysisOptions, petriNet, log,
				analysisMethod);
	}

	/**
	 * {@inheritDoc} Resets the log-based data structures for the replay of this
	 * trace.
	 * 
	 * @param pi
	 *            (not used)
	 * 
	 */
	protected void initTraceReplay(ReplayedLogTrace pi,
			LogReplayAnalysisResult result) {
		logEvents = new DiagnosticLogEventRelation();
		missingTokens = new HashMap<DiagnosticPlace, Integer>();
		wrongTokenPlaces = new HashSet<DiagnosticPlace>();
		failedTransitions = new HashSet<DiagnosticTransition>();
		remainedTransitions = new HashSet<DiagnosticTransition>();
		numberOfConsumedTokens = 0;
		numberOfProducedTokens = 0;
		numberOfMissingTokens = 0; // for fitness recording at log trace itself
		numberOfRemainingTokens = 0; // for fitness recording at log trace
		// itself
		sumOfEnabledTransitions = 0;
		numberOfMeasurements = 0;
		properlyTerminated = true;
		successfullyExecuted = true;
		currentInstance = pi.getProcessInstance();

		// transparently put artificial start task
		if (((ConformanceLogReplayResult) result)
				.calculateImprovedBehavioralAppropriateness() == true) {
			logEvents.addDiagnosticLogEvent("Start", "Artificial");
		}
	}

	/**
	 * {@inheritDoc} For behavioral appropriateness metric the number of enabled
	 * transitions needs to be checked before each replay step.
	 */
	protected void takePreStepExecutionMeasurement(
			LogReplayAnalysisResult result, ReplayedLogTrace pi, int maxDepth) {
		// only if the corresponding metric was chosen to be computed
		// (performance reasons)
		if (((ConformanceLogReplayResult) result)
				.calculateBehavioralAppropriateness() == true) {
			// take measurement before each replay step (needed for
			// appropriateness metric)
			determineNumberOfEnabledTransitions(
					(DiagnosticPetriNet) result.replayedPetriNet,
					(DiagnosticLogTrace) pi, maxDepth);
		}
	}

	/**
	 * {@inheritDoc} For the improved behavioral appropriateness metric one
	 * needs to keep track of the relations between log events.
	 */
	protected void takeLogEventRecordingMeasurement(
			LogReplayAnalysisResult result, ReplayedLogTrace pi,
			AuditTrailEntry ate) {
		// if improved behavioral appropriateness should be calculated,
		// the log event relations must be updated
		if (((ConformanceLogReplayResult) result)
				.calculateImprovedBehavioralAppropriateness() == true) {
			logEvents.addDiagnosticLogEvent(ate.getElement(), ate.getType());
		}
	}

	/**
	 * {@inheritDoc} The replay failure will be recorded for the corresponding
	 * trace, transition, and audit trail entry.
	 */
	protected void takeFailedTaskMeasurement(ReplayedLogTrace pi,
			ReplayedTransition t, AuditTrailEntry ate) {
		try {
			// set successful execution related measures
			DiagnosticLogTrace trace = (DiagnosticLogTrace) pi;
			this.setSuccessfullyExecuted(false);
			DiagnosticTransition trans = (DiagnosticTransition) t;
			trans.setFailedExecution();
			failedTransitions.add(trans);
			DiagnosticAuditTrailEntry diag = trace.getAteDiagnostic(ate);
			diag.setFailedExecution();
		} catch (Exception ex) {
			Message.add(
					"Punishing failed task produced an error for transition "
							+ t.getIdentifier() + " in trace " + pi.getName()
							+ ".\n" + ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc} Records the missing token at the corresponding place in the
	 * Petri net.
	 */
	protected void takeMissingTokenMeasurement(ReplayedPlace p,
			ReplayedLogTrace pi) {
		DiagnosticPlace place = (DiagnosticPlace) p;
		// check whether current place already has a "missing" measurement
		if (missingTokens.containsKey(place)) {
			// merge with existing entry
			Integer oldValue = (Integer) missingTokens.get(place);
			int newValue = oldValue.intValue() + 1;
			missingTokens.put(place, new Integer(newValue));
		} else {
			// add new entry
			missingTokens.put(place, new Integer(1));
		}
		numberOfMissingTokens++; // for the fitness measurement of this trace in
		// the event log itself
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.petrinet.algorithms.logReplay.Measurer
	 * #
	 * takePostTraceReplayMeasurement(org.processmining.framework.models.petrinet
	 * .algorithms.logReplay.LogReplayAnalysisResult,
	 * org.processmining.framework
	 * .models.petrinet.algorithms.logReplay.ReplayedLogTrace)
	 */
	protected void takePostTraceReplayMeasurement(
			LogReplayAnalysisResult result, ReplayedLogTrace pi) {
		// updating the total number of traces is needed both for fitness
		// metrics and for simple behavioral
		// appropriateness metric!
		ConformanceLogReplayResult conformanceResult = (ConformanceLogReplayResult) result;
		conformanceResult.updateNumberOfProcessInstances(pi
				.getNumberOfProcessInstances());

		if (conformanceResult.calculateFitness() == true) {
			// determines the remaining tokens during log replay
			takeRemainingTokensMeasurement(result, pi);
			// add up fitness-related measures
			updateGlobalFitnessMeasurements(result, pi);
		}
		if (conformanceResult.calculateBehavioralAppropriateness() == true) {
			// adds up the mean number of enabled transitions
			updateGlobalMeanNumberOfEnabledTransitions(result, pi);
		}
		if (conformanceResult.calculateImprovedBehavioralAppropriateness() == true) {
			// globally includes the log relations for this trace
			updateGlobalLogEventRelations(result, pi);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.petrinet.algorithms.logReplay.Measurer
	 * #takePostReplayMeasurement(org.processmining.framework.models.petrinet.
	 * algorithms.logReplay.LogReplayAnalysisResult)
	 */
	protected void takePostReplayMeasurement(LogReplayAnalysisResult result) {
		DiagnosticLogEventRelation globalRelation = ((ConformanceLogReplayResult) result)
				.getLogEventRelations();
		// fill non-existing pre- or post-occurrence entries with zero
		// (needed to calculate Never and Sometimes relations)
		globalRelation.completeRelationByZeroEntries();
	}

	/**
	 * Records the number of remaining tokens.
	 * 
	 * @param result
	 *            the result object filled by the log replay method
	 * @param pi
	 *            the trace which is currently replayed
	 */
	private void takeRemainingTokensMeasurement(LogReplayAnalysisResult result,
			ReplayedLogTrace pi) {
		try {
			// finally count the amount of remaining tokens for that instance
			// and store it
			Iterator remainingTokenChecker = result.replayedPetriNet
					.getPlaces().iterator();
			while (remainingTokenChecker.hasNext()) {
				DiagnosticPlace currentPlace = (DiagnosticPlace) remainingTokenChecker
						.next();
				// treat final place differently
				if (currentPlace.outDegree() == 0) {
					// one single token in the final place is required
					if (currentPlace.getNumberOfTokens() == 0) {
						// update corresponding missingTokens measure
						currentPlace.addMissingTokens(1, pi
								.getNumberOfProcessInstances());
						// record that something was "wrong"
						wrongTokenPlaces.add(currentPlace);
						// set successful execution measure
						this.setSuccessfullyExecuted(false);
					} else if (currentPlace.getNumberOfTokens() > 1) {
						// update corresponding remainingToken measure
						int carryover = currentPlace.getNumberOfTokens() - 1;
						currentPlace.addRemainingTokens(carryover, pi
								.getNumberOfProcessInstances());
						// record that something were "wrong"
						wrongTokenPlaces.add(currentPlace);
						// set proper completion measure
						this.setProperlyTerminated(false);
						// for the fitness measurement of this trace in the
						// event log itself
						numberOfRemainingTokens = numberOfRemainingTokens
								+ carryover;
					}
				}
				// treat non-final places
				else {
					if (currentPlace.getNumberOfTokens() > 0) {
						// update corresponding remainingToken measure
						int carryover = currentPlace.getNumberOfTokens();
						currentPlace.addRemainingTokens(carryover, pi
								.getNumberOfProcessInstances());
						// record that something were "wrong"
						wrongTokenPlaces.add(currentPlace);
						// set proper completion measure
						this.setProperlyTerminated(false);
						// for the fitness measurement of this trace in the
						// event log itself
						numberOfRemainingTokens = numberOfRemainingTokens
								+ carryover;

						// mark transitions remaining enabled
						Iterator potentiallyEnabled = currentPlace
								.getSuccessors().iterator();
						while (potentiallyEnabled.hasNext()) {
							DiagnosticTransition currentTransition = (DiagnosticTransition) potentiallyEnabled
									.next();
							if (currentTransition.isEnabled()) {
								currentTransition.setRemainingEnabled();
								remainedTransitions.add(currentTransition);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			Message.add("Taking post replay measurements failed for trace "
					+ pi.getName() + ".\n" + ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * Updates all the fitness-related measurements on the global level, so that
	 * the currently replayed trace is taken into account.
	 */
	private void updateGlobalFitnessMeasurements(
			LogReplayAnalysisResult result, ReplayedLogTrace pi) {
		ConformanceLogReplayResult conformanceResult = (ConformanceLogReplayResult) result;
		int numberOfSimilar = pi.getNumberOfProcessInstances();
		// update consumed tokens counter
		conformanceResult.updateConsumedTokens(getNumberOfConsumedTokens()
				* numberOfSimilar);
		// update produced tokens counter
		conformanceResult.updateProducedTokens(getNumberOfProducedTokens()
				* numberOfSimilar);

		// update missing tokens in diagnostic petri net
		Iterator<DiagnosticPlace> it = missingTokens.keySet().iterator();
		while (it.hasNext()) {
			DiagnosticPlace place = it.next();
			Integer missingValue = missingTokens.get(place);
			place.addMissingTokens(missingValue.intValue(), pi
					.getNumberOfProcessInstances());
			// record that something were "wrong"
			wrongTokenPlaces.add(place);
		}

		// check for each place whether there were tokens missing or remaining
		// during the
		// replay of this trace, and if not: record that zero tokens were
		// "wrong"
		Iterator<Place> placesInNet = result.replayedPetriNet.getPlaces()
				.iterator();
		while (placesInNet.hasNext()) {
			DiagnosticPlace currentPlace = (DiagnosticPlace) placesInNet.next();
			if (wrongTokenPlaces.contains(currentPlace) == false) {
				currentPlace.addNumberInstancesNeitherMissingNorRemaining(pi
						.getNumberOfProcessInstances());
			}
		}

		// check for each transition whether (a) failed execution or (b)
		// remained enabled
		// (used for tool tip in visualization)
		Iterator<Transition> transitionsInNet = result.replayedPetriNet
				.getTransitions().iterator();
		while (transitionsInNet.hasNext()) {
			DiagnosticTransition currentTransition = (DiagnosticTransition) transitionsInNet
					.next();
			boolean failed = failedTransitions.contains(currentTransition);
			boolean remained = remainedTransitions.contains(currentTransition);
			if (failed == true) {
				currentTransition.addFailedInstances(pi
						.getNumberOfProcessInstances());
			}
			if (remained == true) {
				currentTransition.addRemainedInstances(pi
						.getNumberOfProcessInstances());
			}
			if (failed == false && remained == false) {
				currentTransition
						.addNumberInstancesNeitherFailedNorRemaining(pi
								.getNumberOfProcessInstances());
			}
		}

		// update log-centered measures
		if (hasProperlyTerminated() == true) {
			conformanceResult.updateProperlyCompletedInstances(pi
					.getNumberOfProcessInstances());
		}
		if (hasSuccessfullyExecuted() == true) {
			conformanceResult.updateSuccessfullyExecutedInstances(pi
					.getNumberOfProcessInstances());
		}
		if (hasProperlyTerminated() == true
				&& hasSuccessfullyExecuted() == true) {
			conformanceResult.addFittingInstance(pi);
		}

		// check whether fitness value is already recorded
		if (currentInstance.getAttributes().containsKey("Fitness") == false) {
			// record the fitness for this trace in the log itself
			currentInstance.setAttribute("Fitness",
					getFitnessAttributeForCurrentTrace());
		}
	}

	/**
	 * Updates the mean number of enabled transitions measurement on the
	 * aggregate level for the whole log (selection).
	 * 
	 * @param result
	 *            the result object filled by the log replay method
	 * @param pi
	 *            the trace which is currently replayed
	 */
	private void updateGlobalMeanNumberOfEnabledTransitions(
			LogReplayAnalysisResult result, ReplayedLogTrace pi) {
		ConformanceLogReplayResult conformanceResult = (ConformanceLogReplayResult) result;
		conformanceResult.updateMeanOfEnabledTransitions(
				getMeanNumberOfEnabledTransitions(), pi
						.getNumberOfProcessInstances());
	}

	/**
	 * Updates the global relation matrix.
	 * 
	 * @param result
	 *            the result object filled by the log replay method
	 * @param pi
	 *            the trace which is currently replayed
	 */
	private void updateGlobalLogEventRelations(LogReplayAnalysisResult result,
			ReplayedLogTrace pi) {

		// transparently put artificial end task
		if (((ConformanceLogReplayResult) result)
				.calculateImprovedBehavioralAppropriateness() == true) {
			logEvents.addDiagnosticLogEvent("End", "Artificial");
		}

		Iterator<DiagnosticLogEvent> it = logEvents.getDiagnosticLogEvents()
				.iterator();
		DiagnosticLogEventRelation globalRelation = ((ConformanceLogReplayResult) result)
				.getLogEventRelations();
		while (it.hasNext()) {
			DiagnosticLogEvent logEvent = it.next();
			globalRelation.updateLogEventRelation(logEvent, pi
					.getNumberOfProcessInstances());
		}
	}

	// /////////////////// SPECIAL PURPOSE METHODS
	// //////////////////////////////

	/**
	 * Check how many transitions are enabled and submit the measurement to the
	 * mean number of enabled transitions value for the given trace, which is
	 * used to calculate the behavioral appropriateness metric aB.
	 * 
	 * @param diagnosticNet
	 *            The enhanced Petri net in which the log is replayed.
	 * @param pi
	 *            The log trace currently replayed.
	 */
	private void determineNumberOfEnabledTransitions(
			DiagnosticPetriNet diagnosticNet, DiagnosticLogTrace pi,
			int maxDepth) {
		// build coverability graph from the current marking,
		// note that the net must be cloned before to preserve its replay state
		DiagnosticPetriNet clonedNet = (DiagnosticPetriNet) diagnosticNet
				.clone();
		// maxDepth + 1 as looking at enabled tasks from current marking are on
		// next level
		// (different from transparent firing)
		StateSpace coverabilityGraph = CoverabilityGraphBuilder.build(
				clonedNet, maxDepth + 1);
		// find current replay state in state space (i.e., the start state)
		State replayState = (State) coverabilityGraph.getStartState();

		// hash set containing the all the visible tasks that have already
		// been reached in the state space (i.e., were already counted being
		// enabled)
		HashSet alreadyCounted = new HashSet();

		// get upper limit
		int maxEnabled = diagnosticNet.getNumberOfVisibleTasks();
		// contains the states that have been visited already
		HashSet visitedStates = new HashSet();

		// start recursive procedure
		alreadyCounted = traceEnabledTasks(alreadyCounted, coverabilityGraph,
				replayState, maxEnabled, visitedStates);

		// add new measurement to trace
		this.addMeanNumberEnabledMeasurement(alreadyCounted.size());
	}

	/**
	 * Recursively trace the given state space for enabled transitions while
	 * adding newly found (visible) tasks to the alreadyCounted set.
	 * 
	 * @param alreadyCounted
	 *            Contains the visible (transparently) enabled transitions so
	 *            far encountered.
	 * @param coverabilityGraph
	 *            The coverability graph to be traversed.
	 * @param currentState
	 *            The current traversal state.
	 * @return The IDs of the transitions so far encountered.
	 */
	private HashSet traceEnabledTasks(HashSet alreadyCounted,
			StateSpace coverabilityGraph, State currentState, int maxEnabled,
			HashSet visitedStates) {

		if ((visitedStates.contains(currentState) == true)
				|| (alreadyCounted.size() == maxEnabled)) {
			// abort to prevent infinite cycles and
			// stop tracing paths if all possible tasks are already contained
			return alreadyCounted;
		}

		HashSet mergedAlreadyCounted = new HashSet();
		// null check added for bugfix
		// TODO - find out whether, if no outgoing edges exist for this state,
		// not rather an empty list should be
		// delivered (instead of null)
		if (currentState.getOutEdges() != null) {
			Iterator outgoingEdges = currentState.getOutEdges().iterator();
			while (outgoingEdges.hasNext()) {
				ModelGraphEdge currentEdge = (ModelGraphEdge) outgoingEdges
						.next();
				DiagnosticTransition associatedTransition = (DiagnosticTransition) currentEdge.object;
				// spawn new path for every newly found invisible task
				if (associatedTransition.isInvisibleTask()) {
					// remember to prevent infinite loops
					visitedStates.add(currentState);
					State nextState = (State) currentEdge.getDest();
					// recursive call (merge sets)
					mergedAlreadyCounted.addAll(traceEnabledTasks(
							alreadyCounted, coverabilityGraph, nextState,
							maxEnabled, visitedStates));
				}
				// check whether transition was not counted yet
				else if (alreadyCounted.contains(associatedTransition) == false) {
					mergedAlreadyCounted.add(associatedTransition);
				}
			}
		}
		return mergedAlreadyCounted;
	}

	// //////////WRITE ACCESS METHODS FOR DIAGNOSTIC INFORMATION ///////////////

	/**
	 * Set the properlyTerminated attribute.
	 * 
	 * @param value
	 *            The new value to be set.
	 */
	public void setProperlyTerminated(boolean value) {
		properlyTerminated = value;
	}

	/**
	 * Set the properlyTerminated attribute.
	 * 
	 * @param value
	 *            The new value to be set.
	 */
	public void setSuccessfullyExecuted(boolean value) {
		successfullyExecuted = value;
	}

	/**
	 * Increase the numberOfConsumedTokens attribute by the given value.
	 * 
	 * @param value
	 *            The amount to be increased by.
	 */
	public void incrementConsumedTokens(int value) {
		numberOfConsumedTokens = numberOfConsumedTokens + value;
	}

	/**
	 * Increase the numberOfProducedTokens attribute by the given value.
	 * 
	 * @param value
	 *            The amount to be increased by.
	 */
	public void incrementProducedTokens(int value) {
		numberOfProducedTokens = numberOfProducedTokens + value;
	}

	/**
	 * This method is intended to add a new measurement to the mean number of
	 * enabled transitions.
	 * 
	 * @param newNumberEnabled
	 *            The new measurement for the number of enabled transitions.
	 */
	public void addMeanNumberEnabledMeasurement(int newNumberEnabled) {
		sumOfEnabledTransitions = sumOfEnabledTransitions + newNumberEnabled;
		numberOfMeasurements = numberOfMeasurements + 1;
	}

	// /////////// READ ACCESS METHODS FOR DIAGNOSTIC INFORMATION
	// ///////////////

	/**
	 * Get the mean number of enabled transitions during log replay.
	 * 
	 * @return The mean number of enabled transitions for that log trace.
	 */
	public float getMeanNumberOfEnabledTransitions() {
		return (float) sumOfEnabledTransitions / (float) numberOfMeasurements;
	}

	/**
	 * Get the properlyTeminated value for that diagnostic trace.
	 * 
	 * @return The boolean value of having properly terminated.
	 */
	public boolean hasProperlyTerminated() {
		return properlyTerminated;
	}

	/**
	 * Get the successfullyExecuted value for that diagnostic trace.
	 * 
	 * @return The boolean value of having successfully executed.
	 */
	public boolean hasSuccessfullyExecuted() {
		return successfullyExecuted;
	}

	/**
	 * Get the number of consumed tokens while replaying that diagnostic trace.
	 * 
	 * @return The amount of consumed tokens during log replay.
	 */
	public int getNumberOfConsumedTokens() {
		// add 1 for the token consumed in the END place
		return numberOfConsumedTokens + 1;
	}

	/**
	 * Get the number of produced tokens while replaying that diagnostic trace.
	 * 
	 * @return The amount of produced tokens during log replay.
	 */
	public int getNumberOfProducedTokens() {
		// add 1 for the token produced in the START place
		return numberOfProducedTokens + 1;
	}

	/**
	 * Calculates the fitness value for the currently replayed log trace. This
	 * is used to write this diagnostic information back to the log.
	 * 
	 * @return the fitness value as a String
	 */
	public String getFitnessAttributeForCurrentTrace() {
		float missing = (float) numberOfMissingTokens;
		float consumed = (float) getNumberOfConsumedTokens();
		float successfulExecution = (1 - (missing / consumed));

		float remaining = (float) numberOfRemainingTokens;
		float produced = (float) getNumberOfProducedTokens();
		float properCompletion = (1 - (remaining / produced));

		// weigh them to 50% each
		float numResult = ((float) 0.5) * successfulExecution + ((float) 0.5)
				* properCompletion;
		return "" + numResult;
	}
}
