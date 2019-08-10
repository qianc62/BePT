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
package org.processmining.analysis.traceclustering.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.framework.ui.slicker.LogView;

/**
 * @author christian
 * 
 */
public class ClusterSetView extends JPanel implements ListSelectionListener {

	protected ClusterSet clusters;
	protected JComponent view;
	protected ClusterList clusterList;

	public ClusterSetView(ClusterSet clusters) {
		this.clusters = clusters;
		this.setBackground(new Color(80, 80, 80));
		this.setLayout(new BorderLayout());
		view = new JPanel();
		view.setBorder(BorderFactory.createEmptyBorder());
		view.setOpaque(false);
		this.add(view, BorderLayout.CENTER);
		clusterList = new ClusterList(clusters);
		clusterList.setMinimumSize(new Dimension(250, 100));
		clusterList.setMaximumSize(new Dimension(250, 2000));
		clusterList.setPreferredSize(new Dimension(250, 200));
		clusterList.addListSelectionListener(this);
		this.add(clusterList, BorderLayout.WEST);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
	 * .ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent evt) {
		Cluster selected = clusterList.getSelectedCluster();
		try {
			LogView logView = new LogView(selected.getFilteredLog());
			JLabel header = new JLabel("Traces in " + selected.getName() + ":");
			header.setOpaque(false);
			header.setFont(header.getFont().deriveFont(14f));
			header.setForeground(new Color(160, 160, 160));
			header.setAlignmentX(LEFT_ALIGNMENT);
			header.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
			RoundedPanel enclosure = new RoundedPanel(10, 5, 5);
			enclosure.setBackground(new Color(40, 40, 40));
			enclosure.setLayout(new BorderLayout());
			enclosure.add(header, BorderLayout.NORTH);
			enclosure.add(logView, BorderLayout.CENTER);
			this.remove(view);
			view = enclosure;
			this.add(view, BorderLayout.CENTER);
			revalidate();
			repaint();
		} catch (Exception e) {
			JPanel empty = new JPanel();
			empty.setOpaque(false);
			this.remove(view);
			view = empty;
			this.add(view, BorderLayout.CENTER);
			e.printStackTrace();
			return;
		}
	}
}
