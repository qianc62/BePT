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

package org.processmining.analysis.ltlchecker.parser;

import java.util.Iterator;
import java.util.TreeMap;

public class AttributeSet {
	/**
	 * AttributeSet is an wrapperclass on a {@see TreeMap}. In principle is it a
	 * list of names mapping to attribute objects. Some methods are added to be
	 * usefull by parsing.
	 *
	 * @version 0.1
	 * @author HT de Beer
	 */

	/* Fields */

	/** attributes contains the attributes of this set. */
	private TreeMap attributes;

	/* Constructors */

	public AttributeSet() {
		attributes = new TreeMap();
	}

	/* Methods */

	/** Add an {@see Attribute} to this set.
	 *
	 * @param name The identifier of the attribute.
	 * @param attribute The attribute to add to this set.
	 */
	public void add(String name, Attribute attribute) {
		attributes.put(name, attribute);
	}

	/** Returns if name of attribute already exists.
	 *
	 * @param name The identifier of the attribute.
	 *
	 * @return If name is in this set, return true, else false.
	 */
	public boolean exists(String name) {
		return attributes.containsKey(name);
	}

	/** Get an attribute given an identifier
	 *
	 * @param name The identifier of the attribute.
	 *
	 * @return The attribute attached to name, if none attached, return null.
	 */
	public Attribute get(String name) {
		return (Attribute) attributes.get(name);
	}

	/** Is this set empty?
	 *
	 * @return Return true if this set is empty, else false.
	 */
	public boolean isEmpty() {
		return (attributes.size() == 0);
	}

	/** Return an iterator over all attributes in this set.
	 *
	 * @return An iterator over all attributes in this set.
	 */
	public Iterator iterator() {
		return attributes.values().iterator();
	}
}
