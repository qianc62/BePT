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

package org.processmining.analysis;

import javax.swing.JComponent;

import org.processmining.framework.plugin.Plugin;

/**
 * The interface implemented by all analysis algorithms.
 * <p>
 * This interface is the minimum that should be implemented by an analysis
 * algorithm that is used in the process mining framework.
 * 
 * @author Peter van den Brand
 * @version 1.0
 */
public interface AnalysisPlugin extends Plugin {

	/**
	 * Returns the input items needed by this analysis algorithm. The framework
	 * uses this information to let the user select appropriate inputs.
	 * 
	 * @return the input items accepted by this analysis algorithm
	 */
	public AnalysisInputItem[] getInputItems();

	/**
	 * Start this analysis algorithm.
	 * 
	 * @param inputs
	 *            the inputs chosen by the user
	 * @return user interface to the result of the analysis algorithm
	 */
	public JComponent analyse(AnalysisInputItem[] inputs);
}
