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
package org.processmining.analysis.conformance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.fsm.FSMState;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.State;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.CoverabilityGraphBuilder;
import org.processmining.framework.models.petrinet.algorithms.InitialPlaceMarker;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethod;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethodEnum;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;

import att.grappa.Edge;

public class StateSpaceExplorationMethod implements AnalysisMethod {

	private PetriNet myPetriNet; // input Petri net
	private StateSpaceExplorationResult myResult; // corresponding analysis
	// result
	private HashMap<LogEvent, HashSet<Transition>> myAlternativeDuplicateTasks; // remaining
	// alternative
	// duplicates

	private int temp = 0; // counter for amount of traversed paths so far
	private Progress myProgress; // progress bar for state space traversal

	/**
	 * Creates the state space analysis method object. Note that this analysis
	 * only depends on the model (and the mapping established between the log
	 * and the model) but not on the log.
	 * 
	 * @param inputPetriNet
	 *            the PetriNet passed to the conformance check plugin
	 * @praram progress the progress bar to be updated and checked for status
	 */
	public StateSpaceExplorationMethod(PetriNet inputPetriNet, Progress progress) {
		myPetriNet = inputPetriNet;
		myProgress = progress;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return the {@link AnalysisMethodEnum#STATE_SPACE STATE_SPACE}
	 */
	public AnalysisMethodEnum getIdentifier() {
		return AnalysisMethodEnum.STATE_SPACE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.models.petrinet.algorithms.logReplay.
	 * AnalysisMethod
	 * #analyse(org.processmining.framework.models.petrinet.algorithms
	 * .logReplay.AnalysisConfiguration)
	 */
	public AnalysisResult analyse(AnalysisConfiguration analysisOptions) {
		// initialize the result object and the analysis options
		myResult = new StateSpaceExplorationResult(analysisOptions, myPetriNet,
				this);

		// only do the state space analysis if any of the relevant metrics has
		// been
		// requested
		if (myResult.calculateImprovedBehavioralAppropriateness() == true
				|| myResult.calculateImprovedStructuralAppropriateness() == true) {

			// start monitoring progress of log replay
			// myProgress = new Progress("Traversing state space...", 0, 2);
			myProgress.setNote("Traversing state space...");
			myProgress.setMinMax(0, 2);
			myProgress.setProgress(0);

			// build coverability graph from the initial marking in the cloned
			// Petri net
			PetriNet clonedNet = (PetriNet) myPetriNet.clone();
			// remove all tokens and initialize the petri net with one token in
			// start place
			// / TODO - check whether initial marker also necessary for original
			// PN
			InitialPlaceMarker.mark(clonedNet, 1);
			StateSpace coverabilityGraph = CoverabilityGraphBuilder
					.build(clonedNet);

			// remove final states reached by invisible tasks
			// (should not happen due to the lazy semantics of invisible tasks)
			HashSet<ModelGraphVertex> wrongStates = new HashSet<ModelGraphVertex>();
			for (ModelGraphVertex endState : coverabilityGraph.getEndNodes()) {
				boolean wrongState = true;
				for (Edge inEdge : endState.getInEdges()) {
					Transition associatedTransition = (Transition) inEdge.object;
					if (associatedTransition.isInvisibleTask() == false) {
						wrongState = false; // if at least one real transition
						// lead to this state, it is
						// legitimate
						break;
					}
				}
				if (wrongState == true) {
					wrongStates.add(endState);
				}
			}
			for (ModelGraphVertex state : wrongStates) {
				coverabilityGraph.removeVertex(state);
			}

			// contains the states that have been visited already
			HashSet visitedEdges = new HashSet();
			HashSet blockedEdges = new HashSet();
			// find current replay state in state space (i.e., the start state)
			State replayState = (State) coverabilityGraph.getStartState();
			ArrayList<Transition> initialSequence = new ArrayList<Transition>();

			// set up alternative duplicate task detection
			if (myResult.calculateImprovedStructuralAppropriateness() == true) { // configurated

				// TODO move duplicate detection to Petri Net class
				Iterator<Transition> allTrans = clonedNet.getTransitions()
						.iterator();
				HashMap<LogEvent, HashSet<Transition>> mapping = new HashMap<LogEvent, HashSet<Transition>>();
				while (allTrans.hasNext()) {
					Transition currentT = allTrans.next();
					LogEvent currentLe = currentT.getLogEvent();
					if (currentLe != null) {
						HashSet<Transition> duplicates = mapping.get(currentLe);
						if (duplicates != null) {
							duplicates.add(currentT);
						} else {
							duplicates = new HashSet<Transition>();
							duplicates.add(currentT);
						}
						mapping.put(currentLe, duplicates);
					}
				}
				// select only duplicates
				myAlternativeDuplicateTasks = new HashMap<LogEvent, HashSet<Transition>>();
				Iterator<Entry<LogEvent, HashSet<Transition>>> mappingIt = mapping
						.entrySet().iterator();
				while (mappingIt.hasNext()) {
					Entry<LogEvent, HashSet<Transition>> entry = mappingIt
							.next();
					LogEvent le = entry.getKey();
					HashSet<Transition> dupl = entry.getValue();
					if (dupl.size() > 1) {
						myAlternativeDuplicateTasks.put(le, dupl);
					}
				}
			}

			// progress.setNote("Traversing state space...");
			// update GUI progress bar
			myProgress.setProgress(1); // otherwise first time it is not
			// visible!

			try {
				// start recursive state space exploration for the first time
				tracePathsInStateSpace(initialSequence, coverabilityGraph,
						replayState, visitedEdges, blockedEdges);
			} catch (Exception e) {
				// TODO - check how this error can be handled in a better way
				// (e.g. dialog)
				Message
						.add("Analysis aborted by user while traversing the state space. "
								+ e.toString());
			} catch (OutOfMemoryError err) {
				// TODO - check how this error can be handled in a better way
				// (e.g. dialog)
				Message.add("Out of Memory while traversing the state space. "
						+ err.toString());
			}

			// fill the missing relation parts with zero entries
			DiagnosticLogEventRelation globalRelation = myResult
					.getActivityRelations();
			globalRelation.completeRelationByZeroEntries();

			if (myResult.calculateImprovedStructuralAppropriateness() == true) { // configurated
				DiagnosticPetriNet diagnosticNet = myResult.exploredPetriNet;
				// evaluate alternative duplicate tasks
				Iterator<HashSet<Transition>> remainingDupl = myAlternativeDuplicateTasks
						.values().iterator();
				while (remainingDupl.hasNext()) {
					Iterator<Transition> currentSetDupl = remainingDupl.next()
							.iterator();
					while (currentSetDupl.hasNext()) {
						Transition currentDuplicate = currentSetDupl.next();
						// remember alternative duplicate status at diagnostic
						// transition
						DiagnosticTransition diagTrans = (DiagnosticTransition) diagnosticNet
								.findTransition(currentDuplicate);
						diagTrans.setAlternativeDuplicateTask();
						// increment counter for alternative duplicate tasks
						myResult.updateNumberOfAlternativeDuplicates(1);
					}
				}
			}
			// finish GUI progress bar
			myProgress.setProgress(2);
		}

		return myResult;
	}

	/**
	 * Recursive helper method for tracing different execution sequences of
	 * tasks in the process model (state space). <br>
	 * The purpose is an exhaustive exploration of the state space (without
	 * being lost in infinite cycles) and Measurement steps can be injected.
	 * 
	 * @param currentPath
	 *            the firing sequence traced so far
	 * @param coverabilityGraph
	 *            the state space to traverse
	 * @param currentState
	 *            the current state of the traversal
	 * @param passedEdges
	 *            contains the set of edges that were already followed (in order
	 *            to prevent infinite cycles)
	 * @param blockedEdges
	 *            contains the set of edges that should not be followed anymore
	 *            because already were followed twice (in order to prevent
	 *            infinite cycles)
	 * @throws if analysis was aborted by user while traversal
	 */
	protected void tracePathsInStateSpace(ArrayList<Transition> currentPath,
			StateSpace coverabilityGraph, State currentState,
			HashSet<ModelGraphEdge> passedEdges,
			HashSet<ModelGraphEdge> blockedEdges) throws Exception {

		// Message.add("Path " + temp + " - State " +
		// currentState.getIdentifier() + " " + currentState.getLabel(),
		// Message.DEBUG);
		// System.out.println("Path " + temp + " - Current path length: " +
		// currentPath.size() + " - Blocked Edges: " + blockedEdges.size() +
		// "Passed Edges: " + passedEdges.size());

		// abort if user cancelled the log replay
		if (myProgress.isCanceled() == true) {
			throw new Exception(
					"State Space traversal aborted while Processing Path No."
							+ temp);
		} else if (currentState.outDegree() > 0) {
			Iterator outgoingEdges = currentState.getOutEdges().iterator();
			// prevent infinite cycles in the case that outdegree > 0
			// but these edges can all not be followed because they are blocked
			boolean noPathTracable = true;
			while (outgoingEdges.hasNext()) {
				ModelGraphEdge currentEdge = (ModelGraphEdge) outgoingEdges
						.next();
				Transition associatedTransition = (Transition) currentEdge.object;
				// only follow an edge that has not been followed before in
				// order to prevent infinite loops
				if (passedEdges.contains(currentEdge) == false) {
					noPathTracable = false;
					// remember to prevent infinite loops
					HashSet<ModelGraphEdge> extendedPassedEdges = new HashSet<ModelGraphEdge>(
							passedEdges);
					extendedPassedEdges.add(currentEdge);
					// remember firing sequence so far
					ArrayList<Transition> extendedFiringSequence = new ArrayList<Transition>(
							currentPath);
					extendedFiringSequence.add(associatedTransition);
					State nextState = (State) currentEdge.getDest();
					// recursive call
					tracePathsInStateSpace(extendedFiringSequence,
							coverabilityGraph, nextState, extendedPassedEdges,
							blockedEdges);
				}
				// second check: every edge is traced at most 2 times!
				// (afterwards it becomes blocked)
				else if (blockedEdges.contains(currentEdge) == false) {
					noPathTracable = false;
					// remember to prevent infinite loops
					HashSet<ModelGraphEdge> extendedBlockedEdges = new HashSet<ModelGraphEdge>(
							blockedEdges);
					extendedBlockedEdges.add(currentEdge);
					ArrayList<Transition> extendedFiringSequence = new ArrayList<Transition>(
							currentPath);
					extendedFiringSequence.add(associatedTransition);
					State nextState = (State) currentEdge.getDest();
					// recursive call
					tracePathsInStateSpace(extendedFiringSequence,
							coverabilityGraph, nextState, passedEdges,
							extendedBlockedEdges);
				}
			}
			if (noPathTracable == true) { // is the same as if there were no
				// further outedges in this state
				evaluatePath(currentPath);
			}
		} else {
			// evaluate this path according to analysis configuration
			evaluatePath(currentPath);
		}
	}

	// ///////////// Measurement methods for the various metrics

	/**
	 * Builds up the log event relations for the given execution sequence. Also
	 * updates the global relations at the analysis result object.
	 * 
	 * @param path
	 *            the firing sequence derived from the process model
	 */
	protected void evaluatePath(ArrayList<Transition> path) {
		// counting traversed paths in order to give feedback in error message
		temp = temp + 1;

		DiagnosticLogEventRelation leRelation = new DiagnosticLogEventRelation();
		// transparently put artificial start task
		leRelation.addDiagnosticLogEvent("Start", "Artificial");

		// traverse given path
		Iterator<Transition> transitions = path.iterator();
		while (transitions.hasNext()) {
			Transition trans = transitions.next();
			LogEvent le = trans.getLogEvent();
			// only consider visible tasks
			if (le != null) {
				leRelation.addDiagnosticLogEvent(le.getModelElementName(), le
						.getEventType());

				// check for alternative duplicate tasks (if aaS metric enabled)
				checkForAlternativeDuplicateTasks(path, trans);
			}
		}

		// transparently put artificial end task
		leRelation.addDiagnosticLogEvent("End", "Artificial");

		// now update the global log relations
		DiagnosticLogEventRelation globalRelation = myResult
				.getActivityRelations();
		Iterator<DiagnosticLogEvent> it = leRelation.getDiagnosticLogEvents()
				.iterator();
		while (it.hasNext()) {
			DiagnosticLogEvent logEvent = it.next();
			globalRelation.updateLogEventRelation(logEvent, 1);
		}
	}

	/**
	 * Helper method in order to detect non-alternative duplicate tasks and
	 * remove them from the set of duplicate tasks (such that in the end only
	 * alternative duplicate tasks remain in that list.
	 * 
	 * @param path
	 *            the whole model execution path
	 * @param trans
	 *            the transition currently being checked
	 */
	protected void checkForAlternativeDuplicateTasks(
			ArrayList<Transition> path, Transition trans) {
		if (myResult.calculateImprovedStructuralAppropriateness() == true) { // configurated
			LogEvent le = trans.getLogEvent();
			HashSet<Transition> dupl = myAlternativeDuplicateTasks.get(le);
			if (dupl != null) {
				Iterator<Transition> duplIt = dupl.iterator();
				while (duplIt.hasNext()) {
					Transition currentDupl = duplIt.next();
					if (currentDupl != trans
							&& path.contains(currentDupl) == true) {
						// not within class of alternative duplicates -> remove!
						myAlternativeDuplicateTasks.remove(le);
						break;
					}
				}
			}
		}
	}
}
