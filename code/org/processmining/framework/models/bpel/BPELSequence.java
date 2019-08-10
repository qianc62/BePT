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
import org.processmining.framework.models.ModelGraphEdge;
import java.util.HashSet;

/**
 * <p>
 * Title: BPELSequence
 * </p>
 * 
 * <p>
 * Description: CLass for a BPEL sequence activity.
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
public class BPELSequence extends BPELStructured {

	public BPELSequence(Element element) {
		super(element);
	}

	public BPELSequence(String name) {
		super(BPELConstants.stringSequence, name);
	}

	public BPELSequence cloneActivity() {
		BPELSequence clone = new BPELSequence(element
				.getAttribute(BPELConstants.stringName));
		for (BPELActivity activity : getActivities()) {
			clone.appendChildActivity(activity.cloneActivity());
		}
		clone.cloneLinks(this);
		return clone;
	}

	public void acceptVisitor(BPELVisitor visitor) {
		visitor.visit(this);
	}

	public void buildModelGraph(BPEL model) {
		ModelGraphVertex dummy = new ModelGraphVertex(model);
		model.addDummy(dummy);
		dummy.setDotAttribute("shape", "circle");
		dummy.setDotAttribute("label", "");
		for (BPELActivity activity : getActivities()) {
			activity.vertex = new ModelGraphVertex(model);
			model.addVertex(activity.vertex);
			activity.SetActivityAttributes();

			ModelGraphEdge edge = new ModelGraphEdge(dummy, activity.vertex);
			model.addDummy(edge);

			dummy = new ModelGraphVertex(model);
			model.addDummy(dummy);
			dummy.setDotAttribute("shape", "circle");
			dummy.setDotAttribute("label", "");

			edge = new ModelGraphEdge(activity.vertex, dummy);
			model.addDummy(edge);
		}
		addLinksToModelGraph(model, new HashSet<BPELActivity>(getActivities()));
	}
}
