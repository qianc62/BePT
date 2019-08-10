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

package org.processmining.framework.models.epcpack;

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

public class EPCConnector extends EPCConfigurableObject {
	public final static int NONE = 3, OR = 4, AND = 5, XOR = 6;

	private int type;

	public EPCConnector(int type, ConfigurableEPC epc) {
		super(false, epc);
		this.type = type;
	}

	public EPCConnector(int type, boolean configurable, ConfigurableEPC epc) {
		super(configurable, epc);
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean equals(Object o) {
		return o == this;
	}

	public String getTypeAsString() {
		if (type == AND) {
			return "A";
		}
		if (type == OR) {
			return "O";
		}
		if (type == XOR) {
			return "X";
		}
		return "";
	}

	public String toString() {
		String s = "";
		if (type == AND) {
			s = "AND";
		}
		if (type == OR) {
			s = "OR";
		}
		if (type == XOR) {
			s = "XOR";
		}
		return s + " - " + (inDegree() > 1 ? "join" : "split");
	}

}
