package org.processmining.converting.protos;

/*
 * Author: Mariska Netjes
 * (c) 2008 Technische Universiteit Eindhoven and STW
 */

import org.processmining.framework.models.petrinet.*;
import org.processmining.framework.models.protos.*;
import java.util.HashMap;
import java.util.HashSet;
import org.processmining.converting.Converter;
import org.processmining.framework.log.LogEvent;
import org.processmining.mining.petrinetmining.PetriNetResult;
import org.processmining.framework.ui.Message;
import org.processmining.framework.models.yawl.YAWLTask;
import org.processmining.framework.models.ModelGraphVertex;

public class ProtosToPetriNet {
	/*
	 * Prefixes to be used for transitions added for XOR-split and XOR-join
	 * translation
	 */
	private static final String JOIN_STRING = "JOIN_";
	private static final String SPLIT_STRING = "SPLIT_";

	public ProtosToPetriNet() {
	}

	@Converter(name = "Protos: Convert to Petri net", help = "http://prom.win.tue.nl/research/wiki/online/protos2pn")
	public static PetriNetResult convert(ProtosModel model) {
		HashMap map0 = new HashMap();
		HashMap map1 = new HashMap();
		ProtosModel model1 = ProtosToProtos.addImplicitConditions(model, map0);
		PetriNet petriNet = convert(model1, map1);
		PetriNetResult result = new PetriNetResult(null, petriNet);
		return result;
	}

