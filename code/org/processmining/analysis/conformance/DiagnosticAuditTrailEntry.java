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

/**
 * Diagnostic data structure for an audit trail entry.
 * 
 * @author arozinat
 */
public class DiagnosticAuditTrailEntry {

	/**
	 * Indicates whether the firing of the corresponding transition had failed
	 * during log replay.
	 */
	private boolean failedExecution = false;

	/**
	 * Set the failedExecution attribute to true.
	 */
	public void setFailedExecution() {
		failedExecution = true;
	}

	/**
	 * Get the failedExecution value for that diagnostic audit trail entry.
	 * 
	 * @return The boolean value.
	 */
	public boolean getFailedExecutionValue() {
		return failedExecution;
	}
}
