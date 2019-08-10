package org.processmining.analysis.redesign;

import java.util.HashSet;
import org.processmining.framework.models.hlprocess.hlmodel.*;
import org.processmining.framework.models.hlprocess.pattern.*;
import org.processmining.framework.models.hlprocess.*;
import org.processmining.framework.models.petrinet.*;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.analysis.redesign.util.*;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.log.LogEvent;
import att.grappa.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import att.grappa.Edge;
import java.util.Iterator;

import org.processmining.framework.models.hlprocess.att.HLNominalAttribute;
import org.processmining.framework.models.hlprocess.expr.HLDataExpression;
import org.processmining.framework.ui.Message;

/**
 * Defines the transformation of a component into an alternative process part.
 * 
 * @author mnetjes
 */

public class Transformation {

	protected HLPetriNet model;
	protected HLPetriNet subNet = null;
	/**
	 * the component consists of places and transitions.
	 */
	protected Component comp;

	/**
	 * A transformation is performed for an input model. In this input model a
	 * number of nodes are selected. From this selection a component is derived.
	 * The subnet is the projection of the component on the input model.
	 * 
	 * @param aModel
	 *            HLPetriNet the input model which is being redesigned
	 * @param aNodes
	 *            Collection<PNNode> the nodes selected in the input model
	 */
	public Transformation(HLPetriNet aModel, Component aComp) {
		model = aModel;
		comp = aComp;
		subNet = comp.getProjection();
	}

	/**
	 * As a starting point for the parallel and sequence redesign a graph with
	 * only vertices, i.e., no edges are between them, is used. The graph is
	 * created with the transitions selected in the input model.
	 * 
	 * @return graph ModelGraph the resulting graph with only vertices
	 */
	public ModelGraph createInitialGraphNodes() {
		/**
		 * Create an empty graph and add the transitions as vertices. Only
		 * transitions that produce output data are included.
		 */
		ModelGraph graph = new ModelGraph("myGraph");
		for (Transition node : subNet.getPNModel().getTransitions()) {
			HLActivity hlAct = subNet.findActivity(node);
			if (!hlAct.getOutputDataAttributes().isEmpty()) {
				ModelGraphVertex vertex = new ModelGraphVertex(graph);
				vertex.setIdentifier(node.getIdentifier());
				graph.addVertex(vertex);
			}
		}
		return graph;
	}

	/**
	 * A step in the parallel redesign is the creation of a parallel graph.
	 * Relations are added between the vertices in the initial graph based on
	 * the data dependencies.
	 * 
	 * @param graph
	 *            ModelGraph the input graph without edges
	 * @return graph ModelGraph the resulting parallel graph
	 */
	public ModelGraph createParallelGraph(ModelGraph graph) {
		/**
		 * Add arcs between nodes that have a dependency. The result is that
		 * nodes without dependencies are placed in parallel.
		 */
		if (graph.getVerticeList().size() > 1) {
			for (ModelGraphVertex v1 : graph.getVerticeList()) {
				for (ModelGraphVertex v2 : graph.getVerticeList()) {
					HLActivity hlAct1 = subNet.getHLProcess()
							.findActivityByName(v1.getIdentifier());
					HLActivity hlAct2 = subNet.getHLProcess()
							.findActivityByName(v2.getIdentifier());
					List<HLID> dataIDs = hlAct1.getOutputDataAttributeIDs();
					dataIDs.retainAll(hlAct2.getInputDataAttributeIDs());
					if (dataIDs.size() > 0) {
						ModelGraphEdge edge = new ModelGraphEdge(v1, v2);
						graph.addEdge(edge);
					}
				}
			}
		}

		/**
		 * Add articifical start / end node, if the graph starts / ends with
		 * multiple nodes.
		 */
		addStartNode(graph);
		addEndNode(graph);

		/**
		 * Transitively reduces this graph, i.e. removes an edge if there is
		 * another path between two nodes. The result is unique for a-cyclic
		 * graphs See ModelGraph
		 */
		graph.reduceTransitively();

		return graph;
	}

