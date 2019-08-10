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

/* 14/5/2007
 Bug: tijdens gebruik "all activities connected heuristc" toch losse activiteiten!

 */

/* programma bevat maar maakt nog geen gebruik van andIn en andOut-MeasureF2
 Verschillende definities zijn mogelijk:

 ownerE>>>newE + ownerE>>>oldE
 ----------------------------- (is geimplementeerd in F2)
 (2 * ownerE ) + 1

 newE>>>oldE + oldE>>>newE
 -------------------------
 (0.5 * (|newE| + |oldE|)) + 1

 */

package org.processmining.mining.heuristicsmining;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.BitSet;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.heuristics.HNSet;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.util.PluginDocumentationLoader;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.geneticmining.fitness.duplicates.DTContinuousSemanticsFitness;
import org.processmining.mining.geneticmining.fitness.duplicates.DTImprovedContinuousSemanticsFitness;
import org.processmining.mining.heuristicsmining.models.DependencyHeuristicsNet;
import org.processmining.mining.logabstraction.LogAbstraction;
import org.processmining.mining.logabstraction.LogAbstractionImpl;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Ton Weijters
 * @version 1.0
 */

public class HeuristicsMiner implements MiningPlugin {

	public final static double CAUSALITY_FALL = 0.8;
	// public final static boolean LT_DEBUG = false;

	private LogEvents events;
	// the different counters and measurments
	private DoubleMatrix1D startCount;
	private DoubleMatrix1D endCount;
	private DoubleMatrix2D directSuccessionCount; // =
	// logAbstraction.getFollowerInfo(1).copy();
	private DoubleMatrix2D succession2Count; // =
	// logAbstraction.getCloseInInfo(2).copy();

	private DoubleMatrix2D longRangeSuccessionCount; // calculated in
	// makeBasicRelations
	private DoubleMatrix2D longRangeDependencyMeasures; // information about the
	// longrange dependecy
	// relation
	// private DoubleMatrix2D HNlongRangeFollowingChance; //information about
	// the following chance in the Heuristics Net

	private DoubleMatrix2D causalSuccession; // calculated in makeBasicRelations

	private DoubleMatrix1D L1LdependencyMeasuresAll;
	private DoubleMatrix2D L2LdependencyMeasuresAll;
	private DoubleMatrix2D ABdependencyMeasuresAll;

	private DoubleMatrix2D dependencyMeasuresAccepted;

	private DoubleMatrix2D andInMeasuresAll;
	private DoubleMatrix2D andOutMeasuresAll;

	private boolean[] L1Lrelation;
	private int[] L2Lrelation;
	private boolean[] alwaysVisited;

	// noiseCounter counts the total wrong dependency observations in the log
	private DoubleMatrix2D noiseCounters;

	private HeuristicsMinerParameters parameters = new HeuristicsMinerParameters();
	public HeuristicsMinerGUI ui = null;

	public void HeuristicsMiner() {
		// not used
	}

	public String getName() {
		return "Heuristics miner";
	}

	public JPanel getOptionsPanel(LogSummary summary) {
		if (ui == null) {
			ui = new HeuristicsMinerGUI(summary, parameters);
		}
		return ui;
	}

	// this is the Heuristics Miner methode call
	public MiningResult mine(LogReader log) {
		Progress progress;
		LogAbstraction logAbstraction;
		HeuristicsNet net; // because we have a single individual

		// show a small progress window
		progress = new Progress("Mining " + log.getFile().getShortName()
				+ " using " + getName());
		progress.setMinMax(0, 5);

		// general message
		//
		Message.add("Start Heuristics mining ... ");
		Message.add(parameters.toString());
		Message.add("Number of process instances (cases) = "
				+ log.getLogSummary().getNumberOfProcessInstances());
		Message.add("Number of audit trail entries (events) = "
				+ log.getLogSummary().getNumberOfAuditTrailEntries());

		Message.add("Start Heuristics mining ... ", Message.TEST);
		Message.add(parameters.toString(), Message.TEST);
		Message.add("Number of process instances (cases) = "
				+ log.getLogSummary().getNumberOfProcessInstances(),
				Message.TEST);
		Message.add("Number of audit trail entries (events) = "
				+ log.getLogSummary().getNumberOfAuditTrailEntries(),
				Message.TEST);

		logAbstraction = new LogAbstractionImpl(log);
		events = log.getLogSummary().getLogEvents();

		if (progress.isCanceled()) {
			return null;
		}
		try {
			startCount = logAbstraction.getStartInfo().copy();
			if (progress.isCanceled()) {
				return null;
			}
			endCount = logAbstraction.getEndInfo().copy();
			if (progress.isCanceled()) {
				return null;
			}

			directSuccessionCount = logAbstraction.getFollowerInfo(1).copy();
			if (progress.isCanceled()) {
				return null;
			}

			succession2Count = logAbstraction.getCloseInInfo(2).copy();

		} catch (IOException ex) {
			Message.add("Error while reading the log: " + ex.getMessage(),
					Message.ERROR);
			return null;
		}

		if (progress.isCanceled()) {
			return null;
		}

		longRangeSuccessionCount = DoubleFactory2D.dense.make(events.size(),
				events.size(), 0);
		causalSuccession = DoubleFactory2D.dense.make(events.size(), events
				.size(), 0);
		longRangeDependencyMeasures = DoubleFactory2D.dense.make(events.size(),
				events.size(), 0);
		L1LdependencyMeasuresAll = DoubleFactory1D.sparse
				.make(events.size(), 0);
		andInMeasuresAll = DoubleFactory2D.sparse.make(events.size(), events
				.size(), 0);
		andOutMeasuresAll = DoubleFactory2D.sparse.make(events.size(), events
				.size(), 0);

		L2LdependencyMeasuresAll = DoubleFactory2D.sparse.make(events.size(),
				events.size(), 0);
		ABdependencyMeasuresAll = DoubleFactory2D.sparse.make(events.size(),
				events.size(), 0);
		dependencyMeasuresAccepted = DoubleFactory2D.sparse.make(events.size(),
				events.size(), 0);
		noiseCounters = DoubleFactory2D.sparse.make(events.size(), events
				.size(), 0);

		// Building basic relations
		makeBasicRelations(log, CAUSALITY_FALL);
		if (progress.isCanceled()) {
			return null;
		}

		net = makeHeuristicsRelations(log);

		progress.close();

		// return new DTGeneticMinerResult(net, log);
		return new HeuristicsNetResult(net, log);
	}

