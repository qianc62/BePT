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

package org.processmining.analysis.epc;

import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.table.AbstractTableModel;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

class SetTableModel extends AbstractTableModel {

	private ArrayList names = new ArrayList();
	private ArrayList eventSets = new ArrayList();
	private String columnName;

	public SetTableModel(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnName(int col) {
		return columnName;
	}

	public int getRowCount() {
		return names.size();
	}

	public int getColumnCount() {
		return 1;
	}

	public Object getValueAt(int row, int column) {
		return names.get(row);
	}

	public void add(HashSet newSet) {
		if (!eventSets.contains(newSet) && (newSet.size() > 0)) {
			this.names.add("Set " + getRowCount());
			this.eventSets.add(newSet);
			fireTableRowsInserted(this.names.size(), this.names.size());
		}
	}

	public void add(HashSet newSet, String id) {
		if (newSet.isEmpty()) {
			return;
		}
		int i = eventSets.indexOf(newSet);
		if ((i > 0) && (getName(i).equals(id))) {
			return;
		}

		this.names.add(id);
		this.eventSets.add(newSet);
		fireTableRowsInserted(this.names.size(), this.names.size());
	}

	public HashSet getSet(int i) {
		return (HashSet) eventSets.get(i);
	}

	public String getName(int i) {
		return (String) names.get(i);
	}

	public void remove(int i) {
		names.remove(i);
		eventSets.remove(i);
		fireTableRowsDeleted(i, i);
	}

}
