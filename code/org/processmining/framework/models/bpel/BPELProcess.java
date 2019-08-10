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

import org.processmining.framework.models.bpel.visit.*;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.util.ArrayList;

/**
 * <p>
 * Title: BPELProcess
 * </p>
 * 
 * <p>
 * Description: Class for a BPEL process
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
public class BPELProcess implements BPELVisitable {

	/**
	 * The BPEL activity of the process.
	 */
	private BPELActivity activity = null;

	/**
	 * The element underlying the BPEL process.
	 */
	private Element element;

	/*
	 * The next two methods are typically used if a BPEL model is imported from
	 * file, which yields a document with (a tree of) elements. On top of this
	 * document and elements, the BPEL activities need to be constructed.
	 */

	/**
	 * Create a new BPEL process from the given element.
	 * 
	 * @param element
	 *            Element This should be the element underlying the BPEL
	 *            process.
	 */
	public BPELProcess(Element element) {
		this.element = element;
	}

	/**
	 * Traverse the underlying element and create a BPEL activity for every
	 * recognized element.
	 */
	public void hookupActivities() {
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength() && activity == null; i++) {
			Node childNode = childNodes.item(i);
			if (childNode instanceof Element) {
				Element childElement = (Element) childNode;
				String tagName = childElement.getTagName(); // Ignore any prefix
				if (BPELConstants.isAssign(tagName)) {
					activity = new BPELAssign(childElement);
				} else if (BPELConstants.isEmpty(tagName)) {
					activity = new BPELEmpty(childElement);
				} else if (BPELConstants.isFlow(tagName)) {
					activity = new BPELFlow(childElement);
				} else if (BPELConstants.isInvoke(tagName)) {
					activity = new BPELInvoke(childElement);
				} else if (BPELConstants.isPick(tagName)) {
					activity = new BPELPick(childElement);
				} else if (BPELConstants.isReceive(tagName)) {
					activity = new BPELReceive(childElement);
				} else if (BPELConstants.isReply(tagName)) {
					activity = new BPELReply(childElement);
				} else if (BPELConstants.isScope(tagName)) {
					activity = new BPELScope(childElement);
				} else if (BPELConstants.isSequence(tagName)) {
					activity = new BPELSequence(childElement);
				} else if (BPELConstants.isSwitch(tagName)) {
					activity = new BPELSwitch(childElement);
				} else if (BPELConstants.isWait(tagName)) {
					activity = new BPELWait(childElement);
				} else if (BPELConstants.isWhile(tagName)) {
					activity = new BPELWhile(childElement);
				}
				if (activity != null && activity instanceof BPELStructured) {
					((BPELStructured) activity).hookupActivities();
				}
			}
		}
	}

	/*
	 * The next three methods are typically used when creating a BPEL model from
	 * scratch, in which case the underlying elements do not exist yet.
	 */

	/**
	 * Create a new BPEL process with given name.
	 * 
	 * @param name
	 *            String The name attribute of the new process.
	 */
	public BPELProcess(String name) {
		element = BPEL.staticDocument.getDocumentElement(); // .createElement(BPELConstants.stringProcess);
		element.setAttribute(BPELConstants.stringName, name);
	}

	/**
	 * Set the child activity.
	 * 
	 * @param activity
	 *            BPELActivity The child activity.
	 */
	public void setChildActivity(BPELActivity activity) {
		// First, remove the current child activity
		if (this.activity != null) {
			element.removeChild(this.activity.getElement());
		}
		// Second, determine whether we need to include a flow to bind any links
		ArrayList<String> sources = activity.getAllSources();
		if (!sources.isEmpty()) {
			// Found sources, therefore, we need to declare links.
			// Check whether the root is a flow.
			if (!(activity instanceof BPELFlow)) {
				// Not a flow, insert a flow as root.
				BPELFlow flowActivity = new BPELFlow("Flow_Links");
				flowActivity.appendChildActivity(activity);
				activity = flowActivity;
			}
			// Now the root is a flow. Add the links.
			Element flowElement = activity.getElement();
			// Mandatory links element
			Element linksElement = flowElement.getOwnerDocument()
					.createElement(BPELConstants.stringLinks);

			Node firstNode = flowElement.getFirstChild();
			Element firstElement = null;
			while (firstNode != null && !(firstNode instanceof Element)) {
				firstNode = firstNode.getNextSibling();
			}
			if (firstNode != null) {
				firstElement = (Element) firstNode;
			}
			if (firstElement != null) {
				flowElement.insertBefore(linksElement, firstElement);
			} else {
				flowElement.appendChild(linksElement);
			}
			for (String source : sources) {
				Element linkElement = flowElement.getOwnerDocument()
						.createElement(BPELConstants.stringLink);
				linkElement.setAttribute(BPELConstants.stringName, source);
				linksElement.appendChild(linkElement);
			}
		}

		// Third, add the activity
		this.activity = activity;
		element.appendChild(activity.getElement());
		// BPEL.staticDocument.getDocumentElement().appendChild(element);
	}

	/**
	 * Set an attribute for the process. Note that we set if in the underlying
	 * element.
	 * 
	 * @param name
	 *            String The name of the attribute
	 * @param value
	 *            String The value of the attribute
	 */
	public void setAttribute(String name, String value) {
		element.setAttribute(name, value);
	}

	/*
	 * General methods.
	 */

	/**
	 * Return a string representation of the process.
	 * 
	 * @return String A string representation of the process.
	 */
	public String toString() {
		return element.toString();
	}

	/**
	 * Accept a visitor.
	 * 
	 * @param visitor
	 *            BPELVisitor The visitor.
	 */
	public void acceptVisitor(BPELVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * Get the value of the name attribute for the process.
	 * 
	 * @return String The value of the name attribute.
	 */
	public String getName() {
		return element.getAttribute(BPELConstants.stringName);
	}

	/**
	 * Get the child activity of the process.
	 * 
	 * @return BPELActivity The child activity.
	 */
	public BPELActivity getActivity() {
		return activity;
	}

	/**
	 * Count the number of activities in the process.
	 * 
	 * @param name
	 *            String The tag name of the activities to count.
	 * @return int The numbe rof activities in ht eproces swith the given tag
	 *         name.
	 */
	public int countActivities(String name) {
		return activity == null ? 0 : activity.countActivities(name);
	}
}
