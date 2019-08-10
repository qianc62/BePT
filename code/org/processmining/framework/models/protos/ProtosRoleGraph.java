/**
 * Constructs a Role Arc object out of a Node.
 * @param roleArcNode Node The node that contains the Role Arc.
 * @return String Any error message.
 */
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
import org.processmining.framework.models.protos.ProtosRoleArc;
import org.processmining.framework.models.protos.ProtosDrawing;

import org.w3c.dom.*;

/**
 * <p>
 * Title: Protos role graph
 * </p>
 * 
 * <p>
 * Description: Holds a Protos role graph
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
public class ProtosRoleGraph extends ModelGraph {
	private HashSet roles; // contains ProtosRoles
	private HashSet roleArcs; // contains ProtosRoleArcs
	private HashSet drawings; // contains ProtosDrawings

	public ProtosRoleGraph() {
		super("Protos role graph");
		roles = new HashSet();
		roleArcs = new HashSet();
		drawings = new HashSet();
	}

	/**
	 * Added by Mariska Netjes Returns the roles of this RoleGraph.
	 * 
	 * @return ProtosRoleGraph The rolegraph.
	 */
	public HashSet<ProtosRole> getRoles() {
		return roles;
	}

	/**
	 * Constructs a Role Graph object out of a Node.
	 * 
	 * @param roleGraphNode
	 *            Node The node that contains the Role Graph.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node roleGraphNode) {
		String msg = "";
		NodeList nodes = roleGraphNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equalsIgnoreCase(ProtosString.Role)) {
				ProtosRole role = new ProtosRole();
				msg += role.readXMLExport(node);
				roles.add(role);
			} else if (node.getNodeName()
					.equalsIgnoreCase(ProtosString.Rolearc)) {
				ProtosRoleArc roleArc = new ProtosRoleArc();
				msg += roleArc.readXMLExport(node);
				roleArcs.add(roleArc);
			} else if (node.getNodeName()
					.equalsIgnoreCase(ProtosString.Drawing)) {
				ProtosDrawing drawing = new ProtosDrawing();
				msg += drawing.readXMLExport(node);
				drawings.add(drawing);
			}
		}
		return msg;
	}

	/**
	 * Returns the Role Graph object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Role Graph object.
	 * @return String The Role Graph object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag + ">";
		{
			for (Iterator it = roles.iterator(); it.hasNext();) {
				ProtosRole role = (ProtosRole) it.next();
				xml += role.writeXMLExport(ProtosString.Role);
			}
			for (Iterator it = roleArcs.iterator(); it.hasNext();) {
				ProtosRoleArc roleArc = (ProtosRoleArc) it.next();
				xml += roleArc.writeXMLExport(ProtosString.Rolearc);
			}
			for (Iterator it = drawings.iterator(); it.hasNext();) {
				ProtosDrawing drawing = (ProtosDrawing) it.next();
				xml += drawing.writeXMLExport(ProtosString.Drawing);
			}
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
