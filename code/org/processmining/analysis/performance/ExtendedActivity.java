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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.ListIterator;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

/**
 * This class is used to represent activities and to enhance these activities
 * with performance related information, such as the waiting time, execution
 * time, sojourn time and the like.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 * @version 1.0
 * 
 */
public class ExtendedActivity {
	// name of the activity
	private String name;
	// ArrayList that contains all measurements for this activity
	private ArrayList measurements;
	// Used to keep track of waiting time measurements for this activity and for
	// calculation of statistical values(eg mean, stdev)
	private DescriptiveStatistics waitStatistics = DescriptiveStatistics
			.newInstance();
	// Used to keep track of execution time measurements for this activity and
	// for
	// calculation of statistical values(eg mean, stdev)
	private DescriptiveStatistics executionStatistics = DescriptiveStatistics
			.newInstance();
	// Used to keep track of sojourn time measurements for this activity and for
	// calculation of statistical values(eg mean, stdev)
	private DescriptiveStatistics sojournStatistics = DescriptiveStatistics
			.newInstance();
	// Used to keep track of upper bound sojourn time measurements for this
	// activity and for
	// calculation of statistical values(eg mean, stdev). Here times can be
	// stored
	// when a log does not contain any complete events
	private DescriptiveStatistics upperBoundSojournStatistics = DescriptiveStatistics
			.newInstance();
	// Used to keep track of upper bound waiting time measurements for this
	// activity and for
	// calculation of statistical values(eg mean, stdev). Here times can be
	// stored
	// when a log does not contain any complete events
	private DescriptiveStatistics upperBoundWaitStatistics = DescriptiveStatistics
			.newInstance();
	// Used to keep track of upper bound execution time measurements for this
	// activity and for
	// calculation of statistical values(eg mean, stdev). Here times can be
	// stored
	// when a log does not contain any complete events
	private DescriptiveStatistics upperBoundExecutionStatistics = DescriptiveStatistics
			.newInstance();
	// Used for calculation of inter arrival times for this activity
	private SummaryStatistics interArrivalStatistics = SummaryStatistics
			.newInstance();
	// variables, set to true when bound-value is used instead of exact value
	private boolean boundSojournUsed = false;
	private boolean boundWaitingUsed = false;
	private boolean boundExecutionUsed = false;

	/**
	 * Creates and initializes a new ExtendedActivity-object.
	 * 
	 * @param aName
	 *            String: name of the activity
	 */
	public ExtendedActivity(String aName) {
		name = aName;
		measurements = new ArrayList();
	}

