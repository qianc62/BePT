package org.processmining.analysis.performance.basicperformance.model;

import java.util.HashMap;
import java.util.List;
import java.util.Date;

import javax.swing.JPanel;

import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.time.SimpleTimePeriod;

import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;

/**
 * This class defines the interface for a performance, in which for a number of
 * performance values are stored.
 * 
 * @author Minseok Song (m.s.song@tue.nl)
 */

public abstract class AbstractPerformance2D extends AbstractPerformance {

	/**
	 * Set, holding all items present for any instance.
	 */
	protected List<String> items2;
	protected String itemName2;
	protected AbstractPerformance refPerformance;
	protected AbstractPerformance refPerformance2;
	protected HashMap<String, TaskSeries> item2SeriesMap = new HashMap<String, TaskSeries>();
	protected HashMap<String, TaskSeries> item2SeriesWaitingMap = new HashMap<String, TaskSeries>();
	protected HashMap<String, TaskSeries> item2SeriesSojournMap = new HashMap<String, TaskSeries>();

	/**
	 * Creates a new profile with the given name and description
	 */
	public AbstractPerformance2D(String aName, String aNameItem1,
			String aNameItem2, String aDescription) {
		super(aName, aDescription);
		itemName = aNameItem1;
		itemName2 = aNameItem2;
	}

	/**
	 * Creates a new profile with the given name and description
	 */
	public AbstractPerformance2D(String aName, String aNameItem1,
			String aNameItem2, String aDescription, List<String> aItems,
			List<String> bItems) {
		super(aName, aDescription, null);
		items = aItems;
		items2 = bItems;
		itemName = aNameItem1;
		itemName2 = aNameItem2;

		if (items != null && items2 != null) {
			for (String aItem : items) {
				for (String bItem : items2) {
					String itemName = aItem + " - " + bItem;
					StatisticUnit su = new StatisticUnit(itemName);
					executionMap.put(itemName, su);
					StatisticUnit su2 = new StatisticUnit(itemName);
					waitingMap.put(itemName, su2);
				}
				// for gantt chart
				TaskSeries taskSeries = new TaskSeries(aItem);
				item1SeriesMap.put(aItem, taskSeries);
			}
			for (String bItem : items2) {
				TaskSeries taskSeries = new TaskSeries(bItem);
				item2SeriesMap.put(bItem, taskSeries);
			}
		}
	}

	public HashMap<String, TaskSeries> getSecondTaskSeriesMap() {
		return item2SeriesMap;
	}

	public TaskSeries getSecondTaskSeries(String bItem) {
		return item2SeriesMap.get(bItem);
	}

	public HashMap<String, TaskSeries> getSecondTaskSeriesWaitingMap() {
		return item2SeriesWaitingMap;
	}

	public TaskSeries getSecondTaskSeriesWaiting(String aItem) {
		return item2SeriesWaitingMap.get(aItem);
	}

	public void addSecondTaskSeriesWaitingMap(String aItem, Date start, Date end) {
		TaskSeries taskSeries = item2SeriesWaitingMap.get(name);
		taskSeries.add(new Task(aItem, new SimpleTimePeriod(start, end)));
	}

	public HashMap<String, TaskSeries> getSecondTaskSeriesSojournMap() {
		return item2SeriesSojournMap;
	}

	public TaskSeries getSecondTaskSeriesSojourn(String aItem) {
		return item2SeriesSojournMap.get(aItem);
	}

	public void addSecondTaskSeriesSojournMap(String aItem, Date start, Date end) {
		TaskSeries taskSeries = item2SeriesSojournMap.get(name);
		taskSeries.add(new Task(aItem, new SimpleTimePeriod(start, end)));
	}

	public List<String> getSecondItems() {
		return items2;
	}

	public String getSecondItemName() {
		return itemName2;
	}

	public AbstractPerformance getFirstRefPerformance() {
		return refPerformance;
	}

	public AbstractPerformance getSecondRefPerformance() {
		return refPerformance2;
	}

	public void setRefPerformance(AbstractPerformance aRefPerformance,
			AbstractPerformance bRefPerformance) {
		refPerformance = aRefPerformance;
		refPerformance2 = bRefPerformance;
	}

	public void setSecondItems(List<String> bItems) {
		items2 = bItems;
	}

	public void addTaskSeries(String aItem, String bItem, Date start, Date end) {
		TaskSeries taskSeries = item1SeriesMap.get(aItem);
		try {
			if (taskSeries.get(bItem) == null) {
				taskSeries
						.add(new Task(bItem, new SimpleTimePeriod(start, end)));
			} else {
				taskSeries.add(new Task(bItem + "_" + indexGannt++,
						new SimpleTimePeriod(start, end)));
			}

			TaskSeries taskSeries2 = item2SeriesMap.get(bItem);
			if (taskSeries2.get(aItem) == null) {
				taskSeries2.add(new Task(aItem,
						new SimpleTimePeriod(start, end)));
			} else {
				taskSeries2.add(new Task(aItem + "_" + indexGannt++,
						new SimpleTimePeriod(start, end)));
			}
		} catch (Exception ce) {
			Message.add("Fail to add", Message.ERROR);
		}
	}

	public void addTaskSeriesWaiting(String aItem, String bItem, Date start,
			Date end) {
		TaskSeries taskSeries = item1SeriesWaitingMap.get(aItem);
		taskSeries.add(new Task(bItem + "_" + indexGannt++,
				new SimpleTimePeriod(start, end)));
		TaskSeries taskSeries2 = item2SeriesWaitingMap.get(bItem);
		taskSeries2.add(new Task(aItem + "_" + indexGannt++,
				new SimpleTimePeriod(start, end)));
	}

	public void addTaskSeriesSojourn(String aItem, String bItem, Date start,
			Date end) {
		TaskSeries taskSeries = item1SeriesSojournMap.get(aItem);
		taskSeries.add(new Task(bItem + "_" + indexGannt++,
				new SimpleTimePeriod(start, end)));
		TaskSeries taskSeries2 = item2SeriesSojournMap.get(bItem);
		taskSeries2.add(new Task(aItem + "_" + indexGannt++,
				new SimpleTimePeriod(start, end)));
	}

	public void removeOutlier(int min, int max) {
		for (String aItem : items) {
			for (String bItem : items2) {
				String itemName = aItem + " - " + bItem;
				executionMap.get(itemName).removeOutlier(min, max);
				waitingMap.get(itemName).removeOutlier(min, max);
			}
		}
	}
}
