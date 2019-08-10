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
package org.processmining.framework.models.hlprocess.expr.operand;

import org.processmining.framework.models.hlprocess.att.HLAttributeValue;
import org.processmining.framework.models.hlprocess.expr.HLExpressionOperand;

/**
 * A value operand representing fixed value rather than a placeholder.
 * <p>
 * For example, in the expression <code>Amount > 500</code> "Amount" would be a
 * placeholder referring to some attribute in the process (i.e., correspond to
 * an attribute operand) while "500" would be a value operand and ">" would be
 * the operator linking the two.
 * 
 * @see HLAttributeOperand
 */
public class HLValueOperand extends HLExpressionOperand {

	/**
	 * The actual value held by this operand.
	 */
	protected HLAttributeValue value;

	/**
	 * Creates a new value operand based on the given value.
	 * 
	 * @param val
	 *            the value to be represented
	 */
	public HLValueOperand(HLAttributeValue val) {
		value = val;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLExpressionOperand
	 * #getValue()
	 */
	public HLAttributeValue getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLExpressionElement
	 * #toString()
	 */
	public String toString() {
		return "" + value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLExpressionElement
	 * #evaluateToString()
	 */
	public String evaluateToString() {
		return getValue().toString();
	}
}
