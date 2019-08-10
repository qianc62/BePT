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

package org.processmining.analysis.socialnetwork;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.snamining.model.SocialNetworkMatrix;

/**
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Minseok Song
 * @version 1.0
 */
public class SocialNetworkAnalysisPlugin implements AnalysisPlugin {

	public SocialNetworkAnalysisPlugin() {
	}

	public String getHtmlDescription() {
		return "<p> This plug-in read SNA matrix and calculate centrality.<p> ";
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("SNA Matrix") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasSocialNetworkMatrix = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof SocialNetworkMatrix) {
						hasSocialNetworkMatrix = true;
					}
				}
				return hasSocialNetworkMatrix;
			}
		} };
		return items;

	}

	public String getName() {
		return "Analyze Social Network";
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		SocialNetworkMatrix snMatrix = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof SocialNetworkMatrix) {
				snMatrix = (SocialNetworkMatrix) o[i];
			}
		}

		return new SocialNetworkAnalysisUI(snMatrix);
	}
}
