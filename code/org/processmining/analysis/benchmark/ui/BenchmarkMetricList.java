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
package org.processmining.analysis.benchmark.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.benchmark.metric.BenchmarkMetric;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.RuntimeUtils;

/**
 * @author christian
 * 
 */
public class BenchmarkMetricList extends RoundedPanel {

	private static final long serialVersionUID = 397418114171684339L;

	protected static Color COLOR_BG = new Color(140, 140, 140);
	protected static Color COLOR_FG = new Color(30, 30, 30);
	protected static Color ITEM_BG_ENABLED = new Color(20, 80, 20);
	protected static Color ITEM_HL_ENABLED = new Color(60, 140, 60);
	protected static Color ITEM_BG_DISABLED = new Color(80, 20, 20);
	protected static Color ITEM_HL_DISABLED = new Color(140, 60, 60);
	protected static Color ITEM_FG = new Color(200, 200, 200);

	protected BenchmarkUI benchmarkUI;
	protected List<BenchmarkMetricItem> metricItems;
	protected boolean hasReferenceLog;
	protected boolean hasReferenceModel;
	protected JPanel metricPanel;

	public BenchmarkMetricList(BenchmarkUI bUI) {
		super(10, 5, 5);
		benchmarkUI = bUI;
		metricItems = new ArrayList<BenchmarkMetricItem>();
		hasReferenceLog = false;
		hasReferenceModel = false;
		setBackground(COLOR_BG);
		setLayout(new BorderLayout());
		metricPanel = new JPanel();
		metricPanel.setOpaque(true);
		metricPanel.setBackground(COLOR_BG);
		metricPanel.setBorder(BorderFactory.createEmptyBorder());
		metricPanel.setLayout(new BoxLayout(metricPanel, BoxLayout.Y_AXIS));
		JScrollPane metricScrollPane = new JScrollPane(metricPanel);
		metricScrollPane.setBorder(BorderFactory.createEmptyBorder());
		metricScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		metricScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(metricScrollPane, BorderLayout.CENTER);
	}

	public void addBenchmarkMetric(BenchmarkMetric metric) {
		metricItems.add(new BenchmarkMetricItem(metric));
		metricPanel.removeAll();
		for (BenchmarkMetricItem item : metricItems) {
			metricPanel.add(item);
		}
		metricPanel.add(Box.createVerticalGlue());
		metricPanel.revalidate();
		repaint();
	}

	public List<BenchmarkMetric> getBenchmarkMetrics() {
		ArrayList<BenchmarkMetric> metrics = new ArrayList<BenchmarkMetric>();
		for (BenchmarkMetricItem item : metricItems) {
			if (item.isEnabled() == true) {
				metrics.add(item.getMetric());
			}
		}
		return metrics;
	}

	public void setReferenceLogAvailable(boolean isAvailable) {
		hasReferenceLog = isAvailable;
		for (BenchmarkMetricItem item : metricItems) {
			item.checkEnabled();
		}
		benchmarkUI.checkStartEnabled();
	}

	public void setReferenceModelAvailable(boolean isAvailable) {
		hasReferenceModel = isAvailable;
		for (BenchmarkMetricItem item : metricItems) {
			item.checkEnabled();
		}
		benchmarkUI.checkStartEnabled();
	}

	protected class BenchmarkMetricItem extends SmoothPanel {

		protected boolean enabled;
		protected BenchmarkMetric metric;

		protected JButton enableButton;

		public BenchmarkMetricItem(BenchmarkMetric aMetric) {
			super();
			metric = aMetric;
			enabled = true;
			setBorder(BorderFactory.createEmptyBorder(2, 15, 2, 15));
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setBackground(ITEM_BG_ENABLED);
			setHighlight(ITEM_HL_ENABLED);
			// setBorderWidth(5);
			setBorderAlpha(100);
			setMinimumSize(new Dimension(150, 50));
			setMaximumSize(new Dimension(2000, 50));
			setPreferredSize(new Dimension(400, 50));
			JLabel nameLabel = new JLabel(metric.name());
			nameLabel.setOpaque(false);
			nameLabel.setForeground(ITEM_FG);
			enableButton = new SlickerButton("disable");
			enableButton.setToolTipText("click to disable this metric");
			enableButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					toggleEnabled();
				}
			});
			if (RuntimeUtils.isRunningMacOsX() == true) {
				enableButton.setOpaque(false);
			}
			add(nameLabel);
			add(Box.createHorizontalGlue());
			add(enableButton);
			// display help text on click
			addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent evt) {
					if (evt.getButton() == MouseEvent.BUTTON1) {
						benchmarkUI.showHelp(metric.description());
					}
				}

				public void mouseEntered(MouseEvent arg0) { /* ignore */
				}

				public void mouseExited(MouseEvent arg0) { /* ignore */
				}

				public void mousePressed(MouseEvent arg0) { /* ignore */
				}

				public void mouseReleased(MouseEvent arg0) { /* ignore */
				}
			});
		}

		protected BenchmarkMetric getMetric() {
			return metric;
		}

		protected void toggleEnabled() {
			setEnabled(!enabled);
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean isEnabled) {
			enabled = isEnabled;
			if (enabled == true) {
				setBackground(ITEM_BG_ENABLED);
				setHighlight(ITEM_HL_ENABLED);
				enableButton.setText("disable");
				enableButton.setToolTipText("click to disable this metric");
			} else {
				setBackground(ITEM_BG_DISABLED);
				setHighlight(ITEM_HL_DISABLED);
				enableButton.setText("enable");
				enableButton.setToolTipText("click to enable this metric");
			}
			revalidate();
			repaint();
			benchmarkUI.checkStartEnabled();
		}

		public void checkEnabled() {
			boolean canBeEnabled = true;
			if (metric.needsReferenceLog()) {
				canBeEnabled &= hasReferenceLog;
			}
			if (metric.needsReferenceModel()) {
				canBeEnabled &= hasReferenceModel;
			}
			setEnabled(canBeEnabled);
			enableButton.setEnabled(canBeEnabled);
		}
	}

}
