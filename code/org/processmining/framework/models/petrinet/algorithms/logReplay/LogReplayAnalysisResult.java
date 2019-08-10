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

import org.processmining.framework.log.*;
import org.processmining.framework.models.petrinet.*;

/**
 * Contains all the results obtained during log replay analysis. Can be used to
 * retrieve values for the implemented metrics and to get diagnostic
 * visualizations.
 * 
 * @see LogReplayAnalysisMethod
 * 
 * @author arozinat
 */
public class LogReplayAnalysisResult extends AnalysisResult {

	public PetriNet inputPetriNet;
	/** The original Petri net passed to the analysis method. */
	public LogReader inputLogReader;
	/** The original Log reader passed to the analysis method. */
	public ReplayedPetriNet replayedPetriNet;
	/** The Petri net enhanced with diagnostic information. */
	public ReplayedLogReader replayedLog;
	/** The Log reader enhanced with diagnostic information. */

	/**
	 * Per default set to true. Subclasses may make it depend on the metrics
	 * that have been chosen by the user.
	 * 
	 * @see #performLogReplay
	 */
	protected boolean performLogReplay = true;

	/**
	 * Default constructor creating the results object. Creates the diagnostic
	 * data structures {@link #initDiagnosticDataStructures
	 * initDiagnosticDataStructures}.
	 * 
	 * @param analysisOptions
	 *            the configuration as chosen in the settings frame
	 * @param net
	 *            the input PetriNet
	 * @param log
	 *            the input LogReader
	 * @param method
	 *            the LogReplayAnalysisMethod creating this result object
	 */
	public LogReplayAnalysisResult(AnalysisConfiguration analysisOptions,
			PetriNet net, LogReader log, LogReplayAnalysisMethod method) {
		super(analysisOptions);
		inputPetriNet = net;
		inputLogReader = log;
		// custom data structures may be built in sub classes
		initDiagnosticDataStructures();
	}

	/**
	 * Indicates whether the log replay should be performed. This is typically
	 * true as soon as at least one of the metrics based on log replay method
	 * has been chosen by the user.
	 * 
	 * @return <code>true</code> if log replay should be performed,
	 *         <code>false</code> otherwise
	 */
	public boolean performLogReplay() {
		return performLogReplay;
	}

	// //////// METHODS TO BE OVERRIDDEN BY SUBCLASSESS
	// /////////////////////////

	/**
	 * Initializes the diagnostic data structures needed to store the
	 * measurements taken during the log replay analysis. To be overridden by
	 * subclasses as soon as they define custom data structures.
	 */
	protected void initDiagnosticDataStructures() {
		replayedLog = new ReplayedLogReader(inputLogReader);
		replayedPetriNet = new ReplayedPetriNet(inputPetriNet, replayedLog
				.getLogTraceIDs());
	}
}
