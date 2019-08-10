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

package org.processmining.framework.log;

import java.util.ArrayList;

/**
 * A list of <code>LogEvent</code> objects.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 * @author Peter van den Brand
 * @version 1.0
 */

public class LogEvents extends ArrayList<LogEvent> {

	/**
	 * ID for serialization not used yet; implement properly if this changes!
	 */
	private static final long serialVersionUID = 3891096942063774461L;

	/**
	 * Constructor for an empty list.
	 */
	public LogEvents() {
	}

	/**
	 * Find a <code>LogEvent</code> object by its name and type. Returns null if
	 * there is no such <code>LogEvent</code> in the list.
	 * 
	 * @param name
	 *            the name of the <code>LogEvent</code> to find
	 * @param type
	 *            the type of the <code>LogEvent</code> to find
	 * @return the <code>LogEvent</code> object if it is found, null otherwise
	 */
	public LogEvent findLogEvent(String name, String type) {
		LogEvent event = null;
		for (int i = 0; i < size(); i++) {
			event = getEvent(i);
			if (event.equals(name, type)) {
				return event;
			}
		}
		return null; // not found
	}

	/**
	 * Find the index of a given <code>LogEvent</code> object in the list.
	 * Returns -1 if there is no such <code>LogEvent</code> in the list.
	 * 
	 * @param name
	 *            the name of the <code>LogEvent</code> to find
	 * @param type
	 *            the type of the <code>LogEvent</code> to find
	 * @return the index of the <code>LogEvent</code> object in the list if it
	 *         is found, -1 otherwise
	 */
	public int findLogEventNumber(String name, String type) {
		for (int i = 0; i < size(); i++) {
			if (getEvent(i).equals(name, type)) {
				return i;
			}
		}
		return -1; // not found
	}

	/**
	 * Returns the item at the given index.
	 * 
	 * @param i
	 *            the index
	 * @return the item at index i
	 */
	public LogEvent getEvent(int i) {
		return get(i);
	}

	/**
	 * Returns the maximal occurrence count (i.e., occurrence count of the most
	 * frequent log event in the set)
	 * 
	 * @return
	 */
	public int getMaxOccurrenceCount() {
		int max = 0;
		for (LogEvent logEvent : this) {
			int occ = logEvent.getOccurrenceCount();
			if (occ > max) {
				max = occ;
			}
		}
		return max;
	}
}
