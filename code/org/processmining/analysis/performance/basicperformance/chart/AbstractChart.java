package org.processmining.analysis.performance.basicperformance.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.processmining.analysis.performance.basicperformance.BasicPerformanceAnalysisUI;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.processmining.analysis.performance.basicperformance.model.StatisticUnit;
import org.processmining.analysis.performance.basicperformance.model.instance.InstancePerformance;
import org.processmining.analysis.performance.basicperformance.model.task.*;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;

public abstract class AbstractChart implements GuiNotificationTarget {

	public static Color colorBg = new Color(120, 120, 120);
	public static Color colorInnerBg = new Color(140, 140, 140);
	public static Color colorFg = new Color(30, 30, 30);
	public static Color colorTextAreaBg = new Color(160, 160, 160);

	/**
	 * the name of this chart
	 */
	protected String name;

	/**
	 * human-readable description (1 sentence) of what is chart
	 */
	protected String description;

	protected BasicPerformanceAnalysisUI performanceUI = null;
	protected AbstractPerformance absPerformance = null;

	protected boolean isItemA = true;

	protected boolean bLegend = true;
	protected JFreeChart chart = null;
	protected ChartPanel chartPanel;
	protected JScrollPane scrollPane;
	protected JSplitPane splitPane;

	protected GUIPropertyListEnumeration timeUnitSort;
	protected GUIPropertyListEnumeration measureSort;
	protected GUIPropertyListEnumeration performanceSort;
	protected GUIPropertyListEnumeration dim1Sort;
	protected GUIPropertyListEnumeration dim2Sort;

	/**
	 * Creates a new profile with the given name and description
	 */
	public AbstractChart(String aName, String aDescription) {
		name = aName;
		description = aDescription;
		initTimeSort();
		initMeasureSort();
	}

	/**
	 * Creates a new profile with the given name and description
	 */
	public AbstractChart(String aName, String aDescription,
			BasicPerformanceAnalysisUI aPerformanceModel) {
		this(aName, aDescription);
		performanceUI = aPerformanceModel;
		initDimSort();
	}

	protected void initTimeSort() {
		ArrayList<String> timeList = new ArrayList<String>();
		timeList.add("seconds");
		timeList.add("minutes");
		timeList.add("hours");
		timeList.add("days");
		timeList.add("weeks");
		timeList.add("months");
		timeList.add("years");
		timeUnitSort = new GUIPropertyListEnumeration("Time Unit:", "",
				timeList, this, 150);
		timeUnitSort.setValue("hours");
	}

	protected void initMeasureSort() {
		ArrayList<String> measureList = new ArrayList<String>();
		measureList.add("Average");
		measureList.add("Sum");
		measureList.add("Minimum");
		measureList.add("Median");
		measureList.add("Maximum");
		measureList.add("StandDev");
		measureList.add("Frequency");
		measureSort = new GUIPropertyListEnumeration("Measure:", "",
				measureList, this, 150);
	}

	protected void initDimSort() {
		ArrayList<String> dim1List = new ArrayList<String>();
		if (performanceUI.isTaskDim()) {
			dim1List.add("task");
		}
		if (performanceUI.isOriginatorDim()) {
			dim1List.add("originator");
		}
		if (performanceUI.isInstanceDim()) {
			dim1List.add("instance");
		}
		dim1Sort = new GUIPropertyListEnumeration("1st Dim:", "", dim1List,
				this, 150);
		ArrayList<String> dim2List = new ArrayList<String>();
		if (performanceUI.isTaskDim()) {
			dim2List.add("task");
		}
		if (performanceUI.isOriginatorDim()) {
			dim2List.add("originator");
		}
		if (performanceUI.isInstanceDim()) {
			dim2List.add("instance");
		}
		dim2Sort = new GUIPropertyListEnumeration("2st Dim:", "", dim2List,
				this, 150);
	}

	/**
	 * Returns the name of the chart
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the description of the component of the performance
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the name of the chart
	 */
	public String toString() {
		return name;
	}

	protected JPanel getOptionPanel() {
		initTimeSort();
		initMeasureSort();
		initDimSort();
		JPanel panel = new JPanel();
		panel.setBackground(colorBg);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		if (!absPerformance.getName().equals(InstancePerformance.getNameCode())) {
			panel.add(dim1Sort.getPropertyPanel());
			panel.add(dim2Sort.getPropertyPanel());
		}
		panel.add(timeUnitSort.getPropertyPanel());
		panel.add(measureSort.getPropertyPanel());

		ArrayList<String> performanceList = new ArrayList<String>();
		if (absPerformance.getName().equals(InstancePerformance.getNameCode())) {
			performanceList.add("Sojourn Time");
		}
		performanceList.add("Working Time");
		performanceList.add("Waiting Time");

		if (absPerformance instanceof AbstractPerformance2D) {

		} else {
			if (absPerformance.getName().equals(
					InstancePerformance.getNameCode())) {
				performanceList.add("All");
			} else {
				performanceList.add("Both");
			}
		}
		performanceSort = new GUIPropertyListEnumeration("Performance Sort:",
				"", performanceList, this, 150);
		panel.add(performanceSort.getPropertyPanel());
		return panel;
	}