	/**
	 * The parallel redesign is created by a number of methods. First, the
	 * initial graph with only vertices is created, from this a parallel graph
	 * is made. Then, a parallel petri net is created, followed by a parallel
	 * HLPetriNet. The HLPetriNet is incorporated in the input model, this
	 * creating an alternative model.
	 * 
	 * @return alt HLPetriNet the alternative model.
	 */
	public HLPetriNet createParallelRedesign() {
		ModelGraph graph = createInitialGraphNodes();
		ModelGraph parGraph = createParallelGraph(graph);
		PetriNet parNet = createPetriNet(parGraph);
		HLPetriNet alt = replace(model, comp.getNodeList(), parNet);
		return alt;
	}

	/**
	 * A step in the sequence redesign is the creation of a sequential graph.
	 * Relations are added between the vertices in the initial graph based on
	 * the topological order.
	 * 
	 * @param graph
	 *            ModelGraph the input graph without edges
	 * @return graph ModelGraph the resulting sequential graph
	 */
	public ModelGraph createSequentialGraph(ModelGraph graph) {
		ModelGraph seqGraph = createParallelGraph(graph);

		/**
		 * topological sort: a list that gives the topological order of the
		 * input graph is created.
		 * 
		 * @PetriNet - topologicalSort(PetriNet pn)
		 */
		List<Node> sortedNodes = new LinkedList<Node>();
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(seqGraph.getSource());
		while (!queue.isEmpty()) {
			Node node = queue.remove();
			sortedNodes.add(node);
			List<Edge> outgoingArcs = node.getOutEdges();
			if (outgoingArcs == null)
				outgoingArcs = new LinkedList<Edge>();
			for (Edge edge : outgoingArcs) {
				seqGraph.removeEdge((ModelGraphEdge) edge);
				Node opposite = edge.getOpposite(node);
				if (opposite.inDegree() == 0)
					queue.add(opposite);
			}
		}
		/**
		 * remove artificial start and end nodes if present.
		 */
		if (sortedNodes.get(0).toString().contains("addedStartNode")) {
			seqGraph.removeVertex((ModelGraphVertex) sortedNodes.get(0));
			sortedNodes.remove(0);
		}
		if (sortedNodes.get(sortedNodes.size() - 1).toString().contains(
				"addedEndNode")) {
			seqGraph.removeVertex((ModelGraphVertex) sortedNodes
					.get(sortedNodes.size() - 1));
			sortedNodes.remove(sortedNodes.size() - 1);
		}
		/**
		 * create a graph with its nodes in the order of the sorted list.
		 */
		int index = 0;
		for (index = 0; index < sortedNodes.size() - 1; index++) {
			Node n1 = sortedNodes.get(index);
			ModelGraphVertex v1 = (ModelGraphVertex) n1;
			Node n2 = sortedNodes.get(index + 1);
			ModelGraphVertex v2 = (ModelGraphVertex) n2;
			seqGraph.addEdge(v1, v2);
		}

		return seqGraph;
	}

	/**
	 * The sequence redesign is created by a number of methods. First, the
	 * initial graph with only vertices is created, from this a sequential graph
	 * is made. Then, a petri net is created, followed by a HLPetriNet. This
	 * HLPetriNet is incorporated in the input model, this creating an
	 * alternative model.
	 * 
	 * @return alt HLPetriNet the alternative model.
	 */
	public HLPetriNet createSequentialRedesign() {
		ModelGraph graph = createInitialGraphNodes();
		ModelGraph seqGraph = createSequentialGraph(graph);
		PetriNet seqNet = createPetriNet(seqGraph);
		HLPetriNet alt = replace(model, comp.getNodeList(), seqNet);
		return alt;
	}

	/**
	 * Creates a Petri net from a graph
	 * 
	 * @param graph
	 *            ModelGraph input graph that is translated to a Petri net
	 * @return process PetriNet the resulting Petri net
	 */
	public PetriNet createPetriNet(ModelGraph graph) {
		/**
		 * A place is added between each two nodes that are connected in the
		 * graph.
		 */
		PetriNet process = new PetriNet();
		for (ModelGraphVertex vertex : graph.getVerticeList()) {
			Transition t = new Transition(vertex.getIdentifier(), process);
			/**
			 * add log event; necessary to create visible transitions for
			 * PetriNet.
			 */
			if (!(t.getIdentifier().contains("addedStartNode") || t
					.getIdentifier().contains("addedEndNode"))) {
				LogEvent event = new LogEvent(t.getIdentifier(), null);
				t.setLogEvent(event);
			}
			process.addAndLinkTransition(t);
		}
		if (graph.getVerticeList().size() > 1) {
			for (ModelGraphVertex v1 : graph.getVerticeList()) {
				for (ModelGraphVertex v2 : graph.getVerticeList()) {
					if (graph.getEdgesBetween(v1, v2).size() > 0) {
						Transition act1 = process.findTransition(v1
								.getIdentifier());
						Transition act2 = process.findTransition(v2
								.getIdentifier());
						Place newPlace = new Place("place_"
								+ act1.getIdentifier() + "_"
								+ act2.getIdentifier(), process);
						process.addAndLinkPlace(newPlace);
						PNEdge edge1 = new PNEdge(act1, newPlace);
						PNEdge edge2 = new PNEdge(newPlace, act2);
						process.addAndLinkEdge(edge1, act1, newPlace);
						process.addAndLinkEdge(edge2, newPlace, act2);
					}
				}
			}
		}
		return process;
	}

