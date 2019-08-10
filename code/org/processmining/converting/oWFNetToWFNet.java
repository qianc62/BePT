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
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.TransitionCluster;
import org.processmining.framework.models.petrinet.WFNet;
import org.processmining.framework.models.petrinet.oWFNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * <p>
 * Title: oWFNetToWFNet
 * </p>
 * 
 * <p>
 * Description: Converts an open WF ne tinto a WF net, by leaving out the
 * communication places. Note that according to the Berlin guys an open WF net
 * is allowed to have multiple sink places. However, we assume that an open WF
 * net has only one sink place.
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
public class oWFNetToWFNet implements ConvertingPlugin {

	public oWFNetToWFNet() {
	}

	public String getName() {
		return "oWF net to labeld WF net";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:owfn2wfn";
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

		WFNet pn = convert(providedPN);
		pn.Test("oWFNetToWFNet");

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

	public WFNet convert(oWFNet source) {
		WFNet target = new WFNet();
		if (source.getSourcePlace() != null) {
			Place sourcePlace = new Place(source.getSourcePlace().getName(),
					target);
			target.setSourcePlace(sourcePlace);
		}
		if (source.getSinkPlace() != null) {
			Place sinkPlace = new Place(source.getSinkPlace().getName(), target);
			target.setSinkPlace(sinkPlace);
		}

		// Simply copy the Petri net.
		Iterator it = source.getTransitions().iterator();
		HashMap mapping = new HashMap();
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
