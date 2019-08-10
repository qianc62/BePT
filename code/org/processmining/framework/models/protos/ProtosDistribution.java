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
 * Title: Protos distribution
 * </p>
 * 
 * <p>
 * Description: Holds a Protos distribution
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
public class ProtosDistribution {
	private String ID;
	private String name;
	private String description;
	private String workinstruction;
	private HashSet distributionDataIDs; // contains Strings (IDs of data?)
	private HashSet metaValues; // contains ProtosMetaValues
	private boolean deactivated;

	public ProtosDistribution() {
		distributionDataIDs = new HashSet();
		metaValues = new HashSet();
	}

	/**
	 * Constructs a Distribution object out of a "distribution" Node.
	 * 
	 * @param distributionNode
	 *            Node The "distribution" node that contains the Distribution.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node distributionNode) {
		String msg = "";
		ID = distributionNode.getAttributes().getNamedItem(ProtosString.Id)
				.getNodeValue();
		NodeList nodes = distributionNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Name)) {
				name = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Description)) {
				description = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Workinstruction)) {
				workinstruction = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.DistributionData)) {
				String distributionDataID = ProtosUtil.readString(node);
				distributionDataIDs.add(distributionDataID);
			} else if (node.getNodeName().equals(ProtosString.Metavalue)) {
				ProtosMetavalue metavalue = new ProtosMetavalue();
				msg += metavalue.readXMLExport(node);
				metaValues.add(metavalue);
			} else if (node.getNodeName().equals(ProtosString.Deactivated)) {
				deactivated = ProtosUtil.readBool(node);
			}
		}
		return msg;
	}

	/**
	 * Returns the Distribution object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Distribution object.
	 * @return String The Distribution object in Protos XML Export format.
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
			for (Iterator it = distributionDataIDs.iterator(); it.hasNext();) {
				String distributionDataID = (String) it.next();
				xml += ProtosUtil.writeString(ProtosString.GroupRole,
						distributionDataID);
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
