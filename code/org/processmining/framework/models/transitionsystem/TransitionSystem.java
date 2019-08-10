package org.processmining.framework.models.transitionsystem;

import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.models.fsm.AcceptFSM;

/**
 * @author Vladimir Rubin
 * @version 1.0
 */
public class TransitionSystem extends AcceptFSM {
	public static final int IDENTIFIER = 1;
	public static final int ID = 2;

	int visFlags = IDENTIFIER;
	int genFlags;

	public TransitionSystem(String graphName) {
		super(graphName);
	}

	public TransitionSystem(String graphName, int genFlags, int visFlags) {
		super(graphName);
		this.genFlags = genFlags;
		this.visFlags = visFlags;
	}

	public TransitionSystemVertex containsVertex(
			TransitionSystemVertex newVertex) {
		for (int i = 0; i < vertices.size(); i++) {
			TransitionSystemVertex vertex = (TransitionSystemVertex) vertices
					.get(i);
			if (vertex.equalsV(newVertex)) {
				return vertex;
			}
		}
		return null;
	}

	public TransitionSystemVertex addVertex(TransitionSystemVertex newVertex) {
		TransitionSystemVertex existingVertex = this.containsVertex(newVertex);
		if (existingVertex == null) {
			super.addVertex(newVertex);
			return newVertex;
		} else {
			HashSet<String> exLogs = newVertex.getExLogs();
			existingVertex.addExLogs(exLogs);
			return existingVertex;
		}
	}

	public TransitionSystemVertex addVertexQuick(
			TransitionSystemVertex newVertex) {
		super.addVertex(newVertex);
		return newVertex;
	}

	// We don't have multiple edges between two states
	public TransitionSystemEdge addEdge(TransitionSystemEdge newEdge) {
		TransitionSystemVertex v1 = (TransitionSystemVertex) newEdge
				.getSource();
		TransitionSystemVertex v2 = (TransitionSystemVertex) newEdge.getDest();

		HashSet edges = this.getEdgesBetween(v1, v2);
		if ((v1 != v2) && (edges.size() > 1)) { // 1 - created edge
			// The edge has been created, hence the nodes v1 and v2 know about
			// it.
			// and newEdge is not an inEdge of v1 (hence false as 2nd argument)
			v1.removeEdge(newEdge, false);
			// The edge has been created, hence the nodes v1 and v2 know about
			// it.
			// and newEdge is an inEdge of v2 (hence true as 2nd argument)
			v2.removeEdge(newEdge, true);

			Iterator it = edges.iterator();
			return (TransitionSystemEdge) it.next();
		} else if ((v1 == v2) && (edges.size() >= 2)) { // a tricky check for
			// the case of loops
			Iterator it = edges.iterator();
			TransitionSystemEdge existingEdge = null;
			int k = 0;
			while (it.hasNext()) {
				TransitionSystemEdge anEdge = (TransitionSystemEdge) it.next();
				if (anEdge.getIdentifier().equals(newEdge.getIdentifier())) {
					k++;
					existingEdge = anEdge;
				}
			}
			if (k == 1) {
				super.addEdge(newEdge);
				return newEdge;
			} else {
				// The edge has been created, hence the nodes v1 and v2 know
				// about it.
				// and newEdge is not an inEdge of v1 (hence false as 2nd
				// argument)
				v1.removeEdge(newEdge, false);
			}
			// The edge has been created, hence the nodes v1 and v2 know about
			// it.
			// and newEdge is an inEdge of v2 (hence true as 2nd argument)
			v2.removeEdge(newEdge, true);
			return existingEdge;
		} else {
			super.addEdge(newEdge);
			return newEdge;
		}
	}

	public int getStateNameFlag() {
		return visFlags;
	}

	public boolean hasExplicitEnd() {
		if ((genFlags & TSConstants.EXPLICIT_END) == TSConstants.EXPLICIT_END) {
			return true;
		} else {
			return false;
		}
	}
}
