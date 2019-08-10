/**
 * <p>Title: YawlToPetriNet</p>
 *
 * <p>Description: Conversions from Yawl models to Petri nets.</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: TU/e</p>
 *
 * @author Eric Verbeek
 * @version 1.0
 */

package org.processmining.converting.yawl2pn;

import org.processmining.framework.models.petrinet.*;
import org.processmining.framework.models.yawl.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import org.processmining.converting.Converter;
import org.processmining.converting.yawl2yawl.*;
import java.util.Collection;
import org.processmining.mining.petrinetmining.PetriNetResult;
import org.processmining.framework.log.LogEvent;
import org.processmining.converting.PetriNetReduction;

public class YawlToPetriNet {
	/*
	 * Prefixes to be used for places and transitions.
	 */
	private static final String COND_STRING = "COND_"; // for places
	// corresponding to
	// conditions.
	private static final String JOIN_STRING = "JOIN_"; // for transitions
	// corresponding to
	// joins.
	private static final String PRE_STRING = "PRE_"; // for places signalling
	// that a task has
	// started.
	private static final String TASK_STRING = "TASK_"; // for transitions
	// corresponding to
	// tasks.
	private static final String POST_STRING = "POST_"; // for places signalling
	// that a has has
	// completed.
	private static final String SPLIT_STRING = "SPLIT_"; // for transitions
	// corresponding to
	// splits.
	private static final String COMPLETE_STRING = "complete";

	public YawlToPetriNet() {
	}

	@Converter(name = "YAWL: Convert to Petri net", help = "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:yawl2pn:convert")
	public static PetriNetResult convert(YAWLModel model) {
		HashMap map0 = new HashMap();
		HashMap map1 = new HashMap();
		YawlToYawlSettings settings0 = new YawlToYawlSettings();
		YawlToPetriNetSettings settings1 = new YawlToPetriNetSettings();
		YAWLModel model1 = YawlToYawl.addImplicitConditions(model, settings0,
				map0);
		PetriNet petriNet = convert(model1, settings1, map1);
		PetriNetResult result = new PetriNetResult(null, petriNet);
		return result;
	}

