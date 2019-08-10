package org.processmining.analysis.performance.basicperformance.model.task;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;

import org.jfree.data.gantt.TaskSeries;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.processmining.analysis.performance.basicperformance.model.StatisticUnit;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.slicker.ProgressPanel;

public class InstanceTaskTPerformance extends AbstractPerformance2D {
	public InstanceTaskTPerformance(LogReader inputLog) {
		super("Instance-Task", "Instance", "Task", "Instance-Task performance");

		items = new ArrayList<String>();
		items2 = new ArrayList<String>(Arrays.asList(inputLog.getLogSummary()
				.getModelElements()));

		for (ProcessInstance pi : inputLog.getInstances()) {
			String aItem = pi.getName();
			items.add(aItem);
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

	public static String getNameCode() {
		return "Instance-Task";
	}
}
