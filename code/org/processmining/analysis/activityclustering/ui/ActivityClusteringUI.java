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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.NiceDoubleSlider;
import org.deckfour.slickerbox.components.NiceIntegerSlider;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.processmining.analysis.activityclustering.ClusterScanner;
import org.processmining.analysis.activityclustering.ClusterTypeSet;
import org.processmining.analysis.activityclustering.metrics.StrictCorrelationMetric;
import org.processmining.analysis.activityclustering.model.Cluster;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.RuntimeUtils;
import org.processmining.framework.util.StopWatch;

/**
 * @author christian
 * 
 */
public class ActivityClusteringUI extends JPanel {

	protected LogReader log;
	protected StrictCorrelationMetric metric;
	protected JComponent view;

	protected RoundedPanel confPanel;
	protected ProgressPanel progress;

	protected JCheckBox useOriginatorBox;
	protected JCheckBox useProximityBox;
	protected JCheckBox useEventNameBox;
	protected JCheckBox useEventTypeBox;
	protected JCheckBox useDataTypesBox;
	protected JCheckBox useDataValuesBox;
	protected JSlider proximitySlider;
	protected JLabel proximityLabel;
	protected NiceIntegerSlider lookaheadSlider;
	protected NiceDoubleSlider thresholdSlider;
	protected NiceDoubleSlider minOverlapSlider;
	protected JButton startButton;
	protected boolean isAlreadyPainted = false;

	protected HeaderBar header;

