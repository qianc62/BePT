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

import org.processmining.framework.models.hlprocess.distribution.HLDistribution;

/**
 * A numeric probability distribution.
 * 
 * @see #getValue()
 */
public class HLNumericDistribution implements HLAttributeValue {

	/**
	 * The possible values as a probability distribution.
	 */
	protected HLDistribution possibleValues;

	/**
	 * Default constructor.
	 * 
	 * @param dist
	 *            the given probability distribution
	 */
	public HLNumericDistribution(HLDistribution dist) {
		possibleValues = dist;
	}

	/**
	 * Returns the probability distribution.
	 * 
	 * @return the distribution if was specified, <code>null</cod> otherwise
	 */
	public HLDistribution getValue() {
		return possibleValues;
	}

	/**
	 * Provides a new distribution.
	 * 
	 * @param dist
	 *            the new distribution
	 */
	public void setHighLevelDistribution(HLDistribution dist) {
		possibleValues = dist;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		HLNumericDistribution o = null;
		try {
			o = (HLNumericDistribution) super.clone();
			o.possibleValues = (HLDistribution) possibleValues.clone();
			return o;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if ((o instanceof HLNumericDistribution) == false) {
			return false;
		} else {
			return (possibleValues
					.equals(((HLNumericDistribution) o).possibleValues));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + possibleValues.hashCode();
		return hash;
	}

}
