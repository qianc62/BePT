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

package org.processmining.analysis.ltlchecker;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 * CheckResultModel is a tablemodel to store the checkresults of a check. For
 * the correct and incorrect instances of the check, such a model is needed.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public class CheckResultModel extends AbstractTableModel {

	private String[] columnNames;
	private ArrayList checkResults;

	public CheckResultModel(ArrayList checkResults, String kind) {

		columnNames = new String[1];
		columnNames[0] = kind;
		this.checkResults = checkResults;

	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return checkResults.size();
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		return checkResults.get(row);
	}

	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public boolean isCellEditable(int row, int col) {
		return false;
	}

}