	private void showExtraInfo() {

		Message.add("Start information:");
		Message.add(showHeuristicsMiningListI(events, startCount));

		Message.add("End information:");
		Message.add(showHeuristicsMiningListI(events, endCount));

		Message.add("Direct successors counters (|A > B|):");
		Message.add(showHeuristicsMiningMatrixI(events, directSuccessionCount));

		Message.add("Direct successors counters (|A>B>A|):");
		Message.add(showHeuristicsMiningMatrixI(events, succession2Count));

		if (parameters.useLongDistanceDependency) {
			Message.add("Long distance succession counters (|A >>> B|):");
			Message.add(showHeuristicsMiningMatrixI(events,
					longRangeSuccessionCount));
		}

		Message.add("All (also not accepted) L1L-dependency values:");
		Message
				.add(showHeuristicsMiningListR(events, L1LdependencyMeasuresAll));

		Message.add("All (also not accepted) L2L-dependency values:");
		Message.add(showHeuristicsMiningMatrixR(events,
				L2LdependencyMeasuresAll));

		Message.add("All (also not accepted) A>B-dependency values:");
		Message
				.add(showHeuristicsMiningMatrixR(events,
						ABdependencyMeasuresAll));

		if (parameters.useLongDistanceDependency) {
			Message.add("All always visited events:");
			Message.add(showHeuristicsMiningListB(events, alwaysVisited));

			Message.add("All (also not accepted) A>>>B-dependency values:");
			Message.add(showHeuristicsMiningMatrixR(events,
					longRangeDependencyMeasures));
		}

		Message.add("Accepted (L1L, L2L, A>B, A>>>B) dependency values:");
		Message.add(showHeuristicsMiningMatrixR(events,
				dependencyMeasuresAccepted));

		Message.add("All IN and-values:");
		Message.add(showHeuristicsMiningMatrixR(events, andInMeasuresAll));

		Message.add("All OUT and-values:");
		Message.add(showHeuristicsMiningMatrixR(events, andOutMeasuresAll));

		int numberOfConnections = 0;

		for (int i = 0; i < dependencyMeasuresAccepted.rows(); i++) {
			for (int j = 0; j < dependencyMeasuresAccepted.columns(); j++) {
				if (dependencyMeasuresAccepted.get(i, j) > 0.01) {
					numberOfConnections = numberOfConnections + 1;
				}
			}
		}
		Message.add("Total number of connections = " + numberOfConnections
				+ "\n");
		Message
				.add("\"Wrong\" observations (#B>A but A->B accepted) between accepted dependency relations:");
		Message.add(showHeuristicsMiningMatrixI(events, noiseCounters));

		Message.add("Total number of connections = " + numberOfConnections
				+ "\n", Message.TEST);
		Message
				.add(
						"\"Wrong\" observations (#B>A but A->B accepted) between accepted dependency relations:",
						Message.TEST);
		Message.add(showHeuristicsMiningMatrixI(events, noiseCounters),
				Message.TEST);

		int noiseTotal = 0;

		for (int i = 0; i < noiseCounters.rows(); i++) {
			for (int j = 0; j < noiseCounters.columns(); j++) {
				noiseTotal = noiseTotal + (int) noiseCounters.get(i, j);
			}
		}
		Message.add("Total \"wrong\" observations = " + noiseTotal);
		Message.add("Total \"wrong\" observations = " + noiseTotal,
				Message.TEST);

	}

