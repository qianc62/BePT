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

import org.processmining.framework.models.petrinet.PetriNet;

/**
 * Calculates how "compact" the model is in itself. <br>
 * Base class for metrics that calculate the encoding cost of the given Petri
 * net in some form.
 * 
 * @author Anne Rozinat, Christian Guenther
 */
public abstract class MDLCompactnessMetric extends MDLBaseMetric {

	protected PetriNet net;
	protected double encodingCost = 0;

	/**
	 * Creates a new "compactness" metric for the given petri net.
	 * 
	 * @param aName
	 *            the name of this metric (to be passed to super)
	 * @param aDescription
	 *            the description of this metric (to be passed to super)
	 * @param aNet
	 *            the given Petri net model
	 */
	protected MDLCompactnessMetric(String aName, String aDescription) {
		super(aName, aDescription);
	}
}
