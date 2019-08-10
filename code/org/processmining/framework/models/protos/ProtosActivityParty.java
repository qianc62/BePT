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
 * Title: Protos activity party
 * </p>
 * 
 * <p>
 * Description: Holds a Protos activity data
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
public class ProtosActivityParty {
	private String rolegroupobjectID;
	private boolean consulted;
	private boolean informed;

	public ProtosActivityParty() {
	}

	/**
	 * Constructs an Activity Party object out of a "activityparty" Node.
	 * 
	 * @param activityPartyNode
	 *            Node The "activityparty" node that contains the Activity
	 *            Party.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node activityPartyNode) {
		String msg = "";
		NodeList nodes = activityPartyNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.RoleGroupObject)) {
				rolegroupobjectID = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Consulted)) {
				consulted = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.Informed)) {
				informed = ProtosUtil.readBool(node);
			}
		}
		return msg;
	}

	/**
	 * Returns the Activity Party object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Activity Party object.
	 * @return String The Activity Party object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag + ">";
		{
			xml += ProtosUtil.writeString(ProtosString.RoleGroupObject,
					rolegroupobjectID);
			xml += ProtosUtil.writeBool(ProtosString.Consulted, consulted);
			xml += ProtosUtil.writeBool(ProtosString.Informed, informed);
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
