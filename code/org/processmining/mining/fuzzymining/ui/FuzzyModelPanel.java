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

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.FlatTabbedPane;
import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.fuzzymining.edit.FuzzyGraphEditorPanel;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;
import org.processmining.mining.fuzzymining.graph.MutableFuzzyGraph;
import org.processmining.mining.fuzzymining.metrics.MetricsRepository;
import org.processmining.mining.fuzzymining.metrics.unary.UnaryMetric;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FuzzyModelPanel extends GradientPanel implements Provider {

	protected FuzzyGraph originalGraph;
	protected MutableFuzzyGraph graph;
	protected LogReader log;
	protected MetricsRepository metrics;
	protected Color background = new Color(100, 100, 100);
	protected Color canvasBackground = Color.WHITE;
	protected int numberOfNodes;

	protected FastTransformerPanel fastTransformerPanel;
	protected FuzzyGraphEditorPanel editorPanel;
	protected UnaryMetricsPanel unaryMetricsPanel;
	protected BinaryMetricsComparisonPanel binaryMetricsComparisonPanel;
	protected FuzzyAnimationPanel animationPanel;

	public FuzzyModelPanel(MetricsRepository metrics, int numberOfNodes) {
		this(new MutableFuzzyGraph(metrics), metrics.getLogReader(), metrics,
				numberOfNodes);
	}

	public FuzzyModelPanel(FuzzyGraph graph, LogReader log,
			MetricsRepository metrics, int numberOfNodes) {
		super(new Color(60, 60, 60), new Color(20, 20, 20));
		this.originalGraph = graph;
		this.graph = (MutableFuzzyGraph) graph.clone();
		this.log = log;
		this.metrics = metrics;
		this.numberOfNodes = numberOfNodes;
		setupUI();
	}

	protected void setupUI() {
		// setup panel basics
		this.setDoubleBuffered(true);
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder());
		FlatTabbedPane tabPane = new FlatTabbedPane("", new Color(240, 240,
				240, 230), new Color(100, 100, 100), new Color(220, 220, 220,
				150));
		fastTransformerPanel = new FastTransformerPanel(graph, log,
				numberOfNodes);
		tabPane.addTab("Transformer", fastTransformerPanel);
		editorPanel = new FuzzyGraphEditorPanel(graph, false);
		tabPane.addTab("Editor", editorPanel);
		if (metrics != null) {
			// create unary metrics comparison panel
			unaryMetricsPanel = new UnaryMetricsPanel(metrics.getLogReader()
					.getLogSummary().getLogEvents());
			for (UnaryMetric unary : metrics.getUnaryMetrics()) {
				unaryMetricsPanel.addMetric(unary);
			}
			unaryMetricsPanel.addMetric(metrics.getAggregateUnaryMetric());
			RoundedPanel unaryMetricsEnclosure = new RoundedPanel(10, 5, 0);
			unaryMetricsEnclosure.setBackground(Color.black);
			unaryMetricsEnclosure.setLayout(new BorderLayout());
			unaryMetricsEnclosure.add(unaryMetricsPanel, BorderLayout.CENTER);
			// create binary metrics comparison panel
			binaryMetricsComparisonPanel = new BinaryMetricsComparisonPanel(
					metrics);
			// add to tabbed pane
			tabPane.addTab("Unary Metrics", unaryMetricsEnclosure);
			tabPane.addTab("Binary Metrics", binaryMetricsComparisonPanel);
		}
		animationPanel = new FuzzyAnimationPanel(graph, log);
		tabPane.addTab("Animation", animationPanel);
		// assemble UI
		JPanel centerPanel = new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		centerPanel.add(tabPane, BorderLayout.CENTER);
		this.add(centerPanel, BorderLayout.CENTER);
		HeaderBar header = new HeaderBar("Fuzzy Model Toolkit");
		header.setHeight(40);
		this.add(header, BorderLayout.NORTH);
	}

	public FuzzyGraph getOriginalGraph() {
		return originalGraph;
	}

	public FuzzyGraph getCurrentGraph() {
		return graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] ftpo = fastTransformerPanel.getProvidedObjects();
		ProvidedObject[] objects = new ProvidedObject[ftpo.length + 1];
		for (int i = 0; i < ftpo.length; i++) {
			objects[i] = ftpo[i];
		}
		objects[ftpo.length] = new ProvidedObject("Original Fuzzy Model",
				new Object[] { originalGraph });
		return objects;
	}

}
