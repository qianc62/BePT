package org.processmining.mining.tsmining;

import java.util.Collection;
import java.util.HashSet;

import org.processmining.framework.models.transitionsystem.MultiSet;
import org.processmining.framework.models.transitionsystem.TransitionSystemVertex;
import org.processmining.framework.models.transitionsystem.TransitionSystemVertexBag;
import org.processmining.framework.models.transitionsystem.TransitionSystemVertexSet;
import org.processmining.framework.models.transitionsystem.TSConstants;
import org.processmining.framework.models.transitionsystem.TransitionSystem;

/**
 * @author Vladimir Rubin
 * @version 1.0
 */
public class DocsVertexCreator {
	public Collection<String> createDocs(int type) {
		Collection<String> docs = null;
		if (type == TSConstants.SETS)
			docs = new HashSet<String>();
		if (type == TSConstants.BAGS)
			docs = new MultiSet<String>();
		return docs;
	}

	public TransitionSystemVertex createVertex(int type,
			Collection<String> docs, Collection<String> exLogs,
			TransitionSystem g) {
		TransitionSystemVertex vertex = null;
		if (type == TSConstants.SETS)
			vertex = new TransitionSystemVertexSet(docs, exLogs, g);
		if (type == TSConstants.BAGS)
			vertex = new TransitionSystemVertexBag(docs, exLogs, g);
		return vertex;
	}

	public TransitionSystemVertex createVertex(int type,
			Collection<String> docs, String exLog, TransitionSystem g) {
		TransitionSystemVertex vertex = null;
		if (type == TSConstants.SETS)
			vertex = new TransitionSystemVertexSet(docs, exLog, g);
		if (type == TSConstants.BAGS)
			vertex = new TransitionSystemVertexBag(docs, exLog, g);
		return vertex;
	}

	public TransitionSystemVertex createVertex(int type, String identifier,
			TransitionSystem g) {
		TransitionSystemVertex vertex = null;
		if (type == TSConstants.SETS)
			vertex = new TransitionSystemVertexSet(identifier, g);
		if (type == TSConstants.BAGS)
			vertex = new TransitionSystemVertexBag(identifier, g);
		return vertex;
	}
}
