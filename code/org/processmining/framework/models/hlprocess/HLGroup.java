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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A group represents some organizational unit in the high level process. <br>
 * Resources can be part of multiple groups.
 * 
 * @see HLResource
 * @see HLActivity
 * @see HLProcess
 */
public class HLGroup extends HLProcessElement implements Cloneable {

	/** The IDs of the resources that are associated to this group. */
	private ArrayList<HLID> resources;

	/**
	 * Default Constructor.
	 * 
	 * @param name
	 *            the name of the group
	 * @param aProc
	 *            the high level process this group belongs to
	 */
	public HLGroup(String aName, HLProcess aProc) {
		this(aName, new ArrayList<HLID>(), aProc);
	}

	/**
	 * Constructor initializing the group with a given ID.
	 * <p>
	 * Can be used when importing organizational models with existing IDs.
	 * 
	 * @param name
	 *            the name of the group
	 * @param aProc
	 *            the high level process this group belongs to
	 */
	public HLGroup(String aName, HLProcess aProc, HLID id) {
		this(aName, new ArrayList<HLID>(), aProc, id);
	}

	/**
	 * Constructor for creating a group with a name and a set of resources.
	 * 
	 * @param name
	 *            String the name of the group
	 * @param res
	 *            HashSet the set of the resources for the group
	 * @param aProc
	 *            the high level process this activity belongs to
	 */
	public HLGroup(String name, ArrayList<HLID> res, HLProcess aProc) {
		super(name, aProc);
		process.groups.put(getID(), this);
		resources = res;
	}

	/**
	 * Constructor for creating a group with a name and a set of resources and a
	 * given ID.
	 * <p>
	 * Can be used when importing organizational models with existing IDs.
	 * 
	 * @param name
	 *            String the name of the group
	 * @param res
	 *            HashSet the set of the resources for the group
	 * @param aProc
	 *            the high level process this activity belongs to
	 */
	public HLGroup(String name, ArrayList<HLID> res, HLProcess aProc, HLID id) {
		super(name, aProc, id);
		process.groups.put(getID(), this);
		resources = res;
	}

	/**
	 * Adds a new resource and also registers this resource on the process
	 * level.
	 * 
	 * @param resource
	 *            the new resource that belongs to this group now
	 */
	public void addResource(HLResource resource) {
		process.addOrReplace(resource);
		resources.add(resource.getID());
	}

	/**
	 * Adds a new resource while it is assumed that the corresponding resource
	 * exists in the high level process.
	 * 
	 * @param resourceID
	 *            the ID of the resource that now belongs to this group
	 */
	public void addResource(HLID resourceID) {
		resources.add(resourceID);
	}

	/**
	 * Removes the specified resource from this group. Note that the resource
	 * remains in the high level process.
	 * 
	 * @param resourceID
	 *            the id of the resource that is now not part of this group
	 *            anymore
	 */
	public boolean removeResource(HLID resourceID) {
		boolean returnBoolean = false;
		returnBoolean = resources.remove(resourceID);
		return returnBoolean;
	}

	/**
	 * Retrieves all the resources that are associated to this group.
	 * 
	 * @return a list of Resource objects representing the people that have this
	 *         group. If no resources are defined for this group an empty set is
	 *         returned
	 */
	public List<HLResource> getResources() {
		ArrayList<HLResource> result = new ArrayList<HLResource>();
		Iterator<HLID> resourceIt = resources.iterator();
		while (resourceIt.hasNext()) {
			HLID resID = resourceIt.next();
			HLResource res = process.getResource(resID);
			result.add(res);
		}
		return result;
	}

	/**
	 * Returns the IDs of the resources that belong to this group.
	 * 
	 * @return the list of IDs of those resources that belong to this group
	 */
	public List<HLID> getResourceIDs() {
		return resources;
	}

	/**
	 * Determines whether the given resource belongs to this group of not.
	 * 
	 * @param resource
	 *            the resource for which we want to check membership in this
	 *            group
	 * @return <code>true</code> if resource belongs to this group,
	 *         <code>false</code> otherwise
	 */
	public boolean isInGroup(HLResource resource) {
		return resources.contains(resource.getID());
	}

	/**
	 * Makes a deep copy of this object while the ID remains the same. <br>
	 * Note that this method needs to be extended as soon as there are
	 * attributes added to the class which are not primitive or immutable.
	 */
	public Object clone() {
		HLGroup o = null;
		try {
			o = (HLGroup) super.clone();
			o.resources = (ArrayList<HLID>) this.resources.clone();
			return o;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	// TODO Anne: move to visualization classes (out of high level data
	// structure)
	/**
	 * Writes the highlevel group to dot. The general idea is that the relevant
	 * information of the highlevel group is written in a box and if needed can
	 * be connected to another node in the dot file. In that case the connection
	 * has to be an undirected line.
	 * 
	 * @param boxId
	 *            the identifier of the box (in the DOT file) in which the
	 *            relevant information of the highlevel group will be written.
	 * @param nodeId
	 *            the identifier of the node (in the DOT file) to which the box
	 *            that will be created has to be connected. <code>""</code> has
	 *            to be provided if the box that will be created does not need
	 *            to be connected to another node in the DOT file.
	 * @param addText
	 *            additional text that needs to be filled in at the beginning of
	 *            the box
	 * @param bw
	 *            Writer the BufferedWriter used to stream the data to the file
	 * @throws IOException
	 */
	public void writeDistributionToDot(String boxId, String nodeId,
			String addText, Writer bw) throws IOException {
		// write the box itself
		String label = "";
		label = label + addText + "\\n";
		label = label + getName() + "\\n";
		bw.write(boxId + " [shape=\"ellipse\", label=\"" + label + "\"];\n");
		// write the connection (if needed)
		if (!nodeId.equals("")) {
			bw.write(nodeId + " -> " + boxId + " [dir=none, style=dotted];\n");
		}

	}
}
