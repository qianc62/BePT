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

package org.processmining.converting;

import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;

/**
 * The interface implemented by all converting algorithms.
 * <p>
 * This interface is the minimum that should be implemented by a converting
 * algorithm that is used in the process mining framework. See the documentation
 * on how to implement converting algorithms for more information.
 * 
 * @author Boudewijn van Dongen
 * @version 1.0
 */
public interface ConvertingPlugin extends Plugin {

	/**
	 * Execute this conversion algorithm on the given log file.
	 * 
	 * @param original
	 *            The original mining result
	 * @return the result of the conversion, in a <code>MininResult</code>
	 *         object
	 */
	public MiningResult convert(ProvidedObject original);

	/**
	 * This function tells the interface which results are accepted by this
	 * Plugin
	 * 
	 * @param original
	 *            The original mining result
	 * @return Whether or not this result is accepted
	 */
	public boolean accepts(ProvidedObject original);
}
