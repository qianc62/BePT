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

package org.processmining.framework.models.yawl;

import java.io.*;
import java.util.*;

import org.processmining.framework.log.*;
import org.processmining.framework.models.*;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title: YAWL Decomposition
 * </p>
 * <p>
 * Description: Holds an imported YAWL Decomposition
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */

public class YAWLDecomposition extends ModelGraph {

	private HashMap<String, YAWLNode> nodes = new HashMap(); // Maps node names
	// to nodes
	private String id; // The id of the decomposition
	private boolean isRootNet; // Whether this decomposition is the root
	private String xsiType; // the xsi:type of the decomposition

	/**
	 * Create a new YAWL decomposition, given its name, whether it is the root,
	 * and its xsi:type.
	 * 
	 * @param id
	 *            The given name
	 * @param isRootNet
	 *            Wether it is the root ("true") or not (anything else)
	 * @param xsiType
	 *            The xsi:type
	 */
	public YAWLDecomposition(String id, String isRootNet, String xsiType) {
		super(id);
		init(id, isRootNet, xsiType);
	}

	public YAWLDecomposition(YAWLDecomposition decomposition) {
		super(decomposition.id);
		init(decomposition.id, decomposition.isRootNet ? "true" : "false",
				decomposition.xsiType);
	}

	private void init(String id, String isRootNet, String xsiType) {
		this.id = id;
		this.isRootNet = isRootNet.equals("true");
		this.xsiType = xsiType;
	}

	public String getID() {
		return id;
	}
	
	public void setID(String id) {
		this.id = id;
	}

	/**
	 * Returns whether root.
	 * 
	 * @return Whether root.
	 */
	public boolean isRoot() {
		return isRootNet;
	}

	public Collection<YAWLNode> getNodes() {
		return nodes.values();
	}

