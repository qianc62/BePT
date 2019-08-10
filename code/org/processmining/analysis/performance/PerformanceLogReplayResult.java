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

package org.processmining.analysis.performance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.ListIterator;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisMethod;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisResult;

/**
 * Contains all the performance results obtained during log replay analysis. Can
 * be used to retrieve values for the performance metrics and to get extended
 * visualizations.
 * 
 * @see PerformanceMeasurer
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */

public class PerformanceLogReplayResult extends LogReplayAnalysisResult {

	// DescriptiveStatistics-object in which throughput times can be stored
	private DescriptiveStatistics timeStats = DescriptiveStatistics
			.newInstance();
	// SummaryStatistics to obtain mean inter arrival times
	private SummaryStatistics arrivalStats = SummaryStatistics.newInstance();

	// number of log traces that can be replayed normally
	private int properFrequency;

	public PerformanceLogReplayResult(AnalysisConfiguration analysisOptions,
			PetriNet net, LogReader log, LogReplayAnalysisMethod method) {
		// call the constructor of the superclass
		super(analysisOptions, net, log, method);
	}

	/**
	 * Initializes the diagnostic data structures needed to store the
	 * measurements taken during the log replay analysis.
	 */
	protected void initDiagnosticDataStructures() {
		replayedLog = new ExtendedLogReader(inputLogReader);
		replayedPetriNet = new ExtendedPetriNet(inputPetriNet, replayedLog
				.getLogTraceIDs());
	}

	// ////////////////////////////METRICS-RELATED
	// METHODS///////////////////////////

	/**
	 * Calculates the average, min ad max throughput time out of the throughput
	 * times of all traces in piList. Next to this, the arrival rate is
	 * calculated. All metrics are based on the process instances in piList only
	 * 
	 * @param piList
	 *            ArrayList: the process instances used
	 * @param fitOption
	 *            int: the fit option used (how to deal with non-conformance)
	 * @throws Exception
	 */
	public void calculateMetrics(ArrayList piList, int fitOption)
			throws Exception {
		properFrequency = 0;
		timeStats.clear();
		arrivalStats.clear();
		ArrayList arrivalDates = new ArrayList();
		ListIterator lit = piList.listIterator();
		while (lit.hasNext()) {
			ExtendedLogTrace currentTrace = (ExtendedLogTrace) lit.next();
			if (currentTrace.hasProperlyTerminated()
					&& currentTrace.hasSuccessfullyExecuted()) {
				properFrequency++;
			}
			try {
				long tp = (currentTrace.getEndDate().getTime() - currentTrace
						.getBeginDate().getTime());
				if (fitOption == 0) {
					// timeStats based on all traces
					timeStats.addValue(tp);
					arrivalDates.add(currentTrace.getBeginDate());
				}
				if (currentTrace.hasProperlyTerminated()
						&& currentTrace.hasSuccessfullyExecuted()) {
					if (fitOption == 1) {
						// timeStats based on fitting traces only
						timeStats.addValue(tp);
						arrivalDates.add(currentTrace.getBeginDate());
					}
				}
			} catch (NullPointerException ex) {
				ex.printStackTrace();
			}
		}
		Date[] arrivals = (Date[]) arrivalDates.toArray(new Date[0]);
		// make sure arrivaldates are sorted
		Arrays.sort(arrivals);
		if (arrivals.length > 1) {
			for (int i = 1; i < arrivals.length; i++) {
				long t1 = arrivals[i].getTime();
				long t2 = arrivals[i - 1].getTime();
				long iat = arrivals[i].getTime() - arrivals[i - 1].getTime();
				if (iat >= 0) {
					arrivalStats.addValue(iat);
				}
			}
		}
	}

	/**
	 * Exports the throughput times of all process instances in piList to a
	 * comma-seperated text-file.
	 * 
	 * @param piList
	 *            ArrayList: the process instances used
	 * @param file
	 *            File: the file to which the times are exported
	 * @param divider
	 *            long: the time divider used
	 * @param sort
	 *            String: the time sort used
	 * @param fitOption
	 *            int: the fit option used (how to deal with non-conformance)
	 * @throws IOException
	 */
	public void exportToFile(ArrayList piList, File file, long divider,
			String sort, int fitOption) throws IOException {
		Writer output = new BufferedWriter(new FileWriter(file));
		String line = "Log Trace,Throughput time (" + sort + ")\n";
		output.write(line);
		ListIterator lit = piList.listIterator();
		while (lit.hasNext()) {
			ExtendedLogTrace currentTrace = (ExtendedLogTrace) lit.next();
			try {
				double tp = (currentTrace.getEndDate().getTime() - currentTrace
						.getBeginDate().getTime())
						* 1.0 / divider;
				if (fitOption == 0) {
					// times based on all traces
					line = currentTrace.getName() + "," + tp + "\n";
					// write line to the file
					output.write(line);
				}
				if (fitOption == 1 && currentTrace.hasProperlyTerminated()
						&& currentTrace.hasSuccessfullyExecuted()) {
					// times based on fitting traces only
					line = currentTrace.getName() + "," + tp + "\n";
					// write line to the file
					output.write(line);
				}
			} catch (NullPointerException npe) {
			}
		}
		// close the file
		output.close();
	}

