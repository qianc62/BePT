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

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class ContinuousSemanticsParser implements HeuristicsParser {
	protected MarkingHeuristicsNet marking = null;
	protected HNSubSet disabledElements = null;
	protected int numUnparsedElements = 0;
	protected LogEvents logEvents = null;
	protected int numParsedElements = 0;
	protected int numMissingTokens = 0;
	protected int numExtraTokensLeftBehind = 0;

	// //////////////////////////////
	protected int traceSize = 0;

	protected boolean properlyCompleted = false;

	public ContinuousSemanticsParser(HeuristicsNet net, Random generator) {
		marking = new MarkingHeuristicsNet(net, generator);
		disabledElements = new HNSubSet();
		this.logEvents = net.getLogEvents();
	}

	protected void reset() {
		marking.reset();
		disabledElements = new HNSubSet();
		properlyCompleted = false;
		numParsedElements = 0;
		numUnparsedElements = 0;
		numMissingTokens = 0;
		numExtraTokensLeftBehind = 0;

	}

	public boolean parse(ProcessInstance pi) {
		Iterator ATEntriesListIterator = pi.getAuditTrailEntryList().iterator();
		AuditTrailEntry ATEntry = null;
		int numTokens = 0;
		int element = 0;
		int parsingTaskAtPosition = 0;

		reset();

		while (ATEntriesListIterator.hasNext()) {
			ATEntry = (AuditTrailEntry) ATEntriesListIterator.next();
			element = logEvents.findLogEventNumber(ATEntry.getElement(),
					ATEntry.getType());

			try {
				numTokens = marking.fire(element, pi, parsingTaskAtPosition);
				if (numTokens > 0) {
					numMissingTokens += numTokens;
					disabledElements.add(element);
				} else {
					numParsedElements++;
				}

			} catch (ArrayIndexOutOfBoundsException exc) {
				// The searched element does not exist in the individual.
				// This may happen when importing logs and not associating all
				// ATEentries to an element in the individual.
				registerProblemWhileParsing(element);
			} catch (IllegalArgumentException exc) {
				// The searched element does not exist in the individual.
				// This may happen when importing logs and not associating all
				// tasks in the log with tasks in the individual.
				registerProblemWhileParsing(element);
			} catch (NullPointerException exc) {
				// The searched element does not exist in the individual.
				// This may happen when importing logs and not associating all
				// tasks in the log with tasks in the individual.
				registerProblemWhileParsing(element);
			}

			parsingTaskAtPosition++;
		}

		numUnparsedElements = pi.getAuditTrailEntryList().size()
				- numParsedElements;

		if (disabledElements.size() > 0) { // checking if there were missing
			// tokens
			// I needed to use the variable 'disabledElements' because the
			// marking.properlyCompleted()
			// will not work when one of the exceptions in the try/catch abore
			// are raised.
			properlyCompleted = false;
		} else { // checking if tokens are left behind...
			properlyCompleted = marking.properlyCompleted();
		}
		if (marking.endPlace()) {
			numExtraTokensLeftBehind = marking.getNumberTokens() - 1;
			// note that the extra tokens in the end place are also counted.
		} else {
			numExtraTokensLeftBehind = marking.getNumberTokens();
		}

		return true;
	}

	protected void registerProblemWhileParsing(int element) {
		numMissingTokens++;
		disabledElements.add(element);
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

	public int getNumMissingTokens() {
		return numMissingTokens;
	}

	public int getNumExtraTokensLeftBehind() {
		return numExtraTokensLeftBehind;
	}

	public boolean getProperlyCompleted() {
		return properlyCompleted;
	}

	public int getNumParsedElements() {
		return numParsedElements;
	}

}
