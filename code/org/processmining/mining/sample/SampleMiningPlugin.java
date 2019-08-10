package org.processmining.mining.sample;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.log.LogReader;
import org.processmining.mining.Miner;
import org.processmining.mining.NoMiningSettings;

public class SampleMiningPlugin {
	@Miner(name = "Sample miner (with settings)", settings = SampleMiningSettingsGUI.class)
	public SampleMiningResult mine(LogReader log, SampleMiningSettings settings) {
		return new SampleMiningResult(settings.getValue());
	}

	@Miner(name = "Sample miner (with settings based on LogSummary)", settings = SampleMiningSettingsGUIWithLogSummary.class)
	public SampleMiningResult mine(LogReader log,
			SampleMiningSettingsWithLogSummary settings) {
		return new SampleMiningResult(settings.getValue());
	}

	@Miner(name = "Sample miner without any settings", settings = NoMiningSettings.class)
	public SampleMiningResult mine(LogReader log) {
		return new SampleMiningResult("none");
	}

	@Miner(name = "Sample miner without settings but with LogSummary", settings = NoMiningSettings.class)
	public SampleMiningResult mine2(LogReader log,
			SampleMiningSettingsWithLogSummary settings) {
		return new SampleMiningResult(settings.getValue());
	}
}

class SampleMiningResult extends JPanel {

	private static final long serialVersionUID = 1800691420795573061L;

	protected SampleMiningResult(String value) {
		this.add(new JLabel("Setting: " + value));
	}
}
