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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Token;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningResult;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * <p>
 * Title: Liveness and boundedness preserving reduction for Petri nets
 * </p>
 * <p>
 * Description: Reduces a Petri net given a well-known set of liveness and
 * boundedness preserving rules.
 * </p>
 * <p>
 * T. Murata. Petri nets: Properties, analysis and applications.
 * <em>Proceedings of the IEEE</em>, 77(4):541-580, April 1989.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */

public class PetriNetReduction implements ConvertingPlugin {

	/**
	 * The set of nodes not to reduce. These nodes are 'sacrosanct' for the
	 * reduction rules. Nodes are kept in an ArrayList instead of HashSet for
	 * usability reasons. The hashCode of a Transition object depends on the
	 * PetriNet it is contained in and can therefore change AFTER it has been
	 * added to the sacroSanctNodes.
	 */
	private HashSet sacroSanctNodes;

	/**
	 * The set of nodes not to reduce. These nodes are 'sacrosanct' for the
	 * reduction rules. Nodes are kept in an ArrayList instead of HashSet for
	 * usability reasons. The hashCode of a Transition object depends on the
	 * PetriNet it is contained in and can therefore change AFTER it has been
	 * added to the sacroSanctNodes.
	 */
	private ArrayList nonReducableNodes;

	public PetriNetReduction() {
		nonReducableNodes = new ArrayList();
	}

	public String getName() {
		return "Petri net reduction";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:pnred";
	}

	/**
	 * Reduces the first Petri net among the given objects.
	 * 
	 * @param object
	 *            The given objects.
	 * @return The reduced Petri net (the original Petri net if no reductions
	 *         could be applied!)
	 */
	public MiningResult convert(ProvidedObject object) {
		PetriNet providedPN = null;

		for (int i = 0; providedPN == null && i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof PetriNet) {
				providedPN = (PetriNet) object.getObjects()[i];
			}
		}

		if (providedPN == null) {
			return null;
		}

		PetriNet reducedPN = reduce(providedPN);
		reducedPN.Test("PetriNetReduction");

