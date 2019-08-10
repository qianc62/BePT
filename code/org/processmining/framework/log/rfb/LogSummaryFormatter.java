/**
 * Project: ProM
 * File: LogSummaryFormatter.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Aug 24, 2006, 4:51:25 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the
 *      names of its contributors may be used to endorse or promote
 *      products derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.framework.log.rfb;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.processmining.framework.log.InfoItem;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogSummary;

/**
 * This class provides static methods for extracting an HTML-formatted summary
 * from the information contained in a LogSummary instance.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class LogSummaryFormatter {

	// used for percentage formatting
	protected static final NumberFormat numberFormat = NumberFormat
			.getInstance();
	{
		numberFormat.setMinimumFractionDigits(1);
		numberFormat.setMaximumFractionDigits(3);
	}

	// HTML encodings
	protected static final String FONT_TEXT = "<font face=\"helvetica,arial,sans-serif\" size=\"4\">";
	protected static final String FONT_CODE = "<font face=\"andale mono,courier\" color=\"#222222\" size=\"3.2\">";
	protected static final String FONT_HEADING = "<font face=\"arial,helvetica,sans-serif\" size=\"+2\" color=\"#111111\">";
	protected static final String TABLE = "<table bgcolor=\"#CCCCDD\" border=\"0\" width=\"95%\" cellpadding=\"4\" cellspacing=\"2\">";
	protected static final String TD = "<td bgcolor=\"#CCCCCC\" valign=\"top\" align=\"left\">";
	protected static final String TR = "<tr>";
	protected static final String PART_START = "<tr><td width=\"100%\" align=\"left\" bgcolor=\"#AAAAAA\">";
	protected static final String PART_STOP = "</td></tr>";

	/**
	 * Formats information contained in a LogSummary in HTML string format.
	 * 
	 * @param summary
	 *            the LogSummary to extract information from
	 * @return HTML-formatted description string
	 */
	public static String format(LogSummary summary) {
		return format(summary, true, true, true, true, true, true, true);
	}

	/**
	 * Formats information contained in a LogSummary in HTML string format.
	 * 
	 * @param summary
	 *            the LogSummary to extract information from
	 * @param includeHeader
	 *            include general information about the log
	 * @param includeSource
	 *            include information about the source system
	 * @param includeInstances
	 *            include information about process instances
	 * @param includeLogEvents
	 *            include information about all log events
	 * @param includeStartEvents
	 *            include information about starting log events
	 * @param includeEndEvents
	 *            include information about ending log events
	 * @param includeOriginators
	 *            include information about originators
	 * @return HTML-formatted description string
	 */
	public static String format(LogSummary summary, boolean includeHeader,
			boolean includeSource, boolean includeInstances,
			boolean includeLogEvents, boolean includeStartEvents,
			boolean includeEndEvents, boolean includeOriginators) {
		StringBuffer sumString = new StringBuffer();
		writeDocumentHeader(sumString);
		if (includeHeader == true) {
			sumString.append(PART_START);
			heading(sumString, "Log Summary");
			sumString.append(FONT_TEXT + "Number of processes: <b>"
					+ summary.getProcesses().length + "</b></font><br>");
			sumString
					.append(FONT_TEXT
							+ "Total number of process instances: <b>"
							+ summary.getNumberOfProcessInstances()
							+ "</b></font><br>");
			sumString.append(FONT_TEXT
					+ "Total number of audit trail entries: <b>"
					+ summary.getNumberOfAuditTrailEntries()
					+ "</b></font><br>");
			encodeInfoItem(sumString, summary.getWorkflowLog());
			sumString.append(PART_STOP);
		}
		if (includeSource == true) {
			sumString.append(PART_START);
			heading(sumString, "Source");
			encodeInfoItem(sumString, summary.getSource());
			sumString.append(PART_STOP);
		}
		if (includeInstances == true) {
			sumString.append(PART_START);
			heading(sumString, "Process Instances");
			encodeProcessInstances(sumString, summary);
			sumString.append(PART_STOP);
		}
		if (includeLogEvents == true) {
			sumString.append(PART_START);
			heading(sumString, "Log events");
			encodeLogEvents(sumString, summary);
			sumString.append(PART_STOP);
		}
		if (includeStartEvents == true) {
			sumString.append(PART_START);
			heading(sumString, "Starting Log Events");
			encodeStartingEndingLogEvents(sumString, summary
					.getStartingLogEvents(), summary
					.getNumberOfProcessInstances());
			sumString.append(PART_STOP);
		}
		if (includeEndEvents == true) {
			sumString.append(PART_START);
			heading(sumString, "Ending Log Events");
			encodeStartingEndingLogEvents(sumString, summary
					.getEndingLogEvents(), summary
					.getNumberOfProcessInstances());
			sumString.append(PART_STOP);
		}
		if (includeOriginators == true) {
			sumString.append(PART_START);
			heading(sumString, "Originators");
			encodeOriginators(sumString, summary);
			sumString.append(PART_STOP);
		}
		writeDocumentFooter(sumString);
		return sumString.toString();
	}

	protected static void writeDocumentHeader(StringBuffer buffer) {
		buffer.append("<html><body bgcolor=\"#888888\" text=\"#111111\">");
		buffer
				.append("<center><br><table width=\"95%\" border=\"0\" cellspacing=\"10\" cellpadding=\"10\">");
	}

	protected static void writeDocumentFooter(StringBuffer buffer) {
		buffer.append("</table><br><br></body></html>");
	}

	/**
	 * Formats a heading; internal helper method.
	 * 
	 * @param heading
	 *            headline to be formatted
	 * @return HTML string
	 */
	protected static void heading(StringBuffer buffer, String heading) {
		buffer.append(FONT_HEADING);
		buffer.append(heading);
		buffer.append("</font><hr noshade width=\"100%\" size=\"1\"><br>");
	}

	/**
	 * Formats an info item; internal helper method.
	 * 
	 * @param item
	 *            info item to be formatted
	 * @return HTML string
	 */
	protected static void encodeInfoItem(StringBuffer buffer, InfoItem item) {
		buffer.append(FONT_TEXT);
		buffer.append("Name:</font> ");
		buffer.append(FONT_CODE);
		buffer.append(item.getName());
		buffer.append("</font><br>");
		buffer.append(FONT_TEXT);
		buffer.append("Description:</font> ");
		buffer.append(FONT_CODE);
		buffer.append(item.getDescription());
		buffer.append("</font><br><br>");
		buffer.append("<center>" + TABLE + TR);
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Attribute name</b></font></td>");
		buffer.append(TD + FONT_TEXT);
		buffer.append("<b>Value</b></font></td></tr>");
		Map<String, String> atts = item.getData();
		for (String key : atts.keySet()) {
			buffer.append(TR);
			buffer.append(TD);
			buffer.append(FONT_CODE);
			buffer.append(key);
			buffer.append("</font></td>");
			buffer.append(TD);
			buffer.append(FONT_CODE);
			buffer.append(atts.get(key));
			buffer.append("</font></td></tr>");
		}
		buffer.append("</center></table><br>");
	}

	/**
	 * Formats a set of log events, including their sorting by occurrence count
	 * and calculation of occurrence percentages (based on the total number of
	 * audit trail entries in a log file).; internal helper method.
	 * 
	 * @param summary
	 *            The log summary from which the set of log events should be
	 *            formatted
	 * @return HTML string
	 */
	protected static void encodeLogEvents(StringBuffer buffer,
			LogSummary summary) {
		// sort events by occurrence count
		ArrayList<LogEvent> sortedEvents = new ArrayList<LogEvent>();
		for (LogEvent event : summary.getLogEvents()) {
			boolean inserted = false;
			LogEvent current = null;
			for (int i = 0; i < sortedEvents.size(); i++) {
				current = sortedEvents.get(i);
				if (current.getOccurrenceCount() < event.getOccurrenceCount()) {
					// insert at correct position and set marker
					sortedEvents.add(i, event);
					inserted = true;
					break;
				}
			}
			if (inserted == false) {
				// append to end of list
				sortedEvents.add(event);
			}
		}
		// format output
		buffer.append(FONT_TEXT);
		buffer.append("Number of audit trail entries: <b>");
		buffer.append(sortedEvents.size());
		buffer.append("</b></font><br><br>");
		buffer.append("<center>");
		buffer.append(TABLE);
		buffer.append(TR);
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Model element</b></font></td>");
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Event type</b></font></td>");
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Occurrences (absolute)</b></font></td>");
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Occurrences (relative)</b></font></td></tr>");
		for (LogEvent event : sortedEvents) {
			buffer.append(TR);
			buffer.append(TD);
			buffer.append(FONT_CODE);
			buffer.append(event.getModelElementName());
			buffer.append("</font></td>");
			buffer.append(TD);
			buffer.append(FONT_CODE);
			buffer.append(event.getEventType() + "</font></td>");
			buffer.append("</font></td>");
			buffer.append(TD);
			buffer.append(FONT_CODE);
			buffer.append(event.getOccurrenceCount() + "</font></td>");
			buffer.append("</font></td>");
			buffer.append(TD);
			buffer.append(FONT_CODE);
			buffer.append(getOccurrencePercentage(event.getOccurrenceCount(),
					summary.getNumberOfAuditTrailEntries()));
			buffer.append("</font></td></tr>");
		}
		buffer.append("</center></table><br>");
	}

	/**
	 * Formats a set of originators, including their sorting by occurrence count
	 * and calculation of occurrence percentages (based on the total number of
	 * audit trail entries in a log file).; internal helper method.
	 * 
	 * @param summary
	 *            The log summary from which the set of log events should be
	 *            formatted
	 * @return HTML string
	 */
	protected static void encodeOriginators(StringBuffer buffer,
			LogSummary summary) {

		// creating mapping for originators occurrences
		HashMap<Integer, TreeSet> originatorOccurrences = new HashMap<Integer, TreeSet>();

		for (String originator : summary.getOriginators()) {

			int occurrence = 0;

			Map<LogEvent, Integer> eventsAndOccurrencesForOriginator = summary
					.getEventsForOriginator(originator);

			for (LogEvent eventForOriginator : eventsAndOccurrencesForOriginator
					.keySet()) {
				occurrence += eventsAndOccurrencesForOriginator
						.get(eventForOriginator);
			}
			if (occurrence > 0) {
				if (originatorOccurrences.containsKey(occurrence)) {
					TreeSet ts = originatorOccurrences.get(occurrence);
					ts.add(originator);

				} else {
					TreeSet ts = new TreeSet();
					ts.add(originator);
					originatorOccurrences.put(occurrence, ts);
				}
			}

		}

		// sort originators by occurrence count
		TreeSet<Integer> occurrences = new TreeSet<Integer>(
				originatorOccurrences.keySet());
		Integer[] occurrencesArray = new Integer[occurrences.size()];
		occurrencesArray = occurrences.toArray(occurrencesArray);

		// format output
		buffer.append(FONT_TEXT);
		buffer.append("Number of originators: <b>");
		buffer.append(summary.getOriginators().length);
		buffer.append("</b></font><br><br>");
		buffer.append("<center>");
		buffer.append(TABLE);
		buffer.append(TR);
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Originator</b></font></td>");
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Occurrences (absolute)</b></font></td>");
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Occurrences (relative)</b></font></td></tr>");
		for (int i = occurrencesArray.length - 1; i >= 0; i--) {
			TreeSet<String> ts = originatorOccurrences.get(occurrencesArray[i]);
			for (String originator : ts) {
				buffer.append(TR);
				buffer.append(TD);
				buffer.append(FONT_CODE);
				buffer.append(originator);
				buffer.append("</font></td>");
				buffer.append(TD);
				buffer.append(FONT_CODE);
				buffer.append(occurrencesArray[i]);
				buffer.append("</font></td>");
				buffer.append(TD);
				buffer.append(FONT_CODE);
				buffer.append(getOccurrencePercentage(occurrencesArray[i],
						summary.getNumberOfAuditTrailEntries()));
				buffer.append("</font></td></tr>");
			}
		}
		buffer.append("</center></table><br>");
	}

	/**
	 * Formats a set of starting/ending log events, including their sorting by
	 * occurrence count and calculation of occurrence percentages (based on the
	 * total number of audit trail entries in a log file).; internal helper
	 * method.
	 * 
	 * @param mappingStartingEndingLogEventsToFrequencies
	 *            HashMap containing the map between the starting/ending events
	 *            and their occurrences as starting/ending events in the process
	 *            instances in the log
	 * @param totalNumberProcessInstances
	 *            int containing the total number of process instances in the
	 *            log
	 * 
	 * @return HTML string
	 */

	protected static void encodeStartingEndingLogEvents(StringBuffer buffer,
			Map<LogEvent, Integer> mappingStartingEndingLogEventsToFrequencies,
			int totalNumberProcessInstances) {
		// sort events by occurrence count
		ArrayList<LogEvent> sortedEvents = new ArrayList<LogEvent>();
		for (LogEvent event : mappingStartingEndingLogEventsToFrequencies
				.keySet()) {
			boolean inserted = false;
			LogEvent current = null;
			for (int i = 0; i < sortedEvents.size(); i++) {
				current = sortedEvents.get(i);
				if (mappingStartingEndingLogEventsToFrequencies.get(current) < mappingStartingEndingLogEventsToFrequencies
						.get(event)) {
					// insert at correct position and set marker
					sortedEvents.add(i, event);
					inserted = true;
					break;
				}
			}
			if (inserted == false) {
				// append to end of list
				sortedEvents.add(event);
			}
		}
		// format output
		buffer.append(FONT_TEXT);
		buffer.append("Number of audit trail entries: <b>");
		buffer.append(sortedEvents.size());
		buffer.append("</b></font><br><br>");

		buffer.append("<center>");
		buffer.append(TABLE);
		buffer.append(TR);
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Model element</b></font></td>");
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Event type</b></font></td>");
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Occurrences (absolute)</b></font></td>");
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Occurrences (relative)</b></font></td></tr>");
		for (LogEvent event : sortedEvents) {
			buffer.append(TR);
			buffer.append(TD);
			buffer.append(FONT_CODE);
			buffer.append(event.getModelElementName());
			buffer.append("</font></td>");
			buffer.append(TD);
			buffer.append(FONT_CODE);
			buffer.append(event.getEventType());
			buffer.append("</font></td>");
			buffer.append(TD);
			buffer.append(FONT_CODE);
			buffer.append(mappingStartingEndingLogEventsToFrequencies
					.get(event));
			buffer.append("</font></td>");
			buffer.append(TD);
			buffer.append(FONT_CODE);
			buffer.append(getOccurrencePercentage(
					mappingStartingEndingLogEventsToFrequencies.get(event),
					totalNumberProcessInstances));
			buffer.append("</font></td></tr>");
		}
		buffer.append("</center></table><br>");
	}

	/**
	 * Formats a set of process instances, including their sorting by occurrence
	 * count and calculation of occurrence percentages (based on the total
	 * number of process instances in a log file).; internal helper method.
	 * 
	 * @param summary
	 *            The log summary from which the set of process intances should
	 *            be formatted
	 * @return HTML string
	 */
	protected static void encodeProcessInstances(StringBuffer buffer,
			LogSummary summary) {

		// sort process instances by occurrence count
		TreeSet<Integer> occurrences = new TreeSet<Integer>(summary
				.getProcessInstancesOccurrences().keySet());
		Integer[] occurrencesArray = new Integer[occurrences.size()];
		occurrencesArray = occurrences.toArray(occurrencesArray);

		// format output

		buffer.append(FONT_TEXT);
		buffer.append("Number of process instances entries: <b>");
		buffer.append(summary.getNumberOfUniqueProcessInstances());
		buffer.append("</b></font><br><br>");

		buffer.append("<center>");
		buffer.append(TABLE);
		buffer.append(TR);
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Process Instance</b></font></td>");
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Occurrences (absolute)</b></font></td>");
		buffer.append(TD);
		buffer.append(FONT_TEXT);
		buffer.append("<b>Occurrences (relative)</b></font></td></tr>");
		for (int i = occurrencesArray.length - 1; i >= 0; i--) {
			buffer.append(TR);
			buffer.append(TD);
			buffer.append(FONT_CODE);
			buffer.append(summary.getProcessInstancesOccurrences().get(
					occurrencesArray[i]).toString().substring(
					1,
					summary.getProcessInstancesOccurrences().get(
							occurrencesArray[i]).toString().length() - 1));
			buffer.append("</font></td>");
			buffer.append(TD);
			buffer.append(FONT_CODE);
			buffer.append(occurrencesArray[i]);
			buffer.append("</font></td>");
			buffer.append(TD);
			buffer.append(FONT_CODE);
			buffer.append(getOccurrencePercentage(occurrencesArray[i], summary
					.getNumberOfProcessInstances()));
			buffer.append("</font></td></tr>");
		}
		buffer.append("</center></table><br>");
	}

	/**
	 * Extracts the percentage of an occurrence and formats it as string
	 * 
	 * @param occurrence
	 *            number to calculate percentage occurrence from
	 * @param total
	 *            total number of occurrences in the log
	 * @return string containing the required percentage
	 */
	protected static String getOccurrencePercentage(double occurrence,
			double total) {
		double percentage = 100.0 * (double) occurrence / (double) total;
		return numberFormat.format(percentage) + "%";
	}

}