	/**
	 * Returns whether any normal edge exists form the first node to the second
	 * node.
	 * 
	 * @param fromNode
	 *            YAWLNode The given firs tnode.
	 * @param toNode
	 *            YAWLNode The given second node.
	 * @return boolean Returns true if any edge from the first to the second
	 *         node is a normal edge.
	 */
	public boolean hasNormalEdges(YAWLNode fromNode, YAWLNode toNode) {
		HashSet<YAWLEdge> edges = this.getEdgesBetween(fromNode, toNode);
		for (YAWLEdge edge : edges) {
			if (edge.isNormal()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds an input condition with given name.
	 * 
	 * @param name
	 *            The given name
	 */
	public YAWLCondition addInputCondition(String name) {
		YAWLCondition condition = new YAWLCondition(this, YAWLCondition.IN,
				name);
		nodes.put(name, condition);
		super.addVertex(condition);
		return condition;
	}

	/**
	 * Adds an output condition with given name.
	 * 
	 * @param name
	 *            The given name
	 */
	public YAWLCondition addOutputCondition(String name) {
		YAWLCondition condition = new YAWLCondition(this, YAWLCondition.OUT,
				name);
		nodes.put(name, condition);
		super.addVertex(condition);
		return condition;
	}

	/**
	 * Adds a (normal) condition with given name.
	 * 
	 * @param name
	 *            The given name
	 */
	public YAWLCondition addCondition(String name) {
		YAWLCondition condition = new YAWLCondition(this, YAWLCondition.NONE,
				name);
		nodes.put(name, condition);
		addVertex(condition);
		return condition;
	}

	public YAWLCondition addCondition(YAWLCondition condition) {
		YAWLCondition newCondition = new YAWLCondition(this, condition);
		if (nodes.containsKey(newCondition.getID())) {
			Message.add("Overwriting " + newCondition.getID());
		}
		nodes.put(newCondition.getID(), newCondition);
		addVertex(newCondition);
		return newCondition;
	}

	/**
	 * Adds a task with given name, join type, split type, and subdecomposition
	 * name.
	 * 
	 * @param name
	 *            The given name
	 * @param join
	 *            The given join type (and, xor, or)
	 * @param split
	 *            The given split type (and, xor, or)
	 * @param decomposesTo
	 *            The given subdecomposition name.
	 * @return the task created.
	 */
	public YAWLTask addTask(String name, String join, String split,
			String decomposesTo, LogEvent event) {
		int joinType = join.equals("and") ? YAWLTask.AND
				: join.equals("xor") ? YAWLTask.XOR
						: join.equals("or") ? YAWLTask.OR : YAWLTask.NONE;
		int splitType = split.equals("and") ? YAWLTask.AND : split
				.equals("xor") ? YAWLTask.XOR
				: split.equals("or") ? YAWLTask.OR : YAWLTask.NONE;
		YAWLTask task = new YAWLTask(this, joinType, splitType, decomposesTo,
				name, event);
		nodes.put(name, task);
		addVertex(task);
		return task;
	}

	public YAWLTask addTask(YAWLTask task) {
		YAWLTask newTask = new YAWLTask(this, task);
		nodes.put(newTask.getID(), newTask);
		addVertex(newTask);
		return newTask;
	}

	public void removeYawlNode(String name) {
		YAWLNode node = nodes.remove(name);
		removeVertex(node);
	}

	/**
	 * Adds an edge from the given source node to the given destination node,
	 * given whether it is a default flow, given its predicate and its ordering.
	 * 
	 * @param fromName
	 *            The name of the source node
	 * @param toName
	 *            The name of the destination node
	 * @param isDefaultFLow
	 *            Whether it is a default edge
	 * @param predicate
	 *            The given predicate
	 * @param ordering
	 *            The given predicate ordering
	 */
	public YAWLEdge addEdge(String fromName, String toName,
			boolean isDefaultFLow, String predicate, String ordering) {
		YAWLNode fromNode = (YAWLNode) nodes.get(fromName);
		YAWLNode toNode = (YAWLNode) nodes.get(toName);
		// nodes.remove(fromName);
		// nodes.remove(toName);
		YAWLEdge edge = new YAWLEdge(fromNode, toNode, isDefaultFLow,
				predicate, ordering); // Presence of extra parameters results in
		// normal edge
		addEdge(edge);
		// nodes.put(fromName, fromVertex);
		// nodes.put(toName, toVertex);
		return edge;
	}

	public YAWLEdge addEdge(YAWLNode fromNode, YAWLNode toNode, YAWLEdge edge) {
		YAWLEdge newEdge = new YAWLEdge(fromNode, toNode, edge);
		addEdge(newEdge);
		return newEdge;
	}

	/**
	 * Adds a reset edge from the given source node to the given destination
	 * node.
	 * 
	 * @param fromName
	 *            The name of the source node
	 * @param toName
	 *            The name of the destination node
	 */
	public void addResetEdge(String fromName, String toName) {
		YAWLNode fromNode = (YAWLNode) nodes.get(fromName);
		YAWLNode toNode = (YAWLNode) nodes.get(toName);
		// nodes.remove(fromName);
		// nodes.remove(toName);
		YAWLEdge edge = new YAWLEdge(fromNode, toNode); // Absence of extra
		// parameters result in
		// a reset edge
		addEdge(edge);
		// nodes.put(fromName, fromVertex);
		// nodes.put(toName, toVertex);
	}

	/**
	 * Wrties the decomposition to dot.
	 * 
	 * @param bw
	 *            The writer
	 * @throws IOException
	 *             If writing fails
	 */
	public void writeToDot(Writer bw, YAWLModel model) throws IOException {

		// First: set the proper attributes.
		// A YAWL task might have a decomposition. Only now, we know the entire
		// model and can we check for this.
		setDotAttributes();
		Iterator it = getVerticeList().iterator();
		while (it.hasNext()) {
			Object object = it.next();
			if (object instanceof YAWLTask) {
				((YAWLTask) object).setDotAttributes(model);
			}
		}

		// Second, have the ModelGraph write to dot.
		super.writeToDot(bw);
	}

	public void setDotAttributes() {
		setDotAttribute("ranksep", ".3");
		setDotAttribute("margin", "0.0,0.0");
		setDotAttribute("rankdir", "LR");

		setDotNodeAttribute("height", ".3");
		setDotNodeAttribute("width", ".3");
		setDotNodeAttribute("fontname", "Arial");
		setDotNodeAttribute("fontsize", "8");

		setDotEdgeAttribute("arrowsize", ".5");
		setDotEdgeAttribute("fontname", "Arial");
		setDotEdgeAttribute("fontsize", "8");
	}

	/**
	 * Export to YAWL file.
	 * 
	 * @return String The string to export for this YAWLDecompositon.
	 */
	public String writeToYAWL() {
		String s = "";
		s += "\t\t<decomposition\n";
		s += "\t\t\tid=\"" + id + "\"\n";
		if (isRootNet) {
			s += "\t\t\tisRootNet=\"true\"\n";
		}
		s += "\t\t\txsi:type=\"" + xsiType + "\"\n";
		s += "\t\t>\n";

		Iterator it = getVerticeList().iterator();
		if (it.hasNext()) {
			s += "\t\t\t<processControlElements>\n";
			for (int i = 0; i < 3; i++) {
				while (it.hasNext()) {
					Object object = it.next();
					if (object instanceof YAWLTask) {
						s += ((YAWLTask) object).writeToYAWL(i);
					} else if (object instanceof YAWLCondition) {
						s += ((YAWLCondition) object).writeToYAWL(i);
					}
				}
				it = getVerticeList().iterator();
			}
			s += "\t\t\t</processControlElements>\n";
		}

		s += "\t\t</decomposition>\n";
		return s;
	}

	/**
	 * Print key indicators of the YAWLDecomposition to the Test tab.
	 */
	public void Test() {
		int nofTasks = 0;
		int nofConditions = 0;
		int nofUnknowns = 0;
		for (YAWLNode node : getNodes()) {
			if (node instanceof YAWLTask) {
				nofTasks++;
			} else if (node instanceof YAWLCondition) {
				nofConditions++;
			} else {
				nofUnknowns++;
			}
		}
		Message.add("  <Decomposition id=\"" + id + "\" nofTasks=\"" + nofTasks
				+ "\" nofConditions=\"" + nofConditions + "\" nofUnknowns=\""
				+ nofUnknowns + "\" nofArcs=\"" + getEdges().size() + "\"/>",
				Message.TEST);

	}
}
