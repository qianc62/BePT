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

package org.processmining.analysis.ltlchecker;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;

/**
 * CheckResult is a link to a process instance which is checked on a ltl
 * formula. It contains the number of the pi in the log, the name and the
 * process.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public class CheckResult {

	// FIELDS

	/** The number of this pi in the log. */
	private int numberInLog;

	/** The number of this pi in the log. */
	private int numberSimilar;

	/** The name of this process instance. */
	private String name;

	// CONSTRUCTORS

	public CheckResult(int nr, ProcessInstance pi) {
		this.numberInLog = nr;
		this.name = pi.getName();
		this.numberSimilar = MethodsForWorkflowLogDataStructures
				.getNumberSimilarProcessInstances(pi);
	}

	// METHODS

	/**
	 * Get the number of this process instance in the log.
	 * 
	 * @return The number of this process in the log.
	 */
	public int getNumberInLog() {
		return this.numberInLog;
	}

	/**
	 * Get the process instance, given a log.
	 * 
	 * @param log
	 *            The log to found this process instance.
	 * 
	 * @return The process instance corresponding to the number of this in the
	 *         log.
	 */
	public ProcessInstance getProcessInstance(LogReader log) {
		return log.getInstance(numberInLog);
	}

	/**
	 * To string this item.
	 * 
	 * @return The string representation of this.
	 */
	public String toString() {
		return this.name + " (" + numberSimilar + ")";
	}
}
