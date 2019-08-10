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

package org.processmining.analysis.recommendation;

import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.recommendation.contrib.RecommendationContributor;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.PluginCollection;

/**
 * Collection of analysis plugins.
 * 
 * @see AnalysisPlugin
 * @author Peter van den Brand
 * @version 1.0
 */

public class RecommendationCollection extends PluginCollection {

	private static RecommendationCollection instance = null;

	protected RecommendationCollection() {
	}

	/**
	 * Returns an instance of an <code>AnalysisPluginCollection</code>.
	 * 
	 * @return an instance of an <code>AnalysisPluginCollection</code>
	 */
	public static RecommendationCollection getInstance() {
		if (instance == null) {
			instance = new RecommendationCollection();
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
		return plugin instanceof RecommendationContributor;
	}
}
