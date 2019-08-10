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

package org.processmining.framework.ui;

import org.processmining.framework.plugin.ProvidedObject;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
class ProvidedObjectComboItem {

	private ProvidedObject object;
	private String caption;

	public ProvidedObjectComboItem(String caption, ProvidedObject object) {
		this.object = object;
		this.caption = caption;
	}

	public String toString() {
		return caption + " (" + object.getName() + ")";
	}

	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof ProvidedObjectComboItem) {
			// CHeck if the provided objects are the same, as well as the label.
			return o.toString().equals(toString())
					&& ((ProvidedObjectComboItem) o).object.equals(object);
		} else {
			return false;
		}
		// return o != null && o.toString().equals(toString());
	}

	public ProvidedObject getObject() {
		return object;
	}
}
