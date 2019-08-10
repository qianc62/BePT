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

package org.processmining.framework.models.protos;

import java.io.*;
import java.util.*;

import org.processmining.framework.models.*;

import att.grappa.Edge;
import org.w3c.dom.*;

/**
 * <p>
 * Title: Protos subprocess
 * </p>
 * 
 * <p>
 * Description: Holds a Protos subprocess
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
 * @author Eric Verbeek
 * @version 1.0
 */
public class ProtosSubprocess extends ModelGraph {
	private String ID;
	private String name;
	private boolean export;
	private String description;
	private String workinstruction;
	private boolean manualLayout;
	private HashSet<ProtosFlowElement> flowElements;
	private HashSet<ProtosProcessArc> processArcs;
	private HashSet<ProtosDrawing> drawings;
	private HashSet<ProtosDataElement> dataElements;
	private HashSet<ProtosMetavalue> metaValues;

	public ProtosSubprocess() {
		super("");
		flowElements = new HashSet<ProtosFlowElement>();
		processArcs = new HashSet<ProtosProcessArc>();
		drawings = new HashSet<ProtosDrawing>();
		dataElements = new HashSet<ProtosDataElement>();
		metaValues = new HashSet<ProtosMetavalue>();
		setDotAttributes();
	}

	public ProtosSubprocess(String name) {
		super(name);
		ID = new String(name);
		this.name = new String(name);
		flowElements = new HashSet<ProtosFlowElement>();
		processArcs = new HashSet<ProtosProcessArc>();
		drawings = new HashSet<ProtosDrawing>();
		dataElements = new HashSet<ProtosDataElement>();
		metaValues = new HashSet<ProtosMetavalue>();
		setDotAttributes();
	}

	private void setDotAttributes() {
		setDotAttribute("ranksep", ".3");
		setDotAttribute("margin", "0.0,0.0");
		setDotAttribute("rankdir", "TB");
		setDotNodeAttribute("height", ".3");
		setDotNodeAttribute("width", ".3");
		setDotEdgeAttribute("arrowsize", ".5");
	}

	/**
	 * Return the flow element that is associated to the given vertex.
	 * 
	 * @param vertex
	 *            The given vertex.
	 * @return The flow element associated with the given vertex, null if no
	 *         flow element associated to it.
	 */
	public ProtosFlowElement getFlowElement(ModelGraphVertex vertex) {
		/*
		 * For the time being, iterate over all flow elements and check whether
		 * the vertex matches. Better might be to change flowElements into a
		 * HashMap<ModelGraphVertex, ProtosFlowElement>, which would make the
		 * lookup more straightforward. However, this would require some
		 * additional changes. Let's first see whether this method is
		 * sufficient, and worry about performance later on.
		 */
		for (ProtosFlowElement flowElement : flowElements) {
			if (flowElement.getVertex() == vertex) {
				/*
				 * Vertex matches. Return this flow element.
				 */
				return flowElement;
			}
		}
		/*
		 * Vertex not found. Return null.
		 */
		return null;
	}

	/**
	 * Added by Mariska Netjes Return the protos process arc that is associated
	 * to the given edge.
	 * 
	 * @param edge
	 *            The given edge.
	 * @return The protos process arc associated with the given edge, null if no
	 *         arc associated to it.
	 */
	public ProtosProcessArc getArc(ModelGraphEdge edge) {

		for (ProtosProcessArc arc : processArcs) {
			if (arc.getEdge() == edge) {
				/*
				 * Edge matches. Return this process arc.
				 */
				return arc;
			}
		}
		/*
		 * Edge not found. Return null.
		 */
		return null;
	}

	/**
	 * Returns the name of this Subprocess.
	 * 
	 * @return String The name of this Subprocess.
	 */
	public String getName() {
		return name;
	}

	public HashSet<ProtosDataElement> getDataElements() {
		return dataElements;
	}

	/**
	 * Returns the set of activities of this Subprocess.
	 * 
	 * @return HashSet The set of activities of this Subprocess.
	 */
	public HashSet<ProtosFlowElement> getActivities() {
		HashSet<ProtosFlowElement> activities = new HashSet<ProtosFlowElement>();
		for (ProtosFlowElement flowElement : flowElements) {
			if (flowElement.isActivity()) {
				activities.add(flowElement);
			}
		}
		return activities;
	}

	public ProtosFlowElement addActivity(String ID, String name) {
		ProtosFlowElement activity = new ProtosFlowElement(this, ID, name,
				ProtosFlowElement.FlowElementBasicActivity);
		flowElements.add(activity);
		return activity;
	}

	// added by Mariska Netjes
	public ProtosFlowElement addActivity(ProtosFlowElement activity) {
		flowElements.add(activity);
		return activity;
	}

	public void removeActivity(ProtosFlowElement act) {
		if (act.isActivity()) {
			flowElements.remove(act);
		}
	}

	public void setStartEndActivity(String ID, boolean isStart) {
		ProtosFlowElement element = getFlowElement(ID);
		if (element != null) {
			element.setStartEndActivity(isStart);
		}
	}

	public HashSet<ProtosFlowElement> getStatuses() {
		HashSet<ProtosFlowElement> statuses = new HashSet<ProtosFlowElement>();
		for (ProtosFlowElement flowElement : flowElements) {
			if (flowElement.isStatus()) {
				statuses.add(flowElement);
			}
		}
		return statuses;
	}

	public ProtosFlowElement addStatus(String ID, String name) {
		ProtosFlowElement status = new ProtosFlowElement(this, ID, name,
				ProtosFlowElement.FlowElementStatus);
		flowElements.add(status);
		return status;
	}

