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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedTransition;

/**
 * This class is used to be able to extend a transition with performance
 * information, which is stored during log replay and will be used as part of
 * the extended Petri net.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class ExtendedTransition extends ReplayedTransition {

	/**
	 * The activity of which this transition is a part
	 */
	private ExtendedActivity associatedActivity;

	/**
	 * Map used to store the timestamp at which the transition first fired
	 * during execution of each process instance. The process name is used as
	 * search key.
	 */
	private HashMap firstFireTimes = new HashMap();
	/**
	 * Set used to store the process instances that failed execution, before
	 * this transition first fired in those instances
	 */
	private HashSet failedBefore = new HashSet();
	/**
	 * Set used for remembering those cases in which the transition was not
	 * enabled when it was requested to fire, i.e. needed to be enabled
	 * artificially to allow for progress in log replay.
	 */
	private HashSet failedExecution = new HashSet();

	/**
	 * Calls the constructor of its superclass
	 * 
	 * @param t
	 *            Transition
	 * @param net
	 *            PetriNet
	 * @param caseIDs
	 *            ArrayList
	 */
	public ExtendedTransition(Transition t, PetriNet net, ArrayList caseIDs) {
		super(t, net, caseIDs);
	}

	/**
	 * Adds a timestamp to firstFireTimes, but only if such a timestamp does not
	 * yet exist for the current process instance. Furthermore it passes the
	 * timestamp to the ExtendedLogTrace for throughput time measurements.
	 * 
	 * @param extendedTrace
	 *            ReplayedLogTrace: process instance for which this measurement
	 *            is done
	 * @param timestamp
	 *            Date: timestamp added
	 */
	protected void takeTimeBetweenMeasurement(ReplayedLogTrace extendedTrace,
			Date timestamp) {
		String piName = extendedTrace.getName();
		ExtendedLogTrace lt = (ExtendedLogTrace) extendedTrace;
		if (firstFireTimes.get(piName) == null) {
			// record the first fire time of this transition for this proces
			// instance
			if (!lt.hasProperlyTerminated() || !lt.hasSuccessfullyExecuted()) {
				// the log trace has failed before this point
				failedBefore.add(piName);
			}
			firstFireTimes.put(piName, timestamp);
		}
		// and call throughputTimeMeasurement
		lt.recordThroughputTimeMeasurement(timestamp);
	}

	/**
	 * Will be called directly when this transition has fired. It is needed for
	 * calculation of activity-related times (such as execution time, sojourn
	 * time)
	 * 
	 * @param trace
	 *            ReplayedLogTrace: the trace that is replayed
	 * @param timestamp
	 *            Date: the date at which the transition fired
	 * @param transEnabled
	 *            Date: the date at which the transition was enabled
	 */
	protected void takeActivityMeasurement(ReplayedLogTrace trace,
			Date timestamp, Date transEnabled) {
		if (associatedActivity != null) {
			if (transEnabled == null) {
				// make sure transEnabled has a timestamp associated to it
				transEnabled = timestamp;
			}
			ExtendedLogTrace elt = (ExtendedLogTrace) trace;
			if (!hasFailedExecution(trace.getName())) {
				// this transition has not failed execution in this trace
				if (!elt.hasProperlyTerminated()
						|| !elt.hasSuccessfullyExecuted()) {
					// the log trace has failed execution before this
					// measurement was taken

					associatedActivity.measure(trace.getName(), timestamp,
							transEnabled, this.getLogEvent().getEventType()
									.toLowerCase(), null, true);
				} else {
					// the log trace has not failed execution before this
					// measurement was taken
					associatedActivity.measure(trace.getName(), timestamp,
							transEnabled, this.getLogEvent().getEventType()
									.toLowerCase(), null, false);
				}
			} else {
				// this transition has failed execution in this trace
				associatedActivity.measure(trace.getName(), timestamp,
						transEnabled, this.getLogEvent().getEventType()
								.toLowerCase(), this.getLogEvent()
								.getEventType().toLowerCase(), true);
			}
		}
	}

	/**
	 * Set the corresponding failedExecution entry to true.
	 * 
	 * @param pi
	 *            The trace to be updated.
	 * @throws Exception
	 *             In the case that the specified entry is missing.
	 */
	public void setFailedExecution(ReplayedLogTrace pi) {
		// check existence of entry
		failedExecution.add(pi.getName());
	}

	/**
	 * Determine whether execution failed for the log trace with identifier
	 * piName
	 * 
	 * @param piName
	 *            String: the name of the log trace
	 * @return boolean
	 */
	public boolean hasFailedExecution(String piName) {
		if ((failedExecution == null) || !failedExecution.contains(piName)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Determine whether execution of the trace with identifier piName failed
	 * before the first firing of this transition
	 * 
	 * @param piName
	 *            String: the name of the log trace
	 * @return boolean
	 */
	public boolean hasFailedBefore(String piName) {
		if (!failedBefore.contains(piName)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Sets this transitions to be part of activity
	 * 
	 * @param activity
	 *            PerformanceActivity: the activity with which this transition
	 *            is to be associated
	 */
	public void setAssociatedActivity(ExtendedActivity activity) {
		associatedActivity = activity;
	}

	/**
	 * Returns the activity of which this transition is part
	 * 
	 * @return PerformanceActivity
	 */
	public ExtendedActivity getAssociatedActivity() {
		return associatedActivity;
	}

	/**
	 * Returns the date at which the transition first fired as part of process
	 * instance piName
	 * 
	 * @param piName
	 *            String: the name of the process instance
	 * @return Date
	 */
	public Date getFirstFireTime(String piName) {
		Date tempDate = (Date) firstFireTimes.get(piName);
		return (tempDate);
	}

}
