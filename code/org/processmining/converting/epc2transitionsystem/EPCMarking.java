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

package org.processmining.converting.epc2transitionsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEdge;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.models.transitionsystem.TransitionSystem;
import org.processmining.framework.models.transitionsystem.TransitionSystemVertexSet;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Jan Mendling
 * @version 1.0
 */

public class EPCMarking implements Cloneable {

	public static final int DEAD = 0;
	public static final int WAIT = 1;
	public static final int DEADANDWAIT = 2;
	public static final int POSITIVE = 1;
	public static final int NEGATIVE = -1;
	public static final int EMPTY = 0;

	public final static String DEADLOCK = "deadlock";
	public final static String HIGHLIGHT = "highlight";
	public final static String END = "end";
	public final static String NOTE = "note";

	private ArrayList<EPCEdge> positiveTokens = new ArrayList<EPCEdge>();
	private ArrayList<EPCEdge> negativeTokens = new ArrayList<EPCEdge>();
	private HashMap<EPCEdge, Integer> Context = new HashMap<EPCEdge, Integer>();
	private ConfigurableEPC EPC;

	public EPCMarking(ConfigurableEPC Epc, Vector<EPCEdge> negativeArcs,
			Vector<EPCEdge> positiveArcs) {
		EPC = Epc;
		ArrayList<EPCEdge> Edges = EPC.getEdges();
		Context = new HashMap<EPCEdge, Integer>(Edges.size());
		for (EPCEdge edge : Edges) {
			setDeadContext(edge);
		}
		for (EPCEdge edge : negativeArcs) {
			negativeTokens.add(edge);
		}
		for (EPCEdge edge : positiveArcs) {
			positiveTokens.add(edge);
			setWaitContext(edge);
		}
	}

	public EPCMarking(ArrayList<EPCEdge> pT, ArrayList<EPCEdge> nT,
			HashMap<EPCEdge, Integer> ct, ConfigurableEPC cE) {
		EPC = cE;
		positiveTokens = pT;
		negativeTokens = nT;
		Context = ct;
	}

	public ArrayList<EPCMarking> nextMarkings(TransitionSystem TS) {
		EPCMarking newM = this.clone();
		newM.propagateDeadThenWaitContext();
		newM.propagateNegativeTokens();
		return newM.propagatePositiveTokens(TS, this);
	}

