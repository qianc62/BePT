package org.processmining.analysis.performance.basicperformance.chart.defaultcategorylist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.processmining.analysis.performance.basicperformance.BasicPerformanceAnalysisUI;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;

public class BarListChart extends DefaultCategoryListChart {

	GUIPropertyListEnumeration displaySort;
	GUIPropertyListEnumeration orientationSort;
	PlotOrientation order;
	String strCategory;

	public BarListChart(BasicPerformanceAnalysisUI pm) {
		super("Bar Chart", "Bar Chart", pm);
		initGUI();
	}

	public BarListChart() {
		super("Bar Chart", "Bar Chart");
		initGUI();
	}

	public void initGUI() {
		ArrayList<String> displayList = new ArrayList<String>();
		displayList.add("Single");
		displayList.add("Multiple");
		displayList.add("Stacked");
		displaySort = new GUIPropertyListEnumeration("Display:", "",
				displayList, this, 150);

		ArrayList<String> orientationList = new ArrayList<String>();
		orientationList.add("Vertical");
		orientationList.add("Horizontal");
		orientationSort = new GUIPropertyListEnumeration("Orientation:", "",
				orientationList, this, 150);
	}

	protected JScrollPane getGraphPanel() {
		if (displaySort.getValue().equals("Single")) {
			return getGraphPanel_Single();
		} else if (displaySort.getValue().equals("Multiple")) {
			return getGraphPanel_Multi();
		} else {
			return getGraphPanel_Stacked();
		}
	}

	protected JScrollPane getGraphPanel_Single() {
		bLegend = true;
		DefaultCategoryDataset dataset;

		if (absPerformance instanceof AbstractPerformance2D) {
			dataset = getDefaultCategoryDataset((AbstractPerformance2D) absPerformance);
		} else {
			dataset = getDefaultCategoryDataset(absPerformance);
		}

		order = PlotOrientation.VERTICAL;
		if (orientationSort.getValue().equals("Horizontal")) {
			order = PlotOrientation.HORIZONTAL;
		}

		String strCategory = absPerformance.getItemName();

		if (absPerformance instanceof AbstractPerformance2D) {
			if (isItemA) {
				strCategory = ((AbstractPerformance2D) absPerformance)
						.getSecondItemName();
			} else {
				strCategory = ((AbstractPerformance2D) absPerformance)
						.getItemName();
			}
		}

		if (dimSort.getValue().equals("2D")) {
			chart = ChartFactory.createBarChart(null, strCategory, null,
					dataset, order, bLegend, true, false);
		} else if (dimSort.getValue().equals("3D")) {
			chart = ChartFactory.createBarChart3D(null, strCategory, null,
					dataset, order, bLegend, true, false);
		}

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		CategoryAxis axis = plot.getDomainAxis();
		axis.setLowerMargin(0.03);
		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		if (measureSort.getValue().equals("Frequency")) {
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		}
		rangeAxis.setUpperMargin(0.10);

		chartPanel = new ChartPanel(chart, true);
		// added
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setMinimumDrawHeight(10);
		chartPanel.setMaximumDrawHeight(2000);
		chartPanel.setMinimumDrawWidth(20);
		chartPanel.setMaximumDrawWidth(2000);

		// chart domain axis (hours) allows user to use mouse to zoom in
		// and out by dragging cursor across domain axis
		chartPanel.setMouseZoomable(true);
		chartPanel.setDomainZoomable(true);
		chartPanel.setRangeZoomable(true);

		scrollPane = new JScrollPane(chartPanel);
		return scrollPane;
	}