	/**
	 * Adds a new measurement or adjusts the last measurement in ArrayList
	 * measurements. Called when a transition corresponding to the activitity
	 * fires.
	 * 
	 * @param piName
	 *            String: process instance ID in which the measurement was taken
	 * @param timestamp
	 *            Date: measured timestamp
	 * @param transEnabled
	 *            Date: timestamp at which the transition that calls this method
	 *            was enabled
	 * @param type
	 *            String: event type that occurred
	 * @param transitionFailed
	 *            String: contains the event-type of the transition if it did
	 *            not fire regularly
	 * @param failedBefore
	 *            boolean: true in case a transition in the trace with name
	 *            piName failed, before this method was called
	 * 
	 */
	public void measure(String piName, Date timestamp, Date transEnabled,
			String type, String transitionFailed, boolean failedBefore) {
		if (measurements.size() == 0) {
			// no measurements taken for this activity yet, so create the first
			// one
			ActivityMeasurement meas = new ActivityMeasurement(piName,
					transEnabled);
			meas.setDate(timestamp, type, transitionFailed, failedBefore);
			// add the new measurements to the measurements ArrayList
			measurements.add(meas);
		} else {
			// There already exist measurements for this activity, obtain the
			// last measurement
			ActivityMeasurement lastMeasurement = (ActivityMeasurement) measurements
					.get(measurements.size() - 1);
			if (!piName.equals(lastMeasurement.piName)) {
				// this trace is another one than the last, so measurement
				// should be a new one
				ActivityMeasurement meas = new ActivityMeasurement(piName,
						transEnabled);
				meas.setDate(timestamp, type, transitionFailed, failedBefore);
				// add measurement to the measurements-list
				measurements.add(meas);
			} else {
				if (lastMeasurement.completeDate != null
						|| lastMeasurement.abortDate != null) {
					// last measurement was completed or aborted, so create a
					// new measurement
					ActivityMeasurement meas = new ActivityMeasurement(piName,
							transEnabled);
					meas.setDate(timestamp, type, transitionFailed,
							failedBefore);
					// and add the measurement to the measurement-list
					measurements.add(meas);
				} else {
					if (type.equalsIgnoreCase("schedule")) {
						// schedule event
						if (lastMeasurement.scheduleDate == null) {
							lastMeasurement.setDate(timestamp, type,
									transitionFailed, failedBefore);
						} else {
							// under assumption that an activity gets scheduled
							// only once, create a new activity measurement
							ActivityMeasurement meas = new ActivityMeasurement(
									piName, transEnabled);
							meas.setDate(timestamp, type, transitionFailed,
									failedBefore);
							measurements.add(meas);
						}
					} else if (type.equalsIgnoreCase("assign")) {
						// assign event
						if (lastMeasurement.assignDate == null) {
							lastMeasurement.setDate(timestamp, type,
									transitionFailed, failedBefore);
						} else {
							// under assumption that an activity gets assigned
							// only once, create a new activity measurement
							// (can be reassigned more often though)
							ActivityMeasurement meas = new ActivityMeasurement(
									piName, transEnabled);
							meas.setDate(timestamp, type, transitionFailed,
									failedBefore);
							measurements.add(meas);
						}
					} else if (type.equalsIgnoreCase("start")) {
						// start event
						if (lastMeasurement.startDate == null) {
							lastMeasurement.setDate(timestamp, type,
									transitionFailed, failedBefore);
						} else {
							// under assumption that an activity is started
							// only once, create a new activity measurement
							ActivityMeasurement meas = new ActivityMeasurement(
									piName, transEnabled);
							meas.setDate(timestamp, type, transitionFailed,
									failedBefore);
							measurements.add(meas);
						}
					} else {
						// for all other event types
						lastMeasurement.setDate(timestamp, type,
								transitionFailed, failedBefore);
					}
				}
			}
		}
	}

	/**
	 * Sets the end date of the last measurement to timeStamp if timeStamp
	 * occured before the end date of the last measurement or the end date is
	 * currently null. If the transition after the last transition corresponding
	 * to the activity failed, then "end" is added to failedTransitions of the
	 * last measurement. Furthermore, if a transition in the trace piName failed
	 * execution before the transition after the last transition executed, then
	 * "end" is added to failedBeforeTransitions of the last measurement.
	 * 
	 * @param piName
	 *            String: Name of process instance
	 * @param timeStamp
	 *            Date: Date to which the end date of the last measurement is
	 *            compared and possibly set (if before the current end date)
	 * @param failedBeforeEnd
	 *            boolean: true if the trace piName failed before this method
	 *            was called
	 * @param failedEnd
	 *            boolean: true if the transition after the last transition
	 *            corresponding to this activity failed execution
	 */
	public void setFinalTimeLast(String piName, Date timeStamp,
			boolean failedBeforeEnd, boolean failedEnd) {

		if (measurements.size() > 0) {
			// only add if at least one measurement corresponding to this
			// activity exists
			ActivityMeasurement lastMeasurement = (ActivityMeasurement) measurements
					.get(measurements.size() - 1);
			if (piName.equals(lastMeasurement.piName)) {
				if (lastMeasurement.endDate == null
						|| !lastMeasurement.endDate.before(timeStamp)) {
					lastMeasurement.endDate = timeStamp;
					if (failedBeforeEnd) {
						lastMeasurement.failedBeforeTransitions.add("end");
					}
					if (failedEnd) {
						lastMeasurement.failedTransitions.add("end");
					}
				}
			}

		}
	}

	public void setDiscardBoundFinal() {
		ActivityMeasurement lastMeasurement = (ActivityMeasurement) measurements
				.get(measurements.size() - 1);
		lastMeasurement.discardFinal = true;
	}

