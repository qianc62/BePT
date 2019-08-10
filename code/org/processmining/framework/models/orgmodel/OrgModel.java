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

package org.processmining.framework.models.orgmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.Dot;

import att.grappa.Graph;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;

/**
 * <p>
 * Title: Organizational Model
 * </p>
 * <p>
 * Description: Holds an organizational model
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Minseok Song
 * @version 1.0
 */
public class OrgModel implements Cloneable {

	// known Organizational Entity type
	public static final String URI = "\"http://is.tm.tue.nl/staff/msong/OrgModel.xsd\"";
	public static final String NULLORGMODEL = "NOORGMODEL";

	protected HashMap<String, OrgEntity> orgEntities;
	protected HashMap<String, Resource> resources;
	protected HashMap<String, Task> tasks;

	public OrgModel() {
		orgEntities = new HashMap<String, OrgEntity>();
		resources = new HashMap<String, Resource>();
		tasks = new HashMap<String, Task>();
	}

	public OrgModel(HashMap<String, OrgEntity> orgEntities,
			HashMap<String, Resource> resources, HashMap<String, Task> tasks) {
		this.orgEntities = orgEntities;
		this.resources = resources;
		this.tasks = tasks;
	}

	public void setOrgEntities(HashMap<String, OrgEntity> orgEntities) {
		this.orgEntities = orgEntities;
	}

	public HashMap<String, OrgEntity> getOrgEntities() {
		return orgEntities;
	}

	public void setResources(HashMap<String, Resource> resources) {
		this.resources = resources;
	}

	public HashMap<String, Resource> getResources() {
		return resources;
	}

	public void setTasks(HashMap<String, Task> tasks) {
		this.tasks = tasks;
	}

	public HashMap<String, Task> getTasks() {
		return tasks;
	}

	// add by object
	public void addOrgEntity(OrgEntity orgEntity) {
		if (!orgEntities.containsKey(orgEntity.getID()))
			orgEntities.put(orgEntity.getID(), orgEntity);
	}

	public void addResource(Resource resource) {
		if (!resources.containsKey(resource.getID()))
			resources.put(resource.getID(), resource);
	}

	public void addTask(Task task) {
		if (!tasks.containsKey(task.getID()))
			tasks.put(task.getID(), task);
	}

	// get by string
	public OrgEntity getOrgEntity(String id) {
		if (orgEntities.containsKey(id))
			return (OrgEntity) orgEntities.get(id);
		else
			return null;
	}

	public Resource getResource(String id) {
		if (resources.containsKey(id))
			return (Resource) resources.get(id);
		else
			return null;
	}

	public Task getTask(String id) {
		if (tasks.containsKey(id))
			return (Task) tasks.get(id);
		else
			return null;
	}

	// if key is already in Org Entites HashMap
	public boolean changeOrgEntityID(String old_id, String new_id) {
		if (old_id.equals(new_id))
			return true;
		if (orgEntities.containsKey(old_id)) {
			if (!orgEntities.containsKey(new_id)) {
				OrgEntity tempEntity = (OrgEntity) orgEntities.get(old_id);
				tempEntity.setID(new_id);
				orgEntities.remove(old_id);
				orgEntities.put(new_id, tempEntity);
				return true;
			}
		}
		return false;
	}

	// if key is already in Resources HashMap
	public boolean changeResourceID(String old_id, String new_id) {
		if (old_id.equals(new_id))
			return true;
		if (resources.containsKey(old_id)) {
			if (!resources.containsKey(new_id)) {
				Resource tempRes = (Resource) resources.get(old_id);
				tempRes.setID(new_id);
				resources.remove(old_id);
				resources.put(new_id, tempRes);
				return true;
			}
		}
		return false;
	}

	// if key is already in Tasks HashMap
	public boolean changeTaskID(String old_id, String new_id) {
		if (old_id.equals(new_id))
			return true;
		if (tasks.containsKey(old_id)) {
			if (!tasks.containsKey(new_id)) {
				Task tempTask = (Task) tasks.get(old_id);
				tempTask.setID(new_id);
				tasks.remove(old_id);
				tasks.put(new_id, tempTask);
				return true;
			}
		}
		return false;
	}

