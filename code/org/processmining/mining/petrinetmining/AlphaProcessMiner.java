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

package org.processmining.mining.petrinetmining;

import java.util.*;

import org.processmining.converting.*;
import org.processmining.framework.log.*;
import org.processmining.framework.models.petrinet.*;
import org.processmining.framework.models.transitionsystem.*;
import org.processmining.framework.plugin.*;
import org.processmining.framework.ui.*;
import org.processmining.framework.util.*;
import org.processmining.mining.*;
import org.processmining.mining.logabstraction.*;
import cern.colt.list.*;

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

public class AlphaProcessMiner extends LogRelationBasedAlgorithm implements
		ConvertingPlugin {

	private int nme; // Number of model elements

	public String getName() {
		return "Alpha algorithm plugin";
	}

	public MiningResult mine(LogReader log, LogRelations relations,
			final Progress progress) {
		final PetriNet petrinet = new PetriNet();
		final PetriNetResult result = new PetriNetResult(log, petrinet, this);

		if (relations == null || progress.isCanceled()) {
			return null;
		}
		this.logRelations = relations;
		this.nme = logRelations.getLogEvents().size();

		progress.setMinMax(0, 7);
		progress.setProgress(1);

		progress.setProgress(2);
		if (progress.isCanceled()) {
			return null;
		}

		final ArrayList<Transition> transitions = new ArrayList<Transition>(nme);
		// First, we can write all transitions
		for (int i = 0; i < nme; i++) {
			LogEvent e = logRelations.getLogEvents().getEvent(i);
			transitions.add(new Transition(e, petrinet));

		}

		if (showIntermediateUpdates()) {
			SwingWorker worker = new SwingWorker() {
				public Object construct() {
					findPlaces(petrinet, result, transitions,
							new ProgressDummy());
					return null;
				}

				public void finished() {
					petrinet.makeClusters();
					result.repaintNet();
				}

			};
			worker.start();
		} else {
			findPlaces(petrinet, result, transitions, progress);
			petrinet.makeClusters();
		}
		return result;
	}

	private void findPlaces(PetriNet petrinet, PetriNetResult result,
			ArrayList<Transition> transitions, Progress progress) {

		ArrayList tuples = new ArrayList();
		ArrayList<Place> places = new ArrayList();
		for (int i = 0; i < nme; i++) {
			// Skip loops of length one
			if (logRelations.getOneLengthLoopsInfo().get(i) > 0) {
				continue;
			}

			//
			for (int j = 0; j < nme; j++) {
				if (logRelations.getOneLengthLoopsInfo().get(j) > 0) {
					continue;
				}
				if (logRelations.getCausalFollowerMatrix().get(i, j) == 0) {
					continue;
				}

				IntArrayList A = new IntArrayList();
				A.add(i);

				// j is a causal follower of i
				IntArrayList B = new IntArrayList();
				B.add(j);
				// Now, we have a startingpoint to expand the tree,
				// since {i} -> {j}
				ExpandTree(tuples, A, B, 0, 0, petrinet, places, transitions,
						result);
			}
			// In tuples, we now have a collection of ArrayList[2]'s each of
			// which
			// contains information to build the places
		}

		// Second, we can write all places (check for duplicates)
		progress.setProgress(3);
		if (progress.isCanceled()) {
			return;
		}
		// RemoveDuplicates(tuples);

		Place pstart = petrinet.addPlace("pstart");
		pstart.addToken(new Token());
		Place pend = petrinet.addPlace("pend");

		// Third, we write all arcs not for one loops
		progress.setProgress(4);
		if (progress.isCanceled()) {
			return;
		}

		// ///////////////////////////////////////////////

		// Fourth, write all one loop arc's
		progress.setProgress(5);
		if (progress.isCanceled()) {
			return;
		}
		for (int i = 0; i < nme; i++) {
			if (logRelations.getOneLengthLoopsInfo().get(i) == 0) {
				continue;
			}
			IntArrayList pre_i = new IntArrayList();
			IntArrayList suc_i = new IntArrayList();
			for (int j = 0; j < nme; j++) {
				if (logRelations.getCausalFollowerMatrix().get(j, i) > 0) {
					pre_i.add(j);
				}
			}
			for (int j = 0; j < nme; j++) {
				if (logRelations.getCausalFollowerMatrix().get(i, j) > 0) {
					suc_i.add(j);
				}
			}

			IntArrayList tupleNumbers = getTupleNumbersContaining(tuples,
					pre_i, suc_i);
			// Now we can write the arcs from and to the place.

			for (int tupleNumber = 0; tupleNumber < tupleNumbers.size(); tupleNumber++) {
				petrinet.addEdge(getTransition(i, transitions, petrinet),
						places.get(tupleNumbers.get(tupleNumber)));
				petrinet.addEdge(places.get(tupleNumbers.get(tupleNumber)),
						getTransition(i, transitions, petrinet));
			}
		}

		progress.setProgress(6);
		if (progress.isCanceled()) {
			return;
		}
		for (int i = 0; i < nme; i++) {
			if (logRelations.getStartInfo().get(i) == 0) {
				continue;
			}
			petrinet.addEdge(pstart, getTransition(i, transitions, petrinet));
		}

		for (int i = 0; i < nme; i++) {
			if (logRelations.getEndInfo().get(i) == 0) {
				continue;
			}
			petrinet.addEdge(getTransition(i, transitions, petrinet), pend);
		}

		result.repaintNet();
		// Now write clusters.
		progress.setProgress(7);
		if (progress.isCanceled()) {
			return;
		}

		for (int i = 0; i < nme; i++) {
			LogEvent e = logRelations.getLogEvents().getEvent(i);
			if (petrinet.findRandomTransition(e) == null) {
				petrinet.addTransition(new Transition(e, petrinet));
			}
		}

		petrinet.Test("AlphaMinerResult");

		return;

	}

	private boolean removeSmallerThan(ArrayList tuples, IntArrayList[] tuple,
			int tupleIndex, PetriNet net, ArrayList<Place> places) {
		int i = tupleIndex;
		boolean foundLarger = false;
		while (i >= 0 && !foundLarger) {

			IntArrayList[] tuple_i = ((IntArrayList[]) (tuples.get(i)));

			// Now check whether tuple_i is a subset of tuple
			if (tuple[0].toList().containsAll(tuple_i[0].toList())
					&& tuple[1].toList().containsAll(tuple_i[1].toList())) {

				// tuple contains tuple_i
				tuples.remove(i);
				net.delPlace(places.get(i));
				places.remove(i);
			} else if (tuple_i[0].toList().containsAll(tuple[0].toList())
					&& tuple_i[1].toList().containsAll(tuple[1].toList())) {
				// tuple_i contains tuple (hence, there are no smaller tuples
				// before
				foundLarger = true;
			}

			i--;
		}
		return foundLarger;
	}

	private Place addPlaceForTuple(IntArrayList[] tuple, PetriNet petrinet,
			ArrayList<Transition> transitions) {
		Place p = new Place(Arrays.toString(tuple), petrinet);

		petrinet.addPlace(p);
		for (int j = 0; j < tuple[0].size(); j++) {
			petrinet.addEdge(getTransition(tuple[0].get(j), transitions,
					petrinet), p);
		}
		for (int j = 0; j < tuple[1].size(); j++) {
			petrinet.addEdge(p, getTransition(tuple[1].get(j), transitions,
					petrinet));
		}
		return p;
	}

	private Transition getTransition(int i, ArrayList<Transition> transitions,
			PetriNet net) {
		Transition t = transitions.get(i);
		if (net.findTransition(t) == null) {
			net.addTransition(t);
		}
		return t;
	}

	private IntArrayList getTupleNumbersContaining(ArrayList tuples,
			IntArrayList A, IntArrayList B) {
		int i = -1;
		IntArrayList r = new IntArrayList();
		while (i < tuples.size() - 1) {
			i++;
			IntArrayList[] tuple = ((IntArrayList[]) (tuples.get(i)));
			IntArrayList tA = (IntArrayList) A.clone();
			IntArrayList tB = (IntArrayList) B.clone();
			if ((tA.toList().containsAll(tuple[0].toList()) && tB.toList()
					.containsAll(tuple[1].toList()))) {
				// A and B are contained in this tuple
				r.add(i);
			}
		}
		return r;
	}

	private boolean ExpandTree(ArrayList tuples, IntArrayList A,
			IntArrayList B, int sA, int sB, PetriNet net,
			ArrayList<Place> places, ArrayList<Transition> transitions,
			PetriNetResult result) {

		boolean expanded = false;

		int s = sA;
		if (sB < s) {
			s = sB;
			// Look for an element that can be added to A, such that
			// it has no relation with any task in A, and is a causal
			// predecessor of all tasks in B
		}
		for (int i = s; i < nme; i++) {
			if (logRelations.getOneLengthLoopsInfo().get(i) > 0) {
				continue;
			}
			// this is not a loop of length one
			boolean c = (i >= sA) && !A.contains(i);
			if (c) {
				for (int j = 0; j < A.size(); j++) {
					c = c
							&& (logRelations.getCausalFollowerMatrix().get(i,
									A.get(j)) == 0)
							&& (logRelations.getCausalFollowerMatrix().get(
									A.get(j), i) == 0)
							&& (logRelations.getParallelMatrix().get(i,
									A.get(j)) == 0);
					// c == i does not have a relation with any element of A
				}
			}
			if (c) {
				for (int j = 0; j < B.size(); j++) {
					c = c
							&& (logRelations.getCausalFollowerMatrix().get(i,
									B.get(j)) > 0);
					// c == i is a causal predecessor of all elements of B

				}
			}
			boolean d = (i >= sB) && !B.contains(i);
			if (d) {
				for (int j = 0; j < B.size(); j++) {
					d = d
							&& (logRelations.getCausalFollowerMatrix().get(i,
									B.get(j)) == 0)
							&& (logRelations.getCausalFollowerMatrix().get(
									B.get(j), i) == 0)
							&& (logRelations.getParallelMatrix().get(i,
									B.get(j)) == 0);
					// d == i does not have a relation with any element of B
				}
			}
			if (d) {
				for (int j = 0; j < A.size(); j++) {
					d = d
							&& (logRelations.getCausalFollowerMatrix().get(
									A.get(j), i) > 0);
					// d == i is a causal successor of all elements of A

				}
			}
			IntArrayList tA = (IntArrayList) A.clone();
			IntArrayList tB = (IntArrayList) B.clone();

			if (c) {
				// i can be added to A
				A.add(i);
				expanded = ExpandTree(tuples, A, B, i + 1, sB, net, places,
						transitions, result);
				A = tA;
			}
			if (d) {
				// i can be added to A
				B.add(i);
				expanded = ExpandTree(tuples, A, B, sA, i + 1, net, places,
						transitions, result);
				B = tB;
			}
		}
		if (!expanded) {
			IntArrayList[] t = new IntArrayList[2];
			t[0] = (IntArrayList) A.clone();
			t[1] = (IntArrayList) B.clone();

			if (!removeSmallerThan(tuples, t, tuples.size() - 1, net, places)) {
				tuples.add(t);
				places.add(addPlaceForTuple(t, net, transitions));
			}

			if (this.showIntermediateUpdates()) {
				result.repaintNet();
			}
			expanded = true;
		}
		return expanded;
	}

	public String getHtmlDescription() {
		return PluginDocumentationLoader.load(this);
	}

	public MiningResult convert(ProvidedObject providedObject) {
		TransitionSystem ts = null;
		Object[] o = providedObject.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof TransitionSystem) {
				ts = (TransitionSystem) o[i];
			}
		}

		Progress p = new Progress(
				"Converting Transition System to Log logRelations", 0, 5);

		// First, create the bogus log events from the TS
		LogEvents events = new LogEvents();
		for (TransitionSystemEdge edge : (ArrayList<TransitionSystemEdge>) ts
				.getEdges()) {
			if (events.findLogEvent(edge.getIdentifier(), "") == null) {
				events.add(new LogEvent(edge.getIdentifier(), ""));
			}
		}

		LogAbstraction abstraction = new TSLogAbstraction(ts, events);

		logRelations = (new MinValueLogRelationBuilder(abstraction, 0, events))
				.getLogRelations();

		return mine(null, logRelations, p);

	}

	public boolean accepts(ProvidedObject providedObject) {
		Object[] o = providedObject.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof TransitionSystem) {
				return true;
			}
		}
		return false;
	}

}
