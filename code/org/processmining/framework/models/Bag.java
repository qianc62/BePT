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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

public class Bag extends HashSet {
	private HashMap occurances = new HashMap();
	private int numElements;

	public Bag() {
		super();
	}

	public Bag(Collection c) {
		super(c);
		// Build the hashtable
		Iterator it = c.iterator();
		while (it.hasNext()) {
			addOccurance(it.next(), 1);
		}
	}

	private void addOccurance(Object o, int count) {
		if (count > 0) {
			Integer i = (Integer) occurances.get(o);
			if (i == null) {
				occurances.put(o, new Integer(count));
			} else {
				occurances.put(o, new Integer(i.intValue() + count));
			}
			numElements++;
		}
	}

	private boolean removeOccurance(Object o) {
		Integer i = (Integer) occurances.get(o);
		if (i == null) {
			return false;
		} else {
			int j = i.intValue() - 1;
			if (j > 0) {
				occurances.put(o, new Integer(j));
			} else {
				occurances.remove(o);
				super.remove(o);
			}
			numElements--;
			return true;
		}
	}

	public boolean add(Object o) {
		boolean b = super.add(o);
		addOccurance(o, 1);
		return b;
	}

	public boolean add(Object o, int count) {
		boolean b = super.add(o);
		addOccurance(o, count);
		return b;
	}

	public void clear() {
		super.clear();
		occurances.clear();
		numElements = 0;
	}

	public Object clone() {
		return new Bag(this);
	}

	public int getOccurances(Object o) {
		if (occurances.containsKey(o)) {
			return ((Integer) occurances.get(o)).intValue();
		} else {
			return 0;
		}
	}

	public boolean remove(Object o) {
		return removeOccurance(o);
	}

	public int size() {
		return numElements;
	}

	public String toString() {
		String s = "size:" + size() + "  [";
		Iterator it = super.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			int i = getOccurances(o);
			s += "(" + String.valueOf(o);
			s += "," + i + ")";
			if (it.hasNext()) {
				s += ", ";
			}
		}
		s += "]";
		return s;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Bag)) {
			return false;
		}

		boolean b = super.equals(((HashSet) o));
		if (!b) {
			return false;
		}

		Iterator it = iterator();
		while (it.hasNext() && b) {
			Object o2 = it.next();
			b = occurances.get(o2).equals(((Bag) o).occurances.get(o2));
		}
		return b;
	}

	public boolean isLessOrEqual(Bag b) {
		Iterator it = iterator();
		boolean r = true;
		while (it.hasNext() && r) {
			Object o = it.next();
			r = (getOccurances(o) <= b.getOccurances(o));
		}
		return r;
	}

}
