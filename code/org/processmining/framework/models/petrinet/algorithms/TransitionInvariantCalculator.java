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
 * Title: TransitionInvariantCalculator
 * </p>
 * <p>
 * Description: This static class returns an ArrayList of Bags. Each of these
 * bags contains transitions an represents a semi-positive transition invariant
 * for the given PetriNet. The given set is minimal.
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

public class TransitionInvariantCalculator {

	public static synchronized ArrayList calculate(PetriNet net,
			int shortCircuit) {
		Transition extra = null;
		if (shortCircuit > 0) {
			extra = PetriNetShortCircuiter.shortCircuit(net);
		}

		ArrayList inv = new ArrayList();

		DoubleMatrix2D m = InvariantCalculator.calculate(net
				.getIncidenceMatrix());

		rows: for (int i = 0; i < m.rows(); i++) {
			// For each row, make a bag over the places
			Bag b = new Bag();
			columns: for (int j = 0; j < m.columns(); j++) {
				Transition t = (Transition) net.getTransitions().get(j);
				if (t == extra) {
					// The number of executions of this transition should be
					// limited to shortCirucuit.
					if (m.get(i, j) > shortCircuit) {
						continue rows;
					} else {
						continue columns;
					}
				}
				for (int k = 0; k < m.get(i, j); k++) {
					b.add(t);
				}
			}
			inv.add(b);
		}
		if (extra != null && shortCircuit > 0) {
			net.delTransition(extra);
		}

		return inv;

	}
}
