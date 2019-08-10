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
 * RootNode is used for binding formulae and values to the highest level of a
 * defined formula. It is created only for that purpose and fullfill he role of
 * the root as otherwise a other node has fullfilled.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public class RootNode extends TreeNode {

	private FormulaNode formula;

	public RootNode() {
		super();
	}

	public void setFormula(FormulaNode formula) {
		this.formula = formula;
	}

	public FormulaNode getFormula() {
		return formula;
	}

	/** Just return the formula's value. */
	public boolean value(ProcessInstance pi, LinkedList ates, int ateNr) {
		this.nr = ateNr;
		return formula.value(pi, ates, ateNr);
	}

	public String toString() {
		return formula.toString();
	}

}
