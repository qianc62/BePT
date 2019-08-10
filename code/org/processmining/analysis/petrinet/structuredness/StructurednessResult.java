package org.processmining.analysis.petrinet.structuredness;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.bpel.util.Pair;
import org.processmining.framework.models.bpel.util.Quadruple;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.State;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.pattern.Component;
import org.processmining.framework.models.petrinet.pattern.log.Log;

public class StructurednessResult {

	private final PetriNet wfnet;

	private final double myMetric;

	private final int cyclomaticMetric;

	private final int cardosoMetric;

	private final String netName;

	private final String path;

	private final PetriNet originalNet;

	private final Log log;

	private final List<Pair<Place, Set<Set<Place>>>> cardosoCalculation;

	private final Quadruple<StateSpace, Integer, Integer, List<List<ModelGraphVertex>>> cyclomaticCalculation;

	private final Map<String, Component> transition2Component;

	public StructurednessResult(
			String netName,
			String path,
			PetriNet originalNet,
			PetriNet wfnet,
			double myMetric,
			int cardosoMetric,
			List<Pair<Place, Set<Set<Place>>>> cardosoCalculation,
			int cyclomaticMetric,
			Quadruple<StateSpace, Integer, Integer, List<List<ModelGraphVertex>>> cyclomaticCalculation,
			Log log, Map<String, Component> transition2Component) {
		this.netName = netName;
		this.path = path;
		this.originalNet = originalNet;
		this.wfnet = wfnet;
		this.myMetric = myMetric;
		this.cardosoMetric = cardosoMetric;
		this.cardosoCalculation = cardosoCalculation;
		this.cyclomaticCalculation = cyclomaticCalculation;
		this.cyclomaticMetric = cyclomaticMetric;
		this.log = log;
		this.transition2Component = transition2Component;
	}

	public Log getLog() {
		return log;
	}

	public int getCardosoMetric() {
		return cardosoMetric;
	}

	public PetriNet getOriginalNet() {
		return originalNet;
	}

	public double getMyMetric() {
		return myMetric;
	}

	public int getCyclomaticMetric() {
		return cyclomaticMetric;
	}

	public PetriNet getWfnet() {
		return wfnet;
	}

	public String getNetName() {
		return netName;
	}

	public String getPath() {
		return path;
	}

	public List<Pair<Place, Set<Set<Place>>>> getCardosoCalculation() {
		return cardosoCalculation;
	}

	public Quadruple<StateSpace, Integer, Integer, List<List<ModelGraphVertex>>> getCyclomaticCalculation() {
		return cyclomaticCalculation;
	}

	public Map<String, Component> getTransition2Component() {
		return transition2Component;
	}

}
