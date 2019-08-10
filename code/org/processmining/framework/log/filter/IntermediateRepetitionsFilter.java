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

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;
import org.w3c.dom.Node;

/**
 * Filter removes all directly succeding log events that are repetitions (i.e.,
 * occurrences of the same log event) but the first and the last event. The
 * first occurrence is made a Start event, the last occurrence is made an End
 * event.
 * 
 * @author arozinat
 */
public class IntermediateRepetitionsFilter extends LogFilter {

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
	public IntermediateRepetitionsFilter() {
		super(LogFilter.MODERATE, "Repetitions-to-Activity filter");
	}

	protected String getHelpForThisLogFilter() {
		return "Replaces all direct repetitions of the same audit trail entry (equal model element) "
				+ "by one 'start' event with the time stamp from the first occurrence and a 'complete' event "
				+ "with the the time stamp of the last occurrence in this sequence of repetitions. "
				+ "If there is only one single occurrence of one type of audit trail entry, it will be replaced "
				+ "by a 'start' and 'complete' event with the same timestamp. Note that this filter is especially "
				+ "useful after the application of the remap filter in order to map low level events onto a higher "
				+ "level activity type. Keeping the 'start' and 'end' event for each series of repetitions enables "
				+ "the analysis of the higher level log with the Basic Log Statistics plugin.";
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

		try {

			AuditTrailEntryList entries = instance.getAuditTrailEntryList();
			int index = 0;
			boolean isFirst = true; // indicates when a new series of
			// repetitions has started
			AuditTrailEntry current;
			AuditTrailEntry next;

			// walk through the process instance and perform the actual
			// filtering task
			while (index < entries.size()) {
				// get ate at current index position
				current = entries.get(index);

				// check if there is a next ate
				if (index + 1 < entries.size()) {
					next = entries.get(index + 1);
					// check whether this ate is equal to the next one
					if (current.getElement().equals(next.getElement())) {
						// /event type should be ignored so that first start and
						// last complete event are
						// /used for the activity mapping
						// && current.getType().equals(next.getType())) {

						if (isFirst == true) {
							// make start event if is first ate in series of
							// repetitions
							current.setType("start");
							entries.replace(current, index);
							isFirst = false;
							// move to next ate
							index++;
						} else {
							// remove as is intermediate ate in series of
							// repetitions
							entries.remove(index);
							// do not increment index as now points to next ate
						}
					} else {
						// finish this activity
						index = finishActivity(isFirst, current, index, entries);
						isFirst = true;
					}
				} else {
					// finish this activity (is the last)
					index = finishActivity(isFirst, current, index, entries);
					isFirst = true;
				}
			}
			// filtering successful
			return true;
		} catch (Exception ex) {
			Message.add("An error occurred during filtering process instance "
					+ instance.getName());
			ex.printStackTrace();
			// ignore the instance as an error occurred
			return false;
		}
	}

	/**
	 * 
	 * @param isFirst
	 * @param current
	 * @param index
	 * @param entries
	 * @return
	 * @throws Exception
	 */
	private int finishActivity(boolean isFirst, AuditTrailEntry current,
			int index, AuditTrailEntryList entries) throws Exception {
		// is this the first and last ate in a series of repetitions
		if (isFirst == true) {
			// make start and complete ate from this single event
			current.setType("start");
			AuditTrailEntry fakeComplete = (AuditTrailEntry) current.clone();
			fakeComplete.setType("complete");
			entries.replace(current, index);
			entries.insert(fakeComplete, index + 1);
			// move index to ate after fake complete event
			return index + 2;
		} else {
			// simply keep this last event as a complete event
			current.setType("complete");
			entries.replace(current, index);
			// move index to next ate
			return index + 1;
		}
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
				IntermediateRepetitionsFilter.this) {

			protected boolean getAllParametersSet() {
				return true;
			}

			protected JPanel getPanel() {
				return null;
			}

			public LogFilter getNewLogFilter() {
				return new IntermediateRepetitionsFilter();
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