	/**
	 * Convert the given YAWL model to a (possibly reduced) Petri net.
	 * Precondition: All implicit conditions have been added to the YAWL model.
	 * 
	 * @param model
	 *            YAWLModel The given YAWL model.
	 * @param settings
	 *            YawlToPetriNetSettings The settings to use during the
	 *            conversion.
	 * @param map
	 *            HashMap The map to store the YAWL-PN relations in. YAWL
	 *            conditions are mapped onto a place. YAWL tasks, however, are
	 *            mapped onto an array list where: - the first element contains
	 *            the set of join transitions, - the second element is the
	 *            in-between (or busy) place, and - the third element contains
	 *            the set of split transitions. Reset edges are ignored by this
	 *            conversion.
	 * @return PetriNet
	 */
	public static PetriNet convert(YAWLModel model,
			YawlToPetriNetSettings settings, HashMap map) {
		ArrayList nonReducableNodes = new ArrayList();
		/*
		 * Convert the YAWL model to a Petri net.
		 */
		PetriNet petriNet = convert(model, settings, map, nonReducableNodes);
		if (settings.get(YawlToPetriNetSettings.REDUCE_PN)) {
			/*
			 * We have to reduce the Petri net.
			 */
			/*
			 * For mapping object from the original Petri net to the reduced
			 * Petri net.
			 */
			HashMap reductionMap = new HashMap();
			/*
			 * Create a reduced Petri net, leaving the non-reducable nodes
			 * alone.
			 */
			PetriNetReduction reduction = new PetriNetReduction();
			reduction.setNonReducableNodes(nonReducableNodes);
			PetriNet reducedPN = reduction.reduce(petriNet, reductionMap);
			/*
			 * Combine both mappings.
			 */
			HashMap combinedMap = new HashMap();
			for (Object key : map.keySet()) {
				Object value = map.get(key);
				if (value instanceof Place) {
					Place place = (Place) value;
					if (reductionMap.containsKey(place)) {
						combinedMap.put(key, reductionMap.get(place));
					}
				} else if (value instanceof ArrayList) {
					ArrayList list = (ArrayList) value;
					if (list.size() == 3) {
						HashSet joinTransitions = new HashSet();
						for (Object object : (HashSet) list.get(0)) {
							if (reductionMap.containsKey(object)) {
								joinTransitions.add(reductionMap.get(object));
							}
						}
						/*
						 * Join place is non-reducable, hence it should be in
						 * the reduction map.
						 */
						Place joinPlace = (Place) reductionMap.get(list.get(1));
						HashSet splitTransitions = new HashSet();
						for (Object object : (HashSet) list.get(2)) {
							if (reductionMap.containsKey(object)) {
								splitTransitions.add(reductionMap.get(object));
							}
						}
						ArrayList newList = new ArrayList();
						newList.add(joinTransitions);
						newList.add(joinPlace);
						newList.add(splitTransitions);
						combinedMap.put(key, newList);
						if (settings
								.get(YawlToPetriNetSettings.CREATE_CLUSTERS)) {
							/*
							 * Add a cluster for this task. Improves
							 * recognition.
							 */
							TransitionCluster cluster = new TransitionCluster(
									((YAWLNode) key).getIdentifier());
							for (Object object : joinTransitions) {
								cluster.addTransition((Transition) object);
							}
							for (Object object : splitTransitions) {
								cluster.addTransition((Transition) object);
							}
							reducedPN.addCluster(cluster);
						}
					} else if (list.size() == 5) {
						HashSet joinTransitions = new HashSet();
						for (Object object : (HashSet) list.get(0)) {
							if (reductionMap.containsKey(object)) {
								joinTransitions.add(reductionMap.get(object));
							}
						}
						/*
						 * Join place and task transition are non-reducable,
						 * hence they should be in the reduction map.
						 */
						Place joinPlace = (Place) reductionMap.get(list.get(1));
						Transition taskTransition = (Transition) reductionMap
								.get(list.get(2));
						Place splitPlace;
						if (reductionMap.containsKey(list.get(3))) {
							splitPlace = (Place) reductionMap.get(list.get(3));
						} else {
							splitPlace = null;
						}
						HashSet splitTransitions = new HashSet();
						for (Object object : (HashSet) list.get(4)) {
							if (reductionMap.containsKey(object)) {
								splitTransitions.add(reductionMap.get(object));
							}
						}
						ArrayList newList = new ArrayList();
						newList.add(joinTransitions);
						newList.add(joinPlace);
						newList.add(taskTransition);
						newList.add(splitPlace);
						newList.add(splitTransitions);
						combinedMap.put(key, newList);
						if (settings
								.get(YawlToPetriNetSettings.CREATE_CLUSTERS)) {
							/*
							 * Add a cluster for this task. Improves
							 * recognition.
							 */
							TransitionCluster cluster = new TransitionCluster(
									((YAWLNode) key).getIdentifier());
							for (Object object : joinTransitions) {
								cluster.addTransition((Transition) object);
							}
							cluster.addTransition(taskTransition);
							for (Object object : splitTransitions) {
								cluster.addTransition((Transition) object);
							}
							reducedPN.addCluster(cluster);
						}
					}
				}
			}
			/*
			 * Use reduced Petri net instead of original Petri net.
			 */
			petriNet = reducedPN;
			/*
			 * Copy combined map into map to reflect the change in Petri net.
			 */
			map.clear();
			map.putAll(combinedMap);
		}
		return petriNet;
	}

