package org.processmining.analysis.performance.basicperformance.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.deckfour.slickerbox.components.SmoothPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.MatrixSeriesCollection;
import org.jfree.data.xy.NormalizedMatrixSeries;
import org.jfree.util.TableOrder;
import org.processmining.analysis.performance.basicperformance.BasicPerformanceAnalysisUI;
import org.processmining.analysis.performance.basicperformance.model.StatisticUnit;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;

public class BubblesChart extends AbstractChart {

	public BubblesChart() {
		super("Bubbles Chart", "Bubbles Chart");
	}

	public BubblesChart(BasicPerformanceAnalysisUI pm) {
		super("Bubbles Chart", "Bubbles Chart", pm);
	}

	public JPanel getPanel(AbstractPerformance absPer) {
		DefaultCategoryDataset dataset;
		SmoothPanel panel = new SmoothPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());
		AbstractPerformance2D absPer2D = (AbstractPerformance2D) absPer;

		NormalizedMatrixSeries newSeries = new NormalizedMatrixSeries(
				"frequency", absPer2D.getItems().size(), absPer2D
						.getSecondItems().size());
		for (int i = 0; i < absPer2D.getItems().size(); i++) {
			String task = absPer2D.getItems().get(i);
			for (int j = 0; j < absPer2D.getSecondItems().size(); j++) {
				String originator = (String) absPer2D.getSecondItems().get(j);
				StatisticUnit su = absPer2D.getExecutionTimeSU(task + " - "
						+ originator);
				// taskOriMap.get(task+" - "+originator);
				if (su != null && su.getStatistics().getN() != 0) {
					newSeries.update(i, j, su.getStatistics().getN() * 100);
				}
			}
		}

		final MatrixSeriesCollection dataset2 = new MatrixSeriesCollection(
				newSeries);

		final JFreeChart chart = ChartFactory.createBubbleChart(
				"task-originator", "originator", "task", dataset2,
				PlotOrientation.HORIZONTAL, true, true, false);

		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000,
				Color.yellow));

		XYPlot plot = chart.getXYPlot();
		plot.setForegroundAlpha(0.5f);
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setLowerBound(-0.5);
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setLowerBound(-0.5);

		ChartPanel chartPanel = new ChartPanel(chart);
		// chartPanel.setVerticalZoom(true);
		// chartPanel.setHorizontalZoom(true);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 320));

		panel.add(new JScrollPane(chartPanel), BorderLayout.CENTER);
		return panel;
	}

}
