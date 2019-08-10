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

/**
 * A boolean data attribute only allows for the values <code>true</code> and
 * <code>false</code>.
 * <p>
 * The likelihood, or frequency, for each of these values is captured by the
 * distribution of possible values.
 * 
 * @see HLAttribute
 */
public class HLBooleanAttribute extends HLAttribute {

	/**
	 * Default constructor.
	 * 
	 * @param aName
	 *            the name of this attribute
	 * @param aProc
	 *            the high level process it belongs to
	 */
	public HLBooleanAttribute(String aName, HLProcess aProc) {
		super(aName, aProc);
		initialValue = new HLBooleanValue(false);
		possibleValues = new HLBooleanDistribution();
	}

	/**
	 * Constructor to create a fully specified boolean data attribute.
	 * 
	 * @param name
	 *            the name of the data attribute
	 * @param initVal
	 *            the initial value for this attribute
	 * @param aProc
	 *            the process this attribute belongs to
	 */
	public HLBooleanAttribute(String name, boolean initVal, HLProcess aProc) {
		this(name, aProc);
		initialValue = new HLBooleanValue(initVal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.HLAttribute#getPossibleValues
	 * ()
	 */
	public HLBooleanDistribution getPossibleValues() {
		return (HLBooleanDistribution) possibleValues;
	}

	/**
	 * Directly retrieves the boolean initial value for this data attribute.
	 * 
	 * @see #getInitialValue()
	 * @return the boolean default value
	 */
	public boolean getInitialValueBoolean() {
		return ((HLBooleanValue) initialValue).getValue();
	}

	/**
	 * Specifies directly a new boolean initial for this data attribute.
	 * 
	 * @see #setInitialValue(HLAttributeValue)
	 * @param aValue
	 *            the new initial value for the boolean data attribute
	 */
	public void setInitialValue(boolean aValue) {
		initialValue = new HLBooleanValue(aValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.HLAttribute#isValid(org.
	 * processmining.framework.models.hlprocess.att.HLAttributeValue)
	 */
	public boolean isValid(HLAttributeValue val) {
		if (val instanceof HLBooleanValue
				|| val instanceof HLBooleanDistribution) {
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
		return AttributeType.Boolean;
	}

}