	/**
	 * Calculates waiting time, execution time and sojourn time of the activity
	 * for each measurement, if possible, and places these times in
	 * waitStatistics, executionStatistics and sojournStatistics respectively.
	 * In case information is incomplete (i.e. not schedule-start-complete
	 * present) then upper bound for these time-metrics are derived, if possible
	 * and placed in upperBoundWaitStatistics, upperBoundExecutionStatistics and
	 * upperBoundSojournStatistics (e.g. when the log only contains complete
	 * events, then it may be possible to derive an upper bound for the sojourn
	 * time
	 * 
	 * @param piList
	 *            ArrayList: Names of the process instances that are considered
	 * @param fitOption
	 *            int: selected fitness/conformance option (calculation of
	 *            activity metrics is dependent on how we deal with
	 *            non-conformance)
	 * @param failedInstances
	 *            HashSet: set of traces that executed executed unsuccessfully
	 *            or terminated unproperly
	 */
	public void calculateMetrics(ArrayList piList, int fitOption,
			HashSet failedInstances) {
		String[] names = (String[]) piList.toArray(new String[0]);
		Arrays.sort(names);
		// clear all previously recorded statistics
		waitStatistics.clear();
		executionStatistics.clear();
		sojournStatistics.clear();
		upperBoundSojournStatistics.clear();
		upperBoundWaitStatistics.clear();
		upperBoundExecutionStatistics.clear();
		interArrivalStatistics.clear();
		ArrayList arrivalDates = new ArrayList();
		// run through activity measurements of this activity
		ListIterator lit = measurements.listIterator();
		while (lit.hasNext()) {
			ActivityMeasurement am = (ActivityMeasurement) lit.next();
			if (Arrays.binarySearch(names, am.piName) <= -1) {
				// no process instance in piList that corresponds to am.piName
				continue;
			}
			if (fitOption == 0) {
				if (am.enabledDate != null) {
					// add arrival date
					arrivalDates.add(am.enabledDate);
				}
			}
			if (fitOption == 4 && !failedInstances.contains(am.piName)) {
				if (am.enabledDate != null) {
					// add arrival date
					arrivalDates.add(am.enabledDate);
				}
			} else if (fitOption == 4) {
				continue;
			}
			if (fitOption == 1
					&& (am.failedBeforeTransitions.contains("schedule")
							|| am.failedBeforeTransitions.contains("assign")
							|| am.failedBeforeTransitions.contains("reassign") || am.failedBeforeTransitions
							.contains("start"))) {
				// the log trace failed before a measurement for this activity
				// was taken
				continue;
			} else if (fitOption == 1) {
				if (am.enabledDate != null) {
					// add arrival date
					arrivalDates.add(am.enabledDate);
				}
			}
			if (fitOption == 2
					&& am.failedTransitions.size() > 0
					&& !(am.failedTransitions.size() == 1
							&& am.failedTransitions.contains("end") && am.completeDate != null)) {
				// if the set of failed transitions is larger than 0 and does
				// not
				// only contain 'end'
				continue;
			} else if (fitOption == 2) {
				if (am.enabledDate != null) {
					// add arrival date
					arrivalDates.add(am.enabledDate);
				}
			}
			if (fitOption == 3 && am.failedTransitions.contains("start")) {
				// in case a start-transition fails, all time-metrics are
				// useless
				continue;
			} else if (fitOption == 3) {
				if (am.enabledDate != null) {
					// add arrival date
					arrivalDates.add(am.enabledDate);
				}
			}
			if ((am.scheduleDate != null)
					&& (am.startDate != null)
					&& (am.startDate.getTime() - am.scheduleDate.getTime() >= 0)) {
				if (!(fitOption == 3 && am.failedTransitions != null && (am.failedTransitions
						.contains("schedule")
						|| am.failedTransitions.contains("assign") || am.failedTransitions
						.contains("reassign")))) {
					// add waiting time
					waitStatistics.addValue(am.startDate.getTime()
							- am.scheduleDate.getTime());
				}
			}
			if (fitOption == 1
					&& (am.failedBeforeTransitions.contains("complete")
							|| am.failedBeforeTransitions.contains("suspend") || am.failedBeforeTransitions
							.contains("resume"))) {
				// the log trace failed before an execution or sojourn
				// measurement for this activity was taken
				continue;
			}
			if (fitOption == 3
					&& am.failedTransitions != null
					&& (am.failedTransitions.contains("complete")
							|| am.failedTransitions.contains("suspend") || am.failedTransitions
							.contains("resume"))) {
				// in case a complete, suspend or resume-transition fails, only
				// waiting times can be used
				continue;
			}
			if ((am.completeDate != null) && (am.enabledDate != null)// ((am.scheduleDate
					// !=
					// null)
					// ||
					// )
					&& (am.completeDate.getTime() - am.enabledDate.getTime() >= 0)) {
				if (am.scheduleDate != null) {
					// add sojourn time (as time between schedule and complete)
					sojournStatistics.addValue(am.completeDate.getTime()
							- am.scheduleDate.getTime());
				} else {
					// add sojourn time (as time between enabling and complete)
					sojournStatistics.addValue(am.completeDate.getTime()
							- am.enabledDate.getTime());
					boundSojournUsed = true;
				}
			}
			if (am.endDate != null) {
				if (!am.discardFinal && am.abortDate == null) {
					if ((am.scheduleDate != null)
							&& (am.endDate.getTime()
									- am.scheduleDate.getTime() >= 0)
							&& !(((fitOption == 3 || fitOption == 2) && (am.failedTransitions
									.contains("end") || am.failedTransitions
									.contains("schedule"))) || (fitOption == 1 && am.failedBeforeTransitions
									.contains("end")))) {
						// add upper bound of sojourn time
						upperBoundSojournStatistics.addValue(am.endDate
								.getTime()
								- am.scheduleDate.getTime());
					} else if ((am.enabledDate != null)
							&& (am.endDate.getTime() - am.enabledDate.getTime() >= 0)
							&& !(((fitOption == 3 || fitOption == 2) && am.failedTransitions
									.contains("end")) || (fitOption == 1 && am.failedBeforeTransitions
									.contains("end")))) {
						// add upper bound of sojourn time
						upperBoundSojournStatistics.addValue(am.endDate
								.getTime()
								- am.enabledDate.getTime());
					}
				}
			}
			if ((am.startDate != null)
					&& (am.completeDate != null)
					&& (am.completeDate.getTime() - am.startDate.getTime() >= 0)) {
				// add execution time
				executionStatistics.addValue(am.completeDate.getTime()
						- am.startDate.getTime() - am.suspendedTime);
			}
			if (am.startDate != null) {
				if (am.enabledDate != null
						&& (am.startDate.getTime() - am.enabledDate.getTime() >= 0)) {
					// add upper bound of waiting time
					upperBoundWaitStatistics.addValue(am.startDate.getTime()
							- am.enabledDate.getTime());
				}
				if (am.endDate != null
						&& (am.endDate.getTime() - am.startDate.getTime() >= 0)) {
					if (!(((fitOption == 3 || fitOption == 2)
							&& !am.discardFinal && am.abortDate == null && (am.failedTransitions
							.contains("end"))) || (fitOption == 1 && am.failedBeforeTransitions
							.contains("end")))) {
						// add upper bound of execution time
						upperBoundExecutionStatistics.addValue(am.endDate
								.getTime()
								- am.startDate.getTime() - am.suspendedTime);
					}
				}
			}
		}
		if (arrivalDates.size() > 0) {
			Date[] dates = (Date[]) arrivalDates.toArray(new Date[0]);
			Arrays.sort(dates);
			for (int i = 1; i < dates.length; i++) {
				interArrivalStatistics.addValue(dates[i].getTime()
						- dates[i - 1].getTime());
			}
		}
	}

