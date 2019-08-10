/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.analysis.ltlchecker.formulatree;

/**
 * BinaryNode is a node class of the formula tree denoting binary logic
 * operators, like and, or, until, etc.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public abstract class BinaryNode extends FormulaNode {

	// FIELDS

	/** The left child formula every binary operator has. */
	FormulaNode leftChild;

	/** The right child formula every binary operator has. */
	FormulaNode rightChild;

	// CONSTRUCTORS

	public BinaryNode() {
		super();
	}

	// METHODS

	/**
	 * Set the left child node with the formula the child is.
	 * 
	 * @param child
	 *            The left child node.
	 */
	public void setLeftChild(FormulaNode child) {
		this.leftChild = child;
	}

	/**
	 * Set the right child node with the formula the child is.
	 * 
	 * @param child
	 *            The right child node.
	 */
	public void setRightChild(FormulaNode child) {
		this.rightChild = child;
	}

	protected abstract String getOperator();

	public String toString() {
		return "(" + leftChild.toString() + " " + getOperator() + " "
				+ rightChild.toString() + ")";
	}

}
