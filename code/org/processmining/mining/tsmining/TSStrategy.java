package org.processmining.mining.tsmining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.transitionsystem.TransitionSystem;
import org.processmining.framework.models.transitionsystem.TransitionSystemEdge;
import org.processmining.framework.models.transitionsystem.TransitionSystemVertex;

import att.grappa.Edge;
import org.processmining.framework.models.transitionsystem.TSConstants;

/**
 * @author Vladimir Rubin
 * @version 1.0
 */
public abstract class TSStrategy {
	protected TransitionSystem ts;
	protected LogReader log;
	protected DocsVertexCreator factory;

	int genFlags;
	int typeOfTS;
	int setsOrBags;

	public abstract void modifyTS();

	public TSStrategy(LogReader log, TransitionSystem ts, int genFlags,
			int typeOfTS) {
		this.log = log;
		this.ts = ts;
		this.genFlags = genFlags;
		this.typeOfTS = typeOfTS;

		factory = new DocsVertexCreator();
		if ((typeOfTS & TSConstants.SETS) == TSConstants.SETS)
			setsOrBags = TSConstants.SETS;
		else
			setsOrBags = TSConstants.BAGS;

		modifyTS();
	}

	public void addArc(Collection<String> docs, TransitionSystemVertex v1,
			TransitionSystemVertex v2) {
		TransitionSystemEdge newEdge = new TransitionSystemEdge(docs, v1, v2);
		ts.addEdge(newEdge);
	}

	public boolean mergeStates(TransitionSystemVertex v1,
			TransitionSystemVertex v2) {
		Collection<String> newDocs = factory.createDocs(setsOrBags);
		newDocs.addAll(v1.getDocs());
		newDocs.addAll(v2.getDocs());
		HashSet<String> newExLogs = new HashSet<String>();
		newExLogs.addAll(v1.getExLogs());
		newExLogs.addAll(v2.getExLogs());
		TransitionSystemVertex newVertex = factory.createVertex(setsOrBags,
				newDocs, newExLogs, ts);
		// change all the incoming edges of both vertexes
		ArrayList<Edge> v1IncEdges = v1.getInEdges();
		ArrayList<Edge> v2IncEdges = v2.getInEdges();
		for (int i = 0; i < v1IncEdges.size(); i++) {
			TransitionSystemEdge v1IncEdge = (TransitionSystemEdge) v1IncEdges
					.get(i);
			TransitionSystemEdge newIncEdge = new TransitionSystemEdge(
					(Collection<String>) v1IncEdge.getDocs(),
					(TransitionSystemVertex) v1IncEdge.getSource(), newVertex);
			ts.addEdge(newIncEdge);
			ts.removeEdge(v1IncEdge);
		}
		for (int i = 0; i < v2IncEdges.size(); i++) {
			TransitionSystemEdge v2IncEdge = (TransitionSystemEdge) v2IncEdges
					.get(i);
			TransitionSystemEdge newIncEdge = new TransitionSystemEdge(
					(Collection<String>) v2IncEdge.getDocs(),
					(TransitionSystemVertex) v2IncEdge.getSource(), newVertex);
			ts.addEdge(newIncEdge);
			ts.removeEdge(v2IncEdge);
		}
		// change all the outgoing edges of both vertexes
		ArrayList<Edge> v1OutEdges = v1.getOutEdges();
		ArrayList<Edge> v2OutEdges = v2.getOutEdges();
		for (int i = 0; i < v1OutEdges.size(); i++) {
			TransitionSystemEdge v1OutEdge = (TransitionSystemEdge) v1OutEdges
					.get(i);
			TransitionSystemEdge newOutEdge = new TransitionSystemEdge(
					(Collection<String>) v1OutEdge.getDocs(), newVertex,
					(TransitionSystemVertex) v1OutEdge.getDest());
			ts.addEdge(newOutEdge);
			ts.removeEdge(v1OutEdge);
		}
		for (int i = 0; i < v2OutEdges.size(); i++) {
			TransitionSystemEdge v2OutEdge = (TransitionSystemEdge) v2OutEdges
					.get(i);
			TransitionSystemEdge newOutEdge = new TransitionSystemEdge(
					(Collection<String>) v2OutEdge.getDocs(), newVertex,
					(TransitionSystemVertex) v2OutEdge.getDest());
			ts.addEdge(newOutEdge);
			ts.removeEdge(v2OutEdge);
		}
		return false;
	}

	public TransitionSystem getTransitionSystem() {
		return ts;
	}

	boolean isIntersection(Collection a, Collection b) {
		Iterator it = b.iterator();
		while (it.hasNext())
			if (a.contains(it.next()))
				return true;
		return false;
	}
}