	/**
	 * Exports all time measurements (waiting, execution, sojourn time) taken in
	 * process instances in piList and corresponding to this activity to (a
	 * comma-seperated text) file.
	 * 
	 * @param piList
	 *            ArrayList
	 * @param file
	 *            File
	 * @param divider
	 *            long
	 * @param sort
	 *            String
	 * @param fitOption
	 *            int
	 * @throws IOException
	 */
	public void exportToFile(ArrayList piList, File file, long divider,
			String sort, int fitOption) throws IOException {
		String[] names = (String[]) piList.toArray(new String[0]);
		Arrays.sort(names);

		Writer output = new BufferedWriter(new FileWriter(file));
		String line = "Log trace, Waiting time (" + sort
				+ "), Execution time (" + sort + "),Sojourn time (" + sort
				+ ")\n";
		output.write(line);
		ListIterator lit = measurements.listIterator();
		while (lit.hasNext()) {
			ActivityMeasurement am = (ActivityMeasurement) lit.next();
			if (Arrays.binarySearch(names, am.piName) <= -1) {
				// no process instance in piList that corresponds to am.piName
				// or the current measurement should be ignored for other
				// reasons
				continue;
			}
			if (fitOption == 1
					&& (am.failedBeforeTransitions.contains("schedule")
							|| am.failedBeforeTransitions.contains("assign")
							|| am.failedBeforeTransitions.contains("reassign") || am.failedBeforeTransitions
							.contains("start"))) {
				// the log trace failed before a measurement for this activity
				// was taken
				continue;
			}
			if (fitOption == 2
					&& am.failedTransitions.size() > 0
					&& !(am.failedTransitions.size() == 1 && am.failedTransitions
							.contains("end"))) {
				// if the set of failed transitions is larger than 0 and does
				// not
				// only contain 'end'
				continue;
			}
			if (fitOption == 3 && am.failedTransitions.contains("start")) {
				// in case a start-transition fails, all time-metrics are
				// useless
				continue;
			}
			line = am.piName + ",";
			if ((am.scheduleDate != null)
					&& (am.startDate != null)
					&& (am.startDate.getTime() - am.scheduleDate.getTime() >= 0)) {
				if (!(fitOption == 3 && am.failedTransitions != null && (am.failedTransitions
						.contains("schedule")
						|| am.failedTransitions.contains("assign") || am.failedTransitions
						.contains("reassign")))) {
					// add waiting time
					line += ((am.startDate.getTime() - am.scheduleDate
							.getTime()) * 1.0 / divider)
							+ ",";
				} else {
					line += "-,";
				}
			} else if (am.startDate != null && am.enabledDate != null
					&& (am.startDate.getTime() - am.enabledDate.getTime() >= 0)) {
				line += ((am.startDate.getTime() - am.enabledDate.getTime()) * 1.0 / divider)
						+ ",";
			} else {
				line += "-,";
			}
			if (fitOption == 1
					&& (am.failedBeforeTransitions.contains("complete")
							|| am.failedBeforeTransitions.contains("suspend") || am.failedBeforeTransitions
							.contains("resume"))) {
				// the log trace failed before an execution or sojourn
				// measurement for this activity was taken
				line += "-,-\n";
				output.write(line);
				continue;
			}
			if (fitOption == 3
					&& am.failedTransitions != null
					&& (am.failedTransitions.contains("complete")
							|| am.failedTransitions.contains("suspend") || am.failedTransitions
							.contains("resume"))) {
				// in case a complete, suspend or resume-transition fails, only
				// waiting times can be used
				line += "-,-\n";
				output.write(line);
				continue;
			}

			if ((am.startDate != null)
					&& (am.completeDate != null)
					&& (am.completeDate.getTime() - am.startDate.getTime() >= 0)) {
				// add execution time
				line += ((am.completeDate.getTime() - am.startDate.getTime() - am.suspendedTime) * 1.0 / divider)
						+ ",";
			} else if (am.endDate != null && am.startDate != null
					&& (am.endDate.getTime() - am.startDate.getTime() >= 0)) {
				if (!(((fitOption == 3 || fitOption == 2) && (am.failedTransitions
						.contains("end"))) || (fitOption == 1 && am.failedBeforeTransitions
						.contains("end")))) {
					line += ((am.endDate.getTime() - am.startDate.getTime() - am.suspendedTime) * 1.0 / divider)
							+ ",";
				} else {
					line += "-,";
				}
			} else {
				line += "-,";
			}

			if ((am.completeDate != null) && (am.enabledDate != null)// ((am.scheduleDate
					// !=
					// null)
					// ||
					// )
					&& (am.completeDate.getTime() - am.enabledDate.getTime() >= 0)) {
				if (am.scheduleDate != null) {
					// add sojourn time (as time between schedule and complete)
					line += ((am.completeDate.getTime() - am.scheduleDate
							.getTime()) * 1.0 / divider)
							+ "\n";

				} else {
					// add sojourn time (as time between enabling and complete)
					// add sojourn time
					line += ((am.completeDate.getTime() - am.enabledDate
							.getTime()) * 1.0 / divider)
							+ "\n";
				}
			} else if ((am.scheduleDate != null) && (am.endDate != null)
					&& (am.endDate.getTime() - am.scheduleDate.getTime() >= 0)) {
				if (!(((fitOption == 3 || fitOption == 2) && (am.failedTransitions
						.contains("end") || am.failedTransitions
						.contains("schedule"))) || (fitOption == 1 && am.failedBeforeTransitions
						.contains("end")))) {
					line += ((am.endDate.getTime() - am.scheduleDate.getTime()) * 1.0 / divider)
							+ "\n";

				} else {
					line += "-\n";
				}
			} else {
				line += "-\n";
			}
			// write line to the file
			output.write(line);
		}
		// close the file
		output.close();
	}

