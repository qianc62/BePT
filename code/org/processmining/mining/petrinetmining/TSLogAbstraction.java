package org.processmining.mining.petrinetmining;

import java.io.*;
import java.util.*;

import org.processmining.framework.log.*;
import org.processmining.framework.models.transitionsystem.*;
import org.processmining.mining.logabstraction.*;
import cern.colt.matrix.*;
//import sun.reflect.generics.reflectiveObjects.*;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class TSLogAbstraction implements LogAbstraction {

	private DoubleMatrix1D startInfo;
	private DoubleMatrix1D endInfo;
	private TransitionSystem transitionSystem;
	private LogEvents events;
	private DoubleMatrix2D followers = null;
	private DoubleMatrix2D closeIn = null;

	public TSLogAbstraction(TransitionSystem transitionSystem, LogEvents events) {
		this.transitionSystem = transitionSystem;
		this.events = events;
		this.startInfo = DoubleFactory1D.sparse.make(events.size(), 0.0);
		this.endInfo = DoubleFactory1D.sparse.make(events.size(), 0.0);

		for (TransitionSystemEdge edge : (ArrayList<TransitionSystemEdge>) transitionSystem
				.getEdges()) {
			int evtNumber = events.findLogEventNumber(edge.getIdentifier(), "");
			if (edge.getSource().inDegree() == 0) {
				// this is a transition from an initial state
				startInfo.set(evtNumber, startInfo.get(evtNumber) + 1);
			}
			if (edge.getDest().outDegree() == 0) {
				// this is a transition to a final state
				endInfo.set(evtNumber, endInfo.get(evtNumber) + 1);
			}
		}
	}

	public DoubleMatrix2D getFollowerInfo(int distance) throws IOException {
//		if (distance != 1) {
//			throw new NotImplementedException();
//		}
		if (followers == null) {
			followers = DoubleFactory2D.sparse.make(events.size(), events
					.size(), 0.0);
			for (TransitionSystemEdge fromEdge : (ArrayList<TransitionSystemEdge>) transitionSystem
					.getEdges()) {
				int fromEvtNumber = events.findLogEventNumber(fromEdge
						.getIdentifier(), "");
				// Now, find the follower, in terms of transitions
				Iterator it = fromEdge.getDest().getOutEdgesIterator();
				while (it.hasNext()) {
					TransitionSystemEdge toEdge = (TransitionSystemEdge) it
							.next();
					int toEvtNumber = events.findLogEventNumber(toEdge
							.getIdentifier(), "");
					followers.set(fromEvtNumber, toEvtNumber, followers.get(
							fromEvtNumber, toEvtNumber) + 1);
				}
			}
		}
		return followers;
	}

	public DoubleMatrix1D getStartInfo() throws IOException {
		return startInfo;
	}

	public DoubleMatrix1D getEndInfo() throws IOException {
		return endInfo;
	}

	public DoubleMatrix2D getCloseInInfo(int distance) throws IOException {
//		if (distance != 2) {
//			throw new NotImplementedException();
//		}
		if (closeIn == null) {
			closeIn = DoubleFactory2D.sparse.make(events.size(), events.size(),
					0.0);
			for (TransitionSystemEdge fromEdge : (ArrayList<TransitionSystemEdge>) transitionSystem
					.getEdges()) {
				int fromEvtNumber = events.findLogEventNumber(fromEdge
						.getIdentifier(), "");
				// If the edge exists in the other direction, there is a 2-loop
				HashSet<TransitionSystemEdge> edges = transitionSystem
						.getEdgesBetween(fromEdge.getDest(), fromEdge.getDest());
				for (TransitionSystemEdge toEdge : edges) {
					int toEvtNumber = events.findLogEventNumber(toEdge
							.getIdentifier(), "");
					if (toEvtNumber != fromEvtNumber) {
						closeIn.set(fromEvtNumber, toEvtNumber, closeIn.get(
								fromEvtNumber, toEvtNumber) + 1);
					}
				}
			}
		}

		return closeIn;
	}

};