	/**
	 * Convert the given YAWL model to a Petri net. Precondition: All implicit
	 * conditions have been added to the YAWL model.
	 * 
	 * @param model
	 *            YAWLModel The given YAWL model.
	 * @param settings
	 *            YawlToPetriNetSettings The settings to use during the
	 *            conversion.
	 * @param map
	 *            HashMap The map to store the YAWL-PN relations in. YAWL
	 *            conditions are mapped onto a place. YAWL tasks, however, are
	 *            mapped onto an array list where: - the first element contains
	 *            the set of join transitions, - the second element is the
	 *            in-between (or busy) place, and - the third element contains
	 *            the set of split transitions.
	 * @param nonReducableNodes
	 *            ArrayList The list to stroe non-reducable (sacrosanct) nodes
	 *            in. These nodes will 'survive' the reduction. Reset edges are
	 *            ignored by this conversion.
	 * @return PetriNet
	 */
	private static PetriNet convert(YAWLModel model,
			YawlToPetriNetSettings settings, HashMap map,
			ArrayList nonReducableNodes) {
		/*
		 * Create the Petri net.
		 */
		PetriNet petriNet = new PetriNet();
		map.put(model, petriNet);
		/*
		 * Find the root decomposition.
		 */
		for (YAWLDecomposition decomposition : model.getDecompositions()) {
			if (decomposition.isRoot()) {
				/*
				 * First, convert the conditions to places.
				 */
				for (YAWLNode node : decomposition.getNodes()) {
					if (node instanceof YAWLCondition) {
						YAWLCondition condition = (YAWLCondition) node;
						Place place = new Place(COND_STRING
								+ condition.getIdentifier(), petriNet);
						petriNet.addAndLinkPlace(place);
						map.put(condition, place);
						nonReducableNodes.add(place);
					}
				}
				/*
				 * Second, convert the transitions to triplets (join
				 * transitions, place, split transitions). Assume that the
				 * predecessors and successor of the tasks have already been
				 * converted.
				 */
				for (YAWLNode node : decomposition.getNodes()) {
					if (node instanceof YAWLTask) {
						YAWLTask task = (YAWLTask) node;
						Place joinPlace, splitPlace;
						Transition taskTransition;
						/*
						 * First, create the task structure.
						 */
						if (settings
								.get(YawlToPetriNetSettings.CREATE_TASK_TRANSITION)) {
							joinPlace = new Place(PRE_STRING
									+ task.getIdentifier(), petriNet);
							petriNet.addAndLinkPlace(joinPlace);
							splitPlace = new Place(POST_STRING
									+ task.getIdentifier(), petriNet);
							petriNet.addAndLinkPlace(splitPlace);
							taskTransition = new Transition(TASK_STRING
									+ task.getIdentifier(), petriNet);
							if (settings
									.get(YawlToPetriNetSettings.CREATE_LOG_EVENTS)) {
								taskTransition.setLogEvent(new LogEvent(task
										.getIdentifier(), COMPLETE_STRING));
							}
							petriNet.addAndLinkTransition(taskTransition);
							PNEdge joinEdge = new PNEdge(joinPlace,
									taskTransition);
							petriNet.addAndLinkEdge(joinEdge, joinPlace,
									taskTransition);
							PNEdge splitEdge = new PNEdge(taskTransition,
									splitPlace);
							petriNet.addAndLinkEdge(splitEdge, taskTransition,
									splitPlace);
							/*
							 * The task transition should not be reduced.
							 */
							nonReducableNodes.add(taskTransition);
							/*
							 * The split place should not be reduced, as a busy
							 * YAWL task will be mapped onto a token in this
							 * place.
							 */
							// nonReducableNodes.add(splitPlace);
						} else {
							joinPlace = new Place(PRE_STRING
									+ task.getIdentifier(), petriNet);
							petriNet.addAndLinkPlace(joinPlace);
							splitPlace = joinPlace;
							taskTransition = null;
							/*
							 * The split place should not be reduced, as a busy
							 * YAWL task will be mapped onto a token in this
							 * place.
							 */
							// nonReducableNodes.add(splitPlace);
						}
						/*
						 * Second, create all join transitions.
						 */
						HashSet<Transition> joinTransitions = new HashSet<Transition>();
						switch (task.getJoinType()) {
						case YAWLTask.NONE:
						case YAWLTask.AND: {
							convertAndJoin(task, decomposition, petriNet,
									joinPlace, joinTransitions, map);
							break;
						}
						case YAWLTask.XOR: {
							convertXorJoin(task, decomposition, petriNet,
									joinPlace, joinTransitions, map);
							break;
						}
						case YAWLTask.OR: {
							convertOrJoin(task, decomposition, petriNet,
									joinPlace, joinTransitions, map);
							break;
						}
						}
						/*
						 * Third, create all split transitions.
						 */
						HashSet<Transition> splitTransitions = new HashSet<Transition>();
						switch (task.getSplitType()) {
						case YAWLTask.NONE:
						case YAWLTask.AND: {
							convertAndSplit(task, decomposition, petriNet,
									splitPlace, splitTransitions, map);
							break;
						}
						case YAWLTask.XOR: {
							convertXorSplit(task, decomposition, petriNet,
									splitPlace, splitTransitions, map,
									nonReducableNodes);
							break;
						}
						case YAWLTask.OR: {
							convertOrSplit(task, decomposition, petriNet,
									splitPlace, splitTransitions, map,
									nonReducableNodes);
							break;
						}
						}
						/*
						 * Update the mapping.
						 */
						ArrayList list = new ArrayList(3);
						int index = 0;
						list.add(index++, joinTransitions);
						list.add(index++, joinPlace);
						if (taskTransition != null) {
							list.add(index++, taskTransition);
							list.add(index++, splitPlace);
						}
						list.add(index++, splitTransitions);
						map.put(task, list);
						if (settings
								.get(YawlToPetriNetSettings.CREATE_CLUSTERS)) {
							/*
							 * Add a cluster for this task. Improves
							 * recognition.
							 */
							TransitionCluster cluster = new TransitionCluster(
									task.getIdentifier());
							for (Transition transition : joinTransitions) {
								cluster.addTransition(transition);
							}
							if (taskTransition != null) {
								cluster.addTransition(taskTransition);
							}
							for (Transition transition : splitTransitions) {
								cluster.addTransition(transition);
							}
							petriNet.addCluster(cluster);
						}
					}
				}
			}
		}
		return petriNet;
	}

