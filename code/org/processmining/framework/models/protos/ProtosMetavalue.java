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
 * Title: Protos meta value
 * </p>
 * 
 * <p>
 * Description: Holds a Protos meta value
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
public class ProtosMetavalue {
	static int MetavalueInteger = 0;
	static int MetavalueString = 1;
	static int MetavalueFloat = 2;
	static int MetavalueObject = 3;
	static int MetavalueUrl = 4;
	static int MetavalueEmail = 5;
	static int MetavalueEnum = 6;

	private int type; // One of the values above.

	private int integerValue; // Integer
	private String stringValue; // String
	private float floatValue; // Float
	private String objectValue; // Object
	private String windowPlacement; // Url
	private String urlBase; // Url
	private String externalName; // Url, Email
	private String internalName; // Url, Email
	private String enumValue; // Enum

	public ProtosMetavalue() {
	}

	/**
	 * Constructs a Metavalue object out of a Node.
	 * 
	 * @param metavalueNode
	 *            Node The node that contains the Metavalue.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node metavalueNode) {
		String msg = "";
		NodeList nodes = metavalueNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.MetavalueInteger)) {
				type = MetavalueInteger;
				integerValue = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.MetavalueString)) {
				type = MetavalueString;
				stringValue = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.MetavalueFloat)) {
				type = MetavalueFloat;
				floatValue = ProtosUtil.readFloat(node);
			} else if (node.getNodeName().equals(ProtosString.MetavalueObject)) {
				type = MetavalueObject;
				objectValue = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.MetavalueUrl)) {
				type = MetavalueUrl;
				NodeList subNodes = node.getChildNodes();
				for (int j = 0; j < subNodes.getLength(); j++) {
					Node subNode = subNodes.item(j);
					if (subNode.getNodeName().equals(
							ProtosString.WindowPlacement)) {
						windowPlacement = ProtosUtil.readString(subNode);
					} else if (subNode.getNodeName().equals(
							ProtosString.UrlBase)) {
						urlBase = ProtosUtil.readString(subNode);
					} else if (subNode.getNodeName().equals(
							ProtosString.ExternalName)) {
						externalName = ProtosUtil.readString(subNode);
					} else if (subNode.getNodeName().equals(
							ProtosString.InternalName)) {
						internalName = ProtosUtil.readString(subNode);
					}
				}
			} else if (node.getNodeName().equals(ProtosString.MetavalueEmail)) {
				type = MetavalueEmail;
				NodeList subNodes = node.getChildNodes();
				for (int j = 0; j < subNodes.getLength(); j++) {
					Node subNode = subNodes.item(j);
					if (subNode.getNodeName().equals(ProtosString.ExternalName)) {
						externalName = ProtosUtil.readString(subNode);
					} else if (subNode.getNodeName().equals(
							ProtosString.InternalName)) {
						internalName = ProtosUtil.readString(subNode);
					}
				}
			} else if (node.getNodeName().equals(ProtosString.MetavalueEnum)) {
				type = MetavalueEnum;
				enumValue = ProtosUtil.readString(node);
			}
		}
		return msg;
	}

	/**
	 * Returns the Metavalue object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Metavalue object.
	 * @return String The Metavalue object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag + ">";
		{
			if (type == MetavalueInteger) {
				xml += ProtosUtil.writeInt(ProtosString.MetavalueInteger,
						integerValue);
			} else if (type == MetavalueString) {
				xml += ProtosUtil.writeString(ProtosString.MetavalueString,
						stringValue);
			} else if (type == MetavalueFloat) {
				xml += ProtosUtil.writeFloat(ProtosString.MetavalueFloat,
						floatValue);
			} else if (type == MetavalueObject) {
				xml += ProtosUtil.writeString(ProtosString.MetavalueObject,
						objectValue);
			} else if (type == MetavalueUrl) {
				xml += "<" + ProtosString.MetavalueUrl + ">";
				{
					xml += ProtosUtil.writeString(ProtosString.WindowPlacement,
							windowPlacement);
					xml += ProtosUtil
							.writeString(ProtosString.UrlBase, urlBase);
					xml += ProtosUtil.writeString(ProtosString.ExternalName,
							externalName);
					xml += ProtosUtil.writeString(ProtosString.InternalName,
							internalName);
				}
				xml += "</" + ProtosString.MetavalueUrl + ">";
			} else if (type == MetavalueEmail) {
				xml += "<" + ProtosString.MetavalueEmail + ">";
				{
					xml += ProtosUtil.writeString(ProtosString.ExternalName,
							externalName);
					xml += ProtosUtil.writeString(ProtosString.InternalName,
							internalName);
				}
				xml += "</" + ProtosString.MetavalueEmail + ">";
			} else if (type == MetavalueEnum) {
				xml += ProtosUtil.writeString(ProtosString.MetavalueEnum,
						enumValue);
			}
		}
		xml += "</" + tag + ">";
		{
			return xml;
		}
	}
}