	/**
	 * Convert the given Protos model to a Petri net. Precondition: All implicit
	 * statusses have been added to the Protos model.
	 * 
	 * @param model
	 *            Protos The given Protos model.
	 * @param map
	 *            HashMap The map to store the Protos-PN relations in. Protos
	 *            statusses are mapped onto a place. Protos Activities are
	 *            mapped onto transitions. Protos activities with XOR split are
	 *            split up to include the choice behavior in the Petri net.
	 * @return PetriNet
	 */
	public static PetriNet convert(ProtosModel model, HashMap map) {

		/*
		 * Create the Petri net.
		 */
		PetriNet petriNet = new PetriNet();
		map.put(model, petriNet);
		/*
		 * Find the root sub process.
		 */
		for (ProtosSubprocess process : model.getSubprocesses()) {
			if (process.isRoot()) {
				/*
				 * First, convert the statusses to places.
				 */
				for (ProtosFlowElement node : process.getStatuses()) {
					ProtosFlowElement status = (ProtosFlowElement) node;
					Place place = new Place(status.getName(), petriNet);
					petriNet.addAndLinkPlace(place);
					map.put(status, place);
				}

				/*
				 * Second, convert the activities to transitions.
				 */
				for (ProtosFlowElement act : process.getActivities()) {
					Transition task = new Transition(act.getName(), petriNet);
					task.setLogEvent(new LogEvent(task.getIdentifier(),
							"complete"));
					petriNet.addAndLinkTransition(task);
					map.put(act, task);
				}

				/*
				 * Third, convert the arcs to edges for the connections from and
				 * to activities with AND semantics.
				 */
				for (ProtosProcessArc arc : process.getArcs()) {
					ProtosFlowElement fromNode = (ProtosFlowElement) process
							.getFlowElement(arc.getSource());
					ProtosFlowElement toNode = (ProtosFlowElement) process
							.getFlowElement(arc.getTarget());

					if (fromNode.isStatus() && toNode.isActivity()
							&& toNode.getJoinType() == YAWLTask.AND) {
						Place pointOfExit = (Place) map.get(fromNode);
						Transition pointOfEntry = (Transition) map.get(toNode);
						PNEdge pnEdge = new PNEdge(pointOfExit, pointOfEntry);
						petriNet.addAndLinkEdge(pnEdge, pointOfExit,
								pointOfEntry);
						map.put(arc, pnEdge);
					}

					if (fromNode.isActivity() && toNode.isStatus()
							&& fromNode.getSplitType() == YAWLTask.AND) {
						Transition pointOfExit = (Transition) map.get(fromNode);
						Place pointOfEntry = (Place) map.get(toNode);
						PNEdge pnEdge = new PNEdge(pointOfExit, pointOfEntry);
						petriNet.addAndLinkEdge(pnEdge, pointOfExit,
								pointOfEntry);
						map.put(arc, pnEdge);
					}
				}

				/*
				 * For activities with an XOR-split and/or XOR-join semantics
				 * additional transitions and connections are added to include
				 * the XOR behavior.
				 */
				for (ProtosFlowElement act : process.getActivities()) {
					if (act.getJoinType() == YAWLTask.XOR) {
						// First, create a new place where the actual join is
						// made in the petrinet.
						Place joinPlace = new Place(JOIN_STRING + "_"
								+ act.getIdentifier(), petriNet);
						petriNet.addAndLinkPlace(joinPlace);
						// connect task corresponding to node with XOR-join to
						// the join place.
						Transition trans = (Transition) map.get(act);
						PNEdge pnEdge = new PNEdge(joinPlace, trans);
						petriNet.addAndLinkEdge(pnEdge, joinPlace, trans);

						convertXORjoin(act, process, joinPlace, petriNet, map);
					}
					if (act.getSplitType() == YAWLTask.XOR) {
						// First, create a new place where the actual split is
						// made in the petrinet.
						Place splitPlace = new Place(SPLIT_STRING + "_"
								+ act.getIdentifier(), petriNet);
						petriNet.addAndLinkPlace(splitPlace);
						// connect task corresponding to node with XOR-split to
						// the split place.
						Transition trans = (Transition) map.get(act);
						PNEdge pnEdge = new PNEdge(trans, splitPlace);
						petriNet.addAndLinkEdge(pnEdge, trans, splitPlace);

						convertXORsplit(act, process, splitPlace, petriNet, map);

						// map.put(act,splitPlace); //removed
					}

					/*
					 * Activities with OR semantics can not be dealt with.
					 */
					if (act.getSplitType() == YAWLTask.OR
							|| act.getJoinType() == YAWLTask.OR) {
						Message
								.add(
										"The Protos activity with ID: "
												+ act.getID().toString()
												+ "has OR semantics which is not supported by this converter.",
										Message.ERROR);
					}
				}
			}
		}

		/*
		 * Finally, add a place to the start and end transitions if the process
		 * starts / ends with transition(s).
		 */
		for (ModelGraphVertex startVertex : petriNet.getStartNodes()) {
			PNNode startNode = (PNNode) startVertex;
			if (petriNet.findTransition(startNode.getIdentifier()) != null) {
				Place startPlace = new Place("START_"
						+ startNode.getIdentifier(), petriNet);
				petriNet.addAndLinkPlace(startPlace);
				// connect start place to the start task.
				Transition trans = (Transition) startNode;
				PNEdge pnEdge = new PNEdge(startPlace, trans);
				petriNet.addAndLinkEdge(pnEdge, startPlace, trans);
			}
		}
		for (ModelGraphVertex endVertex : petriNet.getEndNodes()) {
			PNNode endNode = (PNNode) endVertex;
			if (petriNet.findTransition(endNode.getIdentifier()) != null) {
				Place endPlace = new Place("END_" + endNode.getIdentifier(),
						petriNet);
				petriNet.addAndLinkPlace(endPlace);
				// connect start place to the start task.
				Transition trans = (Transition) endNode;
				PNEdge pnEdge = new PNEdge(trans, endPlace);
				petriNet.addAndLinkEdge(pnEdge, trans, endPlace);
			}
		}

		int size, nofTransitions, nofPlaces, nofEdges;
		size = map.size();
		nofTransitions = petriNet.getTransitions().size();
		nofPlaces = petriNet.getPlaces().size();
		nofEdges = petriNet.getEdges().size();

		Message.add("<ProtosToPetriNet nofTransitions=\"" + nofTransitions
				+ "\" nofPlaces=\"" + nofPlaces + "\" nofEdges=\"" + nofEdges
				+ "\" sizeMap=\"" + size + "\"/>", Message.TEST);

		// We're done.

		return petriNet;
	}

