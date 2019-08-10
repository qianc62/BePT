package org.processmining.mining;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;

import org.processmining.framework.log.LogReader;
import org.processmining.importing.LogReaderConnection;

public class MiningResultWithLogConnectionImpl extends MiningResultImpl
		implements LogReaderConnection {

	private LogReaderConnection connection;

	public MiningResultWithLogConnectionImpl(LogReader log,
			JComponent component, LogReaderConnection connection) {
		super(log, component);
		this.connection = connection;
	}

	public void connectWith(LogReader newLog, HashMap eventsMapping) {
		if (connection != null) {
			connection.connectWith(newLog, eventsMapping);
		}
	}

	public ArrayList getConnectableObjects() {
		return connection == null ? new ArrayList() : connection
				.getConnectableObjects();
	}
}
