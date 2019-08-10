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

package org.processmining.framework.models.yawl.algorithms;

import java.io.*;
import javax.xml.parsers.*;

import org.processmining.framework.models.yawl.*;
import org.w3c.dom.*;
import org.processmining.framework.log.LogEvent;

/**
 * <p>
 * Title: YAWL parser/reader
 * </p>
 * <p>
 * Description: Parses and reads the incoming YAWL file, and loads the
 * corresponding model into the ProM framework.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */

public class YAWLReader {
	/**
	 * Create the reader
	 */
	private YAWLReader() {
	}

	/**
	 * Read the YAWL file waiting in the given stream, recognize a YAWL model,
	 * and store it as a MiningResult.
	 * 
	 * @param input
	 *            The given stream
	 * @return The MiningResult
	 * @throws java.lang.Exception
	 *             If anything fails
	 */
	public static YAWLModel read(InputStream input) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc;
		YAWLModel model;
		NodeList nodes;
		Node specificationSetNode;
		Node specificationNode;

		dbf.setValidating(false);
		dbf.setIgnoringComments(true);
		dbf.setIgnoringElementContentWhitespace(true);

		// Load the xml file as a tree structure
		doc = dbf.newDocumentBuilder().parse(input);

		// The first element should be a specificationSet
		specificationSetNode = doc.getFirstChild();
		if (!specificationSetNode.getNodeName().equals("specificationSet")) {
			throw new Exception("specificationSet tag not found");
		}