	// change key when
	public boolean hasOrgEntity(String id) {
		return orgEntities.containsKey(id);
	}

	public boolean hasResource(String id) {
		return resources.containsKey(id);
	}

	public boolean hasTask(String id) {
		return tasks.containsKey(id);
	}

	public void removeOrgEntity(OrgEntity orgEntity) {
		orgEntities.remove(orgEntity.getID());

		String[] key = (String[]) resources.keySet().toArray(
				new String[resources.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			Resource tempResource = (Resource) resources.get(key[i]);
			tempResource.removeOrgEntity(orgEntity);
		}

		key = (String[]) tasks.keySet().toArray(
				new String[tasks.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			Task tempTask = (Task) tasks.get(key[i]);
			tempTask.removeOrgEntity(orgEntity);
		}
	}

	public void removeResource(Resource resource) {
		resources.remove(resource.getID());
	}

	public void removeTask(Task task) {
		tasks.remove(task.getID());
	}

	// get task by log event
	public Task getTask(LogEvent le) {
		String[] key = (String[]) tasks.keySet().toArray(
				new String[tasks.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			Task tempTask = (Task) tasks.get(key[i]);
			if (tempTask.getName().equals(le.getModelElementName())
					&& (tempTask.getEventType().equals(le.getEventType())))
				return tempTask;
		}
		return null;
	}

	// get task by name and event type
	public Task getTask(String name, String eventType) {
		String[] key = (String[]) tasks.keySet().toArray(
				new String[tasks.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			Task tempTask = (Task) tasks.get(key[i]);
			if (tempTask.getName().equals(name)
					&& (tempTask.getEventType().equals(eventType)))
				return tempTask;
		}
		return null;
	}

	// get org entity list
	public ArrayList<String> getOrgEntityStringList() {
		ArrayList<String> list;

		String[] key = (String[]) orgEntities.keySet().toArray(
				new String[orgEntities.keySet().size()]);

		list = new ArrayList<String>(Arrays.asList(key));

		Collections.sort(list);

		return list;
	}

	public ArrayList<OrgEntity> getOrgEntityList() {
		ArrayList<OrgEntity> list = new ArrayList<OrgEntity>();

		String[] key = (String[]) orgEntities.keySet().toArray(
				new String[orgEntities.keySet().size()]);

		for (int i = 0; i < key.length; i++) {
			list.add((OrgEntity) orgEntities.get(key[i]));
		}

		return list;
	}

	public List<String> getOrgEntityList(Resource res) {
		List<String> list = new ArrayList<String>();

		Iterator<String> iterator = orgEntities.keySet().iterator();

		while (iterator.hasNext()) {
			String key = iterator.next();
			OrgEntity orgunit = (OrgEntity) orgEntities.get(key);
			if (res.hasOrgEntity(orgunit))
				list.add(key);
		}

		return list;
	}

	// get org entity list by type
	public ArrayList<String> getOrgEntityStringList(String type) {
		ArrayList<String> list = new ArrayList<String>();

		String[] key = (String[]) orgEntities.keySet().toArray(
				new String[orgEntities.keySet().size()]);

		for (int i = 0; i < key.length; i++) {
			OrgEntity temp = (OrgEntity) orgEntities.get(key[i]);
			if (temp.getEntityType().equals(type)) {
				list.add(key[i]);
			}

		}

		Collections.sort(list);
		return list;
	}

	public ArrayList<OrgEntity> getOrgEntityList(String type) {
		ArrayList<OrgEntity> list = new ArrayList<OrgEntity>();

		String[] key = (String[]) orgEntities.keySet().toArray(
				new String[orgEntities.keySet().size()]);

		for (int i = 0; i < key.length; i++) {
			OrgEntity orgentity = (OrgEntity) orgEntities.get(key[i]);
			if (orgentity.getEntityType().equals(type))
				list.add((OrgEntity) orgEntities.get(key[i]));
		}

		return list;
	}

	public List<String> getOrgEntityList(Resource res, String type) {
		List<String> list = new ArrayList<String>();

		Iterator<String> iterator = orgEntities.keySet().iterator();

		while (iterator.hasNext()) {
			String key = iterator.next();
			OrgEntity orgentity = (OrgEntity) orgEntities.get(key);
			if (res != null && res.hasOrgEntity(orgentity) && orgentity != null
					&& orgentity.getEntityType() != null
					&& orgentity.getEntityType().equals(type))
				list.add(key);
		}

		return list;
	}

	public List<String> getOrgEntityList(String orgID, String type) {
		Resource res = getResource(orgID);

		return getOrgEntityList(res, type);
	}

	// get resource list
	public ArrayList<Resource> getResourceList() {
		ArrayList<Resource> list = new ArrayList<Resource>();

		String[] key = (String[]) resources.keySet().toArray(
				new String[resources.keySet().size()]);

		for (int i = 0; i < key.length; i++) {
			list.add((Resource) resources.get(key[i]));
		}

		return list;
	}

	public ArrayList<String> getResourceListInOrgEntity(OrgEntity orgEntity) {
		ArrayList<String> list = new ArrayList<String>();

		String[] key = (String[]) resources.keySet().toArray(
				new String[resources.keySet().size()]);

		for (int i = 0; i < key.length; i++) {
			if (((Resource) resources.get(key[i])).hasOrgEntity(orgEntity))
				list.add(key[i]);
		}
		Collections.sort(list);
		return list;
	}

	public ArrayList<String> getResourceList(OrgEntity orgEntity) {
		ArrayList<String> list = new ArrayList<String>();
		HashSet<String> list_temp = new HashSet<String>();

		String[] key = (String[]) resources.keySet().toArray(
				new String[resources.keySet().size()]);

		for (int i = 0; i < key.length; i++) {
			if (((Resource) resources.get(key[i])).hasOrgEntity(orgEntity))
				list_temp.add(key[i]);
		}
		// retrieve resources from its subOrgEntity
		if (orgEntity.getSubEntityIDs() == null)
			return list;
		for (String subEntityID : orgEntity.getSubEntityIDs()) {
			getSubResourceList(list_temp, getOrgEntity(subEntityID));
		}
		list = new ArrayList<String>(list_temp);
		Collections.sort(list);
		return list;
	}

	public HashSet<String> getSubResourceList(OrgEntity orgEntity) {
		HashSet<String> list = new HashSet<String>();

		for (String subEntityID : orgEntity.getSubEntityIDs()) {
			getSubResourceList(list, getOrgEntity(subEntityID));
		}

		return list;
	}

	public HashSet<String> getSubResourceList(HashSet<String> list,
			OrgEntity orgEntity) {

		String[] key = (String[]) resources.keySet().toArray(
				new String[resources.keySet().size()]);

		for (int i = 0; i < key.length; i++) {
			if (((Resource) resources.get(key[i])).hasOrgEntity(orgEntity))
				list.add(key[i]);
		}

		for (String subEntityID : orgEntity.getSubEntityIDs()) {
			getSubResourceList(list, getOrgEntity(subEntityID));
		}
		return list;
	}

	// get task list
	public ArrayList<Task> getTaskList() {
		ArrayList<Task> list = new ArrayList<Task>();

		String[] key = (String[]) tasks.keySet().toArray(
				new String[tasks.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			list.add((Task) tasks.get(key[i]));
		}

		return list;
	}

	/**
	 * Based on a new group definition, generate a new org model. When this
	 * function is called, original model and parameter has the same task sets.
	 * 
	 * @param listGroups
	 *            ArrayList
	 * 
	 */
	public void reallocateOrgModel(ArrayList<ArrayList<String>> listGroups,
			OrgModel originalModel) {

		HashMap<String, Task> originalTasks = originalModel.getTasks();
		HashMap<String, Resource> originalResources = originalModel
				.getResources();

		// remove existing orgEntities from resources
		String[] key_resource = (String[]) resources.keySet().toArray(
				new String[resources.keySet().size()]);
		for (int i = 0; i < key_resource.length; i++) {
			Resource tempRes = (Resource) resources.get(key_resource[i]);
			String[] key_orgEntity = (String[]) orgEntities.keySet().toArray(
					new String[orgEntities.keySet().size()]);
			for (int j = 0; j < key_orgEntity.length; j++) {
				OrgEntity tempEntity = (OrgEntity) orgEntities
						.get(key_orgEntity[j]);
				if (tempRes.hasOrgEntity(tempEntity)) {
					tempRes.removeOrgEntity(tempEntity);
				}
			}
		}

		// remove existing orgEntities from tasks
		String[] key_task = (String[]) tasks.keySet().toArray(
				new String[tasks.keySet().size()]);
		for (int i = 0; i < key_task.length; i++) {
			Task tempTask = (Task) tasks.get(key_task[i]);
			String[] key_orgEntity = (String[]) orgEntities.keySet().toArray(
					new String[orgEntities.keySet().size()]);
			for (int j = 0; j < key_orgEntity.length; j++) {
				OrgEntity tempEntity = (OrgEntity) orgEntities
						.get(key_orgEntity[j]);
				if (tempTask.hasOrgEntity(tempEntity)) {
					tempTask.removeOrgEntity(tempEntity);
				}
			}
		}

		orgEntities = null;
		orgEntities = new HashMap<String, OrgEntity>();

		// generate mined org models
		for (int i = 0; i < listGroups.size(); i++) {
			ArrayList<String> group = (ArrayList<String>) listGroups.get(i);

			OrgEntity tempEntity = new OrgEntity("minedGroup" + i, "minedGroup"
					+ i, OrgEntity.ORGENTITYTYPE_MININGRESULT);
			orgEntities.put(tempEntity.getID(), tempEntity);

			for (int j = 0; j < group.size(); j++) {
				Resource res = getResource((String) group.get(j));
				res.addOrgEntity(tempEntity);
			}
		}

		String[] originalTaskKey = (String[]) originalTasks.keySet().toArray(
				new String[originalTasks.keySet().size()]);
		for (int i = 0; i < originalTaskKey.length; i++) {
			Task existingTask = (Task) tasks.get(originalTaskKey[i]);
			Task originalTask = (Task) originalTasks.get(originalTaskKey[i]);

			HashSet<OrgEntity> originalOrgEntities = originalTask
					.getOrgEntities();
			String[] originalResourceKey = (String[]) originalResources
					.keySet().toArray(
							new String[originalResources.keySet().size()]);

			ArrayList<String> originalResourceList = new ArrayList<String>(); // list
			// for
			// resource
			// IDs
			// for
			// task
			for (int j = 0; j < originalResourceKey.length; j++) {
				Resource originalRes = originalModel
						.getResource(originalResourceKey[j]);

				Iterator<OrgEntity> it = originalOrgEntities.iterator(); // Actually
				// an
				// original
				// task
				// has
				// an
				// org
				// entity
				while (it.hasNext()) {
					OrgEntity originalOrgEntity = (OrgEntity) it.next();
					if (originalRes.hasOrgEntity(originalOrgEntity)) {
						originalResourceList.add(originalResourceKey[j]);
					}
				}
			}

			for (int j = 0; j < originalResourceList.size(); j++) {
				String[] orgEntitiesKey = (String[]) orgEntities.keySet()
						.toArray(new String[orgEntities.keySet().size()]);
				for (int k = 0; k < orgEntitiesKey.length; k++) {
					OrgEntity existingOrgEntity = (OrgEntity) orgEntities
							.get(orgEntitiesKey[k]);
					Resource tempRes = getResource(originalResourceList.get(j));
					if (tempRes.hasOrgEntity(existingOrgEntity)) {
						existingTask.addOrgEntity(existingOrgEntity);
					}
				}
			}
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

		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		bw.write("<OrgModel");
		bw.write("\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\t");
		bw.write("xsi:noNamespaceSchemaLocation=" + URI + ">\n");

		String[] key = (String[]) orgEntities.keySet().toArray(
				new String[orgEntities.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			((OrgEntity) orgEntities.get(key[i])).writeToXML(bw);
		}

		key = (String[]) resources.keySet().toArray(
				new String[resources.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			((Resource) resources.get(key[i])).writeToXML(bw);
		}

		// write hierarchy relationship using implication
		key = (String[]) orgEntities.keySet().toArray(
				new String[orgEntities.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			((OrgEntity) orgEntities.get(key[i])).writeToXML2(bw);
		}

		key = (String[]) tasks.keySet().toArray(
				new String[tasks.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			((Task) tasks.get(key[i])).writeToXML(bw);
		}
		bw.write("</OrgModel>");
	}

	/**
	 * Writes a DOT representation of this graph to the given
	 * <code>Writer</code>. This representation is used by the
	 * <code>getGrappaVisualization</code> method to generate the visualization.
	 * Note that this function should have a call to <code>
	 * nodeMapping.clear()</code>
	 * at the beginning and it should call
	 * <code>nodeMapping.put(new String(</code>nodeID<code>),</code>nodeObject
	 * <code>);</code> after writing a node to the dot file
	 * 
	 * @param bw
	 *            the DOT representation will be written using this
	 *            <code>Writer</code>
	 * @throws IOException
	 *             in case there is a problem with writing to <code>bw</code>
	 */
	public void writeToDot(Writer bw) throws IOException {

		HashMap<Object, String> nodelist = new HashMap<Object, String>();

		bw.write("digraph G {fontsize=\"8\"; remincross=true;");
		bw.write("fontname=\"Arial\";rankdir=\"TB\";\n");
		bw
				.write("edge [arrowsize=\"0.5\",decorate=true,fontname=\"Arial\",fontsize=\"8\"];\n");
		bw
				.write("node [height=\".1\",width=\".2\",fontname=\"Arial\",fontsize=\"8\",stype=\"filled\",fillcolor=\"palegoldenrod\"];\n");

		int nodenum = 0;
		String[] key;
		// write organizational entities
		key = (String[]) orgEntities.keySet().toArray(
				new String[orgEntities.keySet().size()]);
		for (String aKey : key) {
			nodelist.put((OrgEntity) orgEntities.get(aKey), String
					.valueOf("node" + nodenum));
			bw
					.write("node"
							+ nodenum++
							+ " [shape=\"house\",style=\"filled\",fillcolor=\"mediumturquoise\",label=\""
							+ ((OrgEntity) orgEntities.get(aKey)).getName()
							+ "\"];\n");
		}
		// for hierarchy among OrgEntity
		for (String aKey : key) {
			OrgEntity orgEntity = (OrgEntity) orgEntities.get(aKey);
			for (String bKey : orgEntity.getSubEntityIDs()) {
				OrgEntity orgEntity2 = (OrgEntity) orgEntities.get(bKey);
				bw.write((String) nodelist.get(orgEntity2) + " -> "
						+ (String) nodelist.get(orgEntity) + " [label=\" \"];");
			}
		}
		// write resources
		key = (String[]) resources.keySet().toArray(
				new String[resources.keySet().size()]);
		for (String aKey : key) {
			nodelist.put((Resource) resources.get(aKey), String.valueOf("node"
					+ nodenum));
			bw
					.write("node"
							+ nodenum++
							+ " [shape=\"ellipse\",style=\"filled\",fillcolor=\"lightpink\",label=\""
							+ ((Resource) resources.get(aKey)).getName()
							+ "\"];\n");
		}
		// write tasks
		key = (String[]) tasks.keySet().toArray(
				new String[tasks.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			nodelist.put((Task) tasks.get(key[i]), String.valueOf("node"
					+ nodenum));
			bw
					.write("node"
							+ nodenum++
							+ " [shape=\"box\",style=\"filled\",fillcolor=\"wheat\",label=\""
							+ ((Task) tasks.get(key[i])).getName() + "\\n"
							+ ((Task) tasks.get(key[i])).getEventType()
							+ "\"];\n");
		}
		// write links between resources and organizational entities
		key = (String[]) resources.keySet().toArray(
				new String[resources.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			Resource res = (Resource) resources.get(key[i]);

			String[] key2 = (String[]) orgEntities.keySet().toArray(
					new String[orgEntities.keySet().size()]);
			for (int j = 0; j < key2.length; j++) {
				OrgEntity orgEntity = (OrgEntity) orgEntities.get(key2[j]);

				if (res.hasOrgEntity(orgEntity))
					bw.write((String) nodelist.get(res) + " -> "
							+ (String) nodelist.get(orgEntity)
							+ " [label=\" \"];");
			}
		}

		// write links between tasks and organizational entities
		key = (String[]) tasks.keySet().toArray(
				new String[tasks.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			String[] key2 = (String[]) orgEntities.keySet().toArray(
					new String[orgEntities.keySet().size()]);
			Task task = (Task) tasks.get(key[i]);
			for (int j = 0; j < key2.length; j++) {
				OrgEntity orgEntity = (OrgEntity) orgEntities.get(key2[j]);

				if (task.hasOrgEntity(orgEntity))
					bw.write((String) nodelist.get(orgEntity) + " -> "
							+ (String) nodelist.get(task) + " [label=\" \"];");
			}
		}
		bw.write("}\n");
	}

	public JPanel getGraphPanel() {
		BufferedWriter bw;
		Graph graph;
		NumberFormat nf = NumberFormat.getInstance();
		File dotFile;

		nf.setMinimumFractionDigits(3);
		nf.setMaximumFractionDigits(3);
		try {
			// create temporary DOT file
			dotFile = File.createTempFile("pmt", ".dot");
			bw = new BufferedWriter(new FileWriter(dotFile, false));
			writeToDot(bw);
			bw.close();

			// execute dot and parse the output of dot to create a Graph
			graph = Dot.execute(dotFile.getAbsolutePath());

			// adjust some settings
			graph.setEditable(true);
			graph.setMenuable(true);
			graph.setErrorWriter(new PrintWriter(System.err, true));

			// create the visual component and return it
			GrappaPanel gp = new GrappaPanel(graph);
			gp.addGrappaListener(new GrappaAdapter());
			gp.setScaleToFit(true);

			return gp;
		} catch (Exception ex) {
			Message.add("Error while performing graph layout: "
					+ ex.getMessage(), Message.ERROR);
			return null;
		}
	}

	/**
	 * Writes a DOT representation of this graph to the given
	 * <code>Writer</code>. This representation is used by the
	 * <code>getGrappaVisualization</code> method to generate the visualization.
	 * Note that this function should have a call to <code>
	 * nodeMapping.clear()</code>
	 * at the beginning and it should call
	 * <code>nodeMapping.put(new String(</code>nodeID<code>),</code>nodeObject
	 * <code>);</code> after writing a node to the dot file
	 * 
	 * @param bw
	 *            the DOT representation will be written using this
	 *            <code>Writer</code>
	 * @throws IOException
	 *             in case there is a problem with writing to <code>bw</code>
	 */
	public void writeToDot(Writer bw, boolean bOrgEntity, boolean bResource,
			boolean bTask) throws IOException {

		HashMap<Object, String> nodelist = new HashMap<Object, String>();

		bw.write("digraph G {fontsize=\"8\"; remincross=true;");
		bw.write("fontname=\"Arial\";rankdir=\"BT\";\n");
		bw
				.write("edge [arrowsize=\"0.5\",decorate=true,fontname=\"Arial\",fontsize=\"8\"];\n");
		bw
				.write("node [height=\".1\",width=\".2\",fontname=\"Arial\",fontsize=\"8\",stype=\"filled\",fillcolor=\"palegoldenrod\"];\n");

		int nodenum = 0;
		String[] key;
		if (bOrgEntity) {
			// for OrgEntity
			key = (String[]) orgEntities.keySet().toArray(
					new String[orgEntities.keySet().size()]);
			for (String aKey : key) {
				nodelist.put((OrgEntity) orgEntities.get(aKey), String
						.valueOf("node" + nodenum));
				bw
						.write("node"
								+ nodenum++
								+ " [shape=\"house\",style=\"filled\",fillcolor=\"mediumturquoise\",label=\""
								+ ((OrgEntity) orgEntities.get(aKey)).getName()
								+ "\"];\n");
			}
			// for hierarchy among OrgEntity
			for (String aKey : key) {
				OrgEntity orgEntity = (OrgEntity) orgEntities.get(aKey);
				for (String bKey : orgEntity.getSubEntityIDs()) {
					OrgEntity orgEntity2 = (OrgEntity) orgEntities.get(bKey);
					bw.write((String) nodelist.get(orgEntity2) + " -> "
							+ (String) nodelist.get(orgEntity)
							+ " [label=\" \"];");
				}
			}
		}
		if (bResource) {
			key = (String[]) resources.keySet().toArray(
					new String[resources.keySet().size()]);
			for (String aKey : key) {
				nodelist.put((Resource) resources.get(aKey), String
						.valueOf("node" + nodenum));
				bw
						.write("node"
								+ nodenum++
								+ " [shape=\"ellipse\",style=\"filled\",fillcolor=\"lightpink\",label=\""
								+ ((Resource) resources.get(aKey)).getName()
								+ "\"];\n");
			}
		}

		if (bTask) {
			key = (String[]) tasks.keySet().toArray(
					new String[tasks.keySet().size()]);
			for (String aKey : key) {
				nodelist.put((Task) tasks.get(aKey), String.valueOf("node"
						+ nodenum));
				bw
						.write("node"
								+ nodenum++
								+ " [shape=\"box\",style=\"filled\",fillcolor=\"wheat\",label=\""
								+ ((Task) tasks.get(aKey)).getName() + "\\n"
								+ ((Task) tasks.get(aKey)).getEventType()
								+ "\"];\n");
			}
		}
		if (bResource && bOrgEntity) {
			key = (String[]) resources.keySet().toArray(
					new String[resources.keySet().size()]);
			for (String aKey : key) {
				Resource res = (Resource) resources.get(aKey);

				String[] key2 = (String[]) orgEntities.keySet().toArray(
						new String[orgEntities.keySet().size()]);
				for (String bKey : key2) {
					OrgEntity orgEntity = (OrgEntity) orgEntities.get(bKey);

					if (res.hasOrgEntity(orgEntity))
						bw
								.write((String) nodelist.get(orgEntity)
										+ " -> "
										+ (String) nodelist.get(res)
										+ " [label=\" \",arrowtail=normal,arrowhead=none];");
				}
			}
		}
		if (bTask && bOrgEntity) {
			key = (String[]) tasks.keySet().toArray(
					new String[tasks.keySet().size()]);
			for (String aKey : key) {
				String[] key2 = (String[]) orgEntities.keySet().toArray(
						new String[orgEntities.keySet().size()]);
				Task task = (Task) tasks.get(aKey);
				for (String bKey : key2) {
					OrgEntity orgEntity = (OrgEntity) orgEntities.get(bKey);

					if (task.hasOrgEntity(orgEntity))
						bw
								.write((String) nodelist.get(task)
										+ " -> "
										+ (String) nodelist.get(orgEntity)
										+ " [label=\" \",arrowtail=none,arrowhead=none,color=gray,size=0.5];");
				}
			}
		}
		bw.write("}\n");
	}

	public JPanel getGraphPanel(boolean bOrgEntity, boolean bResource,
			boolean bTask) {
		BufferedWriter bw;
		Graph graph;
		NumberFormat nf = NumberFormat.getInstance();
		File dotFile;

		nf.setMinimumFractionDigits(3);
		nf.setMaximumFractionDigits(3);
		try {
			// create temporary DOT file
			dotFile = File.createTempFile("pmt", ".dot");
			bw = new BufferedWriter(new FileWriter(dotFile, false));
			writeToDot(bw, bOrgEntity, bResource, bTask);
			bw.close();

			// execute dot and parse the output of dot to create a Graph
			graph = Dot.execute(dotFile.getAbsolutePath());

			// adjust some settings
			graph.setEditable(true);
			graph.setMenuable(true);
			graph.setErrorWriter(new PrintWriter(System.err, true));

			// create the visual component and return it
			GrappaPanel gp = new GrappaPanel(graph);
			gp.addGrappaListener(new GrappaAdapter());
			gp.setScaleToFit(true);

			return gp;
		} catch (Exception ex) {
			Message.add("Error while performing graph layout: "
					+ ex.getMessage(), Message.ERROR);
			return null;
		}
	}

	public void writeToTestLog() {
		Message.add("<OrganizationalModel>", Message.TEST);
		Message.add("<ORGMODELSummary numberOfOriginators=\""
				+ resources.size() + "\">", Message.TEST);
		Message.add("<ORGMODELSummary numberOfOrgEntities=\""
				+ orgEntities.size() + "\">", Message.TEST);
		Message.add("<ORGMODELSummary numberOfTasks=\"" + tasks.size() + "\">",
				Message.TEST);

		// number of unconnected resources
		String[] key = (String[]) resources.keySet().toArray(
				new String[resources.keySet().size()]);
		int sum = 0;
		for (int i = 0; i < key.length; i++) {
			Resource tempResource = (Resource) resources.get(key[i]);
			sum += tempResource.getNumberOfOrgEntity();
		}
		Message.add(
				"<ORGMODELSummary numberOfLinkBetweenOriginatorAndOrgEntity=\""
						+ sum + "\">", Message.TEST);

		// number of unconnected tasks
		key = (String[]) tasks.keySet().toArray(
				new String[tasks.keySet().size()]);
		sum = 0;
		for (int i = 0; i < key.length; i++) {
			Task tempTask = (Task) tasks.get(key[i]);
			sum += tempTask.getNumberOfOrgEntity();
		}
		Message.add("<ORGMODELSummary numberOfUnconnectedTasks=\"" + sum
				+ "\">", Message.TEST);

		// number of unconnected tasks
		Message.add("</OrganizationalModel>", Message.TEST);
	}

	// //////////////// GENERAL PURPOSE METHODS
	// /////////////////////////////////

	/**
	 * Makes a deep copy of the object, i.e., reconstructs the OrgModel
	 * structure with cloned resources, orgentities, and tasks. Note that this
	 * method needs to be extended as soon as there are attributes added to the
	 * class which are not primitive or immutable.
	 * 
	 * @return the cloned object
	 */
	public Object clone() {

		OrgModel o = new OrgModel();
		o.orgEntities = new HashMap<String, OrgEntity>();
		o.resources = new HashMap<String, Resource>();
		;
		o.tasks = new HashMap<String, Task>();

		Iterator<String> it = orgEntities.keySet().iterator();

		// cloning organizational unit
		while (it.hasNext()) {
			String key = it.next();
			OrgEntity orgunit = (OrgEntity) (orgEntities.get(key));
			OrgEntity cronedOrgEntity = (OrgEntity) orgunit.clone();
			o.orgEntities.put(key, cronedOrgEntity);
		}

		// cloning resource
		it = resources.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Resource resource = (Resource) (resources.get(key));
			Resource cronedResource = (Resource) resource.clone();
			o.resources.put(key, cronedResource);
		}

		// cloning task
		it = tasks.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Task task = (Task) (tasks.get(key));
			Task cronedTask = (Task) task.clone();
			o.tasks.put(key, cronedTask);
		}

		// make relations between resource and org entity
		String[] key = (String[]) resources.keySet().toArray(
				new String[resources.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			Resource res = (Resource) resources.get(key[i]);
			Resource clonedRes = (Resource) o.getResource(key[i]);

			String[] key2 = (String[]) orgEntities.keySet().toArray(
					new String[orgEntities.keySet().size()]);
			for (int j = 0; j < key2.length; j++) {
				OrgEntity orgEntity = (OrgEntity) orgEntities.get(key2[j]);
				if (res.hasOrgEntity(orgEntity)) {
					OrgEntity clonedOrgEntity = (OrgEntity) o.orgEntities
							.get(key2[j]);
					clonedRes.addOrgEntity(clonedOrgEntity);
				}
			}
		}

		// makre relations between task and org entity
		key = (String[]) tasks.keySet().toArray(
				new String[tasks.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			String[] key2 = (String[]) orgEntities.keySet().toArray(
					new String[orgEntities.keySet().size()]);
			Task task = (Task) tasks.get(key[i]);
			Task clonedTask = (Task) o.getTask(key[i]);

			for (int j = 0; j < key2.length; j++) {
				OrgEntity orgEntity = (OrgEntity) orgEntities.get(key2[j]);
				if (task.hasOrgEntity(orgEntity)) {
					OrgEntity clonedOrgEntity = (OrgEntity) o.orgEntities
							.get(key2[j]);
					clonedTask.addOrgEntity(clonedOrgEntity);
				}
			}
		}

		return o;
	}
}
