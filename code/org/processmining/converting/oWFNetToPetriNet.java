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
import java.util.Iterator;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.TransitionCluster;
import org.processmining.framework.models.petrinet.oWFNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * <p>
 * Title: oWFNetToPetriNet
 * </p>
 * 
 * <p>
 * Description: Convert an open WF net into a Petri net by turning the
 * communication places into regular places.
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
 * @author Eric Verbeek
 * @version 1.0
 */
public class oWFNetToPetriNet implements ConvertingPlugin {

	public oWFNetToPetriNet() {
	}

	public String getName() {
		return "oWF net to Petri net";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:owfn2pn";
	}

	public MiningResult convert(ProvidedObject object) {
		oWFNet providedPN = null;
		LogReader log = null;

		for (int i = 0; i < object.getObjects().length; i++) {
			if (providedPN == null && object.getObjects()[i] instanceof oWFNet) {
				providedPN = (oWFNet) object.getObjects()[i];
			}
			if (log == null && object.getObjects()[i] instanceof LogReader) {
				log = (LogReader) object.getObjects()[i];
			}
		}

		if (providedPN == null) {
			return null;
		}

		PetriNet pn = convert(providedPN);
		pn.Test("oWFNetToPetriNet");

		return new PetriNetResult(log, pn);
	}

	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof oWFNet) {
				return true;
			}
		}
		return false;
	}

	public PetriNet convert(oWFNet source) {
		PetriNet target = new PetriNet();

		Iterator it = source.getTransitions().iterator();
		HashMap<Transition, Transition> mapping = new HashMap<Transition, Transition>();
		while (it.hasNext()) {
			Transition transition = (Transition) it.next();
			Transition clonedTransition = (Transition) transition.clone();
			target.addAndLinkTransition(clonedTransition);
			mapping.put(transition, clonedTransition);
		}

		it = source.getPlaces().iterator();
		while (it.hasNext()) {
			Place place = (Place) it.next();
			Place clonedPlace = (Place) place.clone();
			target.addAndLinkPlace(clonedPlace);
		}
		// Turn communication places into regular ones.
		for (String name : source.getInputs().keySet()) {
			Place place = new Place(name, target);
			target.addAndLinkPlace(place);
		}
		for (String name : source.getOutputs().keySet()) {
			Place place = new Place(name, target);
			target.addAndLinkPlace(place);
		}

		it = source.getEdges().iterator();
		while (it.hasNext()) {
			PNEdge edge = (PNEdge) it.next();
			PNEdge clonedEdge = (PNEdge) edge.clone();
			if (edge.isPT()) {
				Place p = (Place) edge.getSource();
				Place myPlace = (Place) target.findPlace(p.getIdentifier());
				Transition t = (Transition) edge.getDest();
				Transition myTransition = (Transition) mapping.get(t);
				target.addAndLinkEdge(clonedEdge, myPlace, myTransition);
			} else {
				Place p = (Place) edge.getDest();
				Place myPlace = (Place) target.findPlace(p.getIdentifier());
				Transition t = (Transition) edge.getSource();
				Transition myTransition = (Transition) mapping.get(t);
				target.addAndLinkEdge(clonedEdge, myTransition, myPlace);
			}
		}
		// Added edges for communication places.
		for (String name : source.getInputs().keySet()) {
			Place place = target.findPlace(name);
			for (Transition transition : source.getInputs().get(name)) {
				PNEdge edge = new PNEdge(place, mapping.get(transition));
				target.addAndLinkEdge(edge, place, mapping.get(transition));
			}
		}
		for (String name : source.getOutputs().keySet()) {
			Place place = target.findPlace(name);
			for (Transition transition : source.getOutputs().get(name)) {
				PNEdge edge = new PNEdge(mapping.get(transition), place);
				target.addAndLinkEdge(edge, mapping.get(transition), place);
			}
		}

		it = source.getClusters().iterator();
		while (it.hasNext()) {
			TransitionCluster cluster = (TransitionCluster) it.next();
			TransitionCluster clonedCluster = (TransitionCluster) cluster
					.clone();
			target.addCluster(clonedCluster);
		}

		return target;
	}
}