		// Next, we look for a specification element
		specificationNode = null;
		nodes = specificationSetNode.getChildNodes();
		for (int i = 0; specificationNode == null && i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("specification")) {
				specificationNode = node;
			}
		}
		if (specificationNode == null) {
			throw new Exception("specification tag not found");
		}

		// Create a YAWL model with the uri as name.
		model = new YAWLModel(specificationNode.getAttributes().getNamedItem(
				"uri").getNodeValue());

		// Add all decompositions
		nodes = specificationNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("decomposition")) {
				parseDecomposition(node, model, false);
			} else if (node.getNodeName().equals("rootNet")) {
				parseDecomposition(node, model, true);
			}
		}
		return model;
	}

	/**
	 * Add a decomposition to the given model, starting from the given node.
	 * 
	 * @param rootNode
	 *            The given node
	 * @param model
	 *            The given model
	 */
	public static void parseDecomposition(Node rootNode, YAWLModel model,
			boolean isRootNet) {
		NodeList nodes = rootNode.getChildNodes();
		// Store attributes
		Node idNode = rootNode.getAttributes().getNamedItem("id");
		Node isRootNetNode = rootNode.getAttributes().getNamedItem("isRootNet");
		Node xsiTypeNode = rootNode.getAttributes().getNamedItem("xsi:type");

		// Create decomposition and add it to the model
		YAWLDecomposition decomposition = new YAWLDecomposition(
				idNode == null ? "<unknown>" : idNode.getNodeValue(),
				isRootNet ? "true" : (isRootNetNode == null ? "false"
						: isRootNetNode.getNodeValue()),
				xsiTypeNode == null ? "<unknown>" : xsiTypeNode.getNodeValue());
		model.addDecomposition(idNode == null ? "<unknown>" : idNode
				.getNodeValue(), decomposition);

		// Add all nodes and edges
		Node node;
		for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);

			if (node.getNodeName().equals("processControlElements")) {
				parseProcessControlElements(node, decomposition);
			}
		}
	}

	/**
	 * Add all nodes and edges to given decomposition, starting from the given
	 * node
	 * 
	 * @param rootNode
	 *            The given node
	 * @param decomposition
	 *            The given decomposition
	 */
	public static void parseProcessControlElements(Node rootNode,
			YAWLDecomposition decomposition) {
		// First, load all nodes
		NodeList nodes = rootNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("inputCondition")) {
				// Found an input condition, add it
				parseInputCondition(node, decomposition);
			} else if (node.getNodeName().equals("outputCondition")) {
				// Found an output condition, add it
				parseOutputCondition(node, decomposition);
			} else if (node.getNodeName().equals("condition")) {
				// Found a normal condition, add it
				parseCondition(node, decomposition);
			} else if (node.getNodeName().equals("task")) {
				// Found a task, add it
				parseTask(node, decomposition);
			}
		}
		// Second, load all edges
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("inputCondition") ||
			// node.getNodeName().equals("outputCondition") ||
					node.getNodeName().equals("condition")) {
				// Found a condition, add all normal outgoing edges
				parseFlowsInto(node, decomposition);
			} else if (node.getNodeName().equals("task")) {
				// Found a task, add all normal outgoing edges and all reset
				// outgoing edges
				parseFlowsInto(node, decomposition);
				parseRemovesTokens(node, decomposition);
			}
		}
	}

	/**
	 * Add an input condition to the given decomposition, starting from the
	 * given node.
	 * 
	 * @param rootNode
	 *            The given node
	 * @param decomposition
	 *            The given decomposition
	 */
	public static void parseInputCondition(Node rootNode,
			YAWLDecomposition decomposition) {
		Node node = rootNode.getAttributes().getNamedItem("id");
		decomposition
				.addInputCondition(node == null ? "" : node.getNodeValue());
	}

	/**
	 * Add an output condition to the given decomposition, starting from the
	 * given node.
	 * 
	 * @param rootNode
	 *            The given node
	 * @param decomposition
	 *            The given decomposition
	 */
	public static void parseOutputCondition(Node rootNode,
			YAWLDecomposition decomposition) {
		Node node = rootNode.getAttributes().getNamedItem("id");
		decomposition.addOutputCondition(node == null ? "" : node
				.getNodeValue());
	}

	/**
	 * Add a normal condition to the given decomposition, starting from the
	 * given node.
	 * 
	 * @param rootNode
	 *            The given node
	 * @param decomposition
	 *            The given decomposition
	 */
	public static void parseCondition(Node rootNode,
			YAWLDecomposition decomposition) {
		Node node = rootNode.getAttributes().getNamedItem("id");
		decomposition.addCondition(node == null ? "" : node.getNodeValue());
	}

	/**
	 * Add a task to the given decomposition, starting from the given node.
	 * 
	 * @param rootNode
	 *            The given node
	 * @param decomposition
	 *            The given decomposition
	 */
	public static void parseTask(Node rootNode, YAWLDecomposition decomposition) {
		Node idNode = rootNode.getAttributes().getNamedItem("id");
		String name = null;
		Node joinNode = null;
		Node splitNode = null;
		Node decomposesToNode = null;
		Node node;
		NodeList nodes = rootNode.getChildNodes();

		// Look for join, split and decomposesTo attributes
		for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			if (node.getNodeName().equals("join")) {
				joinNode = node.getAttributes().getNamedItem("code");
			} else if (node.getNodeName().equals("split")) {
				splitNode = node.getAttributes().getNamedItem("code");
			} else if (node.getNodeName().equals("decomposesTo")) {
				decomposesToNode = node.getAttributes().getNamedItem("id");
			} else if (node.getNodeName().equals("name")) {
				name = node.getTextContent();
			}
		}

		// Add the task
		YAWLTask task = decomposition
				.addTask(idNode.getNodeValue(), joinNode == null ? "xor"
						: joinNode.getNodeValue(), splitNode == null ? "and"
						: splitNode.getNodeValue(),
						decomposesToNode == null ? "" : decomposesToNode
								.getNodeValue(), null);
		task.setIdentifier(name);
		task.setLogEvent(new LogEvent(task.getIdentifier(), "unknown:normal"));
	}

	/**
	 * Adds a normal edge to the given decomposition, starting from the given
	 * node.
	 * 
	 * @param rootNode
	 *            The given node
	 * @param decomposition
	 *            The given decomposition
	 */
	public static void parseFlowsInto(Node rootNode,
			YAWLDecomposition decomposition) {
		Node fromNode = rootNode.getAttributes().getNamedItem("id");
		if (fromNode != null) {
			NodeList nodes = rootNode.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeName().equals("flowsInto")) {
					// Found a normal edge, add it
					NodeList nodes2 = node.getChildNodes();
					Node toNode = null;
					Node isDefaultFlowNode = null;
					Node predicateNode = null;
					Node orderingNode = null;

					// Look for attributes
					for (int j = 0; j < nodes2.getLength(); j++) {
						Node node2 = nodes2.item(j);
						if (node2.getNodeName().equals("nextElementRef")) {
							toNode = node2.getAttributes().getNamedItem("id");
						} else if (node2.getNodeName().equals("isDefaultFlow")) {
							isDefaultFlowNode = node2;
						} else if (node2.getNodeName().equals("predicate")) {
							predicateNode = node2.getFirstChild(); // Get the
							// implicit
							// text node
							orderingNode = node2.getAttributes().getNamedItem(
									"ordering");
						}
					}

					// Add the edge, provided its destination has been found
					if (toNode != null) {
						decomposition.addEdge(fromNode.getNodeValue(), toNode
								.getNodeValue(), isDefaultFlowNode != null,
								predicateNode == null ? null : predicateNode
										.getNodeValue(),
								orderingNode == null ? null : orderingNode
										.getNodeValue());
					}
				}
			}
		}
	}

	/**
	 * Adds a reset edge to the given decomposition, starting from the given
	 * node.
	 * 
	 * @param rootNode
	 *            The given node
	 * @param decomposition
	 *            The given decomposition
	 */
	public static void parseRemovesTokens(Node rootNode,
			YAWLDecomposition decomposition) {
		Node fromNode = rootNode.getAttributes().getNamedItem("id");
		Node toNode;
		if (fromNode != null) {
			NodeList nodes = rootNode.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeName().equals("removesTokens")) {
					// Found a reset edge, add it
					toNode = node.getAttributes().getNamedItem("id");

					// Add the edge, provided its destination has been found
					if (toNode != null) {
						decomposition.addResetEdge(fromNode.getNodeValue(),
								toNode.getNodeValue());
					}
				}
			}
		}
	}
}
