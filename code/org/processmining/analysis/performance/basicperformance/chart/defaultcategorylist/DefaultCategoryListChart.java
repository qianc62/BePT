package org.processmining.analysis.performance.basicperformance.chart.defaultcategorylist;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;

import org.jfree.data.category.DefaultCategoryDataset;
import org.processmining.analysis.performance.basicperformance.BasicPerformanceAnalysisUI;
import org.processmining.analysis.performance.basicperformance.model.StatisticUnit;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.processmining.analysis.performance.basicperformance.chart.defaultcategory.DefaultCategoryChart;

public class DefaultCategoryListChart extends DefaultCategoryChart implements
		ItemListener {

	protected JCheckBox nullCheckBox = new JCheckBox("Include null values");

	public DefaultCategoryListChart() {
		super("DefaultCategoryList Chart", "DefaultCategoryList Chart");
		nullCheckBox.setSelected(false);
		nullCheckBox.addItemListener(this);
	}

	public DefaultCategoryListChart(String name, String desc) {
		super(name, desc);
		nullCheckBox.setSelected(false);
		nullCheckBox.addItemListener(this);
	}

	public DefaultCategoryListChart(String name, String desc,
			BasicPerformanceAnalysisUI pm) {
		super(name, desc, pm);
		nullCheckBox.setSelected(false);
		nullCheckBox.addItemListener(this);
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if ((source == nullCheckBox)) {
			if (splitPane != null && scrollPane != null) {
				splitPane.remove(scrollPane);
			}
			splitPane.setRightComponent(getGraphPanel());
		}
	}

	// Spider Chart
	// //////////////////////////////////////////////////////////////////////////////
	protected ArrayList<DefaultCategoryDataset> getDefaultCategoryListDataset(
			AbstractPerformance performance) {
		List<String> items = performance.getItems();
		ArrayList<DefaultCategoryDataset> datasetArray = new ArrayList<DefaultCategoryDataset>();

		DefaultCategoryDataset tempdataset = new DefaultCategoryDataset();
		DefaultCategoryDataset tempdataset2 = new DefaultCategoryDataset();
		DefaultCategoryDataset tempdataset3 = new DefaultCategoryDataset();
		for (String aItem : items) {
			if (performanceSort.getValue().equals("Waiting Time")
					|| performanceSort.getValue().equals("Both")
					|| performanceSort.getValue().equals("All")) {
				StatisticUnit su = performance.getWaitingTimeSU(aItem);
				if (su != null && su.getStatistics().getN() != 0) {
					addDate(tempdataset, su, "Waiting time", aItem);
				} else if (nullCheckBox.isSelected()) {
					addDate(tempdataset, su, "Waiting time", aItem);
				}
			}
			if (performanceSort.getValue().equals("Working Time")
					|| performanceSort.getValue().equals("Both")
					|| performanceSort.getValue().equals("All")) {
				StatisticUnit su = performance.getExecutionTimeSU(aItem);
				if (su != null && su.getStatistics().getN() != 0) {
					addDate(tempdataset2, su, "Working time", aItem);
				} else if (nullCheckBox.isSelected()) {
					addDate(tempdataset2, su, "Working time", aItem);
				}
			}
			if (performanceSort.getValue().equals("Sojourn Time")
					|| performanceSort.getValue().equals("Both")
					|| performanceSort.getValue().equals("All")) {
				StatisticUnit su = performance.getSojournTimeSU(aItem);
				if (su != null && su.getStatistics().getN() != 0) {
					addDate(tempdataset3, su, "Sojourn time", aItem);
				} else if (nullCheckBox.isSelected()) {
					addDate(tempdataset3, su, "Sojourn time", aItem);
				}
			}
		}
		if (performanceSort.getValue().equals("Waiting Time")
				|| performanceSort.getValue().equals("Both")
				|| performanceSort.getValue().equals("All"))
			datasetArray.add(tempdataset);
		if (performanceSort.getValue().equals("Working Time")
				|| performanceSort.getValue().equals("Both")
				|| performanceSort.getValue().equals("All"))
			datasetArray.add(tempdataset2);
		if (performanceSort.getValue().equals("Sojourn Time")
				|| performanceSort.getValue().equals("Both")
				|| performanceSort.getValue().equals("All"))
			datasetArray.add(tempdataset3);
		return datasetArray;
	}

	protected ArrayList<DefaultCategoryDataset> getDefaultCategoryListDataset(
			AbstractPerformance2D performance2D) {
		List<String> items = performance2D.getItems();
		List<String> items2 = performance2D.getSecondItems();

		ArrayList<DefaultCategoryDataset> datasetArray = new ArrayList<DefaultCategoryDataset>();

		if (isItemA) {
			for (String aItem : items) {
				DefaultCategoryDataset tempdataset = new DefaultCategoryDataset();
				for (String bItem : items2) {
					if (performanceSort.getValue().equals("Working Time")) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(tempdataset, su, aItem, bItem);
						} else if (nullCheckBox.isSelected()) {
							addDate(tempdataset, su, aItem, bItem);
						}
					} else if (performanceSort.getValue()
							.equals("Waiting Time")) {
						StatisticUnit su = performance2D.getWaitingTimeSU(aItem
								+ " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(tempdataset, su, aItem, bItem);
						} else if (nullCheckBox.isSelected()) {
							addDate(tempdataset, su, aItem, bItem);
						}
					} else if (performanceSort.getValue().equals("Both")) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(tempdataset, su, aItem, bItem);
						} else if (nullCheckBox.isSelected()) {
							addDate(tempdataset, su, aItem, bItem);
						}
						su = performance2D.getWaitingTimeSU(aItem + " - "
								+ bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(tempdataset, su, aItem, bItem + "_w");
						} else if (nullCheckBox.isSelected()) {
							addDate(tempdataset, su, aItem, bItem + "_w");
						}
					}
				}
				datasetArray.add(tempdataset);
			}
		} else {
			for (String bItem : items2) {
				DefaultCategoryDataset tempdataset = new DefaultCategoryDataset();
				for (String aItem : items) {
					if (performanceSort.getValue().equals("Working Time")) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(tempdataset, su, bItem, aItem);
						} else if (nullCheckBox.isSelected()) {
							addDate(tempdataset, su, bItem, aItem);
						}
					} else if (performanceSort.getValue()
							.equals("Waiting Time")) {
						StatisticUnit su = performance2D.getWaitingTimeSU(aItem
								+ " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(tempdataset, su, bItem, aItem);
						} else if (nullCheckBox.isSelected()) {
							addDate(tempdataset, su, bItem, aItem);
						}
					} else if (performanceSort.getValue().equals("Both")) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(tempdataset, su, bItem, aItem);
						} else if (nullCheckBox.isSelected()) {
							addDate(tempdataset, su, bItem, aItem);
						}
						su = performance2D.getWaitingTimeSU(aItem + " - "
								+ bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(tempdataset, su, bItem, aItem + "_w");
						} else if (nullCheckBox.isSelected()) {
							addDate(tempdataset, su, bItem, aItem + "_w");
						}
					}
				}
				datasetArray.add(tempdataset);
			}
		}
		return datasetArray;
	}
}
