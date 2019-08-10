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

import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.rfb.AuditTrailEntryImpl;
import org.processmining.framework.util.ToolTipComboBox;
import org.w3c.dom.Node;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title: AddArtificialEndTaskLogFilter
 * </p>
 * 
 * <p>
 * Description: This class adds an artificial task to the <i>end</i> of every
 * process instance in the log.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class AddArtificialEndTaskLogFilter extends LogFilter {

	protected static Date FUTURE_DATE = (new GregorianCalendar(2500, 1, 1))
			.getTime();
	private String eventName;
	private String eventType;

	public AddArtificialEndTaskLogFilter() {
		super(LogFilter.FAST, "Add Artificial End Task Log Filter");
	}

	public AddArtificialEndTaskLogFilter(String name, String eventType) {
		super(LogFilter.FAST, "Add Artificial End Task Log Filter");
		this.eventName = name;
		this.eventType = eventType;
	}

	protected String getHelpForThisLogFilter() {
		return "Adds a task "
				+ "to the <i>end</i> of every process instance in the log.";
	}

	/**
	 * Filters a single process instance.
	 * 
	 * @param instance
	 *            the process instance to filter
	 * @return true if the whole process instance passes the filter, false if
	 *         the process insstance should be discarded
	 */
	protected boolean doFiltering(ProcessInstance instance) {

		// creating the artificial end task ate
		AuditTrailEntry ate = new AuditTrailEntryImpl();
		ate.setElement(eventName);
		ate.setType(eventType);
		// ate.setTimestamp(new Date(System.currentTimeMillis() + 100000)); //
		// future
		ate.setOriginator("Artificial (ProM)");
		// adding the new end ate at the last position
		try {
			instance.getAuditTrailEntryList().append(ate);
		} catch (IOException e) {
			Message.add("Fatal error in class " + this.getClass() + ":",
					Message.ERROR);
			return false;
		}
		return true;
	}

	/**
	 * The log is changed.
	 * 
	 * @return boolean true
	 */
	public boolean thisFilterChangesLog() {
		return true;
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary,
				AddArtificialEndTaskLogFilter.this) {

			JLabel taskNameLabel;
			JTextField taskName;
			JLabel taskEventTypeLabel;
			ToolTipComboBox eventTypes;

			public LogFilter getNewLogFilter() {
				return new AddArtificialEndTaskLogFilter(taskName.getText(),
						LogStateMachine.EVENT_TYPES[eventTypes
								.getSelectedIndex()]);
			}

			protected JPanel getPanel() {
				taskNameLabel = new JLabel("Name");
				taskName = new JTextField(10);
				taskEventTypeLabel = new JLabel("Event Type");

				eventTypes = new ToolTipComboBox(LogStateMachine.EVENT_TYPES);

				if (eventName == null) {
					taskName.setText("ArtificialEndTask");
				} else {
					taskName.setText(eventName);
				}

				if (eventType == null) {
					eventTypes.setSelectedIndex(LogStateMachine.ORD_COMPLETE);
				} else {
					for (int i = 0; i < LogStateMachine.EVENT_TYPES.length; i++) {
						if (eventType.equals(LogStateMachine.EVENT_TYPES[i])) {
							eventTypes.setSelectedIndex(i);
							break;
						}
					}
				}

				JPanel end = new JPanel(new BorderLayout());
				end
						.add(
								new JLabel(
										"<html>Provide the name and event type of the artificial end task:</html>"),
								BorderLayout.NORTH);
				end.add(new JLabel("  "), BorderLayout.WEST);
				JPanel p = new JPanel();
				p.add(taskNameLabel);
				p.add(taskName);
				p.add(taskEventTypeLabel);
				p.add(eventTypes);
				end.add(p, BorderLayout.CENTER);
				return end;
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
		output.write("<eventName>" + eventName + "</eventName>\n");
		output.write("<eventType>" + eventType + "</eventType>\n");
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
			if (n.getNodeName().equals("eventName")) {
				eventName = n.getFirstChild().getNodeValue();
			} else if (n.getNodeName().equals("eventType")) {
				eventType = n.getFirstChild().getNodeValue();
			}
		}
	}

}
