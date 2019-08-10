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
import org.processmining.framework.models.hlprocess.expr.HLExpressionElement;
import org.processmining.framework.models.hlprocess.expr.HLExpressionOperand;
import org.processmining.framework.models.hlprocess.expr.HLExpressionOperator;
import org.processmining.framework.models.hlprocess.expr.operand.HLValueOperand;

/**
 * Logical = operator.
 * <p>
 * Requires at least two input operands to be evaluated.
 */
public class HLEqualOperator extends HLExpressionOperator {

	/**
	 * Default constructor.
	 */
	public HLEqualOperator() {
		minNumberInputs = 2;
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
		boolean result = false;
		// get first value for first comparison
		HLAttributeValue lastValue = operands.get(0).getValue();
		for (HLExpressionOperand op : operands) {
			result = lastValue.equals(op.getValue());
			// keep value for comparison with next value
			lastValue = op.getValue();
		}
		return new HLValueOperand(new HLBooleanValue(result));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLExpressionElement
	 * #toString()
	 */
	public String toString() {
		return "=";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLDataExpressionElement
	 * #hasMaxNumberInputs()
	 */
	public boolean hasMaxNumberInputs() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLExpressionElement
	 * #evaluateToString()
	 */
	public String evaluateToString() {
		String result = "";
		for (int i = 0; i < getExpressionNode().getChildCount(); i++) {
			HLExpressionElement childExpr = (HLExpressionElement) ((DefaultMutableTreeNode) getExpressionNode()
					.getChildAt(i)).getUserObject();
			if (result == "") {
				result = result + childExpr.evaluateToString();
			} else {
				result = result + " == " + childExpr.evaluateToString();
			}
		}
		return result;
	}

}