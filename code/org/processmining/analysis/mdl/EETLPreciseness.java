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
package org.processmining.analysis.mdl;

import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisResult;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedPlace;
import org.processmining.framework.ui.Message;

/**
 * Calculates the encoding length needed to encode the log in the model with
 * error recovery, with en explicit encoding (always space reserved for the
 * potential error encoding), and including token punishement.
 * 
 * @author Anne Rozinat
 */
public class EETLPreciseness extends MDLPrecisenessMetric {

	protected EETLPreciseness() {
		super(
				"Explicit Encoding with Token List",
				"Metric determining the encoding cost for the given "
						+ "log in the given Petri net model based on the explicit encoding of potential errors for each replay "
						+ "step (recording the number of tokens that were missing in which places).");
		measurer = new EETLEncoder();
	}

	class EETLEncoder extends PrecisenessMeasurer {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.processmining.analysis.mdl.MDLPrecisenessMetric.PrecisenessMeasurer
		 * #evaluateNumberOfEnabledTasks(int)
		 */
		protected void evaluateNumberOfEnabledTasks(int amountOfEnabled) {
			if (traceEncodingCost != -1) {
				traceEncodingCost += (Math.log(amountOfEnabled + 1) / Math
						.log(2)); // = log2(amountOfEnabled + 1)
				// debug
				Message.add("\n + log2(" + (amountOfEnabled + 1)
						+ ")       // log2(amountOfEnabled + 1)", 3);
			}
		}

		/**
		 * Adds up the cost for specifying each missing token (in the course of
		 * log replay).
		 */
		protected void takeMissingTokenMeasurement(ReplayedPlace p,
				ReplayedLogTrace pi) {
			if (traceEncodingCost != -1) {
				traceEncodingCost += Math.log(net.getPlaces().size())
						/ Math.log(2); // = log2(|P|)
				// debug
				Message.add("\n + log2(" + (net.getPlaces().size())
						+ ")       // log2(|P|)", 3);
			}
		}

		/**
		 * Adds up the cost for specifying the transition to be fired after
		 * inserting the missing tokens (but this time all must be enabled).
		 */
		protected void takeArtificiallyEnabledMeasurement(
				LogReplayAnalysisResult result, ReplayedLogTrace pi,
				int maxDepth) {
			int amountOfEnabled = determineNumberOfEnabledTasks(result, pi,
					maxDepth);
			if (traceEncodingCost != -1 && amountOfEnabled != 0) {
				traceEncodingCost += (Math.log(amountOfEnabled) / Math.log(2)); // =
				// log2(amountOfEnabled')
				// debug
				Message.add("\n + log2(" + (amountOfEnabled)
						+ ")       // log2(amountOfEnabled')", 3);
			} else {
				// this should not happen as the insertion of missing tokens
				// should have enabled the transition
				traceEncodingCost = -1;
			}
		}

		/**
		 * Adds up the cost for stating how many tokens need to be created (for
		 * each error) at the very end of the log replay. Assumes that arc
		 * weights are <= 1.
		 */
		protected void takePostReplayMeasurement(LogReplayAnalysisResult result) {
			if (traceEncodingCost != -1) {
				traceEncodingCost += noOfErrors
						* (Math.log(net.getPlaces().size()) / Math.log(2)); // =
				// k
				// log2(|P|)
				// debug
				Message.add("\n + " + noOfErrors + "log2("
						+ (net.getPlaces().size()) + ")       // k log2(|P|)",
						3);
			}
		}

	}
}
