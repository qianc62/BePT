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
 * Title: Protos role arc
 * </p>
 * 
 * <p>
 * Description: Holds a Protos role arc (from/to)
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
public class ProtosRoleArc {
	private String fromID;
	private String toID;

	public ProtosRoleArc() {
	}

	/**
	 * Constructs a Role Arc object out of a Node.
	 * 
	 * @param roleArcNode
	 *            Node The node that contains the Role Arc.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node roleArcNode) {
		String msg = "";
		fromID = roleArcNode.getAttributes().getNamedItem(ProtosString.From)
				.getNodeValue();
		toID = roleArcNode.getAttributes().getNamedItem(ProtosString.To)
				.getNodeValue();
		return msg;
	}

	/**
	 * Returns the Role Arc object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Role Arc object.
	 * @return String The Role Arc object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag;
		xml += " " + ProtosString.From + "=\"" + fromID;
		xml += "\" " + ProtosString.To + "=\"" + toID + "\"/>";
		return xml;
	}
}
