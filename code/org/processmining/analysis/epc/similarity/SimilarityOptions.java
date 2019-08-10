package org.processmining.analysis.epc.similarity;

import java.io.*;

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
public class SimilarityOptions {
	// All values should be between 0 and 1;
	private double eventThreshold;
	private double functionThreshold;
	private double eventSemanticWeight;
	private double eventSyntaxWeight;
	private double functionSemanticWeight;
	private double functionSyntaxWeight;
	private double functionStructureWeight;
	private int parallelThreads = 4;
	private String footprintFolder;
	private boolean removeInitialFinalNodes = true;
	private boolean useSimilarityValues = true;

	public SimilarityOptions() {
		setDefaults();
	}

	public String toString() {
		return "eventThreshold: " + eventThreshold + " \t"
				+ "functionThreshold: " + functionThreshold + " \t"
				+ "eventSemanticWeight: " + eventSemanticWeight + " \t"
				+ "eventSyntaxWeight: " + eventSyntaxWeight + " \t"
				+ "functionSemanticWeight: " + functionSemanticWeight + " \t"
				+ "functionSyntaxWeight: " + functionSyntaxWeight + " \t"
				+ "functionStructureWeight: " + functionStructureWeight + " \t"
				+ "removeStartAndFinalNode: " + removeInitialFinalNodes + " \t"
				+ "useSimilarityValues: " + useSimilarityValues + " \t"
				+ "footprintFolder: " + footprintFolder;

	}

	public SimilarityOptions(double eventThreshold, double functionThreshold,
			double eventSemanticWeight, double eventSyntaxWeight,
			double functionSemanticWeight, double functionSyntaxWeight,
			double functionStructureWeight, String footprintFolder,
			int parallelThreads, boolean removeInitialFinalNodes,
			boolean useSimilarityValues) {
		this.eventThreshold = eventThreshold;
		this.functionThreshold = functionThreshold;
		this.eventSemanticWeight = eventSemanticWeight;
		this.eventSyntaxWeight = eventSyntaxWeight;
		this.functionSemanticWeight = functionSemanticWeight;
		this.functionSyntaxWeight = functionSyntaxWeight;
		this.functionStructureWeight = functionStructureWeight;
		this.footprintFolder = footprintFolder;
		this.parallelThreads = parallelThreads;
		this.removeInitialFinalNodes = removeInitialFinalNodes;
		this.useSimilarityValues = useSimilarityValues;
	}

	private void checkFolderName() {
		if (footprintFolder == null) {
			footprintFolder = "";
		}
		if (!footprintFolder.endsWith(File.separator)) {
			footprintFolder += File.separator;
		}
	}

	private boolean checkValue(double d) {
		return (d >= 0.0) && (d <= 1.0);
	}

	public void setDefaults() {
		// eventThreshold = 0.89;
		// functionThreshold = 0.9;
		// eventSemanticWeight = 1.0;
		// eventSyntaxWeight = 0.0;
		// functionSemanticWeight = 0.5;
		// functionSyntaxWeight = 0;
		// functionStructureWeight = 1.0;
		// removeInitialFinalNodes = true;
		// useSimilarityValues = false;
		// footprintFolder = System.getProperty("java.io.tmpdir");
		eventThreshold = 0.0;
		functionThreshold = 0.0;
		eventSemanticWeight = 0.0;
		eventSyntaxWeight = 0.0;
		functionSemanticWeight = 0.0;
		functionSyntaxWeight = 1.0;
		functionStructureWeight = 0.0;
		removeInitialFinalNodes = true;
		useSimilarityValues = true;
		footprintFolder = System.getProperty("java.io.tmpdir");
	}

	public void setEventThreshold(double eventThreshold) {
		if (checkValue(eventThreshold)) {
			this.eventThreshold = eventThreshold;
		}
	}

	public void setFunctionThreshold(double functionThreshold) {
		if (checkValue(functionThreshold)) {
			this.functionThreshold = functionThreshold;
		}
	}

	public void setEventSemanticWeight(double eventSemanticWeight) {
		if (checkValue(eventSemanticWeight)) {
			this.eventSemanticWeight = eventSemanticWeight;
		}
	}

	public void setEventSyntaxWeight(double eventSyntaxWeight) {
		if (checkValue(eventSyntaxWeight)) {
			this.eventSyntaxWeight = eventSyntaxWeight;
		}
	}

	public void setFunctionSemanticWeight(double functionSemanticWeight) {
		if (checkValue(functionSemanticWeight)) {
			this.functionSemanticWeight = functionSemanticWeight;
		}
	}

	public void setFunctionSyntaxWeight(double functionSyntaxWeight) {
		if (checkValue(functionSyntaxWeight)) {
			this.functionSyntaxWeight = functionSyntaxWeight;
		}
	}

	public void setFunctionStructureWeight(double functionStructureWeight) {
		if (checkValue(functionStructureWeight)) {
			this.functionStructureWeight = functionStructureWeight;
		}
	}

	public void setFootprintFolder(String footprintFolder) {
		this.footprintFolder = footprintFolder;
		checkFolderName();
	}

	public void setParallelThreads(int parallelThreads) {
		this.parallelThreads = parallelThreads;
	}

	public void setRemoveInitialFinalNodes(boolean removeInitialFinalNodes) {
		this.removeInitialFinalNodes = removeInitialFinalNodes;
	}

	public void setUseSimilarityValues(boolean useSimilarityValues) {
		this.useSimilarityValues = useSimilarityValues;
	}

	public double getEventThreshold() {
		return eventThreshold;
	}

	public double getFunctionThreshold() {
		return functionThreshold;
	}

	public double getEventSemanticWeight() {
		return eventSemanticWeight;
	}

	public double getEventSyntaxWeight() {
		return eventSyntaxWeight;
	}

	public double getFunctionSemanticWeight() {
		return functionSemanticWeight;
	}

	public double getFunctionSyntaxWeight() {
		return functionSyntaxWeight;
	}

	public double getFunctionStructureWeight() {
		return functionStructureWeight;
	}

	public String getFootprintFolder() {
		checkFolderName();
		return footprintFolder;
	}

	public int getParallelThreads() {
		return parallelThreads;
	}

	public boolean getRemoveInitialFinalNodes() {
		return removeInitialFinalNodes;
	}

	public boolean getUseSimilarityValues() {
		return useSimilarityValues;
	}
}
