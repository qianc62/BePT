package org.processmining.analysis.eventmodelmerge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLTypes.EventType;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.ui.Message;

import org.processmining.framework.models.petrinet.TransitionCluster;

/**
 * For the provided HLPetriNet which is based on events, a new HLPetriNet with
 * default information will be generated and which is now based on
 * <i>activities</i>. That means that the events in the HLPetriNet that refer to
 * the same activity are merged into one activity. Also a mapping will be
 * generated for which highlevelactivities, that refer to an event, are merged
 * into which highlevelactivity that refers to an activity.
 * 
 * @author rmans
 */
public class MergeHLPetriNetIntoActivityModel {

	/**
	 * The HLPetriNet that contains events
	 */
	private HLPetriNet hlPetriNetWithEvents;

	/**
	 * the mapping from transitions in the High level PetriNet that refers to
	 * activities, to the transitions in the HLPetriNet that refer to events and
	 * that are merged into that transition that refers to an activity
	 */
	private HashMap<Transition, ArrayList<Transition>> mergedPNTransitions = new HashMap<Transition, ArrayList<Transition>>();

	/**
	 * the mapping from activities in the HLPetriNet model that refer to
	 * activities to the activities in the HLPetriNet that refer to events and
	 * that are merged into that activity in the real HLPetriNet model that
	 * refers to activities.
	 */
	private HashMap<HLID, ArrayList<HLID>> mapping = new HashMap<HLID, ArrayList<HLID>>();

	/**
	 * Basic constructor
	 * 
	 * @param HLPetriNet
	 *            HLPetriNet the HLPetriNet that refers to events and that needs
	 *            to be merged into an HLPetriNet that only refers to activities
	 *            (so with no events)
	 */
	public MergeHLPetriNetIntoActivityModel(HLPetriNet hlPetriNet) {
		super();
		hlPetriNetWithEvents = hlPetriNet;
	}

	/**
	 * Merges the provided HLPetriNet into an HLPetriNet that only refers to
	 * activities.
	 * 
	 * @return HLPetriNet the HLPetriNet that only refers to activities. The
	 *         provided HLPetriNet is a newly created object which only contains
	 *         default information. So no information is transferred from the
	 *         HLPetriNet that refers to events to the HLPetriNet that only
	 *         refers to activities.
	 */
	public HLPetriNet mergeHLPetriNetIntoActivityModel() {
		// create the HLPetriNet with activities
		PetriNet oldPN = hlPetriNetWithEvents.getPNModel();
		PetriNet newPN = (PetriNet) oldPN.clone();
		HLPetriNet activityModel = new HLPetriNet(createActivityModelOfPN(
				newPN, oldPN));
		// create the mapping from the highlevelactivities in the activity model
		// to the highlevelactivities in the
		// HLPetriNet that contained events
		Iterator<Entry<Transition, ArrayList<Transition>>> it = mergedPNTransitions
				.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Transition, ArrayList<Transition>> entry = it.next();
			Transition activityTransition = entry.getKey();
			// find the corresponding highlevelactivity
			HLActivity highLevelActivity = activityModel
					.findActivity(activityTransition);
			// get the merged transitions and find the corresponding
			// highlevelactivities
			ArrayList<HLID> mergedHighLevelTransitions = new ArrayList<HLID>();
			Iterator<Transition> mergedTransitions = entry.getValue()
					.iterator();
			while (mergedTransitions.hasNext()) {
				Transition mergedTransition = mergedTransitions.next();
				// find the corresponding highlevelactivity
				HLActivity hlAct = hlPetriNetWithEvents
						.findActivity(mergedTransition);
				if (hlAct != null) {
					mergedHighLevelTransitions.add(hlAct.getID());
				} else {
					Message
							.add(
									"Error while combining low-level activities into high-level activities for transition: "
											+ mergedTransition.getIdentifier(),
									Message.ERROR);
				}
			}
			// add to the mapping
			mapping.put(highLevelActivity.getID(), mergedHighLevelTransitions);
		}
		// in the case that the Petri Net only contains one type of event, only
		// the event type needs to be removed from the
		// name of the transition
		HashSet<String> evtTypes = getEventTypes(hlPetriNetWithEvents);
		if (evtTypes.size() == 1) {
			// rename the highleveltransitions (the corresponding name of the
			// transition is automatically changed)
			// get the name of the event type
			String evtType = (String) evtTypes.iterator().next();
			Iterator<HLActivity> activitiesIt = activityModel.getHLProcess()
					.getActivities().iterator();
			while (activitiesIt.hasNext()) {
				HLActivity activity = activitiesIt.next();
				String name = activity.getName();
				String nameWithoutEvtType = name.replace(evtType, "");
				String finalName = nameWithoutEvtType.trim();
				activity.setName(finalName);
			}
		}

