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

import org.processmining.framework.models.*;

/**
 * <p>
 * Title: YAWL Condition
 * </p>
 * <p>
 * Description: Holds a YAWL condition
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

public class YAWLCondition extends YAWLNode {
	// Types of condition
	public static final int IN = 0; // Input condition
	public static final int OUT = 1; // Output condition
	public static final int NONE = 2; // Normal condition

	private int type = NONE;

	/**
	 * Construct a condition in the given graph with given name and type.
	 * 
	 * @param g
	 *            The given graph
	 * @param type
	 *            The given type
	 * @param id
	 *            X[\tY], where X is the condition name, and Y makes the id
	 *            unique.
	 */
	public YAWLCondition(ModelGraph g, int type, String id) {
		super(g, id);
		init(type, id);
	}

	public YAWLCondition(ModelGraph g, YAWLCondition condition) {
		super(g, condition.id);
		init(condition.type, condition.id);
	}

	private void init(int type, String id) {
		this.type = type;

		setAttribute("label", name); // Conditions have a simple label

		if (type == IN) {
			setDotAttribute("label", "");
			setDotAttribute("shape", "custom");
			setDotAttribute("shapefile", "att.grappa.yawl.InputShape");
		} else if (type == OUT) {
			setDotAttribute("label", "");
			setDotAttribute("shape", "custom");
			setDotAttribute("shapefile", "att.grappa.yawl.OutputShape");
		} else {
			setDotAttribute("label", "" /* (String) getAttributeValue("label") */);
			setDotAttribute("shape", "circle");
		}
	}

	public void normalize() {
		if (type != NONE) {
			init(NONE, getID());
			clearDotAttribute("shapefile");
		}
	}

	public boolean isInputCondition() {
		return type == IN;
	}

	public boolean isOutputCondition() {
		return type == OUT;
	}

	/**
	 * Export to YAWL file.
	 * 
	 * @param i
	 *            Writing phase: 0 = inputCondition, 2 = outputCondition, 1 =
	 *            rest.
	 * @return String The string to export for this YAWLDecompositon.
	 */
	public String writeToYAWL(int phase) {
		String s = "";
		if ((phase == 0 && type == IN) || (phase == 1 && type == NONE)
				|| (phase == 2 && type == OUT)) {
			if (type == OUT) {
				s += "\t\t\t\t<outputCondition id=\"Node" + getId() + "\"/>\n";
			} else {
				if (type == IN) {
					s += "\t\t\t\t<inputCondition\n";
				} else {
					s += "\t\t\t\t<condition\n";
				}
				s += "\t\t\t\t\tid=\"Node" + getId() + "\"\n";
				s += "\t\t\t\t>\n";

				if (type == NONE) {
					s += "\t\t\t\t\t<name>" + getIdentifier() + "</name>\n";
				}

				Iterator it = getOutEdgesIterator();
				while (it.hasNext()) {
					YAWLEdge edge = (YAWLEdge) it.next();
					s += edge.writeToYAWL(YAWLTask.NONE, YAWLEdge.NORMAL);
				}

				if (type == IN) {
					s += "\t\t\t\t</inputCondition>\n";
				} else {
					s += "\t\t\t\t</condition>\n";
				}
			}
		}
		return s;
	}
}
