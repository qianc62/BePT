package org.processmining.mining.tsmining;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.transitionsystem.PetrifyConstants;
import org.processmining.framework.models.transitionsystem.TSConstants;
import org.processmining.framework.models.transitionsystem.TransitionSystem;
import org.processmining.framework.models.transitionsystem.TransitionSystemEdge;
import org.processmining.framework.models.transitionsystem.TransitionSystemVertex;

/**
 * @author Vladimir Rubin
 * @version 1.0
 */
public class TSGAlgorithmNoTime extends TSGAlgorithm {

	public TSGAlgorithmNoTime(LogReader log, int typeOfTS, int genFlags,
			int visFlags) {
		super(log, typeOfTS, genFlags, visFlags);
	}

	@Override
	/*
	 * The log contains no timestamps, so it's ordered Only one activity occurs
	 * at one point of time (no transactions)
	 */
	public TransitionSystem generateTS() {
		TransitionSystem ts = new TransitionSystem("Labeled Transition System",
				genFlags, visFlags);
		Iterator logIt = log.instanceIterator();

		Collection<String> finalDocSet = null;
		TransitionSystemVertex finalVertex = null;
		if ((genFlags & TSConstants.EXPLICIT_END) == TSConstants.EXPLICIT_END) // create
		// one
		// final
		// vertex
		// for
		// all
		// cases
		{
			finalDocSet = factory.createDocs(setsOrBags);
			finalDocSet.add(new String("END"));
			finalVertex = factory.createVertex(setsOrBags, finalDocSet, "", ts);
			ts.addVertex(finalVertex);
		}
		while (logIt.hasNext()) {
			ProcessInstance exLog = (ProcessInstance) logIt.next();
			String exLogName = exLog.getName();
			Iterator entryIt = exLog.getAuditTrailEntryList().iterator();
			Collection<String> docs = factory.createDocs(setsOrBags);
			Collection<String> commit = new HashSet<String>();
			// create initial vertex
			TransitionSystemVertex zeroVertex = factory.createVertex(
					setsOrBags, docs, exLogName, ts);
			TransitionSystemVertex lastVertex = ts.addVertex(zeroVertex);
			// main loop: reading
			// create a new vertex, when reading a new entry;
			// create an edge with the label equal to the commit
			AuditTrailEntry entry;
			String doc;
			while (entryIt.hasNext()) {
				entry = (AuditTrailEntry) entryIt.next();
				doc = entry.getElement();
				if ((genFlags & TSConstants.EVENT_TYPES) == TSConstants.EVENT_TYPES)
					doc = doc.concat(PetrifyConstants.EVENTTYPESEPARATOR)
							.concat(entry.getType());
				docs.add(doc);
				commit.add(doc);
				TransitionSystemVertex newVertex = factory.createVertex(
						setsOrBags, docs, exLogName, ts);
				TransitionSystemVertex createdVertex = ts.addVertex(newVertex);
				if ((createdVertex == lastVertex)
						&& ((genFlags & TSConstants.KILL_LOOPS) == TSConstants.KILL_LOOPS))
					;
				else {
					TransitionSystemEdge edge = new TransitionSystemEdge(
							commit, lastVertex, createdVertex);
					ts.addEdge(edge);
				}
				commit = new HashSet<String>();
				lastVertex = createdVertex;
			}
			if ((genFlags & TSConstants.EXPLICIT_END) == TSConstants.EXPLICIT_END) // add
			// the
			// edge
			// to
			// the
			// final
			// Vertex
			{
				TransitionSystemEdge finalEdge = new TransitionSystemEdge(
						finalDocSet, lastVertex, finalVertex);
				ts.addEdge(finalEdge);
			}
		}
		return ts;
	}
}