	private double calculateDependencyMeasure(int i, int j) {
		return ((double) directSuccessionCount.get(i, j) - directSuccessionCount
				.get(j, i))
				/ ((double) directSuccessionCount.get(i, j)
						+ directSuccessionCount.get(j, i) + parameters
						.getDependencyDivisor());
	}

	private double calculateL1LDependencyMeasure(int i) {
		return ((double) directSuccessionCount.get(i, i))
				/ (directSuccessionCount.get(i, i) + parameters
						.getDependencyDivisor());
	}

	private double calculateL2LDependencyMeasure(int i, int j) {
		// problem if for instance we have a A -> A loop
		// in parallel with B the |A>B>A|-value can be high without a L2L-loop
		if ((L1Lrelation[i] && succession2Count.get(i, j) >= parameters
				.getPositiveObservationsThreshold())
				|| (L1Lrelation[j] && succession2Count.get(j, i) >= parameters
						.getPositiveObservationsThreshold())) {
			return 0.0;
		} else {
			return ((double) succession2Count.get(i, j) + succession2Count.get(
					j, i))
					/ (succession2Count.get(i, j) + succession2Count.get(j, i) + parameters
							.getDependencyDivisor());
		}
	}

	private double calculateLongDistanceDependencyMeasure(int i, int j) {
		return ((double) longRangeSuccessionCount.get(i, j) / (events.getEvent(
				i).getOccurrenceCount() + parameters.getDependencyDivisor()))
				- (5.0 * (Math.abs(events.getEvent(i).getOccurrenceCount()
						- events.getEvent(j).getOccurrenceCount())) / events
						.getEvent(i).getOccurrenceCount());

	}

	private void makeBasicRelations(LogReader log, double causalityFall) {
		log.reset();
		while (log.hasNext()) {
			ProcessInstance pi = log.next();
			AuditTrailEntries ate = pi.getAuditTrailEntries();

			int i = 0;
			boolean terminate = false;

			while (!terminate) {
				ate.reset();
				// Skip the first i entries of the trace
				for (int j = 0; j < i; j++) {
					ate.next();
				}
				// Work with the other entries.
				AuditTrailEntry begin = ate.next();
				// Find the correct row of the matices
				int row = events.findLogEventNumber(begin.getElement(), begin
						.getType());
				int distance = 0;
				boolean foundSelf = false;
				HNSubSet done = new HNSubSet();
				terminate = (!ate.hasNext());
				while (ate.hasNext() && (!foundSelf)) {
					AuditTrailEntry end = ate.next();
					int column = events.findLogEventNumber(end.getElement(),
							end.getType());

					foundSelf = (row == column);
					distance++;

					if (done.contains(column)) {
						continue;
					}
					done.add(column);

					// update long range matrix
					longRangeSuccessionCount.set(row, column,
							longRangeSuccessionCount.get(row, column) + 1);

					// update causal matrix
					causalSuccession.set(row, column, causalSuccession.get(row,
							column)
							+ Math.pow(causalityFall, distance - 1));

				}
				i++;
			}
		}

		// calculate causalSuccesion (==> not yet used during heuristics process
		// mining!!!
		for (int i = 0; i < causalSuccession.rows(); i++) {
			for (int j = 0; j < causalSuccession.columns(); j++) {
				if (causalSuccession.get(i, j) == 0) {
					continue;
				}
				causalSuccession.set(i, j,
						((double) causalSuccession.get(i, j))
								/ longRangeSuccessionCount.get(i, j));
			}
		}
		// calculate longRangeDependencyMeasures
		for (int i = 0; i < longRangeDependencyMeasures.rows(); i++) {
			for (int j = 0; j < longRangeDependencyMeasures.columns(); j++) {
				if (events.getEvent(i).getOccurrenceCount() == 0) {
					continue;
				}
				longRangeDependencyMeasures.set(i, j,
						calculateLongDistanceDependencyMeasure(i, j));
			}

		}

	}

	private String showHeuristicsMiningListB(LogEvents events, boolean[] matrix) {
		DecimalFormat eventNum = new DecimalFormat("00");

		String res = "";
		// Events name list
		for (int j = 0; j < events.size(); j++) {
			res = res + eventNum.format(j) + " " + matrix[j] + " ("
					+ events.getEvent(j).getModelElementName() + ") \n";
		}
		return res;
	}

	private String showHeuristicsMiningListI(LogEvents events,
			DoubleMatrix1D matrix) {
		DecimalFormat eventNum = new DecimalFormat("00");
		DecimalFormat intNum = new DecimalFormat("0000");

		String res = "";
		// Events name list
		for (int j = 0; j < matrix.size(); j++) {
			res = res + eventNum.format(j) + " " + intNum.format(matrix.get(j))
					+ " (" + events.getEvent(j).getModelElementName() + ") \n";
		}
		return res;
	}

	private String showHeuristicsMiningListR(LogEvents events,
			DoubleMatrix1D matrix) {
		DecimalFormat eventNum = new DecimalFormat("00");
		DecimalFormat realNum = new DecimalFormat("+0.000;-0.000");

		String res = "";
		// Events name list
		for (int j = 0; j < matrix.size(); j++) {
			res = res + eventNum.format(j) + " "
					+ realNum.format(matrix.get(j)) + " ("
					+ events.getEvent(j).getModelElementName() + ") \n";
		}
		return res;
	}

