package org.processmining.framework.models.transitionsystem;

import java.util.Collection;
import java.util.HashSet;

import org.processmining.framework.models.fsm.FSMState;

public abstract class TransitionSystemVertex extends FSMState {
	protected HashSet<String> exLogs;

	public TransitionSystemVertex(TransitionSystem g) {
		super(g, "");
	}

	public void addExLog(String exLog) {
		exLogs.add(exLog);
	}

	public void addExLogs(Collection<String> logs) {
		exLogs.addAll(logs);
	}

	public HashSet<String> getExLogs() {
		return exLogs;
	}

	public void setExLogs(HashSet<String> exLogs) {
		this.exLogs = exLogs;
	}

	public abstract boolean equalsV(TransitionSystemVertex v1);

	public abstract void makeIdentifier();

	public abstract void addDocument(String doc);

	public abstract void addDocuments(Collection<String> docs);

	public abstract Collection<String> getDocs();

	public abstract void setDocs(Collection<String> docs);
}
