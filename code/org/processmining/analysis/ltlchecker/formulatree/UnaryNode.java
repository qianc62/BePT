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
 * UnaryNode is a node class of the formula tree denoting unary logic operators,
 * like not, always, etc.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public abstract class UnaryNode extends FormulaNode {

	// FIELDS

	/** The child formula every unary operator has. */
	FormulaNode child;

	// CONSTRUCTORS

	public UnaryNode() {
		super();
	}

	// METHODS

	/**
	 * Set the child node with the formula the child is.
	 * 
	 * @param child
	 *            The child node.
	 */
	public void setChild(FormulaNode child) {
		this.child = child;
	}

	protected abstract String getOperator();

	public String toString() {
		return getOperator() + "( " + (child == null ? "" : child.toString())
				+ " )";
	}

}