	private String showHeuristicsMiningMatrixI(LogEvents events,
			DoubleMatrix2D matrix) {
		DecimalFormat eventNum = new DecimalFormat("00");
		DecimalFormat intNum = new DecimalFormat("0000");

		String res = "";
		String dummy2, dummy1 = "                                                                     ";
		int L;
		final int NAMELENGTH = 20, POS = 4;

		// First line of the matrix
		res = res + dummy1.substring(0, NAMELENGTH + POS) + "  ";
		for (int j = 0; j < matrix.columns(); j++) {
			res = res + eventNum.format(j) + dummy1.substring(0, POS - 2) + " ";
		}
		res = res + "\n";

		// Other lines of the matrix
		for (int i = 0; i < matrix.rows(); i++) {
			L = events.getEvent(i).getModelElementName().length();
			if (L < NAMELENGTH) {
				dummy2 = events.getEvent(i).getModelElementName()
						+ dummy1.substring(0, NAMELENGTH - L);
			} else {
				dummy2 = events.getEvent(i).getModelElementName().substring(0,
						NAMELENGTH);
			}

			res = res + eventNum.format(i) + " " + dummy2 + " ";
			for (int j = 0; j < matrix.columns(); j++) {
				res = res + intNum.format(matrix.get(i, j)) + " ";
			}
			res = res + "\n";
		}
		return res;
	}

	private String showHeuristicsMiningMatrixR(LogEvents events,
			DoubleMatrix2D matrix) {
		DecimalFormat eventNum = new DecimalFormat("00");
		DecimalFormat realNum = new DecimalFormat("+0.000;-0.000");

		String res = "";
		String dummy2, dummy1 = "                                                                     ";
		int L;
		final int NAMELENGTH = 20, POS = 6;

		// First line of the matrix
		res = res + dummy1.substring(0, NAMELENGTH + POS) + "  ";
		for (int j = 0; j < matrix.columns(); j++) {
			res = res + eventNum.format(j) + dummy1.substring(0, POS - 2) + " ";
		}
		res = res + "\n";

		// Other lines of the matrix
		for (int i = 0; i < matrix.rows(); i++) {
			L = events.getEvent(i).getModelElementName().length();
			if (L < NAMELENGTH) {
				dummy2 = events.getEvent(i).getModelElementName()
						+ dummy1.substring(0, NAMELENGTH - L);
			} else {
				dummy2 = events.getEvent(i).getModelElementName().substring(0,
						NAMELENGTH);
			}

			res = res + eventNum.format(i) + " " + dummy2 + " ";
			for (int j = 0; j < matrix.columns(); j++) {
				res = res + realNum.format(matrix.get(i, j)) + " ";
			}
			res = res + "\n";
		}
		return res;
	}

	public boolean escapeToEndPossibleF(int x, int y, BitSet alreadyVisit,
			DependencyHeuristicsNet result) {
		HNSet outputSetX, outputSetY = new HNSet();
		// double max, min, minh;
		boolean escapeToEndPossible;
		int minNum;

		// [A B]
		// X [C] ---> Y
		// [D B F]

		// build subset h = [A B C D E F] of all elements of outputSetX
		// search for minNum of elements of min subset with X=B as element: [A
		// B] , minNum = 2

		outputSetX = result.getOutputSet(x);
		outputSetY = result.getOutputSet(y);

		HNSubSet h = new HNSubSet();
		minNum = 1000;
		for (int i = 0; i < outputSetX.size(); i++) {
			HNSubSet outputSubSetX = new HNSubSet();
			outputSubSetX = outputSetX.get(i);
			if ((outputSubSetX.contains(y)) && (outputSubSetX.size() < minNum)) {
				minNum = outputSubSetX.size();
			}
			for (int j = 0; j < outputSubSetX.size(); j++) {
				h.add(outputSubSetX.get(j));
			}
		}

		if (alreadyVisit.get(x)) {
			return false;
		} else if (x == y) {
			return false;
		} else if (outputSetY.size() < 0) {
			// y is an eEe element
			return false;
		} else if (h.size() == 0) {
			// x is an eEe element
			return true;
		} else if (h.contains(y) && (minNum == 1)) {
			// x is unique connected with y
			return false;
		} else {
			// iteration over OR-subsets in outputSetX
			for (int i = 0; i < outputSetX.size(); i++) {
				HNSubSet outputSubSetX = new HNSubSet();
				outputSubSetX = outputSetX.get(i);
				escapeToEndPossible = false;
				for (int j = 0; j < outputSubSetX.size(); j++) {
					int element = outputSubSetX.get(j);
					BitSet hulpAV = (BitSet) alreadyVisit.clone();
					hulpAV.set(x);
					if (escapeToEndPossibleF(element, y, hulpAV, result)) {
						escapeToEndPossible = true;
					}

				}
				if (!escapeToEndPossible) {
					return false;
				}
			}
			return true;
		}
	}