	public void propagateDeadThenWaitContext() {
		// find dead and wait enabled nodes
		Stack<EPCObject> deadCandidates = new Stack<EPCObject>();
		Stack<EPCObject> waitCandidates = new Stack<EPCObject>();
		ArrayList<EPCObject> nodes = new ArrayList();
		nodes.addAll(EPC.getConnectors());
		nodes.addAll(EPC.getEvents());
		nodes.addAll(EPC.getFunctions());
		for (EPCObject node : nodes) {
			if (node.getOutEdges() == null || node.getInEdges() == null) {
				continue;
			}
			Iterator outs = node.getOutEdgesIterator();
			int outContext = Context.get(outs.next());
			while (outs.hasNext()) {
				int nextContext = Context.get(outs.next());
				if (nextContext != outContext) {
					outContext = DEADANDWAIT;
					break;
				}
			}
			if (outContext == DEADANDWAIT) {
				deadCandidates.push(node);
				waitCandidates.push(node);
				continue;
			}
			Iterator ins = node.getInEdgesIterator();
			int inContext = Context.get(ins.next());
			while (ins.hasNext()) {
				int nextContext = Context.get(ins.next());
				if (nextContext != inContext) {
					inContext = DEADANDWAIT;
					break;
				}
			}
			if (inContext != WAIT && outContext == WAIT) {
				deadCandidates.push(node);
			}
			if (inContext != DEAD && outContext == DEAD) {
				waitCandidates.push(node);
			}
		}

		// dead propagation
		while (!deadCandidates.isEmpty()) {
			EPCObject node = deadCandidates.pop();
			if (node.getOutEdges() == null || node.getInEdges() == null) {
				continue;
			}
			Iterator ins = node.getInEdgesIterator();
			Iterator outs = node.getOutEdgesIterator();
			if (node instanceof EPCFunction || node instanceof EPCEvent) {
				int inContext = Context.get(ins.next());
				while (ins.hasNext()) {
					int nextContext = Context.get(ins.next());
					if (nextContext != inContext) {
						inContext = DEADANDWAIT;
						break;
					}
				}
				if (inContext == DEAD) {
					while (outs.hasNext()) {
						EPCEdge edge = (EPCEdge) outs.next();
						if (Context.get(edge) == WAIT
								&& (!positiveTokens.contains(edge))
								&& (!negativeTokens.contains(edge))) {
							setDeadContext(edge);
							if (!deadCandidates.contains((EPCObject) edge
									.getDest())) {
								deadCandidates.push((EPCObject) edge.getDest());
							}
						}
					}
				}
			} else {
				// String type =
				// node.toString().toLowerCase().substring(0,node.toString().indexOf("-")).trim();
				if (node instanceof EPCConnector
						&& (((EPCConnector) node).getType() == EPCConnector.AND)) {
					int inContext = Context.get(ins.next());
					while (ins.hasNext()) {
						int nextContext = Context.get(ins.next());
						if (nextContext != inContext) {
							inContext = DEADANDWAIT;
							break;
						}
					}
					if (inContext == DEAD || inContext == DEADANDWAIT) {
						while (outs.hasNext()) {
							EPCEdge edge = (EPCEdge) outs.next();
							if (Context.get(edge) == WAIT
									&& (!positiveTokens.contains(edge))
									&& (!negativeTokens.contains(edge))) {
								setDeadContext(edge);
								if (!deadCandidates.contains((EPCObject) edge
										.getDest())) {
									deadCandidates.push((EPCObject) edge
											.getDest());
								}
							}
						}
					}
				}
				if (node instanceof EPCConnector
						&& (((EPCConnector) node).getType() != EPCConnector.AND)) {
					int inContext = Context.get(ins.next());
					while (ins.hasNext()) {
						int nextContext = Context.get(ins.next());
						if (nextContext != inContext) {
							inContext = DEADANDWAIT;
							break;
						}
					}
					if (inContext == DEAD) {
						while (outs.hasNext()) {
							EPCEdge edge = (EPCEdge) outs.next();
							if (Context.get(edge) == WAIT
									&& (!positiveTokens.contains(edge))
									&& (!negativeTokens.contains(edge))) {
								setDeadContext(edge);
								if (!deadCandidates.contains((EPCObject) edge
										.getDest())) {
									deadCandidates.push((EPCObject) edge
											.getDest());
								}
							}
						}
					}
				}
			}
		}// while dead propagation

		// wait propagation
		while (!waitCandidates.isEmpty()) {
			EPCObject node = waitCandidates.pop();
			if (node.getOutEdges() == null || node.getInEdges() == null) {
				continue;
			}
			Iterator ins = node.getInEdgesIterator();
			Iterator outs = node.getOutEdgesIterator();
			if (node instanceof EPCFunction || node instanceof EPCEvent) {
				int inContext = Context.get(ins.next());
				while (ins.hasNext()) {
					int nextContext = Context.get(ins.next());
					if (nextContext != inContext) {
						inContext = DEADANDWAIT;
						break;
					}
				}
				if (inContext == WAIT) {
					while (outs.hasNext()) {
						EPCEdge edge = (EPCEdge) outs.next();
						if (Context.get(edge) == DEAD
								&& (!positiveTokens.contains(edge))
								&& (!negativeTokens.contains(edge))) {
							setWaitContext(edge);
							if (!waitCandidates.contains((EPCObject) edge
									.getDest())) {
								waitCandidates.push((EPCObject) edge.getDest());
							}
						}
					}
				}
			} else {
				String type = node.toString().toLowerCase().substring(0,
						node.toString().indexOf("-")).trim();
				if (node instanceof EPCConnector && type.equals("and")) {
					int inContext = Context.get(ins.next());
					while (ins.hasNext()) {
						int nextContext = Context.get(ins.next());
						if (nextContext != inContext) {
							inContext = DEADANDWAIT;
							break;
						}
					}
					if (inContext == WAIT) {
						while (outs.hasNext()) {
							EPCEdge edge = (EPCEdge) outs.next();
							if (Context.get(edge) == DEAD
									&& (!positiveTokens.contains(edge))
									&& (!negativeTokens.contains(edge))) {
								setWaitContext(edge);
								if (!waitCandidates.contains((EPCObject) edge
										.getDest())) {
									waitCandidates.push((EPCObject) edge
											.getDest());
								}
							}
						}
					}
				}
				if (node instanceof EPCConnector && (!type.equals("and"))) {
					int inContext = Context.get(ins.next());
					while (ins.hasNext()) {
						int nextContext = Context.get(ins.next());
						if (nextContext != inContext) {
							inContext = DEADANDWAIT;
							break;
						}
					}
					if (inContext == WAIT || inContext == DEADANDWAIT) {
						while (outs.hasNext()) {
							EPCEdge edge = (EPCEdge) outs.next();
							if (Context.get(edge) == DEAD
									&& (!positiveTokens.contains(edge))
									&& (!negativeTokens.contains(edge))) {
								setWaitContext(edge);
								if (!waitCandidates.contains((EPCObject) edge
										.getDest())) {
									waitCandidates.push((EPCObject) edge
											.getDest());
								}
							}
						}
					}
				}
			}
		}// while wait propagation
	}

