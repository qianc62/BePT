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

import java.util.ArrayList;

/**
 * QuantorNode is a node class of the formula tree denoting the quantors for all
 * and exists.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public abstract class QuantorNode extends FormulaNode {

	// FIELDS

	/** The list with children formulae of this quantor. */
	ArrayList children;

	// CONSTRUCTORS

	public QuantorNode() {
		super();
		children = new ArrayList();
	}

	// METHODS

	/**
	 * Add a child node with the formula the child is.
	 * 
	 * @param child
	 *            The child node.
	 */
	public void addChild(FormulaNode child) {
		children.add(child);
	}

	protected abstract String getOperator();

	public String toString() {
		String s = getOperator() + "( ";
		for (int i = 0; i < children.size(); i++) {
			s += (i > 0 ? "," : "")
					+ ((FormulaNode) children.get(i)).toString();
		}
		return s + " )";
	}
}
