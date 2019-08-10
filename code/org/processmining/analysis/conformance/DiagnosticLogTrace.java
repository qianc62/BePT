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
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;

/**
 * A data structure that collects diagnostic information for a process instance
 * in the context of conformance checking.
 * 
 * @author arozinat
 */
public class DiagnosticLogTrace extends ReplayedLogTrace {

	/**
	 * TODO: replace by pointers (potentially proxy objects) to those ATEs that
	 * have actually failed
	 * 
	 * Relates each audit trail entry with a diagnostic data structure that
	 * records its conformance check results.
	 */
	private HashMap diagnosticAteMap;

	/**
	 * Create a diagnostic log trace by copying the ordinary process instance.
	 * 
	 * @param pi
	 *            The template process instance used to create the diagnostic
	 *            log trace.
	 */
	public DiagnosticLogTrace(ProcessInstance pi) {
		super(pi);
		diagnosticAteMap = new HashMap();
		Iterator it = pi.getAuditTrailEntryList().iterator();
		while (it.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) it.next();
			diagnosticAteMap.put(ate, new DiagnosticAuditTrailEntry());
		}
	}

	/**
	 * Retrieves the diagnostic data structure for the given audit trail entry.
	 * 
	 * @param ate
	 *            the audit trail entry for which the diagnostic data is
	 *            requested
	 * @return the diagnostic data structure for the given audit trail entry if
	 *         found, <code>null</code> otherwise
	 */
	public DiagnosticAuditTrailEntry getAteDiagnostic(AuditTrailEntry ate) {
		return (DiagnosticAuditTrailEntry) diagnosticAteMap.get(ate);
	}
}
