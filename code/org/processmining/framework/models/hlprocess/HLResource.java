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
 * A resource represents a resource entity (e.g., a person or machine) in the
 * high level process. <br>
 * Currently, a resource only has a name and an identity. In the future, more
 * characteristics can be added to, e.g., specify the working scheme of a person
 * (like working part-time or not).
 * 
 * @see HLGroup
 * @see HLProcess
 */
public class HLResource extends HLProcessElement implements Cloneable {

	/**
	 * Default Constructor.
	 * 
	 * @param name
	 *            the name of the resource
	 * @param aProc
	 *            the high level process this resource belongs to
	 */
	public HLResource(String aName, HLProcess aProc) {
		super(aName, aProc);
		process.resources.put(getID(), this);
		process.groups.get(process.anybodyHLID).addResource(getID());
	}

	/**
	 * Constructor initializing the resource with a given ID.
	 * <p>
	 * Can be used when importing organizational models with existing IDs.
	 * 
	 * @param name
	 *            the name of the resource
	 * @param aProc
	 *            the high level process this resource belongs to
	 */
	public HLResource(String aName, HLProcess aProc, HLID id) {
		super(aName, aProc, id);
		process.resources.put(getID(), this);
		process.groups.get(process.anybodyHLID).addResource(getID());
	}

	/**
	 * Makes a deep copy of this object while the ID remains the same. <br>
	 * Note that this method needs to be extended as soon as there are
	 * attributes added to the class which are not primitive or immutable.
	 */
	public Object clone() {
		HLResource o = null;
		try {
			o = (HLResource) super.clone();
			return o;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
