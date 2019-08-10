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

package org.processmining.analysis.log.scale;

import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.PluginCollection;

/**
 * Collection of analysis plugins.
 * 
 * @see AnalysisPlugin
 * @author Peter van den Brand
 * @version 1.0
 */

public class ScaleCollection extends PluginCollection {

	private static ScaleCollection instance = null;

	protected ScaleCollection() {
	}

	/**
	 * Returns an instance of an <code>AnalysisPluginCollection</code>.
	 * 
	 * @return an instance of an <code>AnalysisPluginCollection</code>
	 */
	public static ScaleCollection getInstance() {
		if (instance == null) {
			instance = new ScaleCollection();
		}
		return instance;
	}

	/**
	 * Returns <code>true</code> if the given plugin is an
	 * <code>ProcessInstanceScale</code>.
	 * 
	 * @param plugin
	 *            the plugin to check
	 * @return <code>true</code> if the given plugin is an
	 *         <code>ProcessInstanceScale</code>, <code>false</code> otherwise.
	 */
	public boolean isValidPlugin(Plugin plugin) {
		return plugin instanceof ProcessInstanceScale;
	}
}
