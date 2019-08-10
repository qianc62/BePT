package org.processmining.analysis.performance.componentstate.logutil;

import java.util.Date;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.ProcessInstance;
import java.util.Map;

/**
 * @author Minseok Song
 */
public class AbstractLogUnit implements Comparable {

	/*
	 * static facilities for providing unique ascending indices
	 */
	protected ProcessInstance processInstance = null;
	protected AuditTrailEntry logEvent = null;
	protected long position = -1;
	protected long relativePosition = -1;
	protected Date currentDate = new Date();
	protected Date relativeDate = null;

	/**
	 * constructor protected - use factory methods for instantiation;
	 * 
	 * @param aProcessInstance
	 *            the process instance where this abstract event is contained
	 * @param aStartEvent
	 *            left boundary atomic event
	 * @param anEndEvent
	 *            right boundary atomic event
	 */
	protected AbstractLogUnit(ProcessInstance aProcessInstance,
			AuditTrailEntry aLogEvent) {
		this.processInstance = aProcessInstance;
		this.logEvent = aLogEvent;
	}

	/**
	 * Creates a new durating abstract event
	 * 
	 * @param aProcessInstance
	 * @param aStartEvent
	 * @param anEndEvent
	 * @return
	 */
	public static AbstractLogUnit create(ProcessInstance aProcessInstance,
			AuditTrailEntry aLogEvent) {
		return new AbstractLogUnit(aProcessInstance, aLogEvent);
	}

	/**
	 * @return the process instance this event stems from
	 */
	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	public String getElement() {
		return logEvent.getElement();
	}

	public String getOriginator() {
		return logEvent.getOriginator();
	}

	public String getType() {
		return logEvent.getType();
	}

	public Date getTimestamp() {
		return logEvent.getTimestamp();
	}

	public Map<String, String> getAttributes() {
		return logEvent.getAttributes();
	}

	public Date getRelativeTimestamp() {
		return new Date(logEvent.getTimestamp().getTime()
				- ((AuditTrailEntry) processInstance.getAuditTrailEntryList()
						.iterator().next()).getTimestamp().getTime());
	}

	public Date getRelativeTimestamp(Date aDate) {
		return new Date(logEvent.getTimestamp().getTime() - aDate.getTime());
	}

	public Date getRelativeRatio() {
		long over = 0, end = 1;
		try {
			over = logEvent.getTimestamp().getTime()
					- ((AuditTrailEntry) processInstance
							.getAuditTrailEntryList().iterator().next())
							.getTimestamp().getTime();
			end = ((AuditTrailEntry) processInstance.getAuditTrailEntryList()
					.get(processInstance.getAuditTrailEntryList().size() - 1))
					.getTimestamp().getTime()
					- ((AuditTrailEntry) processInstance
							.getAuditTrailEntryList().iterator().next())
							.getTimestamp().getTime();
		} catch (Exception ce) {
		}
		;
		return new Date((long) (((float) over / (float) end) * 10000));
	}

	public Date getRelativeRatio(Date startDate, Date endDate) {
		return new Date(
				(long) ((float) (logEvent.getTimestamp().getTime() - startDate
						.getTime())
						/ (float) (endDate.getTime() - startDate.getTime()) * 10000));
	}

	public Date getCurrentTimeStamp() {
		return currentDate;
	}

	public Date getActualTimeStamp() {
		return logEvent.getTimestamp();
	}

	public void setCurrentTimeStamp() {
		currentDate = logEvent.getTimestamp();
	}

	public void setCurrentTimeStampLogical() {
		currentDate = new Date(position);
	}

	public void setCurrentTimeStampLogicalRelative() {
		currentDate = new Date(relativePosition);
	}

	public void setCurrentTimeStampRelative_Instance() {
		currentDate = new Date(logEvent.getTimestamp().getTime()
				- ((AuditTrailEntry) processInstance.getAuditTrailEntryList()
						.iterator().next()).getTimestamp().getTime());
		relativeDate = currentDate;
	}

	public void setCurrentTimeStampRelative_Others(Date aDate) {
		currentDate = new Date(logEvent.getTimestamp().getTime()
				- aDate.getTime());
		relativeDate = currentDate;
	}

	public void setCurrentTimeStampRelativeRatio_Instance() {
		long over = 0, end = 1;
		try {
			over = logEvent.getTimestamp().getTime()
					- ((AuditTrailEntry) processInstance
							.getAuditTrailEntryList().iterator().next())
							.getTimestamp().getTime();
			end = ((AuditTrailEntry) processInstance.getAuditTrailEntryList()
					.get(processInstance.getAuditTrailEntryList().size() - 1))
					.getTimestamp().getTime()
					- ((AuditTrailEntry) processInstance
							.getAuditTrailEntryList().iterator().next())
							.getTimestamp().getTime();
		} catch (Exception ce) {
		}
		;
		currentDate = new Date((long) (((float) over / (float) end) * 10000));
		relativeDate = currentDate;
	}

	public void setCurrentTimeStampRelativeRatio_Others(Date startDate,
			Date endDate) {
		currentDate = new Date((long) ((float) (logEvent.getTimestamp()
				.getTime() - startDate.getTime())
				/ (float) (endDate.getTime() - startDate.getTime()) * 10000));
		relativeDate = currentDate;
	}

	// added for performance
	public void setCurrentTimeStampRelative() {
		currentDate = relativeDate;
	}

	// position related methods
	public long getPosition() {
		return position;
	}

	public void setPosition(long pos) {
		position = pos;
	}

	public void resetPosition() {
		position = -1;
	}

	// relative position related methods
	public long getRelativePosition() {
		return relativePosition;
	}

	public void setRelativePosition(long pos) {
		relativePosition = pos;
	}

	public void resetRelativePosition() {
		relativePosition = -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		if ((currentDate.getTime() - ((AbstractLogUnit) arg0)
				.getCurrentTimeStamp().getTime()) > 0)
			return 1;
		if ((currentDate.getTime() - ((AbstractLogUnit) arg0)
				.getCurrentTimeStamp().getTime()) < 0)
			return -1;
		else
			return 0;
	}

	public int getComparisonIndex() {
		return (int) position;
	}

	public String toString() {
		return "AbstractEvent '" + logEvent.getElement() + "' ("
				+ logEvent.getTimestamp() + ")";
	}
}
