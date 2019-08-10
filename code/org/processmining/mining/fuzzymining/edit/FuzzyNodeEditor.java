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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.gantzgraf.model.GGNode;
import org.deckfour.slickerbox.components.NiceDoubleSlider;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.components.NiceSlider.Orientation;
import org.processmining.mining.fuzzymining.graph.ClusterNode;
import org.processmining.mining.fuzzymining.graph.GGAdapter;
import org.processmining.mining.fuzzymining.graph.MutableFuzzyGraph;
import org.processmining.mining.fuzzymining.graph.Node;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FuzzyNodeEditor extends JPanel {

	private static final long serialVersionUID = -8841397534791559018L;

	protected FuzzyGraphEditorPanel parent;
	protected GGNode graphNode;
	protected Node fuzzyNode;
	protected boolean isCluster = false;
	protected ActionListener closeListener;

	protected JTextField nameField;
	protected JTextField typeField;
	protected NiceDoubleSlider significanceSlider;

	public FuzzyNodeEditor(FuzzyGraphEditorPanel parent, GGNode node,
			ActionListener closeListener) {
		this.parent = parent;
		this.graphNode = node;
		this.fuzzyNode = (Node) node
				.getAttribute(GGAdapter.ATTR_FUZZY_GRAPH_ELEMENT);
		isCluster = (this.fuzzyNode instanceof ClusterNode);
		this.closeListener = closeListener;
		initializeGui();
	}

	protected void initializeGui() {
		// setup panel basics
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// node name
		nameField = new JTextField(fuzzyNode.getElementName());
		nameField.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				// live update of node property changes
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						fuzzyNode.setElementName(nameField.getText());
						updateGraphNodeLabel();
					}
				});
			}
		});
		nameField.setFont(nameField.getFont().deriveFont(11f));
		nameField.setBackground(new Color(180, 180, 180));
		nameField.setForeground(new Color(10, 10, 10));
		nameField.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		JLabel nameLabel = new JLabel("Name:");
		nameLabel.setMaximumSize(new Dimension(40, 25));
		nameLabel.setPreferredSize(new Dimension(40, 25));
		nameLabel.setOpaque(false);
		nameLabel.setFont(nameLabel.getFont().deriveFont(11f));
		JPanel namePanel = FuzzyGraphEditorPanel.layoutHorizontally(
				new Component[] { nameLabel, Box.createHorizontalStrut(5),
						nameField }, 20);
		// node type
		typeField = new JTextField(fuzzyNode.getEventType());
		if (isCluster) {
			typeField.setEditable(false);
		} else {
			typeField.addKeyListener(new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					// live update of node property changes
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							fuzzyNode.setEventType(typeField.getText());
							updateGraphNodeLabel();
						}
					});
				}
			});
		}
		typeField.setFont(typeField.getFont().deriveFont(11f));
		typeField.setBackground(new Color(180, 180, 180));
		typeField.setForeground(new Color(10, 10, 10));
		typeField.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		JLabel typeLabel = new JLabel("Type:");
		typeLabel.setMaximumSize(new Dimension(40, 25));
		typeLabel.setPreferredSize(new Dimension(40, 25));
		typeLabel.setOpaque(false);
		typeLabel.setFont(typeLabel.getFont().deriveFont(11f));
		JPanel typePanel = FuzzyGraphEditorPanel.layoutHorizontally(
				new Component[] { typeLabel, Box.createHorizontalStrut(5),
						typeField }, 20);
		// significance slider
		significanceSlider = new NiceDoubleSlider("significance", 0, 1.0,
				fuzzyNode.getSignificance(), Orientation.VERTICAL);
		if (isCluster) {
			significanceSlider.setEnabled(false);
		} else {
			// editing significance is for non-cluster nodes only!
			significanceSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					// live update of node property changes
					fuzzyNode.setSignificance(significanceSlider.getValue());
					updateGraphNodeLabel();
				}
			});
		}
		// delete node button
		JButton deleteNodeButton = new SlickerButton("delete");
		deleteNodeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((MutableFuzzyGraph) fuzzyNode.getGraph())
						.hidePermanently(fuzzyNode);
				parent.updateGraphLayout();
				closeListener.actionPerformed(new ActionEvent(this, 0,
						"node deleted"));
			}
		});
		// close button
		JButton closeButton = new SlickerButton("close");
		closeButton.addActionListener(closeListener);
		// assemble UI
		JLabel title = new JLabel("Edit node");
		title.setOpaque(false);
		title.setFont(title.getFont().deriveFont(15f));
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setHorizontalTextPosition(JLabel.CENTER);
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		this.add(FuzzyGraphEditorPanel.centerHorizontally(title, 30));
		if (isCluster == true) {
			this.add(Box.createVerticalStrut(8));
			String warning = "<html>You can only edit the name of a cluster node, or "
					+ "remove it from the graph (with all its contained elements). "
					+ "Editing event type and significance is reserved for primitive "
					+ "nodes.</html>";
			JLabel warningLabel = FuzzyGraphEditorPanel
					.createMessageLabel(warning);
			warningLabel.setForeground(new Color(80, 0, 0));
			this.add(warningLabel);
			this.add(Box.createVerticalStrut(8));
		} else {
			this.add(Box.createVerticalStrut(15));
		}
		this.add(namePanel);
		this.add(Box.createVerticalStrut(8));
		this.add(typePanel);
		this.add(Box.createVerticalStrut(15));
		this.add(significanceSlider);
		this.add(Box.createVerticalStrut(15));
		this
				.add(FuzzyGraphEditorPanel.centerHorizontally(deleteNodeButton,
						25));
		this.add(Box.createVerticalStrut(10));
		this.add(FuzzyGraphEditorPanel.centerHorizontally(closeButton, 25));
		this.add(Box.createVerticalGlue());
	}

	protected void updateGraphNodeLabel() {
		graphNode.setLabel(GGAdapter.getNodeLabel(fuzzyNode));
		graphNode.updateView();
		graphNode.updateView();
	}

}
