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
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.SmoothPanel;
import org.deckfour.slickerbox.ui.SlickerCheckBoxUI;
import org.deckfour.slickerbox.ui.SlickerSliderUI;
import org.processmining.mining.fuzzymining.metrics.Metric;
import org.processmining.mining.fuzzymining.metrics.binary.CorrelationBinaryLogMetric;
import org.processmining.mining.fuzzymining.metrics.binary.SignificanceBinaryDerivateMetric;
import org.processmining.mining.fuzzymining.metrics.binary.SignificanceBinaryLogMetric;
import org.processmining.mining.fuzzymining.metrics.trace.TraceMetric;
import org.processmining.mining.fuzzymining.metrics.unary.UnaryMetric;

/**
 * @author christian
 * 
 */
public class MetricConfigurationComponent extends SmoothPanel implements
		ChangeListener {

	protected static DecimalFormat format = new DecimalFormat("0.000");
	protected static Color COLOR_UNARY = new Color(107, 59, 50);
	protected static Color COLOR_TRACE = new Color(54, 69, 111);
	protected static Color COLOR_BINSIG = new Color(112, 104, 61);
	protected static Color COLOR_BINCOR = new Color(80, 95, 66);
	protected static Color COLOR_UNARY_HILIGHT = new Color(147, 99, 90);
	protected static Color COLOR_TRACE_HILIGHT = new Color(94, 109, 151);
	protected static Color COLOR_BINSIG_HILIGHT = new Color(152, 144, 101);
	protected static Color COLOR_BINCOR_HILIGHT = new Color(120, 135, 106);
	protected static Color COLOR_FG = new Color(190, 190, 190);

	protected JCheckBox activeBox;
	protected JCheckBox invertBox;
	protected JSlider slider;
	protected JLabel weightLabel;
	protected double rememberWeight;
	protected Metric metric;
	protected String metricType;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
	 * .JList, java.lang.Object, int, boolean, boolean)
	 */
	public MetricConfigurationComponent(Metric aMetric) {
		metric = aMetric;
		if (metric instanceof TraceMetric) {
			this.setBackground(COLOR_TRACE);
			this.setHighlight(COLOR_TRACE_HILIGHT);
			metricType = "trace metric";
		} else if (metric instanceof UnaryMetric) {
			this.setBackground(COLOR_UNARY);
			this.setHighlight(COLOR_UNARY_HILIGHT);
			metricType = "unary metric";
		} else if (metric instanceof SignificanceBinaryLogMetric
				|| metric instanceof SignificanceBinaryDerivateMetric) {
			this.setBackground(COLOR_BINSIG);
			this.setHighlight(COLOR_BINSIG_HILIGHT);
			metricType = "binary significance metric";
		} else if (metric instanceof CorrelationBinaryLogMetric) {
			this.setBackground(COLOR_BINCOR);
			this.setHighlight(COLOR_BINCOR_HILIGHT);
			metricType = "binary correlation metric";
		}
		this.setToolTipText(metric.getDescription());
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		/*
		 * this.setBorder(BorderFactory.createCompoundBorder(
		 * BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
		 * BorderFactory.createEmptyBorder(15, 15, 15, 15)) );
		 */
		this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		this.setMinimumSize(new Dimension(350, 110));
		this.setMaximumSize(new Dimension(1000, 110));
		this.setPreferredSize(new Dimension(400, 110));
		JLabel metricLabel = new JLabel(metric.getName());
		metricLabel.setOpaque(false);
		metricLabel.setForeground(new Color(240, 240, 240));
		JLabel typeLabel = new JLabel("(" + metricType + ")");
		typeLabel.setForeground(COLOR_FG);
		typeLabel.setOpaque(false);
		typeLabel.setFont(typeLabel.getFont().deriveFont(10.0f));
		activeBox = new JCheckBox("active");
		activeBox.setUI(new SlickerCheckBoxUI());
		activeBox.setOpaque(false);
		activeBox.setSelected(metric.getNormalizationMaximum() > 0.0);
		activeBox.addChangeListener(this);
		activeBox.setForeground(COLOR_FG);
		invertBox = new JCheckBox("invert");
		invertBox.setUI(new SlickerCheckBoxUI());
		invertBox.setOpaque(false);
		invertBox.setSelected(metric.getInvert());
		invertBox.addChangeListener(this);
		invertBox.setForeground(COLOR_FG);
		slider = new JSlider(JSlider.HORIZONTAL, 0, 1000, (int) (metric
				.getNormalizationMaximum() * 1000));
		slider.setUI(new SlickerSliderUI(slider));
		slider.setOpaque(false);
		slider.addChangeListener(this);
		JLabel sliderLabel = new JLabel("weight:");
		sliderLabel.setOpaque(false);
		sliderLabel.setForeground(COLOR_FG);
		weightLabel = new JLabel(format
				.format(metric.getNormalizationMaximum()));
		weightLabel.setOpaque(false);
		weightLabel.setForeground(COLOR_FG);
		JPanel upperPanel = new JPanel();
		upperPanel.setOpaque(false);
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));
		upperPanel.add(metricLabel);
		upperPanel.add(Box.createHorizontalStrut(10));
		upperPanel.add(typeLabel);
		upperPanel.add(Box.createHorizontalGlue());
		upperPanel.add(invertBox);
		upperPanel.add(Box.createHorizontalStrut(10));
		upperPanel.add(activeBox);
		JPanel lowerPanel = new JPanel();
		lowerPanel.setOpaque(false);
		lowerPanel.setLayout(new BorderLayout());
		lowerPanel.add(sliderLabel, BorderLayout.WEST);
		lowerPanel.add(slider, BorderLayout.CENTER);
		lowerPanel.add(weightLabel, BorderLayout.EAST);
		JLabel descrLabel = new JLabel(metric.getDescription());
		descrLabel.setAlignmentX(LEFT_ALIGNMENT);
		descrLabel.setForeground(COLOR_FG);
		descrLabel.setOpaque(false);
		descrLabel.setFont(descrLabel.getFont().deriveFont(9.0f));
		JPanel midPanel = new JPanel();
		midPanel.setOpaque(false);
		midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.X_AXIS));
		midPanel.add(descrLabel);
		midPanel.add(Box.createHorizontalGlue());
		this.add(upperPanel);
		this.add(Box.createVerticalStrut(4));
		this.add(midPanel);
		this.add(Box.createVerticalStrut(4));
		this.add(lowerPanel);
		this.add(Box.createVerticalGlue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == slider) {
			double value = (double) slider.getValue() / 1000.0;
			metric.setNormalizationMaximum(value);
			weightLabel.setText(format.format(value));
			repaint();
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
		if (evt.getSource() == activeBox) {
			if (activeBox.isSelected() == true) {
				slider.setEnabled(true);
				metric
						.setNormalizationMaximum((double) slider.getValue() / 1000.0);
			} else {
				slider.setEnabled(false);
				rememberWeight = metric.getNormalizationMaximum();
				metric.setNormalizationMaximum(0.0);
			}
			repaint();
		} else if (evt.getSource() == invertBox) {
			metric.setInvert(invertBox.isSelected());
			repaint();
		} else if (evt.getSource() == slider) {
			double value = (double) slider.getValue() / 1000.0;
			metric.setNormalizationMaximum(value);
			weightLabel.setText(format.format(value));
			repaint();
		}
	}

}
