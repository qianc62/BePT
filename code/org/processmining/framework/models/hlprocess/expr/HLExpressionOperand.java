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
package org.processmining.framework.models.hlprocess.expr;

import org.processmining.framework.models.hlprocess.att.HLAttributeValue;

/**
 * Expression element that corresponds to an operand in the expression.
 * <p>
 * To be valid, it should be a leaf node in the expression tree.
 */
public abstract class HLExpressionOperand extends HLExpressionElement {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLDataExpressionElement
	 * #hasMinNumberInputs()
	 */
	public boolean hasMinNumberInputs() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLDataExpressionElement
	 * #hasMaxNumberInputs()
	 */
	public boolean hasMaxNumberInputs() {
		// default maximum number is 0
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLExpressionElement
	 * #evaluate()
	 */
	public HLExpressionOperand evaluate() {
		return this;
	}

	/**
	 * Retrieves the value associated to this operand.
	 * 
	 * @return the value of this operand
	 */
	public abstract HLAttributeValue getValue();

}
