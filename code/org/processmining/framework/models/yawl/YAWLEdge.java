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

package org.processmining.framework.models.yawl;

import java.io.*;

import org.processmining.framework.models.*;
import java.util.HashMap;

/**
 * <p>
 * Title: YAWL edge
 * </p>
 * <p>
 * Description: Holds a YAWL edge
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */

public class YAWLEdge extends ModelGraphEdge {

	// Types of YAWL edges
	public static final int NORMAL = 0; // Normal
	public static final int RESET = 1; // Reset (cancellation)

	private int type;
	private boolean isDefaultFlow;
	private String predicate;
	private int ordering;

	/**
	 * Create a normal YAWL edge with given source, destination, predicate,
	 * ordering, and whether it is a default edge.
	 * 
	 * @param fromVertex
	 *            The given source node
	 * @param toVertex
	 *            The given destination node
	 * @param isDefaultFlow
	 *            Whether it is a default edge
	 * @param predicate
	 *            The given predicate
	 * @param ordering
	 *            The given predicate ordering
	 */
	public YAWLEdge(YAWLNode fromNode, YAWLNode toNode, boolean isDefaultFlow,
			String predicate, String ordering) {
		super(fromNode, toNode);
		init(isDefaultFlow, predicate, ordering == null ? 0 : Integer.valueOf(
				ordering).intValue());
	}

	/**
	 * Create a reset YAWL edge with given source and destination.
	 * 
	 * @param fromVertex
	 *            The given source node
	 * @param toVertex
	 *            The given destination node
	 */
	public YAWLEdge(YAWLNode fromNode, YAWLNode toNode) {
		super(fromNode, toNode);
		init();
	}

	public YAWLEdge(YAWLNode fromNode, YAWLNode toNode, YAWLEdge edge) {
		super(fromNode, toNode);
		switch (edge.type) {
		case NORMAL: {
			init(edge.isDefaultFlow, edge.predicate, edge.ordering);
			break;
		}
		case RESET: {
			init();
			break;
		}
		}
	}

	private void init(boolean isDefaultFlow, String predicate, int ordering) {
		this.type = NORMAL;
		this.isDefaultFlow = isDefaultFlow;
		this.predicate = predicate;
		this.ordering = ordering;

		if (isDefaultFlow) {
			setDotAttribute("label", "[default]");
			setDotAttribute("color", "green");
		} else if (predicate != null) {
			setDotAttribute("label", this.predicate + " [" + this.ordering
					+ "]");
		}
	}

	private void init() {
		this.type = RESET;
		this.isDefaultFlow = false;
		this.predicate = null;
		this.ordering = 0;

		setDotAttribute("constraint", "false");
		setDotAttribute("style", "dashed");
		setDotAttribute("color", "red");
	}

	/**
	 * Returns whether this is a normal edge.
	 * 
	 * @return Whther a normal edge
	 */
	public boolean isNormal() {
		return type == NORMAL;
	}

	public boolean isDefaultFlow() {
		return isDefaultFlow;
	}

	public String getPredicate() {
		return predicate;
	}

	public int getOrdering() {
		return ordering;
	}

	public void setType(int type) {
		switch (type) {
		case NORMAL: {
			init(this.isDefaultFlow, this.predicate, this.ordering);
			break;
		}
		case RESET: {
			init();
			break;
		}
		}
	}

	/**
	 * Export to YAWL file.
	 * 
	 * @param splitType
	 *            int The split type of the originating YAWL node.
	 * @return String The string to export for this YAWLDecompositon.
	 */
	public String writeToYAWL(int splitType, int type) {
		String s = "";
		if (type != this.type) {
			return "";
		} else if (type == NORMAL) {
			s += "\t\t\t\t\t<flowsInto>\n";
			s += "\t\t\t\t\t\t<nextElementRef id=\"Node" + getDest().getId()
					+ "\"/>\n";
			if (predicate != null && predicate.length() > 0) {
				boolean hasPredicate = false;
				if (splitType == YAWLTask.XOR) {
					s += "\t\t\t\t\t\t<predicate ordering=\"" + ordering
							+ "\">";
					hasPredicate = true;
				} else if (splitType == YAWLTask.OR) {
					s += "\t\t\t\t\t\t<predicate>";
					hasPredicate = true;
				}
				if (hasPredicate) {
					// Predicate might contain special characters. Have them
					// replaced.
					s += predicate.replaceAll("&", "&amp;").replaceAll("<",
							"&lt;").replaceAll(">", "&gt;");
					s += "</predicate>\n";
				}
			}
			if (isDefaultFlow) {
				s += "\t\t\t\t\t\t<isDefaultFlow/>\n";
			}
			s += "\t\t\t\t\t</flowsInto>\n";
		} else if (type == RESET) {
			s += "\t\t\t\t\t<removesTokens id=\"Node" + getDest().getId()
					+ "\"/>\n";
		}
		return s;
	}
}
