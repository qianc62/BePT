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

import java.util.Arrays;
import java.util.BitSet;

/**
 * An efficient, fast, and sparse storage for indexed doubles. (as used in trace
 * clustering profiles)
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class EfficientSparseDoubleArray {

	protected double sparseValue;
	protected BitSet indexUsedMap;
	protected int[] indices;
	protected double[] values;
	protected int realSize;

	public EfficientSparseDoubleArray(double sparseValue) {
		this.sparseValue = sparseValue;
		indexUsedMap = new BitSet();
		indexUsedMap.clear();
		indices = new int[10];
		Arrays.fill(indices, -1);
		values = new double[10];
		realSize = 0;
	}

	public synchronized double get(int virtualIndex) {
		if (indexUsedMap.get(virtualIndex) == false) {
			// value not set
			return sparseValue;
		} else {
			// value set
			int realIndex = findIndex(virtualIndex);
			return values[realIndex];
		}
	}

	public synchronized void set(int virtualIndex, double value) {
		if (value != sparseValue) {
			// value needs to be set
			if (indexUsedMap.get(virtualIndex) == true) {
				// index has been used before, update
				int realIndex = findIndex(virtualIndex);
				values[realIndex] = value;
			} else {
				// index needs to be introduced
				if (realSize == indices.length) {
					growArray(); // enlarge this set
				}
				// insert index
				if (realSize == 0 || virtualIndex > indices[realSize - 1]) {
					// largest index yet, append
					// (maintain increasing sorted order in set)
					indices[realSize] = virtualIndex;
					values[realSize] = value;
				} else {
					// we need to insert this index / value in between
					// (maintain increasing sorted order in set)
					for (int i = realSize - 1; i >= 0; i--) {
						if (indices[i] > virtualIndex) {
							// shift to the right
							indices[i + 1] = indices[i];
							values[i + 1] = values[i];
							if (i == 0) {
								// we have hit the bottom of the collection,
								// i.e. the new index is the smallest in set
								indices[i] = virtualIndex;
								values[i] = value;
								break;
							}
						} else if (indices[i] < virtualIndex) {
							indices[i + 1] = virtualIndex;
							values[i + 1] = value;
							break;
						} else {
							System.out
									.println("ERROR 6: Index already contained?");
						}
					}
				}
				// update data structures
				indexUsedMap.set(virtualIndex, true);
				realSize++;
			}
		} else if (indexUsedMap.get(virtualIndex) == true) {
			// index is currently set, needs to be removed
			int realIndex = findIndex(virtualIndex);
			int maxIndex = realSize - 1;
			for (int i = realIndex; i < maxIndex; i++) {
				indices[i] = indices[i + 1];
				values[i] = values[i + 1];
			}
			indices[maxIndex] = -1;
			values[maxIndex] = 0.0;
			indexUsedMap.set(virtualIndex, false);
			realSize--;
		}
	}

	protected synchronized void growArray() {
		// we need to grow the index and value arrays
		int largerSize = indices.length + (indices.length / 2);
		int largerIndices[] = new int[largerSize];
		Arrays.fill(largerIndices, -1);
		double largerValues[] = new double[largerSize];
		// copy data over
		for (int i = 0; i < realSize; i++) {
			largerIndices[i] = indices[i];
			largerValues[i] = values[i];
		}
		// replace data structures
		indices = largerIndices;
		values = largerValues;
	}

	protected synchronized int findIndex(int virtualIndex) {
		// binary search
		int low = 0;
		int high = realSize - 1;
		int mid;
		while (low <= high) {
			mid = (low + high) / 2;
			if (indices[mid] > virtualIndex) {
				high = mid - 1;
			} else if (indices[mid] < virtualIndex) {
				low = mid + 1;
			} else {
				return mid;
			}
		}
		throw new AssertionError(
				"Addressing error! (could not find proper index)");
	}

}
