package com.iise.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.petrinet.PNEdge;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Transition;
import org.jbpt.petri.Place;
import org.jbpt.petri.Node;
import org.jbpt.petri.Flow;
import org.jbpt.petri.unfolding.ProperCompletePrefixUnfolding;
import org.jbpt.petri.unfolding.Event;
import org.jbpt.petri.unfolding.Condition;
import org.jbpt.hypergraph.abs.Vertex;

public class PetriNetConversion {

	@SuppressWarnings("unchecked")
	public static org.jbpt.petri.NetSystem convert(
			org.processmining.framework.models.petrinet.PetriNet _pn) {
		org.jbpt.petri.NetSystem ns = new org.jbpt.petri.NetSystem();
		Map<org.processmining.framework.models.ModelGraphVertex, org.jbpt.petri.Node> nodeMap = new HashMap<org.processmining.framework.models.ModelGraphVertex, org.jbpt.petri.Node>();
		for (org.processmining.framework.models.petrinet.Transition _t : _pn
				.getTransitions()) {
			org.jbpt.petri.Transition t = new org.jbpt.petri.Transition(
					_t.getIdentifier());
			ns.addTransition(t);
			nodeMap.put(_t, t);
		}
		for (org.processmining.framework.models.petrinet.Place _p : _pn
				.getPlaces()) {
			org.jbpt.petri.Place p = new org.jbpt.petri.Place(
					_p.getIdentifier());
			ns.addPlace(p);
			if (_p.getInEdges() == null || _p.getInEdges().isEmpty()) {
				ns.getMarking().put(p, 1);
			}
			nodeMap.put(_p, p);
		}
		ArrayList<org.processmining.framework.models.ModelGraphEdge> _edges = _pn
				.getEdges();
		for (org.processmining.framework.models.ModelGraphEdge _e : _edges) {
			org.processmining.framework.models.ModelGraphVertex _tail = _e
					.getSource();
			org.processmining.framework.models.ModelGraphVertex _head = _e
					.getDest();
			ns.addFlow(nodeMap.get(_tail), nodeMap.get(_head));
		}
		return ns;
	}

	public static PetriNet convertNS2PN (NetSystem ns)
	{
		PetriNet pn = new PetriNet();
		Map<Node, ModelGraphVertex> nodeMap = new HashMap();

		for (Transition _t : ns.getTransitions())
		{
			org.processmining.framework.models.petrinet.Transition t = new org.processmining.framework.models.petrinet.Transition(
					_t.getLabel(), pn);
			t.setLogEvent(new LogEvent(t.getIdentifier(), "auto"));
			pn.addTransition(t);
			nodeMap.put(_t, t);
		}

		for (Place _p : ns.getPlaces()) {
			org.processmining.framework.models.petrinet.Place p = new org.processmining.framework.models.petrinet.Place(
					_p.getLabel(), pn);
			pn.addPlace(p);
			nodeMap.put(_p, p);
		}

		for(Flow flow: ns.getEdges())
		{
			PNEdge edge = null;
			if(flow.getSource() instanceof Place)
				edge = new PNEdge((org.processmining.framework.models.petrinet.Place)nodeMap.get(flow.getSource()), (org.processmining.framework.models.petrinet.Transition)nodeMap.get(flow.getTarget()));
			else
				edge = new PNEdge((org.processmining.framework.models.petrinet.Transition)nodeMap.get(flow.getSource()), (org.processmining.framework.models.petrinet.Place)nodeMap.get(flow.getTarget()));

			pn.addEdge(edge);
		}

		return pn;
	}

