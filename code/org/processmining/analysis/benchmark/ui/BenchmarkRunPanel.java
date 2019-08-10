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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.processmining.analysis.benchmark.BenchmarkItem;
import org.processmining.analysis.benchmark.metric.BenchmarkMetric;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.util.RuntimeUtils;

/**
 * Implements the 'Progress' interface of ProM in form of a panel.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 */
public class BenchmarkRunPanel extends Progress {

	protected static Color colorBgUp = new Color(80, 80, 80);
	protected static Color colorBgDown = new Color(40, 40, 40);
	protected static Color colorBgInner = new Color(120, 120, 120);
	protected static Color colorFg = new Color(40, 40, 40);

	protected GradientPanel panel;
	protected JProgressBar smallProgress;
	protected JProgressBar bigProgress;
	protected JLabel smallLabel;
	protected JLabel bigLabel;
	protected JLabel title;

	protected boolean isRunning;
	protected boolean isCancelled;

	public BenchmarkRunPanel() {
		isRunning = true;
		panel = new GradientPanel(colorBgUp, colorBgDown);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		RoundedPanel innerPanel = new RoundedPanel(20, 0, 0);
		innerPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		innerPanel.setBackground(colorBgInner);
		innerPanel.setMinimumSize(new Dimension(400, 160));
		innerPanel.setMaximumSize(new Dimension(600, 200));
		innerPanel.setPreferredSize(new Dimension(500, 180));
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
		bigProgress = new JProgressBar();
		bigProgress.setOpaque(false);
		bigProgress.setIndeterminate(true);
		smallProgress = new JProgressBar();
		smallProgress.setOpaque(false);
		smallProgress.setIndeterminate(true);
		title = new JLabel("Performing benchmark");
		title.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		title.setOpaque(false);
		title.setFont(title.getFont().deriveFont(16.0f));
		bigLabel = new JLabel("");
		bigLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		bigLabel.setOpaque(false);
		smallLabel = new JLabel("");
		smallLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		smallLabel.setOpaque(false);
		JButton abortButton = new SlickerButton("abort");
		if (RuntimeUtils.isRunningMacOsX() == true) {
			abortButton.setOpaque(false);
		}
		abortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				isCancelled = true;
				isRunning = false;
			}
		});
		JButton skipButton = new SlickerButton("skip metric");
		if (RuntimeUtils.isRunningMacOsX() == true) {
			skipButton.setOpaque(false);
		}
		skipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				isCancelled = true;
			}
		});
		JPanel abortPanel = new JPanel();
		abortPanel.setOpaque(false);
		abortPanel.setBorder(BorderFactory.createEmptyBorder());
		abortPanel.setLayout(new BoxLayout(abortPanel, BoxLayout.X_AXIS));
		abortPanel.add(Box.createHorizontalGlue());
		abortPanel.add(abortButton);
		abortPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		abortPanel.add(skipButton);
		innerPanel.add(Box.createVerticalGlue());
		innerPanel.add(title);
		innerPanel.add(Box.createVerticalStrut(10));
		innerPanel.add(bigProgress);
		innerPanel.add(Box.createVerticalStrut(10));
		innerPanel.add(bigLabel);
		innerPanel.add(Box.createVerticalStrut(10));
		innerPanel.add(smallProgress);
		innerPanel.add(Box.createVerticalStrut(10));
		innerPanel.add(smallLabel);
		innerPanel.add(Box.createVerticalStrut(10));
		innerPanel.add(abortPanel);
		innerPanel.add(Box.createVerticalGlue());
		panel.add(Box.createHorizontalGlue());
		panel.add(innerPanel);
		panel.add(Box.createHorizontalGlue());
	}

	public void runBenchmark(final LogReader referenceLog,
			final PetriNet referenceModel, final List<BenchmarkItem> items,
			final List<BenchmarkMetric> metrics,
			final ActionListener finishListener) {
		Thread benchmarkThread = new Thread() {
			public void run() {
				performBenchmarkRoutine(referenceLog, referenceModel, items,
						metrics);
				// notify finish listener
				finishListener.actionPerformed(new ActionEvent(this,
						ActionEvent.ACTION_PERFORMED, "calculation finished"));
			}
		};
		benchmarkThread.start();
	}

	protected void performBenchmarkRoutine(LogReader referenceLog,
			PetriNet referenceModel, List<BenchmarkItem> items,
			List<BenchmarkMetric> metrics) {
		isRunning = true;
		int numberOfCalculations = items.size() * metrics.size();
		bigProgress.setMinimum(0);
		bigProgress.setMaximum(numberOfCalculations + 1);
		bigProgress.setIndeterminate(false);
		for (BenchmarkMetric metric : metrics) {
			for (BenchmarkItem item : items) {
				isCancelled = false;
				bigLabel.setText("Calculating " + metric.name() + " for "
						+ item.getName());
				bigProgress.setValue(bigProgress.getValue() + 1);
				smallLabel.setText("calculation in progress...");
				smallProgress.setIndeterminate(true);
				double measurement = metric.measure(item.getModel(), item
						.getLog(), referenceModel, this);
				item.setMeasurement(metric.name(), measurement);
				if (isRunning == false) {
					break;
				}
			}
			if (isRunning == false) {
				break;
			}
		}
	}

	public boolean isAborted() {
		return !isRunning;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#isCanceled()
	 */
	@Override
	public boolean isCanceled() {
		// Give metrics a chance to react on cancel and to
		// return and (approximated) value!
		// return isAborted();
		return isCancelled;
	}

	public JPanel getPanel() {
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#close()
	 */
	@Override
	public void close() {
		// ignore this one
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#getMaximum()
	 */
	@Override
	public int getMaximum() {
		return smallProgress.getMaximum();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#setMaximum(int)
	 */
	@Override
	public void setMaximum(int m) {
		smallProgress.setMaximum(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#setMinimum(int)
	 */
	@Override
	public void setMinimum(int m) {
		smallProgress.setMinimum(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#setMinMax(int, int)
	 */
	@Override
	public void setMinMax(int min, int max) {
		smallProgress.setMinimum(min);
		smallProgress.setMaximum(max);
		smallProgress.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#setNote(java.lang.String)
	 */
	@Override
	public void setNote(String note) {
		smallLabel.setText(note);
		smallLabel.revalidate();
		smallLabel.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#setProgress(int)
	 */
	@Override
	public void setProgress(int nv) {
		smallProgress.setIndeterminate(false);
		smallProgress.setValue(nv);
		smallProgress.repaint();
	}

}
