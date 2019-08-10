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

import java.util.ArrayList;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import org.processmining.framework.models.bpel.visit.*;
import java.util.HashSet;
import java.util.HashMap;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ModelGraphEdge;

/**
 * <p>
 * Title: BPEL activity
 * </p>
 * 
 * <p>
 * Description: Superclass for any BPEL activity
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
public abstract class BPELActivity implements BPELVisitable {

	/**
	 * The underlying element.
	 */
	protected Element element;

	public ModelGraphVertex vertex;

	/**
	 * A list of sources and whether it is valid.
	 */
	private ArrayList<String> sources;
	private boolean sourcesOK = false;

	/**
	 * A list of targets and whether it is valid.
	 */
	private ArrayList<String> targets;
	private boolean targetsOK = false;

	/**
	 * Create a new BPEL activity from hte given element.
	 * 
	 * @param element
	 *            Element The given element.
	 */
	public BPELActivity(Element element) {
		this.element = element;
	}

	/**
	 * Create a new BPEL activity with given tag name and given name.
	 * 
	 * @param tagName
	 *            String The given tag name.
	 * @param name
	 *            String The given name.
	 */
	public BPELActivity(String tagName, String name) {
		element = BPEL.staticDocument.createElement(tagName);
		element.setAttribute(BPELConstants.stringName, name);
	}

	/**
	 * Get the underlying element.
	 * 
	 * @return Element THe underlying element.
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * Get a name for the activity.
	 * 
	 * @param isUnique
	 *            boolean Whether the name should be unique w.r.t. its sibling
	 *            activities.
	 * @return String A name for the activity.
	 */
	public String getName(boolean isUnique) {
		String name;
		String tagName = element.getTagName();
		String attrName = element.getAttribute(BPELConstants.stringName);
		if (isUnique) {
			name = tagName;
			if (attrName == null || attrName.length() == 0) {
				name += " " + attrName;
			}
			// Add a 'brother' number.
			int i = 0;
			Node node = element;
			while (node.getPreviousSibling() != null) {
				i++;
				node = node.getPreviousSibling();
			}
			name += "[" + i;
			// In case of a switch or pick, the sibling activities can also be
			// nephews, in which case the 'brother' number might not be unique.
			// Therefore, we also include a 'nephew' number.
			node = node.getParentNode();
			if (node != null) {
				i = 0;
				while (node.getPreviousSibling() != null) {
					i++;
					node = node.getPreviousSibling();
				}
				name += "," + i;
			}
			name += "]";
		} else {
			if (attrName == null || attrName.length() == 0) {
				name = tagName;
			} else {
				name = attrName;
			}
		}
		return name;
	}

	/**
	 * Count the number of elements in the tree that have the given tag name.
	 * 
	 * @param name
	 *            String The given tag name.
	 * @return int 1 if the underlying element has the given tag name, otherwise
	 *         0.
	 */
	public int countActivities(String name) {
		return BPELConstants.endsWith(element.getTagName(), name) ? 1 : 0;
	}

	/**
	 * Return a string representation of the activity.
	 * 
	 * @return String A string representation of the activity.
	 */
	public String toString() {
		return getName(false);// element.toString();
	}

	/**
	 * Append an attribute with given name an dvalue to the activity.
	 * 
	 * @param name
	 *            String The given name.
	 * @param value
	 *            String The given value.
	 */
	public void appendAttribute(String name, String value) {
		Attr attr = element.getOwnerDocument().createAttribute(name);
		attr.setNodeValue(value);
		element.appendChild(attr);
	}

	/**
	 * Append an outgoing link with given name to the activity.
	 * 
	 * @param name
	 *            String The given name.
	 */
	public void appendSource(String name) {
		sourcesOK = false;
		Element source = element.getOwnerDocument().createElement(
				BPELConstants.stringSource);
		source.setAttribute(BPELConstants.stringLinkName, name);
		element.appendChild(source);
	}

	/**
	 * Append an incoming link with given name to the activity.
	 * 
	 * @param name
	 *            String The given name.
	 */
	public void appendTarget(String name) {
		targetsOK = false;
		Element target = element.getOwnerDocument().createElement(
				BPELConstants.stringTarget);
		target.setAttribute(BPELConstants.stringLinkName, name);
		element.appendChild(target);
	}

	/**
	 * Get a list of all outgoing links of this activity.
	 * 
	 * @return ArrayList A list of all outgoing links.
	 */
	public ArrayList<String> getSources() {
		if (!sourcesOK) {
			sourcesOK = true;
			sources = new ArrayList<String>();
			NodeList nodes = element.getChildNodes();
			int n = nodes.getLength();
			for (int i = 0; i < n; i++) {
				Node node = nodes.item(i);
				if (node instanceof Element) {
					Element element = (Element) node;
					if (BPELConstants.isSource(element.getTagName())) {
						sources.add(element
								.getAttribute(BPELConstants.stringLinkName));
					}
				}
			}
		}
		return sources;
	}

	/**
	 * Get a list of all outgoing links of this activity and all its
	 * descendants. If some link is both an incoming and an outgoing link of the
	 * activity and all its descendants, then it will not be part of the list.
	 * 
	 * @return ArrayList A list of all outgoing links.
	 */
	public ArrayList<String> getFamilySources() {
		return getSources();
	}

	public ArrayList<String> getAllSources() {
		return getSources();
	}

	/**
	 * Get a list of all incoming links of this activity.
	 * 
	 * @return ArrayList A list of all incoming links.
	 */
	public ArrayList<String> getTargets() {
		if (!targetsOK) {
			targetsOK = true;
			targets = new ArrayList<String>();
			NodeList nodes = element.getChildNodes();
			int n = nodes.getLength();
			for (int i = 0; i < n; i++) {
				Node node = nodes.item(i);
				if (node instanceof Element) {
					Element element = (Element) node;
					if (BPELConstants.isTarget(element.getTagName())) {
						targets.add(element
								.getAttribute(BPELConstants.stringLinkName));
					}
				}
			}
		}
		return targets;
	}

	/**
	 * Get a list of all incoming links of this activity and all its
	 * descendants. If some link is both an incoming and an outgoing link of the
	 * activity and all its descendants, then it will not be part of the list.
	 * 
	 * @return ArrayList A list of all incoming links.
	 */
	public ArrayList<String> getFamilyTargets() {
		return getTargets();
	}

	public ArrayList<String> getAllTargets() {
		return getTargets();
	}

	public void addLinksToModelGraph(BPEL model,
			HashSet<BPELActivity> activities) {
		HashMap<BPELActivity, HashSet<String>> sourceMap = new HashMap<BPELActivity, HashSet<String>>();
		HashSet<String> allSources = new HashSet<String>();
		HashMap<BPELActivity, HashSet<String>> targetMap = new HashMap<BPELActivity, HashSet<String>>();
		HashSet<String> allTargets = new HashSet<String>();
		// Get the outgoing links
		for (BPELActivity sourceActivity : activities) {
			HashSet<String> set = new HashSet<String>(sourceActivity
					.getFamilySources());
			sourceMap.put(sourceActivity, set);
			allSources.addAll(set);
		}
		// Get the incoming links
		for (BPELActivity targetActivity : activities) {
			HashSet<String> set = new HashSet<String>(targetActivity
					.getFamilyTargets());
			targetMap.put(targetActivity, set);
			allTargets.addAll(set);
		}

		HashSet<String> allLinks = new HashSet<String>();
		allLinks.addAll(allSources);
		allLinks.addAll(allTargets);

		HashMap<String, ModelGraphVertex> allLinkVertices = new HashMap<String, ModelGraphVertex>();

		// Create a place for every link.
		for (String link : allLinks) {
			ModelGraphVertex linkVertex = new ModelGraphVertex(model);
			model.addVertex(linkVertex);
			allLinkVertices.put(link, linkVertex);
			linkVertex.setDotAttribute("shape", "ellipse");
			linkVertex.setDotAttribute("color", "blue");
			linkVertex.setDotAttribute("label", link);
		}
		// Add an arc for every outgoing link.
		for (BPELActivity activity : activities) {
			for (String link : sourceMap.get(activity)) {
				ModelGraphEdge edge = new ModelGraphEdge(activity.vertex,
						allLinkVertices.get(link));
				model.addDummy(edge);
				edge.setDotAttribute("color", "blue");
			}
		}
		// Add an arc for every incoming link.
		for (BPELActivity activity : activities) {
			for (String link : targetMap.get(activity)) {
				ModelGraphEdge edge = new ModelGraphEdge(allLinkVertices
						.get(link), activity.vertex);
				model.addDummy(edge);
				edge.setDotAttribute("color", "blue");
			}
		}
	}

	public void SetActivityAttributes() {
		vertex.setDotAttribute("shape", "box");
		vertex.setDotAttribute("label", getName(false));
	}

	/**
	 * Clone the activity.
	 * 
	 * @return BPELActivity A shallow clone of the activity.
	 */
	public abstract BPELActivity cloneActivity();

	/**
	 * Clone links.
	 * 
	 * @param activity
	 *            BPELActivity The activity to clone the links from.
	 */
	public void cloneLinks(BPELActivity activity) {
		ArrayList<String> sources = activity.getSources();
		ArrayList<String> targets = activity.getTargets();
		for (String source : sources) {
			appendSource(source);
		}
		for (String target : targets) {
			appendTarget(target);
		}
	}

	public void acceptVisitor(BPELVisitor visitor) {
		visitor.visit(this);
	}

	public void setJoinCondition(String condition) {
		element.setAttribute(BPELConstants.stringJoinCondition, condition);
	}

	public String getJoinCondition() {
		return element.getAttribute(BPELConstants.stringJoinCondition);
	}
}
