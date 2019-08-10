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

import org.w3c.dom.Element;

import org.processmining.framework.models.bpel.visit.*;
import org.processmining.framework.models.ModelGraphVertex;
import java.util.HashSet;
import org.processmining.framework.models.ModelGraphEdge;

/**
 * <p>
 * Title: BPELFLow
 * </p>
 * 
 * <p>
 * Description: Class for a BPEL flow activity.
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
public class BPELFlow extends BPELStructured {

	/*
	 * For the time being, we do not register the links in a flow explicitly.
	 * However, we might need to do so in th efuture.
	 */

	public BPELFlow(Element element) {
		super(element);
	}

	public BPELFlow(String name) {
		super(BPELConstants.stringFlow, name);
	}

	public BPELFlow cloneActivity() {
		BPELFlow clone = new BPELFlow(element
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

	public void buildModelGraph(BPEL model) {
		ModelGraphVertex pin = new ModelGraphVertex(model);
		model.addDummy(pin);
		pin.setDotAttribute("shape", "circle");
		pin.setDotAttribute("label", "");
		ModelGraphVertex tin = new ModelGraphVertex(model);
		model.addDummy(tin);
		tin.setDotAttribute("shape", "box");
		tin.setDotAttribute("label", "");
		ModelGraphEdge edge = new ModelGraphEdge(pin, tin);
		model.addDummy(edge);
		ModelGraphVertex pout = new ModelGraphVertex(model);
		model.addDummy(pout);
		pout.setDotAttribute("shape", "circle");
		pout.setDotAttribute("label", "");
		ModelGraphVertex tout = new ModelGraphVertex(model);
		model.addDummy(tout);
		tout.setDotAttribute("shape", "box");
		tout.setDotAttribute("label", "");
		edge = new ModelGraphEdge(tout, pout);
		model.addDummy(edge);
		for (BPELActivity activity : getActivities()) {
			activity.vertex = new ModelGraphVertex(model);
			model.addVertex(activity.vertex);
			activity.SetActivityAttributes();

			pin = new ModelGraphVertex(model);
			model.addDummy(pin);
			pin.setDotAttribute("shape", "circle");
			pin.setDotAttribute("label", "");

			pout = new ModelGraphVertex(model);
			model.addDummy(pout);
			pout.setDotAttribute("shape", "circle");
			pout.setDotAttribute("label", "");

			edge = new ModelGraphEdge(tin, pin);
			model.addDummy(edge);
			edge = new ModelGraphEdge(pin, activity.vertex);
			model.addDummy(edge);
			edge = new ModelGraphEdge(activity.vertex, pout);
			model.addDummy(edge);
			edge = new ModelGraphEdge(pout, tout);
			model.addDummy(edge);
		}
		addLinksToModelGraph(model, new HashSet<BPELActivity>(getActivities()));
	}
}
