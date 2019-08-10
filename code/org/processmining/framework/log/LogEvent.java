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

import java.io.Serializable;

/**
 * A log event (ie unique combination of a name (ie TASK_A) and a type (ie
 * complete)).
 * 
 * @author Peter van den Brand
 */

public class LogEvent implements Comparable, Serializable, Cloneable {

	/**
	 * serialization version id; not used, implement properly if you want to
	 * use!
	 */
	private static final long serialVersionUID = 1334376593749624757L;

	protected String modelElementName = null;
	protected String modelElementType = null;
	protected int occurenceCount = 0;

	public LogEvent(String name, String type) {
		this(name, type, 1);
	}

	public LogEvent(String name, String type, int numSimilarTraces) {
		modelElementName = name;
		modelElementType = type;
		occurenceCount = numSimilarTraces;
	}

	public String getModelElementName() {
		return modelElementName;
	}

	public String getEventType() {
		return modelElementType;
	}

	public void copy(LogEvent e) {
		modelElementName = e.modelElementName;
		modelElementType = e.modelElementType;
		occurenceCount = e.occurenceCount;
	}

	/**
	 * Returns the number of times this event occurs in the workflow log.
	 * 
	 * @return the number of times this event occurs in the workflow log
	 */
	public int getOccurrenceCount() {
		return occurenceCount;
	}

	public void incOccurrenceCount() {
		occurenceCount++;
	}

	public void incOccurrenceCount(int numSimilarInstances) {
		occurenceCount += numSimilarInstances;
	}

	public boolean equals(String name, String type) {
		return ((modelElementName.equals(name)) && (modelElementType
				.equals(type)));
	}

	/**
	 * Overrides the equals method in order to specify when two log events are
	 * considered as equal.
	 * 
	 * @return <code>true</code> if the name and the type are equal,
	 *         <code>false</code> otherwise
	 */
	public boolean equals(Object o) {
		if (o == null || !(o instanceof LogEvent)) {
			return false;
		}
		LogEvent lme = (LogEvent) o;
		return modelElementName.equals(lme.modelElementName)
				&& modelElementType.equals(lme.modelElementType);
	}

	/**
	 * Overridden to produce the same hash code for equal objects.
	 * 
	 * @return the hash code calculated
	 */
	public int hashCode() {
		// simple recipe for generating hashCode given by
		// Effective Java (Addison-Wesley, 2001)
		int result = 17;
		result = 37 * result + modelElementName.hashCode();
		result = 37 * result + modelElementType.hashCode();
		return result;
	}

	/**
	 * Overridden to produce a string representation of this log event.
	 * 
	 * @return a string composed out of the name of the log event and its type
	 *         in the following form: "name (type)"
	 */
	public String toString() {
		return modelElementName + " (" + modelElementType + ")";
	}

	/**
	 * Overridden to specify when a log event is considered smaller or greate
	 * than another.
	 * 
	 * @param o
	 *            the object to be compared with
	 * @return the value yielded when comparing the string representation of
	 *         this log event with the one of the given object
	 * @see toString()
	 */
	public int compareTo(Object o) {
		LogEvent toCompare = (LogEvent) o;
		return this.toString().compareTo(toCompare.toString());
	}

	/**
	 * Make a deep copy of the object. Note that this method needs to be
	 * extended as soon as there are attributes added to the class which are not
	 * primitive or immutable.
	 * 
	 * @return the cloned object
	 */
	public Object clone() {
		LogEvent o = null;
		try {
			o = (LogEvent) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		// clone referenced objects to realize deep copy
		// (but there are only simple data types)
		return o;
	}
}
