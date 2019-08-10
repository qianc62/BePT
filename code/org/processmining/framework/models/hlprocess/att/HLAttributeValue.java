/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/
package org.processmining.framework.models.hlprocess.att;

/**
 * Interface for typed attribute values.
 * <p>
 * Attribute values need to be cloneable and able to compare for equality with
 * other values.
 */
public interface HLAttributeValue extends Cloneable {

	/**
	 * An attribute value needs to be able to compare for equality with other
	 * values.
	 */
	public boolean equals(Object o);

	/**
	 * Needs to be implemented such that yields equal hashCode for equal
	 * attribute values.
	 */
	public int hashCode();

	/**
	 * An attribute value needs to be cloneable.
	 */
	public Object clone();

	/**
	 * A string representation must be made available for each value.
	 * 
	 * @return the textual representation
	 */
	public String toString();
}
