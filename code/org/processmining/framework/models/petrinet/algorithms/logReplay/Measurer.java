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

package org.processmining.framework.models.petrinet.algorithms.logReplay;

import org.processmining.framework.log.*;
import org.processmining.framework.models.petrinet.*;

/**
 * Statelesss strategy object determining which kind of measurements should be
 * taken along the course of log replay. In order to use a Measurer you should
 * derive your own subclass, add a method to this parent class that does nothing
 * (so other subclasses will not inherit your specific measurement strategy),
 * and override it in your subclass in such a way that the desired measurements
 * are taken.
 * 
 * @author Anne Rozinat
 */
public class Measurer {

	/**
	 * To be overridden in subclasses that want create custom analysis results.
	 * 
	 * @param analysisOptions
	 *            the configuration object determining which parts of the
	 *            analysis should be carried out (can be <code>null</code>, then
	 *            all options are interpreted as enabled)
	 */
	protected LogReplayAnalysisResult initLogReplayAnalysisResult(
			AnalysisConfiguration analysisOptions, PetriNet petriNet,
			LogReader log, LogReplayAnalysisMethod analysisMethod) {
		// initialize the result object and the analysis options
		return new LogReplayAnalysisResult(analysisOptions, petriNet, log,
				analysisMethod);
	}

	/**
	 * Will be called once and at the very beginning of replaying the whole log.
	 */
	protected void initLogReplay() {
		// do nothing per default
	}

	/**
	 * Will be called at the very beginning of replaying a trace.
	 * 
	 * @param pi
	 *            the trace currently being replayed
	 * @param result
	 *            the result object filled by the log replay method
	 */
	protected void initTraceReplay(ReplayedLogTrace pi,
			LogReplayAnalysisResult result) {
		// do nothing per default
	}

	/**
	 * Will be called directly after fetching a new log event from the currently
	 * replayed log trace. Does nothing per default (to be overridden in
	 * subclasses if necessary).
	 * 
	 * @param result
	 *            the result object filled by the log replay method
	 * @param pi
	 *            the trace which is currently replayed
	 * @param maxDepth
	 *            the maximum depth until which invisible tasks should be traced
	 *            (e.g., to determine the number of enabled tasks)
	 */
	protected void takePreStepExecutionMeasurement(
			LogReplayAnalysisResult result, ReplayedLogTrace pi, int maxDepth) {
		// do nothing per default
	}

	/**
	 * Will be called directly after fetching the new log event from the
	 * currently replayed log trace.
	 * 
	 * @param result
	 *            the result object filled by the log replay method
	 * @param pi
	 *            the trace which is currently replayed
	 * @param ate
	 *            the log event occurrence currently replayed
	 */
	protected void takeLogEventRecordingMeasurement(
			LogReplayAnalysisResult result, ReplayedLogTrace pi,
			AuditTrailEntry ate) {
		// do nothing per default
	}

	/**
	 * Will be called directly before a failed task becomes artificially enabled
	 * in order to progress with the log replay (non-blocking). Does nothing per
	 * default (to be overridden in subclasses if necessary).
	 * 
	 * @param pi
	 *            the trace which is currently replayed
	 * @param t
	 *            the transition to be artificially enabled
	 * @param ate
	 *            the audit trail entry relating to this failed execution
	 */
	protected void takeFailedTaskMeasurement(ReplayedLogTrace pi,
			ReplayedTransition t, AuditTrailEntry ate) {
		// do nothing per default
	}

	/**
	 * Will be called directly after artificially creating a new token for
	 * enabling a task that has failed execution during log replay. Does nothing
	 * per default (to be overridden in subclasses if necessary).
	 * 
	 * @param p
	 *            the place for which the token is created
	 * @param pi
	 *            the trace which is currently replayed
	 */
	protected void takeMissingTokenMeasurement(ReplayedPlace p,
			ReplayedLogTrace pi) {
		// do nothing per default
	}

	/**
	 * Will be called directly after finishing replay of the current trace. Does
	 * nothing per default (to be overridden in subclasses if necessary).
	 * 
	 * @param result
	 *            the result object filled by the log replay method
	 * @param pi
	 *            the trace which is currently replayed
	 */
	protected void takePostTraceReplayMeasurement(
			LogReplayAnalysisResult result, ReplayedLogTrace pi) {
		// do nothing per default
	}

	/**
	 * Will be called after the whole replay of the log has finished. Does
	 * nothing per default (to be overridden in subclasses if necessary).
	 * 
	 * @param result
	 *            the result object filled by the log replay method
	 */
	protected void takePostReplayMeasurement(LogReplayAnalysisResult result) {
		// do nothing per default
	}

	/**
	 * Will be called directly after the failed task has been artificially
	 * enabled (but before it was fired).
	 * 
	 * @param result
	 *            the result object filled by the log replay method
	 * @param pi
	 *            the trace which is currently replayed
	 * @param maxDepth
	 *            the maximum depth until which invisible tasks should be traced
	 *            (e.g., to determine the number of enabled tasks)
	 */
	protected void takeArtificiallyEnabledMeasurement(
			LogReplayAnalysisResult result, ReplayedLogTrace pi, int maxDepth) {
		// do nothing per default
	}
}
