package org.processmining.analysis.performance.basicperformance.model.instance;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.jfree.data.gantt.TaskSeries;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.slicker.ProgressPanel;

import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance;
import org.processmining.analysis.performance.basicperformance.model.StatisticUnit;

public class InstancePerformance extends AbstractPerformance {

	public InstancePerformance(LogReader inputLog) {
		super("Instance ", "Instance performance");
		// new ArrayList(Arrays.asList(summary.getModelElements())));
		items = new ArrayList<String>();
		for (ProcessInstance pi : inputLog.getInstances()) {
			String aItem = pi.getName();
			items.add(aItem);
			StatisticUnit su = new StatisticUnit(aItem);
			executionMap.put(aItem, su);
			StatisticUnit su2 = new StatisticUnit(aItem);
			waitingMap.put(aItem, su2);
			StatisticUnit su3 = new StatisticUnit(aItem);
			sojournMap.put(aItem, su3);
		}
		TaskSeries taskSeries = new TaskSeries(name);
		item1SeriesMap.put(name, taskSeries);
	}

	public static String getNameCode() {
		return "Instance ";
	}
}
