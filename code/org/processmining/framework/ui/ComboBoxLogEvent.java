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

package org.processmining.framework.ui;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.LogEventProvider;

/**
 * A ComboBoxLogEvent is used when a mapping is to be established between an
 * imported model and a loaded log. In this case every task in the model needs
 * to be either
 * <ol>
 * <li>
 * associated to an existing log event from the log, or</li>
 * <li>
 * made invisible (i.e., no log event will be associated at all), or</li>
 * <li>
 * kept visible (i.e., a dummy log event will be associated to the task as it is
 * visible, but the corresponding log event is not contained in the log).</li>
 * </ol>
 * 
 * @author bvdonge
 */
public class ComboBoxLogEvent implements Comparable, LogEventProvider {

	public final static String VISIBLE = " Make Visible";
	public final static String INVISIBLE = " Make Invisible";
	public final static String NONE = " None";

	private LogEvent e;

	/**
	 * Creates a ComboboxLogEvent for the given log event.
	 * 
	 * @param e
	 *            the given log event
	 */
	public ComboBoxLogEvent(LogEvent e) {
		this.e = e;
	}

	/**
	 * Creates the string to be displayed in the corresponding combo box entry.
	 */
	public String toString() {
		if ((e.getModelElementName().equals(VISIBLE) && e.getEventType()
				.equals(VISIBLE))
				|| (e.getModelElementName().equals(INVISIBLE) && e
						.getEventType().equals(INVISIBLE))
				|| (e.getModelElementName().equals(NONE) && e.getEventType()
						.equals(NONE))) {
			return e.getModelElementName();
		} else {
			return e.getModelElementName() + " (" + e.getEventType() + ")";
		}
	}

	/**
	 *@deprecated Please now use getLogEvent()
	 *@see getLogEvent() This method returns the current LogEvent the
	 *      LogEventProvider has stored
	 * 
	 * @return LogEvent The stored LogEvent
	 */
	public LogEvent getModelElement() {
		return getLogEvent();
	}

	/**
	 *@deprecated Please now use setLogEvent(LogEvent le)
	 *@see setLogEvent(LogEvent le) This method sets the LogEvent for the
	 *      LogEventProvider to store
	 * 
	 * @param le
	 *            LogEvent The LogEvent to Store
	 */
	public void setModelElement(LogEvent le) {
		setLogEvent(le);
	}

	/**
	 * Retrieves the log event belonging to this ComboBoxLogEvent entry.
	 * 
	 * @return the stored LogEvent
	 */
	public LogEvent getLogEvent() {
		return e;
	}

	/**
	 * Sets the associated log event.
	 * 
	 * @param le
	 *            the log event to be associated to this ComboBoxLogEvent
	 */
	public void setLogEvent(LogEvent le) {
		e = le;
	}

	/**
	 * Overrides the equals method in order to specify when two
	 * ComboBoxLogEvents are equal to each other.
	 * 
	 * @param o
	 *            the ComboBoxLogEvent to be compared with
	 * @return <code>true</code> if the associated log events are equal,
	 *         <code>false</code> otherwise
	 */
	public boolean equals(Object o) {
		return o != null && o instanceof ComboBoxLogEvent ? e
				.equals(((ComboBoxLogEvent) o).e) : false;
	}

	/**
	 * Overrides the compareTo method in order to whether the given object is
	 * smaller or greater than this one. This is used in order to sort the
	 * ComboBoxLogEvent entries in the combo box.
	 * 
	 * @param object
	 *            the ComboBoxLogEvent to be compared with
	 * @return -1 if given object is considered smaller, 1 if given object is
	 *         considered greater than this one
	 */
	public int compareTo(Object object) {
		ComboBoxLogEvent ce = (ComboBoxLogEvent) object;

		if (e.getModelElementName().compareTo(
				ce.getLogEvent().getModelElementName()) < 0) {
			return -1;
		} else if (e.getModelElementName().compareTo(
				ce.getLogEvent().getModelElementName()) > 0) {
			return 1;
		} else {
			return e.getEventType().compareTo(ce.getLogEvent().getEventType());
		}
	}
}
