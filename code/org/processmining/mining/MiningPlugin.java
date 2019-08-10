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

import javax.swing.JPanel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.plugin.Plugin;

/**
 * The interface implemented by all mining algorithms.
 * <p>
 * This interface is the minimum that should be implemented by a mining
 * algorithm that is used in the process mining framework. See the documentation
 * on how to implement mining algorithms for more information.
 * 
 * @author Peter van den Brand
 * @version 1.0
 */
public interface MiningPlugin extends Plugin {

	/**
	 * Returns the option panel associated with this mining algorithm or
	 * <code>null</code> if it has no options panel. The option panel is the
	 * panel that is shown when a log file has just been opened. The user can
	 * set the options for the mining algorithm here.
	 * 
	 * @param summary
	 *            all known information about the log that's going to be mined
	 *            with this algorithm
	 * @return the option panel associated with this mining algorithm or
	 *         <code>null</code> if it has no options panel
	 */
	public JPanel getOptionsPanel(LogSummary summary);

	/**
	 * Execute this mining algorithm on the given log file. The <code>log</code>
	 * parameter provides methods to extract the process instances and audit
	 * trail entries (cases) from the log file. This method is allowed to return
	 * <code>null</code> in case there are no results.
	 * 
	 * @param log
	 *            process instances and audit trail entries
	 * @return the result of the mining algorithm, in a <code>MininResult</code>
	 *         object
	 */
	public MiningResult mine(LogReader log);
}
