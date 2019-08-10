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
 * Title: Protos role
 * </p>
 * 
 * <p>
 * Description: Holds a Protos role
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
public class ProtosRole {
	private String ID;
	private String name;
	private String description;
	private String workinstruction;
	private String organisation;
	private int available;
	private double cost;
	private HashSet metavalues; // ProtosMetaValues
	private boolean deactivated;

	public ProtosRole() {
		metavalues = new HashSet();
	}

	// added by Mariska Netjes
	public String getName() {
		return name;
	}

	// added by Mariska Netjes
	public String getID() {
		return ID;
	}

	// added by Mariska Netjes
	public int getAvailable() {
		return available;
	}

	/**
	 * Constructs a Role object out of a Node.
	 * 
	 * @param roleNode
	 *            Node The node that contains the Role.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node roleNode) {
		String msg = "";
		ID = roleNode.getAttributes().getNamedItem(ProtosString.Id)
				.getNodeValue();
		organisation = roleNode.getAttributes().getNamedItem(
				ProtosString.Organisation).getNodeValue();
		NodeList nodes = roleNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Name)) {
				name = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Description)) {
				description = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Workinstruction)) {
				workinstruction = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Available)) {
				available = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.Cost)) {
				cost = ProtosUtil.readDouble(node);
			} else if (node.getNodeName().equals(ProtosString.Metavalue)) {
				ProtosMetavalue metavalue = new ProtosMetavalue();
				msg += metavalue.readXMLExport(node);
				metavalues.add(metavalue);
			} else if (node.getNodeName().equals(ProtosString.Deactivated)) {
				deactivated = ProtosUtil.readBool(node);
			}
		}
		return msg;
	}

	/**
	 * Returns the Role object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Role object.
	 * @return String The Role object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag;
		xml += " " + ProtosString.Id + "=\"" + ID;
		xml += "\" " + ProtosString.Organisation + "=\"" + organisation;
		xml += "\">";
		{
			xml += ProtosUtil.writeString(ProtosString.Name, name);
			xml += ProtosUtil.writeInt(ProtosString.Available, available);
			xml += ProtosUtil.writeDouble(ProtosString.Cost, cost);
			xml += ProtosUtil
					.writeString(ProtosString.Description, description);
			xml += ProtosUtil.writeString(ProtosString.Workinstruction,
					workinstruction);
			for (Iterator it = metavalues.iterator(); it.hasNext();) {
				ProtosMetavalue metavalue = (ProtosMetavalue) it.next();
				xml += metavalue.writeXMLExport(ProtosString.Metavalue);
			}
			xml += ProtosUtil.writeBool(ProtosString.Deactivated, deactivated);
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
