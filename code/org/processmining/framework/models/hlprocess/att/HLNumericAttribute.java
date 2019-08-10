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

import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLProcess;
import org.processmining.framework.models.hlprocess.HLTypes.AttributeType;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLDistribution;

/**
 * <p>
 * A numeric attribute allows for integer values within a certain value range.
 * <p>
 * The possible values can be specified according to a probabilistic
 * distribution.
 * 
 * @see HLAttribute
 */
public class HLNumericAttribute extends HLAttribute {

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            the name of this attribute
	 * @param aProc
	 *            the high level process it belongs to
	 */
	public HLNumericAttribute(String name, HLProcess aProc) {
		super(name, aProc);
		initialValue = new HLNumericValue(0);
		possibleValues = new HLNumericDistribution(new HLGeneralDistribution());
	}

	/**
	 * Constructor to create a fully-specified numeric data attribute.
	 * 
	 * @param name
	 *            the name of the data attribute
	 * @param dist
	 *            the distribution of the possible values
	 * @param initVal
	 *            the initial value
	 * @param aProc
	 *            the process this attribute belongs to
	 */
	public HLNumericAttribute(String name, HLDistribution dist, int initVal,
			HLProcess aProc) {
		this(name, aProc);
		initialValue = new HLNumericValue(initVal);
		possibleValues = new HLNumericDistribution(dist);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.HLAttribute#getPossibleValues
	 * ()
	 */
	public HLNumericDistribution getPossibleValues() {
		return (HLNumericDistribution) possibleValues;
	}

	/**
	 * Directly retrieves the numeric initial value for this attribute.
	 * 
	 * @see #getInitialValue()
	 * @return the numeric default value
	 */
	public int getInitialValueNumeric() {
		return ((HLNumericValue) initialValue).getValue();
	}

	/**
	 * Directly specifies the numeric value new initial value for this
	 * attribute.
	 * 
	 * @param aValue
	 *            the new initial value for the numeric data attribute
	 */
	public void setInitialValue(int aValue) {
		initialValue = new HLNumericValue(aValue);
	}

	/**
	 * Retrieves the possible numeric values for the associated data attribute.
	 * 
	 * @return the distribution of possible numeric values for this data
	 *         attribute
	 */
	public HLDistribution getPossibleValuesNumeric() {
		return ((HLNumericDistribution) possibleValues).getValue();
	}

	/**
	 * Specifies the distribution of possible numeric values for the data
	 * attribute.
	 * 
	 * @param dist
	 *            the new possible numeric values for the data attribute
	 */
	public void setPossibleValues(HLDistribution dist) {
		possibleValues = new HLNumericDistribution(dist);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.HLAttribute#isValid(org.
	 * processmining.framework.models.hlprocess.att.HLAttributeValue)
	 */
	public boolean isValid(HLAttributeValue val) {
		// TODO: also include value range check
		if (val instanceof HLNumericValue
				|| val instanceof HLNumericDistribution) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.models.hlprocess.HLAttribute#getType()
	 */
	public AttributeType getType() {
		return AttributeType.Numeric;
	}

}
