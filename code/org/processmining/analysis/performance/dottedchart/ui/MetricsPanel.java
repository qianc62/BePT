/*
 * Created on July. 04, 2007
 *
 * Author: Minseok Song
 * (c) 2006 Technische Universiteit Eindhoven, Minseok Song
 * all rights reserved
 *
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */

package org.processmining.analysis.performance.dottedchart.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JTable;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JComboBox;

import java.util.ArrayList;
import java.util.Date;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.processmining.analysis.performance.dottedchart.model.DottedChartModel;
import org.processmining.analysis.performance.dottedchart.model.OneMetricTableModel;
import org.processmining.analysis.performance.dottedchart.model.OverallMetricTableModel;

public class MetricsPanel extends JPanel {
	protected DottedChartModel dcModel = null;
	protected DottedChartPanel dcPanel = null;

	private JLabel timeSortLabel = new JLabel("Time sort (metrics):");
	private String[] timeSorts = { "milli", "seconds", "minutes", "hours", // array
			// of
			// "time sort"
			// strings
			// required
			// to
			// fill
			// the
			// timeBox
			// combobox
			// with
			"days", "weeks", "months", "years" };
	private long[] dividers = { 1, 1000, 60000, 3600000L, 86400000L,
			604800000L, 2592000000L, 31536000000L };
	private JComboBox timeBox = new JComboBox(timeSorts);
	private long timeDivider = 1000; // The selected data-element type

	public MetricsPanel(DottedChartModel aDcModel, DottedChartPanel aDcPanel) {
		dcModel = aDcModel;
		dcPanel = aDcPanel;
		timeBox.setSelectedIndex(1);
		displayPerformanceMetrics();
		registerGUIListerner();
	}

	/**
	 * Displays the performance metrics of each pattern on the east side of the
	 * plug-in window.
	 * 
	 * @param sortedArray
	 *            int[]
	 */

