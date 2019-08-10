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
public class ActivityInversionFilter extends LogFilter {

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
	public ActivityInversionFilter() {
		super(LogFilter.MODERATE, "Activity Inversion filter");
	}

	protected String getHelpForThisLogFilter() {
		return "This filter replaces two succeeding log events 'a complete' and 'b start' by "
				+ "'a-to-b start' and 'a-to-b complete'. This only works if no parallel activities are recorded in the log, "
				+ "i.e., 'start' and 'end' events are not interleaving. <br> "
				+ "The purpose of this filter is to analyse idle times rather than activity durations (e.g., using the "
				+ "Basic Statistics Plugin). <br>"
				+ "Note that if there are only 'complete' events in the log, then two succeeding 'complete' events are "
				+ "being replaced by a 'start' and 'complete' event, respectively.";
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
			AuditTrailEntry current;
			AuditTrailEntry next;
			AuditTrailEntry additional;
			String newName = "";

			// walk through the process instance and perform the actual
			// filtering task
			while (index < entries.size()) {
				// get ate at current index position
				current = entries.get(index);

				if (current.getType().equals("complete")) {
					// check if there is a next ate
					if (index + 1 < entries.size()) {
						next = entries.get(index + 1);
						// case 1: "complete-to-start" becomes
						// "start-to-complete"
						if (next.getType().equals("start")) {
							newName = current.getElement() + "-to-"
									+ next.getElement();
							// now replace the previous and this entry
							current.setType("start");
							current.setElement(newName);
							entries.replace(current, index);
							next.setType("complete");
							next.setElement(newName);
							entries.replace(next, index + 1);
							// jump over the two replaced entries
							index = index + 2;
						}
						// case 2: "complete-to-complete" becomes
						// "start-to-complete"
						else if (next.getType().equals("complete")) {
							newName = current.getElement() + "-to-"
									+ next.getElement();
							// now replace the previous and this entry
							current.setType("start");
							current.setElement(newName);
							entries.replace(current, index);
							// check whether next entry is not last entry in log
							if (index + 2 != entries.size()) {
								// normally make additional entry
								additional = (AuditTrailEntry) next.clone();
								additional.setType("complete");
								additional.setElement(newName);
								entries.insert(additional, index + 1);
							} else {
								// but replace last entry
								next.setType("complete");
								next.setElement(newName);
								entries.replace(next, index + 1);
							}
							// jump to the next regular "complete" ATE
							index = index + 2;
						}
					} else {
						index = index + 1;
					}
				} else {
					// if the log is indeed formed by succeeding 'start' and
					// 'end' events,
					// this should only happen at the very beginning and very
					// end of each instance
					entries.remove(index);
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
	 * Returns a Panel for the setting of parameters.
	 * 
	 * @param summary
	 *            A LogSummary to be used for setting parameters.
	 * @return JPanel
	 * @todo Implement this org.processmining.framework.log.LogFilter method
	 */
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary,
				ActivityInversionFilter.this) {

			protected boolean getAllParametersSet() {
				return true;
			}

			protected JPanel getPanel() {
				return null;
			}

			public LogFilter getNewLogFilter() {
				return new ActivityInversionFilter();
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
