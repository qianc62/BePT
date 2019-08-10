package org.processmining.analysis.performance.dottedchart.model;

import javax.swing.table.AbstractTableModel;
import org.processmining.framework.log.LogReader;
import java.util.ArrayList;

/**
 * Data structure for ExtendedLogTable
 * 
 * @author Minseok Song
 * @version 1.0
 */
public class ExtendedLogTable extends AbstractTableModel {

	private LogReader inputLog;
	private ArrayList instanceIDs;

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
		return inputLog.numberOfInstances();
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

	public ExtendedLogTable(LogReader aInputLog, ArrayList aInstanceIDs) {
		inputLog = aInputLog;
		instanceIDs = aInstanceIDs;
	}
}
