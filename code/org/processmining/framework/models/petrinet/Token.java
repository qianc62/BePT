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

package org.processmining.framework.models.petrinet;

import java.util.*;

/**
 * Token can be held by places. Since they are consumed and produced if a
 * transition fires, they flow through the PetriNet. The current position of all
 * tokens in the net are denoted as the current marking of the net, and this
 * determines the current state of the dynamic structure.
 * 
 * @see Place
 * @see Transition
 * @see PetriNet
 */

public class Token implements Cloneable {

	private Date timestamp; /* holds timestamp for this token, may be null */
	private boolean isTimed; /* states whether there is a timestamp associated */

	public Token(Date timestamp) {
		this.timestamp = timestamp;
		isTimed = true;
	}

	public boolean isTimed() {
		return this.isTimed;
	}

	public Token() {
		isTimed = false;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date value) {
		timestamp = value;
	}

	// //////////////// GENERAL PURPOSE METHODS
	// /////////////////////////////////

	/**
	 * Make a deep copy of the object. Note that this method needs to be
	 * extended as soon as there are attributes added to the class which are not
	 * primitive or immutable.
	 * 
	 * @return Object The cloned object.
	 */
	public Object clone() {
		Token o = null;
		try {
			o = (Token) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		// clone referenced objects to realize deep copy
		if (timestamp != null) {
			o.timestamp = (Date) timestamp.clone();
		}
		return o;
	}

}
