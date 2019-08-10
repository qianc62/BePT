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
import java.util.*;
import javax.xml.parsers.*;
import org.processmining.framework.models.pdm.*;
import org.w3c.dom.*;

/**
 * <p>
 * Title: PDM design reader
 * </p>
 * <p>
 * Description: Parses and reads the incoming PDM design file, loads the
 * corresponding activities into the ProM framework and connects it to the
 * corresponding elements of the PDM model.
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

public class PDMDesignReader {
	public PDMDesignReader() {
		/*
		 * try { jbInit(); } catch (Exception ex) { ex.printStackTrace(); }
		 */
	}

	private String pdmmodel; // the name of the PDM model on which the XML file
	// to be read is defining a process model.
	private PDMModel model; // the PDM model on which the XML file to be read is
	// defining a process model.
	private PDMDesign design;

	/**
	 * Create the reader
	 */
	public PDMDesignReader(PDMModel model) {
		this.model = model;
	}

	/**
	 * Read the PDM file waiting in the given stream, recognize a PDM model and
	 * store it.
	 */
	public PDMProcessModel read(InputStream input, PDMModel model)
			throws Exception {
		this.model = model;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc;
		NodeList nodes, nodes2;

		dbf.setValidating(false);
		dbf.setIgnoringComments(true);
		dbf.setIgnoringElementContentWhitespace(true);

		// Load the XML file as a tree structure
		doc = dbf.newDocumentBuilder().parse(input);

		// check if the first element is a 'ProcessModel' element
		if (!doc.getDocumentElement().getTagName().equals("ProcessModel")) {
			throw new Exception("ProcessModel tag not found");
		}

		// create a PDM process model
		PDMProcessModel pm = new PDMProcessModel();

		// the nodes of the xml-file are stored in variable 'nodes' and are
		// parsed separately according to their meaning
		nodes = null;
		nodes = doc.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("Design")) {
				parseDesign(node, pm);
				// } else if (!node.getNodeName().equals("Design")) {
				// throw new Exception("Design tag not found");
			}
		}

		// when all nodes are parsed the complete model is returned
		return pm;
	}

	/**
	 * Parses an XML node of the type Design
	 * 
	 * @param rootNode
	 *            Node
	 * @param model
	 *            PDMDesign
	 */
	public void parseDesign(Node rootNode, PDMProcessModel pm) {
		Node idnode = rootNode.getAttributes().getNamedItem("DesignID");
		PDMDesign design1 = new PDMDesign(idnode.getNodeValue());
		design = design1;
		pm.addDesign(design);
		NodeList nodes2 = rootNode.getChildNodes();
		Node node;
		for (int i = 0; i < nodes2.getLength(); i++) {
			node = nodes2.item(i);
			if (node.getNodeName().equals("Activity")) {
				parseActivity(node, design);
			}
		}
	}

	/**
	 * Parses an XML node of the type Activity
	 * 
	 * @param rootNode
	 *            Node
	 * @param model
	 *            PDMModel
	 */
	public void parseActivity(Node rootNode, PDMDesign design) {
		Node idnode = rootNode.getAttributes().getNamedItem("ActivityID");
		PDMActivity activity = new PDMActivity(idnode.getNodeValue());
		design.addActivity(activity);
		NodeList nodes3 = rootNode.getChildNodes();
		Node node;
		for (int i = 0; i < nodes3.getLength(); i++) {
			node = nodes3.item(i);
			if (node.getNodeName().equals("Operation")) {
				parseOperation(node, activity);
			}
		}

	}

	/**
	 * Parses an XML node of the type Operation
	 * 
	 * @param rootNode
	 *            Node
	 * @param model
	 *            PDMModel
	 */
	public void parseOperation(Node rootNode, PDMActivity activity) {
		Node refnode = rootNode.getAttributes().getNamedItem("OperationRef");
		// System.out.println(refnode);

		// The design should be connected to the already loaded PDM model.
		// Therefore, the operationrefs are linked to the operation-objects
		// in the PDM model.
		PDMOperation operation = model.getOperation(refnode.getNodeValue());
		if (operation != null) {
			activity.addOperation(operation);
		} else
			System.out.println("activity is null");
	}

	public PDMDesign getDesign() {
		return this.design;
	}

}