	public JPanel getPanel(AbstractPerformance absPer) {
		absPerformance = absPer;
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());
		splitPane = new JSplitPane();
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(getOptionPanel());
		splitPane.setRightComponent(getGraphPanel());
		splitPane.setBackground(colorBg);
		splitPane.setDividerSize(0);
		panel.add(splitPane, BorderLayout.CENTER);
		return panel;
	}

	protected JScrollPane getGraphPanel() {
		return new JScrollPane();
	}

	public void updateGUI() {
		updatePerformance();
		splitPane.remove(scrollPane);
		JScrollPane jscrollPanel = getGraphPanel();
		jscrollPanel.setBackground(colorBg);
		splitPane.setRightComponent(jscrollPanel);
	}

	protected long getTimeUnit() {
		if (((String) measureSort.getValue()).equals("Frequency")) {
			return 1;
		}
		if (timeUnitSort.getValue().equals("seconds")) {
			return 1000;
		} else if (timeUnitSort.getValue().equals("minutes")) {
			return 60000;
		} else if (timeUnitSort.getValue().equals("hours")) {
			return 3600000L;
		} else if (timeUnitSort.getValue().equals("days")) {
			return 86400000L;
		} else if (timeUnitSort.getValue().equals("weeks")) {
			return 604800000L;
		} else if (timeUnitSort.getValue().equals("months")) {
			return 2592000000L;
		} else {
			return 31536000000L;
		}
	}

	protected void addDate(DefaultCategoryDataset dataset, StatisticUnit su,
			String rowKey, String columnKey) {
		String sort = (String) measureSort.getValue();
		if (sort.equals("Minimum")) {
			dataset.addValue(su.getStatistics().getMin() / getTimeUnit(),
					rowKey, columnKey);
		} else if (sort.equals("Average")) {
			dataset.addValue(su.getStatistics().getMean() / getTimeUnit(),
					rowKey, columnKey);
		} else if (sort.equals("Median")) {
			dataset.addValue(su.getStatistics().getPercentile(50)
					/ getTimeUnit(), rowKey, columnKey);
		} else if (sort.equals("Maximum")) {
			dataset.addValue(su.getStatistics().getMax() / getTimeUnit(),
					rowKey, columnKey);
		} else if (sort.equals("Sum")) {
			dataset.addValue(su.getStatistics().getSum() / getTimeUnit(),
					rowKey, columnKey);
		} else if (sort.equals("StandDev")) {
			dataset.addValue(su.getStatistics().getStandardDeviation()
					/ getTimeUnit(), rowKey, columnKey);
		} else if (sort.equals("Frequency")) {
			dataset.addValue(su.getStatistics().getN() / getTimeUnit(), rowKey,
					columnKey);
		}
	}

	protected double getValue(StatisticUnit su) {
		String sort = (String) measureSort.getValue();
		if (sort.equals("Minimum")) {
			return su.getStatistics().getMin() / getTimeUnit();
		} else if (sort.equals("Average")) {
			return su.getStatistics().getMean() / getTimeUnit();
		} else if (sort.equals("Median")) {
			return su.getStatistics().getPercentile(50) / getTimeUnit();
		} else if (sort.equals("Maximum")) {
			return su.getStatistics().getMax() / getTimeUnit();
		} else if (sort.equals("Sum")) {
			return su.getStatistics().getSum() / getTimeUnit();
		} else if (sort.equals("StandDev")) {
			return su.getStatistics().getStandardDeviation() / getTimeUnit();
		} else {
			return su.getStatistics().getN() / getTimeUnit();
		}
	}

	public void updatePerformance() {
		if (!absPerformance.getName().equals(InstancePerformance.getNameCode())) {
			if (dim1Sort.getValue().equals("task")) {
				if (dim2Sort.getValue().equals("task")) {
					absPerformance = performanceUI
							.getPerformance(TaskTPerformance.getNameCode());
					isItemA = true;
				} else if (dim2Sort.getValue().equals("originator")) {
					absPerformance = performanceUI
							.getPerformance(TaskOriginatorTPerformance
									.getNameCode());
					isItemA = false;
				} else if (dim2Sort.getValue().equals("instance")) {
					absPerformance = performanceUI
							.getPerformance(InstanceTaskTPerformance
									.getNameCode());
					isItemA = true;
				}
			} else if (dim1Sort.getValue().equals("instance")) {
				if (dim2Sort.getValue().equals("task")) {
					absPerformance = performanceUI
							.getPerformance(InstanceTaskTPerformance
									.getNameCode());
				} else if (dim2Sort.getValue().equals("originator")) {
					absPerformance = performanceUI
							.getPerformance(InstOriTPerformance.getNameCode());
				} else if (dim2Sort.getValue().equals("instance")) {
					absPerformance = performanceUI
							.getPerformance(InstanceTPerformance.getNameCode());
				}
				isItemA = false;
			} else if (dim1Sort.getValue().equals("originator")) {
				if (dim2Sort.getValue().equals("task")) {
					absPerformance = performanceUI
							.getPerformance(TaskOriginatorTPerformance
									.getNameCode());
				} else if (dim2Sort.getValue().equals("originator")) {
					absPerformance = performanceUI
							.getPerformance(OriginatorTPerformance
									.getNameCode());
				} else if (dim2Sort.getValue().equals("instance")) {
					absPerformance = performanceUI
							.getPerformance(InstOriTPerformance.getNameCode());
				}
				isItemA = true;
			}
		}
	}
}
