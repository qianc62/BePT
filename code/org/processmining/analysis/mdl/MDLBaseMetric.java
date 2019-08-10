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
 * Base class for all MDL metrics.
 * 
 * @author Anne Rozinat, Christian Guenther
 */
public abstract class MDLBaseMetric {

	protected String name;
	protected String description;

	/**
	 * Creates a new metric based on a name and a description.
	 * 
	 * @param aName
	 *            the given name
	 * @param aDescription
	 *            the given description
	 * @see #getName()
	 * @see #getDescription()
	 * @see #getEncodingCost()
	 */
	protected MDLBaseMetric(String aName, String aDescription) {
		name = aName;
		description = aDescription;
	}

	/**
	 * Returns the name of this metric. This name should be meaningful enough to
	 * be e.g. displayed in a GUI element that allows to choose a specific
	 * metric from a list of available ones.
	 * 
	 * @return the name of this metric
	 */
	public String getName() {
		return name;
	}

	/**
	 * A description that allows the user of this metric to understand
	 * sufficiently what distinguishes this particular metrics from other
	 * available metrics.
	 * 
	 * @return the description about this metric
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @see #getName()
	 */
	public String toString() {
		return name;
	}

	/**
	 * Returns the encoding cost, i.e., the measured value for this MDL metric.
	 * 
	 * @param progress
	 *            the progress panel to indicate status of metric calculation
	 * @return the cost of encoding the event log in the given process model
	 * @param aNet
	 *            the given Petri net model
	 * @param aLog
	 *            the given Event log (can be null if not needed)
	 */
	public abstract int getEncodingCost(ProgressPanel progress, PetriNet aNet,
			LogReader aLog);

	/**
	 * Helper method to calculate upper bound of encoding costs.
	 * 
	 * @param value
	 *            the double value of which the upper bound should be determined
	 * @return the upper bound of the given value
	 */
	public int getUpperBound(double value) {
		int result = (int) value;
		double resultCompare = result;
		if (resultCompare < value) {
			return result + 1;
		} else {
			return result;
		}
	}
}
