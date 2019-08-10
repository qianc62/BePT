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

package org.processmining.framework.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.processmining.framework.log.InfoItem;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.log.filter.EndLogFilter;
import org.processmining.framework.log.filter.StartLogFilter;
import org.processmining.framework.util.ToolTipComboBox;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class DefaultLogFilterUI extends JPanel {

	final static int WIDTH = 350;

	private String logFilename;
	private ArrayList eventTypeControls = new ArrayList();
	private String[] eventTypes;

	private JPanel eventsPanel = new JPanel();
	private JLabel eventTypesLabel = new JLabel();
	private JPanel infoPanel = new JPanel();
	private GridBagLayout gridBagLayout5 = new GridBagLayout();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private JPanel procsPanel = new JPanel();
	private JScrollPane processesScrollPane = new JScrollPane();
	private JLabel processListLabel1 = new JLabel();
	private JTable processesTable = new JTable();
	private JTable wflogTable = new JTable();
	private JTable sourceTable = new JTable();
	private JLabel jLabel1 = new JLabel();
	private JLabel jLabel2 = new JLabel();
	private JPanel eventTypesPanel = new JPanel();
	private ToolTipComboBox startEvents;
	private ToolTipComboBox endEvents;
	private JPanel startEndPanel = new JPanel(new GridLayout(2, 1));
	BorderLayout borderLayout2 = new BorderLayout();
	GridBagLayout gridBagLayout3 = new GridBagLayout();

	/**
	 * Construct a UI for selecting options for a logfile. The options presented
	 * depend on the given summary.
	 * 
	 * @param summary
	 *            LogSummary
	 */
	public DefaultLogFilterUI(LogSummary summary) {
		this(summary, true, "", null);
	}

	/**
	 * Construct a UI for selecting options for a logfile. The options presented
	 * depend on the given summary.
	 * 
	 * @param summary
	 *            LogSummary
	 * @param showStartEnd
	 *            boolean to tell if the panel with compulsary start and end
	 *            evens should be shown
	 */
	public DefaultLogFilterUI(LogSummary summary, boolean showStartEnd,
			String selectedProcess, HashMap events) {

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		processesTable
				.setModel(new ProcessesTableModel(summary.getProcesses()));
		processesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		wflogTable.setModel(new InfoItemTableModel(summary.getWorkflowLog()));
		sourceTable.setModel(new InfoItemTableModel(summary.getSource()));
		setEventTypes(summary.getEventTypes(), events);

		int h = 200 + 20 * summary.getEventTypes().length + 40;
		int h2 = 0;
		if (showStartEnd) {
			ComboBoxLogEvent[] logEvents;
			logEvents = new ComboBoxLogEvent[summary.getLogEvents().size() + 1];
			logEvents[0] = new ComboBoxLogEvent(new LogEvent(
					ComboBoxLogEvent.NONE, ComboBoxLogEvent.NONE));
			for (int i = 0; i < summary.getLogEvents().size(); i++) {
				logEvents[i + 1] = new ComboBoxLogEvent(summary.getLogEvents()
						.getEvent(i));
			}
			Arrays.sort(logEvents);

			JPanel start = new JPanel(new BorderLayout());
			start.add(new JLabel(
					"<html>Optionally select a compulsory start event</html>"),
					BorderLayout.NORTH);
			startEvents = new ToolTipComboBox(logEvents);
			start.add(new JLabel("  "), BorderLayout.WEST);
			start.add(startEvents, BorderLayout.CENTER);

			JPanel end = new JPanel(new BorderLayout());
			end.add(new JLabel(
					"<html>Optionally select a compulsory final event</html>"),
					BorderLayout.NORTH);
			endEvents = new ToolTipComboBox(logEvents);
			end.add(new JLabel("  "), BorderLayout.WEST);
			end.add(endEvents, BorderLayout.CENTER);

			startEndPanel.add(start);
			startEndPanel.add(end);
			h2 = 80;
			startEndPanel.setBounds(new Rectangle(0, h, WIDTH, h2));
			this.add(startEndPanel, null);
		}
		this.setMinimumSize(new Dimension(120, h + h2));
		this.setPreferredSize(new Dimension(
				(int) getPreferredSize().getWidth(), h + h2));

		if (processesTable.getModel().getRowCount() > 0) {
			processesTable.clearSelection();
			processesTable.setRowSelectionInterval(0, 0);
		}
		selectProcess(selectedProcess);

	}

	/**
	 * Give a logfilter that filters the log using the given options. Note that
	 * if no valid options are set, the returned LogFilter equals null
	 * 
	 * Here, we take into account the compulsory start and end events
	 * 
	 * @return LogFilter
	 */
	public LogFilter getLogFilter() {
		LogFilter filter;

		String process = getSelectedProcess();

		if (process == null) {
			JOptionPane.showMessageDialog(MainUI.getInstance(),
					"Please open a log file and select a process first",
					"Error", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}

		DefaultLogFilter f = new DefaultLogFilter(DefaultLogFilter.DISCARD);
		f.setProcess(process);
		for (int i = 0; i < eventTypeControls.size(); i++) {
			EventTypeControls control = (EventTypeControls) eventTypeControls
					.get(i);
			f.filterEventType(control.getName(), control.getState());
		}
		filter = f;
		// Check for filtering start events
		if (startEvents != null) {
			ComboBoxLogEvent start = (ComboBoxLogEvent) startEvents
					.getSelectedItem();
			if (!start.getLogEvent().getModelElementName().equals(
					ComboBoxLogEvent.NONE)) {
				LogEvents events = new LogEvents();
				events.add(start.getLogEvent());
				StartLogFilter startfilter = new StartLogFilter(events);
				startfilter.setLowLevelFilter(filter);
				filter = startfilter;
			}
		}
		// Check for filtering end events
		if (endEvents != null) {
			ComboBoxLogEvent end = (ComboBoxLogEvent) endEvents
					.getSelectedItem();
			if (!end.getLogEvent().getModelElementName().equals(
					ComboBoxLogEvent.NONE)) {
				LogEvents le = new LogEvents();
				le.add(end.getLogEvent());
				EndLogFilter endfilter = new EndLogFilter(le);
				endfilter.setLowLevelFilter(filter);
				filter = endfilter;
			}
		}
		return filter;
	}

	public String getSelectedProcess() {
		return processesTable.getSelectedRow() < 0 ? null
				: (String) processesTable.getModel().getValueAt(
						processesTable.getSelectedRow(), 0);
	}

	public String getFilename() {
		return logFilename;
	}

	public String[] getEventTypes() {
		return eventTypes;
	}

	private void setEventTypes(String[] types, HashMap events) {
		eventTypes = types;
		int h = 20;

		eventsPanel.setBounds(new Rectangle(0, 200, WIDTH, 0 + h * types.length
				+ 30));

		eventsPanel.setMinimumSize(new Dimension(120, 20 + h * types.length
				+ 30));
		eventsPanel.setPreferredSize(new Dimension(120, 20 + h * types.length
				+ 30));

		eventTypesPanel
				.setMinimumSize(new Dimension(120, h * types.length + 30));
		eventTypesPanel.setPreferredSize(new Dimension(120, h * types.length
				+ 30));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.weighty = 1.0;

		for (int i = 0; i < types.length; i++) {

			JLabel label = new JLabel(types[i]);
			label.setToolTipText(types[i]);
			label.setMinimumSize(new Dimension(100, 50));
			label.setPreferredSize(new Dimension(100, 50));
			JRadioButton ignore = new JRadioButton("ignore");
			JRadioButton include = new JRadioButton("include");
			JRadioButton discardInstance = new JRadioButton("discard instance");
			ButtonGroup group = new ButtonGroup();

			ignore.setFont(ignore.getFont().deriveFont(Font.PLAIN));
			include.setFont(include.getFont().deriveFont(Font.PLAIN));
			discardInstance.setFont(discardInstance.getFont().deriveFont(
					Font.PLAIN));

			group.add(ignore);
			group.add(include);
			group.add(discardInstance);

			if ((events != null) && events.containsKey(types[i])) {
				int choice = ((Integer) events.get(types[i])).intValue();
				if (choice == DefaultLogFilter.INCLUDE) {
					include.setSelected(true);
				} else if (choice == DefaultLogFilter.DISCARD) {
					ignore.setSelected(true);
				} else {
					discardInstance.setSelected(true);
				}

			} else {

				if (types[i].equals("reassign") || types[i].equals("suspend")
						|| types[i].equals("resume")) {
					ignore.setSelected(true);
				} else if (types[i].equals("withdraw")
						|| types[i].equals("ate_abort")
						|| types[i].equals("pi_abort")) {
					discardInstance.setSelected(true);
				} else {
					// schedule, assign, start, complete, autoskip, manualskip,
					// rest
					include.setSelected(true);
				}
			}
			eventTypeControls.add(new EventTypeControls(types[i], ignore,
					include));

			eventTypesPanel.add(label, c);
			c.gridx++;
			eventTypesPanel.add(ignore, c);
			c.gridx++;
			eventTypesPanel.add(include, c);
			c.gridx++;
			eventTypesPanel.add(discardInstance, c);

			c.gridx = 0;
			c.gridy++;
		}

	}

	private void jbInit() throws Exception {
		eventTypesLabel.setText("Event types");
		processListLabel1.setMaximumSize(new Dimension(52, 15));
		processListLabel1.setText("Processes");
		jLabel1.setMaximumSize(new Dimension(200, 15));
		jLabel1.setMinimumSize(new Dimension(200, 15));
		jLabel1.setPreferredSize(new Dimension(200, 15));
		jLabel1.setText("Workflow log information");
		jLabel2.setMaximumSize(new Dimension(120, 20));
		jLabel2.setMinimumSize(new Dimension(120, 15));
		jLabel2.setOpaque(false);
		jLabel2.setPreferredSize(new Dimension(120, 15));
		jLabel2.setText("Source information");

		infoPanel.setLayout(gridBagLayout5);
		this.setPreferredSize(new Dimension(WIDTH, 100));
		this.setRequestFocusEnabled(true);
		// infoPanel.setBackground(Color.gray);
		infoPanel.setMinimumSize(new Dimension(120, 100));
		infoPanel.setPreferredSize(new Dimension(120, 100));
		infoPanel.setRequestFocusEnabled(true);
		infoPanel.setBounds(new Rectangle(0, 0, WIDTH, 100));
		// procsPanel.setBackground(Color.orange);
		procsPanel.setMaximumSize(new Dimension(2147483647, 2147483647));
		procsPanel.setMinimumSize(new Dimension(120, 60));
		procsPanel.setPreferredSize(new Dimension(120, 60));
		procsPanel.setBounds(new Rectangle(0, 100, WIDTH, 100));
		wflogTable.setMinimumSize(new Dimension(120, 20));
		wflogTable.setPreferredSize(new Dimension(120, 20));
		sourceTable.setMaximumSize(new Dimension(120, 20));
		sourceTable.setPreferredSize(new Dimension(120, 20));
		processesScrollPane.setMinimumSize(new Dimension(120, 45));
		// eventsPanel.setBackground(Color.red);
		processesTable.setMinimumSize(new Dimension(120, 45));
		eventTypesPanel.setLayout(gridBagLayout3);
		// eventTypesPanel.setBackground(Color.green);
		infoPanel.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
						5, 0, 0), 0, 0));
		infoPanel.add(wflogTable, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 25, 0, 0), 0, 0));
		infoPanel.add(jLabel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
						5, 0, 0), 0, 0));
		infoPanel.add(sourceTable, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 25, 0, 0), 0, 0));

		processesScrollPane.setBorder(null);
		processesScrollPane.setPreferredSize(new Dimension(120, 45));
		processesScrollPane.getViewport().add(processesTable, null);
		procsPanel.setLayout(gridBagLayout2);
		procsPanel.add(processListLabel1, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 0, 0), 0, 5));
		procsPanel.add(processesScrollPane, new GridBagConstraints(0, 1, 1, 1,
				1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 25, 5, 5), 0, 0));

		eventsPanel.setLayout(borderLayout2);
		eventsPanel.add(eventTypesLabel, BorderLayout.NORTH);
		eventsPanel.add(eventTypesPanel, BorderLayout.CENTER);

		this.setEnabled(true);
		this.add(infoPanel, null);
		this.add(procsPanel, null);
		this.add(eventsPanel, null);
		this.setLayout(null);
	}

	/**
	 * Selects the given process in the process table
	 * 
	 * @param process
	 *            String
	 */
	public void selectProcess(String process) {
		for (int i = 0; i < processesTable.getRowCount(); i++) {
			if (((String) processesTable.getModel().getValueAt(i, 0))
					.equals(process)) {
				processesTable.setRowSelectionInterval(i, i);
				return;
			}
		}
	}
}

