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

package org.processmining.mining.heuristicsmining;

/**
 * @author Ton Weijters
 * @version 1.0
 */

public class HeuristicsMinerParameters {
	public static final double RELATIVE_TO_BEST_THRESHOLD = 0.05;
	public static final int POSITIVE_OBSERVATIONS_THRESHOLD = 10;
	public static final double DEPENDENCY_THRESHOLD = 0.90;
	public static final double L1L_THRESHOLD = 0.90;
	public static final double L2L_THRESHOLD = 0.90;
	public static final double LONG_DISTANCE_THRESHOLD = 0.90;
	public static final int DEPENDENCY_DIVISOR = 1;
	public static final double AND_THRESHOLD = 0.10;

	public static final String RELATIVE_TO_BEST_THRESHOLD_L = "Relative-to-best threshold ";
	public static final String POSITIVE_OBSERVATIONS_THRESHOLD_L = "Positive observations ";
	public static final String DEPENDENCY_THRESHOLD_L = "Dependency threshold ";
	public static final String L1L_THRESHOLD_L = "Length-one-loops threshold ";
	public static final String L2L_THRESHOLD_L = "Length-two-loops threshold ";
	public static final String LONG_DISTANCE_THRESHOLD_L = "Long distance threshold ";
	public static final String DEPENDENCY_DIVISOR_L = "Dependency divisor ";
	public static final String AND_THRESHOLD_L = "AND threshold ";

	private double relativeToBestThreshold = RELATIVE_TO_BEST_THRESHOLD;
	private int positiveObservationsThreshold = POSITIVE_OBSERVATIONS_THRESHOLD;
	private double dependencyThreshold = DEPENDENCY_THRESHOLD;
	private double l1lThreshold = L1L_THRESHOLD;
	private double l2lThreshold = L2L_THRESHOLD;
	private double LDThreshold = LONG_DISTANCE_THRESHOLD;
	private int dependencyDivisor = DEPENDENCY_DIVISOR;
	private double andThreshold = AND_THRESHOLD;
	public boolean extraInfo = false;
	public boolean useAllConnectedHeuristics = true;
	public boolean useLongDistanceDependency = false;

	public double getRelativeToBestThreshold() {
		return relativeToBestThreshold;
	}

	public int getPositiveObservationsThreshold() {
		return positiveObservationsThreshold;
	}

	public double getDependencyThreshold() {
		return dependencyThreshold;
	}

	public double getL1lThreshold() {
		return l1lThreshold;
	}

	public double getL2lThreshold() {
		return l2lThreshold;
	}

	public double getLDThreshold() {
		return LDThreshold;
	}

	public int getDependencyDivisor() {
		return dependencyDivisor;
	}

	public double getAndThreshold() {
		return andThreshold;
	}

	public void setRelativeToBestThreshold(double x) {
		relativeToBestThreshold = x;
	}

	public void setPositiveObservationsThreshold(int n) {
		positiveObservationsThreshold = n;
	}

	public void setDependencyThreshold(double x) {
		dependencyThreshold = x;
	}

	public void setL1lThreshold(double x) {
		l1lThreshold = x;
	}

	public void setL2lThreshold(double x) {
		l2lThreshold = x;
	}

	public void setLDThreshold(double x) {
		LDThreshold = x;
	}

	public void setDependencyDivisor(int n) {
		dependencyDivisor = n;
	}

	public void setAndThreshold(double x) {
		andThreshold = x;
	}

	public void setExtraInfo(boolean x) {
		extraInfo = x;
	}

	public void setUseAllConnectedHeuristics(boolean x) {
		useAllConnectedHeuristics = x;
	}

	public void setUseLongDistanceDependency(boolean x) {
		useLongDistanceDependency = x;
	}

	public String toString() {
		String output = "THRESHOLDS:\n" + RELATIVE_TO_BEST_THRESHOLD_L + " "
				+ relativeToBestThreshold + "\n"
				+ POSITIVE_OBSERVATIONS_THRESHOLD_L + " "
				+ positiveObservationsThreshold + "\n" + DEPENDENCY_THRESHOLD_L
				+ " " + dependencyThreshold + "\n" + L1L_THRESHOLD_L + " "
				+ l1lThreshold + "\n" + L2L_THRESHOLD_L + " " + l2lThreshold
				+ "\n" + LONG_DISTANCE_THRESHOLD_L + " " + LDThreshold + "\n"
				+ DEPENDENCY_DIVISOR_L + " " + dependencyDivisor + "\n"
				+ AND_THRESHOLD_L + " " + andThreshold + "\n" + "Extra Info "
				+ Boolean.toString(extraInfo) + "\n"
				+ "Use all-events-connected-heuristic "
				+ Boolean.toString(useAllConnectedHeuristics) + "\n"
				+ "Use long distance dependency heuristics "
				+ Boolean.toString(useLongDistanceDependency) + "\n";
		return output;
	}
}
