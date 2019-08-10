package org.processmining.framework.models.orgmodel;

import java.util.*;
import java.io.IOException;
import java.io.Writer;

public class OrgEntity implements Cloneable {

	public static final String ORGENTITYTYPE_ORGUNIT = "OrgUnit";
	public static final String ORGENTITYTYPE_ROLE = "Role";
	public static final String ORGENTITYTYPE_MININGRESULT = "MinedGroup";
	public static final ArrayList<String> ORGENTITYTYPE_ARRAYLIST = new ArrayList<String>(
			Arrays.asList(new String[] { ORGENTITYTYPE_ORGUNIT,
					ORGENTITYTYPE_ROLE, ORGENTITYTYPE_MININGRESULT }));

	private String entityID = null;
	private String entityName = null;
	private String entityType = null;

	private ArrayList<String> superEntityIDs = null;
	private ArrayList<String> subEntityIDs = null;

	public OrgEntity() {
		superEntityIDs = new ArrayList<String>();
		subEntityIDs = new ArrayList<String>();
	}

	public OrgEntity(String id, String name) {
		this.entityID = id;
		if (name.length() > 0)
			this.entityName = name;
		else
			this.entityName = " ";
		superEntityIDs = new ArrayList<String>();
		subEntityIDs = new ArrayList<String>();
	}

	public OrgEntity(String id, String name, String type) {
		this.entityID = id;

		if (name.length() > 0)
			this.entityName = name;
		else
			this.entityName = " ";

		if (type.length() > 0)
			this.entityType = type;
		else
			this.entityType = "EntityType";
		superEntityIDs = new ArrayList<String>();
		subEntityIDs = new ArrayList<String>();
	}

	public String getID() {
		return entityID;
	}

	public void setID(String id) {
		if (id.length() > 0)
			this.entityID = id;
		else
			this.entityID = " ";
	}

	public String getName() {
		return entityName;
	}

	public void setName(String name) {
		if (name.length() > 0)
			this.entityName = name;
		else
			this.entityName = " ";
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String type) {
		if (entityType.length() > 0)
			this.entityType = type;
		else
			this.entityType = " ";
	}

	public ArrayList<String> getSuperEntityIDs() {
		return superEntityIDs;
	}

	public ArrayList<String> getSubEntityIDs() {
		return subEntityIDs;
	}

	public void addSuperEntityID(String entityID) {
		superEntityIDs.add(entityID);
	}

	public void addSubEntityID(String entityID) {
		subEntityIDs.add(entityID);
	}

	public void removeSuperEntityID(String entityID) {
		superEntityIDs.remove(entityID);
	}

	public void removeSubEntityID(String entityID) {
		subEntityIDs.remove(entityID);
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
		bw.write("<OrgEntity>\n<EntityID>" + getID() + "</EntityID>\n");
		bw.write("<EntityName>" + getName() + "</EntityName>\n");
		bw.write("<EntityType>" + getEntityType()
				+ "</EntityType>\n</OrgEntity>\n");
	}

	/**
	 * Export to OrgModel file.
	 * 
	 * @param bw
	 *            Writer
	 * @throws IOException
	 *             If writing fails
	 */
	public void writeToXML2(Writer bw) throws IOException {
		for (String key : subEntityIDs) {
			bw.write("<Implication>\n<Source>" + getID() + "</Source>\n");
			bw.write("<Target>" + key + "</Target>\n</Implication>\n");
		}
	}

	// //////////////// GENERAL PURPOSE METHODS
	// /////////////////////////////////
	/**
	 * Makes a deep copy of the object, i.e., reconstructs the OrgEntity Note
	 * that this method needs to be extended as soon as there are attributes
	 * added to the class which are not primitive or immutable.
	 * 
	 * @return the cloned object
	 */
	public Object clone() {
		OrgEntity o = null;
		o = new OrgEntity(this.entityID, this.entityName, this.entityType);
		return o;
	}

}
