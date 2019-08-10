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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.logReplay.Measurer;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedTransition;

/**
 * The diagnostic transition can be enhanced by diagnostic information to be
 * stored during log replay and will be used to form a part of the diagnostic
 * Petri net.
 * 
 * @author arozinat
 */
public class DiagnosticTransition extends ReplayedTransition {

	/**
	 * Data structure for storage of the amount of firings of this transition.
	 */
	private int numberOfFirings;

	/**
	 * Data structure for remembering those cases in which the transition was
	 * not enabled when it was requested to fire, i.e. needed to be enabled
	 * artificially to allow for progress in log replay.
	 */
	private boolean failedExecution;

	/**
	 * Data structure for remembering those cases in which the transition was
	 * remaining enabled after log replay, i.e. the trace did not complete
	 * properly.
	 */
	private boolean remainingEnabled;

	/**
	 * Counts the number of process instances for which this transition has
	 * failed successful execution.
	 */
	private int failedInstances = 0;

	/**
	 * Counts the number of process instances for which this transition has
	 * remained enabled.
	 */
	private int remainingInstances = 0;

	/**
	 * The number of process instances for which this this transition neither
	 * failed nor was remaining.
	 */
	private int aggregatedZero;

	// ///////////////////////// advance structural appropriateness diagnostics

	/**
	 * Indicates whether this task is a redundant invisible task.
	 */
	private boolean isRedundantInvisibleTask = false;

	/**
	 * Indicates whether this taks is an alternative duplicate task.
	 */
	private boolean isAlternativeDuplicateTask = false;

	// ///////////////////////// advanced behavioral appropriateness diagnostics

	/**
	 * Contains all those log events that always followed the key log event in
	 * the log (but only sometimes in the model).
	 */
	private Set<Transition> alwaysFollows = new HashSet<Transition>();

	/**
	 * Contains all those log events that never followed the key log event in
	 * the log (but sometimes in the model).
	 */
	private Set<Transition> neverFollows = new HashSet<Transition>();

	/**
	 * Contains all those log events that always followed the key log event in
	 * the log (but only sometimes in the model).
	 */
	private Set<Transition> alwaysPrecedes = new HashSet<Transition>();

	/**
	 * Contains all those log events that never followed the key log event in
	 * the log (but sometimes in the model).
	 */
	private Set<Transition> neverPrecedes = new HashSet<Transition>();

	/**
	 * <p>
	 * The constructor creates a normal transition but additionally initializes
	 * the diagnostic information for every process instance specified.
	 * </p>
	 * <p>
	 * Note that the the copy constructor must be simulated on this level to
	 * assign the right Petri net to the new Transition, which eventually is
	 * required by Grappa.
	 * </p>
	 * 
	 * @param t
	 *            The template transition for creating the object (to be passed
	 *            to super class).
	 * @param net
	 *            The Petri net it belongs to (to be passed to super class).
	 * @param caseIDs
	 *            The list of process instances that want to be able to store
	 *            results of the conformance analysis in this diagnostic
	 *            transition.
	 */
	public DiagnosticTransition(Transition t, PetriNet net, ArrayList caseIDs) {
		super(t, net, caseIDs);
	}

	// /////// OVERRIDDEN METHODS FOR STORING DIAGNOSTIC INFORMATION //////////

	/**
	 * {@inheritDoc} Counts the consumed and produced tokens and remembers the
	 * passed edges.
	 */
	protected void takePreFiringMeasurement(ReplayedLogTrace diagnosticTrace,
			Measurer measurer) {
		// store diagnostic information
		countConsumedAndProducedTokens((DiagnosticLogTrace) diagnosticTrace,
				measurer);
		rememberPassedEdges(diagnosticTrace);
	}

	/**
	 * {@inheritDoc} Increments the number of firings for this transition.
	 */
	protected void takePostFiringMeasurement(ReplayedLogTrace diagnosticTrace) {
		// update entry for that instance
		incrementNumberOfFirings(diagnosticTrace.getNumberOfProcessInstances());
	}

	/**
	 * Adds the specified number of process instances to the counter of traces
	 * that failed execution for this transition.
	 * 
	 * @param value
	 *            corresponds to the number of similar process instances
	 */
	public void addFailedInstances(int value) {
		failedInstances = failedInstances + value;
	}

	/**
	 * Adds the specified number of process instances to the counter of traces
	 * that remained enabled for this transition.
	 * 
	 * @param value
	 *            corresponds to the number of similar process instances
	 */
	public void addRemainedInstances(int value) {
		remainingInstances = remainingInstances + value;
	}

