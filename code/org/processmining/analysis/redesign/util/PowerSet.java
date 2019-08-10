package org.processmining.analysis.redesign.util;

/**
 * Copyright 2004 Oscar Kind
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.*;
import java.math.BigInteger;

/**
 * A PowerSet class. Models an immutable power set of another set.
 * <p>
 * The parameterized constructor calls one of the three methods that each define
 * another method to determine a power set.
 * <p>
 * All algorithms have the same complexity: O(n*2<sup>n</sup>).
 * 
 * @author <a href="mailto:oscar@hccnet.nl">Oscar Kind</a>
 */
public class PowerSet<E> extends AbstractSet<Set<E>> implements Set<Set<E>> {
	/**
	 * The Set containing the actual power set.
	 */
	private Set<Set<E>> powerSet;

	/**
	 * Create a PowerSet of an empty set.
	 */
	public PowerSet() {
		powerSet = new HashSet<Set<E>>();
		powerSet.add(new HashSet<E>());
		powerSet = Collections.unmodifiableSet(powerSet);
	}

	/**
	 * Create a PowerSet.
	 * 
	 * @param set
	 *            The set to create the power set of.
	 */
	public PowerSet(Set<E> set) {
		this.powerSet = createPowerSet1(set);
	}

	/**
	 * Create a PowerSet.
	 * <p>
	 * Uses a nested loop to build the superset by growing the set it is a power
	 * set of. This method's construction is identical to the recursive
	 * algorithm, but doesn't use as much stack space.
	 * 
	 * @param set
	 *            The set to create the power set of.
	 * 
	 * @return the superset of set.
	 */
	protected final Set<Set<E>> createPowerSet1(Set<E> set) {
		// Create a Set to hold the power set we're constructing.

		Set<Set<E>> powerSet = new HashSet<Set<E>>();
		powerSet.add(new HashSet<E>());

		// For each element in the set:

		for (E element : set) {
			// Add the subsets in the current power set again, but with the new
			// element added.

			// Note: the new set is necessary because otherwise we'd change
			// powerSet while iterating it.

			for (Set<E> subset : new HashSet<Set<E>>(powerSet)) {
				// Note: the new subset is necessary because it is also present
				// in the current powerSet.

				Set<E> newSubset = new HashSet<E>(subset);
				newSubset.add(element);
				powerSet.add(newSubset);
			}
		}

		// Make the power set immutable to prevent it's iterator from removing
		// elements.

		return Collections.unmodifiableSet(powerSet);
	}

	/**
	 * Create a PowerSet.
	 * <p>
	 * Uses recursion to create the power set of a reduced set, and adds the
	 * elements that the power set of the complete set has extra.
	 * 
	 * @param set
	 *            The set to create the power set of.
	 * 
	 * @return the superset of set.
	 */
	protected final Set<Set<E>> createPowerSet2(Set<E> set) {
		Set<Set<E>> powerSet;

		if (set.isEmpty()) {
			// The PowerSet of the empty Set contains one element: the empty
			// Set.

			powerSet = new HashSet<Set<E>>();
			powerSet.add(new HashSet<E>());
		} else {
			// Remove one item from the set.

			Set<E> smallerSet = new HashSet<E>(set);
			Iterator<E> iterator = set.iterator();
			E element = iterator.next();
			iterator.remove();

			// Create the power set of the reduced set.

			powerSet = createPowerSet2(smallerSet);

			// Add the removed item to copies of the elements of the power set,
			// and add the changed copies to the superset.

			for (Set<E> subset : new HashSet<Set<E>>(powerSet)) {
				Set<E> newSubset = new HashSet<E>(subset);
				newSubset.add(element);
				powerSet.add(newSubset);
			}
		}

		// Make the power set immutable to prevent it's iterator from removing
		// elements.

		return Collections.unmodifiableSet(powerSet);
	}

	/**
	 * Create a PowerSet.
	 * <p>
	 * Creates the elements of the superset using the bit pattern of the counter
	 * that enumerates the elements of the superset.
	 * 
	 * @param set
	 *            The set to create the superset of.
	 * 
	 * @return the power set of set.
	 */
	protected final Set<Set<E>> createPowerSet3(Set<E> set) {
		// Create a Set to make into a HashSet.

		Set<Set<E>> powerSet = new HashSet<Set<E>>();

		// Calculate the size of the PowerSet.

		// WARNING:
		// This doesn't work when the Set contains more than Integer.MAX_VALUE
		// elements. {@link Set#size()} is specified to return Integer.MAX_VALUE
		// in that case, which is too small.

		int setSize = set.size();
		BigInteger sizeOfPowerSet = new BigInteger(new byte[] { 2 });
		sizeOfPowerSet = sizeOfPowerSet.pow(setSize);

		// Create a List with the elements of set, to ensure we can create a
		// subset
		// using a bit pattern. Needed because a Set nor its Iterator guarantee
		// a
		// constant order.

		List<E> setAsList = new ArrayList<E>(set);

		// Add all subsets of set, as enumerated by the bit pattern of index.

		for (BigInteger index = BigInteger.ZERO; (index
				.compareTo(sizeOfPowerSet) <= 0); index = index
				.add(BigInteger.ONE)) {
			// Create a subset using the bit pattern of index.

			Set<E> subset = new HashSet<E>();
			for (int bit = 0; bit < setSize; bit++) {
				if (index.testBit(bit)) {
					subset.add(setAsList.get(bit)); // setAsList.size() ==
					// setSize
				}
			}

			// Add the subset to the power set.

			powerSet.add(subset);
		}

		// Make the power set immutable to prevent it's iterator from removing
		// elements.

		return Collections.unmodifiableSet(powerSet);
	}

	/**
	 * Returns an iterator over the elements contained in this collection.
	 * 
	 * @return an iterator over the elements contained in this collection.
	 */
	public Iterator<Set<E>> iterator() {
		return powerSet.iterator();
	}

	/**
	 * Returns the number of elements in this collection. If the collection
	 * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
	 * <tt>Integer.MAX_VALUE</tt>.
	 * 
	 * @return the number of elements in this collection.
	 */
	public int size() {
		return powerSet.size();
	}
}