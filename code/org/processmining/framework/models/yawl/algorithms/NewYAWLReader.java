/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/
package org.processmining.framework.models.yawl.algorithms;

import java.io.InputStream;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLCondition;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLProcess;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.att.HLAttributeValue;
import org.processmining.framework.models.hlprocess.att.HLBooleanAttribute;
import org.processmining.framework.models.hlprocess.att.HLBooleanValue;
import org.processmining.framework.models.hlprocess.att.HLNominalAttribute;
import org.processmining.framework.models.hlprocess.att.HLNominalValue;
import org.processmining.framework.models.hlprocess.att.HLNumericAttribute;
import org.processmining.framework.models.hlprocess.att.HLNumericValue;
import org.processmining.framework.models.hlprocess.expr.HLDataExpression;
import org.processmining.framework.models.hlprocess.expr.HLExpressionOperator;
import org.processmining.framework.models.hlprocess.expr.operand.HLAttributeOperand;
import org.processmining.framework.models.hlprocess.expr.operand.HLValueOperand;
import org.processmining.framework.models.hlprocess.expr.operator.HLEqualOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLGreaterEqualOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLGreaterOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLNotEqualOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLSmallerEqualOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLSmallerOperator;
import org.processmining.framework.models.hlprocess.hlmodel.HLYAWL;
import org.processmining.framework.models.yawl.YAWLDecomposition;
import org.processmining.framework.models.yawl.YAWLModel;
import org.processmining.framework.models.yawl.YAWLTask;
import org.processmining.framework.ui.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import att.grappa.Edge;

/**
 * YAWL parser/reader that parses newYAWL and creates a hlYAWL data structure
 * rather than a low-level YAWL control-flow model.
 * 
 * @author Anne Rozinat
 */
public class NewYAWLReader {

