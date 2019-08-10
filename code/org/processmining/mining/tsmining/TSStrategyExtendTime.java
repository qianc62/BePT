package org.processmining.mining.tsmining;

import java.util.ArrayList;
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
import org.processmining.framework.models.transitionsystem.TransitionSystemVertex;

/**
 * @author Vladimir Rubin
 * @version 1.0
 */
public class TSStrategyExtendTime extends TSStrategy {

	public TSStrategyExtendTime(LogReader log, TransitionSystem ts,
			int genFlags, int typeOfTS) {
		super(log, ts, genFlags, typeOfTS);
	}

	@Override
	/*
	 * Add transitions to the existing basic ts A transition is added, when all
	 * the preconditions are fulfilled, i.e. a state contains its input
	 * documents
	 */
	public void modifyTS() {
		ArrayList vertices = ts.getVerticeList();
		int vSize = vertices.size();
		Iterator logIt = log.instanceIterator();

		while (logIt.hasNext()) {
			ProcessInstance exLog = (ProcessInstance) logIt.next();
			Iterator entryIt = exLog.getAuditTrailEntryList().iterator();
			Date currentTimestamp, newTimestamp;
			HashSet<String> commit = new HashSet<String>();
			AuditTrailEntry entry = (AuditTrailEntry) entryIt.next();
			String doc = entry.getElement();
			if ((genFlags & TSConstants.EVENT_TYPES) == TSConstants.EVENT_TYPES)
				doc = doc.concat(PetrifyConstants.EVENTTYPESEPARATOR).concat(
						entry.getType());
			commit.add(doc);
			currentTimestamp = entry.getTimestamp();
			while (entryIt.hasNext()) {
				entry = (AuditTrailEntry) entryIt.next();
				newTimestamp = entry.getTimestamp();
				if (currentTimestamp.getTime() < newTimestamp.getTime()) {
					int i = 0, j = 0;
					for (; i < vSize; i++) {
						TransitionSystemVertex vertexA = (TransitionSystemVertex) vertices
								.get(i);
						Collection<String> docsA = vertexA.getDocs();
						if (!isIntersection(docsA, commit)) {
							Collection<String> docsA_commit = factory
									.createDocs(setsOrBags);
							docsA_commit.addAll(docsA);
							docsA_commit.addAll(commit);
							for (j = 0; j < vSize; j++) {
								TransitionSystemVertex vertexB = (TransitionSystemVertex) vertices
										.get(j);
								if (vertexB != vertexA) {
									Collection<String> docsB = vertexB
											.getDocs();
									if (docsB.equals(docsA_commit)) {
										addArc(commit, vertexA, vertexB); // use
										// operation
										// from
										// the
										// strategy
									}
								}
							}
						}
					}
					commit = new HashSet<String>();
					currentTimestamp.setTime(newTimestamp.getTime());
				}
				doc = entry.getElement();
				if ((genFlags & TSConstants.EVENT_TYPES) == TSConstants.EVENT_TYPES)
					doc = doc.concat(PetrifyConstants.EVENTTYPESEPARATOR)
							.concat(entry.getType());
				commit.add(doc);
			}
		}
	}
}
