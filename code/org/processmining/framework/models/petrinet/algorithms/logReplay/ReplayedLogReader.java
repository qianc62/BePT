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

package org.processmining.framework.models.petrinet.algorithms.logReplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;

/**
 * A log reader involved in the log replay analysis method. It can be traversed
 * with methods like reset(), hasNext(), and next() like with the LogReader
 * class. Furthermore is used to extend the log reader interface with some more
 * log access related methods, like finding a process instance by its ID. <br>
 * Note that this implies that the whole log is kept in memory (this is not the
 * case for the LogReader class), which can be problematic as soon as the log
 * file becomes very big. However, since this class is intended to derive
 * subclasses which will store diagnostic information for each of the traces
 * this will happen anyway.
 * 
 * @see ReplayedLogTrace
 * @see ReplayedPetriNet
 * @see LogReplayAnalysisMethod
 * 
 * @author arozinat
 */
public class ReplayedLogReader {

	/**
	 * The encapsulated original log reader, which was used to create this
	 * enhanced log reader.
	 */
	protected LogReader originalLogReader;

	/**
	 * The list of diagnostic log traces contained in that log. They can be
	 * accessed via corresponding iterating methods like reset(), hasNext(), and
	 * next(), and the ordering corresponds to the ordering of the log.
	 */
	protected ArrayList logTraces = new ArrayList();

	/**
	 * Alternative data structure directly accessing all the diagnostic log
	 * traces contained in that log by their ID (i.e., their name).
	 */
	protected HashMap logTraceMap = new HashMap();

	/**
	 * List containing all log traces sorted with respect to the number of
	 * process instances represented by them in a descending manner (i.e., the
	 * trace representing the most process instances comes first, the one
	 * representing the least comes last). If there are two traces representing
	 * the same number of instances, their ordering is arbitrary.
	 */
	protected ArrayList sortedTraceList = new ArrayList();

	/**
	 * Step through the diagnostic log offering access to the diagnostic log
	 * traces via methods like reset(), hasNext(), and next().
	 */
	protected Iterator logIterator;

	/**
	 * Construct an enhanced log reader by wrapping the original one.
	 * Furthermore the diagnostic data structures are initialized by wrapping
	 * all process instances in a diagnostic log trace. Note that it is assumed
	 * here that the log might have been pre-processed and similar instances got
	 * summarized (since they share the same diagnostic results), i.e., that the
	 * log has already been aggregated on a logical level.
	 * 
	 * @param logReader
	 *            the original log reader
	 */
	public ReplayedLogReader(LogReader logReader) {
		originalLogReader = logReader;
		init(logReader);
	}

	/**
	 * Constructs an enhanced log reader by wrapping the original one. <br>
	 * Note that the diagnostic data structures are initialized in deriving
	 * subclasses by wrapping all process instances in a custom log trace.
	 * 
	 * @param logReader
	 *            the original log reader
	 */
	private void init(LogReader logReader) {
		Iterator it = originalLogReader.instanceIterator();
		while (it.hasNext()) {
			ProcessInstance currentInstance = (ProcessInstance) it.next();
			ReplayedLogTrace newTrace = createReplayedLogTrace(currentInstance);
			logTraces.add(newTrace);
			// TODO - use actual ID to store the traces (in general, it cannot
			// be assumed that the
			// name of a process instance is unique) to be checked what is
			// better to display to the user
			logTraceMap.put(newTrace.getProcessInstance().getName(), newTrace);
			if (sortedTraceList.size() == 0) {
				// first element - add anyway
				sortedTraceList.add(newTrace);
			} else {
				// put current trace at the right place in sorted list
				Iterator alreadySortedTraces = sortedTraceList.iterator();
				while (alreadySortedTraces.hasNext()) {
					ReplayedLogTrace currentAlreadySorted = (ReplayedLogTrace) alreadySortedTraces
							.next();
					if (newTrace.getNumberOfProcessInstances() > currentAlreadySorted
							.getNumberOfProcessInstances()) {
						// put it before that (smaller) trace
						sortedTraceList.add(sortedTraceList
								.indexOf(currentAlreadySorted), newTrace);
						break;
					} else if (alreadySortedTraces.hasNext() == false) {
						// make sure that the trace is put in the end, if it is
						// the so far "smallest" one
						sortedTraceList.add(newTrace);
						break;
					}
				}
			}
		}
	}

	/**
	 * Wrappes the given process instance in a ReplayedLogTrace. <br>
	 * This method should be overridden in order to define plugin-specific
	 * diagnostic data structures for log traces.
	 * 
	 * @param pi
	 *            the process instance to be wrapped
	 * @return the newly created log trace data structure
	 */
	protected ReplayedLogTrace createReplayedLogTrace(ProcessInstance pi) {
		return new ReplayedLogTrace(pi);
	}

	// /////// READ ACCESS METHODS FOR DIAGNOSTIC INFORMATION //////////

	/**
	 * Get the list of diagnostic log traces contained in that log.
	 * 
	 * @return The list of log traces.
	 */
	public ArrayList getDiagnosticLogTraces() {
		return logTraces;
	}

	/**
	 * Returns the encapsulated log reader.
	 * 
	 * @return the original log reader
	 */
	public LogReader getLogReader() {
		return originalLogReader;
	}

	// /////// PROVIDE ACCESS TO DIAGNOSTIC LOG TRACES //////////