	public HNSet buildOrInputSets(int ownerE, HNSubSet inputSet) {
		HNSet h = new HNSet();
		int currentE;
		// using the welcome method,
		// distribute elements of TreeSet inputSet over the elements of HashSet
		// h
		boolean minimalOneOrWelcome;
		// setE = null;
		// Iterator hI = h.iterator();
		HNSubSet helpTreeSet;
		for (int isetE = 0; isetE < inputSet.size(); isetE++) {
			currentE = inputSet.get(isetE);
			minimalOneOrWelcome = false;
			for (int ihI = 0; ihI < h.size(); ihI++) {
				helpTreeSet = h.get(ihI);
				if (xorInWelcome(ownerE, currentE, helpTreeSet)) {
					minimalOneOrWelcome = true;
					helpTreeSet.add(currentE);
				}
			}
			if (!minimalOneOrWelcome) {
				helpTreeSet = new HNSubSet();
				helpTreeSet.add(currentE);
				h.add(helpTreeSet);
			}
		}

		// look to the (A v B) & (B v C) example with B A C in the inputSet;
		// result is [AB] [C]
		// repeat to get [AB] [BC]

		for (int isetE = 0; isetE < inputSet.size(); isetE++) {
			currentE = inputSet.get(isetE);
			for (int ihI = 0; ihI < h.size(); ihI++) {
				helpTreeSet = h.get(ihI);
				if (xorInWelcome(ownerE, currentE, helpTreeSet)) {
					helpTreeSet.add(currentE);
				}
			}
		}
		return h;
	}

	public HNSet buildOrOutputSets(int ownerE, HNSubSet outputSet) {
		HNSet h = new HNSet();
		int currentE;

		// using the welcome method,
		// distribute elements of TreeSet inputSet over the elements of HashSet
		// h
		boolean minimalOneOrWelcome;
		// setE = null;
		HNSubSet helpTreeSet;
		for (int isetE = 0; isetE < outputSet.size(); isetE++) {
			currentE = outputSet.get(isetE);
			minimalOneOrWelcome = false;
			for (int ihI = 0; ihI < h.size(); ihI++) {
				helpTreeSet = h.get(ihI);
				if (xorOutWelcome(ownerE, currentE, helpTreeSet)) {
					minimalOneOrWelcome = true;
					helpTreeSet.add(currentE);
				}
			}
			if (!minimalOneOrWelcome) {
				helpTreeSet = new HNSubSet();
				helpTreeSet.add(currentE);
				h.add(helpTreeSet);
			}
		}

		// look to the (A v B) & (B v C) example with B A C in the inputSet;
		// result is [AB] [C]
		// repeat to get [AB] [BC]
		for (int isetE = 0; isetE < outputSet.size(); isetE++) {
			currentE = outputSet.get(isetE);
			for (int ihI = 0; ihI < h.size(); ihI++) {
				helpTreeSet = h.get(ihI);
				if (xorOutWelcome(ownerE, currentE, helpTreeSet)) {
					helpTreeSet.add(currentE);
				}
			}
		}

		return h;
	}

	private boolean xorInWelcome(int ownerE, int newE, HNSubSet h) {
		boolean welcome = true;
		int oldE;
		double andValue;

		for (int ihI = 0; ihI < h.size(); ihI++) {
			oldE = h.get(ihI);
			andValue = andInMeasureF(ownerE, oldE, newE);
			if (newE != oldE) {
				andInMeasuresAll.set(newE, oldE, andValue);
			}
			if (andValue > parameters.getAndThreshold()) {
				welcome = false;
			}
		}
		return welcome;
	}

	private boolean xorOutWelcome(int ownerE, int newE, HNSubSet h) {
		boolean welcome = true;
		int oldE;
		double andValue;

		for (int ihI = 0; ihI < h.size(); ihI++) {
			oldE = h.get(ihI);
			andValue = andOutMeasureF(ownerE, oldE, newE);
			if (newE != oldE) {
				andOutMeasuresAll.set(newE, oldE, andValue);
			}
			if (andValue > parameters.getAndThreshold()) {
				welcome = false;
			}
		}
		return welcome;
	}

	private double andInMeasureF(int ownerE, int oldE, int newE) {
		if (ownerE == newE) {
			return 0.0;
		} else if ((directSuccessionCount.get(oldE, newE) < parameters
				.getPositiveObservationsThreshold())
				|| (directSuccessionCount.get(newE, oldE) < parameters
						.getPositiveObservationsThreshold())) {
			return 0.0;
		} else {
			return ((double) directSuccessionCount.get(oldE, newE) + directSuccessionCount
					.get(newE, oldE))
					/
					// relevantInObservations;
					(directSuccessionCount.get(newE, ownerE)
							+ directSuccessionCount.get(oldE, ownerE) + 1);
		}
	}

