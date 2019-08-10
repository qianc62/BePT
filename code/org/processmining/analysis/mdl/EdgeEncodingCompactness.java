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
 * Calculates the encoding length needed to encode the model from an edge
 * perspective. For each edge one needs to specify the input place (or
 * transition) and output transition (or place).
 * 
 * @author Christian Guenther
 */
public class EdgeEncodingCompactness extends MDLCompactnessMetric {

	protected EdgeEncodingCompactness() {
		super(
				"Edge-based encoding compactness",
				"Metric determining the encoding cost for the given "
						+ "Petri net model based on the input and output node encoding for each of the edges.");
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
		encodingCost = net.getEdges().size();
		return (int) encodingCost;
	}
}
