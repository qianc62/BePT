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

import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.AdvancedLogFilterSettingsComponent;
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
public class AbstractLogFilter extends LogFilter {

	private LogFilter lowLevelFilter;
	private boolean changes;
	private String help;

	/**
	 * This constructor should only be called by LogFilter!!
	 */
	public AbstractLogFilter() {
		super(LogFilter.SLOW, "");
		// This constructor should only be called by LogFilter!!
	}

	public AbstractLogFilter(String name, LogFilter filter) {
		super(filter.getComplexity(), name);
		this.lowLevelFilter = filter;
		this.changes = filter.changesLog();
		this.help = filter.getHelp();
	}

	protected boolean doFiltering(ProcessInstance instance) {
		// Filtering is let to the lower level filter
		return lowLevelFilter.filter(instance);
	}

	protected String getHelpForThisLogFilter() {
		return help;
	}

	public boolean thisFilterChangesLog() {
		return changes;
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary, AbstractLogFilter.this) {

			public LogFilter getNewLogFilter() {
				return new AbstractLogFilter(getName(), lowLevelFilter);
			}

			protected JPanel getPanel() {
				AdvancedLogFilterSettingsComponent panel = new AdvancedLogFilterSettingsComponent(
						summary);
				LogFilter f = lowLevelFilter;
				while (f != null) {
					panel.addLogFilterToBegin(f, f.getName());
					f = f.getFilter();
				}
				return panel;
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
		lowLevelFilter.writeXML(output);
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
		for (int i = logFilterSpecifcNode.getChildNodes().getLength() - 1; i >= 0; i--) {
			Node n = logFilterSpecifcNode.getChildNodes().item(i);
			if (n.getNodeName().equals("LogFilter")) {
				try {
					lowLevelFilter = LogFilter.readXML(n);
					changes = lowLevelFilter.changesLog();
					help = lowLevelFilter.getHelp();
					load = lowLevelFilter.getComplexity();
				} catch (Exception ex) {
					Message.add("Problem when loading LogFilter: "
							+ ex.toString(), Message.ERROR);
				}

			}
		}

	}
}
