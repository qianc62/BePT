package org.processmining.analysis.performance.executiontimes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.processmining.analysis.originatoravailability.OriginatorAvailability;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.util.ComparablePair;

public class ExecutionTimesResult {

	private static final long serialVersionUID = 1L;

	private MeasurementModel model;
	private TaskModel taskModel;
	private ExecutionTimes rawMeasurements;

	public ExecutionTimesResult(LogReader log,
			OriginatorAvailability availability, boolean useTaskOntologies,
			boolean useOriginatorOntologies) {
		ArrayList<Event> events = new ArrayList<Event>();

		for (ProcessInstance pi : log.getInstances()) {
			for (AuditTrailEntry ate : pi.getListOfATEs()) {
				if (ate.getType().equals("start")) {
					Set<String> taskConcepts = useTaskOntologies ? log
							.getLogSummary().getOntologies()
							.translateToReferencesInOntology(
									ate.getElementModelReferences())
							: Collections.<String> emptySet();
					Set<String> origConcepts = useOriginatorOntologies ? log
							.getLogSummary().getOntologies()
							.translateToReferencesInOntology(
									ate.getOriginatorModelReferences())
							: Collections.<String> emptySet();

					events
							.add(new Event(EventType.START_TASK, pi.getName(),
									ate.getElement(), ate.getOriginator(), ate
											.getTimestamp(), taskConcepts,
									origConcepts));
				} else if (ate.getType().equals("complete")) {
					events.add(new Event(EventType.END_TASK, pi.getName(), ate
							.getElement(), ate.getOriginator(), ate
							.getTimestamp(), null, null));
				}
			}
		}
		for (Map.Entry<String, Set<ComparablePair<Date, Date>>> shift : availability
				.getData().entrySet()) {
			for (ComparablePair<Date, Date> window : shift.getValue()) {
				events.add(new Event(EventType.START_WORKING, null, null, shift
						.getKey(), window.getFirst(), null, null));
				events.add(new Event(EventType.STOP_WORKING, null, null, shift
						.getKey(), window.getSecond(), null, null));
			}
		}
		Collections.sort(events, new Comparator<Event>() {
			public int compare(Event o1, Event o2) {
				return o1.getTimestamp().compareTo(o2.getTimestamp());
			}
		});
		/*
		 * for (Event event : events) { System.out.println(event); }
		 */

		Map<String, State> states = new HashMap<String, State>();
		for (String originator : log.getLogSummary().getOriginators()) {
			states.put(originator, new State());
		}

		rawMeasurements = new ExecutionTimes(useTaskOntologies,
				useOriginatorOntologies);
		for (Event event : events) {
			states.get(event.getOriginator()).update(event, rawMeasurements);
		}
		// System.out.println(results);

		model = new MeasurementModel(rawMeasurements);
		taskModel = new TaskModel(rawMeasurements);
	}

	public ExecutionTimes getRawMeasurements() {
		return rawMeasurements;
	}

	public TableModel getTaskTableModel() {
		return taskModel;
	}

	public MeasurementModel getMeasurementTableModel() {
		return model;
	}
}

interface StatisticFunction {
	double getValue(SummaryStatistics stats);
}

class MeasurementModel extends AbstractTableModel {

	private static final long serialVersionUID = -1570824864400502735L;
	private final ExecutionTimes measurements;
	private StatisticFunction function;

	public MeasurementModel(ExecutionTimes measurements) {
		this.measurements = measurements;
		this.function = new StatisticFunction() {
			public double getValue(SummaryStatistics stats) {
				return stats.getMean();
			}
		};
	}

	public void setStatistic(StatisticFunction function) {
		this.function = function;
		fireTableDataChanged();
	}

	public int getColumnCount() {
		return measurements.getOriginators().size() + 1;
	}

	public int getRowCount() {
		return measurements.getTasks().size();
	}

	public String getColumnName(int col) {
		return col == 0 ? "" : measurements.getOriginators().get(col - 1);
	}

	public Object getValueAt(int row, int col) {
		if (col == 0) {
			return measurements.getTasks().get(row);
		} else {
			double value = function.getValue(measurements.get(measurements
					.getTasks().get(row), measurements.getOriginators().get(
					col - 1)));

			return Double.isNaN(value) ? "" : Long
					.toString((long) (value + 0.5));
		}
	}
}

class TaskModel extends AbstractTableModel {

	private static final long serialVersionUID = -8452809308397788207L;
	private final ExecutionTimes measurements;

	public TaskModel(ExecutionTimes measurements) {
		this.measurements = measurements;
	}

	public int getColumnCount() {
		return 8;
	}

	public int getRowCount() {
		return measurements.getTasks().size();
	}

