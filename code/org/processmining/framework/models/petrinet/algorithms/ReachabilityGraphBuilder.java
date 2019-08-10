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
import org.processmining.framework.models.fsm.FSMTransition;

/**
 * <p>
 * Title: ReachabilityGraphBuilder
 * </p>
 * <p>
 * Description: This static class provides an implemantation to build the
 * reachabilitygraph of a PetriNet, given an initial marking. Please note thatt
 * this reachability graph can be infinite and therefore, the algorithm is not
 * guaranteed to terminate. However, if the PetriNet is bounded, then the
 * reachabilitygraph is finite.
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

public class ReachabilityGraphBuilder {

	public synchronized static StateSpace build(PetriNet net) {
		// The assumption here is that this is a marked PetriNet

		StateSpace reachability = new StateSpace(net);
		State s = new State(reachability);
		// Create the new state
		Iterator it2 = net.getPlaces().iterator();
		while (it2.hasNext()) {
			Place p = (Place) it2.next();
			if (p.getNumberOfTokens() > 0) {
				s.addPlace(p, p.getNumberOfTokens());
			}
		}
		reachability.addState(s);
		reachability.setStartState(s);
		HashSet newNodes = new HashSet();
		newNodes.add(s);
		do {
			extendReachabilityGraph(net, reachability, newNodes);
		} while (!newNodes.isEmpty());
		return reachability;
	}

	private static synchronized void extendReachabilityGraph(PetriNet net,
			StateSpace graphSoFar, HashSet newNodes) {

		// Chose a state.
		State state = (State) newNodes.iterator().next();
		// set the state of this Petri net to state
		InitialPlaceMarker.mark(net, 0);
		Iterator it = state.iterator();
		while (it.hasNext()) {
			Place p = (Place) it.next();
			for (int i = 0; i < state.getOccurances(p); i++) {
				p.addToken(new Token());
			}
		}

		Marking sinkMarking = new Marking();
		ModelGraphVertex sink = net.getSink();
		if (sink instanceof Place) {
			sinkMarking.addPlace((Place) sink, 1);
		}

		it = net.getTransitions().iterator();
		while (it.hasNext()) {
			Transition t = (Transition) it.next();
			if (t.isEnabled()) {
				t.fireQuick();
				State s = new State(graphSoFar);
				// Create the new state
				Iterator it2 = net.getPlaces().iterator();
				while (it2.hasNext()) {
					Place p = (Place) it2.next();
					if (p.getNumberOfTokens() > 0) {
						s.addPlace(p, p.getNumberOfTokens());
					}
				}
				// check whether the graph contains this state
				int i = graphSoFar.getVerticeList().indexOf(s);
				if (i != -1) {
					s = (State) graphSoFar.getVerticeList().get(i);
					FSMTransition e = new FSMTransition(state, s, t
							.getIdentifier());
					graphSoFar.addEdge(e);
					e.object = t;
				} else {
					graphSoFar.addState(s);
					// HV
					if (s.getMarking().equals(sinkMarking)) {
						graphSoFar.addAcceptState(s);
					}
					FSMTransition e = new FSMTransition(state, s, t
							.getIdentifier());
					graphSoFar.addEdge(e);
					e.object = t;
					newNodes.add(s);
				}
				t.unFireQuick();
			}
		}
		newNodes.remove(state);
	}

}
