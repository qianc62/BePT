package org.processmining.analysis.performance.executiontimes;

public class ExecutionTimeMeasurement {
	private final String task;
	private final String originator;
	private final double value;

	public ExecutionTimeMeasurement(String task, String originator, double value) {
		this.task = task;
		this.originator = originator;
		this.value = value;
	}

	public String getTask() {
		return task;
	}

	public String getOriginator() {
		return originator;
	}

	public double getValue() {
		return value;
	}
}
