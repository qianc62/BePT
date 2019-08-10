package org.processmining.mining.sample;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.framework.log.LogSummary;

public class SampleMiningSettingsGUI extends JPanel {

	private static final long serialVersionUID = -2720292789575028345L;

	private JTextField field;

	public SampleMiningSettingsGUI(SampleMiningSettings defaults,
			LogSummary summary) {
		field = new JTextField("" + summary.getNumberOfProcessInstances());
		this.add(field);
	}

	public SampleMiningSettings getSettings() {
		return new SampleMiningSettings(field.getText());
	}
}