	/**
	 * Creates a HLPetri net from a Petri net. It is assumed the Petri net is
	 * derived from a HLPetriNet, which contains at least the transitions in the
	 * Petri net
	 * 
	 * @param net
	 *            PetriNet the Petri net that is translated to a HLPetriNet
	 * @param hlNet
	 *            HLPetriNet the HLPetriNet which HL info is used in the
	 *            translation
	 * @return copyNet HLPetriNet the resulting HLPetriNet
	 */
	public HLPetriNet createHLPetriNet(PetriNet net, HLPetriNet hlNet) {
		/**
		 * First, create a new HLPetriNet with net as lower-level petrinet
		 */
		HLPetriNet copyNet = new HLPetriNet(net);
		/**
		 * Then, re-establish the HLActivities
		 */
		for (ModelGraphVertex oldVertex : hlNet.getActivityNodes()) {
			for (ModelGraphVertex newVertex : copyNet.getGraphNodes()) {
				if (oldVertex.getIdentifier().equalsIgnoreCase(
						newVertex.getIdentifier())) {
					HLActivity clonedAct = (HLActivity) hlNet.findActivity(
							oldVertex).clone();
					copyNet.setActivity(newVertex, clonedAct);
					break;
				}
			}
		}
		/**
		 * Then, re-establish the HLChoices and copy the HL info from the hlNet
		 * to the HLChoice in the copyNet.
		 */
		for (ModelGraphVertex oldVertex : hlNet.getChoiceNodes()) {
			HLChoice oldChoice = (HLChoice) hlNet.findChoice(oldVertex);
			for (ModelGraphVertex newVertex : copyNet.getChoiceNodes()) {
				if (newVertex.getIdentifier().equalsIgnoreCase(
						oldVertex.getIdentifier())) {
					HLChoice newChoice = copyNet.findChoice(newVertex);
					for (Edge newEdge : newVertex.getOutEdges()) {
						// Attempt to make this work when target has been
						// removed during redesign.
						// // for (Edge oldEdge : oldVertex.getOutEdges()) {
						// Transition newTrans = (Transition) newEdge.getHead();
						// HLActivity newTarget =
						// copyNet.findActivity(newTrans);
						// // Transition oldTrans = (Transition)
						// oldEdge.getHead();
						// HLActivity posTarget = hlNet.findActivity(newTrans);
						// if (oldVertex.getSuccessors().contains(newTrans)) {
						// //kan niet!!
						// // if (posTarget.getID().equals(newTarget.getID()) {
						// // set choice target
						// HLCondition newCondition =
						// newChoice.addChoiceTarget(newTarget.getID());
						// HLCondition oldCondition =
						// oldChoice.getCondition(oldTarget.getID());
						// HLDataExpression expression =
						// oldCondition.getExpression();
						// if (expression != null) {
						// newCondition.setExpression(expression);
						// }
						// int frequency = oldCondition.getFrequency();
						// newCondition.setFrequency(frequency);
						// double prob = oldCondition.getProbability();
						// newCondition.setProbability(prob);
						// }
						// // }

						for (Edge oldEdge : oldVertex.getOutEdges()) {
							/**
							 * set choice target
							 */
							Transition newTrans = (Transition) newEdge
									.getHead();
							HLActivity newTarget = copyNet
									.findActivity(newTrans);
							Transition oldTrans = (Transition) oldEdge
									.getHead();
							HLActivity oldTarget = hlNet.findActivity(oldTrans);
							if (oldTarget.getID().equals(newTarget.getID())) {
								HLCondition newCondition = newChoice
										.addChoiceTarget(newTarget.getID());
								try {
									/**
									 * old target may differ from new target,
									 * has to be improved.
									 */
									HLCondition oldCondition = oldChoice
											.getCondition(oldTarget.getID());
									HLDataExpression expression = oldCondition
											.getExpression();
									if (expression != null) {
										newCondition.setExpression(expression);
									}
									int frequency = oldCondition.getFrequency();
									newCondition.setFrequency(frequency);
									double prob = oldCondition.getProbability();
									newCondition.setProbability(prob);
								} catch (Exception ex) {
									Message
											.add(
													"Error during adaptation of transition targets. Target "
															+ newTrans
																	.getIdentifier()
															+ " is not the same as the target of the input model",
													Message.ERROR);
								}
							}
						}
					}
					break;
				}
			}
		}
		/**
		 * Finally, copy the hl information from hlNet to copyNet.
		 */
		/**
		 * copy global info
		 */
		HLGlobal copyGlobal = (HLGlobal) hlNet.getHLProcess().getGlobalInfo();
		copyNet.getHLProcess().getGlobalInfo().setName(copyGlobal.getName());
		copyNet.getHLProcess().getGlobalInfo().setCaseGenerationScheme(
				copyGlobal.getCaseGenerationScheme());
		copyNet.getHLProcess().getGlobalInfo().setTimeUnit(
				copyGlobal.getTimeUnit());
		/**
		 * make deep copy attribute objects
		 */
		for (HLAttribute att : hlNet.getHLProcess().getAttributes()) {
			HLAttribute clonedAtt = (HLAttribute) att.clone();
			copyNet.getHLProcess().addOrReplace(clonedAtt);
		}
		/**
		 * make deep copy resource objects
		 */
		for (HLResource res : hlNet.getHLProcess().getResources()) {
			HLResource clonedRes = (HLResource) res.clone();
			copyNet.getHLProcess().addOrReplace(clonedRes);
		}
		/**
		 * make deep copy of group objects
		 */
		// update nobody and anybody ID as activities will still point to nobody
		// and anybody IDs
		// from the source hlModel (cloning of IDs is not needed)
		copyNet.getHLProcess().removeGroupWithoutAffectingAssigmnets(
				copyNet.getHLProcess().getNobodyGroupID());
		copyNet.getHLProcess().setNobodyGroupID(
				hlNet.getHLProcess().getNobodyGroupID());
		copyNet.getHLProcess().removeGroupWithoutAffectingAssigmnets(
				copyNet.getHLProcess().getAnybodyGroupID());
		copyNet.getHLProcess().setAnybodyGroupID(
				hlNet.getHLProcess().getAnybodyGroupID());
		// now clone the real group objects
		Iterator<HLGroup> groups = hlNet.getHLProcess().getAllGroups()
				.iterator();
		while (groups.hasNext()) {
			HLGroup grp = groups.next();
			HLGroup clonedGrp = (HLGroup) grp.clone();
			copyNet.getHLProcess().addOrReplace(clonedGrp);
		}
		return copyNet;
	}

