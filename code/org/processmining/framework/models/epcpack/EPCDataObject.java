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

import java.io.*;
import java.util.HashMap;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class EPCDataObject extends EPCObject implements DOTCodeWriter {

	private String label;

	private EPCFunction function;

	public EPCDataObject(String label, EPCFunction function) {
		super(function.getEPC());
		this.label = label;
		this.function = function;
	}

	public String toString() {
		return label;
	}

	public String getLabel() {
		return label;
	}

	public void writeDOTCode(Writer bw, HashMap nodeMapping) throws IOException {
		bw.write("node" + getId() + " [shape =\"box\",label=\"");
		bw.write(label);
		bw.write("\"];\n");
		nodeMapping.put(new String("node" + getId()), this);
	}

}
