/*
 * Created on Jun 6, 2005
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

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.ProcessInstance;

/**
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class AbstractEvent implements LogItem {

	/*
	 * static facilities for providing unique ascending indices
	 */

	protected ProcessInstance processInstance = null;
	protected AuditTrailEntry startEvent = null;
	protected AuditTrailEntry endEvent = null;
	protected LogItemOrder position = null;

	/**
	 * constructor protected - use factory methods for instantiation;
	 * 
	 * @param aProcessInstance
	 *            the process instance where this abstract event is contained
	 * @param aStartEvent
	 *            left boundary atomic event
	 * @param anEndEvent
	 *            right boundary atomic event
	 * @param aPosition
	 *            logical log position (first=0, ascending)
	 */
	protected AbstractEvent(ProcessInstance aProcessInstance,
			AuditTrailEntry aStartEvent, AuditTrailEntry anEndEvent) {
		processInstance = aProcessInstance;
		startEvent = aStartEvent;
		endEvent = anEndEvent;
		position = LogItemOrder.getOrder(this);
	}

	/**
	 * Creates a new durating abstract event
	 * 
	 * @param aProcessInstance
	 * @param aStartEvent
	 * @param anEndEvent
	 * @return
	 */
	public static AbstractEvent create(ProcessInstance aProcessInstance,
			AuditTrailEntry aStartEvent, AuditTrailEntry anEndEvent) {
		return new AbstractEvent(aProcessInstance, aStartEvent, anEndEvent);
	}

	/**
	 * creates a new atomic abstract event
	 * 
	 * @param aProcessInstance
	 * @param aStartEvent
	 * @return
	 */
	public static AbstractEvent create(ProcessInstance aProcessInstance,
			AuditTrailEntry aStartEvent) {
		return new AbstractEvent(aProcessInstance, aStartEvent, aStartEvent);
	}

	/**
	 * @return the process instance this event stems from
	 */
	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	/**
	 * @return the start event of a composite event; single event if atomic
	 */
	public AuditTrailEntry getStartEvent() {
		return startEvent;
	}

	/**
	 * @return the end event of a composite event; single event if atomic
	 */
	public AuditTrailEntry getEndEvent() {
		return endEvent;
	}

	/**
	 * Convenience method for AuditTrailEntry compatibility; mapped to start
	 * event.
	 * 
	 * @return
	 */
	public String getElement() {
		return startEvent.getElement();
	}

	/**
	 * Convenience method for AuditTrailEntry compatibility; mapped to start
	 * event.
	 * 
	 * @return
	 */
	public String getOriginator() {
		return startEvent.getOriginator();
	}

	/**
	 * Convenience method for AuditTrailEntry compatibility; mapped to start
	 * event.
	 * 
	 * @return
	 */
	public String getType() {
		return startEvent.getType();
	}

	/**
	 * Convenience method for AuditTrailEntry compatibility; mapped to start
	 * event.
	 * 
	 * @return
	 */
	public Date getTimestamp() {
		return getLeftBoundaryTimestamp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.LogItem#getLeftBoundaryTimestamp()
	 */
	public Date getLeftBoundaryTimestamp() {
		return startEvent.getTimestamp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.LogItem#getRightBoundaryTimestamp()
	 */
	public Date getRightBoundaryTimestamp() {
		return endEvent.getTimestamp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.dmcscanning.LogItem#getPosition()
	 */
	public long getPosition() {
		return position.getLongRepresentation();
	}

	public LogItemOrder getOrder() {
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		return position.compareTo(((AbstractEvent) arg0).getOrder());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.dmcscanning.LogItem#isAtomic()
	 */
	public boolean isAtomic() {
		return ((startEvent == endEvent) || (startEvent.equals(endEvent)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.logutils.LogItem#getComparisonIndex
	 * ()
	 */
	public int getComparisonIndex() {
		return (int) position.getLongRepresentation();
	}

	public String toString() {
		return "AbstractEvent '" + startEvent.getElement() + "' ("
				+ startEvent.getTimestamp() + ")";
	}
}
