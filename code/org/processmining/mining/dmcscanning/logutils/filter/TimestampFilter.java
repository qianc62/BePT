/*
 * Created on Jun 3, 2005
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
package org.processmining.mining.dmcscanning.logutils.filter;

import java.util.Date;

import org.processmining.framework.log.AuditTrailEntry;

/**
 * Event filter matching all events before, after or at a given date. For
 * creating date-based range filters, compose two of these (left & right
 * boundary)
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class TimestampFilter implements EventFilter {

	protected static int IS_BEFORE = 0;
	protected static int IS_AFTER = 0;
	protected static int IS_EXACTLY = 0;

	protected Date timestamp = null;
	protected int mode = 0;

	protected TimestampFilter() {
		// disable standard constructor
	}

	/**
	 * protected constructor - use factory methods!
	 * 
	 * @param aTimestamp
	 *            timestamp to match
	 * @param aMode
	 *            mode in which to match (before, after, exactly)
	 */
	protected TimestampFilter(Date aTimestamp, int aMode) {
		timestamp = aTimestamp;
		mode = aMode;
	}

	/**
	 * Factory method: matches all events before a given date
	 * 
	 * @param aTimestamp
	 * @return
	 */
	public static TimestampFilter before(Date aTimestamp) {
		return new TimestampFilter(aTimestamp, TimestampFilter.IS_BEFORE);
	}

	/**
	 * Factory method: matches all events after a given date
	 * 
	 * @param aTimestamp
	 * @return
	 */
	public static TimestampFilter after(Date aTimestamp) {
		return new TimestampFilter(aTimestamp, TimestampFilter.IS_AFTER);
	}

	/**
	 * Factory method: matches all events exactly at a given date
	 * 
	 * @param aTimestamp
	 * @return
	 */
	public static TimestampFilter exactly(Date aTimestamp) {
		return new TimestampFilter(aTimestamp, TimestampFilter.IS_EXACTLY);
	}

	/**
	 * Factory method: matches all events before a given date in milliseconds
	 * 
	 * @param aTimeMillis
	 * @return
	 */
	public static TimestampFilter before(long aTimeMillis) {
		return new TimestampFilter(new Date(aTimeMillis),
				TimestampFilter.IS_BEFORE);
	}

	/**
	 * Factory method: matches all events after a given date in milliseconds
	 * 
	 * @param aTimeMillis
	 * @return
	 */
	public static TimestampFilter after(long aTimeMillis) {
		return new TimestampFilter(new Date(aTimeMillis),
				TimestampFilter.IS_AFTER);
	}

	/**
	 * Factory method: matches all events exactly at a given date in
	 * milliseconds
	 * 
	 * @param aTimeMillis
	 * @return
	 */
	public static TimestampFilter exactly(long aTimeMillis) {
		return new TimestampFilter(new Date(aTimeMillis),
				TimestampFilter.IS_EXACTLY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.abstractlog.EventFilter#matches(
	 * org.processmining.framework.log.AuditTrailEntry)
	 */
	public boolean matches(AuditTrailEntry entry) {
		if (mode == TimestampFilter.IS_AFTER) {
			return entry.getTimestamp().after(timestamp);
		} else if (mode == TimestampFilter.IS_BEFORE) {
			return entry.getTimestamp().before(timestamp);
		} else if (mode == TimestampFilter.IS_EXACTLY) {
			return (entry.getTimestamp().compareTo(timestamp) == 0);
		} else {
			return true; // incorrect filter matches all (excluded by factory
			// methods)
		}
	}

}
