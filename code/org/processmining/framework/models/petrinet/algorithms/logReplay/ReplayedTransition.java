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

package org.processmining.framework.models.petrinet.algorithms.logReplay;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.State;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.Token;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.CoverabilityGraphBuilder;
import org.processmining.framework.ui.Message;

/**
 * A transition involved in the log replay analysis method.
 * 
 * @see ReplayedPlace
 * @see ReplayedPetriNet
 * @see LogReplayAnalysisMethod
 * 
 * @author Anne Rozinat
 */
public class ReplayedTransition extends Transition {

	/**
	 * The constructor creates a normal transition but additionally initializes
	 * the diagnostic information for every process instance specified. <br>
	 * Note that the the copy constructor must be simulated on this level to
	 * assign the right Petri net to the new Transition, which eventually is
	 * required by Grappa.
	 * 
	 * @param t
	 *            the template transition for creating the object (to be passed
	 *            to super class)
	 * @param net
	 *            the Petri net it belongs to (to be passed to super class)
	 * @param caseIDs
	 *            a list of strings, containing the IDs of those cases for which
	 *            we want to store diagnostic results in deriving subclasses
	 *            (see {@link #initDiagnosticDataStructures
	 *            initDiagnosticDataStructures})
	 */
	public ReplayedTransition(Transition t, PetriNet net, ArrayList caseIDs) {
		super(t.getLogEvent(), net);
		this.setValue(t.getValue());
		this.setIdentifier(t.getIdentifier());
		// so deriving subclasses only need to override this init method
		initDiagnosticDataStructures(caseIDs);
	}

	// /////// METHODS TO BE OVERRIDDEN IN SUBCLASSES //////////

	/**
	 * Initializes the diagnostic data structures. Does nothing per default (to
	 * be overridden in subclasses if necessary).
	 * 
	 * @param caseIDs
	 *            a list of strings, containing the IDs of those cases for which
	 *            we want to store diagnostic results in deriving subclasses
	 */
	protected void initDiagnosticDataStructures(ArrayList caseIDs) {
		// do nothing per default
	}

	/**
	 * Will be called directly before this transition is fired. Does nothing per
	 * default (to be overridden in subclasses if necessary).
	 * 
	 * @param replayedTrace
	 *            the case currently replayed
	 * @param measurer
	 *            the measurer object in order to record diagnostic information
	 */
	protected void takePreFiringMeasurement(ReplayedLogTrace replayedTrace,
			Measurer measurer) {
		// do nothing per default
	}

	/**
	 * Will be called directly after this transition has been fired. Does
	 * nothing per default (to be overridden in subclasses if necessary).
	 * 
	 * @param replayedTrace
	 *            the case currently replayed
	 */
	protected void takePostFiringMeasurement(ReplayedLogTrace replayedTrace) {
		// do nothing per default
	}

	// /////// LOG REPLAY RELATED METHODs //////////

	/**
	 * Fires the transition like normally but in addition adds diagnostic
	 * information for the respective process instance.
	 * 
	 * @param replayedTrace
	 *            the trace currently replayed
	 * @param timeStamp
	 *            the time stamp at which the transition is fired (can be
	 *            <code>null</code> if the log does not contain time
	 *            information)
	 * @param measurer
	 *            the measurer object in order to record diagnostic information
	 */
	public void fireQuick(ReplayedLogTrace replayedTrace, Date timeStamp,
			Measurer measurer) {
		// inject measurement
		takePreFiringMeasurement(replayedTrace, measurer);
		// actually fire this transition
		fireTransition(replayedTrace, timeStamp);
		// inject measurement
		takePostFiringMeasurement(replayedTrace);
	}

	/**
	 * Will be called when this transition fires (during log replay). It is
	 * needed to keep track of times-in-between transitions. Does nothing per
	 * default (to be overridden in subclasses if necessary).
	 * 
	 * @param replayedTrace
	 *            ReplayedLogTrace: the trace that is currently replayed
	 * @param timestamp
	 *            Date
	 */
	protected void takeTimeBetweenMeasurement(ReplayedLogTrace replayedTrace,
			Date timestamp) {
		// do nothing by default
	}

