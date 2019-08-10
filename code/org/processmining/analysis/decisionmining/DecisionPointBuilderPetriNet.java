/**
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *    Copyright (c) 2003-2006 TU/e Eindhoven
 *    by Eindhoven University of Technology
 *    Department of Information Systems
 *    http://is.tm.tue.nl
 *
 ************************************************************************/

package org.processmining.analysis.decisionmining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;

/**
 * Implements the specification of decision points for Petri net models.
 * 
 * @author Anne Rozinat (a.rozinat@tm.tue.nl)
 */
public class DecisionPointBuilderPetriNet implements DecisionPointBuilder {

	/** The visualization class for Petri net decision points. */
	private PetriNet myModel;

	/**
	 * An internal mapping from each decision point to a place in the model (key
	 * = DecisionPoint, value = Place).
	 */
	private HashMap myDecisionPointMapping;

	/** A list containing all decision points for this model. */
	private ArrayList myDecisionPointList;

	/** the enclosing analysis result frame */
	private DecisionPointAnalysisResult myParent;

	/**
	 * Creates a decision point builder for a Petri net model.
	 * 
	 * @param model
	 *            the given Petri net model
	 * @param parent
	 *            the enclosing parent analysis result
	 */
	public DecisionPointBuilderPetriNet(PetriNet model,
			DecisionPointAnalysisResult parent) {
		myModel = model;
		myParent = parent;
	}

	// //////// Interface implementation related methods //////////////

	/**
	 * {@inheritDoc}
	 */
	public int getNumberOfDecisionPoints() {
		return getDecisionPoints().size();
	}

