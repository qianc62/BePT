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
package org.processmining.framework.ui.slicker.logdialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.deckfour.slickerbox.components.GradientPanel;
import org.processmining.framework.log.LogFilter;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class LogFilterListCellRenderer extends GradientPanel implements
		ListCellRenderer {

	protected static Color colorText = new Color(10, 10, 10, 230);
	protected static Color colorTextSelected = new Color(255, 255, 255, 190);
	protected static Color colorFast2 = new Color(0, 110, 0);
	protected static Color colorFast1 = new Color(20, 140, 20);
	protected static Color colorModerate2 = new Color(130, 90, 0);
	protected static Color colorModerate1 = new Color(140, 110, 20);
	protected static Color colorSlow2 = new Color(110, 0, 0);
	protected static Color colorSlow1 = new Color(140, 20, 20);
	protected static Color colorSelected2 = new Color(0, 0, 0);
	protected static Color colorSelected1 = new Color(40, 40, 40);

	protected JLabel nameLabel;
	protected JLabel speedLabel;

	public LogFilterListCellRenderer() {
		super(colorFast1, colorFast2);
		this.setMinimumSize(new Dimension(100, 30));
		this.setMaximumSize(new Dimension(500, 30));
		this.setPreferredSize(new Dimension(200, 30));
		this.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.nameLabel = new JLabel("change_me");
		this.nameLabel.setOpaque(false);
		this.nameLabel.setForeground(colorText);
		this.nameLabel.setVerticalAlignment(JLabel.CENTER);
		this.nameLabel.setFont(this.nameLabel.getFont().deriveFont(13f));
		this.speedLabel = new JLabel("change_me");
		this.speedLabel.setOpaque(false);
		this.speedLabel.setForeground(colorText);
		this.speedLabel.setVerticalAlignment(JLabel.CENTER);
		this.speedLabel.setFont(this.speedLabel.getFont().deriveFont(11f)
				.deriveFont(Font.ITALIC));
		this.add(nameLabel);
		this.add(Box.createHorizontalGlue());
		this.add(speedLabel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
	 * .JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object obj,
			int index, boolean isSelected, boolean hasFocus) {
		LogFilter filter = (LogFilter) obj;
		this.nameLabel.setText(filter.getName());
		if (filter.getComplexity() == LogFilter.FAST) {
			this.setColors(colorFast1, colorFast2);
			this.speedLabel.setText("Fast");
		} else if (filter.getComplexity() == LogFilter.MODERATE) {
			this.setColors(colorModerate1, colorModerate2);
			this.speedLabel.setText("Moderate");
		} else if (filter.getComplexity() == LogFilter.SLOW) {
			this.setColors(colorSlow1, colorSlow2);
			this.speedLabel.setText("Slow");
		} else {
			this.setColors(colorSlow1, colorSlow2);
			this.speedLabel.setText("UNKNOWN");
		}
		if (isSelected == true) {
			this.setColors(colorSelected1, colorSelected2);
			this.speedLabel.setForeground(colorTextSelected);
			this.nameLabel.setForeground(colorTextSelected);
		} else {
			this.speedLabel.setForeground(colorText);
			this.nameLabel.setForeground(colorText);
		}
		return this;
	}

}
