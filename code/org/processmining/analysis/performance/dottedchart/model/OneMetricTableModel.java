package org.processmining.analysis.performance.dottedchart.model;

import javax.swing.table.AbstractTableModel;

/**
 * Data structure for tables containing one column with metrics (e.g. throughput
 * time)
 * 
 * @author Minseok Song
 * @version 1.0
 */
public class OneMetricTableModel extends AbstractTableModel {

	private String[] columnNames = { "items", "values" };
	private Object[][] data = { { "time(first)", "" }, { "time(end)", "" },
			{ "avg. interval", "" }, { "min interval", "" },
			{ "max interval", "" } };

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return data.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public boolean isCellEditable(int row, int col) {
		return false;
	}

	/*
	 * Set value at field[row, col] in the table data can change.
	 */
	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}

	public void setHeadings(String one, String two) {
		columnNames[0] = one;
		columnNames[1] = two;
	}
}