	/**
	 * Convert the AND-join part of a YAWL task.
	 * 
	 * @param task
	 *            YAWLTask The task with the AND-join.
	 * @param decomposition
	 *            YAWLDecomposition The decomposition the task belongs to.
	 * @param petriNet
	 *            PetriNet The resulting Petri Net.
	 * @param taskPlace
	 *            Place The 'busy' place for the YAWL task.
	 * @param joinTransitions
	 *            HashSet The set of join transitions created so far.
	 * @param map
	 *            HashMap Map with YAWL-PN relations.
	 */
	private static void convertAndJoin(YAWLTask task,
			YAWLDecomposition decomposition, PetriNet petriNet,
			Place taskPlace, HashSet<Transition> joinTransitions, HashMap map) {
		/*
		 * First, create the join transition.
		 */
		String inputs = "";
		for (int i = 0; i < getNormalPredecessors(decomposition, task).size(); i++) {
			inputs += "1";
		}
		Transition joinTransition = new Transition(JOIN_STRING + inputs + "_"
				+ task.getIdentifier(), petriNet);
		petriNet.addAndLinkTransition(joinTransition);
		joinTransitions.add(joinTransition);
		/*
		 * Second, create the edge to the 'busy' place.
		 */
		PNEdge pnEdge = new PNEdge(joinTransition, taskPlace);
		petriNet.addAndLinkEdge(pnEdge, joinTransition, taskPlace);
		/*
		 * Third, copy the normal YAWL edges to Petri net edges.
		 */
		for (Object object : getNormalPredecessors(decomposition, task)) {
			if (object instanceof YAWLCondition) {
				YAWLCondition condition = (YAWLCondition) object;
				Place place = (Place) map.get(condition);
				pnEdge = new PNEdge(place, joinTransition);
				petriNet.addAndLinkEdge(pnEdge, place, joinTransition);
			}
		}
	}

	/**
	 * Convert the XOR-join part of a YAWL task.
	 * 
	 * @param task
	 *            YAWLTask The task with the XOR-join.
	 * @param decomposition
	 *            YAWLDecomposition The decomposition the task belongs to.
	 * @param petriNet
	 *            PetriNet The resulting Petri Net.
	 * @param taskPlace
	 *            Place The 'busy' place for the YAWL task.
	 * @param joinTransitions
	 *            HashSet The set of join transitions created so far.
	 * @param map
	 *            HashMap Map with YAWL-PN relations.
	 */
	private static void convertXorJoin(YAWLTask task,
			YAWLDecomposition decomposition, PetriNet petriNet,
			Place taskPlace, HashSet<Transition> joinTransitions, HashMap map) {
		/*
		 * Create a separate transition for every normal YAWL edge.
		 */
		int rank = 0;
		for (Object object : getNormalPredecessors(decomposition, task)) {
			if (object instanceof YAWLCondition) {
				YAWLCondition condition = (YAWLCondition) object;
				Place place = (Place) map.get(condition);
				/*
				 * Create transition for this edge.
				 */
				String inputs = "";
				for (int i = 0; i < rank; i++) {
					inputs += "0";
				}
				inputs += "1";
				for (int i = rank + 1; i < getNormalPredecessors(decomposition,
						task).size(); i++) {
					inputs += "0";
				}
				Transition joinTransition = new Transition(JOIN_STRING + inputs
						+ "_" + task.getIdentifier(), petriNet);
				petriNet.addAndLinkTransition(joinTransition);
				joinTransitions.add(joinTransition);
				/*
				 * And create both edges.
				 */
				PNEdge pnEdge = new PNEdge(place, joinTransition);
				petriNet.addAndLinkEdge(pnEdge, place, joinTransition);
				pnEdge = new PNEdge(joinTransition, taskPlace);
				petriNet.addAndLinkEdge(pnEdge, joinTransition, taskPlace);
				rank++;
			}
		}
	}

