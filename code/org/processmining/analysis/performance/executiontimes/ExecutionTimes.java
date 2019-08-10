package org.processmining.analysis.performance.executiontimes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public class ExecutionTimes {

	private Map<String, Map<String, SummaryStatistics>> measurements;
	private List<ExecutionTimeMeasurement> rawMeasurements;
	private Map<String, SummaryStatistics> taskMeasurements;
	private Set<String> originators;
	private Set<String> tasks;
	private final boolean useTaskOntologies;
	private final boolean useOriginatorOntologies;

	public ExecutionTimes(boolean useTaskOntologies,
			boolean useOriginatorOntologies) {
		this.useTaskOntologies = useTaskOntologies;
		this.useOriginatorOntologies = useOriginatorOntologies;
		measurements = new HashMap<String, Map<String, SummaryStatistics>>();
		taskMeasurements = new HashMap<String, SummaryStatistics>();
		originators = new TreeSet<String>();
		tasks = new TreeSet<String>();
		rawMeasurements = new ArrayList<ExecutionTimeMeasurement>();
	}

	public List<String> getTasks() {
		return new ArrayList<String>(tasks);
	}

	public List<String> getOriginators() {
		return new ArrayList<String>(originators);
	}

	public List<ExecutionTimeMeasurement> getRawMeasurements() {
		return rawMeasurements;
	}

	public void add(String taskLabel, String originatorLabel,
			Set<String> taskMRs, Set<String> origMRs, double executionTime) {
		Set<String> taskList = useTaskOntologies ? taskMRs
				: new HashSet<String>(Arrays.asList(new String[] { taskLabel }));
		Set<String> originatorList = useOriginatorOntologies ? origMRs
				: new HashSet<String>(Arrays
						.asList(new String[] { originatorLabel }));

		for (String task : taskList) {
			for (String originator : originatorList) {
				rawMeasurements.add(new ExecutionTimeMeasurement(task,
						originator, executionTime));

				tasks.add(task);
				originators.add(originator);

				Map<String, SummaryStatistics> origs = measurements.get(task);

				if (origs == null) {
					origs = new HashMap<String, SummaryStatistics>();
					measurements.put(task, origs);
				}
				SummaryStatistics stats = origs.get(originator);

				if (stats == null) {
					stats = SummaryStatistics.newInstance();
					origs.put(originator, stats);
				}
				stats.addValue(executionTime);

				SummaryStatistics taskStats = taskMeasurements.get(task);
				if (taskStats == null) {
					taskStats = SummaryStatistics.newInstance();
					taskMeasurements.put(task, taskStats);
				}
				taskStats.addValue(executionTime);
			}
		}
	}

	public SummaryStatistics get(String task, String originator) {
		Map<String, SummaryStatistics> origs = measurements.get(task);

		if (origs == null) {
			return SummaryStatistics.newInstance();
		}
		SummaryStatistics stats = origs.get(originator);

		return stats == null ? SummaryStatistics.newInstance() : stats;
	}

	public SummaryStatistics get(String task) {
		SummaryStatistics stats = taskMeasurements.get(task);
		return stats == null ? SummaryStatistics.newInstance() : stats;
	}
}
