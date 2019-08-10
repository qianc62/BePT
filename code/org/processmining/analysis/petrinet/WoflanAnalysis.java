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

/*
 * $Archive: /ProcessMining/src/org/processmining/analysis/petrinet/WoflanAnalysis.java $
 * Last changed by: $Author: Bfvdonge $
 * Last changed at: $Date: 3-02-06 11:45 $
 * Revision number: $Revision: 5 $
 * $NoKeywords: $
 *
 * Copyright (c) 2005 Eindhoven Technical University of Technology
 * All rights reserved.
 */
package org.processmining.analysis.petrinet;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * <p>
 * Title: WoflanAnalysis
 * </p>
 * <p>
 * Description: Woflan Analysis Plugin
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005 Eric Verbeek
 * </p>
 * <p>
 * Company: Technische Universiteit Eindhoven
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */

public class WoflanAnalysis implements AnalysisPlugin {

	public WoflanAnalysis() {
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("Petrinet") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof PetriNet) {
						return true;
					}
				}
				return false;
			}
		} };
		return items;
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		PetriNet net = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet) {
				net = (PetriNet) o[i];
			}
		}
		return new WoflanAnalysisUI(net);
	}

	public String getName() {
		return ("Woflan Analysis");
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:analysis:woflan";
	}

}
