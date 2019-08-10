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

import org.processmining.framework.ui.Message;

/**
 * Assumes error-free log replay. Therefore, only the number of enabled
 * transitions in each stage of replay needs to be considered.
 * 
 * @author Anne Rozinat, Christian Guenther
 */
public class ErrorFreePreciseness extends MDLPrecisenessMetric {

	protected ErrorFreePreciseness() {
		super(
				"Error-free Encoding",
				"This metric assumes that the log fits the model, and only the number of enabled tasks "
						+ "per replay steps need to be considered for the encoding");
		measurer = new ErrorFreeEncoder();
	}

	class ErrorFreeEncoder extends PrecisenessMeasurer {

		/**
		 * Use the number of enabled tasks (measured before each replay step) to
		 * calculate different encoding costs.
		 * 
		 * @param amountOfEnabled
		 */
		protected void evaluateNumberOfEnabledTasks(int amountOfEnabled) {
			if (traceEncodingCost != -1 && amountOfEnabled != 0) {
				traceEncodingCost += (Math.log(amountOfEnabled) / Math.log(2)); // =
				// log2(amountOfEnabled)
				// debug message
				Message.add("\n + log2(" + amountOfEnabled
						+ ")       // log2(amountOfEnabled)", 3);
			} else {
				// this should not happen in errorfree log
				traceEncodingCost = -1;
			}
		}

	}
}
