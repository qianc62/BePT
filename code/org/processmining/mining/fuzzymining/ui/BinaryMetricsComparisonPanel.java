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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.util.RuntimeUtils;
import org.processmining.mining.fuzzymining.metrics.MetricsRepository;
import org.processmining.mining.fuzzymining.metrics.binary.BinaryMetric;

/**
 * @author christian
 * 
 */
public class BinaryMetricsComparisonPanel extends JPanel {

	protected static Color COLOR_BG = new Color(50, 50, 50);

	protected ArrayList<BinaryMetric> metrics;
	protected LogEvents events;
	protected JComboBox comboBox;
	protected JPanel graphPanel;

	public BinaryMetricsComparisonPanel(MetricsRepository metricsRepository) {
		this.setOpaque(false);
		events = metricsRepository.getLogReader().getLogSummary()
				.getLogEvents();
		metrics = new ArrayList<BinaryMetric>();
		metrics.add(metricsRepository.getAggregateSignificanceBinaryMetric());
		metrics.add(metricsRepository.getAggregateCorrelationBinaryMetric());
		metrics.add(metricsRepository.getAggregateCorrelationBinaryLogMetric());
		metrics
				.add(metricsRepository
						.getAggregateSignificanceBinaryLogMetric());
		metrics.addAll(metricsRepository.getBinaryLogMetrics());
		metrics.addAll(metricsRepository.getBinaryDerivateMetrics());
		// setup GUI
		this.setLayout(new BorderLayout());
		comboBox = new JComboBox(metrics.toArray());
		if (RuntimeUtils.isRunningMacOsX() == true) {
			comboBox.setOpaque(false);
		}
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				displayMetric((BinaryMetric) comboBox.getSelectedItem());
			}
		});
		JLabel metricsLabel = new JLabel("Binary metric to display: ");
		metricsLabel.setOpaque(false);
		metricsLabel.setForeground(new Color(40, 40, 40));
		JPanel binMetricsControlPanel = new JPanel();
		binMetricsControlPanel.setOpaque(false);
		binMetricsControlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5,
				5, 5));
		binMetricsControlPanel.setLayout(new BoxLayout(binMetricsControlPanel,
				BoxLayout.X_AXIS));
		binMetricsControlPanel.add(Box.createHorizontalGlue());
		binMetricsControlPanel.add(metricsLabel);
		binMetricsControlPanel.add(Box.createHorizontalStrut(10));
		binMetricsControlPanel.add(comboBox);
		binMetricsControlPanel.add(Box.createHorizontalGlue());
		this.add(binMetricsControlPanel, BorderLayout.NORTH);
		graphPanel = new RoundedPanel(10, 5, 0);
		graphPanel.setBackground(Color.BLACK);
		graphPanel.setLayout(new BorderLayout());
		this.add(graphPanel, BorderLayout.CENTER);
		displayMetric(metrics.get(0));
	}

	protected void displayMetric(BinaryMetric metric) {
		graphPanel.removeAll();
		graphPanel.add(new BinaryMetricsPanel(metric, events),
				BorderLayout.CENTER);
		this.validate();
		repaint();
	}
}