	/**
	 * Checks whether normal time measurements can be used (sojourn time,
	 * waiting time, execution). If not, upper bounds of these time measurements
	 * are used instead (if possible).
	 */
	public void checkWhichMetricsToUse() {
		if (sojournStatistics.getN() == 0
				&& upperBoundSojournStatistics.getN() > 0) {
			sojournStatistics = upperBoundSojournStatistics;
			boundSojournUsed = true;
		}
		if (waitStatistics.getN() == 0 && upperBoundWaitStatistics.getN() > 0) {
			waitStatistics = upperBoundWaitStatistics;
			boundWaitingUsed = true;
		}
		if (executionStatistics.getN() == 0
				&& upperBoundExecutionStatistics.getN() > 0) {
			executionStatistics = upperBoundExecutionStatistics;
			boundExecutionUsed = true;
		}
	}

	// ////////////////////GET METHODS/////////////////////////
	/**
	 * Returns the name of the activity
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the mean waiting time of this activity. Note that the method
	 * calculateTimes should be called before this method.
	 * 
	 * @return double
	 */
	public double getMeanWaitTime() {
		return waitStatistics.getMean();
	}

	/**
	 * Returns the minimum waiting time of this activity. Note that the method
	 * calculateTimes should be called before this method.
	 * 
	 * @return double
	 */
	public double getMinWaitTime() {
		return waitStatistics.getMin();
	}

