package org.processmining.mining;

import javax.swing.JPanel;

import org.processmining.framework.log.LogReader;

public interface NewStyleMiningPlugin {
	public MiningResult mine(LogReader log, JPanel optionsPanel);
}
