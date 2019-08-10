package org.processmining.analysis.performance.basicperformance.chart;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.deckfour.slickerbox.components.SmoothPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.processmining.analysis.performance.basicperformance.BasicPerformanceAnalysisUI;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.processmining.analysis.performance.basicperformance.model.instance.InstancePerformance;
import org.processmining.analysis.performance.basicperformance.model.task.InstOriTPerformance;
import org.processmining.analysis.performance.basicperformance.model.task.InstanceTaskTPerformance;
import org.processmining.analysis.performance.basicperformance.model.task.TaskOriginatorTPerformance;
import org.processmining.framework.util.GUIPropertyListEnumeration;

public class GanttChart extends AbstractChart {

	protected GUIPropertyListEnumeration displaySort;

	public GanttChart() {
		super("Gantt Chart", "Gantt Chart");
		initGUI();
	}

	public GanttChart(BasicPerformanceAnalysisUI pm) {
		super("Gantt Chart", "Gantt Chart", pm);
		initGUI();
	}

	public void initGUI() {
		ArrayList<String> displayList = new ArrayList<String>();
		displayList.add("Single");
		displayList.add("Multi");
		displaySort = new GUIPropertyListEnumeration("Display:", "",
				displayList, this, 150);
	}

	// Gantt Chart
	// //////////////////////////////////////////////////////////////////////////////
	public JScrollPane getPanel2(AbstractPerformance absPer) {
		SmoothPanel panel = new SmoothPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());

		String label;
		JFreeChart chart;
		TaskSeriesCollection dataset = new TaskSeriesCollection();

		for (TaskSeries ts : absPer.getFirstTaskSeriesMap().values()) {
			dataset.add(ts);
		}
		label = absPer.getName();
		int number = absPer.getFirstTaskSeriesMap().values().size();

		chart = ChartFactory.createGanttChart(null, // chart title
				label, // domain axis label
				"Date", // range axis label
				dataset, // data
				false, // include legend
				true, // tooltips
				false // urls
				);