	public static NetSystem convertCPU2NS(ProperCompletePrefixUnfolding _cpu)
	{
		org.jbpt.petri.NetSystem ns = new org.jbpt.petri.NetSystem();
		HashMap<Vertex, Node> nodeMap = new HashMap();

		for (Event _e : _cpu.getEvents())
		{
			String tLabel = _e.toString();
			if(_e.getTransition().getName().equals(""))
				tLabel = _e.getTransition().getId() + _e.getLabel();

			Transition t = new Transition(tLabel);
			ns.addTransition(t);
			nodeMap.put(_e, t);
		}

		for (Condition _c : _cpu.getConditions())
		{
			String pLabel = _c.toString();
			if(_c.getPlace().getName().equals(""))
				pLabel = _c.getPlace().getId() + _c.getLabel();

			Place p = new Place(pLabel);
			ns.addPlace(p);
			nodeMap.put(_c, p);
		}

		for (Event _e : _cpu.getEvents())
		{
			for (Condition _preC : _e.getPreConditions())
				ns.addFlow(nodeMap.get(_preC), nodeMap.get(_e));

			for (Condition _posC : _e.getPostConditions())
				ns.addFlow(nodeMap.get(_e), nodeMap.get(_posC));
		}

		//traverse all the cut-off events
		_cpu.getCutoffEvents().stream().forEach(cut ->
		{
//			System.out.print(cut.getLabel());
//			System.out.print(" --> ");
//			Event corr = _cpu.getCorrespondingEvent(cut);
//			System.out.println(corr.getLabel());

			//get cut-off conditions and link to corr conditions
			HashMap<Node, Node> edgeMap = new HashMap();
			cut.getLocalConfiguration().getCut().stream().forEach(cutCond ->
			{
				//get the corr condition of one cut-off condition
				Condition corrCond = cutCond.getCorrespondingCondition();
//				System.out.print(cutCond.getLabel());
//				System.out.print("'s corresponding condition is: ");
//				System.out.println(corrCond.getLabel());

				//record the output of a cutoff event to its corr condition
				if(cutCond.isCutoffPost())
					edgeMap.put(nodeMap.get(cutCond), nodeMap.get(corrCond));
			});

			//change the outputs of the cutoff event
			edgeMap.keySet().stream().forEach(cutCond ->
			{
				//get the only predecessor of cutCond in CPU
				Transition transInput = ns.getPreset((Place)cutCond).iterator().next();
				ns.removeNode(cutCond);
				//not assuming that cutCond must be one of the outputs of cut
				ns.addFlow(transInput, edgeMap.get(cutCond));
			});
		});

		return ns;
	}

	public static org.processmining.framework.models.petrinet.PetriNet convert(
			org.jbpt.petri.unfolding.ProperCompletePrefixUnfolding _cpu) {
		org.processmining.framework.models.petrinet.PetriNet pn = new org.processmining.framework.models.petrinet.PetriNet();
		Map<org.jbpt.hypergraph.abs.Vertex, org.processmining.framework.models.ModelGraphVertex> nodeMap = new HashMap<org.jbpt.hypergraph.abs.Vertex, org.processmining.framework.models.ModelGraphVertex>();
		for (org.jbpt.petri.unfolding.Event _e : _cpu.getEvents()) {
			org.processmining.framework.models.petrinet.Transition t = new org.processmining.framework.models.petrinet.Transition(
					_e.getLabel(), pn);
			t.setLogEvent(new LogEvent(t.getIdentifier(), "auto"));
			pn.addTransition(t);
			nodeMap.put(_e, t);
		}
		for (org.jbpt.petri.unfolding.Condition _c : _cpu.getConditions()) {
			org.processmining.framework.models.petrinet.Place p = new org.processmining.framework.models.petrinet.Place(
					_c.getLabel(), pn);
			pn.addPlace(p);
			nodeMap.put(_c, p);
		}
		for (org.jbpt.petri.unfolding.Event _e : _cpu.getEvents()) {
			for (org.jbpt.petri.unfolding.Condition _preC : _e
					.getPreConditions()) {
				org.processmining.framework.models.petrinet.PNEdge e = new org.processmining.framework.models.petrinet.PNEdge(
						(org.processmining.framework.models.petrinet.Place) nodeMap
								.get(_preC),
						(org.processmining.framework.models.petrinet.Transition) nodeMap
								.get(_e));
				pn.addEdge(e);
			}
		}
		for (org.jbpt.petri.unfolding.Condition _c : _cpu.getConditions()) {
			org.jbpt.petri.unfolding.Event _preE = _c.getPreEvent();
			if (_preE != null) {
				org.processmining.framework.models.petrinet.PNEdge e = new org.processmining.framework.models.petrinet.PNEdge(
						(org.processmining.framework.models.petrinet.Transition) nodeMap
								.get(_preE),
						(org.processmining.framework.models.petrinet.Place) nodeMap
								.get(_c));
				pn.addEdge(e);
			}
		}
		return pn;
	}

}
