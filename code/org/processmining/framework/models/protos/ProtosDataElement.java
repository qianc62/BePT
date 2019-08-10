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
 * Title: Protos data element
 * </p>
 * 
 * <p>
 * Description: Holds a Protos data element
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
public class ProtosDataElement {
	static int DataElementScalar = 0; // Mariska: regular Protos Data-element
	static int DataElementDocument = 1;
	static int DataElementFolder = 2;
	static int DataElementStruct = 3;
	static int DataElementApplication = 4;

	private int type; // One of the values above

	private String ID;
	private String name;
	private String dataType; // Scalar, Document
	private int length; // Scalar
	private String description;
	private String workinstruction;
	private String label; // Scalar, Document, Folder, Struct, Application
	private String format; // Scalar
	private String yesLabel; // Scalar
	private String noLabel; // Scalar
	private String switchValues; // Scalar
	private HashSet parties; // Scalar, Document, Folder, Struct, Application
	// (contains IDs)
	private HashSet metaValues; // Scalar, Document, Folder, Struct, Application
	// (contains MetaValues)
	private boolean deactivated; // Scalar, Document, Folder, Struct,
	// Application
	private String iospec; // Document
	private HashSet datas; // Document, Folder, Struct, Application (contains
	// IDs)
	private String standardLetter; // Document
	private boolean automatic; // Application
	private String comScript; // Application
	private int nrVarArgs; // Application

	public ProtosDataElement() {
		parties = new HashSet();
		metaValues = new HashSet();
		datas = new HashSet();
	}

	public String getName() {
		return name;
	}

	public String getID() {
		return ID;
	}

	public int getType() {
		return type;
	}

	public String getDataType() {
		return dataType;
	}

