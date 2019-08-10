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
package org.processmining.analysis.mdl;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.slicker.ProgressPanel;

/**
 * Simple "compactness" metric based on the number of arcs and nodes contained
 * in the Petri net. (Does not abstract from potential Start and End tasks in
 * the model yet).
 * 
 * @author Anne Rozinat, Christian Guenther
 */
public class ElementBasedCompactness extends MDLCompactnessMetric {

	protected ElementBasedCompactness() {
		super(
				"Element-based compactness",
				"Simple metric only considering the number of "
						+ "arcs and nodes in the given Petri net as an indicator of compactness.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.mdl.MDLBaseMetric#getEncodingCost(org.
	 * processmining.framework.ui.slicker.ProgressPanel)
	 */
	public int getEncodingCost(ProgressPanel progress, PetriNet aNet,
			LogReader aLog) {
		net = aNet;
		encodingCost = net.getEdges().size() + net.getNodes().size();
		// TODO: should abstract from START and COMPLETE tasks
		return (int) encodingCost;
	}
}