	public void displayPerformanceMetrics() {
		String type = dcPanel.getTimeOption();
		ArrayList<DescriptiveStatistics> aList = dcModel.getTimeStatistics();
		ArrayList<String> aTitles = dcModel.getDescriptiveStatisticsTitles();

		ArrayList<String> sortedTitleList = dcModel.getSortedKeySetList();

		this.removeAll();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// add time option menu
		this.add(Box.createRigidArea(new Dimension(5, 10)));
		JPanel menuPanel = new JPanel(new BorderLayout());
		menuPanel.setPreferredSize(new Dimension(160, 45));
		menuPanel.setMaximumSize(new Dimension(180, 45));
		timeSortLabel.setAlignmentX(LEFT_ALIGNMENT);
		menuPanel.add(timeSortLabel, BorderLayout.NORTH);
		timeBox.setMaximumSize(new Dimension(160, 20));
		timeBox.setAlignmentX(LEFT_ALIGNMENT);
		menuPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		menuPanel.add(timeBox, BorderLayout.CENTER);
		this.add(menuPanel);
		this.add(Box.createRigidArea(new Dimension(5, 10)));

		// for each frequency get the set of patterns that have that frequency
		// (run from high frequency to low)
		int size = 0;
		for (int i = 0; i < aList.size(); i++) {
			try {
				String key;
				DescriptiveStatistics currentDS = null;
				if (i != 0)
					key = sortedTitleList.get(i - 1);
				else {
					key = aTitles.get(0);
					currentDS = aList.get(i);
				}

				if (i > 0
						&& dcModel.getTypeHashMap().equals(
								DottedChartPanel.ST_INST)
						&& !dcModel.getInstanceTypeToKeep().contains(key))
					continue;
				size++;

				if (i > 0) {
					for (int j = 1; j < aTitles.size(); j++) {
						if (aTitles.get(j).equals(key))
							currentDS = aList.get(j);
					}
				}
				AbstractTableModel otm;
				// create labels that contains information about the pattern
				if (i == 0)
					otm = new OverallMetricTableModel();
				else
					otm = new OneMetricTableModel();
				DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
				dtcr.setBackground(new Color(235, 235, 235));
				JTable table = new JTable(otm);
				table.setPreferredSize(new Dimension(200, 55));
				table.setMaximumSize(new Dimension(200, 55));
				table.getColumnModel().getColumn(0).setPreferredWidth(70);
				table.getColumnModel().getColumn(0).setMaxWidth(100);
				table.getTableHeader().setFont(
						new Font("SansSerif", Font.PLAIN, 12));
				table.getColumnModel().getColumn(0).setCellRenderer(dtcr);
				table.setBorder(BorderFactory.createEtchedBorder());

				// place throughput times in table
				if (type.equals(DottedChartPanel.TIME_ACTUAL)) {
					if (i == 0) {
						table.setValueAt(DateFormat.getInstance().format(
								dcModel.getLogBoundaryLeft()), 0, 1);
						table.setValueAt(DateFormat.getInstance().format(
								dcModel.getLogBoundaryRight()), 1, 1);
					} else {
						table.setValueAt(DateFormat.getInstance().format(
								dcModel.getStartDateofLogUniList(key)), 0, 1);
						table.setValueAt(DateFormat.getInstance().format(
								dcModel.getEndDateofLogUniList(key)), 1, 1);
					}
					table.setValueAt(formatString(currentDS.getMean()
							/ timeDivider, 5), 2, 1);
					table.setValueAt(formatString(currentDS.getMin()
							/ timeDivider, 5), 3, 1);
					table.setValueAt(formatString(currentDS.getMax()
							/ timeDivider, 5), 4, 1);
				} else if (type.equals(DottedChartPanel.TIME_RELATIVE_TIME)) {
					if (i == 0) {
						table.setValueAt(formatDate(dcModel
								.getLogBoundaryLeft()), 0, 1);
						table.setValueAt(formatDate(dcModel
								.getLogBoundaryRight()), 1, 1);
					} else {
						table.setValueAt(formatDate(dcModel
								.getStartDateofLogUniList(key)), 0, 1);
						table.setValueAt(formatDate(dcModel
								.getEndDateofLogUniList(key)), 1, 1);
					}
					table.setValueAt(formatString(currentDS.getMean()
							/ timeDivider, 5), 2, 1);
					table.setValueAt(formatString(currentDS.getMin()
							/ timeDivider, 5), 3, 1);
					table.setValueAt(formatString(currentDS.getMax()
							/ timeDivider, 5), 4, 1);
				} else if (type.equals(DottedChartPanel.TIME_RELATIVE_RATIO)) {
					if (i == 0) {
						table.setValueAt(formatRatio(dcModel
								.getLogBoundaryLeft()), 0, 1);
						table.setValueAt(formatRatio(dcModel
								.getLogBoundaryRight()), 1, 1);
					} else {
						table.setValueAt(formatRatio(dcModel
								.getStartDateofLogUniList(key)), 0, 1);
						table.setValueAt(formatRatio(dcModel
								.getEndDateofLogUniList(key)), 1, 1);
					}
					table.setValueAt(
							formatString(currentDS.getMean() / 100, 5), 2, 1);
					table.setValueAt(formatString(currentDS.getMin() / 100, 5),
							3, 1);
					table.setValueAt(formatString(currentDS.getMax() / 100, 5),
							4, 1);
				} else if (type.equals(DottedChartPanel.TIME_LOGICAL)
						|| type.equals(DottedChartPanel.TIME_LOGICAL_RELATIVE)) {
					if (i == 0) {
						table.setValueAt(formatString(dcModel
								.getLogBoundaryLeft().getTime(), 5), 0, 1);
						table.setValueAt(formatString(dcModel
								.getLogBoundaryRight().getTime(), 5), 1, 1);
					} else {
						table.setValueAt(formatString((dcModel
								.getStartDateofLogUniList(key)).getTime(), 5),
								0, 1);
						table.setValueAt(formatString((dcModel
								.getEndDateofLogUniList(key)).getTime(), 5), 1,
								1);
					}
					table
							.setValueAt(formatString(currentDS.getMean(), 5),
									2, 1);
					table.setValueAt(formatString(currentDS.getMin(), 5), 3, 1);
					table.setValueAt(formatString(currentDS.getMax(), 5), 4, 1);
				}

				JPanel tempPanel = new JPanel(new BorderLayout());
				table.setAlignmentX(CENTER_ALIGNMENT);
				tempPanel.setPreferredSize(new Dimension(160, 98));
				tempPanel.setMaximumSize(new Dimension(180, 98));
				tempPanel.add(table.getTableHeader(), BorderLayout.NORTH);
				tempPanel.add(table, BorderLayout.CENTER);
				JPanel tempPanel2 = new JPanel(new BorderLayout());
				JLabel patternLabel = new JLabel("Component " + key + ":");
				patternLabel.setAlignmentX(LEFT_ALIGNMENT);

				JLabel frequencyLabel = null;
				if (i == 0)
					frequencyLabel = new JLabel("# of components: "
							+ currentDS.getN());
				else
					frequencyLabel = new JLabel("# of dots: "
							+ dcModel.getNumberOfLogUnits(key));

				frequencyLabel.setAlignmentX(LEFT_ALIGNMENT);
				frequencyLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
				tempPanel2.add(patternLabel, BorderLayout.NORTH);
				tempPanel2.add(frequencyLabel, BorderLayout.CENTER);
				tempPanel2.add(tempPanel, BorderLayout.SOUTH);
				this.add(tempPanel2);
				this.add(Box.createRigidArea(new Dimension(5, 10)));
			} catch (NullPointerException ex) {
				// can occur when patternMap does not contain a pattern with
				// this frequency
				size--;
			}
		}
		// make sure the pattern performance information is displayed properly
		this.setPreferredSize(new Dimension(200, 140 * (size + 1)));
		this.revalidate();
		this.repaint();

	}

