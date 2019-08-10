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
package org.processmining.framework.models.hlprocess.expr.operator;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.processmining.framework.models.hlprocess.att.HLAttributeValue;
import org.processmining.framework.models.hlprocess.att.HLBooleanValue;
import org.processmining.framework.models.hlprocess.att.HLNumericValue;
import org.processmining.framework.models.hlprocess.expr.HLExpressionElement;
import org.processmining.framework.models.hlprocess.expr.HLExpressionOperand;
import org.processmining.framework.models.hlprocess.expr.HLExpressionOperator;
import org.processmining.framework.models.hlprocess.expr.operand.HLValueOperand;

/**
 * Logical > operator.
 * <p>
 * Requires at exactly two numeric input operands to be evaluated.
 */
public class HLGreaterOperator extends HLExpressionOperator {

	/**
	 * Default constructor.
	 */
	public HLGreaterOperator() {
		minNumberInputs = 2;
		maxNumberInputs = 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLExpressionOperator
	 * #evaluateOperands(java.util.List)
	 */
	protected HLExpressionOperand evaluateOperands(
			List<HLExpressionOperand> operands) {
		HLAttributeValue firstValue = operands.get(0).getValue();
		HLAttributeValue secondValue = operands.get(1).getValue();
		if (firstValue instanceof HLNumericValue
				&& secondValue instanceof HLNumericValue) {
			boolean result = ((HLNumericValue) firstValue).getValue() > ((HLNumericValue) secondValue)
					.getValue();
			return new HLValueOperand(new HLBooleanValue(result));
		} else {
			// TODO: better raise an exception and write error output
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLExpressionElement
	 * #toString()
	 */
	public String toString() {
		return ">";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLDataExpressionElement
	 * #hasMaxNumberInputs()
	 */
	public boolean hasMaxNumberInputs() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLExpressionElement
	 * #evaluateToString()
	 */
	public String evaluateToString() {
		HLExpressionElement first = (HLExpressionElement) ((DefaultMutableTreeNode) getExpressionNode()
				.getChildAt(0)).getUserObject();
		HLExpressionElement second = (HLExpressionElement) ((DefaultMutableTreeNode) getExpressionNode()
				.getChildAt(1)).getUserObject();
		return first.evaluateToString() + " > " + second.evaluateToString();
	}

}
