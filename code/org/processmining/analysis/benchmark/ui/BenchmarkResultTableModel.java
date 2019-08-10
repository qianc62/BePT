/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.analysis.benchmark.ui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.processmining.analysis.benchmark.BenchmarkItem;
import org.processmining.analysis.benchmark.metric.BenchmarkMetric;

/**
 * @author christian
 */
public class BenchmarkResultTableModel extends AbstractTableModel {

	protected List<BenchmarkItem> items;
	protected List<BenchmarkMetric> metrics;

	public BenchmarkResultTableModel(List<BenchmarkItem> benchmarkItems,
			List<BenchmarkMetric> benchmarkMetrics) {
		items = benchmarkItems;
		metrics = benchmarkMetrics;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return items.size() + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return metrics.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int y, int x) {
		if (x == 0 && y < metrics.size()) {
			return metrics.get(y).name();
		} else if (x <= items.size() && y < metrics.size()) {
			String metric = metrics.get(y).name();
			return items.get(x - 1).getMeasurement(metric);
		} else {
			return -1.0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int x) {
		if (x == 0) {
			return String.class;
		} else {
			return Double.class;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int x) {
		if (x == 0) {
			return "Benchmark metric \\ item";
		} else if (x <= metrics.size()) {
			return items.get(x - 1).getName();
		} else {
			return "invalid";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}
}
