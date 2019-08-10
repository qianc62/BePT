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

package org.processmining.framework.models;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

class EnumToIterator implements Iterator {
	private Enumeration e;

	public EnumToIterator(Enumeration e) {
		this.e = e;
	}

	public boolean hasNext() {
		return e.hasMoreElements();
	}

	public Object next() {
		return e.nextElement();
	}

	public void remove() {
		/** @todo Implement this java.util.Iterator method */
		throw new java.lang.UnsupportedOperationException(
				"Method remove() not yet implemented.");
	}
}
