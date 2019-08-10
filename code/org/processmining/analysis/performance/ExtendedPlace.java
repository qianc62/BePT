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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedPlace;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedTransition;
import org.processmining.framework.ui.Message;

/**
 * This class is used to be able to extend places with performance information,
 * which is to be stored during log replay and will be used to form a part of
 * the extended Petri net.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class ExtendedPlace extends ReplayedPlace {
	/**
	 * Data structure for storage of the time measurements.
	 * 
	 * @see TimeMeasurement
	 */
	private ArrayList timeMeasurements = new ArrayList();

	// Contains the transitions that have put a token in this place that
	// has not yet been removed
	private ArrayList enablingTransitions = new ArrayList();
	// DescriptiveStatistics in which time-metrics are stored
	private DescriptiveStatistics waitStatistics = DescriptiveStatistics
			.newInstance();
	private DescriptiveStatistics syncStatistics = DescriptiveStatistics
			.newInstance();
	private DescriptiveStatistics sojStatistics = DescriptiveStatistics
			.newInstance();
	// SummaryStatistics to obtain mean inter-arrival time
	private SummaryStatistics arrivalStatistics = SummaryStatistics
			.newInstance();
	// Keeps track of whether the user has set waiting time settings
	// specifically
	// for this place(true) or not
	private boolean hasOwnSettings = false;
	// if the user has set own settings, they can be put in the variables below
	private ArrayList bounds;
	private ArrayList colors;

	/**
	 * The constructor creates a normal place but additionally initializes
	 * 
	 * @param p
	 *            The place (to be passed to super class).
	 * @param net
	 *            The Petri net it belongs to (to be passed to super class).
	 * @param caseIDs
	 *            The list of diagnostic log traces that want to be able to
	 *            store results of the conformance analysis in this diagnostic
	 *            place.
	 */
	public ExtendedPlace(Place p, PetriNet net, ArrayList caseIDs) {
		super(p, net, caseIDs);
	}

	// /////////////////////PLACE METRICS METHODS////////////////////////
	/**
	 * Calculates the synchronization time and waiting time of one token in this
	 * place and stores it.
	 * 
	 * @param tokenAvailable
	 *            Date the Date at which a token was first available in this
	 *            place
	 * @param transEnabled
	 *            Date the Date at which the transition to fire was enabled
	 * @param transFired
	 *            Date the Date at which the transition fired
	 * @param trace
	 *            ReplayedLogTrace the process instance in which this
	 *            measurement was taken
	 */
	public void recordTimeMeasurement(Date tokenAvailable, Date transEnabled,
			Date transFired, ReplayedLogTrace trace) {
		long sync = (transEnabled.getTime() - tokenAvailable.getTime());
		long wait = (transFired.getTime() - transEnabled.getTime());
		ExtendedLogTrace lt = (ExtendedLogTrace) trace;
		TimeMeasurement tm;
		if (!lt.hasProperlyTerminated() || !lt.hasSuccessfullyExecuted()) {
			// the log trace has failed execution before this measurement was
			// taken
			tm = new TimeMeasurement(trace.getName(), wait, sync,
					tokenAvailable, true);
		} else {
			// measurement is not disturbed by a non-fitting log trace
			tm = new TimeMeasurement(trace.getName(), wait, sync,
					tokenAvailable, false);
		}
		timeMeasurements.add(tm);
		Message.add(getIdentifier() + " data: " + tm.toString(), Message.DEBUG);
	}

	/**
	 * takes the waiting time, synchronization time and the sojourn time of each
	 * timemeasurement and puts them in the sorted arrays waitArray, synchArray
	 * and sojArray respectively.
	 * 
	 * @param piList
	 *            ArrayList: the process instance on which the metrics
	 * @param fitOption
	 *            int: the used fit option (how to deal with conformance)
	 * @param failedInstances
	 *            HashSet: set of traces that executed executed unsuccessfully
	 *            or terminated unproperly
	 */
	public void calculateMetrics(ArrayList piList, int fitOption,
			HashSet failedInstances) {
		waitStatistics.clear();
		syncStatistics.clear();
		sojStatistics.clear();
		arrivalStatistics.clear();
		ArrayList betweenDates = new ArrayList();
		String[] names = (String[]) piList.toArray(new String[0]);
		Arrays.sort(names);
		Iterator it = timeMeasurements.iterator();
		while (it.hasNext()) {
			TimeMeasurement tm = (TimeMeasurement) it.next();
			if (Arrays.binarySearch(names, tm.piName) <= -1) {
				// no process instance in piList that corresponds to tm.piName
				continue;
			}
			if (fitOption == 0) {
				// time-metrics based on all selected traces
				waitStatistics.addValue(tm.waitTime);
				syncStatistics.addValue(tm.syncTime);
				sojStatistics.addValue(tm.syncTime + tm.waitTime);
				betweenDates.add(tm.arrivalDate);
			} else if (fitOption == 1 && !tm.failedBefore) {
				// time-metrics based on measurements taken in selected traces
				// before traces fail
				waitStatistics.addValue(tm.waitTime);
				syncStatistics.addValue(tm.syncTime);
				sojStatistics.addValue(tm.syncTime + tm.waitTime);
				betweenDates.add(tm.arrivalDate);
			} else if (fitOption == 2) {
				// time-metrics based on measurements taken in those traces
				// where no related transition fails
				boolean isUnaffected = true;
				ListIterator transitions = getRelatedTransitions()
						.listIterator();
				while (transitions.hasNext() && isUnaffected) {
					ExtendedTransition trans = (ExtendedTransition) transitions
							.next();
					if (trans.hasFailedExecution(tm.piName)) {
						isUnaffected = false;
					}
				}
				// add the times to the ArrayLists in case the trace has
				// completed
				// properly and succesfully
				if (isUnaffected) {
					waitStatistics.addValue(tm.waitTime);
					syncStatistics.addValue(tm.syncTime);
					sojStatistics.addValue(tm.syncTime + tm.waitTime);
					betweenDates.add(tm.arrivalDate);
				}
			} else if (fitOption == 3 && !failedInstances.contains(tm.piName)) {
				waitStatistics.addValue(tm.waitTime);
				syncStatistics.addValue(tm.syncTime);
				sojStatistics.addValue(tm.syncTime + tm.waitTime);
				betweenDates.add(tm.arrivalDate);
			}
		}
		// place the between dates in a sorted array
		Date[] dates = (Date[]) betweenDates.toArray(new Date[0]);
		Arrays.sort(dates);
		if (dates.length > 1) {
			for (int i = 1; i < dates.length; i++) {
				long iat = dates[i].getTime() - dates[i - 1].getTime();
				arrivalStatistics.addValue(iat);
			}
		}
	}

	/**
	 * Exports all time measurements (waiting, synchronization, sojourn time)
	 * taken in process instances in piList and corresponding to this place to
	 * (a comma-seperated text) file.
	 * 
	 * @param piList
	 *            ArrayList: the process instances
	 * @param file
	 *            File: the file to which the measurements are exported
	 * @param divider
	 *            long: the used time divider
	 * @param sort
	 *            String: the used time sort
	 * @param fitOption
	 *            int: the fit option used (how to deal with non-conformance)
	 * @throws IOException
	 *             : can occur when connecting to file
	 */

	public void exportToFile(ArrayList piList, File file, long divider,
			String sort, int fitOption) throws IOException {
		String[] names = (String[]) piList.toArray(new String[0]);
		Arrays.sort(names);
		Writer output = new BufferedWriter(new FileWriter(file));
		String line = "Log Trace,Waiting time (" + sort
				+ "),Synchronization time (" + sort + "),Sojourn time (" + sort
				+ ")\n";
		output.write(line);
		Iterator it = timeMeasurements.iterator();
		while (it.hasNext()) {
			TimeMeasurement tm = (TimeMeasurement) it.next();
			if (Arrays.binarySearch(names, tm.piName) <= -1) {
				continue;
			}
			if (fitOption == 0) {
				// time-metrics based on all selected traces
				line = tm.piName + ",";
				line += (tm.waitTime * 1.0 / divider) + ",";
				line += (tm.syncTime * 1.0 / divider * 1.0) + ",";
				line += ((tm.waitTime + tm.syncTime) * 1.0 / divider * 1.0)
						+ "\n";
				// write line to the file
				output.write(line);
			}
			if (fitOption == 1 && !tm.failedBefore) {
				// time-metrics based on measurements taken in selected traces
				// before traces fail
				line = tm.piName + ",";
				line += (tm.waitTime * 1.0 / divider) + ",";
				line += (tm.syncTime * 1.0 / divider * 1.0) + ",";
				line += ((tm.waitTime + tm.syncTime) * 1.0 / divider * 1.0)
						+ "\n";
				// write line to the file
				output.write(line);
			}
			if (fitOption == 2) {
				// time-metrics based on measurements taken in those traces
				// where no related transition fails
				boolean isUnaffected = true;
				ListIterator transitions = getRelatedTransitions()
						.listIterator();
				while (transitions.hasNext() && isUnaffected) {
					ExtendedTransition trans = (ExtendedTransition) transitions
							.next();
					if (trans.hasFailedExecution(tm.piName)) {
						isUnaffected = false;
					}
				}

				// add the times to the ArrayLists in case the trace has
				// completed
				// properly and succesfully
				if (isUnaffected) {
					line = tm.piName + ",";
					line += (tm.waitTime * 1.0 / divider) + ",";
					line += (tm.syncTime * 1.0 / divider * 1.0) + ",";
					line += ((tm.waitTime + tm.syncTime) * 1.0 / divider * 1.0)
							+ "\n";
					// write line to the file
					output.write(line);
				}
			}
		}
		// close the file
		output.close();
	}

	// ///////////////////ACTIVITY METRICS METHODS/////////////////////////
	/**
	 * Sets the last transition that put a token in this place to trans
	 * 
	 * @param trans
	 *            ReplayedTransition: the transition that last put a token in
	 *            this place
	 */
	public void setEnablingTransition(ReplayedTransition trans) {
		enablingTransitions.add((ExtendedTransition) trans);
	}

	/**
	 * Removes the enabling transition and sets the final time of the activity
	 * corresponding to the enabling transition. But only when it is a different
	 * activity than the activity corresponding to transition rt
	 * 
	 * @param endTime
	 *            Date: the timestamp
	 * @param trace
	 *            ReplayedLogTrace: the trace that is currently being replayed
	 * @param rt
	 *            ReplayedTransition: the transition that called this method
	 */
	public void removeEnablingTransition(Date endTime, ReplayedLogTrace trace,
			ReplayedTransition rt) {
		String piName = trace.getName();
		ListIterator lit = enablingTransitions.listIterator();
		ExtendedTransition enablingTransition = null;
		if (lit.hasNext()) {
			enablingTransition = (ExtendedTransition) lit.next();
			if (enablingTransition != null
					&& enablingTransition.getAssociatedActivity() != null) {
				boolean failedBeforeSecond = false, secondFailed = false;
				// check whether the transition has failed in this process
				// instance
				if (((ExtendedTransition) rt).hasFailedExecution(piName)) {
					secondFailed = true;
				}
				ExtendedLogTrace lt = (ExtendedLogTrace) trace;
				if (!lt.hasProperlyTerminated()
						|| !lt.hasSuccessfullyExecuted()) {
					failedBeforeSecond = true;
				}
				try {
					if (!rt.getLogEvent().getModelElementName()
							.equalsIgnoreCase(
									enablingTransition.getLogEvent()
											.getModelElementName())) {
						// the endTime of the activity corresponding to the
						// enablingTransition is only
						// set when rt and enablingTransition correspond to
						// different activities
						enablingTransition.getAssociatedActivity()
								.setFinalTimeLast(piName, endTime,
										failedBeforeSecond, secondFailed);
						if (enablingTransitions.size() > 1) {
							// discard bound value for final time of enabling
							// transition if multiple tokens in input place of
							// rt
							enablingTransition.getAssociatedActivity()
									.setDiscardBoundFinal();
						}
						while (lit.hasNext()) {
							// discard bound value for all transitions if
							// multiple tokens in input place of rt
							ExtendedTransition trans = (ExtendedTransition) lit
									.next();
							trans.getAssociatedActivity()
									.setDiscardBoundFinal();
						}
						enablingTransitions.remove(enablingTransition);
						enablingTransition = null;
					}
				} catch (NullPointerException npe) {
					// can be caused by invisible transitions
				}
			}
		}
	}

	// ///////////////////GET AND SET METHODS//////////////////////

	/**
	 * Sets waiting time settings specific for this place
	 * 
	 * @param bnds
	 *            ArrayList: the bounds of the waiting time levels for this
	 *            place.
	 * @param cols
	 *            ArrayList: the colors of the waiting time levels for this
	 *            place
	 */
	public void setOwnSettings(ArrayList bnds, ArrayList cols) {
		hasOwnSettings = true;
		bounds = bnds;
		colors = cols;
	}

	/**
	 * sets hasSettings to has
	 * 
	 * @param has
	 *            boolean
	 */
	public void setHasOwnSettings(boolean has) {
		hasOwnSettings = has;
	}

	/**
	 * Returns true if the place has its own bottleneck settings, else false
	 * 
	 * @return boolean
	 */
	public boolean hasSettings() {
		return (hasOwnSettings);
	}

	/**
	 * Returns the bounds of the place's bottleneck settings
	 * 
	 * @return ArrayList
	 */
	public ArrayList getBounds() {
		return (bounds);
	}

	/**
	 * Returns the colors of the place's bottleneck settings
	 * 
	 * @return ArrayList
	 */
	public ArrayList getColors() {
		return (colors);
	}

	/**
	 * Returns the transitions that are related to this place, i.e., all
	 * transitions for which this place is an input or an output place.
	 * 
	 * @return ArrayList
	 */
	public ArrayList getRelatedTransitions() {
		Iterator inedges = this.getInEdgesIterator();
		Iterator outedges = this.getOutEdgesIterator();
		HashSet transitionSet = new HashSet();
		while (inedges.hasNext()) {
			ExtendedPNEdge edge = (ExtendedPNEdge) inedges.next();
			transitionSet.add((ExtendedTransition) edge.getSource());
		}
		while (outedges.hasNext()) {
			ExtendedPNEdge edge = (ExtendedPNEdge) outedges.next();
			transitionSet.add((ExtendedTransition) edge.getDest());
		}
		return (new ArrayList(transitionSet));
	}

	/**
	 * Returns the mean waiting time of this place, calculated based on the
	 * times in waitStatistics. Make sure method calculateMetrics is called
	 * before this one.
	 * 
	 * @return double
	 */
	public double getMeanWaitingTime() {
		return waitStatistics.getMean();
	}

	/**
	 * Returns the minimum waiting time of this place, based on the waiting
	 * times in array waitStatistics (make sure method calculateMetrics is
	 * called before this one)
	 * 
	 * @return double
	 */
	public double getMinWaitingTime() {
		return waitStatistics.getMin();
	}

	/**
	 * Returns the maximum waiting time of this place, based on the waiting
	 * times in array waitStatistics (make sure method calculateMetrics is
	 * called before this one)
	 * 
	 * @return double
	 */
	public double getMaxWaitingTime() {
		return waitStatistics.getMax();
	}

	/**
	 * Returns the standard deviation in waiting time of this place, based on
	 * the waiting times in waitStatistics (make sure calculateMetrics is called
	 * before this method) and meanTime.
	 * 
	 * @return double
	 */
	public double getStdevWaitingTime() {
		return waitStatistics.getStandardDeviation();
	}

	/**
	 * Returns the mean waiting time of this place, calculated based on the
	 * times in syncStatistics. Make sure method calculateMetrics is called
	 * before this one.
	 * 
	 * @return double
	 */
	public double getMeanSynchronizationTime() {
		return syncStatistics.getMean();
	}

	/**
	 * Returns the minimum synchronization time of this place, based on the
	 * synchronization times in syncStatistics, make sure calculateMetrics is
	 * called before this method
	 * 
	 * @return double
	 */
	public double getMinSynchronizationTime() {
		return syncStatistics.getMin();
	}

	/**
	 * Returns the maximum synchronization time of this place, based on the
	 * synchronization times in syncStatistics, make sure calculateMetrics is
	 * called before this method
	 * 
	 * @return double
	 */
	public double getMaxSynchronizationTime() {
		return syncStatistics.getMax();
	}

	/**
	 * Returns the standard deviation in synchronization time of this place,
	 * based on the waiting times in syncStatistics (make sure calculateMetrics
	 * is called before this method) and meanTime.
	 * 
	 * @return double
	 */
	public double getStdevSynchronizationTime() {
		return syncStatistics.getStandardDeviation();
	}

	/**
	 * Returns the mean sojourn time of this place, calculated based on the
	 * sojourn times as present in sojStatistics (make sure method
	 * calculateMetrics is called before this method.
	 * 
	 * @return double
	 */
	public double getMeanSojournTime() {
		return sojStatistics.getMean();
	}

	/**
	 * Returns the minimum sojourn time of this place, based on the sojourn
	 * times as present in sojStatistics (make sure method calculateMetrics is
	 * called before this method.
	 * 
	 * @return double
	 */
	public double getMinSojournTime() {
		return sojStatistics.getMin();
	}

	/**
	 * Returns the maximum sojourn time of this place, based on the sojourn
	 * times as present in sojStatistics (make sure method calculateMetrics is
	 * called before this method.
	 * 
	 * @return double
	 */
	public double getMaxSojournTime() {
		return sojStatistics.getMax();
	}

	/**
	 * Returns the standard deviation in sojourn time of this place, based on
	 * piList and meanTime.
	 * 
	 * @return double
	 */
	public double getStdevSojournTime() {
		return sojStatistics.getStandardDeviation();
	}

	/**
	 * Returns the average of fast, slow and normal speed waiting/synch/soj
	 * times Where [0..2]: fast wait-sync-soj [3..5]: slow wait-sync-soj [6..8]:
	 * middle wait-sync-soj
	 * 
	 * @param fastestPercentage
	 *            double: percentage of 'fast' times
	 * @param slowestPercentage
	 *            double: percentage of 'slow' times
	 * @return double[]
	 */
	public double[] getAvgTimes(double fastestPercentage,
			double slowestPercentage) {
		long waitTotal = 0, syncTotal = 0, sojTotal = 0;
		double[] waitArray = waitStatistics.getSortedValues();
		double[] syncArray = syncStatistics.getSortedValues();
		double[] sojArray = sojStatistics.getSortedValues();

		int[] sizes = getSizes(fastestPercentage, slowestPercentage);
		int fastSize = sizes[0], slowSize = sizes[1], middleSize = sizes[2];
		// since waitArray, syncArray and sojArray are of the same size we can
		// run through all three arrays at the same time
		for (int i = 0; i < fastSize; i++) {
			waitTotal += waitArray[i];
			syncTotal += syncArray[i];
			sojTotal += sojArray[i];
		}
		double avgFastWaitTime = 0, avgFastSyncTime = 0, avgFastSojTime = 0;
		if (fastSize != 0) {
			avgFastWaitTime = (waitTotal * 1.0) / fastSize;
			avgFastSyncTime = (syncTotal * 1.0) / fastSize;
			avgFastSojTime = (sojTotal * 1.0) / fastSize;
		}
		// calculate average of the slowest traces
		int upperSize = waitArray.length - slowSize;
		waitTotal = 0;
		syncTotal = 0;
		sojTotal = 0;
		for (int i = upperSize; i < waitArray.length; i++) {
			waitTotal += waitArray[i];
			syncTotal += syncArray[i];
			sojTotal += sojArray[i];
		}
		double avgSlowWaitTime = 0, avgSlowSyncTime = 0, avgSlowSojTime = 0;
		if (slowSize > 0) {
			avgSlowWaitTime = (waitTotal * 1.0) / slowSize;
			avgSlowSyncTime = (syncTotal * 1.0) / slowSize;
			avgSlowSojTime = (sojTotal * 1.0) / slowSize;
		}

		// calculate the middle/normal-speed traces
		waitTotal = 0;
		syncTotal = 0;
		sojTotal = 0;
		for (int i = fastSize; i < upperSize; i++) {
			waitTotal += waitArray[i];
			syncTotal += syncArray[i];
			sojTotal += sojArray[i];
		}

		double avgMiddleWaitTime = 0, avgMiddleSyncTime = 0, avgMiddleSojTime = 0;
		if (middleSize > 0) {
			avgMiddleWaitTime = (waitTotal * 1.0) / middleSize;
			avgMiddleSyncTime = (syncTotal * 1.0) / middleSize;
			avgMiddleSojTime = (sojTotal * 1.0) / middleSize;
		}
		double[] timeArray = new double[9];
		timeArray[0] = avgFastWaitTime;
		timeArray[1] = avgFastSyncTime;
		timeArray[2] = avgFastSojTime;
		timeArray[3] = avgSlowWaitTime;
		timeArray[4] = avgSlowSyncTime;
		timeArray[5] = avgSlowSojTime;
		timeArray[6] = avgMiddleWaitTime;
		timeArray[7] = avgMiddleSyncTime;
		timeArray[8] = avgMiddleSojTime;
		return timeArray;
	}

	/**
	 * Returns an array containing the number of measurements that are
	 * considered to be fast (place 0 in array), the number of measurements that
	 * are considered to be slow (place 1 in array) and the number of
	 * measurements that are of 'normal' speed (place 2 in array). Based on
	 * fastestPercentage, slowestPercentage and waitArray (thus method
	 * calculateMetrics() should be called before this one)
	 * 
	 * @param fastestPercentage
	 *            double
	 * @param slowestPercentage
	 *            double
	 * @return int[]
	 */
	public int[] getSizes(double fastestPercentage, double slowestPercentage) {
		int[] sizes = new int[3];
		String sizeString;
		int length = waitStatistics.getValues().length;
		sizeString = Math.round((length * fastestPercentage) / 100.0) + "";
		sizes[0] = Integer.parseInt(sizeString);
		if (sizes[0] != length) {
			sizeString = Math.round((length * slowestPercentage) / 100.0) + "";
			sizes[1] = Integer.parseInt(sizeString);
			if ((sizes[0] + sizes[1]) > length) {
				// Make sure that sizes[0] + sizes[2] remains smaller than
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
	 * Returns the frequency of cases that have visited this place, based on the
	 * process instances in piList, make sure calculateMetrics is called before
	 * this method
	 * 
	 * @return long
	 */
	public long getFrequency() {
		return (waitStatistics.getN());
	}

	/**
	 * Returns the total frequency of tokens that leave this place in a regular
	 * fashion (not artificially), counting only the process instances in piList
	 * This method is needed to be able to calculate probabilities at XOR-splits
	 * 
	 * @param piList
	 *            ArrayList: the process instances
	 * @param fitOption
	 *            int: the fit Option used (how to deal with conformance)
	 * @param failedInstances
	 *            HashSet: set of traces that executed executed unsuccessfully
	 *            or terminated unproperly
	 * @return long
	 */
	public long getTotalOutEdgeFrequency(ArrayList piList, int fitOption,
			HashSet failedInstances) {
		String[] names = (String[]) piList.toArray(new String[0]);
		Arrays.sort(names);
		ListIterator outEdges = this.getOutEdges().listIterator();
		int number = 0;
		while (outEdges.hasNext()) {
			ExtendedPNEdge thisedge = (ExtendedPNEdge) outEdges.next();
			number += thisedge.getFrequency(piList, fitOption, failedInstances);
		}
		return (number);
	}

	/**
	 * Returns the arrival rate of cases in this place.
	 * 
	 * @return double
	 */
	public double getArrivalRate() {
		double rate = 0;
		if (arrivalStatistics.getN() > 0 && arrivalStatistics.getMean() != 0) {
			// calculate mean arrival rate from mean arrival time
			rate = (1 / arrivalStatistics.getMean());
		}
		// return the mean arrival rate
		return rate;
	}

	// //////////////// GENERAL PURPOSE METHODS
	// /////////////////////////////////

	/**
	 * Makes a deep copy of the object. Note that this method needs to be
	 * extended as soon as there are attributes added to the class which are not
	 * primitive or immutable.
	 * 
	 * @return Object the cloned object
	 */
	public Object clone() {
		ExtendedPlace o = null;
		o = (ExtendedPlace) super.clone();
		// clone referenced objects to realize deep copy

		if (timeMeasurements != null) {
			o.timeMeasurements = (ArrayList) timeMeasurements.clone();
		}
		return o;
	}

	/**
	 * Data structure consisting of some performance-related measurements for
	 * this place. <br>
	 * Suppose there is a transition with two input places p1 and p2, which has
	 * a firing time stamp t. The tokens in those input places also have a
	 * timestamp tj (which is derived from the transition that had produced the
	 * token): t1 and t2, respectively. Then we can define tm as the greatest
	 * timestamp of the input-place tokens, that is, max(t1,t2).
	 * 
	 * @see TimeMeasurement#waitTime waitTime
	 * @see TimeMeasurement#syncTime syncTime
	 * 
	 * @author Boudewijn van Dongen
	 */
	static class TimeMeasurement implements Cloneable {

		/**
		 * The processID for which this measurement has been taken.
		 */
		public String piName;

		/**
		 * The waiting time is the time a token spends time in a place although
		 * the transition is already enabled (i.e., t - tm).
		 */
		public long waitTime;

		/**
		 * The synchronization time is the time the token spends in a place
		 * until the transition becomes enabled (i.e., tm - tj).
		 */
		public long syncTime;

		/**
		 * The arrival date is needed for calculation of the arrival rate
		 */
		public Date arrivalDate;

		/**
		 * If this time-measurement was taken after a task failed execution in
		 * the log trace with processID piName, then this boolean is true else
		 * false
		 */
		public boolean failedBefore = false;

		/**
		 * Creates a new time measurement data structure.
		 * 
		 * @param piName
		 *            the processID for which this measurement has been taken
		 * @param waitTime
		 *            the waiting time is the time a token spends time in a
		 *            place although the transition is already enabled (i.e., t
		 *            - tm)
		 * @param syncTime
		 *            the synchronization time is the time the token spends in a
		 *            place until the transition becomes enabled (i.e., tm - tj)
		 * @param tokenAvailable
		 *            the date of arrival of a token
		 * @param failed
		 *            boolean
		 */
		public TimeMeasurement(String piName, long waitTime, long syncTime,
				Date tokenAvailable, boolean failed) {
			this.piName = piName;
			this.waitTime = waitTime;
			this.syncTime = syncTime;
			arrivalDate = tokenAvailable;
			failedBefore = failed;
		}

		/**
		 * Creates a string of the measured values as an HTML table row.
		 * 
		 * @return a string containing the processID, the synchronization time,
		 *         and then the waiting time
		 */
		public String toString() {
			return "<tr><td>" + piName + "</td>" + "<td>" + syncTime + "</td>"
					+ "<td>" + waitTime + "</td></tr>";
		}

		/**
		 * Make a deep copy of the object. Note that this method needs to be
		 * extended as soon as there are attributes added to the class which are
		 * not primitive or immutable.
		 * 
		 * @return Object The cloned object.
		 */
		public Object clone() {
			TimeMeasurement o = null;
			try {
				o = (TimeMeasurement) super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			// clone referenced objects to realize deep copy
			return o;
		}
	}
}
