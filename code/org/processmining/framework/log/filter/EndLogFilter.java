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

import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JPanel;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.w3c.dom.Node;

/**
 * This logFilter filters the log based on the final event of a ProcessInstance.
 * 
 * During construction, the modelElementName and EventType of the initial event
 * should be given, together with another filter. Then, ProcessInstances are
 * ignored if their first event does not match the given event.
 * 
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
public class EndLogFilter extends LogFilter {
	private LogEvents endEvents;

	public EndLogFilter() {
		super(LogFilter.FAST, "Final Event Log Filter");
	}

	public EndLogFilter(LogEvents endEvents) {
		super(LogFilter.FAST, "Final Event Log Filter");
		this.endEvents = endEvents;
	}

	protected String getHelpForThisLogFilter() {
		return "Removes a process instance from the Log, if it does not "
				+ "end with the given event.";
	}

	/**
	 * Filters a single process instance.
	 * 
	 * @param instance
	 *            the process instance to filter
	 * @return true if the whole process instance passes the filter, false if
	 *         the process insstance should be discarded
	 * @todo Implement this org.processmining.framework.log.LogFilter method
	 */
	protected boolean doFiltering(ProcessInstance instance) {
		String lastElementInstance = instance.getAuditTrailEntries().last()
				.getElement();
		String lastElementTypeInstance = instance.getAuditTrailEntries().last()
				.getType();

		for (int i = 0; i < endEvents.size(); i++) {
			LogEvent le = endEvents.getEvent(i);
			if (le.getModelElementName().equals(lastElementInstance)
					&& le.getEventType().equals(lastElementTypeInstance)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * The log is not changed
	 * 
	 * @return boolean false
	 */
	public boolean thisFilterChangesLog() {
		return false;
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary, EndLogFilter.this) {

			LogEventCheckBox[] checks;

			public LogFilter getNewLogFilter() {
				LogEvents e = new LogEvents();
				for (int i = 0; i < checks.length; i++) {
					if (checks[i].isSelected()) {
						e.add(checks[i].getLogEvent());
					}
				}
				return new EndLogFilter(e);
			}

			protected JPanel getPanel() {
				int size = summary.getLogEvents().size();
				checks = new LogEventCheckBox[size];
				JPanel p = new JPanel(new GridLayout(size, 1));

				for (int i = 0; i < size; i++) {
					checks[i] = new LogEventCheckBox(summary.getLogEvents()
							.getEvent(i));
					checks[i].setSelected(false);
				}

				Arrays.sort(checks);
				for (int i = 0; i < checks.length; i++) {
					p.add(checks[i]);
					if ((endEvents != null)
							&& (endEvents.contains(checks[i].getLogEvent()))) {
						checks[i].setSelected(true);
					}
				}
				return p;
			}

			protected boolean getAllParametersSet() {
				return true;
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
		Iterator it = endEvents.iterator();
		while (it.hasNext()) {
			LogEvent event = (LogEvent) it.next();
			output.write("<logEvent name=\"" + event.getModelElementName()
					+ "\"" + " type=\"" + event.getEventType() + "\""
					+ " numsim=\"" + event.getOccurrenceCount() + "\"/>\n");
		}
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
		endEvents = new LogEvents();
		for (int i = 0; i < logFilterSpecifcNode.getChildNodes().getLength(); i++) {
			Node n = logFilterSpecifcNode.getChildNodes().item(i);
			if (n.getNodeName().equals("logEvent")) {
				String name = n.getAttributes().getNamedItem("name")
						.getNodeValue();
				String type = n.getAttributes().getNamedItem("type")
						.getNodeValue();
				int occ = Integer.parseInt(n.getAttributes().getNamedItem(
						"numsim").getNodeValue());
				endEvents.add(new LogEvent(name, type, occ));
			}
		}
	}
}
