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
package org.processmining.analysis.mdl;

import java.util.HashSet;
import java.util.Iterator;

import org.processmining.analysis.conformance.MaximumSearchDepthDiagnosis;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.State;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.CoverabilityGraphBuilder;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisMethod;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisResult;
import org.processmining.framework.models.petrinet.algorithms.logReplay.Measurer;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedTransition;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;

/**
 * Base metric for all MDL metrics that measure how much the model allows to
 * "compress" the log. The idea is that it should be more efficient to specify
 * the log with respect to a model than to explicitly give the log (as, e.g., in
 * a sequence it is clear which is the next element depending on the current log
 * replay status - so no encoding effort is needed at all). <br>
 * The so-called "preciseness" metrics are based on the log replay as
 * implemented in the ProM framework, and may or may not deal with errors (i.e.,
 * fitness problems).
 * 
 * @see MDLCompactnessMetric
 * 
 * @author Anne Rozinat, Christian Guenther
 */
public abstract class MDLPrecisenessMetric extends MDLBaseMetric {

	protected PrecisenessMeasurer measurer;
	protected PetriNet net;
	protected LogReader log;

	/**
	 * Creates a new "preciseness" metric for the given petri net and event log.
	 * 
	 * @param aName
	 *            the name of this metric (to be passed to super)
	 * @param aDescription
	 *            the description of this metric (to be passed to super)
	 */
	protected MDLPrecisenessMetric(String aName, String aDescription) {
		super(aName, aDescription);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.mdl.MDLBaseMetric#getEncodingCost(org.
	 * processmining.framework.ui.slicker.ProgressPanel)
	 */
	public int getEncodingCost(ProgressPanel progress, PetriNet aNet,
			LogReader aLog) {
		net = aNet;
		log = aLog;
		// invoke log replay analysis method --> "Preciseness" measure part
		LogReplayAnalysisMethod logReplayAnalysis = new LogReplayAnalysisMethod(
				net, log, measurer, progress);
		int maxSearchDepth = MaximumSearchDepthDiagnosis
				.determineMaximumSearchDepth(net);

		// automatically set maximum search depth for log replay
		logReplayAnalysis.setMaxDepth(maxSearchDepth);
		logReplayAnalysis.analyse(null);

		// get the collected encoding costs from the measurer object
		return measurer.getEncodingCost();
	}

	/**
	 * Custom measurer that collects data during log replay that is needed to
	 * calculate the enconding costs of the corresponding "preciseness" metric.
	 * 
	 * @author Anne Rozinat
	 */
	abstract class PrecisenessMeasurer extends Measurer {

		protected double totalEncodingCost = 0;
		protected double traceEncodingCost = 0;
		protected int groupedInstances = 0;
		protected int noOfErrors = 0; // number of errors = corresponds to k

		/**
		 * Measurer collected data during log replay. This is now used to
		 * calculate the total encoding costs.
		 * 
		 * @return encoding cost
		 */
		protected int getEncodingCost() {
			// give upper bound
			return getUpperBound(totalEncodingCost);
		}

		// ///////////////// Measurer methods /////////////////////

		/**
		 * Resets the encoding costs at the beginning of each new log replay.
		 */
		protected void initLogReplay() {
			totalEncodingCost = 0;
			traceEncodingCost = 0;
			noOfErrors = 0;
			groupedInstances = 0;
			// debug
			Message.add("\n\n", 3);
		}

		/**
		 * Records the number of grouped process instances.
		 */
		protected void initTraceReplay(ReplayedLogTrace pi,
				LogReplayAnalysisResult result) {
			traceEncodingCost = 0;
			noOfErrors = 0;
			groupedInstances = MethodsForWorkflowLogDataStructures
					.getNumberSimilarProcessInstances(pi.getProcessInstance());
		}

		/**
		 * Measures the Number of enabled tasks at current replay stage (before
		 * replay of the ate).
		 */
		protected void takePreStepExecutionMeasurement(
				LogReplayAnalysisResult result, ReplayedLogTrace pi,
				int maxDepth) {
			int noOfEnabledTasks = determineNumberOfEnabledTasks(result, pi,
					maxDepth);
			// record measurement for current ate
			evaluateNumberOfEnabledTasks(noOfEnabledTasks);
		}

