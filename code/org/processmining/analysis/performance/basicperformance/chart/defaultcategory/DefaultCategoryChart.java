package org.processmining.analysis.performance.basicperformance.chart.defaultcategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.category.DefaultCategoryDataset;
import org.processmining.analysis.performance.basicperformance.BasicPerformanceAnalysisUI;
import org.processmining.analysis.performance.basicperformance.chart.AbstractChart;
import org.processmining.analysis.performance.basicperformance.model.StatisticUnit;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.processmining.framework.util.GUIPropertyListEnumeration;

public class DefaultCategoryChart extends AbstractChart {

	protected GUIPropertyListEnumeration dimSort = null;

	public DefaultCategoryChart() {
		super("DefaultCategory Chart", "DefaultCategory Chart");
		init2D3DSort();
	}

	public DefaultCategoryChart(String name, String desc) {
		super(name, desc);
		init2D3DSort();
	}

	public DefaultCategoryChart(String name, String desc,
			BasicPerformanceAnalysisUI pm) {
		super(name, desc, pm);
		init2D3DSort();
	}

	public void init2D3DSort() {
		ArrayList<String> dimList = new ArrayList<String>();
		dimList.add("2D");
		dimList.add("3D");
		dimSort = new GUIPropertyListEnumeration("Dimension:", "", dimList,
				this, 150);
	}

	// Default Category Dataset
	// //////////////////////////////////////////////////////////////////////
	protected DefaultCategoryDataset getDefaultCategoryDataset(
			AbstractPerformance2D performance2D) {
		List<String> items = performance2D.getItems();
		List<String> items2 = performance2D.getSecondItems();

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		if (isItemA) {
			if (performanceSort.getValue().equals("Working Time")) {
				for (String aItem : items) {
					for (String bItem : items2) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, aItem, bItem);
						}
					}
				}
			} else if (performanceSort.getValue().equals("Waiting Time")) {
				for (String aItem : items) {
					for (String bItem : items2) {
						StatisticUnit su = performance2D.getWaitingTimeSU(aItem
								+ " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, aItem, bItem);
						}
					}
				}
			} else if (performanceSort.getValue().equals("Sojourn Time")) {
				for (String aItem : items) {
					for (String bItem : items2) {
						StatisticUnit su = performance2D.getSojournTimeSU(aItem
								+ " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, aItem, bItem);
						}
					}
				}
			} else if (performanceSort.getValue().equals("Both")) {
				for (String aItem : items) {
					for (String bItem : items2) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, aItem, bItem);
						}

						su = performance2D.getWaitingTimeSU(aItem + " - "
								+ bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, aItem, bItem + "_w");
						}
					}
				}
			} else if (performanceSort.getValue().equals("All")) {
				for (String aItem : items) {
					for (String bItem : items2) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, aItem, bItem);
						}

						su = performance2D.getWaitingTimeSU(aItem + " - "
								+ bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, aItem, bItem + "_w");
						}

						su = performance2D.getSojournTimeSU(aItem + " - "
								+ bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, aItem, bItem + "_s");
						}
					}
				}

			}
			if (items.size() > 20)
				bLegend = false;
		} else {
			if (performanceSort.getValue().equals("Working Time")) {
				for (String bItem : items2) {
					for (String aItem : items) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, bItem, aItem);
						}
					}
				}
			} else if (performanceSort.getValue().equals("Waiting Time")) {
				for (String bItem : items2) {
					for (String aItem : items) {
						StatisticUnit su = performance2D.getWaitingTimeSU(aItem
								+ " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, bItem, aItem);
						}
					}
				}
			} else if (performanceSort.getValue().equals("Sojourn Time")) {
				for (String bItem : items2) {
					for (String aItem : items) {
						StatisticUnit su = performance2D.getSojournTimeSU(aItem
								+ " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, bItem, aItem);
						}
					}
				}
			} else if (performanceSort.getValue().equals("Both")) {
				for (String aItem : items) {
					for (String bItem : items2) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, bItem, aItem);
						}

						su = performance2D.getWaitingTimeSU(aItem + " - "
								+ bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, bItem, aItem + "_w");
						}
					}
				}
			} else if (performanceSort.getValue().equals("All")) {
				for (String aItem : items) {
					for (String bItem : items2) {
						StatisticUnit su = performance2D
								.getExecutionTimeSU(aItem + " - " + bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, bItem, aItem);
						}

						su = performance2D.getWaitingTimeSU(aItem + " - "
								+ bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, bItem, aItem + "_w");
						}

						su = performance2D.getSojournTimeSU(aItem + " - "
								+ bItem);
						if (su != null && su.getStatistics().getN() != 0) {
							addDate(dataset, su, bItem, aItem + "_s");
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

	protected DefaultCategoryDataset getDefaultCategoryDataset(
			AbstractPerformance performance) {
		List<String> items = sortItems(performance.getItems(), performance
				.getExecutionMap());

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (String aItem : items) {
			if (performanceSort.getValue().equals("Waiting Time")
					|| performanceSort.getValue().equals("Both")
					|| performanceSort.getValue().equals("All")) {
				StatisticUnit su = performance.getWaitingTimeSU(aItem);
				if (su != null && su.getStatistics().getN() != 0) {
					addDate(dataset, su, "waiting time", aItem);
				}
			}
			if (performanceSort.getValue().equals("Working Time")
					|| performanceSort.getValue().equals("Both")
					|| performanceSort.getValue().equals("All")) {
				StatisticUnit su2 = performance.getExecutionTimeSU(aItem);
				if (su2 != null && su2.getStatistics().getN() != 0) {
					addDate(dataset, su2, "working time", aItem);
				}
			}
			if (performanceSort.getValue().equals("Sojourn Time")
					|| performanceSort.getValue().equals("Both")
					|| performanceSort.getValue().equals("All")) {
				StatisticUnit su2 = performance.getSojournTimeSU(aItem);
				if (su2 != null && su2.getStatistics().getN() != 0) {
					addDate(dataset, su2, "Sojourn time", aItem);
				}
			}
		}
		if (items.size() > 20 || !performanceSort.getValue().equals("Both")) {
			bLegend = false;
		} else {
			bLegend = true;
		}
		return dataset;
	}

	protected List<String> sortItems(List<String> items,
			HashMap<String, StatisticUnit> map) {
		for (int i = 0; i < items.size() - 1; i++) {
			for (int k = i + 1; k < items.size(); k++) {
				if (getValue(map.get(items.get(i))) > getValue(map.get(items
						.get(k)))) {
					String temp = items.get(i);
					items.set(i, items.get(k));
					items.set(k, temp);
				}
			}
		}
		return items;
	}
}
