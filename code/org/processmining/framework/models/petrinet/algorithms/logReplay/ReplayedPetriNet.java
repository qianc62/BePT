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

package org.processmining.framework.models.petrinet.algorithms.logReplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.TransitionCluster;

/**
 * A Petri net involved in the log replay analysis method.
 * 
 * @see ReplayedTransition
 * @see ReplayedPlace
 * @see LogReplayAnalysisMethod
 * 
 * @author arozinat
 */
public class ReplayedPetriNet extends PetriNet {

	/**
	 * This field is used to store the number of invisible tasks for performance
	 * reasons, since the conformance checker asks for whether there are any
	 * invisible tasks in the model after every replay step.
	 */
	// private int invisibleTasks;
	private ArrayList invisibleTasks;

	/**
	 * Constructs a ReplayedPetriNet out of an ordinary one. Initializes the
	 * diagnostic data structures for the given caseIDs.
	 * 
	 * @param net
	 *            the Petri net that is re-established in the replayed net
	 * @param caseIDs
	 *            a list of strings containing the IDs of those instances that
	 *            want to store diagnostic information (can be <case>null</case>
	 *            if not needed)
	 */
	public ReplayedPetriNet(PetriNet net, ArrayList caseIDs) {
		// establish the same petri net structure like in the given model
		reproducePetriNetStructure(net, this, true, caseIDs);
		// store the number of invisible tasks for performance reasons
		// invisibleTasks = getNumberOfInvisibleTasks();
		invisibleTasks = getInvisibleTasks();
	}

	/**
	 * Copy constructor reconstructing the Petri net structure of the given
	 * template object. Needed for subclasses that want to change the type of
	 * visualization.
	 * 
	 * @param copyTemplate
	 *            the ReplayedPetriNet serving as a template for this object
	 */
	public ReplayedPetriNet(ReplayedPetriNet copyTemplate) {
		// establish the same petri net structure like in the template net
		reproducePetriNetStructure(copyTemplate, this, false, null);
		// needs to be extended as soon as there are attributes added to the
		// class..
		// this.numberOfInvisibleTasks = copyTemplate.numberOfInvisibleTasks;
		this.invisibleTasks = this.getInvisibleTasks();
	}

	/**
	 * Re-establishes the structure of the template Petri net for the given
	 * target Petri net. Eigher constructs a ReplayedPetriNet out of an ordinary
	 * one and the list of case IDs of those process instances, for which
	 * diagnostic information should be recorded, or simply clones the given
	 * template net (depending on the <code>initialize</code> parameter).
	 * 
	 * @see reproduceTransition
	 * @see reproducePlace
	 * @see reproduceEdge
	 * 
	 * @param from
	 *            the Petri net serving as a template
	 * @param to
	 *            the target Petri net
	 * @param initialize
	 *            <code>true</code> if the new ReplayedPetriNet is initially
	 *            built (and therefore diagnostic data structures need to be
	 *            initialized), <code>false</code> if the new ReplayedPetriNet
	 *            should be a clone of the template net
	 * @param caseIDs
	 *            the list of trace IDs for initializing diagnostic data
	 *            structures (only considered if <code>initialize</code> has
	 *            been set <code>true</code>)
	 */
	private void reproducePetriNetStructure(PetriNet from, ReplayedPetriNet to,
			boolean initialize, ArrayList caseIDs) {

		Iterator transitions = from.getTransitions().iterator();
		HashMap mapping = new HashMap();
		while (transitions.hasNext()) {
			Transition transition = (Transition) transitions.next();
			Transition replayedTransition;

			/*
			 * TODO: merge pure copy functionality with the clone method instead
			 * (should happen on the Petri net level already!)
			 */
			if (initialize == true) {
				// reproduce transition (polymorphicallly substituted by
				// subclasses)
				replayedTransition = makeTransition(transition, to, caseIDs);
				// mapping to target net has been established already
				to.addTransition(replayedTransition);
			} else {
				replayedTransition = (Transition) transition.clone();
				// mapping to target net needs to be established
				to.addAndLinkTransition(replayedTransition);
			}
			// keep the mapping until the edges have been established
			mapping.put(transition, replayedTransition);
		}
		Iterator places = from.getPlaces().iterator();
		while (places.hasNext()) {
			Place place = (Place) places.next();
			Place replayedPlace;

			/**
			 * @todo anne: merge pure copy functionality with the clone method
			 *       instead (should happen on the Petri net level already!)
			 */
			if (initialize == true) {
				// reproduce place (polymorphicallly substituted by subclasses)
				replayedPlace = makePlace(place, to, caseIDs);
				// mapping to target net has been established already
				to.addPlace(replayedPlace);
			} else {
				replayedPlace = (Place) place.clone();
				// mapping to target net needs to be established
				to.addAndLinkPlace(replayedPlace);
			}
		}
		Iterator edges = from.getEdges().iterator();
		while (edges.hasNext()) {
			PNEdge edge = (PNEdge) edges.next();
			PNEdge replayedEdge;
			// if place is source
			if (edge.isPT()) {
				Place p = (Place) edge.getSource();
				// find respective place in this net (place names are assumed to
				// be unique)
				Place myPlace = to.findPlace(p.getIdentifier());
				Transition t = (Transition) edge.getDest();
				// find respective transition in this net
				Transition myTransition = (Transition) mapping.get(t);

				/*
				 * TODO: merge pure copy functionality with the clone method
				 * instead (should happen on the Petri net level already!)
				 */
				if (initialize == true) {
					// reproduce edge (polymorphicallly substituted by
					// subclasses)
					replayedEdge = makeEdge(edge, myPlace, myTransition, to,
							caseIDs);
					// connection to source and target node has been established
					// already
					to.addEdge(replayedEdge);
				} else {
					replayedEdge = (PNEdge) edge.clone();
					// connection to source and target node needs to be
					// established
					to.addAndLinkEdge(replayedEdge, myPlace, myTransition);
				}
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

				/*
				 * TODO: merge pure copy functionality with the clone method
				 * instead (should happen on the Petri net level already!)
				 */
				if (initialize == true) {
					// reproduce edge (polymorphicallly substituted by
					// subclasses)
					replayedEdge = makeEdge(edge, myTransition, myPlace, to,
							caseIDs);
					// connection to source and target node has been established
					// already
					to.addEdge(replayedEdge);
				} else {
					replayedEdge = (PNEdge) edge.clone();
					// connection to source and target node needs to be
					// established
					to.addAndLinkEdge(replayedEdge, myTransition, myPlace);
				}
			}
		}
		Iterator clusters = from.getClusters().iterator();
		while (clusters.hasNext()) {
			TransitionCluster currentCluster = (TransitionCluster) clusters
					.next();
			// clusters so far not used to store diagnostic data - if needed,
			// provide and call protected method "makeCluster" like for the rest
			to.addCluster(new TransitionCluster(currentCluster));
		}
	}

