package org.processmining.analysis.performance.advanceddottedchartanalysis.model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.processmining.analysis.performance.advanceddottedchartanalysis.ui.DottedChartOptionPanel;
import org.processmining.analysis.performance.advanceddottedchartanalysis.ui.UIUtil;

public class StatisticsModel {

	private HashMap<String, HashMap<String, DescriptiveStatistics>> overallMap = null;

	// GUI properties
	private JLabel timeSortLabel = new JLabel("Time sort (metrics):");
	private String[] timeSorts = { "milli", "seconds", "minutes", "hours",
			"days", "weeks", "months", "years" };
	private long[] dividers = { 1, 1000, 60000, 3600000L, 86400000L,
			604800000L, 2592000000L, 31536000000L };
	private JComboBox timeBox = new JComboBox(timeSorts);
	private long timeDivider = 1000;
	private JPanel jPanel = new JPanel();
	private DottedChartModel dcModel;
	private String type;
	private String timeOp;
	protected long timeOffset = 0;// 1*3600000L;
	private ArrayList<String> sortedTitleList;
	private UIUtil uiUtil = new UIUtil();

	public StatisticsModel(DottedChartModel dcModel) {
		this.dcModel = dcModel;
		overallMap = new HashMap<String, HashMap<String, DescriptiveStatistics>>();
		timeBox.setSelectedIndex(3);
		timeDivider = dividers[timeBox.getSelectedIndex()];
		registerGUIListerner();
	}

	public void initStatistics() {
		initStatistic(DottedChartModel.TIME_ACTUAL, DottedChartModel.ST_INST);
		initStatistic(DottedChartModel.TIME_ACTUAL, DottedChartModel.ST_TASK);
		initStatistic(DottedChartModel.TIME_ACTUAL, DottedChartModel.ST_ORIG);
		initStatistic(DottedChartModel.TIME_ACTUAL, DottedChartModel.ST_EVEN);
		// initStatistic(DottedChartPanel.TIME_RELATIVE_TIME,DottedChartModel.ST_INST);
		initStatistic(DottedChartModel.TIME_RELATIVE_TIME,
				DottedChartModel.ST_TASK);
		initStatistic(DottedChartModel.TIME_RELATIVE_TIME,
				DottedChartModel.ST_ORIG);
		initStatistic(DottedChartModel.TIME_RELATIVE_TIME,
				DottedChartModel.ST_EVEN);
		initStatistic(DottedChartModel.TIME_RELATIVE_RATIO,
				DottedChartModel.ST_INST);
		initStatistic(DottedChartModel.TIME_RELATIVE_RATIO,
				DottedChartModel.ST_TASK);
		initStatistic(DottedChartModel.TIME_RELATIVE_RATIO,
				DottedChartModel.ST_ORIG);
		initStatistic(DottedChartModel.TIME_RELATIVE_RATIO,
				DottedChartModel.ST_EVEN);
		initStatistic(DottedChartModel.TIME_LOGICAL_RELATIVE,
				DottedChartModel.ST_INST); // make it but not use it
		initStatistic(DottedChartModel.TIME_LOGICAL_RELATIVE,
				DottedChartModel.ST_TASK);
		initStatistic(DottedChartModel.TIME_LOGICAL_RELATIVE,
				DottedChartModel.ST_ORIG);
		initStatistic(DottedChartModel.TIME_LOGICAL_RELATIVE,
				DottedChartModel.ST_EVEN);
	}

	private void initStatistic(String timeOption, String componentType) {
		HashMap<String, DescriptiveStatistics> tempMap = new HashMap<String, DescriptiveStatistics>();
		overallMap.put(timeOption + componentType, tempMap);
		tempMap.put(DottedChartModel.STATISTICS_OVERALL, DescriptiveStatistics
				.newInstance());
		for (String str : dcModel.getItemArrayList(componentType)) {
			tempMap.put(str, DescriptiveStatistics.newInstance());
		}
	}

	public void addValue(String timeOption, String instanceId, long duration) {
		overallMap.get(timeOption + DottedChartModel.ST_INST).get(instanceId)
				.addValue(duration);
	}

	public void addValueOverAll(String timeOption, long duration) {
		overallMap.get(timeOption + DottedChartModel.ST_INST).get(
				DottedChartModel.STATISTICS_OVERALL).addValue(duration);
	}

