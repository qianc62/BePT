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
 * @author Ana Karla A. de Medeiros, Peter van den Brand
 * @version 1.0
 */
public class HNSet {

	private HNSubSet[] set;
	private int size;

	public HNSet() {
		set = new HNSubSet[10];
		size = 0;

	}

	// this constructor is only used by deepCopy
	private HNSet(HNSet setToCopy) {
		set = new HNSubSet[setToCopy.set.length];
		size = setToCopy.size;

		for (int i = 0; i < size; i++) {
			set[i] = setToCopy.set[i].deepCopy();
		}
	}

	public final int size() {
		return size;
	}

	public final HNSubSet get(int index) {
		return set[index];
	}

	/*
	 * public final boolean contains(int value) { return
	 * Arrays.binarySearch(subset, value) >= 0; }
	 */
	public final HNSet deepCopy() {
		return new HNSet(this);
	}

	public void add(HNSubSet subset) {
		if (subset == null) {
			throw new RuntimeException("don't add null, I don't like that!");
		}

		// do binary search to find position of new element
		int pos = binarySearch(subset);

		if (pos < 0) {
			pos = (-pos - 1);

			// increase capacity if needed
			if (size == set.length) {
				HNSubSet[] newSet = new HNSubSet[set.length * 2];

				System.arraycopy(set, 0, newSet, 0, set.length);
				set = newSet;
			}

			// make space for the new element ...
			System.arraycopy(set, pos, set, pos + 1, size - pos);

			// ... and insert it
			set[pos] = subset;
			size++;
		}
	}

	public void addAll(HNSet setToInclude) {
		if (set != null) {

			for (int i = 0; i < setToInclude.size(); i++) {
				HNSubSet subset = setToInclude.get(i);
				// do binary search to find position of new element
				int pos = binarySearch(subset);

				if (pos < 0) {
					pos = (-pos - 1);

					// increase capacity if needed
					if (size == set.length) {
						HNSubSet[] newSet = new HNSubSet[set.length * 2];

						System.arraycopy(set, 0, newSet, 0, set.length);
						set = newSet;
					}

					// make space for the new element ...
					System.arraycopy(set, pos, set, pos + 1, size - pos);

					// ... and insert it
					set[pos] = subset;
					size++;
				}
			}
		}
	}

	public boolean contains(HNSubSet subset) {
		return binarySearch(subset) >= 0;
	}

	public void remove(HNSubSet subset) {
		int pos = binarySearch(subset);

		if (pos >= 0) {
			System.arraycopy(set, pos + 1, set, pos, size - pos - 1);
			size--;
		}
	}

	public void removeAll(HNSet toRemove) {
		for (int i = 0; i < toRemove.size; i++) {
			remove(toRemove.get(i));
		}
	}

	public int hashCode() {

		int hash = 0;
		for (int i = 0; i < size; i++) {
			hash += (this.get(i).hashCode() * 31 ^ (size - i + 1));

		}

		return hash;
	}

	public boolean equals(Object o) {
		HNSet otherSet = (HNSet) o;

		if (otherSet == null || otherSet.size != size) {
			return false;
		}

		for (int i = 0; i < size; i++) {
			if (!set[i].equals(otherSet.set[i])) {
				return false;
			}
		}
		return true;
	}

	// copied from the standard Arrays class, and adapted to our case:
	private int binarySearch(HNSubSet key) {
		int low = 0;
		int high = size - 1; // search only the added elements, not the extra
		// capacity elements

		while (low <= high) {
			int mid = (low + high) >> 1;
			HNSubSet midVal = set[mid];
			int cmp = midVal.compareTo(key);

			if (cmp < 0) {
				low = mid + 1;
			} else if (cmp > 0) {
				high = mid - 1;
			} else {
				return mid; // key found
			}
		}
		return -(low + 1); // key not found.
	}

	public String toString() {
		StringBuffer str = new StringBuffer("[");
		int i;

		for (i = 0; i < size; i++) {
			str.append(set[i].toString()).append(",");
		}
		if (i > 0) {
			str.deleteCharAt(str.length() - 1);
		}
		str.append("]");

		return str.toString();
	}

	public static final HNSubSet getUnionSet(HNSet set) {
		HNSubSet unionSet = new HNSubSet();
		HNSubSet subset = null;

		for (int i = 0; i < set.size(); i++) {
			subset = set.get(i);
			for (int j = 0; j < subset.size(); j++) {
				unionSet.add(subset.get(j));
			}

		}

		return unionSet;
	}

	public static HNSet removeElementFromSubsets(HNSet set, int element) {

		HNSet returnSet = new HNSet();
		HNSubSet subset = null;

		for (int iSet = 0; iSet < set.size(); iSet++) {
			subset = set.get(iSet);
			subset.remove(element);
			if (subset.size() > 0) {
				returnSet.add(subset);
			}
		}

		return returnSet;
	}

}