	private void registerGUIListerner() {
		// timeBox listener: set timeSort to the sort that is selected in the
		// box
		// and set timeDivider to the corresponding divider.
		timeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeDivider = dividers[timeBox.getSelectedIndex()];
				displayPerformanceMetrics();
			}
		});

	}

	// util method /////////////////////////////////////////////////////////////
	/*
	 * Formats a double to display it in the right manner, with 'places' being
	 * the maximum number of decimal places allowed
	 * 
	 * @param val double
	 * 
	 * @param places int
	 * 
	 * @return String
	 */
	private String formatString(double val, int places) {
		if (Double.valueOf(val).equals(Double.NaN))
			return "0.0";
		String cur = "";
		DecimalFormat df;
		double bound = Math.pow(10.0, (0 - places));
		String tempString = "0";
		for (int i = 0; i < places - 1; i++) {
			tempString += "#";
		}
		if ((val != 0.0) && (val < bound)) {
			// display scientific notation
			if (places == 0) {
				df = new DecimalFormat("0E0");
			} else {
				df = new DecimalFormat("0." + tempString + "E0");
			}
			cur = df.format(val);
		} else {
			if (places == 0) {
				df = new DecimalFormat("0");
			} else {
				df = new DecimalFormat("0." + tempString);
			}
			cur = df.format(val);
		}
		return cur;
	}

	private String formatDate(Date time) {
		long timeStart = time.getTime();
		long days = timeStart / 1000 / 60 / 60 / 24;
		long hours = (timeStart - days * 24 * 60 * 60 * 1000) / 1000 / 60 / 60;
		long minutes = (timeStart - days * 24 * 60 * 60 * 1000 - hours * 60 * 60 * 1000) / 1000 / 60;
		long seconds = (timeStart - days * 24 * 60 * 60 * 1000 - hours * 60
				* 60 * 1000 - minutes * 60 * 1000) / 1000;
		return String.valueOf(days + "days:" + hours + ":" + minutes + ":"
				+ seconds);
	}

	private String formatRatio(Date time) {
		long timeStart = time.getTime();
		return String.valueOf(timeStart / 100 + "."
				+ (timeStart - timeStart / 100 * 100) + "%");
	}
}
