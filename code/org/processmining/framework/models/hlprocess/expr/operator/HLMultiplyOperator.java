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

import org.processmining.framework.models.hlprocess.att.HLNumericValue;
import org.processmining.framework.models.hlprocess.expr.HLExpressionElement;
import org.processmining.framework.models.hlprocess.expr.HLExpressionOperand;
import org.processmining.framework.models.hlprocess.expr.HLExpressionOperator;
import org.processmining.framework.models.hlprocess.expr.operand.HLValueOperand;

/**
 * Numeric * operator.
 * <p>
 * Requires at least two numeric input operands to be evaluated.
 */
public class HLMultiplyOperator extends HLExpressionOperator {

	/**
	 * Default constructor.
	 */
	public HLMultiplyOperator() {
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
		HLExpressionOperand first = operands.remove(0);
		int result = 0;
		if (first.getValue() instanceof HLNumericValue) {
			result = ((HLNumericValue) first.getValue()).getValue();
		} else {
			// TODO: better raise an exception and write error output
			return null;
		}
		// now multiply remaining
		for (HLExpressionOperand op : operands) {
			if (op.getValue() instanceof HLNumericValue) {
				result = result * ((HLNumericValue) op.getValue()).getValue();
			} else {
				// TODO: better raise an exception and write error output
				return null;
			}
		}
		return new HLValueOperand(new HLNumericValue(result));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLExpressionElement
	 * #toString()
	 */
	public String toString() {
		return "*";
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
				result = result + "(" + childExpr.evaluateToString() + ")";
			} else {
				result = result + " * " + "(" + childExpr.evaluateToString()
						+ ")";
			}
		}
		return result;
	}

}