	/**
	 * Will be called directly when this transition has fired. It is needed for
	 * calculation of activity-related times (such as execution time, throughput
	 * time) Does nothing per default (to be overridden in subclasses if
	 * necessary).
	 * 
	 * @param trace
	 *            ReplayedLogTrace The trace that is currently being replayed
	 * @param timestamp
	 *            Date TimeStamp at which this transition has fired
	 * @param transEnabled
	 *            Date TimeStamp at which this transition was enabled
	 */
	protected void takeActivityMeasurement(ReplayedLogTrace trace,
			Date timestamp, Date transEnabled) {
		// do nothing by default
	}

	/**
	 * Fires the transition with or without time stamp. If a time stamp is
	 * available, e.g., measurements evaluating the time stamp of the consumed
	 * tokens are given.
	 * 
	 * @param replayedTrace
	 *            the trace currently being replayed
	 * @param timeStamp
	 *            the time stamp related to the firing of this transition (can
	 *            be <code>null</code> if the log does not contain time
	 *            information)
	 */
	private void fireTransition(ReplayedLogTrace replayedTrace, Date timeStamp) {
		Iterator it = getInEdgesIterator();
		Date maximumTime = null;
		while (it.hasNext()) {
			PNEdge e = (PNEdge) it.next();
			ReplayedPlace p = (ReplayedPlace) e.getSource();
			// record that the traversed edge has been visited
			ReplayedEdge re = (ReplayedEdge) e;
			re.recordProbMeasurement(replayedTrace);

			Token t;
			if (timeStamp == null) {
				t = p.getRandomAvailableToken();
			} else {
				t = p.getRandomAvailableToken(timeStamp);
			}
			Date tokenTime = t.getTimestamp();
			if (maximumTime == null) {
				maximumTime = tokenTime;
			} else {
				if ((tokenTime != null) && (tokenTime.after(maximumTime))) {
					maximumTime = tokenTime;
				}
			}
		}
		if (timeStamp == null) {
			timeStamp = maximumTime;
		}

		if (timeStamp == null) {
			super.fireQuick();
		} else {
			if (maximumTime != null) {
				// Record for each input place:
				// 1) Time of the token in the place
				// 2) MaximumTime - time of the token in the place
				// 3) Time of firing - maximumTime
				// 4) name of Process Instance
				it = getInEdgesIterator();
				while (it.hasNext()) {
					ReplayedPlace p = (ReplayedPlace) ((PNEdge) it.next())
							.getSource();
					Token t;
					t = p.getRandomAvailableToken(timeStamp);
					Date tokenTime = t.getTimestamp();
					if (tokenTime == null) {
						tokenTime = maximumTime;
					}
					// inject measurements
					p.recordTimeMeasurement(tokenTime, maximumTime, timeStamp,
							replayedTrace);

					p.removeEnablingTransition(timeStamp, replayedTrace, this);
				}
			}

			it = getOutEdgesIterator();
			while (it.hasNext()) {
				// set this transition to be the enabling transition of all
				// output places
				ReplayedPlace p = (ReplayedPlace) ((PNEdge) it.next())
						.getDest();
				p.setEnablingTransition(this);
			}
			// take activity measurement
			takeActivityMeasurement(replayedTrace, timeStamp, maximumTime);
			// take time-in-between measurement
			takeTimeBetweenMeasurement(replayedTrace, timeStamp);
			super.fireQuick(timeStamp);
		}
	}

	/**
	 * Check whether this transition could be enabled through invisible tasks
	 * and do it if possible; i.e., the current marking of the net is changed
	 * (used to replay nets with invisible tasks correctly).
	 * 
	 * @param replayedTrace
	 *            the log trace for which the potential firings will be noted
	 *            (can be <code>null</code> if no diagnostic information should
	 *            be collected)
	 * @param timeStamp
	 *            the time at which the transition should be enabled (can be
	 *            <code>null</code> if the log does not contain time
	 *            information)
	 * @param maxDepth
	 *            the maximum depth of search for a sequence of invisible tasks.
	 *            Limited search if maxDepth >= 0. The full state space will be
	 *            constructed if maxDepth < 0
	 * @param measurer
	 *            the measurer object in order to record diagnostic information
	 * @return <code>true</code> if could be enabled, <code>false</code>
	 *         otherwise
	 */
	public boolean isEnabled(ReplayedLogTrace replayedTrace, Date timeStamp,
			int maxDepth, Measurer measurer, LogReplayAnalysisMethod logReplay,
			int atesPos) {
		ReplayedPetriNet replayedNet = (ReplayedPetriNet) getSubgraph();
		List<Transition> firingSequence = getShortestSequenceOfInvisibleTasks(
				replayedNet, maxDepth, logReplay, atesPos);
		if (firingSequence != null) {
			// fire the found sequence of invisible tasks to enable this
			// transition
			fireSequenceOfTasks(firingSequence, replayedNet, replayedTrace,
					timeStamp, measurer);
			return true;
		} else {
			// there is no sequence of invisible tasks enabling this transition
			return false;
		}
	}