	/**
	 * Returns the maximum waiting time of this activity. Note that the method
	 * calculateTimes should be called before this method.
	 * 
	 * @return double
	 */
	public double getMaxWaitTime() {
		return waitStatistics.getMax();
	}

	/**
	 * Returns the standard deviation in waiting time of this activity. Note
	 * that the method calculateTimes should be called before this method.
	 * 
	 * @return double
	 */
	public double getStdevWaitTime() {
		return waitStatistics.getStandardDeviation();
	}

	/**
	 * Returns the number of waiting time measurements present for this activity
	 * 
	 * @return long
	 */
	public long getFrequencyWait() {
		return waitStatistics.getN();
	}

	/**
	 * Returns the average of fast, slow and normal speed waiting times in form
	 * of an array of type double. Where: [0..2]: fast-slow-normal
	 * 
	 * @param fastestPercentage
	 *            double: percentage of 'fast' times
	 * @param slowestPercentage
	 *            double: percentage of 'slow' times
	 * @return double[]
	 */
	public double[] getAvgWaitTimes(double fastestPercentage,
			double slowestPercentage) {
		return getAvgTimes(fastestPercentage, slowestPercentage, waitStatistics
				.getSortedValues());
	}

	/**
	 * Returns the mean execution time of this activity. Note that the method
	 * calculateTimes should be called before this method.
	 * 
	 * @return double
	 */
	public double getMeanExecutionTime() {
		return executionStatistics.getMean();
	}

	/**
	 * Returns the minimum execution time of this activity. Note that the method
	 * calculateTimes should be called before this method.
	 * 
	 * @return double
	 */
	public double getMinExecutionTime() {
		return executionStatistics.getMin();
	}

	/**
	 * Returns the maximum execution time of this activity. Note that the method
	 * calculateTimes should be called before this method.
	 * 
	 * @return double
	 */
	public double getMaxExecutionTime() {
		return executionStatistics.getMax();
	}

	/**
	 * Returns the standard deviation in execution time of this activity. Note
	 * that the method calculateTimes should be called before this method.
	 * 
	 * @return double
	 */
	public double getStdevExecutionTime() {
		return executionStatistics.getStandardDeviation();
	}

	/**
	 * Returns the number of execution time measurements present for this
	 * activity
	 * 
	 * @return long
	 */
	public long getFrequencyExecution() {
		return executionStatistics.getN();
	}

	/**
	 * Returns the average of fast, slow and normal speed execution times in
	 * form of an array of type double. Where: [0..2]: fast-slow-normal
	 * 
	 * @param fastestPercentage
	 *            double: percentage of 'fast' times
	 * @param slowestPercentage
	 *            double: percentage of 'slow' times
	 * @return double[]
	 */
	public double[] getAvgExecutionTimes(double fastestPercentage,
			double slowestPercentage) {
		return getAvgTimes(fastestPercentage, slowestPercentage,
				executionStatistics.getSortedValues());
	}

