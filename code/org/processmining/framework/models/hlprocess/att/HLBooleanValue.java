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
 * Represents a boolean attribute value.
 * 
 * @see #getValue()
 * @see HLBooleanAttribute
 */
public class HLBooleanValue implements HLAttributeValue {

	/**
	 * The actual boolean value held by this attribute value.
	 */
	protected boolean value;

	/**
	 * Constructor initializing the boolean value held by this attribute value.
	 * 
	 * @param aValue
	 *            the new value
	 */
	public HLBooleanValue(boolean aValue) {
		value = aValue;
	}

	/**
	 * Directly retrieves the boolean value from this boolean attribute value.
	 * 
	 * @return the boolean value held by this attribute value
	 */
	public boolean getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if ((o instanceof HLBooleanValue) == false) {
			return false;
		} else {
			return (getValue() == ((HLBooleanValue) o).getValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int hash = 7;
		int var_code = value ? 1 : 0;
		hash = 31 * hash + var_code;
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.att.HLAttributeValue#clone()
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (value == true) {
			return "true";
		} else {
			return "false";
		}
	}
}
