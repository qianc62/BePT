package org.processmining.mining.sample;

import org.processmining.framework.log.LogSummary;

public class SampleMiningSettingsWithLogSummary {

	private String value;

	public SampleMiningSettingsWithLogSummary(LogSummary summary) {
		value = "Number of PIs: " + summary.getNumberOfProcessInstances();
	}

	public SampleMiningSettingsWithLogSummary(String text) {
		value = text;
	}

	public String getValue() {
		return value;
	}
}
