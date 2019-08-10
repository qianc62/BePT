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

import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.expr.operand.HLAttributeOperand;

/**
 * An expression that, e.g., ranges over data attributes and compares them to
 * expected values. These terms can be connected by elementary logic.
 * 
 * @see HLExpressionOperand
 * @see HLExpressionOperator
 */
public class HLDataExpression implements Cloneable, Comparable {

	/**
	 * The root node of the data expression.
	 */
	protected HLExpressionElement root;
	/**
	 * Ther can be an order rank associated to an expression, which indicates
	 * the evaluation order with respect to its "sister" expressions.
	 * <p>
	 * The default is "-1" and indicates that no explicit rank was set.
	 */
	protected int orderRank;
	/**
	 * Expression string that will be displayed in GUI if no explicit logical
	 * expression has been provided.
	 * <p>
	 * If the expression string equals "default", then the CPN export will
	 * generate a complementary expression with respect to the "sister"
	 * expressions.
	 */
	protected String expressionString = "";

	/**
	 * Default constructor. Creates an empty expression.
	 */
	public HLDataExpression() {
		this(null, -1);
	}

	/**
	 * Constructor assigning the given expression element to the root node of
	 * the expression tree.
	 * 
	 * @param element
	 *            the root expression element
	 */
	public HLDataExpression(HLExpressionElement element) {
		this(element, -1);
	}

	/**
	 * Creates a data expressions and sets a relative rank that needs to be
	 * interpreted against the ranks of expressions involved in the same choice.
	 * 
	 * @param element
	 *            the expression
	 * @param rank
	 *            the rank starting from 0 (reflects interpretation order in
	 *            case a group of expressions evaluate to true)
	 */
	public HLDataExpression(HLExpressionElement element, int rank) {
		root = element;
		orderRank = rank;
	}

	/**
	 * Returns the relative rank (with respect to other expressions).
	 * <p>
	 * Per default, no rank (i.e., -1) is provided. Otherwise ranks start with 0
	 * 
	 * @return the rank for this expression
	 */
	public int getOrderRank() {
		return orderRank;
	}

	/**
	 * Sets the provided expression string.
	 * 
	 * @param expression
	 *            the string holding an expression
	 */
	public void setExpression(String expression) {
		expressionString = expression;
	}

	/**
	 * Retrieves the expression string.
	 * <p>
	 * Note that is only filled if an expression string has been provided. Is
	 * not automatically correlated with the logical expression object.
	 * 
	 * @return the expression string
	 */
	public String getExpressionString() {
		return expressionString;
	}

	/**
	 * Assigns the given expression element to the root node of the expression
	 * tree.
	 * 
	 * @param element
	 *            the element to be assigned to the root
	 */
	public void setRootExpressionElement(HLExpressionElement element) {
		root = element;
	}

	/**
	 * Returns expression element that is associated to the root node.
	 * 
	 * @return the root expression element
	 */
	public HLExpressionElement getRootExpressionElement() {
		return root;
	}

	/**
	 * Replaces the given attribute ID at each attribute operand in the
	 * expression by the the new attribute ID.
	 * 
	 * @param oldID
	 *            the ID to be replaced
	 * @param newID
	 *            the new ID to be assigned to the operands instead
	 */
	public void replaceAttribute(HLID oldID, HLID newID) {
		replaceAttribute(root, oldID, newID);
	}

