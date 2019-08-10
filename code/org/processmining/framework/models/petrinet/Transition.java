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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.LogEventProvider;

/**
 * A transition is node in a Petri net. It is the active component in the Petri
 * net structure, and when it fires then it removes a token from all its input
 * places and produces a token for all its output places. This changes the
 * marking, and therefore the state of the Peti net. <br>
 * From the business process perspective, a transition corresponds to a step, or
 * a task, in a Petri net process model.
 * 
 * @see PetriNet
 * @see Place
 * @see LogEvent
 * 
 * @author bfvdonge
 */
public class Transition extends PNNode implements LogEventProvider, Cloneable,
		Serializable, Comparable {

	private LogEvent logModelElement; /* the associated log event */
	private int number; /* can be used for numbering transitions in a unique way */

	/**
	 * Constructor specifying the kind of log event.
	 * 
	 * @param logModelElement
	 *            the log event associated
	 * @param net
	 *            the Petri net it belongs to
	 */
	public Transition(LogEvent logModelElement, PetriNet net) {
		this(net);
		this.logModelElement = logModelElement;
		if (logModelElement != null) {
			setIdentifier(logModelElement.getModelElementName());
		}
	}

	/**
	 * Basic constructor.
	 * 
	 * @param net
	 *            the Petri net it belongs to
	 */
	public Transition(PetriNet net) {
		super(net);
		setIdentifier("");
		this.logModelElement = null;
	}

	/**
	 * Constructor specifying the name.
	 * 
	 * @param identifier
	 *            the name of the transition
	 * @param net
	 *            the Petri net it belongs to
	 */
	public Transition(String identifier, PetriNet net) {
		this(net);
		setIdentifier(identifier);
	}

	/**
	 * Copy Constructor. Note that the Transition will think it exists in the
	 * same graph. If you want to copy it to another graph, call setSubgraph()
	 * explicitely.
	 * 
	 * @param t
	 *            the template transition
	 */
	public Transition(Transition t) {
		this(t.getLogEvent(), (PetriNet) t.getSubgraph());
		setValue(t.getValue());
		setIdentifier(t.getIdentifier());
	}

	// /////////////////////// GET + SET
	// ////////////////////////////////////////

	/**
	 * @deprecated Please now use getLogEvent()
	 * @see getLogEvent() This method returns the current LogEvent the
	 *      LogEventProvider has stored
	 * @return the stored LogEvent
	 */
	public LogEvent getModelElement() {
		return getLogEvent();
	}

	/**
	 * @deprecated Please now use setLogEvent(LogEvent le)
	 * @see setLogEvent(LogEvent le) This method sets the LogEvent for the
	 *      LogEventProvider to store
	 * @param le
	 *            the LogEvent to store
	 */
	public void setModelElement(LogEvent le) {
		setLogEvent(le);
	}

	/**
	 * Gets the log event belonging to this transition.
	 * 
	 * @return the log event associated
	 */
	public LogEvent getLogEvent() {
		return ((logModelElement == null) ? null : logModelElement);
	}

	/**
	 * Sets the log event belonging to this transition.
	 * 
	 * @param lme
	 *            the new log event associated
	 */
	public void setLogEvent(LogEvent lme) {
		logModelElement = lme;
	}

	/**
	 * Assigns a number to this transition.
	 * <p>
	 * Since the identifier of a transition is not necessarily unique (i.e., two
	 * transitions can have the same name) this method can be used to assign
	 * unique numbers to the transitions of a Petri net, if needed.
	 * {@link getNumber getNumber()} can be used to retrieve the number.
	 * 
	 * @param n
	 *            the number to be assigned
	 */
	public void setNumber(int n) {
		number = n;
	}

	/**
	 * Returns the number which is associated to this transition.
	 * <p>
	 * Since the identifier of a transition is not necessarily unique (i.e., two
	 * transitions can have the same name) this method can be used to retrieve
	 * unique numbers from the transitions of a Petri net, if needed. However,
	 * they are not associated automatically but must have been set using the
	 * {@link setNumber setNumber(int i)} method before.
	 * 
	 * @return the number associated
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Returns the name of the transition. Note that this is not necessarily
	 * unique for two different transitions, they can have the same name. For
	 * comparison the {@link equals equals} method should be used.
	 * 
	 * @return the name of the transition
	 */
	public String toString() {
		return getIdentifier();
	}

	// /////////////////////// FIRE + UNFIRE
	// ////////////////////////////////////

	/**
	 * Checks whether this transition is enabled. That is the case if there are
	 * sufficient tokens available on all its input places, i.e., as many as
	 * would be consumed when firing the transition.
	 * 
	 * @see isNegativeEnabled
	 * 
	 * @return <code>true</code> if the transition is enabled,
	 *         <code>false</code> otherwise
	 */
	public boolean isEnabled() {
		// For all incoming places, these places should have a token
		boolean b = true;
		Iterator it = getPredecessors().iterator();
		while (it.hasNext() && b) {
			Place p = (Place) it.next();
			b = p.getNumberOfTokens() >= ((PetriNet) getGraph())
					.getEdgesBetween(p, this).size();
		}
		return b;
	}

	/**
	 * Fires this transition.
	 * <p>
	 * Note that this only happens if the transition is enabled. If this check
	 * is not needed, {@link Transition#fireQuick() fireQuick()} should be used
	 * instead.
	 * <p>
	 * The effect is that tokens are produced in the output places of this
	 * transition.
	 * 
	 * @see isEnabled()
	 */
	public void fire() {
		if (isEnabled()) {
			fireQuick();
		}
	}

	/**
	 * Fires this transition.
	 * <p>
	 * Note that it is not checked whether the transition is enabled. If this is
	 * needed, {@link Transition#fire() fire()} should be used instead.
	 * <p>
	 * The effect is that tokens are produced in the output places of this
	 * transition.
	 */
	public void fireQuick() {
		Iterator it = getInEdgesIterator();
		while (it.hasNext()) {
			Place p = (Place) ((PNEdge) it.next()).getSource();
			Token t = p.getRandomAvailableToken();
			p.removeToken(t);
		}
		if (getOutEdges() != null) {
			it = getOutEdges().iterator();
			while (it.hasNext()) {
				((Place) ((PNEdge) it.next()).getDest()).addToken(new Token());
			}
		}
	}

	/**
	 * Checks whether this transition is enabled, taking time information into
	 * account. That is the case if, at the given point in time, there are
	 * sufficient tokens available on all its input places, i.e., as many as
	 * would be consumed when firing the transition.
	 * 
	 * @param timestamp
	 *            the time specified
	 * @return <code>true</code> if the transition is enabled,
	 *         <code>false</code> otherwise
	 */
	public boolean isEnabled(Date timestamp) {
		// For all incoming places, these places should have a token
		boolean b = true;
		Iterator it = getPredecessors().iterator();
		while (it.hasNext() && b) {
			Place p = (Place) it.next();
			b = p.getNumberOfTokens(timestamp) >= ((PetriNet) getGraph())
					.getEdgesBetween(p, this).size();
		}
		return b;
	}

	/**
	 * Fires this transition at the given point in time.
	 * <p>
	 * Note that this only happens if the transition is enabled. If this check
	 * is not needed, {@link Transition#fireQuick(Date timestamp) fireQuick(Date
	 * timestamp)} should be used instead.
	 * <p>
	 * The effect is that tokens are produced in the output places of this
	 * transition.
	 * 
	 * @param timestamp
	 *            the time specified
	 */
	public void fire(Date timestamp) {
		if (isEnabled(timestamp)) {
			fireQuick(timestamp);
		}
	}

	/**
	 * Fires this transition at the given point in time.
	 * <p>
	 * Note that it is not checked whether the transition is enabled. If this is
	 * needed, {@link Transition#fire(Date timestamp) fire(Date timestamp)}
	 * should be used instead.
	 * <p>
	 * The effect is that tokens are produced in the output places of this
	 * transition.
	 * 
	 * @param timestamp
	 *            the time specified
	 */
	public void fireQuick(Date timestamp) {
		Iterator it = getInEdgesIterator();
		while (it.hasNext()) {
			Place p = (Place) ((PNEdge) it.next()).getSource();
			Token t = p.getRandomAvailableToken(timestamp);
			p.removeToken(t);
		}
		it = getOutEdgesIterator();
		while (it.hasNext()) {
			((Place) ((PNEdge) it.next()).getDest()).addToken(new Token(
					timestamp));
		}
	}

	/**
	 * Checks whether this transition is negatively enabled. That is the case if
	 * there are sufficient tokens available on all its output places, i.e., as
	 * many as were produced when firing the transition.
	 * 
	 * @see isEnabled
	 * 
	 * @return <code>true</code> if the transition is enabled,
	 *         <code>false</code> otherwise
	 */
	public boolean isNegativeEnabled() {
		// For all incoming places, these places should have a token
		boolean b = true;
		Iterator it = getSuccessors().iterator();
		while (it.hasNext() && b) {
			Place p = (Place) it.next();
			b = p.getNumberOfTokens() >= ((PetriNet) getGraph())
					.getEdgesBetween(this, p).size();
		}
		return b;
	}

	/**
	 * Unfires this transition. Note that this only happens if the transition is
	 * negatively enabled. If this check is not needed,
	 * {@link Transition#unFireQuick() unFireQuick()} should be used instead.
	 * 
	 * @see isNegativeEnabled
	 */
	public void unFire() {
		if (isNegativeEnabled()) {
			unFireQuick();
		}
	}

	/**
	 * Unfires this transition. Note that it is not checked whether the
	 * transition is negatively enabled. If this is needed,
	 * {@link Transition#unFire() unFire()} should be used instead.
	 */
	public void unFireQuick() {
		Iterator it = getOutEdgesIterator();
		while (it.hasNext()) {
			Place p = (Place) ((PNEdge) it.next()).getDest();
			Token t = p.getRandomAvailableToken();
			p.removeToken(t);
		}
		it = getInEdgesIterator();
		while (it.hasNext()) {
			((Place) ((PNEdge) it.next()).getSource()).addToken(new Token());
		}
	}

	// /////////////////////// PROPOSITIONAL FUNCTIONS
	// //////////////////////////

	/**
	 * Compares a given log event with the one associated to this transition.
	 * 
	 * @param lme
	 *            the log event to be compared with
	 * @return <code>true</code> if they are equal, <code>false</code> otherwise
	 */
	protected boolean hasLogModelElement(LogEvent lme) {
		if ((logModelElement == null) || (lme == null)) {
			return false;
		}
		return logModelElement.equals(lme.getModelElementName(), lme
				.getEventType());
	}

	/**
	 * A task is considered invisible if there is no log event associated to it.
	 * 
	 * @return <code>true</code> if this transition is an invisible task,
	 *         <code>false</code> otherwise
	 */
	public boolean isInvisibleTask() {
		if (logModelElement == null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * A task is considered as a duplicate task if there is at least one other
	 * task in the model which has the same log event associated. <br>
	 * Note that this leads to the fact that the occurrences of duplicate tasks
	 * cannot be distinguished in the event log. Note further that in the case
	 * of duplicate tasks the method {@link PetriNet#findTransitions(LogEvent)
	 * findTransitions} will return multiple tasks being associated to that log
	 * event.
	 * 
	 * @return <code>true</code> if this transition is a duplicate task,
	 *         <code>false</code> otherwise
	 */
	public boolean isDuplicateTask() {
		if (logModelElement == null) {
			return false;
		}

		Iterator allTransitions = ((PetriNet) this.getSubgraph())
				.getTransitions().iterator();
		while (allTransitions.hasNext()) {
			Transition transition = (Transition) allTransitions.next();
			if ((transition != this) && (transition != null)
					&& (transition.getLogEvent() != null)
					&& (transition.getLogEvent().equals(this.getLogEvent()))) {
				// at least one other transition having the same log event found
				// --> duplicate task
				return true;
			}
		}

		// other transition having the same log event found
		return false;
	}

	// //////////////// GENERAL PURPOSE METHODS
	// /////////////////////////////////

	/**
	 * Overridden to specify when two Petri net transitions are considered to be
	 * equal. <br>
	 * Two transitions are considered equal if they have the same name and if
	 * their preceding and succeeding places are also equal.
	 * 
	 * @param o
	 *            the <code>Transition</code> to be compared with
	 * @return <code>true</code> if the identifiers of the transitions and the
	 *         identifiers of all input and output places are the same,
	 *         <code>false</code> otherwise
	 */
	public boolean equals(Object o) {
		// check object identity first
		if (this == o) {
			return true;
		}
		// check type (which includes check for null)
		if ((o instanceof Transition) == false) {
			return false;
		}
		// names must be equal
		if (getIdentifier().equals(((Transition) o).getIdentifier()) == false) {
			return false;
		}
		// names of the predecessors must be equal (object identity is too
		// strong
		// as nodes will be cloned if the Petri net is cloned)
		// however, same order is assumed for the time being (since is
		// preserverd via cloning)
		if (inDegree() != ((Transition) o).inDegree()) {
			return false;
		}
		if (outDegree() != ((Transition) o).outDegree()) {
			return false;
		}

		// names of the predecessors must be equal (object identity is too
		// strong
		// as nodes will be cloned if the Petri net is cloned)
		ArrayList inPlaces = new ArrayList();
		// take all predecessors of this
		inPlaces.addAll(getPredecessors());
		// remove all that are not in the predecessorlist of o
		if (inPlaces.retainAll(((Transition) o).getPredecessors())) {
			// the list changed, i.e. they are not equal
			return false;
		}

		// names of the successors must be equal (object identity is too strong
		// as nodes will be cloned if the Petri net is cloned)
		ArrayList outPlaces = new ArrayList();
		// take all successors of this
		outPlaces.addAll(getSuccessors());
		// remove all that are not in the successorlist of o
		if (outPlaces.retainAll(((Transition) o).getSuccessors())) {
			// the list changed, i.e. they are not equal
			return false;
		}

		return true;
	}

	/**
	 * Overridden to produce the same hash code for equal objects.
	 * 
	 * @return int The hash code calculated.
	 */
	public int hashCode() {
		// simple recipe for generating hashCode given by
		// Effective Java (Addison-Wesley, 2001)
		int result = 17;
		result = 37 * result + identifier.hashCode();
		Iterator myPredecessors = getPredecessors().iterator();
		while (myPredecessors.hasNext()) {
			Place myPlace = (Place) myPredecessors.next();
			result = 37 * result + myPlace.hashCode();
		}
		Iterator mySuccessors = getSuccessors().iterator();
		while (mySuccessors.hasNext()) {
			Place myPlace = (Place) mySuccessors.next();
			result = 37 * result + myPlace.hashCode();
		}
		return result;
	}

	/**
	 * Make a deep copy of the object. Note that this method needs to be
	 * extended as soon as there are attributes added to the class which are not
	 * primitive or immutable. <br>
	 * Note further that the belonging Petri net is not cloned (so the cloned
	 * object will point to the same one as this object). Only the
	 * {@link PetriNet#clone PetriNet.clone()} method will update the reference
	 * correspondingly.
	 * 
	 * @return the cloned object
	 */
	public Object clone() {
		Transition o = null;
		o = (Transition) super.clone();
		// clone referenced objects to realize deep copy
		if (this.logModelElement != null) {
			o.logModelElement = (LogEvent) this.logModelElement.clone();
		}
		return o;
	}

	// added by zhougz 2010.03.31

	/**
	 * Fork fires this transition
	 * <p>
	 * Note that it is not checked whether the transition is enabled. If this is
	 * needed, {@link Transition#fire(Date timestamp) fire(Date timestamp)}
	 * should be used instead.
	 * <p>
	 * The effect is that tokens are not produced in the output places of this
	 * transition.
	 * 
	 * */

	public void forkFire() {
		Iterator it = getInEdgesIterator();
		while (it.hasNext()) {
			Place p = (Place) ((PNEdge) it.next()).getSource();
			Token t = p.getRandomAvailableToken();
			p.removeToken(t);
		}

	}

	/**
	 * fork unfires this transition. Note that it is not checked whether the
	 * transition is negatively enabled. If this is needed,
	 * {@link Transition#unFire() unFire()} should be used instead.
	 */
	public void forkUnfire() {
		Iterator it = getInEdgesIterator();
		while (it.hasNext()) {
			((Place) ((PNEdge) it.next()).getSource()).addToken(new Token());
		}
	}

	@Override
	public int compareTo(Object o) {
		Transition other = (Transition) o;
		return this.getId() - other.getId();
	}
}
