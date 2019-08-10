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

import org.processmining.framework.models.*;
import org.processmining.framework.models.petrinet.*;

/**
 * <p>
 * Title: InitialPlaceMarker
 * </p>
 * <p>
 * Description: This static class marks all the places of a PetriNet that do not
 * have incoming arcs with the given number of tokens. All other places will
 * contain 0 tokens after execution.
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

public class InitialPlaceMarker {

	public static synchronized Bag mark(PetriNet net, int tokenCount) {
		Iterator it = net.getPlaces().iterator();
		Bag s = new Bag();
		while (it.hasNext()) {
			Place p = (Place) it.next();
			p.removeAllTokens();
			if (p.inDegree() == 0) {
				for (int i = 0; i < tokenCount; i++) {
					p.addToken(new Token());
				}
				s.add(p, tokenCount);
			}
		}
		return s;

	}

	public static synchronized Bag mark(PetriNet net, int tokenCount,
			Date timestamp) {
		Iterator it = net.getPlaces().iterator();
		Bag s = new Bag();
		while (it.hasNext()) {
			Place p = (Place) it.next();
			p.removeAllTokens();
			if (p.inDegree() == 0) {
				for (int i = 0; i < tokenCount; i++) {
					p.addToken(new Token(timestamp));
				}
				s.add(p, tokenCount);
			}
		}
		return s;

	}

}
