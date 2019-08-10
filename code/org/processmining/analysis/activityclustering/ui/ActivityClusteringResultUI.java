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
package org.processmining.analysis.activityclustering.ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.processmining.analysis.activityclustering.ClusterTypeSet;
import org.processmining.analysis.activityclustering.model.ClusterType;
import org.processmining.framework.log.LogReader;

/**
 * @author christian
 * 
 */
public class ActivityClusteringResultUI extends JPanel {

	protected LogReader log;
	protected ClusterTypeSet clusters;
	protected JList clusterList;
	protected JList footprintList;
	protected Color colorListBg = new Color(140, 140, 140);
	protected Color colorListFg = new Color(40, 40, 40);
	protected Color colorListSelectedBg = new Color(20, 20, 60);
	protected Color colorListSelectedFg = new Color(220, 40, 40);

	public ActivityClusteringResultUI(LogReader log, ClusterTypeSet clusters) {
		this.clusters = clusters;
		this.log = log;
		setupGui();
	}

	protected void setupGui() {
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(90, 90, 90));
		JPanel browserPanel = new JPanel();
		browserPanel.setOpaque(true);
		browserPanel.setLayout(new BorderLayout());
		browserPanel.setBackground(new Color(40, 40, 40));
		browserPanel.setBorder(BorderFactory.createEmptyBorder());
		RoundedPanel browserInnerPanel = new RoundedPanel(10, 5, 5);
		browserInnerPanel.setBackground(new Color(100, 100, 100));
		browserInnerPanel.setLayout(new BoxLayout(browserInnerPanel,
				BoxLayout.X_AXIS));
		clusterList = new JList();
		clusterList.setBackground(colorListBg);
		clusterList.setForeground(colorListFg);
		clusterList.setSelectionForeground(colorListBg);
		clusterList.setSelectionBackground(colorListSelectedBg);
		clusterList.setSelectionForeground(colorListSelectedFg);
		// clusterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		footprintList = new JList();
		footprintList.setBackground(colorListBg);
		footprintList.setForeground(colorListFg);
		footprintList.setSelectionForeground(colorListBg);
		footprintList.setSelectionBackground(colorListSelectedBg);
		footprintList.setSelectionForeground(colorListSelectedFg);
		clusterList.setModel(new ClusterTypeSetListModel(clusters));
		clusterList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				footprintList.setModel(new FootprintListModel(
						((ClusterType) clusterList.getSelectedValue())
								.footprint()));
			}
		});
		JScrollPane clusterListScrollPane = new JScrollPane(clusterList);
		clusterListScrollPane.setBorder(BorderFactory.createLineBorder(
				new Color(80, 80, 80), 2));
		JScrollPane footprintListScrollPane = new JScrollPane(footprintList);
		footprintListScrollPane.setBorder(BorderFactory.createLineBorder(
				new Color(80, 80, 80), 2));
		browserInnerPanel.add(clusterListScrollPane);
		browserInnerPanel.add(Box.createHorizontalStrut(5));
		browserInnerPanel.add(footprintListScrollPane);
		browserPanel.add(browserInnerPanel, BorderLayout.CENTER);
		this.add(browserPanel, BorderLayout.SOUTH);
		ActivityClusterPanel clusterPanel = new ActivityClusterPanel(log,
				clusters);
		JScrollPane scrollPane = new JScrollPane(clusterPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		this.add(scrollPane, BorderLayout.CENTER);
	}

}
