/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.orgmodel.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.processmining.framework.ui.Message;
import java.io.InputStream;
import org.processmining.framework.models.orgmodel.*;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogEvent;

/**
 * Reads a Organizational Model from a OMML (Organizational Model Markup
 * Language) file.
 * 
 * @author Minseok Song
 * @version 1.0
 */

public class OmmlReader {

	private static HashMap<String, OrgEntity> orgentities;
	private static HashMap<String, Resource> resources;
	private static HashMap<String, Task> tasks;

	public static OrgModel read(InputStream input) {
		orgentities = new HashMap<String, OrgEntity>();
		resources = new HashMap<String, Resource>();
		tasks = new HashMap<String, Task>();

		readFile(input);

		return new OrgModel(orgentities, resources, tasks);
	}

	public static OrgModel read(String fileName) {
		orgentities = new HashMap<String, OrgEntity>();
		resources = new HashMap<String, Resource>();
		tasks = new HashMap<String, Task>();

		readFile(fileName);

		return new OrgModel(orgentities, resources, tasks);
	}

	private static void readFile(InputStream input) {
		SAXBuilder builder = new SAXBuilder();

		try {
			Document doc = builder.build(input);
			Element root = doc.getRootElement();
			List children = root.getChildren();
			for (int i = 0; i < children.size(); i++) {
				Element element = (Element) children.get(i);
				if (element != null)
					if ("OrgEntity".equals(element.getName()))
						addOrgEntity(element);
					else if ("Task".equals(element.getName()))
						addTaskEntity(element);
					else if ("Resource".equals(element.getName()))
						addResource(element);
					else if ("Implication".equals(element.getName()))
						addImplication(element);
			}
		} catch (JDOMException e) {
			System.out.println("JDOMException");
		} catch (IOException e) {
			System.out.println("IOException");
		}

	}

	private static void readFile(String fileName) {
		SAXBuilder builder = new SAXBuilder();

		try {
			Document doc = builder.build(new File(fileName));
			Element root = doc.getRootElement();
			List children = root.getChildren();
			for (int i = 0; i < children.size(); i++) {
				Element element = (Element) children.get(i);
				if (element != null)
					if ("OrgEntity".equals(element.getName()))
						addOrgEntity(element);
					else if ("Task".equals(element.getName()))
						addTaskEntity(element);
					else if ("Resource".equals(element.getName()))
						addResource(element);
			}
		} catch (JDOMException e) {
			System.out.println("JDOMException");
		} catch (IOException e) {
			System.out.println("IOException");
		}
	}

	private static void addOrgEntity(Element element) throws IOException {
		String id = element.getChildText("EntityID");
		String name = element.getChildText("EntityName");
		String type = element.getChildText("EntityType");

		OrgEntity orgentity = new OrgEntity(id, name, type);
		if (!orgentities.containsKey(id))
			orgentities.put(id, orgentity);
	}

	private static void addImplication(Element element) throws IOException {
		String sourceID = element.getChildText("Source");
		String targetID = element.getChildText("Target");
		OrgEntity sourceOrgEntity = orgentities.get(sourceID);
		OrgEntity targetOrgEntity = orgentities.get(targetID);
		sourceOrgEntity.addSubEntityID(targetID);
		targetOrgEntity.addSuperEntityID(sourceID);
	}

	private static void addResource(Element element) throws IOException {
		String id = element.getChildText("ResourceID");
		String name = "";
		if (element.getChild("ResourceName") != null)
			name = element.getChildText("ResourceName");
		List qualitites = element.getChildren("HasEntity");
		Resource resource = new Resource(id, name);

		for (Iterator iterator = qualitites.iterator(); iterator.hasNext();) {
			Element hasQuality = (Element) iterator.next();
			String qualityID = hasQuality.getText();
			if (orgentities.containsKey(qualityID)) {
				resource.addOrgEntity((OrgEntity) orgentities.get(qualityID));
			} else {
				Message.add("OrgEntity (ID=\"" + qualityID
						+ "\") does not exists. The OrgEntity is ignored", 3);
			}
		}

		if (!resources.containsKey(id))
			resources.put(id, resource);
		else
			Message.add((new StringBuilder(" Duplicate entry for Resource <"))
					.append(id).append(", ").append(name).append("> Entry")
					.append("ignored.").toString(), 3);
	}

	private static void addTaskEntity(Element element) throws IOException {
		String id = element.getChildText("TaskID");
		String name = "";
		if (element.getChild("TaskName") != null)
			name = element.getChildText("TaskName");
		String eventtype = "";
		if (element.getChild("EventType") != null)
			eventtype = element.getChildText("EventType");

		List qualitites = element.getChildren("HasEntity");

		Task task = new Task(id, name, eventtype);

		LogEvent e = new LogEvent(name, eventtype);
		task.setLogEvent(e);

		for (Iterator iterator = qualitites.iterator(); iterator.hasNext();) {
			Element hasQuality = (Element) iterator.next();
			String qualityID = hasQuality.getText();
			if (orgentities.containsKey(qualityID)) {
				task.addOrgEntity((OrgEntity) orgentities.get(qualityID));
			} else {
				Message.add("OrgEntity (ID=\"" + qualityID
						+ "\") does not exists. The OrgEntity is ignored", 3);
			}
		}

		if (!tasks.containsKey(id))
			tasks.put(id, task);
		else
			Message.add((new StringBuilder(" Duplicate entry for Task <"))
					.append(id).append(", ").append(name).append("> Entry")
					.append("ignored.").toString(), 3);
	}
}