	public String getColumnName(int col) {
		String[] names = new String[] { "Activity", "Minimum (sec)",
				"Maximum (sec)", "Average (sec)", "Standard Deviation (sec)",
				"Geometric Mean (sec)", "Sum (sec)", "No. of Measurements" };
		return names[col];
	}

	public Object getValueAt(int row, int col) {
		if (col == 0) {
			return measurements.getTasks().get(row);
		} else {
			SummaryStatistics stat = measurements.get(measurements.getTasks()
					.get(row));
			double value = -1;
			double factor = 1.0;

			switch (col) {
			case 1:
				value = stat.getMin() / factor;
				break;
			case 2:
				value = stat.getMax() / factor;
				break;
			case 3:
				value = stat.getMean() / factor;
				break;
			case 4:
				value = stat.getStandardDeviation();
				break;
			case 5:
				value = stat.getGeometricMean() / factor;
				break;
			case 6:
				value = stat.getSum() / factor;
				break;
			case 7:
				value = stat.getN();
				break;
			}
			return Double.isNaN(value) ? "" : Double.toString(Math
					.round(value * 10) / 10.0);
		}
	}

}

class RunningTask {
	public RunningTask(String pi, String task, Set<String> taskMRs,
			Set<String> origMRs) {
		this.pi = pi;
		this.task = task;
		this.value = 0.0;
		this.taskMRs = taskMRs;
		this.origMRs = origMRs;
	}

	String pi;
	String task;
	Double value;
	Set<String> origMRs;
	Set<String> taskMRs;
}

class State {
	private boolean busy;
	private List<RunningTask> runningTasks;
	private Date timestamp;
	private Calendar cal1 = Calendar.getInstance();
	private Calendar cal2 = Calendar.getInstance();

	private boolean DIVIDE_OVERLAPPING = true;

	public State() {
		busy = false;
		runningTasks = new LinkedList<RunningTask>();
		timestamp = null;
	}

	public void update(Event event, ExecutionTimes results) {
		/*
		 * System.out.println(event);
		 * 
		 * System.out.print("  busy=" + busy + ", timestamp=" + timestamp +
		 * ", running tasks: "); for (RunningTask i : runningTasks) {
		 * System.out.print(i.pi + "/" + i.task + " (" + i.value + ") "); }
		 * System.out.println();
		 */

		if (busy && !runningTasks.isEmpty()) {
			cal1.setTime(timestamp);
			cal2.setTime(event.getTimestamp());
			long millis = cal2.getTimeInMillis() - cal1.getTimeInMillis();

			for (RunningTask task : runningTasks) {
				task.value += millis / 1000.0
						/ (DIVIDE_OVERLAPPING ? runningTasks.size() : 1.0);
			}
		}
		switch (event.getType()) {
		case START_TASK:
			runningTasks.add(new RunningTask(event.getPI(), event.getTask(),
					event.getTaskMRs(), event.getOrigMRs()));
			break;
		case END_TASK:
			Iterator<RunningTask> iter = runningTasks.iterator();

			while (iter.hasNext()) {
				RunningTask task = iter.next();

				if (task.pi.equals(event.getPI())
						&& task.task.equals(event.getTask())) {
					results.add(event.getTask(), event.getOriginator(),
							task.taskMRs, task.origMRs, task.value);
					iter.remove();
					break;
				}
			}
			break;
		case START_WORKING:
			busy = true;
			break;
		case STOP_WORKING:
			busy = false;
			break;
		}
		timestamp = event.getTimestamp();

		/*
		 * System.out.print("  busy=" + busy + ", timestamp=" + timestamp +
		 * ", running tasks: "); for (RunningTask i : runningTasks) {
		 * System.out.print(i.pi + "/" + i.task + " (" + i.value + ") "); }
		 * System.out.println();
		 */
	}
}

enum EventType {
	START_TASK, END_TASK, START_WORKING, STOP_WORKING
}

class Event {
	private final EventType type;
	private final Date timestamp;
	private final String task;
	private final String originator;
	private final String pi;
	private final Set<String> taskMRs;
	private final Set<String> origMRs;

	public Event(EventType type, String pi, String task, String originator,
			Date timestamp, Set<String> taskConcepts, Set<String> origConcepts) {
		this.type = type;
		this.pi = pi;
		this.task = task;
		this.originator = originator;
		this.timestamp = timestamp;
		this.taskMRs = taskConcepts;
		this.origMRs = origConcepts;
	}

	public String getPI() {
		return pi;
	}

	public String getTask() {
		return task;
	}

	public EventType getType() {
		return type;
	}

	public String getOriginator() {
		return originator;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Set<String> getTaskMRs() {
		return taskMRs;
	}

	public Set<String> getOrigMRs() {
		return origMRs;
	}

	public String toString() {
		return "Event(type=" + type + ", task=" + task + ", originator="
				+ originator + ", timestamp=" + timestamp + ")";
	}
}