	/**
	 * Replaces the sub net formed by the selected nodes by an alternative
	 * HLPetriNet. It is assumed that both nets fulfill the requirements that
	 * are necessary to return a sound model.
	 * 
	 * @param model
	 *            HLPetriNet the original model of which a part is replaced
	 * @param nodes
	 *            the selected nodes in the original model that are replaced
	 * @param altPart
	 *            PetriNet the replacing part
	 * @return altModel HLPetriNet the resulting model
	 */
	public HLPetriNet replace(HLPetriNet model, List<PNNode> nodes,
			PetriNet altPart) {
		/**
		 * Construct the lower level Petri net that replaces the original model.
		 * In the replacement, the component is removed from the original model
		 * and replaced by the created alternative part. Start with the original
		 * model
		 */
		Object o = model.getPNModel().clone();
		PetriNet altNet = (PetriNet) o;

		/**
		 * remove the component nodes.
		 */
		for (PNNode node : altNet.getNodes()) {
			for (PNNode selNode : nodes) {
				if (node.getIdentifier().equalsIgnoreCase(
						selNode.getIdentifier())) {
					altNet.removeNode(node.getIdentifier());
					altNet.removeVertex(node);
				}
			}
		}

		/**
		 * add the transitions from the created alternative part.
		 */
		for (Transition trans : altPart.getTransitions()) {
			Object to = trans.clone();
			Transition t = (Transition) to;
			altNet.addAndLinkTransition(t);
		}

		/**
		 * add the places from the created alternative part.
		 */
		for (Place place : altPart.getPlaces()) {
			Place p = new Place(place.getIdentifier(), altNet);
			altNet.addAndLinkPlace(p);
		}

		/**
		 * Now, we are adding all relevant edges. First, note that the edges
		 * from the original model that should remain are still there. Then, add
		 * the edges from the alternative part.
		 */
		Iterator edges = altPart.getEdges().iterator();
		while (edges.hasNext()) {
			PNEdge edge = (PNEdge) edges.next();
			PNEdge clonedEdge = (PNEdge) edge.clone();
			/**
			 * if place is source
			 */
			if (edge.isPT()) {
				Place p = (Place) edge.getSource();
				// find respective place in this net (place names are assumed to
				// be unique)
				Place myPlace = (Place) altNet.findPlace(p.getIdentifier());
				Transition t = (Transition) edge.getDest();
				// find respective transition in this net
				Transition myTransition = (Transition) altNet.findTransition(t
						.getIdentifier());
				// establish cloned place/transition as new source/target
				altNet.addAndLinkEdge(clonedEdge, myPlace, myTransition);
			}
			/**
			 * if transition is source
			 */
			else {
				Place p = (Place) edge.getDest();
				/**
				 * find respective place in this net (place names are assumed to
				 * be unique)
				 */
				Place myPlace = (Place) altNet.findPlace(p.getIdentifier());
				Transition t = (Transition) edge.getSource();
				/**
				 * find respective transition in this net
				 */
				Transition myTransition = (Transition) altNet.findTransition(t
						.getIdentifier());
				/**
				 * establish cloned transition/place as new source/target
				 */
				altNet.addAndLinkEdge(clonedEdge, myTransition, myPlace);
			}
		}

		/**
		 * Finally, connect the alternative part to the surrounding model.
		 */
		/**
		 * First, connect the source of the alternative part with the preceding
		 * part of the altNet
		 */
		for (PNNode node : model.getPNModel().getNodes()) {
			/**
			 * find the source node of the component
			 */
			if (subNet.getPNModel().getSource().getIdentifier()
					.equalsIgnoreCase(node.getIdentifier())) {
				/**
				 * check if subNet starts with place. if so, add place before
				 * source of altNet. also add it to altPart, to give it a
				 * matching source
				 */
				if (node instanceof Place) {
					Transition firstTransition = (Transition) altPart
							.getSource();
					Place newPlace = new Place(subNet.getPNModel().getSource()
							.getIdentifier(), altNet);
					altNet.addAndLinkPlace(newPlace);
					altPart.addAndLinkPlace(newPlace);
					PNEdge edge = new PNEdge(newPlace, altNet
							.findTransition(firstTransition.getIdentifier()));
					altNet.addAndLinkEdge(edge, newPlace, altNet
							.findTransition(firstTransition.getIdentifier()));
					altPart.addAndLinkEdge(edge, newPlace, altNet
							.findTransition(firstTransition.getIdentifier()));
				}
				/**
				 * only connect with preceding part if source node has
				 * predecessors
				 */
				if (!(node.getPredecessors().size() == 0)) {
					/**
					 * get the predecessors of this source node, they have to be
					 * connected with the alternative part.
					 */
					HashSet<PNNode> preds = node.getPredecessors();
					/**
					 * get the source node of the alternative part
					 */
					PNNode altSource = (PNNode) altPart.getSource();
					/**
					 * connect the predecessors with the altSource node
					 */
					for (PNNode pred : preds) {
						/**
						 * if both are transitions a place has to be added
						 */
						if (altNet.findTransition(pred.getIdentifier()) != null
								&& altNet.findTransition(altSource
										.getIdentifier()) != null) {
							Place newPlace = new Place("place_"
									+ pred.getIdentifier() + "_"
									+ altSource.getIdentifier(), altNet);
							altNet.addAndLinkPlace(newPlace);
							PNEdge edge1 = new PNEdge(altNet
									.findTransition(pred.getIdentifier()),
									newPlace);
							PNEdge edge2 = new PNEdge(newPlace, altNet
									.findTransition(altSource.getIdentifier()));
							altNet.addAndLinkEdge(edge1, altNet
									.findTransition(pred.getIdentifier()),
									newPlace);
							altNet.addAndLinkEdge(edge2, newPlace, altNet
									.findTransition(altSource.getIdentifier()));

						} else {
							/**
							 * pred is place and altSource is transition
							 */
							if (altNet.findPlace(pred.getIdentifier()) != null
									&& altNet.findTransition(altSource
											.getIdentifier()) != null) {
								PNEdge edge = new PNEdge(altNet.findPlace(pred
										.getIdentifier()), altNet
										.findTransition(altSource
												.getIdentifier()));
								altNet.addAndLinkEdge(edge, altNet
										.findPlace(pred.getIdentifier()),
										altNet.findTransition(altSource
												.getIdentifier()));
							} else {
								/**
								 * pred is transition and altSource is place
								 */
								if (altNet.findTransition(pred.getIdentifier()) != null
										&& altNet.findPlace(altSource
												.getIdentifier()) != null) {
									PNEdge edge = new PNEdge(altNet
											.findTransition(pred
													.getIdentifier()), altNet
											.findPlace(altSource
													.getIdentifier()));
									altNet.addAndLinkEdge(edge, altNet
											.findTransition(pred
													.getIdentifier()), altNet
											.findPlace(altSource
													.getIdentifier()));
								}
							}
						}
					}
				}
			}
		}

		/**
		 * Then, connect the sink of the alternative part with the succeeding
		 * part of the altNet
		 */
		for (PNNode node : model.getPNModel().getNodes()) {
			/**
			 * find the sink node of the component
			 */
			if (subNet.getPNModel().getSink().getIdentifier().equalsIgnoreCase(
					node.getIdentifier())) {
				/**
				 * check if subNet ends with place. if so, add place before sink
				 * of altNet. also add it to altPart, to give it a matching
				 * source
				 */
				if (node instanceof Place) {
					Transition lastTransition = (Transition) altPart.getSink();
					Place newPlace = new Place(subNet.getPNModel().getSink()
							.getIdentifier(), altNet);
					altNet.addAndLinkPlace(newPlace);
					altPart.addAndLinkPlace(newPlace);
					PNEdge edge = new PNEdge(altNet
							.findTransition(lastTransition.getIdentifier()),
							newPlace);
					altNet.addAndLinkEdge(edge, altNet
							.findTransition(lastTransition.getIdentifier()),
							newPlace);
					altPart.addAndLinkEdge(edge, altNet
							.findTransition(lastTransition.getIdentifier()),
							newPlace);
				}
				/**
				 * only connect with succeeding part if sink node has successors
				 */
				if (!(node.getSuccessors().size() == 0)) {
					/**
					 * get the successors of this sink node, they have to be
					 * connected with the alternative part
					 */
					HashSet<PNNode> sucs = node.getSuccessors();
					/**
					 * get the sink node of the alternative part
					 */
					PNNode altSink = (PNNode) altPart.getSink();
					/**
					 * connect the altSink node with the successors
					 */
					for (PNNode suc : sucs) {
						/**
						 * if both are transitions a place has to be added
						 */
						if (altNet.findTransition(suc.getIdentifier()) != null
								&& altNet.findTransition(altSink
										.getIdentifier()) != null) {
							Place newPlace = new Place("place_"
									+ altSink.getIdentifier() + "_"
									+ suc.getIdentifier(), altNet);
							altNet.addAndLinkPlace(newPlace);
							PNEdge edge1 = new PNEdge(newPlace, altNet
									.findTransition(suc.getIdentifier()));
							PNEdge edge2 = new PNEdge(altNet
									.findTransition(altSink.getIdentifier()),
									newPlace);
							altNet.addAndLinkEdge(edge1, newPlace, altNet
									.findTransition(suc.getIdentifier()));
							altNet.addAndLinkEdge(edge2, altNet
									.findTransition(altSink.getIdentifier()),
									newPlace);

						} else {
							/**
							 * suc is place and altSource is transition
							 */
							if (altNet.findPlace(suc.getIdentifier()) != null
									&& altNet.findTransition(altSink
											.getIdentifier()) != null) {
								PNEdge edge = new PNEdge(
										altNet.findTransition(altSink
												.getIdentifier()), altNet
												.findPlace(suc.getIdentifier()));
								altNet
										.addAndLinkEdge(edge, altNet
												.findTransition(altSink
														.getIdentifier()),
												altNet.findPlace(suc
														.getIdentifier()));
							} else {
								/**
								 * pred is transition and altSource is place
								 */
								if (altNet.findTransition(suc.getIdentifier()) != null
										&& altNet.findPlace(altSink
												.getIdentifier()) != null) {
									PNEdge edge = new PNEdge(
											altNet.findPlace(altSink
													.getIdentifier()), altNet
													.findTransition(suc
															.getIdentifier()));
									altNet.addAndLinkEdge(edge,
											altNet.findPlace(altSink
													.getIdentifier()), altNet
													.findTransition(suc
															.getIdentifier()));
								}
							}
						}
					}
				}
			}
		}

		/**
		 * The low-level Petri net is finished. create a HLPetrinet from it note
		 * that with param model addedStartNode and addedEndNode are not added
		 * to the HLActivities of altModel.
		 */
		HLPetriNet altModel = createHLPetriNet(altNet, model);

		return altModel;
	}