	/**
	 * Reproduces a transition based on the given transition. <br>
	 * Should be overridden by subclasses so that they can build up their own
	 * diagnostic data structures instead.
	 * 
	 * @param template
	 *            the Transition serving as a template for the new Transition
	 * @param targetNet
	 *            the ReplayedPetriNet to which the new transition belongs to
	 * @param caseIDs
	 *            the list of trace IDs for initializing diagnostic data
	 *            structures
	 * @return the reproduced Transition
	 */
	protected Transition makeTransition(Transition template,
			ReplayedPetriNet targetNet, ArrayList caseIDs) {

		// note that it is necessary to use this constructor passing the right
		// subgraph
		return new ReplayedTransition(template, targetNet, caseIDs);
	}

	/**
	 * Reproduces a place based on the given place. <br>
	 * Should be overridden by subclasses so that they can build up their own
	 * diagnostic data structures instead.
	 * 
	 * @param template
	 *            the Place serving as a template for the new Place
	 * @param targetNet
	 *            the ReplayedPetriNet to which the new place belongs to
	 * @param caseIDs
	 *            the list of trace IDs for initializing diagnostic data
	 *            structures
	 * @return the reproduced Place
	 */
	protected Place makePlace(Place template, ReplayedPetriNet targetNet,
			ArrayList caseIDs) {

		// note that it is necessary to use this constructor passing the right
		// subgraph
		return new ReplayedPlace(template, targetNet, caseIDs);
	}

	/**
	 * Reproduces an edge based on the given edge (Direction: Place ->
	 * Transition). <br>
	 * Should be overridden by subclasses so that they can build up their own
	 * diagnostic data structures instead.
	 * 
	 * @param template
	 *            the PNEdge serving as a template for the new PNEdge
	 * @param sourceNode
	 *            the Place being the source of the new edge
	 * @param targetNode
	 *            the Transition being the target of the new edge
	 * @param targetNet
	 *            the ReplayedPetriNet to which the new place belongs to
	 * @param caseIDs
	 *            the list of trace IDs for initializing diagnostic data
	 *            structures
	 * @return the reproduced PNEdge
	 */
	protected PNEdge makeEdge(PNEdge template, Place sourceNode,
			Transition targetNode, ReplayedPetriNet targetNet, ArrayList caseIDs) {

		/*
		 * TODO: check whether it makes sense to pass template edge and target
		 * net for the general case
		 */
		return new ReplayedEdge(sourceNode, targetNode, caseIDs);
	}

	/**
	 * Reproduces an edge based on the given edge (Direction: Transition ->
	 * Place). <br>
	 * Should be overridden by subclasses so that they can build up their own
	 * diagnostic data structures instead.
	 * 
	 * @param template
	 *            the PNEdge serving as a template for the new PNEdge
	 * @param sourceNode
	 *            the Transition being the source of the new edge
	 * @param targetNode
	 *            the Place being the target of the new edge
	 * @param targetNet
	 *            the ReplayedPetriNet to which the new place belongs to
	 * @param caseIDs
	 *            the list of trace IDs for initializing diagnostic data
	 *            structures
	 * @return the reproduced PNEdge
	 */
	protected PNEdge makeEdge(PNEdge template, Transition sourceNode,
			Place targetNode, ReplayedPetriNet targetNet, ArrayList caseIDs) {

		/*
		 * TODO: check whether it makes sense to pass template edge and target
		 * net for the general case
		 */
		return new ReplayedEdge(sourceNode, targetNode, caseIDs);
	}

	// /////////////////// MAPPING RELATED METHODS
	// //////////////////////////////

	/**
	 * Determines whether thera are invisible tasks contained in the model,
	 * i.e., those that do not have a corresponding log event.
	 * 
	 * @return <code>true</code> if there are any invisble tasks,
	 *         <code>false</code> otherwise
	 */
	public boolean hasEnabledInvisibleTasks() {
		if (invisibleTasks.size() > 0) {
			Iterator it = invisibleTasks.iterator();
			while (it.hasNext()) {
				Transition trans = (Transition) it.next();
				// if at least one is enabled
				if (trans.isEnabled()) {
					return true;
				}
			}
		}
		return false;
	}
}
