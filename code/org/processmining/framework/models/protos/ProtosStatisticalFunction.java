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
 * Title: Protos statistical function
 * </p>
 * 
 * <p>
 * Description: Holds a Protos statistical function
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
public class ProtosStatisticalFunction {
	static int StatisticalFunctionNexp = 0;
	static int StatisticalFunctionBeta = 1;
	static int StatisticalFunctionNormal = 2;
	static int StatisticalFunctionErlang = 3;
	static int StatisticalFunctionGamma = 4;
	static int StatisticalFunctionUniform = 5;
	static int StatisticalFunctionConstant = 6;

	private int type; // One of the values above;

	private float mean; // Nexp, Beta, Normal, Erlang, Gamma, Constant
	private float minimum; // Beta, Uniform
	private float maximum; // Beta, Uniform
	private float variance; // Normal, Gamma
	private float number; // Erlang

	public ProtosStatisticalFunction() {
	}

	// all methods added by Mariska Netjes
	public int getType() {
		return type;
	}

	public float getMean() {
		return mean;
	}

	public float getMinimum() {
		return minimum;
	}

	public float getMaximum() {
		return maximum;
	}

	public float getVariance() {
		return variance;
	}

	public float getNumber() {
		return number;
	}

	/**
	 * Constructs a Statistical Function object (except for tis type) out of a
	 * "Nexp", "Beta", "Normal", "Erlang", "Gamma", "Uniform", or "Constant"
	 * Node.
	 * 
	 * @param anyNode
	 *            Node The "Nexp", "Beta", "Normal", "Erlang", "Gamma",
	 *            "Uniform", or "Constant" node that contains the Statistical
	 *            Function.
	 * @return String Any error message.
	 */
	public String readXMLExportAny(Node anyNode) {
		String msg = "";
		NamedNodeMap attributes = anyNode.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node node = attributes.item(i);
			if (node.getNodeName().equals(ProtosString.Mean)) {
				mean = ProtosUtil.readFloat(node);
			} else if (node.getNodeName().equals(ProtosString.Minimum)) {
				minimum = ProtosUtil.readFloat(node);
			} else if (node.getNodeName().equals(ProtosString.Maximum)) {
				maximum = ProtosUtil.readFloat(node);
			} else if (node.getNodeName().equals(ProtosString.Variance)) {
				variance = ProtosUtil.readFloat(node);
			} else if (node.getNodeName().equals(ProtosString.Number)) {
				number = ProtosUtil.readFloat(node);
			}
		}
		return msg;
	}

	/**
	 * Constructs a Statistical Function object out of a statistical function
	 * Node.
	 * 
	 * @param statisticalFunctionNode
	 *            Node The node that contains the Statistical Function.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node statisticalFunctionNode) {
		String msg = "";
		NodeList nodes = statisticalFunctionNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Nexp)) {
				type = StatisticalFunctionNexp;
				msg += readXMLExportAny(node);
			} else if (node.getNodeName().equals(ProtosString.Beta)) {
				type = StatisticalFunctionBeta;
				msg += readXMLExportAny(node);
			} else if (node.getNodeName().equals(ProtosString.Normal)) {
				type = StatisticalFunctionNormal;
				msg += readXMLExportAny(node);
			} else if (node.getNodeName().equals(ProtosString.Erlang)) {
				type = StatisticalFunctionErlang;
				msg += readXMLExportAny(node);
			} else if (node.getNodeName().equals(ProtosString.Gamma)) {
				type = StatisticalFunctionGamma;
				msg += readXMLExportAny(node);
			} else if (node.getNodeName().equals(ProtosString.Uniform)) {
				type = StatisticalFunctionUniform;
				msg += readXMLExportAny(node);
			} else if (node.getNodeName().equals(ProtosString.Constant)) {
				type = StatisticalFunctionConstant;
				msg += readXMLExportAny(node);
			}
		}
		return msg;
	}

	/**
	 * Returns the Statistical Function object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Statistical Function object.
	 * @return String The Statistical Function object in Protos XML Export
	 *         format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag + ">";
		{
			if (type == StatisticalFunctionNexp) {
				xml += "<" + ProtosString.Nexp + " " + ProtosString.Mean
						+ "=\"" + mean + "\"/>";

			} else if (type == StatisticalFunctionBeta) {
				xml += "<" + ProtosString.Beta + " " + ProtosString.Mean
						+ "=\"" + mean + "\" " + ProtosString.Minimum + "=\""
						+ minimum + "\" " + ProtosString.Maximum + "=\""
						+ maximum + "\"/>";

			} else if (type == StatisticalFunctionNormal) {
				xml += "<" + ProtosString.Normal + " " + ProtosString.Mean
						+ "=\"" + mean + "\" " + ProtosString.Variance + "=\""
						+ variance + "\"/>";

			} else if (type == StatisticalFunctionErlang) {
				xml += "<" + ProtosString.Erlang + " " + ProtosString.Mean
						+ "=\"" + mean + "\" " + ProtosString.Number + "="
						+ number + "\"/>";

			} else if (type == StatisticalFunctionGamma) {
				xml += "<" + ProtosString.Gamma + " " + ProtosString.Mean
						+ "=\"" + mean + "\" " + ProtosString.Variance + "=\""
						+ variance + "\"/>";

			} else if (type == StatisticalFunctionUniform) {
				xml += "<" + ProtosString.Uniform + " " + ProtosString.Minimum
						+ "=\"" + minimum + "\" " + ProtosString.Maximum
						+ "=\"" + maximum + "\"/>";

			} else if (type == StatisticalFunctionConstant) {
				xml += "<" + ProtosString.Constant + " " + ProtosString.Mean
						+ "=\"" + mean + "\"/>";

			}
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
