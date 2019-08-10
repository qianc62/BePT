package org.processmining.framework.models.transitionsystem;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.fsm.FSMTransition;

/**
 * @author Vladimir Rubin
 * @version 1.0
 */
public class TransitionSystemEdge extends FSMTransition {
	private HashSet<String> docs;
	private String identifier;

	public TransitionSystemEdge(Collection<String> docs,
			TransitionSystemVertex source, TransitionSystemVertex destination) {
		super(source, destination, "");
		this.docs = new HashSet<String>(docs);
		this.makeIdentifier();
		this.object = docs;
		// Edges are always added automatically to the graph
	}

	public TransitionSystemEdge(String identifier,
			TransitionSystemVertex source, TransitionSystemVertex destination) {
		super(source, destination, "");
		this.setIdentifier(identifier);
		this.docs = new HashSet<String>();
		this.docs.add(identifier);
		this.object = docs;
		// System.out.println(source.getGraph().toString());
		// Edges are always added automatically to the graph
	}

	void makeIdentifier() {
		String st = "";
		Iterator<String> it = docs.iterator();
		while (it.hasNext()) {
			st = st.concat(it.next())
					.concat(PetrifyConstants.EDGEDOCSSEPARATOR);
		}
		st = st.substring(0, st.length()
				- PetrifyConstants.EDGEDOCSSEPARATOR.length());
		setIdentifier(st);
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
		setCondition(identifier);
	}

	public void addDocument(String doc) {
		docs.add(doc);
	}

	public void addDocuments(Collection<String> docs) {
		docs.addAll(docs);
	}

	public HashSet<String> getDocs() {
		return docs;
	}

	public void setDocs(HashSet<String> docs) {
		this.docs = docs;
	}
}
