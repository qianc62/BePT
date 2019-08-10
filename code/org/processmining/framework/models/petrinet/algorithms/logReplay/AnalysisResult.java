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

/**
 * An analysis result belongs to a specific analysis method and stores all the
 * relevant information needed afterwards to, e.g., calculate metrics, or
 * provide diagnostic visualizations.
 * 
 * @see AnalysisMethod#analyse
 * 
 * @author Anne Rozinat (a.rozinat@tm.tue.nl)
 */
public abstract class AnalysisResult {

	/**
	 * A list of {@link AnalysisConfiguration AnalysisConfiguration} objects
	 * representing those optional metrics that have been chosen by the user in
	 * the analysis settings frame.
	 */
	protected AnalysisConfiguration myAnalysisOptions;

	/**
	 * Constructor.
	 * 
	 * @param analysisOptions
	 *            the configuration settings that have been chosen by the user
	 *            (if <code>null</code>, the complete analysis should be carried
	 *            out)
	 */
	public AnalysisResult(AnalysisConfiguration analysisOptions) {
		myAnalysisOptions = analysisOptions;
	}
}
