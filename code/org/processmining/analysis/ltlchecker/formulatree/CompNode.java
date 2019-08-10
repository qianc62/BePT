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
 * CompNode is a node class of the formula tree denoting comparator operators,
 * like ==, ~=, <=, >=, etc.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public abstract class CompNode extends FormulaNode {

	// FIELDS
	/** == */
	final public static int EQUAL = 0;
	/** != */
	final public static int NOTEQUAL = 1;
	/** <= */
	final public static int LESSEREQUAL = 2;
	/** >= */
	final public static int BIGGEREQUAL = 3;
	/** ~= */
	final public static int REGEXPEQUAL = 4;
	/** < */
	final public static int LESSER = 5;
	/** > */
	final public static int BIGGER = 6;
	/** in */
	final public static int IN = 7;

	// CONSTRUCTORS

	public CompNode() {
		super();
	}

	public String opAsString(int op) {
		switch (op) {
		case EQUAL:
			return "==";
		case NOTEQUAL:
			return "!=";
		case LESSEREQUAL:
			return "=<";
		case BIGGEREQUAL:
			return ">=";
		case REGEXPEQUAL:
			return "matches";
		case BIGGER:
			return ">";
		case IN:
			return "in";

		}
		return "";
	}

	// METHODS

}
