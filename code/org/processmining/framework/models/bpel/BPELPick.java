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

import java.util.HashMap;

import org.w3c.dom.Element;

import org.processmining.framework.models.bpel.visit.*;
import org.processmining.framework.models.bpel.util.Pair;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import org.processmining.framework.models.ModelGraphVertex;
import java.util.HashSet;
import org.processmining.framework.models.ModelGraphEdge;

/**
 * <p>
 * Title: BPELPick
 * </p>
 * 
 * <p>
 * Description: Class for a BPEL pick activity.
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
public class BPELPick extends BPELStructured {

	/**
	 * Use own activity list and backmap, as the child activities of a pick
	 * activity are its grandchild elements (and not its child elements).
	 */
	private ArrayList<BPELActivity> activities;
	private boolean activitiesOK = false;
	private HashMap<Element, BPELActivity> map;

	/**
	 * Map child activity to either a message (onMessage) or a pair of for-until
	 * (onAlarm).
	 */
	public HashMap<BPELActivity, String> messages;
	public HashMap<BPELActivity, Pair<String, String>> alarms;

	public BPELPick(Element element) {
		super(element);
		map = new HashMap<Element, BPELActivity>();
		messages = new HashMap<BPELActivity, String>();
		alarms = new HashMap<BPELActivity, Pair<String, String>>();
	}

	public BPELPick(String name) {
		super(BPELConstants.stringPick, name);
		messages = new HashMap<BPELActivity, String>();
		alarms = new HashMap<BPELActivity, Pair<String, String>>();
	}

	public BPELPick cloneActivity() {
		BPELPick clone = new BPELPick(element
				.getAttribute(BPELConstants.stringName));
		clone.cloneLinks(this);
		for (BPELActivity activity : messages.keySet()) {
			clone.getMessages().put(activity, messages.get(activity));
		}
		for (BPELActivity activity : alarms.keySet()) {
			clone.getAlarms().put(activity, alarms.get(activity));
		}
		return clone;
	}

	public void acceptVisitor(BPELVisitor visitor) {
		visitor.visit(this);
	}

	public HashMap<BPELActivity, String> getMessages() {
		return messages;
	}

	public HashMap<BPELActivity, Pair<String, String>> getAlarms() {
		return alarms;
	}

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
					NodeList grandChildNodes = childElement.getChildNodes();
					int m = grandChildNodes.getLength();
					for (int j = 0; j < m; j++) {
						Node grandChildNode = grandChildNodes.item(j);
						if (grandChildNode instanceof Element) {
							Element grandChildElement = (Element) grandChildNode;
							if (map.containsKey(grandChildElement)) {
								activities.add(map.get(grandChildElement));
							}
						}
					}
				}
			}
		}
		return activities;
	}

	public void hookupActivities() {
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode instanceof Element) {
				Element childElement = (Element) childNode;
				String tagName = childElement.getTagName();
				String message = null;
				String alarmFor = null;
				String alarmUntil = null;
				if (BPELConstants.isOnMessage(tagName)) {
					// Create a message string.
					message = "message";
					String partnerLink = childElement
							.getAttribute("partnerLink");
					if (partnerLink != null) {
						message += " partnerLink=\"" + partnerLink + "\"";
					}
					String portType = childElement.getAttribute("portType");
					if (portType != null) {
						message += " portType=\"" + portType + "\"";
					}
					String operation = childElement.getAttribute("operation");
					if (operation != null) {
						message += " operation=\"" + operation + "\"";
					}
					String variable = childElement.getAttribute("variable");
					if (variable != null) {
						message += " variable=\"" + variable + "\"";
					}
				} else if (BPELConstants.isOnAlarm(tagName)) {
					// Get for and until.
					alarmFor = childElement.getAttribute("for");
					alarmUntil = childElement.getAttribute("until");
				}
				if (message != null || alarmFor != null || alarmUntil != null) {
					NodeList grandChildNodes = childElement.getChildNodes();
					for (int j = 0; j < grandChildNodes.getLength(); j++) {
						Node grandChildNode = grandChildNodes.item(j);
						if (grandChildNode instanceof Element) {
							BPELActivity activity = null;
							Element grandChildElement = (Element) grandChildNode;
							tagName = grandChildElement.getTagName();
							if (BPELConstants.isAssign(tagName)) {
								activity = new BPELAssign(grandChildElement);
							} else if (BPELConstants.isEmpty(tagName)) {
								activity = new BPELEmpty(grandChildElement);
							} else if (BPELConstants.isFlow(tagName)) {
								activity = new BPELFlow(grandChildElement);
							} else if (BPELConstants.isInvoke(tagName)) {
								activity = new BPELInvoke(grandChildElement);
							} else if (BPELConstants.isPick(tagName)) {
								activity = new BPELPick(grandChildElement);
							} else if (BPELConstants.isReceive(tagName)) {
								activity = new BPELReceive(grandChildElement);
							} else if (BPELConstants.isReply(tagName)) {
								activity = new BPELReply(grandChildElement);
							} else if (BPELConstants.isScope(tagName)) {
								activity = new BPELScope(grandChildElement);
							} else if (BPELConstants.isSequence(tagName)) {
								activity = new BPELSequence(grandChildElement);
							} else if (BPELConstants.isSwitch(tagName)) {
								activity = new BPELSwitch(grandChildElement);
							} else if (BPELConstants.isWait(tagName)) {
								activity = new BPELWait(grandChildElement);
							} else if (BPELConstants.isWhile(tagName)) {
								activity = new BPELWhile(grandChildElement);
							}
							if (activity != null) {
								// appendChildActivity2(activity);
								activitiesOK = false;
								map.put(activity.getElement(), activity);
								if (message != null) {
									messages.put(activity, message);
								} else {
									alarms.put(activity, Pair.create(alarmFor,
											alarmUntil));
								}
								if (activity instanceof BPELStructured) {
									((BPELStructured) activity)
											.hookupActivities();
								}
							}
						}
					}
				}
			}
		}
	}

	public void buildModelGraph(BPEL model) {
		ModelGraphVertex pin = new ModelGraphVertex(model);
		model.addDummy(pin);
		pin.setDotAttribute("shape", "circle");
		pin.setDotAttribute("label", "");
		ModelGraphVertex pout = new ModelGraphVertex(model);
		model.addDummy(pout);
		pout.setDotAttribute("shape", "circle");
		pout.setDotAttribute("label", "");

		for (BPELActivity activity : getActivities()) {
			activity.vertex = new ModelGraphVertex(model);
			model.addVertex(activity.vertex);
			activity.SetActivityAttributes();

			ModelGraphVertex dummy = new ModelGraphVertex(model);
			model.addVertex(dummy);
			dummy.setDotAttribute("shape", "ellipse");
			if (getMessages().containsKey(activity)) {
				dummy.setDotAttribute("label", getMessages().get(activity));
				dummy.setDotAttribute("color", "green");
			} else if (getAlarms().containsKey(activity)) {
				dummy.setDotAttribute("label", getAlarms().get(activity)
						.toString());
				dummy.setDotAttribute("color", "red");
			} else {
				dummy.setDotAttribute("label", activity.getName(false));
			}

			ModelGraphEdge edge = new ModelGraphEdge(pin, activity.vertex);
			model.addDummy(edge);
			edge = new ModelGraphEdge(dummy, activity.vertex);
			model.addDummy(edge);
			edge = new ModelGraphEdge(activity.vertex, pout);
			model.addDummy(edge);
		}
		addLinksToModelGraph(model, new HashSet<BPELActivity>(getActivities()));
	}
}
