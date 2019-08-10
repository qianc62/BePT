package org.processmining.mining.tsmining;

import java.util.Collection;
import java.util.Date;
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
public class TSGAlgorithmTime extends TSGAlgorithm {

	public TSGAlgorithmTime(LogReader log, int typeOfTS, int genFlags,
			int visFlags) {
		super(log, typeOfTS, genFlags, visFlags);
	}

	@Override
	/*
	 * Generate a Transition System Effective algorithm: adding states and
	 * transitions running through the log Here, one commit can contain several
	 * documents!
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
			ts.addAcceptState(ts.addVertex(finalVertex));
		}
		// main loop iterating through all the process instances and audit
		// trails in them
		while (logIt.hasNext()) {
			ProcessInstance exLog = (ProcessInstance) logIt.next();
			String exLogName = exLog.getName();
			Iterator entryIt = exLog.getAuditTrailEntryList().iterator();
			Collection<String> docs = factory.createDocs(setsOrBags);
			HashSet<String> commit = new HashSet<String>();
			Date currentTimestamp, newTimestamp;
			// create initial vertex
			TransitionSystemVertex zeroVertex = factory.createVertex(
					setsOrBags, docs, exLogName, ts);
			TransitionSystemVertex createdZeroVertex = ts.addVertex(zeroVertex);
			TransitionSystemVertex lastVertex = createdZeroVertex;
			ts.setStartState(lastVertex);
			// read the first entry and get its timestamp
			AuditTrailEntry entry = (AuditTrailEntry) entryIt.next();
			currentTimestamp = entry.getTimestamp();
			String doc = entry.getElement();
			if ((genFlags & TSConstants.EVENT_TYPES) == TSConstants.EVENT_TYPES)
				doc = doc.concat(PetrifyConstants.EVENTTYPESEPARATOR).concat(
						entry.getType());
			docs.add(doc);
			commit.add(doc);
			// main loop: continue reading
			// create a new vertex, when the timestamp is changed;
			// create an edge with the label equal to the commit
			while (entryIt.hasNext()) {
				entry = (AuditTrailEntry) entryIt.next();
				newTimestamp = entry.getTimestamp();
				if (currentTimestamp.getTime() < newTimestamp.getTime()) {
					TransitionSystemVertex newVertex = factory.createVertex(
							setsOrBags, docs, exLogName, ts);
					TransitionSystemVertex createdVertex = ts
							.addVertex(newVertex);
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
					currentTimestamp.setTime(newTimestamp.getTime());
				}
				doc = entry.getElement();
				if ((genFlags & TSConstants.EVENT_TYPES) == TSConstants.EVENT_TYPES)
					doc = doc.concat(PetrifyConstants.EVENTTYPESEPARATOR)
							.concat(entry.getType());
				docs.add(doc);
				commit.add(doc);
			}
			// finish the game: add the last vertex and edge
			TransitionSystemVertex newVertex = factory.createVertex(setsOrBags,
					docs, exLogName, ts);
			TransitionSystemVertex createdVertex = ts.addVertex(newVertex);
			if ((createdVertex == lastVertex)
					&& ((genFlags & TSConstants.KILL_LOOPS) == TSConstants.KILL_LOOPS))
				;
			else {
				TransitionSystemEdge edge = new TransitionSystemEdge(commit,
						lastVertex, createdVertex);
				ts.addEdge(edge);
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
						finalDocSet, createdVertex, finalVertex);
				ts.addEdge(finalEdge);
			}
		}
		return ts;
	}
}
