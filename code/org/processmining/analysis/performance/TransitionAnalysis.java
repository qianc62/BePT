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

/*
 * Copyright (c) 2006 Eindhoven University of Technology
 * All rights reserved.
 */
package org.processmining.analysis.performance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * This is class is needed to calculate metrics concerning two transitions. Most
 * importantly, the (avg,min,max etc.) time-in-between the two transitions can
 * be calculated. Where time in between two transitions for a certain process
 * instance is the time between the first firing of the one transition and the
 * first firing of the other transition.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 * @todo Peter: consider if we should determine which of the two transitions
 *       occurs first, and if we should keep seperate metrics for the times
 *       transition one occurs before two and the other way around
 */
public class TransitionAnalysis {

	// transitions that are analyzed
	private ExtendedTransition last;
	private ExtendedTransition other;

	// DescriptiveStatistics to store time-between metrics in
	private DescriptiveStatistics timeStats = DescriptiveStatistics
			.newInstance();

	/**
	 * Initialize object of TransitionAnalysisResult with transitions lastTrans
	 * and otherTrans
	 * 
	 * @param lastTrans
	 *            ExtendedTransition: one of the two transitions
	 * @param otherTrans
	 *            ExtendedTransition: one of the two transitions
	 */
	public TransitionAnalysis(ExtendedTransition lastTrans,
			ExtendedTransition otherTrans) {
		last = lastTrans;
		other = otherTrans;
	}

	// //////// TRANSITION METRIC CALCULATION METHODS //////////

	/**
	 * Calculates the average, maximal and minimum time tokens spend between the
	 * two transitions, and the standard deviation in this time, and averages of
	 * the fastest, slowest and normal time-in-betweens. Next to this, the
	 * frequency of cases in which both transitions fire is calculated. Results
	 * of the calculations are stored in corresponding instance-variables.
	 * 
	 * @param piList
	 *            ArrayList: The process instances on which calculations are
	 *            based
	 * @param fitOption
	 *            int: The fit option used (how to deal with non-conformance)
	 */
	public void calculateMetrics(ArrayList piList, int fitOption,
			HashSet failedInstances) {
		timeStats.clear();
		ListIterator it = piList.listIterator();
		while (it.hasNext()) {
			String piName = (String) it.next();
			if ((other.getFirstFireTime(piName) != null)
					&& (last.getFirstFireTime(piName) != null)) {
				// Absolute value is used in calculation of time in between
				long transTime = Math.abs(other.getFirstFireTime(piName)
						.getTime()
						- last.getFirstFireTime(piName).getTime());
				if (fitOption == 0) {
					// times based on all measurements
					timeStats.addValue(transTime);
				}
				if ((fitOption == 1 && !other.hasFailedBefore(piName))
						&& (!last.hasFailedBefore(piName))) {
					// times based on measurements in traces where both
					// transitions
					// fire (at least) once, before problems occur
					// (i.e. a transition fails execution)
					timeStats.addValue(transTime);
				}
				if ((fitOption == 2 && !other.hasFailedExecution(piName))
						&& (!last.hasFailedExecution(piName))) {
					// times based on measurements in those traces where both
					// transitions do no fail execution
					timeStats.addValue(transTime);
				}
				if (fitOption == 3 && !failedInstances.contains(piName)) {
					timeStats.addValue(transTime);
				}
			}
		}
	}

	/**
	 * Exports all time-in-between measurements corresponding to this
	 * TransitionAnalysis to a comma-seperated text-file.
	 * 
	 * @param piList
	 *            ArrayList: the Process instances used
	 * @param file
	 *            File: the file to which the measurements should be exported
	 * @param divider
	 *            long: the time divider used
	 * @param sort
	 *            String: the time sort used
	 * @param fitOption
	 *            int: The fit option used (how to deal with non-conformance) *
	 * @throws IOException
	 */
	public void exportToFile(ArrayList piList, File file, long divider,
			String sort, int fitOption) throws IOException {
		Writer output = new BufferedWriter(new FileWriter(file));
		String line = "Log Trace, Time (" + sort + ") in between transitions "
				+ last.getLogEvent().getModelElementName() + "-"
				+ last.getLogEvent().getEventType() + " and "
				+ other.getLogEvent().getModelElementName() + "-"
				+ other.getLogEvent().getEventType() + "\n";
		// write line to file
		output.write(line);
		ListIterator it = piList.listIterator();
		while (it.hasNext()) {
			String piName = (String) it.next();
			if ((other.getFirstFireTime(piName) != null)
					&& (last.getFirstFireTime(piName) != null)) {
				// Note that the absolute value is used.
				double transTime = (Math.abs(other.getFirstFireTime(piName)
						.getTime()
						- last.getFirstFireTime(piName).getTime()) * 1.0)
						/ divider;
				if (fitOption == 0) {
					// times based on all measurements
					line = piName + "," + transTime + "\n";
					// write line to file
					output.write(line);
				}
				if ((fitOption == 1 && !other.hasFailedBefore(piName))
						&& (!last.hasFailedBefore(piName))) {
					// times based on measurements in traces where both
					// transitions
					// fire (at least) once, before problems occur
					// (i.e. a transition fails execution)
					line = piName + "," + transTime + "\n";
					// write line to file
					output.write(line);
				}
				if ((fitOption == 2 && !other.hasFailedExecution(piName))
						&& (!last.hasFailedExecution(piName))) {
					// times based on measurements in those traces where both
					// transitions do no fail execution
					line = piName + "," + transTime + "\n";
					// write line to file
					output.write(line);
				}
			}
		}
		// close the file
		output.close();
	}