		chartPanel = new ChartPanel(chart);
		scrollPane = new JScrollPane(chartPanel);
		chartPanel.setBackground(colorBg);
		scrollPane.setBackground(colorBg);
		return scrollPane;
	}

	protected JScrollPane getGraphPanel_multi() {
		AbstractPerformance2D performance2D;
		SmoothPanel panel = new SmoothPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());

		performance2D = (AbstractPerformance2D) absPerformance;

		String label;
		JFreeChart chart;
		TaskSeriesCollection dataset = new TaskSeriesCollection();
		int number = 0;
		bLegend = false;
		JPanel jtempPanel = new JPanel();
		GridLayout tempLayout = new GridLayout(0, 3);
		jtempPanel.setLayout(tempLayout);

		if (isItemA) {
			label = performance2D.getSecondItemName();
			for (TaskSeries ts : performance2D.getFirstTaskSeriesMap().values()) {
				dataset = new TaskSeriesCollection();
				dataset.add(ts);

				chart = ChartFactory.createGanttChart(String.valueOf(ts
						.getKey()), // chart title
						label, // domain axis label
						"Date", // range axis label
						dataset, // data
						bLegend, // include legend
						true, // tooltips
						false // urls
						);

				ChartPanel tempChartPanel = new ChartPanel(chart);
				tempChartPanel
						.setPreferredSize(new java.awt.Dimension(250, 160));
				jtempPanel.add(tempChartPanel);
			}
			if (performance2D.getSecondItems().size() < 10)
				bLegend = true;

		} else {
			label = performance2D.getItemName();
			for (TaskSeries ts : performance2D.getSecondTaskSeriesMap()
					.values()) {

				dataset = new TaskSeriesCollection();
				dataset.add(ts);

				chart = ChartFactory.createGanttChart(String.valueOf(ts
						.getKey()), // chart title
						label, // domain axis label
						"Date", // range axis label
						dataset, // data
						bLegend, // include legend
						true, // tooltips
						false // urls
						);

				ChartPanel tempChartPanel = new ChartPanel(chart);
				tempChartPanel
						.setPreferredSize(new java.awt.Dimension(250, 160));
				jtempPanel.add(tempChartPanel);
			}
			if (performance2D.getItems().size() < 10)
				bLegend = true;
		}

		scrollPane = new JScrollPane(jtempPanel);
		scrollPane.setBackground(colorBg);
		return scrollPane;
	}

	protected JScrollPane getGraphPanel() {
		AbstractPerformance2D performance2D;
		SmoothPanel panel = new SmoothPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());

		if (absPerformance == null) {
			JTextArea textarea = new JTextArea(
					"Two different dimentions are required");
			panel.add(textarea);
			scrollPane = new JScrollPane(panel);
			textarea.setBackground(colorBg);
			return scrollPane;
		}

		if (absPerformance instanceof AbstractPerformance2D) {
			performance2D = (AbstractPerformance2D) absPerformance;
		} else {
			return getPanel2(absPerformance);
		}

		if (displaySort.getValue().equals("Multi")) {
			return getGraphPanel_multi();
		}

		String label;
		JFreeChart chart;
		TaskSeriesCollection dataset = new TaskSeriesCollection();
		int number = 0;
		bLegend = false;
		if (isItemA) {
			for (TaskSeries ts : performance2D.getFirstTaskSeriesMap().values()) {
				dataset.add(ts);
			}
			if (performance2D.getSecondItems().size() < 10)
				bLegend = true;
			label = performance2D.getSecondItemName();
		} else {
			for (TaskSeries ts : performance2D.getSecondTaskSeriesMap()
					.values()) {
				dataset.add(ts);
			}
			if (performance2D.getItems().size() < 10)
				bLegend = true;
			label = performance2D.getName();
		}

		number = Math.max(performance2D.getSecondTaskSeriesMap().values()
				.size(), performance2D.getFirstTaskSeriesMap().values().size());

		chart = ChartFactory.createGanttChart("Gantt Chart", // chart title
				label, // domain axis label
				"Date", // range axis label
				dataset, // data
				bLegend, // include legend
				true, // tooltips
				false // urls
				);

		chartPanel = new ChartPanel(chart);
		scrollPane = new JScrollPane(chartPanel);
		chartPanel.setBackground(colorBg);
		scrollPane.setBackground(colorBg);
		return scrollPane;
	}

	protected JPanel getOptionPanel() {
		initDimSort();
		initGUI();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		if (absPerformance == null) {
			panel.add(displaySort.getPropertyPanel());
			panel.add(dim1Sort.getPropertyPanel());
			panel.add(dim2Sort.getPropertyPanel());
		} else if (!absPerformance.getName().equals(
				InstancePerformance.getNameCode())) {
			panel.add(displaySort.getPropertyPanel());
			panel.add(dim1Sort.getPropertyPanel());
			panel.add(dim2Sort.getPropertyPanel());
		}
		panel.setBackground(colorBg);
		return panel;
	}

	public void updatePerformance() {
		if (absPerformance == null
				|| !absPerformance.getName().equals(
						InstancePerformance.getNameCode())) {
			if (dim1Sort.getValue().equals("task")) {
				if (dim2Sort.getValue().equals("task")) {
					absPerformance = null;
					// absPerformance =
					// performanceUI.getPerformance(TaskOriginatorTPerformance.getNameCode());
					// isItemA = false;
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
			} else if (performanceUI.isInstanceDim()
					&& dim1Sort.getValue().equals("instance")) {
				if (dim2Sort.getValue().equals("task")) {
					absPerformance = performanceUI
							.getPerformance(InstanceTaskTPerformance
									.getNameCode());
				} else if (dim2Sort.getValue().equals("originator")) {
					absPerformance = performanceUI
							.getPerformance(InstOriTPerformance.getNameCode());
				} else if (dim2Sort.getValue().equals("instance")) {
					absPerformance = null;
					// absPerformance =
					// performanceUI.getPerformance(InstanceTaskTPerformance.getNameCode());
				}
				isItemA = false;
			} else if (performanceUI.isOriginatorDim()
					&& dim1Sort.getValue().equals("originator")) {
				if (dim2Sort.getValue().equals("task")) {
					absPerformance = performanceUI
							.getPerformance(TaskOriginatorTPerformance
									.getNameCode());
				} else if (dim2Sort.getValue().equals("originator")) {
					absPerformance = null;
					// absPerformance =
					// performanceUI.getPerformance(InstOriTPerformance.getNameCode());
				} else if (dim2Sort.getValue().equals("instance")) {
					absPerformance = performanceUI
							.getPerformance(InstOriTPerformance.getNameCode());
				}
				isItemA = true;
			}
		}
	}
}
