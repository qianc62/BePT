package org.processmining.mining.sample;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.framework.log.LogSummary;

public class SampleMiningSettingsGUIWithLogSummary extends JPanel {

	private static final long serialVersionUID = 6536681076752878149L;

	private JTextField field;
	private JLabel label;

	public SampleMiningSettingsGUIWithLogSummary(
			SampleMiningSettingsWithLogSummary defaults, LogSummary summary) {
		label = new JLabel("Default: " + defaults.getValue());
		field = new JTextField("" + summary.getNumberOfProcessInstances());
		this.add(label);
		this.add(field);
	}

	public SampleMiningSettingsWithLogSummary getSettings() {
		return new SampleMiningSettingsWithLogSummary(field.getText());
	}

	public String getValue() {
		return field.getText();
	}
}