	/**
	 * Constructs a Data Element object (except for its type) out of a "scalar",
	 * "document", "folder", "struct", or "application" Node.
	 * 
	 * @param anyNode
	 *            Node The "scalar", "document", "folder", "struct", or
	 *            "application" node that contains the Data Element.
	 * @return String Any error message.
	 */
	private String readXMLExportAny(Node anyNode) {
		String msg = "";
		ID = anyNode.getAttributes().getNamedItem(ProtosString.Id)
				.getNodeValue();
		NodeList nodes = anyNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Name)) {
				name = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Type)) {
				dataType = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Length)) {
				length = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.Automatic)) {
				automatic = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.IOSpec)) {
				iospec = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.ComScript)) {
				comScript = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.NrVarArgs)) {
				nrVarArgs = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.Description)) {
				description = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Workinstruction)) {
				workinstruction = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Label)) {
				label = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.StandardLetter)) {
				standardLetter = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Format)) {
				format = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.YesLabel)) {
				yesLabel = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.NoLabel)) {
				noLabel = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.SwitchValues)) {
				switchValues = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.DocumentData)
					|| node.getNodeName().equals(ProtosString.FolderData)
					|| node.getNodeName().equals(ProtosString.StructData)
					|| node.getNodeName().equals(ProtosString.ApplicationData)) {
				String data = ProtosUtil.readString(node);
				datas.add(data);
			} else if (node.getNodeName().equals(ProtosString.ScalarParty)
					|| node.getNodeName().equals(ProtosString.DocumentParty)
					|| node.getNodeName().equals(ProtosString.FolderParty)
					|| node.getNodeName().equals(ProtosString.StructParty)
					|| node.getNodeName().equals(ProtosString.ApplicationParty)) {
				String party = ProtosUtil.readString(node);
				parties.add(party);
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
	 * Constructs a Data Element object out of a "dataelement" Node.
	 * 
	 * @param dataElementNode
	 *            Node The "dataelement" node that contains the Data Element.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node dataElementNode) {
		String msg = "";
		NodeList nodes = dataElementNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Scalar)) {
				type = DataElementScalar;
				msg += readXMLExportAny(node);
			} else if (node.getNodeName().equals(ProtosString.Document)) {
				type = DataElementDocument;
				msg += readXMLExportAny(node);
			} else if (node.getNodeName().equals(ProtosString.Folder)) {
				type = DataElementFolder;
				msg += readXMLExportAny(node);
			} else if (node.getNodeName().equals(ProtosString.Struct)) {
				type = DataElementStruct;
				msg += readXMLExportAny(node);
			} else if (node.getNodeName().equals(ProtosString.Application)) {
				type = DataElementApplication;
				msg += readXMLExportAny(node);
			}
		}
		return msg;
	}

	/**
	 * Returns the Data Element object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Data Element object.
	 * @return String The Data Element object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag + ">";
		{
			if (type == DataElementScalar) {
				xml += "<" + ProtosString.Scalar + " " + ProtosString.Id
						+ "=\"" + ID + "\">";
				{
					xml += ProtosUtil.writeString(ProtosString.Name, name);
					xml += ProtosUtil.writeString(ProtosString.Type, dataType);
					xml += ProtosUtil.writeInt(ProtosString.Length, length);
					xml += ProtosUtil.writeString(ProtosString.Description,
							description);
					xml += ProtosUtil.writeString(ProtosString.Workinstruction,
							workinstruction);
					xml += ProtosUtil.writeString(ProtosString.Label, label);
					xml += ProtosUtil.writeString(ProtosString.Format, format);
					xml += ProtosUtil.writeString(ProtosString.YesLabel,
							yesLabel);
					xml += ProtosUtil
							.writeString(ProtosString.NoLabel, noLabel);
					xml += ProtosUtil.writeString(ProtosString.SwitchValues,
							switchValues);
					for (Iterator it = parties.iterator(); it.hasNext();) {
						String party = (String) it.next();
						xml += ProtosUtil.writeString(ProtosString.ScalarParty,
								party);
					}
					for (Iterator it = metaValues.iterator(); it.hasNext();) {
						ProtosMetavalue metavalue = (ProtosMetavalue) it.next();
						xml += metavalue.writeXMLExport(ProtosString.Metavalue);
					}
					xml += ProtosUtil.writeBool(ProtosString.Deactivated,
							deactivated);
				}
				xml += "</" + ProtosString.Scalar + ">";
			} else if (type == DataElementDocument) {
				xml += "<" + ProtosString.Document + " " + ProtosString.Id
						+ "=\"" + ID + "\">";
				{
					xml += ProtosUtil.writeString(ProtosString.Name, name);
					xml += ProtosUtil.writeString(ProtosString.Type, dataType);
					xml += ProtosUtil.writeString(ProtosString.IOSpec, iospec);
					xml += ProtosUtil.writeString(ProtosString.Description,
							description);
					xml += ProtosUtil.writeString(ProtosString.Workinstruction,
							workinstruction);
					for (Iterator it = datas.iterator(); it.hasNext();) {
						String data = (String) it.next();
						xml += ProtosUtil.writeString(
								ProtosString.DocumentData, data);
					}
					for (Iterator it = parties.iterator(); it.hasNext();) {
						String party = (String) it.next();
						xml += ProtosUtil.writeString(
								ProtosString.DocumentParty, party);
					}
					xml += ProtosUtil.writeString(ProtosString.Label, label);
					xml += ProtosUtil.writeString(ProtosString.StandardLetter,
							standardLetter);
					for (Iterator it = metaValues.iterator(); it.hasNext();) {
						ProtosMetavalue metavalue = (ProtosMetavalue) it.next();
						xml += metavalue.writeXMLExport(ProtosString.Metavalue);
					}
					xml += ProtosUtil.writeBool(ProtosString.Deactivated,
							deactivated);
				}
				xml += "</" + ProtosString.Document + ">";
			} else if (type == DataElementFolder) {
				xml += "<" + ProtosString.Folder + " " + ProtosString.Id
						+ "=\"" + ID + "\">";
				{
					xml += ProtosUtil.writeString(ProtosString.Name, name);
					xml += ProtosUtil.writeString(ProtosString.Description,
							description);
					xml += ProtosUtil.writeString(ProtosString.Workinstruction,
							workinstruction);
					for (Iterator it = datas.iterator(); it.hasNext();) {
						String data = (String) it.next();
						xml += ProtosUtil.writeString(ProtosString.FolderData,
								data);
					}
					for (Iterator it = parties.iterator(); it.hasNext();) {
						String party = (String) it.next();
						xml += ProtosUtil.writeString(ProtosString.FolderParty,
								party);
					}
					xml += ProtosUtil.writeString(ProtosString.Label, label);
					for (Iterator it = metaValues.iterator(); it.hasNext();) {
						ProtosMetavalue metavalue = (ProtosMetavalue) it.next();
						xml += metavalue.writeXMLExport(ProtosString.Metavalue);
					}
					xml += ProtosUtil.writeBool(ProtosString.Deactivated,
							deactivated);
				}
				xml += "</" + ProtosString.Folder + ">";
			} else if (type == DataElementStruct) {
				xml += "<" + ProtosString.Struct + " " + ProtosString.Id
						+ "=\"" + ID + "\">";
				{
					xml += ProtosUtil.writeString(ProtosString.Name, name);
					xml += ProtosUtil.writeString(ProtosString.Description,
							description);
					xml += ProtosUtil.writeString(ProtosString.Workinstruction,
							workinstruction);
					for (Iterator it = datas.iterator(); it.hasNext();) {
						String data = (String) it.next();
						xml += ProtosUtil.writeString(ProtosString.StructData,
								data);
					}
					for (Iterator it = parties.iterator(); it.hasNext();) {
						String party = (String) it.next();
						xml += ProtosUtil.writeString(ProtosString.StructParty,
								party);
					}
					xml += ProtosUtil.writeString(ProtosString.Label, label);
					for (Iterator it = metaValues.iterator(); it.hasNext();) {
						ProtosMetavalue metavalue = (ProtosMetavalue) it.next();
						xml += metavalue.writeXMLExport(ProtosString.Metavalue);
					}
					xml += ProtosUtil.writeBool(ProtosString.Deactivated,
							deactivated);
				}
				xml += "</" + ProtosString.Struct + ">";
			} else if (type == DataElementApplication) {
				xml += "<" + ProtosString.Application + " " + ProtosString.Id
						+ "=\"" + ID + "\">";
				{
					xml += ProtosUtil.writeString(ProtosString.Name, name);
					xml += ProtosUtil.writeBool(ProtosString.Automatic,
							automatic);
					xml += ProtosUtil.writeString(ProtosString.Description,
							description);
					xml += ProtosUtil.writeString(ProtosString.Workinstruction,
							workinstruction);
					for (Iterator it = datas.iterator(); it.hasNext();) {
						String data = (String) it.next();
						xml += ProtosUtil.writeString(
								ProtosString.ApplicationData, data);
					}
					for (Iterator it = parties.iterator(); it.hasNext();) {
						String party = (String) it.next();
						xml += ProtosUtil.writeString(
								ProtosString.ApplicationParty, party);
					}
					xml += ProtosUtil.writeString(ProtosString.Label, label);
					xml += ProtosUtil.writeString(ProtosString.ComScript,
							comScript);
					xml += ProtosUtil.writeInt(ProtosString.NrVarArgs,
							nrVarArgs);
					for (Iterator it = metaValues.iterator(); it.hasNext();) {
						ProtosMetavalue metavalue = (ProtosMetavalue) it.next();
						xml += metavalue.writeXMLExport(ProtosString.Metavalue);
					}
					xml += ProtosUtil.writeBool(ProtosString.Deactivated,
							deactivated);
				}
				xml += "</" + ProtosString.Application + ">";
			}
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
