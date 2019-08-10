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

package org.processmining.analysis.epcmerge;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.LogEventProvider;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCObject;

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
 * @author bvdonge, fgottschalk
 */
public class ComboBoxEPCObject implements Comparable {

	public final static String NONE = " None";

	private EPCObject o;
	private boolean none = false;

	/**
	 * Creates a ComboboxLogEvent for the given log event.
	 * 
	 * @param e
	 *            the given log event
	 */
	public ComboBoxEPCObject(EPCObject o) {
		this.o = o;
	}

	public ComboBoxEPCObject() {
		none = true;
		this.o = new EPCConnector(0, new ConfigurableEPC());
	}

	/**
	 * Creates the string to be displayed in the corresponding combo box entry.
	 */
	public String toString() {

		return (none ? NONE : o.getIdentifier());
	}

	/**
	 * Retrieves the log event belonging to this ComboBoxLogEvent entry.
	 * 
	 * @return the stored LogEvent
	 */
	public EPCObject getEPCObject() {
		if (none) {
			return null;
		} else {
			return o;
		}
	}

	/**
	 * Sets the associated log event.
	 * 
	 * @param le
	 *            the log event to be associated to this ComboBoxLogEvent
	 */
	public void setEPCObject(EPCObject le) {
		o = le;
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
	public boolean equals(Object obj) {
		if (obj instanceof ComboBoxEPCObject) {
			return (!none && obj != null ? o
					.equals(((ComboBoxEPCObject) obj).o) : false);
		} else {
			return (!none && obj != null && obj instanceof EPCObject ? o
					.equals((EPCObject) obj) : false);

		}
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
		ComboBoxEPCObject ce = (ComboBoxEPCObject) object;
		if (none) {
			return -1;
		} else if (o.getIdentifier().compareTo(
				ce.getEPCObject().getIdentifier()) < 0) {
			return -1;
		} else if (o.getIdentifier().compareTo(
				ce.getEPCObject().getIdentifier()) > 0) {
			return 1;
		} else {
			if (EPCFunction.class.isAssignableFrom(o.getClass())
					&& EPCFunction.class.isAssignableFrom(ce.getEPCObject()
							.getClass())) {
				return 0;
			} else if (EPCEvent.class.isAssignableFrom(o.getClass())
					&& EPCEvent.class.isAssignableFrom(ce.getEPCObject()
							.getClass())) {
				return 0;
			} else if (EPCConnector.class.isAssignableFrom(o.getClass())
					&& EPCConnector.class.isAssignableFrom(ce.getEPCObject()
							.getClass())
					&& o.getType() == ce.getEPCObject().getType()) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}
