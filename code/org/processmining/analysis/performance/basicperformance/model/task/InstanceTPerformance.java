package org.processmining.analysis.performance.basicperformance.model.task;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.jfree.data.gantt.TaskSeries;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance;
import org.processmining.analysis.performance.basicperformance.model.StatisticUnit;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

public class InstanceTPerformance extends AbstractPerformance {

	public InstanceTPerformance(LogReader inputLog) {
		super("Instance", "InstanceT performance");
		// new ArrayList(Arrays.asList(summary.getModelElements())));
		items = new ArrayList<String>();
		for (ProcessInstance pi : inputLog.getInstances()) {
			String aItem = pi.getName();
			items.add(aItem);
			StatisticUnit su = new StatisticUnit(aItem);
			executionMap.put(aItem, su);
			StatisticUnit su2 = new StatisticUnit(aItem);
			waitingMap.put(aItem, su2);
		}
		TaskSeries taskSeries = new TaskSeries(name);
		item1SeriesMap.put(name, taskSeries);
	}

	public static String getNameCode() {
		return "Instance";
	}

}
