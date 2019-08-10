package org.processmining.analysis.performance.basicperformance.chart;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.processmining.analysis.performance.basicperformance.BasicPerformanceAnalysisUI;
import org.processmining.analysis.performance.basicperformance.model.StatisticUnit;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;

import org.processmining.analysis.performance.basicperformance.model.instance.InstancePerformance;

public class BoxAndWhiskerChart extends AbstractChart {
	boolean bLegend = true;

	public BoxAndWhiskerChart() {
		super("Box-and-Whisker", "Box-and-Whisker");
	}

	public BoxAndWhiskerChart(BasicPerformanceAnalysisUI pm) {
		super("Box-and-Whisker", "Box-and-Whisker", pm);
	}

	// Box-and-Whisker Chart
	// //////////////////////////////////////////////////////////////////////////////
	protected DefaultBoxAndWhiskerCategoryDataset getBoxAndWhiskerCategoryDataset(
			AbstractPerformance performance) {
		List<String> items = performance.getItems();

		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		for (String aItem : items) {
			if (performanceSort.getValue().equals("Waiting Time")) {
				StatisticUnit su = performance.getWaitingTimeSU(aItem);
				if (su != null && su.getStatistics().getN() != 0) {
					DescriptiveStatistics ds = su.getStatistics();
					List list = new ArrayList();
					for (int k = 0; k < ds.getN(); k++) {
						list.add(ds.getValues()[k] / getTimeUnit());
					}
					if (list.size() != 0) {
						dataset.add(list, aItem, "");
					}
				}
			} else if (performanceSort.getValue().equals("Working Time")) {
				StatisticUnit su2 = performance.getExecutionTimeSU(aItem);
				if (su2 != null && su2.getStatistics().getN() != 0) {
					DescriptiveStatistics ds = su2.getStatistics();
					List list = new ArrayList();
					for (int k = 0; k < ds.getN(); k++) {
						list.add(ds.getValues()[k] / getTimeUnit());
					}
					if (list.size() != 0) {
						dataset.add(list, aItem, "");
					}
				}
			} else if (performanceSort.getValue().equals("Sojourn Time")) {
				StatisticUnit su3 = performance.getSojournTimeSU(aItem);
				if (su3 != null && su3.getStatistics().getN() != 0) {
					DescriptiveStatistics ds = su3.getStatistics();
					List list = new ArrayList();
					for (int k = 0; k < ds.getN(); k++) {
						list.add(ds.getValues()[k] / getTimeUnit());
					}
					if (list.size() != 0) {
						dataset.add(list, aItem, "");
					}
				}
			}
		}
		if (items.size() > 20) {
			bLegend = false;
		} else {
			bLegend = true;
		}
		return dataset;
	}

	protected DefaultBoxAndWhiskerCategoryDataset getBoxAndWhiskerCategoryDataset(
			AbstractPerformance2D performance2D) {
		List<String> items = performance2D.getItems();
		List<String> items2 = performance2D.getSecondItems();

		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

		if (isItemA) {
			if (performanceSort.getValue().equals("Working Time")) {
				for (String aItem : items) {
					for (String bItem : items2) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							DescriptiveStatistics ds = su.getStatistics();
							List list = new ArrayList();
							for (int k = 0; k < ds.getN(); k++) {
								list.add(ds.getValues()[k] / getTimeUnit());
							}
							if (list.size() != 0) {
								dataset.add(list, aItem, bItem);
							}
						}
					}
				}
			} else if (performanceSort.getValue().equals("Waiting Time")) {
				for (String aItem : items) {
					for (String bItem : items2) {
						StatisticUnit su = performance2D.getWaitingTimeSU(aItem
								+ " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							DescriptiveStatistics ds = su.getStatistics();
							List list = new ArrayList();
							for (int k = 0; k < ds.getN(); k++) {
								list.add(ds.getValues()[k] / getTimeUnit());
							}
							if (list.size() != 0) {
								dataset.add(list, aItem, bItem);
							}
						}
					}
				}
			}
			if (items.size() > 20) {
				bLegend = false;
			} else {
				bLegend = true;
			}

		} else {
			if (performanceSort.getValue().equals("Working Time")) {
				for (String bItem : items2) {
					for (String aItem : items) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							DescriptiveStatistics ds = su.getStatistics();
							List list = new ArrayList();
							for (int k = 0; k < ds.getN(); k++) {
								list.add(ds.getValues()[k] / getTimeUnit());
							}
							if (list.size() != 0) {
								dataset.add(list, bItem, aItem);
							}
						}
					}
				}
			} else if (performanceSort.getValue().equals("Waiting Time")) {
				for (String bItem : items2) {
					for (String aItem : items) {
						StatisticUnit su = performance2D.getWaitingTimeSU(aItem
								+ " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							DescriptiveStatistics ds = su.getStatistics();
							List list = new ArrayList();
							for (int k = 0; k < ds.getN(); k++) {
								list.add(ds.getValues()[k] / getTimeUnit());
							}
							if (list.size() != 0) {
								dataset.add(list, bItem, aItem);
							}
						}
					}
				}
			}
			if (items2.size() > 20) {
				bLegend = false;
			} else {
				bLegend = true;
			}
		}

		return dataset;
	}

	protected JScrollPane getGraphPanel() {
		DefaultBoxAndWhiskerCategoryDataset dataset = null;
		if (absPerformance instanceof AbstractPerformance2D) {
			dataset = getBoxAndWhiskerCategoryDataset((AbstractPerformance2D) absPerformance);
		} else {
			dataset = getBoxAndWhiskerCategoryDataset(absPerformance);
		}

		String strCategory = absPerformance.getItemName();

		if (absPerformance instanceof AbstractPerformance2D) {
			strCategory = ((AbstractPerformance2D) absPerformance)
					.getSecondItemName();
		}
		JFreeChart chart;

		CategoryAxis xAxis = new CategoryAxis(strCategory);
		NumberAxis yAxis = new NumberAxis("Time");
		yAxis.setAutoRangeIncludesZero(false);
		BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
		renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
		CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

		chart = new JFreeChart(null, new Font("SansSerif", Font.BOLD, 14),
				plot, bLegend);
		chartPanel = new ChartPanel(chart);
		scrollPane = new JScrollPane(chartPanel);
		chartPanel.setBackground(colorBg);
		scrollPane.setBackground(colorBg);
		return scrollPane;
	}

	protected JPanel getOptionPanel() {
		initTimeSort();
		initDimSort();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		if (!absPerformance.getName().equals(InstancePerformance.getNameCode())) {
			panel.add(dim1Sort.getPropertyPanel());
			panel.add(dim2Sort.getPropertyPanel());
		}

		panel.add(timeUnitSort.getPropertyPanel());
		ArrayList<String> performanceList = new ArrayList<String>();
		if (absPerformance.getName().equals(InstancePerformance.getNameCode())) {
			performanceList.add("Sojourn Time");
		}
		performanceList.add("Working Time");
		performanceList.add("Waiting Time");

		performanceSort = new GUIPropertyListEnumeration("Time Sort:", "",
				performanceList, this, 150);
		panel.add(performanceSort.getPropertyPanel());
		panel.setBackground(colorBg);
		return panel;
	}
}
