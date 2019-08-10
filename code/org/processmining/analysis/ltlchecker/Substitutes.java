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

package org.processmining.analysis.ltlchecker;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.processmining.analysis.ltlchecker.formulatree.RootNode;
import org.processmining.analysis.ltlchecker.formulatree.SetValueNode;
import org.processmining.analysis.ltlchecker.formulatree.ValueNode;

/**
 * Substitutes is a wrapper class for a treemap containing substitutes in local
 * context. That is local variables are defined and later on they get an value
 * which is used by comparisons and formula calls.
 * 
 * These substitutes are also used to attach user-added values for formulae. The
 * checker therefor does not see any difference between a formula call in the
 * language and one via the GUI.
 * 
 * @version 0.1
 * @author HT de Beer
 */

public class Substitutes {

	// FIELDS

	/**
	 * The map containing the substitutes: that is an id with an corresponding
	 * value.
	 */
	private TreeMap<String, ValueNode> substs;

	// CONSTRUCTORS

	public Substitutes() {
		substs = new TreeMap<String, ValueNode>();
	}

	public Substitutes(TreeMap<String, ValueNode> substs) {
		this.substs = substs;
	}

	// METHODS

	/**
	 * Set the binding node for user added values so that these parameters are
	 * bounded on the highest level, ther where they are used. Actually this
	 * binding is not needed because the values the user add are literals.
	 * Binding is only needed by attribute values. But every value node should
	 * have a binder.
	 * 
	 * @param binder
	 *            The node to bind the values in this substituesset.
	 */
	public void setBinder(RootNode binder) {
		Iterator<ValueNode> i = substs.values().iterator();

		while (i.hasNext()) {
			i.next().setBinder(binder);
		}
	}

	/**
	 * Add the value of an id, by calling of an function or in an comparison
	 * expression.
	 * 
	 * @param id
	 *            The id for which the value is to be set.
	 * @param val
	 *            The value for the id.
	 */
	public void add(String id, ValueNode val) {
		substs.put(id, val);
	}

	/**
	 * Get the value of an id.
	 * 
	 * @param id
	 *            The id the value is to be get.
	 * 
	 * @return The value of id.
	 */
	public ValueNode get(String id) {
		return substs.get(id);
	}

	public boolean hasId(String id) {
		return substs.containsKey(id);
	}

	public TreeMap<String, ValueNode> getAll() {
		return substs;
	}

	/**
	 * Create a shallow copy of this Substitutes object, that is, the
	 * substitutes themselves are not copied.
	 */
	public Object clone() {
		Substitutes result = new Substitutes();

		for (Map.Entry<String, ValueNode> item : substs.entrySet()) {
			result.add(item.getKey(), item.getValue());
		}
		return result;
	}

	public String toString() {
		String result = "<table>";
		Iterator i = substs.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			ValueNode vn = (ValueNode) substs.get(key);
			if (vn instanceof SetValueNode) {
				result += "<tr><td>" + key + "</td><td>=</td>";
				result += "<td>" + vn.toString() + "</td>";
				result += "</tr>";
			}
			;
		}
		;
		return result + "</table>";
	}

}