	/**
	 * An additional node is added if the graph starts with more than one node
	 * 
	 * @param process
	 *            ModelGraph the graph under consideration
	 * @return process ModelGraph the graph with or without added start node
	 */
	public ModelGraph addStartNode(ModelGraph process) {
		/**
		 * add a new start node if there are multiple start nodes and connect
		 * the new node to the start nodes
		 * */
		if (process.getStartNodes().size() > 1) {
			ModelGraphVertex newStart = new ModelGraphVertex(process);
			newStart.setIdentifier("addedStartNode" + ManagerID.getNewID());
			process.addVertex(newStart);
			for (ModelGraphVertex start : process.getStartNodes()) {
				if (start != newStart) {
					ModelGraphEdge edge = new ModelGraphEdge(newStart, start);
					process.addEdge(edge);
				}
			}

		}
		return process;
	}

	/**
	 * An additional node is added if the graph ends with more than one node
	 * 
	 * @param process
	 *            ModelGraph the graph under consideration
	 * @return process ModelGraph the graph with or without added end node
	 */
	public ModelGraph addEndNode(ModelGraph process) {
		/**
		 * add a new end node if there are multiple end nodes and connect the
		 * end nodes to the new node.
		 * */
		if (process.getEndNodes().size() > 1) {
			ModelGraphVertex newEnd = new ModelGraphVertex(process);
			newEnd.setIdentifier("addedEndNode" + ManagerID.getNewID());
			process.addVertex(newEnd);
			for (ModelGraphVertex end : process.getEndNodes()) {
				if (end != newEnd) {
					ModelGraphEdge edge = new ModelGraphEdge(end, newEnd);
					process.addEdge(edge);
				}
			}

		}
		return process;
	}