	/**
	 * Add the given number of process instances to the number of instances
	 * where this transition was neither remaining nor failed during log replay.
	 * 
	 * @param similarInstances
	 *            number of process instances for this trace
	 */
	public void addNumberInstancesNeitherFailedNorRemaining(int similarInstances) {
		aggregatedZero = aggregatedZero + similarInstances;
	}

	// /////// INTERNAL HELPER METHODS FOR STORING DIAGNOSTIC INFORMATION
	// ///////

	/**
	 * Increments the corresponding numberOfFirings entry by the given value.
	 * 
	 * @param value
	 *            corresponds to the number of similar process instances
	 */
	private void incrementNumberOfFirings(int value) {
		numberOfFirings = numberOfFirings + value;
	}

	/**
	 * Count the number of tokens to be consumed and produced by this transition
	 * and update the respective attributes of the diagnostic log trace.
	 * 
	 * @param diagnosticTrace
	 *            the diagnostic log trace which is currently replayed and for
	 *            which all the diagnostic infomation is remembered
	 * @param measurer
	 *            the measurer object in order to record diagnostic information
	 */
	private void countConsumedAndProducedTokens(
			DiagnosticLogTrace diagnosticTrace, Measurer measurer) {
		/** @todo This should be adapted to an arc weight > 1 */
		int consumedTokens = getPredecessors().size();
		int producedTokens = getSuccessors().size();
		// record measurement for current trace
		ConformanceMeasurer conformanceMeasurer = (ConformanceMeasurer) measurer;
		conformanceMeasurer.incrementConsumedTokens(consumedTokens);
		conformanceMeasurer.incrementProducedTokens(producedTokens);
	}

	/**
	 * Increments the number of times each of the incoming and outgoing edges
	 * has been passed during log replay.
	 * 
	 * @param piName
	 *            the ID of the log trace which is currently replayed and for
	 *            which all the diagnostic infomation is remembered
	 */
	private void rememberPassedEdges(ReplayedLogTrace pi) {
		Iterator it = this.getInEdgesIterator();
		while (it.hasNext()) {
			DiagnosticPNEdge currentEdge = (DiagnosticPNEdge) it.next();
			// update entry for that instance
			currentEdge.incrementNumberOfPasses(pi
					.getNumberOfProcessInstances());
		}
		it = this.getOutEdgesIterator();
		while (it.hasNext()) {
			DiagnosticPNEdge currentEdge = (DiagnosticPNEdge) it.next();
			// update entry for that instance
			currentEdge.incrementNumberOfPasses(pi
					.getNumberOfProcessInstances());
		}
	}

	/**
	 * Sets the corresponding failedExecution entry to true.
	 */
	public void setFailedExecution() {
		failedExecution = true;
	}

	/**
	 * Sets the corresponding remainingEnabled entry to true.
	 */
	public void setRemainingEnabled() {
		remainingEnabled = true;
	}

	/**
	 * Sets the corresponding failedExecution entry to true.
	 */
	public void setRedundantInvisibleTask() {
		isRedundantInvisibleTask = true;
	}

	/**
	 * Sets the corresponding remainingEnabled entry to true.
	 */
	public void setAlternativeDuplicateTask() {
		isAlternativeDuplicateTask = true;
	}

	/**
	 * Record that this task was always followed by the given task in the log
	 * (but only sometimes in the model.
	 * 
	 * @param trans
	 *            task that always followed this task
	 */
	public void addAlwaysFollows(Transition trans) {
		alwaysFollows.add(trans);
	}

	/**
	 * Record that this task was never followed by the given task in the log
	 * (but sometimes in the model.
	 * 
	 * @param trans
	 *            task that never followed this task
	 */
	public void addNeverFollows(Transition trans) {
		neverFollows.add(trans);
	}

	/**
	 * Record that this task was always preceded by the given task in the log
	 * (but only sometimes in the model.
	 * 
	 * @param trans
	 *            task that always preceded this task
	 */
	public void addAlwaysPrecedes(Transition trans) {
		alwaysPrecedes.add(trans);
	}

	/**
	 * Record that this task was never preceded by the given task in the log
	 * (but sometimes in the model.
	 * 
	 * @param trans
	 *            task that never preceded this task
	 */
	public void addNeverPrecedes(Transition trans) {
		neverPrecedes.add(trans);
	}

