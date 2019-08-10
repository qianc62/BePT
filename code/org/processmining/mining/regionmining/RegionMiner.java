package org.processmining.mining.regionmining;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JPanel;

import org.processmining.converting.ConvertingPlugin;
import org.processmining.converting.RegionsToPetriNet;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.Region;
import org.processmining.framework.models.RegionList;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.Token;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.ReachabilityGraphBuilder;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.mining.MiningPlugin;
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
public class RegionMiner implements MiningPlugin {

	public RegionMinerOptionsPanel options;

	public RegionMiner() {
	}

	public String getName() {
		return "Region miner";
	}

	public String getHtmlDescription() {
		return "This plugin uses the theory of regions for mining";
	}

	public JPanel getOptionsPanel(LogSummary summary) {
		options = new RegionMinerOptionsPanel(summary);
		return options;
	}

	public MiningResult mine(final LogReader log) {
		int progmax = log.numberOfInstances() + 3;

		Progress progress = new Progress("Aggregating Regions", 0, progmax);
		progress.inc();

		ProcessInstance pi;
		RegionList regions = new RegionList();
		// initialize with the empty region
		HashSet eventsSoFar = new HashSet();
		regions.add(new Region(new HashSet(), new HashSet()));
		ModelGraph piGraph = new ModelGraph("");
		ModelGraphVertex initialVertex = new ModelGraphVertex("initial",
				piGraph);
		int id = 0;
		Iterator<ProcessInstance> logIt = log.instanceIterator();
		while (logIt.hasNext() && !progress.isCanceled()) {
			pi = logIt.next();
			HashSet eventsCurrentPI = new HashSet();
			piGraph = pi2graph(pi, initialVertex, eventsCurrentPI);
			RegionList piRegions;

			if (options.maxSize() != 0) {
				piRegions = piGraph.calculateRegionsMaxSize(eventsCurrentPI,
						options.maxSize());
			} else if (options.nonCompOnly()) {
				piRegions = piGraph.calculateAllNonCompRegions(eventsCurrentPI);
			} else if (options.minimalOnly()) {
				piRegions = piGraph.calculateMinimalRegions(eventsCurrentPI);
			} else {
				piRegions = piGraph.calculateAllRegions(eventsCurrentPI);
			}

			piRegions.removeComplements();
			RegionList newRegions = new RegionList();
			Iterator<Region> it = regions.iterator();
			while (it.hasNext()) {
				Iterator<Region> it2 = piRegions.iterator();
				Region r1 = it.next();
				while (it2.hasNext()) {
					Region r2 = it2.next();
					if (areCompatibleRegions(r1, eventsSoFar, r2,
							eventsCurrentPI)) { // &&
						// !hasEmptyIntersection(r1,r2))
						// {
						// The two regions are compatible
						Region newR = new Region(r1, r2);
						if (!newRegions.contains(newR)) {
							newRegions.add(newR);
						}
					}
				}
			}
			regions = newRegions;
			// regions.removeComplements();
			eventsSoFar.addAll(eventsCurrentPI);
			progress.setNote(pi.getName());
			progress.inc();
		}
		progress.close();
		regions.retainMinimal();
		progress.setNote("Removing non-minimal regions");
		progress.inc();
		regions.removeEmpty();
		progress.setNote("Removing empty region");
		progress.inc();

		final PetriNet net = new PetriNet();
		HashMap reg2place = new HashMap();

		progress.setNote("Building Petri~net");
		progress.setMaximum(progmax + log.getLogSummary().getLogEvents().size()
				+ regions.size() + 4);

		Place initialPlace = new Place("initial Place", net);
		net.addPlace(initialPlace);
		Transition initialTransition = new Transition("initial Transition", net);
		net.addTransition(initialTransition);
		net.addEdge(initialPlace, initialTransition);

		id = 0;
		Iterator<Region> it2 = regions.iterator();
		while (it2.hasNext()) {
			progress.inc();
			Region region = it2.next();
			Place p = net.addPlace(new Place("Place " + id++, net));
			reg2place.put(region, p);
			if (region.contains(initialVertex)) {
				net.addEdge(initialTransition, p);
			}
		}

		Iterator<LogEvent> it = log.getLogSummary().getLogEvents().iterator();
		while (it.hasNext()) {
			progress.inc();
			LogEvent obj = it.next();
			Transition t = net.addAndLinkTransition(new Transition(
					(LogEvent) obj, net));

			Collection preReg = regions.getPreRegions(obj);
			it2 = preReg.iterator();
			while (it2.hasNext()) {
				Region reg = it2.next();
				Place p = (Place) reg2place.get(reg);
				if (p != null) {
					net.addEdge(p, t);
				}
			}
			Collection postReg = regions.getPostRegions(obj);
			it2 = postReg.iterator();
			while (it2.hasNext()) {
				Region reg = it2.next();
				Place p = (Place) reg2place.get(reg);
				if (p != null) {
					net.addEdge(t, p);
				}
			}
		}

		if (options.doPostProcessing()) {
			Message.add("<RegionMiner>", Message.TEST);
			net.Test("PetriNetFound");
			progress.setNote("Building State space");
			initialPlace.addToken(new Token());
			final StateSpace statespace = ReachabilityGraphBuilder.build(net);

			progress.setNote("Calculating Regions");
			ConvertingPlugin conv = new RegionsToPetriNet();
			MiningResult result = conv.convert(new ProvidedObject() {
				public Object[] getObjects() {
					return (new Object[] {
							log,
							statespace,
							statespace.calculateMinimalRegions(net
									.getTransitions()) });
				}
			});
			Message.add("</RegionMiner>", Message.TEST);
			return result;
		} else {
			net.Test("RegionMinerResult");
			return new PetriNetResult(log, net);
		}
	}

