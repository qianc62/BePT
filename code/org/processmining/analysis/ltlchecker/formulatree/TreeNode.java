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
 * Main class for treenodes, for formula nodes as well as for valuenodes.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public class TreeNode {

	/**
	 * The number of ate in pi currently working on. This is needed for binding
	 * values: which ate to use to get the value?
	 */
	protected int nr;

	public TreeNode() {
		nr = 0;
	}

	public int getNr() {
		return this.nr;
	}

}