	/**
	 * Resets the log relation element counter to 0, so that it can be filled
	 * with new values (based on the new log selection).
	 */
	public void resetAlwaysAndNeverRelations() {
		alwaysFollows = new HashSet<Transition>();
		neverFollows = new HashSet<Transition>();
		alwaysPrecedes = new HashSet<Transition>();
		neverPrecedes = new HashSet<Transition>();
	}

	// /////////// READ ACCESS METHODS FOR DIAGNOSTIC INFORMATION
	// ///////////////

	/**
	 * Determines the amount of firings for this transition.
	 * 
	 * @return The amount of firings for this transition
	 */
	public int getNumberOfFirings() {
		return numberOfFirings;
	}

	/**
	 * Determine whether execution failed for this transition.
	 * 
	 * @return The failed execution status for this transition
	 */
	public boolean hasFailedExecution() {
		return failedExecution;
	}

	/**
	 * Determine whether the transition remained enabled for this transition.
	 * 
	 * @return The failed execution status for for this transition
	 */
	public boolean hasRemainedEnabled() {
		return remainingEnabled;
	}

	/**
	 * Check whether the transition has fired at least once.
	 * 
	 * @return True if has fired at least once, false otherwise
	 */
	public boolean hasFired() {
		if (getNumberOfFirings() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets the corresponding isRedundantInvisibleTask status.
	 */
	public boolean isRedundantInvisibleTask() {
		return isRedundantInvisibleTask;
	}

	/**
	 * Gets the corresponding isAlternativeDuplicateTask status.
	 */
	public boolean isAlternativeDuplicateTask() {
		return isAlternativeDuplicateTask;
	}

	/**
	 * Retrieves the tasks that were always followed the given task in the log
	 * (but only sometimes in the model.
	 * 
	 * @return all tasks that always followed this task
	 */
	public Set<Transition> getAlwaysFollows() {
		return alwaysFollows;
	}

	/**
	 * Retrieves the tasks that were never followed the given task in the log
	 * (but sometimes in the model.
	 * 
	 * @return all tasks that never followed this task
	 */
	public Set<Transition> getNeverFollows() {
		return neverFollows;
	}

	/**
	 * Retrieves the tasks that were always preceded the given task in the log
	 * (but only sometimes in the model.
	 * 
	 * @return all tasks that always preceded this task
	 */
	public Set<Transition> getAlwaysPrecedes() {
		return alwaysPrecedes;
	}

	/**
	 * Retrieves the tasks that were never preceded the given task in the log
	 * (but sometimes in the model.
	 * 
	 * @return all tasks that never preceded this task
	 */
	public Set<Transition> getNeverPrecedes() {
		return neverPrecedes;
	}

	// ////////////////////////////////////////////////////////////////////////

	/**
	 * Based on the current selection recorded at the DiagnosticPetriNet level,
	 * a tool tip containing information about the distribution of missing and
	 * remaining tokens with respect to the process instances.
	 * 
	 * @return The string to be displayed
	 */
	public String getDiagnosticToolTip(DiagnosticPetriNet net) {
		if ((remainingInstances != 0) || (failedInstances != 0)) {
			// now build tool tip
			// build head of table
			String toolTip = "<HTML><TABLE><TR><TD BGCOLOR=#FFFFFF>Error Type</TD><TD  BGCOLOR=#FFFFFF># Instances</TD></TR>";
			if (aggregatedZero != 0) {
				toolTip = toolTip
						+ "<TR><TD BGCOLOR=#80AA80>Neither </TD><TD BGCOLOR=#80AA80>"
						+ aggregatedZero + "</TD></TR>";
			}
			if (remainingInstances != 0) {
				toolTip = toolTip
						+ "<TR><TD BGCOLOR=#FF8080>Remained </TD><TD BGCOLOR=#FF8080>"
						+ remainingInstances + "</TD></TR>";
			}
			if (failedInstances != 0) {
				toolTip = toolTip
						+ "<TR><TD BGCOLOR=#FF8080>Failed </TD><TD BGCOLOR=#FF8080>"
						+ failedInstances + "</TD></TR>";
			}
			toolTip = toolTip + "</HTML>";
			return toolTip;
		} else {
			return getOrdinaryToolTip();
		}
	}

	/**
	 * Only displays the name of the transition.
	 * 
	 * @return the name of the transition as html string
	 */
	public String getOrdinaryToolTip() {
		// just display name
		String nameTmp = toString();
		String name = "";
		char[] arr = nameTmp.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == '\\') {
				// \n arrives as two chars '\\' and 'n'
				name += "<BR>";
				i++;
			} else {
				name += arr[i];
			}
		}
		return "<HTML>" + name + "</HTML>";
	}
}
