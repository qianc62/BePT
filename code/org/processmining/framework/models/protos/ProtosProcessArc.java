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
import org.processmining.framework.models.protos.*;

import org.w3c.dom.*;

/**
 * <p>
 * Title: Protos process arc
 * </p>
 * 
 * <p>
 * Description: Holds a Protos process arc
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
public class ProtosProcessArc {
	private String fromID;
	private String toID;
	private String type;
	private String label;
	private ProtosExpression condition;
	private String description;
	private String workinstruction;
	private double simulationFrequency;
	private HashSet dataIDs; // contains IDs
	private HashSet metaValues; // contains ProtosMetaValues

	private ModelGraphEdge edge = null;
	private ProtosSubprocess subprocess = null;

	public ProtosProcessArc(ProtosSubprocess subprocess) {
		this.subprocess = subprocess;
		dataIDs = new HashSet();
		metaValues = new HashSet();
	}

	public ProtosProcessArc(ProtosSubprocess subprocess, String fromID,
			String toID) {
		this.subprocess = subprocess;
		dataIDs = new HashSet();
		metaValues = new HashSet();

		this.fromID = fromID;
		this.toID = toID;
		type = "Action";
		label = "";
		description = "";
		workinstruction = "";
		simulationFrequency = 0.0;

		ProtosFlowElement from = subprocess.getFlowElement(fromID);
		ProtosFlowElement to = subprocess.getFlowElement(toID);
		if (from != null && to != null) {
			edge = new ModelGraphEdge(from.getVertex(), to.getVertex());
			subprocess.addEdge(edge);
		}
	}

	public ProtosSubprocess getSubprocess() {
		return subprocess;
	}

	public ModelGraphEdge getEdge() {
		return edge;
	}

	public String getSource() {
		return fromID;
	}

	public String getTarget() {
		return toID;
	}

	public double getSimulationFrequency() {
		return simulationFrequency;
	}

	public ProtosExpression getCondition() {
		return condition;
	}

	public boolean hasCondition() {
		if (condition != null) {
			return true;
		} else {
			return false;
		}
	}

	public String getLabel() {
		return label;
	}

	/**
	 * Constructs a Process Arc object out of a Node.
	 * 
	 * @param processArcNode
	 *            Node The node that contains the Process Arc.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node processArcNode) {
		String msg = "";
		fromID = processArcNode.getAttributes().getNamedItem(ProtosString.From)
				.getNodeValue();
		toID = processArcNode.getAttributes().getNamedItem(ProtosString.To)
				.getNodeValue();
		ProtosFlowElement from = subprocess.getFlowElement(fromID);
		ProtosFlowElement to = subprocess.getFlowElement(toID);
		if (from != null && to != null) {
			edge = new ModelGraphEdge(from.getVertex(), to.getVertex());
			subprocess.addEdge(edge);
		}
		NodeList nodes = processArcNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Type)) {
				type = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Label)) {
				label = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Condition)) {
				// ProtosExpression condition = new ProtosExpression(); is
				// changed in:
				condition = new ProtosExpression(); // by Mariska to make
				// condition work.
				msg += condition.readXMLExport(node);
			} else if (node.getNodeName().equals(ProtosString.Description)) {
				description = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Workinstruction)) {
				workinstruction = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.SimFrequency)) {
				simulationFrequency = ProtosUtil.readDouble(node);
			} else if (node.getNodeName().equals(ProtosString.ArcData)) {
				String dataID = ProtosUtil.readString(node);
				dataIDs.add(dataID);
			} else if (node.getNodeName().equals(ProtosString.Metavalue)) {
				ProtosMetavalue metavalue = new ProtosMetavalue();
				msg += metavalue.readXMLExport(node);
			}
		}
		return msg;
	}

	/**
	 * Returns the Process Arc object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Process Arc object.
	 * @return String The Process Arc object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag;
		xml += " " + ProtosString.From + "=\"" + fromID;
		xml += "\" " + ProtosString.To + "=\"" + toID + "\">";
		{
			xml += ProtosUtil.writeString(ProtosString.Type, type);
			xml += ProtosUtil.writeString(ProtosString.Label, label);
			if (condition != null) {
				xml += condition.writeXMLExport(ProtosString.Condition);
			}
			xml += ProtosUtil
					.writeString(ProtosString.Description, description);
			xml += ProtosUtil.writeString(ProtosString.Workinstruction,
					workinstruction);
			xml += ProtosUtil.writeDouble(ProtosString.SimFrequency,
					simulationFrequency);
			for (Iterator it = dataIDs.iterator(); it.hasNext();) {
				String dataID = (String) it.next();
				xml += ProtosUtil.writeString(ProtosString.ArcData, dataID);
			}
			for (Iterator it = metaValues.iterator(); it.hasNext();) {
				ProtosMetavalue metavalue = (ProtosMetavalue) it.next();
				xml += metavalue.writeXMLExport(ProtosString.Metavalue);
			}
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
