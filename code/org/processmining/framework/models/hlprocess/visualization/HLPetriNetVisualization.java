/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/
package org.processmining.framework.models.hlprocess.visualization;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLCondition;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLResource;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.HLTypes.ChoiceEnum;
import org.processmining.framework.models.hlprocess.HLTypes.Perspective;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.ui.Message;

import att.grappa.Element;
import att.grappa.Subgraph;

/**
 * Overrides the default visualization methods to include all the simulation
 * information of a HighLevelPetriNet
 * 
 * @author rmans
 */
public class HLPetriNetVisualization extends PetriNet {

	/**
	 * the link to the highlevelPN to be able to access the simulation
	 * information that exists for nodes and edges.
	 */
	protected HLPetriNet highLevelPN;
	/** The perspective that needs to be shown in the Petri Net */
	protected Set<Perspective> perspectivesToShow = new HashSet<Perspective>();
	/** the mapping */
	protected HashMap mappingBoxesToBoxId = new HashMap();

	/**
	 * Creates a diagnostic petri net visualization based on the given
	 * perspectives.
	 * 
	 * @param hlPetriNet
	 *            the high level petri net to be visualized
	 */
	public HLPetriNetVisualization(HLPetriNet hlPetriNet,
			Set<Perspective> thePerspectivesToShow) {
		super();
		highLevelPN = hlPetriNet;
		perspectivesToShow = thePerspectivesToShow;
		PetriNet net = hlPetriNet.getPNModel();

		// construct the petri net without clusters
		// establish the same petri net structure like in the given model
		HashMap mapping = new HashMap();
		// arraylist for all nodes in the petri net
		ArrayList nodeList = new ArrayList();
		Subgraph graph = net.getGrappaVisualization().getSubgraph().getGraph();
		// get all the nodes that are in a cluster
		Enumeration subGraphElts = graph.subgraphElements();
		while (subGraphElts.hasMoreElements()) {
			Element e1 = (Element) subGraphElts.nextElement();
			if (e1 instanceof Subgraph) {
				Subgraph subgraph = (Subgraph) e1;
				Enumeration enumerationNodes = subgraph.nodeElements();
				// put all the nodeElements in nodeList
				while (enumerationNodes.hasMoreElements()) {
					Element enumNode = (Element) enumerationNodes.nextElement();
					if (enumNode != null
							&& enumNode.object instanceof ModelGraphVertex) {
						nodeList.add(enumNode);
					}
				}
			}
		}
		// add the nodes that are not in a cluster to the list of all nodes
		Enumeration nodeElts = graph.nodeElements();
		while (nodeElts.hasMoreElements()) {
			Element e1 = (Element) nodeElts.nextElement();
			if (e1.object != null && e1.object instanceof ModelGraphVertex) {
				nodeList.add(e1);
			}
		}
		// create new transitions
		Iterator transitions = nodeList.iterator();
		while (transitions.hasNext()) {
			Element e1 = (Element) transitions.next();
			if (e1.object != null && e1.object instanceof Transition) {
				// Node n = (Node) e1;
				Transition newTransition = new Transition(
						(Transition) e1.object);
				newTransition.setSubgraph(this);
				this.addTransition(newTransition);

				// keep the mapping until the edges have been established
				mapping.put((Transition) e1.object, newTransition);
			}
		}

		// convert the ordinary places to simulated places
		Iterator places = nodeList.iterator();
		while (places.hasNext()) {
			Element e1 = (Element) places.next();
			if (e1.object != null && e1.object instanceof Place) {
				// Node n = (Node) e1;
				Place newPlace = new Place(((Place) e1.object).getIdentifier(),
						this);
				this.addPlace(newPlace);
			}
		}

		// create new edges.
		Iterator edges = net.getEdges().iterator();
		while (edges.hasNext()) {
			PNEdge edge = (PNEdge) edges.next();
			PNEdge newEdge;
			// if place is source
			if (edge.isPT()) {
				Place p = (Place) edge.getSource();
				// find respective place in this net (place names are assumed to
				// be unique)
				Place myPlace = this.findPlace(p.getIdentifier());
				Transition t = (Transition) edge.getDest();
				// find respective transition in this net
				Transition myTransition = (Transition) mapping.get(t);
				// reproduce edge
				newEdge = new PNEdge(myPlace, myTransition);
				this.addEdge(newEdge);
			}
			// if transition is source
			else {
				Place p = (Place) edge.getDest();
				// find respective place in this net (place names are assumed to
				// be unique)
				Place myPlace = (Place) this.findPlace(p.getIdentifier());
				Transition t = (Transition) edge.getSource();
				// find respective transition in this net
				Transition myTransition = (Transition) mapping.get(t);
				// reproduce edge
				newEdge = new PNEdge(myTransition, myPlace);
				this.addEdge(newEdge);
			}
		}
	}

