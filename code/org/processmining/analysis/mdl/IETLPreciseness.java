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

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisResult;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedPlace;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedTransition;
import org.processmining.framework.ui.Message;

/**
 * Calculates the encoding length needed to encode the log in the model with
 * error recovery, with en implicit encoding (giving the number of following
 * error-free log events to avoid using space for actually correct parts of the
 * log), and including token punishement.
 * 
 * @author Anne Rozinat
 */
public class IETLPreciseness extends MDLPrecisenessMetric {

	protected IETLPreciseness() {
		super(
				"Implicit Encoding with Token List",
				"Metric determining the encoding cost for the given "
						+ "log in the given Petri net model based on the implicit encoding of potential errors for each replay (i.e., "
						+ "giving the number of following error-free log events to avoid using space for actually correct parts of the log) "
						+ "step (recording the number of tokens that were missing in which places).");
		measurer = new IETLEncoder();
	}

	class IETLEncoder extends PrecisenessMeasurer {

		/**
		 * TODO: think about moving this part to a common IE superclass (or the
		 * MDL preciseness class) as it is common behavior for IETL and IEVT
		 * (but not of the explicit encodings).
		 */
		protected int goodEventsCounter;
		protected int seqLengthOfGoodEvents;

		protected int lastAmountOfEnabled; // workaround: need to substract part

		// of encoding cost again if error
		// occurs

		/**
		 * Resets the counters for calculating the encoding costs at the
		 * beginning of each new log replay.
		 */
		protected void initLogReplay() {
			super.initLogReplay();
			goodEventsCounter = 0;
			seqLengthOfGoodEvents = 0;
			lastAmountOfEnabled = 0;
		}

		/**
		 * Resets the counters for calculating the encoding costs at the
		 * beginning of each new log trace.
		 */
		protected void initTraceReplay(ReplayedLogTrace pi,
				LogReplayAnalysisResult result) {
			super.initTraceReplay(pi, result);
			goodEventsCounter = 0;
			seqLengthOfGoodEvents = 0;
			lastAmountOfEnabled = 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.processmining.analysis.mdl.MDLPrecisenessMetric.PrecisenessMeasurer
		 * #evaluateNumberOfEnabledTasks(int)
		 */
		protected void evaluateNumberOfEnabledTasks(int amountOfEnabled) {
			// this method will be called for every event replay, i.e., first
			// count as successful
			// and if still task failed, this will be compensated by
			// takeFailedTaskMeasurement method (- 1)
			goodEventsCounter++;

			if (traceEncodingCost != -1) {
				// add encoding cost for "good" events (NOTE: will need to be
				// substracted again in case of failure)
				traceEncodingCost += (Math.log(amountOfEnabled) / Math.log(2)); // =
				// log2(amountOfEnabled)
				// debug
				Message.add("\n + log2(" + (amountOfEnabled)
						+ ")       // log2(amountOfEnabled)", 3);

				lastAmountOfEnabled = amountOfEnabled; // record for potential
				// compensation in case
				// of failure
			}
		}

		protected void takeFailedTaskMeasurement(ReplayedLogTrace pi,
				ReplayedTransition t, AuditTrailEntry ate) {
			super.takeFailedTaskMeasurement(pi, t, ate);
			// store how many events could be replayed in a row without failure
			// so far..
			seqLengthOfGoodEvents = goodEventsCounter - 1;
			// .. and reset the counter
			goodEventsCounter = 0;

			if (traceEncodingCost != -1) {
				// REMOVE encoding cost for "good" events as failure occurred
				traceEncodingCost -= (Math.log(lastAmountOfEnabled) / Math
						.log(2)); // = log2(amountOfEnabled)
				// debug
				Message.add("\n - log2(" + (lastAmountOfEnabled)
						+ ")       // REMOVE log2(amountOfEnabled)", 3);

				if (seqLengthOfGoodEvents != 0) {
					// add encoding cost for block BEFORE this failure
					int upperBound = getUpperBound(Math
							.log(seqLengthOfGoodEvents)
							/ Math.log(2));
					traceEncodingCost += 2 * (upperBound + 1) + 1; // = 2 * ( [
					// log2(bj)
					// ] + 1) +
					// 1
					// debug
					Message
							.add(
									"\n + 2 * ( [ log2("
											+ (seqLengthOfGoodEvents)
											+ ") ] + 1) + 1       // 2 * ( [ log2(bj) ] + 1) + 1",
									3);
				} else {
					// cost to encode 0 block length is 3 bits
					traceEncodingCost += 3;
					// debug
					Message.add("\n + 3       // 3 (block length of 0)", 3);
				}
			}
		}

		/**
		 * Adds up the cost for specifying each missing token (in the course of
		 * log replay).
		 */
		protected void takeMissingTokenMeasurement(ReplayedPlace p,
				ReplayedLogTrace pi) {
			if (traceEncodingCost != -1) {
				traceEncodingCost += (Math.log(net.getPlaces().size()) / Math
						.log(2)); // = log2(|P|)
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
				traceEncodingCost += Math.log(amountOfEnabled) / Math.log(2); // =
				// log2(amountOfEnabled')
				// debug
				Message.add("\n + log2(" + (amountOfEnabled)
						+ ")       // log2(amountOfEnabled')", 3);
			} else {
				// this should not happen as the insertion of missing tokens
				// should have enabled the transition
				// TODO: throw and handle an exception (write error message to
				// console) instead of -1 thing!
				traceEncodingCost = -1;
			}
		}

		/**
		 * Adds up the cost for stating how many tokens need to be created (for
		 * each error) at the very end of the log replay. Assumes that arc
		 * weights are <= 1.
		 */
		protected void takePostReplayMeasurement(LogReplayAnalysisResult result) {
			// record actual error encoding cost
			if (traceEncodingCost != -1) {
				traceEncodingCost += (noOfErrors * (Math.log(net.getPlaces()
						.size()) / Math.log(2))); // = k log2(|P|)
				// debug
				Message.add("\n + " + noOfErrors + "log2("
						+ (net.getPlaces().size()) + ")       // k log2(|P|)",
						3);

				// store how many events could be replayed in a row without
				// failure so far..
				seqLengthOfGoodEvents = goodEventsCounter;

				if (seqLengthOfGoodEvents != 0) {
					// add encoding cost for LAST block (is 0 if last event was
					// a failure)
					int upperBound = getUpperBound(Math
							.log(seqLengthOfGoodEvents)
							/ Math.log(2));
					traceEncodingCost += 2 * (upperBound + 1) + 1; // = 2 * ( [
					// log2(bj)
					// ] + 1) +
					// 1
					// debug
					Message
							.add(
									"\n + 2 * ( [ log2("
											+ (seqLengthOfGoodEvents)
											+ ") ] + 1) + 1       // 2 * ( [ log2(bj) ] + 1) + 1",
									3);
				} else {
					// cost to encode 0 block length is 3 bits
					traceEncodingCost += 3;
					// debug
					Message.add("\n + 3       // 3 (block length of 0)", 3);
				}
			}
		}

	}

}
