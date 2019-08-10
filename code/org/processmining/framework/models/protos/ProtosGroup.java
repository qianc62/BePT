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
 * Title: Protos group
 * </p>
 * 
 * <p>
 * Description: Holds a Protos group
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
public class ProtosGroup {
	private String ID;
	private String name;
	private String description;
	private String workinstruction;
	private HashSet groupRoleIDs; // contains Strings (IDs of roles)
	private HashSet metaValues; // contains ProtosMetaValues
	private boolean deactivated;

	public ProtosGroup() {
		groupRoleIDs = new HashSet();
		metaValues = new HashSet();
	}

	/**
	 * Constructs a Group object out of a Node.
	 * 
	 * @param groupNode
	 *            Node The node that contains the Group.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node groupNode) {
		String msg = "";
		ID = groupNode.getAttributes().getNamedItem(ProtosString.Id)
				.getNodeValue();
		NodeList nodes = groupNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Name)) {
				name = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Description)) {
				description = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Workinstruction)) {
				workinstruction = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.GroupRole)) {
				String groupRoleID = ProtosUtil.readString(node);
				groupRoleIDs.add(groupRoleID);
			} else if (node.getNodeName().equals(ProtosString.Metavalue)) {
				ProtosMetavalue metaValue = new ProtosMetavalue();
				msg += metaValue.readXMLExport(node);
				metaValues.add(metaValue);
			} else if (node.getNodeName().equals(ProtosString.Deactivated)) {
				deactivated = node.equals(ProtosString.True);
			}
		}
		return msg;
	}

	/**
	 * Returns the Group object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Group object.
	 * @return String The Group object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag;
		xml += " " + ProtosString.Id + "=\"" + ID;
		xml += "\">";
		{
			xml += ProtosUtil.writeString(ProtosString.Name, name);
			xml += ProtosUtil
					.writeString(ProtosString.Description, description);
			xml += ProtosUtil.writeString(ProtosString.Workinstruction,
					workinstruction);
			for (Iterator it = groupRoleIDs.iterator(); it.hasNext();) {
				String groupRoleID = (String) it.next();
				xml += ProtosUtil.writeString(ProtosString.GroupRole,
						groupRoleID);
			}
			for (Iterator it = metaValues.iterator(); it.hasNext();) {
				ProtosMetavalue metavalue = (ProtosMetavalue) it.next();
				xml += metavalue.writeXMLExport(ProtosString.Metavalue);
			}
			xml += ProtosUtil.writeBool(ProtosString.Deactivated, deactivated);
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
