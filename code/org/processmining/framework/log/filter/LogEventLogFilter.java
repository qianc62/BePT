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
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.w3c.dom.Node;

/**
 * This logFilter filters the log based on the modelelements of
 * audittrailentries.
 * 
 * During construction, a list of events to keep has to be provided, together
 * with another LogFilter. Then afterwards, using the method filterEventType(),
 * specific events are filtered.
 * 
 * ProcessInstances are ignored if they turn out to be empty after filtering.
 * 
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class LogEventLogFilter extends LogFilter {
	// 29 March rmans, changed from private to protected so that the inheriting
	// classes (LogEventLogFilterEnh) can access this field
	protected LogEvents eventsToKeep;

	public LogEventLogFilter() {
		super(LogFilter.MODERATE, "Event Log Filter");
	}

	public LogEventLogFilter(LogEvents eventsToKeep) {
		super(LogFilter.MODERATE, "Event Log Filter");
		this.eventsToKeep = eventsToKeep;
	}

	// 29 March rmans, changed so that from the inheriting classes
	// (LogEventLogFilterEnh)
	// can set its own load and name
	public LogEventLogFilter(int load, String name) {
		super(load, name);
	}

	// 29 March rmans, changed so that from the inheriting classes
	// (LogEventLogFilterEnh)
	// can set its own load and name
	public LogEventLogFilter(int load, String name, LogEvents eventsToKeep) {
		super(load, name);
		this.eventsToKeep = eventsToKeep;
	}

	public void removeLogEvent(LogEvent event) {
		eventsToKeep.remove(event);
	}

	public void addLogEvent(LogEvent event) {
		if (!eventsToKeep.contains(event)) {
			eventsToKeep.add(event);
		}
	}

	protected boolean doFiltering(ProcessInstance instance) {

		AuditTrailEntries entries = instance.getAuditTrailEntries();
		while (entries.hasNext()) {
			AuditTrailEntry entry = entries.next();
			LogEvent e = eventsToKeep.findLogEvent(entry.getElement(), entry
					.getType());

			if (e == null) {
				entries.remove();
			}
		}
		return (entries.size() > 0);

	}

	/**
	 * The log is changed, it events are present in the processinstance that are
	 * not in the list given in the constructor.
	 * 
	 * @return boolean true
	 */
	public boolean thisFilterChangesLog() {
		return true;
	}

	protected String getHelpForThisLogFilter() {
		return "Removes all AuditTrailEntries from the log that do not correspond "
				+ "to one of the LogEvents in the selection.";
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary, LogEventLogFilter.this) {

			LogEventCheckBox[] checks;

			public LogFilter getNewLogFilter() {
				LogEvents e = new LogEvents();
				for (int i = 0; i < checks.length; i++) {
					if (checks[i].isSelected()) {
						e.add(checks[i].getLogEvent());
					}
				}
				return new LogEventLogFilter(e);
			}

			protected JPanel getPanel() {
				int size = summary.getLogEvents().size();
				checks = new LogEventCheckBox[size];
				JPanel p = new JPanel(new GridLayout(size, 1));
				Iterator it = summary.getLogEvents().iterator();
				int i = 0;
				while (it.hasNext()) {
					checks[i++] = new LogEventCheckBox((LogEvent) it.next());
				}
				Arrays.sort(checks);
				for (i = 0; i < checks.length; i++) {
					p.add(checks[i]);
					if ((eventsToKeep != null)
							&& (!eventsToKeep.contains(checks[i].getLogEvent()))) {
						checks[i].setSelected(false);
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
		Iterator it = eventsToKeep.iterator();
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
		eventsToKeep = new LogEvents();
		for (int i = 0; i < logFilterSpecifcNode.getChildNodes().getLength(); i++) {
			Node n = logFilterSpecifcNode.getChildNodes().item(i);
			if (n.getNodeName().equals("logEvent")) {
				String name = n.getAttributes().getNamedItem("name")
						.getNodeValue();
				String type = n.getAttributes().getNamedItem("type")
						.getNodeValue();
				int occ = Integer.parseInt(n.getAttributes().getNamedItem(
						"numsim").getNodeValue());
				eventsToKeep.add(new LogEvent(name, type, occ));
			}
		}
	}

	public boolean acceptsAll(Collection events) {
		return eventsToKeep.containsAll(events);
	}
}

class LogEventCheckBox extends JCheckBox implements Comparable {

	private LogEvent le;

	public LogEventCheckBox(LogEvent le) {
		super("<html><B>" + le.getModelElementName() + " (" + le.getEventType()
				+ ")</B></html>", true);
		this.le = le;
	}

	public LogEvent getLogEvent() {
		return le;
	}

	public int compareTo(Object o) {
		return getText().compareTo(((LogEventCheckBox) o).getText());
	}
}
