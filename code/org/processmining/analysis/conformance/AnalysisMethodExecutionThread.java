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

package org.processmining.analysis.conformance;

import java.util.Iterator;

import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethod;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;

/**
 * Custom thread implementation that executes the given {@link AnalysisMethod
 * AnalysisMethod} and provides those categories with the results that have
 * asked for it.
 * 
 * @author arozinat
 */
public class AnalysisMethodExecutionThread extends Thread {

	private AnalysisMethod myMethod;
	private ThreadNotificationTarget myNotificationTarget;

	/**
	 * Creates this thread and initializes its attributes.
	 * 
	 * @param method
	 *            the analysis method to be executed in this thread
	 * @param settings
	 *            the settings frame which has to be notified after the work is
	 *            done
	 */
	public AnalysisMethodExecutionThread(AnalysisMethod method,
			ThreadNotificationTarget target) {
		myMethod = method;
		myNotificationTarget = target;
	}

	/**
	 * Executes the given analysis method, provides all registered categores
	 * with the results, and informs the settings frame that it is done.
	 * 
	 * @see ConformanceAnalysisSettings#threadDone threadDone
	 * @see ConformanceAnalysisConfiguration#hasRegisteredFor hasRegisteredFor
	 */
	public void run() {
		// invoke analysis method
		AnalysisResult currentResult = myMethod.analyse(myNotificationTarget
				.getAnalysisConfiguration());
		// get those categories that registered for this result
		Iterator allCategories = myNotificationTarget
				.getAnalysisConfiguration().getChildConfigurations().iterator();
		while (allCategories.hasNext()) {
			AnalysisConfiguration category = (AnalysisConfiguration) allCategories
					.next();
			if (category.hasRegisteredFor(myMethod.getIdentifier())) {
				category.addAnalysisResult(currentResult);
			}
		}
		// notify settings frame
		myNotificationTarget.threadDone(this);
	}
}