	public void addValuesForOthers() {
		addValue(DottedChartModel.TIME_ACTUAL + DottedChartModel.ST_TASK,
				dcModel.getTaskCode(), dcModel.getCurrentTime(), dcModel
						.getItemArrayList(DottedChartModel.ST_TASK));
		addValue(DottedChartModel.TIME_ACTUAL + DottedChartModel.ST_ORIG,
				dcModel.getOrigiantorCode(), dcModel.getCurrentTime(), dcModel
						.getItemArrayList(DottedChartModel.ST_ORIG));
		addValue(DottedChartModel.TIME_ACTUAL + DottedChartModel.ST_EVEN,
				dcModel.getEventCode(), dcModel.getCurrentTime(), dcModel
						.getItemArrayList(DottedChartModel.ST_EVEN));
		addValue(
				DottedChartModel.TIME_RELATIVE_TIME + DottedChartModel.ST_TASK,
				dcModel.getTaskCode(), dcModel.getRelativeTime(), dcModel
						.getItemArrayList(DottedChartModel.ST_TASK));
		addValue(
				DottedChartModel.TIME_RELATIVE_TIME + DottedChartModel.ST_ORIG,
				dcModel.getOrigiantorCode(), dcModel.getRelativeTime(), dcModel
						.getItemArrayList(DottedChartModel.ST_ORIG));
		addValue(
				DottedChartModel.TIME_RELATIVE_TIME + DottedChartModel.ST_EVEN,
				dcModel.getEventCode(), dcModel.getRelativeTime(), dcModel
						.getItemArrayList(DottedChartModel.ST_EVEN));
		addValue(DottedChartModel.TIME_RELATIVE_RATIO
				+ DottedChartModel.ST_TASK, dcModel.getTaskCode(), dcModel
				.getRelativeRatio(), dcModel
				.getItemArrayList(DottedChartModel.ST_TASK));
		addValue(DottedChartModel.TIME_RELATIVE_RATIO
				+ DottedChartModel.ST_ORIG, dcModel.getOrigiantorCode(),
				dcModel.getRelativeRatio(), dcModel
						.getItemArrayList(DottedChartModel.ST_ORIG));
		addValue(DottedChartModel.TIME_RELATIVE_RATIO
				+ DottedChartModel.ST_EVEN, dcModel.getEventCode(), dcModel
				.getRelativeRatio(), dcModel
				.getItemArrayList(DottedChartModel.ST_EVEN));
		addValue(DottedChartModel.TIME_LOGICAL_RELATIVE
				+ DottedChartModel.ST_TASK, dcModel.getTaskCode(), dcModel
				.getLogicalRelative(), dcModel
				.getItemArrayList(DottedChartModel.ST_TASK));
		addValue(DottedChartModel.TIME_LOGICAL_RELATIVE
				+ DottedChartModel.ST_ORIG, dcModel.getOrigiantorCode(),
				dcModel.getLogicalRelative(), dcModel
						.getItemArrayList(DottedChartModel.ST_ORIG));
		addValue(DottedChartModel.TIME_LOGICAL_RELATIVE
				+ DottedChartModel.ST_EVEN, dcModel.getEventCode(), dcModel
				.getLogicalRelative(), dcModel
				.getItemArrayList(DottedChartModel.ST_EVEN));
	}

	public void addValue(String componentType, int[] code, long[] times,
			ArrayList<String> codeNames) {
		for (int k = 0; k < codeNames.size(); k++) {
			ArrayList<Long> list = new ArrayList<Long>();
			for (int j = 0; j < code.length; j++) {
				if (code[j] == k)
					list.add(times[j]);
			}
			Object[] ia = list.toArray();
			Arrays.sort(ia);
			for (int j = 1; j < ia.length; j++) {
				overallMap.get(componentType).get(codeNames.get(k)).addValue(
						(Long) ia[j] - (Long) ia[j - 1]);
			}
			if (ia.length > 1)
				overallMap.get(componentType).get(
						DottedChartModel.STATISTICS_OVERALL).addValue(
						(Long) ia[ia.length - 1] - (Long) ia[0]);
		}
	}

	public JPanel getPanel(DottedChartOptionPanel dcop) {
		update(dcop);
		return jPanel;
	}