	/**
	 * Returns the mean sojourn time of this activity. Note that the method
	 * calculateTimes should be called before this method.
	 * 
	 * @return double
	 */
	public double getMeanSojournTime() {
		return sojournStatistics.getMean();
	}

	/**
	 * Returns the minimum sojourn time of this activity. Note that the method
	 * calculateTimes should be called before this method.
	 * 
	 * @return double
	 */
	public double getMinSojournTime() {
		return sojournStatistics.getMin();
	}

	/**
	 * Returns the maximum sojourn time of this activity. Note that the method
	 * calculateTimes should be called before this method.
	 * 
	 * @return double
	 */
	public double getMaxSojournTime() {
		return sojournStatistics.getMax();
	}

	/**
	 * Returns the standard deviation in sojourn time of this activity. Note
	 * that the method calculateTimes should be called before this method.
	 * 
	 * @return double
	 */
	public double getStdevSojournTime() {
		return sojournStatistics.getStandardDeviation();
	}

	/**
	 * Returns the number of sojourn time measurements present for this activity
	 * 
	 * @return long
	 */
	public long getFrequencySojourn() {
		return sojournStatistics.getN();
	}

	/**
	 * Returns the average of fast, slow and normal speed sojourn times in form
	 * of an array of type double. Where: [0..2]: fast-slow-normal
	 * 
	 * @param fastestPercentage
	 *            double: percentage of 'fast' times
	 * @param slowestPercentage
	 *            double: percentage of 'slow' times
	 * @return double[]
	 */
	public double[] getAvgSojournTimes(double fastestPercentage,
			double slowestPercentage) {
		return getAvgTimes(fastestPercentage, slowestPercentage,
				sojournStatistics.getSortedValues());
	}

	/**
	 * Returns the arrival rate
	 * 
	 * @return double
	 */
	public double getArrivalRate() {
		double rate = 0;
		if (interArrivalStatistics.getN() > 0
				&& interArrivalStatistics.getMean() != 0
				&& (waitStatistics.getN() > 0 || executionStatistics.getN() > 0 || sojournStatistics
						.getN() > 0)) {
			// calculate mean arrival rate from mean arrival time
			rate = (1 / interArrivalStatistics.getMean());
		}
		return rate;
	}

	/**
	 * Returns the average of fast, slow and normal speed times of the times
	 * that occur in the inputArray. Results are returned in form of an array.
	 * Where: [0..2]: fast-slow-normal
	 * 
	 * @param fastestPercentage
	 *            double
	 * @param slowestPercentage
	 *            double
	 * @param inputArray
	 *            double[]
	 * @return double[]
	 */
	private double[] getAvgTimes(double fastestPercentage,
			double slowestPercentage, double[] inputArray) {
		long total = 0;
		int[] sizes = getSizes(fastestPercentage, slowestPercentage,
				inputArray.length);
		int fastSize = sizes[0], slowSize = sizes[1], middleSize = sizes[2];
		for (int i = 0; i < fastSize; i++) {
			total += inputArray[i];
		}
		double avgFastTime = 0;
		if (fastSize != 0) {
			avgFastTime = (total * 1.0) / fastSize;
		}
		// calculate average of the slowest traces
		int upperSize = inputArray.length - slowSize;
		total = 0;
		for (int i = upperSize; i < inputArray.length; i++) {
			total += inputArray[i];
		}
		double avgSlowTime = 0;
		if (slowSize > 0) {
			avgSlowTime = (total * 1.0) / slowSize;
		}

		// calculate the middle/normal-speed traces
		total = 0;
		for (int i = fastSize; i < upperSize; i++) {
			total += inputArray[i];
		}
		double avgMiddleTime = 0;
		if (middleSize > 0) {
			avgMiddleTime = (total * 1.0) / middleSize;
		}
		double[] timeArray = new double[3];
		timeArray[0] = avgFastTime;
		timeArray[1] = avgSlowTime;
		timeArray[2] = avgMiddleTime;
		return timeArray;
	}