		/**
		 * Increments the counter of errors in the measurer object.
		 */
		protected void takeFailedTaskMeasurement(ReplayedLogTrace pi,
				ReplayedTransition t, AuditTrailEntry ate) {
			noOfErrors++;
		}

		/**
		 * Takes the number of grouped traces into account.
		 */
		protected void takePostTraceReplayMeasurement(
				LogReplayAnalysisResult result, ReplayedLogTrace pi) {
			totalEncodingCost = totalEncodingCost
					+ (groupedInstances * getUpperBound(traceEncodingCost));
		}

		// //////////////////////////// Custom methods
		// ////////////////////////////

		/**
		 * Use the number of enabled tasks (measured before each replay step) to
		 * calculate different encoding costs.
		 * 
		 * @param amountOfEnabled
		 */
		protected void evaluateNumberOfEnabledTasks(int amountOfEnabled) {
			// do nothing (to be implemented in subclasses)
		}

		/**
		 * TODO: move this method to log replay package to avoid code
		 * duplication
		 * 
		 * Determines the number of enabled tasks while transparently treating
		 * invisible tasks.
		 * 
		 * @param result
		 * @param pi
		 * @param maxDepth
		 * @return
		 */
		protected int determineNumberOfEnabledTasks(
				LogReplayAnalysisResult result, ReplayedLogTrace pi,
				int maxDepth) {
			// build (partial) coverability graph from the current marking,
			// note that the net must be cloned before to preserve its replay
			// state
			PetriNet clonedNet = (PetriNet) result.replayedPetriNet.clone();
			// (add 1 as we want to look at enabled tasks from current replay
			// marking)
			StateSpace coverabilityGraph = CoverabilityGraphBuilder.build(
					clonedNet, maxDepth + 1);
			// find current replay state in state space (i.e., the start state)
			State replayState = (State) coverabilityGraph.getStartState();

			// hash set containing the all the visible tasks that have already
			// been reached in the state space (i.e., were already counted being
			// enabled)
			HashSet<Transition> alreadyCounted = new HashSet<Transition>();

			// get upper limit
			int maxEnabled = result.replayedPetriNet.getNumberOfVisibleTasks();
			// contains the states that have been visited already
			HashSet<State> visitedStates = new HashSet<State>();

			// start recursive procedure
			alreadyCounted = traceEnabledTasks(alreadyCounted,
					coverabilityGraph, replayState, maxEnabled, visitedStates);

			// return number of enabled tasks
			return alreadyCounted.size();
		}

		/**
		 * Recursively trace the given state space for enabled transitions while
		 * adding newly found (visible) tasks to the alreadyCounted set.
		 * 
		 * @param alreadyCounted
		 *            Contains the visible (transparently) enabled transitions
		 *            so far encountered.
		 * @param coverabilityGraph
		 *            The coverability graph to be traversed.
		 * @param currentState
		 *            The current traversal state.
		 * @return The transitions so far encountered.
		 */
		private HashSet<Transition> traceEnabledTasks(
				HashSet<Transition> alreadyCounted,
				StateSpace coverabilityGraph, State currentState,
				int maxEnabled, HashSet<State> visitedStates) {

			if ((visitedStates.contains(currentState) == true)
					|| (alreadyCounted.size() == maxEnabled)) {
				// abort to prevent infinite cycles and
				// stop tracing paths if all possible tasks are already
				// contained
				return alreadyCounted;
			}

			HashSet<Transition> mergedAlreadyCounted = new HashSet<Transition>();
			if (currentState.getOutEdges() != null) {
				Iterator outgoingEdges = currentState.getOutEdges().iterator();
				while (outgoingEdges.hasNext()) {
					ModelGraphEdge currentEdge = (ModelGraphEdge) outgoingEdges
							.next();
					Transition associatedTransition = (Transition) currentEdge.object;
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
	}
}
