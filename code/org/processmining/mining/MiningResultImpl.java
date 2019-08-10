package org.processmining.mining;

import javax.swing.JComponent;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;

public class MiningResultImpl implements MiningResult, Provider {

	private LogReader log;
	private JComponent component;

	public MiningResultImpl(LogReader log, JComponent component) {
		this.log = log;
		this.component = component;
	}

	public LogReader getLogReader() {
		return log;
	}

	public JComponent getVisualization() {
		return component;
	}

	public ProvidedObject[] getProvidedObjects() {
		if (component != null && component instanceof Provider) {
			return ((Provider) component).getProvidedObjects();
		}
		return new ProvidedObject[0];
	}
}