	/**
	 * Returns an array containing the number of measurements that are
	 * considered to be fast (place 0 in array), the number of measurements that
	 * are considered to be slow (place 1 in array) and the number of
	 * measurements that are of 'normal' speed (place 2 in array). Based on
	 * fastestPercentage, slowestPercentage and the given length.
	 * 
	 * @param fastestPercentage
	 *            double
	 * @param slowestPercentage
	 *            double
	 * @param length
	 *            int
	 * @return int[]
	 */
	public int[] getSizes(double fastestPercentage, double slowestPercentage,
			int length) {
		int[] sizes = new int[3];
		String sizeString;
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

	public boolean getBoundSojournUsed() {
		return boundSojournUsed;
	}

	public boolean getBoundWaitingUsed() {
		return boundWaitingUsed;
	}

	public boolean getBoundExecutionUsed() {
		return boundExecutionUsed;
	}

	/**
	 * Class used to store performance measurements corresponding to activities
	 * 
	 * @author Peter T.G. Hornix
	 */
	static class ActivityMeasurement implements Cloneable {
		// The processID for which this measurement has been taken.
		public String piName;
		// Date at which a transition belonging to this activity was first
		// enabled
		public Date enabledDate;
		// Date the activity was scheduled
		public Date scheduleDate;
		// Date the activity was assigned to someone/something (not used in
		// calculations)
		public Date assignDate;
		// Date at which the activity was started
		public Date startDate;
		// Date at which the activity was completed
		public Date completeDate;
		// Date at which the activity was aborted
		public Date abortDate;
		// Date at which the activity was suspended
		public Date suspendDate;
		// Date at which a transition of the next activity was enabled to fire
		public Date endDate;
		// Time the activity was suspended
		public long suspendedTime = 0;
		// Set of transitions corresponding to the activity that failed in the
		// trace in which this measurement was taken
		public HashSet failedTransitions = new HashSet();
		// Set of transitions corresponding to the activity, that fired after a
		// transition failed in the trace with name piName
		public HashSet failedBeforeTransitions = new HashSet();
		// Boolean to discard upper bounds that replace complete events
		public boolean discardFinal = false;

		/**
		 * Creates a new activity measurement data structure.
		 * 
		 * @param piName
		 *            String: the processID for which this measurement has been
		 *            taken
		 * @param firstEnabledDate
		 *            Date: the Date at which the activity was first enabled
		 */
		public ActivityMeasurement(String piName, Date firstEnabledDate) {
			this.piName = piName;
			enabledDate = firstEnabledDate;
		}

		/**
		 * Sets the date corresponding to the event-type that occurred to
		 * timestamp. Except when the event-type is resume, in that case the
		 * suspended time is adjusted. Next to this, this methods sets
		 * ignoreMeasurement to the value of `ignore`.
		 * 
		 * @param timestamp
		 *            Date to be set
		 * @param type
		 *            String Event-type
		 * @param failedTrans
		 *            String
		 * @param failedBefore
		 *            boolean
		 */
		public void setDate(Date timestamp, String type, String failedTrans,
				boolean failedBefore) {
			Collator c = Collator.getInstance();
			// treat upper and lower case letters as being equal
			c.setStrength(Collator.PRIMARY);
			if (c.equals(type, "schedule")) {
				scheduleDate = timestamp;
			} else if (c.equals(type, "start")) {
				startDate = timestamp;
			} else if (c.equals(type, "assign")) {
				assignDate = timestamp;
			} else if (c.equals(type, "suspend")) {
				suspendDate = timestamp;
			} else if (c.equals(type, "resume")) {
				if (suspendDate != null) {
					suspendedTime += timestamp.getTime()
							- suspendDate.getTime();
				}
			} else if (c.equals(type, "complete")) {

				completeDate = timestamp;
			} else if (c.equals(type, "abort") || c.equals(type, "withdraw")
					|| c.equals(type, "skip")) {
				// @todo: not sure, if these are the right event-types, adjust
				// if needed.
				abortDate = timestamp;
			}
			if (failedTrans != null) {
				failedTransitions.add(failedTrans);
			}
			if (failedBefore) {
				failedBeforeTransitions.add(type);
			}
		}

		/**
		 * Make a deep copy of the object. Note that this method needs to be
		 * extended as soon as there are attributes added to the class which are
		 * not primitive or immutable.
		 * 
		 * @return Object The cloned object.
		 */
		public Object clone() {
			ActivityMeasurement o = null;
			try {
				o = (ActivityMeasurement) super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			// clone referenced objects to realize deep copy
			return o;
		}
	}
}
