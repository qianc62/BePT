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

package org.processmining.framework.models.petrinet.algorithms.logReplay;

/**
 * Enum class representing all the different analysis methods that are
 * available. Each metric will be associated to such a method, and each method
 * only gathers the information needed in order to compute the selected metrics. <br>
 * Note that the listed analysis techniques (besides the log replay method) are
 * only used by the conformance checker. However, the list can be extended by
 * anybody who wants to use the general mechanisms provided in this package for
 * supporting further analysis techniques.
 * 
 * @see AnalysisMethod
 * @see AnalysisResult
 * 
 * @author Anne Rozinat
 */
public class AnalysisMethodEnum implements Cloneable {

	private final String methodName; /* The name of the current analysis method. */

	/**
	 * The log replay analysis method.
	 */
	public static final AnalysisMethodEnum LOG_REPLAY = new AnalysisMethodEnum(
			"Log Replay Analysis");

	/**
	 * The state space analysis method.
	 */
	public static final AnalysisMethodEnum STATE_SPACE = new AnalysisMethodEnum(
			"State Space Analysis");

	/**
	 * The structural analysis method.
	 */
	public static final AnalysisMethodEnum STRUCTURAL = new AnalysisMethodEnum(
			"Structural Analysis");

	/**
	 * The constructor is private to only allow for pre-defined analysis
	 * methods.
	 * 
	 * @param name
	 *            the name of the analysis method.
	 */
	private AnalysisMethodEnum(String name) {
		this.methodName = name;
	}

	/**
	 * Gets the current analysis method.
	 * 
	 * @return a String containing the analysis method name
	 */
	public String toString() {
		return methodName;
	}

	/**
	 * Overridden to specify when two AnalysisMethodEnums are considered to be
	 * equal.
	 * 
	 * @param o
	 *            Object the <code>AnalysisMethodEnum</code> to be compared with
	 * @return boolean <code>true</code> if they relate to the same nominal
	 *         value, <code>false</code> otherwise
	 */
	public boolean equals(Object o) {
		// check object identity first
		if (this == o) {
			return true;
		}
		// check type (which includes check for null)
		return (o instanceof AnalysisMethodEnum)
				&& methodName.equals(((AnalysisMethodEnum) o).toString());
	}

	/**
	 * Overridden to produce the same hash code for equal objects.
	 * 
	 * @return int The hash code calculated.
	 */
	public int hashCode() {
		// simple recipe for generating hashCode given by
		// Effective Java (Addison-Wesley, 2001)
		int result = 17;
		result = 37 * result + methodName.hashCode();
		return result;
	}

	/**
	 * Makes a deep copy of this object. Overrides {@link Object#clone clone}.
	 * 
	 * @return the cloned object
	 */
	public Object clone() {
		AnalysisMethodEnum newObject = null;
		try {
			newObject = (AnalysisMethodEnum) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		// the name attribute should be cloned by the Object.clone() already
		return newObject;
	}
}
