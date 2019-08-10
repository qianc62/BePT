package org.processmining.converting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
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
public class ProcessInstanceToPetriNetConverter implements ConvertingPlugin {
	public ProcessInstanceToPetriNetConverter() {
	}

	public String getName() {
		return "Partial order to Petri net";
	}

	public String getHtmlDescription() {
		return "This plugin converts process instances with partial order information to a Petri net";
	}

	public MiningResult convert(ProvidedObject original) {
		int k = 0;
		ProcessInstance pi = null;
		while ((pi == null) && (k < original.getObjects().length)) {
			if ((original.getObjects()[k] instanceof ProcessInstance)
					&& (((ProcessInstance) original.getObjects()[k])
							.getAttributes().containsKey(
									ProcessInstance.ATT_PI_PO) && ((ProcessInstance) original
							.getObjects()[k]).getAttributes().get(
							ProcessInstance.ATT_PI_PO).equals("true"))) {
				pi = (ProcessInstance) original.getObjects()[k];
			}
			k++;
		}

		PetriNet net = new PetriNet();
		Iterator i;
		int counter;

		net.setIdentifier(pi.getName());

		HashMap mapping = new HashMap();

		// copy nodes as transitions
		i = pi.getAuditTrailEntryList().iterator();

		while (i.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) i.next();

			Transition t = new Transition(new LogEvent(ate.getElement(), ate
					.getType()), net);
			net.addTransition(t);
			mapping.put(ate.getAttributes().get(ProcessInstance.ATT_ATE_ID), t);
		}

		// copy edges, and create a place in between the transitions
		i = pi.getAuditTrailEntryList().iterator();
		counter = 0;

		while (i.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) i.next();

			String post = ate.getAttributes().get(ProcessInstance.ATT_ATE_POST);
			if (post == null) {
				post = "";
			}
			StringTokenizer st = new StringTokenizer(post, ",", false);

			while (st.hasMoreTokens()) {
				Transition t = (Transition) mapping.get(st.nextToken());

				Place p = net.addPlace("node" + counter);

				net.addEdge((Transition) mapping.get(ate.getAttributes().get(
						ProcessInstance.ATT_ATE_ID)), p);
				net.addEdge(p, t);

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

		return new PetriNetResult(net);

	}

	public boolean accepts(ProvidedObject original) {
		int i = 0;
		boolean b = false;
		while (!b && (i < original.getObjects().length)) {
			b |= (original.getObjects()[i] instanceof ProcessInstance)
					&& (((ProcessInstance) original.getObjects()[i])
							.getAttributes().containsKey(
									ProcessInstance.ATT_PI_PO) && ((ProcessInstance) original
							.getObjects()[i]).getAttributes().get(
							ProcessInstance.ATT_PI_PO).equals("true"));
			i++;
		}
		return b;
	}
}
