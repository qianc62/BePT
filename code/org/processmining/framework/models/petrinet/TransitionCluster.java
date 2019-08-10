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

import java.util.*;

/**
 * A transition cluster is a group of transitions in a Petri net that are
 * grouped together. For example, the {@link PetriNet#makeClusters makeClusters}
 * procedure groups transitions belonging to the same activity but to different
 * log event types (such as Start, Schedule, Complete). The cluster will be
 * visualized by drawing a blue box around the group of transitions.
 * <p>
 * Note that the transition contained in the cluster must be also contained in
 * the transition list of the Petri net. The grouping is considered to be
 * additional information based on that set of transitions.
 * 
 * @author not attributable
 */
public class TransitionCluster extends ArrayList {

	private String label; /* the name of the cluster */

	/**
	 * Default constructor.
	 * 
	 * @param label
	 *            the name of the cluster
	 */
	public TransitionCluster(String label) {
		this.label = label;
	}

	/**
	 * Copy constructor initializing the transition cluster based on the given
	 * template cluster. Note that referenced Transitions are not cloned
	 * themselves (not a deep copy).
	 * 
	 * @param copyTemplate
	 *            the given cluster serving as a template for this object
	 */
	public TransitionCluster(TransitionCluster copyTemplate) {
		this.label = copyTemplate.getLabel();
		Iterator it = copyTemplate.iterator();
		while (it.hasNext()) {
			this.addTransition((Transition) it.next());
		}
	}

	/**
	 * Adds a transition to this cluster.
	 * 
	 * @param t
	 *            the transition to be added
	 * @return <code>true</code> if the transition is not contained yet, and the
	 *         transition could be added. <code>False</code> otherwise
	 */
	public boolean addTransition(Transition t) {
		boolean b = contains(t);
		if (b == false) {
			add(t);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets a transition from the cluster based on the given index.
	 * 
	 * @param i
	 *            the index
	 * @return the transition that resides in the cluster at that index
	 */
	public Transition getTransition(int i) {
		return (Transition) get(i);
	}

	/**
	 * Gets the name of the transition cluster.
	 * 
	 * @return the name of the cluster
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Makes a deep copy of the object. Note that this method needs to be
	 * extended as soon as there are attributes added to the class which are not
	 * primitive or immutable. <br>
	 * Note further that the referenced transitions are not cloned themselves,
	 * so in that sense it is a shallow copy. This is needed to, e.g.,
	 * re-establish the structure of a PetriNet.
	 * 
	 * @return Object the cloned object
	 */
	public Object clone() {
		TransitionCluster o = new TransitionCluster(this);
		return o;
	}
}
