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

package org.processmining.converting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.algorithms.ConnectorStructureExtractor;
import org.processmining.framework.models.petrinet.PNNode;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningResult;
import org.processmining.mining.epcmining.EPCResult;

/**
 * <p>
 * Title: Labeled WF Net to EPC convertor
 * </p>
 * 
 * <p>
 * Description: Takes a labeled WF-net and converts it into an EPC. This
 * convertor heavily depends on the convertor form labeled WF nets to YAWL
 * models.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004 Eric Verbeek
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class WFNetToEPC implements ConvertingPlugin {

	public WFNetToEPC() {
	}

	public String getName() {
		return "Labeled WF net to EPC";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:wfn2epc";
	}

	public MiningResult convert(ProvidedObject object) {
		PetriNet providedPN = null;
		LogReader log = null;

		for (int i = 0; i < object.getObjects().length; i++) {
			if (providedPN == null
					&& object.getObjects()[i] instanceof PetriNet) {
				providedPN = (PetriNet) object.getObjects()[i];
			}
			if (log == null && object.getObjects()[i] instanceof LogReader) {
				log = (LogReader) object.getObjects()[i];
			}
		}

		if (providedPN == null) {
			return null;
		}

		boolean hasArcWeights = false;
		for (Transition transition : providedPN.getTransitions()) {
			if (transition.getPredecessors().size() != (transition.getInEdges() != null ? transition
					.getInEdges().size()
					: 0)) {
				hasArcWeights = true;
			} else if (transition.getSuccessors().size() != (transition
					.getOutEdges() != null ? transition.getOutEdges().size()
					: 0)) {
				hasArcWeights = true;
			}
		}
		if (hasArcWeights) {
			Message.add("Plug-in \"" + getName() + "\"", Message.WARNING);
			Message.add("  Petri-net \"" + providedPN.getName()
					+ "\" contains arc weights that exceed one.",
					Message.WARNING);
			Message
					.add(
							"  These arc weights will be ignored by this plug-in, that is, assumed to be one.",
							Message.WARNING);
		}

		ConfigurableEPC epc = convert(providedPN);
		epc.Test("WFNetToEPC");

		return new EPCResult(log, epc);
	}

	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof PetriNet) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Construct a unqiue name for the given object.
	 * 
	 * @param element
	 *            ModelGraphVertex THe given object.
	 * @return String The constructed name.
	 */
	public String getName(ModelGraphVertex element) {
		String name = element.getIdentifier();
		return name;
	}

	/**
	 * Convert the given Petri net into an EPC.
	 * 
	 * @param net
	 *            PetriNet The given Petri net.
	 * @return EPC The constructed EPC.
	 */
	public ConfigurableEPC convert(PetriNet net) {
		// Initiate the EPC
		ConfigurableEPC epc = new EPC();
		HashMap<PNNode, EPCConnector> pointsOfEntry = new HashMap<PNNode, EPCConnector>();
		HashMap<PNNode, EPCConnector> pointsOfExit = new HashMap<PNNode, EPCConnector>();

		HashSet<Place> places = new HashSet<Place>();
		places.addAll(net.getPlaces());
		HashSet<Transition> transitions = new HashSet<Transition>();
		transitions.addAll(net.getTransitions());
		HashMap<Transition, HashSet<PNNode>> joinSpheres = new HashMap<Transition, HashSet<PNNode>>();
		HashMap<Transition, HashSet<PNNode>> splitSpheres = new HashMap<Transition, HashSet<PNNode>>();
		for (Transition transition : net.getTransitions()) {
			if (!transition.isInvisibleTask()) {
				HashSet<PNNode> joinSphere = getSphere(net, transition, true);
				HashSet<PNNode> splitSphere = getSphere(net, transition, false);
				Message.add("transition=" + transition.toString(),
						Message.DEBUG);
				joinSpheres.put(transition, joinSphere);
				splitSpheres.put(transition, splitSphere);
				Message.add("joinSphere=" + joinSphere.toString(),
						Message.DEBUG);
				Message.add("splitSphere=" + splitSphere.toString(),
						Message.DEBUG);
				places.removeAll(joinSphere);
				places.removeAll(splitSphere);
				transitions.removeAll(joinSphere);
				transitions.removeAll(splitSphere);

				// A YAWL task is broken down into a join connector, an event, a
				// function, and a split connector.
				EPCFunction function = new EPCFunction(
						transition.getLogEvent(), false, epc);
				if (transition.getLogEvent() == null) {
					function.setIdentifier(transition.getIdentifier());
				}
				epc.addFunction(function);
				EPCEvent event = new EPCEvent("Status change to "
						+ getName(transition), epc);
				epc.addEvent(event);
				epc.addEdge(event, function);

				EPCConnector joinConnector = null;
				String joinType = getType(transition, joinSphere, true);
				if (joinType.compareTo("and") == 0) {
					joinConnector = new EPCConnector(EPCConnector.AND, epc);
				} else if (joinType.compareTo("xor") == 0) {
					joinConnector = new EPCConnector(EPCConnector.XOR, epc);
				} else if (joinType.compareTo("or") == 0) {
					joinConnector = new EPCConnector(EPCConnector.OR, epc);
				} else {
					joinConnector = new EPCConnector(EPCConnector.AND, epc);
				}
				epc.addConnector(joinConnector);
				epc.addEdge(joinConnector, event);

				EPCConnector splitConnector = null;
				String splitType = getType(transition, splitSphere, false);
				if (splitType.compareTo("and") == 0) {
					splitConnector = new EPCConnector(EPCConnector.AND, epc);
				} else if (splitType.compareTo("xor") == 0) {
					splitConnector = new EPCConnector(EPCConnector.XOR, epc);
				} else if (splitType.compareTo("or") == 0) {
					splitConnector = new EPCConnector(EPCConnector.OR, epc);
				} else {
					splitConnector = new EPCConnector(EPCConnector.AND, epc);
				}
				epc.addConnector(splitConnector);
				epc.addEdge(function, splitConnector);

				pointsOfEntry.put(transition, joinConnector);
				if (transition.getSuccessors().isEmpty()) {
					EPCEvent completedEvent = new EPCEvent("Completed "
							+ getName(transition), epc);
					epc.addEvent(completedEvent);
					epc.addEdge(splitConnector, completedEvent);
				} else {
					pointsOfExit.put(transition, splitConnector);
				}
			}
		}
		// Second, remaining silent transitions are mapped
		// Message.add(places.toString(), Message.DEBUG);
		while (!transitions.isEmpty()) {
			Transition maxTransition = null;
			int size, maxSize = -1;
			HashSet<PNNode> joinSphere, splitSphere;
			for (Transition transition : transitions) {
				joinSphere = getSphere(net, transition, true);
				splitSphere = getSphere(net, transition, false);
				size = joinSphere.size() + splitSphere.size();
				if (size > maxSize) {
					maxTransition = transition;
					maxSize = size;
				}
			}
			Transition transition = maxTransition;
			joinSphere = getSphere(net, transition, true);
			splitSphere = getSphere(net, transition, false);
			Message.add("transition=" + transition.toString(), Message.DEBUG);
			joinSpheres.put(transition, joinSphere);
			splitSpheres.put(transition, splitSphere);
			Message.add("joinSphere=" + joinSphere.toString(), Message.DEBUG);
			Message.add("splitSphere=" + splitSphere.toString(), Message.DEBUG);
			places.removeAll(joinSphere);
			places.removeAll(splitSphere);
			transitions.removeAll(joinSphere);
			transitions.removeAll(splitSphere);

			// A YAWL router is broken down into a join connector and a split
			// connector.
			EPCConnector joinConnector = null;
			String joinType = getType(transition, joinSphere, true);
			if (joinType.compareTo("and") == 0) {
				joinConnector = new EPCConnector(EPCConnector.AND, epc);
			} else if (joinType.compareTo("xor") == 0) {
				joinConnector = new EPCConnector(EPCConnector.XOR, epc);
			} else if (joinType.compareTo("or") == 0) {
				joinConnector = new EPCConnector(EPCConnector.OR, epc);
			} else {
				joinConnector = new EPCConnector(EPCConnector.AND, epc);
			}
			epc.addConnector(joinConnector);

			EPCConnector splitConnector = null;
			String splitType = getType(transition, splitSphere, false);
			if (splitType.compareTo("and") == 0) {
				splitConnector = new EPCConnector(EPCConnector.AND, epc);
			} else if (splitType.compareTo("xor") == 0) {
				splitConnector = new EPCConnector(EPCConnector.XOR, epc);
			} else if (splitType.compareTo("or") == 0) {
				splitConnector = new EPCConnector(EPCConnector.OR, epc);
			} else {
				splitConnector = new EPCConnector(EPCConnector.AND, epc);
			}
			epc.addConnector(splitConnector);
			epc.addEdge(joinConnector, splitConnector);

			pointsOfEntry.put(transition, joinConnector);
			pointsOfExit.put(transition, splitConnector);
		}
		// Third, add all mapped transitions as YAWL tasks (has been done now in
		// previous steps
		/*
		 * it = joinSpheres.keySet().iterator(); while (it.hasNext()) {
		 * Transition transition = (Transition) it.next(); HashSet joinSphere =
		 * (HashSet) joinSpheres.get(transition); HashSet splitSphere =
		 * (HashSet) splitSpheres.get(transition);
		 * decomposition.addTask(getName(transition), getType(transition,
		 * joinSphere, true), getType(transition, splitSphere, false), "Task");
		 * }
		 */
		// Fourth, add all remaining places as YAWL conditions
		// Message.add(places.toString(), Message.DEBUG);
		for (Place place : places) {
			if (place.getPredecessors().isEmpty()) {
				// Introduce a new start event and start function if need be.
				boolean needStartEvent = true;
				if (place.getSuccessors().size() == 1) {
					Transition transition = (Transition) place.getSuccessors()
							.iterator().next();
					if (!transition.isInvisibleTask()) {
						needStartEvent = false; // Don't introduce if the only
						// successor is a visible task.
					}
				} else if (place.getSuccessors().size() == 0) {
					EPCEvent event = new EPCEvent(place.getIdentifier(), epc);
					epc.addEvent(event);
					needStartEvent = false;
				}
				if (needStartEvent) {
					EPCFunction function = new EPCFunction(null, false, epc);
					epc.addFunction(function);
					EPCEvent event = new EPCEvent("Start", epc);
					epc.addEvent(event);
					epc.addEdge(event, function);
					EPCConnector splitConnector = new EPCConnector(
							EPCConnector.XOR, epc);
					epc.addConnector(splitConnector);
					epc.addEdge(function, splitConnector);
					pointsOfExit.put(place, splitConnector);
				}
			} else if (place.getSuccessors().isEmpty()) {
				EPCConnector joinConnector = new EPCConnector(EPCConnector.XOR,
						epc);
				epc.addConnector(joinConnector);
				EPCEvent event = new EPCEvent(getName(place), epc);
				epc.addEvent(event);
				epc.addEdge(joinConnector, event);
				pointsOfEntry.put(place, joinConnector);
			} else {
				// A YAWL condition is broken down into an XOR join connector
				// and an XOR split connector.
				EPCConnector joinConnector = new EPCConnector(EPCConnector.XOR,
						epc);
				epc.addConnector(joinConnector);
				EPCConnector splitConnector = new EPCConnector(
						EPCConnector.XOR, epc);
				epc.addConnector(splitConnector);
				epc.addEdge(joinConnector, splitConnector);
				pointsOfEntry.put(place, joinConnector);
				pointsOfExit.put(place, splitConnector);
			}
		}
		// Fifth, add edges between YAWL tasks
		for (Object joinTransition : joinSpheres.keySet()) {
			for (Object splitTransition : splitSpheres.keySet()) {
				HashSet<PNNode> joinSphere = joinSpheres.get(joinTransition);
				HashSet<PNNode> splitSphere = splitSpheres.get(splitTransition);
				if (joinTransition == splitTransition) {
					joinSphere.remove(joinTransition);
					splitSphere.remove(splitTransition);
				}
				if (connect(splitSphere, joinSphere)) {
					EPCConnector pointOfExit = (EPCConnector) pointsOfExit
							.get(splitTransition);
					EPCConnector pointOfEntry = (EPCConnector) pointsOfEntry
							.get(joinTransition);
					if (pointOfExit != null && pointOfEntry != null) {
						pointsOfExit.remove(splitTransition);
						pointsOfEntry.remove(joinTransition);
						epc.addEdge(pointOfExit, pointOfEntry);
						pointsOfExit.put((PNNode) splitTransition, pointOfExit);
						pointsOfEntry
								.put((PNNode) joinTransition, pointOfEntry);
					}
				}
				if (joinTransition == splitTransition) {
					joinSphere.add((PNNode) joinTransition);
					splitSphere.add((PNNode) splitTransition);
				}
			}
		}
		// Sixth, add edges from YAWL task to YAWL conditions and v.v.
		for (Object transition : splitSpheres.keySet()) {
			for (Place place : places) {
				HashSet<PNNode> placeSphere = new HashSet<PNNode>();
				placeSphere.add(place);
				HashSet<PNNode> joinSphere = (HashSet<PNNode>) joinSpheres
						.get(transition);
				HashSet<PNNode> splitSphere = (HashSet<PNNode>) splitSpheres
						.get(transition);
				if (connect(splitSphere, placeSphere)) {
					EPCConnector pointOfExit = (EPCConnector) pointsOfExit
							.get(transition);
					EPCConnector pointOfEntry = (EPCConnector) pointsOfEntry
							.get(place);
					if (pointOfExit != null && pointOfEntry != null) {
						pointsOfExit.remove(transition);
						pointsOfEntry.remove(place);
						epc.addEdge(pointOfExit, pointOfEntry);
						pointsOfExit.put((PNNode) transition, pointOfExit);
						pointsOfEntry.put(place, pointOfEntry);
					}
				}
				if (connect(placeSphere, joinSphere)) {
					EPCConnector pointOfExit = (EPCConnector) pointsOfExit
							.get(place);
					EPCConnector pointOfEntry = (EPCConnector) pointsOfEntry
							.get(transition);
					if (pointOfExit != null && pointOfEntry != null) {
						pointsOfExit.remove(place);
						pointsOfEntry.remove(transition);
						epc.addEdge(pointOfExit, pointOfEntry);
						pointsOfExit.put(place, pointOfExit);
						pointsOfEntry.put((PNNode) transition, pointOfEntry);
					}
				}
			}
		}

		// Remove any source connect. because we did not add a new start event
		// (and function), any original start event will generate a source
		// connector.
		Iterator it = epc.getConnectors().iterator();
		while (it.hasNext()) {
			EPCConnector connector = (EPCConnector) it.next();
			if (connector.getPredecessors().isEmpty()) {
				epc.delConnector(connector);
				it = epc.getConnectors().iterator();
			}
		}

		// Finally, reduce any connector that has one input and one output.
		return ConnectorStructureExtractor.extract(epc, true, false, false,
				false, false, false, false, true, false, false);
		// return epc;
	}

	/**
	 * Construct the join/split sphere for the given transition in the given
	 * Petri net.
	 * 
	 * @param net
	 *            PetriNet The given Petri net.
	 * @param labeledTransition
	 *            Transition The given transition.
	 * @param isJoin
	 *            boolean Whether to construct the join sphere (true) or a split
	 *            sphere (false).
	 * @return HashSet
	 */
	private HashSet<PNNode> getSphere(PetriNet net,
			Transition labeledTransition, boolean isJoin) {
		return WFNetToYAWL.getSphere(net, labeledTransition, isJoin, false);
	}

	/**
	 * Determine the join/split type of the task corresponding to the given
	 * transition.
	 * 
	 * @param sphereTransition
	 *            Transition The given transition.
	 * @param sphere
	 *            HashSet Its join/split sphere (depends on isJoin).
	 * @param isJoin
	 *            boolean Whether the join split is requested (true) or the
	 *            split type (false).
	 * @return String
	 */
	public String getType(Transition sphereTransition, HashSet<PNNode> sphere,
			boolean isJoin) {
		return WFNetToYAWL.getType(sphereTransition, sphere, isJoin);
	}

	/**
	 * Determine whether there should be a YAWL edge from 'one YAWL object' to
	 * 'another YAWL object'.
	 * 
	 * @param splitSphere
	 *            HashSet The split sphere of 'one YAWL object'.
	 * @param joinSphere
	 *            HashSet The split sphere of 'another YAWL object'.
	 * @return boolean Whether there should be an edge.
	 */
	private boolean connect(HashSet<PNNode> splitSphere,
			HashSet<PNNode> joinSphere) {
		return WFNetToYAWL.connect(splitSphere, joinSphere);
	}

}
