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

package org.processmining.analysis.performance.sequence;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.DoubleClickTable;

/**
 * This class contains the panel in which the filter options can be set.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class FilterOptions extends JPanel {
	/**
	 * The used log
	 */
	private LogReader log;
	/**
	 * The list of instances in the log that have been selected
	 */
	private int[] selectedInstanceIndices;
	/**
	 * The list of sequences in the sequence diagram
	 */
	private ArrayList sequences;
	/**
	 * The list of patterns in the pattern diagram
	 */
	private ArrayList patterns;

	/**
	 * The timeSort in use
	 */
	private ArrayList instanceIDs = new ArrayList();
	private String timeSort = "seconds";
	private long timeDivider = 1000;
	private PerformanceSequenceAnalysis psa;
	// GUI related components
	private JLabel l0 = new JLabel("<html> <br>Select:</html>");
	private JLabel l1 = new JLabel(
			"<html> <br>Process instances contained in:</html>");
	private JLabel l2 = new JLabel("  to  ");
	private JLabel l3 = new JLabel("  ");
	private JLabel l4 = new JLabel("  ");
	private JLabel l5 = new JLabel("  ");
	private JLabel l6 = new JLabel("");
	private ButtonGroup bg = new ButtonGroup();
	private JRadioButton r0 = new JRadioButton("All process instances");
	private JRadioButton r1 = new JRadioButton(
			"Sequences with throughput time ");
	private JRadioButton r2 = new JRadioButton("All patterns/sequences");
	private JRadioButton r3 = new JRadioButton("Patterns");
	private JRadioButton r4 = new JRadioButton(
			"Patterns with (mean) throughput time ");
	private String[] selectionOptions0 = { "above", "below" };
	private String[] selectionOptions1 = { "above", "below" };
	private JComboBox selectionBox0 = new JComboBox(selectionOptions1);
	private JComboBox selectionBox1 = new JComboBox(selectionOptions0);

	private JButton updateButton = new JButton("Update");
	private JButton invertButton = new JButton("Invert Selection");
	private JButton useButton = new JButton("Use Selected Instances");
	private JButton cancelButton = new JButton("Cancel");
	private JTextField throughputField0 = new JTextField("0.0");
	private JTextField throughputField1 = new JTextField("0.0");
	private JTextField lowBoundField = new JTextField("0");
	private JTextField highBoundField = new JTextField("0");
	private DoubleClickTable processInstanceIDsTable;

	private JScrollPane tableContainer;
	private JPanel westPanel = new JPanel();
	private JPanel centerPanel = new JPanel();
	private JPanel searchPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	/**
	 * Constructor to initialize filter options
	 * 
	 * @param log
	 *            LogReader : the log out of which process instances are
	 *            obtained
	 * @param selectedInstanceIndices
	 *            int[] : the indices of the process instances that are
	 *            currently selected (initially all selected)
	 * @param sequences
	 *            ArrayList : the existing sequences
	 * @param patterns
	 *            ArrayList : the existing patterns
	 * @param timeSort
	 *            String : the time sort used
	 * @param timeDivider
	 *            long : the time divider used
	 * @param patternDrawn
	 *            boolean : if true, pattern options should be available
	 * @param psa
	 *            PerformanceSequenceAnalysis : the object from which this
	 *            constructor was called
	 */
	public FilterOptions(LogReader log, int[] selectedInstanceIndices,
			ArrayList sequences, ArrayList patterns, String timeSort,
			long timeDivider, boolean patternDrawn,
			PerformanceSequenceAnalysis psa) {
		this.log = log;
		this.selectedInstanceIndices = selectedInstanceIndices;
		this.sequences = sequences;
		this.patterns = patterns;
		this.timeSort = timeSort;
		this.timeDivider = timeDivider;
		this.psa = psa;
		try {
			obtainInstanceIDs();
			jbInit(patternDrawn);
			registerGuiListener();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Actually builds the GUI
	 * 
	 * @param patternDrawn
	 *            boolean
	 * @throws Exception
	 */
	private void jbInit(boolean patternDrawn) throws Exception {
		// Initialize the table which contains the log traces
		processInstanceIDsTable = new DoubleClickTable(new ExtendedLogTable(),
				updateButton);
		selectInstances(selectedInstanceIndices);

		tableContainer = new JScrollPane(processInstanceIDsTable);

		// initialize the westPanel (which contains the processInstanceIDsTable)
		westPanel.setPreferredSize(new Dimension(150, 300));
		westPanel.setLayout(new BorderLayout());
		westPanel.add(tableContainer, BorderLayout.CENTER);
		westPanel.add(invertButton, BorderLayout.SOUTH);
		centerPanel.setLayout(new BorderLayout());
		buttonPanel.add(useButton);
		buttonPanel.add(cancelButton);
		centerPanel.add(buttonPanel, BorderLayout.SOUTH);
		bg.add(r0);
		searchPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		searchPanel.add(l0, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		r0.setSelected(true);
		searchPanel.add(r0, gbc);
		if (sequences != null && sequences.size() > 0) {
			bg.add(r1);
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.gridwidth = 4;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			searchPanel.add(l1, gbc);
			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.gridwidth = 4;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			searchPanel.add(r1, gbc);
			gbc.gridx = 5;
			gbc.gridy = 3;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.WEST;
			searchPanel.add(selectionBox0, gbc);
			gbc.gridx = 6;
			gbc.gridy = 3;
			gbc.gridwidth = 1;
			searchPanel.add(l3, gbc);
			gbc.gridx = 7;
			gbc.gridy = 3;
			gbc.gridwidth = 1;
			throughputField0.setMinimumSize(new Dimension(70, 20));
			throughputField0.setPreferredSize(new Dimension(70, 20));
			searchPanel.add(throughputField0, gbc);
			gbc.gridx = 8;
			gbc.gridy = 3;
			gbc.gridwidth = 1;
			l5.setText(" " + timeSort);
			searchPanel.add(l5, gbc);
		}
		if (patternDrawn && patterns != null && patterns.size() != 0) {
			bg.add(r2);
			bg.add(r3);
			bg.add(r4);
			gbc.gridx = 0;
			gbc.gridy = 4;
			gbc.gridwidth = 3;
			searchPanel.add(r2, gbc);
			gbc.gridx = 0;
			gbc.gridy = 5;
			gbc.gridwidth = 1;
			searchPanel.add(r3, gbc);
			gbc.gridx = 1;
			gbc.gridy = 5;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.WEST;
			lowBoundField.setMinimumSize(new Dimension(50, 20));
			lowBoundField.setPreferredSize(new Dimension(50, 20));
			searchPanel.add(lowBoundField, gbc);
			gbc.gridx = 2;
			gbc.gridy = 5;
			gbc.gridwidth = 1;
			searchPanel.add(l2, gbc);
			gbc.gridx = 3;
			gbc.gridy = 5;
			gbc.gridwidth = 1;
			highBoundField.setText(patterns.size() - 1 + "");
			highBoundField.setMinimumSize(new Dimension(50, 20));
			highBoundField.setPreferredSize(new Dimension(50, 20));
			searchPanel.add(highBoundField, gbc);
			gbc.gridx = 0;
			gbc.gridy = 6;
			gbc.gridwidth = 4;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			searchPanel.add(r4, gbc);
			gbc.gridx = 5;
			gbc.gridy = 6;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.WEST;
			searchPanel.add(selectionBox1, gbc);
			gbc.gridx = 6;
			gbc.gridy = 6;
			gbc.gridwidth = 1;
			searchPanel.add(l3, gbc);
			gbc.gridx = 7;
			gbc.gridy = 6;
			gbc.gridwidth = 1;
			throughputField1.setMinimumSize(new Dimension(70, 20));
			throughputField1.setPreferredSize(new Dimension(70, 20));
			searchPanel.add(throughputField1, gbc);
			gbc.gridx = 8;
			gbc.gridy = 6;
			gbc.gridwidth = 1;
			l4.setText(" " + timeSort);
			searchPanel.add(l4, gbc);
		}

		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 1;
		searchPanel.add(updateButton, gbc);
		gbc.gridx = 2;
		gbc.gridy = 7;
		gbc.gridwidth = 4;
		l6.setText("   (" + selectedInstanceIndices.length
				+ " process instances selected)");
		searchPanel.add(l6, gbc);
		searchPanel.setBorder(BorderFactory.createEtchedBorder());
		centerPanel.add(searchPanel, BorderLayout.NORTH);
		this.setLayout(new BorderLayout());
		this.add(westPanel, BorderLayout.WEST);
		this.add(centerPanel, BorderLayout.CENTER);
	}

	/**
	 * Connects GUI-components with listener-methods
	 */
	private void registerGuiListener() {
		useButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				psa.closeFrame(getSelectionStatus());
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				psa.closeFrame(null);
			}
		});
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSelectionStatus();
			}
		});
		invertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				invertSelectionStatus();
			}
		});
		lowBoundField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				r3.setSelected(true);
			}

			public void focusLost(FocusEvent e) {

			}
		});
		highBoundField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				r3.setSelected(true);
			}

			public void focusLost(FocusEvent e) {

			}
		});
		throughputField0.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				r1.setSelected(true);
			}

			public void focusLost(FocusEvent e) {

			}
		});
		throughputField1.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				r4.setSelected(true);
			}

			public void focusLost(FocusEvent e) {

			}
		});
		selectionBox0.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				r1.setSelected(true);
			}

			public void focusLost(FocusEvent e) {

			}
		});
		selectionBox1.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				r4.setSelected(true);
			}

			public void focusLost(FocusEvent e) {

			}
		});
	}

	/**
	 * selects those instances in the process instance table that have an index
	 * that is in the indices list
	 * 
	 * @param indices
	 *            int[]
	 */
	private void selectInstances(int[] indices) {
		l6.setText("   (" + indices.length + " process instances selected)");
		processInstanceIDsTable.getSelectionModel().removeSelectionInterval(0,
				processInstanceIDsTable.getRowCount() - 1);
		HashSet intervals = new HashSet();
		if (indices.length > 0) {
			Arrays.sort(indices);
			int firstOfInterval = indices[0];
			int lastOfInterval = firstOfInterval;
			for (int i = 1; i < indices.length; i++) {
				int index = indices[i];
				if (!(lastOfInterval == index - 1)) {
					int[] interval = new int[2];
					interval[0] = firstOfInterval;
					interval[1] = lastOfInterval;
					intervals.add(interval);
					firstOfInterval = index;
				}
				lastOfInterval = index;
			}
			int[] interval = new int[2];
			interval[0] = firstOfInterval;
			interval[1] = lastOfInterval;
			intervals.add(interval);
		}

		Iterator its = intervals.iterator();
		while (its.hasNext()) {
			int[] interval = (int[]) its.next();
			// select interval
			processInstanceIDsTable.getSelectionModel().addSelectionInterval(
					interval[0], interval[1]);
		}
	}

	/**
	 * Updates the current selection status of the table containing the process
	 * instances. This means that instances that meet the requirements of the
	 * filled in options get selected
	 */
	private void updateSelectionStatus() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (r0.isSelected()) {
			int number = log.getLogSummary().getNumberOfProcessInstances();
			int[] indices = new int[number];
			for (int i = 0; i < number; i++) {
				indices[i] = i;
			}
			selectInstances(indices);
		} else if (r1.isSelected()) {
			HashSet piNames = new HashSet();
			for (int i = 0; i < sequences.size(); i++) {
				Sequence sequence = (Sequence) sequences.get(i);
				double throughputBound = Double.parseDouble(throughputField0
						.getText());
				try {
					if ((selectionBox0.getSelectedIndex() == 0 && sequence
							.getThroughputTime() > throughputBound
							* timeDivider)
							|| (selectionBox0.getSelectedIndex() == 1 && sequence
									.getThroughputTime() < throughputBound
									* timeDivider)) {
						piNames.add(sequence.getPiName());
					}
				} catch (NumberFormatException ex) {
					// do nothing
				}
			}
			// run through log to derive intervals
			int[] indices = new int[piNames.size()];
			int selectedIndex = 0, index = 0;
			log.reset();
			Iterator it = log.instanceIterator();
			// obtain indices of the process instances that are to be selected
			while (it.hasNext()) {
				ProcessInstance pi = (ProcessInstance) it.next();
				if (piNames.contains(pi.getName())) {
					indices[selectedIndex++] = index;
				}
				index++;
			}
			// select instances
			selectInstances(indices);
		} else {
			HashSet piNames = new HashSet();
			for (int i = 0; i < patterns.size(); i++) {
				Pattern pattern = (Pattern) patterns.get(i);
				if (r2.isSelected()) {
					piNames.addAll(pattern.getPiNames());
				} else if (r3.isSelected()) {
					try {
						int lowBound = Integer
								.parseInt(lowBoundField.getText());
						int highBound = Integer.parseInt(highBoundField
								.getText());
						if (lowBound <= pattern.getPatternNumber()
								&& pattern.getPatternNumber() <= highBound) {
							piNames.addAll(pattern.getPiNames());
						}
					} catch (NumberFormatException ex) {
						// do nothing
					}

				} else {
					// r4 selected
					try {
						double throughputBound = Double
								.parseDouble(throughputField1.getText());
						if ((selectionBox1.getSelectedIndex() == 0 && pattern
								.getMeanThroughputTime() > throughputBound
								* timeDivider)
								|| (selectionBox1.getSelectedIndex() == 1 && pattern
										.getMeanThroughputTime() < throughputBound
										* timeDivider)) {
							piNames.addAll(pattern.getPiNames());
						}

					} catch (NumberFormatException ex) {
						// do nothing
					}

				}
			}
			// run through log to derive intervals
			int[] indices = new int[piNames.size()];
			int selectedIndex = 0, index = 0;
			log.reset();
			Iterator it = log.instanceIterator();
			// obtain indices of the process instances that are to be selected
			while (it.hasNext()) {
				ProcessInstance pi = (ProcessInstance) it.next();
				if (piNames.contains(pi.getName())) {
					indices[selectedIndex++] = index;
				}
				index++;
			}
			// select instances
			selectInstances(indices);
		}
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Inverts the current selection status of the table containing the process
	 * instances. This means that instances that have been selected will result
	 * being unselected and the other way round.
	 */
	private void invertSelectionStatus() {
		ListSelectionModel selectionModel = processInstanceIDsTable
				.getSelectionModel();
		// step through the table
		for (int index = 0; index < log.numberOfInstances(); index++) {
			if (selectionModel.isSelectedIndex(index) == true) {
				// if entry is currently selected --> deselect
				selectionModel.removeSelectionInterval(index, index);
			} else {
				// if entry is currently not selected --> select
				selectionModel.addSelectionInterval(index, index);
			}
		}
	}

	/**
	 * Obtains the names of the process instances in the log and stores them in
	 * the instanceIDs list
	 */
	public void obtainInstanceIDs() {
		// note that there is not just the keyset returned to keep
		// the oder of the instances
		instanceIDs.clear();
		log.reset();
		Iterator allTraces = log.instanceIterator();
		while (allTraces.hasNext()) {
			ProcessInstance currentTrace = (ProcessInstance) allTraces.next();
			instanceIDs.add(currentTrace.getName());
		}
	}

	/**
	 * Retrieves the current selection status in the form of the process
	 * instances itselves.
	 * 
	 * @return ArrayList a list of DiagnosticLogTraces
	 */
	/*
	 * 8 private ArrayList getSelectedInstances() { ArrayList selectedInstances
	 * = new ArrayList(); int[] indexArray = getSelectionStatus(); for (int i =
	 * 0; i < indexArray.length; i++) {
	 * selectedInstances.add(log.getInstance(indexArray[i])); } return
	 * selectedInstances; }
	 */
	/**
	 * Retrieves the current selection status based on table indices.
	 * 
	 * @return int[] an array of indices indicating those instances that are
	 *         currently selected
	 */
	private int[] getSelectionStatus() {
		return processInstanceIDsTable.getSelectedRows();
	}

	/**
	 * Private data structure for the table containing the process instance IDs.
	 */
	private class ExtendedLogTable extends AbstractTableModel {

		/**
		 * Specify the headings for the columns.
		 * 
		 * @param col
		 *            The column specified.
		 * @return The heading of the respective column.
		 */
		public String getColumnName(int col) {
			// heading of the first column
			return "Process Instances";
		}

		/**
		 * Specify the number of rows.
		 * 
		 * @return The number of traces in the log.
		 */
		public int getRowCount() {
			return log.numberOfInstances();
		}

		/**
		 * Specifiy the number of columns.
		 * 
		 * @return Always 1.
		 */
		public int getColumnCount() {
			return 1;
		}

		/**
		 * Method to fill a certain field of the table with contents.
		 * 
		 * @param row
		 *            The specified row.
		 * @param column
		 *            The specified column.
		 * @return The content to display at the table field specified.
		 */
		public Object getValueAt(int row, int column) {
			// fill column with trace IDs
			return instanceIDs.get(row);

		}
	}

}
