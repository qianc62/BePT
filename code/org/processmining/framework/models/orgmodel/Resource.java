package org.processmining.framework.models.orgmodel;

import java.util.*;
import org.processmining.framework.ui.Message;
import java.io.IOException;
import java.io.Writer;

public class Resource implements Cloneable {

	private String resourceID;
	private String name;
	private HashSet<OrgEntity> orgEntities;

	public Resource(String id, String name) {
		resourceID = id;
		if (name.length() > 0)
			this.name = name;
		else
			this.name = " ";
		orgEntities = new HashSet<OrgEntity>();
	}

	public String getID() {
		return resourceID;
	}

	public void setID(String id) {
		if (id.length() > 0)
			this.resourceID = id;
		else
			this.resourceID = " ";
	}

	public String getName() {
		if (name.length() > 0)
			return name;
		else
			return ".";
	}

	public void setName(String name) {
		if (name.length() > 0)
			this.name = name;
		else
			this.name = " ";
	}

	// methods for OrgEntity
	public void removeOrgEntity(OrgEntity orgEntity) {
		if (orgEntity == null) {
			Message
					.add(
							(new StringBuilder(
									"Tried to remove a null-value to the Org Entity of Resource"))
									.append(toString()).toString(), 3);
			return;
		}

		if (orgEntities.contains(orgEntity))
			orgEntities.remove(orgEntity);
	}

	public void addOrgEntity(OrgEntity orgEntity) {
		if (orgEntity == null) {
			Message
					.add(
							"Tried to add a null-value to the OrgEntity-list of Resource",
							4);
			// Message.add((new StringBuilder(
			// "Tried to add a null-value to the OrgEntity-list of Resource")).append(toString()).toString(),
			// 3);
			return;
		}
		if (!orgEntities.contains(orgEntity))
			orgEntities.add(orgEntity);
	}

	public boolean hasOrgEntity(OrgEntity orgEntity) {
		boolean bResult = false;

		if (orgEntity != null) {
			if (orgEntities.contains(orgEntity))
				bResult = true;
		}

		return bResult;
	}

	public int getNumberOfOrgEntity() {
		return orgEntities.size();
	}

	public String toString() {
		String s = (new StringBuilder(String.valueOf(resourceID))).append(", ")
				.append(name).append("\n").toString();
		for (Iterator<OrgEntity> iterator = orgEntities.iterator(); iterator
				.hasNext();) {
			OrgEntity orgEntity = iterator.next();
			s = (new StringBuilder(String.valueOf(s))).append("  ").append(
					orgEntity.toString()).toString();
		}

		return s;
	}

	/**
	 * Export to OrgModel file.
	 * 
	 * @param bw
	 *            Writer
	 * @throws IOException
	 *             If writing fails
	 */
	public void writeToXML(Writer bw) throws IOException {
		bw.write("<Resource>\n<ResourceID>" + getID() + "</ResourceID>\n");
		bw.write("<ResourceName>" + getName() + "</ResourceName>\n");

		for (Iterator<OrgEntity> iterator = orgEntities.iterator(); iterator
				.hasNext();) {
			OrgEntity orgEntity = (OrgEntity) iterator.next();
			bw.write("<HasEntity>" + orgEntity.getID() + "</HasEntity>\n");
		}

		bw.write("</Resource>\n");
	}

	// //////////////// GENERAL PURPOSE METHODS
	// /////////////////////////////////

	/**
	 * Makes a deep copy of the object, i.e., reconstructs the Resource Note
	 * that this method needs to be extended as soon as there are attributes
	 * added to the class which are not primitive or immutable.
	 * 
	 * @return the cloned object
	 */
	public Object clone() {
		Resource o = null;
		o = new Resource(this.resourceID, this.name);
		return o;
	}

}