	public void propagateNegativeTokens() {
		Stack<EPCObject> negativeCandidates = new Stack<EPCObject>();
		boolean hasfired = true;
		while (!negativeTokens.isEmpty() && hasfired) {
			Stack<EPCObject> newNegativeCandidates = new Stack<EPCObject>();
			hasfired = false;
			for (EPCEdge edge : negativeTokens) {
				EPCObject dest = (EPCObject) edge.getDest();
				if (!negativeCandidates.contains(dest)) {
					if (dest.getOutEdges() == null || dest.getInEdges() == null) {
						continue;
					} else {
						negativeCandidates.push(dest);
					}
				}
			}
			for (EPCObject node : negativeCandidates) {
				if (node.getOutEdges() == null || node.getInEdges() == null) {
					continue;
				}
				Iterator ins = node.getInEdgesIterator();
				Iterator outs = node.getOutEdgesIterator();
				Boolean isenabled = true;
				while (ins.hasNext()) {
					EPCEdge nextedge = (EPCEdge) ins.next();
					if (getState(nextedge) != NEGATIVE) {
						isenabled = false;
						break;
					}
				}
				if (isenabled == true) {
					while (outs.hasNext()) {
						EPCEdge nextedge = (EPCEdge) outs.next();
						if (getState(nextedge) != EMPTY) {
							isenabled = false;
							break;
						}
					}
				}
				if (isenabled) {
					fireNegative(node);
					Iterator<EPCEdge> outedges = node.getOutEdgesIterator();
					while (outedges.hasNext()) {
						newNegativeCandidates.add((EPCObject) outedges.next()
								.getDest());
					}
					hasfired = true;
				}
			}
			negativeCandidates.removeAllElements();
			negativeCandidates.addAll(newNegativeCandidates);
		}
	}