		return activityModel;
	}

	/**
	 * Returns the different log event types that are present in the PetriNet
	 * 
	 * @param hlPN
	 *            HLPetriNet the PetriNet for which we want to obtain the
	 *            different log event types that are present.
	 * @return HashSet the different log event types that are present in the
	 *         PetriNet
	 */
	private HashSet<String> getEventTypes(HLPetriNet hlPN) {
		HashSet<String> returnEvtTypes = new HashSet<String>();
		Iterator<Transition> transitionsIt = hlPN.getPNModel().getTransitions()
				.iterator();
		while (transitionsIt.hasNext()) {
			Transition transition = transitionsIt.next();
			if (transition.getLogEvent() != null) {
				returnEvtTypes.add(transition.getLogEvent().getEventType());
			}
		}
		return returnEvtTypes;
	}

	/**
	 * merges transitions to activities in the petri net, where possible.
	 * 
	 * @param pn
	 *            PetriNet the petri net for which transitions have to be merged
	 *            into activities.
	 * @param oldPN
	 *            PetriNet the old petri net to keep track of which transitions
	 *            are merged for an activity
	 * @return PetriNet the petri net in which transitions are merged into
	 *         activities, where possible
	 */
	private PetriNet createActivityModelOfPN(PetriNet pn, PetriNet oldPN) {
		// start with merging transitions to activities.
		// it is asssumed that there are no duplicates in the process
		// model!!!!!!!!
		ArrayList placesToExamine = new ArrayList<Place>(pn.getPlaces());
		ArrayList placesIt = new ArrayList(pn.getPlaces());
		for (int i = 0; i < placesIt.size(); i++) {
			if (placesToExamine.contains(placesIt.get(i))) {
				// the place still needs to be checked
				Place placeIt = (Place) placesIt.get(i);
				if (placeIt.inDegree() == 1 && placeIt.outDegree() == 1) {
					// check whether the place is inbetween schedule and start
					Transition inTransition = (Transition) placeIt
							.getPredecessors().iterator().next();
					Transition outTransition = (Transition) placeIt
							.getSuccessors().iterator().next();
					// check whether inTransition and outTransition is already
					// an activity because in that case
					// you can stop with checking
					if (!(mergedPNTransitions.keySet().contains(inTransition) || mergedPNTransitions
							.keySet().contains(outTransition))) {

						// check whether they belong to the same activity
						if (getActivityName(inTransition).equals(
								getActivityName(outTransition))) {

							// for inTransition and outTransition you can have 3
							// different combinations
							// namely: 1) schedule, start 2) start, complete 3)
							// schedule, complete
							if (getEventType(inTransition) == EventType.SCHEDULE
									&& getEventType(outTransition) == EventType.START) {
								// check whether the direct successor of start
								// is complete
								String name = getActivityName(inTransition);
								Transition complTrans = findTransitionByNameAndEvent(
										pn, name, "complete");
								if (complTrans != null) {
									// check whether one of the successor
									// transitions is the complete version
									HashSet checkSet = outTransition
											.getSuccessors();
									checkSet.retainAll(complTrans
											.getPredecessors());
									if (checkSet.size() == 1) {
										// try to fuse schedule, start, complete
										ArrayList nodes = new ArrayList();
										nodes.add(inTransition);
										nodes.add(placeIt);
										nodes.add(outTransition);
										nodes.add(checkSet.iterator().next());
										nodes.add(complTrans);
										mergedPNTransitions
												.putAll(fusionSeriesTransitions(
														pn, nodes,
														placesToExamine, oldPN));
									}
								} else {
									// try to fuse schedule and start
									ArrayList nodes = new ArrayList();
									nodes.add(inTransition);
									nodes.add(placeIt);
									nodes.add(outTransition);
									mergedPNTransitions
											.putAll(fusionSeriesTransitions(pn,
													nodes, placesToExamine,
													oldPN));
								}
							} else if (getEventType(inTransition) == EventType.START
									&& getEventType(outTransition) == EventType.COMPLETE) {
								// check whether the direct predecessor of start
								// is schedule
								String name = getActivityName(inTransition);
								Transition schedTrans = findTransitionByNameAndEvent(
										pn, name, "schedule");
								if (schedTrans != null) {
									// check whether one of the predecessor
									// transitions is the complete version
									HashSet checkSet = inTransition
											.getPredecessors();
									checkSet.retainAll(schedTrans
											.getSuccessors());
									if (checkSet.size() == 1) {
										// try to fuse schedule, start, complete
										ArrayList nodes = new ArrayList();
										nodes.add(schedTrans);
										nodes.add(checkSet.iterator().next());
										nodes.add(inTransition);
										nodes.add(placeIt);
										nodes.add(outTransition);
										mergedPNTransitions
												.putAll(fusionSeriesTransitions(
														pn, nodes,
														placesToExamine, oldPN));
									}
								} else {
									// try to fuse start and complete
									ArrayList nodes = new ArrayList();
									nodes.add(inTransition);
									nodes.add(placeIt);
									nodes.add(outTransition);
									mergedPNTransitions
											.putAll(fusionSeriesTransitions(pn,
													nodes, placesToExamine,
													oldPN));
								}
							} else if (getEventType(inTransition) == EventType.SCHEDULE
									&& getEventType(outTransition) == EventType.COMPLETE) {
								// check whether there should be start in
								// between
								String name = getActivityName(inTransition);
								Transition startTrans = findTransitionByNameAndEvent(
										pn, name, "start");
								if (startTrans == null) {
									// try to fuse schedule and complete
									ArrayList nodes = new ArrayList();
									nodes.add(inTransition);
									nodes.add(placeIt);
									nodes.add(outTransition);
									mergedPNTransitions
											.putAll(fusionSeriesTransitions(pn,
													nodes, placesToExamine,
													oldPN));
								}
							} else {
								// generate exception
							}
						}
					}
				}
			}
		}
		// add the transitions that are not merged also to the mapping
		Iterator transitionsIt = pn.getTransitions().iterator();
		while (transitionsIt.hasNext()) {
			Transition transitionIt = (Transition) transitionsIt.next();
			if (!mergedPNTransitions.containsKey(transitionIt)) {
				// get the corresponding transition in oldPN
				Transition oldTransition = oldPN.findTransition(transitionIt);
				ArrayList<Transition> mappedTransitions = new ArrayList<Transition>();
				mappedTransitions.add(oldTransition);
				mergedPNTransitions.put(transitionIt, mappedTransitions);
			}
		}
		ArrayList<TransitionCluster> clusters = ((PetriNet) pn).getClusters();
		((PetriNet) pn).getClusters().removeAll(clusters);
		return pn;
	}

	/**
	 * Executes a fusion of series transitions ([Murata89], fig22.b). It is
	 * checked whether the fusion of series transitions may be executed.
	 * 
	 * @param newPN
	 *            PetriNet the petri net in which the transitions and places are
	 *            located for which fusion of series transitions has to be
	 *            executed
	 * @param nodes
	 *            ArrayList the places and transitions that are involved in the
	 *            fusion of series transitions. The nodes in the arraylist
	 *            should be of a transition-place-transition sequence in case of
	 *            only three nodes and they should be of a
	 *            transition-place-transition-place-transition sequence in case
	 *            of five nodes. Furthermore, the nodes have to be placed in the
	 *            arraylist in the same order as the sequential relationship
	 *            that exists between the nodes in the petri net.
	 * @param placesToExamine
	 *            ArrayList list that contains the places that need to be
	 *            examined for whether fusion of series transitions is possible.
	 * @param oldPN
	 *            the old petri net (for which transitions needs to be merged
	 *            into activities).
	 * @return a mapping for an activity to the transitions that are merged for
	 *         that activity
	 * 
	 *         If for a transition no transitions have been merged, then there
	 *         is only a mapping from the transition in the final simulation
	 *         model to the transition in the old petri net.
	 */
	private HashMap<Transition, ArrayList<Transition>> fusionSeriesTransitions(
			PetriNet newPN, ArrayList nodes, ArrayList placesToExamine,
			PetriNet oldPN) {
		HashMap<Transition, ArrayList<Transition>> mappingActivityToOldTransitions = new HashMap<Transition, ArrayList<Transition>>();
		ArrayList<Transition> mergedTransitions = new ArrayList<Transition>();
		if (nodes.size() == 5) {
			// check whether the in- and outdegree of the places are 1
			// check whether the indegree of the last two transitions is 1
			Place p1 = (Place) nodes.get(1);
			Place p2 = (Place) nodes.get(3);
			Transition t2 = (Transition) nodes.get(2);
			Transition t3 = (Transition) nodes.get(4);
			if (p1.inDegree() == 1 && p1.outDegree() == 1 && p2.inDegree() == 1
					&& p2.outDegree() == 1 && t2.inDegree() == 1
					&& t3.inDegree() == 1) {
				Transition t1 = (Transition) nodes.get(0);
				// find the transitions in the old PN that are going to be
				// merged
				Transition t1Old = oldPN.findTransition(t1);
				mergedTransitions.add(t1Old);
				Transition t2Old = oldPN.findTransition(t2);
				mergedTransitions.add(t2Old);
				Transition t3Old = oldPN.findTransition(t3);
				mergedTransitions.add(t3Old);

				// fusion for t1, p1 and t2
				newPN.delPlace(p1);
				// merge t1 and t2
				HashSet succT2 = t2.getSuccessors();
				// create an edge from t1 to each successor of t2
				Iterator succs = succT2.iterator();
				while (succs.hasNext()) {
					Place successor = (Place) succs.next();
					PNEdge newEdge = new PNEdge(t1, successor);
					newPN.addEdge(newEdge);
				}
				// remove t2
				newPN.delTransition(t2);

				// fusion for t1, p2, t3
				newPN.delPlace(p2);
				// merge t1 and t3
				HashSet succT3 = t3.getSuccessors();
				// create an edge from t1 to each successor of t3
				Iterator succs2 = succT3.iterator();
				while (succs2.hasNext()) {
					Place successor = (Place) succs2.next();
					PNEdge newEdge = new PNEdge(t1, successor);
					newPN.addEdge(newEdge);
				}
				// remove t3
				newPN.delTransition(t3);
				//
				placesToExamine.remove(p1);
				placesToExamine.remove(p2);
				// rename t1 and remove logevent
				t1.setIdentifier(getActivityName(t1));
				// report which transitions are merged into an activity
				mappingActivityToOldTransitions.put(t1, mergedTransitions);
				// make a fake log event for the transition that is now an
				// activity
				LogEvent fake = new LogEvent(getActivityName(t1), "none");
				t1.setLogEvent(fake);
			}
		} else if (nodes.size() == 3) {
			// check whether the in- and outdegree of the place is 1
			// and that the indegree of transition t2 is 1.
			Place p1 = (Place) nodes.get(1);
			Transition t1 = (Transition) nodes.get(0);
			Transition t2 = (Transition) nodes.get(2);
			if ((p1.inDegree() == 1 && p1.outDegree() == 1)
					&& t2.inDegree() == 1) {

				// find the transitions in the old PN that are going to be
				// merged
				Transition t1Old = oldPN.findTransition(t1);
				mergedTransitions.add(t1Old);
				Transition t2Old = oldPN.findTransition(t2);
				mergedTransitions.add(t2Old);

				// fusion for t1, p1 and t2
				newPN.delPlace(p1);
				// merge t1 and t2
				HashSet succT2 = t2.getSuccessors();
				// create an edge from t1 to each successor of t2
				Iterator succs = succT2.iterator();
				while (succs.hasNext()) {
					Place successor = (Place) succs.next();
					PNEdge newEdge = new PNEdge(t1, successor);
					newPN.addEdge(newEdge);
				}
				// remove t2
				newPN.delTransition(t2);

				// rename t1 and remove logevent
				t1.setIdentifier(getActivityName(t1));
				// report which transitions are merged into an activity
				mappingActivityToOldTransitions.put(t1, mergedTransitions);
				// make a fake log event for the transition that is now an
				// activity
				LogEvent fake = new LogEvent(getActivityName(t1), "none");
				t1.setLogEvent(fake);
			}
		} else {
			// generate exception
		}

		return mappingActivityToOldTransitions;
	}

	/**
	 * Retrieves the mapping of which highlevelactivities (events) are merged
	 * into a real highlevelactivity
	 * 
	 * @return HashMap the mapping from the highlevelactivities of the
	 *         HLPetriNet, that is merged into an activitymodel, to the
	 *         highlevel activities in the provided HLPetriNet with events that
	 *         needed to be merged into an activity model.
	 */
	public HashMap<HLID, ArrayList<HLID>> getMapping() {
		return mapping;
		// HashMap<HLActivity, ArrayList<HLActivity>> returnHashMap = new
		// HashMap<HLActivity,ArrayList<HLActivity>>();
		// // TODO Anne: check and remove
		// //Iterator<Entry<HLTransition, ArrayList<HLTransition>>> it =
		// mapping.entrySet().iterator();
		// Iterator<Entry<String, ArrayList<String>>> it =
		// mapping.entrySet().iterator();
		// while (it.hasNext()) {
		// Entry entry = it.next();
		// HLActivity castKey = (HLActivity) entry.getKey();
		// ArrayList<HLActivity> castValue = (ArrayList<HLActivity>)
		// entry.getValue();
		// returnHashMap.put(castKey, castValue);
		// }
		// return returnHashMap;
	}

	/**
	 * Returns the matching enumeration event type based on the log event
	 * attached to the transition.
	 * 
	 * @param transition
	 *            the transition for which the event type enum is requested
	 * @return the given event type if it can be matched. EventType.COMPLETE
	 *         otherwise
	 */
	private EventType getEventType(Transition transition) {
		EventType result = EventType.COMPLETE;
		// get associated log event type
		LogEvent le = transition.getLogEvent();

		// check for invisible tasks
		if (le != null) {
			String type = le.getEventType();
			if (type.equals("schedule")) {
				result = EventType.SCHEDULE;
			} else if (type.equals("start")) {
				result = EventType.START;
			} else if (type.equals("complete")) {
				result = EventType.COMPLETE;
			}
		}
		return result;
	}

	/**
	 * Retrieves the activity name of a transition.
	 * 
	 * @param transition
	 *            Transition the transition of which we want to obtain the
	 *            activity name.
	 * @return String the activity name of the transition.
	 */
	private String getActivityName(Transition transition) {
		String result = "";
		// get associated log event type
		LogEvent le = transition.getLogEvent();

		// check for invisible tasks
		if (le != null) {
			result = le.getModelElementName();
		} else {
			result = transition.getIdentifier();
		}
		return result;
	}

	/**
	 * Finds the first matching transition in a Petri Net based on a name and
	 * the type of event.
	 * 
	 * @param sourcePN
	 *            PetriNet the Petri Net that needs to be gone through
	 * @param name
	 *            the name
	 * @param event
	 *            the type of event
	 * @return the first transition that satisfies the given name and the name
	 *         of the event. <code>Null</code> otherwise
	 */
	private Transition findTransitionByNameAndEvent(PetriNet sourcePN,
			String name, String event) {
		Transition noTransition = null;
		// get the petrinet of the input simulation model
		Iterator<Transition> transitions = sourcePN.getTransitions().iterator();
		while (transitions.hasNext()) {
			Transition transition = transitions.next();
			LogEvent le = transition.getLogEvent();

			// ignore invisible tasks
			if (le != null) {
				String leName = le.getModelElementName();
				String leType = le.getEventType();
				// return the first matching transition found in the model
				if (leName.equals(name) && leType.equals(event)) {
					return transition;
				}
			}
		}
		return noTransition;
	}
}