	public ActivityClusteringUI(LogReader log) {
		this.log = log;
		this.metric = new StrictCorrelationMetric();
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(60, 60, 60));
		header = new HeaderBar("Activity Clustering ][");
		this.add(header, BorderLayout.NORTH);
		buildConfigurationPanel();
		this.add(confPanel, BorderLayout.CENTER);
		view = confPanel;
		progress = new ProgressPanel("Clustering");
	}

	protected void startClustering() {
		this.remove(view);
		this.add(progress.getPanel(), BorderLayout.CENTER);
		this.view = progress.getPanel();
		this.revalidate();
		this.repaint();
		Thread scanThread = new Thread() {
			public void run() {
				ClusterScanner scanner = new ClusterScanner(log, metric,
						lookaheadSlider.getValue(), thresholdSlider.getValue());
				ClusterTypeSet typeSet = new ClusterTypeSet(minOverlapSlider
						.getValue());
				progress.setMinMax(0, log.numberOfInstances());
				for (int i = 0; i < log.numberOfInstances(); i++) {
					progress.setProgress(i);
					progress.setNote("Scanning trace " + (i + 1) + " (of "
							+ log.numberOfInstances() + ")");
					try {
						List<Cluster> clusters = scanner.scanInstance(i);
						for (int k = 0; k < clusters.size(); k++) {
							typeSet.add(clusters.get(k));
						}
					} catch (IOException e) {
						// nothing we can do here..
						e.printStackTrace();
					}
				}
				Message.add("Scanned a total of " + typeSet.size()
						+ " cluster types");
				remove(view);
				ActionListener closeListener = new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						header.setCloseActionListener(null);
						remove(view);
						view = confPanel;
						add(confPanel, BorderLayout.CENTER);
						revalidate();
						repaint();
					}
				};
				header.setCloseActionListener(closeListener);
				view = new ActivityClusteringResultUI(log, typeSet);
				add(view, BorderLayout.CENTER);
				revalidate();
				repaint();
			}
		};
		scanThread.start();
	}

	protected void buildConfigurationPanel() {
		confPanel = new RoundedPanel(5, 10, 10);
		confPanel.setBackground(new Color(140, 140, 140));
		confPanel.setLayout(new BoxLayout(confPanel, BoxLayout.Y_AXIS));
		JLabel topLabel = new JLabel("Configuration");
		topLabel.setFont(topLabel.getFont().deriveFont(16f).deriveFont(
				Font.BOLD));
		proximityLabel = new JLabel(StopWatch.formatDuration(600000));
		proximityLabel.setFont(proximityLabel.getFont().deriveFont(11f));
		useOriginatorBox = new JCheckBox("enforce same originator in clusters",
				true);
		useOriginatorBox.setOpaque(false);
		useOriginatorBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				metric.setEnforceSameOriginator(useOriginatorBox.isSelected());
			}
		});
		useProximityBox = new JCheckBox("cluster proximate events", true);
		useProximityBox.setOpaque(false);
		useProximityBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				metric.setUseProximity(useProximityBox.isSelected());
				proximitySlider.setEnabled(useProximityBox.isSelected());
			}
		});
		useEventNameBox = new JCheckBox("cluster similar event names", true);
		useEventNameBox.setOpaque(false);
		useEventNameBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				metric.setUseEventNames(useEventNameBox.isSelected());
			}
		});
		useEventTypeBox = new JCheckBox("cluster dissimilar event types", true);
		useEventTypeBox.setOpaque(false);
		useEventTypeBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				metric.setUseEventTypes(useEventTypeBox.isSelected());
			}
		});
		useDataTypesBox = new JCheckBox("cluster events with same attributes",
				true);
		useDataTypesBox.setOpaque(false);
		useDataTypesBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				metric.setUseDataAttributes(useDataTypesBox.isSelected());
			}
		});
		useDataValuesBox = new JCheckBox("cluster events with similar data",
				true);
		useDataValuesBox.setOpaque(false);
		useDataValuesBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				metric.setUseDataValues(useDataValuesBox.isSelected());
			}
		});
		proximitySlider = new JSlider(JSlider.HORIZONTAL, 1, 10800000, 600000);
		proximitySlider.setOpaque(false);
		proximitySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				proximityLabel.setText(StopWatch.formatDuration(proximitySlider
						.getValue()));
				metric.setProximityZone(proximitySlider.getValue());
			}
		});
		JLabel proximityIntroLabel = new JLabel("Proximity window");
		proximityIntroLabel.setOpaque(false);
		startButton = new AutoFocusButton("start clustering");
		if (RuntimeUtils.isRunningMacOsX() == true) {
			startButton.setOpaque(false);
		}
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startClustering();
			}
		});
		startButton.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
					startClustering();
				}
			}

			public void keyReleased(KeyEvent evt) { /* ignore */
			}

			public void keyTyped(KeyEvent evt) { /* ignore */
			}
		});
		startButton.requestFocusInWindow();
		lookaheadSlider = new NiceIntegerSlider("Lookahead", 2, 200, 20);
		thresholdSlider = new NiceDoubleSlider("Min. correlation", 0.0, 1.0,
				0.2);
		minOverlapSlider = new NiceDoubleSlider("Footprint overlap", 0.5, 1.0,
				0.7);
		JLabel clusteringTitle = new JLabel("Clustering options:");
		clusteringTitle.setOpaque(false);
		// assemble UI
		confPanel.add(packLeftAligned(topLabel, 0));
		confPanel.add(Box.createVerticalStrut(10));
		confPanel.add(packLeftAligned(clusteringTitle, 50));
		confPanel.add(packLeftAligned(lookaheadSlider, 60));
		confPanel.add(packLeftAligned(thresholdSlider, 60));
		confPanel.add(packLeftAligned(minOverlapSlider, 60));
		confPanel.add(Box.createVerticalStrut(10));
		confPanel.add(packLeftAligned(this.useProximityBox, 60));
		confPanel.add(Box.createVerticalStrut(5));
		confPanel.add(packLeftAligned(proximityIntroLabel, 40));
		confPanel.add(this.proximitySlider);
		confPanel.add(packLeftAligned(this.proximityLabel, 60));
		confPanel.add(Box.createVerticalStrut(10));
		confPanel.add(packLeftAligned(this.useEventNameBox, 60));
		confPanel.add(packLeftAligned(this.useEventTypeBox, 60));
		confPanel.add(Box.createVerticalStrut(10));
		confPanel.add(packLeftAligned(this.useDataTypesBox, 60));
		confPanel.add(packLeftAligned(this.useDataValuesBox, 60));
		confPanel.add(Box.createVerticalGlue());
		confPanel.add(packLeftAligned(startButton, 50));
	}

	public JPanel packLeftAligned(JComponent component, int distFromLeft) {
		Dimension compPref = component.getPreferredSize();
		Dimension compMin = component.getMinimumSize();
		JPanel packed = new JPanel();
		packed.setPreferredSize(compPref);
		packed.setMaximumSize(new Dimension(2000, compPref.height));
		packed.setMinimumSize(compMin);
		packed.setOpaque(false);
		packed.setBorder(BorderFactory.createEmptyBorder());
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		packed.add(Box.createHorizontalStrut(distFromLeft));
		packed.add(component);
		packed.add(Box.createHorizontalGlue());
		return packed;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		startButton.requestFocusInWindow();
	}

	public void paint(Graphics g) {
		if (isAlreadyPainted == false) {
			startButton.requestFocusInWindow();
			isAlreadyPainted = true;
		}
		super.paint(g);
	}

}
