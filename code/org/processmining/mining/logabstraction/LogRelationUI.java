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

package org.processmining.mining.logabstraction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.processmining.framework.log.LogSummary;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.framework.log.LogStateMachine;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class LogRelationUI extends JPanel {
	private String[] eventTypes;
	private TimeParTableModel tableContents = new TimeParTableModel();
	private LogSummary summary;

	private ImageIcon timeParallelIcon = new ImageIcon(System
			.getProperty("user.dir")
			+ System.getProperty("file.separator")
			+ "images"
			+ System.getProperty("file.separator") + "emit_parallel_choice.gif");
	private JCheckBox fsmLogFilter = new JCheckBox();
	private JCheckBox usePOInfo = new JCheckBox();
	private JCheckBox showIntermediateResults = new JCheckBox();
	// private JCheckBox heuristicsFilter = new JCheckBox();
	private JPanel mainPanel = new JPanel();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	// private JPanel heuristicsPanel = new JPanel();
	private JPanel fsmPanel = new JPanel();
	private JPanel usePOInfoPanel = new JPanel();
	private JPanel showIntermediateResultsPanel = new JPanel();
	// private JPanel heuristicsSubPanel = new JPanel();
	private JPanel timeParPanel = new JPanel();
	private JTable timePars = new JTable();
	private JScrollPane timeParScrollPane = new JScrollPane();
	private JPanel timeParSubPanel = new JPanel();
	private JLabel iconLabel = new JLabel();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private JButton addTimePar = new JButton();
	private JButton removeTimePar = new JButton();
	private BorderLayout borderLayout2 = new BorderLayout();
	private JCheckBox timeParCheck = new JCheckBox();
	private BorderLayout borderLayout3 = new BorderLayout();
	private FlowLayout flowLayout1 = new FlowLayout();
	private GridBagLayout gridBagLayout3 = new GridBagLayout();

	public LogRelationUI(LogSummary summary) {
		this.summary = summary;
		timePars.setModel(tableContents);
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean getFsmLogFilter() {
		return fsmLogFilter.isSelected();
	}

	public String[][] getIntervals() {
		return timeParCheck.isSelected() ? tableContents.getIntervals()
				: new String[0][0];
	}

	private void jbInit() throws Exception {

		flowLayout1.setAlignment(FlowLayout.LEFT);
		this.setLayout(flowLayout1);

		// ------- Use partial order info as direct follow relation
		// ---------------------------------------
		usePOInfo
				.setText("Use partial order information to derive succession relation.");
		usePOInfo.setEnabled(true);
		usePOInfo.setSelected(true);
		usePOInfoPanel.setLayout(new BorderLayout());
		usePOInfoPanel.add(usePOInfo, BorderLayout.CENTER);

		// ------- FSM log filter ---------------------------------------
		fsmLogFilter
				.setText("Enforce causal dependencies between events of the same activity.");
		fsmLogFilter.setEnabled(true);
		fsmLogFilter.setSelected(true);
		fsmPanel.setLayout(borderLayout3);
		fsmPanel.add(fsmLogFilter, BorderLayout.CENTER);

		// ------- time parallelism filter ------------------------------
		timeParCheck
				.setText("<html>Enforce parallelism relations between all event of activities,<br>"
						+ "if they overlap in time in some instance, taking into account the given<br>"
						+ "start and final events.</html>");

		iconLabel.setIcon(timeParallelIcon);
		addTimePar.setText("Add");
		addTimePar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] eventTypes = summary.getEventTypes();

				if (eventTypes != null) {
					AddEntryDialog d = new AddEntryDialog(MainUI.getInstance(),
							"Choose event types", true);
					CenterOnScreen.center(d);

					if (d.showModal(eventTypes)) {
						tableContents.add(d.getStartEvent(), d.getEndEvent());
					}
				}
			}
		});
		removeTimePar.setText("Remove");
		removeTimePar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (timePars.getSelectedRow() >= 0) {
					tableContents.remove(timePars.getSelectedRow());
				}
			}
		});
		timeParScrollPane.setPreferredSize(new Dimension(100, 100));

		timeParSubPanel.setLayout(gridBagLayout2);
		timeParSubPanel.add(iconLabel, new GridBagConstraints(0, 0, 1, 2, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		timeParSubPanel.add(addTimePar, new GridBagConstraints(1, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));
		timeParSubPanel.add(removeTimePar, new GridBagConstraints(1, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 10, 0, 0), 0, 0));
		timeParSubPanel.add(timeParScrollPane, new GridBagConstraints(0, 2, 2,
				1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(10, 0, 10, 0), 0, 0));
		timeParScrollPane.getViewport().add(timePars, null);

		timeParCheck.setEnabled(true);
		timeParCheck.setSelected(true);
		timeParPanel.setLayout(borderLayout2);
		timeParPanel.add(timeParCheck, BorderLayout.NORTH);
		timeParPanel.add(timeParSubPanel, BorderLayout.CENTER);

		String[] eventTypes = summary.getEventTypes();
		for (String s1 : eventTypes) {
			for (String s2 : eventTypes) {
				if (s1.equals(LogStateMachine.SCHEDULE)
						&& s2.equals(LogStateMachine.COMPLETE)) {
					tableContents.add(s1, s2);
				}
				if (s1.equals(LogStateMachine.START)
						&& s2.equals(LogStateMachine.COMPLETE)) {
					tableContents.add(s1, s2);
				}
				if (s1.equals(LogStateMachine.SCHEDULE)
						&& s2.equals(LogStateMachine.START)) {
					tableContents.add(s1, s2);
				}
				if (s1.equals(LogStateMachine.SUSPEND)
						&& s2.equals(LogStateMachine.RESUME)) {
					tableContents.add(s1, s2);
				}

			}
		}

		// ------- show Intermediate results
		// ---------------------------------------
		showIntermediateResults.setText("Show intermediate results.");
		showIntermediateResults.setEnabled(true);
		showIntermediateResults.setSelected(false);
		showIntermediateResultsPanel.setLayout(new BorderLayout());
		showIntermediateResultsPanel.add(showIntermediateResults,
				BorderLayout.CENTER);

		// ------- main panel ------------------------------
		mainPanel.setLayout(gridBagLayout1);
		mainPanel.add(usePOInfoPanel, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));
		mainPanel.add(fsmPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(
						10, 10, 10, 10), 0, 0));
		mainPanel.add(timeParPanel, new GridBagConstraints(0, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(10, 10, 10, 10), 0, 0));
		mainPanel.add(showIntermediateResultsPanel, new GridBagConstraints(0,
				3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
		this.add(mainPanel, null);
	}

	/**
	 * usePO
	 * 
	 * @return boolean
	 */
	public boolean usePO() {
		return usePOInfo.isSelected();
	}

	public boolean showIntResults() {
		return showIntermediateResults.isSelected();
	}
}

class TimeParTableModel extends AbstractTableModel {

	private ArrayList start = new ArrayList();
	private ArrayList end = new ArrayList();

	public TimeParTableModel() {
	}

	public String getColumnName(int col) {
		return col == 0 ? "Start event" : "End event";
	}

	public int getRowCount() {
		return start.size();
	}

	public int getColumnCount() {
		return 2;
	}

	public Object getValueAt(int row, int column) {
		return column == 0 ? start.get(row) : end.get(row);
	}

	public void add(String start, String end) {
		this.start.add(start);
		this.end.add(end);
		fireTableRowsInserted(this.start.size(), this.start.size());
	}

	public void remove(int i) {
		start.remove(i);
		end.remove(i);
		fireTableRowsDeleted(i, i);
	}

	public String[][] getIntervals() {
		String[][] result = new String[start.size()][];

		for (int i = 0; i < start.size(); i++) {
			result[i] = new String[2];
			result[i][0] = (String) start.get(i);
			result[i][1] = (String) end.get(i);
		}
		return result;
	}
}