	/**
	 * Resets the diagnostic log reader to its start position (i.e., the
	 * iterator is placed in front of the first element). <br>
	 * Note that here the iteration is not over the original log reader (see
	 * {@see #getLogReader()} but over the log traces enhanced with diagnostic
	 * information.
	 */
	public void reset() {
		logIterator = logTraces.iterator();
	}

	/**
	 * Determines whether there is a log trace left to be read by the diagnostic
	 * log reader (i.e., the iterator is not yet located after the last
	 * element). <br>
	 * Note that here the iteration is not over the original log reader (see
	 * {@see #getLogReader()} but over the log traces enhanced with diagnostic
	 * information.
	 * 
	 * @return <code>true</code> if there is a trace to be read,
	 *         <code>false</code> otherwise
	 */
	public boolean hasNext() {
		return logIterator.hasNext();
	}

	/**
	 * Reads the current diagnostic log trace and move to the next one (i.e.,
	 * fetch the next element and place the iterator in behind it). Note that
	 * the existance of an element in that position should be checked before
	 * using logicalHasNext(). <br>
	 * Note that here the iteration is not over the original log reader (see
	 * {@see #getLogReader()} but over the log traces enhanced with diagnostic
	 * information.
	 * 
	 * @return the current diagnostic log trace
	 */
	public ReplayedLogTrace next() {
		return (ReplayedLogTrace) logIterator.next();
	}

	/**
	 * Finds a specific trace in the log.
	 * 
	 * @param id
	 *            the ID of the trace (accessible by getName()) in the log
	 * @return the specified diagnostic log trace if found, <code>null</code>
	 *         otherwise
	 */
	public ReplayedLogTrace getLogTrace(String id) {
		// check whether entry exists
		if (logTraceMap.containsKey(id)) {
			return (ReplayedLogTrace) logTraceMap.get(id);
		} else {
			return null;
		}
	}

	/**
	 * Determines the index of the given trace with respect to the ordering of
	 * the log file.
	 * 
	 * @param id
	 *            the ID of the trace (accessible by getName()) in the log
	 * @return the index of the trace, if found. -1 otherwise
	 */
	public int getIndexOfLogTrace(String id) {
		return logTraces.indexOf(getLogTrace(id));
	}

	/**
	 * Creates a list of all the log trace IDs contained in the log.
	 * 
	 * @return an ArrayList containing the IDs as Strings
	 */
	public ArrayList getLogTraceIDs() {
		// note that there is not just the keyset returned to keep
		// the oder of the instances
		ArrayList instanceIDs = new ArrayList();
		Iterator allTraces = logTraces.iterator();
		while (allTraces.hasNext()) {
			ReplayedLogTrace currentTrace = (ReplayedLogTrace) allTraces.next();
			instanceIDs.add(currentTrace.getName());
		}
		return instanceIDs;
	}

	/**
	 * Gets the most frequent log traces determined by the given percentage from
	 * the log. While doing so the percentage might not be matched exactly as
	 * each trace can represent a number of process instances. The value is then
	 * rounded up and can be retrieved using the method getPerCentage.
	 * 
	 * @param perCentage
	 *            the percentage ranging from 1 to 100
	 * @return a list of trace IDs referencing the most frequent instances in
	 *         the log
	 */
	public ArrayList getMostFrequentTraces(int perCentage) {
		Iterator sortedTraces = sortedTraceList.iterator();
		ArrayList mostFrequent = new ArrayList();
		while (sortedTraces.hasNext()) {
			ReplayedLogTrace currentTrace = (ReplayedLogTrace) sortedTraces
					.next();
			mostFrequent.add(currentTrace.getName());
			if (getPercentage(mostFrequent) >= perCentage) {
				return mostFrequent;
			}
		}
		return mostFrequent;
	}

	/**
	 * Calculates the (rounded) percentage of process instances represented by
	 * the given log traces with respect to the whole log.
	 * 
	 * @param selectedTraces
	 *            the log traces (specified by their ID) for which the
	 *            percentage is to be calculated
	 * @return the percentage ranging from 0 (empty list) to 100
	 */
	public int getPercentage(ArrayList selectedTraces) {
		Iterator traceIDs = selectedTraces.iterator();
		int selectedNumberOfProcessInstances = 0;
		while (traceIDs.hasNext()) {
			String currentID = (String) traceIDs.next();
			ReplayedLogTrace currentTrace = getLogTrace(currentID);
			if (currentTrace != null) {
				selectedNumberOfProcessInstances += currentTrace
						.getNumberOfProcessInstances();
			}
		}
		// calculate percentage
		float percentage = ((float) selectedNumberOfProcessInstances * 100)
				/ (float) getOverallNumberOfProcessInstances();
		return (int) percentage;
	}

	/**
	 * Returns the number of traces contained in the log.
	 * 
	 * @return the number of log traces
	 */
	public int getSizeOfLog() {
		return logTraces.size();
	}

	/**
	 * Returns the number of process instances represented by the aggregrated
	 * log traces (taking the number of instances represented by each trace into
	 * account).
	 * 
	 * @return the number of process instances represented
	 */
	public int getOverallNumberOfProcessInstances() {
		return originalLogReader.getLogSummary().getNumberOfProcessInstances();
	}

	/**
	 * Retrieves the log summary from the internally held <code>LogReader</code>
	 * to, e.g., find the corresponding log event for an audit trail entry.
	 * 
	 * @return LogSummary the <code>LogSummary</code> of the log
	 */
	public LogSummary getLogSummary() {
		return originalLogReader.getLogSummary();
	}
}
