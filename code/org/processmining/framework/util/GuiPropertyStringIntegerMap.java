package org.processmining.framework.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

public class GuiPropertyStringIntegerMap implements TableModelListener,
		ListSelectionListener {

	// property attributes
	private String myName;
	private String myDescription;
	private GuiNotificationTarget myTarget;
	private boolean myEditable;
	private int myPreferredHeight;
	private GuiPropertyStringIntegerMap propertyReference = this; // hack: need
	// reference
	// in
	// anonymous
	// class def
	// GUI attributes
	protected static int valCounter = 1;
	private JTable myJTable;
	private JButton myAddButton;
	private JButton myRemoveButton;
	private JPanel resultPanel;
	private JPanel tablePanel;

	private HashMap<String, Integer> myValues;

	/**
	 * Creates a string list property without a discription and notification.
	 * 
	 * @param name
	 *            the name of this property
	 * @param values
	 *            the initial string values of this property
	 */
	public GuiPropertyStringIntegerMap(String name,
			HashMap<String, Integer> values) {
		this(name, null, values);
	}

	/**
	 * Creates a string list property without notification.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param values
	 *            the initial string values of this property
	 */
	public GuiPropertyStringIntegerMap(String name, String description,
			HashMap<String, Integer> values) {
		this(name, description, values, null, 100, true);
	}

	/**
	 * Creates a string list without a discription.
	 * 
	 * @param name
	 *            the name of this property
	 * @param values
	 *            the initial string values of this property
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GuiPropertyStringIntegerMap(String name,
			HashMap<String, Integer> values, GuiNotificationTarget target) {
		this(name, null, values, target, 100, true);
	}

	/**
	 * Creates a string list property.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param values
	 *            the initial string values of this property
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GuiPropertyStringIntegerMap(String name, String description,
			HashMap<String, Integer> values, GuiNotificationTarget target,
			boolean editable) {
		this(name, description, values, target, 100, editable);
	}

	/**
	 * Creates a string list property.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param values
	 *            the initial string values of this property
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 * @param height
	 *            the preferred height for the list property (default value is
	 *            50)
	 * @param editable
	 *            whether the displayed table should be editable or not
	 */
	public GuiPropertyStringIntegerMap(String name, String description,
			HashMap<String, Integer> values, GuiNotificationTarget target,
			int height, boolean editable) {
		myName = name;
		myDescription = description;
		myTarget = target;
		myPreferredHeight = height;
		myValues = values;
		myEditable = editable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejavax.swing.event.TableModelListener#tableChanged(javax.swing.event.
	 * TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e) {
		int row = e.getFirstRow();
		int column = e.getColumn();
		StringIntegerTableModel model = (StringIntegerTableModel) e.getSource();
		if (column == 0) {
			// first remove the old value as the possible value string has
			// changed
			myValues.remove(model.getLastEditedString());
		}
		myValues.put((String) model.getValueAt(row, 0), (Integer) model
				.getValueAt(row, 1));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
	 * .ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		int[] selectedRows = myJTable.getSelectedRows();
		if (myEditable == true) {
			if (selectedRows.length == 0) {
				myRemoveButton.setEnabled(false);
			} else {
				myRemoveButton.setEnabled(true);
			}
		}
	}

	/**
	 * Retrieves all the possible values specified for this property.
	 * 
	 * @return all possible values
	 */
	public HashMap<String, Integer> getAllValues() {
		return myValues;
	}

	/**
	 * Creates GUI panel containg this property, ready to display in some
	 * settings dialog.
	 * 
	 * @return the graphical panel representing this property
	 */
	public JPanel getPropertyPanel() {
		if (resultPanel == null) {
			resultPanel = new JPanel();
			resultPanel.setLayout(new BoxLayout(resultPanel,
					BoxLayout.PAGE_AXIS));

			if (myName != null) {
				JPanel namePanel = new JPanel();
				namePanel.setLayout(new BoxLayout(namePanel,
						BoxLayout.LINE_AXIS));
				JLabel myNameLabel = new JLabel(" " + myName);
				if (myDescription != null) {
					myNameLabel.setToolTipText(myDescription);
				}
				namePanel.add(myNameLabel);
				namePanel.add(Box.createHorizontalGlue());
				resultPanel.add(namePanel);
				resultPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			}
			myJTable = new JTable(new StringIntegerTableModel(myEditable));
			myJTable.getModel().addTableModelListener(this);
			myJTable.getSelectionModel().addListSelectionListener(this);
			// TODO: check whether Java 6 can be assumed here (feature only in
			// Java 6)
			// TODO: check whether second one works with selection and removal
			// (simpler)
			// myJTable.setRowSorter(new TableRowSorter(myJTable.getModel()));
			// myJTable.setAutoCreateRowSorter(true);
			tablePanel = new JPanel();
			tablePanel
					.setLayout(new BoxLayout(tablePanel, BoxLayout.LINE_AXIS));
			JScrollPane listScroller = new JScrollPane(myJTable);
			listScroller
					.setPreferredSize(new Dimension(250, myPreferredHeight));
			tablePanel.add(listScroller);
			resultPanel.add(tablePanel);
			resultPanel.add(Box.createRigidArea(new Dimension(0, 5)));

			if (myEditable == true) {
				myAddButton = new JButton("Add");
				myAddButton.setToolTipText("Adds new value to list");
				myAddButton.addActionListener(new ActionListener() {
					// specify the action to be taken when pressing the "Add"
					// button
					public void actionPerformed(ActionEvent e) {
						myValues.put("New value " + valCounter, new Integer(1));
						valCounter++;
						updateTable();
					}
				});

				myRemoveButton = new JButton("Remove");
				myRemoveButton
						.setToolTipText("Removes selected value from list");
				// is disabled per default since no element is selected per
				// default
				myRemoveButton.setEnabled(false);
				myRemoveButton.addActionListener(new ActionListener() {
					// specify the action to be taken when pressing the "Remove"
					// button
					public void actionPerformed(ActionEvent e) {
						int[] selectedRows = myJTable.getSelectedRows();
						StringIntegerTableModel model = (StringIntegerTableModel) myJTable
								.getModel();
						for (int i = 0; i < selectedRows.length; i++) {
							// TODO: check whether Java 6 can be assumed here
							// (feature only in Java 6)
							// translate selected rows into rows in the model
							// (view may be resorted)
							// selectedRows[i] =
							// myJTable.convertRowIndexToModel(selectedRows[i]);
							String selectedValue = (String) model.getValueAt(
									selectedRows[i], 0);
							myValues.remove(selectedValue);
						}
						myRemoveButton.setEnabled(false);
						updateTable();
					}
				});

				JPanel buttonsPanel = new JPanel();
				buttonsPanel.setLayout(new BoxLayout(buttonsPanel,
						BoxLayout.LINE_AXIS));
				buttonsPanel.add(myAddButton);
				buttonsPanel.add(Box.createHorizontalGlue());
				buttonsPanel.add(myRemoveButton);
				resultPanel.add(buttonsPanel);
			}
		}
		return resultPanel;
	}

	/*
	 * Recreates the table based on its underlying map.
	 */
	private void updateTable() {
		myJTable = new JTable(new StringIntegerTableModel(myEditable));
		myJTable.getModel().addTableModelListener(propertyReference);
		tablePanel.removeAll();
		JScrollPane listScroller = new JScrollPane(myJTable);
		listScroller.setPreferredSize(new Dimension(250, myPreferredHeight));
		tablePanel.add(listScroller);
		tablePanel.validate();
		tablePanel.repaint();
		resultPanel.validate();
		resultPanel.repaint();
		if (myTarget != null) {
			// notify owner of this property if specified
			myTarget.updateGUI();
		}
	}

	private class StringIntegerTableModel extends AbstractTableModel {

		private String[] columnNames = { "Value", "Frequency" };
		private Object[][] data;
		private String lastEditedString = "";
		private boolean editable;

		public StringIntegerTableModel(boolean toBeMadeEditable) {
			int i = 0;
			editable = toBeMadeEditable;
			data = new Object[myValues.size()][2];
			for (Entry<String, Integer> val : myValues.entrySet()) {
				data[i][0] = val.getKey();
				data[i][1] = val.getValue();
				i++;
			}
		}

		/**
		 * Returns the old value of the "Nominal Value" cell that was edited the
		 * last time.
		 * 
		 * @return the last edited "Nominal Value" before its editing
		 */
		public String getLastEditedString() {
			return lastEditedString;
		}

		public boolean isCellEditable(int row, int col) {
			if (editable == true) {
				return true;
			} else {
				return false;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		public String getColumnName(int col) {
			return columnNames[col].toString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return columnNames.length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount() {
			return data.length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
		 * int, int)
		 */
		public void setValueAt(Object value, int row, int col) {
			if (value != null) {
				if (col == 0) {
					// first save old data value (needs to be replaced in
					// listener)
					lastEditedString = (String) data[row][0];
					for (int i = 0; i < data.length; i++) {
						// only allow update if new string is not yet in the set
						// of possible values
						if (data[i][0].equals(value)) {
							return;
						}
					}
				}
				data[row][col] = value;
				fireTableCellUpdated(row, col);
			}
		}

	}

}
