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

package org.processmining.framework.log.classic;

import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntries;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class ModelElementInstancesClassic {

	private ArrayList ates;
	private Iterator iterator;

	public ModelElementInstancesClassic() {
		ates = new ArrayList();
		reset();
	}

	public void add(AuditTrailEntries list) {
		ates.add(list);
		reset();
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public AuditTrailEntries next() {
		return (AuditTrailEntries) iterator.next();
	}

	public void reset() {
		iterator = ates.iterator();
	}

	public int size() {
		return ates.size();
	}

	public AuditTrailEntries get(int index) {
		return (AuditTrailEntries) ates.get(index);
	}

	public AuditTrailEntries first() {
		return ates.size() > 0 ? (AuditTrailEntries) ates.get(0) : null;
	}

	public AuditTrailEntries last() {
		return ates.size() > 0 ? (AuditTrailEntries) ates.get(ates.size() - 1)
				: null;
	}

	public String toString() {
		String s = "    ModelElementInstances:\n";

		for (int i = 0; i < ates.size(); i++) {
			s += "      " + ates.get(i).toString() + "\n";
		}
		return s;
	}
}