	/**
	 * Actually fire a given sequence of transitions in the given Petri net.
	 * Note that the transitions do not need to be objects contained in the
	 * given net but their corresponding transition will be looked up using the
	 * <code>equals()<code> method.
	 * Note further that it is assumed that there is such a corresponding
	 * transition each and that their execution is seamlessly possible, i.e.,
	 * all of them will be enabled.
	 * 
	 * @param firingSequence
	 *            the list of transitions to be fired
	 * @param replayedNet
	 *            the Petri net currently replayed
	 * @param replayedTrace
	 *            the trace currently replayed (can be <code>null</code> if no
	 *            diagnostic information should be collected)
	 * @param timeStamp
	 *            the time at which the transition should be enabled (can be
	 *            <code>null</code> if the log does not contain time
	 *            information)
	 * @param measurer
	 *            the measurer object in order to record diagnostic information
	 */
	public void fireSequenceOfTasks(List<Transition> firingSequence,
			ReplayedPetriNet replayedNet, ReplayedLogTrace replayedTrace,
			Date timeStamp, Measurer measurer) {
		Iterator foundSequence = firingSequence.iterator();
		while (foundSequence.hasNext()) {
			ReplayedTransition taskInStateSpace = (ReplayedTransition) foundSequence
					.next();
			// get corresponding transition of the real net
			ReplayedTransition taskInReplayedNet = (ReplayedTransition) replayedNet
					.findTransition(taskInStateSpace);
			if (replayedTrace != null) {
				taskInReplayedNet.fireQuick(replayedTrace, timeStamp, measurer);
			} else {
				if (timeStamp == null) {
					taskInReplayedNet.fireQuick();
				} else {
					taskInReplayedNet.fireQuick(timeStamp);
				}
			}
		}
	}

	/**
	 * Check whether the transition might be enabled through one or more
	 * invisible transitions. The shortest sequence found will be delivered.
	 * 
	 * @param replayedNet
	 *            the Petri net currently replayed
	 * @param maxDepth
	 *            the maximum depth of search for a sequence of invisible tasks.
	 *            Limited search if maxDepth >= 0. The full state space will be
	 *            constructed if maxDepth < 0
	 * @return the shortest sequence found (empty list if was enabled already),
	 *         <code>null</code> otherwise
	 */
	public List<Transition> getShortestSequenceOfInvisibleTasks(
			ReplayedPetriNet replayedNet, int maxDepth,
			LogReplayAnalysisMethod logReplay, int atesPos) {
		List<Transition> foundFiringSequence = new ArrayList<Transition>();
		if (super.isEnabled() == true) {
			// if normally enabled no extra check necessary (empty list
			// returned)
		} else if (replayedNet.hasEnabledInvisibleTasks() == false) {
			// if there are no invisible tasks in the model, or none of them is
			// enabled, we do not need to check the state space (for perfomance
			// reasons)
			return null;
		} else {
			if (maxDepth == 0) {
				// for performance reasons, the building of the coverability
				// graph is avoided
				return null;
			}
			// build coverability graph from the current marking,
			// note that the net must be cloned before to preserve its replay
			// state
			ReplayedPetriNet clonedNet = (ReplayedPetriNet) replayedNet.clone();
			if (maxDepth > 0) {
				// increase maxDepth by one as in addition to the specified
				// length of invisible tasks
				// also the currently replayed transition must be present in the
				// partial state space
				// --> but if maxDepth is -1, this would result in 0 instead of
				// building the whole state space
				maxDepth = maxDepth + 1;
			}
			StateSpace coverabilityGraph = CoverabilityGraphBuilder.build(
					clonedNet, maxDepth);
			// find current replay state in state space (i.e., the start state)
			State replayState = (State) coverabilityGraph.getStartState();
			foundFiringSequence = findShortesPathOfInvisibleTasks(
					coverabilityGraph, replayState, logReplay, clonedNet,
					atesPos);

		}
		return foundFiringSequence;
	}