	public ArrayList<EPCMarking> propagatePositiveTokens(TransitionSystem TS,
			EPCMarking old) {
		ArrayList<EPCMarking> nextMarkings = new ArrayList<EPCMarking>();
		Stack<EPCObject> positiveCandidates = new Stack<EPCObject>();
		for (EPCEdge edge : positiveTokens) {
			EPCObject dest = (EPCObject) edge.getDest();
			if (!positiveCandidates.contains(dest)) {
				if (dest.getOutEdges() == null || dest.getInEdges() == null) {
					continue;
				} else {
					positiveCandidates.push(dest);
				}
			}
		}
		Boolean isAnyEnabled = false;
		EPCObject node;
		while (!positiveCandidates.isEmpty()) {
			node = positiveCandidates.pop();
			if (node.getOutEdges() == null || node.getInEdges() == null) {
				continue;
			}
			Iterator ins = node.getInEdgesIterator();
			Iterator outs = node.getOutEdgesIterator();
			Boolean isenabled = true;
			int indexof = 0;
			if (node.toString().indexOf("-") > 0) {
				indexof = node.toString().indexOf("-");
			}
			String type = node.toString().toLowerCase().substring(0, indexof)
					.trim();
			if (node instanceof EPCFunction || node instanceof EPCEvent
					|| (node instanceof EPCConnector && type.equals("and"))) {
				while (ins.hasNext()) {
					EPCEdge nextedge = (EPCEdge) ins.next();
					if (getState(nextedge) != POSITIVE) {
						isenabled = false;
						break;
					}
				}
			}
			if (node instanceof EPCConnector && type.equals("xor")) {
				isenabled = false;
				while (ins.hasNext()) {
					EPCEdge nextedge = (EPCEdge) ins.next();
					if (getState(nextedge) == POSITIVE) {
						isenabled = true;
						break;
					}
				}
			}
			if (node instanceof EPCConnector && type.equals("or")) {
				isenabled = false;
				while (ins.hasNext()) {
					EPCEdge nextedge = (EPCEdge) ins.next();
					if (getState(nextedge) == POSITIVE) {
						isenabled = true;
					}
					if (getState(nextedge) == EMPTY
							&& Context.get(nextedge) == WAIT) {
						isenabled = false;
						break;
					}
				}
			}
			if (isenabled == true) {
				while (outs.hasNext()) {
					EPCEdge nextedge = (EPCEdge) outs.next();
					if (getState(nextedge) != EMPTY) {
						isenabled = false;
						break;
					}
				}
			}
			if (isenabled == true) {
				nextMarkings.addAll(firePositive(node, TS, old));
				isAnyEnabled = true;
			}
		}
		if (isAnyEnabled == false) {
			TransitionSystemVertexSet newone = new TransitionSystemVertexSet(
					old.toHashSet(), "", TS);
			TransitionSystemVertexSet realnew;
			if (TS.containsVertex(newone) != null) {
				realnew = (TransitionSystemVertexSet) TS.containsVertex(newone);
			} else {
				realnew = newone;
			}
			if (!old.isFinalMarking()) {
				realnew.setAttribute(HIGHLIGHT, DEADLOCK);
			} else {
				realnew.setAttribute(HIGHLIGHT, END);
			}
			TS.addVertex(realnew);
		}
		return nextMarkings;
	}

	public void setDeadContext(EPCEdge Edge) {
		Context.put(Edge, DEAD);
	}

	public void setWaitContext(EPCEdge Edge) {
		Context.put(Edge, WAIT);
	}

	public void removeNegative(EPCEdge edge) {
		negativeTokens.remove(edge);
	}

	public void removePositive(EPCEdge edge) {
		positiveTokens.remove(edge);
	}

	public void addNegative(EPCEdge edge) {
		if (!negativeTokens.contains(edge)) {
			negativeTokens.add(edge);
		}
	}

	public void addPositive(EPCEdge edge) {
		if (!positiveTokens.contains(edge)) {
			positiveTokens.add(edge);
		}
	}

	public int getState(EPCEdge Edge) {
		if (negativeTokens.contains(Edge)) {
			return NEGATIVE;
		}
		if (positiveTokens.contains(Edge)) {
			return POSITIVE;
		}
		return EMPTY;
	}

