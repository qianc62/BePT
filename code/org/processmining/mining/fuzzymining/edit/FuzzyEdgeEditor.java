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
package org.processmining.mining.fuzzymining.edit;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.gantzgraf.model.GGEdge;
import org.deckfour.slickerbox.components.NiceDoubleSlider;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.components.NiceSlider.Orientation;
import org.processmining.mining.fuzzymining.graph.ClusterNode;
import org.processmining.mining.fuzzymining.graph.Edge;
import org.processmining.mining.fuzzymining.graph.GGAdapter;
import org.processmining.mining.fuzzymining.graph.MutableFuzzyGraph;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FuzzyEdgeEditor extends JPanel {

	private static final long serialVersionUID = 1520601923312409475L;

	protected FuzzyGraphEditorPanel parent;

	protected GGEdge graphEdge;
	protected Edge fuzzyEdge;
	protected MutableFuzzyGraph graph;
	protected ActionListener closeListener;
	protected boolean isConnectedToCluster = false;

	protected NiceDoubleSlider significanceSlider;
	protected NiceDoubleSlider correlationSlider;

	public FuzzyEdgeEditor(FuzzyGraphEditorPanel parent, GGEdge edge,
			ActionListener closeListener) {
		this.parent = parent;
		this.graphEdge = edge;
		this.fuzzyEdge = (Edge) edge
				.getAttribute(GGAdapter.ATTR_FUZZY_GRAPH_ELEMENT);
		this.graph = (MutableFuzzyGraph) edge
				.getAttribute(GGAdapter.ATTR_FUZZY_GRAPH);
		if (this.fuzzyEdge.getSource() instanceof ClusterNode
				|| this.fuzzyEdge.getTarget() instanceof ClusterNode) {
			isConnectedToCluster = true;
		} else {
			isConnectedToCluster = false;
		}
		this.closeListener = closeListener;
		initializeGui();
	}

	protected void initializeGui() {
		// setup panel basics
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder());
		// significance slider
		significanceSlider = new NiceDoubleSlider("significance", 0, 1.0,
				fuzzyEdge.getSignificance(), Orientation.VERTICAL);
		if (isConnectedToCluster) {
			significanceSlider.setEnabled(false);
		} else {
			// editing significance is for non-cluster connected edges only!
			significanceSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					// live update of edge property changes
					int sourceIndex = fuzzyEdge.getSource().getIndex();
					int targetIndex = fuzzyEdge.getTarget().getIndex();
					graph.setBinarySignificance(sourceIndex, targetIndex,
							significanceSlider.getValue());
					graph.getEdgeSignificanceMetric().setMeasure(sourceIndex,
							targetIndex, significanceSlider.getValue());
					fuzzyEdge.setSignificance(significanceSlider.getValue());
					updateGraphEdge();
				}
			});
		}
		// correlation slider
		correlationSlider = new NiceDoubleSlider("correlation", 0, 1.0,
				fuzzyEdge.getCorrelation(), Orientation.VERTICAL);
		if (isConnectedToCluster) {
			correlationSlider.setEnabled(false);
		} else {
			// editing correlation is for non-cluster connected edges only!
			correlationSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					// live update of edge property changes
					int sourceIndex = fuzzyEdge.getSource().getIndex();
					int targetIndex = fuzzyEdge.getTarget().getIndex();
					graph.setBinaryCorrelation(sourceIndex, targetIndex,
							correlationSlider.getValue());
					graph.getEdgeCorrelationMetric().setMeasure(sourceIndex,
							targetIndex, correlationSlider.getValue());
					fuzzyEdge.setCorrelation(correlationSlider.getValue());
					updateGraphEdge();
				}
			});
		}
		// slider panel
		JPanel sliderPanel = FuzzyGraphEditorPanel.layoutHorizontally(
				new Component[] { significanceSlider, correlationSlider }, -1);
		// delete node button
		JButton deleteEdgeButton = new SlickerButton("delete");
		deleteEdgeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graph.removePermanently(fuzzyEdge);
				parent.updateGraphLayout();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						closeListener.actionPerformed(new ActionEvent(this, 0,
								"edge deleted"));
					}
				});
			}
		});
		// close button
		JButton closeButton = new SlickerButton("close");
		closeButton.addActionListener(closeListener);
		// assemble UI
		JLabel title = new JLabel("Edit edge");
		title.setOpaque(false);
		title.setFont(title.getFont().deriveFont(15f));
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setHorizontalTextPosition(JLabel.CENTER);
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		this.add(FuzzyGraphEditorPanel.centerHorizontally(title, 30));
		if (isConnectedToCluster == true) {
			this.add(Box.createVerticalStrut(8));
			String warning = "<html>The edge you have selected is connected to a cluster, "
					+ "which means it actually represents a set of connections. "
					+ "Thus, you cannot modify its significance or correlation.</html>";
			JLabel warningLabel = FuzzyGraphEditorPanel
					.createMessageLabel(warning);
			warningLabel.setForeground(new Color(80, 0, 0));
			this.add(warningLabel);
			this.add(Box.createVerticalStrut(8));
		} else {
			this.add(Box.createVerticalStrut(15));
		}
		this.add(sliderPanel);
		this.add(Box.createVerticalStrut(15));
		this
				.add(FuzzyGraphEditorPanel.centerHorizontally(deleteEdgeButton,
						25));
		this.add(Box.createVerticalStrut(10));
		this.add(FuzzyGraphEditorPanel.centerHorizontally(closeButton, 25));
		this.add(Box.createVerticalGlue());
	}

	protected void updateGraphEdge() {
		graphEdge.setPainter(GGAdapter.createEdgePainter(fuzzyEdge));
		graphEdge.setLabel(GGAdapter.getEdgeLabel(fuzzyEdge));
		graphEdge.setRelativeWidth((float) significanceSlider.getValue() / 2f);
		graphEdge.updateView();
		graphEdge.updateView();
	}
}
