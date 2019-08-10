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
 * @author not attributable
 * @version 1.0
 */

public class ExtraBehaviorParser extends ContinuousSemanticsParser {

	private int numTotalEnabledElements = 0;

	public ExtraBehaviorParser(HeuristicsNet net, Random generator) {
		super(net, generator);
	}

	protected void reset() {
		super.reset();
		numTotalEnabledElements = 0;
	}

	public boolean parse(ProcessInstance pi) {
		Iterator ATEntriesIterator = pi.getAuditTrailEntryList().iterator();
		AuditTrailEntry ATEntry = null;
		int numTokens = 0;
		int element = 0;
		int parsingTaskAtPosition = 0;

		reset();

		while (ATEntriesIterator.hasNext()) {
			ATEntry = (AuditTrailEntry) ATEntriesIterator.next();
			element = logEvents.findLogEventNumber(ATEntry.getElement(),
					ATEntry.getType());
			numTotalEnabledElements += marking.getCurrentNumEnabledElements();
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

	public int getNumTotalEnabledElements() {
		return numTotalEnabledElements;
	}

}