	/*
	 * Convert the XOR-split part of a Protos activity.
	 * 
	 * @param node FlowElement
	 * 
	 * @param process ProtosSubprocess
	 * 
	 * @param map HashMap
	 */
	private static void convertXORsplit(ProtosFlowElement act,
			ProtosSubprocess process, Place splitPlace, PetriNet petriNet,
			HashMap map) {
		int rank = 1;
		// Create a new transition for each outgoing branch and connect it.
		for (Object object : getNormalSuccessors(process, act)) {
			ProtosFlowElement status = (ProtosFlowElement) object;
			// status and place have already been mapped.
			Place place = (Place) map.get(status);
			// create new transition.
			Transition splitTransition = new Transition(SPLIT_STRING + rank
					+ "_" + act.getIdentifier(), petriNet);
			petriNet.addAndLinkTransition(splitTransition);
			// create edge from new split place to new transition.
			PNEdge splitEdge = new PNEdge(splitPlace, splitTransition);
			petriNet.addAndLinkEdge(splitEdge, splitPlace, splitTransition);
			// create edge from new transition to outgoing place.
			PNEdge newEdge = new PNEdge(splitTransition, place);
			petriNet.addAndLinkEdge(newEdge, splitTransition, place);
			rank++;
		}
	}

	/*
	 * Convert the XOR-join part of a Protos activity.
	 * 
	 * @param node FlowElement
	 * 
	 * @param process ProtosSubprocess
	 * 
	 * @param map HashMap
	 */
	private static void convertXORjoin(ProtosFlowElement node,
			ProtosSubprocess process, Place joinPlace, PetriNet petriNet,
			HashMap map) {
		int rank = 1;
		// Create a new transition for each ingoing branch and connect it.
		for (Object object : getNormalPredecessors(process, node)) {
			ProtosFlowElement status = (ProtosFlowElement) object;
			if (status.isStatus()) {
				// status and place have already been mapped.
				Place place = (Place) map.get(status);
				// create new transition.
				Transition joinTransition = new Transition(JOIN_STRING + rank
						+ "_" + node.getIdentifier(), petriNet);
				petriNet.addAndLinkTransition(joinTransition);
				// create edge from ingoing place to new transition.
				PNEdge newEdge = new PNEdge(place, joinTransition);
				petriNet.addAndLinkEdge(newEdge, place, joinTransition);
				// create edge from new transition to new join place.
				PNEdge joinEdge = new PNEdge(joinTransition, joinPlace);
				petriNet.addAndLinkEdge(joinEdge, joinTransition, joinPlace);
				rank++;
			}
		}
	}

	/**
	 * Get all nodes that are successor through normal edges for the given node.
	 * 
	 * @param process
	 *            ProtosSubprocess The subprocess containing the given node.
	 * @param node
	 *            ProtosFlowElement The given node.
	 * @return HashSet
	 */
	private static HashSet<ProtosFlowElement> getNormalSuccessors(
			ProtosSubprocess process, ProtosFlowElement node) {
		HashSet<ProtosFlowElement> nodes = new HashSet<ProtosFlowElement>();
		for (Object object : process.getArcs()) {
			if (object instanceof ProtosProcessArc) {
				ProtosProcessArc edge = (ProtosProcessArc) object;
				ProtosFlowElement fromNode = (ProtosFlowElement) edge.getEdge()
						.getSource();
				ProtosFlowElement toNode = (ProtosFlowElement) edge.getEdge()
						.getDest();
				if (fromNode == node) {
					nodes.add(toNode);
				}
			}
		}
		return nodes;
	}

	/**
	 * Get all nodes that are predecessor through normal edges for the given
	 * node.
	 * 
	 * @param process
	 *            ProtosSubprocess The subprocess containing the given node.
	 * @param node
	 *            ProtosFlowElement The given node.
	 * @return HashSet
	 */
	private static HashSet<ProtosFlowElement> getNormalPredecessors(
			ProtosSubprocess process, ProtosFlowElement node) {
		HashSet<ProtosFlowElement> nodes = new HashSet<ProtosFlowElement>();
		for (Object object : process.getArcs()) {
			if (object instanceof ProtosProcessArc) {
				ProtosProcessArc edge = (ProtosProcessArc) object;
				ProtosFlowElement fromNode = (ProtosFlowElement) edge.getEdge()
						.getSource();
				ProtosFlowElement toNode = (ProtosFlowElement) edge.getEdge()
						.getDest();
				if (toNode == node) {
					nodes.add(fromNode);
				}
			}
		}
		return nodes;
	}

}
