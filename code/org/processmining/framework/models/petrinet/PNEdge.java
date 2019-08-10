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

import org.processmining.framework.models.*;
import att.grappa.*;

/**
 * Class representing a directed arc in a Petri net model. Petri nets are
 * bipartite graphs and therefore do only allow to connect places with
 * transitions or the other way around.
 * 
 * @see PetriNet
 * @see Transition
 * @see Place
 */
public class PNEdge extends ModelGraphEdge implements Cloneable {

	//By QC
	public int id = -1;
	public String label = "";

	private boolean PT = true;
	private boolean TP = false;

	private double prob;
	private boolean direction;

	/**
	 * Constructor creates an edge from the given place to the given transition.
	 * 
	 * @param p
	 *            the place to be connected to this arc
	 * @param t
	 *            the transition to be connected to this arc
	 */
	public PNEdge(Place p, Transition t) {
		super(p, t);
		direction = PT;
		prob = 1;
	}

	/**
	 * Constructor creates an edge from the given transition to the given place.
	 * 
	 * @param t
	 *            the transition to be connected to this arc
	 * @param p
	 *            the place to be connected to this arc
	 * @see getProbability
	 */
	public PNEdge(Transition t, Place p) {
		super(t, p);
		direction = TP;
		prob = 1;
	}

	/**
	 * Assigns a probability value to the arc. This might be used to store the
	 * frequency of using certain paths in the Petri net model.
	 * 
	 * @param p
	 *            the probabilty to be associated to this arc
	 */
	public void setProbability(double p) {
		prob = p;
	}

	/**
	 * Retrieves the probability value for this arc. Note that there is not
	 * automatically a valid value but it must have been assigned upfront using
	 * {@link #setProbability setProbability}
	 * 
	 * @return the probability value associated to this arc
	 */
	public double getProbability() {
		return prob;
	}

	/**
	 * Determines whether this arc leads from a place to a transition.
	 * 
	 * @return <code>true</code> if this edge is directed from a place to a
	 *         transition, <code>false</code> otherwise
	 */
	public boolean isPT() {
		return direction;
	}

	/**
	 * Determines whether this arc leads from a transition to a place.
	 * 
	 * @return <code>true</code> if this edge is directed from a transition to a
	 *         place, <code>false</code> otherwise
	 */
	public boolean isTP() {
		return !direction;
	}

	/**
	 * Working method to reset source and target node with package access only. <br>
	 * This is needed to re-establish the structure of a Petri net. However,
	 * please note that there is no consistency check included (such as ensuring
	 * that one of the nodes is a place and the other a transition). So be
	 * careful when using it for other purposes.
	 * 
	 * @param source
	 *            the new tail node of this edge
	 * @param target
	 *            the new head node of this edge
	 */
	void setSourceAndTargetNode(Node source, Node target) {
		this.tailNode = source;
		this.headNode = target;
		this.tailNode.addEdge(this, false);
		this.headNode.addEdge(this, true);
	}

	/**
	 * Make a shallow copy of the object.
	 * 
	 * @return Object the cloned object
	 */
	public Object clone() {
		PNEdge o = null;
		try {
			o = (PNEdge) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * @return The transition connected to this Node
	 */
	public Transition getTransition() {
		if (isPT())
			return (Transition) getHead();
		return (Transition) getTail();
	}
}
