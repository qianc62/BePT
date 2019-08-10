package org.processmining.mining.regionmining;

import org.processmining.framework.log.LogSummary;

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
public class ParikhLanguageRegionMinerOptions {

	public static String NO_RES = "No structural restrictions";
	public static String STATEMACHINE = "State machine";
	public static String MARKEDGRAPH = "Marked graph";
	public static String EXT_FREE_CHOICE = "Extended free-choice";
	public static String NO_PLACES = "Do not introduce places";

	public ParikhLanguageRegionMinerOptions(LogSummary summary) {
		elementaryNet = true;
		intVars = true;
		useCausalDependencies = true;
		restrictToCausalDependencies = false;
		denySelfLoops = elementaryNet;
		restrictToDisconnectedTrans = false;
		removeExistingPlaces = true;
		maxInEdges = summary.getLogEvents().size();
		maxOutEdges = maxInEdges;
		maxTotalEdges = maxInEdges + maxOutEdges;
		maxPlaces = summary.getLogEvents().size()
				* summary.getLogEvents().size();
		simulationBoundedness = 0;
		searchInitialMarking = true;
		emptyAfterCompleteCase = true;
		restrictions = NO_RES;
	}

	private String restrictions;

	public void setRestrictions(String val) {
		restrictions = val;
	}

	public String getRestrictions() {
		return restrictions;
	}

	private boolean elementaryNet;

	public void setElementaryNet(boolean val) {
		elementaryNet = val;
	}

	public boolean getElementaryNet() {
		return elementaryNet;
	}

	private boolean emptyAfterCompleteCase;

	public void setEmptyAfterCompleteCase(boolean val) {
		emptyAfterCompleteCase = val;
	}

	public boolean getEmptyAfterCompleteCase() {
		return emptyAfterCompleteCase;
	}

	private boolean intVars;

	public void setIntVars(boolean val) {
		intVars = val;
	}

	public boolean getIntVars() {
		return intVars;
	}

	private boolean useCausalDependencies;

	public void setUseCausalDependencies(boolean val) {
		useCausalDependencies = val;
	}

	public boolean getUseCausalDependencies() {
		return useCausalDependencies;
	}

	private boolean restrictToCausalDependencies;

	public void setRestrictToCausalDependencies(boolean val) {
		restrictToCausalDependencies = val;
	}

	public boolean getRestrictToCausalDependencies() {
		return restrictToCausalDependencies;
	}

	private boolean denySelfLoops;

	public void setDenySelfLoops(boolean val) {
		denySelfLoops = val;
	}

	public boolean getDenySelfLoops() {
		return denySelfLoops;
	}

	private boolean restrictToDisconnectedTrans;

	public void setRestrictToDisconnectedTrans(boolean val) {
		restrictToDisconnectedTrans = val;
	}

	public boolean getRestrictToDisconnectedTrans() {
		return restrictToDisconnectedTrans;
	}

	private boolean removeExistingPlaces;

	public void setRemoveExistingPlaces(boolean val) {
		removeExistingPlaces = val;
	}

	public boolean getRemoveExistingPlaces() {
		return removeExistingPlaces;
	}

	private int maxInEdges;

	public void setMaxInEdges(int val) {
		maxInEdges = val;
	}

	public int getMaxInEdges() {
		return maxInEdges;
	}

	private int maxOutEdges;

	public void setMaxOutEdges(int val) {
		maxOutEdges = val;
	}

	public int getMaxOutEdges() {
		return maxOutEdges;
	}

	private int maxTotalEdges;

	public void setMaxTotalEdges(int val) {
		maxTotalEdges = val;
	}

	public int getMaxTotalEdges() {
		return maxTotalEdges;
	}

	private int maxPlaces;

	public void setMaxPlaces(int val) {
		maxPlaces = val;
	}

	public int getMaxPlaces() {
		return maxPlaces;
	}

	private int simulationBoundedness;

	public void setSimulationBoundedness(int val) {
		simulationBoundedness = val;
	}

	public int getSimulationBoundedness() {
		return simulationBoundedness;
	}

	private boolean searchInitialMarking;

	public void setSearchInitialMarking(boolean val) {
		searchInitialMarking = val;
	}

	public boolean getSearchInitialMarking() {
		return searchInitialMarking;
	}

}
