package org.processmining.framework.models.fsm;

import org.processmining.framework.models.ModelGraph;
import java.util.TreeMap;
import org.processmining.framework.ui.Message;
import att.grappa.Edge;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>
 * Title: FSM
 * </p>
 * 
 * <p>
 * Description: Models a Finite State Machine
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 * 
 *          Code rating: Red
 * 
 *          Review rating: Red
 */
public class FSM extends ModelGraph {

	private TreeMap<FSMPayload, FSMState> mapping;

	/**
	 * Create the FSM with given name.
	 * 
	 * @param name
	 *            String The given name.
	 */
	public FSM(String name) {
		super(name);
		setDotAttribute("remincross", "true");
		setDotAttribute("rankdir", "TB");
		setDotNodeAttribute("shape", "ellipse");
		setDotNodeAttribute("height", ".2");
		setDotNodeAttribute("width", ".2");
		mapping = new TreeMap<FSMPayload, FSMState>();
	}

	public void clearFSM() {
		mapping = null;
	}

	public FSMState addState(FSMState state) {
		FSMPayload payload = state.getPayload();
		if (payload == null) {
			this.addVertex(state);
			return state;
		}
		if (!mapping.containsKey(payload)) {
			mapping.put(payload, state);
			this.addVertex(state);
		} else {
			state = mapping.get(payload);
		}
		return state;
	}

	public FSMState getState(FSMPayload payload) {
		return mapping.get(payload);
	}

	public FSMTransition addTransition(FSMState fromState, FSMState toState,
			String condition) {
		FSMPayload payload = fromState.getPayload();
		boolean b = false;
		if (payload != null) {
			if (!mapping.containsKey(payload)) {
				mapping.put(payload, fromState);
				fromState = (FSMState) this.addVertex(fromState);
			} else {
				fromState = mapping.get(payload);
				b = true;
			}
		}
		payload = toState.getPayload();
		if (payload != null) {
			if (!mapping.containsKey(payload)) {
				mapping.put(payload, toState);
				toState = (FSMState) this.addVertex(toState);
			} else {
				toState = mapping.get(payload);
				b = true;
			}
		}
		if (b) {
			if (fromState.getOutEdges() != null) {
				for (Edge edge : fromState.getOutEdges()) {
					FSMTransition oldTransition = (FSMTransition) edge;
					FSMState oldState = (FSMState) oldTransition.getDest();
					if (oldState == toState) {
						if (oldTransition.getCondition().equals(condition)) {
							return oldTransition;
						}
					}
				}
			}
		}
		FSMTransition newTransition = new FSMTransition(fromState, toState,
				condition);
		this.addEdge(newTransition);
		return newTransition;
	}
}
