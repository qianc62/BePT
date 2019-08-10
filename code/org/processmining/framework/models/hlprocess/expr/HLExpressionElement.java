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

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * An expression element is associated to a node in the expression tree.
 * 
 * @see HLExpressionOperator
 * @see HLExpressionOperand
 */
public abstract class HLExpressionElement implements Cloneable {

	/**
	 * The node in the expression tree that represents this expression element.
	 */
	protected DefaultMutableTreeNode node;

	/**
	 * The minimum number of input elements required for this expression
	 * element.
	 * <p>
	 * Subclasses are expected to initialize this value if it is meaningful and
	 * to indicate via {@link #hasMinNumberInputs()} whether it is meaningful or
	 * not.
	 */
	protected int minNumberInputs = 0;
	/**
	 * The maximum number of input elements required for this expression
	 * element.
	 * <p>
	 * Subclasses are expected to initialize this value if it is meaningful and
	 * to indicate via {@link #hasMaxNumberInputs()} whether it is meaningful or
	 * not.
	 */
	protected int maxNumberInputs = 0;

	/**
	 * Default constructor.
	 */
	protected HLExpressionElement() {
		node = new DefaultMutableTreeNode(this);
	}

	/**
	 * Returns the root node of this expression tree. The expression element is
	 * associated to this node and cann be retrieved via
	 * {@link DefaultMutableTreeNode#getUserObject()}.
	 * <p>
	 * In contrast to the expression element itself, the node can be asked for
	 * its child nodes.
	 * 
	 * @return the root node of this expression tree
	 */
	public DefaultMutableTreeNode getExpressionNode() {
		return node;
	}

	/**
	 * Replaces the associated tree node in the expression by the given one.
	 * 
	 * @param newNode
	 *            the new node in the expression tree for this element
	 */
	public void setExpressionNode(DefaultMutableTreeNode newNode) {
		node = newNode;
	}

	/**
	 * Adds a new expression element as a child to the root node.
	 * <p>
	 * Transparently creates a new tree node representing the expression
	 * element.
	 * 
	 * @see #addSubExpression(DefaultMutableTreeNode)
	 * @param item
	 *            the node to be added as a child
	 * @return the newly created tree node
	 */
	public DefaultMutableTreeNode addSubElement(HLExpressionElement item) {
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(item);
		item.setExpressionNode(newNode);
		node.add(newNode);
		return newNode;
	}

	/**
	 * Adds the given expression node to the expression tree.
	 * <p>
	 * Preserves existing sub tree structure but assumes that expression
	 * elements are already properly associated to all expression nodes.
	 * 
	 * @see #addSubElement(HLExpressionElement)
	 * @param subNode
	 *            the sub expression tree node to be added
	 */
	public void addSubExpression(DefaultMutableTreeNode subNode) {
		node.add(subNode);
	}

	/**
	 * Retrieves the minimum number of input elements required for this
	 * expression element.
	 * <p>
	 * Use {@link #hasMinNumberInputs()} to find out whether a minimum number
	 * has been specified for this expression element.
	 */
	public int getMinNumberInputs() {
		return minNumberInputs;
	}

	/**
	 * Retrieves the maximum number of input elements required for this
	 * expression element.
	 * <p>
	 * Use {@link #hasMaxNumberInputs()} to find out whether a minimum number
	 * has been specified for this expression element.
	 */
	public int getMaxNumberInputs() {
		return maxNumberInputs;
	}

	/**
	 * Checks recursively whether the minumum and maximum number input
	 * requirements are satisfied (for this expression element and for each of
	 * its children).
	 * <p>
	 * Note that no type checking is performed and, therefore, the expression
	 * might be still not valid.
	 * 
	 * @return whether this sub expression is valid
	 */
	public boolean isValid() {
		// check this node first
		if (hasMinNumberInputs() && node.getChildCount() < minNumberInputs) {
			return false;
		} else if (hasMaxNumberInputs()
				&& node.getChildCount() > maxNumberInputs) {
			return false;
		}
		// now check child nodes
		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node
					.getChildAt(i);
			HLExpressionElement childExpr = (HLExpressionElement) child
					.getUserObject();
			if (childExpr.isValid() == false) {
				return false;
			}
		}
		// seems ok
		return true;
	}

	/**
	 * <p>
	 * Evaluates the expression element based on its input parameters.
	 * <p>
	 * Assumes that the expression is valid and the operator can be applied to
	 * its input operands. As a result, the sub tree connected to this operator
	 * can be replaced by the resulting operand as a leaf node.
	 * 
	 * @see #isValid(javax.swing.tree.TreeNode)
	 * 
	 * @return the evaluation result of this expression element
	 */
	public abstract HLExpressionOperand evaluate();

	/**
	 * Subclasses need to indicate whether a minimum number of required input
	 * elements has been specified or not.
	 * 
	 * @return <code>true</code> if a minimum number of input elements is
	 *         required, <code>false</code> otherwise
	 */
	public abstract boolean hasMinNumberInputs();

	/**
	 * Subclasses need to indicate whether a maximum number of required input
	 * elements has been specified or not.
	 * 
	 * @return <code>true</code> if a maximum number of input elements is
	 *         required, <code>false</code> otherwise
	 */
	public abstract boolean hasMaxNumberInputs();

	/**
	 * Subclasses need to provide a string representation. Operators should give
	 * the name (or symbol) or the operator while operands should provide a
	 * suitable value representation.
	 */
	public abstract String toString();

	/**
	 * Generates a string representation of the subtree starting from this
	 * element.
	 * 
	 * @return the string representation of the whole subtree
	 */
	public abstract String evaluateToString();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		HLExpressionElement o;
		try {
			o = (HLExpressionElement) super.clone();
			// preserve the double-linked structure between tree nodes
			// and expression objects
			o.node = (DefaultMutableTreeNode) node.clone();
			o.node.setUserObject(this);
			return o;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
