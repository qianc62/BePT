package org.processmining.analysis.performance.basicperformance.chart.defaultcategorylist;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.util.TableOrder;
import org.processmining.analysis.performance.basicperformance.BasicPerformanceAnalysisUI;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;

public class SpiderChart extends DefaultCategoryListChart {

	public SpiderChart() {
		super("Spider Chart", "Spider Chart");
	}

	public SpiderChart(BasicPerformanceAnalysisUI pm) {
		super("Spider Chart", "Spider Chart", pm);
	}

	protected JPanel getOptionPanel() {
		JPanel panel = super.getOptionPanel();
		panel.add(nullCheckBox);
		return panel;
	}

	protected JScrollPane getGraphPanel() {
		ArrayList<DefaultCategoryDataset> datasetArray;

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());
		if (absPerformance instanceof AbstractPerformance2D) {
			datasetArray = getDefaultCategoryListDataset((AbstractPerformance2D) absPerformance);
		} else {
			datasetArray = getDefaultCategoryListDataset(absPerformance);
		}

		if (datasetArray.size() == 1) {
			SpiderWebPlot plot = new SpiderWebPlot(datasetArray.get(0),
					TableOrder.BY_ROW);
			JFreeChart jchart = new JFreeChart(absPerformance.getName(),
					new Font("SansSerif", Font.PLAIN, 8), plot, false);
			ChartPanel chartPanel = new ChartPanel(jchart);
			panel.add(new JScrollPane(chartPanel), BorderLayout.CENTER);
		} else {
			JPanel tempPanel = new JPanel();
			GridLayout tempLayout = new GridLayout(0, 3);
			tempPanel.setLayout(tempLayout);
			for (int i = 0; i < datasetArray.size(); i++) {
				if (datasetArray.get(i).getRowCount() == 0)
					continue;
				String aItem = (String) datasetArray.get(i).getRowKey(0);
				SpiderWebPlot plot = new SpiderWebPlot(datasetArray.get(i),
						TableOrder.BY_ROW);
				JFreeChart jchart = new JFreeChart(aItem, new Font("SansSerif",
						Font.PLAIN, 8), plot, false);
				ChartPanel chartPanel = new ChartPanel(jchart);
				chartPanel.setPreferredSize(new java.awt.Dimension(280, 180));
				tempPanel.add(chartPanel);
			}
			tempPanel.setPreferredSize(new java.awt.Dimension(280 * 3,
					180 * (datasetArray.size() / 3 + 1)));
			panel.add(new JScrollPane(tempPanel), BorderLayout.CENTER);
		}
		scrollPane = new JScrollPane(panel);
		return scrollPane;
	}

}
