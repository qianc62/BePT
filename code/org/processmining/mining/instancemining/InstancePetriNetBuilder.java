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

package org.processmining.mining.instancemining;

import java.util.HashMap;
import java.util.Iterator;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.mining.logabstraction.LogRelations;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class InstancePetriNetBuilder extends InstanceModelGraphBuilder {

	public InstancePetriNetBuilder(LogRelations relations) {
		super(relations);
	}

	public InstancePetriNetBuilder(InstanceModelGraphBuilder imb) {
		super();
		this.mainGraph = imb.mainGraph;
		this.vertices = imb.vertices;
	}

	public ModelGraph build(ProcessInstance instance) {
		return toPetriNet(super.build(instance));
	}

	private ModelGraph toPetriNet(ModelGraph graph) {
		PetriNet net = new PetriNet();
		Iterator i;
		int counter;

		net.setIdentifier(graph.getIdentifier());

		HashMap mapping = new HashMap();

		// copy nodes as transitions
		i = graph.getVerticeList().iterator();
		while (i.hasNext()) {
			ModelGraphVertex v = (ModelGraphVertex) i.next();

			Transition t = new Transition((LogEvent) v.object, net);
			net.addTransition(t);
			mapping.put(v, t);
		}

		// copy edges, and create a place in between the transitions
		i = graph.getVerticeList().iterator();
		counter = 0;
		while (i.hasNext()) {
			ModelGraphVertex v = (ModelGraphVertex) i.next();
			Iterator successors = v.getSuccessors().iterator();

			while (successors.hasNext()) {
				ModelGraphVertex v2 = (ModelGraphVertex) successors.next();
				Place p = net.addPlace("node" + counter);

				net.addEdge((Transition) mapping.get(v), p);
				net.addEdge(p, (Transition) mapping.get(v2));

				counter++;
			}
		}

		Place start = net.addPlace("node" + counter);
		counter++;
		Place end = net.addPlace("node" + counter);
		counter++;

		i = net.getTransitions().iterator();
		while (i.hasNext()) {
			Transition t = (Transition) i.next();
			if (t.inDegree() == 0) {
				net.addEdge(start, t);
			}
			if (t.outDegree() == 0) {
				net.addEdge(t, end);
			}
		}

		if (start.outDegree() > 1) {
			i = start.getSuccessors().iterator();
			Transition st = net.addTransition(new Transition((LogEvent) null,
					net));
			net.addEdge(start, st);
			while (i.hasNext()) {
				Transition t = (Transition) i.next();
				Place p = net.addPlace("node" + counter);
				counter++;
				net.delEdge(start, t);
				net.addEdge(p, t);
				net.addEdge(st, p);
			}

		}

		if (end.inDegree() > 1) {
			i = end.getPredecessors().iterator();
			Transition st = net.addTransition(new Transition((LogEvent) null,
					net));
			net.addEdge(st, end);
			while (i.hasNext()) {
				Transition t = (Transition) i.next();
				Place p = net.addPlace("node" + counter);
				counter++;
				net.delEdge(t, end);
				net.addEdge(t, p);
				net.addEdge(p, st);
			}

		}

		return net;
	}

}
