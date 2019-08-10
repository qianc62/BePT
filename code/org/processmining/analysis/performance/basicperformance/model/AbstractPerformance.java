package org.processmining.analysis.performance.basicperformance.model;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.time.SimpleTimePeriod;
import org.processmining.framework.ui.slicker.ProgressPanel;

/**
 * This class defines the interface for a performance, in which for a number of
 * performance values are stored.
 * 
 * @author Minseok Song (m.s.song@tue.nl)
 */

public abstract class AbstractPerformance {

	public static int indexGannt = 0;

	/**
	 * the name of this profile
	 */
	protected String name;

	/**
	 * the name of the item
	 */
	protected String itemName;

	/**
	 * human-readable description (1 sentence) of what is measured
	 */
	protected String description;

	/**
	 * Set, holding all items present for any instance.
	 */
	protected List<String> items;

	protected HashMap<String, StatisticUnit> executionMap = new HashMap<String, StatisticUnit>();
	protected HashMap<String, StatisticUnit> waitingMap = new HashMap<String, StatisticUnit>();
	protected HashMap<String, StatisticUnit> sojournMap = new HashMap<String, StatisticUnit>();
	protected HashMap<String, TaskSeries> item1SeriesMap = new HashMap<String, TaskSeries>();
	protected HashMap<String, TaskSeries> item1SeriesWaitingMap = new HashMap<String, TaskSeries>();
	protected HashMap<String, TaskSeries> item1SeriesSojournMap = new HashMap<String, TaskSeries>();

	/**
	 * Creates a new profile with the given name and description
	 */
	public AbstractPerformance(String aName, String aDescription) {
		name = aName;
		itemName = aName;
		description = aDescription;
	}

	/**
	 * Creates a new profile with the given name and description
	 */
	public AbstractPerformance(String aName, String aDescription,
			List<String> aItems) {
		name = aName;
		itemName = aName;
		description = aDescription;
		items = aItems;
		if (items != null) {
			for (String aItem : items) {
				StatisticUnit su = new StatisticUnit(aItem);
				executionMap.put(aItem, su);
				StatisticUnit su2 = new StatisticUnit(aItem);
				waitingMap.put(aItem, su2);
				StatisticUnit su3 = new StatisticUnit(aItem);
				sojournMap.put(aItem, su3);
				// for gantt chart
			}
			TaskSeries taskSeries = new TaskSeries(name);
			item1SeriesMap.put(name, taskSeries);
		}

	}

	/**
	 * Creates a new profile with the given name and description
	 */
	/*
	 * public AbstractPerformance(String aName, String aDescription,
	 * List<String> aItems) { name = aName; itemName = aName; description =
	 * aDescription; items = aItems; if(items!=null) { for (String aItem :
	 * items) { StatisticUnit su = new StatisticUnit(aItem);
	 * executionMap.put(aItem, su); StatisticUnit su2 = new
	 * StatisticUnit(aItem); waitingMap.put(aItem, su2); StatisticUnit su3 = new
	 * StatisticUnit(aItem); sojournMap.put(aItem, su3); // for gantt chart }
	 * TaskSeries taskSeries = new TaskSeries(name); item1SeriesMap.put(name,
	 * taskSeries); } }
	 */
	public void setItems(List<String> aItems) {
		items = aItems;
	}

	public List<String> getItems() {
		return items;
	}

	public String getItemName() {
		return itemName;
	}

	/**
	 * Returns the name of the component of the performance
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the description of the component of the performance
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the name of the component of the performance
	 */
	public String toString() {
		return name;
	}

	public static void initIndex() {
		indexGannt = 0;
	}

	/**
	 * Add a value to the data set for a Item
	 */
	public void addExecutionTime(String aItem, double value) {
		if (executionMap.get(aItem) != null)
			executionMap.get(aItem).addValue(value);
	}

	public void addWaitingTime(String aItem, double value) {
		if (waitingMap.get(aItem) != null)
			waitingMap.get(aItem).addValue(value);
	}

	public void addSojournTime(String aItem, double value) {
		if (sojournMap.get(aItem) != null)
			sojournMap.get(aItem).addValue(value);
	}

	public HashMap<String, StatisticUnit> getExecutionMap() {
		return executionMap;
	}

	public HashMap<String, StatisticUnit> getWaitingMap() {
		return waitingMap;
	}

	public HashMap<String, StatisticUnit> getSojournMap() {
		return sojournMap;
	}

	public StatisticUnit getExecutionTimeSU(String aItem) {
		return executionMap.get(aItem);
	}

	public StatisticUnit getWaitingTimeSU(String aItem) {
		return waitingMap.get(aItem);
	}

	public StatisticUnit getSojournTimeSU(String aItem) {
		return sojournMap.get(aItem);
	}

	public HashMap<String, TaskSeries> getFirstTaskSeriesMap() {
		return item1SeriesMap;
	}

	public TaskSeries getFirstTaskSeries(String aItem) {
		return item1SeriesMap.get(aItem);
	}

	public void addTaskSeries(String aItem, Date start, Date end) {
		TaskSeries taskSeries = item1SeriesMap.get(name);
		taskSeries.add(new Task(aItem + "_" + indexGannt++,
				new SimpleTimePeriod(start, end)));
	}

	public HashMap<String, TaskSeries> getFirstTaskSeriesWaitingMap() {
		return item1SeriesWaitingMap;
	}

	public TaskSeries getTaskSeriesWaiting(String aItem) {
		return item1SeriesWaitingMap.get(aItem);
	}

	public void addTaskSeriesWaitingMap(String aItem, Date start, Date end) {
		TaskSeries taskSeries = item1SeriesWaitingMap.get(name);
		if (taskSeries.get(aItem) == null) {
			taskSeries.add(new Task(aItem, new SimpleTimePeriod(start, end)));
		} else {
			taskSeries.add(new Task(aItem + "_" + indexGannt++,
					new SimpleTimePeriod(start, end)));
		}
	}

	public HashMap<String, TaskSeries> getFirstTaskSeriesSojournMap() {
		return item1SeriesSojournMap;
	}

	public TaskSeries getTaskSeriesSojourn(String aItem) {
		return item1SeriesSojournMap.get(aItem);
	}

	public void addTaskSeriesSojournMap(String aItem, Date start, Date end) {
		TaskSeries taskSeries = item1SeriesSojournMap.get(name);
		taskSeries.add(new Task(aItem + "_" + indexGannt++,
				new SimpleTimePeriod(start, end)));
	}

	public void removeOutlier(int min, int max) {
		for (String item : items) {
			executionMap.get(item).removeOutlier(min, max);
			waitingMap.get(item).removeOutlier(min, max);
		}
	}
}