class ProcessesTableModel extends AbstractTableModel {

	private InfoItem[] data;
	private boolean hasModelReferences;

	public ProcessesTableModel(InfoItem[] data) {
		hasModelReferences = false;
		for (InfoItem d : data) {
			hasModelReferences = hasModelReferences
					|| (d.getModelReferences() != null && d
							.getModelReferences().size() > 0);
		}
		this.data = data;
	}

	public String getColumnName(int col) {
		return col == 0 ? "Name" : (col == 1 ? "Description"
				: "Model references");
	}

	public int getRowCount() {
		return data.length;
	}

	public int getColumnCount() {
		return hasModelReferences ? 3 : 2;
	}

	public Object getValueAt(int row, int column) {
		return column == 0 ? data[row].getName() : (column == 1 ? data[row]
				.getDescription() : getRefList(data[row]));
	}

	private String getRefList(InfoItem item) {
		StringBuffer result = new StringBuffer();
		if (item.getModelReferences() != null) {
			for (String uri : item.getModelReferences()) {
				result.append(uri);
				result.append(" ");
			}
		}
		return result.toString();
	}
}

class InfoItemTableModel extends AbstractTableModel {

	private ArrayList names = new ArrayList();
	private ArrayList values = new ArrayList();

	public InfoItemTableModel(InfoItem data) {
		Map attributes = data.getData();
		Iterator i = attributes.keySet().iterator();

		if (data.getName() != null && !data.getName().equals("")) {
			names.add("Name");
			values.add(data.getName());
		}
		if (data.getDescription() != null && !data.getDescription().equals("")) {
			names.add("Description");
			values.add(data.getDescription());
		}
		// if (data.getModelReferences() != null) {
		// for (String uri : data.getModelReferences()) {
		// names.add("Model reference");
		// values.add(uri);
		// }
		// }

		while (i.hasNext()) {
			String n = (String) i.next();

			names.add(n);
			values.add(attributes.get(n));
		}
	}

	public String getColumnName(int col) {
		return col == 0 ? "Name" : "Value";
	}

	public int getRowCount() {
		return names.size();
	}

	public int getColumnCount() {
		return 2;
	}

	public Object getValueAt(int row, int column) {
		return column == 0 ? names.get(row) : values.get(row);
	}
}