		return new PetriNetResult(null, reducedPN);
	}

	/**
	 * Determines whether this pluging accepts the given objects as input.
	 * 
	 * @param object
	 *            The given objects.
	 * @return Whether this plugin accepts the given objects.
	 */
	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof PetriNet) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Specify which nodes in the Petri net should not be reduced.
	 * 
	 * @param nodes
	 *            The set of nodes not to reduce.
	 */
	public void setNonReducableNodes(ArrayList nodes) {
		nonReducableNodes = nodes;
	}

	/**
	 * Specify which nodes in the Petri net should not be reduced.
	 * 
	 * @param nodes
	 *            The set of nodes not to reduce.
	 */
	public void setNonReducableNodes(HashSet nodes) {
		nonReducableNodes = new ArrayList(nodes);
	}

	/**
	 * Apply the liveness and boundedness preserving reduction rules on the
	 * given Petri net as long as reductions can be applied.
	 * 
	 * @param providedPN
	 *            The given Petri net.
	 * @return If any reduction could be applied, a fully reduced Petri net.
	 *         Otherwise, the given Petri net.
	 */
	public PetriNet reduce(PetriNet providedPN) {
		return reduce(providedPN, new HashMap());
	}

	public PetriNet reduce(PetriNet originalPN, HashMap pnMap) {
		// for performance reasons, convert the nonReducableNodes into a HashSet
		PetriNet providedPN = originalPN;
		sacroSanctNodes = new HashSet();
		Iterator it = nonReducableNodes.iterator();
		while (it.hasNext()) {
			sacroSanctNodes.add(it.next());
		}
		Message
				.add("Irreducible: " + sacroSanctNodes.toString(),
						Message.DEBUG);

		int count = 1;
		int nofPlaces = providedPN.getPlaces().size();
		int nofTransitions = providedPN.getTransitions().size();
		int nofArcs = providedPN.getEdges().size();
		HashMap workMap = new HashMap();

		// Start reduction. Store all reductions in workMap.
		PetriNet reducedPN = reduceOnce(providedPN, workMap);
		while (count > 0 && reducedPN != providedPN) { // Some reductions could
			// be applied, try
			// again.
			HashMap newMap = new HashMap();
			providedPN = reducedPN;
			reducedPN = reduceOnce(providedPN, newMap);

			if (providedPN != reducedPN) {
				// Update workMap
				HashMap updMap = new HashMap();
				for (Object key : workMap.keySet()) {
					if (newMap.containsKey(workMap.get(key))) {
						updMap.put(key, newMap.get(workMap.get(key)));
					}
				}
				workMap = updMap;
			}
		}

		// No reductions could be applied any more. Return result.
		// Note that if no reductions could be applied at all, then reducedPN
		// equals providedPN.
		for (Object key : workMap.keySet()) {
			pnMap.put(key, workMap.get(key));
		}

		sacroSanctNodes = null;
		return reducedPN;
	}

	/*
	 * The reduction rules cannot reduce a vertex with incident edges with
	 * weight exceeding 1. Therefore, we need to test explicitly whether such
	 * edge exists.
	 */

	/**
	 * Checks whether all outgoing edges have weight 1.
	 * 
	 * @param vertex
	 *            ModelGraphVertex The given vertex
	 * @return boolean Whether all outgoing edges have weight 1.
	 */
	private boolean hasUniqueSuccessors(ModelGraphVertex vertex) {
		return vertex.getSuccessors().size() == (vertex.getOutEdges() == null ? 0
				: vertex.getOutEdges().size());
	}

	/**
	 * Checks whether all incoming edges have weight 1.
	 * 
	 * @param vertex
	 *            ModelGraphVertex The given vertex
	 * @return boolean Whether all incoming edges have weight 1.
	 */
	private boolean hasUniquePredecessors(ModelGraphVertex vertex) {
		return vertex.getPredecessors().size() == (vertex.getInEdges() == null ? 0
				: vertex.getInEdges().size());
	}

	/**
	 * Apply the liveness and boundedness preserving reduction rules on the
	 * given Petri net in one pass.
	 * 
	 * @param providedPN
	 *            The given Petri net.
	 * @return If any reduction could be applied, a reduced Petri net.
	 *         Otherwise, the given Petri net.
	 */
	public PetriNet reduceOnce(PetriNet providedPN) {
		return reduceOnce(providedPN, new HashMap());
	}

	public PetriNet reduceOnce(PetriNet providedPN, HashMap pnMap) {
		HashSet notReducedPlaces = new HashSet();
		HashSet notReducedTransitions = new HashSet();
		HashMap reducedPlaces = new HashMap();
		HashMap reducedTransitions = new HashMap() {
			public Object put(Object key, Object object) {
				Message.add("Map " + key.toString() + " to "
						+ object.toString(), Message.DEBUG);
				return super.put(key, object);
			}
		};
		HashSet mappedSacroSanctNodes = new HashSet();

		Iterator it;

		PetriNet reducedPN = new PetriNet();
		HashMap places = new HashMap();
		HashMap transitions = new HashMap();

		it = providedPN.getPlaces().iterator();
		while (it.hasNext()) {
			Place place = (Place) it.next();
			if (sacroSanctNodes.contains(place)) {
				Place mappedPlace = reducedPN.addPlace(place.getIdentifier());
				for (int i = 0; i < place.getNumberOfTokens(); i++) {
					mappedPlace.addToken(new Token());
				}
				places.put(place, mappedPlace);
				pnMap.put(place, mappedPlace);
			} else {
				notReducedPlaces.add(place);
			}
		}
		it = providedPN.getTransitions().iterator();
		while (it.hasNext()) {
			Transition transition = (Transition) it.next();
			if (sacroSanctNodes.contains(transition)) {
				// First clone the Transition and then add it to the right graph
				// using the addAndLinkTransition method.
				Transition mappedTransition = new Transition(transition);
				reducedPN.addAndLinkTransition(mappedTransition);

				transitions.put(transition, mappedTransition);
				pnMap.put(transition, mappedTransition);
			} else {
				notReducedTransitions.add(transition);
			}
		}

		// Check Murata rules one by one.
		boolean nothingReduced = true;

		// Rule 1: Fusion of Series Places
		it = providedPN.getPlaces().iterator();
		while (it.hasNext()) {
			Place place = (Place) it.next();
			if ((notReducedPlaces.contains(place) || sacroSanctNodes
					.contains(place))
					&& // place not reduced
					hasUniqueSuccessors(place)
					&& place.getSuccessors().size() == 1) { // place has only
				// one output
				// transition
				Transition transition = (Transition) place.getSuccessors()
						.iterator().next(); // Get the output transition
				if (notReducedTransitions.contains(transition)
						&& // output transition is not reduced
						hasUniquePredecessors(transition)
						&& transition.getPredecessors().size() == 1
						&& // output transition has only one input place
						hasUniqueSuccessors(transition)
						&& transition.getSuccessors().size() == 1) { // output
					// transition
					// has
					// only
					// one
					// output
					// place
					Place otherPlace = (Place) transition.getSuccessors()
							.iterator().next(); // Get output place
					if ((notReducedPlaces.contains(otherPlace) || (!sacroSanctNodes
							.contains(place) && sacroSanctNodes
							.contains(otherPlace)))
							&&
							// if place is sacrosanct, then otherPlace is not
							// allowed to have additional inputs.
							(!sacroSanctNodes.contains(place) || (hasUniquePredecessors(otherPlace) && otherPlace
									.getPredecessors().size() == 1))) {
						Message.add("FSP(" + place.toString() + ","
								+ transition.toString() + ","
								+ otherPlace.toString() + ")", Message.DEBUG);
						if (sacroSanctNodes.contains(place)) {
							// Reduce otherPlace
							notReducedPlaces.remove(otherPlace);
							reducedPlaces.put(otherPlace, place);
						} else {
							// Reduce place
							notReducedPlaces.remove(place);
							reducedPlaces.put(place, otherPlace);
						}
						// Reduce transition to itself (no arcs to transfer)
						notReducedTransitions.remove(transition);
						reducedTransitions.put(transition, transition);
						nothingReduced = false;
					}
				}
			}
		}

		// Rule 2: Fusion of Series Transitions
		it = providedPN.getTransitions().iterator();
		while (it.hasNext()) {
			Transition transition = (Transition) it.next();
			if ((notReducedTransitions.contains(transition) || sacroSanctNodes
					.contains(transition))
					&& // transition not reduced
					hasUniquePredecessors(transition)
					&& transition.getPredecessors().size() == 1) { // transition
				// has only
				// one input
				// place
				Place place = (Place) transition.getPredecessors().iterator()
						.next(); // Get the input place
				if (notReducedPlaces.contains(place)
						&& // output place not reduced
						hasUniquePredecessors(place)
						&& place.getPredecessors().size() == 1
						&& // input place has only one input transition
						hasUniqueSuccessors(place)
						&& place.getSuccessors().size() == 1) { // input place
					// has only one
					// output
					// transition
					Transition otherTransition = (Transition) place
							.getPredecessors().iterator().next(); // Get input
					// transition
					if ((notReducedTransitions.contains(otherTransition) || (!sacroSanctNodes
							.contains(transition) && sacroSanctNodes
							.contains(otherTransition)))
							&& (!sacroSanctNodes.contains(transition) || (hasUniqueSuccessors(otherTransition) && otherTransition
									.getSuccessors().size() == 1))) {
						Message.add("FST(" + transition.toString() + ","
								+ place.toString() + ","
								+ otherTransition.toString() + ")",
								Message.DEBUG);
						if (sacroSanctNodes.contains(transition)) {
							// Reduce otherTransition to transition
							notReducedTransitions.remove(otherTransition);
							reducedTransitions.put(otherTransition, transition);
						} else {
							// Reduce transition to otherTransition
							notReducedTransitions.remove(transition);
							reducedTransitions.put(transition, otherTransition);
						}
						// Reduce place to itself
						notReducedPlaces.remove(place);
						reducedPlaces.put(place, place);
						nothingReduced = false;
					}
				}
			}
		}

		// Rule 3: Fusion of Parallel Places
		// Generalization on input and output transitions
		it = providedPN.getPlaces().iterator();
		while (it.hasNext()) {
			Place place = (Place) it.next();
			if ((notReducedPlaces.contains(place) || sacroSanctNodes
					.contains(place))
					&& // place not reduced
					hasUniqueSuccessors(place)
					&& place.getSuccessors().size() >= 1
					&& // place has at least one output transition
					hasUniquePredecessors(place)
					&& place.getPredecessors().size() >= 1) { // place has at
				// least one
				// input
				// transition
				Transition transition = (Transition) place.getPredecessors()
						.iterator().next(); // Get input transition
				Iterator it2 = transition.getSuccessors().iterator();
				while (it2.hasNext()) {
					Place otherPlace = (Place) it2.next(); // For every output
					// place of the
					// input transition
					if (place != otherPlace && // other place is not place
							(notReducedPlaces.contains(otherPlace) || (!sacroSanctNodes
									.contains(place) && sacroSanctNodes
									.contains(otherPlace))) && // other place
							// not reduced
							otherPlace.getSuccessors().equals(
									place.getSuccessors()) && // other place has
							// same output
							// transitions
							otherPlace.getPredecessors().equals(
									place.getPredecessors())) { // other place
						// has same
						// input
						// transitions
						Message.add("FPP(" + place.toString() + ","
								+ transition.toString() + ","
								+ otherPlace.toString() + ")", Message.DEBUG);
						if (sacroSanctNodes.contains(place)) {
							// Reduce otherPlace to itself
							notReducedPlaces.remove(otherPlace);
							reducedPlaces.put(otherPlace, otherPlace);
							nothingReduced = false;
						} else {
							// Reduce place to itself
							notReducedPlaces.remove(place);
							reducedPlaces.put(place, place);
							nothingReduced = false;
						}
					}
				}
			}
		}

		// Rule 4: Fusion of Parallel Transitions
		// Generalization on input and output places
		it = providedPN.getTransitions().iterator();
		while (it.hasNext()) {
			Transition transition = (Transition) it.next();
			if (notReducedTransitions.contains(transition)
					&& // transition not reduced
					hasUniqueSuccessors(transition)
					&& transition.getSuccessors().size() >= 1
					&& // transition has at least one output place
					hasUniquePredecessors(transition)
					&& transition.getPredecessors().size() >= 1) { // transition
				// has at
				// least one
				// input
				// place
				Place place = (Place) transition.getPredecessors().iterator()
						.next(); // Get input place
				Place otherPlace = (Place) transition.getSuccessors()
						.iterator().next(); // Get output place
				Iterator it2 = place.getSuccessors().iterator();
				while (it2.hasNext()) {
					Transition otherTransition = (Transition) it2.next(); // For
					// every
					// output
					// transition
					// of
					// the
					// input
					// place
					if (transition != otherTransition && // other transition is
							// not transition
							!sacroSanctNodes.contains(otherTransition) && // other
							// transition
							// is
							// not
							// sacro
							// sanct
							otherTransition.getSuccessors().equals(
									transition.getSuccessors()) && // other
							// transition
							// also has
							// only one
							// output
							// place
							otherTransition.getPredecessors().equals(
									transition.getPredecessors())) { // other
						// transition
						// also
						// has
						// only
						// one
						// input
						// place
						Message.add("FPT(" + transition.toString() + ","
								+ place.toString() + ","
								+ otherTransition.toString() + ")",
								Message.DEBUG);
						// Reduce other transition to itself
						notReducedTransitions.remove(otherTransition);
						reducedTransitions
								.put(otherTransition, otherTransition);
						nothingReduced = false;
					}
				}
			}
		}

		// Rule 5: Elimination of Self-loop Places
		it = providedPN.getPlaces().iterator();
		while (it.hasNext()) {
			Place place = (Place) it.next();
			if (notReducedPlaces.contains(place)
					&& // place not reduced
					hasUniqueSuccessors(place)
					&& place.getSuccessors().size() == 1
					&& // place has only one output transition
					hasUniquePredecessors(place)
					&& place.getPredecessors().size() == 1 && // place has only
					// one input
					// transition
					place.getNumberOfTokens() > 0) { // place contains tokens
				if (place.getSuccessors().iterator().next() == place
						.getPredecessors().iterator().next()) { // input
					// transition
					// equals output
					// transition
					// Reduce place onto itself
					Message.add("ELP(" + place.toString() + ")", Message.DEBUG);
					notReducedPlaces.remove(place);
					reducedPlaces.put(place, place);
					nothingReduced = false;
				}
			}
		}

		// Rule 6: Elimination of Self-loop Transitions
		it = providedPN.getTransitions().iterator();
		while (it.hasNext()) {
			Transition transition = (Transition) it.next();
			if (notReducedTransitions.contains(transition)
					&& // transition not reduced
					hasUniqueSuccessors(transition)
					&& transition.getSuccessors().size() == 1
					&& // transition has only one output place
					hasUniquePredecessors(transition)
					&& transition.getPredecessors().size() == 1) { // transition
				// has only
				// one input
				// place
				if (transition.getSuccessors().iterator().next() == transition
						.getPredecessors().iterator().next()) { // input place
					// equals output
					// place
					// Reduce transition onto itself
					Message.add("ELT(" + transition.toString() + ")",
							Message.DEBUG);
					notReducedTransitions.remove(transition);
					reducedTransitions.put(transition, transition);
					nothingReduced = false;
				}
			}
		}

		if (nothingReduced) {
			return providedPN;
		}

		// Add places
		it = notReducedPlaces.iterator();
		while (it.hasNext()) {
			Place place = (Place) it.next();
			Place mappedPlace = reducedPN.addPlace(place.getIdentifier());
			for (int i = 0; i < place.getNumberOfTokens(); i++) {
				mappedPlace.addToken(new Token());
			}
			places.put(place, mappedPlace);
			pnMap.put(place, mappedPlace);
		}

		// Add transitions
		it = notReducedTransitions.iterator();
		while (it.hasNext()) {
			Transition transition = (Transition) it.next();
			Transition mappedTransition = reducedPN
					.addAndLinkTransition(new Transition(transition));
			transitions.put(transition, mappedTransition);
			pnMap.put(transition, mappedTransition);
		}

		// Add edges
		it = providedPN.getPlaces().iterator();
		while (it.hasNext()) {
			Place place = (Place) it.next();
			if (place.getOutEdges() != null) {
				Iterator it2 = place.getOutEdges().iterator();
				while (it2.hasNext()) {
					ModelGraphEdge edge = (ModelGraphEdge) it2.next();
					Transition transition = (Transition) edge.getDest();
					// Transfer edge as much as possible
					Place mappedPlace;
					if (reducedPlaces.containsKey(place)) {
						mappedPlace = (Place) reducedPlaces.get(place);
						while (reducedPlaces.containsKey(mappedPlace)
								&& (mappedPlace != reducedPlaces
										.get(mappedPlace))) {
							mappedPlace = (Place) reducedPlaces
									.get(mappedPlace);
						}
					} else {
						mappedPlace = place;
					}
					Transition mappedTransition;
					if (reducedTransitions.containsKey(transition)) {
						mappedTransition = (Transition) reducedTransitions
								.get(transition);
						while (reducedTransitions.containsKey(mappedTransition)
								&& (mappedTransition != reducedTransitions
										.get(mappedTransition))) {
							mappedTransition = (Transition) reducedTransitions
									.get(mappedTransition);
						}
					} else {
						mappedTransition = transition;
					}
					Message.add("Transition " + transition.getIdentifier()
							+ "->" + mappedTransition.getIdentifier(),
							Message.DEBUG);
					// Add transferred edge, if needed
					if ((!reducedPlaces.containsKey(mappedPlace) || mappedPlace != reducedPlaces
							.get(mappedPlace))
							&& (!reducedTransitions
									.containsKey(mappedTransition) || mappedTransition != reducedTransitions
									.get(mappedTransition))) {
						reducedPN.addEdge((Place) places.get(mappedPlace),
								(Transition) transitions.get(mappedTransition));
					}
				}
			}
			if (place.getInEdges() != null) {
				Iterator it2 = place.getInEdges().iterator();
				while (it2.hasNext()) {
					ModelGraphEdge edge = (ModelGraphEdge) it2.next();
					Transition transition = (Transition) edge.getSource();
					// Transfer edge as much as possible
					Place mappedPlace;
					if (reducedPlaces.containsKey(place)) {
						mappedPlace = (Place) reducedPlaces.get(place);
						while (reducedPlaces.containsKey(mappedPlace)
								&& (mappedPlace != reducedPlaces
										.get(mappedPlace))) {
							mappedPlace = (Place) reducedPlaces
									.get(mappedPlace);
						}
					} else {
						mappedPlace = place;
					}
					Transition mappedTransition;
					if (reducedTransitions.containsKey(transition)) {
						mappedTransition = (Transition) reducedTransitions
								.get(transition);
						while (reducedTransitions.containsKey(mappedTransition)
								&& (mappedTransition != reducedTransitions
										.get(mappedTransition))) {
							mappedTransition = (Transition) reducedTransitions
									.get(mappedTransition);
						}
					} else {
						mappedTransition = transition;
					}
					// Add transferred edge, if needed
					if ((!reducedPlaces.containsKey(mappedPlace) || mappedPlace != reducedPlaces
							.get(mappedPlace))
							&& (!reducedTransitions
									.containsKey(mappedTransition) || mappedTransition != reducedTransitions
									.get(mappedTransition))) {
						reducedPN.addEdge((Transition) transitions
								.get(mappedTransition), (Place) places
								.get(mappedPlace));
					}
				}
			}
		}

		it = providedPN.getPlaces().iterator();
		while (it.hasNext()) {
			Place place = (Place) it.next();
			if (sacroSanctNodes.contains(place)) {
				mappedSacroSanctNodes.add(places.get(place));
			}
		}
		it = providedPN.getTransitions().iterator();
		while (it.hasNext()) {
			Transition transition = (Transition) it.next();
			if (sacroSanctNodes.contains(transition)) {
				mappedSacroSanctNodes.add(transitions.get(transition));
			}
		}
		sacroSanctNodes = mappedSacroSanctNodes;

		// Return the reduced PN
		return reducedPN;
	}

}
