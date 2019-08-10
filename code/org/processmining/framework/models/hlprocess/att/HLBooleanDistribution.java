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
 * A boolean probability distribution.
 * <p>
 * Per default assigns equal probability to both <code>true</code> and
 * <code>false</code> values.
 * 
 * @see #addFrequency(boolean)
 * @see #getFrequency(boolean)
 */
public class HLBooleanDistribution implements HLAttributeValue {

	/**
	 * The frequency or relative likelihood of the the value <code>true</code>.
	 */
	protected int freqTrueValue = 0;
	/**
	 * The frequency or relative likelihood of the the value <code>false</code>.
	 */
	protected int freqFalseValue = 0;

	/**
	 * Adds more weight to the given boolean value.
	 * <p>
	 * As a consequence it becomes more likely relative to the other value.
	 * 
	 * @param aValue
	 *            the value to be increased in likelyhood
	 */
	public void addFrequency(boolean aValue) {
		if (aValue == true) {
			freqTrueValue = freqTrueValue + 1;
		} else {
			freqFalseValue = freqFalseValue + 1;
		}
	}

	/**
	 * Retrieves the frequency of the given boolean value.
	 * <p>
	 * Note that for a default boolean distribution the relative frequency 1
	 * will be returned for both <code>true</code> and <code>false</code>
	 * values.
	 * <p>
	 * The likelihood of a value can be increased by calling the method
	 * {@link #addFrequency(boolean)}.
	 * 
	 * @param aValue
	 *            the value for which the relative frequency is to be obtained
	 * @return the relative frequency for the given value (needs to be evaluated
	 *         with respect to the frequency of the counter value to be
	 *         interpreted as a probability)
	 */
	public int getFrequency(boolean aValue) {
		if (freqTrueValue == 0 && freqFalseValue == 0) {
			// if never any frequency was assigned assume
			// a 50 / 50 distribution
			return 1;
			// otherwise return the actual frequencies
		} else if (aValue == true) {
			return freqTrueValue;
		} else {
			return freqFalseValue;
		}
	}

	/**
	 * Sets the relative frequency of the value <code>true</code> to the given
	 * value
	 * 
	 * @param aFreq
	 *            the new frequency for 'true'
	 */
	public void setTrueFrequency(int aFreq) {
		freqTrueValue = aFreq;
	}

	/**
	 * Sets the relative frequency of the value <code>false</code> to the given
	 * value
	 * 
	 * @param aFreq
	 *            the new frequency for 'false'
	 */
	public void setFalseFrequency(int aFreq) {
		freqFalseValue = aFreq;
	}

	/**
	 * Calculate the probability that the boolean value is <code>true</code>.
	 * This is based on the relative frequency measured for <code>true</code>
	 * and <code>false</code> values.
	 * 
	 * @return the probability of this distribution for the value being
	 *         <code>true</code>
	 */
	public double getProbability() {
		double valTrue = getFrequency(true);
		double valFalse = getFrequency(false);
		return valTrue / (valTrue + valFalse);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		HLBooleanDistribution o = null;
		try {
			o = (HLBooleanDistribution) super.clone();
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
		} else if ((o instanceof HLBooleanDistribution) == false) {
			return false;
		} else {
			return (freqTrueValue == ((HLBooleanDistribution) o).freqTrueValue && freqFalseValue == ((HLBooleanDistribution) o).freqFalseValue);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + freqTrueValue;
		hash = 31 * hash + freqFalseValue;
		return hash;
	}
}
