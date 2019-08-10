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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.DefaultLogFilterUI;
import org.processmining.framework.ui.Message;
import org.w3c.dom.Node;

/**
 * This logFilter filters the log based on the event types of audittrailentries.
 * The following options are available: 1) INCLUDE: Include all
 * AuditTrailEntries 2) DISCARD: Ignore the AuditTrailEntry, but not the
 * ProcessInstance 3) DISCARD_INSTANCE: Ignore the whole ProcessInstance if a
 * certain LogEvent is found.
 * 
 * During construction, a default behaviour can be given. Then afterwards, using
 * the method filterEventType(), specific events can be filtered.
 * 
 * ProcessInstances are also ignored if their getProcess does not equal the
 * process set by setProcess in this filter.
 * 
 * ProcessInstances are ignored if they turn out to be empty after filtering.
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

public class DefaultLogFilter extends LogFilter {

	public final static int INCLUDE = 0, DISCARD = 1, DISCARD_INSTANCE = 2;

	private int defaultBehavior;
	private String process;
	private Map events;

	public DefaultLogFilter() {
		this(DISCARD);
	}

	public DefaultLogFilter(int defaultBehavior) {
		super(LogFilter.MODERATE, "Default Log Filter");
		this.defaultBehavior = defaultBehavior;
		process = null;
		events = new HashMap();
	}

	public void filterEventType(String type, int action) {
		events.put(type.trim(), new Integer(action));
	}

	public void setProcess(String name) {
		process = name;
	}

	public String getProcess() {
		return process;
	}

	protected boolean doFiltering(ProcessInstance instance) {
		AuditTrailEntryList entries;

		if (defaultBehavior == INCLUDE) {
			return true;
		}

		if (process != null && !process.equals(instance.getProcess())) {
			return false;
		}

		entries = instance.getAuditTrailEntryList();
		for (int i = entries.size() - 1; i >= 0; i--) {
			AuditTrailEntry entry = null;
			try {
				entry = entries.get(i);
				int action = events.containsKey(entry.getType()) ? ((Integer) events
						.get(entry.getType())).intValue()
						: defaultBehavior;

				if (action == DISCARD) {
					entries.remove(i);
				} else if (action == DISCARD_INSTANCE) {
					return false;
				}
			} catch (IOException ex) {
				Message.add("Error while filtering instance: "
						+ ex.getMessage(), Message.ERROR);
			} catch (IndexOutOfBoundsException ex) {
				Message.add("Error while filtering instance: "
						+ ex.getMessage(), Message.ERROR);
			}
		}

		return (entries.size() > 0);
	}

	/**
	 * The log is changes, since events are removed that do not refer to
	 * specific events types.
	 * 
	 * @return boolean true
	 */
	public boolean thisFilterChangesLog() {
		return true;
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary, this) {

			private DefaultLogFilterUI ui;

			public LogFilter getNewLogFilter() {
				return ui.getLogFilter();
			}

			protected JPanel getPanel() {
				ui = new DefaultLogFilterUI(summary, false, process,
						(HashMap) events);
				return ui;
			}

			protected boolean getAllParametersSet() {
				return ui.getLogFilter() != null;
			}

		};
	}

	protected String getHelpForThisLogFilter() {
		return "First, all Process Instances that do not belong to the "
				+ "selected process are removed. <br>"
				+ "Second, it removes AuditTrailEntries from the log that are "
				+ "set to be \"ignored\". Then, if a ProcessInstance constains "
				+ "an AuditTrailEntry with an eventtype that is set to \"discard "
				+ "instance\" the Process instance is removed.";
	}

	/**
	 * Write the inside of the <FilterSpecific> tag in the XML export file to
	 * the OutputStream output.
	 * 
	 * @param output
	 *            OutputStream
	 */
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		output.write("<defaultBehavior>" + defaultBehavior
				+ "</defaultBehavior>\n");
		output.write("<process>" + process + "</process>\n");
		Iterator it = events.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Integer what = (Integer) events.get(key);
			output.write("<rule event=\"" + key + "\">" + what + "</rule>\n");
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
		events = new HashMap();
		for (int i = logFilterSpecifcNode.getChildNodes().getLength() - 1; i >= 0; i--) {
			Node n = logFilterSpecifcNode.getChildNodes().item(i);
			if (n.getNodeName().equals("defaultBehavior")) {
				defaultBehavior = Integer.parseInt(n.getFirstChild()
						.getNodeValue());
			} else if (n.getNodeName().equals("process")) {
				process = n.getFirstChild().getNodeValue();
			} else if (n.getNodeName().equals("rule")) {
				String key = n.getAttributes().getNamedItem("event")
						.getNodeValue();
				int what = Integer.parseInt(n.getFirstChild().getNodeValue());
				events.put(key, new Integer(what));
			}
		}
	}
}
