/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.hlprocess;

/**
 * ID class for high-level process. <br>
 * Any HLProcessElement has an ID associated that can be used to retrieve this
 * element from the HLProcess (i.e., the ID serves as a "soft link" that can be
 * stored by other HLProcess elements without storing the actual object. <br>
 * Furthermore, the clone methods of high-level process elements do not alter
 * their ID. This way, corresponding high-level elements can be found back in
 * similar or copied high-level processes. <br>
 * Note that these "soft links" within a single high-level process and the
 * possibility to retrieve corresponding high-level elements in a related
 * process are the actual purpose of the HLID class, and that global ID
 * uniqueness across all high-level processes is not guaranteed.
 */
public class HLID {

	private static long idCounter = System.currentTimeMillis();

	private long id;
	private String name;

	/**
	 * Creates a new ID from the ID counter.
	 * 
	 * @return the new ID
	 */
	private static synchronized long createUniqueID() {
		return idCounter++;
	}

	/**
	 * Generates a default name for given ID.
	 * 
	 * @param idNumber
	 *            the ID for which the ID string is to be generated
	 * @return the ID name
	 */
	protected static String generateDefaultName(long idNumber) {
		return "ID" + idNumber;
	}

	/**
	 * Constructs a high-level ID based on a new, automatically generated ID.
	 */
	public HLID() {
		this(null);
	}

	/**
	 * Constructs a high-level ID based on a given ID. <br>
	 * Explicit ID creation should only be used if the input ID network is
	 * consistent. Furthermore, explicit ID creation should not be mixed with
	 * automatic ID creation as otherwise uniqueness of IDs is not guaranteed.
	 * 
	 * @param aName
	 *            the given, explicit ID
	 */
	public HLID(String aName) {
		id = createUniqueID();
		if (aName != null) {
			name = aName;
		} else {
			name = generateDefaultName(id);
		}
	}

	/**
	 * Returns ID name of this ID object. <br>
	 * Note that when IDs are created with an explicitly given name, it cannot
	 * be guaranteed that the result of this method is unique.
	 * 
	 * @return the ID string for this ID
	 */
	public String getName() {
		return name;
	}

	/**
	 * Two IDs are considered equal if their ID strings are equal. <br>
	 * Note that even for explicitly created IDs there is a globally unique ID,
	 * which, however, is not used for comparison in this method.
	 */
	public boolean equals(Object o) {
		if (this == o) {
			// check object identity first
			return true;
		} else if ((o instanceof HLID) == false) {
			// check type (which includes check for null)
			return false;
		} else {
			// IDs must be equal
			return ((HLID) o).name.equals(name);
		}
	}

	/**
	 * Overridden to produce the same hash code for equal objects.
	 * 
	 * @return int The hash code calculated.
	 */
	public int hashCode() {
		return name.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}
}
