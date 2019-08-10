package org.processmining.framework.models.orgmodel;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashSet;

import org.processmining.framework.models.LogEventProvider;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Minseok Song
 * @version 1.0
 */
public class Task implements LogEventProvider, Cloneable {

	private LogEvent logModelElement; /* the associated log event */
	private String taskId;
	private String name;
	private String eventType;

	private HashSet<OrgEntity> orgEntities;

	public Task(String id, String name) {

		taskId = id;
		if (name.length() > 0)
			this.name = name;
		else
			this.name = " ";

		orgEntities = new HashSet<OrgEntity>();
	}

	public Task(String id, String name, String eventType) {

		taskId = id;
		if (name.length() > 0)
			this.name = name;
		else
			this.name = " ";

		if (eventType.length() > 0)
			this.eventType = eventType;
		else
			this.eventType = " ";

		orgEntities = new HashSet<OrgEntity>();
	}

	public String getID() {
		return taskId;
	}

	public void setID(String id) {
		if (id.length() > 0)
			this.taskId = id;
		else
			this.taskId = " ";
	}

	public String getName() {
		if (name.length() > 0)
			return name;
		else
			return " ";
	}

	public void setName(String name) {
		if (name.length() > 0)
			this.name = name;
		else
			this.name = " ";
	}

	public String getEventType() {
		if (eventType.length() > 0)
			return eventType;
		else
			return " ";
	}

	public void setEventType(String type) {
		if (type.length() > 0)
			this.eventType = type;
		else
			this.eventType = " ";
	}

	public HashSet<OrgEntity> getOrgEntities() {
		return orgEntities;
	}

	public String getEntityListString() {
		String listNames = new String();
		ArrayList<String> tempArray = new ArrayList<String>();

		for (Iterator<OrgEntity> iterator = orgEntities.iterator(); iterator
				.hasNext();) {
			OrgEntity orgEntity = iterator.next();
			tempArray.add(orgEntity.getID());
		}
		Collections.sort(tempArray);

		for (int i = 0; i < tempArray.size(); i++) {
			listNames += tempArray.get(i);
			if (i < tempArray.size() - 1)
				listNames += ":";
		}

		return listNames;
	}

	public String getEntityNameListString() {
		String listNames = new String();
		ArrayList<String> tempArray = new ArrayList<String>();

		for (Iterator<OrgEntity> iterator = orgEntities.iterator(); iterator
				.hasNext();) {
			OrgEntity orgEntity = iterator.next();
			tempArray.add(orgEntity.getName());
		}
		Collections.sort(tempArray);

		for (int i = 0; i < tempArray.size(); i++) {
			listNames += tempArray.get(i);
			if (i < tempArray.size() - 1)
				listNames += ":";
		}

		return listNames;
	}

	public void setOrgEntities(HashSet<OrgEntity> orgEntities) {
		this.orgEntities = orgEntities;
	}

	// methods for OrgEntity
	public void removeOrgEntity(OrgEntity orgEntity) {
		if (orgEntity == null) {
			Message.add((new StringBuilder(
					"Tried to remove a null-value to the Org Entity of Task"))
					.append(toString()).toString(), 3);
			return;
		}

		if (orgEntities.contains(orgEntity))
			orgEntities.remove(orgEntity);
	}

	public void addOrgEntity(OrgEntity orgEntity) {
		if (orgEntity == null) {
			Message.add((new StringBuilder(
					"Tried to add a null-value to the OrgEntity-list of Task"))
					.append(toString()).toString(), 3);
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

	public void addResourceToOrgEntity(Resource resource) {
		Iterator<OrgEntity> it = orgEntities.iterator();
		while (it.hasNext()) {
			OrgEntity orgEntity = it.next();
			resource.addOrgEntity(orgEntity);
		}
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
		bw.write("<Task>\n<TaskID>" + getID() + "</TaskID>\n");
		bw.write("<TaskName>" + getName() + "</TaskName>\n");
		bw.write("<EventType>" + getEventType() + "</EventType>\n");
		for (Iterator<OrgEntity> iterator = orgEntities.iterator(); iterator
				.hasNext();) {
			OrgEntity orgEntity = iterator.next();
			bw.write("<HasEntity>" + orgEntity.getID() + "</HasEntity>\n");
		}
		bw.write("</Task>\n");
	}

	/**
	 * Gets the log event belonging to this transition.
	 * 
	 * @return the log event associated
	 */
	public LogEvent getLogEvent() {
		return ((logModelElement == null) ? null : logModelElement);
	}

	/**
	 * Sets the log event belonging to this transition.
	 * 
	 * @param lme
	 *            the new log event associated
	 */
	public void setLogEvent(LogEvent lme) {
		logModelElement = lme;
	}

	// //////////////// GENERAL PURPOSE METHODS
	// /////////////////////////////////
	/**
	 * Returns the name of the task.
	 * 
	 * @return the name of the transition
	 */
	public String toString() {
		return name + "\\n" + eventType;
	}

	/**
	 * Makes a deep copy of the object, i.e., reconstructs the Task Note that
	 * this method needs to be extended as soon as there are attributes added to
	 * the class which are not primitive or immutable.
	 * 
	 * @return the cloned object
	 */
	public Object clone() {
		Task o = null;
		o = new Task(this.taskId, this.name, this.eventType);
		return o;
	}
}