	/**
	 * {@inheritDoc}
	 */
	public DecisionPoint getDecisionPointAt(int index) {
		return (DecisionPoint) getDecisionPoints().get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList getDecisionPoints() {
		if (myDecisionPointMapping == null || myDecisionPointList == null) {
			// determine decision points for this model
			myDecisionPointMapping = new HashMap();
			myDecisionPointList = new ArrayList();
			Iterator allPlaces = myModel.getPlaces().iterator();
			while (allPlaces.hasNext()) {
				Place place = (Place) allPlaces.next();
				// places with more than one outgoing arc are decision points!
				if (place.outDegree() > 1) {
					DecisionPoint decisionPoint = new DecisionPointPetriNet(
							place.getIdentifier(), myParent, myModel, place);
					Iterator outgoingArcs = place.getOutEdges().iterator();
					// follow outgoing arcs to determine target concept
					while (outgoingArcs.hasNext()) {
						PNEdge edge = (PNEdge) outgoingArcs.next();
						Transition destTransition = (Transition) edge.getDest();
						ArrayList targetCategory;
						if (destTransition.isInvisibleTask() == true
								|| destTransition.isDuplicateTask() == true) {
							// trace until visible and non-duplicate tasks have
							// been found
							targetCategory = traceTargetCategory(destTransition);
						} else {
							LogEvent logEvent = destTransition.getLogEvent();
							// although only one element, representation is list
							// of disjunctions
							targetCategory = new ArrayList();
							targetCategory.add(logEvent);
						}

						// only if could be successfully specified (otherwise
						// discard that branch from the analysis)
						if (targetCategory.size() > 0) {
							decisionPoint.addTargetCategory(targetCategory,
									destTransition);
						}
					}
					myDecisionPointMapping.put(decisionPoint, place);
					myDecisionPointList.add(decisionPoint);
				}
			}
		}
		return myDecisionPointList;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set getDirectPredecessors(DecisionPoint refPoint) {
		HashSet resultList = new HashSet();
		Place p = (Place) myDecisionPointMapping.get(refPoint);
		if (p != null) {
			Iterator directPredecessors = p.getPredecessors().iterator();
			while (directPredecessors.hasNext()) {
				Transition predecessor = (Transition) directPredecessors.next();
				if (predecessor.isInvisibleTask() == true) {
					// further trace until visible task found
					Set recursiveList = traceForNextVisiblePredecessors(
							predecessor, new HashSet());
					resultList.addAll(recursiveList);
				} else {
					resultList.add(predecessor);
				}
			}
		}
		return resultList;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set getAllPredecessors(DecisionPoint refPoint) {
		HashSet resultList = new HashSet();
		Place p = (Place) myDecisionPointMapping.get(refPoint);
		if (p != null) {
			Iterator directPredecessors = p.getPredecessors().iterator();
			while (directPredecessors.hasNext()) {
				Transition predecessor = (Transition) directPredecessors.next();
				if (predecessor.isInvisibleTask() == false) {
					resultList.add(predecessor);
				}
				// further trace backwards
				Set recursiveList = traceForAllVisiblePredecessors(predecessor,
						new HashSet());
				resultList.addAll(recursiveList);
			}
		}
		return resultList;
	}

	// //////// Private methods //////////////////////////

	/**
	 * Traces the succesors of an invisible task to determine the succeeding
	 * visible tasks. Note that the search stops if an OR-join has been reached,
	 * and that the nature of the succeeding splits does not matter (i.e.,
	 * succeeding parallel and alternative tasks are treated in the same way).
	 * 
	 * @param invisibleTask
	 *            the task determining the staring point for the recursive
	 *            search
	 * @return a list of log events that can be understood as a disjunctive
	 *         definition of the corresponding target category (i.e., the
	 *         occurrence of any of them means that one specific alternative
	 *         branch has been taken)
	 */
	private ArrayList traceTargetCategory(Transition invisibleTask) {
		ArrayList resultCategory = new ArrayList();
		Iterator nextPlaces = invisibleTask.getSuccessors().iterator();
		while (nextPlaces.hasNext()) {
			Place successorPlace = (Place) nextPlaces.next();
			// stop if OR-join has been encountered (temporary simplification:
			// no loops)
			if (successorPlace.inDegree() == 1) {
				Iterator nextTransitions = successorPlace.getSuccessors()
						.iterator();
				while (nextTransitions.hasNext()) {
					Transition successorTransition = (Transition) nextTransitions
							.next();
					if (successorTransition.isInvisibleTask()
							|| successorTransition.isDuplicateTask() == true) {
						ArrayList recursiveList = traceTargetCategory(successorTransition);
						resultCategory.addAll(recursiveList);
					} else {
						resultCategory.add(successorTransition.getLogEvent());
					}
				}
			}
		}
		return resultCategory;
	}

	/**
	 * Recursively retrieves the direct predecessors or the given transition.
	 * Note that invisible tasks are transparently traced until the next
	 * adjacent visible tasks have been found (they are not represented in the
	 * list as they do not have a log event associated).
	 * 
	 * @param refTrans
	 *            the reference transition for the predecessor search
	 * @param alreadyVisited
	 *            a set of those transitions that have been traced already (in
	 *            order to prevent infinite loops in the case the process model
	 *            is cyclic)
	 * @return the set of all (visible) tasks directly prededing the given
	 *         reference transition
	 */
	private Set traceForNextVisiblePredecessors(Transition refTrans,
			HashSet alreadyVisited) {
		HashSet resultList = new HashSet();
		// prevent infinite loops
		if (alreadyVisited.contains(refTrans) == false) {
			alreadyVisited.add(refTrans);
			Iterator allPrecedingPlaces = refTrans.getPredecessors().iterator();
			while (allPrecedingPlaces.hasNext()) {
				Place precedingPlace = (Place) allPrecedingPlaces.next();
				Iterator allPrecedingTransitions = precedingPlace
						.getPredecessors().iterator();
				while (allPrecedingTransitions.hasNext()) {
					Transition precedingTransition = (Transition) allPrecedingTransitions
							.next();
					if (precedingTransition.isInvisibleTask() == true) {
						Set recursiveList = traceForNextVisiblePredecessors(
								precedingTransition, alreadyVisited);
						resultList.addAll(recursiveList);
					} else {
						resultList.add(precedingTransition);
					}
				}
			}
		}
		return resultList;
	}

	/**
	 * Recursively retrieves all predecessors or the given transition. Note that
	 * invisible tasks are transparently traced (and not represented in the list
	 * as they do not have a log event associated).
	 * 
	 * @param refTrans
	 *            the reference transition for the predecessor search
	 * @param alreadyVisited
	 *            a set of those transitions that have been traced already (in
	 *            order to prevent infinite loops in the case the process model
	 *            is cyclic)
	 * @return the set of all (visible) tasks prededing the given reference
	 *         transition
	 */
	private Set traceForAllVisiblePredecessors(Transition refTrans,
			HashSet alreadyVisited) {
		HashSet resultList = new HashSet();
		// prevent infinite loops
		if (alreadyVisited.contains(refTrans) == false) {
			alreadyVisited.add(refTrans);
			Iterator allPrecedingPlaces = refTrans.getPredecessors().iterator();
			while (allPrecedingPlaces.hasNext()) {
				Place precedingPlace = (Place) allPrecedingPlaces.next();
				Iterator allPrecedingTransitions = precedingPlace
						.getPredecessors().iterator();
				while (allPrecedingTransitions.hasNext()) {
					Transition precedingTransition = (Transition) allPrecedingTransitions
							.next();
					if (precedingTransition.isInvisibleTask() == false) {
						resultList.add(precedingTransition);
					}
					// further trace backwards
					Set recursiveList = traceForAllVisiblePredecessors(
							precedingTransition, alreadyVisited);
					resultList.addAll(recursiveList);
				}
			}
		}
		return resultList;
	}
}
