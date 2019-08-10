package org.processmining.analysis.performance.fsmanalysis;

import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.processmining.framework.models.fsm.AcceptFSM;

public class FSMStatistics {

	protected AcceptFSM fsm;
	protected HashMap<String, DescriptiveStatistics> suSojournMap;
	protected HashMap<String, DescriptiveStatistics> suRemainingMap;
	protected HashMap<String, DescriptiveStatistics> edgeInterMap;
	protected HashMap<String, DescriptiveStatistics> suElapsedMap;

	public FSMStatistics() {
		suSojournMap = new HashMap<String, DescriptiveStatistics>();
		suRemainingMap = new HashMap<String, DescriptiveStatistics>();
		edgeInterMap = new HashMap<String, DescriptiveStatistics>();
		suElapsedMap = new HashMap<String, DescriptiveStatistics>();
	}

	public FSMStatistics(AcceptFSM fsm) {
		this.fsm = fsm;
		suSojournMap = new HashMap<String, DescriptiveStatistics>();
		suRemainingMap = new HashMap<String, DescriptiveStatistics>();
		edgeInterMap = new HashMap<String, DescriptiveStatistics>();
		suElapsedMap = new HashMap<String, DescriptiveStatistics>();
	}

	public AcceptFSM getFSM() {
		return fsm;
	}

	public HashMap<String, DescriptiveStatistics> getRemainingMap() {
		return suRemainingMap;
	}

	public HashMap<String, DescriptiveStatistics> getSojournMap() {
		return suSojournMap;
	}

	public HashMap<String, DescriptiveStatistics> getElapsedMap() {
		return suElapsedMap;
	}

	public HashMap<String, DescriptiveStatistics> getEdgeMap() {
		return edgeInterMap;
	}
}
