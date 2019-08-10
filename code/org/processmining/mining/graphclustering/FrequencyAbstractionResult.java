/**
 * Project: ProM
 * File: FrequencyAbstractionResult.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Jul 7, 2006, 3:11:32 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright 
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in 
 *      the documentation and/or other materials provided with the 
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the 
 *      names of its contributors may be used to endorse or promote 
 *      products derived from this software without specific prior written 
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.mining.graphclustering;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;
import org.processmining.mining.MiningResult;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FrequencyAbstractionResult extends JPanel implements MiningResult,
		Provider {

	/**
	 * serialization version uid not used currently - implement properly if you
	 * plan to do so!
	 */
	private static final long serialVersionUID = 3995190570612689639L;

	protected LogReader logReader = null;
	protected LogEvents logEvents = null;
	protected DoubleMatrix2D followMatrix = null;

	protected ClusterGraph graph = null;
	protected JSplitPane splitPane = null;
	protected JSlider thresholdSlider = null;
	protected JLabel thresholdLabel = null;
	protected JCheckBox attenuateEdgesCheckBox = null;
	protected JCheckBox mergeClustersCheckBox = null;

	public FrequencyAbstractionResult(LogReader reader, LogEvents events,
			DoubleMatrix2D follows) {
		logReader = reader;
		logEvents = events;
		followMatrix = follows;
		generateGUI();
		if (UISettings.getInstance().getTest() == true) {
			Message.add("<FrequencyAbstractionMiner>", Message.TEST);
			graph.testOutput();
			Message.add("</FrequencyAbstractionMiner>", Message.TEST);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getLogReader()
	 */
	public LogReader getLogReader() {
		return logReader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getVisualization()
	 */
	public JComponent getVisualization() {
		return this;
	}

	protected void generateGUI() {
		this.setBackground(new Color(255, 0, 0));
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		this.setLayout(new BorderLayout());
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
		upperPanel.setMinimumSize(new Dimension(150, 150));
		upperPanel.setBackground(new Color(200, 190, 150));
		JLabel upperControlLabel = new JLabel("Threshold control");
		upperControlLabel.setOpaque(false);
		upperPanel.add(upperControlLabel);
		attenuateEdgesCheckBox = new JCheckBox("attenuate edges", true);
		attenuateEdgesCheckBox.setOpaque(false);
		attenuateEdgesCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				redrawGraph();
			}
		});
		upperPanel.add(attenuateEdgesCheckBox);
		mergeClustersCheckBox = new JCheckBox("merge sibling clusters", true);
		mergeClustersCheckBox.setOpaque(false);
		mergeClustersCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				redrawGraph();
			}
		});
		upperPanel.add(mergeClustersCheckBox);
		controlPanel.add(upperPanel, BorderLayout.NORTH);
		JPanel thresholdPanel = new JPanel();
		thresholdPanel.setLayout(new BorderLayout());
		thresholdPanel.setBackground(new Color(0, 0, 0));
		thresholdSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);
		thresholdSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				updateThresholdLabel();
			}
		});
		thresholdPanel.add(thresholdSlider, BorderLayout.CENTER);
		thresholdLabel = new JLabel("threshold");
		thresholdLabel.setBackground(new Color(0, 0, 0));
		thresholdLabel.setForeground(new Color(255, 0, 0));
		thresholdLabel.setOpaque(true);
		thresholdLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		thresholdPanel.add(thresholdLabel, BorderLayout.SOUTH);
		controlPanel.add(thresholdPanel, BorderLayout.CENTER);
		splitPane.setRightComponent(controlPanel);
		controlPanel.setMaximumSize(new Dimension(150, 500));
		graph = new ClusterGraph(logEvents, followMatrix);
		setSliderToThresholdShowing(5);
		this.add(splitPane, BorderLayout.CENTER);
		redrawGraph();
	}

	protected double getThresholdFromSlider() {
		double threshold = (double) thresholdSlider.getValue() / 1000.0;
		// normalize threshold to minimal node frequency
		threshold = ((1.0 - graph.getMinimalNodeFrequency()) * threshold)
				+ graph.getMinimalNodeFrequency();
		return threshold;
	}

	protected void setSliderToThresholdShowing(int showingNumberOfNodes) {
		double threshold = graph.getThresholdShowing(showingNumberOfNodes)
				- graph.getMinimalNodeFrequency();
		threshold = threshold / (1.0 - graph.getMinimalNodeFrequency());
		System.out.println("setting slider to initial value of "
				+ (int) (threshold * 1000.0));
		thresholdSlider.setValue((int) (threshold * 1000.0));
	}

	protected void updateThresholdLabel() {
		thresholdLabel.setText("Threshold: "
				+ ClusterGraph.format(getThresholdFromSlider()));
		if (thresholdSlider.getValueIsAdjusting() == false) {
			redrawGraph();
		}
	}

	protected void redrawGraph() {
		double threshold = getThresholdFromSlider();
		graph.setAttenuateEdges(attenuateEdgesCheckBox.isSelected());
		graph.setMergeClusters(mergeClustersCheckBox.isSelected());
		Message.add("redrawing with clustering threshold of " + threshold);
		// SpareClusterGraph spg = new SpareClusterGraph(logEvents,
		// followMatrix, threshold);
		try {
			JPanel graphPanel = graph.getGraphPanel(threshold);
			graphPanel.setPreferredSize(new Dimension(800, 600));
			graphPanel.setSize(new Dimension(800, 600));
			graphPanel.setMinimumSize(new Dimension(800, 600));
			splitPane.setLeftComponent(graphPanel);
			splitPane.setDividerLocation(splitPane.getWidth() - 180);
			// splitPane.setLeftComponent(spg.getGraphPanel(threshold));
			// splitPane.setDividerLocation(0.9);
		} catch (Exception e) {
			Message.add("Unable to draw clustering graph!", Message.NORMAL);
			e.printStackTrace();
		}
	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = { new ProvidedObject(
				"Frequency cluster graph", new Object[] { new DotFileWriter() {
					public void writeToDot(Writer bw) throws IOException {
						graph.writeToDot(bw);
					}
				} }) };
		return objects;
	}

};
