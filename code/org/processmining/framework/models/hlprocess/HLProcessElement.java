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
 * Base class for high-level process elements, such as high-level activities or
 * high-level resources. <br>
 * Handles common concepts among these high-level process elements, such as the
 * ID management.
 */
public abstract class HLProcessElement {

	/** the ID for this high-level process element */
	private HLID id;
	/** the high level process this element belongs to */
	protected HLProcess process;
	/** the name of this high-level process element */
	protected String name;

	/**
	 * Default constructor creating a new ID for this element. Must be called in
	 * deriving subclases.
	 * 
	 * @param aName
	 *            the name of this element
	 * @param aProc
	 *            the high level process it belongs to
	 */
	protected HLProcessElement(String aName, HLProcess aProc) {
		this(aName, aProc, new HLID());
	}

	/**
	 * Constructor for creating an element with a given ID. Needed to restore
	 * serialized high-level processes with a given ID structure. Should not be
	 * used when normally building up a high level process as might interfere
	 * with other, automatically generated, IDs.
	 * 
	 * @param aName
	 *            the name of this element
	 * @param aProc
	 *            the high level process it belongs to
	 * @param anId
	 *            the given ID for this element
	 */
	protected HLProcessElement(String aName, HLProcess aProc, HLID anId) {
		name = aName;
		process = aProc;
		id = anId;
	}

	/**
	 * Retrieves the ID for this high-level process element. <br>
	 * Can be used to reference this high-level process element without storing
	 * the explicit reference.
	 * 
	 * @return the ID for this high-level process element
	 */
	public HLID getID() {
		return id;
	}

	/**
	 * Retrieves the HLProcess object that is associated to this high-level
	 * process element.
	 * 
	 * @return the HLProcess object
	 */
	public HLProcess getHLProcess() {
		return process;
	}

	/**
	 * Retrieves the name of this high-level process element
	 * 
	 * @return the name of this high-level process element
	 */
	public String getName() {
		return name;
	}

	/**
	 * Specifies a new name for this high-level process element
	 * 
	 * @param aName
	 *            the new name for high-level process element
	 */
	public void setName(String aName) {
		name = aName;
	}

	/**
	 * High-level process elements are considered to be equal if their IDs are
	 * equal.
	 */
	public boolean equals(Object o) {
		if (this == o) {
			// check object identity first
			return true;
		} else if ((o instanceof HLActivity) == false) {
			// check type (which includes check for null)
			return false;
		} else {
			// IDs must be equal
			return id.equals(((HLActivity) o).getID());
		}
	}

	/**
	 * Overridden to produce the same hash code for equal objects.
	 * 
	 * @return the hash code based on the ID object.
	 */
	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * Overrides the toString() method by returning the name of this high-level
	 * process element.
	 * 
	 * @return the name of this element
	 */
	public String toString() {
		return getName();
	}
}
