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

package org.processmining.analysis.heuristics;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * @author Ana Karla A. de Medeiros, Peter van den Brand
 * @version 1.0
 */

public class HeuristicsNetInputItem extends AnalysisInputItem {

	public HeuristicsNetInputItem(String label) {
		super(label);
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof HeuristicsNet) {
				return true;
			}
		}
		return false;
	}
}
