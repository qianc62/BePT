package org.processmining.analysis.differences.fa;

/**
 * The IntegerPartition is a class for sets of IntegerSets. No actual checking
 * is done to ensure that the "partition" actually is a valid integer partition
 * (i.e. that no different sets of the partition contain the same element).
 * 
 * @author Marcus Johansson, Remco Dijkman
 * @version 1.0
 */

public class IntegerPartition {
	private class GroupElement {
		private IntegerSet element;
		private GroupElement nextElement;

		public IntegerSet getElement() {
			return element;
		}

		public GroupElement nextElement() {
			return nextElement;
		}

		public void setNextElement(GroupElement e) {
			nextElement = e;
		}

		public GroupElement(IntegerSet element, GroupElement nextElement) {
			this.element = element;
			this.nextElement = nextElement;
		}
	}

	private GroupElement root;
	private GroupElement top;
	private int currSize;

	/**
	 * Constructs an empty partition.
	 */
	public IntegerPartition() {
		root = top = null;
		currSize = 0;
	}

	/**
	 * Adds a group to the partition. If the group already exists, nothing
	 * happens.
	 * 
	 * @param num
	 *            The integer to add.
	 * @return The id of the group.
	 * 
	 */
	public int addGroup(IntegerSet group) {
		if (contains(group))
			return -1;

		if (root == null && top == null) {
			root = new GroupElement(group, null);
			top = root;
			currSize++;
		} else {
			top.setNextElement(new GroupElement(group, null));
			top = top.nextElement();
			currSize++;
		}

		return currSize - 1;
	}

	/**
	 * Adds an array of groups to the partition.
	 * 
	 * @param p
	 *            The array of groups to add.
	 * @param size
	 *            The size of the array of groups.
	 * 
	 */
	public void addGroups(IntegerSet[] p, int size) {
		for (int i = 0; i < size; i++) {
			if (p[i] != null) {
				addGroup(p[i]);
			}
		}
	}

	/**
	 * Returns the group with the given order index.
	 * 
	 * @param groupNum
	 *            The index of the group to return.
	 * @return The group at the given index.
	 * 
	 */
	public IntegerSet getGroup(int groupNum) {
		GroupElement next = root;

		int i = 0;
		while (next != null) {
			if (i == groupNum) {
				return next.getElement();
			}

			next = next.nextElement();
			i++;
		}

		return null;
	}

	/**
	 * Returns an array representation of the partition..
	 * 
	 * @return An array of IntegerSets representating the partition.
	 * 
	 */
	public IntegerSet[] getGroups() {
		IntegerSet[] array = new IntegerSet[currSize];

		GroupElement next = root;

		int i = 0;
		while (next != null) {
			array[i++] = next.getElement();
			next = next.nextElement();
		}

		return array;
	}

	/**
	 * Return the number of groups in the partition.
	 * 
	 * @return The number of groups in the partition.
	 * 
	 */
	public int getSize() {
		return currSize;
	}

	/**
	 * Returns the group index containing the given element.
	 * 
	 * @param elem
	 *            The element to search for.
	 * @return An int containing the group index containing the given element.
	 *         -1 if not found.
	 * 
	 */
	public int getElementGroupNumber(int elem) {
		GroupElement next = root;

		int i = 0;
		while (next != null) {
			if (next.getElement().contains(elem)) {
				return i;
			}
			next = next.nextElement();
			i++;
		}

		return -1;
	}

	/**
	 * Checks whether a given partition is a subset of this partition.
	 * 
	 * @param p
	 *            The array of groups to add.
	 * @return true if the given partition is a subset, otherwise false.
	 * 
	 */
	public boolean subset(IntegerPartition p) {
		IntegerSet[] pArray = p.getGroups();
		int pLen = p.getSize();

		for (int i = 0; i < pLen; i++) {
			if (!this.contains(pArray[i])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns the group index of the given group.
	 * 
	 * @param s
	 *            The group to search for.
	 * @return The index of the group.
	 * 
	 */
	public int getGroupNumber(IntegerSet s) {
		GroupElement next = root;

		int i = 0;
		while (next != null) {
			if (next.getElement().equals(s)) {
				return i;
			}
			next = next.nextElement();
			i++;
		}

		return -1;
	}

	/**
	 * Returns the first (i.e. the oldest one) group of the partition.
	 * 
	 * @return The first/oldest group.
	 * 
	 */
	public IntegerSet popFirstGroup() {
		if (root == null) {
			return null;
		}

		IntegerSet pop = root.getElement();

		if (top == root) {
			top = root = null;
		} else {
			root = root.nextElement();
		}

		return pop;

	}

	/**
	 * Checks if a given group is contained in the partition.
	 * 
	 * @param s
	 *            The group to search for.
	 * @return true if the group was found in the partition, otherwise false.
	 * 
	 */
	public boolean contains(IntegerSet s) {
		GroupElement next = root;

		while (next != null) {
			if (next.getElement().equals(s)) {
				return true;
			}

			next = next.nextElement();
		}

		return false;
	}

	/**
	 * Checks if this partition equals a given partition.
	 * 
	 * @param p
	 *            The partition to check for equality with.
	 * @return true if the partitions are identical, otherwise false.
	 * 
	 */
	public boolean equals(IntegerPartition p) {
		if (p.subset(this) && subset(p)) {
			return true;
		}

		return false;
	}

	/**
	 * Returns a string representation of the partition.
	 * 
	 * @return The partition as a String.
	 * 
	 */
	public String toString() {
		String str = "{";

		GroupElement elem = root;

		while (elem != null) {
			if (elem != root) {
				str += ", ";
			}

			str += elem.getElement();
			elem = elem.nextElement();
		}

		return str + "}";
	}

}
