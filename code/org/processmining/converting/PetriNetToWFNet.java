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
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.TransitionCluster;
import org.processmining.framework.models.petrinet.WFNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * <p>
 * Title: PetriNetToWFNet
 * </p>
 * 
 * <p>
 * Description: Converts a PetriNet object into a WFNet object (by specifying
 * the source place and the sink place). Additional source and sink places will
 * be left out.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class PetriNetToWFNet implements ConvertingPlugin {

	/**
	 * The designated source and sink place.
	 */
	private Place sourcePlace = null;
	private Place sinkPlace = null;

	public PetriNetToWFNet() {
	}

	public String getName() {
		return "Petri net to labeled WF net";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:pn2wfn";
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

		WFNet pn = convert(providedPN);
		pn.Test("PetriNetToWFNet");

		return new PetriNetResult(log, pn);
	}

	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof PetriNet) {
				return true;
			}
		}
		return false;
	}

	public WFNet convert(PetriNet source) {
		// Construct the sets of source and sink places.
		HashSet<Place> sourcePlaces = new HashSet<Place>();
		HashSet<Place> sinkPlaces = new HashSet<Place>();
		HashSet<Place> places = new HashSet<Place>(source.getPlaces());
		String prefix = "wf_";

		for (Place place : places) {
			if (place.getPredecessors().isEmpty()) {
				sourcePlaces.add(place);
			}
			if (place.getSuccessors().isEmpty()) {
				sinkPlaces.add(place);
			}
			while (place.getIdentifier().startsWith(prefix)) {
				prefix = prefix + "_";
			}
		}

		// Construct the WF net.
		WFNet target = new WFNet();

		// Copy all transitions.
		Iterator it = source.getTransitions().iterator();
		HashMap mapping = new HashMap();
		while (it.hasNext()) {
			Transition transition = (Transition) it.next();
			Transition clonedTransition = (Transition) transition.clone();
			target.addAndLinkTransition(clonedTransition);
			mapping.put(transition, clonedTransition);
			while (transition.getIdentifier().startsWith(prefix)) {
				prefix = prefix + "_";
			}
		}

		// Copy all places except the non-selected source and sink places.
		it = source.getPlaces().iterator();
		while (it.hasNext()) {
			Place place = (Place) it.next();
			Place clonedPlace = (Place) place.clone();
			target.addAndLinkPlace(clonedPlace);
			mapping.put(place, clonedPlace);
		}

		// Copy the relevant edges.
		it = source.getEdges().iterator();
		while (it.hasNext()) {
			PNEdge edge = (PNEdge) it.next();
			PNEdge clonedEdge = (PNEdge) edge.clone();
			if (edge.isPT()) {
				Place p = (Place) edge.getSource();
				Place myPlace = (Place) mapping.get(p);
				Transition t = (Transition) edge.getDest();
				Transition myTransition = (Transition) mapping.get(t);
				target.addAndLinkEdge(clonedEdge, myPlace, myTransition);
			} else {
				Place p = (Place) edge.getDest();
				Place myPlace = (Place) mapping.get(p);
				Transition t = (Transition) edge.getSource();
				Transition myTransition = (Transition) mapping.get(t);
				target.addAndLinkEdge(clonedEdge, myTransition, myPlace);
			}
		}

		// Make a unique source place and a unique sink place
		if (sourcePlaces.size() == 1) {
			sourcePlace = sourcePlaces.iterator().next();
		} else {
			sourcePlace = new Place(prefix + "i", target);
			target.addPlace(sourcePlace);
		}
		if (sinkPlaces.size() == 1) {
			sinkPlace = sinkPlaces.iterator().next();
		} else {
			sinkPlace = new Place(prefix + "o", target);
			target.addPlace(sinkPlace);
		}
		target.setSourcePlace(sourcePlace);
		target.setSinkPlace(sinkPlace);
		if (sourcePlaces.size() > 1) {
			for (Place place : sourcePlaces) {
				Transition transition = new Transition(prefix + "i_"
						+ place.getIdentifier(), target);
				target.addTransition(transition);
				target.addEdge(sourcePlace, transition);
				target.addEdge(transition, (Place) mapping.get(place));
			}
		}
		if (sinkPlaces.size() > 1) {
			for (Place place : sinkPlaces) {
				Transition transition = new Transition(prefix
						+ place.getIdentifier() + "_o", target);
				target.addTransition(transition);
				target.addEdge((Place) mapping.get(place), transition);
				target.addEdge(transition, sinkPlace);
			}
		}

		// Copy the clusters.
		it = source.getClusters().iterator();
		while (it.hasNext()) {
			TransitionCluster cluster = (TransitionCluster) it.next();
			TransitionCluster clonedCluster = (TransitionCluster) cluster
					.clone();
			target.addCluster(clonedCluster);
		}

		return target;
	}

	public void setSourcePlace(Place place) {
		sourcePlace = place;
	}

	public void setSinkPlace(Place place) {
		sinkPlace = place;
	}
}