	/**
	 * The input model is cloned to create a copy of it.
	 * 
	 * @return alt HLPetriNet the resulting model
	 */
	public HLPetriNet createCopy() {
		HLPetriNet alt = (HLPetriNet) model.clone();
		return alt;
	}

	/**
	 * The input model is copied and a constraint, in the form of a HLAttribute,
	 * is added between the two selected transitions.
	 * 
	 * @param firstTransitionIdentifier
	 *            String the identifier of the first transition
	 * @param secondTransitionIdentifier
	 *            String the identifier of the second transition
	 * @param ConstraintName
	 *            String the name of the constraint
	 * @return alt HLPetriNet the resulting model
	 */
	public HLPetriNet addConstraint(String firstTransitionIdentifier,
			String secondTransitionIdentifier, String constraintName) {
		/**
		 * copy the input model
		 */
		HLPetriNet alt = createCopy();
		/**
		 * add attribute to set of attributes
		 */
		HLAttribute constraint = new HLNominalAttribute(constraintName, alt
				.getHLProcess());
		alt.getHLProcess().addOrReplace(constraint);
		/**
		 * add attribute to the transitions For the first transition, start
		 * point of the constraint, an output data attribute is added, for the
		 * second an input data attribute. The result is the constraint that the
		 * first transition has to be placed before the second.
		 */
		HLActivity firstAct = alt.getHLProcess().findActivityByName(
				firstTransitionIdentifier);
		firstAct.addOutputDataAttribute(constraint.getID());
		HLActivity secAct = alt.getHLProcess().findActivityByName(
				secondTransitionIdentifier);
		secAct.addInputDataAttribute(constraint.getID());
		return alt;
	}
}
