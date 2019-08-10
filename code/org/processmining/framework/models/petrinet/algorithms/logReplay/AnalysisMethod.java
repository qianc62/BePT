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
 * Interface for an analysis method. Assuming an analysis method implementing
 * this interface eases its parameterized execution, such as used in
 * {@link org.processmining.analysis.conformance.AnalysisMethodExecutionThread
 * AnalysisMethodExecutionThread}. <br>
 * Note that this interface is not related to the notion of an
 * {@link org.processmining.analysis.AnalysisPlugin
 * org.processmining.analysis.AnalysisPlugin AnalysisPlugin}, but rather helps
 * coordinating different analysis techniques that are used within one single
 * plug-in.
 * 
 * @see AnalysisResult
 * 
 * @author Anne Rozinat (a.rozinat@tm.tue.nl)
 */
public interface AnalysisMethod {

	/**
	 * Starts the analysis.
	 * 
	 * @param analysisConfiguration
	 *            the given analysis configuration determining which parts of
	 *            the analysis have been chosen by the user (can be
	 *            <code>null</code> if the full analysis should be carried out)
	 * @return the corresponding AnalysisResult object
	 */
	public AnalysisResult analyse(AnalysisConfiguration analysisConfiguration);

	/**
	 * Gets the belonging identifier. In order to implement a new analysis
	 * method one has to extend the {@link AnalysisMethodEnum
	 * AnalysisMethodEnum} class by another nominal value denominating this kind
	 * of analysis method.
	 * 
	 * @return the nominal value identifying this analysis method
	 */
	public AnalysisMethodEnum getIdentifier();
}
