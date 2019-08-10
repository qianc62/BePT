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

package org.processmining.framework.models.heuristics;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Ana Karla A. de Medeiros, Peter van den Brand
 * @version 1.0
 */
public class HNSubSet implements Comparable {

	private int[] subset;
	private int size;
	private int hash;

	private static int[] hashValues;

	static {
		int num = 1000;

		hashValues = new int[num];
		for (int i = 0; i < num; i++) {
			hashValues[i] = i * ((int) Math.pow(31, i));
		}
	}

	public HNSubSet() {
		subset = new int[10];
		size = 0;
		hash = 0;
	}

	// this constructor is only used by deepCopy
	private HNSubSet(HNSubSet setToCopy) {
		subset = new int[setToCopy.subset.length];
		System.arraycopy(setToCopy.subset, 0, subset, 0, setToCopy.size);
		size = setToCopy.size;
		hash = setToCopy.hash;
	}

	public final int size() {
		return size;
	}

	public final int get(int index) {
		return subset[index];
	}

	public final boolean contains(int value) {
		return binarySearch(value) >= 0;
	}

	public final HNSubSet deepCopy() {
		return new HNSubSet(this);
	}

	public void addAll(HNSubSet toAdd) {
		for (int i = 0; i < toAdd.size; i++) {
			this.add(toAdd.get(i));
		}
	}

	public void add(int i) {

		// do binary search to find position of new element
		int pos = binarySearch(i);

		if (pos < 0) {
			pos = (-pos - 1);

			// increase capacity if needed
			if (size == subset.length) {
				int[] newSubset = new int[subset.length * 2];

				System.arraycopy(subset, 0, newSubset, 0, subset.length);
				subset = newSubset;
			}

			// make space for the new element ...
			System.arraycopy(subset, pos, subset, pos + 1, size - pos);

			// ... and insert it
			subset[pos] = i;
			size++;

			hash += hashValues[Math.abs(i % hashValues.length)];
		}
	}

	public String toString() {
		StringBuffer str = new StringBuffer("[");
		int i;

		for (i = 0; i < size; i++) {
			str.append(subset[i]).append(",");
		}
		if (i > 0) {
			str.deleteCharAt(str.length() - 1);
		}
		str.append("]");

		return str.toString();

	}

	public void remove(int i) {
		int pos = binarySearch(i);

		if (pos >= 0) {
			System.arraycopy(subset, pos + 1, subset, pos, size - pos - 1);
			size--;

			hash -= hashValues[Math.abs(i % hashValues.length)];
		}
	}

	public void removeAll(HNSubSet toRemove) {
		for (int i = 0; i < toRemove.size; i++) {
			remove(toRemove.get(i));
		}
	}

	public int hashCode() {
		return hash;
	}

	// we compare HNSubSets in the following way:
	// 1) a non-null HNSubSet is always 'less than' a null element
	// 2) an HNSubSet with smaller size is always 'less than' an HNSubSet with
	// bigger size
	// 3) after comparing the size, we compare the elements one by one
	public int compareTo(Object o) {
		if (o == null) {
			return -1;
		}

		HNSubSet set = (HNSubSet) o;

		if (size != set.size) {
			return size < set.size ? -1 : 1;
		}

		for (int i = 0; i < size; i++) {
			if (subset[i] != set.subset[i]) {
				return subset[i] < set.subset[i] ? -1 : 1;
			}
		}
		return 0;
	}

	public boolean equals(Object o) {
		HNSubSet set = (HNSubSet) o;

		if (set == null || set.size != size || set.hash != hash) {
			return false;
		}

		// 'i' goes from size - 1 to 0 to gain a bit of speed
		for (int i = size - 1; i >= 0; i--) {
			if (subset[i] != set.subset[i]) {
				return false;
			}
		}
		return true;
	}

	// copied from the standard Arrays class, and adapted to our case:
	private int binarySearch(int key) {
		int low = 0;
		int high = size - 1; // search only the added elements, not the extra
		// capacity elements

		while (low <= high) {
			int mid = (low + high) >> 1;
			int midVal = subset[mid];

			if (midVal < key) {
				low = mid + 1;
			} else if (midVal > key) {
				high = mid - 1;
			} else {
				return mid; // key found
			}
		}
		return -(low + 1); // key not found.
	}

	public static DoubleMatrix1D toDoubleMatrix1D(HNSubSet elements, int size) {

		DoubleMatrix1D matrix = new SparseDoubleMatrix1D(size);

		for (int i = 0; i < elements.size(); i++) {
			matrix.set(elements.get(i), 1);
		}

		return matrix;
	}

}
