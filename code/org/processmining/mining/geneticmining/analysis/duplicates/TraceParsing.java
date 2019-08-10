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

package org.processmining.mining.geneticmining.analysis.duplicates;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Random;

import mathCollection.HashMultiset;
import mathCollection.Multiset;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.models.heuristics.MarkingHeuristicsNet;
import org.processmining.framework.util.MethodsForFiles;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;
import org.processmining.importing.heuristicsnet.HeuristicsNetFromFile;

/**
 * <p>
 * This class calculates the precision and recall of two
 * <code>HeuristicsNet</code> with respect to the parsing of the traces.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class TraceParsing implements PrecisionRecall {

	private double precision;
	private double recall;
	private LogReader log;
	private HeuristicsNet baseHN;
	private HeuristicsNet foundHN;

	// private boolean useContinuousSemantics;

	/**
	 * Creates an object of <code>TraceEquivalent.</code>
	 * 
	 * @param log
	 *            LogReader log to use during the calculation of the precision
	 *            and recall
	 * @param baseHN
	 *            HeuristicsNet base heuristics net. The precision and recall
	 *            consider this net as the correct solution.
	 * @param foundHN
	 *            HeuristicsNet found heuristis net. The precision and recall
	 *            compare this net to the base heuristics net.
	 */
	// * @param useContinuousSemantics boolean indicates if the continuous
	// semantics parser
	// * should be used while computing the precision and recall. When this
	// value is false, the
	// * stop semantics parser is used.
	public TraceParsing(LogReader log, HeuristicsNet baseHN,
			HeuristicsNet foundHN) throws Exception {
		// boolean useContinuousSemantics) throws Exception {

		if (log != null) {
			this.log = log;
		} else {
			throw new NullPointerException("Log is null!");
		}

		if (baseHN != null) {
			this.baseHN = baseHN;
			// making the baseHN and the log work on the same indeces for the
			// tasks/elements...
			try {
				this.baseHN.setLogEvents(log.getLogSummary().getLogEvents());
			} catch (ArrayIndexOutOfBoundsException exc) {
				throw new ArrayIndexOutOfBoundsException(
						"The log does not have all the events in the 'baseHN'!");
			}
		} else {
			throw new NullPointerException("Base heuristics net is null!");
		}

		if (foundHN != null) {
			this.foundHN = foundHN;
			// making the foundHN and the log work on the same indeces for the
			// tasks/elements...
			try {
				this.foundHN.setLogEvents(log.getLogSummary().getLogEvents());
			} catch (ArrayIndexOutOfBoundsException exc) {
				throw new ArrayIndexOutOfBoundsException(
						"The log does not have all the events in the 'foundHN'!");
			}
		} else {
			throw new NullPointerException("Found heuristics net is null!");
		}

		// this.useContinuousSemantics = useContinuousSemantics;

		calculatePrecisionAndRecall();

	}

	public double getPrecision() {
		return precision;
	}

	public double getRecall() {
		return recall;
	}

	private void calculatePrecisionAndRecall() {

		double partialPrecision = 0.0;
		double partialRecall = 0.0;

		// creating a marking for every individual
		MarkingHeuristicsNet baseHNmarking = new MarkingHeuristicsNet(baseHN,
				new Random(Long.MAX_VALUE));
		MarkingHeuristicsNet foundHNmarking = new MarkingHeuristicsNet(foundHN,
				new Random(Long.MAX_VALUE));
		// parsing the log
		Iterator logReaderInstanceIterator = log.instanceIterator();
		while (logReaderInstanceIterator.hasNext()) {

			partialPrecision = 0.0;
			partialRecall = 0.0;

			// reading the process instance
			ProcessInstance pi = (ProcessInstance) logReaderInstanceIterator
					.next();
			// resetting the markings...
			baseHNmarking.reset();
			foundHNmarking.reset();
			// getting the elements in the pi
			Iterator atesIterator = pi.getAuditTrailEntryList().iterator();
			Multiset baseMS;
			Multiset foundMS;
			Multiset intersection;
			AuditTrailEntry ate;
			int element;

			// from position "0" to "n-1" at the pi... (n is the size of the pi)
			// calculate the partial recall and precision for the trace
			for (int indexPi = 0; indexPi < pi.getAuditTrailEntryList().size(); indexPi++) {
				// calculating partial fitness (starts at position zero!)
				baseMS = toSet(baseHNmarking.getCurrentEnabledElements(),
						baseHN.getDuplicatesMapping(), baseHN.getLogEvents());
				foundMS = toSet(foundHNmarking.getCurrentEnabledElements(),
						foundHN.getDuplicatesMapping(), foundHN.getLogEvents());
				intersection = baseMS.intersection(foundMS);

				if (baseMS.size() > 0) {
					partialRecall += ((double) intersection.size() / baseMS
							.size());
				} // else, division by zero, no need to increment the
				// partialRecall

				if (foundMS.size() > 0) {
					partialPrecision += ((double) intersection.size() / foundMS
							.size());
				} // else, division by zero, no need to increment the
				// partialPrecision

				// firing the element at position "indexPi"
				ate = (AuditTrailEntry) atesIterator.next();
				element = baseHN.getLogEvents().findLogEventNumber(
						ate.getElement(), ate.getType());
				try {
					baseHNmarking.fire(element, pi, indexPi);
				} catch (ArrayIndexOutOfBoundsException exc) {
					// The individual does not contain the element... just
					// proceed.
				}

				try {
					foundHNmarking.fire(element, pi, indexPi);
				} catch (ArrayIndexOutOfBoundsException exc) {
					// The individual does not contain the element... just
					// proceed.
				} catch (NullPointerException npe) {
					// The individual does not contain the element... just
					// proceed.
				}
			}
			// divinding by the size of the trace times the frequency of the
			// trace
			double piFrequency = MethodsForWorkflowLogDataStructures
					.getNumberSimilarProcessInstances(pi);
			partialRecall = ((piFrequency * partialRecall) / pi
					.getAuditTrailEntryList().size());
			partialPrecision = ((piFrequency * partialPrecision) / pi
					.getAuditTrailEntryList().size());

			// adding to the recall and precision
			recall += partialRecall;
			precision += partialPrecision;
		}
		recall /= log.getLogSummary().getNumberOfProcessInstances();
		precision /= log.getLogSummary().getNumberOfProcessInstances();
	}

	private static Multiset toSet(HNSubSet subset, int[] duplicatesMapping,
			LogEvents le) {
		// NOTE: The multiset is actually a set!
		// I just used a multiset to get the "intersection" for free. :)
		Multiset ms = new HashMultiset();
		for (int i = 0; i < subset.size(); i++) {
			String element = null;
			try {
				element = le.getEvent(duplicatesMapping[subset.get(i)])
						.getModelElementName()
						+ " "
						+ le.getEvent(duplicatesMapping[subset.get(i)])
								.getEventType();
			} catch (ArrayIndexOutOfBoundsException exc) {
				// the task is in the net, but not in the event log
				// this may happen when the net is connected to a log that has
				// fewer events than the net
				element = subset.toString() + " " + i; // we just use some
				// string to count as an
				// enabled event!
			}
			if (!ms.contains(element)) {
				ms.add(element);
			}
		}

		return ms;

	}

	public static void main(String[] args) throws Exception {

		DefaultLogFilter logFilter = new DefaultLogFilter(
				DefaultLogFilter.INCLUDE);
		logFilter.setProcess("CpnToolsLog");
		LogReader logReader = LogReaderFactory
				.createInstance(
						logFilter,
						LogFile
								.getInstance(MethodsForFiles
										.extractFiles("C:\\AKThesisExperiments\\GenAlgNoiseFree\\setup\\logs\\bn1\\groupedFollowsbn1.zip")[0]));

		HeuristicsNet baseHN = new HeuristicsNetFromFile(
				new FileInputStream(
						new File(
								"C:\\AKThesisExperiments\\GenAlgNoiseFree\\setup\\desiredNets\\bn1.hn")))
				.getNet();

		HeuristicsNet foundHN = new HeuristicsNetFromFile(
				new FileInputStream(
						new File(
								"C:\\AKThesisExperiments\\GenAlgNoiseFree\\Backup_resultsHeuristicsAndGO\\bn1\\individuals\\bn1_groupedFollowsbn1.zip#groupedFollowsbn1.xml_seed5718490662849960997.txt\\gen_999\\ind_0.hn")))
				.getNet();

		TraceParsing te = new TraceParsing(logReader, baseHN, foundHN);
		System.out.println("Precision = " + te.getPrecision());
		System.out.println("Recall = " + te.getRecall());

	}

}
