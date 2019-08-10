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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.w3c.dom.Node;

/**
 * This logFilter filters the log based on the originators of audittrailentries.
 * 
 * During construction, a list of originators to keep has to be provided,
 * together with another LogFilter. Then afterwards, using the method
 * doFiltering(), specific originators are filtered.
 * 
 * ProcessInstances are ignored if they turn out to be empty after filtering.
 * 
 * This filter is based on the LogEventLogFilter
 * 
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Mark Hage
 * @version 1.0
 */

public class LogOriginatorLogFilter extends LogFilter {
	private ArrayList<String> originatorsToKeep;

	public LogOriginatorLogFilter() {
		super(LogFilter.MODERATE, "Originator Log Filter");
	}

	public LogOriginatorLogFilter(String[] originatorsToKeep) {
		super(LogFilter.MODERATE, "Originator Log Filter");
		this.originatorsToKeep = new ArrayList<String>();

		for (int i = 0; i < originatorsToKeep.length; i++) {
			this.originatorsToKeep.add(originatorsToKeep[i]);
		}
	}

	public void removeLogOrginator(String originator) {
		originatorsToKeep.remove(originator);
	}

	public void addLogOriginator(String originator) {
		if (!originatorsToKeep.contains(originator)) {
			originatorsToKeep.add(originator);
		}
	}

	protected boolean doFiltering(ProcessInstance instance) {

		AuditTrailEntries entries = instance.getAuditTrailEntries();
		while (entries.hasNext()) {
			AuditTrailEntry entry = entries.next();
			if (!originatorsToKeep.contains(entry.getOriginator())) {
				entries.remove();
			}
		}
		return entries.size() > 0;

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
				+ "to one of the Originators in the selection.";
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary,
				LogOriginatorLogFilter.this) {

			OriginatorCheckBox[] checks;

			public LogFilter getNewLogFilter() {
				ArrayList<String> orAl = new ArrayList<String>();
				for (int i = 0; i < checks.length; i++) {
					if (checks[i].isSelected()) {
						orAl.add(checks[i].getOriginator());
					}
				}
				String[] orig = new String[orAl.size()];
				orAl.toArray(orig);

				return new LogOriginatorLogFilter(orig);
			}

			protected JPanel getPanel() {
				String[] orig = summary.getOriginators();
				int size = orig.length;
				checks = new OriginatorCheckBox[size];
				JPanel p = new JPanel(new GridLayout(size, 1));
				int it = 0;
				int i = 0;
				while (it < size) {
					checks[i++] = new OriginatorCheckBox(orig[it]);
					it++;
				}
				Arrays.sort(checks);
				for (i = 0; i < checks.length; i++) {
					p.add(checks[i]);
					if ((originatorsToKeep != null)
							&& (!originatorsToKeep.contains(checks[i]
									.getOriginator()))) {
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
		Iterator it = originatorsToKeep.iterator();
		while (it.hasNext()) {
			String orig = (String) it.next();
			output.write("<logOriginator name=\"" + orig + "\"/>\n");
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
		originatorsToKeep = new ArrayList<String>();
		for (int i = 0; i < logFilterSpecifcNode.getChildNodes().getLength(); i++) {
			Node n = logFilterSpecifcNode.getChildNodes().item(i);
			if (n.getNodeName().equals("logOriginator")) {
				String name = n.getAttributes().getNamedItem("name")
						.getNodeValue();
				originatorsToKeep.add(name);
			}
		}
	}

	public boolean acceptsAll(Collection originators) {
		return originatorsToKeep.containsAll(originators);
	}
}

class OriginatorCheckBox extends JCheckBox implements Comparable {

	private String lo;

	public OriginatorCheckBox(String lo) {
		super("<html><B>" + lo + "</B></html>", true);
		this.lo = lo;
	}

	public String getOriginator() {
		return lo;
	}

	public int compareTo(Object o) {
		return getText().compareTo(((OriginatorCheckBox) o).getText());
	}
}
