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

import org.processmining.framework.models.hlprocess.att.HLBooleanValue;
import org.processmining.framework.models.hlprocess.expr.HLExpressionElement;
import org.processmining.framework.models.hlprocess.expr.HLExpressionOperand;
import org.processmining.framework.models.hlprocess.expr.HLExpressionOperator;
import org.processmining.framework.models.hlprocess.expr.operand.HLValueOperand;

/**
 * Logical NOT operator.
 * <p>
 * Requires exactly one boolean input operand to be evaluated.
 */
public class HLNotOperator extends HLExpressionOperator {

	/**
	 * Default constructor.
	 */
	public HLNotOperator() {
		minNumberInputs = 1;
		maxNumberInputs = 1;
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
		for (HLExpressionOperand op : operands) {
			if (op.getValue() instanceof HLBooleanValue) {
				result = !((HLBooleanValue) op.getValue()).getValue();
			} else {
				// TODO: better raise an exception and write error output
				return null;
			}
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
		return "NOT";
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
		String result = "";
		if (getExpressionNode().getChildCount() == 0) {
			return result;
		} else {
			HLExpressionElement first = (HLExpressionElement) ((DefaultMutableTreeNode) getExpressionNode()
					.getChildAt(0)).getUserObject();
			return "!(" + first.evaluateToString() + ")";
		}
	}

}