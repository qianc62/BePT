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

import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethod;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisMethod;

/**
 * Custom thread implementation that executes the given {@link AnalysisMethod
 * AnalysisMethod} and provides those categories with the results that have
 * asked for it. It is required to have the analysis running in a thread.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class PerformanceAnalysisMethodExecutionThread extends Thread {

	private AnalysisMethod myMethod;
	private PerformanceAnalysisSettings mySettings;
	private PerformanceConfiguration config;

	/**
	 * Creates this thread and initializes its attributes.
	 * 
	 * @param method
	 *            the analysis method to be executed in this thread
	 * @param conf
	 *            BottleneckConfiguration: the BottleneckConfiguration-object
	 *            used
	 * @param set
	 *            PerformanceAnalysisSettings: the PerformanceAnalysisSettings
	 *            object used
	 */
	public PerformanceAnalysisMethodExecutionThread(
			LogReplayAnalysisMethod method, PerformanceConfiguration conf,
			PerformanceAnalysisSettings set) {
		myMethod = method;
		config = conf;
		mySettings = set;
	}

	/**
	 * Executes the given analysis method, provides all registered categories
	 * with the results, and informs the settings frame that it is done.
	 */
	public void run() {
		// invoke analysis method
		AnalysisResult currentResult = myMethod.analyse(null);
		if (config != null) {
			config.addAnalysisResult(currentResult);
		}
		if (mySettings != null) {
			mySettings.setCurrentResult(currentResult);
			mySettings.threadDone(this);
		}
	}

}
