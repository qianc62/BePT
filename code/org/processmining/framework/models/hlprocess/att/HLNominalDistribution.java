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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A nominal values probability distribution.
 * <p>
 * Per default assigns equal probability to all nominal values.
 * 
 * @see #addPossibleValueNominal(String)
 * @see #getFrequencyPossibleValueNominal(String)
 */
public class HLNominalDistribution implements HLAttributeValue {

	/**
	 * The list of possible values and their frequencies.
	 */
	protected HashMap<String, Integer> possibleValues = new HashMap<String, Integer>();

	/**
	 * Retrieves the possible nominal values for the associated data attribute.
	 * 
	 * @return the nominal possible values for this data attribute. If no
	 *         possible values are defined an empty list is returned
	 */
	public List<String> getValues() {
		ArrayList<String> result = new ArrayList<String>();
		for (String val : possibleValues.keySet()) {
			result.add(val);
		}
		return result;
	}

	/**
	 * Retrieves the map of possible values and their corresponding frequencies.
	 * 
	 * @return the map of values and frequencies
	 */
	public HashMap<String, Integer> getValuesAndFrequencies() {
		return possibleValues;
	}

	/**
	 * Specifies the possible values for the data attribute based on the given
	 * list. <br>
	 * If there are duplicate values in the list, their frequency will be
	 * captured accordingly.
	 * 
	 * @param values
	 *            the new possible values for the data attribute
	 */
	public void setPossibleValues(List<String> values) {
		possibleValues = new HashMap<String, Integer>();
		for (String current : values) {
			if (possibleValues.containsKey(current) == false) {
				possibleValues.put(current, new Integer(1));
			} else {
				possibleValues.put(current, new Integer(possibleValues
						.get(current) + 1));
			}
		}
	}

	/**
	 * Directly sets the possible values for this data attribute together with
	 * their relative frequencies.
	 * 
	 * @param values
	 *            the new nominal distribution of values
	 */
	public void setPossibleValues(HashMap<String, Integer> values) {
		possibleValues = values;
	}

	/**
	 * Adds the given value to the set of possible values for this attribute. <br>
	 * Will increase the frequency of the value if already there
	 * 
	 * @param aValue
	 *            the nominal value to be added
	 */
	public void addPossibleValueNominal(String aValue) {
		if (possibleValues.containsKey(aValue) == false) {
			possibleValues.put(aValue, new Integer(1));
		} else {
			possibleValues.put(aValue, new Integer(
					possibleValues.get(aValue) + 1));
		}
	}

	/**
	 * Retrieves the frequency value for the given value. <br>
	 * Note that in relation to the frequencies of the other nominal values, a
	 * percentage, or likelihood, for the given value can be represented.
	 * 
	 * @param value
	 *            the nominal value for which the frequency is requested
	 * @return the frequency that was recorded for this nominal value. 0 if
	 *         given value is not among the possible values in this distribution
	 */
	public int getFrequencyPossibleValueNominal(String value) {
		Integer result = possibleValues.get(value);
		if (result != null) {
			return result.intValue();
		} else {
			return 0;
		}
	}

	/**
	 * Calculates the sum of all frequencies observed for all possible values in
	 * this distribution.
	 * 
	 * @return the sum of all frequency values
	 */
	public int getSumOfAllFrequencies() {
		int result = 0;
		for (String val : possibleValues.keySet()) {
			Integer freqVal = possibleValues.get(val);
			result = result + freqVal.intValue();
		}
		return result;
	}

	/**
	 * Specifies the frequency for the given value. <br>
	 * Note that in relation to the frequencies of the other nominal values, a
	 * percentage, or likelihood, for the given value can be represented. <br>
	 * If the given nominal value is not among the possible nominal values, this
	 * method has no effect.
	 * 
	 * @param value
	 *            the nominal value for which the frequency should be recorded
	 * @param freq
	 *            the frequency for the given nominal value
	 */
	public void setFrequencyPossibleValueNominal(String value, int freq) {
		if (possibleValues.containsKey(value)) {
			possibleValues.put(value, freq);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		HLNominalDistribution o = null;
		try {
			o = (HLNominalDistribution) super.clone();
			o.possibleValues = (HashMap<String, Integer>) possibleValues
					.clone();
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
		} else if ((o instanceof HLNominalDistribution) == false) {
			return false;
		} else {
			return (possibleValues == ((HLNominalDistribution) o).possibleValues);
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
