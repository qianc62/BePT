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

package org.processmining.framework.models.bpel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.ui.Message;
import org.w3c.dom.DOMImplementation;

/**
 * <p>
 * Title: BPEL
 * </p>
 * 
 * <p>
 * Description: CLass for BPEL models
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class BPEL extends ModelGraph {

	/**
	 * Stuff needed to create documents.
	 */
	public static DocumentBuilderFactory staticFactory;
	public static DocumentBuilder staticBuilder;
	public static DOMImplementation staticImplementation;

	/**
	 * Needed to be able to create elements. (To create an element, we need a
	 * document.)
	 */
	public static Document staticDocument;

	/**
	 * The document underlyin the BPEL model.
	 */
	private Document document;

	/**
	 * Whether the BPEL model was constructed from a document. Otherwise, it is
	 * constructed from scratch.
	 */
	private boolean fromDocument;

	/**
	 * The process of the model.
	 */
	private BPELProcess process;

	/**
	 * Create a new BPEL model with the given name
	 * 
	 * @param name
	 *            String
	 */
	public BPEL(String name) {
		super(name);
		// FindBugs sais: Write to static field from instance method
		staticFactory = DocumentBuilderFactory.newInstance();
		try {
			// Create a document.
			// FindBugs sais: Write to static field from instance method
			staticBuilder = staticFactory.newDocumentBuilder();
			// FindBugs sais: Write to static field from instance method
			staticImplementation = staticBuilder.getDOMImplementation();
			// FindBugs sais: Write to static field from instance method
			staticDocument = document = staticImplementation.createDocument(
					"http://schemas.xmlsoap.org/ws/2003/03/business-process/",
					"process", null);
			// Set the name attribute of the root element.
			document.getDocumentElement().setAttribute(
					BPELConstants.stringName, name);
		} catch (Exception ex) {
			Message.add(
					"Error while constructing document: " + ex.getMessage(),
					Message.ERROR);
		}
		// Set the process of the model (assuming that one exists).
		// this.process = process;
		// Created from scratch.
		fromDocument = false;
	}

	/**
	 * Create a new BPEL model from a given document.
	 * 
	 * @param document
	 *            Document The given document.
	 */
	public BPEL(Document document) {
		// Set the name of the modelgraph, to keep Java happy :-).
		super(document.getLocalName());
		// Set the underlying document.
		this.document = document;
		// Created from a document.
		fromDocument = true;
	}

	/**
	 * Set the process of the model.
	 * 
	 * @param process
	 *            BPELProcess The process.
	 * 
	 *            Has not been tested extensively, might need some work.
	 */
	public void setProcess(BPELProcess process) {
		this.process = process;
		if (!fromDocument && process.getActivity() != null) {
			document.getDocumentElement().appendChild(
					process.getActivity().getElement());
		}
	}

	/**
	 * Get the name of the BPEL model.
	 * 
	 * @return String The name of the BPEL model.
	 */
	public String getName() {
		return document.getDocumentElement().getAttribute(
				BPELConstants.stringName);
	}

	/**
	 * Get the process of the BPEL model.
	 * 
	 * @return BPELProcess The process of the BPEL model.
	 */
	public BPELProcess getProcess() {
		return process;
	}

	/**
	 * Get the document of the BPEL model.
	 * 
	 * @return Document The document of the BPEL model.
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Get a string representation of the BPEL model.
	 * 
	 * @return String A string representation of the BPEL model.
	 */
	public String toString() {
		return document.toString();
	}

	public void initModelGraph() {
		clearGraph();
		setDotAttribute("ranksep", ".3");
		setDotAttribute("margin", "0.0,0.0");
		setDotAttribute("rankdir", "TB");
		setDotNodeAttribute("height", ".3");
		setDotNodeAttribute("width", ".3");
		setDotEdgeAttribute("arrowsize", ".5");
	}
}