	public void fireNegative(EPCObject node) {
		if (node.getOutEdges() == null || node.getInEdges() == null) {
			return;
		}
		Iterator ins = node.getInEdgesIterator();
		Iterator outs = node.getOutEdgesIterator();
		while (ins.hasNext()) {
			EPCEdge nextedge = (EPCEdge) ins.next();
			removeNegative(nextedge);
			setDeadContext(nextedge);
		}
		while (outs.hasNext()) {
			EPCEdge nextedge = (EPCEdge) outs.next();
			addNegative(nextedge);
			setDeadContext(nextedge);
		}
	}

	public ArrayList<EPCMarking> firePositive(EPCObject node,
			TransitionSystem TS, EPCMarking old) {
		ArrayList<EPCMarking> newMarkings = new ArrayList<EPCMarking>();
		if (node.getOutEdges() == null || node.getInEdges() == null) {
			return newMarkings;
		}
		Iterator ins = node.getInEdgesIterator();
		Iterator outs = node.getOutEdgesIterator();
		EPCMarking nextMarking = this.clone();
		EPCTransitionRelation TR;
		int indexof = 0;
		if (node.toString().indexOf("-") > 0) {
			indexof = node.toString().indexOf("-");
		}
		// String type =
		// node.toString().toLowerCase().substring(0,indexof).trim();
		if (node instanceof EPCFunction
				|| node instanceof EPCEvent
				|| (node instanceof EPCConnector && (((EPCConnector) node)
						.getType() == EPCConnector.AND))) {
			while (ins.hasNext()) {
				EPCEdge nextedge = (EPCEdge) ins.next();
				nextMarking.removeNegative(nextedge);
				nextMarking.removePositive(nextedge);
				nextMarking.setDeadContext(nextedge);
			}
			while (outs.hasNext()) {
				EPCEdge nextedge = (EPCEdge) outs.next();
				nextMarking.addPositive(nextedge);
				nextMarking.setWaitContext(nextedge);
			}
			String label = node.getIdentifier();
			if (node instanceof EPCConnector
					&& (((EPCConnector) node).getType() == EPCConnector.AND)) {
				label = "and" + node.getId();
			}
			TR = new EPCTransitionRelation(old.toHashSet(), nextMarking
					.toHashSet(), label);
			if (TR.addToTransitionSystem(TS)) {
				newMarkings.add(nextMarking);
			}
		}
		if (node instanceof EPCConnector
				&& (((EPCConnector) node).getType() == EPCConnector.XOR)) {
			while (ins.hasNext()) {
				EPCEdge nextedge = (EPCEdge) ins.next();
				nextMarking.removeNegative(nextedge);
				nextMarking.setDeadContext(nextedge);
			}
			while (outs.hasNext()) {
				EPCEdge next2edge = (EPCEdge) outs.next();
				nextMarking.setDeadContext(next2edge);
			}
			Iterator ins2 = node.getInEdgesIterator();
			String label = "xor" + node.getId();
			if (label == null || label.length() < 1) {
				label = "" + node.getId() + "xor";
			}
			while (ins2.hasNext()) {
				EPCEdge nextedge = (EPCEdge) ins2.next();
				if (nextMarking.getState(nextedge) == POSITIVE) {
					EPCMarking next2Marking = nextMarking.clone();
					next2Marking.removePositive(nextedge);
					Iterator outs2 = node.getOutEdgesIterator();
					EPCMarking thisMarking;
					while (outs2.hasNext()) {
						thisMarking = next2Marking.clone();
						EPCEdge next2edge = (EPCEdge) outs2.next();
						thisMarking.addPositive(next2edge);
						thisMarking.setWaitContext(next2edge);
						TR = new EPCTransitionRelation(old.toHashSet(),
								thisMarking.toHashSet(), label);
						if (TR.addToTransitionSystem(TS)) {
							newMarkings.add(thisMarking);
						}
					}
				}
			}
		}
		if (node instanceof EPCConnector
				&& (((EPCConnector) node).getType() == EPCConnector.OR)) {
			while (ins.hasNext()) {
				EPCEdge nextedge = (EPCEdge) ins.next();
				nextMarking.removeNegative(nextedge);
				nextMarking.removePositive(nextedge);
				nextMarking.setDeadContext(nextedge);
			}
			while (outs.hasNext()) {
				EPCEdge nextedge = (EPCEdge) outs.next();
				nextMarking.addNegative(nextedge);
				nextMarking.setDeadContext(nextedge);
			}
			Iterator outs2 = node.getOutEdgesIterator();
			EPCMarking thisMarking;
			int outcount = 0;
			String label = "or" + node.getId();
			if (label == null || label.length() < 1) {
				label = "or" + node.getId();
			}
			while (outs2.hasNext()) {
				thisMarking = nextMarking.clone();
				EPCEdge nextedge = (EPCEdge) outs2.next();
				thisMarking.addPositive(nextedge);
				thisMarking.removeNegative(nextedge);
				thisMarking.setWaitContext(nextedge);
				thisMarking.propagateNegativeTokens();
				TR = new EPCTransitionRelation(old.toHashSet(), thisMarking
						.toHashSet(), label);
				// Message.add(label+" "+thisMarking.toString()+thisMarking.ContextToString());
				if (TR.addToTransitionSystem(TS)) {
					newMarkings.add(thisMarking);
				}
				outcount++;
			}
			if (outcount > 1) {
				Iterator outs3 = node.getOutEdgesIterator();
				thisMarking = nextMarking.clone();
				while (outs3.hasNext()) {
					EPCEdge nextedge = (EPCEdge) outs3.next();
					thisMarking.addPositive(nextedge);
					thisMarking.removeNegative(nextedge);
					thisMarking.setWaitContext(nextedge);
				}
				TR = new EPCTransitionRelation(old.toHashSet(), thisMarking
						.toHashSet(), label);
				if (TR.addToTransitionSystem(TS)) {
					newMarkings.add(thisMarking);
				}
			}
		}
		return newMarkings;
	}

