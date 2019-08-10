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
 * Title: Protos meta name
 * </p>
 * 
 * <p>
 * Description: Holds a Protos meta name
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
public class ProtosMetaName {
	private String name;
	private String suffix;
	private String metaType;
	private HashSet enums; // contains Strings

	public ProtosMetaName() {
		enums = new HashSet();
	}

	/**
	 * Constructs a Meta Name object out of a Node.
	 * 
	 * @param metaNameNode
	 *            Node The node that contains the Meta Name.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node metaNameNode) {
		String msg = "";
		NodeList nodes = metaNameNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Name)) {
				name = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Suffix)) {
				suffix = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.MetaType)) {
				metaType = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Enum)) {
				String enm = ProtosUtil.readString(node);
				enums.add(enm);
			}
		}
		return msg;
	}

	/**
	 * Returns the Meta Name object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Meta Name object.
	 * @return String The Meta Name object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag + ">";
		{
			xml += ProtosUtil.writeString(ProtosString.Name, name);
			xml += ProtosUtil.writeString(ProtosString.Suffix, suffix);
			xml += ProtosUtil.writeString(ProtosString.MetaType, metaType);
			for (Iterator it = enums.iterator(); it.hasNext();) {
				String enm = (String) it.next();
				xml += ProtosUtil.writeString(ProtosString.Enum, enm);
			}
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
