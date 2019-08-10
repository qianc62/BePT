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

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogReader;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;

/**
 * This class is used to enhance the log reader with performance analysis
 * results.
 * 
 * @see ReplayedLogReader
 * @see ExtendedLogTrace
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */

public class ExtendedLogReader extends ReplayedLogReader {

	public ExtendedLogReader(LogReader logReader) {
		super(logReader);
	}

	/**
	 * {@inheritDoc} <br>
	 * Creates a diagnostic data structure for log traces in the context of the
	 * conformance checker.
	 */
	protected ReplayedLogTrace createReplayedLogTrace(ProcessInstance pi) {
		return new ExtendedLogTrace(pi);
	}

	/**
	 * Finds a specific trace in the log.
	 * 
	 * @param id
	 *            the ID of the trace (accessible by getName()) in the log
	 * @return the specified extended log trace if found, <code>null</code>
	 *         otherwise
	 */
	public ExtendedLogTrace getExtendedLogTrace(String id) {
		// check whether entry exists
		if (logTraceMap.containsKey(id)) {
			return (ExtendedLogTrace) logTraceMap.get(id);
		} else {
			return null;
		}
	}

}
