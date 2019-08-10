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
package org.processmining.mining.dmcscanning.logutils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.ProcessInstance;

/**
 * AnchoredEvent is a wrapper class for AuditTrailEntry. It transparently
 * provides all functionality of AuditTrailEntry, augmented by a reference to
 * the originating ProcessInstance, plus the sequence number (or, position)
 * within that trace. Notice: If the AuditTrailEntry is extended with a copy
 * constructor this class can be simplified by deriving it directly from
 * AuditTrailEntry.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class AnchoredEvent implements LogItem {

	// static facility for automatizing position counting
	protected static HashMap positions = new HashMap();

	/**
	 * provides a convenience facility for automatically managing a set of
	 * key-unique counters. In short, we maintain to each process instance id a
	 * long counter. Requesting a new counter value to a process instance id
	 * from this method returns a unique (w.r.t. this process instance id)
	 * counter value and transparently increments the counter in the back.
	 * 
	 * @param processInstanceId
	 *            the process instance id within which the counter value is
	 *            unique
	 * @return next unique counter value, ascending, starting from 0.
	 */
	protected static long nextCounterFor(String processInstanceId) {
		if (positions.containsKey(processInstanceId)) {
			// counter already present, increment and return
			long counter = ((Long) positions.get(processInstanceId))
					.longValue();
			positions.put(processInstanceId, new Long(counter + 1));
			return counter;
		} else {
			// initialize and insert new counter
			positions.put(processInstanceId, new Long(1));
			return 0;
		}
	}

	/**
	 * resets all counters
	 */
	public static void resetCounters() {
		positions = new HashMap();
	}

	/*
	 * instance attributes.
	 */
	protected AuditTrailEntry entry = null;
	protected ProcessInstance processInstance = null;
	protected long position = -1;

	/**
	 * Creates a new AnchoredEvent, wrapping an AuditTrailEntry with augmented
	 * Meta-Information
	 * 
	 * @param anEntry
	 *            the AuditTrailEntry instance to be wrapped
	 * @param aProcessInstance
	 *            process instance the ATE belongs to
	 * @param aPosition
	 *            position of the ATE within the process instance (starting from
	 *            0)
	 */
	public AnchoredEvent(AuditTrailEntry anEntry,
			ProcessInstance aProcessInstance, long aPosition) {
		entry = anEntry;
		processInstance = aProcessInstance;
		position = aPosition;
	}

	/**
	 * Creates a new AnchoredEvent that is automatically equipped with a
	 * sequence/position counter value, unique to the provided process instance.
	 * 
	 * @param anEntry
	 *            the AuditTrailEntry instance to be wrapped
	 * @param aProcessInstance
	 *            process instance the ATE belongs to
	 */
	public AnchoredEvent(AuditTrailEntry anEntry,
			ProcessInstance aProcessInstance) {
		this(anEntry, aProcessInstance, nextCounterFor(aProcessInstance
				.getProcess()
				+ aProcessInstance.getName()));
	}

	/**
	 * Tests two events for equality, based on their process instance and
	 * position
	 * 
	 * @param other
	 * @return
	 */
	public boolean equals(AnchoredEvent other) {
		return (isInSameProcessInstanceAs(other) && (position == other
				.getPosition()));
	}

	/**
	 * @return the wrapped audit trail entry
	 */
	public AuditTrailEntry getAuditTrailEntry() {
		return entry;
	}

	/**
	 * @return the absolute position of this event with the process instance
	 *         (starting from 0)
	 */
	public long getPosition() {
		return position;
	}

	/**
	 * @return reference to the process instance this event belongs to
	 */
	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	/**
	 * Test, whether another event belongs to the same process instance
	 * 
	 * @param other
	 *            another AnchoredEvent
	 * @return whether both events refer to the same (object identity) process
	 *         instance
	 */
	public boolean isInSameProcessInstanceAs(AnchoredEvent other) {
		return (processInstance == other.getProcessInstance());
	}

	/**
	 * Retrieves the number of steps, that the calling event is away from the
	 * other one. This distance can also be negative, indicating that the other
	 * event is in fact located after the calling one.
	 * 
	 * @param other
	 * @return
	 */
	public long positionRelativeTo(AnchoredEvent other) {
		return (position - other.getPosition());
	}

	/**
	 * -- convenience method --
	 * 
	 * @return whether this event has occurred later than the parameter event
	 */
	public boolean occurredLaterThan(AnchoredEvent other) {
		return (positionRelativeTo(other) > 0);
	}

	/**
	 * -- convenience method --
	 * 
	 * @return whether this event has occurred earlier than the parameter event
	 */
	public boolean occurredEarlierThan(AnchoredEvent other) {
		return !occurredLaterThan(other);
	}

	/*
	 * --------- methods wrapped from AuditTrailEntry
	 * ---------------------------------
	 */
	/**
	 * @return wfm element name
	 */
	public String getElement() {
		return entry.getElement();
	}

	/**
	 * @return type of ATE
	 */
	public String getType() {
		return entry.getType();
	}

	/**
	 * @return timestamp of event
	 */
	public Date getTimestamp() {
		return entry.getTimestamp();
	}

	/**
	 * @return originator of event
	 */
	public String getOriginator() {
		return entry.getOriginator();
	}

	/**
	 * @return data fields of event
	 */
	public Map getData() {
		return entry.getData();
	}

	/**
	 * @return a string representation
	 */
	public String toString() {
		return "[AnchoredEntry wrapping: " + entry.toString() + ", ProcInst: "
				+ processInstance.getName() + ", SeqNrPos: " + position + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		AnchoredEvent other = (AnchoredEvent) arg0;
		return this.getComparisonIndex() - other.getComparisonIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.dmcscanning.LogItem#getStartTimestamp()
	 */
	public Date getLeftBoundaryTimestamp() {
		return getTimestamp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.dmcscanning.LogItem#getEndTimestamp()
	 */
	public Date getRightBoundaryTimestamp() {
		return getTimestamp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.dmcscanning.LogItem#isAtomic()
	 */
	public boolean isAtomic() {
		// anchored events are not durable
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.logutils.LogItem#getComparisonIndex
	 * ()
	 */
	public int getComparisonIndex() {
		return (int) this.getPosition();
	}
}
