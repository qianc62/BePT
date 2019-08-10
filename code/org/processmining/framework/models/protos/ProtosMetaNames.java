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
 * Title: Protos meta names
 * </p>
 * 
 * <p>
 * Description: Hodls Protos meta names
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
public class ProtosMetaNames {
	// All HashSets contain ProtosMetaNames, that is, objects of the class
	// ProtosMetaName.
	private HashSet processmodels;
	private HashSet arcs;
	private HashSet activities;
	private HashSet triggers;
	private HashSet subprocesses;
	private HashSet archives;
	private HashSet documents;
	private HashSet folders;
	private HashSet scalars;
	private HashSet structs;
	private HashSet applications;
	private HashSet roles;
	private HashSet distributions;
	private HashSet statuses;
	private HashSet groups;

	public ProtosMetaNames() {
		processmodels = new HashSet();
		arcs = new HashSet();
		activities = new HashSet();
		triggers = new HashSet();
		subprocesses = new HashSet();
		archives = new HashSet();
		documents = new HashSet();
		folders = new HashSet();
		scalars = new HashSet();
		structs = new HashSet();
		applications = new HashSet();
		roles = new HashSet();
		distributions = new HashSet();
		statuses = new HashSet();
		groups = new HashSet();
	}

	/**
	 * Constructs a Meta Names object out of a Node.
	 * 
	 * @param metaNamesNode
	 *            Node The node that contains the Meta Names.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node metaNamesNode) {
		String msg = "";
		NodeList nodes = metaNamesNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.ProcessModel)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				processmodels.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.Arc)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				arcs.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.Activity)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				activities.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.Trigger)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				triggers.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.SubProcess)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				subprocesses.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.Archive)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				archives.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.Document)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				documents.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.Folder)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				folders.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.Scalar)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				scalars.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.Struct)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				structs.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.Application)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				applications.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.Role)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				roles.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.Distribution)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				distributions.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.Status)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				statuses.add(metaName);
			} else if (node.getNodeName().equals(ProtosString.Group)) {
				ProtosMetaName metaName = new ProtosMetaName();
				msg += metaName.readXMLExport(node);
				groups.add(metaName);
			}
		}
		return msg;
	}

	/**
	 * Returns the Meta Names object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Meta Names object.
	 * @return String The Meta Names object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag + ">";
		{
			for (Iterator it = processmodels.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.ProcessModel);
			}
			for (Iterator it = arcs.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.Arc);
			}
			for (Iterator it = activities.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.Activity);
			}
			for (Iterator it = triggers.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.Trigger);
			}
			for (Iterator it = subprocesses.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.SubProcess);
			}
			for (Iterator it = archives.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.Archive);
			}
			for (Iterator it = documents.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.Document);
			}
			for (Iterator it = folders.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.Folder);
			}
			for (Iterator it = scalars.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.Scalar);
			}
			for (Iterator it = structs.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.Struct);
			}
			for (Iterator it = applications.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.Application);
			}
			for (Iterator it = roles.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.Role);
			}
			for (Iterator it = distributions.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.Distribution);
			}
			for (Iterator it = statuses.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.Status);
			}
			for (Iterator it = groups.iterator(); it.hasNext();) {
				ProtosMetaName metaName = (ProtosMetaName) it.next();
				xml += metaName.writeXMLExport(ProtosString.Group);
			}
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
