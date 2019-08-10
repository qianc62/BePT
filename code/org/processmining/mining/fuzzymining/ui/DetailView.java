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
package org.processmining.mining.fuzzymining.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.mining.fuzzymining.replay.DetailNodeAnalysis;
import org.processmining.mining.fuzzymining.replay.FuzzyDetailAnalysis;

/**
 * @author christian
 * 
 */
public class DetailView extends JPanel {

	protected FuzzyDetailAnalysis analysis;
	protected HeaderBar headerBar;

	public DetailView(FuzzyDetailAnalysis analysis) {
		this.analysis = analysis;
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		JPanel eventPanel = new JPanel();
		eventPanel.setLayout(new BoxLayout(eventPanel, BoxLayout.Y_AXIS));
		eventPanel.setBorder(BorderFactory.createEmptyBorder());
		eventPanel.setOpaque(true);
		eventPanel.setBackground(new Color(30, 30, 30));
		eventPanel.add(Box.createVerticalStrut(10));
		// add all detail event views
		List<DetailNodeAnalysis> nodeAnalysis = new ArrayList<DetailNodeAnalysis>(
				analysis.getNodeAnalysis());
		Collections.sort(nodeAnalysis);
		Collections.reverse(nodeAnalysis);
		for (DetailNodeAnalysis node : nodeAnalysis) {
			DetailEventView eventView = new DetailEventView(node);
			eventView.setAlignmentX(LEFT_ALIGNMENT);
			JPanel comprisePanel = new JPanel();
			comprisePanel.setAlignmentX(LEFT_ALIGNMENT);
			comprisePanel.setBorder(BorderFactory.createEmptyBorder());
			comprisePanel.setOpaque(false);
			comprisePanel.setLayout(new BoxLayout(comprisePanel,
					BoxLayout.X_AXIS));
			comprisePanel.add(eventView);
			comprisePanel.add(Box.createHorizontalGlue());
			eventPanel.add(comprisePanel);
		}
		JScrollPane scrollPane = new JScrollPane(eventPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(25);
		scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBackground(new Color(30, 30, 30));
		scrollPane.setOpaque(true);
		JScrollBar vBar = scrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(30, 30, 30),
				new Color(140, 140, 140), new Color(80, 80, 80), 4, 12));
		JScrollBar hBar = scrollPane.getHorizontalScrollBar();
		hBar.setUI(new SlickerScrollBarUI(hBar, new Color(30, 30, 30),
				new Color(140, 140, 140), new Color(80, 80, 80), 4, 12));
		headerBar = new HeaderBar("event class inspector");
		this.add(headerBar, BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);
	}

	public void setCloseActionListener(ActionListener listener) {
		this.headerBar.setCloseActionListener(listener);
	}

}
