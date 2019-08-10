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

package org.processmining.framework.log.filter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;
import org.w3c.dom.Node;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class DuplicateTasksLogFilter extends LogFilter {

	/**
	 * Instantiates a LogFilter with a pointer to the lower level filter. When
	 * filtering, the <code>filter(ProcessInstance pi)</code> method should
	 * first call <code>filter.filter(pi)</code>. If that returns false, no
	 * further filtering is necessary.
	 * 
	 * @param lowLevelFilter
	 *            the filter that should be checked first, to see if the
	 *            instance can be discared anyway. Note that this filter can be
	 *            null.
	 */
	public DuplicateTasksLogFilter() {
		super(LogFilter.MODERATE, "Duplicate task filter");
	}

	protected String getHelpForThisLogFilter() {
		return "Removes all AuditTrailEntries from the log that have the "
				+ "same ElementName and EventType as the previous AuditTrailEntry.";
	}

	/**
	 * Method to tell whether this LogFilter changes the log or not.
	 * 
	 * @return boolean True if this LogFilter changes the process instance in
	 *         the <code>filter()</code> method. False otherwise.
	 * @todo Implement this org.processmining.framework.log.LogFilter method
	 */
	public boolean thisFilterChangesLog() {
		return true;
	}

	/**
	 * Filters a single process instance.
	 * 
	 * @param instance
	 *            the process instance to filter
	 * @return true if the whole process instance passes the filter, false if
	 *         the process instance should be discarded.
	 */
	protected boolean doFiltering(ProcessInstance instance) {
		// This is handled by the filter method of LogFilter and does not belong
		// here
		// if ((filter != null) && !filter.filter(instance) ||
		// instance.isEmpty()) {
		// return false;
		// }
		assert (!instance.isEmpty());

		AuditTrailEntryList entries = instance.getAuditTrailEntryList();

		for (int currentIndex = 0; currentIndex < entries.size() - 1; currentIndex++) {
			AuditTrailEntry entry;
			try {
				entry = entries.get(currentIndex);
				int nextIndex = currentIndex + 1;
				AuditTrailEntry nextEntry = entries.get(nextIndex);
				if (entry.getElement().equals(nextEntry.getElement())
						&& entry.getType().equals(nextEntry.getType())) {
					entries.remove(nextIndex);
					currentIndex--; // because one element has been removed from
					// the trace
				}
			} catch (IndexOutOfBoundsException e) {
			} catch (IOException e) {
				return false;
			}

		}

		return !instance.isEmpty();
	}

	/**
	 * Returns a Panel for the setting of parameters.
	 * 
	 * @param summary
	 *            A LogSummary to be used for setting parameters.
	 * @return JPanel
	 * @todo Implement this org.processmining.framework.log.LogFilter method
	 */
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary,
				DuplicateTasksLogFilter.this) {

			protected boolean getAllParametersSet() {
				return true;
			}

			protected JPanel getPanel() {
				return null;
			}

			public LogFilter getNewLogFilter() {
				return new DuplicateTasksLogFilter();
			}
		};
	}

	/**
	 * Write the inside of the <FilterSpecific> tag in the XML export file to
	 * the OutputStream output.
	 * 
	 * @param output
	 *            OutputStream
	 */
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		// do nothing!
	}

	/**
	 * Read the inside of the <FilterSpecific> tag in the XML export file from
	 * the InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		// do nothing!
	}
}
