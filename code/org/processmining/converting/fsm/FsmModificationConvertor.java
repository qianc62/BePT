package org.processmining.converting.fsm;

import org.processmining.converting.Converter;
import javax.swing.JComponent;
import org.processmining.framework.models.fsm.AcceptFSM;
import org.processmining.framework.models.fsm.FSMTransition;
import org.processmining.framework.models.fsm.FSMState;
import java.util.HashMap;
import java.util.HashSet;
import org.processmining.framework.models.ModelGraphVertex;
import att.grappa.Edge;
import java.util.TreeMap;
import java.util.TreeSet;
import org.processmining.framework.models.fsm.FSMPayload;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title: FSsmModificationConvertor
 * </p>
 * 
 * <p>
 * Description: Several conversions for FSMs that might be nice to use after
 * having mined an FSM.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class FsmModificationConvertor {
	private static HashMap<AcceptFSM, FsmModificationSettings> settingsMap = new HashMap<AcceptFSM, FsmModificationSettings>();

	/**
	 * Not implemented yet. This conversion plug-in is embedded into the FSM
	 * mining plug-in.
	 * 
	 * @param fsm
	 *            AcceptFSM
	 * @return JComponent
	 */
	@Converter(name = "FSM modification")
	public static JComponent convert(AcceptFSM fsm) {
		return null;
	}

	/**
	 * Convert the FSM given the settings.
	 * 
	 * @param fsm
	 *            AcceptFSM The given FSM.
	 * @param settings
	 *            FsmModificationSettings The given settings.
	 * @return AcceptFSM The converted FSM.
	 */
	public AcceptFSM convert(AcceptFSM fsm, FsmModificationSettings settings) {
		int oldSize;
		boolean done = false;

		/**
		 * Repeat converting the FSM until no conversion yields results any
		 * more.
		 */
		while (!done) {
			done = true;
			/**
			 * If applicable, remove self loops. Reduces number of edges on
			 * actual removal.
			 */
			if (settings.getUse(FsmModificationSettings.KILLSELFLOOPSTRATEGY)) {
				oldSize = fsm.getNumberOfEdges();
				fsm = killSelfLoops(fsm);
				if (oldSize != fsm.getNumberOfEdges()) {
					Message.add("Removed " + (oldSize - fsm.getNumberOfEdges())
							+ " edges");
					done = false;
				}
			}
			/**
			 * If applicable, improve the diamond structure. Increases number of
			 * edges on actual improvement.
			 */
			if (settings.getUse(FsmModificationSettings.EXTENDSTRATEGY)) {
				oldSize = fsm.getNumberOfEdges();
				fsm = improveDiamondStructure(fsm);
				if (oldSize != fsm.getNumberOfEdges()) {
					Message.add("Added " + (fsm.getNumberOfEdges() - oldSize)
							+ " edges");
					done = false;
				}
			}
			/**
			 * If applicable, merge states if outputs are identical. Reduces
			 * number of states on actual marge.
			 */
			if (settings.getUse(FsmModificationSettings.MERGYBYOUTPUTSTRATEGY)) {
				oldSize = fsm.getVerticeList().size();
				fsm = mergeByOutput(fsm);
				if (oldSize != fsm.getVerticeList().size()) {
					Message.add("Removed "
							+ (oldSize - fsm.getVerticeList().size())
							+ " nodes");
					done = false;
				}
			}
			/**
			 * If applicable, merge states if inputs are identical. Reduces
			 * number of states on actual merge.
			 */
			if (settings.getUse(FsmModificationSettings.MERGYBYINPUTSTRATEGY)) {
				oldSize = fsm.getVerticeList().size();
				fsm = mergeByInput(fsm);
				if (oldSize != fsm.getVerticeList().size()) {
					Message.add("Removed "
							+ (oldSize - fsm.getVerticeList().size())
							+ " nodes");
					done = false;
				}
			}
		}
		return fsm;
	}

	/**
	 * Returns a copy of the given FSM that does not contain self loops.
	 * 
	 * @param fsm
	 *            AcceptFSM
	 * @return AcceptFSM
	 */
	public AcceptFSM killSelfLoops(AcceptFSM fsm) {
		AcceptFSM newFsm = new AcceptFSM(fsm.getName());
		/**
		 * Copy all edges together with their states.
		 */
		for (Object object : fsm.getEdges()) {
			FSMTransition transition = (FSMTransition) object;
			FSMState fromState = (FSMState) transition.getSource();
			FSMState toState = (FSMState) transition.getDest();
			/**
			 * Check whether self loop.
			 */
			if (fromState != toState) {
				/**
				 * No self loop. Copy.
				 */
				FSMState newFromState = new FSMState(newFsm, fromState
						.getLabel(), fromState.getPayload());
				FSMState newToState = new FSMState(newFsm, toState.getLabel(),
						toState.getPayload());
				if (fsm.getStartStates().contains(fromState)) {
					newFsm.setStartState(newFromState);
				}
				if (fsm.getStartStates().contains(toState)) {
					newFsm.setStartState(newToState);
				} else {
					newFsm.addTransition(newFromState, newToState, transition
							.getCondition());
				}
				if (fsm.getAcceptStates().contains(fromState)) {
					newFsm.addAcceptState(newFromState);
				}
				if (fsm.getAcceptStates().contains(toState)) {
					newFsm.addAcceptState(newToState);
				}
			}
		}
		return newFsm;
	}

	/**
	 * Returns a copy of the given FSM with improved diamond structure.
	 * 
	 * @param fsm
	 *            AcceptFSM
	 * @return AcceptFSM
	 */
	public AcceptFSM improveDiamondStructure(AcceptFSM fsm) {
		AcceptFSM newFsm = new AcceptFSM(fsm.getName());
		/**
		 * First, copy all states and edges.
		 */
		for (Object object : fsm.getEdges()) {
			FSMTransition transition = (FSMTransition) object;
			FSMState fromState = (FSMState) transition.getSource();
			FSMState toState = (FSMState) transition.getDest();
			FSMState newFromState = new FSMState(newFsm, fromState.getLabel(),
					fromState.getPayload());
			FSMState newToState = new FSMState(newFsm, toState.getLabel(),
					toState.getPayload());
			if (fsm.getStartState() == fromState) {
				newFsm.setStartState(newFromState);
			} else if (fsm.getStartState() == toState) {
				newFsm.setStartState(newToState);
			}
			if (fsm.getAcceptStates().contains(fromState)) {
				newFsm.addAcceptState(newFromState);
			}
			if (fsm.getAcceptStates().contains(toState)) {
				newFsm.addAcceptState(newToState);
			}
			newFsm.addTransition(newFromState, newToState, transition
					.getCondition());
		}
		/**
		 * Second, check whether we can improve the diamond structure by adding
		 * edges.
		 */
		for (Object object : fsm.getEdges()) {
			FSMTransition transition = (FSMTransition) object;
			FSMState fromState = (FSMState) transition.getSource();
			FSMState toState = (FSMState) transition.getDest();
			if (toState == fromState) {
				continue;
			}
			if (fromState.getOutEdges() == null) {
				continue;
			}
			/**
			 * fromState: state from which we can do actions A and B (A might be
			 * identical to B). toState: state we reach after doing B in
			 * fromState. toState2: state we reach after doing A in fromState.
			 * toState3: state we reach after doing B in toState2. If we cannot
			 * reach toState3 from toState by doing an A, an A edge will be
			 * added from toState to toState3.
			 */
			for (Object object2 : fromState.getOutEdges()) {
				FSMTransition transition2 = (FSMTransition) object2;
				FSMState toState2 = (FSMState) transition2.getDest();
				if (toState2 == fromState) {
					continue;
				}
				if (toState2 == toState) {
					continue;
				}
				if (toState2.getOutEdges() == null) {
					continue;
				}
				for (Object object3 : toState2.getOutEdges()) {
					FSMTransition transition3 = (FSMTransition) object3;
					if (!transition.getCondition().equals(
							transition3.getCondition())) {
						continue;
					}
					FSMState toState3 = (FSMState) transition3.getDest();
					if (toState3 == fromState) {
						continue;
					}
					if (toState3 == toState) {
						continue;
					}
					if (toState3 == toState2) {
						continue;
					}
					if (toState.getOutEdges() == null) {
						continue;
					}
					boolean found = false;
					for (Object object4 : toState.getOutEdges()) {
						FSMTransition transition4 = (FSMTransition) object4;
						if (transition4.getDest() == toState3) {
							if (transition4.getCondition().equals(
									transition2.getCondition())) {
								found = true;
							}
						}
					}
					if (!found) {
						newFsm.addTransition(toState, toState3, transition2
								.getCondition());
					}
				}
			}
		}
		return newFsm;
	}

	/**
	 * Returns a copy of the given FSM with all state with identical outputs
	 * merged.
	 * 
	 * @param fsm
	 *            AcceptFSM
	 * @return AcceptFSM
	 */
	public AcceptFSM mergeByOutput(AcceptFSM fsm) {
		AcceptFSM newFsm = new AcceptFSM(fsm.getName());
		TreeMap<String, FSMState> outputMap = new TreeMap<String, FSMState>();
		TreeMap<Integer, FSMState> stateMap = new TreeMap<Integer, FSMState>();
		for (ModelGraphVertex vertex : fsm.getVerticeList()) {
			FSMState state = (FSMState) vertex;
			TreeSet<String> outgoing = new TreeSet<String>();
			if (state.getOutEdges() != null) {
				for (Edge edge : state.getOutEdges()) {
					if (edge instanceof FSMTransition) {
						FSMTransition transition = (FSMTransition) edge;
						outgoing.add(transition.getCondition());
					}
				}
			}
			FSMState newState;
			if (outputMap.containsKey(outgoing.toString())) {
				newState = outputMap.get(outgoing.toString());
				FSMPayload payload = newState.getPayload().merge(
						state.getPayload());
				newState.setPayload(payload);
			} else {
				newState = new FSMState(newFsm, state.getLabel(), state
						.getPayload());
				outputMap.put(outgoing.toString(), newState);
			}
			stateMap.put(state.getId(), newState);
		}
		for (Object object : fsm.getEdges()) {
			FSMTransition transition = (FSMTransition) object;
			FSMState fromState = (FSMState) transition.getSource();
			FSMState toState = (FSMState) transition.getDest();
			FSMState newFromState = stateMap.get(fromState.getId());
			FSMState newToState = stateMap.get(toState.getId());
			if (fsm.getStartState() == fromState) {
				newFsm.setStartState(newFromState);
			} else if (fsm.getStartState() == toState) {
				newFsm.setStartState(newToState);
			}
			if (fsm.getAcceptStates().contains(fromState)) {
				newFsm.addAcceptState(newFromState);
			}
			if (fsm.getAcceptStates().contains(toState)) {
				newFsm.addAcceptState(newToState);
			}
			newFsm.addTransition(newFromState, newToState, transition
					.getCondition());
		}
		return newFsm;
	}

	/**
	 * Returns a copy of the given FSM with all state with identical inputs
	 * merged.
	 * 
	 * @param fsm
	 *            AcceptFSM
	 * @return AcceptFSM
	 */
	public AcceptFSM mergeByInput(AcceptFSM fsm) {
		AcceptFSM newFsm = new AcceptFSM(fsm.getName());
		TreeMap<String, FSMState> inputMap = new TreeMap<String, FSMState>();
		TreeMap<Integer, FSMState> stateMap = new TreeMap<Integer, FSMState>();
		for (ModelGraphVertex vertex : fsm.getVerticeList()) {
			FSMState state = (FSMState) vertex;
			TreeSet<String> incoming = new TreeSet<String>();
			if (state.getInEdges() != null) {
				for (Edge edge : state.getInEdges()) {
					if (edge instanceof FSMTransition) {
						FSMTransition transition = (FSMTransition) edge;
						incoming.add(transition.getCondition());
					}
				}
			}
			FSMState newState;
			if (inputMap.containsKey(incoming.toString())) {
				newState = inputMap.get(incoming.toString());
				FSMPayload payload = newState.getPayload().merge(
						state.getPayload());
				newState.setPayload(payload);
			} else {
				newState = new FSMState(newFsm, state.getLabel(), state
						.getPayload());
				inputMap.put(incoming.toString(), newState);
			}
			stateMap.put(state.getId(), newState);
		}
		for (Object object : fsm.getEdges()) {
			FSMTransition transition = (FSMTransition) object;
			FSMState fromState = (FSMState) transition.getSource();
			FSMState toState = (FSMState) transition.getDest();
			FSMState newFromState = stateMap.get(fromState.getId());
			FSMState newToState = stateMap.get(toState.getId());
			if (fsm.getStartState() == fromState) {
				newFsm.setStartState(newFromState);
			} else if (fsm.getStartState() == toState) {
				newFsm.setStartState(newToState);
			}
			if (fsm.getAcceptStates().contains(fromState)) {
				newFsm.addAcceptState(newFromState);
			}
			if (fsm.getAcceptStates().contains(toState)) {
				newFsm.addAcceptState(newToState);
			}
			newFsm.addTransition(newFromState, newToState, transition
					.getCondition());
		}
		return newFsm;
	}
}
