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
package org.processmining.mining.fuzzymining;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.FlatTabbedPane;
import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerRadioButtonUI;
import org.deckfour.slickerbox.ui.SlickerSliderUI;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.UISettings;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.fuzzymining.attenuation.Attenuation;
import org.processmining.mining.fuzzymining.attenuation.LinearAttenuation;
import org.processmining.mining.fuzzymining.attenuation.NRootAttenuation;
import org.processmining.mining.fuzzymining.metrics.Metric;
import org.processmining.mining.fuzzymining.metrics.MetricsRepository;
import org.processmining.mining.fuzzymining.metrics.binary.BinaryMetric;
import org.processmining.mining.fuzzymining.metrics.unary.UnaryMetric;
import org.processmining.mining.fuzzymining.ui.AttenuationDisplayPanel;
import org.processmining.mining.fuzzymining.ui.FuzzyMiningResult;
import org.processmining.mining.fuzzymining.ui.MetricConfigurationComponent;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FuzzyMiner extends JPanel implements MiningPlugin {

	protected static final String METRICS_ALL = "show all metrics";
	protected static final String METRICS_TRACE = "show trace metrics";
	protected static final String METRICS_UNARY = "show unary significance metrics";
	protected static final String METRICS_BINSIG = "show binary significance metrics";
	protected static final String METRICS_BINCOR = "show binary correlation metrics";

	/*
	 * protected static Color COLOR_OUTER_BG = new Color(130, 130, 130);
	 * protected static Color COLOR_BG = new Color(120, 120, 120);
	 */

	protected MetricsRepository metrics;
	protected Attenuation attenuation;

	protected JComboBox metricsFilterBox;
	protected JScrollPane metricsScrollPane;
	protected JPanel metricsList;

	protected JRadioButton attLinearRadioButton;
	protected JRadioButton attNRootRadioButton;
	protected JSlider maxRelDistSlider;
	protected JSlider attNRootNSlider;
	protected AttenuationDisplayPanel attDisplayPanel;

	/**
	 * 
	 */
	public FuzzyMiner() {
		// nothing to see here, move along ;-)
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#getOptionsPanel(org.processmining
	 * .framework.log.LogSummary)
	 */
	public JPanel getOptionsPanel(LogSummary summary) {
		// setup
		this.removeAll();
		this.setLayout(new BorderLayout());
		metrics = MetricsRepository.createRepository(summary);
		// GUI setup
		metricsFilterBox = new JComboBox(new String[] { FuzzyMiner.METRICS_ALL,
				FuzzyMiner.METRICS_TRACE, FuzzyMiner.METRICS_UNARY,
				FuzzyMiner.METRICS_BINSIG, FuzzyMiner.METRICS_BINCOR });
		metricsFilterBox.setOpaque(false);
		metricsFilterBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// adjust metric list's contents to combo box filter selection
				// value
				metricsList = new JPanel();
				metricsList.setBackground(new Color(40, 40, 40));
				metricsList.setBorder(BorderFactory.createEmptyBorder());
				metricsList.setLayout(new BoxLayout(metricsList,
						BoxLayout.Y_AXIS));
				ArrayList<Metric> upData = new ArrayList<Metric>();
				String filter = (String) metricsFilterBox.getSelectedItem();
				if (filter.equals(FuzzyMiner.METRICS_ALL)) {
					upData.addAll(metrics.getTraceMetrics());
					upData.addAll(metrics.getUnaryMetrics());
					upData.addAll(metrics.getSignificanceBinaryMetrics());
					upData.addAll(metrics.getCorrelationBinaryMetrics());
				} else if (filter.equals(FuzzyMiner.METRICS_TRACE)) {
					upData.addAll(metrics.getTraceMetrics());
				} else if (filter.equals(FuzzyMiner.METRICS_UNARY)) {
					upData.addAll(metrics.getUnaryMetrics());
				} else if (filter.equals(FuzzyMiner.METRICS_BINSIG)) {
					upData.addAll(metrics.getSignificanceBinaryMetrics());
				} else if (filter.equals(FuzzyMiner.METRICS_BINCOR)) {
					upData.addAll(metrics.getCorrelationBinaryMetrics());
				}
				for (Metric metric : upData) {
					metricsList.add(new MetricConfigurationComponent(metric));
				}
				metricsScrollPane.getViewport().setView(metricsList);
			}
		});
		metricsList = new JPanel();
		metricsList.setBorder(BorderFactory.createEmptyBorder());
		metricsList.setBackground(new Color(40, 40, 40));
		metricsList.setLayout(new BoxLayout(metricsList, BoxLayout.Y_AXIS));
		ArrayList<Metric> upData = new ArrayList<Metric>();
		upData.addAll(metrics.getTraceMetrics());
		upData.addAll(metrics.getUnaryMetrics());
		upData.addAll(metrics.getSignificanceBinaryMetrics());
		upData.addAll(metrics.getCorrelationBinaryMetrics());
		for (Metric metric : upData) {
			metricsList.add(new MetricConfigurationComponent(metric));
		}
		metricsScrollPane = new JScrollPane(metricsList);
		metricsScrollPane.setBorder(BorderFactory.createEmptyBorder());
		metricsScrollPane.setOpaque(false);
		metricsScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		metricsScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		RoundedPanel metricsPanel = new RoundedPanel(10, 5, 0);
		metricsPanel.setBackground(new Color(80, 80, 80));
		metricsPanel.setLayout(new BorderLayout());
		metricsPanel.setMinimumSize(new Dimension(480, 200));
		metricsPanel.setMaximumSize(new Dimension(1000, 2000));
		metricsPanel.setPreferredSize(new Dimension(480, 300));
		JPanel filterPanel = new JPanel();
		filterPanel.setOpaque(false);
		filterPanel.setLayout(new BorderLayout());
		filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		JLabel filterLabel = new JLabel("Customize view: ");
		filterLabel.setForeground(new Color(160, 160, 160));
		filterLabel.setOpaque(false);
		filterPanel.add(filterLabel, BorderLayout.WEST);
		filterPanel.add(metricsFilterBox, BorderLayout.CENTER);
		metricsPanel.add(filterPanel, BorderLayout.NORTH);
		metricsPanel.add(metricsScrollPane, BorderLayout.CENTER);
		// right panel
		ChangeListener attUpdateChangeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				updateAttenuationPanel();
			}
		};
		attenuation = new NRootAttenuation(2.7, 5);
		attLinearRadioButton = new JRadioButton("Linear attenuation", false);
		attLinearRadioButton.setUI(new SlickerRadioButtonUI());
		attLinearRadioButton.setOpaque(false);
		attLinearRadioButton.addChangeListener(attUpdateChangeListener);
		attNRootRadioButton = new JRadioButton("Nth root with radical", true);
		attNRootRadioButton.setUI(new SlickerRadioButtonUI());
		attNRootRadioButton.setOpaque(false);
		attNRootRadioButton.addChangeListener(attUpdateChangeListener);
		ButtonGroup attButtonGroup = new ButtonGroup();
		attButtonGroup.add(attLinearRadioButton);
		attButtonGroup.add(attNRootRadioButton);
		attNRootNSlider = new JSlider(JSlider.HORIZONTAL, 1000, 4000, 2700);
		attNRootNSlider.setUI(new SlickerSliderUI(attNRootNSlider));
		attNRootNSlider.addChangeListener(attUpdateChangeListener);
		maxRelDistSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 4);
		maxRelDistSlider.setUI(new SlickerSliderUI(maxRelDistSlider));
		maxRelDistSlider.addChangeListener(attUpdateChangeListener);
		attDisplayPanel = new AttenuationDisplayPanel(attenuation, 5);
		attDisplayPanel.setBackground(Color.BLACK);
		RoundedPanel attDisplayRoundedPanel = RoundedPanel.enclose(
				attDisplayPanel, 10, 5, 0);
		attDisplayRoundedPanel.setMinimumSize(new Dimension(200, 150));
		attDisplayRoundedPanel.setPreferredSize(new Dimension(300, 200));
		attDisplayRoundedPanel.setMaximumSize(new Dimension(400, 300));
		JPanel confPanel = new JPanel();
		confPanel.setOpaque(false);
		confPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		confPanel.setLayout(new BoxLayout(confPanel, BoxLayout.Y_AXIS));
		confPanel.setMaximumSize(new Dimension(800, 500));
		confPanel.setMinimumSize(new Dimension(230, 180));
		confPanel.setPreferredSize(new Dimension(250, 180));
		JPanel maxDistPanel = new JPanel();
		maxDistPanel.setMaximumSize(new Dimension(1000, 40));
		maxDistPanel.setOpaque(false);
		maxDistPanel.setLayout(new BoxLayout(maxDistPanel, BoxLayout.X_AXIS));
		JLabel maxDistLabel = new JLabel("Maximal event distance:");
		maxDistLabel.setOpaque(false);
		maxDistPanel.add(maxDistLabel);
		maxDistPanel.add(Box.createHorizontalStrut(5));
		maxDistPanel.add(maxRelDistSlider);
		JPanel attHeaderPanel = new JPanel();
		attHeaderPanel.setMaximumSize(new Dimension(1000, 30));
		attHeaderPanel.setOpaque(false);
		attHeaderPanel
				.setLayout(new BoxLayout(attHeaderPanel, BoxLayout.X_AXIS));
		JLabel attHeaderLabel = new JLabel("Select attenuation to use:");
		attHeaderLabel.setOpaque(false);
		attHeaderPanel.add(attHeaderLabel);
		attHeaderPanel.add(Box.createHorizontalGlue());
		JPanel attNRootPanel = new JPanel();
		attNRootPanel.setMaximumSize(new Dimension(1000, 40));
		attNRootPanel.setOpaque(false);
		attNRootPanel.setLayout(new BoxLayout(attNRootPanel, BoxLayout.X_AXIS));
		attNRootPanel.add(Box.createHorizontalStrut(30));
		attNRootPanel.add(attNRootRadioButton);
		attNRootPanel.add(Box.createHorizontalStrut(5));
		attNRootPanel.add(attNRootNSlider);
		JPanel attLinearPanel = new JPanel();
		attLinearPanel.setMaximumSize(new Dimension(1000, 40));
		attLinearPanel.setOpaque(false);
		attLinearPanel
				.setLayout(new BoxLayout(attLinearPanel, BoxLayout.X_AXIS));
		attLinearPanel.add(Box.createHorizontalStrut(30));
		attLinearPanel.add(attLinearRadioButton);
		attLinearPanel.add(Box.createHorizontalGlue());
		// assemble conf. panel
		confPanel.add(maxDistPanel);
		confPanel.add(Box.createVerticalStrut(20));
		confPanel.add(attHeaderPanel);
		confPanel.add(Box.createVerticalStrut(15));
		confPanel.add(attNRootPanel);
		confPanel.add(Box.createVerticalStrut(10));
		confPanel.add(attLinearPanel);
		// assemble right hand panel
		// SmoothPanel rightSplit = new SmoothPanel();
		JPanel rightSplit = new JPanel();
		rightSplit.setOpaque(false);
		rightSplit.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		// rightSplit.setBackground(COLOR_BG);
		rightSplit.setLayout(new BorderLayout());
		rightSplit.add(attDisplayRoundedPanel, BorderLayout.WEST);
		rightSplit.add(confPanel, BorderLayout.CENTER);
		// add to root
		GradientPanel back = new GradientPanel(new Color(80, 80, 80),
				new Color(40, 40, 40));
		back.setLayout(new BorderLayout());
		back.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		FlatTabbedPane tabs = new FlatTabbedPane("Configuration", new Color(20,
				20, 20, 230), new Color(160, 160, 160, 180), new Color(220,
				220, 220, 180));
		tabs.addTab("Measurement", rightSplit);
		tabs.addTab("Metrics", metricsPanel);
		back.add(tabs, BorderLayout.CENTER);
		HeaderBar header = new HeaderBar("Fuzzy Miner");
		header.setHeight(40);
		this.add(header, BorderLayout.NORTH);
		this.add(back, BorderLayout.CENTER);
		this.setBackground(new Color(40, 40, 40));
		this.setOpaque(true);
		return this;
	}

	protected void updateAttenuationPanel() {
		int maxDistance = maxRelDistSlider.getValue();
		if (attLinearRadioButton.isSelected()) {
			attenuation = new LinearAttenuation(maxDistance, maxDistance);
		} else if (attNRootRadioButton.isSelected()) {
			double n = (double) attNRootNSlider.getValue() / 1000.0;
			attenuation = new NRootAttenuation(n, maxDistance);
		}
		attDisplayPanel.setAttenuation(attenuation);
		attDisplayPanel.setMaxDistance(maxDistance);
		attDisplayPanel.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#mine(org.processmining.framework
	 * .log.LogReader)
	 */
	public MiningResult mine(LogReader log) {
		if (isCorrectlyConfigured() == false) {
			String message = "The Fuzzy Miner needs at least one active log-based metric\n"
					+ "with a positive weight, from all major types, i.e.:\n"
					+ " - at least one log-based unary metric\n"
					+ " - at least one log-based binary significance metric\n"
					+ " - at least one log-based binary correlation metric\n"
					+ "Adjust your metrics' weight accordingly, please!";
			JOptionPane.showMessageDialog(MainUI.getInstance(), message,
					"Invalid metrics configuration!",
					JOptionPane.WARNING_MESSAGE);
			return null;
		}
		/*
		 * long time = System.currentTimeMillis(); Progress progress = new
		 * Progress("Scanning log metrics"); try { metrics.apply(log,
		 * attenuation, (Integer)maxRelDistSlider.getValue(), progress); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } time = System.currentTimeMillis() - time;
		 * Message.add("Fuzzy Miner: Building repository took " + time + " ms.",
		 * Message.NORMAL); progress.close(); // test output start
		 * if(UISettings.getInstance().getTest()==true) { printTestOutput(); }
		 * // test output end return new FuzzyMiningResult(metrics);
		 */
		return new FuzzyMiningResult(metrics, log, attenuation,
				(int) maxRelDistSlider.getValue());
	}

	protected boolean isCorrectlyConfigured() {
		boolean minOneUnary = false;
		boolean minOneBinarySig = false;
		for (UnaryMetric metric : metrics.getUnaryLogMetrics()) {
			if (metric.getNormalizationMaximum() > 0.0) {
				minOneUnary = true;
				break;
			}
		}
		if (minOneUnary == false) {
			return false;
		}
		for (BinaryMetric metric : metrics.getSignificanceBinaryLogMetrics()) {
			if (metric.getNormalizationMaximum() > 0.0) {
				minOneBinarySig = true;
				break;
			}
		}
		if (minOneBinarySig == false) {
			return false;
		}
		for (BinaryMetric metric : metrics.getCorrelationBinaryLogMetrics()) {
			if (metric.getNormalizationMaximum() > 0.0) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Fuzzy Miner";
	}

	protected void printTestOutput() {
		Message.add("<FuzzyMiner>", Message.TEST);
		Message.add("<TraceMetrics>", Message.TEST);
		for (Metric metric : metrics.getTraceMetrics()) {
			printTestOutputForMetric(metric);
		}
		Message.add("</TraceMetrics>", Message.TEST);
		Message.add("<UnaryMetrics>", Message.TEST);
		for (Metric metric : metrics.getUnaryMetrics()) {
			printTestOutputForMetric(metric);
		}
		Message.add("</UnaryMetrics>", Message.TEST);
		Message.add("<BinaryLogMetrics>", Message.TEST);
		for (Metric metric : metrics.getBinaryLogMetrics()) {
			printTestOutputForMetric(metric);
		}
		Message.add("</BinaryLogMetrics>", Message.TEST);
		Message.add("<BinaryDerivateMetrics>", Message.TEST);
		for (Metric metric : metrics.getBinaryDerivateMetrics()) {
			printTestOutputForMetric(metric);
		}
		Message.add("</BinaryDerivateMetrics>", Message.TEST);
		Message.add("</FuzzyMiner>", Message.TEST);
	}

	protected void printTestOutputForMetric(Metric metric) {
		double testVal = 0.0;
		if (metric instanceof UnaryMetric) {
			UnaryMetric unary = (UnaryMetric) metric;
			for (int i = 0; i < unary.size(); i++) {
				testVal += unary.getMeasure(i);
			}
		} else if (metric instanceof BinaryMetric) {
			BinaryMetric binary = (BinaryMetric) metric;
			for (int x = 0; x < binary.size(); x++) {
				for (int y = 0; y < binary.size(); y++) {
					testVal += binary.getMeasure(x, y);
				}
			}
		}
		Message.add("<Metric name=\"" + metric.getName() + "\">" + testVal
				+ "</Metric>", Message.TEST);
	}

}
