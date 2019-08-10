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

package org.processmining.framework.models.petrinet.algorithms;

import java.io.*;
import java.util.*;

import org.processmining.framework.models.petrinet.*;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title: TPNWriter
 * </p>
 * <p>
 * Description: This static class returns a TPN representation of a PetriNet.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: Technische Universiteit Eindhoven
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */

public class TPNWriter {

	/**
	 * This value is used to indicate the event type of an invisible transition.
	 */
	public final static String INVISIBLE_EVENT_TYPE = "$invisible$";

	public static String write(PetriNet net) {
		int i = 0;
		String tpn = "";
		int nofLines = 0;
		Iterator it = net.getPlaces().iterator();
		while (it.hasNext()) {
			Place p = (Place) (it.next());
			p.setNumber(i);
			i++;
			tpn += "place \"place_" + p.getNumber() + "\"";
			if (p.inDegree() == 0) {
				tpn += " init 1";
			}
			tpn += ";\n";
			nofLines++;
		}

		it = net.getTransitions().iterator();
		while (it.hasNext()) {
			Transition t = (Transition) (it.next());
			t.setNumber(i++);
			String label = t.getIdentifier();
			if (t.isInvisibleTask()) {
				label += "\\" + "n" + INVISIBLE_EVENT_TYPE;
			}
			tpn += "trans \"t_" + i + "\"~\"" + label + "\" in ";
			/*
			 * The use of getPredecessors() and getSuccessors() in the following
			 * piece of code results in wrong results in case of arc weights >
			 * 1. Therefore, we need to iterate over the incoming and outgoing
			 * edges.
			 */
			// Iterator it2 = t.getPredecessors().iterator();
			Iterator it2 = t.getInEdgesIterator();
			while (it2.hasNext()) {
				ModelGraphEdge edge = (ModelGraphEdge) it2.next();
				// Place p = (Place) it2.next();
				Place p = (Place) edge.getSource();
				tpn += "\"place_" + p.getNumber() + "\" ";
			}
			tpn += "out ";
			// it2 = t.getSuccessors().iterator();
			it2 = t.getOutEdgesIterator();
			while (it2.hasNext()) {
				ModelGraphEdge edge = (ModelGraphEdge) it2.next();
				// Place p = (Place) it2.next();
				Place p = (Place) edge.getDest();
				tpn += "\"place_" + p.getNumber() + "\" ";
			}
			tpn += ";\n";
			nofLines++;
		}
		return tpn;
	}

}
