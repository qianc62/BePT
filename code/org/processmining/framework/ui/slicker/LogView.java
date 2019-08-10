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
package org.processmining.framework.ui.slicker;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.framework.log.LogReader;

/**
 * @author christian
 * 
 */
public class LogView extends JPanel {

	protected LogReader log;

	public LogView(LogReader log) {
		this.log = log;
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.setOpaque(false);
		JPanel tracePanel = new JPanel();
		tracePanel.setLayout(new BoxLayout(tracePanel, BoxLayout.Y_AXIS));
		tracePanel.setBorder(BorderFactory.createEmptyBorder());
		tracePanel.setOpaque(true);
		tracePanel.setBackground(new Color(30, 30, 30));
		tracePanel.add(Box.createVerticalStrut(10));
		// add all trace replay views
		for (int i = 0; i < log.numberOfInstances(); i++) {
			ProcessInstanceView instanceView = new ProcessInstanceView(log
					.getInstance(i), log.getLogSummary());
			instanceView.setAlignmentX(LEFT_ALIGNMENT);
			JPanel comprisePanel = new JPanel();
			comprisePanel.setAlignmentX(LEFT_ALIGNMENT);
			comprisePanel.setBorder(BorderFactory.createEmptyBorder());
			comprisePanel.setOpaque(false);
			comprisePanel.setLayout(new BoxLayout(comprisePanel,
					BoxLayout.X_AXIS));
			comprisePanel.add(instanceView);
			comprisePanel.add(Box.createHorizontalGlue());
			tracePanel.add(comprisePanel);
		}
		JScrollPane scrollPane = new JScrollPane(tracePanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(40);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(40);
		scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBackground(new Color(30, 30, 30));
		scrollPane.setOpaque(true);
		JScrollBar vBar = scrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(30, 30, 30),
				new Color(140, 140, 140), new Color(80, 80, 80), 4, 12));
		JScrollBar hBar = scrollPane.getHorizontalScrollBar();
		hBar.setUI(new SlickerScrollBarUI(hBar, new Color(30, 30, 30),
				new Color(140, 140, 140), new Color(80, 80, 80), 4, 12));
		this.add(scrollPane, BorderLayout.CENTER);
	}

}