	// ///////// VISUALIZATION SECTION //////////

	public void writeToDot(Writer bw) throws IOException {
		initDotWriting(bw);
		// in the case that separate boxes needs to be created and that
		// connections need
		// to be made between these boxes and transitions, some additional code
		// needs
		// can be filled in here. (e.g. for the data perspective)
		if (perspectivesToShow.contains(HLTypes.Perspective.DATA_AT_TASKS)) {
			// visualize the data attributes
			Iterator<HLAttribute> dataAttrs = highLevelPN.getHLProcess()
					.getAttributes().iterator();
			int counterDataAttrs = 0;
			while (dataAttrs.hasNext()) {
				HLAttribute dataAttr = dataAttrs.next();
				counterDataAttrs++;
				String idBox = "data" + counterDataAttrs;
				dataAttr.writeDistributionToDot(idBox, "", "", bw);
				// add the data attribute to the mapping together with the
				// corresponding box id
				mappingBoxesToBoxId.put(dataAttr, idBox);
			}
		}
		// present organizational information
		if (perspectivesToShow.contains(HLTypes.Perspective.ROLES_AT_TASKS)) {
			// visualize the groups
			Iterator<HLGroup> groups = highLevelPN.getHLProcess().getGroups()
					.iterator();
			int counterGroups = 0;
			while (groups.hasNext()) {
				HLGroup group = groups.next();
				counterGroups++;
				String idBox = "group" + counterGroups;
				group.writeDistributionToDot(idBox, "", "", bw);
				// add the group to the mapping together with the corresponding
				// box id
				mappingBoxesToBoxId.put(group, idBox);
			}
		}
		// present organizational information
		if (perspectivesToShow
				.contains(HLTypes.Perspective.ORGANIZATIONAL_MODEL)) {
			// visualize the resources in one box
			Iterator<HLResource> resources = highLevelPN.getHLProcess()
					.getResources().iterator();
			String resourcesLabel = "";
			while (resources.hasNext()) {
				HLResource resource = resources.next();
				resourcesLabel = resourcesLabel + resource.getName() + "\\n";
			}
			if (!resourcesLabel.equals("")) {
				bw.write("resources" + " [shape=\"ellipse\", label=\""
						+ resourcesLabel + "\"];\n");
			}
		}
		writeTransitionsToDot(bw);
		// in the case that the case generation scheme perspective is selected,
		// also write the general distribution to
		// the graph
		if (perspectivesToShow.contains(HLTypes.Perspective.CASE_GEN_SCHEME)) {
			highLevelPN.getHLProcess().getGlobalInfo()
					.getCaseGenerationScheme().writeDistributionToDot("cgs",
							"", "Case Generation Scheme:", bw);
		}
		writePlacesToDot(bw);
		writeEdgesToDot(bw);
		writeClustersToDot(bw);
		finishDotWriting(bw);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.petrinet.PetriNet#writeTransitionsToDot
	 * (java.io.Writer)
	 */
	protected void writeTransitionsToDot(Writer bw) throws IOException {
		if (perspectivesToShow.size() == 0) {
			// TODO is there a reason for not calling the super method in this
			// case? (Anne)
			// i.e.:
			// use default
			super.writeTransitionsToDot(bw);
			/*
			 * Iterator it = getTransitions().iterator(); while (it.hasNext()) {
			 * Transition t = (Transition) (it.next()); String label =
			 * t.getIdentifier(); // write to dot bw.write("t" + t.getNumber() +
			 * " [shape=\"box\"" + ",label=\"" + label + "\"];\n"); // connect
			 * Petri net nodes to later grappa components nodeMapping.put(new
			 * String("t" + t.getNumber()), t); }
			 */
		} else {
			Iterator<Transition> it = getTransitions().iterator();
			while (it.hasNext()) {
				Transition t = it.next();
				String label = t.getIdentifier();
				// when the frequency perspective is selected, the frequency
				// also needs to be displayed in the transition
				// for each transitions involved in a choice show the relative
				// frequencies
				// (a) regardless the actual choice configuration (CHOICE_FREQ
				// mode)
				// (b) only if the choice has been configured to take place
				// based on frequencies (CHOICE_CONF)
				if (perspectivesToShow
						.contains(HLTypes.Perspective.CHOICE_CONF)
						|| perspectivesToShow
								.contains(HLTypes.Perspective.CHOICE_FREQ)) {
					// find related high leve activity for current transition
					HLActivity act = highLevelPN.findActivity(t);
					boolean found = false;
					// check whether transition is involved in choice at all
					Iterator<HLChoice> choices = highLevelPN.getHLProcess()
							.getChoices().iterator();
					while (choices.hasNext() && found == false) {
						HLChoice choice = choices.next();
						// is this the choice related to this transition?
						Iterator<HLID> targetIDs = choice.getChoiceTargetIDs()
								.iterator();
						while (targetIDs.hasNext()) {
							HLID actID = targetIDs.next();
							if (act.getID().equals(actID)) {
								HLCondition cond = choice.getCondition(actID);
								// OK - transition is involed in choice
								// now either (a) always include if CHOICE_FREQ
								// mode (regardless choice configuration)
								if (perspectivesToShow
										.contains(HLTypes.Perspective.CHOICE_FREQ)) {
									label = label + "\\nfreq="
											+ cond.getFrequency();
									found = true; // stop looking for further
									// choices
								}
								// or (b) only include if choice is configured
								// as based on frequencies (CHOICE_CONF)
								else if (perspectivesToShow
										.contains(HLTypes.Perspective.CHOICE_CONF)) {
									if (choice.getChoiceConfiguration().equals(
											ChoiceEnum.FREQ)) {
										label = label + "\\nfreq="
												+ cond.getFrequency();
										found = true; // stop looking for
										// further choices
									}
								}
							}
						}

						// //////////////
						// TODO Anne: check and remove
						// Iterator<HLActivity> freqDeps =
						// choice.getFrequencyDependencies().iterator();
						// while (freqDeps.hasNext() && found == false) {
						// HLActivity freqTarget = freqDeps.next();
						// if (freqTarget == act) {
						// // OK - transition is involed in choice
						// // now either (a) always include if CHOICE_FREQ mode
						// (regardless choice configuration)
						// if
						// (perspectivesToShow.contains(HighLevelTypes.Perspective.CHOICE_FREQ))
						// {
						// label = label + "\\nfreq=" +
						// act.getFrequencyDependency();
						// found = true; // stop looking for further choices
						// }
						// // or (b) only include if choice is configured as
						// based on frequencies (CHOICE_CONF)
						// else if
						// (perspectivesToShow.contains(HighLevelTypes.Perspective.CHOICE_CONF))
						// {
						// if
						// (choice.getChoiceConfiguration().equals(ChoiceEnum.FREQ))
						// {
						// label = label + "\\nfreq=" +
						// act.getFrequencyDependency();
						// found = true; // stop looking for further choices
						// }
						// }
						// }
						// }
					}
				}
				// write to dot
				bw.write("t"
						+ t.getNumber()
						+ " [shape=\"box\""
						+ (t.getLogEvent() != null ? ",label=\"" + label + "\""
								: ",label=\"\",style=\"filled\"") + "];\n");
				// connect Petri net nodes to later grappa components
				nodeMapping.put(new String("t" + t.getNumber()), t);
			}
			// if the time perspective is selected also add a box and link it to
			// the transition
			Iterator<Transition> transitionsIt = getTransitions().iterator();
			int counterTiming = 0;
			while (transitionsIt.hasNext()) {
				Transition transition = transitionsIt.next();
				// get the corresponding highlevelactivity
				HLActivity act = highLevelPN.findActivity(transition);
				if (act != null) {
					if (perspectivesToShow
							.contains(HLTypes.Perspective.TIMING_EXECTIME)) {
						counterTiming++;
						act.getExecutionTime().writeDistributionToDot(
								"i" + counterTiming,
								"t" + transition.getNumber(),
								"Execution time:", bw);
					}
					if (perspectivesToShow
							.contains(HLTypes.Perspective.TIMING_WAITTIME)) {
						counterTiming++;
						act.getWaitingTime().writeDistributionToDot(
								"i" + counterTiming,
								"t" + transition.getNumber(), "Waiting time:",
								bw);
					}
					if (perspectivesToShow
							.contains(HLTypes.Perspective.TIMING_SOJTIME)) {
						counterTiming++;
						act.getSojournTime().writeDistributionToDot(
								"i" + counterTiming,
								"t" + transition.getNumber(), "Sojourn time:",
								bw);
					}
					// if the data perspective has been selected, then the data
					// attributes of the highlevelactivities need
					// to be connected to the box of the corresponding data
					// attribute
					if (perspectivesToShow
							.contains(HLTypes.Perspective.DATA_AT_TASKS)) {
						Iterator<HLAttribute> outputDataAttrs = act
								.getOutputDataAttributes().iterator();
						while (outputDataAttrs.hasNext()) {
							HLAttribute dataAttr = outputDataAttrs.next();
							// get the corresponding boxID
							String boxID = (String) mappingBoxesToBoxId
									.get(dataAttr);
							// if is null, then the data attribute attached to
							// this transition could not be found back in the
							// global mapping!
							if (boxID != null) {
								// write the edge
								bw.write("t" + transition.getNumber() + " -> "
										+ boxID
										+ " [dir=none, style=dotted];\n");
							}
						}
						Iterator<HLAttribute> inputDataAttrs = act
								.getInputDataAttributes().iterator();
						while (inputDataAttrs.hasNext()) {
							HLAttribute dataAttr = inputDataAttrs.next();
							// get the corresponding boxID
							String boxID = (String) mappingBoxesToBoxId
									.get(dataAttr);
							// if is null, then the data attribute attached to
							// this transition could not be found back in the
							// global mapping!
							if (boxID != null) {
								// write the edge
								bw.write("t" + transition.getNumber() + " -> "
										+ boxID
										+ " [dir=none, style=dotted];\n");
							}
						}
					}
					// if the organizational perspective has been selected, then
					// the groups of the highlevelactivities need
					// to be connected to the box of the corresponding group
					if (perspectivesToShow
							.contains(HLTypes.Perspective.ROLES_AT_TASKS)) {
						if (act.getGroup() != null) {
							HLGroup group = act.getGroup();
							// get the corresponding boxID
							String boxID = (String) mappingBoxesToBoxId
									.get(group);
							// write the edge
							bw.write("t" + transition.getNumber() + " -> "
									+ boxID + " [dir=none, style=dotted];\n");
						}
					}
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.petrinet.PetriNet#writePlacesToDot
	 * (java.io.Writer)
	 */
	protected void writePlacesToDot(Writer bw) throws IOException {
		super.writePlacesToDot(bw);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.petrinet.PetriNet#writeEdgesToDot(
	 * java.io.Writer)
	 */
	protected void writeEdgesToDot(Writer bw) throws IOException {
		if (perspectivesToShow.size() == 0) {
			super.writeEdgesToDot(bw);
		} else if (perspectivesToShow.contains(HLTypes.Perspective.CHOICE_DATA)
				|| perspectivesToShow.contains(HLTypes.Perspective.CHOICE_PROB)
				|| perspectivesToShow.contains(HLTypes.Perspective.CHOICE_CONF)) {
			try {
				Iterator it = this.getEdges().iterator();
				while (it.hasNext()) {
					PNEdge e = (PNEdge) (it.next());
					if (e.isPT()) {
						Place p = (Place) e.getSource();
						Transition t = (Transition) e.getDest();
						if (p.getOutEdges().size() > 1) {
							// for the places with more than one outgoing edge,
							// display the data or probability dependency, if
							// needed
							// check which perspectives are selected
							String labelText = "";
							if (perspectivesToShow
									.contains(HLTypes.Perspective.CHOICE_DATA)) {
								// get corresponding highleveldata dependency
								Iterator<HLChoice> choiceIt = highLevelPN
										.getHLProcess().getChoices().iterator();
								while (choiceIt.hasNext()) {
									HLChoice choice = choiceIt.next();
									// only if this is the right choice node ..
									if (p
											.equals(highLevelPN
													.findModelGraphVertexForChoice(choice
															.getID()))) {
										Iterator<HLID> targetIDs = choice
												.getChoiceTargetIDs()
												.iterator();
										while (targetIDs.hasNext()) {
											HLID actID = targetIDs.next();
											// .. and if target node is right
											// transition
											if (t
													.equals(highLevelPN
															.findModelGraphVertexForActivity(actID))) {
												labelText = "\""
														+ choice
																.getCondition(
																		actID)
																.getEscapedExpression()
														+ "\"";
												break;
											}
										}
									}
								}
								// Iterator<HighLevelDataDependency> dataDeps =
								// highLevelPN.getDataDependencies().iterator();
								// while (dataDeps.hasNext()) {
								// HLDataDependency dataDep =
								// (HLDataDependency)dataDeps.next();
								// //if (dataDep.getSourceNode().equals(p) &&
								// // ((Transition)
								// dataDep.getTargetNode()).getLogEvent().equals(t.getLogEvent()))
								// {
								// if (dataDep.getSourceNode().equals(p) &&
								// ((Transition)
								// dataDep.getTargetNode()).equals(t)) {
								// labelText = "\"" +
								// dataDep.getEscapedExpression() + "\"";
								// break;
								// }
								// }
							} else if (perspectivesToShow
									.contains(HLTypes.Perspective.CHOICE_PROB)) {
								// get corresponding highlevelprobability
								// dependency
								Iterator<HLChoice> choiceIt = highLevelPN
										.getHLProcess().getChoices().iterator();
								while (choiceIt.hasNext()) {
									HLChoice choice = choiceIt.next();
									// only if this is the right choice node ..
									if (p
											.equals(highLevelPN
													.findModelGraphVertexForChoice(choice
															.getID()))) {
										Iterator<HLID> targetIDs = choice
												.getChoiceTargetIDs()
												.iterator();
										while (targetIDs.hasNext()) {
											HLID actID = targetIDs.next();
											// .. and if target node is right
											// transition
											if (t
													.equals(highLevelPN
															.findModelGraphVertexForActivity(actID))) {
												double prob = choice
														.getCondition(actID)
														.getProbability();
												// display probability in 2
												// decimal digits
												DecimalFormat digits = new DecimalFormat();
												digits
														.setMaximumFractionDigits(2);
												digits
														.setMinimumFractionDigits(2);
												String probStr = digits
														.format(prob);
												// replace commas with dots,
												// since dot forces use of
												// US-Locale
												probStr = probStr.replace(",",
														".");
												// add probStr to labelText
												if (labelText.equals("")) {
													labelText = probStr;
												} else {
													labelText = labelText + " "
															+ probStr;
												}
												break;
											}
										}
									}
								}
								// /////
								// Iterator<HighLevelProbabilityDependency>
								// probDeps =
								// highLevelPN.getProbabilityDependencies().iterator();
								// while (probDeps.hasNext()) {
								// HighLevelProbabilityDependency probDep =
								// probDeps.next();
								// //if (probDep.getSourceNode().equals(p) &&
								// // ((Transition)
								// probDep.getTargetNode()).getLogEvent().equals(t.getLogEvent()))
								// {
								// if (probDep.getSourceNode().equals(p) &&
								// ((Transition)
								// probDep.getTargetNode()).equals(t)) {
								// double prob = probDep.getProbability();
								// // display probability in 2 decimal digits
								// DecimalFormat digits = new DecimalFormat();
								// digits.setMaximumFractionDigits(2);
								// digits.setMinimumFractionDigits(2);
								// String probStr = digits.format(prob);
								// //replace commas with dots, since dot forces
								// use of US-Locale
								// probStr = probStr.replace(",", ".");
								// // add probStr to labelText
								// if (labelText.equals("")) {
								// labelText = probStr;
								// }
								// else {
								// labelText = labelText + " " + probStr;
								// }
								// break;
								// }
								// }
							}
							// visualize the choices according to the choice
							// configuration
							// (needed to visualize actual choices in CPN
							// export)
							// note that only information for (a) data-based
							// decision rules
							// and (b) probabilites is put at the arcs
							else if (perspectivesToShow
									.contains(HLTypes.Perspective.CHOICE_CONF)) {
								Iterator<HLChoice> choices = highLevelPN
										.getHLProcess().getChoices().iterator();
								while (choices.hasNext()) {
									HLChoice choice = choices.next();
									// is this the choice related to the source
									// node of the current edge?
									if (highLevelPN
											.findModelGraphVertexForChoice(
													choice.getID()).equals(p)) {
										// is the choice based on data
										// attributes?
										if (choice.getChoiceConfiguration()
												.equals(ChoiceEnum.DATA)) {
											// find the right alternative branch
											// first
											Iterator<HLID> targetIDs = choice
													.getChoiceTargetIDs()
													.iterator();
											while (targetIDs.hasNext()) {
												HLID actID = targetIDs.next();
												// .. and if target node is
												// right transition
												if (t
														.equals(highLevelPN
																.findModelGraphVertexForActivity(actID))) {
													// now actually write the
													// data expression at the
													// arc
													labelText = "\""
															+ choice
																	.getCondition(
																			actID)
																	.getEscapedExpression()
															+ "\"";
													break;
												}
											}
											// /
											// Iterator<HighLevelDataDependency>
											// dataDeps =
											// choice.getDataDependencies().iterator();
											// while (dataDeps.hasNext()) {
											// HLDataDependency dataDep =
											// (HLDataDependency)
											// dataDeps.next();
											// if (((Transition)
											// dataDep.getTargetNode()).equals(t))
											// {
											// // now actually write the data
											// expression at the arc
											// labelText = "\"" +
											// dataDep.getEscapedExpression() +
											// "\"";
											// break;
											// }
											// }
										}
										// or is the choice based on
										// probabilities?
										else if (choice
												.getChoiceConfiguration()
												.equals(ChoiceEnum.PROB)) {
											// find the right alternative branch
											// first
											Iterator<HLID> targetIDs = choice
													.getChoiceTargetIDs()
													.iterator();
											while (targetIDs.hasNext()) {
												HLID actID = targetIDs.next();
												// .. and if target node is
												// right transition
												if (t
														.equals(highLevelPN
																.findModelGraphVertexForActivity(actID))) {
													// now actually write the
													// data expression at the
													// arc
													double prob = choice
															.getCondition(actID)
															.getProbability();
													// display probability in 2
													// decimal digits
													DecimalFormat digits = new DecimalFormat();
													digits
															.setMaximumFractionDigits(2);
													digits
															.setMinimumFractionDigits(2);
													String probStr = digits
															.format(prob);
													// replace commas with dots,
													// since dot forces use of
													// US-Locale
													probStr = probStr.replace(
															",", ".");
													// add probStr to labelText
													if (labelText.equals("")) {
														labelText = probStr;
													} else {
														labelText = labelText
																+ " " + probStr;
													}
													break;
												}
											}
											//
											// Iterator<HighLevelProbabilityDependency>
											// probDeps =
											// choice.getProbabilityDependencies().iterator();
											// while (probDeps.hasNext()) {
											// HighLevelProbabilityDependency
											// probDep = probDeps.next();
											// if (((Transition)
											// probDep.getTargetNode()).equals(t))
											// {
											// // now actually write the data
											// expression at the arc
											// double prob =
											// probDep.getProbability();
											// // display probability in 2
											// decimal digits
											// DecimalFormat digits = new
											// DecimalFormat();
											// digits.setMaximumFractionDigits(2);
											// digits.setMinimumFractionDigits(2);
											// String probStr =
											// digits.format(prob);
											// // replace commas with dots,
											// since dot forces use of US-Locale
											// probStr = probStr.replace(",",
											// ".");
											// // add probStr to labelText
											// if (labelText.equals("")) {
											// labelText = probStr;
											// }
											// else {
											// labelText = labelText + " " +
											// probStr;
											// }
											// break;
											// }
											// }
										}
										// in case choice is configured as
										// unguided or based on frequencies,
										// the arc label remains empty
										else {
											labelText = "\"" + "\"";
										}
									}
								}
							}
							bw.write("p" + p.getNumber() + " -> t"
									+ t.getNumber() + " [label=" + labelText
									+ "];\n");
						} else {
							// write a normal edge
							bw.write("p" + p.getNumber() + " -> t"
									+ t.getNumber() + ";\n");
						}
					} else {
						// write a normal edge
						Place p = (Place) e.getDest();
						Transition t = (Transition) e.getSource();
						bw.write("t" + t.getNumber() + " -> p" + p.getNumber()
								+ ";\n");
					}
				}
			} catch (Exception e) {
				Message.add("Failure while updating the visualization.\n"
						+ e.toString(), 2);
				e.printStackTrace();
			}

		} else {
			super.writeEdgesToDot(bw);
		}
	}

	protected void finishDotWriting(Writer bw) throws IOException {
		super.finishDotWriting(bw);
	}
}
