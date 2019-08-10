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
 * Title: Protos activity data
 * </p>
 * 
 * <p>
 * Description: Holds Protos activity data
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
public class ProtosActivityData {
	private String dataObjectID;
	private boolean mandatory;
	private boolean created;
	private boolean deleted;
	private ProtosExpression simulation;

	public boolean isMandatory() {
		return mandatory;
	}

	public boolean isCreated() {
		return created;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public ProtosActivityData() {
		simulation = new ProtosExpression();
	}

	public String getDataObjectID() {
		return dataObjectID;
	}

	public ProtosExpression getSimulation() {
		return simulation;
	}

	/**
	 * Constructs an Activity Data object out of a "activitydata" Node.
	 * 
	 * @param activityDataNode
	 *            Node The "activitydata" node that contains the Activity Data.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node activityDataNode) {
		String msg = "";
		NodeList nodes = activityDataNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.DataObject)) {
				dataObjectID = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Mandatory)) {
				mandatory = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.Created)) {
				created = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.Deleted)) {
				deleted = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.Simulation)) {
				msg += simulation.readXMLExport(node);
			}
		}
		return msg;
	}

	/**
	 * Returns the Activity Data object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Activity Data object.
	 * @return String The Activity Data object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag + ">";
		{
			xml += ProtosUtil
					.writeString(ProtosString.DataObject, dataObjectID);
			xml += ProtosUtil.writeBool(ProtosString.Mandatory, mandatory);
			xml += ProtosUtil.writeBool(ProtosString.Created, created);
			xml += ProtosUtil.writeBool(ProtosString.Deleted, deleted);
			xml += simulation.writeXMLExport(ProtosString.Simulation);
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
