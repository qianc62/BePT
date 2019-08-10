/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.hlprocess;

import java.util.HashSet;
import java.util.Set;

import org.processmining.framework.models.hlprocess.HLTypes.Perspective;
import org.processmining.framework.models.hlprocess.HLTypes.TimeUnit;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLDistribution;
import org.processmining.framework.ui.Message;

/**
 * Holds information about global characteristics of the process. For example,
 * the case arrival rate that was measured, or specified, for the corresponding
 * high level process.
 * 
 * @see HLProcess
 */
public class HLGlobal implements Cloneable {

	/** the name of this highlevelprocess */
	protected String name = "";
	/** the default time unit for this process value */
	protected HLTypes.TimeUnit timeUnit = HLTypes.TimeUnit.MINUTES;
	/** the case generation scheme for this process */
	protected HLDistribution caseGenerationScheme = new HLGeneralDistribution();
	/** the covered perspectives for this process */
	protected Set<Perspective> coveredPerspectives = new HashSet<Perspective>();
	/** the high level process this element belongs to */
	protected HLProcess process;

	/**
	 * Default constructor.
	 * 
	 * @param theProcess
	 *            the high-level process this global info refers to
	 */
	public HLGlobal(HLProcess theProcess) {
		process = theProcess;
	}

	/**
	 * Sets the name of the high level process.
	 * 
	 * @param name
	 *            the name of the high level process
	 */
	public void setName(String aName) {
		name = aName;
	}

	/**
	 * Retrieves the name of this high level process.
	 * 
	 * @return the name of the high level process
	 */
	public String getName() {
		return name;
	}

	/**
	 * Adds a new perspective, i.e., indicates that information was provided for
	 * this perspective.
	 * 
	 * @param perspective
	 *            the new perspective that is covered by the high level process
	 */
	public void addPerspective(Perspective perspective) {
		coveredPerspectives.add(perspective);
	}

	/**
	 * Retrieves the set of perspectives that are covered by this high level
	 * process.
	 * 
	 * @return the set of perspectives that are covered
	 */
	public Set<Perspective> getPerspectives() {
		return coveredPerspectives;
	}

	/**
	 * Retrieves the case arrival distribution for this process.
	 * 
	 * @return the case arrival distribution of the process. If no case
	 *         generation scheme has been set, a default distribution will be
	 *         returned.
	 */
	public HLDistribution getCaseGenerationScheme() {
		return caseGenerationScheme;
	}

	/**
	 * Retrieves the unit in which all the provided information related to time
	 * should be interpreted. <br>
	 * Note that currently it is only possible to specify the time unit
	 * globally. That is, it will apply to all time-related values in the high
	 * level process.
	 * 
	 * @see #getCaseGenerationScheme(HLDistribution)
	 * @see HLActivity#getExecutionTime()
	 * @see HLActivity#getWaitingTime()
	 * @see HLActivity#getSojournTime()
	 * 
	 * @return the time unit to be used for interpretation
	 */
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	/**
	 * Provides a case arrival distribution for this process.
	 * 
	 * @param dist
	 *            the distribution for the case arrival scheme of the process
	 */
	public void setCaseGenerationScheme(HLDistribution dist) {
		caseGenerationScheme = dist;
	}

	/**
	 * Specifies the unit in which the provided information related to time
	 * should be interpreted. <br>
	 * Note that currently it is only possible to specify the time unit
	 * globally. That is, it will apply to all time-related values in the high
	 * level process.
	 * 
	 * @see #getCaseGenerationScheme(HLDistribution)
	 * @see HLActivity#getExecutionTime()
	 * @see HLActivity#getWaitingTime()
	 * @see HLActivity#getSojournTime()
	 * 
	 * @param unit
	 *            the time unit to be used for interpretation
	 */
	public void setTimeUnit(HLTypes.TimeUnit unit) {
		timeUnit = unit;
	}

	/**
	 * Changes the time unit for time-related information.
	 * <p>
	 * This methods changes current values as they are recalculated based on the
	 * previous and the new time unit. Note that this can lead to rounding
	 * problems as in CPN Tools the time is in integers only.
	 * <p>
	 * If simply the reference time should be set, then use the method
	 * {@link #setTimeUnit(TimeUnit)} instead.
	 * 
	 * @see #getCaseGenerationScheme(HLDistribution)
	 * @see HLActivity#getExecutionTime()
	 * @see HLActivity#getWaitingTime()
	 * @see HLActivity#getSojournTime()
	 * @param unit
	 *            the new time unit to be used
	 */
	public void changeTimeUnit(TimeUnit unit) {
		if (getTimeUnit() != unit) {
			// something has changed
			double conversionValue = (double) getTimeUnit()
					.getConversionValue()
					/ (double) unit.getConversionValue();
			// apply the conversion to each highleveltransition
			for (HLActivity hlTransition : process.getActivities()) {
				hlTransition.getExecutionTime().setTimeMultiplicationValue(
						conversionValue);
				hlTransition.getSojournTime().setTimeMultiplicationValue(
						conversionValue);
				hlTransition.getWaitingTime().setTimeMultiplicationValue(
						conversionValue);
			}
			// apply the conversion to the case generation scheme
			getCaseGenerationScheme().setTimeMultiplicationValue(
					conversionValue);
			// set the time unit to the new value
			setTimeUnit(unit);
		}
	}

	/**
	 * Makes a deep copy of the object. <br>
	 * Note that this method needs to be extended as soon as there are
	 * attributes added to the class which are not primitive or immutable.
	 */
	public Object clone() {
		HLGlobal o = null;
		try {
			o = (HLGlobal) super.clone();
			// clone case generation scheme
			o.caseGenerationScheme = (HLDistribution) this.caseGenerationScheme
					.clone();
			// cloned the coveredperspectives
			o.coveredPerspectives = (Set<HLTypes.Perspective>) ((HashSet<HLTypes.Perspective>) this.coveredPerspectives)
					.clone();
			return o;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}

	/**
	 * Writes the global high level process information to the test log.
	 */
	public void writeToTestLog() {
		Message.add("<HLGlobal>", Message.TEST);
		// timing info
		Message.add("DistributionType of case generation scheme: "
				+ this.getCaseGenerationScheme().getDistributionType(),
				Message.TEST);
		// number of covered perspectives
		Message.add("Number of Covered Perspectives ["
				+ this.getPerspectives().size() + "]", Message.TEST);
		Message.add("</HLGlobal>", Message.TEST);
	}
}