	/**
	 * Convert the OR-join part of a YAWL task.
	 * 
	 * @param task
	 *            YAWLTask The task with the OR-join.
	 * @param decomposition
	 *            YAWLDecomposition The decomposition the task belongs to.
	 * @param petriNet
	 *            PetriNet The resulting Petri Net.
	 * @param taskPlace
	 *            Place The 'busy' place for the YAWL task.
	 * @param joinTransitions
	 *            HashSet The set of join transitions created so far.
	 * @param map
	 *            HashMap Map with YAWL-PN relations.
	 */
	private static void convertOrJoin(YAWLTask task,
			YAWLDecomposition decomposition, PetriNet petriNet,
			Place taskPlace, HashSet<Transition> joinTransitions, HashMap map) {
		convertOrJoin(task, petriNet, taskPlace, joinTransitions, map,
				getNormalPredecessors(decomposition, task),
				new HashSet<YAWLCondition>(), "");
	}

	/**
	 * Partially convert the OR-join part of a YAWL task.
	 * 
	 * @param task
	 *            YAWLTask The task with the OR-join.
	 * @param petriNet
	 *            PetriNet The resulting Petri Net.
	 * @param taskPlace
	 *            Place The 'busy' place for the YAWL task.
	 * @param joinTransitions
	 *            HashSet The set of join transitions created so far.
	 * @param map
	 *            HashMap Map with YAWL-PN relations.
	 * @param predecessors
	 *            Collection Predecessors of this task still to choose from.
	 * @param conditions
	 *            HashSet Chosen predecessors for the current branch.
	 */
	private static void convertOrJoin(YAWLTask task, PetriNet petriNet,
			Place taskPlace, HashSet<Transition> joinTransitions, HashMap map,
			Collection predecessors, HashSet<YAWLCondition> conditions,
			String inputs) {
		if (predecessors.isEmpty()) {
			/*
			 * All choices made in this branch. Create a transition for the
			 * chosen predecessors, provided that any predecessor has been
			 * chosen.
			 */
			if (!conditions.isEmpty()) {
				/*
				 * First, create the transition.
				 */
				Transition joinTransition = new Transition(JOIN_STRING + inputs
						+ "_" + task.getIdentifier(), petriNet);
				petriNet.addAndLinkTransition(joinTransition);
				joinTransitions.add(joinTransition);
				/*
				 * Second, add the edge to the 'busy' place.
				 */
				PNEdge pnEdge = new PNEdge(joinTransition, taskPlace);
				petriNet.addAndLinkEdge(pnEdge, joinTransition, taskPlace);
				/*
				 * Third, add edges for all chosen conditions.
				 */
				for (YAWLCondition condition : conditions) {
					Place place = (Place) map.get(condition);
					pnEdge = new PNEdge(place, joinTransition);
					petriNet.addAndLinkEdge(pnEdge, place, joinTransition);
				}
			}
		} else {
			/*
			 * Select the next predecessor to choose for.
			 */
			YAWLCondition condition = (YAWLCondition) predecessors.iterator()
					.next();
			predecessors.remove(condition);
			/*
			 * First, start a branch where this predecessor has not been chosen.
			 */
			convertOrJoin(task, petriNet, taskPlace, joinTransitions, map,
					predecessors, conditions, inputs + "0");
			/*
			 * Second, start a branch where this predecessor has been chosen.
			 */
			conditions.add(condition);
			convertOrJoin(task, petriNet, taskPlace, joinTransitions, map,
					predecessors, conditions, inputs + "1");
			conditions.remove(condition);
			/*
			 * Clean up.
			 */
			predecessors.add(condition);
		}
	}

