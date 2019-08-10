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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import org.processmining.framework.models.ModelGraphVertex;
import java.util.HashSet;
import org.processmining.framework.models.ModelGraphEdge;

/**
 * <p>
 * Title: BPELSwitch
 * </p>
 * 
 * <p>
 * Description: Class for a BPEL switch activity
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
public class BPELSwitch extends BPELStructured {

	/**
	 * Like BPELPick, have own activites and backmap.
	 */
	private ArrayList<BPELActivity> activities;
	private boolean activitiesOK = false;
	private HashMap<Element, BPELActivity> map;

	/**
	 * Map every child activity onto its condition.
	 */
	private HashMap<BPELActivity, String> cases;

	public BPELSwitch(Element element) {
		super(element);
		map = new HashMap<Element, BPELActivity>();
		cases = new HashMap<BPELActivity, String>();
	}

	public BPELSwitch(String name) {
		super(BPELConstants.stringSwitch, name);
		map = new HashMap<Element, BPELActivity>();
		cases = new HashMap<BPELActivity, String>();
	}

	public BPELSwitch cloneActivity() {
		BPELSwitch clone = new BPELSwitch(element
				.getAttribute(BPELConstants.stringName));
		clone.cloneLinks(this);
		for (BPELActivity activity : getActivities()) {
			clone.appendChildActivity(cases.get(activity), activity
					.cloneActivity());
		}
		return clone;
	}

	public void acceptVisitor(BPELVisitor visitor) {
		visitor.visit(this);
	}

	public HashMap<BPELActivity, String> getCases() {
		return cases;
	}

	/**
	 * Append a child activity. Can be either case or otherwise.
	 * 
	 * @param condition
	 *            String The condition. If null, then otherwise, else case.
	 * @param activity
	 *            BPELActivity The child activity.
	 */
	public void appendChildActivity(String condition, BPELActivity activity) {
		activitiesOK = false;
		boolean isOtherwise = condition == null
				|| condition.compareToIgnoreCase("true") == 0;
		// Create a case element, unless the condition is null, in which case we
		// create an otherwise element.
		Element caseElement = BPEL.staticDocument
				.createElement(isOtherwise ? BPELConstants.stringOtherwise
						: BPELConstants.stringCase);
		// The case/otherwise element will be a child of this element.
		element.appendChild(caseElement);
		// Add the condition atribute in case of a case element.
		if (!isOtherwise) {
			element.setAttribute("condition", condition);
		}
		// The element of the given activity will be a child of the
		// case/otherwise element.
		caseElement.appendChild(activity.getElement());
		// Map the element onto the activity.
		map.put(activity.getElement(), activity);
		// Map the activity onto its condition. Use "true" in case of otherwise
		// element.
		if (!isOtherwise) {
			cases.put(activity, condition);
		} else {
			cases.put(activity, "true");
		}
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
				String condition = null;
				if (BPELConstants.isCase(tagName)) {
					condition = childElement.getAttribute("condition");
				} else if (BPELConstants.isOtherwise(tagName)) {
					condition = new String("true");
				}
				if (condition != null) {
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
								cases.put(activity, condition);
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

		ModelGraphVertex t, p = pin;
		ModelGraphEdge edge;
		for (BPELActivity activity : getActivities()) {
			if (getCases().get(activity).equals("true")) {
				p = pin;
			} else {
				t = new ModelGraphVertex(model);
				model.addDummy(t);
				t.setDotAttribute("shape", "box");
				t.setDotAttribute("label", getCases().get(activity));

				p = new ModelGraphVertex(model);
				model.addDummy(p);
				p.setDotAttribute("shape", "circle");
				p.setDotAttribute("label", "");

				edge = new ModelGraphEdge(pin, t);
				model.addDummy(edge);
				edge = new ModelGraphEdge(t, p);
				model.addDummy(edge);

				pin = new ModelGraphVertex(model);
				model.addDummy(pin);
				pin.setDotAttribute("shape", "circle");
				pin.setDotAttribute("label", "");

				edge = new ModelGraphEdge(t, pin);
				model.addDummy(edge);
			}
			activity.vertex = new ModelGraphVertex(model);
			model.addVertex(activity.vertex);
			activity.SetActivityAttributes();

			edge = new ModelGraphEdge(p, activity.vertex);
			model.addDummy(edge);
			edge = new ModelGraphEdge(activity.vertex, pout);
			model.addDummy(edge);
		}
		if (p != pin) {
			ModelGraphVertex empty = new ModelGraphVertex(model);
			model.addDummy(empty);
			empty.setDotAttribute("shape", "box");
			empty.setDotAttribute("label", "empty");

			edge = new ModelGraphEdge(pin, empty);
			model.addDummy(edge);
			edge = new ModelGraphEdge(empty, pout);
			model.addDummy(edge);
		}

		addLinksToModelGraph(model, new HashSet<BPELActivity>(getActivities()));
	}
}
