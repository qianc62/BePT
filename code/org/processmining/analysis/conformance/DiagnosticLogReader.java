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

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogReader;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;

/**
 * This class is used to enhance the log reader with conformance check results.
 * 
 * @see ReplayedLogReader
 * @see DiagnosticLogTrace
 * 
 * @author arozinat
 */
public class DiagnosticLogReader extends ReplayedLogReader {

	/**
	 * Construct an enhanced log reader by wrapping the original one.
	 * Furthermore the diagnostic data structures are initialized by wrapping
	 * all process instances in a diagnostic log trace. Note that it is assumed
	 * here that the log has been pre-processed and similar instances got
	 * summarized (since they share the same diagnostic results), i.e., that the
	 * log has already been aggregated on a logical level.
	 * 
	 * @param logReader
	 *            The original log reader (to be passed to super).
	 */
	public DiagnosticLogReader(LogReader logReader) {
		super(logReader);
	}

	/**
	 * {@inheritDoc} <br>
	 * Creates a diagnostic data structure for log traces in the context of the
	 * conformance checker.
	 */
	protected ReplayedLogTrace createReplayedLogTrace(ProcessInstance pi) {
		return new DiagnosticLogTrace(pi);
	}
}
