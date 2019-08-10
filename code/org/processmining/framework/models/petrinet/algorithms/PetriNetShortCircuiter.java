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

import java.util.*;

import org.processmining.framework.models.petrinet.*;

/**
 * <p>
 * Title: PetriNetShortCircuiter
 * </p>
 * <p>
 * Description: This class adds a transition to the Petrinet, such that it
 * consumes tokens from all places that have no outgoing arcs and it produces
 * places in all places that have no incoming arcs. A pointer to the Transition
 * is provided. If no places with no incoming arcs exist, or no places with no
 * outgoing arcs exist, null is returned.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: Technische Universiteit Eindhoven
 * </p>
 * 
 * @author Boudewijn van Dongen
 * @version 1.0
 */

public class PetriNetShortCircuiter {

	public static synchronized Transition shortCircuit(PetriNet net) {
		// First, short circuit the net.
		Iterator it = net.getPlaces().iterator();
		HashSet in = new HashSet();
		HashSet out = new HashSet();

		while (it.hasNext()) {
			Place p = (Place) it.next();
			if (p.inDegree() == 0) {
				in.add(p);
			}
			if (p.outDegree() == 0) {
				out.add(p);
			}
		}
		if ((in.size() == 0) || (out.size() == 0)) {
			return null;
		}
		Transition extra = net.addTransition(new Transition(
				"Added by shortCircuiter", net));
		it = in.iterator();
		while (it.hasNext()) {
			net.addEdge(extra, (Place) it.next());
		}
		it = out.iterator();
		while (it.hasNext()) {
			net.addEdge((Place) it.next(), extra);
		}
		return extra;
	}

}
