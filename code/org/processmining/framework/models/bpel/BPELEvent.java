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

import org.processmining.framework.log.LogEvent;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ModelGraph;

import org.processmining.framework.models.bpel.visit.*;

/**
 * <p>
 * Title: BPELEvent
 * </p>
 * 
 * <p>
 * Description: Superclass for the BPEL event activities (invoke, receive,
 * reply).
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
public abstract class BPELEvent extends BPELActivity {

	/*
	 * These activites could be linked to a log event.
	 */
	private LogEvent event;
	private ModelGraphVertex vertex;

	public BPELEvent(Element element) {
		super(element);
	}

	public BPELEvent(String tagName, String name) {
		super(tagName, name);
	}

	public LogEvent getLogEvent() {
		return event;
	}

	public void setLogEvent(LogEvent event) {
		this.event = event;
	}

	public ModelGraphVertex getVertex() {
		return vertex;
	}

	public void setVertex(ModelGraphVertex vertex) {
		this.vertex = vertex;
	}

	public void newVertex(ModelGraph graph) {
		vertex = new ModelGraphVertex(graph);
	}

	public void acceptVisitor(BPELVisitor visitor) {
		visitor.visit(this);
	}
}