	// ////////////////////////////GET
	// METHODS///////////////////////////////////////

	/**
	 * Calculates and returns the stdev in throughput time out of the throughput
	 * times in timeStats. (make sure calculateProcessMetrics() is called before
	 * this method).
	 * 
	 * @return double
	 */
	public double getStdevThroughputTime() {
		return timeStats.getStandardDeviation();
	}

	/**
	 * Calculates the average of the (fastestpercentage) fast traces, the
	 * (slowestPercentage) slow traces and the (100% - fastestPercentage -
	 * slowestPercentage) normal speed traces and returns these averages in an
	 * array, where [0]: avg fast throughput time [1]: avg slow throughput time
	 * [2]: avg middle throughput time
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
		long total = 0;
		// obtain the number of fast , slow, normal traces
		int[] sizes = getSizes(fastestPercentage, slowestPercentage);
		int fastSize = sizes[0], slowSize = sizes[1], middleSize = sizes[2];
		for (int i = 0; i < fastSize; i++) {
			total += timeList[i];
		}
		// calculate average of the fastest traces
		double avgFastestTime = 0.0;
		if (fastSize != 0) {
			avgFastestTime = (total * 1.0) / fastSize;
		}
		// calculate average of the slowest traces
		int upperSize = timeList.length - slowSize;
		total = 0;
		for (int i = upperSize; i < timeList.length; i++) {
			total += timeList[i];
		}
		double avgSlowestTime = 0.0;
		if (slowSize > 0) {
			avgSlowestTime = (total * 1.0) / slowSize;
		}

		// calculate the middle/normal-speed traces
		total = 0;
		for (int i = fastSize; i < upperSize; i++) {
			total += timeList[i];
		}
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
	 * considered to be fast, i.e. have a low throughput time (place 0 in
	 * array), the number of process instances that are slow (place 1 in array)
	 * and the number of process instances that are considered to be of normal
	 * speed (place 2 in array). Based on fastestPercentage, slowestPercentage
	 * and timeList (thus method calculateProcessMetrics() should be called
	 * before this one)
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
				// the number of measurements in timeList (rounding could mess
				// this up)
				sizes[1] = length - sizes[0];
			}
		} else {
			sizes[1] = 0;
		}
		sizes[2] = length - sizes[0] - sizes[1];
		return sizes;
	}

	/**
	 * Calculates and returns the arrival rate of the traces in piList
	 * 
	 * @return double
	 */
	public double getArrivalRate() {
		double arrivalRate = 0;
		if (arrivalStats.getN() > 0 && arrivalStats.getMean() != 0) {
			// mean arrivalRate is 1 divided by the mean of the inter-arrival
			// times
			arrivalRate = 1 / arrivalStats.getMean();
		}
		return arrivalRate;
	}

	/**
	 * Returns the arrival Stats of the traces in piList
	 * 
	 * @return SummaryStatistics
	 */
	public SummaryStatistics getArrivalStats() {
		return arrivalStats;
	}

	/**
	 * Returns the mean throughput time
	 * 
	 * @return double
	 */
	public double getMeanThroughputTime() {
		return timeStats.getMean();
	}

	/**
	 * Returns the minimal throughput time. Note that method
	 * calculateProcessMetrics() should be called before this method.
	 * 
	 * @return double
	 */
	public double getMinThroughputTime() {
		return timeStats.getMin();
	}

	/**
	 * Returns the maximal throughput time
	 * 
	 * @return double
	 */
	public double getMaxThroughputTime() {
		return timeStats.getMax();
	}

	/**
	 * returns the number of cases that execute successfully and complete
	 * properly
	 * 
	 * @return int
	 */
	public int getProperFrequency() {
		return (properFrequency);
	}

	// ////////////////////////////GRAPPA-RELATED
	// METHODS///////////////////////////
	/**
	 * Creates a visualization of the performance analysis results. Note that a
	 * change of the display state by the user will have no effect before
	 * calling this methods. This is intended to prevent unnecessary cloning of
	 * the extended petri net, which actually delivers the custom visualization
	 * of the performance analysis results.
	 * 
	 * @param selectedInstances
	 *            The process instances that have been selected for updating the
	 *            visualization.
	 * @return The visualization wrapped in a ModelGraphPanel.
	 */
	public ModelGraphPanel getVisualization(ArrayList selectedInstances) {
		// sets the currentlySelectedInstances attribute, which is necessary
		// because
		// the writeToDot() method has a fixed interface, though the
		// visualization should
		// be able to take them into account
		((ExtendedPetriNet) replayedPetriNet).currentlySelectedInstances = selectedInstances;
		ModelGraphPanel myResultVisualization;
		myResultVisualization = ((ExtendedPetriNet) replayedPetriNet)
				.getGrappaVisualization();
		return myResultVisualization;
	}

}
