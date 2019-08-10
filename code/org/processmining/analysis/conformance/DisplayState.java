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

package org.processmining.analysis.conformance;

/**
 * Storing the visualization state of the conformance analysis result.
 * 
 * @author arozinat
 */
public class DisplayState {

	/**
	 * The name of the current visualization state.
	 */
	private final String name;

	/**
	 * The state of displaying the model.
	 */
	public static final DisplayState MODEL = new DisplayState("Model");

	/**
	 * The state of displaying the log.
	 */
	public static final DisplayState LOG = new DisplayState("Log");

	/**
	 * The state of displaying the log.
	 */
	public static final DisplayState BEHAVIORAL = new DisplayState("Behavioral");

	public static final DisplayState STRUCTURAL = new DisplayState("Structural");

	public boolean redundantInvisiblesOption = false;

	public boolean alternativeDuplicatesOption = false;

	/**
	 * Represents the model visualization option of indicating the token counter
	 * results.
	 */
	public boolean alwaysFollowsOption = false;

	/**
	 * Represents the model visualization option of indicating the tasks that
	 * have remained enabled.
	 */
	public boolean neverFollowsOption = false;

	/**
	 * Represents the model visualization option of indicating the tasks that
	 * have failed execution.
	 */
	public boolean alwaysPrecedesOption = false;

	/**
	 * Represents the model visualization option of indicating the tasks that
	 * have been executed.
	 */
	public boolean neverPrecedesOption = false;

	/**
	 * Represents the model visualization option of indicating the token counter
	 * results.
	 */
	public boolean tokenCouterOption = false;

	/**
	 * Represents the model visualization option of indicating the tasks that
	 * have remained enabled.
	 */
	public boolean remainingTransitionsOption = false;

	/**
	 * Represents the model visualization option of indicating the tasks that
	 * have failed execution.
	 */
	public boolean failedTransitionsOption = false;

	/**
	 * Represents the model visualization option of indicating the tasks that
	 * have been executed.
	 */
	public boolean pathCoverageOption = false;

	/**
	 * Represents the model visualization option of indicating the number of
	 * times an edge has been passed during log replay.
	 */
	public boolean passedEdgesOption = false;

	/**
	 * Represents the log visualization option of indicating the log events that
	 * have failed execution.
	 */
	public boolean failedEventsOption = false;

	/**
	 * The constructor is private to only allow for pre-defined display states.
	 * 
	 * @param name
	 *            The new display state.
	 */
	private DisplayState(String name) {
		this.name = name;
	}

	/**
	 * Get the current display state.
	 * 
	 * @return The current display state.
	 */
	public String toString() {
		return name;
	}
}
