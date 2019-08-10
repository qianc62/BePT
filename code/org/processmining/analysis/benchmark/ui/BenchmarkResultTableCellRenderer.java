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

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author christian
 * 
 */
public class BenchmarkResultTableCellRenderer extends JLabel implements
		TableCellRenderer {

	public static DecimalFormat format = new DecimalFormat("0.000");

	public BenchmarkResultTableCellRenderer() {
		this.setForeground(Color.BLACK);
		this.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		this.setOpaque(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax
	 * .swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		double measurement = (Double) value;
		if (measurement >= 0.0) {
			this.setForeground(Color.BLACK); // otherwise red will persist once
			// it has been used
			this.setText(format.format(measurement));
			this.setBackground(createBackgroundColor(measurement));
		} else {
			this.setText("invalid");
			this.setForeground(Color.RED);
			// this.setBackground(Color.BLACK);
			this.setBackground(Color.WHITE);
		}
		return this;
	}

	protected Color createBackgroundColor(double value) {
		if (value < 0.0) {
			return Color.BLACK;
		}
		int red, green;
		if (value < 0.5) {
			red = 255;
			green = (int) (510.0 * value);
		} else {
			green = 255;
			red = (int) (510 * (1.0 - value));
		}
		return new Color(red, green, 0);
	}

}
