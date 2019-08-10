/*
 * Created on May 20, 2005
 *
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * (c) 2005 Technische Universiteit Eindhoven, Christian W. Guenther
 * all rights reserved
 * 
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source 
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */
package org.processmining.mining.dmcscanning;

import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Progress;
import org.processmining.mining.dmcscanning.equivalence.ObjectEquivalence;
import org.processmining.mining.dmcscanning.logutils.AbstractEvent;
import org.processmining.mining.dmcscanning.logutils.LogItemOrder;

/**
 * BufferedDMCScanner This class provides static facilities for extracting DMCs
 * from a log. Next to integrating event buffering (for implementing the
 * shifting scan window overlay) it encapsulates complete DMC extraction and
 * handling, returning all DMCs within the provided log within a dedicated
 * DMCSet instance for further processing.
 * 
 * Notice: Consider moving these static method(s) to another class to clean up
 * design.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class BufferedDmcScanner {

	/**
	 * Scans a log for all DMCs, extracts them and returns them in a DMCSet
	 * collection. The scanning pass is implemented as follows: The log is
	 * scanned event-wise, 'looking ahead' a subset of events as specified by
	 * maximum proximity and event count. Within that subset, all events that
	 * satisfy the specified conditions (equal originator or event type) are
	 * added to the currently scanned DMC.
	 * 
	 * @param reader
	 *            the LogReader to be used for extracting the log in question
	 * @param progress
	 *            a Progress window to display status information
	 * @param maxProximity
	 *            the temporal extension (in milliseconds) of the scan window
	 *            (i.e. the maximum time between the first and last events
	 *            within a DMC) - ignored if <=0.
	 * @param maxEvents
	 *            maximum number of events to be contained within a DMC -
	 *            ignored if <=0.
	 * @param enforceOriginator
	 *            only events with the same originator are contained within one
	 *            DMC
	 * @param enforceEventType
	 *            only events with the same type are contained within one DMC
	 * @return a DMCSet instance containing the scanning result, i.e. all
	 *         extracted DMCs
	 */
	public static DmcSet scanInitialDmcs(LogReader reader, Progress progress,
			long maxProximity, long maxEvents, boolean enforceOriginator,
			boolean enforceEventType, ObjectEquivalence equivalence) {
		progress.setNote("Scanning initial clusters...");
		progress.setMinMax(0, reader.getLogSummary()
				.getNumberOfAuditTrailEntries());
		DmcSet set = new DmcSet();
		ArrayList<AbstractEvent> buffer = new ArrayList<AbstractEvent>();
		long curMaxTime = Long.MAX_VALUE;
		Dmc.resetIdCounter();
		Admc.resetIdCounter();
		LogItemOrder.reset();
		AbstractEvent event = null;
		// iterate through all process instances within the log
		for (int i = 0; i < reader.numberOfInstances(); i++) {
			// initialize
			buffer.clear();
			curMaxTime = Long.MAX_VALUE;
			ProcessInstance procInst = reader.getInstance(i);
			Iterator ateIterator = procInst.getAuditTrailEntryList().iterator();
			// assemble DMCs
			if (ateIterator.hasNext()) { // skip empty process instances
				event = AbstractEvent.create(procInst,
						(AuditTrailEntry) ateIterator.next()); // initial event
				buffer.add(event);
				curMaxTime = event.getTimestamp().getTime() + maxProximity; // initialize
				// maximal
				// proximity
				// accumulate next DMC, respecting time and counter boundaries
				while (buffer.size() >= 1) {
					// get next event, if available
					if (ateIterator.hasNext()) {
						event = AbstractEvent.create(procInst,
								(AuditTrailEntry) ateIterator.next());
					}
					// extract next DMC, if boundary conditions satisfied
					// (checking validity first),
					// or if no further events in process instance
					if ((ateIterator.hasNext() == false)
							|| ((maxProximity > 0) && (event.getTimestamp()
									.getTime() > curMaxTime))
							|| ((maxEvents > 0) && (buffer.size() == maxEvents))) {
						// prepare DMC assembly
						Dmc nextDmc = new Dmc();
						AbstractEvent tmpEvent = (AbstractEvent) buffer.get(0);
						String refOriginator = tmpEvent.getOriginator();
						String refEventType = tmpEvent.getType();
						// check each event in buffer for criteria
						for (Iterator it = buffer.iterator(); it.hasNext();) {
							tmpEvent = (AbstractEvent) it.next();
							if (enforceOriginator == true
									&& refOriginator != null
									&& tmpEvent.getOriginator() != null
									&& (tmpEvent.getOriginator().equals(
											refOriginator) == false)) {
								continue;
							} else if (enforceEventType
									&& (tmpEvent.getType().equals(refEventType) == false)) {
								continue;
							} else if ((maxProximity > 0)
									&& (tmpEvent.getTimestamp().getTime() > curMaxTime)) {
								continue;
							} else {
								nextDmc.addEvent(tmpEvent);
							}
						}
						nextDmc.makeFootprintCanonical(equivalence); // make
						// canonical
						set.insert(nextDmc); // add to (initial) DMC set
						buffer.remove(0); // move scan window 'to the right'
						// adjust boundary indicators
						if (buffer.size() > 0) { // buffer may be empty
							curMaxTime = ((AbstractEvent) buffer.get(0))
									.getTimestamp().getTime()
									+ maxProximity;
						}
						// progress notification
						progress.setProgress(set.size());
					}
					// if (above) new event has been extracted, append to buffer
					if ((buffer.size() > 0)
							&& (event != buffer.get(buffer.size() - 1))) {
						buffer.add(event);
					}
				}
			}
		}
		return set;
	}

}