	private double andOutMeasureF(int ownerE, int oldE, int newE) {
		if (ownerE == newE) {
			return 0.0;
		} else if ((directSuccessionCount.get(oldE, newE) < parameters
				.getPositiveObservationsThreshold())
				|| (directSuccessionCount.get(newE, oldE) < parameters
						.getPositiveObservationsThreshold())) {
			return 0.0;
		} else {
			return ((double) directSuccessionCount.get(oldE, newE) + directSuccessionCount
					.get(newE, oldE))
					/
					// relevantOutObservations;
					(directSuccessionCount.get(ownerE, newE)
							+ directSuccessionCount.get(ownerE, oldE) + 1);
		}
	}

	private double andInMeasureF2(int ownerE, int oldE, int newE) {
		if ((ownerE == newE) || (newE == oldE)) {
			return 0.0;
			/*
			 * } else if ((directSuccessionCount.get(oldE, newE) <
			 * parameters.getPositiveObservationsThreshold()) ||
			 * (directSuccessionCount.get(newE, oldE) <
			 * parameters.getPositiveObservationsThreshold())) { return 0.0;
			 */
		} else {
			return ((double) (longRangeSuccessionCount.get(newE, ownerE) + longRangeSuccessionCount
					.get(oldE, ownerE)) /
			// relevantInObservations;
			((2 * events.getEvent(ownerE).getOccurrenceCount()) + 1));
		}
	}

	private double andOutMeasureF2(int ownerE, int oldE, int newE) {
		if ((ownerE == newE) || (newE == oldE)) {
			return 0.0;
			/*
			 * } else if ((directSuccessionCount.get(oldE, newE) <
			 * parameters.getPositiveObservationsThreshold()) ||
			 * (directSuccessionCount.get(newE, oldE) <
			 * parameters.getPositiveObservationsThreshold())) { return 0.0;
			 */
		} else {
			return ((double) longRangeSuccessionCount.get(ownerE, newE) + longRangeSuccessionCount
					.get(ownerE, oldE))
					/
					// relevantInObservations;
					((2 * events.getEvent(ownerE).getOccurrenceCount()) + 1);

		}
	}

