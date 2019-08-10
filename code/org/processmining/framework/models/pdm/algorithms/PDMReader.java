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

package org.processmining.framework.models.pdm.algorithms;

import java.io.*;
import javax.xml.parsers.*;

import org.processmining.framework.models.pdm.*;
import org.w3c.dom.*;

/**
 * <p>
 * Title: PDM reader
 * </p>
 * <p>
 * Description: Parses and reads the incoming PDM file, and loads the
 * corresponding model into the ProM framework.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Irene Vanderfeesten
 * @version 1.0
 */

public class PDMReader {
	/**
	 * Create the reader
	 */
	private PDMReader() {
	}

	/**
	 * Read the PDM file waiting in the given stream, recognize a PDM model and
	 * store it.
	 */
	public static PDMModel read(InputStream input) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc;
		PDMModel model;
		NodeList nodes;

		dbf.setValidating(false);
		dbf.setIgnoringComments(true);
		dbf.setIgnoringElementContentWhitespace(true);

		// Load the XML file as a tree structure
		doc = dbf.newDocumentBuilder().parse(input);

		// check if the first element is a 'pdm' element
		if (!doc.getDocumentElement().getTagName().equals("PDM")) {
			throw new Exception("PDM tag not found");
		}

		// the nodes of the xml-file are stored in variable 'nodes' and are
		// parsed separately according to their meaning
		nodes = null;
		nodes = doc.getDocumentElement().getChildNodes();
		String name = "pdmmodel";
		for (int i = 1; i < nodes.getLength(); i++) {
			Node namenode = nodes.item(i);
			if (namenode.getNodeName().equals("Name")) {
				name = namenode.getTextContent();
				// System.out.println("Name = " + name);
			}
		}
		model = new PDMModel(name);
		for (int i = 1; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("DataElement")) {
				parseDataElement(node, model);
			} else if (node.getNodeName().equals("Resource")) {
				parseResource(node, model);
			} else if (node.getNodeName().equals("Operation")) {
				parseOperation(node, model);
			} else if (node.getNodeName().equals("RootElementRef")) {
				parseRootElement(node, model);
			}
		}

		// when all nodes are parsed the complete model is returned
		return model;
	}

	/**
	 * Parses an XML node of the type DataElement
	 * 
	 * @param rootNode
	 *            Node
	 * @param model
	 *            PDMModel
	 */
	public static void parseDataElement(Node rootNode, PDMModel model) {
		Node idnode = rootNode.getAttributes().getNamedItem("DataElementID");
		Node descr = rootNode.getAttributes().getNamedItem("Description");
		if (!(descr == null)) {
			PDMDataElement dataElement1 = new PDMDataElement(model, idnode
					.getNodeValue(), descr.getNodeValue());
			model.addDataElement(dataElement1);
		} else {
			PDMDataElement dataElement2 = new PDMDataElement(model, idnode
					.getNodeValue(), null);
			model.addDataElement(dataElement2);
		}
	}

	/**
	 * Parses an XML node of the type RootElementRef
	 * 
	 * @param rootNode
	 *            Node
	 * @param model
	 *            PDMModel
	 */
	public static void parseRootElement(Node rootNode, PDMModel model) {
		// Node refnode =
		// rootNode.getAttributes().getNamedItem("RootElementRef");
		// System.out.println(refnode);
		// PDMDataElement dataElement =
		// model.getDataElement(refnode.getNodeValue());
		// model.setRootElement(dataElement);
		// System.out.println("RootElement: " + dataElement.getID());
		if (rootNode.getNodeName().equals("RootElementRef")) {
			String ref;
			ref = rootNode.getTextContent();
			PDMDataElement elt = model.getDataElement(ref);
			// System.out.println(elt.getID());
			model.setRootElement(elt);
		}
	}

	/**
	 * Parses an XML node of the type Resource
	 * 
	 * @param rootNode
	 *            Node
	 * @param model
	 *            PDMModel
	 */
	public static void parseResource(Node rootNode, PDMModel model) {
		Node idnode = rootNode.getAttributes().getNamedItem("ResourceID");
		PDMResource resource = new PDMResource(idnode.getNodeValue());
		model.addResource(resource);
	}

	/**
	 * Parses an XML node of the type Operation
	 * 
	 * @param rootNode
	 *            Node
	 * @param model
	 *            PDMModel
	 */
	public static void parseOperation(Node rootNode, PDMModel model) {
		NodeList nodes = rootNode.getChildNodes();

		// store the attributes of the operation
		Node idnode = rootNode.getAttributes().getNamedItem("OperationID");

		// create the operation and add it to the model
		PDMOperation operation = new PDMOperation(model, idnode.getNodeValue());
		model.addOperation(operation);

		// add all elements of the operation
		Node node;
		for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			if (node.getNodeName().equals("Input")) {
				parseInputElements(node, operation, model);
			} else if (node.getNodeName().equals("Output")) {
				parseOutputElements(node, operation, model);
			} else if (node.getNodeName().equals("ResourceRef")) {
				parseResourceRef(node, operation, model);
			} else if (node.getNodeName().equals("Cost")) {
				parseCost(node, operation);
			} else if (node.getNodeName().equals("Time")) {
				parseTime(node, operation);
			} else if (node.getNodeName().equals("Probability")) {
				parseProbability(node, operation);
			}

			else if (node.getNodeName().equals("Condition")) {
				parseCondition(node, operation, model);
			}

		}
	}

	/**
	 * Parses an XML node of the type Input.
	 * 
	 * @param rootNode
	 *            Node
	 * @param operation
	 *            PDMOperation
	 * @param model
	 *            PDMModel
	 */
	public static void parseInputElements(Node rootNode,
			PDMOperation operation, PDMModel model) {
		NodeList nodes = rootNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node;
			node = nodes.item(i);
			if (node.getNodeName().equals("DataElementRef")) {
				String ref;
				ref = node.getTextContent();
				operation.addInputElement(ref, model);
			}
		}
	}

	/**
	 * Parses an XML node of the type Output.
	 * 
	 * @param rootNode
	 *            Node
	 * @param operation
	 *            PDMOperation
	 * @param model
	 *            PDMModel
	 */
	public static void parseOutputElements(Node rootNode,
			PDMOperation operation, PDMModel model) {
		NodeList nodes = rootNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node;
			node = nodes.item(i);
			if (node.getNodeName().equals("DataElementRef")) {
				String ref;
				ref = node.getTextContent();
				operation.addOutputElement(ref, model);
			}
		}

	}

	/**
	 * Parses an XML node of the type ResourceRef.
	 * 
	 * @param rootNode
	 *            Node
	 * @param operation
	 *            PDMOperation
	 * @param model
	 *            PDMModel
	 */
	public static void parseResourceRef(Node rootNode, PDMOperation operation,
			PDMModel model) {
		Node node = rootNode;
		if (node.getNodeName().equals("ResourceRef")) {
			String ref;
			ref = node.getTextContent();
			operation.addResource(ref, model);
		}
	}

	/**
	 * Parses an XML Nodel of the type Cost.
	 * 
	 * @param rootNode
	 *            Node
	 * @param operation
	 *            PDMOperation
	 */
	public static void parseCost(Node rootNode, PDMOperation operation) {
		Node node = rootNode;
		if (node.getNodeName().equals("Cost")) {
			String str = node.getTextContent();
			Integer cst = new Integer(str);
			int cost = cst.intValue();
			operation.setCost(cost);
		}
	}

	/**
	 * Parses an XML node of the type Time (i.e. duration of the operation).
	 * 
	 * @param rootNode
	 *            Node
	 * @param operation
	 *            PDMOperation
	 */
	public static void parseTime(Node rootNode, PDMOperation operation) {
		Node node = rootNode;
		if (node.getNodeName().equals("Time")) {
			String str = node.getTextContent();
			Integer tm = new Integer(str);
			int time = tm.intValue();
			operation.setDuration(time);
		}
	}

	private static void parseProbability(Node rootNode, PDMOperation operation) {
		Node node = rootNode;
		if (node.getNodeName().equals("Probability")) {
			String str = node.getTextContent();
			Double tm = new Double(str);
			double prob = tm.doubleValue();
			operation.setFailureProbability(prob);
		}
	}

	/**
	 * Parses an XML node of the type Condition.
	 * 
	 * @param node
	 *            Node
	 * @param operation
	 *            PDMOperation
	 */
	private static void parseCondition(Node rootNode, PDMOperation operation,
			PDMModel model) {
		Node node = rootNode;
		if (node.getNodeName().equals("Condition")) {
			String str = node.getTextContent();
			/*
			 * if (str.contains("!=")){ String[] condArray = str.split("!="); if
			 * (condArray.length == 2) { String d = condArray[0]; String value =
			 * condArray[1]; PDMDataElement data = model.getDataElement(d);
			 * String conID = operation.getID() + "-" + data.getID();
			 * PDMCondition con = new PDMCondition(conID, data, value);
			 * System.out.println(con.writeCondition());
			 * operation.addCondition(con); }
			 * 
			 * } else
			 */
			if (str.contains("=")) {
				String[] condArray = str.split("=");
				if (condArray.length == 2) {
					String d = condArray[0];
					String value = condArray[1];
					PDMDataElement data = model.getDataElement(d);
					String conID = operation.getID() + "-" + data.getID();
					PDMCondition con = new PDMCondition(conID, data, value);
					System.out.println(con.writeCondition());
					operation.addCondition(con);
				}
			} else
				System.out.println("Condition in wrong text format");
		}

	}

}
