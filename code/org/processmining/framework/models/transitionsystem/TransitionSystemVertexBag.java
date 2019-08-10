package org.processmining.framework.models.transitionsystem;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class TransitionSystemVertexBag extends TransitionSystemVertex {
	private MultiSet<String> docs;

	public TransitionSystemVertexBag(Collection<String> docs,
			Collection<String> exLogs, TransitionSystem g) {
		super(g);
		this.docs = new MultiSet<String>(docs);
		this.exLogs = new HashSet<String>(exLogs);
		this.makeIdentifier();
	}

	public TransitionSystemVertexBag(Collection<String> docs, String exLog,
			TransitionSystem g) {
		super(g);
		this.docs = new MultiSet<String>(docs);
		this.exLogs = new HashSet<String>();
		exLogs.add(exLog);
		this.makeIdentifier();
	}

	public TransitionSystemVertexBag(String identifier, TransitionSystem g) {
		super(g);
		this.docs = new MultiSet<String>();
		this.exLogs = new HashSet<String>();
		this.setIdentifier(identifier);
		setLabel(identifier);
	}

	public void makeIdentifier() {
		TransitionSystem ts = (TransitionSystem) getGraph();
		if (ts.getStateNameFlag() == TransitionSystem.ID) {
			setLabel(((Integer) getId()).toString());
		} else {
			String st = "s_";
			Iterator<String> it = docs.iterator();
			while (it.hasNext()) {
				st = st.concat(it.next()).concat("_");
			}
			st = st.substring(0, st.length() - 1);
			setLabel(st);
		}
	}

	public boolean equalsV(TransitionSystemVertex v1) {
		if (!(v1 instanceof TransitionSystemVertexBag))
			return false;
		else {
			MultiSet<String> otherDocs = (MultiSet<String>) v1.getDocs();
			return docs.equals(otherDocs);
		}
	}

	public void addDocument(String doc) {
		this.docs.add(doc);
	}

	public void addDocuments(Collection<String> docs) {
		this.docs.addAll(docs);
	}

	public Collection<String> getDocs() {
		return docs;
	}

	public void setDocs(Collection<String> docs) {
		docs = new MultiSet<String>(docs);
	}
}
