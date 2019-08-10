/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.mining.organizationmining.util;

import java.util.ArrayList;

/**
 * @author christian
 * 
 */
public class SparseArray<T> {

	protected ArrayList<Integer> indices;
	protected ArrayList<T> values;
	protected T sparseValue;

	public SparseArray(T aSparseValue) {
		indices = new ArrayList<Integer>();
		values = new ArrayList<T>();
		sparseValue = aSparseValue;
	}

	public void set(int index, T value) {
		int localIndex = translateIndex(index);
		if (value.equals(sparseValue)) {
			if (localIndex >= 0) {
				// sparse value to be written over
				// already recorded value; remove
				indices.remove(localIndex);
			}
			// else: writing sparse value to previously
			// not existing field does not require any
			// action.
			return;
		} else if (localIndex < 0) {
			// non-sparse value to be written over
			// previously sparse field; insert index
			localIndex = insertIndex(index);
		} else {
			// non-sparse value to be written over
			// previously inhabited field;
			// remove old value first
			values.remove(localIndex);
		}
		// insert value at correct position
		values.add(localIndex, value);
	}

	public T get(int index) {
		int localIndex = translateIndex(index);
		if (localIndex < 0) {
			return sparseValue;
		} else {
			return values.get(localIndex);
		}
	}

	protected int translateIndex(int virtualIndex) {
		// int currentIndex = java.util.Collections.binarySearch(indices,
		// virtualIndex);
		// int currentIndex = indices.indexOf(virtualIndex);
		int currentIndex = findIndex(virtualIndex);
		if (currentIndex >= 0) {
			return currentIndex;
		} else {
			return -1;
		}
	}

	protected int findIndex(int virtualIndex) {
		int size = indices.size();
		if (size == 0) {
			return -1;
		} else if (size == 1) {
			if (indices.get(0) == virtualIndex) {
				return 0;
			} else {
				return -1;
			}
		}
		// binary search
		int low = 0;
		int high = size - 1;
		int mid, current;
		while (low <= high) {
			mid = (low + high) / 2;
			current = indices.get(mid);
			if (current > virtualIndex) {
				high = mid - 1;
			} else if (current < virtualIndex) {
				low = mid + 1;
			} else {
				return -1;
			}
		}
		return -1;
	}

	protected int insertIndex(int index) {
		// find correct insert position;
		// search from the back of index list
		for (int i = indices.size(); i > 0; i--) {
			int lowerIndex = indices.get(i - 1);
			if (lowerIndex < index) {
				indices.add(i, index);
				return i;
			}
		}
		// smallest index so far;
		// insert at head of the index list
		indices.add(0, index);
		return 0;
	}

}
