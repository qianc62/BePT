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

import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisMethod;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisResult;
import org.processmining.framework.models.petrinet.algorithms.logReplay.Measurer;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedTransition;
import org.processmining.framework.ui.Message;

/**
 * Actually takes those measurements that are needed to calculate some
 * performance metrics. This is a subclass of the Measurer class.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */

public class PerformanceMeasurer extends Measurer {

	/**
	 * {@inheritDoc} Creates an analysis result of type
	 * {@link PerformanceLogReplayResult PerformanceLogReplayResult}.
	 */
	protected LogReplayAnalysisResult initLogReplayAnalysisResult(
			AnalysisConfiguration analysisOptions, PetriNet petriNet,
			LogReader log, LogReplayAnalysisMethod analysisMethod) {
		// initialize the result object and the analysis options
		return new PerformanceLogReplayResult(analysisOptions, petriNet, log,
				analysisMethod);
	}

	/**
	 * {@inheritDoc} Records that the corresponding log trace has not completed
	 * succesfully
	 * 
	 * @param pi
	 *            ReplayedLogTrace
	 * @param t
	 *            ReplayedTransition
	 * @param ate
	 *            AuditTrailEntry
	 */
	protected void takeFailedTaskMeasurement(ReplayedLogTrace pi,
			ReplayedTransition t, AuditTrailEntry ate) {
		try {
			// set successful execution related measures
			((ExtendedLogTrace) pi).setSuccessfullyExecuted(false);
			((ExtendedTransition) t).setFailedExecution((ExtendedLogTrace) pi);
		} catch (Exception ex) {
			Message.add("Failed task produced an error for transition "
					+ t.getIdentifier() + " in trace " + pi.getName() + ".\n"
					+ ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc} Checks whether places have tokens remaining and records
	 * that the corresponding log trace has not properly terminated if this is
	 * the case.
	 * 
	 */
	protected void takePostTraceReplayMeasurement(
			LogReplayAnalysisResult result, ReplayedLogTrace pi) {
		try {
			// finally count the amount of remaining tokens for that instance
			// and store it
			Iterator remainingTokenChecker = result.replayedPetriNet
					.getPlaces().iterator();
			while (remainingTokenChecker.hasNext()) {
				ExtendedPlace currentPlace = (ExtendedPlace) remainingTokenChecker
						.next();
				// treat final place differently
				if (currentPlace.outDegree() == 0) {
					// one single token in the final place is required
					if (currentPlace.getNumberOfTokens() == 0) {
						// set successful execution measure
						((ExtendedLogTrace) pi).setSuccessfullyExecuted(false);
					} else if (currentPlace.getNumberOfTokens() > 1) {
						// set proper completion measure
						((ExtendedLogTrace) pi).setProperlyTerminated(false);
					}
				}
				// treat non-final places
				else {
					if (currentPlace.getNumberOfTokens() > 0) {
						// set proper completion measure
						((ExtendedLogTrace) pi).setProperlyTerminated(false);
					}
				}
			}
		} catch (Exception ex) {
			Message.add("Taking post replay measurements failed for trace "
					+ pi.getName() + ".\n" + ex.toString(), 2);
			ex.printStackTrace();
		}
	}
}
