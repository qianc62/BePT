package org.processmining.framework.models.logabstraction;

import java.util.List;
import org.processmining.framework.log.LogEvent;
import java.util.ArrayList;
import java.util.HashMap;
import org.processmining.framework.log.AuditTrailEntry;
import java.util.Iterator;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.analysis.log.scale.ProcessInstanceScale;
import java.util.Arrays;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.Message;

import java.util.SortedSet;
import java.util.TreeSet;

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
public class MultiSetAbstraction extends ProcessInstanceBasedAbstraction {

	private HashMap<LogEvent, Integer> processSteps = new HashMap<LogEvent, Integer>();

	protected boolean multiSet;

	public HashMap<LogEvent, Integer> getProcessSteps() {
		return processSteps;
	}

	public MultiSetAbstraction(LogReader log, ProcessInstance pi,
			ProcessInstanceScale scale, boolean multiset) {
		super(scale, log);
		this.multiSet = multiset;
		Iterator<AuditTrailEntry> it = pi.getAuditTrailEntryList().iterator();
		while (it.hasNext()) {
			AuditTrailEntry ate = it.next();
			LogEvent event = new LogEvent(ate.getElement(), ate.getType());
			if (processSteps.containsKey(event) && multiSet) {
				processSteps.put(event, new Integer(processSteps.get(event)
						.intValue() + 1));
			} else {
				// logEvents.add(event);
				processSteps.put(event, new Integer(1));
			}
		}
		addInstance(pi);
	}

	public double calculateFitness(LogAbstraction abstr) {
		MultiSetAbstraction incompleteExecution = (MultiSetAbstraction) abstr;

		double matchingSteps = 0.0;
		double executedSoFarSteps = 0.0;

		for (LogEvent step : incompleteExecution.processSteps.keySet()) {
			if (processSteps.containsKey(step)) {
				if (processSteps.get(step).intValue() >= incompleteExecution.processSteps
						.get(step).intValue()) {
					matchingSteps += incompleteExecution.processSteps.get(step)
							.intValue();
				} else {
					matchingSteps += processSteps.get(step).intValue();
				}
			}
			executedSoFarSteps += incompleteExecution.processSteps.get(step)
					.intValue();
		}

		double result;
		if (executedSoFarSteps == 0.0) {
			result = 1;
		} else {
			result = (new Double(matchingSteps) / new Double(executedSoFarSteps));
		}

		return result;
	}

	public SortedSet getPossibleNextLogEvents(LogAbstraction partialTrace) {
		if (multiSet) {
			return getMultiSetPossibleNextLogEvents(partialTrace);
		} else {
			return getSetPossibleNextLogEvents(partialTrace);
		}
	}

	public SortedSet getSetPossibleNextLogEvents(LogAbstraction partialTrace) {
		SortedSet<LogEvent> containedLogEvents = new TreeSet<LogEvent>(
				processSteps.keySet());

		return containedLogEvents;
	}

	public SortedSet getMultiSetPossibleNextLogEvents(
			LogAbstraction partialTrace) {
		MultiSetAbstraction partial = (MultiSetAbstraction) partialTrace;

		SortedSet<LogEvent> containedLogEvents = new TreeSet<LogEvent>(
				processSteps.keySet());

		for (LogEvent evt : partial.processSteps.keySet()) {
			int occInPartial = partial.processSteps.get(evt).intValue();
			if (containedLogEvents.contains(evt)) {
				int occRemaining = processSteps.get(evt).intValue()
						- occInPartial;
				if (occRemaining <= 0) {
					// no occurences of this activity are possible anymore
					containedLogEvents.remove(evt);
				}
			}

		}
		return containedLogEvents;
	}

	public String toString() {
		return processSteps.toString();
	}
}