	private HeuristicsNet makeHeuristicsRelations(LogReader log) {
		// use causalSuccession =>
		// directSuccession >
		// succession2Count ABA
		// longRangeSuccession >>>
		// Starter(s)
		// Ender(s)

		// make: causalRelations -->
		// parallelRelations ||

		int bestStart = 0;
		int bestEnd = 0;
		int size = events.size();
		double score;

		DependencyHeuristicsNet result = new DependencyHeuristicsNet(events,
				dependencyMeasuresAccepted, directSuccessionCount);

		double measure = 0.0;
		double[] bestInputMeasure = new double[size];
		double[] bestOutputMeasure = new double[size];
		int[] bestInputEvent = new int[size];
		int[] bestOutputEvent = new int[size];
		L1Lrelation = new boolean[size];
		L2Lrelation = new int[size];
		alwaysVisited = new boolean[size];

		HNSubSet[] inputSet = new HNSubSet[size];
		HNSubSet[] outputSet = new HNSubSet[size];

		for (int i = 0; i < size; i++) {
			inputSet[i] = new HNSubSet();
			outputSet[i] = new HNSubSet();
			L1Lrelation[i] = false;
			L2Lrelation[i] = -10;
		}

		// stap 1: Look for the best start and end task:
		// ============================================
		for (int i = 0; i < size; i++) {
			if (startCount.get(i) > startCount.get(bestStart)) {
				bestStart = i;
			}
			if (endCount.get(i) > endCount.get(bestEnd)) {
				bestEnd = i;
			}
		}

		// setting the start task
		HNSubSet startTask = new HNSubSet();
		startTask.add(bestStart);
		result.setStartTasks(startTask);
		// setting the end task

		HNSubSet endTask = new HNSubSet();
		endTask.add(bestEnd);
		result.setEndTasks(endTask);

		// update noiseCounters
		noiseCounters.set(bestStart, 0, log.getLogSummary()
				.getNumberOfProcessInstances()
				- startCount.get(bestStart));
		noiseCounters.set(0, bestEnd, log.getLogSummary()
				.getNumberOfProcessInstances()
				- endCount.get(bestEnd));

		if (Math.abs(startCount.get(bestStart)
				- log.getLogSummary().getNumberOfProcessInstances()) > parameters
				.getPositiveObservationsThreshold()) {
			Message.add("The BEGIN task is possible not unique?");
			Message.add("Use the 'Add an artificial BEGIN-task' option!");
			Message.add("The BEGIN task is possible not unique?", Message.TEST);
			Message.add("Use the 'Add an artificial BEGIN-task' option!",
					Message.TEST);
		}

		if (Math.abs(endCount.get(bestEnd)
				- log.getLogSummary().getNumberOfProcessInstances()) > parameters
				.getPositiveObservationsThreshold()) {
			Message.add("The END task is possible not unique?");
			Message.add("Use the 'Add an artificial END-task' option!");
			Message.add("The END task is possible not unique?", Message.TEST);
			Message.add("Use the 'Add an artificial END-task' option!",
					Message.TEST);
		}

		// Stap 2: build dependencyMeasuresAccepted
		// ============================================

		// stap 2.1 L1L loops (remark: L1L loops overrules L2L loops to prevent
		// the EAE pitfall!
		// with E in a direct loop)
		for (int i = 0; i < size; i++) {
			measure = calculateL1LDependencyMeasure(i);
			L1LdependencyMeasuresAll.set(i, measure);
			if (measure >= parameters.getL1lThreshold()
					&& directSuccessionCount.get(i, i) >= parameters
							.getPositiveObservationsThreshold()) {
				dependencyMeasuresAccepted.set(i, i, measure);
				L1Lrelation[i] = true;
				inputSet[i].add(i);
				outputSet[i].add(i);
			}
		}

		// stap 2.2: L2L loops
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				measure = calculateL2LDependencyMeasure(i, j);
				L2LdependencyMeasuresAll.set(i, j, measure);
				L2LdependencyMeasuresAll.set(j, i, measure);

				if ((i != j)
						&& (measure >= parameters.getL2lThreshold())
						&& ((succession2Count.get(i, j) + succession2Count.get(
								j, i)) >= parameters
								.getPositiveObservationsThreshold())) {
					dependencyMeasuresAccepted.set(i, j, measure);
					dependencyMeasuresAccepted.set(j, i, measure);
					L2Lrelation[i] = j;
					L2Lrelation[j] = i;
					inputSet[i].add(j);
					outputSet[j].add(i);
					inputSet[j].add(i);
					outputSet[i].add(j);
				}
			}
		}

		// Stap 2.3: normal dependecy measure
		// Stap 2.3.1: independed of any threshold
		// search the best input and output connection.
		// stap 2.3.1.1: initialization
		for (int i = 0; i < size; i++) {
			bestInputMeasure[i] = -10.0;
			bestOutputMeasure[i] = -10.0;
			bestInputEvent[i] = -1;
			bestOutputEvent[i] = -1;
		}

		// stap 2.3.1.2: search the beste ones:
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (i != j) {
					measure = calculateDependencyMeasure(i, j);
					ABdependencyMeasuresAll.set(i, j, measure);

					if (measure > bestOutputMeasure[i]) {
						bestOutputMeasure[i] = measure;
						bestOutputEvent[i] = j;
					}
					if (measure > bestInputMeasure[j]) {
						bestInputMeasure[j] = measure;
						bestInputEvent[j] = i;
					}
				}
			}
		}
		// Extra check for best compared with L2L-loops
		for (int i = 0; i < size; i++) {
			if ((i != bestStart) && (i != bestEnd)) {
				for (int j = 0; j < size; j++) {
					measure = calculateL2LDependencyMeasure(i, j);
					if (measure > bestInputMeasure[i]) {
						dependencyMeasuresAccepted.set(i, j, measure);
						dependencyMeasuresAccepted.set(j, i, measure);
						L2Lrelation[i] = j;
						L2Lrelation[j] = i;
						inputSet[i].add(j);
						outputSet[j].add(i);
						inputSet[j].add(i);
						outputSet[i].add(j);
					}
				}
			}

		}

		// stap 2.3.1.3: update the dependencyMeasuresAccepted matrix,
		// the inputSet, outputSet arrays and
		// the noiseCounters matrix
		//
		// extra: if L1Lrelation[i] then process normal
		// if L2Lrelation[i]=j is a ABA connection then only attach the
		// strongest
		// input and output connection

		if (parameters.useAllConnectedHeuristics) {
			for (int i = 0; i < size; i++) {
				int j = L2Lrelation[i];
				if (i != bestStart) {
					if ((j > -1) && (bestInputMeasure[j] > bestInputMeasure[i])) {
						// i is in a L2L relation with j but j has a stronger
						// input connection
						// do nothing
					} else {
						dependencyMeasuresAccepted.set(bestInputEvent[i], i,
								bestInputMeasure[i]);
						inputSet[i].add(bestInputEvent[i]);
						outputSet[bestInputEvent[i]].add(i);
						noiseCounters
								.set(bestInputEvent[i], i,
										directSuccessionCount.get(i,
												bestInputEvent[i]));
					}
				}
				if (i != bestEnd) {
					if ((j > -1)
							&& (bestOutputMeasure[j] > bestOutputMeasure[i])) {
						// i is in a L2L relation with j but j has a stronger
						// input connection
						// do nothing
					} else {
						dependencyMeasuresAccepted.set(i, bestOutputEvent[i],
								bestOutputMeasure[i]);
						inputSet[bestOutputEvent[i]].add(i);
						outputSet[i].add(bestOutputEvent[i]);
						noiseCounters.set(i, bestOutputEvent[i],
								directSuccessionCount
										.get(bestOutputEvent[i], i));
					}
				}

			}
		}

		// Stap 2.3.2: search for other connections that fulfill all the
		// thresholds:

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (dependencyMeasuresAccepted.get(i, j) <= 0.0001) {
					measure = calculateDependencyMeasure(i, j);
					if (((bestOutputMeasure[i] - measure) <= parameters
							.getRelativeToBestThreshold())
							&& (directSuccessionCount.get(i, j) >= parameters
									.getPositiveObservationsThreshold())
							&& (measure >= parameters.getDependencyThreshold())) {
						dependencyMeasuresAccepted.set(i, j, measure);
						inputSet[j].add(i);
						outputSet[i].add(j);
						noiseCounters
								.set(i, j, directSuccessionCount.get(j, i));
					}
				}
			}
		}

		// Stap 3: Given the InputSets and OutputSets build
		// OR-subsets;

		// AndOrAnalysis andOrAnalysis = new AndOrAnalysis();
		// double AverageRelevantInObservations = 0.0;
		// double AverageRelevantOutObservations = 0.0;
		// double sumIn, sumOut;

		// depending on the current event i, calculate the numeber of
		// relevant In and Out observations
		// NOT IN USE !!!

		for (int i = 0; i < size; i++) {
			result.setInputSet(i, buildOrInputSets(i, inputSet[i]));
			result.setOutputSet(i, buildOrOutputSets(i, outputSet[i]));
		}

		// Update the HeuristicsNet with non binairy dependecy relations:

		// Search for always visited activities:

		if (parameters.useLongDistanceDependency) {
			alwaysVisited[bestStart] = false;
			for (int i = 1; i < size; i++) {
				BitSet h = new BitSet();
				if (escapeToEndPossibleF(bestStart, i, h, result)) {
					alwaysVisited[i] = false;
				} else {
					alwaysVisited[i] = true;
				}
			}
		}

		if (parameters.useLongDistanceDependency) {
			for (int i = (size - 1); i >= 0; i--) {
				for (int j = (size - 1); j >= 0; j--) {
					if ((i == j) || (alwaysVisited[j] && (j != bestEnd))) {
						continue;
					}
					score = calculateLongDistanceDependencyMeasure(i, j);
					if (score > parameters.getLDThreshold()) {
						BitSet h = new BitSet();
						if (escapeToEndPossibleF(i, j, h, result)) {
							// HNlongRangeFollowingChance.set(i, j, hnc);
							dependencyMeasuresAccepted.set(i, j, score);

							// update heuristicsNet
							HNSubSet helpSubSet = new HNSubSet();
							HNSet helpSet = new HNSet();

							helpSubSet.add(j);
							helpSet = result.getOutputSet(i);
							helpSet.add(helpSubSet);
							result.setOutputSet(i, helpSet);

							helpSubSet = new HNSubSet();
							helpSet = new HNSet();

							helpSubSet.add(i);
							helpSet = result.getInputSet(j);
							helpSet.add(helpSubSet);
							result.setInputSet(j, helpSet);
						}
					}
				}
			}
		}

		int numberOfConnections = 0;

		for (int i = 0; i < dependencyMeasuresAccepted.rows(); i++) {
			for (int j = 0; j < dependencyMeasuresAccepted.columns(); j++) {
				if (dependencyMeasuresAccepted.get(i, j) > 0.01) {
					numberOfConnections = numberOfConnections + 1;
				}
			}
		}
		Message.add("Total number of connections = " + numberOfConnections);
		Message.add("Total number of connections = " + numberOfConnections,
				Message.TEST);

		int noiseTotal = 0;

		for (int i = 0; i < noiseCounters.rows(); i++) {
			for (int j = 0; j < noiseCounters.columns(); j++) {
				noiseTotal = noiseTotal + (int) noiseCounters.get(i, j);
			}
		}
		Message.add("Total \"wrong\" observations = " + noiseTotal);
		Message.add("Total \"wrong\" observations = " + noiseTotal,
				Message.TEST);

		if (parameters.extraInfo) {
			showExtraInfo();
		}

		// parse the log to get extra parse information:
		// (i) fitness
		// (ii) the number of times a connection is used

		HeuristicsNet[] population = new HeuristicsNet[1];
		population[0] = result;

		DTContinuousSemanticsFitness fitness1 = new DTContinuousSemanticsFitness(
				log);
		fitness1.calculate(population);

		Message.add("Continuous semantics fitness = "
				+ population[0].getFitness());
		Message.add("Continuous semantics fitness = "
				+ population[0].getFitness(), Message.TEST);

		DTImprovedContinuousSemanticsFitness fitness2 = new DTImprovedContinuousSemanticsFitness(
				log);
		fitness2.calculate(population);

		Message.add("Improved Continuous semantics fitness = "
				+ population[0].getFitness());
		Message.add("Improved Continuous semantics fitness = "
				+ population[0].getFitness(), Message.TEST);

		population[0].disconnectUnusedElements();

		return population[0];

	}

	private void message(String msg, int stage, Progress progress) {
		Message.add(msg, Message.DEBUG);
		if (progress != null) {
			progress.setNote(msg);
			progress.setProgress(stage);
		}
	}

	// see Ana Karla "RealGeneticMiner" for an example
	public String getHtmlDescription() {
		return PluginDocumentationLoader.load(this);

	}
}
