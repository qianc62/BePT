package org.processmining.analysis.differences.fa;

import java.util.Iterator;
import java.util.Set;

/**
 * The IntegerSet is a class for sets of integers.
 * 
 * @author Marcus Johansson, Remco Dijkman
 * @version 1.0
 */

public class IntegerSet {
	private class SetElement {
		private int element;
		private SetElement nextElement;

		public int getElement() {
			return element;
		}

		public SetElement nextElement() {
			return nextElement;
		}

		public void setNextElement(SetElement e) {
			nextElement = e;
		}

		public SetElement(int element, SetElement nextElement) {
			this.element = element;
			this.nextElement = nextElement;
		}
	}

	private SetElement root;
	private SetElement top;

	private int currSize;

	/**
	 * Constructs an empty set.
	 */
	public IntegerSet() {
		root = top = null;
		currSize = 0;
	}

	/**
	 * Constructs a set identical to a given IntegerSet.
	 * 
	 * @param m
	 *            The set to copy.
	 */
	public IntegerSet(IntegerSet m) {
		this();

		int[] mArray = m.getArray();
		int mLen = m.getCardinality();

		for (int i = 0; i < mLen; i++) {
			add(mArray[i]);
		}

	}

	/**
	 * Constructs a set that contains the elements from another set.
	 * 
	 * @param s
	 *            The set to copy.
	 */
	public IntegerSet(Set<Integer> s) {
		this();

		for (Iterator<Integer> i = s.iterator(); i.hasNext();) {
			add(i.next());
		}

	}

	/**
	 * Returns an array representation of this set.
	 * 
	 * @return An array containing the elements of the set.
	 * 
	 */
	public int[] getArray() {
		int[] array = new int[currSize];

		SetElement next = root;

		int i = 0;
		while (next != null) {
			array[i++] = next.getElement();
			next = next.nextElement();
		}

		if (i != currSize)
			System.out.println("BAHAJANO!!!");
		return array;
	}

	/**
	 * Returns the cardinality of the set.
	 * 
	 * @return The cardinality (size) of the set.
	 * 
	 */
	public int getCardinality() {
		return currSize;
	}

	/**
	 * Returns the union of this set and a given set.
	 * 
	 * @param m
	 *            The IntegerSet to union with.
	 * @return IntegerSet representing the union of this set and m.
	 * 
	 */
	public IntegerSet union(IntegerSet m) {
		IntegerSet res = new IntegerSet(this);

		int[] mArray = m.getArray();
		int mLen = m.getCardinality();

		for (int i = 0; i < mLen; i++) {
			if (!res.contains(mArray[i])) {
				res.add(mArray[i]);
			}
		}

		return res;
	}

	/**
	 * Returns the intersection of this set and a given set.
	 * 
	 * @param m
	 *            The IntegerSet to intersect with.
	 * @return IntegerSet representing the intersection of this set and m.
	 * 
	 */
	public IntegerSet intersection(IntegerSet m) {
		IntegerSet res = new IntegerSet();

		int[] mArray = m.getArray();
		int mLen = m.getCardinality();

		for (int i = 0; i < mLen; i++) {
			if (this.contains(mArray[i])) {
				res.add(mArray[i]);
			}
		}

		return res;
	}

	/**
	 * Checks whether this set equals the empty set.
	 * 
	 * @return true if this set is empty, otherwise false.
	 * 
	 */
	public boolean isEmpty() {
		return (currSize == 0) ? true : false;
	}

	/**
	 * Adds an element to the set. If the element already exists, nothing
	 * happens.
	 * 
	 * @param num
	 *            The integer to add.
	 * 
	 */
	public void add(int num) {
		if (contains(num))
			return;

		if (root == null && top == null) {
			root = new SetElement(num, null);
			top = root;
			currSize++;
		} else {
			top.setNextElement(new SetElement(num, null));
			top = top.nextElement();
			currSize++;
		}
	}

	/**
	 * Checks whether a given integer is contained in the set.
	 * 
	 * @param d
	 *            The integer to look for.
	 * @return true if the integer is contained within the set, otherwise false.
	 * 
	 */
	public boolean contains(int d) {
		SetElement elem = root;

		while (elem != null) {
			if (elem.getElement() == d) {
				return true;
			}

			elem = elem.nextElement();
		}

		return false;
	}

	/**
	 * Returns a string representation of the set.
	 * 
	 * @return The set as a String.
	 * 
	 */
	public String toString() {
		String str = "{";

		SetElement elem = root;

		while (elem != null) {
			if (elem != root) {
				str += ", ";
			}

			str += elem.getElement();
			elem = elem.nextElement();
		}

		return str + "}";
	}

	/**
	 * Removes a given integer from the set. If the integer does not exist in
	 * the set, nothing happens.
	 * 
	 * @param d
	 *            The integer to remove.
	 * 
	 */
	public void remove(int d) {
		SetElement elem = root;
		SetElement prev = null;

		while (elem != null) {
			if (elem.getElement() == d) {
				if (prev == null) {
					if (top == root) {
						top = elem.nextElement();
					}
					root = elem.nextElement();
				} else {

					if (elem == top) {
						top = prev;
					}

					prev.setNextElement(elem.nextElement());
				}
				currSize--;
				break;
			}

			prev = elem;
			elem = elem.nextElement();
		}

	}

	/**
	 * Removes a the elements of a given set from the set.
	 * 
	 * @param m
	 *            The set to remove.
	 * @return The set containing the remaining elements.
	 * 
	 */
	public IntegerSet removeSet(IntegerSet m) {
		IntegerSet res = new IntegerSet();

		int[] array = getArray();
		int len = getCardinality();

		for (int i = 0; i < len; i++) {
			if (!m.contains(array[i])) {
				res.add(array[i]);
			}
		}

		return res;
	}

	/**
	 * Returns the first element of the set (the set is not sorted, so the
	 * "first" element is practically the oldest element).
	 * 
	 * @return The oldest element of the set.
	 * 
	 */
	public int firstElement() {
		if (root == null) {
			return -1;
		}

		return root.getElement();
	}

	/**
	 * Checks if the set is identical (contains the same elements) to a given
	 * set.
	 * 
	 * @param m
	 *            The set to check for equality with.
	 * @return true if the sets are equal, otherwise false.
	 * 
	 */
	public boolean equals(IntegerSet m) {
		if (m.subset(this) && this.subset(m)) {
			return true;
		}

		return false;
	}

	/**
	 * Checks if a given set is a subset to this set.
	 * 
	 * @param m
	 *            The set to check with.
	 * @return true if the given set is a subset, otherwise false.
	 * 
	 */
	public boolean subset(IntegerSet m) {
		int[] mArray = m.getArray();
		int mLen = m.getCardinality();

		for (int i = 0; i < mLen; i++) {
			if (!this.contains(mArray[i])) {
				return false;
			}
		}

		return true;
	}

}