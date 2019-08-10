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

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisResult;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;

/**
 * Calculates the encoding length needed to encode the log in the model with
 * error recovery, with en explicit encoding (always space reserved for the
 * potential error encoding), and directly specifying the violating transition.
 * 
 * @author Anne Rozinat
 */
public class EEVTPreciseness extends MDLPrecisenessMetric {

	protected EEVTPreciseness() {
		super(
				"Explicit Encoding with Violating Transition",
				"Metric determining the encoding cost for the given "
						+ "log in the given Petri net model based on the explicit encoding of potential errors for each replay "
						+ "step (recording directly the violating transition that was not enabled).");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.mdl.MDLPrecisenessMetric#getEncodingCost(org
	 * .processmining.framework.ui.slicker.ProgressPanel,
	 * org.processmining.framework.models.petrinet.PetriNet,
	 * org.processmining.framework.log.LogReader)
	 */
	public int getEncodingCost(ProgressPanel progress, PetriNet aNet,
			LogReader aLog) {
		measurer = new EEVTEncoder();
		return super.getEncodingCost(progress, aNet, aLog);
	}

	class EEVTEncoder extends PrecisenessMeasurer {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.processmining.analysis.mdl.MDLPrecisenessMetric.PrecisenessMeasurer
		 * #evaluateNumberOfEnabledTasks(int)
		 */
		protected void evaluateNumberOfEnabledTasks(int amountOfEnabled) {
			traceEncodingCost += (Math.log(amountOfEnabled + 1) / Math.log(2)); // =
			// log2(amountOfEnabled
			// +
			// 1)
			// debug
			Message.add("\n + log2(" + (amountOfEnabled + 1)
					+ ")       // log2(amountOfEnabled + 1)", 3);
		}

		/**
		 * Adds up the punishment of the violating transitions at the end of log
		 * replay.
		 */
		// protected void takePostReplayMeasurement(LogReplayAnalysisResult
		// result) {
		// traceEncodingCost += noOfErrors *
		// (Math.log(net.getTransitions().size()) / Math.log(2)); // = k
		// log2(|T|)
		// // debug
		// Message.add("\n + " + noOfErrors + "log2(" +
		// (net.getTransitions().size()) + ")       // k log2(|T|)", 3);
		// }
		/**
		 * Adds up the punishment of the violating transitions at the end of
		 * trace replay.
		 */
		protected void takePostTraceReplayMeasurement(
				LogReplayAnalysisResult result, ReplayedLogTrace pi) {
			traceEncodingCost += noOfErrors
					* (Math.log(net.getTransitions().size()) / Math.log(2)); // =
			// k
			// log2(|T|)
			// debug
			Message.add("\n + " + noOfErrors + "log2("
					+ (net.getTransitions().size()) + ")       // k log2(|T|)",
					3);
			super.takePostTraceReplayMeasurement(result, pi);
		}
	}
}
