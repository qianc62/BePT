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
 * Title: Protos model
 * </p>
 * 
 * <p>
 * Description: Holds a Protos model
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class ProtosModel extends ModelGraph {
	private String name;
	private ProtosRoleGraph roleGraph;
	private HashSet<ProtosGroup> groups;
	private HashSet<ProtosDistribution> distributions;
	private HashSet<ProtosSubprocess> subprocesses;
	private ProtosMetaNames metaNames;
	private HashSet<ProtosMetavalue> metaValues;
	private ProtosModelOptions protosModelOptions;

	public ProtosModel() {
		super("");
		roleGraph = new ProtosRoleGraph();
		groups = new HashSet<ProtosGroup>();
		distributions = new HashSet<ProtosDistribution>();
		subprocesses = new HashSet<ProtosSubprocess>();
		metaNames = new ProtosMetaNames();
		metaValues = new HashSet<ProtosMetavalue>();
		protosModelOptions = new ProtosModelOptions();
	}

	public ProtosModel(String name) {
		super(name);
		this.name = name;
		roleGraph = new ProtosRoleGraph();
		groups = new HashSet<ProtosGroup>();
		distributions = new HashSet<ProtosDistribution>();
		subprocesses = new HashSet<ProtosSubprocess>();
		metaNames = new ProtosMetaNames();
		metaValues = new HashSet<ProtosMetavalue>();
		protosModelOptions = new ProtosModelOptions();
	}

	/**
	 * Returns the name of the Model.
	 * 
	 * @return String The name of the Model.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the set of subprocesses of this Model.
	 * 
	 * @return HashSet The set of subprocesses.
	 */
	public HashSet<ProtosSubprocess> getSubprocesses() {
		return subprocesses;
	}

	public ProtosSubprocess getRootSubprocess() {
		for (ProtosSubprocess process : getSubprocesses()) {
			if (process.isRoot()) {
				return process;
			}
		}
		return null;
	}

	/**
	 * Added by Mariska Netjes Returns the rolegraph of this Model.
	 * 
	 * @return ProtosRoleGraph The rolegraph.
	 */
	public ProtosRoleGraph getRoleGraph() {
		return roleGraph;
	}

	// added by Mariska
	public ProtosModelOptions getModelOptions() {
		return protosModelOptions;
	}

	// added by Mariska
	public ProtosSubprocess addSubprocess(ProtosSubprocess process) {
		subprocesses.add(process);
		return process;
	}

	public ProtosSubprocess addSubprocess(String name) {
		ProtosSubprocess process = new ProtosSubprocess(name);
		subprocesses.add(process);
		return process;
	}

	/**
	 * Constructs a Model object out of a Document.
	 * 
	 * @param doc
	 *            Document The document that contains the Model.
	 * @return String Any error message.
	 */
	public String readXMLExport(Document doc) {
		String msg = "";
		Node protosModelNode = doc.getFirstChild();
		if (!protosModelNode.getNodeName().equals(ProtosString.ProtosModel)) {
			msg += "protosmodel tag not found\n";
		} else {
			NodeList nodes = protosModelNode.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeName().equals(ProtosString.Name)) {
					name = ProtosUtil.readString(node);
					setName(name);
				} else if (node.getNodeName().equals(ProtosString.RoleGraph)) {
					msg += roleGraph.readXMLExport(node);
				} else if (node.getNodeName().equals(ProtosString.Group)) {
					ProtosGroup group = new ProtosGroup();
					msg += group.readXMLExport(node);
					groups.add(group);
				} else if (node.getNodeName().equals(ProtosString.Distribution)) {
					ProtosDistribution distribution = new ProtosDistribution();
					msg += distribution.readXMLExport(node);
					distributions.add(distribution);
				} else if (node.getNodeName().equals(ProtosString.SubProcess)) {
					ProtosSubprocess subprocess = new ProtosSubprocess();
					msg += subprocess.readXMLExport(node);
					subprocesses.add(subprocess);
				} else if (node.getNodeName().equals(ProtosString.MetaNames)) {
					msg += metaNames.readXMLExport(node);
				} else if (node.getNodeName().equals(ProtosString.Metavalue)) {
					ProtosMetavalue metavalue = new ProtosMetavalue();
					msg += metavalue.readXMLExport(node);
					metaValues.add(metavalue);
				} else if (node.getNodeName().equals(
						ProtosString.ProtosModelOptions)) {
					msg += protosModelOptions.readXMLExport(node);
				}
			}
		}
		return msg;
	}

	/**
	 * Returns the Model object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Model object.
	 * @return String The Model object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		Iterator it;
		String xml = "<" + tag + ">";
		{
			xml += ProtosUtil.writeString(ProtosString.Name, name);
			xml += roleGraph.writeXMLExport(ProtosString.RoleGraph);
			for (ProtosGroup group : groups) {
				xml += group.writeXMLExport(ProtosString.Group);
			}
			for (ProtosDistribution distribution : distributions) {
				xml += distribution.writeXMLExport(ProtosString.Distribution);
			}
			for (ProtosSubprocess subprocess : subprocesses) {
				xml += subprocess.writeXMLExport(ProtosString.SubProcess);
			}
			xml += metaNames.writeXMLExport(ProtosString.MetaNames);
			for (ProtosMetavalue metaValue : metaValues) {
				xml += metaValue.writeXMLExport(ProtosString.Metavalue);
			}
			xml += protosModelOptions
					.writeXMLExport(ProtosString.ProtosModelOptions);
		}
		xml += "</" + tag + ">";
		return xml;
	}

	/**
	 * Write the DOT representation of this Model to file.
	 * 
	 * @param bw
	 *            Writer The writer for this file.
	 * @throws IOException
	 *             Writing might fail.
	 */
	public void writeToDot(Writer bw) throws IOException {
		if (subprocesses.size() > 0) {
			ProtosSubprocess subprocess = subprocesses.iterator().next();
			subprocess.writeToDot(bw);
		}
	}
}