	private ModelGraph pi2graph(ProcessInstance pi, ModelGraphVertex initial,
			HashSet edgeObjects) {
		ModelGraph graph = new ModelGraph("");
		int id = 0;
		ModelGraphVertex prevNode = graph.addVertex(initial);
		initial.setSubgraph(graph);

		Iterator<AuditTrailEntry> it = pi.getAuditTrailEntryList().iterator();
		while (it.hasNext()) {
			AuditTrailEntry ate = it.next();
			ModelGraphVertex newNode = graph.addVertex(new ModelGraphVertex(
					"vertex" + id++, graph));
			LogEvent le = new LogEvent(ate.getElement(), ate.getType());
			graph.addEdge(prevNode, newNode).object = le;
			edgeObjects.add(le);
			prevNode = newNode;
		}
		return graph;
	}

	private boolean areCompatibleRegions(Region r1, Collection objects1,
			Region r2, Collection objects2) {
		boolean result = true;

		HashSet s1 = new HashSet(r2.getInput());
		s1.retainAll(objects1);

		HashSet s2 = new HashSet(r1.getInput());
		s2.retainAll(objects2);

		result &= s1.equals(s2);
		if (!result) {
			return false;
		}

		s1 = new HashSet(r2.getOutput());
		s1.retainAll(objects1);

		s2 = new HashSet(r1.getOutput());
		s2.retainAll(objects2);

		result &= s1.equals(s2);
		return result;

		/*
		 * Iterator it = r1.getInput().iterator(); while (it.hasNext() &&
		 * result) { Object o = it.next(); result &= r2.getInput().contains(o)
		 * || !objects2.contains(o); } it = r1.getOutput().iterator(); while
		 * (it.hasNext() && result) { Object o = it.next(); result &=
		 * r2.getOutput().contains(o) || !objects2.contains(o); } it =
		 * r2.getInput().iterator(); while (it.hasNext() && result) { Object o =
		 * it.next(); result &= r1.getInput().contains(o) ||
		 * !objects1.contains(o); } it = r2.getOutput().iterator(); while
		 * (it.hasNext() && result) { Object o = it.next(); result &=
		 * r1.getOutput().contains(o) || !objects1.contains(o); } return result;
		 */
	}

	private boolean hasEmptyIntersection(Region r1, Region r2) {

		Iterator it = r1.getInput().iterator();
		while (it.hasNext()) {
			if (r2.getInput().contains(it.next())) {
				return false;
			}
		}
		it = r1.getOutput().iterator();
		while (it.hasNext()) {
			if (r2.getOutput().contains(it.next())) {
				return false;
			}
		}
		return !(r1.getInput().isEmpty() && r1.getOutput().isEmpty()
				&& r2.getInput().isEmpty() && r2.getOutput().isEmpty());
	}

}