	// //////////////////////////GET
	// METHODS///////////////////////////////////////
	/**
	 * Returns the average time tokens spend between the two transitions.
	 * 
	 * @return double
	 */
	public double getMeanTime() {
		return timeStats.getMean();
	}

	/**
	 * Returns the maximal time tokens spend between the two transitions.
	 * 
	 * @return double
	 */
	public double getMaxTime() {
		return timeStats.getMax();
	}

	/**
	 * Returns the minimum time tokens spend between the two transitions.
	 * 
	 * @return double
	 */
	public double getMinTime() {
		return timeStats.getMin();
	}

	/**
	 * Calculates and returns the value of the standard deviation of the
	 * time-in-between transition last and transition other.
	 * 
	 * @return double
	 */
	public double getStdevTimeInBetween() {
		return timeStats.getStandardDeviation();
	}

	/**
	 * Calculates the average of the (fastestpercentage) fast traces, the
	 * (slowestPercentage) slow traces and the (100% - fastestPercentage -
	 * slowestPercentage) normal speed traces and returns these averages in an
	 * array, where [0]: avg fast time in between [1]: avg slow time in between
	 * [2]: avg middle time in between
	 * 
	 * @param fastestPercentage
	 *            double: the percentage of measurements that is to be counted
	 *            as fast
	 * @param slowestPercentage
	 *            double: the percentage of measurements that is to be counted
	 *            as slow
	 * @return double[]
	 */
	public double[] getAverageTimes(double fastestPercentage,
			double slowestPercentage) {
		// initialize arrays
		double[] timeList = timeStats.getSortedValues();
		double[] avgTimes = new double[3];
		// get time in between of traces sorted ascending by time
		long total = 0;
		// obtain the number of fast , slow, normal traces
		int[] sizes = getSizes(fastestPercentage, slowestPercentage);
		int fastSize = sizes[0], slowSize = sizes[1], middleSize = sizes[2];
		for (int i = 0; i < fastSize; i++) {
			// step through the fast traces
			total += timeList[i];
		}
		// calculate average of the fast traces
		double avgFastestTime = 0.0;
		if (fastSize != 0) {
			avgFastestTime = (total * 1.0) / fastSize;
		}

		int upperSize = timeList.length - slowSize;
		total = 0;
		for (int i = upperSize; i < timeList.length; i++) {
			// step through the slow traces
			total += timeList[i];
		}
		// calculate average of the slowest traces
		double avgSlowestTime = 0.0;
		if (slowSize != 0) {
			avgSlowestTime = (total * 1.0) / slowSize;
		}

		total = 0;
		for (int i = fastSize; i < upperSize; i++) {
			// step through the normal traces
			total += timeList[i];
		}
		// calculate the average of the normal traces
		double avgMiddleTime = 0.0;
		if (middleSize > 0) {
			avgMiddleTime = (total * 1.0) / middleSize;
		}
		avgTimes[0] = avgFastestTime;
		avgTimes[1] = avgSlowestTime;
		avgTimes[2] = avgMiddleTime;
		return avgTimes;
	}

	/**
	 * Returns an array containing the number of process instances that are
	 * considered to be fast, i.e. have a low time in between (place 0 in
	 * array), the number of process instances that are slow (place 1 in array)
	 * and the number of process instances that are considered to be of normal
	 * speed (place 2 in array). Based on fastestPercentage, slowestPercentage
	 * and the values in timeStats (thus method calculateTimeInBetweenMetrics()
	 * should be called before this one)
	 * 
	 * @param fastestPercentage
	 *            double: the percentage of measurements that is to be counted
	 *            as fast
	 * @param slowestPercentage
	 *            double: the percentage of measurements that is to be counted
	 *            as slow
	 * @return int[]
	 */
	public int[] getSizes(double fastestPercentage, double slowestPercentage) {
		int[] sizes = new int[3];
		String sizeString;
		int length = timeStats.getValues().length;
		sizeString = Math.round((length * fastestPercentage) / 100.0) + "";
		sizes[0] = Integer.parseInt(sizeString);
		if (sizes[0] != length) {
			sizeString = Math.round((length * slowestPercentage) / 100.0) + "";
			sizes[1] = Integer.parseInt(sizeString);
			if ((sizes[0] + sizes[1]) > length) {
				// Make sure that sizes[0] + sizes[1] remains smaller than
				// the length of the timeList (rounding could mess this up)
				sizes[1] = length - sizes[0];
			}
		} else {
			sizes[1] = 0;
		}
		sizes[2] = length - sizes[0] - sizes[1];
		return sizes;
	}

	/**
	 * Returns the frequency of process instances in which both transitions
	 * appear.
	 * 
	 * @return long
	 */
	public long getFrequency() {
		return (timeStats.getN());
	}

}
