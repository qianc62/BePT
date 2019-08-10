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

package org.processmining.framework.models.pdm;

import org.processmining.framework.models.petrinet.*;
import java.lang.*;
import java.util.*;
import java.io.*;

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
public class PDMTransition extends Transition {

	public HashSet dataElements = new HashSet();
	public HashMap operations = new HashMap();
	public String transitionID = new String();
	public Double cost;
	public Double time;
	public PDMResource resource;

	public PDMTransition(String identifier, PetriNet net, PDMActivity activity) {
		super(net);
		this.dataElements = activity.getDataElements();
		this.operations = activity.getOperations();
		this.transitionID = identifier;
		setIdentifier(identifier);
	}

	public PDMTransition(String identifier, PetriNet net) {
		super(net);
		this.transitionID = identifier;
		setIdentifier(identifier);
	}

	public void addDataElement(PDMDataElement data) {
		dataElements.add(data);
	}

	public void addOperation(HashSet inputs, HashSet outputs) {

	}

	public void writeContentToDot(Writer bw) throws IOException {
		// First, write all data elements
		Iterator it = dataElements.iterator();
		while (it.hasNext()) {
			PDMDataElement d = (PDMDataElement) it.next();
			bw.write(identifier + d.getID()
					+ " [shape=circle, height=\".3\", label=\"" + d.getID()
					+ "\"];\n");
		}

		// Secondly, write operations
		Object[] ops = operations.values().toArray();
		for (int i = 0; i < ops.length; i++) {
			PDMOperation op = (PDMOperation) ops[i];

			// write the point in between
			bw
					.write(identifier
							+ op.getID()
							+ " [shape=circle, fixedsize=true, height=\".1\", style=filled, label=\"\"];\n");

			// write the arcs from the input elements to the point in between
			HashMap input = op.getInputElements();
			Iterator it2 = input.values().iterator();
			while (it2.hasNext()) {
				PDMDataElement dataElement = (PDMDataElement) it2.next();
				bw.write(identifier + dataElement.getID() + " -> " + identifier
						+ op.getID() + "[arrowhead=none];\n");
			}

			// write the arcs from the point in between to the output elements.
			HashMap output = op.getOutputElements();
			Iterator it3 = output.values().iterator();
			while (it3.hasNext()) {
				PDMDataElement dataElement = (PDMDataElement) it3.next();
				bw.write(identifier + op.getID() + " -> " + identifier
						+ dataElement.getID() + ";\n");
			}

			// op.writeToDot(bw);
		}

	}

	public String getID() {
		return transitionID;
	}

	public PDMOperation getAOperation() {
		Object[] ar = operations.values().toArray();
		PDMOperation result = (PDMOperation) ar[1];
		return result;

	}

}