	protected JScrollPane getGraphPanel_Multi() {
		ArrayList<DefaultCategoryDataset> datasetArray;

		order = PlotOrientation.VERTICAL;
		if (orientationSort.getValue().equals("Horizontal")) {
			order = PlotOrientation.HORIZONTAL;
		}

		strCategory = absPerformance.getItemName();

		if (absPerformance instanceof AbstractPerformance2D) {
			if (isItemA) {
				strCategory = ((AbstractPerformance2D) absPerformance)
						.getSecondItemName();
			} else {
				strCategory = ((AbstractPerformance2D) absPerformance)
						.getItemName();
			}
		}

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());
		if (absPerformance instanceof AbstractPerformance2D) {
			datasetArray = getDefaultCategoryListDataset((AbstractPerformance2D) absPerformance);
		} else {
			datasetArray = getDefaultCategoryListDataset(absPerformance);
		}
		int numChart = 0;
		if (datasetArray.size() == 1) {
			addChart(panel, datasetArray.get(0));
		} else {
			JPanel tempPanel = new JPanel();
			GridLayout tempLayout = new GridLayout(0, 3);
			tempPanel.setLayout(tempLayout);
			for (int i = 0; i < datasetArray.size(); i++) {
				if (datasetArray.get(i).getRowCount() == 0)
					continue;
				addChart(tempPanel, datasetArray.get(i));
				numChart++;
			}
			tempPanel.setPreferredSize(new java.awt.Dimension(250 * 3,
					160 * (numChart / 3 + 1)));
			panel.add(new JScrollPane(tempPanel), BorderLayout.CENTER);
		}
		scrollPane = new JScrollPane(panel);
		return scrollPane;
	}

	protected JScrollPane getGraphPanel_Stacked() {
		DefaultCategoryDataset dataset;
		if (absPerformance instanceof AbstractPerformance2D) {
			dataset = getDefaultCategoryDataset((AbstractPerformance2D) absPerformance);
		} else {
			dataset = getDefaultCategoryDataset(absPerformance);
		}

		PlotOrientation order = PlotOrientation.VERTICAL;
		if (orientationSort.getValue().equals("Horizontal")) {
			order = PlotOrientation.HORIZONTAL;
		}

		String strCategory = absPerformance.getItemName();
		if (absPerformance instanceof AbstractPerformance2D) {
			if (isItemA) {
				strCategory = ((AbstractPerformance2D) absPerformance)
						.getSecondItemName();
			} else {
				strCategory = ((AbstractPerformance2D) absPerformance)
						.getItemName();
			}
		}
		if (dimSort.getValue().equals("2D")) {
			chart = ChartFactory.createStackedBarChart(null, strCategory,
					"time", dataset, order, bLegend, true, false);
		} else if (dimSort.getValue().equals("3D")) {
			chart = ChartFactory.createStackedBarChart3D(null, strCategory,
					"time", dataset, order, bLegend, true, false);
		}

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		if (measureSort.getValue().equals("Frequency")) {
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		}
		rangeAxis.setUpperMargin(0.15);

		chartPanel = new ChartPanel(chart, false);
		scrollPane = new JScrollPane(chartPanel);
		return scrollPane;
	}

	private void addChart(JPanel panel, DefaultCategoryDataset dataset) {
		String aItem = (String) dataset.getRowKey(0);
		JFreeChart chart;
		if (dimSort.getValue().equals("2D")) {
			chart = ChartFactory.createBarChart(aItem, strCategory, null,
					dataset, order, false, true, false);
		} else {
			chart = ChartFactory.createBarChart3D(aItem, strCategory, null,
					dataset, order, false, true, false);
		}
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		CategoryAxis axis = plot.getDomainAxis();
		axis.setLowerMargin(0.03);
		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		if (measureSort.getValue().equals("Frequency")) {
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		}
		rangeAxis.setUpperMargin(0.10);

		ChartPanel chartPanel = new ChartPanel(chart, true);
		chartPanel.setPreferredSize(new java.awt.Dimension(250, 160));
		panel.add(new JScrollPane(chartPanel), BorderLayout.CENTER);
	}

	protected JPanel getOptionPanel() {
		JPanel panel = super.getOptionPanel();
		panel.add(dimSort.getPropertyPanel());
		panel.add(displaySort.getPropertyPanel());
		panel.add(orientationSort.getPropertyPanel());
		return panel;
	}
}
