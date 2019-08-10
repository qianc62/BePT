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

import org.processmining.framework.models.*;
import org.processmining.framework.models.fsm.FSMState;

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

public class State extends FSMState implements Comparable {
	public final static int OMEGA = Marking.OMEGA;
	private Marking marking;
	private State predecessor;
	public int index;

	private String id = "";

	public State(StateSpace stateSpace) {
		super(stateSpace, "");
		marking = new Marking();
		predecessor = null;
	}

	public State getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(State state) {
		predecessor = state;
	}

	public int getTokenCount() {
		return marking.getTokenCount();
	}

	public Marking getMarking() {
		return marking;
	}

	public void setIdentifier(String s) {
		id = s;
	}

	public String getIdentifier() {
		return id;
	}

	public void addPlace(Place place, int count) {
		marking.addPlace(place, count);
	}

	public boolean equals(Object state) {
		if (!(state instanceof State)) {
			return false;
		}

		return marking.equals(((State) state).getMarking());
	}

	public Iterator iterator() {
		return marking.iterator();
	}

	public boolean isLessOrEqual(State state) {
		return marking.isLessOrEqual(state.getMarking());
	}

	public int getTokens(Place place) {
		return marking.getTokens(place);
	}

	// Deprecated, use getTokens(place)
	public int getOccurances(Place place) {
		return marking.getTokens(place);
	}

	public int compareTo(Object object) {
		State state = (State) object;
		return marking.compareTo(state.getMarking());
	}

	public String toString() {
		if (id != "") {
			return id;
		} else {
			return marking.toString();
		}
	}

}
