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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 */
public class ClusterList extends RoundedPanel {

	private static final long serialVersionUID = 2015846398013137615L;

	protected static Color COLOR_BG = new Color(140, 140, 140);
	protected static Color COLOR_FG = new Color(30, 30, 30);
	protected static Color COLOR_ITEM_BG = new Color(50, 50, 50);
	protected static Color COLOR_ITEM_HL = new Color(80, 80, 80);
	protected static Color COLOR_ITEM_BG_SELECTED = new Color(120, 10, 10);
	protected static Color COLOR_ITEM_HL_SELECTED = new Color(160, 50, 50);
	protected static Color COLOR_ITEM_FG = new Color(200, 200, 200);

	protected DefaultListModel itemListModel;
	protected JList itemList;

	public ClusterList(ClusterSet clusters) {
		super(10, 5, 5);
		setBackground(COLOR_BG);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		itemListModel = new DefaultListModel();
		for (Cluster cluster : clusters.getClusters()) {
			itemListModel.addElement(cluster);
		}
		itemList = new JList(itemListModel);
		itemList.setBackground(new Color(100, 100, 100));
		itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		itemList.setCellRenderer(new SmoothItemListRenderer());
		// title
		add(wrapNamedComponent("Select cluster to show details", null));
		add(Box.createVerticalStrut(8));
		// create list of items
		JScrollPane listScrollPane = new JScrollPane(itemList);
		listScrollPane.setBorder(BorderFactory.createEmptyBorder());
		listScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		listScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(listScrollPane);
	}

	public void addListSelectionListener(ListSelectionListener listener) {
		itemList.addListSelectionListener(listener);
	}

	public Cluster getSelectedCluster() {
		return (Cluster) itemList.getSelectedValue();
	}

	protected static JPanel wrapNamedComponent(String name, JComponent component) {
		JPanel wrapped = new JPanel();
		wrapped.setMaximumSize(new Dimension(2000, 30));
		wrapped.setMinimumSize(new Dimension(100, 30));
		wrapped.setPreferredSize(new Dimension(200, 30));
		wrapped.setBorder(BorderFactory.createEmptyBorder());
		wrapped.setOpaque(false);
		wrapped.setLayout(new BoxLayout(wrapped, BoxLayout.X_AXIS));
		if (name != null) {
			JLabel title = new JLabel(name);
			title.setOpaque(false);
			title.setForeground(COLOR_FG);
			wrapped.add(title);
			if (component != null) {
				wrapped.add(Box.createHorizontalStrut(10));
				wrapped.add(component);
			}
			wrapped.add(Box.createHorizontalGlue());
		} else {
			wrapped.add(Box.createHorizontalGlue());
			wrapped.add(component);
		}
		return wrapped;
	}

	protected class SmoothItemListRenderer implements ListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value,
				int index, final boolean isSelected, boolean cellHasFocus) {
			SmoothPanel cell = new SmoothPanel() {
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					if (isSelected == true) {
						int width = this.getWidth();
						int height = this.getHeight();
						final Graphics2D g2d = (Graphics2D) g;
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
								RenderingHints.VALUE_ANTIALIAS_ON);
						g2d.setColor(new Color(220, 220, 220, 180));
						int unit = height / 3;
						int x[] = new int[] { width - unit - unit - 4,
								width - unit - 4, width - unit - unit - 4 };
						int y[] = new int[] { unit, height / 2, height - unit };
						g2d.fillPolygon(x, y, 3);
					}
				}
			};
			if (isSelected == true) {
				cell.setBackground(COLOR_ITEM_BG_SELECTED);
				cell.setHighlight(COLOR_ITEM_HL_SELECTED);
			} else {
				cell.setBackground(COLOR_ITEM_BG);
				cell.setHighlight(COLOR_ITEM_HL);
			}
			cell.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
			cell.setMinimumSize(new Dimension(80, 40));
			cell.setMaximumSize(new Dimension(2000, 40));
			cell.setPreferredSize(new Dimension(200, 40));
			cell.setBorderAlpha(120);
			cell.setLayout(new BoxLayout(cell, BoxLayout.X_AXIS));
			Cluster cluster = (Cluster) value;
			String name = cluster.getName() + " (" + cluster.size()
					+ " traces)";
			JLabel label = new JLabel(name);
			label.setForeground(COLOR_ITEM_FG);
			label.setOpaque(false);
			cell.add(label);
			cell.add(Box.createHorizontalGlue());
			return cell;
		}

	}

}