	/**
	 * Prevent instantiation.
	 */
	private NewYAWLReader() {
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
	public static HLYAWL read(InputStream input) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc;
		YAWLModel model;
		HLYAWL result;
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
				// make the root net of the YAWL model the name of the whole
				// model
				// (otherwise the name is simply "YAWL model")
				model.setName(node.getNodeName());
			}
		}

		// Now parse the high-level information (data and resources)
		result = new HLYAWL(model);
		nodes = specificationNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("decomposition")) {
				parseDecompositionForHLInformation(node, result, false);
			} else if (node.getNodeName().equals("rootNet")) {
				parseDecompositionForHLInformation(node, result, true);
			}
		}

		// indicate which kind of high-level information was parsed here
		result.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.DATA_INITIAL_VAL);
		result.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.DATA_AT_TASKS);
		result.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.ROLES_AT_TASKS);
		result.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.CHOICE_DATA);

		return result;
	}

	/**
	 * Add a decomposition to the given model, starting from the given node.
	 * 
	 * @param rootNode
	 *            The given node
	 * @param model
	 *            The YAWL model being created
	 * @param isRootNet
	 *            indicates whether decomposition is root net (beware: even if
	 *            passed as <code>false</code> it can still be detected to be
	 *            <code>true</code> in attribute)
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
			}
		}

		// Add the task
		YAWLTask task = decomposition
				.addTask(idNode.getNodeValue(), joinNode == null ? "xor"
						: joinNode.getNodeValue(), splitNode == null ? "and"
						: splitNode.getNodeValue(),
						decomposesToNode == null ? "" : decomposesToNode
								.getNodeValue(), null);
		// TODO Anne: check how to deal with this "unknown:normal" event type as
		// this will be
		// displayed as part of the task name - not desired. YAWL models will
		// contain atomic tasks,
		// which might be decomposed into sub-processes etc., but the offering,
		// start, completion etc.
		// is on a different abstraction layer (happens in the engine and
		// becomes logged etc.)
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

	// //////////////////////// HLProcess related parsing ///////////////////

	// TODO Anne: check whether can be parsed during first reading process
	// already as
	// unnecessarily duplicates code. but the HLYAWL model is created based on
	// the parsed
	// control-flow model. therefore, so far, plain import code is exactly the
	// same (only less)
	// as in the YAWLReader -- put them together where possible

	/**
	 * Add high-level information (such as data and resources) to imported YAWL
	 * model.
	 * 
	 * @param rootNode
	 *            the given node
	 * @param result
	 *            the resulting HLYAWL model to be created
	 * @param isRootNet
	 *            indicates whether decomposition is root net (beware: even if
	 *            passed as <code>false</code> it can still be detected to be
	 *            <code>true</code> in attribute)
	 */
	public static void parseDecompositionForHLInformation(Node rootNode,
			HLYAWL result, boolean isRootNet) {
		NodeList nodes = rootNode.getChildNodes();
		// Store attributes
		Node isRootNetNode = rootNode.getAttributes().getNamedItem("isRootNet");
		// Root net status could also be indicated in the decomposition
		// attribute
		if (isRootNet == false && isRootNetNode != null) {
			isRootNet = isRootNetNode.getNodeValue().equals("true");
		}

		// Add all nodes and edges
		Node node;
		for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			// Parse global data attributes for data perspective
			// for root net
			if (isRootNet == true && node.getNodeName().equals("localVariable")) {
				parseDataVariable(node, result);
			} else if (isRootNet == true
					&& node.getNodeName().equals("inputParam")) {
				parseDataVariable(node, result);
			} else if (isRootNet == true
					&& node.getNodeName().equals("outputParam")) {
				parseDataVariable(node, result);
			}

			// Parse output parameters output parameters at decompositions for
			// data perspective
			// for non-root net decompositions only as root is no task in
			// process
			else if (isRootNet == false
					&& node.getNodeName().equals("outputParam")) {
				Node parent = node.getParentNode();
				if (parent.getNodeName().equals("decomposition")) {
					// get the id - name of task
					Node fromNode = rootNode.getAttributes().getNamedItem("id");
					if (fromNode != null) {
						String decompositionName = fromNode.getNodeValue();
						for (HLActivity act : result.getHLProcess()
								.getActivities()) {
							String actName = act.getName();
							// matches activity names that start like the
							// composition
							// but then are followed by an underscore and some
							// number
							if (actName
									.matches(decompositionName + "_([0-9])*")) {
								addDataAttributeToHLActivity(node, result, act);
							}
						}
					} else {
						Message.add(
								"The id of the decomposition could not be read when parsing "
										+ "for output parameters.",
								Message.ERROR);
					}
				}
			}
			// parse groups at tasks
			else if (node.getNodeName().equals("processControlElements")) {
				NodeList subNodes = node.getChildNodes();
				Node subNode;
				for (int j = 0; j < subNodes.getLength(); j++) {
					subNode = subNodes.item(j);
					if (subNode.getNodeName().equals("task")) {
						parseTaskNode(subNode, result);
						// only xor and or tasks can provide link conditions
						parseFlowsIntoForHLInformation(subNode, result);
					}
				}
			}
		}
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
	public static void parseFlowsIntoForHLInformation(Node rootNode,
			HLYAWL result) {
		HLActivity hlAct = null;
		Node fromNode = rootNode.getAttributes().getNamedItem("id");
		if (fromNode != null) {
			// first find hlActivity that belongs to this task
			String taskName = fromNode.getNodeValue();
			for (HLActivity act : result.getHLProcess().getActivities()) {
				if (taskName.equals(act.getName())) {
					hlAct = act;
				}
			}
			// now read each outgoing arc for link condition
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
					// parse condition targets and add expressions
					parseFlowTargets(toNode, predicateNode, isDefaultFlowNode,
							orderingNode, result, hlAct);
				}
			}
		}
	}

	public static void parseFlowTargets(Node toNode, Node predicateNode,
			Node isDefaultFlowNode, Node orderingNode, HLYAWL result,
			HLActivity hlAct) {
		// get the target node
		String toElementName = toNode.getNodeValue();
		HLActivity targetAct;
		// add a new condition target for flow element, if exists
		if ((predicateNode != null || isDefaultFlowNode != null)
				&& hlAct != null) {
			ModelGraphVertex yawlTask = result
					.findModelGraphVertexForActivity(hlAct.getID());
			HLChoice choice = result.findChoice(yawlTask);
			// add target to choice
			if (choice != null) {

				// (a) try whether target is already activity
				targetAct = result.getHLProcess().findActivityByName(
						toElementName);
				if (targetAct != null) {
					HLCondition cond = choice
							.addChoiceTarget(targetAct.getID());
					writeDataExpression(predicateNode, cond, result,
							orderingNode);
				} else {
					// (b) try to read condition node and find targets from
					// there
					for (ModelGraphVertex graphNode : result.getGraphNodes()) {
						if (graphNode.getIdentifier().equals(toElementName)) {
							// look for out transitions of this place (as place
							// cannot be a target)
							if (graphNode.getOutEdges() != null) {
								for (Edge outCond : graphNode.getOutEdges()) {
									ModelGraphVertex outTask = (ModelGraphVertex) outCond
											.getHead();
									targetAct = result.findActivity(outTask);
									HLCondition cond = choice
											.addChoiceTarget(targetAct.getID());
									if (targetAct != null) {
										writeDataExpression(predicateNode,
												cond, result, orderingNode);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Actually parses and writes the data expression.
	 * <p>
	 * If the predicate node is <code>null</code> it is assumed that this will
	 * be a 'default' (i.e., "else") node.
	 * 
	 * @param predicateNode
	 *            the predicate node holding the XPATH expression
	 * @param cond
	 *            the condition where this expression should be set
	 */
	public static void writeDataExpression(Node predicateNode,
			HLCondition cond, HLYAWL result, Node orderingNode) {
		if (predicateNode != null) {
			String expressionString = predicateNode.getNodeValue();
			expressionString = expressionString.trim();
			expressionString = expressionString.replaceAll(
					"number\\([/A-Za-z]*/(.*)/text\\(\\)\\)", "$1");
			expressionString = expressionString.replaceAll(
					"boolean\\([/A-Za-z]*/(.*)/text\\(\\)\\)", "$1");
			expressionString = expressionString.replaceAll(
					"[/A-Za-z]*/(.*)/text\\(\\)", "$1");
			HLExpressionOperator operator = null;
			if (expressionString.contains(">")) {
				operator = new HLGreaterOperator();
				String[] operands = expressionString.split(">");
				parseExpressionOperands(operands, operator, result);
			} else if (expressionString.contains("<")) {
				operator = new HLSmallerOperator();
				String[] operands = expressionString.split("<");
				parseExpressionOperands(operands, operator, result);
			} else if (expressionString.contains("<=")) {
				operator = new HLSmallerEqualOperator();
				String[] operands = expressionString.split("<=");
				parseExpressionOperands(operands, operator, result);
			} else if (expressionString.contains(">=")) {
				operator = new HLGreaterEqualOperator();
				String[] operands = expressionString.split(">=");
				parseExpressionOperands(operands, operator, result);
			} else if (expressionString.contains("=")) {
				operator = new HLEqualOperator();
				String[] operands = expressionString.split("=");
				parseExpressionOperands(operands, operator, result);
			} else if (expressionString.contains("!=")) {
				operator = new HLNotEqualOperator();
				String[] operands = expressionString.split("!=");
				parseExpressionOperands(operands, operator, result);
			}
			// try to read ordering node
			if (orderingNode != null) {
				try {
					long numericValue = Long.parseLong(orderingNode
							.getNodeValue());
					cond.setExpression(new HLDataExpression(operator,
							(int) numericValue));
				} catch (NumberFormatException ex) {
					// do nothing as is simply not numeric
				}
			} else {
				cond.setExpression(new HLDataExpression(operator));
			}
		} else {
			cond.setExpression("default");
		}
	}

	/**
	 * Parse expression operands and add them to operator expression node.
	 * 
	 * @param operands
	 *            the oparands to be parsed
	 * @param operator
	 *            the operator to which the operands should be added
	 * @param result
	 *            the high level process for accessing attribute information
	 */
	public static void parseExpressionOperands(String[] operands,
			HLExpressionOperator operator, HLYAWL result) {
		for (int i = 0; i < operands.length; i++) {
			String operand = operands[i].trim();
			// remove surrounding '' around a value
			operand = operand.replaceAll("'(.*)'", "$1");
			// (a) is numeric value
			try {
				long numericValue = Long.parseLong(operand);
				HLAttributeValue hlValue = new HLNumericValue(
						(int) numericValue);
				HLValueOperand hlOp = new HLValueOperand(hlValue);
				operator.addSubElement(hlOp);
			} catch (NumberFormatException ex) {
				// do nothing as is simply not numeric
			}

			// (b) is attribute value
			HLAttribute hlAtt = result.getHLProcess().findAttributeByName(
					operand);
			if (hlAtt != null) {
				HLAttributeOperand hlOp = new HLAttributeOperand(hlAtt.getID(),
						result.getHLProcess());
				operator.addSubElement(hlOp);
				// (c) is boolean value
			} else if (operand.equals("true") || operand.equals("false")) {
				boolean value = Boolean.parseBoolean(operand);
				HLAttributeValue hlValue = new HLBooleanValue(value);
				HLValueOperand hlOp = new HLValueOperand(hlValue);
				operator.addSubElement(hlOp);
				// (d) else: is nominal (string) value
			} else {
				HLAttributeValue hlValue = new HLNominalValue(operand);
				HLValueOperand hlOp = new HLValueOperand(hlValue);
				operator.addSubElement(hlOp);
			}
		}
	}

	/**
	 * Parses the name of the output data element and assign to corresponding
	 * task.
	 * 
	 * @param varNode
	 *            the "ouputParam" node
	 * @param result
	 *            high level yawl model to be filled
	 * @param act
	 *            the high level activity that provides this attribute
	 */
	public static void addDataAttributeToHLActivity(Node varNode,
			HLYAWL result, HLActivity act) {
		String name = ""; // always there
		NodeList nodes = varNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("name")) {
				name = node.getTextContent();
			}
		}
		HLAttribute hlAtt = result.getHLProcess().findAttributeByName(name);
		if (hlAtt != null) {
			act.addOutputDataAttribute(hlAtt.getID());
		}
	}

	/**
	 * Parses the data variable element.
	 * 
	 * @param varNode
	 *            the "localVariable" node
	 * @param result
	 *            the HLYawl model to be filled
	 */
	public static void parseDataVariable(Node varNode, HLYAWL result) {
		String name = ""; // always there
		String type = ""; // always there
		String initialValue = null; // only for localVariable
		NodeList nodes = varNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("name")) {
				name = node.getTextContent();
			} else if (node.getNodeName().equals("type")) {
				type = node.getTextContent();
			} else if (node.getNodeName().equals("initialValue")
					|| node.getNodeName().equals("defaultValue")) {
				initialValue = node.getTextContent();
			}
		}
		// read type
		if (type.equals("boolean")) {
			HLBooleanAttribute boolAtt = new HLBooleanAttribute(name, result
					.getHLProcess());
			if (initialValue != null) {
				// read the provided initial value
				boolean initVal = Boolean.getBoolean(initialValue);
				boolAtt.setInitialValue(initVal);
			} else {
				// link the initial value to the distribution of possible values
				// i.e., make it random
				boolAtt.useInitialValue(false);
			}
		} else if (type.endsWith("double") || type.equals("long")
				|| type.equals("duration")) {
			HLNumericAttribute numAtt = new HLNumericAttribute(name, result
					.getHLProcess());
			if (initialValue != null) {
				// TODO: check whether next to int also other types could be
				// supported!
				Double parsedInitVal = new Double(initialValue);
				if (parsedInitVal != null) {
					int initVal = parsedInitVal.intValue();
					numAtt.setInitialValue(initVal);
				} else {
					Message.add("Initial or default value for attribute "
							+ name + " could not be read. ", Message.DEBUG);
					// link the initial value to the distribution of possible
					// values
					// i.e., make it random
					numAtt.useInitialValue(false);
				}
			} else {
				// link the initial value to the distribution of possible values
				// i.e., make it random
				numAtt.useInitialValue(false);
			}
			// read all remaining types as nominal attributes
		} else if (type.equals("string") || type.equals("date")
				|| type.equals("time") || type.equals("duration")) {
			HLNominalAttribute nomAtt = new HLNominalAttribute(name, result
					.getHLProcess());
			if (initialValue != null) {
				// read the provided initial value
				nomAtt.setInitialValue(initialValue);
			} else {
				// link the initial value to the distribution of possible values
				// i.e., make it random
				nomAtt.useInitialValue(false);
			}
		}
	}

	/**
	 * Parses the "task" element within the "processControlElements" tag.
	 * 
	 * @param node
	 *            the "task" node
	 * @param result
	 *            the HLYawl model to be filled
	 */
	public static void parseTaskNode(Node node, HLYAWL result) {
		Node idNode = node.getAttributes().getNamedItem("id");
		String name = idNode.getNodeValue();

		NodeList subNodes = node.getChildNodes();
		Node subNode;
		for (int i = 0; i < subNodes.getLength(); i++) {
			subNode = subNodes.item(i);
			// check for resources
			if (subNode.getNodeName().equals("resourcing")) {
				parseResourcing(subNode, result, name);
			}
			// TODO: check whether needed for reading the data mappings
			// from net variables to local variables and vice versa
			// else if (subNode.getNodeName().equals("completedMappings")) {
			// parseDataMapping(subNode, result, name);
			// }
		}
	}

	/**
	 * Parses the "resourcing" element within the "task" tag.
	 * 
	 * @param node
	 *            the "resourcing" node
	 * @param result
	 *            the HLYawl model to be filled
	 */
	public static void parseResourcing(Node node, HLYAWL result, String taskName) {
		NodeList subNodes = node.getChildNodes();
		Node subNode;
		for (int i = 0; i < subNodes.getLength(); i++) {
			subNode = subNodes.item(i);
			if (subNode.getNodeName().equals("offer")) {
				parseOffer(subNode, result, taskName);
			}
		}
	}

	/**
	 * Parses the "offer" element within the "resourcing" tag.
	 * 
	 * @param node
	 *            the "offer" node
	 * @param result
	 *            the HLYawl model to be filled
	 */
	public static void parseOffer(Node node, HLYAWL result, String taskName) {
		NodeList subNodes = node.getChildNodes();
		Node subNode;
		for (int i = 0; i < subNodes.getLength(); i++) {
			subNode = subNodes.item(i);
			if (subNode.getNodeName().equals("distributionSet")) {
				NodeList subSubNodes = subNode.getChildNodes();
				Node subSubNode;
				for (int j = 0; j < subSubNodes.getLength(); j++) {
					subSubNode = subSubNodes.item(j);
					if (subSubNode.getNodeName().equals("initialSet")) {
						NodeList roleNodes = subSubNode.getChildNodes();
						Node roleNode;
						for (int k = 0; k < roleNodes.getLength(); k++) {
							roleNode = roleNodes.item(k);
							// get role into HLProcess
							if (roleNode.getNodeName().equals("role")) {
								String roleName = roleNode.getTextContent();
								// ignore Nobody role tag as default in
								// hlActivity is "nobody"
								// do not create extra group for this
								if (roleName.equals("nobody") == false) {
									HLGroup belongingHlGroup = null;
									HLProcess hlProc = result.getHLProcess();
									// all groups with equal name will be mapped
									// on the same group
									Iterator<HLGroup> groupIt = hlProc
											.getGroups().iterator();
									while (groupIt.hasNext()) {
										HLGroup grp = groupIt.next();
										if (grp.getID().toString().equals(
												roleName)) {
											belongingHlGroup = grp;
										}
									}
									// if role read for the first time -> make
									// new group
									if (belongingHlGroup == null) {
										HLID grpID = new HLID(roleName);
										belongingHlGroup = new HLGroup(
												"Role ID: " + roleName, hlProc,
												grpID);
									}
									// now register with current task
									// TODO Anne: do mapping on the way to avoid
									// getting tasks
									// by name (not necessarily unique)
									Iterator<HLActivity> actIt = hlProc
											.getActivities().iterator();
									while (actIt.hasNext()) {
										HLActivity act = actIt.next();
										if (act.getName().equals(taskName)) {
											act.setGroup(belongingHlGroup
													.getID());
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * TODO Anne: actually relates to the data element that is involved in
	 * expression. check and remove
	 * 
	 * Parses the "completedMappings" element within the "task" tag.
	 * 
	 * @param node
	 *            the "completedMappings" node
	 * @param result
	 *            the HLYawl model to be filled
	 */
	// public static void parseDataMapping(Node node, HLYAWL result, String
	// taskName) {
	// NodeList subNodes = node.getChildNodes();
	// Node subNode;
	// for (int i = 0; i < subNodes.getLength(); i++) {
	// subNode = subNodes.item(i);
	// if (subNode.getNodeName().equals("mapping")) {
	// NodeList subSubNodes = subNode.getChildNodes();
	// Node subSubNode;
	// for (int j = 0; j < subSubNodes.getLength(); j++) {
	// subSubNode = subSubNodes.item(j);
	// if (subSubNode.getNodeName().equals("mapsTo")) {
	// String varName = subSubNode.getTextContent();
	// // match variables by name
	// HLAttribute belongingHlAtt = null;
	// HLProcess hlProc = result.getHLProcess();
	// // all vars with equal name will be mapped on the same att
	// Iterator<HLAttribute> attIt = hlProc.getAttributes().iterator();
	// while (attIt.hasNext()) {
	// HLAttribute att = attIt.next();
	// if (att.getName().equals(varName)) {
	// belongingHlAtt = att;
	// }
	// }
	// // TODO Anne: check whether can happen that the attribute is not
	// // registered as global variable?
	// if (belongingHlAtt != null) {
	// // now register with current task
	// // TODO Anne: do mapping on the way to avoid getting tasks
	// // by name (not necessarily unique)
	// Iterator<HLActivity> actIt = hlProc.getActivities().iterator();
	// while (actIt.hasNext()) {
	// HLActivity act = actIt.next();
	// if (act.getName().equals(taskName)) {
	// act.addDataAttribute(belongingHlAtt.getID());
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	// }
}
