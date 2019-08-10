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
 * Represents a nominal attribute value.
 * 
 * @see #getValue()
 * @see HLNominalAttribute
 */
public class HLNominalValue implements HLAttributeValue {

	/**
	 * The actual nominal value held by this attribute value.
	 */
	protected String value;

	/**
	 * Creates a new nominal value from the given value.
	 * 
	 * @param aValue
	 *            the value for this nominal attribute value
	 */
	public HLNominalValue(String aValue) {
		value = aValue;
	}

	/**
	 * Directly retrieves the associated String value for this nominal
	 * attribute.
	 * 
	 * @return the nominal value
	 */
	public String getValue() {
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
		} else if ((o instanceof HLNominalValue) == false) {
			return false;
		} else {
			return (getValue().equals(((HLNominalValue) o).getValue()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + value.hashCode();
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
		return value;
	}
}
