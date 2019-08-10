package org.processmining.framework.models.transitionsystem;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.models.ModelGraph;

/**
 * @author Vladimir Rubin
 * @version 1.0
 */
public class TransitionSystemVertexSet extends TransitionSystemVertex {
	private HashSet<String> docs;

	public TransitionSystemVertexSet(Collection<String> docs,
			Collection<String> exLogs, TransitionSystem g) {
		super(g);
		this.docs = new HashSet<String>(docs);
		this.exLogs = new HashSet<String>(exLogs);
		this.makeIdentifier();
		// g.addVertex(this);
	}

	public TransitionSystemVertexSet(Collection<String> docs, String exLog,
			TransitionSystem g) {
		super(g);
		this.docs = new HashSet<String>(docs);
		this.exLogs = new HashSet<String>();
		exLogs.add(exLog);
		this.makeIdentifier();
		// g.addVertex(this);
	}

	public TransitionSystemVertexSet(String identifier, TransitionSystem g) {
		super(g);
		this.docs = new HashSet<String>();
		this.exLogs = new HashSet<String>();
		this.setIdentifier(identifier);
		setLabel(identifier);
		// g.addVertex(this);
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
		setIdentifier(getLabel());
	}

	public boolean equalsV(TransitionSystemVertex v1) {
		return docs.equals(v1.getDocs());
	}

	public void addDocument(String doc) {
		docs.add(doc);
	}

	public void addDocuments(Collection<String> docs) {
		docs.addAll(docs);
	}

	public void addExLog(String exLog) {
		exLogs.add(exLog);
	}

	public void addExLogs(Collection<String> logs) {
		exLogs.addAll(logs);
	}

	public void print() {
		System.out.print("( {");
		for (String d : docs)
			System.out.print(d + " ");
		System.out.print("}, {");
		for (String e : exLogs)
			System.out.print(e + " ");
		System.out.println("} );");

	}

	public Collection<String> getDocs() {
		return docs;
	}

	public void setDocs(Collection<String> docs) {
		this.docs = (HashSet<String>) docs;
	}
}
