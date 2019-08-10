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
import java.util.*;

import org.processmining.framework.log.*;
import org.processmining.framework.models.*;
import org.processmining.framework.models.epcpack.*;

/**
 * <p>
 * Title: YAWL Task
 * </p>
 * <p>
 * Description: Holds a YAWL task
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

public class YAWLTask extends YAWLNode {

	// Possible join/split types.
	public static final int AND = EPCConnector.AND;
	public static final int XOR = EPCConnector.XOR;
	public static final int OR = EPCConnector.OR;
	public static final int NONE = EPCConnector.NONE;

	private int joinType = NONE;
	private int splitType = NONE;
	private String decomposesTo = null; // Name of subdecomposition.
	private LogEvent event = null;

	/**
	 * Create a YAWL task, given its graph, its join type, its split types, its
	 * subdecomposition name, and name.
	 * 
	 * @param g
	 *            The given graph
	 * @param joinType
	 *            The given join type
	 * @param splitType
	 *            The given split type
	 * @param decomposesTo
	 *            The name of the subdecomposition
	 * @param id
	 *            X[\tY], where X is the task name, and Y makes the id unique.
	 */
	public YAWLTask(ModelGraph g, int joinType, int splitType,
			String decomposesTo, String id, LogEvent event) {
		super(g, id);
		init(joinType, splitType, decomposesTo, id, event);
	}

	public YAWLTask(ModelGraph g, YAWLTask task) {
		super(g, task.id);
		init(task.joinType, task.splitType, task.decomposesTo, task.id,
				task.event);
	}

	private void init(int joinType, int splitType, String decomposesTo,
			String id, LogEvent event) {
		this.joinType = joinType;
		this.splitType = splitType;
		this.decomposesTo = decomposesTo == null ? "" : decomposesTo;
		this.event = event;

		String label = name + "\\n" + decomposesTo; // Stack name and name of
		// subdecomposition as label
		setAttribute("label", label);

		setDotAttribute("label", label);
		setDotAttribute("shape", "custom");
	}

	public void setLogEvent(LogEvent event) {
		this.event = event;
	}

	public LogEvent getLogEvent() {
		return this.event;
	}

	public String getDecomposition() {
		return this.decomposesTo;
	}

	/**
	 * Get the join type. Used by att.grappa.yawl.SplitJoinTask to determine
	 * which symbol to draw for join and split.
	 * 
	 * @return The join type
	 */
	public int getJoinType() {
		Iterator it = getInEdgesIterator();
		// Count number of normal input edges
		int cnt = 0;
		while (it.hasNext()) {
			YAWLEdge edge = (YAWLEdge) it.next();
			if (edge.isNormal()) {
				cnt++;
			}
		}
		// Draw special symbol only if more than one normal input edge.
		return cnt <= 1 ? NONE : joinType;
	}

	/**
	 * Get the split type. Used by att.grappa.yawl.SplitJoinTask to determine
	 * which symbol to draw for join and split.
	 * 
	 * @return The split type
	 */
	public int getSplitType() {
		Iterator it = getOutEdgesIterator();
		int cnt = 0;
		while (it.hasNext()) {
			YAWLEdge edge = (YAWLEdge) it.next();
			if (edge.isNormal()) {
				cnt++;
			}
		}
		// Draw special symbol only if more than one normal input edge.
		return cnt <= 1 ? NONE : splitType;
	}

	public void setDotAttributes(YAWLModel model) {
		if (model.isComposite(decomposesTo)) {
			setDotAttribute("shapefile",
					"att.grappa.yawl.SplitJoinCompositeTask");
		} else {
			setDotAttribute("shapefile", "att.grappa.yawl.SplitJoinTask");
		}
	}

	/**
	 * Export to YAWL file.
	 * 
	 * @param phase
	 *            Writing phase: 0 = inputCondition, 2 = outputCondition, 1 =
	 *            rest.
	 * @return String The string to export for this YAWLDecompositon.
	 */
	public String writeToYAWL(int phase) {
		String s = "";
		if (phase == 1) {
			int st = getSplitType();
			int jt = getJoinType();
			if (st == NONE) {
				st = AND;
			}
			if (jt == NONE) {
				jt = XOR;
			}
			s += "\t\t\t\t<task\n";
			s += "\t\t\t\t\tid=\"Node" + getId() + "\"\n";
			s += "\t\t\t\t>\n";

			s += "\t\t\t\t\t<name>" + getIdentifier() + "</name>\n";
			s += "\t\t\t\t\t<documentation>" + getIdentifier()
					+ "</documentation>\n";

			// First, normal edges
			Iterator it = getOutEdgesIterator();
			while (it.hasNext()) {
				YAWLEdge edge = (YAWLEdge) it.next();
				s += edge.writeToYAWL(splitType, YAWLEdge.NORMAL);
			}

			// Second, join and split type
			s += "\t\t\t\t\t<join code=\""
					+ (jt == AND ? "and" : (jt == OR ? "or" : "xor"))
					+ "\"/>\n";
			s += "\t\t\t\t\t<split code=\""
					+ (st == AND ? "and" : (st == OR ? "or" : "xor"))
					+ "\"/>\n";

			// Third, reset edges.
			it = getOutEdgesIterator();
			while (it.hasNext()) {
				YAWLEdge edge = (YAWLEdge) it.next();
				s += edge.writeToYAWL(splitType, YAWLEdge.RESET);
			}

			/*
			 * Eric Verbeek, February 11, 2008. Ticket #447: An empty
			 * decomposition should not be exported.
			 */
			if (decomposesTo.length() > 0) {
				s += "\t\t\t\t\t<decomposesTo id=\"" + decomposesTo + "\"/>\n";
			}
			s += "\t\t\t\t</task>\n";
		}
		return s;
	}
}
