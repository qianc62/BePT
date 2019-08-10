/**
 * Project: ProM
 * File: SplitOnBreakDmcScanner.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Apr 24, 2006, 2:19:17 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 ***********************************************************
 * 
 * This software is part of the ProM package          
 * http://www.processmining.org/               
 *                                                         
 * Copyright (c) 2003-2006 TU/e Eindhoven
 * and is licensed under the
 * Common Public License, Version 1.0
 * by Eindhoven University of Technology 
 * Department of Information Systems        
 * http://is.tm.tue.nl            
 *                                               
 ***********************************************************/
package org.processmining.mining.dmcscanning;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Progress;
import org.processmining.mining.dmcscanning.equivalence.ObjectEquivalence;
import org.processmining.mining.dmcscanning.logutils.AbstractEvent;
import org.processmining.mining.dmcscanning.logutils.LogItemOrder;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class SplitOnBreakDmcScanner {

	public static DmcSet scanInitialDmcs(LogReader reader, Progress progress,
			long breakThreshold, long maxEventsThreshold,
			ObjectEquivalence equivalence) {
		DmcSet set = new DmcSet();
		reader.reset();
		progress.setNote("Scanning initial DMCs...");
		progress.setMinMax(0, reader.getLogSummary()
				.getNumberOfAuditTrailEntries());
		long lastTimestamp = Long.MAX_VALUE;
		Dmc.resetIdCounter();
		Admc.resetIdCounter();
		LogItemOrder.reset();
		AbstractEvent event = null;
		Dmc currentDmc = null;
		while (reader.hasNext()) {
			// initialize (substract break threshold to prevent overflow
			// of the long value on comparison below)
			lastTimestamp = Long.MAX_VALUE - breakThreshold;
			ProcessInstance procInst = reader.next();
			AuditTrailEntries ates = procInst.getAuditTrailEntries();
			currentDmc = new Dmc();
			while (ates.hasNext()) {
				event = AbstractEvent.create(procInst, ates.next());
				if (event.getTimestamp().getTime() > lastTimestamp
						+ breakThreshold) {
					// break detected; finalize and flush current cluster
					currentDmc.makeFootprintCanonical(equivalence);
					set.insert(currentDmc);
					// create new current cluster and re-initialize
					currentDmc = new Dmc();
					currentDmc.addEvent(event);
					lastTimestamp = Long.MAX_VALUE - breakThreshold;
				} else {
					// no break; adjust last timestamp and add to current
					// cluster
					lastTimestamp = event.getTimestamp().getTime();
					currentDmc.addEvent(event);
				}
				// respect maximal number of events per cluster
				if (currentDmc.size() >= maxEventsThreshold) {
					// finalize and insert last cluster of this process instance
					currentDmc.makeFootprintCanonical(equivalence);
					set.insert(currentDmc);
					// intialize scanner
					currentDmc = new Dmc();
					lastTimestamp = Long.MAX_VALUE - breakThreshold;
				}
			}
			if (currentDmc.size() > 0) {
				// finalize and insert last cluster of this process instance
				currentDmc.makeFootprintCanonical(equivalence);
				set.insert(currentDmc);
			}
		}
		return set;
	}

}