	public void update(DottedChartOptionPanel dcop) {
		type = dcop.getComponentType();
		timeOp = dcop.getTimeOption();
		sortedTitleList = dcModel.getSortedMapModel().getSortedItemArrayList(
				dcop.getTimeOption(), dcop.getComponentType(),
				dcop.getSortStandard(), dcop.isDescCheckBoxSelected());
		displayPerformanceMetrics();
	}

	public void displayPerformanceMetrics() {
		HashMap<String, DescriptiveStatistics> aMap = null;
		if (timeOp.equals(DottedChartModel.TIME_RELATIVE_TIME)
				&& type.equals(DottedChartModel.ST_INST)) {
			aMap = overallMap.get(DottedChartModel.TIME_ACTUAL
					+ DottedChartModel.ST_INST);
		} else {
			aMap = overallMap.get(timeOp + type);
		}
		// initialize panel
		initPanel();

		int size = 0;
		long[] startTime = dcModel.getSortedMapModel().getStartTimes(timeOp,
				type, sortedTitleList);
		long[] endTime = dcModel.getSortedMapModel().getEndTimes(timeOp, type,
				sortedTitleList);

		// make the overall table
		AbstractTableModel otm = new OverallMetricTableModel();
		if (timeOp.equals(DottedChartModel.TIME_ACTUAL)) {
			jPanel.add(drawTable(otm, type, aMap
					.get(DottedChartModel.STATISTICS_OVERALL), dcModel
					.getLogMinValue(), dcModel.getLogMaxValue()));
		} else if (timeOp.equals(DottedChartModel.TIME_RELATIVE_TIME)) {
			jPanel.add(drawTable(otm, type, aMap
					.get(DottedChartModel.STATISTICS_OVERALL), 0, dcModel
					.getLogRelativeMaxValue()));
		} else if (timeOp.equals(DottedChartModel.TIME_RELATIVE_RATIO)) {
			jPanel.add(drawTable(otm, type, aMap
					.get(DottedChartModel.STATISTICS_OVERALL), 0, dcModel
					.getLogRelativeRatioMaxValue()));
		} else if (timeOp.equals(DottedChartModel.TIME_LOGICAL_RELATIVE)) {
			jPanel.add(drawTable(otm, type, aMap
					.get(DottedChartModel.STATISTICS_OVERALL), 0, dcModel
					.getLogLogiclRelativeMaxValue()));
		}
		jPanel.add(Box.createRigidArea(new Dimension(5, 10)));

		// make statistics panel
		for (String str : sortedTitleList) {
			DescriptiveStatistics currentDS = aMap.get(str);
			otm = new OneMetricTableModel();

			jPanel.add(drawTable(otm, str, currentDS, startTime[size],
					endTime[size]));
			jPanel.add(Box.createRigidArea(new Dimension(5, 10)));
			size++;
		}
		// make sure the pattern performance information is displayed properly
		jPanel.setPreferredSize(new Dimension(200, 50 + 138 * (size + 1)));
		jPanel.revalidate();
		jPanel.repaint();
	}

	// convenient method
	// /////////////////////////////////////////////////////////////
	private void initPanel() {
		jPanel.removeAll();
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

		// add time option menu
		jPanel.add(Box.createRigidArea(new Dimension(5, 10)));
		JPanel menuPanel = new JPanel(new BorderLayout());
		menuPanel.setPreferredSize(new Dimension(160, 45));
		menuPanel.setMaximumSize(new Dimension(180, 45));
		timeSortLabel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		menuPanel.add(timeSortLabel, BorderLayout.NORTH);
		timeBox.setMaximumSize(new Dimension(160, 20));
		timeBox.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		menuPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		menuPanel.add(timeBox, BorderLayout.CENTER);
		jPanel.add(menuPanel);
		jPanel.add(Box.createRigidArea(new Dimension(5, 10)));
	}

