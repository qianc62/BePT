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
 * Title: BPELScope
 * </p>
 * 
 * <p>
 * Description: Class for a BPEL scope activity.
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
public class BPELScope extends BPELStructured {

	/*
	 * To do: event handlers, fault handlers, compensation handlers.
	 */

	public BPELScope(Element element) {
		super(element);
	}

	public BPELScope(String name) {
		super(BPELConstants.stringScope, name);
	}

	public BPELScope cloneActivity() {
		BPELScope clone = new BPELScope(element
				.getAttribute(BPELConstants.stringName));
		clone.cloneLinks(this);
		for (BPELActivity activity : getActivities()) {
			clone.appendChildActivity(activity.cloneActivity());
		}
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
		ModelGraphVertex pin = new ModelGraphVertex(model);
		model.addDummy(pin);
		pin.setDotAttribute("shape", "circle");
		pin.setDotAttribute("label", "");
		ModelGraphVertex pout = new ModelGraphVertex(model);
		model.addDummy(pout);
		pout.setDotAttribute("shape", "circle");
		pout.setDotAttribute("label", "");

		BPELActivity activity = getActivity();

		activity.vertex = new ModelGraphVertex(model);
		model.addVertex(activity.vertex);
		activity.SetActivityAttributes();

		ModelGraphEdge edge = new ModelGraphEdge(pin, activity.vertex);
		model.addDummy(edge);
		edge = new ModelGraphEdge(activity.vertex, pout);
		model.addDummy(edge);

		addLinksToModelGraph(model, new HashSet<BPELActivity>(getActivities()));
	}
}
