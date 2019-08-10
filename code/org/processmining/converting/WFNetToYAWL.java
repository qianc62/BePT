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
import org.processmining.framework.models.petrinet.PNNode;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.yawl.YAWLDecomposition;
import org.processmining.framework.models.yawl.YAWLModel;
import org.processmining.framework.models.yawl.YAWLTask;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningResult;
import org.processmining.mining.yawlmining.YAWLResult;

/**
 * <p>
 * Title: Labeled WF Net to YAWL model convertor
 * </p>
 * 
 * <p>
 * Description: Takes a labeled WF-net and converts it into a YAWL model.
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
public class WFNetToYAWL implements ConvertingPlugin {

	private boolean isSimple = false;

	public WFNetToYAWL() {
	}

	public WFNetToYAWL(boolean isSimple) {
		this.isSimple = isSimple;
	}

	public String getName() {
		return "Labeled WF net to YAWL model";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:wfn2yawl";
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

		YAWLModel model = convert(providedPN);
		model.Test("WFNetToYAWL");

		return new YAWLResult(log, model);
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
	 *            ModelGraphVertex The given object.
	 * @return String X\tY, where X is the object's name and Y makes the name
	 *         unique. Y will be stripped from the name at an appropriate time,
	 *         leaving X.
	 */
	public String getName(ModelGraphVertex element) {
		String name = element.getIdentifier() + '\t'
				+ String.valueOf(element.getId());
		return name;
	}

	/**
	 * Convert the given Petri net into a YAWL model.
	 * 
	 * @param net
	 *            PetriNet The given Petri net.
	 * @return YAWLModel The constructed YAWL model.
	 */
	public YAWLModel convert(PetriNet net) {
		String uri = net.getIdentifier().replaceAll(" ", ""); // Remove spaces,
		// as these ar
		// enot allowed
		// in an URI.
		boolean needsRouter = false;
		if (uri.length() < 1) {
			uri = "WFNet" + String.valueOf(net.hashCode());
		}
		// Add the YAWL model
		YAWLModel model = new YAWLModel(uri);
		// Add the root decomposition
		YAWLDecomposition decomposition = new YAWLDecomposition("labeledWFNet",
				"true", "NetFactsType");
		model.addDecomposition("labeledWFNet", decomposition);

		// Add the decomposition that will be used for all transitions
		YAWLDecomposition taskDecomposition = new YAWLDecomposition("Task",
				"false", "WebServiceGatewayFactsType");
		model.addDecomposition("Task", taskDecomposition);

		HashSet<Place> places = new HashSet<Place>();
		places.addAll(net.getPlaces());
		HashSet<Transition> transitions = new HashSet<Transition>();
		transitions.addAll(net.getTransitions());
		HashMap<Transition, HashSet<PNNode>> joinSpheres = new HashMap<Transition, HashSet<PNNode>>();
		HashMap<Transition, HashSet<PNNode>> splitSpheres = new HashMap<Transition, HashSet<PNNode>>();
		Iterator it = net.getTransitions().iterator();
		// First, all labeled transitions are mapped
		while (it.hasNext()) {
			Transition transition = (Transition) it.next();
			if (!transition.isInvisibleTask()) {
				HashSet<PNNode> joinSphere = getSphere(net, transition, true,
						isSimple);
				HashSet<PNNode> splitSphere = getSphere(net, transition, false,
						isSimple);
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
				YAWLTask yt = decomposition.addTask(getName(transition),
						getType(transition, joinSphere, true), getType(
								transition, splitSphere, false), "Task",
						transition.getLogEvent());
				yt.setIdentifier(transition.getIdentifier());
			}
		}
		// Second, remaining silent transitions are mapped
		// Message.add(places.toString(), Message.DEBUG);
		while (!transitions.isEmpty()) {
			it = transitions.iterator();
			Transition transition, maxTransition = null;
			int size, maxSize = -1;
			HashSet<PNNode> joinSphere, splitSphere;
			while (it.hasNext()) {
				transition = (Transition) it.next();
				joinSphere = getSphere(net, transition, true, isSimple);
				splitSphere = getSphere(net, transition, false, isSimple);
				size = joinSphere.size() + splitSphere.size();
				if (size > maxSize) {
					maxTransition = transition;
					maxSize = size;
				}
			}
			transition = maxTransition;
			joinSphere = getSphere(net, transition, true, isSimple);
			splitSphere = getSphere(net, transition, false, isSimple);
			Message.add("transition=" + transition.toString(), Message.DEBUG);
			joinSpheres.put(transition, joinSphere);
			splitSpheres.put(transition, splitSphere);
			Message.add("joinSphere=" + joinSphere.toString(), Message.DEBUG);
			Message.add("splitSphere=" + splitSphere.toString(), Message.DEBUG);
			places.removeAll(joinSphere);
			places.removeAll(splitSphere);
			transitions.removeAll(joinSphere);
			transitions.removeAll(splitSphere);
			YAWLTask yt = decomposition.addTask(getName(transition), getType(
					transition, joinSphere, true), getType(transition,
					splitSphere, false), "Router", null);
			yt.setIdentifier(transition.getIdentifier());
			needsRouter = true;
		}
		if (needsRouter) {
			YAWLDecomposition routerDecomposition = new YAWLDecomposition(
					"Router", "false", "WebServiceGatewayFactsType");
			model.addDecomposition("Router", routerDecomposition);

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
		it = places.iterator();
		while (it.hasNext()) {
			Place place = (Place) it.next();
			if (place.getPredecessors().isEmpty()) {
				decomposition.addInputCondition(getName(place));
			} else if (place.getSuccessors().isEmpty()) {
				decomposition.addOutputCondition(getName(place));
			} else {
				decomposition.addCondition(getName(place));
			}
		}
		HashMap<String, String> map = new HashMap<String, String>();
		// Fifth, add edges between YAWL tasks
		it = joinSpheres.keySet().iterator();
		while (it.hasNext()) {
			Transition joinTransition = (Transition) it.next();
			Iterator it2 = joinSpheres.keySet().iterator();
			while (it2.hasNext()) {
				Transition splitTransition = (Transition) it2.next();
				HashSet<PNNode> splitSphere = splitSpheres.get(splitTransition);
				String splitType = getType(splitTransition, splitSphere, false);
				HashSet<PNNode> joinSphere = joinSpheres.get(joinTransition);
				if (joinTransition == splitTransition) {
					joinSphere.remove(joinTransition);
					splitSphere.remove(splitTransition);
				}
				if (connect(splitSphere, joinSphere)) {
					String ordering = "0";
					if (map.containsKey(getName(splitTransition))) {
						ordering = map.get(getName(splitTransition));
						int i = Integer.parseInt(ordering) + 1;
						ordering = String.valueOf(i);
						decomposition.addEdge(getName(splitTransition),
								getName(joinTransition), false, "true",
								splitType.equals("xor") ? ordering : null);
					} else {
						decomposition.addEdge(getName(splitTransition),
								getName(joinTransition), splitType
										.equals("xor")
										|| splitType.equals("or"), splitType
										.equals("or") ? "true" : null,
								splitType.equals("xor") ? ordering : null);
					}
					map.put(getName(splitTransition), ordering);
				}
				if (joinTransition == splitTransition) {
					joinSphere.add(joinTransition);
					splitSphere.add(splitTransition);
				}
			}
		}
		// Sixth, add edges from YAWL task to YAWL conditions and v.v.
		it = joinSpheres.keySet().iterator();
		while (it.hasNext()) {
			Transition transition = (Transition) it.next();
			HashSet<PNNode> joinSphere = (HashSet<PNNode>) joinSpheres
					.get(transition);
			HashSet<PNNode> splitSphere = (HashSet<PNNode>) splitSpheres
					.get(transition);
			String splitType = getType(transition, splitSphere, false);
			Iterator it2 = places.iterator();
			while (it2.hasNext()) {
				Place place = (Place) it2.next();
				HashSet<PNNode> placeSphere = new HashSet<PNNode>();
				placeSphere.add(place);
				if (connect(splitSphere, placeSphere)) {
					String ordering = "0";
					if (map.containsKey(getName(transition))) {
						ordering = map.get(getName(transition));
						int i = Integer.parseInt(ordering) + 1;
						ordering = String.valueOf(i);
						decomposition.addEdge(getName(transition),
								getName(place), false, "true", splitType
										.equals("xor") ? ordering : null);
					} else {
						decomposition.addEdge(getName(transition),
								getName(place), splitType.equals("xor")
										|| splitType.equals("or"), splitType
										.equals("or") ? "true" : null,
								splitType.equals("xor") ? ordering : null);
					}
					map.put(getName(transition), ordering);
				}
				if (connect(placeSphere, joinSphere)) {
					decomposition.addEdge(getName(place), getName(transition),
							false, null, "0");
				}
			}
		}

		return model;
	}

	/**
	 * Construct the join/split sphere for the given transition in ht egiven
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
	public static HashSet<PNNode> getSphere(PetriNet net,
			Transition labeledTransition, boolean isJoin, boolean isSimple) {
		HashSet<PNNode> sphere = new HashSet<PNNode>();
		sphere.add(labeledTransition);
		boolean done = false, inSphere;
		Place place;
		Transition transition;
		Iterator it, it2;

		while (!done) {
			done = true; // No place has been added yet.
			// First, find and add places for which all output (input)
			// transitions are already part of the sphere.
			it = net.getPlaces().iterator();
			while (it.hasNext()) {
				place = (Place) it.next();
				if (!sphere.contains(place) && place.getSuccessors().size() > 0
						&& place.getPredecessors().size() > 0) {
					if (isJoin) {
						it2 = place.getSuccessors().iterator();
					} else {
						it2 = place.getPredecessors().iterator();
					}
					// Check whether all output (input) transitions are already
					// in sphere.
					inSphere = true;
					while (inSphere && it2.hasNext()) {
						if (sphere.contains(it2.next())) {
						} else {
							inSphere = false;
						}
					}
					if (inSphere) { // Yes, they are. Add this place.
						sphere.add(place);
						done = false; // A place has been added.
					}
				}
			}
			if (!done) { // Only if some places have been added to the sphere.
				done = true; // No transition has been added yet.
				// Second, find and add transitions for which all output (input)
				// places are already part of the sphere.
				it = net.getTransitions().iterator();
			}
			while (it.hasNext()
					&& (!isSimple || !labeledTransition.isInvisibleTask())) {
				transition = (Transition) it.next();
				if (!sphere.contains(transition)
						&& transition.isInvisibleTask()) {
					if (isJoin) {
						it2 = transition.getSuccessors().iterator();
					} else {
						it2 = transition.getPredecessors().iterator();
					}
					// Check whether all output (input) places are already in
					// sphere.
					inSphere = true;
					while (inSphere && it2.hasNext()) {
						if (sphere.contains(it2.next())) {
						} else {
							inSphere = false;
						}
					}
					if (inSphere) { // Yes, they are. Add this transition.
						sphere.add(transition);
						done = isSimple; // A transition has been added.
					}
				}
			}
		}
		return sphere;
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
	public static String getType(Transition sphereTransition,
			HashSet<PNNode> sphere, boolean isJoin) {
		boolean isNone = true, isAnd = true, isXor = true;
		Iterator it = sphere.iterator();
		while (it.hasNext() && (isNone || isAnd || isXor)) {
			Object object = it.next();
			if (object instanceof Place) {
				Place place = (Place) object;
				if (place.getPredecessors().size() != 1) { // If place with
					// multiple inputs
					// Keep only isXor and only if join sphere.
					isNone = false;
					isAnd = false;
					if (!isJoin) {
						isXor = false;
					}
				}
				if (place.getSuccessors().size() != 1) { // If place has
					// multiple outputs
					// Keep only isXor and only if split sphere.
					isNone = false;
					isAnd = false;
					if (isJoin) {
						isXor = false;
					}
				}
			} else {
				Transition transition = (Transition) object;
				if ((transition != sphereTransition || isJoin) && // Ignore
						// inputs of
						// sphere
						// transition
						// if join
						// sphere
						transition.getPredecessors().size() != 1) { // If
					// transition
					// has
					// multiple
					// inputs
					// Keep only isAnd and only if join sphere.
					isNone = false;
					isXor = false;
					if (!isJoin) {
						isAnd = false;
					}
				}
				if ((transition != sphereTransition || !isJoin) && // Ignore
						// outputs
						// of sphere
						// transition
						// if split
						// sphere
						transition.getSuccessors().size() != 1) { // If
					// transition
					// has
					// multiple
					// outputs
					// Keep only isAnd and only if split sphere
					isNone = false;
					isXor = false;
					if (isJoin) {
						isAnd = false;
					}
				}
			}
		}
		if (isNone) {
			return "none";
		} else if (isAnd) {
			return "and";
		} else if (isXor) {
			return "xor";
		} else {
			return "or";
		}
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
	public static boolean connect(HashSet<PNNode> splitSphere,
			HashSet<PNNode> joinSphere) {
		boolean connect = false;
		Iterator it = splitSphere.iterator();
		while (!connect && it.hasNext()) {
			Object object = it.next();
			if (joinSphere.contains(object)) {
				connect = true;
			} else {
				if (object instanceof Place) {
					Place place = (Place) object;
					Iterator it2 = place.getSuccessors().iterator();
					while (!connect && it2.hasNext()) {
						Transition transition = (Transition) it2.next();
						if (joinSphere.contains(transition)) {
							connect = true;
						}
					}
				} else {
					Transition transition = (Transition) object;
					Iterator it2 = transition.getSuccessors().iterator();
					while (!connect && it2.hasNext()) {
						Place place = (Place) it2.next();
						if (joinSphere.contains(place)) {
							connect = true;
						}
					}
				}
			}
		}
		return connect;
	}
}
