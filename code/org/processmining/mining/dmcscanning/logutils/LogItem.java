/*
 * Created on May 30, 2005
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

/**
 * Interface 'LogItem' This interface is to be used to access position and
 * extension information about log-anchored items. Implementing classes can
 * represent log items spread over multiple sub-items, and as such the start-
 * and end-getter methods are expected to return the respective boundary values.
 * Intented for visualizing log-anchored items from an abstract view.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public interface LogItem extends Comparable {

	/**
	 * Start and end position are expected to represent sub-item boundaries of
	 * compound log items. In case the implementing class represents an atomic
	 * log item, the respective start- and end-getters are expected to return an
	 * identical value.
	 * 
	 * @return the timestamp representing the left boundary of this log item
	 */
	public Date getLeftBoundaryTimestamp();

	/**
	 * Start and end position are expected to represent sub-item boundaries of
	 * compound log items. In case the implementing class represents an atomic
	 * log item, the respective start- and end-getters are expected to return an
	 * identical value.
	 * 
	 * @return the timestamp representing the right boundary of this log item
	 */
	public Date getRightBoundaryTimestamp();

	/**
	 * The return value represents the logical position of this log item within
	 * the sequence of events (starting from zero and ascending). If not
	 * implemented, return a negative value here.
	 * 
	 * @return
	 */
	public long getPosition();

	/**
	 * Determines, whether this log event is atomic or durable; Atomic, in this
	 * context, is interpreted as tStart == tEnd, in contrast to durable, where
	 * tStart < tEnd.
	 * 
	 * @return
	 */
	public boolean isAtomic();

}