	public String toString() {
		String label = "";
		for (EPCEdge edge : positiveTokens) {
			label = label + "+" + (edge.getId());
		}
		for (EPCEdge edge : negativeTokens) {
			label = label + "-" + (edge.getId());
		}
		return label;
	}

	public String ContextToString() {
		String label = "";
		for (EPCEdge edge : Context.keySet()) {
			if (Context.get(edge) == WAIT) {
				label = label + " " + edge.getId() + "w";
			} else {
				label = label + " " + edge.getId() + "d";
			}
		}
		return label;
	}

	public HashSet<String> toHashSet() {
		HashSet<String> docs = new HashSet<String>();
		for (EPCEdge edge : positiveTokens) {
			docs.add("+" + edge.getId());
		}
		for (EPCEdge edge : negativeTokens) {
			docs.add("-" + edge.getId());
		}
		return docs;
	}

	public boolean isFinalMarking() {
		boolean isFinal = true;
		for (EPCEdge edge : positiveTokens) {
			if (edge.getDest().getSuccessors().size() > 0) {
				isFinal = false;
				break;
			}
		}
		return isFinal;
	}

	public EPCMarking clone() {
		ArrayList<EPCEdge> posTokens = new ArrayList<EPCEdge>();
		posTokens.addAll(positiveTokens);
		ArrayList<EPCEdge> negTokens = new ArrayList<EPCEdge>();
		negTokens.addAll(negativeTokens);
		HashMap<EPCEdge, Integer> cont = new HashMap<EPCEdge, Integer>();
		cont.putAll(Context);
		EPCMarking sameMarking = new EPCMarking(posTokens, negTokens, cont, EPC);
		return sameMarking;
	}
}
