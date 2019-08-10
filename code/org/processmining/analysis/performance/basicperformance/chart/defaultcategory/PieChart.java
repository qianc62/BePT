package org.processmining.analysis.performance.basicperformance.chart.defaultcategory;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.util.TableOrder;
import org.jfree.data.general.DefaultPieDataset;

import org.processmining.analysis.performance.basicperformance.BasicPerformanceAnalysisUI;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.processmining.analysis.performance.basicperformance.model.StatisticUnit;

public class PieChart extends DefaultCategoryChart {

	public PieChart() {
		super("Pie Chart", "Pie Chart");
	}

	public PieChart(BasicPerformanceAnalysisUI pm) {
		super("Pie Chart", "Pie Chart", pm);
	}

	protected ArrayList<DefaultPieDataset> getPieDatasetList(
			AbstractPerformance2D performance2D) {
		List<String> items = performance2D.getItems();
		List<String> items2 = performance2D.getSecondItems();

		ArrayList<DefaultPieDataset> dataset = new ArrayList<DefaultPieDataset>();
		if (isItemA) {
			if (performanceSort.getValue().equals("Working Time")) {
				for (String aItem : items) {
					DefaultPieDataset dpd = new DefaultPieDataset();
					for (String bItem : items2) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addData(dpd, su, bItem);
						}
					}
					dataset.add(dpd);
				}
			} else if (performanceSort.getValue().equals("Waiting Time")) {
				for (String aItem : items) {
					DefaultPieDataset dpd = new DefaultPieDataset();
					for (String bItem : items2) {
						StatisticUnit su = performance2D.getWaitingTimeSU(aItem
								+ " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addData(dpd, su, bItem);
						}
					}
					dataset.add(dpd);
				}
			} else if (performanceSort.getValue().equals("Both")) {
				for (String aItem : items) {
					DefaultPieDataset dpd = new DefaultPieDataset();
					for (String bItem : items2) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addData(dpd, su, bItem);
						}

						su = performance2D.getWaitingTimeSU(aItem + " - "
								+ bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addData(dpd, su, bItem + "_w");
						}
					}
					dataset.add(dpd);
				}

			}
			if (items.size() > 20)
				bLegend = false;
		} else {
			if (performanceSort.getValue().equals("Working Time")) {
				for (String bItem : items2) {
					DefaultPieDataset dpd = new DefaultPieDataset();
					for (String aItem : items) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addData(dpd, su, aItem);
						}
					}
					dataset.add(dpd);
				}
			} else if (performanceSort.getValue().equals("Waiting Time")) {
				for (String bItem : items2) {
					DefaultPieDataset dpd = new DefaultPieDataset();
					for (String aItem : items) {
						StatisticUnit su = performance2D.getWaitingTimeSU(aItem
								+ " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addData(dpd, su, aItem);
						}
					}
					dataset.add(dpd);
				}
			} else if (performanceSort.getValue().equals("Both")) {
				for (String bItem : items2) {
					DefaultPieDataset dpd = new DefaultPieDataset();
					for (String aItem : items) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addData(dpd, su, aItem);
						}

						su = performance2D.getWaitingTimeSU(aItem + " - "
								+ bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addData(dpd, su, aItem + "_w");
						}
					}
					dataset.add(dpd);
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

	protected void addData(DefaultPieDataset dataset, StatisticUnit su,
			String columnKey) {
		String sort = (String) measureSort.getValue();
		if (sort.equals("Minimum")) {
			dataset.setValue(columnKey, su.getStatistics().getMin()
					/ getTimeUnit());
		} else if (sort.equals("Average")) {
			dataset.setValue(columnKey, su.getStatistics().getMean()
					/ getTimeUnit());
		} else if (sort.equals("Median")) {
			dataset.setValue(columnKey, su.getStatistics().getPercentile(50)
					/ getTimeUnit());
		} else if (sort.equals("Maximum")) {
			dataset.setValue(columnKey, su.getStatistics().getMax()
					/ getTimeUnit());
		} else if (sort.equals("Sum")) {
			dataset.setValue(columnKey, su.getStatistics().getSum()
					/ getTimeUnit());
		} else if (sort.equals("StandDev")) {
			dataset.setValue(columnKey, su.getStatistics()
					.getStandardDeviation()
					/ getTimeUnit());
		} else if (sort.equals("Frequency")) {
			dataset.setValue(columnKey, su.getStatistics().getN()
					/ getTimeUnit());
		}
	}

	protected JScrollPane getGraphPanel() {
		bLegend = true;

		DefaultCategoryDataset dataset = null;
		ArrayList<DefaultPieDataset> dataset2;
		if (absPerformance instanceof AbstractPerformance2D) {
			int num = 0;
			num = (isItemA) ? absPerformance.getItems().size()
					: ((AbstractPerformance2D) absPerformance).getSecondItems()
							.size();
			if (num > 10) {
				dataset2 = getPieDatasetList((AbstractPerformance2D) absPerformance);
				JPanel tempPanel = new JPanel();
				GridLayout tempLayout = new GridLayout(0, 3);
				tempPanel.setLayout(tempLayout);
				List<String> items = null;
				if (isItemA) {
					items = absPerformance.getItems();

				} else {
					items = ((AbstractPerformance2D) absPerformance)
							.getSecondItems();
				}
				int numChart = 0;
				for (int i = 0; i < items.size(); i++) {
					if (dataset2.get(i).getItemCount() < 1)
						continue;
					numChart++;
					boolean legend = (dataset2.get(i).getItemCount() > 10) ? false
							: true;
					JFreeChart chart1 = ChartFactory.createPieChart(items
							.get(i), dataset2.get(i), legend, false, false);
					PiePlot plot1 = (PiePlot) chart1.getPlot();
					plot1.setCircular(true);
					ChartPanel chartPanel = new ChartPanel(chart1);
					chartPanel
							.setPreferredSize(new java.awt.Dimension(280, 180));
					tempPanel.add(chartPanel);
				}
				tempPanel.setPreferredSize(new java.awt.Dimension(280 * 3,
						180 * (numChart / 3 + 1)));
				scrollPane = new JScrollPane(tempPanel);
				return scrollPane;
			} else {
				dataset = getDefaultCategoryDataset((AbstractPerformance2D) absPerformance);
				if (isItemA) {
					if (((AbstractPerformance2D) absPerformance)
							.getSecondItems().size() > 20)
						bLegend = false;
				} else {
					if (absPerformance.getItems().size() > 20)
						bLegend = false;
				}
			}

		} else {
			dataset = getDefaultCategoryDataset(absPerformance);
		}

		TableOrder order = TableOrder.BY_ROW;
		if (dimSort.getValue().equals("2D")) {
			chart = ChartFactory.createMultiplePieChart(null, dataset, order,
					bLegend, true, false);
		} else if (dimSort.getValue().equals("3D")) {
			chart = ChartFactory.createMultiplePieChart3D(null, dataset, order,
					bLegend, true, false);
		}

		chart.setBackgroundPaint(new Color(216, 255, 216));

		MultiplePiePlot plot = (MultiplePiePlot) chart.getPlot();
		JFreeChart subchart = plot.getPieChart();
		plot.setLimit(0.10);
		PiePlot p = (PiePlot) subchart.getPlot();
		p.setLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		p.setInteriorGap(0.30);

		chartPanel = new ChartPanel(chart, true);
		chartPanel.setMouseZoomable(true);
		chartPanel.setDomainZoomable(true);
		chartPanel.setRangeZoomable(true);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setMinimumDrawHeight(10);
		chartPanel.setMaximumDrawHeight(2000);
		chartPanel.setMinimumDrawWidth(20);
		chartPanel.setMaximumDrawWidth(2000);
		scrollPane = new JScrollPane(chartPanel);
		return scrollPane;
	}

	protected JPanel getOptionPanel() {
		JPanel panel = super.getOptionPanel();
		panel.add(dimSort.getPropertyPanel());
		return panel;
	}
}
