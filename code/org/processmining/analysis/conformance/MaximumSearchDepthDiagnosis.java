package org.processmining.analysis.conformance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;

import att.grappa.Edge;

/**
 * This class suggests a maximum search depth for sequences of invisible tasks
 * based on the structure of the Petri net. This can be used to build only a
 * partial state space during the search for invisible tasks that might enable
 * another, visible task. The maximum number of directly connected invisible
 * tasks is determined for estimating this value.
 * 
 * @author Anne Rozinat
 */
public class MaximumSearchDepthDiagnosis {

	/**
	 * This method estimates the maximum search depth based on the maximum
	 * number of directly connected invisible tasks.
	 * 
	 * @param net
	 *            the given Petri net
	 * @return the maximum search depth
	 */
	public static int determineMaximumSearchDepth(PetriNet net) {

		// check all transitions for their maximum-length sequence for
		// enabling transitions
		int max = 0;
		Iterator<Transition> allTrans = net.getTransitions().iterator();
		while (allTrans.hasNext()) {
			Transition trans = allTrans.next();
			int currentMax = MaximumSearchDepthDiagnosis
					.determineMaximumEnablingSequ(trans,
							new ArrayList<Transition>());
			if (currentMax == -1) {
				// due to loop of invisible tasks no maximum search depth can be
				// determined
				return -1;
			} else if (currentMax > max) {
				max = currentMax;
			}
		}

		// check also for invisible tasks leading to the final place
		// (as they need to be transparently fired to properly complete)
		Iterator<Place> allPlaces = net.getPlaces().iterator();
		while (allPlaces.hasNext()) {
			Place place = allPlaces.next();
			if (place.outDegree() == 0) {
				// final place! (recall assumption of workflow net)
				Iterator<Edge> inEdgesFinalPlace = place.getInEdgesIterator();
				while (inEdgesFinalPlace.hasNext()) {
					PNEdge edgeToPlace = (PNEdge) inEdgesFinalPlace.next();
					Transition trans = (Transition) edgeToPlace.getTail();
					if (trans.isInvisibleTask() == true) {
						ArrayList<Transition> alreadyVisited = new ArrayList<Transition>();
						alreadyVisited.add(trans);
						int currentMax = MaximumSearchDepthDiagnosis
								.determineMaximumEnablingSequ(trans,
										alreadyVisited);
						if (currentMax == -1) {
							// due to loop of invisible tasks no maximum search
							// depth can be determined
							return -1;
						} else if (currentMax + 1 > max) {
							max = currentMax + 1;
						}
					}
				}
				break;
			}
		}

		return max;
	}

	/**
	 * Helper method to determine the maximum enabling sequence of invisible
	 * tasks from the given transition backwards.
	 * 
	 * @param trans
	 *            the transition for which to determine the max sequence
	 * @param alreadyVisited
	 *            the list of invisible transitions already traced in recursive
	 *            way. Needed to prevent infinite loop in case of loop of
	 *            invisible tasks in the model
	 * @return the maximum enabling sequence of invisible tasks for this task.
	 *         Can be 0 if there are no invisible tasks in the model. If there
	 *         are loops of invisible tasks then the whole coverability graph
	 *         needs to be built during log replay (and now maximum search depth
	 *         can be determined). In this case -1 is returned
	 */
	private static int determineMaximumEnablingSequ(Transition startTrans,
			ArrayList<Transition> alreadyVisited) {
		int max = 0;

		// look for all input places (to be added)
		List<Edge> inEdges = startTrans.getInEdges();
		if (inEdges != null) {
			Iterator<Edge> inEdgesTrans = startTrans.getInEdges().iterator();
			while (inEdgesTrans.hasNext()) {
				PNEdge edgeToTrans = (PNEdge) inEdgesTrans.next();
				Place place = (Place) edgeToTrans.getTail();

				// look for all input transitions (to determine the maximum)
				int placeMax = 0;
				Iterator<Edge> inEdgesPlace = place.getInEdgesIterator();
				while (inEdgesPlace.hasNext()) {
					PNEdge edgeToPlace = (PNEdge) inEdgesPlace.next();
					Transition trans = (Transition) edgeToPlace.getTail();
					if (trans.isInvisibleTask() == true) {
						// check for loops of invisible tasks
						if (alreadyVisited.contains(trans)) {
							return -1;
						} else {
							alreadyVisited.add(trans);
							int transMax = MaximumSearchDepthDiagnosis
									.determineMaximumEnablingSequ(trans,
											alreadyVisited);
							// check for loops of invisible tasks
							if (transMax == -1) {
								return -1;
							} else if (transMax + 1 > placeMax) {
								// set current transition to current maximum
								// (plus 1 as the invisible task itself needs to
								// be counted)
								placeMax = transMax + 1;
							}
						}
					}
				}
				// add maximum length to overall maximum length
				max = max + placeMax;
			}
		}

		// return search depth needed for enabling this task via invisibles
		return max;
	}
}
