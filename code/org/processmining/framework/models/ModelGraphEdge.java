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

package org.processmining.framework.models;

import att.grappa.Edge;
import java.util.HashMap;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class ModelGraphEdge extends Edge {

	public Object object2;
	public Edge visualObject;

	protected double value;

	protected String style;

	private HashMap<String, String> attributes;

	public ModelGraphEdge(ModelGraphVertex source,
			ModelGraphVertex destination, String style, float value) {
		super(source.getSubgraph(), source, destination);
		this.value = value;
		this.style = style;
		object = null;
		clearDotAttributes();
	}

	public ModelGraphEdge(ModelGraphVertex source, ModelGraphVertex destination) {
		this(source, destination, "", 0);
		clearDotAttributes();
	}

	public void clearDotAttributes() {
		if (attributes != null) {
			attributes.clear();
			attributes = null;
		}
		attributes = new HashMap<String, String>();
	}

	public void clearDotAttribute(String attribute) {
		attributes.remove(attribute);
	}

	public void setDotAttribute(String attribute, String value) {
		attributes.put(attribute, value);
	}

	public String getDotAttribute(String attribute) {
		return attributes.get(attribute);
	}

	public String getDotAttributes() {
		String result = "";
		String prefix = "";

		for (String attribute : attributes.keySet()) {
			result += prefix + attribute + "=\""
					+ attributes.get(attribute).replaceAll("\"", "\\\\\"")
					+ "\"";
			prefix = ",";
		}
		if (result == "") {
			result = "label=\"\"";
		}
		return result;
	}

	public void setValue(double v) {
		value = v;
	}

	public double getValue() {
		return value;
	}

	public String getStyle() {
		return style;
	}

	public ModelGraphVertex getSource() {
		return (ModelGraphVertex) getTail();
	}

	public ModelGraphVertex getDest() {
		return (ModelGraphVertex) getHead();
	}
}
