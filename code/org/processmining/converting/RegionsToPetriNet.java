package org.processmining.converting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.Region;
import org.processmining.framework.models.RegionList;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.fsm.FSM;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
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
public class RegionsToPetriNet implements ConvertingPlugin {
	public RegionsToPetriNet() {
	}

	public String getName() {
		return "Regions to Petri net";
	}

	public String getHtmlDescription() {
		return "This plugin translates a statespace with a set of regions into a Petri net";
	}

	public MiningResult convert(ProvidedObject original) {
		Object[] o = original.getObjects();

		FSM statespace = null;
		RegionList regions = null;
		LogReader log = null;
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof FSM) {
				statespace = (FSM) o[i];
			}
			if (o[i] instanceof RegionList) {
				regions = (RegionList) o[i];
			}
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
			}

		}

		PetriNet net = new PetriNet();

		HashMap reg2place = new HashMap();

		Iterator<Region> it = regions.iterator();
		while (it.hasNext()) {
			Region region = it.next();
			Place p = net.addPlace(new Place(region.toString(), net));
			reg2place.put(region, p);
		}

		it = statespace.getEdgeObjects().iterator();
		while (it.hasNext()) {
			Object obj = (Object) it.next();
			Transition t;
			if (obj instanceof Transition) {
				t = net.addAndLinkTransition(new Transition((Transition) obj));
			} else if (obj instanceof LogEvent) {
				t = net
						.addAndLinkTransition(new Transition((LogEvent) obj,
								net));
			} else {
				t = net.addTransition(new Transition(obj.toString(), net));
			}
			ArrayList preReg = statespace.getPreRegions(regions, obj);
			Iterator it2 = preReg.iterator();
			while (it2.hasNext()) {
				HashSet reg = (HashSet) it2.next();
				Place p = (Place) reg2place.get(reg);
				if (p != null) {
					net.addEdge(p, t);
				}
			}
			ArrayList postReg = statespace.getPostRegions(regions, obj);
			it2 = postReg.iterator();
			while (it2.hasNext()) {
				HashSet reg = (HashSet) it2.next();
				Place p = (Place) reg2place.get(reg);
				if (p != null) {
					net.addEdge(t, p);
				}
			}
		}

		net.Test("RegionsConvertedTo");

		return new PetriNetResult(log, net);

	}

	public boolean accepts(ProvidedObject original) {
		Object[] o = original.getObjects();

		boolean statespace = false;
		boolean regions = false;
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof FSM) {
				statespace |= true;
			}
			if (o[i] instanceof RegionList) {
				regions |= true;
			}

		}
		return statespace && regions;
	}
}
