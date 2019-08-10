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

package org.processmining.framework.models.hlprocess.hlmodel;

import java.util.ArrayList;
import java.util.Set;
import java.util.Map.Entry;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLTypes.Perspective;
import org.processmining.framework.models.hlprocess.visualization.HLPetriNetVisualization;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;

import att.grappa.Edge;

/**
 * High-level process model implementation for Petri nets. <br>
 * Maps Transitions onto HLActivities and Places with more than one outgoing arc
 * to HLChoices.
 */
public class HLPetriNet extends HLModel {

	/**
	 * Creates a high-level process that belongs to the given Petri net process.
	 * 
	 * @param net
	 *            PetriNet the underlying petri net process
	 */
	public HLPetriNet(PetriNet net) {
		super(net);
		initialize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.hlmodel.HLModel#initialize()
	 */
	protected void initialize() {
		super.initialize();
		PetriNet petriNet = (PetriNet) model;
		initActivities(petriNet);
		initChoices(petriNet);
	}

	/*
	 * Maps each transition onto a high-level activity.
	 */
	private void initActivities(PetriNet petriNet) {
		for (Transition trans : petriNet.getTransitions()) {
			HLActivity hlAct = new HLActivity(trans.getIdentifier(), hlProcess);
			vertexToHLActivityMapping.put(trans, hlAct.getID());
		}
	}

	/*
	 * Maps each place with more than one outgoing arc to a high-level choice.
	 */
	private void initChoices(PetriNet petriNet) {
		for (Place place : petriNet.getPlaces()) {
			// find choice constructs in this Petri net and add them to
			// simulation model
			// in Petri net terms a choice is a place with more than one
			// outgoing arcs
			int noBranches = place.outDegree();
			double probabilityPerBranch = 1.0 / noBranches;
			if (noBranches > 1) {
				// create choice construct and add to simulation model
				HLChoice hlChoice = new HLChoice(place.getIdentifier(),
						hlProcess);
				vertexToHLChoiceMapping.put(place, hlChoice.getID());
				// create default dependencies
				// follow outgoing arcs to determine target node of dependency
				for (Edge edge : place.getOutEdges()) {
					Transition destTransition = (Transition) ((PNEdge) edge)
							.getDest();
					// find hl activity for this transition
					for (Entry<ModelGraphVertex, HLID> entry : vertexToHLActivityMapping
							.entrySet()) {
						if (entry.getKey() == destTransition) {
							HLID actID = vertexToHLActivityMapping.get(entry
									.getKey());
							hlChoice.addChoiceTarget(actID);
						}
					}
				}
			}
		}
	}

	/**
	 * Provide a view on all high level activities that are associated to a
	 * visible transition in the underlying Petri net model.
	 */
	public ArrayList<HLActivity> getSelectedActivities() {
		ArrayList<HLActivity> result = new ArrayList<HLActivity>();
		for (ModelGraphVertex vertex : vertexToHLActivityMapping.keySet()) {
			if (((Transition) vertex).isInvisibleTask() == false) {
				HLID actID = vertexToHLActivityMapping.get(vertex);
				result.add(hlProcess.getActivity(actID));
			}
		}
		return result;
	}

	/**
	 * Returns the Petri net model where this high level process refers to.
	 * 
	 * @return the belonging Petri net model
	 */
	public PetriNet getPNModel() {
		return (PetriNet) model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.hlmodel.HLModel#getVisualization
	 * ()
	 */
	public HLPetriNetVisualization getVisualization(
			Set<Perspective> perspectivesToShow) {
		return new HLPetriNetVisualization(this, perspectivesToShow);
	}

	public String toString() {
		return "Petri Net model: " + hlProcess.getGlobalInfo().getName();
	}

}