	/**
	 * Finds the best shortest sequence of invisible tasks to enable this
	 * transition in and iterative way.
	 * 
	 * @param coverabilityGraph
	 *            the partial state space (limited by the maxdepth)
	 * @param initialState
	 *            the start state for looking into enabling sequences
	 * @return the shortest sequence of invisible tasks (if there are more than
	 *         one shortest, the future will be explored to choose the best
	 *         candidate)
	 */
	private List<Transition> findShortesPathOfInvisibleTasks(
			StateSpace coverabilityGraph, State initialState,
			LogReplayAnalysisMethod logReplay, ReplayedPetriNet petriNet,
			int atesPos) {
		ArrayList<Transition> resultList = new ArrayList<Transition>();
		HashSet<ReplayState> replayScenarios = new HashSet<ReplayState>();
		HashSet<ReplayState> successfulReplays = new HashSet<ReplayState>();
		Iterator outgoingEdges;
		// length 1 sequences
		outgoingEdges = initialState.getOutEdgesIterator();
		while (outgoingEdges.hasNext()) {
			ModelGraphEdge currentEdge = (ModelGraphEdge) outgoingEdges.next();
			ReplayedTransition associatedTransition = (ReplayedTransition) currentEdge.object;
			if (associatedTransition.isInvisibleTask()) {
				ReplayState replayState = new ReplayState();
				replayState.visitedStates.add(initialState);
				replayState.transitionSequence.add(associatedTransition);
				replayState.currentReplayState = (State) currentEdge.getDest();
				replayScenarios.add(replayState);
			}
		}
		// length > 1 sequences
		while (successfulReplays.size() == 0 && replayScenarios.size() != 0) {
			HashSet<ReplayState> copiedReplayScenarios = (HashSet<ReplayState>) replayScenarios
					.clone(); // to avoid concurrent modification
			for (ReplayState scenario : copiedReplayScenarios) {
				outgoingEdges = scenario.currentReplayState
						.getOutEdgesIterator();
				while (outgoingEdges.hasNext()) {
					ModelGraphEdge currentEdge = (ModelGraphEdge) outgoingEdges
							.next();
					ReplayedTransition associatedTransition = (ReplayedTransition) currentEdge.object;
					if (associatedTransition.equals(this)) {
						successfulReplays.add(scenario);
						break;
					} else if (associatedTransition.isInvisibleTask()) {
						State nextState = (State) currentEdge.getDest();
						if (scenario.visitedStates.contains(nextState) == false) {
							ReplayState replayState = new ReplayState();
							replayState.visitedStates = new HashSet<State>(
									scenario.visitedStates);
							replayState.visitedStates.add(initialState);
							replayState.transitionSequence = new ArrayList<Transition>(
									scenario.transitionSequence);
							replayState.transitionSequence
									.add(associatedTransition);
							replayState.currentReplayState = nextState;
							replayScenarios.add(replayState);
						}
					}
				}
				replayScenarios.remove(scenario);
			}
		}
		if (successfulReplays.size() == 0) {
			return null;
		} else if (successfulReplays.size() == 1
				|| logReplay.findBestShortestSequence == false) {
			resultList
					.addAll(successfulReplays.iterator().next().transitionSequence);
		} else if (successfulReplays.size() > 1) {
			HashMap<List<Transition>, Transition> candidates = new HashMap<List<Transition>, Transition>();
			for (ReplayState candidate : successfulReplays) {
				candidates.put(candidate.transitionSequence, this);
			}
			try {
				List<Transition> traced = logReplay
						.chooseBestSequenceOfInvisibleTasks(candidates,
								petriNet, atesPos);
				if (traced != null) {
					return traced;
				} else {
					return candidates.keySet().iterator().next();
				}
			} catch (Exception ex) {
				Message
						.add("Search for best (of multiple) shortest sequences failed.");
				ex.printStackTrace();
				resultList.addAll(candidates.keySet().iterator().next());
			}
		}
		return resultList;
	}

	/**
	 * Data structure holding the current replay state, the visited states and a
	 * sequence of transitions for log replay purposes.
	 */
	class ReplayState {
		State currentReplayState;
		List<Transition> transitionSequence = new ArrayList<Transition>();
		Set<State> visitedStates = new HashSet<State>(); // remember to prevent
		// infinite loops
	}

}
