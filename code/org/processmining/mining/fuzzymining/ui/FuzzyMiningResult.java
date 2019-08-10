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
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.deckfour.slickerbox.components.HeaderBar;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.mining.MiningResult;
import org.processmining.mining.fuzzymining.attenuation.Attenuation;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;
import org.processmining.mining.fuzzymining.metrics.Metric;
import org.processmining.mining.fuzzymining.metrics.MetricsRepository;
import org.processmining.mining.fuzzymining.metrics.binary.BinaryMetric;
import org.processmining.mining.fuzzymining.metrics.unary.UnaryMetric;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FuzzyMiningResult extends JPanel implements MiningResult, Provider {

	protected MetricsRepository metrics;
	protected FuzzyModelPanel resultPanel;

	public FuzzyMiningResult(final MetricsRepository metrics,
			final LogReader log, final Attenuation attenuation,
			final int maxRelDist) {
		this.metrics = metrics;
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder());
		HeaderBar waitHeader = new HeaderBar("Scanning fuzzy metrics");
		waitHeader.setHeight(40);
		this.add(waitHeader, BorderLayout.NORTH);
		final ProgressPanel progress = new ProgressPanel("Scanning log metrics");
		this.add(progress.getPanel(), BorderLayout.CENTER);
		revalidate();
		final Thread metricsThread = new Thread() {
			public void run() {
				long time = System.currentTimeMillis();
				try {
					metrics.apply(log, attenuation, maxRelDist, progress);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				time = System.currentTimeMillis() - time;
				Message.add("Fuzzy Miner: Building repository took " + time
						+ " ms.", Message.NORMAL);
				// test output start
				if (UISettings.getInstance().getTest() == true) {
					printTestOutput();
				}
				// complete UI
				initializeGui();
				revalidate();
			}
		};
		this.addAncestorListener(new AncestorListener() {
			protected boolean hasRun = false;

			public synchronized void ancestorAdded(AncestorEvent event) {
				if (hasRun == false) {
					hasRun = true;
					metricsThread.start();
				}
			}

			public void ancestorMoved(AncestorEvent event) {
				// ignore
			}

			public void ancestorRemoved(AncestorEvent event) {
				// ignore
			}
		});
	}

	public FuzzyMiningResult(MetricsRepository metricsRepository) {
		metrics = metricsRepository;
		initializeGui();
	}

	protected void initializeGui() {
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(100, 100, 100));
		resultPanel = new FuzzyModelPanel(metrics, 7);
		this.removeAll();
		this.add(resultPanel, BorderLayout.CENTER);
	}

	public FuzzyGraph getGraph() {
		return resultPanel.getCurrentGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getLogReader()
	 */
	public LogReader getLogReader() {
		return metrics.getLogReader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getVisualization()
	 */
	public JComponent getVisualization() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		if (resultPanel != null) {
			return resultPanel.getProvidedObjects();
		} else {
			return new ProvidedObject[] {};
		}
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
