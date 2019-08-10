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
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.mining.logabstraction.LogRelations;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * This class builds an instance net from log relations and one or more process
 * instances.
 * <p>
 * The net is returned as a <code>ModelGraph</code> object. Use the class
 * <code>InstancePetriNetBuilder</code> to obtain the instance net as a
 * <code>PetriNet</code> object, or the class <code>InstanceEPCBuilder</code> to
 * obtain the instance net as an <code>EPC</code> object.
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

public class InstanceModelGraphBuilder implements InstanceNetBuilder {

	protected ModelGraph mainGraph;
	protected ArrayList vertices;

	protected InstanceModelGraphBuilder() {
	}

	public InstanceModelGraphBuilder(LogRelations relations) {
		LogEvents modelElements = relations.getLogEvents();
		DoubleMatrix2D causalRelations = relations.getCausalFollowerMatrix();

		// Building instance nets is done in the following way:
		//
		// 1) Build a causal relations graph for the whole log
		//
		// 2) For each instance, take the part of the subgraph containing
		// exactly the ModelElements of which AuditTrailEntries are in the
		// instance

		mainGraph = new ModelGraph("main Graph");
		vertices = new ArrayList();

		mainGraph.setIdentifier("main_graph");

		for (int i = 0; i < modelElements.size(); i++) {
			ModelGraphVertex v = new ModelGraphVertex(
					modelElements.getEvent(i), mainGraph);

			vertices.add(v);
			mainGraph.addVertex(v);
			v.setIdentifier(modelElements.getEvent(i).getModelElementName()
					+ "\\n" + modelElements.getEvent(i).getEventType());
			if (relations.getOneLengthLoopsInfo().get(i) > 0) {
				mainGraph.addEdge(v, v);
			}
		}
		for (int i = 0; i < modelElements.size(); i++) {
			for (int j = 0; j < modelElements.size(); j++) {
				if (causalRelations.get(i, j) > 0) {
					mainGraph.addEdge((ModelGraphVertex) vertices.get(i),
							(ModelGraphVertex) vertices.get(j));
				}
			}
		}
	}

	public ModelGraph getCompleteGraph() {
		return mainGraph;
	}

	private ModelGraphVertex findVertex(AuditTrailEntry ate) {
		Iterator i = vertices.iterator();

		while (i.hasNext()) {
			ModelGraphVertex v = (ModelGraphVertex) i.next();
			LogEvent lme = (LogEvent) v.object;

			if (lme.equals(ate.getElement(), ate.getType())) {
				return v;
			}
		}
		throw new Error("Implementation error, should never reach this point.");
	}

	public ModelGraph build(ProcessInstance instance) {
		ModelGraph net = new ModelGraph("instance " + instance.getName());
		AuditTrailEntries ates = instance.getAuditTrailEntries();
		ArrayList processInstance = new ArrayList();
		AuditTrailEntry ate;

		net.setIdentifier(instance.getName());

		while (ates.hasNext()) {
			processInstance.add(ates.next());
		}

		for (int k = 0; k < processInstance.size(); k++) {
			ModelGraphVertex v = findVertex((AuditTrailEntry) processInstance
					.get(k));

			ModelGraphVertex w = new ModelGraphVertex(net);
			w.object = v.object;
			w.setValue(v.getValue());
			w.setIdentifier(v.getIdentifier());

			w.object2 = processInstance.get(k);

			net.addVertex(w);

		}

		// Now, in the main graph, find the Transition that
		// belongs to this audittrailentry.
		for (int k = 0; k < processInstance.size(); k++) {
			ModelGraphVertex v = findVertex((AuditTrailEntry) processInstance
					.get(k));
			ModelGraphVertex ev = (ModelGraphVertex) net.getVerticeList()
					.get(k);

			// Find the first predecessor in the log that is a causal
			// predecessor of ev
			// according to the main graph
			int n = k - 1;
			while (n >= 0) {
				ModelGraphVertex v2 = findVertex((AuditTrailEntry) processInstance
						.get(n));

				if (v2.getSuccessors().contains(v)) {
					// Now, in the instance graph, find the Transition that
					// belongs to this function from the main graph.
					ModelGraphVertex ev2 = (ModelGraphVertex) net
							.getVerticeList().get(n);

					// There is a relation: v2 -> v
					// So introduce the relation ev2 -> ev
					net.addEdge(ev2, ev);
					break;
				}
				n--;
			}

			// Find the first successor in the log that is a causal successor of
			// ev
			// according to the main graph
			n = k + 1;
			while (n < processInstance.size()) {
				ModelGraphVertex v2 = findVertex((AuditTrailEntry) processInstance
						.get(n));

				if (v2.getPredecessors().contains(v)) {
					ModelGraphVertex ev2 = (ModelGraphVertex) net
							.getVerticeList().get(n);

					// There is a relation: t -> t2
					// So introduce the relation et -> et2
					net.addEdge(ev, ev2);
					break;
				}
				n++;
			}
		}
		return net;
	}
}
