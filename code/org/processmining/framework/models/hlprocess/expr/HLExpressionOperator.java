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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * An operator represents a generic function that can be used to evaluate a
 * given number of input parameters (operands).
 * <p>
 * To be valid, it needs to have at least as many inputs as specified (and
 * potentially a maximum number of inputs, if specified). An operator is not
 * allowed to be a leaf node in the expression tree.
 */
public abstract class HLExpressionOperator extends HLExpressionElement {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLDataExpressionElement
	 * #hasMinNumberInputs()
	 */
	public boolean hasMinNumberInputs() {
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
		ArrayList<HLExpressionOperand> operands = new ArrayList<HLExpressionOperand>();
		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node
					.getChildAt(i);
			HLExpressionElement exprEl = (HLExpressionElement) childNode
					.getUserObject();
			// evaluate potential sub trees
			HLExpressionOperand operand = exprEl.evaluate();
			operands.add(operand);
		}
		// actually evaluate the operands
		return evaluateOperands(operands);
	}

	/**
	 * Actual function to be implemented by each operator.
	 * 
	 * @param operands
	 *            the input operands to be evaluated
	 * @return the output operand (potentially to be further evaluated)
	 */
	protected abstract HLExpressionOperand evaluateOperands(
			List<HLExpressionOperand> operands);

}
