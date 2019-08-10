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
package org.processmining.mining.cloudchamber;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerComboBoxUI;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.deckfour.slickerbox.ui.SlickerSliderUI;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class CloudChamberMiner implements MiningPlugin, MiningResult, Provider {

	protected LogReader logReader;
	protected JSlider slider;
	protected CloudChamberPanel ccPanel;
	protected JScrollPane scrollPane;
	protected JLabel viewLabel;
	protected JList instancesList;
	protected int[] selectedInstances;
	protected JComboBox instanceJumpCombo;
	protected JButton helpButton;
	protected static DecimalFormat format = new DecimalFormat("##0.00 %");
	protected static Color COLOR_BG = new Color(150, 150, 150);
	protected static Color COLOR_FG = new Color(40, 40, 40);
	protected UpdateWorker worker;
	protected ProgressPanel progressPanel;

	/**
	 *
	 */
	public CloudChamberMiner() {
		worker = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#getOptionsPanel(org.processmining
	 * .framework.log.LogSummary)
	 */
	public JPanel getOptionsPanel(LogSummary summary) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#mine(org.processmining.framework
	 * .log.LogReader)
	 */
	public MiningResult mine(LogReader log) {
		logReader = log;
		return this;
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
		return "Cloud chamber miner";
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
		JPanel result = getCloudChamberPanel(true);
		result.setBackground(COLOR_BG);
		return result;
	}

	public JPanel getCloudChamberPanel(boolean includeHeader) {
		progressPanel = new ProgressPanel("Processing log");
		JPanel root = new JPanel();
		root.setOpaque(false);
		root.setBorder(BorderFactory.createEmptyBorder());
		root.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(new JPanel());
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 5));
		JScrollBar vBar = scrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(10, 10, 10), new Color(50, 50, 50), 4, 12));
		vBar.setOpaque(false);
		JScrollBar hBar = scrollPane.getHorizontalScrollBar();
		hBar.setUI(new SlickerScrollBarUI(hBar, new Color(0, 0, 0, 0),
				new Color(10, 10, 10), new Color(50, 50, 50), 4, 12));
		hBar.setOpaque(false);
		scrollPane.setOpaque(false);
		root.add(scrollPane, BorderLayout.CENTER);
		slider = new JSlider(JSlider.HORIZONTAL, 0, 100000, 0);
		slider.setUI(new SlickerSliderUI(slider));
		slider.setOpaque(false);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				double value = (double) slider.getValue() / 100000.0;
				int horizVal = (int) ((double) scrollPane
						.getHorizontalScrollBar().getMaximum() * value);
				int vertiVal = (int) ((double) scrollPane
						.getVerticalScrollBar().getMaximum() * value);
				scrollPane.getHorizontalScrollBar().setValue(horizVal);
				scrollPane.getVerticalScrollBar().setValue(vertiVal);
				viewLabel.setText(format.format(value));
			}
		});
		slider.setOpaque(false);
		RoundedPanel southPanel = new RoundedPanel(10, 0, 0);
		southPanel.setBackground(new Color(80, 80, 80));
		southPanel.setLayout(new BorderLayout());
		Color southFgColor = new Color(180, 180, 180);
		JLabel slideLabel = new JLabel("Navigate along the log:");
		slideLabel.setOpaque(false);
		slideLabel.setForeground(southFgColor);
		slideLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
		viewLabel = new JLabel("0.0 %");
		viewLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
		viewLabel.setOpaque(false);
		viewLabel.setForeground(southFgColor);
		southPanel.add(slideLabel, BorderLayout.WEST);
		southPanel.add(slider, BorderLayout.CENTER);
		southPanel.add(viewLabel, BorderLayout.EAST);
		root.add(southPanel, BorderLayout.SOUTH);
		// create list of traces
		String[] instanceNames = new String[logReader.numberOfInstances()];
		selectedInstances = new int[logReader.numberOfInstances()];
		for (int i = 0; i < instanceNames.length; i++) {
			instanceNames[i] = logReader.getInstance(i).getName();
			selectedInstances[i] = i;
		}
		instancesList = new JList(instanceNames);
		instancesList.setOpaque(true);
		instancesList.setBackground(COLOR_FG);
		instancesList.setForeground(COLOR_BG);
		instancesList.setSelectionBackground(new Color(80, 80, 80));
		instancesList.setSelectionForeground(COLOR_FG);
		instancesList.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		instancesList.setSelectionInterval(0, instanceNames.length - 1); // select
		// all
		instancesList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					redrawSelection();
				}
			}
		});
		instancesList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane instancesScrollPane = new JScrollPane(instancesList);
		instancesScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0,
				5));
		instancesScrollPane.setOpaque(true);
		instancesScrollPane.setBackground(COLOR_FG);
		JScrollBar ivBar = instancesScrollPane.getVerticalScrollBar();
		ivBar.setUI(new SlickerScrollBarUI(ivBar, COLOR_FG, new Color(140, 140,
				140), new Color(80, 80, 80), 4, 12));
		JScrollBar ihBar = instancesScrollPane.getHorizontalScrollBar();
		ihBar.setUI(new SlickerScrollBarUI(ihBar, COLOR_FG, new Color(140, 140,
				140), new Color(80, 80, 80), 4, 12));
		JLabel instancesLabel = new JLabel("Select cases:");
		instancesLabel.setForeground(COLOR_FG);
		instancesLabel.setOpaque(false);
		instancesLabel
				.setBorder(BorderFactory.createEmptyBorder(10, 10, 7, 10));
		instanceJumpCombo = new JComboBox(instanceNames);
		instanceJumpCombo.setUI(new SlickerComboBoxUI());
		instanceJumpCombo.setOpaque(false);
		instanceJumpCombo.setSelectedIndex(0);
		instanceJumpCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollToInstance(instanceJumpCombo.getSelectedIndex());
			}
		});
		JPanel jumpPanel = new JPanel();
		jumpPanel.setOpaque(false);
		jumpPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		jumpPanel.setLayout(new BoxLayout(jumpPanel, BoxLayout.Y_AXIS));
		JLabel jumpLabel = new JLabel("Jump to instance:");
		jumpLabel.setOpaque(false);
		jumpLabel.setForeground(COLOR_FG);
		jumpLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		instanceJumpCombo.setAlignmentX(JComboBox.CENTER_ALIGNMENT);
		helpButton = new AutoFocusButton("Help");
		helpButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		helpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ccPanel.showHelp();
			}
		});
		jumpPanel.add(Box.createVerticalGlue());
		jumpPanel.add(jumpLabel);
		jumpPanel.add(Box.createVerticalStrut(5));
		jumpPanel.add(instanceJumpCombo);
		jumpPanel.add(Box.createVerticalStrut(15));
		jumpPanel.add(helpButton);
		jumpPanel.add(Box.createVerticalGlue());
		HeaderBar header = new HeaderBar("Cloud chamber");
		header.setHeight(40);
		JPanel headerPanel = new JPanel();
		headerPanel.setOpaque(false);
		headerPanel.setLayout(new BorderLayout());
		headerPanel.setBorder(BorderFactory.createEmptyBorder());
		if (includeHeader == true) {
			headerPanel.add(header, BorderLayout.NORTH);
		}
		headerPanel.add(instancesLabel, BorderLayout.CENTER);
		JPanel instancesPanel = new JPanel();
		instancesPanel.setBorder(BorderFactory.createEmptyBorder());
		instancesPanel.setOpaque(false);
		instancesPanel.setLayout(new BorderLayout());
		instancesPanel.add(headerPanel, BorderLayout.NORTH);
		instancesPanel.add(instancesScrollPane, BorderLayout.CENTER);
		instancesPanel.add(jumpPanel, BorderLayout.SOUTH);
		instancesPanel.setMaximumSize(new Dimension(180, 1000));
		instancesPanel.setMinimumSize(new Dimension(180, 50));
		instancesPanel.setPreferredSize(new Dimension(180, 500));
		JPanel splitPanel = new JPanel();
		splitPanel.setOpaque(true);
		splitPanel.setBorder(BorderFactory.createEmptyBorder());
		splitPanel.setOpaque(false);
		splitPanel.setLayout(new BorderLayout());
		splitPanel.add(root, BorderLayout.CENTER);
		splitPanel.add(instancesPanel, BorderLayout.EAST);
		// start drawing
		redrawSelection();
		return splitPanel;
	}

	protected synchronized void redrawSelection() {
		this.instancesList.setEnabled(false);
		this.instanceJumpCombo.setEnabled(false);
		this.helpButton.setEnabled(false);
		this.slider.setEnabled(false);
		if (worker != null) {
			return;
			// worker.stop();
		}
		worker = new UpdateWorker();
		progressPanel.setProgress(0);
		progressPanel.setNote("Refreshing data view...");
		scrollPane.setViewportView(progressPanel.getPanel());
		scrollPane.revalidate();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				worker.start();
			}

		});
		try {
			Thread.sleep(80);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void scrollToInstance(int index) {
		if (index < 0) {
			return;
		}
		int coord = 0;
		for (int i = 0; i < index; i++) {
			coord += logReader.getInstance(selectedInstances[i])
					.getAuditTrailEntryList().size();
		}
		// prevent over-scrolling
		Dimension viewSize = scrollPane.getViewport().getExtentSize();
		int minSize = Math.max(viewSize.height, viewSize.width);
		int maxCoord = ccPanel.getSize().width;
		if ((maxCoord - coord) < minSize) {
			coord = maxCoord - minSize;
		}
		if (coord < 0) {
			coord = 0;
		}
		// set new view coordinate in scrollpane
		scrollPane.getViewport().setViewPosition(new Point(coord, coord));
		double value = (double) scrollPane.getHorizontalScrollBar().getValue()
				/ (double) scrollPane.getHorizontalScrollBar().getMaximum();
		slider.setValue((int) (value * (double) slider.getMaximum()));
	}

	protected class UpdateWorker extends SwingWorker {
		protected boolean isActive = true;
		protected boolean isRunning = true;

		public Object construct() {
			LogReader filteredReader;
			progressPanel.setMinMax(0, 6);
			scrollPane.revalidate();
			scrollPane.repaint();
			try {
				if (isActive == false) {
					exit();
					return null;
				}
				selectedInstances = instancesList.getSelectedIndices();
				progressPanel.setProgress(1);
				filteredReader = LogReaderFactory.createInstance(logReader,
						selectedInstances);
				progressPanel.setProgress(2);
				if (isActive == false) {
					exit();
					return null;
				}
				CloudChamberStats stats = new CloudChamberStats(filteredReader);
				progressPanel.setProgress(3);
				if (isActive == false) {
					exit();
					return null;
				}
				ccPanel = new CloudChamberPanel(stats);
				progressPanel.setProgress(4);
				if (isActive == false) {
					exit();
					return null;
				}
				scrollPane.setViewportView(ccPanel);
				progressPanel.setProgress(5);
				if (isActive == false) {
					exit();
					return null;
				}
				scrollPane.getHorizontalScrollBar().setMaximum(
						ccPanel.getWidth());
				if (isActive == false) {
					exit();
					return null;
				}
				scrollPane.getVerticalScrollBar().setMaximum(
						ccPanel.getHeight());
				if (isActive == false) {
					exit();
					return null;
				}
				slider.setValue(0);
				if (isActive == false) {
					exit();
					return null;
				}
				instanceJumpCombo.removeAllItems();
				for (int i = 0; i < selectedInstances.length; i++) {
					instanceJumpCombo.addItem(logReader.getInstance(
							selectedInstances[i]).getName());
					if (isActive == false) {
						exit();
						return null;
					}
				}
				if (instanceJumpCombo.getItemCount() > 0) {
					instanceJumpCombo.setSelectedIndex(0);
				}
				progressPanel.setProgress(6);
				instancesList.setEnabled(true);
				instanceJumpCombo.setEnabled(true);
				helpButton.setEnabled(true);
				slider.setEnabled(true);
				worker = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
			exit();
			return ccPanel;
		}

		public synchronized void stop() {
			while (isRunning == true) {
				try {
					wait();
				} catch (InterruptedException e) {
					// ignore.. (we are in while loop, so ok)
					e.printStackTrace();
				}
			}
			isActive = false;
		}

		protected synchronized void exit() {
			isRunning = false;
			notifyAll();
		}
	}

	public LogReader getPreviewedLog() {
		try {
			return LogReaderFactory
					.createInstance(logReader, selectedInstances);
		} catch (Exception e) {
			// something was wrong...
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		LogReader previewed = getPreviewedLog();
		return new ProvidedObject[] { new ProvidedObject(
				"Previewed log selection", new Object[] { previewed }) };
	}
}
