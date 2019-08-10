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

package org.processmining.framework.models.epcpack.algorithms;

import java.util.*;

import org.processmining.framework.models.*;
import org.processmining.framework.models.epcpack.*;
import org.processmining.framework.models.petrinet.*;
import att.grappa.Edge;

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
public class EPCToPetriNetConverter {

	/**
	 * This static method converts an EPC into a Petri net. It takes two
	 * parameters, namely the EPC and a HashMap. The HashMap connectorMapping
	 * will be filled with a mapping from connectors of the EPC to transitions
	 * belonging to that connector in the PetriNet. The transitions are stored
	 * in an ArrayList
	 * 
	 * @param baseEPC
	 *            EPC
	 * @param connectorMapping
	 *            HashMap
	 * @return PetriNet
	 */
	public static PetriNet convert(ConfigurableEPC baseEPC,
			HashMap connectorMapping) {
		return convert(baseEPC, connectorMapping, null, null);
	}

	// HV: Extend this method with two mappings: one from functions to
	// transitions and one from xor connectors to places.
	// HV: Both may be null.
	public static PetriNet convert(ConfigurableEPC baseEPC,
			HashMap connectorMapping,
			HashMap<EPCFunction, Transition> functionMapping,
			HashMap<EPCConnector, Place> xorMapping) {
		PetriNet petrinet = new PetriNet();
		HashMap<Long, Object> inmapping = new HashMap();
		HashMap<Long, Object> outmapping = new HashMap();
		int counter = 0;
		int i;

		// HV 08-01024: Places get merged somewhere late in the conversion. As a
		// result, places mapped onto might need updating.
		// Hence, we need a backwards mapping.
		HashMap<Place, EPCConnector> xorBackMapping = new HashMap<Place, EPCConnector>();

		// First, copy all functions as transitions
		Iterator fi = baseEPC.getFunctions().iterator();
		while (fi.hasNext()) {
			EPCFunction f = (EPCFunction) fi.next();
			Place p1 = petrinet.addPlace("p" + counter);
			counter++;
			Place p2 = petrinet.addPlace("p" + counter);
			counter++;
			Transition t = petrinet.addTransition(new Transition(f
					.getLogEvent(), petrinet));
			if (t.getLogEvent() == null) {
				t.setIdentifier(f.getIdentifier());
			}
			t.object = f;

			// HV: Remember that this function is mapped onto this transition.
			if (functionMapping != null) {
				functionMapping.put(f, t);
			}

			petrinet.addEdge(p1, t);
			petrinet.addEdge(t, p2);

			if (f.inDegree() > 0) {
				outmapping
						.put(getInEdgeObjectIterator(f).next().getIdKey(), p1);
			}
			if (f.outDegree() > 0) {
				inmapping
						.put(getOutEdgeObjectIterator(f).next().getIdKey(), p2);
			}
		}

		// Then, copy all events as places
		Iterator ei = baseEPC.getEvents().iterator();
		while (ei.hasNext()) {
			EPCEvent e = (EPCEvent) ei.next();
			Place p = petrinet.addPlace("p" + counter);
			counter++;
			p.object = e;

			if (e.inDegree() > 0) {
				outmapping.put(getInEdgeObjectIterator(e).next().getIdKey(), p);
			}
			if (e.outDegree() > 0) {
				inmapping.put(getOutEdgeObjectIterator(e).next().getIdKey(), p);
			}
		}

		// Now, convert all connectors into invisible transitions with leading
		// and trailing places. Later, these places will be connected with
		// transitions, or fused with other places
		Iterator ci = baseEPC.getConnectors().iterator();
		while (ci.hasNext()) {
			EPCConnector c = (EPCConnector) ci.next();
			ArrayList transitions = new ArrayList();
			// HV 08-01-22 Maps c to an empty list of transitions? What's the
			// use?
			// THis is not mapped onto an empty list. The list is filled
			// afterwards.
			connectorMapping.put(c, transitions);

			if (c.getType() == EPCConnector.AND) {
				// generate one transition for the split or join
				if ((c.inDegree() == 1)) {
					Place inPlace = petrinet.addPlace("p" + counter);
					outmapping.put(
							getInEdgeObjectIterator(c).next().getIdKey(),
							inPlace);

					// Connectors are mapped onto lists of transitions. Please
					// check code before changing
					// it! This was essential for EPC verification!
					// connectorMapping.put(c, inPlace); //HV 08-01-22

					counter++;
					Transition t = petrinet.addTransition(new Transition("t"
							+ counter, petrinet));
					t.object = c;
					counter++;
					transitions.add(t);
					petrinet.addEdge(inPlace, t);
					Iterator<Edge> it = getOutEdgeObjectIterator(c);
					while (it.hasNext()) {
						Place outPlace = petrinet.addPlace("p" + counter);
						counter++;
						petrinet.addEdge(t, outPlace);
						inmapping.put(it.next().getIdKey(), outPlace);
					}
				}
				if ((c.outDegree() == 1)) {
					Place outPlace = petrinet.addPlace("p" + counter);
					counter++;
					inmapping.put(
							getOutEdgeObjectIterator(c).next().getIdKey(),
							outPlace);

					// Connectors are mapped onto lists of transitions. Please
					// check code before changing
					// it! This was essential for EPC verification!
					// connectorMapping.put(c, outPlace); //HV 08-01-22

					Transition t = petrinet.addTransition(new Transition("t"
							+ counter, petrinet));
					counter++;
					transitions.add(t);
					t.object = c;
					petrinet.addEdge(t, outPlace);
					Iterator<Edge> it = getInEdgeObjectIterator(c);
					while (it.hasNext()) {
						Place inPlace = petrinet.addPlace("p" + counter);
						counter++;
						petrinet.addEdge(inPlace, t);
						outmapping.put(it.next().getIdKey(), inPlace);
					}
				}
			}
			if (c.getType() == EPCConnector.XOR) {
				// generate many transitions for the split or join
				if ((c.inDegree() == 1)) {
					Place inPlace = petrinet.addPlace("p" + counter);
					outmapping.put(
							getInEdgeObjectIterator(c).next().getIdKey(),
							inPlace);
					// Connectors are mapped onto lists of transitions. Please
					// check code before changing
					// it! This was essential for EPC verification!
					// connectorMapping.put(c, inPlace); //HV 08-01-22

					// HV: Remember that this xor connector is mapped onto this
					// place and v.v.
					if (xorMapping != null) {
						xorMapping.put(c, inPlace);
						xorBackMapping.put(inPlace, c);
					}

					counter++;
					Iterator<Edge> it = getOutEdgeObjectIterator(c);
					while (it.hasNext()) {
						Transition t = petrinet.addTransition(new Transition(
								"t" + counter, petrinet));
						transitions.add(t);
						counter++;
						t.object = c;

						petrinet.addEdge(inPlace, t);
						Place outPlace = petrinet.addPlace("p" + counter);
						counter++;
						petrinet.addEdge(t, outPlace);
						inmapping.put(it.next().getIdKey(), outPlace);
					}
				}
				if ((c.outDegree() == 1)) {
					Place outPlace = petrinet.addPlace("p" + counter);
					counter++;
					inmapping.put(
							getOutEdgeObjectIterator(c).next().getIdKey(),
							outPlace);
					// Connectors are mapped onto lists of transitions. Please
					// check code before changing
					// it! This was essential for EPC verification!
					// connectorMapping.put(c, outPlace);//HV 08-01-22

					// HV: Remember that this xor connector is mapped onto this
					// place and v.v.
					if (xorMapping != null) {
						xorMapping.put(c, outPlace);
						xorBackMapping.put(outPlace, c);
					}

					Iterator<Edge> it = getInEdgeObjectIterator(c);
					while (it.hasNext()) {
						Transition t = petrinet.addTransition(new Transition(
								"t" + counter, petrinet));
						transitions.add(t);
						counter++;
						t.object = c;
						petrinet.addEdge(t, outPlace);
						Place inPlace = petrinet.addPlace("p" + counter);
						counter++;
						petrinet.addEdge(inPlace, t);
						outmapping.put(it.next().getIdKey(), inPlace);
					}
				}
			}
			if (c.getType() == EPCConnector.OR) {
				// generate many transitions for all possible combinations
				// of the split or joins
				if ((c.inDegree() == 1)) {
					Place inPlace = petrinet.addPlace("p" + counter);
					counter++;
					outmapping.put(
							getInEdgeObjectIterator(c).next().getIdKey(),
							inPlace);
					// Connectors are mapped onto lists of transitions. Please
					// check code before changing
					// it! This was essential for EPC verification!
					// connectorMapping.put(c, inPlace); //HV 08-01-22

					HashMap places = new HashMap();
					Iterator<Edge> it = getOutEdgeObjectIterator(c);
					i = 0;
					while (it.hasNext()) {
						Place p = petrinet.addPlace("p" + counter);
						places.put(new Integer((int) Math.pow(2, i)), p);
						counter++;
						i++;
						inmapping.put(it.next().getIdKey(), p);
					}
					for (i = 1; i < Math.pow(2, c.outDegree()); i++) {
						Transition t = petrinet.addTransition(new Transition(
								"t" + counter, petrinet));
						transitions.add(t);
						t.object = c;
						counter++;
						petrinet.addEdge(inPlace, t);
						int bit = 1;
						while (bit < i + 1) {
							Integer key = new Integer(i & bit);
							if (key.intValue() != 0) {
								petrinet.addEdge(t, (Place) places.get(key));
							}
							bit *= 2;
						}

					}
				}
				if ((c.outDegree() == 1)) {
					Place outPlace = petrinet.addPlace("p" + counter);
					counter++;
					inmapping.put(
							getOutEdgeObjectIterator(c).next().getIdKey(),
							outPlace);
					// Connectors are mapped onto lists of transitions. Please
					// check code before changing
					// it! This was essential for EPC verification!
					// connectorMapping.put(c, outPlace); //HV 08-01-22

					HashMap places = new HashMap();
					Iterator<Edge> it = getInEdgeObjectIterator(c);
					i = 0;
					while (it.hasNext()) {
						Place p = petrinet.addPlace("p" + counter);
						places.put(new Integer((int) Math.pow(2, i)), p);
						counter++;
						i++;
						outmapping.put(it.next().getIdKey(), p);
					}
					for (i = 1; i < Math.pow(2, c.inDegree()); i++) {
						Transition t = petrinet.addTransition(new Transition(
								"t" + counter, petrinet));
						transitions.add(t);
						counter++;
						t.object = c;
						petrinet.addEdge(t, outPlace);
						int bit = 1;
						while (bit < i + 1) {
							Integer key = new Integer(i & bit);
							if (key.intValue() != 0) {
								petrinet.addEdge((Place) places.get(key), t);
							}
							bit *= 2;
						}

					}
				}
			}
		}

		HashMap mapping = new HashMap();
		Iterator<Edge> ied = getEPCEdgeObjectIterator(baseEPC);
		while (ied.hasNext()) {
			Edge e = ied.next();
			Place p1 = (Place) inmapping.get(e.getIdKey());
			Place p2 = (Place) outmapping.get(e.getIdKey());

			if (mapping.containsKey(p1)) {
				p1 = (Place) mapping.get(p1);
			}
			if (mapping.containsKey(p2)) {
				p2 = (Place) mapping.get(p2);
			}

			Place p3;
			if (p2.object instanceof EPCEvent) {
				p3 = petrinet.mergePlaces(p2, p1);
				// Check whether the place removed (p1) was mapped onto. If so,
				// map onto p2 instead.
				if (xorBackMapping.containsKey(p1)) {
					EPCConnector c = xorBackMapping.get(p1);
					xorMapping.remove(c);
					xorBackMapping.remove(p1);
					xorMapping.put(c, p2);
					xorBackMapping.put(p2, c);
				}
			} else {
				p3 = petrinet.mergePlaces(p1, p2);
				// Check whether the place removed (p2) was mapped onto. If so,
				// map onto p1 instead.
				if (xorBackMapping.containsKey(p2)) {
					EPCConnector c = xorBackMapping.get(p2);
					xorMapping.remove(c);
					xorBackMapping.remove(p2);
					xorMapping.put(c, p1);
					xorBackMapping.put(p1, c);
				}
			}
			// The place that is deleted from the net is p3;
			// Therefore, we need to make a mapping from that p3
			// to the place that it is mapped onto
			mapping.put(p3, (p3 == p1 ? p2 : p1));
		}
		return petrinet;
	}

	private static Iterator<Edge> getInEdgeObjectIterator(ModelGraphVertex node) {
		return node.getInEdges().iterator();
	}

	private static Iterator<Edge> getOutEdgeObjectIterator(ModelGraphVertex node) {
		return node.getOutEdges().iterator();
	}

	private static Iterator<Edge> getEPCEdgeObjectIterator(ConfigurableEPC epc) {
		return epc.getEdges().iterator();

	}

}