	/**
	 * See convertAndJoin.
	 * 
	 * @param task
	 *            YAWLTask
	 * @param decomposition
	 *            YAWLDecomposition
	 * @param petriNet
	 *            PetriNet
	 * @param taskPlace
	 *            Place
	 * @param splitTransitions
	 *            HashSet
	 * @param map
	 *            HashMap
	 */
	private static void convertAndSplit(YAWLTask task,
			YAWLDecomposition decomposition, PetriNet petriNet,
			Place taskPlace, HashSet<Transition> splitTransitions, HashMap map) {
		String outputs = "";
		for (int i = 0; i < getNormalSuccessors(decomposition, task).size(); i++) {
			outputs += "1";
		}
		Transition splitTransition = new Transition(SPLIT_STRING + outputs
				+ "_" + task.getIdentifier(), petriNet);
		petriNet.addAndLinkTransition(splitTransition);
		splitTransitions.add(splitTransition);
		PNEdge pnEdge = new PNEdge(taskPlace, splitTransition);
		petriNet.addAndLinkEdge(pnEdge, taskPlace, splitTransition);
		for (Object object : getNormalSuccessors(decomposition, task)) {
			if (object instanceof YAWLCondition) {
				YAWLCondition condition = (YAWLCondition) object;
				Place place = (Place) map.get(condition);
				pnEdge = new PNEdge(splitTransition, place);
				petriNet.addAndLinkEdge(pnEdge, splitTransition, place);
			}
		}
	}

	/**
	 * See convertXorJoin.
	 * 
	 * @param task
	 *            YAWLTask
	 * @param decomposition
	 *            YAWLDecomposition
	 * @param petriNet
	 *            PetriNet
	 * @param taskPlace
	 *            Place
	 * @param splitTransitions
	 *            HashSet
	 * @param map
	 *            HashMap
	 */
	private static void convertXorSplit(YAWLTask task,
			YAWLDecomposition decomposition, PetriNet petriNet,
			Place taskPlace, HashSet<Transition> splitTransitions, HashMap map,
			ArrayList nonReducableNodes) {
		int rank = 0;
		ArrayList nonReducableTemp = new ArrayList();
		for (Object object : getNormalSuccessors(decomposition, task)) {
			if (object instanceof YAWLCondition) {
				YAWLCondition condition = (YAWLCondition) object;
				Place place = (Place) map.get(condition);
				String outputs = "";
				for (int i = 0; i < rank; i++) {
					outputs += "0";
				}
				outputs += "1";
				for (int i = rank + 1; i < getNormalSuccessors(decomposition,
						task).size(); i++) {
					outputs += "0";
				}
				Transition splitTransition = new Transition(SPLIT_STRING
						+ outputs + "_" + task.getIdentifier(), petriNet);
				petriNet.addAndLinkTransition(splitTransition);
				splitTransitions.add(splitTransition);
				PNEdge pnEdge = new PNEdge(splitTransition, place);
				petriNet.addAndLinkEdge(pnEdge, splitTransition, place);
				pnEdge = new PNEdge(taskPlace, splitTransition);
				petriNet.addAndLinkEdge(pnEdge, taskPlace, splitTransition);
				nonReducableTemp.add(splitTransition);
				rank++;
			}
		}
		if (nonReducableTemp.size() > 1) {
			/*
			 * An explicit choice. Make sure this moment of choice does not get
			 * lost in the reduction.
			 */
			nonReducableNodes.addAll(nonReducableTemp);
		}
	}

	/**
	 * See convertOrJoin.
	 * 
	 * @param task
	 *            YAWLTask
	 * @param decomposition
	 *            YAWLDecomposition
	 * @param petriNet
	 *            PetriNet
	 * @param taskPlace
	 *            Place
	 * @param splitTransitions
	 *            HashSet
	 * @param map
	 *            HashMap
	 */
	private static void convertOrSplit(YAWLTask task,
			YAWLDecomposition decomposition, PetriNet petriNet,
			Place taskPlace, HashSet<Transition> splitTransitions, HashMap map,
			ArrayList nonReducableNodes) {
		ArrayList nonReducableTemp = new ArrayList();
		convertOrSplit(task, petriNet, taskPlace, splitTransitions, map,
				getNormalSuccessors(decomposition, task),
				new HashSet<YAWLCondition>(), nonReducableTemp, "");
		if (nonReducableTemp.size() > 1) {
			/*
			 * An explicit choice. Make sure this moment of choice does not get
			 * lost in the reduction.
			 */
			nonReducableNodes.addAll(nonReducableTemp);
		}
	}

