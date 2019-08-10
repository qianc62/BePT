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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.gantzgraf.ui.GGGraphView;
import org.deckfour.slickerbox.components.InspectorButton;
import org.deckfour.slickerbox.components.StackedCardsTabbedPane;
import org.deckfour.slickerbox.ui.SlickerCheckBoxUI;
import org.deckfour.slickerbox.ui.SlickerRadioButtonUI;
import org.deckfour.slickerbox.ui.SlickerSliderUI;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.mining.fuzzymining.filter.FuzzyGraphProjectionFilter;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;
import org.processmining.mining.fuzzymining.graph.GGAdapter;
import org.processmining.mining.fuzzymining.graph.MutableFuzzyGraph;
import org.processmining.mining.fuzzymining.graph.transform.BestEdgeTransformer;
import org.processmining.mining.fuzzymining.graph.transform.ConcurrencyEdgeTransformer;
import org.processmining.mining.fuzzymining.graph.transform.FastTransformer;
import org.processmining.mining.fuzzymining.graph.transform.FuzzyEdgeTransformer;
import org.processmining.mining.fuzzymining.metrics.MetricsRepository;
import org.processmining.mining.fuzzymining.replay.FuzzyDetailAnalysis;
import org.processmining.mining.fuzzymining.replay.FuzzyReplay;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FastTransformerPanel extends JPanel implements ChangeListener,
		ItemListener, Provider {

	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String EDGE_TRANSFORMER_SELECTION = "EdgeTransformerSelection";
	public static final String EDGE_TRANSFORMER_SELECTION_BEST_EDGES = "EdgeTransformerSelectionBestEdges";
	public static final String EDGE_TRANSFORMER_SELECTION_FUZZY_EDGES = "EdgeTransformerSelectionFuzzyEdges";
	public static final String CONCURRENCY_EDGE_TRANSFORMER_ACTIVE = "ConcurrencyEdgeTransformerActive";
	public static final String NODE_CUTOFF = "NodeCutoff";
	public static final String FUZZY_EDGE_RATIO = "FuzzyEdgeRatio";
	public static final String FUZZY_EDGE_CUTOFF = "FuzzyEdgeCutoff";
	public static final String CONCURRENCY_THRESHOLD = "ConcurrencyThreshold";
	public static final String CONCURRENCY_RATIO = "ConcurrencyRatio";
	protected static final String EDGES_FUZZY_IGNORE_LOOPS = "EdgesFuzzyIgnoreLoops";
	protected static final String EDGES_FUZZY_INTERPRET_ABSOLUTE = "EdgesFuzzyInterpretAbsolute";

	protected LogReader log = null;
	protected MutableFuzzyGraph graph;
	protected FastTransformer fastTransformer;
	protected BestEdgeTransformer bestEdgeTransformer;
	protected FuzzyEdgeTransformer fuzzyEdgeTransformer;
	protected ConcurrencyEdgeTransformer concurrencyEdgeTransformer;

	protected FuzzyReplay replay;
	protected FuzzyDetailAnalysis detail;
	protected ReplayView replayView;
	protected DetailView detailView;

	protected SidePanel sidePanel;

	protected boolean enableRedraw;

	protected JPanel rootPanel;
	protected GGGraphView graphPanel = null;
	protected JRadioButton edgesBestRadioButton;
	protected JRadioButton edgesFuzzyRadioButton;
	protected JSlider nodeSignificanceSlider;
	protected JSlider edgesFuzzyRatioSlider;
	protected JSlider edgesFuzzyPercentageSlider;
	protected JSlider edgesConcurrencyThresholdSlider;
	protected JSlider edgesConcurrencyRatioSlider;
	protected JLabel nodeSignificanceLabel;
	protected JLabel edgesFuzzyRatioLabel;
	protected JLabel edgesFuzzyPercentageLabel;
	protected JLabel edgesConcurrencyThresholdLabel;
	protected JLabel edgesConcurrencyRatioLabel;
	protected JCheckBox edgesFuzzyIgnoreLoopBox;
	protected JCheckBox edgesFuzzyInterpretAbsoluteBox;
	protected JCheckBox edgesConcurrencyActiveBox;
	protected LedGauge replayGauge;
	protected LedGauge detailGauge;
	protected Color COLOR_BG = new Color(60, 60, 60);
	protected Color COLOR_BG2 = new Color(120, 120, 120);
	protected Color COLOR_FG = new Color(30, 30, 30);
	protected Font smallFont;

	public FastTransformerPanel(MetricsRepository metrics, int showNumberOfNodes) {
		this(new MutableFuzzyGraph(metrics), metrics.getLogReader(),
				showNumberOfNodes);
	}

	public FastTransformerPanel(MutableFuzzyGraph graph, int showNumberOfNodes) {
		this(graph, null, showNumberOfNodes);
	}

	public FastTransformerPanel(MutableFuzzyGraph graph, LogReader log,
			int showNumberOfNodes) {
		this.log = log;
		this.graph = graph;
		this.setBackground(new Color(240, 240, 240));
		fastTransformer = new FastTransformer();
		bestEdgeTransformer = new BestEdgeTransformer();
		fuzzyEdgeTransformer = new FuzzyEdgeTransformer();
		concurrencyEdgeTransformer = new ConcurrencyEdgeTransformer();
		enableRedraw = false;
		this.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						redrawGraph();
					}
				});
			}

			public void ancestorMoved(AncestorEvent event) {
				// ignore
			}

			public void ancestorRemoved(AncestorEvent event) {
				// ignore
			}
		});
		initializeGui();
		setGuiToInitialValues(showNumberOfNodes);
		readGraphAttributes();
		enableRedraw = true;
	}

	public FuzzyGraph getGraph() {
		return graph;
	}

	protected boolean readGraphAttributes() {
		String edgeTransformerSelection = graph
				.getAttribute(EDGE_TRANSFORMER_SELECTION);
		if (edgeTransformerSelection != null) {
			if (edgeTransformerSelection
					.equalsIgnoreCase(EDGE_TRANSFORMER_SELECTION_BEST_EDGES)) {
				edgesBestRadioButton.setSelected(true);
				fastTransformer.addInterimTransformer(bestEdgeTransformer);
			} else if (edgeTransformerSelection
					.equalsIgnoreCase(EDGE_TRANSFORMER_SELECTION_FUZZY_EDGES)) {
				edgesFuzzyRadioButton.setSelected(true);
				fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
			}
		}
		String concurrencyTransformerActive = graph
				.getAttribute(CONCURRENCY_EDGE_TRANSFORMER_ACTIVE);
		if (concurrencyTransformerActive != null) {
			if (concurrencyTransformerActive.equals(TRUE)) {
				edgesConcurrencyActiveBox.setSelected(true);
				fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
			} else if (concurrencyTransformerActive.equals(FALSE)) {
				edgesConcurrencyActiveBox.setSelected(false);
				fastTransformer
						.removePreTransformer(concurrencyEdgeTransformer);
			}
		}
		String nodeCutoff = graph.getAttribute(NODE_CUTOFF);
		if (nodeCutoff != null) {
			double nodeThreshold = Double.parseDouble(nodeCutoff);
			nodeSignificanceSlider.setValue((int) (nodeThreshold * 1000.0));
			nodeSignificanceLabel.setText(MutableFuzzyGraph
					.format(nodeThreshold));
			fastTransformer.setThreshold(nodeThreshold);
		}
		String fuzzyEdgeRatio = graph.getAttribute(FUZZY_EDGE_RATIO);
		if (fuzzyEdgeRatio != null) {
			double fuzzyERatio = Double.parseDouble(fuzzyEdgeRatio);
			edgesFuzzyRatioSlider.setValue((int) (fuzzyERatio * 1000.0));
			edgesFuzzyRatioLabel.setText(MutableFuzzyGraph.format(fuzzyERatio));
			fuzzyEdgeTransformer.setSignificanceCorrelationRatio(fuzzyERatio);
		}
		String fuzzyEdgeCutoff = graph.getAttribute(FUZZY_EDGE_CUTOFF);
		if (fuzzyEdgeCutoff != null) {
			double fuzzyECutoff = Double.parseDouble(fuzzyEdgeCutoff);
			edgesFuzzyPercentageSlider.setValue((int) (fuzzyECutoff * 1000.0));
			edgesFuzzyPercentageLabel.setText(MutableFuzzyGraph
					.format(fuzzyECutoff));
			fuzzyEdgeTransformer.setPreservePercentage(fuzzyECutoff);
		}
		String concurrencyThreshold = graph.getAttribute(CONCURRENCY_THRESHOLD);
		if (concurrencyThreshold != null) {
			double threshold = Double.parseDouble(concurrencyThreshold);
			edgesConcurrencyThresholdSlider
					.setValue((int) (threshold * 1000.0));
			edgesConcurrencyThresholdLabel.setText(MutableFuzzyGraph
					.format(threshold));
			concurrencyEdgeTransformer.setPreserveThreshold(threshold);
		}
		String concurrencyRatio = graph.getAttribute(CONCURRENCY_RATIO);
		if (concurrencyRatio != null) {
			double ratio = Double.parseDouble(concurrencyRatio);
			edgesConcurrencyRatioSlider.setValue((int) (ratio * 1000.0));
			edgesConcurrencyRatioLabel.setText(MutableFuzzyGraph.format(ratio));
			concurrencyEdgeTransformer.setRatioThreshold(ratio);
		}
		String ignoreLoops = graph.getAttribute(EDGES_FUZZY_IGNORE_LOOPS);
		if (ignoreLoops != null) {
			if (ignoreLoops.equals(TRUE)) {
				edgesFuzzyIgnoreLoopBox.setSelected(true);
				fuzzyEdgeTransformer.setIgnoreSelfLoops(true);
			} else if (ignoreLoops.equals(FALSE)) {
				edgesFuzzyIgnoreLoopBox.setSelected(false);
				fuzzyEdgeTransformer.setIgnoreSelfLoops(false);
			}
		}
		String interpretAbsolute = graph
				.getAttribute(EDGES_FUZZY_INTERPRET_ABSOLUTE);
		if (interpretAbsolute != null) {
			if (interpretAbsolute.equals(TRUE)) {
				edgesFuzzyInterpretAbsoluteBox.setSelected(true);
				fuzzyEdgeTransformer.setInterpretPercentageAbsolute(true);
			} else if (interpretAbsolute.equals(FALSE)) {
				edgesFuzzyInterpretAbsoluteBox.setSelected(false);
				fuzzyEdgeTransformer.setInterpretPercentageAbsolute(false);
			}
		}
		return false;
	}

	protected void initializeGui() {
		// derive standard control element font
		this.smallFont = this.getFont().deriveFont(11f);
		// root panel
		rootPanel = new JPanel();
		rootPanel.setBorder(BorderFactory.createEmptyBorder());
		rootPanel.setBackground(new Color(100, 100, 100));
		rootPanel.setLayout(new BorderLayout());
		// upper node significance panel
		JPanel upperControlPanel = new JPanel();
		upperControlPanel
				.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		upperControlPanel.setBackground(COLOR_BG2);
		upperControlPanel.setOpaque(true);
		upperControlPanel.setLayout(new BorderLayout());
		JLabel nodeSigSliderLabel = new JLabel("Significance cutoff");
		nodeSigSliderLabel.setFont(this.smallFont);
		nodeSigSliderLabel.setOpaque(false);
		nodeSigSliderLabel.setForeground(COLOR_FG);
		centerHorizontally(nodeSigSliderLabel);
		upperControlPanel.add(nodeSigSliderLabel, BorderLayout.NORTH);
		nodeSignificanceLabel = new JLabel("0.000");
		nodeSignificanceLabel.setOpaque(false);
		nodeSignificanceLabel.setForeground(COLOR_FG);
		nodeSignificanceLabel.setFont(this.smallFont);
		centerHorizontally(nodeSignificanceLabel);
		upperControlPanel.add(packVerticallyCentered(nodeSignificanceLabel, 50,
				20), BorderLayout.SOUTH);
		nodeSignificanceSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);
		nodeSignificanceSlider
				.setUI(new SlickerSliderUI(nodeSignificanceSlider));
		nodeSignificanceSlider.addChangeListener(this);
		nodeSignificanceSlider.setOpaque(false);
		nodeSignificanceSlider
				.setToolTipText("<html>The lower this value, the more<br>"
						+ "events are shown as single activities,<br>"
						+ "increasing the detail and complexity<br>"
						+ "of the model.</html>");
		upperControlPanel.add(nodeSignificanceSlider, BorderLayout.CENTER);
		// lower edge transformer panel
		JPanel lowerControlPanel = new JPanel();
		lowerControlPanel
				.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		lowerControlPanel.setBackground(COLOR_BG2);
		lowerControlPanel.setOpaque(true);
		lowerControlPanel.setLayout(new BorderLayout());
		// lower header panel (radio buttons etc.)
		JPanel lowerHeaderPanel = new JPanel();
		lowerHeaderPanel.setOpaque(false);
		lowerHeaderPanel.setLayout(new BoxLayout(lowerHeaderPanel,
				BoxLayout.Y_AXIS));
		JLabel lowerHeaderLabel = new JLabel("Edge transformer");
		lowerHeaderLabel.setOpaque(false);
		lowerHeaderLabel.setForeground(COLOR_FG);
		lowerHeaderLabel.setFont(this.smallFont);
		// centerHorizontally(lowerHeaderLabel);
		edgesBestRadioButton = new JRadioButton("Best edges");
		edgesBestRadioButton.setUI(new SlickerRadioButtonUI());
		edgesBestRadioButton.setFont(this.smallFont);
		edgesBestRadioButton.setBorder(BorderFactory.createEmptyBorder(2, 10,
				2, 2));
		edgesBestRadioButton.setOpaque(false);
		edgesBestRadioButton.setForeground(COLOR_FG);
		edgesBestRadioButton.setAlignmentX(JRadioButton.LEFT_ALIGNMENT);
		edgesBestRadioButton.setHorizontalAlignment(JRadioButton.LEFT);
		edgesBestRadioButton.addItemListener(this);
		edgesBestRadioButton
				.setToolTipText("<html>Activates the 'Best edges'<br>"
						+ "edge filtering strategy, which<br>"
						+ "preserves for each node the two most<br>"
						+ "significant connections.</html>");
		edgesFuzzyRadioButton = new JRadioButton("Fuzzy edges");
		edgesFuzzyRadioButton.setUI(new SlickerRadioButtonUI());
		edgesFuzzyRadioButton.setFont(this.smallFont);
		edgesFuzzyRadioButton.setBorder(BorderFactory.createEmptyBorder(2, 10,
				2, 2));
		edgesFuzzyRadioButton.setOpaque(false);
		edgesFuzzyRadioButton.setForeground(COLOR_FG);
		edgesFuzzyRadioButton.setAlignmentX(JRadioButton.LEFT_ALIGNMENT);
		edgesFuzzyRadioButton.setHorizontalAlignment(JRadioButton.LEFT);
		edgesFuzzyRadioButton.addItemListener(this);
		edgesFuzzyRadioButton
				.setToolTipText("<html>Activates the 'Fuzzy edges'<br>"
						+ "edge filtering strategy, which is<br>"
						+ "based on the utility value of each<br>"
						+ "edge for any node.</html>");
		ButtonGroup radioEdgesGroup = new ButtonGroup();
		radioEdgesGroup.add(edgesBestRadioButton);
		radioEdgesGroup.add(edgesFuzzyRadioButton);
		lowerHeaderPanel.add(lowerHeaderLabel);
		lowerHeaderPanel.add(Box.createVerticalStrut(2));
		lowerHeaderPanel.add(edgesBestRadioButton);
		lowerHeaderPanel.add(edgesFuzzyRadioButton);
		lowerHeaderPanel.add(Box.createVerticalStrut(5));
		// lower slider parent panel
		JPanel lowerSliderPanel = new JPanel();
		lowerSliderPanel.setOpaque(false);
		lowerSliderPanel.setLayout(new BoxLayout(lowerSliderPanel,
				BoxLayout.X_AXIS));
		// lower ratio slider panel
		JPanel fuzzyRatioPanel = new JPanel();
		fuzzyRatioPanel.setOpaque(false);
		fuzzyRatioPanel.setLayout(new BorderLayout());
		JLabel fuzzyRatioHeader = new JLabel("Utility rt.");
		fuzzyRatioHeader.setFont(this.smallFont);
		fuzzyRatioHeader.setOpaque(false);
		fuzzyRatioHeader.setForeground(COLOR_FG);
		centerHorizontally(fuzzyRatioHeader);
		edgesFuzzyRatioSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);
		edgesFuzzyRatioSlider.setUI(new SlickerSliderUI(edgesFuzzyRatioSlider));
		edgesFuzzyRatioSlider.setOpaque(false);
		edgesFuzzyRatioSlider.addChangeListener(this);
		edgesFuzzyRatioSlider
				.setToolTipText("<html>Controls the utility ratio used<br>"
						+ "for edge filtering. A higher value will<br>"
						+ "give more preference to edges' significance,<br>"
						+ "lower value prefers correlation.</html>");
		edgesFuzzyRatioLabel = new JLabel("0.000");
		centerHorizontally(edgesFuzzyRatioLabel);
		edgesFuzzyRatioLabel.setSize(new Dimension(100, 25));
		edgesFuzzyRatioLabel.setForeground(COLOR_FG);
		edgesFuzzyRatioLabel.setFont(this.smallFont);
		fuzzyRatioPanel.add(packVerticallyCentered(fuzzyRatioHeader, 60, 20),
				BorderLayout.NORTH);
		fuzzyRatioPanel.add(edgesFuzzyRatioSlider, BorderLayout.CENTER);
		fuzzyRatioPanel.add(
				packVerticallyCentered(edgesFuzzyRatioLabel, 40, 20),
				BorderLayout.SOUTH);
		// lower percentage slider panel
		JPanel fuzzyPercentagePanel = new JPanel();
		fuzzyPercentagePanel.setOpaque(false);
		fuzzyPercentagePanel.setLayout(new BorderLayout());
		JLabel fuzzyPercentageHeader = new JLabel("Cutoff");
		fuzzyPercentageHeader.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		fuzzyPercentageHeader.setOpaque(false);
		fuzzyPercentageHeader.setForeground(COLOR_FG);
		fuzzyPercentageHeader.setFont(this.smallFont);
		centerHorizontally(fuzzyPercentageHeader);
		edgesFuzzyPercentageSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);
		edgesFuzzyPercentageSlider.setUI(new SlickerSliderUI(
				edgesFuzzyPercentageSlider));
		edgesFuzzyPercentageSlider.setOpaque(false);
		edgesFuzzyPercentageSlider.addChangeListener(this);
		edgesFuzzyPercentageSlider
				.setToolTipText("<html>Determines the minimal utility for<br>"
						+ "an edge to be included, with a larger value<br>"
						+ "allowing more edges to be displayed, thus<br>"
						+ "increasing the detail of the model.</html>");
		edgesFuzzyPercentageLabel = new JLabel("0.000");
		edgesFuzzyPercentageLabel.setForeground(COLOR_FG);
		edgesFuzzyPercentageLabel.setSize(new Dimension(100, 25));
		edgesFuzzyPercentageLabel.setFont(this.smallFont);
		centerHorizontally(edgesFuzzyPercentageLabel);
		fuzzyPercentagePanel.add(packVerticallyCentered(fuzzyPercentageHeader,
				40, 20), BorderLayout.NORTH);
		fuzzyPercentagePanel.add(edgesFuzzyPercentageSlider,
				BorderLayout.CENTER);
		fuzzyPercentagePanel.add(packVerticallyCentered(
				edgesFuzzyPercentageLabel, 40, 20), BorderLayout.SOUTH);
		// assemble lower slider panel
		lowerSliderPanel.add(fuzzyPercentagePanel);
		lowerSliderPanel.add(fuzzyRatioPanel);
		// assemble check box panel
		JPanel lowerSettingsPanel = new JPanel();
		lowerSettingsPanel.setOpaque(false);
		lowerSettingsPanel.setLayout(new BoxLayout(lowerSettingsPanel,
				BoxLayout.Y_AXIS));
		edgesFuzzyIgnoreLoopBox = new JCheckBox("ignore self-loops");
		edgesFuzzyIgnoreLoopBox.setUI(new SlickerCheckBoxUI());
		edgesFuzzyIgnoreLoopBox.setOpaque(false);
		edgesFuzzyIgnoreLoopBox.setForeground(COLOR_FG);
		edgesFuzzyIgnoreLoopBox.setFont(this.smallFont);
		edgesFuzzyIgnoreLoopBox.addItemListener(this);
		edgesFuzzyIgnoreLoopBox
				.setToolTipText("<html>If active, length-1-loops (i.e.,<br>"
						+ "repeptitions of one event) will not be,<br>"
						+ "taken into account when filtering edges.</html>");
		edgesFuzzyInterpretAbsoluteBox = new JCheckBox("interpret absolute");
		edgesFuzzyInterpretAbsoluteBox.setUI(new SlickerCheckBoxUI());
		edgesFuzzyInterpretAbsoluteBox.setOpaque(false);
		edgesFuzzyInterpretAbsoluteBox.setForeground(COLOR_FG);
		edgesFuzzyInterpretAbsoluteBox.setFont(this.smallFont);
		edgesFuzzyInterpretAbsoluteBox.addItemListener(this);
		edgesFuzzyInterpretAbsoluteBox
				.setToolTipText("<html>If active, all edges' utility value<br>"
						+ "must exceed the cutoff globally, i.e. in an<br>"
						+ "absolute way, rather than locally, i.e. in a<br>"
						+ "relative way.</html>");
		lowerSettingsPanel.add(edgesFuzzyIgnoreLoopBox);
		lowerSettingsPanel.add(edgesFuzzyInterpretAbsoluteBox);
		// assemble lower control panel
		lowerControlPanel.add(lowerHeaderPanel, BorderLayout.NORTH);
		lowerControlPanel.add(lowerSliderPanel, BorderLayout.CENTER);
		lowerControlPanel.add(lowerSettingsPanel, BorderLayout.SOUTH);
		// concurrency edge transformer slider panel
		JPanel concurrencySliderPanel = new JPanel();
		concurrencySliderPanel.setOpaque(false);
		concurrencySliderPanel.setLayout(new BoxLayout(concurrencySliderPanel,
				BoxLayout.X_AXIS));
		// concurrency edge preserve threshold slider panel
		JPanel concurrencyPreservePanel = new JPanel();
		concurrencyPreservePanel.setOpaque(false);
		concurrencyPreservePanel.setLayout(new BorderLayout());
		JLabel concurrencyPreserveHeader = new JLabel("Preserve");
		concurrencyPreserveHeader.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		concurrencyPreserveHeader.setOpaque(false);
		concurrencyPreserveHeader.setForeground(COLOR_FG);
		concurrencyPreserveHeader.setFont(this.smallFont);
		centerHorizontally(concurrencyPreserveHeader);
		edgesConcurrencyThresholdSlider = new JSlider(JSlider.VERTICAL, 0,
				1000, 0);
		edgesConcurrencyThresholdSlider.setUI(new SlickerSliderUI(
				edgesConcurrencyThresholdSlider));
		edgesConcurrencyThresholdSlider.setOpaque(false);
		edgesConcurrencyThresholdSlider.addChangeListener(this);
		edgesConcurrencyThresholdSlider
				.setToolTipText("<html>For conflicting relations, this sets the<br>"
						+ "threshold for their relative significance which,<br>"
						+ "if not exceeded, causes the conflicting edges to<br>"
						+ "fall victim to simplification. A smaller value<br>"
						+ "allows more conflicting relations in the model.</html>");
		edgesConcurrencyThresholdLabel = new JLabel("0.000");
		edgesConcurrencyThresholdLabel.setSize(new Dimension(100, 25));
		edgesConcurrencyThresholdLabel.setForeground(COLOR_FG);
		edgesConcurrencyThresholdLabel.setFont(this.smallFont);
		centerHorizontally(edgesConcurrencyThresholdLabel);
		concurrencyPreservePanel.add(packVerticallyCentered(
				concurrencyPreserveHeader, 60, 20), BorderLayout.NORTH);
		concurrencyPreservePanel.add(edgesConcurrencyThresholdSlider,
				BorderLayout.CENTER);
		concurrencyPreservePanel.add(packVerticallyCentered(
				edgesConcurrencyThresholdLabel, 40, 20), BorderLayout.SOUTH);
		// concurrency edge ratio threshold slider panel
		JPanel concurrencyRatioPanel = new JPanel();
		concurrencyRatioPanel.setOpaque(false);
		concurrencyRatioPanel.setLayout(new BorderLayout());
		JLabel concurrencyRatioHeader = new JLabel("Ratio");
		concurrencyRatioHeader.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		concurrencyRatioHeader.setOpaque(false);
		concurrencyRatioHeader.setForeground(COLOR_FG);
		concurrencyRatioHeader.setFont(this.smallFont);
		centerHorizontally(concurrencyRatioHeader);
		edgesConcurrencyRatioSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);
		edgesConcurrencyRatioSlider.setUI(new SlickerSliderUI(
				edgesConcurrencyRatioSlider));
		edgesConcurrencyRatioSlider.setOpaque(false);
		edgesConcurrencyRatioSlider.addChangeListener(this);
		edgesConcurrencyRatioSlider
				.setToolTipText("<html>For conflicting relations which have fallen<br>"
						+ "victim to simplification, this determines ratio<br>"
						+ "threshold. A lower value prefers sequentialization of<br>"
						+ "conflicting relations, a higher value tends to<br>"
						+ "interpret them as being scheduled concurrently.</html>");
		edgesConcurrencyRatioLabel = new JLabel("0.000");
		edgesConcurrencyRatioLabel.setSize(new Dimension(100, 25));
		edgesConcurrencyRatioLabel.setForeground(COLOR_FG);
		edgesConcurrencyRatioLabel.setFont(this.smallFont);
		centerHorizontally(edgesConcurrencyRatioLabel);
		concurrencyRatioPanel.add(packVerticallyCentered(
				concurrencyRatioHeader, 60, 20), BorderLayout.NORTH);
		concurrencyRatioPanel.add(edgesConcurrencyRatioSlider,
				BorderLayout.CENTER);
		concurrencyRatioPanel.add(packVerticallyCentered(
				edgesConcurrencyRatioLabel, 40, 20), BorderLayout.SOUTH);
		// assemble concurrency slider panel
		concurrencySliderPanel.add(concurrencyPreservePanel);
		concurrencySliderPanel.add(concurrencyRatioPanel);
		// setup concurrency parent panel
		JPanel concurrencyParentPanel = new JPanel();
		concurrencyParentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5,
				5, 5));
		concurrencyParentPanel.setBackground(COLOR_BG2);
		concurrencyParentPanel.setOpaque(true);
		concurrencyParentPanel.setLayout(new BorderLayout());
		edgesConcurrencyActiveBox = new JCheckBox("Filter concurrency");
		edgesConcurrencyActiveBox.setUI(new SlickerCheckBoxUI());
		edgesConcurrencyActiveBox.setOpaque(false);
		edgesConcurrencyActiveBox.setForeground(COLOR_FG);
		edgesConcurrencyActiveBox.setFont(this.smallFont);
		edgesConcurrencyActiveBox.addItemListener(this);
		edgesConcurrencyActiveBox
				.setToolTipText("<html>This control can be used to switch off<br>"
						+ "concurrency filtering in the model.</html>");
		JPanel edgesConcurrencyHeaderPanel = new JPanel();
		edgesConcurrencyHeaderPanel.setLayout(new BoxLayout(
				edgesConcurrencyHeaderPanel, BoxLayout.Y_AXIS));
		edgesConcurrencyHeaderPanel.setOpaque(false);
		edgesConcurrencyHeaderPanel.setBorder(BorderFactory.createEmptyBorder(
				5, 10, 20, 10));
		edgesConcurrencyHeaderPanel.add(edgesConcurrencyActiveBox);
		edgesConcurrencyHeaderPanel.add(Box.createVerticalGlue());
		concurrencyParentPanel.add(edgesConcurrencyHeaderPanel,
				BorderLayout.NORTH);
		concurrencyParentPanel.add(concurrencySliderPanel, BorderLayout.CENTER);
		// assemble slick tab pane
		StackedCardsTabbedPane tabPane = new StackedCardsTabbedPane();
		tabPane.addTab("Concurrency filter", concurrencyParentPanel);
		tabPane.addTab("Edge filter", lowerControlPanel);
		tabPane.addTab("Node filter", upperControlPanel);
		tabPane.setActive(2);
		tabPane.setMinimumSize(new Dimension(190, 220));
		tabPane.setMaximumSize(new Dimension(190, 10000));
		tabPane.setPreferredSize(new Dimension(190, 10000));
		tabPane.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		replayGauge = new LedGauge(
				"Log conformance",
				"This amount of events in the log could\nbe replayed successfully.\n(click for details)",
				2);
		replayGauge.setMinimumSize(new Dimension(26, 20));
		replayGauge.setMaximumSize(new Dimension(26, 10000));
		replayGauge.setPreferredSize(new Dimension(26, 10000));
		MouseListener replayListener = new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				if (replayView != null || enableRedraw == false) {
					return; // ignore
				}
				replayView = new ReplayView(replay);
				ActionListener closeListener = new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						rootPanel.remove(replayView);
						rootPanel.add(graphPanel, BorderLayout.CENTER);
						sidePanel.setCanvasBg(Color.WHITE);
						rootPanel.revalidate();
						rootPanel.repaint();
						replayView = null;
					}
				};
				replayView.setCloseActionListener(closeListener);
				if (detailView != null) {
					rootPanel.remove(detailView);
					detailView = null;
				} else {
					rootPanel.remove(graphPanel);
				}
				rootPanel.add(replayView, BorderLayout.CENTER);
				sidePanel.setCanvasBg(new Color(30, 30, 30));
				rootPanel.revalidate();
				rootPanel.repaint();
			}

			public void mouseEntered(MouseEvent arg0) { /* ignore */
			}

			public void mouseExited(MouseEvent arg0) { /* ignore */
			}

			public void mousePressed(MouseEvent arg0) { /* ignore */
			}

			public void mouseReleased(MouseEvent arg0) { /* ignore */
			}
		};
		replayGauge.addMouseListener(replayListener);
		detailGauge = new LedGauge(
				"Model detail",
				"All visible nodes in the model represent this\namount of overall significance.\n(click for details)",
				1);
		detailGauge.setMinimumSize(new Dimension(26, 20));
		detailGauge.setMaximumSize(new Dimension(26, 10000));
		detailGauge.setPreferredSize(new Dimension(26, 10000));
		MouseListener detailListener = new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				if (detailView != null || enableRedraw == false) {
					return; // ignore
				}
				detailView = new DetailView(detail);
				ActionListener closeListener = new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						rootPanel.remove(detailView);
						rootPanel.add(graphPanel, BorderLayout.CENTER);
						sidePanel.setCanvasBg(Color.WHITE);
						rootPanel.revalidate();
						rootPanel.repaint();
						detailView = null;
					}
				};
				detailView.setCloseActionListener(closeListener);
				if (replayView != null) {
					rootPanel.remove(replayView);
					replayView = null;
				} else {
					rootPanel.remove(graphPanel);
				}
				replayView = null;
				rootPanel.add(detailView, BorderLayout.CENTER);
				sidePanel.setCanvasBg(new Color(30, 30, 30));
				rootPanel.revalidate();
				rootPanel.repaint();
			}

			public void mouseEntered(MouseEvent arg0) { /* ignore */
			}

			public void mouseExited(MouseEvent arg0) { /* ignore */
			}

			public void mousePressed(MouseEvent arg0) { /* ignore */
			}

			public void mouseReleased(MouseEvent arg0) { /* ignore */
			}
		};
		detailGauge.addMouseListener(detailListener);
		InspectorButton detailButton = new InspectorButton();
		detailButton.setToolTipText("click to show model detail inspector");
		detailButton.addMouseListener(detailListener);
		detailButton.setAlignmentX(CENTER_ALIGNMENT);
		detailButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		detailButton.setMinimumSize(new Dimension(20, 20));
		InspectorButton replayButton = new InspectorButton();
		replayButton.setToolTipText("click to show log replay inspector");
		replayButton.addMouseListener(replayListener);
		replayButton.setAlignmentX(CENTER_ALIGNMENT);
		replayButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		replayButton.setMinimumSize(new Dimension(20, 20));
		JPanel replayPanel = new JPanel();
		replayPanel.setLayout(new BoxLayout(replayPanel, BoxLayout.Y_AXIS));
		replayPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 2));
		replayPanel.setOpaque(false);
		replayPanel.add(detailGauge);
		replayPanel.add(detailButton);
		replayPanel.add(Box.createVerticalStrut(5));
		replayPanel.add(replayGauge);
		replayPanel.add(replayButton);
		JPanel rightPanel = new JPanel();
		rightPanel.setBorder(BorderFactory.createEmptyBorder());
		rightPanel.setLayout(new BorderLayout());
		rightPanel.setOpaque(false);
		rightPanel.add(tabPane, BorderLayout.CENTER);
		if (log != null) {
			rightPanel.add(replayPanel, BorderLayout.WEST);
		}
		sidePanel = new SidePanel();
		rootPanel.add(sidePanel, BorderLayout.WEST);
		rootPanel.add(rightPanel, BorderLayout.EAST);
		graphPanel = new GGGraphView();
		rootPanel.add(graphPanel, BorderLayout.CENTER);
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.add(rootPanel, BorderLayout.CENTER);
	}

	protected JPanel packVerticallyCentered(JComponent component, int width,
			int height) {
		JPanel boxed = new JPanel();
		boxed.setLayout(new BoxLayout(boxed, BoxLayout.X_AXIS));
		boxed.setBorder(BorderFactory.createEmptyBorder());
		boxed.setOpaque(false);
		Dimension dim = new Dimension(width, height);
		component.setMinimumSize(dim);
		component.setMaximumSize(dim);
		component.setPreferredSize(dim);
		component.setSize(dim);
		boxed.add(Box.createHorizontalGlue());
		boxed.add(component);
		boxed.add(Box.createHorizontalGlue());
		return boxed;
	}

	protected void setGuiToInitialValues(int showingNumberOfNodes) {

		if (showingNumberOfNodes > 0) {
			double nodeThreshold = graph
					.getThresholdShowingPrimitives(showingNumberOfNodes)
					- graph.getMinimalNodeSignificance();
			nodeThreshold = nodeThreshold
					/ (1.0 - graph.getMinimalNodeSignificance());
			nodeSignificanceSlider.setValue((int) (nodeThreshold * 1000.0));
			fastTransformer.setThreshold(nodeThreshold);
			edgesFuzzyRadioButton.setSelected(true);
			fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
			edgesFuzzyRatioSlider.setValue(750);
			edgesFuzzyRatioLabel.setText("0.750");
			fuzzyEdgeTransformer.setSignificanceCorrelationRatio(0.75);
			edgesFuzzyPercentageSlider.setValue(200);
			edgesFuzzyPercentageLabel.setText("0.200");
			fuzzyEdgeTransformer.setPreservePercentage(0.2);
			edgesFuzzyIgnoreLoopBox.setSelected(true);
			fuzzyEdgeTransformer.setIgnoreSelfLoops(true);
			edgesFuzzyInterpretAbsoluteBox.setSelected(false);
			fuzzyEdgeTransformer.setInterpretPercentageAbsolute(false);
			edgesConcurrencyActiveBox.setSelected(true);
			fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
			edgesConcurrencyThresholdSlider.setValue(600);
			edgesConcurrencyThresholdLabel.setText("0.600");
			concurrencyEdgeTransformer.setPreserveThreshold(0.6);
			edgesConcurrencyRatioSlider.setValue(700);
			edgesConcurrencyRatioLabel.setText("0.700");
			concurrencyEdgeTransformer.setRatioThreshold(0.7);
		} else {
			nodeSignificanceSlider
					.setValue(nodeSignificanceSlider.getMinimum());
			fastTransformer.setThreshold(0.0);
			edgesFuzzyRadioButton.setSelected(true);
			fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
			edgesFuzzyRatioSlider.setValue(750);
			edgesFuzzyRatioLabel.setText("0.750");
			fuzzyEdgeTransformer.setSignificanceCorrelationRatio(0.75);
			edgesFuzzyPercentageSlider.setValue(1000);
			edgesFuzzyPercentageLabel.setText("1.0");
			fuzzyEdgeTransformer.setPreservePercentage(1.0);
			edgesFuzzyIgnoreLoopBox.setSelected(true);
			fuzzyEdgeTransformer.setIgnoreSelfLoops(true);
			edgesFuzzyInterpretAbsoluteBox.setSelected(false);
			fuzzyEdgeTransformer.setInterpretPercentageAbsolute(false);
			edgesConcurrencyActiveBox.setSelected(true);
			fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
			edgesConcurrencyThresholdSlider.setValue(1000);
			edgesConcurrencyThresholdLabel.setText("1.0");
			concurrencyEdgeTransformer.setPreserveThreshold(1.0);
			edgesConcurrencyRatioSlider.setValue(700);
			edgesConcurrencyRatioLabel.setText("0.700");
			concurrencyEdgeTransformer.setRatioThreshold(0.7);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject fuzzyGraphObject = new ProvidedObject("Fuzzy Graph",
				new Object[] { (FuzzyGraph) graph });
		if (log != null) {
			LogReader filteredLog = null;
			try {
				// create projection filter, on top of the log's original filter
				FuzzyGraphProjectionFilter projectionFilter = new FuzzyGraphProjectionFilter(
						graph);
				projectionFilter.setLowLevelFilter(log.getLogFilter());
				filteredLog = LogReaderFactory.createInstance(projectionFilter,
						log);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			ProvidedObject filteredLogObject = new ProvidedObject(
					"Filtered log (projected on fuzzy graph)",
					new Object[] { filteredLog });
			return new ProvidedObject[] { fuzzyGraphObject, filteredLogObject };
		} else {
			return new ProvidedObject[] { fuzzyGraphObject };
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource() == nodeSignificanceSlider) {
			updateNodeSignificanceSlider();
		} else if (evt.getSource() == edgesFuzzyRatioSlider) {
			updateFuzzyEdgeRatioSlider();
		} else if (evt.getSource() == edgesFuzzyPercentageSlider) {
			updateFuzzyEdgePercentageSlider();
		} else if (evt.getSource() == edgesBestRadioButton
				|| evt.getSource() == edgesFuzzyRadioButton) {
			updateEdgeTransformerSelection();
		} else if (evt.getSource() == edgesConcurrencyThresholdSlider) {
			updateConcurrencyThresholdSlider();
		} else if (evt.getSource() == edgesConcurrencyRatioSlider) {
			updateConcurrencyRatioSlider();
		}
	}

	public void redrawGraph() {
		if (enableRedraw == false) {
			return; // ignore
		}
		updateGraphAttributesFromUI();
		setGuiEnabled(false);
		// setting progress view
		if (replayView != null) {
			rootPanel.remove(replayView);
			replayView = null;
			rootPanel.add(graphPanel, BorderLayout.CENTER);
		}
		if (detailView != null) {
			rootPanel.remove(detailView);
			detailView = null;
			rootPanel.add(graphPanel, BorderLayout.CENTER);
		}
		sidePanel.setVisible(false);
		rootPanel.revalidate();
		final Thread updateThread = new Thread() {
			public void run() {
				graph.initializeGraph();
				fastTransformer.transform(graph);
				graphPanel.setGraph(new GGAdapter(graph), new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						sidePanel.setVisible(true);
						if (log != null) {
							repaint();
							// replay log in new graph to determine metrics
							// values
							Message.add("Starting fuzzy graph replay...",
									Message.NORMAL);
							long start = System.currentTimeMillis();
							try {
								replayGauge.setValid(false);
								detailGauge.setValid(false);
								detail = new FuzzyDetailAnalysis(graph);
								detailGauge.setCoverage(detail.getDetail());
								replay = new FuzzyReplay(graph, log,
										replayGauge);
								Message
										.add(
												"...replayed fuzzy graph in "
														+ ((System
																.currentTimeMillis() - start) / 1000)
														+ " seconds.",
												Message.NORMAL);
								Message.add("Measured fuzzy conformance is "
										+ replay.getValue(), Message.NORMAL);
								replayGauge.setCoverage(replay.getValue());
							} catch (IOException ioe) {
								// no way to fix this here...
								Message
										.add(
												"Fatal error in Fuzzy Miner: please check your STDERR output for stack trace!",
												Message.ERROR);
								ioe.printStackTrace();
							} finally {
								setGuiEnabled(true);
							}
						} else {
							// no log to replay
							setGuiEnabled(true);
						}
					}
				});
			}
		};
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// start replay in separate thread
				updateThread.start();
			}
		});
	}

	public void setGuiEnabled(boolean enabled) {
		enableRedraw = enabled;
		edgesBestRadioButton.setEnabled(enabled);
		edgesFuzzyRadioButton.setEnabled(enabled);
		nodeSignificanceSlider.setEnabled(enabled);
		edgesFuzzyRatioSlider.setEnabled(enabled);
		edgesFuzzyPercentageSlider.setEnabled(enabled);
		edgesConcurrencyThresholdSlider.setEnabled(enabled);
		edgesConcurrencyRatioSlider.setEnabled(enabled);
		edgesFuzzyIgnoreLoopBox.setEnabled(enabled);
		edgesFuzzyInterpretAbsoluteBox.setEnabled(enabled);
		edgesConcurrencyActiveBox.setEnabled(enabled);
	}

	protected void updateEdgeTransformerSelection() {
		if (edgesBestRadioButton.isSelected() == true) {
			setFuzzyEdgeControlsEnabled(false);
			fastTransformer.removeInterimTransformer(fuzzyEdgeTransformer);
			fastTransformer.addInterimTransformer(bestEdgeTransformer);
			redrawGraph();
		} else if (edgesFuzzyRadioButton.isSelected() == true) {
			setFuzzyEdgeControlsEnabled(true);
			fastTransformer.removeInterimTransformer(bestEdgeTransformer);
			fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
			redrawGraph();
		}
	}

	protected void setFuzzyEdgeControlsEnabled(boolean enabled) {
		edgesFuzzyRatioSlider.setEnabled(enabled);
		edgesFuzzyPercentageSlider.setEnabled(enabled);
		edgesFuzzyIgnoreLoopBox.setEnabled(enabled);
		edgesFuzzyInterpretAbsoluteBox.setEnabled(enabled);
	}

	protected void setConcurrencyEdgeTransformerActive(boolean active) {
		if (active == true) {
			fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
		} else {
			fastTransformer.removePreTransformer(concurrencyEdgeTransformer);
		}
		edgesConcurrencyRatioSlider.setEnabled(active);
		edgesConcurrencyThresholdSlider.setEnabled(active);
		redrawGraph();
	}

	protected void updateNodeSignificanceSlider() {
		double value = getNodeThresholdFromSlider();
		nodeSignificanceLabel.setText(MutableFuzzyGraph.format(value));
		if (nodeSignificanceSlider.getValueIsAdjusting() == false) {
			fastTransformer.setThreshold(value);
			redrawGraph();
		}
	}

	protected void updateFuzzyEdgeRatioSlider() {
		double value = getFuzzyEdgeRatioFromSlider();
		edgesFuzzyRatioLabel.setText(MutableFuzzyGraph.format(value));
		if (edgesFuzzyRatioSlider.getValueIsAdjusting() == false) {
			fuzzyEdgeTransformer.setSignificanceCorrelationRatio(value);
			redrawGraph();
		}
	}

	protected void updateFuzzyEdgePercentageSlider() {
		double value = getFuzzyEdgePercentageFromSlider();
		edgesFuzzyPercentageLabel.setText(MutableFuzzyGraph.format(value));
		if (edgesFuzzyPercentageSlider.getValueIsAdjusting() == false) {
			fuzzyEdgeTransformer.setPreservePercentage(value);
			redrawGraph();
		}
	}

	protected void updateConcurrencyThresholdSlider() {
		double value = getConcurrencyThresholdFromSlider();
		edgesConcurrencyThresholdLabel.setText(MutableFuzzyGraph.format(value));
		if (edgesConcurrencyThresholdSlider.getValueIsAdjusting() == false) {
			concurrencyEdgeTransformer.setPreserveThreshold(value);
			redrawGraph();
		}
	}

	protected void updateConcurrencyRatioSlider() {
		double value = getConcurrencyRatioFromSlider();
		edgesConcurrencyRatioLabel.setText(MutableFuzzyGraph.format(value));
		if (edgesConcurrencyRatioSlider.getValueIsAdjusting() == false) {
			concurrencyEdgeTransformer.setRatioThreshold(value);
			redrawGraph();
		}
	}

	protected double getNodeThresholdFromSlider() {
		double threshold = (double) nodeSignificanceSlider.getValue() / 1000.0;
		// normalize threshold to minimal node frequency
		threshold = ((1.0 - graph.getMinimalNodeSignificance()) * threshold)
				+ graph.getMinimalNodeSignificance();
		return threshold;
	}

	protected double getFuzzyEdgeRatioFromSlider() {
		return (double) edgesFuzzyRatioSlider.getValue() / 1000.0;
	}

	protected double getFuzzyEdgePercentageFromSlider() {
		return (double) edgesFuzzyPercentageSlider.getValue() / 1000.0;
	}

	protected double getConcurrencyThresholdFromSlider() {
		double value = Math.pow(((double) edgesConcurrencyThresholdSlider
				.getValue() / 1000.0), 4.0);
		return value;
	}

	protected double getConcurrencyRatioFromSlider() {
		return (double) edgesConcurrencyRatioSlider.getValue() / 1000.0;
	}

	protected void updateGraphAttributesFromUI() {
		if (edgesFuzzyIgnoreLoopBox.isSelected()) {
			graph.setAttribute(EDGES_FUZZY_IGNORE_LOOPS, TRUE);
		} else {
			graph.setAttribute(EDGES_FUZZY_IGNORE_LOOPS, FALSE);
		}
		if (edgesFuzzyInterpretAbsoluteBox.isSelected()) {
			graph.setAttribute(EDGES_FUZZY_INTERPRET_ABSOLUTE, TRUE);
		} else {
			graph.setAttribute(EDGES_FUZZY_INTERPRET_ABSOLUTE, FALSE);
		}
		if (edgesConcurrencyActiveBox.isSelected()) {
			graph.setAttribute(CONCURRENCY_EDGE_TRANSFORMER_ACTIVE, TRUE);
		} else {
			graph.setAttribute(CONCURRENCY_EDGE_TRANSFORMER_ACTIVE, FALSE);
		}
		graph.setAttribute(CONCURRENCY_RATIO, Double
				.toString(getConcurrencyRatioFromSlider()));
		graph.setAttribute(CONCURRENCY_THRESHOLD, Double
				.toString(getConcurrencyThresholdFromSlider()));
		graph.setAttribute(FUZZY_EDGE_CUTOFF, Double
				.toString(getFuzzyEdgePercentageFromSlider()));
		graph.setAttribute(FUZZY_EDGE_RATIO, Double
				.toString(getFuzzyEdgeRatioFromSlider()));
		graph.setAttribute(NODE_CUTOFF, Double
				.toString(getNodeThresholdFromSlider()));
		if (edgesBestRadioButton.isSelected() == true) {
			graph.setAttribute(EDGE_TRANSFORMER_SELECTION,
					EDGE_TRANSFORMER_SELECTION_BEST_EDGES);
		} else if (edgesFuzzyRadioButton.isSelected() == true) {
			graph.setAttribute(EDGE_TRANSFORMER_SELECTION,
					EDGE_TRANSFORMER_SELECTION_FUZZY_EDGES);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() == edgesFuzzyIgnoreLoopBox) {
			boolean enabled = edgesFuzzyIgnoreLoopBox.isSelected();
			this.fuzzyEdgeTransformer.setIgnoreSelfLoops(enabled);
			redrawGraph();
		} else if (evt.getSource() == edgesFuzzyInterpretAbsoluteBox) {
			boolean enabled = edgesFuzzyInterpretAbsoluteBox.isSelected();
			this.fuzzyEdgeTransformer.setInterpretPercentageAbsolute(enabled);
			redrawGraph();
		} else if (evt.getSource() == edgesBestRadioButton
				|| evt.getSource() == edgesFuzzyRadioButton) {
			updateEdgeTransformerSelection();
		} else if (evt.getSource() == edgesConcurrencyActiveBox) {
			setConcurrencyEdgeTransformerActive(edgesConcurrencyActiveBox
					.isSelected());
		}
	}

	protected void centerHorizontally(JLabel label) {
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
	}

	protected class SidePanel extends JPanel {

		private static final long serialVersionUID = -5079687464761777691L;
		protected Color canvasBg = Color.WHITE;

		public SidePanel() {
			this.setDoubleBuffered(true);
			this.setLayout(new BorderLayout());
			this.setBorder(BorderFactory.createEmptyBorder());
			this.setOpaque(false);
			this.setMinimumSize(new Dimension(5, 200));
			this.setMaximumSize(new Dimension(5, 2000));
			this.setPreferredSize(new Dimension(5, 1000));
		}

		public void setCanvasBg(Color color) {
			canvasBg = color;
		}

		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			// paint rounded boundary of graph panel to the right
			g2d.setColor(canvasBg);
			g2d
					.fill(new RoundRectangle2D.Float(0, -0.5f, 15, getHeight(),
							8, 8));
		}
	}

}