	/**
	 * Replaces the given old attribute ID by the new ID for all attribute
	 * operands at or below the given expression element in the tree.
	 * 
	 * @param elem
	 *            the root of the sub expression to be treated
	 * @param oldID
	 *            the ID to be replaced
	 * @param newID
	 *            the new ID to be assigned if oldID matches current ID
	 */
	private void replaceAttribute(HLExpressionElement elem, HLID oldID,
			HLID newID) {
		if (elem instanceof HLAttributeOperand) {
			((HLAttributeOperand) elem).replaceAttribute(oldID, newID);
			// attribute operand is leaf node -> so no further recursion needed
		} else if (elem != null) {
			for (int i = 0; i < elem.getExpressionNode().getChildCount(); i++) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) elem
						.getExpressionNode().getChildAt(i);
				HLExpressionElement child = (HLExpressionElement) node
						.getUserObject();
				replaceAttribute(child, oldID, newID);
			}
		}
	}

	/**
	 * Evaluate the sub expression up to this expression element.
	 * 
	 * @param attachedNode
	 *            the node referring to the position of this element in the tree
	 * @return the outcome of evaluating the sub expression up to the given tree
	 *         node
	 */
	public HLExpressionElement evaluate() {
		if (root.isValid() == false) {
			return null;
		} else {
			return root.evaluate();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (root != null) {
			return root.evaluateToString();
		} else {
			return expressionString;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.att.HLAttributeValue#clone()
	 */
	public Object clone() {
		HLDataExpression o = null;
		try {
			o = (HLDataExpression) super.clone();
			// clone tree structure only (shallow copy preserves links to
			// original expression element objects)
			if (root != null) {
				DefaultMutableTreeNode originalRootNode = root
						.getExpressionNode();
				DefaultMutableTreeNode clonedRootNode = (DefaultMutableTreeNode) originalRootNode
						.clone();
				cloneTreeStructure(originalRootNode, clonedRootNode);
				// now clone expression elements
				o.root = cloneExpressionElements(clonedRootNode);
			}
			return o;
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Clones the tree structure in a shallow way (same user objects will be
	 * referenced in the cloned tree).
	 * 
	 * @param originalNode
	 *            the node which sub structure shall be copied
	 * @param copiedNode
	 *            the node at which the original sub structure will be
	 *            re-established
	 */
	private void cloneTreeStructure(DefaultMutableTreeNode originalNode,
			DefaultMutableTreeNode copiedNode) {
		// clone the tree structure of the expression
		for (int i = 0; i < originalNode.getChildCount(); i++) {
			DefaultMutableTreeNode originalChild = (DefaultMutableTreeNode) originalNode
					.getChildAt(i);
			DefaultMutableTreeNode copiedChild = (DefaultMutableTreeNode) originalChild
					.clone();
			copiedNode.add(copiedChild);
			cloneTreeStructure(originalChild, copiedChild);
		}
	}

	/**
	 * Actually clones the expression elements at each of the sub nodes in the
	 * tree.
	 * 
	 * @param node
	 *            the node which sub tree should be treated
	 * @return the cloned expression element that belongs to the given node
	 */
	private HLExpressionElement cloneExpressionElements(
			DefaultMutableTreeNode node) {
		HLExpressionElement orginalEl = (HLExpressionElement) node
				.getUserObject();
		HLExpressionElement clonedEl = (HLExpressionElement) orginalEl.clone();
		clonedEl.setExpressionNode(node);
		node.setUserObject(clonedEl);
		// clone the tree structure of the expression
		for (int i = 0; i < node.getChildCount(); i++) {
			cloneExpressionElements((DefaultMutableTreeNode) node.getChildAt(i));
		}
		return clonedEl;
	}

	/**
	 * Compares expressions based on the order rank.
	 * <p>
	 * Be careful as this must not necessarily be set (default is -1) and there
	 * are also default expressions. Therefore, the user of this class should
	 * make sure that only comparable expressions (i.e., those having a valid
	 * order rank provided, and no duplicate order ranks exist among the
	 * compared set as otherwise there will be no total order).
	 * <p>
	 * Note: this class has a natural ordering that is inconsistent with equals.
	 */
	public int compareTo(Object o) {
		if (((HLDataExpression) o).getOrderRank() == getOrderRank()) {
			// this rank is equal to the rank of the specified object
			return 0;
		} else if (((HLDataExpression) o).getOrderRank() > getOrderRank()) {
			// this rank is smaller than the rank of the specified object
			return -1;
		} else {
			// this rank is greater than the rank of the specified object
			return 1;
		}
	}

}
