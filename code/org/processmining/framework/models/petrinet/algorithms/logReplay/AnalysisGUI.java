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

import java.util.*;

import javax.swing.*;

import org.processmining.framework.plugin.*;

/**
 * This class can be used to create the resulting content for some analysis. As
 * an example, it is used for each tab in the {@link ConformanceAnalysisResults
 * ConformanceAnalysisResults} frame.
 * 
 * @author arozinat
 */
public class AnalysisGUI extends JPanel implements Provider {

	/**
	 * Required for a serializable class (generated quickfix). Not directly
	 * used.
	 */
	private static final long serialVersionUID = -9001223414040689539L;

	/**
	 * Contains the {@link AnalysisResult AnalysisResult} objects this category
	 * registered for.
	 */
	protected Set<AnalysisResult> myAnalysisResults;

	/**
	 * Default constructor.
	 * 
	 * @param analysisResults
	 *            the set of the {@link AnalysisResult AnalysisResult} objects
	 *            this category registered for
	 */
	public AnalysisGUI(Set<AnalysisResult> analysisResults) {
		myAnalysisResults = analysisResults;
	}

	/**
	 * Specifies objects provided by the analysis methods executed, so that they
	 * can be further used to, e.g., export an item.
	 * 
	 * @return <code>null</code> (should be overridden by subclasses)
	 */
	public ProvidedObject[] getProvidedObjects() {
		return null;
	}

	/**
	 * Updates the analysis results for the current subset of the log (i.e.,
	 * only for the selected process instances).
	 * 
	 * @param updatedAnalysisResults
	 *            the result object filled with new values
	 */
	public void updateResults(LogReplayAnalysisResult updatedAnalysisResults) {
		// do nothing
	}
}
