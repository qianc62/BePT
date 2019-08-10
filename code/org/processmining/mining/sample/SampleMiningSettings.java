package org.processmining.mining.sample;

public class SampleMiningSettings {

	private String value;

	public SampleMiningSettings() {
		// fill in the defaults here; these will be shown in the settings GUI at
		// first
		value = "";
	}

	public SampleMiningSettings(String s) {
		value = s;
	}

	public String getValue() {
		return value;
	}
}
