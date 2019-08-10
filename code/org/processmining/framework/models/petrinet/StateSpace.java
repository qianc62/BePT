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

package org.processmining.framework.models.petrinet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.fsm.AcceptFSM;

/**
 * A Petri-net based state space, wheras states correspond to markings of the
 * net, and edges represent transitions in the Petri net. <br>
 * The actual construction of the state space (e.g., as a reachability graph, or
 * a coverability graph) is not done here but in dedicated classes. They,
 * however, use this data structure and add the states that were determined in
 * the course of the corresponding algorithm.
 * 
 * @see org.processmining.framework.models.petrinet.algorithms.ReachabilityGraphBuilder
 * @see org.processmining.framework.models.petrinet.algorithms.CoverabilityGraphBuilder
 */
public class StateSpace extends AcceptFSM {

	private PetriNet pnet;

	private HashMap placeValues = new HashMap();
	private boolean useIdentifier = false;

	/**
	 * Initializes a Petri-net based state space, wheras states correspond to
	 * markings of the net, and edges represent transitions in the Petri net.
	 * 
	 * @param pnet
	 *            the given Petri net
	 */
	public StateSpace(PetriNet pnet) {
		super("StateSpace :" + pnet.getIdentifier());
		this.pnet = pnet;
	}

	public PetriNet getPetriNet() {
		return pnet;
	}

	public void destroyStateSpace() {
		placeValues = null;
		pnet = null;
		this.clearAcceptFSM();
		this.clearFSM();
		this.clearModelGraph();
		this.clearGGraph();
		this.clearSubgraph();
		this.clearElement();
	}

	/**
	 * @todo: provide documentation
	 * @param s
	 *            State
	 * @return State
	 */
	public State addState(State s) {
		super.addVertex(s);

		Iterator it = s.iterator();
		while (it.hasNext()) {
			Place p = (Place) it.next();
			if (placeValues.get(p) == null) {
				placeValues.put(p, new ArrayList());
			}
			ArrayList pVals = (ArrayList) placeValues.get(p);
			if (pVals.indexOf(new Integer(s.getOccurances(p))) < 0) {
				pVals.add(new Integer(s.getOccurances(p)));
			}
		}
		s.setLabel(s.getMarking().toString());
		return s;
	}

	public boolean setUseIdentifier(boolean newUseIdentifier) {
		boolean oldUseIdentifier = useIdentifier;
		useIdentifier = newUseIdentifier;
		return oldUseIdentifier;
	}

	/**
	 * @todo: provide documentation
	 * @param bw
	 *            BufferedWriter
	 * @throws IOException
	 */
	public void writeToFSM(BufferedWriter bw) throws IOException {

		Iterator it = placeValues.keySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			Place p = (Place) it.next();
			p.setNumber(i++);
			ArrayList pVals = (ArrayList) placeValues.get(p);
			bw.write((useIdentifier ? p.getIdentifier() : "p" + p.getNumber())
					+ "(" + (pVals.size() + 2) + ") ");

			bw.write("\"0\" ");

			Iterator it2 = pVals.iterator();
			while (it2.hasNext()) {
				Integer i2 = (Integer) it2.next();
				bw.write("\""
						+ (i2.intValue() == State.OMEGA ? "OMEGA" : i2
								.toString()) + "\" ");
			}
			bw.write("\"" + p.getIdentifier() + "\"");
			bw.newLine();
		}
		bw.write("---");
		bw.newLine();

		// Now, start writing the states
		it = getVerticeList().iterator();
		i = 1;
		while (it.hasNext()) {
			State s = (State) it.next();
			s.index = i++;

			int[] string = new int[placeValues.keySet().size()];
			for (int j = 0; j < placeValues.keySet().size(); j++) {
				string[j] = 0;
			}

			Iterator it2 = s.iterator();
			while (it2.hasNext()) {
				Place p = (Place) it2.next();
				int o = s.getOccurances(p);
				string[p.getNumber()] = ((ArrayList) placeValues.get(p))
						.indexOf(new Integer(o)) + 1;
			}

			for (int j = 0; j < placeValues.keySet().size(); j++) {
				bw.write(string[j] + " ");
			}
			bw.newLine();
		}

		bw.write("---");
		bw.newLine();

		// Now, write the edges
		it = getEdges().iterator();
		while (it.hasNext()) {
			ModelGraphEdge e = (ModelGraphEdge) it.next();
			State source = (State) e.getSource();
			State dest = (State) e.getDest();
			bw.write("" + source.index);
			bw.write(" ");
			bw.write("" + dest.index);
			bw.write(" ");
			bw.write("\"" + e.object.toString() + "\"");
			bw.newLine();
		}
	}

	public List<List<ModelGraphVertex>> getStronglyConnectedComponents() {
		List<ModelGraphVertex> order = new ArrayList<ModelGraphVertex>();
		order.add(getStartState());
		List<List<ModelGraphVertex>> nodes = computeFinishingTimes(order, true);
		Collections.reverse(nodes.get(0));
		List<List<ModelGraphVertex>> result = computeFinishingTimes(nodes
				.get(0), false);
		return result;
	}

	private List<List<ModelGraphVertex>> computeFinishingTimes(
			List<ModelGraphVertex> nodes, boolean forward) {
		List<List<ModelGraphVertex>> forest = new ArrayList<List<ModelGraphVertex>>();
		Set<ModelGraphVertex> visited = new LinkedHashSet<ModelGraphVertex>();
		for (ModelGraphVertex node : nodes) {
			List<ModelGraphVertex> tree = new ArrayList<ModelGraphVertex>();
			computeFinishingTimesDFS(node, tree, visited, forward);
			if (!tree.isEmpty())
				forest.add(tree);
		}
		return forest;
	}

	private void computeFinishingTimesDFS(ModelGraphVertex node,
			List<ModelGraphVertex> tree, Set<ModelGraphVertex> visited,
			boolean forward) {
		if (visited.add(node)) {
			Collection<ModelGraphVertex> nextNodes;
			if (forward)
				nextNodes = node.getSuccessors();
			else
				nextNodes = node.getPredecessors();
			for (ModelGraphVertex nextNode : nextNodes) {
				computeFinishingTimesDFS(nextNode, tree, visited, forward);
			}
			tree.add(node);
		}
	}

}
