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
import cern.colt.matrix.*;

/**
 * <p>
 * Title: PlaceInvariantCalculator
 * </p>
 * <p>
 * Description: This static class returns an ArrayList of Bags. Each of these
 * bags contains places an represents a semi-positive place invariant for the
 * given PetriNet. The given set is minimal.
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

public class PlaceInvariantCalculator {

	public static synchronized ArrayList calculate(PetriNet net,
			int shortCircuit) {

		Transition extra = null;
		if (shortCircuit > 0) {
			extra = PetriNetShortCircuiter.shortCircuit(net);
		}

		DoubleMatrix2D m = InvariantCalculator.calculate(net
				.getIncidenceMatrix().viewDice());

		if (extra != null && shortCircuit > 0) {
			net.delTransition(extra);
		}

		ArrayList inv = new ArrayList();

		rows: for (int i = 0; i < m.rows(); i++) {
			// For each row, make a bag over the places
			Bag b = new Bag();
			columns: for (int j = 0; j < m.columns(); j++) {
				Place p = (Place) net.getPlaces().get(j);
				// Check the value of any initial place
				if ((p.inDegree() == 0) || (p.outDegree() == 0)) {
					// check the value of this place. It should be at most
					// shortCircuit
					if (m.get(i, j) > shortCircuit) {
						continue rows;
					}
				}
				for (int k = 0; k < m.get(i, j); k++) {
					b.add(p);
				}
			}
			inv.add(b);
		}

		return inv;

	}
}