	/**
	 * Added by Mariska Netjes
	 */
	public void removeStatus(ProtosFlowElement status) {
		if (status.isStatus()) {
			flowElements.remove(status);
		}
	}

	public HashSet<ProtosProcessArc> getArcs() {
		HashSet<ProtosProcessArc> arcs = new HashSet<ProtosProcessArc>();
		for (ProtosProcessArc arc : processArcs) {
			ProtosProcessArc processArc = (ProtosProcessArc) arc;
			ProtosFlowElement source = getFlowElement(processArc.getSource());
			ProtosFlowElement target = getFlowElement(processArc.getTarget());
			if (source.isActivity() && target.isStatus()) {
				arcs.add(processArc);
			} else if (source.isStatus() && target.isActivity()) {
				arcs.add(processArc);
			} else if (source.isActivity() && target.isActivity()) {
				arcs.add(processArc);
			}
		}
		return arcs;
	}

	public ProtosProcessArc addArc(String fromID, String toID) {
		ProtosProcessArc arc = new ProtosProcessArc(this, fromID, toID);
		processArcs.add(arc);
		return arc;
	}

	public void removeArc(ProtosProcessArc arc) {
		processArcs.remove(arc);
	}

	/**
	 * Added by Mariska Netjes Returns the set of flowElements of this
	 * Subprocess.
	 * 
	 * @return HashSet The set of flowElements of this Subprocess.
	 */
	public HashSet<ProtosFlowElement> getFlowElements() {
		return flowElements;
	}

	/**
	 * Returns the flow element with given ID.
	 * 
	 * @param ID
	 *            String The given ID.
	 * @return ProtosFlowElement The flow element with that ID, null if not
	 *         found.
	 */
	public ProtosFlowElement getFlowElement(String ID) {
		for (ProtosFlowElement flowElement : flowElements) {
			if (ID.equals(flowElement.getID())) {
				return flowElement;
			}
		}
		return null;
	}

	/**
	 * Constructs a Subprocess object out of a Node.
	 * 
	 * @param subprocessNode
	 *            Node The node that contains the Statistical Subprocess.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node subprocessNode) {
		String msg = "";
		ID = subprocessNode.getAttributes().getNamedItem(ProtosString.Id)
				.getNodeValue();
		NodeList nodes = subprocessNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Name)) {
				name = ProtosUtil.readString(node);
				setName(name);
			} else if (node.getNodeName().equals(ProtosString.Export)) {
				export = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.Description)) {
				description = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Workinstruction)) {
				workinstruction = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.ManualLayout)) {
				manualLayout = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.ProcessFlow)) {
				NodeList subNodes = node.getChildNodes();
				for (int j = 0; j < subNodes.getLength(); j++) {
					Node subNode = subNodes.item(j);
					if (subNode.getNodeName().equals(ProtosString.FlowElement)) {
						ProtosFlowElement flowElement = new ProtosFlowElement(
								this);
						msg += flowElement.readXMLExport(subNode);
						flowElements.add(flowElement);
					} else if (subNode.getNodeName().equals(
							ProtosString.ProcessArc)) {
						ProtosProcessArc processArc = new ProtosProcessArc(this);
						msg += processArc.readXMLExport(subNode);
						processArcs.add(processArc);
					} else if (subNode.getNodeName().equals(
							ProtosString.Drawing)) {
						ProtosDrawing drawing = new ProtosDrawing();
						msg += drawing.readXMLExport(subNode);
						drawings.add(drawing);
					}
				}
			} else if (node.getNodeName().equals(ProtosString.Data)) {
				NodeList subNodes = node.getChildNodes();
				for (int j = 0; j < subNodes.getLength(); j++) {
					Node subNode = subNodes.item(j);
					if (subNode.getNodeName().equals(ProtosString.DataElement)) {
						ProtosDataElement dataElement = new ProtosDataElement();
						msg += dataElement.readXMLExport(subNode);
						dataElements.add(dataElement);
					}
				}
			} else if (node.getNodeName().equals(ProtosString.Metavalue)) {
				ProtosMetavalue metavalue = new ProtosMetavalue();
				msg += metavalue.readXMLExport(node);
				metaValues.add(metavalue);
			}
		}
		return msg;
	}

	/**
	 * Returns the Subprocess object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Subprocess object.
	 * @return String The Subprocess object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag;
		xml += " " + ProtosString.Id + "=\"" + ID;
		xml += "\">";
		{
			xml += ProtosUtil.writeString(ProtosString.Name, name);
			xml += ProtosUtil.writeBool(ProtosString.Export, export);
			xml += ProtosUtil
					.writeBool(ProtosString.ManualLayout, manualLayout);
			xml += ProtosUtil
					.writeString(ProtosString.Description, description);
			xml += ProtosUtil.writeString(ProtosString.Workinstruction,
					workinstruction);
			xml += "<" + ProtosString.ProcessFlow + ">";
			{
				for (ProtosFlowElement flowElement : flowElements) {
					xml += flowElement.writeXMLExport(ProtosString.FlowElement);
				}
				for (ProtosProcessArc processArc : processArcs) {
					xml += processArc.writeXMLExport(ProtosString.ProcessArc);
				}
				for (ProtosDrawing drawing : drawings) {
					xml += drawing.writeXMLExport(ProtosString.Drawing);
				}
			}
			xml += "</" + ProtosString.ProcessFlow + ">";
			xml += "<" + ProtosString.Data + ">";
			{
				for (ProtosDataElement dataElement : dataElements) {
					xml += dataElement.writeXMLExport(ProtosString.DataElement);
				}
			}
			xml += "</" + ProtosString.Data + ">";
			for (ProtosMetavalue metavalue : metaValues) {
				xml += metavalue.writeXMLExport(ProtosString.Metavalue);
			}
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
