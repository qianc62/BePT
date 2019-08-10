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

import java.util.Iterator;
import java.util.LinkedList;

import org.processmining.framework.log.ProcessInstance;

/**
 * ForallNode is a node class of the formula tree denoting the for all quantor.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public class ForallNode extends QuantorNode {

	// FIELDS

	// CONSTRUCTORS

	public ForallNode() {
		super();
	}

	// METHODS

	/**
	 * Compute the value of this node, that is te value of this node given the
	 * i-th ate of pi, computed by calling the value method of the child.
	 * 
	 * @param pi
	 *            The current process instance.
	 * @param ate
	 *            The current audit trail entry of pi.
	 * 
	 * @return The value of this node computed by calling the value method of
	 *         the child applied to the operator of this node.
	 */
	public boolean value(ProcessInstance pi, LinkedList ates, int ateNr) {
		nr = ateNr;
		boolean result = true;

		Iterator i = children.iterator();
		FormulaNode fnode;

		while (i.hasNext()) {
			fnode = (FormulaNode) i.next();
			if (!fnode.value(pi, ates, ateNr)) {
				result = false;
			}
			;
		}
		;

		return result;
	}

	protected String getOperator() {
		return "forall";
	}
}
