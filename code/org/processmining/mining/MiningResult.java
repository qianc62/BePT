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

package org.processmining.mining;

import javax.swing.JComponent;

import org.processmining.framework.log.LogReader;

/**
 * Captures the result of the execution of a mining algorithm.
 * <p>
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

public interface MiningResult {

	/**
	 * Returns a component that contains the visualization of this mining
	 * result. This function should return <code>null</code> if this result
	 * cannot be visualized.
	 * 
	 * @return a component that contains the visualization of this mining result
	 *         or <code>null</code> if this result cannot be visualized
	 */
	public JComponent getVisualization();

	/**
	 * Returns the <code>LogReader</code> object that was used to generate this
	 * mining result. This function may return null.
	 * 
	 * @return the <code>LogReader</code> object that was used to generate this
	 *         mining result or null.
	 */
	public LogReader getLogReader();
}
