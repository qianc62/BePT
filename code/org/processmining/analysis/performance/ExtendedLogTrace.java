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

import java.util.Date;

import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;

/**
 * This class is used to enhance log traces with performance information, such
 * as throughput time.
 * 
 * @author Peter Hornix (p.t.g.hornix@student.tue.nl)
 */
public class ExtendedLogTrace extends ReplayedLogTrace {

	// keep track of begin and end date of each trace.
	private Date beginDate = null;
	private Date endDate = null;

	/**
	 * Data structure for storage of the proper termination status (i.e., no
	 * tokens are left after replaying the trace) of each process instance. For
	 * every entry the key is the ID of the corresponding process instance and
	 * the value is the bolean value for that case.
	 */
	private boolean properlyTerminated = true;

	/**
	 * Data structure for storage of the successful execution status (i.e., no
	 * tokens had been artificially created while replaying the trace) of each
	 * process instance. For every entry the key is the ID of the corresponding
	 * process instance and the value is the bolean value for that case.
	 */
	private boolean successfullyExecuted = true;

	/**
	 * Create an extended log trace by copying the ordinary process instance.
	 * 
	 * @param pi
	 *            The template process instance used to create the diagnostic
	 *            log trace.
	 */
	public ExtendedLogTrace(ProcessInstance pi) {
		super(pi);
	}

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
	 * Set the successfullyExecuted attribute.
	 * 
	 * @param value
	 *            The new value to be set.
	 */
	public void setSuccessfullyExecuted(boolean value) {
		successfullyExecuted = value;
	}

	/**
	 * Get the properlyTeminated value for that trace.
	 * 
	 * @return The boolean value of having properly terminated.
	 */
	public boolean hasProperlyTerminated() {
		return properlyTerminated;
	}

	/**
	 * Get the successfullyExecuted value for that trace.
	 * 
	 * @return The boolean value of having successfully executed.
	 */
	public boolean hasSuccessfullyExecuted() {
		return successfullyExecuted;
	}

	/**
	 * Returns the begin date and time of the trace
	 * 
	 * @return Date
	 */
	public Date getBeginDate() {
		return (beginDate);
	}

	/**
	 * Returns the end date and time of the trace.
	 * 
	 * @return Date
	 */
	public Date getEndDate() {
		return (endDate);
	}

	/**
	 * Records time-related metrics for this process instance. Needed to be able
	 * to calculate the throughput time of the trace. (Called each time a
	 * transition fires in this trace, during log replay)
	 * 
	 * @param fireTime
	 *            Date: time at which a transition fires
	 */
	public void recordThroughputTimeMeasurement(Date fireTime) {
		// compare fireTime with beginDate. If fireTime occurs earlier or
		// beginDate
		// equals null, set beginDate to fireTime
		if ((beginDate == null) || (fireTime.before(beginDate))) {
			beginDate = fireTime;
		}
		// compare fireTime with endDate. If fireTime occurs later or endDate
		// equals
		// null, set endDate to fireTime
		if ((endDate == null) || (fireTime.after(endDate))) {
			endDate = fireTime;
		}
	}

}
