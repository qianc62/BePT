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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.epcpack.EPCConfigurableObject;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEdge;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.models.epcpack.InstanceEPC;
import org.processmining.mining.logabstraction.LogRelations;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class InstanceEPCBuilder extends InstanceModelGraphBuilder {

	public InstanceEPCBuilder(LogRelations relations) {
		super(relations);
	}

	public InstanceEPCBuilder(InstanceModelGraphBuilder imb) {
		super();
		this.mainGraph = imb.mainGraph;
		this.vertices = imb.vertices;
	}

	public ModelGraph build(ProcessInstance instance) {
		return toEPC(super.build(instance));
	}

	private ModelGraph toEPC(ModelGraph graph) {
		InstanceEPC epc = new InstanceEPC(false);
		Iterator i;
		int counter;

		epc.setIdentifier(graph.getIdentifier());

		HashMap mapping = new HashMap();

		// copy nodes as functions
		i = graph.getVerticeList().iterator();
		while (i.hasNext()) {
			ModelGraphVertex v = (ModelGraphVertex) i.next();

			EPCFunction f = epc.addFunction(new EPCFunction(
					((LogEvent) v.object), epc));
			f.object2 = v.object2;
			mapping.put(v, f);
		}

		// copy edges, we don't care about using the proper connectors yet
		i = graph.getVerticeList().iterator();
		counter = 0;
		while (i.hasNext()) {
			ModelGraphVertex v = (ModelGraphVertex) i.next();
			Iterator successors = v.getSuccessors().iterator();

			while (successors.hasNext()) {
				ModelGraphVertex v2 = (ModelGraphVertex) successors.next();

				epc.addEdge((EPCFunction) mapping.get(v), (EPCFunction) mapping
						.get(v2));
			}
		}

		// First, for all sets of outgoing arcs, if the size of this set is more
		// than one,
		// add a connector
		Iterator it = epc.getFunctions().iterator();
		while (it.hasNext()) {
			EPCFunction f = (EPCFunction) it.next();

			if (f.outDegree() > 1) {
				// Introduce a new and-split connector
				EPCConnector c = epc.addConnector(new EPCConnector(
						EPCConnector.AND, epc));

				Iterator it2 = f.getOutEdgesIterator();
				epc.addEdge(f, c);
				while (it2.hasNext()) {
					EPCEdge e = (EPCEdge) it2.next();
					epc.addEdge(c, (EPCConfigurableObject) e.getDest());
					epc.removeEdge(e);
				}
			}
		}

		// Second, for all sets of incoming arcs, if the size of this set is
		// more than one,
		// add a connector
		it = epc.getFunctions().iterator();
		while (it.hasNext()) {
			EPCFunction f = (EPCFunction) it.next();

			if (f.inDegree() > 1) {
				// Introduce a new and-split connector
				EPCConnector c = epc.addConnector(new EPCConnector(
						EPCConnector.AND, epc));

				Iterator it2 = f.getInEdgesIterator();
				epc.addEdge(c, f);
				while (it2.hasNext()) {
					EPCEdge e = (EPCEdge) it2.next();
					epc.addEdge((EPCConfigurableObject) e.getSource(), c);
					epc.removeEdge(e);
				}
			}
		}

		// Now create an event for each of the functions
		it = epc.getFunctions().iterator();
		while (it.hasNext()) {
			EPCFunction f = (EPCFunction) it.next();

			if (f.inDegree() == 0) {
				continue;
			}

			EPCEvent e = epc.addEvent(new EPCEvent("status changed to "
					+ f.getIdentifier(), epc));
			// For this event, add an arc to the function,
			// copy the incoming arc of the function and
			// remove original incoming arc

			// find the predecessor of f
			EPCObject o = (EPCObject) f.getPredecessors().iterator().next();
			epc.addEdge(e, f);
			epc.delEdge(o, f);
			epc.addEdge(o, e);
		}

		// Now, add a start event
		EPCEvent e = epc.addEvent(new EPCEvent("start instance", epc));
		it = epc.getFunctions().iterator();
		while (it.hasNext()) {
			EPCFunction f = (EPCFunction) it.next();

			if (f.inDegree() == 0) {
				epc.addEdge(e, f);
			}
		}
		// Check whether an extra connector is needed
		if (e.outDegree() > 1) {
			EPCConnector c = epc.addConnector(new EPCConnector(
					EPCConnector.AND, epc));
			Iterator it2 = e.getSuccessors().iterator();

			while (it2.hasNext()) {
				EPCObject f = (EPCObject) it2.next();

				epc.addEdge(c, f);
				epc.delEdge(e, f);
			}
			epc.addEdge(e, c);
		}

		// Now, add a end event
		e = epc.addEvent(new EPCEvent("complete instance", epc));
		it = epc.getFunctions().iterator();
		while (it.hasNext()) {
			EPCFunction f = (EPCFunction) it.next();

			if (f.outDegree() == 0) {
				epc.addEdge(f, e);
			}
		}
		// Check whether an extra connector is needed
		if (e.inDegree() > 1) {
			EPCConnector c = epc.addConnector(new EPCConnector(
					EPCConnector.AND, epc));
			Iterator it2 = e.getPredecessors().iterator();

			while (it2.hasNext()) {
				EPCObject f = (EPCObject) it2.next();
				epc.addEdge(f, c);
				epc.delEdge(f, e);
			}
			epc.addEdge(c, e);
		}

		// In the final step, we walk through all pairs of connectors.
		// If two connectors are of the same type (i.e. join/split) and they
		// have
		// the same input (output respectively) they can be replaced by two
		// other connectors.
		it = epc.getConnectors().iterator();
		while (it.hasNext()) {
			EPCConnector c1 = (EPCConnector) it.next();
			Iterator it2 = epc.getConnectors().iterator();

			while (it2.hasNext()) {
				EPCConnector c2 = (EPCConnector) it2.next();
				if (c1 == c2) {
					continue;
				}

				// Check whether c1 and c2 are of the same type
				if (c1.getType() != c2.getType()) {
					continue;
				}

				if (c1.getPredecessors().equals(c2.getPredecessors())
						&& (c1.inDegree() > 1)) {
					// OK, c1 and c2 have the same set of inputs.
					// Note that they both have exactly one output, and that
					// these outputs are
					// not the same.
					EPCObject o1 = (EPCObject) c1.getSuccessors().iterator()
							.next();
					EPCObject o2 = (EPCObject) c2.getSuccessors().iterator()
							.next();

					epc.delEdge(c1, o1);
					epc.removeEdges(c2.getIncidentEdges());
					epc.addEdge(c1, c2);
					epc.addEdge(c2, o1);
					epc.addEdge(c2, o2);
					c2.setType(EPCConnector.XOR);
				}
				if (c1.getSuccessors().equals(c2.getSuccessors())
						&& (c1.outDegree() > 1)) {
					// OK, c1 and c2 have the same set of outputs.
					// Note that they both have exactly one input, and that
					// these inputs are
					// not the same.
					EPCObject o1 = (EPCObject) c1.getPredecessors().iterator()
							.next();
					EPCObject o2 = (EPCObject) c2.getPredecessors().iterator()
							.next();

					epc.delEdge(o1, c1);
					epc.removeEdges(c2.getIncidentEdges());
					epc.addEdge(c2, c1);
					epc.addEdge(o1, c2);
					epc.addEdge(o2, c2);
					c2.setType(EPCConnector.XOR);
				}
			}
		}

		ArrayList remove = new ArrayList();
		// We only have to check whether there are obsolete connectors:
		it = epc.getConnectors().iterator();
		while (it.hasNext()) {
			EPCConnector c1 = (EPCConnector) it.next();

			if ((c1.inDegree() > 1) || (c1.outDegree() > 1)) {
				continue;
			}
			epc.addEdge((EPCObject) c1.getPredecessors().iterator().next(),
					(EPCObject) c1.getSuccessors().iterator().next());
			remove.add(c1);
		}

		it = remove.iterator();
		while (it.hasNext()) {
			epc.delConnector((EPCConnector) it.next());
		}

		/**/
		return epc;
	}
}
