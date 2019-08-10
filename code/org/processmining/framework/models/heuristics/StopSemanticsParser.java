/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.heuristics;

import java.util.*;

import org.processmining.framework.log.*;
import cern.colt.matrix.*;

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
public class StopSemanticsParser implements HeuristicsParser {

	private MarkingHeuristicsNet marking = null;
	private HNSubSet disabledElements = null;
	private LogEvents logEvents = null;
	private int numParsedElements = 0;
	private int numUnparsedElements = 0;
	private boolean properlyCompleted = false;
	private boolean completed = false;
	HeuristicsNet net = null;

	public StopSemanticsParser(HeuristicsNet net, Random generator) {
		marking = new MarkingHeuristicsNet(net, generator);
		disabledElements = new HNSubSet();
		this.logEvents = net.getLogEvents();
		this.net = net;
	}

	private void reset() {
		marking.reset();
		disabledElements = null;
		disabledElements = new HNSubSet();
		numParsedElements = 0;
		numUnparsedElements = 0;
		properlyCompleted = false;
		completed = false;
	}

	public boolean parse(ProcessInstance pi) {
		Iterator ATEntriesListIterator = pi.getAuditTrailEntryList().iterator();
		AuditTrailEntry ATEntry = null;
		int element = 0;
		int numTokens = 0;
		int parsingTaskAtPosition = 0;
		DoubleMatrix2D arcUsageBeforeFiring = null;
		int[] duplicatesActualFiringBeforeFiring = null;

		reset();

		while (ATEntriesListIterator.hasNext()) {
			ATEntry = (AuditTrailEntry) ATEntriesListIterator.next();
			element = logEvents.findLogEventNumber(ATEntry.getElement(),
					ATEntry.getType());
			try {
				arcUsageBeforeFiring = net.getArcUsage();
				duplicatesActualFiringBeforeFiring = net
						.getDuplicatesActualFiring();
				numTokens = marking.fire(element, pi, parsingTaskAtPosition);
			} catch (ArrayIndexOutOfBoundsException exc) {
				// The searched element does not exist in the individual.
				// This may happen when importing logs and not associating all
				// ATEentries to an element in the individual.
				numTokens++;
			} catch (IllegalArgumentException exc) {
				// The searched element does not exist in the individual.
				// This may happen when importing logs and not associating all
				// tasks in the log with tasks in the individual.
				numTokens++;
			} catch (NullPointerException exc) {
				// The searched element does not exist in the individual.
				// This may happen when importing logs and not associating all
				// tasks in the log with tasks in the individual.
				numTokens++;
			}

			parsingTaskAtPosition++;
			if (numTokens > 0) {
				disabledElements.add(element);
				numUnparsedElements = pi.getAuditTrailEntryList().size()
						- numParsedElements;
				// because the element was not enabled, we need to restore the
				// "arc usage"
				// and "actual firing" to the values before the firing of
				// element
				net.setArcUsage(arcUsageBeforeFiring);
				net
						.setDuplicatesActualFiring(duplicatesActualFiringBeforeFiring);
				return false;
			}
			numParsedElements++;

		}

		properlyCompleted = marking.properlyCompleted();
		completed = true;

		return true;
	}

	public HNSubSet getDisabledElements() {
		return disabledElements;
	}

	public int getSizeDisabledElements() {
		return disabledElements.size();
	}

	public int getNumUnparsedElements() {
		return numUnparsedElements;
	}

	public int getNumParsedElements() {
		return numParsedElements;
	}

	public boolean getProperlyCompleted() {
		return properlyCompleted;
	}

	public boolean getCompleted() {
		return completed;
	}

}
