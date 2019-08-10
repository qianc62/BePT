/*
 * Created on Jun 10, 2005
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
package org.processmining.mining.dmcscanning.logutils;

import java.util.ArrayList;
import java.util.HashMap;

import org.processmining.framework.log.ProcessInstance;

/**
 * Maintains the absolute ordering of abstract events even from different
 * process instances.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class LogItemOrder implements Comparable {

	protected static ArrayList processInstances = new ArrayList();
	protected static HashMap logItemCounters = new HashMap();

	protected int processInstanceOrder = 0;
	protected int logItemOrder = 0;

	protected LogItemOrder(int procInst, int logItem) {
		processInstanceOrder = procInst;
		logItemOrder = logItem;
	}

	public static void reset() {
		processInstances = new ArrayList();
		logItemCounters = new HashMap();
	}

	public static LogItemOrder getOrder(AbstractEvent item) {
		int piOrder = 0;
		int liOrder = 0;
		// determine ordering index of process instance first
		String piKey = getProcessInstanceId(item.getProcessInstance());
		if (processInstances.contains(piKey) == false) {
			processInstances.add(piKey);
			logItemCounters.put(piKey, new Integer(0));
		}
		piOrder = processInstances.indexOf(piKey);
		// determine relative ordering index of log item within process instance
		liOrder = ((Integer) logItemCounters.get(piKey)).intValue();
		logItemCounters.put(piKey, new Integer(liOrder + 1));
		// return unique ordering object
		return new LogItemOrder(piOrder, liOrder);
	}

	protected static String getProcessInstanceId(ProcessInstance procInst) {
		return (procInst.getProcess() + "_" + procInst.getName());
	}

	public int getProcessInstanceOrder() {
		return processInstanceOrder;
	}

	public int getLogItemOrder() {
		return logItemOrder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		LogItemOrder other = (LogItemOrder) arg0;
		if (processInstanceOrder == other.getProcessInstanceOrder()) {
			// same process instance
			return logItemOrder - other.getLogItemOrder();
		} else {
			return processInstanceOrder - other.getProcessInstanceOrder();
		}
	}

	/**
	 * checks for identity of two log item order objects
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			// reference equality
			return true;
		}
		if (obj instanceof LogItemOrder) {
			LogItemOrder other = (LogItemOrder) obj;
			if ((other.getProcessInstanceOrder() == processInstanceOrder)
					&& (other.getLogItemOrder() == logItemOrder)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public long getLongRepresentation() {
		long longRep = processInstanceOrder * 10000;
		longRep += logItemOrder;
		return longRep;
	}

}
