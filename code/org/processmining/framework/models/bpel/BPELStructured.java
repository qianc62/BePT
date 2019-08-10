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
import java.util.HashMap;
import java.util.HashSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Title: BPELStructured
 * </p>
 * 
 * <p>
 * Description: Superclass for a structured BPEL activity
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
public abstract class BPELStructured extends BPELActivity {

	/**
	 * List with child activities, and whether it is valid.
	 */
	private ArrayList<BPELActivity> activities;

	private boolean activitiesOK = false;

	/**
	 * Backmapping from child elements to child activities. Note that the switch
	 * and pick have their own mappings.
	 */
	private HashMap<Element, BPELActivity> map;

	/**
	 * Create a structred BPEL activity from an element.
	 * 
	 * @param element
	 *            Element
	 */
	public BPELStructured(Element element) {
		super(element);
		map = new HashMap<Element, BPELActivity>();
	}

	/**
	 * Count the activities with given tag name in the structred BPEL activity
	 * and its descendants.
	 * 
	 * @param name
	 *            String The given tag name.
	 * @return int The number of activities with given tag name in the structred
	 *         BPEL activity and its descendants.
	 */
	public int countActivities(String name) {
		int count = 0;
		for (BPELActivity activity : getActivities()) {
			count += activity.countActivities(name);
		}
		return count
				+ (BPELConstants.endsWith(element.getTagName(), name) ? 1 : 0);
	}

	/**
	 * Create a child activity for every recognized child element, recursively.
	 */
	public void hookupActivities() {
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode instanceof Element) {
				BPELActivity activity = null;
				Element childElement = (Element) childNode;
				String tagName = childElement.getTagName();
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
				if (activity != null) {
					// Set the backmap.
					map.put(childElement, activity);
					// The recursive part.
					if (activity instanceof BPELStructured) {
						((BPELStructured) activity).hookupActivities();
					}
				}
			}
		}
	}

	/**
	 * Create a structred BPEL activity, given its tagName and name.
	 * 
	 * @param tagName
	 *            String The given tagName.
	 * @param name
	 *            String The given name.
	 */
	public BPELStructured(String tagName, String name) {
		super(tagName, name);
		map = new HashMap<Element, BPELActivity>();
	}

	/**
	 * Append a child activity.
	 * 
	 * @param activity
	 *            BPELActivity The child activity.
	 */
	public void appendChildActivity(BPELActivity activity) {
		activitiesOK = false;
		element.appendChild(activity.getElement());
		map.put(activity.getElement(), activity);
	}

	public void removeChildActivity(BPELActivity activity) {
		activitiesOK = false;
		element.removeChild(activity.getElement());
		map.remove(activity.getElement());
	}

	public void removeAllChildActivities() {
		for (BPELActivity activity : new ArrayList<BPELActivity>(
				getActivities())) {
			removeChildActivity(activity);
		}
	}

	/**
	 * Get a list of all child activites.
	 * 
	 * @return ArrayList The child activities.
	 */
	public ArrayList<BPELActivity> getActivities() {
		if (!activitiesOK) {
			activitiesOK = true;
			activities = new ArrayList<BPELActivity>();
			NodeList childNodes = element.getChildNodes();
			int n = childNodes.getLength();
			for (int i = 0; i < n; i++) {
				Node childNode = childNodes.item(i);
				if (childNode instanceof Element) {
					Element childElement = (Element) childNode;
					if (map.containsKey(childElement)) {
						activities.add(map.get(childElement));
					}
				}
			}
		}
		return activities;
	}

	/**
	 * Get the outgoing links that are not incoming links of this activity and
	 * its descendants.
	 * 
	 * @return ArrayList The links.
	 */
	public ArrayList<String> getFamilySources() {
		ArrayList familySources = new ArrayList(getSources());
		ArrayList familyTargets = new ArrayList(getTargets());
		for (BPELActivity activity : getActivities()) {
			familySources.addAll(activity.getFamilySources());
		}
		for (BPELActivity activity : getActivities()) {
			familyTargets.addAll(activity.getFamilyTargets());
		}
		familySources.removeAll(familyTargets);
		return familySources;
	}

	/**
	 * Get the incoming links that are not outgoing links of this activity and
	 * its descendants.
	 * 
	 * @return ArrayList The links.
	 */
	public ArrayList<String> getFamilyTargets() {
		ArrayList familySources = new ArrayList(getSources());
		ArrayList familyTargets = new ArrayList(getTargets());
		for (BPELActivity activity : getActivities()) {
			familySources.addAll(activity.getFamilySources());
		}
		for (BPELActivity activity : getActivities()) {
			familyTargets.addAll(activity.getFamilyTargets());
		}
		familyTargets.removeAll(familySources);
		return familyTargets;
	}

	public ArrayList<String> getAllSources() {
		ArrayList allSources = new ArrayList(getSources());
		for (BPELActivity activity : getActivities()) {
			allSources.addAll(activity.getAllSources());
		}
		return allSources;
	}

	public ArrayList<String> getAllTargets() {
		ArrayList allTargets = new ArrayList(getTargets());
		for (BPELActivity activity : getActivities()) {
			allTargets.addAll(activity.getAllTargets());
		}
		return allTargets;
	}

	public abstract BPELStructured cloneActivity();

	public abstract void buildModelGraph(BPEL model);

	public void SetActivityAttributes() {
		super.SetActivityAttributes();
		vertex.setDotAttribute("peripheries", "2");
	}
}