	private JPanel drawTable(AbstractTableModel otm, String str,
			DescriptiveStatistics currentDS, long start, long end) {
		DefaultTableCellRenderer dtcr2 = new DefaultTableCellRenderer();
		dtcr2.setBackground(new Color(235, 235, 235));
		JTable table = new JTable(otm);
		table.setPreferredSize(new Dimension(200, 55));
		table.setMaximumSize(new Dimension(200, 55));
		table.getColumnModel().getColumn(0).setPreferredWidth(70);
		table.getColumnModel().getColumn(0).setMaxWidth(100);
		table.getTableHeader().setFont(new Font("SansSerif", Font.PLAIN, 12));
		table.getColumnModel().getColumn(0).setCellRenderer(dtcr2);
		table.setBorder(BorderFactory.createEtchedBorder());
		if (timeOp.equals(DottedChartModel.TIME_ACTUAL)) {
			table.setValueAt(DateFormat.getInstance()
					.format(start - timeOffset), 0, 1);
			table.setValueAt(DateFormat.getInstance().format(end - timeOffset),
					1, 1);
			table.setValueAt(uiUtil.formatString(currentDS.getMean()
					/ timeDivider, 5), 2, 1);
			table.setValueAt(uiUtil.formatString(currentDS.getMin()
					/ timeDivider, 5), 3, 1);
			table.setValueAt(uiUtil.formatString(currentDS.getMax()
					/ timeDivider, 5), 4, 1);
		} else if (timeOp.equals(DottedChartModel.TIME_RELATIVE_TIME)) {
			table.setValueAt(uiUtil.formatDate(start), 0, 1);
			table.setValueAt(uiUtil.formatDate(end), 1, 1);
			table.setValueAt(uiUtil.formatString(currentDS.getMean()
					/ timeDivider, 5), 2, 1);
			table.setValueAt(uiUtil.formatString(currentDS.getMin()
					/ timeDivider, 5), 3, 1);
			table.setValueAt(uiUtil.formatString(currentDS.getMax()
					/ timeDivider, 5), 4, 1);
		} else if (timeOp.equals(DottedChartModel.TIME_RELATIVE_RATIO)) {
			table.setValueAt(uiUtil.formatRatio(start), 0, 1);
			table.setValueAt(uiUtil.formatRatio(end), 1, 1);
			table.setValueAt(uiUtil.formatString(currentDS.getMean()
					/ timeDivider, 5), 2, 1);
			table.setValueAt(uiUtil.formatString(currentDS.getMin()
					/ timeDivider, 5), 3, 1);
			table.setValueAt(uiUtil.formatString(currentDS.getMax()
					/ timeDivider, 5), 4, 1);
		} else if (timeOp.equals(DottedChartModel.TIME_LOGICAL_RELATIVE)) {
			if (type.equals(DottedChartModel.ST_INST)) {
				table.setValueAt(uiUtil.formatString(start, 5), 0, 1);
				table.setValueAt(uiUtil.formatString(end, 5), 1, 1);
				table.setValueAt(uiUtil.formatString(1, 5), 2, 1);
				table.setValueAt(uiUtil.formatString(1, 5), 3, 1);
				table.setValueAt(uiUtil.formatString(1, 5), 4, 1);
			} else {
				table.setValueAt(uiUtil.formatString(start, 5), 0, 1);
				table.setValueAt(uiUtil.formatString(end, 5), 1, 1);
				table.setValueAt(uiUtil.formatString(currentDS.getMean(), 5),
						2, 1);
				table.setValueAt(uiUtil.formatString(currentDS.getMin(), 5), 3,
						1);
				table.setValueAt(uiUtil.formatString(currentDS.getMax(), 5), 4,
						1);
			}
		}
		JPanel tempPanel0 = new JPanel(new BorderLayout());
		table.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		tempPanel0.setPreferredSize(new Dimension(160, 98));
		tempPanel0.setMaximumSize(new Dimension(180, 98));
		tempPanel0.add(table.getTableHeader(), BorderLayout.NORTH);
		tempPanel0.add(table, BorderLayout.CENTER);
		JPanel tempPanel = new JPanel(new BorderLayout());
		JLabel patternLabel = null;

		if (otm instanceof OverallMetricTableModel) {
			patternLabel = new JLabel("Component Type: " + str);
		} else {
			patternLabel = new JLabel("Component ID: " + str);
		}

		patternLabel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

		JLabel frequencyLabel = null;
		if (otm instanceof OverallMetricTableModel) {
			frequencyLabel = new JLabel("# of components: " + currentDS.getN());
		} else {
			frequencyLabel = new JLabel("# of dots: " + (currentDS.getN() + 1));
		}

		frequencyLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		tempPanel.add(patternLabel, BorderLayout.NORTH);
		tempPanel.add(frequencyLabel, BorderLayout.CENTER);
		tempPanel.add(tempPanel0, BorderLayout.SOUTH);
		return tempPanel;
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
}
