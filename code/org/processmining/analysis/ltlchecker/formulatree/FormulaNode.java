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

import java.util.LinkedList;

import org.processmining.framework.log.ProcessInstance;

/**
 * FormulaNode is the main class of the hierarchy of formula nodes to denote a
 * LTL formula.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public abstract class FormulaNode extends TreeNode {

	// FIELDS

	// CONSTRUCTORS
	public FormulaNode() {
		super();
	}

	// METHODS

	/**
	 * Compute the value of this node, that is te value of this node given the
	 * i-th ate of pi, computed by calling the value method of the children.
	 * 
	 * @param pi
	 *            The current process instance.
	 * @param ate
	 *            The current audit trail entry of pi.
	 * 
	 * @return The value of this node computed by calling the value method of
	 *         the children applied to the operator of this node.
	 */
	public abstract boolean value(ProcessInstance pi, LinkedList ates, int ateNr);

	/**
	 * Write this node as a string
	 * 
	 * @return this node as a string
	 */
	public abstract String toString();

}