	/**
	 * See convertOrJoin.
	 * 
	 * @param task
	 *            YAWLTask
	 * @param petriNet
	 *            PetriNet
	 * @param taskPlace
	 *            Place
	 * @param splitTransitions
	 *            HashSet
	 * @param map
	 *            HashMap
	 * @param successors
	 *            Collection
	 * @param conditions
	 *            HashSet
	 */
	private static void convertOrSplit(YAWLTask task, PetriNet petriNet,
			Place taskPlace, HashSet<Transition> splitTransitions, HashMap map,
			Collection successors, HashSet<YAWLCondition> conditions,
			ArrayList nonReducableNodes, String outputs) {
		if (successors.isEmpty()) {
			if (!conditions.isEmpty()) {
				Transition splitTransition = new Transition(SPLIT_STRING
						+ outputs + "_" + task.getIdentifier(), petriNet);
				petriNet.addAndLinkTransition(splitTransition);
				splitTransitions.add(splitTransition);
				PNEdge pnEdge = new PNEdge(taskPlace, splitTransition);
				petriNet.addAndLinkEdge(pnEdge, taskPlace, splitTransition);
				for (YAWLCondition condition : conditions) {
					Place place = (Place) map.get(condition);
					pnEdge = new PNEdge(splitTransition, place);
					petriNet.addAndLinkEdge(pnEdge, splitTransition, place);
				}
				nonReducableNodes.add(splitTransition);
			}
		} else {
			YAWLCondition condition = (YAWLCondition) successors.iterator()
					.next();
			successors.remove(condition);
			convertOrSplit(task, petriNet, taskPlace, splitTransitions, map,
					successors, conditions, nonReducableNodes, outputs + "0");
			conditions.add(condition);
			convertOrSplit(task, petriNet, taskPlace, splitTransitions, map,
					successors, conditions, nonReducableNodes, outputs + "1");
			conditions.remove(condition);
			successors.add(condition);
		}
	}

	/**
	 * Get all nodes that are predecessor through normal edges for the given
	 * node.
	 * 
	 * @param decomposition
	 *            YAWLDecomposition The decomposition containing the given node.
	 * @param node
	 *            YAWLNode The given node.
	 * @return HashSet
	 */
	private static HashSet<YAWLNode> getNormalPredecessors(
			YAWLDecomposition decomposition, YAWLNode node) {
		HashSet<YAWLNode> nodes = new HashSet<YAWLNode>();
		for (Object object : decomposition.getEdges()) {
			if (object instanceof YAWLEdge) {
				YAWLEdge edge = (YAWLEdge) object;
				if (edge.isNormal()) {
					YAWLNode fromNode = (YAWLNode) edge.getSource();
					YAWLNode toNode = (YAWLNode) edge.getDest();
					if (toNode == node) {
						nodes.add(fromNode);
					}
				}
			}
		}
		return nodes;
	}

	/**
	 * Get all nodes that are successor through normal edges for the given node.
	 * 
	 * @param decomposition
	 *            YAWLDecomposition The decomposition containing the given node.
	 * @param node
	 *            YAWLNode The given node.
	 * @return HashSet
	 */
	private static HashSet<YAWLNode> getNormalSuccessors(
			YAWLDecomposition decomposition, YAWLNode node) {
		HashSet<YAWLNode> nodes = new HashSet<YAWLNode>();
		for (Object object : decomposition.getEdges()) {
			if (object instanceof YAWLEdge) {
				YAWLEdge edge = (YAWLEdge) object;
				if (edge.isNormal()) {
					YAWLNode fromNode = (YAWLNode) edge.getSource();
					YAWLNode toNode = (YAWLNode) edge.getDest();
					if (fromNode == node) {
						nodes.add(toNode);
					}
				}
			}
		}
		return nodes;
	}
}
