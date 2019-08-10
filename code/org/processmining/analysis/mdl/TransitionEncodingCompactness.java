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
 * Calculates the encoding length needed to encode the model from a transition
 * perspective. For each transition one needs to specify the input places and
 * output places.
 * 
 * @author Christian Guenther
 */
public class TransitionEncodingCompactness extends MDLCompactnessMetric {

	protected TransitionEncodingCompactness() {
		super(
				"Transition-based encoding compactness",
				"Metric determining the encoding cost for the given "
						+ "Petri net model based on the input and output place encoding for each of the transitions.");
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
		encodingCost = net.getTransitions().size();
		return (int) encodingCost;
	}
}
