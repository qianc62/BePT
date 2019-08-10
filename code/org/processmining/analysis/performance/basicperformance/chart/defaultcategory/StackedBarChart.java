package org.processmining.analysis.performance.basicperformance.chart.defaultcategory;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;

import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.processmining.framework.util.GUIPropertyListEnumeration;

public class StackedBarChart extends DefaultCategoryChart {

	GUIPropertyListEnumeration orientationSort;

	public StackedBarChart() {
		super("Stacked Bar Chart", "Stacked Bar Chart");
		ArrayList<String> orientationList = new ArrayList<String>();
		orientationList.add("Vertical");
		orientationList.add("Horizontal");
		orientationSort = new GUIPropertyListEnumeration("Display:", "",
				orientationList, this, 150);
	}

	// Pie Chart
	// //////////////////////////////////////////////////////////////////////////////
	protected JScrollPane getGraphPanel() {
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
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setUpperMargin(0.15);

		chartPanel = new ChartPanel(chart, false);
		scrollPane = new JScrollPane(chartPanel);
		return scrollPane;
	}

	protected JPanel getOptionPanel() {
		JPanel panel = super.getOptionPanel();
		panel.add(dimSort.getPropertyPanel());
		panel.add(orientationSort.getPropertyPanel());
		return panel;
	}
}
