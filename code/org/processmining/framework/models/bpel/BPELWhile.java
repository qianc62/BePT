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

import org.w3c.dom.Element;

import org.processmining.framework.models.bpel.visit.*;
import org.processmining.framework.models.ModelGraphVertex;
import java.util.HashSet;
import org.processmining.framework.models.ModelGraphEdge;

/**
 * <p>
 * Title: BPELWhile
 * </p>
 * 
 * <p>
 * Description: Class for a BPEL while acitivity.
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
public class BPELWhile extends BPELStructured {

	public BPELWhile(Element element) {
		super(element);
	}

	public BPELWhile(String name) {
		super(BPELConstants.stringWhile, name);
	}

	public BPELWhile cloneActivity() {
		BPELWhile clone = new BPELWhile(element
				.getAttribute(BPELConstants.stringName));
		for (BPELActivity activity : getActivities()) {
			clone.appendChildActivity(activity.cloneActivity());
		}
		clone.cloneLinks(this);
		clone.appendChildActivity(getActivity().cloneActivity());
		return clone;
	}

	public void acceptVisitor(BPELVisitor visitor) {
		visitor.visit(this);
	}

	public BPELActivity getActivity() {
		ArrayList<BPELActivity> activities = this.getActivities();
		return activities.get(0);
	}

	public void buildModelGraph(BPEL model) {
		String condition = element.getAttribute("condition");
		if (condition == null) {
			condition = "";
		}

		ModelGraphVertex pin = new ModelGraphVertex(model);
		model.addDummy(pin);
		pin.setDotAttribute("shape", "circle");
		pin.setDotAttribute("label", "");
		ModelGraphVertex pout = new ModelGraphVertex(model);
		model.addDummy(pout);
		pout.setDotAttribute("shape", "circle");
		pout.setDotAttribute("label", "");

		ModelGraphVertex t = new ModelGraphVertex(model);
		model.addDummy(t);
		t.setDotAttribute("shape", "box");
		t.setDotAttribute("label", "");

		ModelGraphVertex p = new ModelGraphVertex(model);
		model.addDummy(p);
		p.setDotAttribute("shape", "circle");
		p.setDotAttribute("label", "");

		ModelGraphVertex u = new ModelGraphVertex(model);
		model.addDummy(u);
		u.setDotAttribute("shape", "box");
		u.setDotAttribute("label", condition);

		ModelGraphVertex q = new ModelGraphVertex(model);
		model.addDummy(q);
		q.setDotAttribute("shape", "circle");
		q.setDotAttribute("label", "");

		BPELActivity activity = getActivity();
		activity.vertex = new ModelGraphVertex(model);
		model.addVertex(activity.vertex);
		activity.SetActivityAttributes();

		ModelGraphEdge edge = new ModelGraphEdge(pin, t);
		model.addDummy(edge);
		edge = new ModelGraphEdge(t, p);
		model.addDummy(edge);
		edge = new ModelGraphEdge(p, u);
		model.addDummy(edge);
		edge = new ModelGraphEdge(u, pout);
		model.addDummy(edge);
		edge.setDotAttribute("label", "no");
		edge = new ModelGraphEdge(u, q);
		model.addDummy(edge);
		edge.setDotAttribute("label", "yes");
		edge = new ModelGraphEdge(q, activity.vertex);
		model.addDummy(edge);
		edge = new ModelGraphEdge(activity.vertex, p);
		model.addDummy(edge);

		addLinksToModelGraph(model, new HashSet<BPELActivity>(getActivities()));
	}
}
