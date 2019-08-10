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
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedPlace;

/**
 * The diagnostic place can be enhanced by diagnostic information to be stored
 * during log replay and will be used to form a part of the diagnostic Petri
 * net.
 * 
 * @author arozinat
 */
public class DiagnosticPlace extends ReplayedPlace {

	/**
	 * Data structure for storage of the amount of missing tokens (takes the
	 * number of similar process instances into account).
	 */
	private int missingTokens;

	/**
	 * Data structure for storage of the amount of missing tokens (does not take
	 * the number of similar process instances into account).
	 */
	private int missingTokensSemanticLevel;

	/**
	 * Data structure for storage of the amount of remaining tokens (takes the
	 * number of similar process instances into account).
	 */
	private int remainingTokens;

	/**
	 * Data structure for storage of the amount of remaining tokens (does not
	 * take the number of similar process instances into account).
	 */
	private int remainingTokensSemanticLevel;

	/**
	 * The number of process instances for which this there were neither tokens
	 * missing nor remaining in this place.
	 */
	private int aggregatedZero;

	/**
	 * Records for each amount of remaining tokens for how many process
	 * instances that many tokens were remaining (for tooltip).
	 */
	private HashMap<Integer, Integer> aggregatedRemaining = new HashMap<Integer, Integer>();

	/**
	 * Records for each amount of missing tokens for how many process instances
	 * that many tokens were missing (for tooltip).
	 */
	private HashMap<Integer, Integer> aggregatedMissing = new HashMap<Integer, Integer>();

	/**
	 * The constructor creates a normal place but additionally initializes the
	 * counter of missing and remaining tokens for every process instance
	 * specified.
	 * 
	 * @param p
	 *            The place (to be passed to super class).
	 * @param net
	 *            The Petri net it belongs to (to be passed to super class).
	 * @param caseIDs
	 *            The list of diagnostic log traces that want to be able to
	 *            store results of the conformance analysis in this diagnostic
	 *            place.
	 */
	public DiagnosticPlace(Place p, PetriNet net, ArrayList caseIDs) {
		super(p, net, caseIDs);
	}

	/**
	 * Increment the corresponding missingTokens entry by the specified value.
	 * 
	 * @param value
	 *            the value to be added to the current value
	 * @param the
	 *            number of similar instances for the current trace
	 */
	public void addMissingTokens(int value, int similarInstances) {
		// modify the corresponding entries
		missingTokensSemanticLevel = missingTokensSemanticLevel + value;
		missingTokens = missingTokens + (value * similarInstances);

		// check whether current "missing" amount is already present
		// can be done here because missing tokens are only determined once
		// (at the end of the replay of the trace)
		if (aggregatedMissing.containsKey(new Integer(value))) {
			// merge with existing entry
			Integer oldValue = (Integer) aggregatedMissing.get(new Integer(
					value));
			int newValue = oldValue.intValue() + similarInstances;
			aggregatedMissing.put(new Integer(value), new Integer(newValue));
		} else {
			// add new entry
			aggregatedMissing.put(new Integer(value), new Integer(
					similarInstances));
		}
	}

	/**
	 * Increment the corresponding remainingTokens entry by the specified value.
	 * 
	 * @param value
	 *            the value to be added to the current value
	 * @param the
	 *            number of similar instances for the current trace
	 */
	public void addRemainingTokens(int value, int similarInstances) {
		// modify the corresponding entries
		remainingTokensSemanticLevel = remainingTokensSemanticLevel + value;
		remainingTokens = remainingTokens + (value * similarInstances);

		// check whether current "remaining" amount is already present
		// can be done here because remaining tokens are only determined once
		// (at the end of the replay of the trace)
		if (aggregatedRemaining.containsKey(new Integer(value))) {
			// merge with existing entry
			Integer oldValue = (Integer) aggregatedRemaining.get(new Integer(
					value));
			int newValue = oldValue.intValue() + similarInstances;
			aggregatedRemaining.put(new Integer(value), new Integer(newValue));
		} else {
			// add new entry
			aggregatedRemaining.put(new Integer(value), new Integer(
					similarInstances));
		}
	}

	/**
	 * Add the given number of process instances to the number of instances
	 * where neither tokens were missing nor remaining during log replay.
	 * 
	 * @param similarInstances
	 *            number of process instances for this trace
	 */
	public void addNumberInstancesNeitherMissingNorRemaining(
			int similarInstances) {
		aggregatedZero = aggregatedZero + similarInstances;
	}

	// /////// READ ACCESS METHODS FOR DIAGNOSTIC INFORMATION //////////

	/**
	 * Determine the amount of missing tokens.
	 * 
	 * @return the amount of missing tokens in this place
	 */
	public int getMissingTokens() {
		return missingTokens;
	}

	/**
	 * Determine the amount of all missing tokens at this Place. Note that the
	 * number of similar instances subsumed by a logical log trace is not taken
	 * into account here, which among others is used for the visual feedback
	 * where solely the trace as a logical unit is of interest.
	 * 
	 * @return The amount of missing tokens in this place
	 */
	public int getMissingTokensOnSemanticLevel() {
		return missingTokensSemanticLevel;
	}

	/**
	 * Determine the amount of remaining tokens.
	 * 
	 * @return the amount of remaining tokens in this place
	 */
	public int getRemainingTokens() {
		return remainingTokens;
	}

	/**
	 * Determine the amount of all remaining tokens this Place. Note that the
	 * number of similar instances subsumed by the logical log trace is not
	 * taken into account here, which among others is used for the visual
	 * feedback where solely the trace as a logical unit is of interest.
	 * 
	 * @return The amount of remaining tokens in this place
	 */
	public int getRemainingTokensOnSemanticLevel() {
		return remainingTokensSemanticLevel;
	}

	// /////// HELPER METHODS FOR //////////

	/**
	 * Based on the current selection recorded at the DiagnosticPetriNet level,
	 * a tool tip containing information about the distribution of missing and
	 * remaining tokens with respect to the process instances.
	 * 
	 * @return The string to be displayed
	 */
	public String getDiagnosticToolTip(DiagnosticPetriNet net) {
		// only build diagnostic tool tip if there are any results
		if ((aggregatedMissing.size() != 0)
				|| (aggregatedRemaining.size() != 0)) {
			// now sort the entries and build tool tip
			TreeMap sortedRemaining = new TreeMap(aggregatedRemaining);
			Iterator traverseRemaining = sortedRemaining.keySet().iterator();
			// start from the middle to the beginning (because of ascending
			// sorting)
			String toolTip = "";
			while (traverseRemaining.hasNext()) {
				Integer currentAmount = (Integer) traverseRemaining.next();
				Integer currentInstances = (Integer) sortedRemaining
						.get(currentAmount);
				// append in front
				toolTip = "<TR><TD BGCOLOR=#FF8080>+"
						+ currentAmount.intValue()
						+ "</TD><TD BGCOLOR=#FF8080>"
						+ currentInstances.intValue() + "</TD></TR>" + toolTip;
			}
			// build head of table
			toolTip = "<HTML><TABLE><TR><TD BGCOLOR=#FFFFFF># Tokens</TD><TD  BGCOLOR=#FFFFFF># Instances</TD></TR>"
					+ toolTip;
			// proceed from middle to the end
			if (aggregatedZero != 0) {
				toolTip = toolTip
						+ "<TR><TD BGCOLOR=#80AA80>&#160;0 </TD><TD BGCOLOR=#80AA80>"
						+ aggregatedZero + "</TD></TR>";
			}
			TreeMap sortedMissing = new TreeMap(aggregatedMissing);
			Iterator traverseMissing = sortedMissing.keySet().iterator();
			while (traverseMissing.hasNext()) {
				Integer currentAmount = (Integer) traverseMissing.next();
				Integer currentInstances = (Integer) sortedMissing
						.get(currentAmount);
				toolTip = toolTip + "<TR><TD BGCOLOR=#FF8080>-"
						+ currentAmount.intValue()
						+ "</TD><TD BGCOLOR=#FF8080>"
						+ currentInstances.intValue() + "</TD></TR>";
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
	 * @return the name of the transition as string
	 */
	public String getOrdinaryToolTip() {
		// just display name
		return toString();
	}

	/**
	 * Creates a tool tip for this place stating the available performance
	 * measurements for the set of selected cases.
	 * 
	 * @param piNames
	 *            a list of process IDs
	 * @return the HTML tool tip (ready to be displayed)
	 */
	/*
	 * public String getPerformanceToolTip(ArrayList piNames) { double mt =
	 * getMeanSyncTime(piNames); double mw = getMeanWaitingTime(piNames); return
	 * "<html><table><tr><td>Synchronization time: </td><td>" + mt +
	 * "</td></tr>" + "<tr><td>Waiting time: </td><td>" + mw + "</td></tr>" +
	 * "<tr><td>Sojourn time: </td><td>" + (mw + mt) + "</td></tr>" +
	 * "</table></html>"; }
	 */

	// //////////////// GENERAL PURPOSE METHODS
	// /////////////////////////////////
	/**
	 * Makes a deep copy of the object. Note that this method needs to be
	 * extended as soon as there are attributes added to the class which are not
	 * primitive or immutable.
	 * 
	 * @return Object the cloned object
	 */
	public Object clone() {
		DiagnosticPlace o = null;
		o = (DiagnosticPlace) super.clone();
		// clone referenced objects to realize deep copy
		// if (missingTokens != null) {
		// o.missingTokens = (HashMap) missingTokens.clone();
		// }
		// if (remainingTokens != null) {
		// o.remainingTokens = (HashMap) remainingTokens.clone();
		// }
		// if (timeMeasurements != null) {
		/**
		 * @todo anne: check whether each of the timeMeasurement objects needs
		 *       to be cloned explicitly
		 */
		// o.timeMeasurements = (ArrayList) timeMeasurements.clone();
		// }
		return o;
	}
}

/**
 * Data structure consisting of some performance-related measurements for this
 * place. <br>
 * Suppose there is a transition with two input places p1 and p2, which has a
 * firing time stamp t. The tokens in those input places also have a timestamp
 * tj (which is derived from the transition that had produced the token): t1 and
 * t2, respectively. Then we can define tm as the greatest timestamp of the
 * input-place tokens, that is, max(t1,t2).
 * 
 * @see TimeMeasurement#waitTime waitTime
 * @see TimeMeasurement#syncTime syncTime
 * 
 * @author Boudewijn van Dongen
 */
// class TimeMeasurement implements Cloneable {
/**
 * The processID for which this measurement has been taken.
 */
// public String piName;
/**
 * The waiting time is the time a token spends time in a place although the
 * transition is already enabled (i.e., t - tm).
 */
// public long waitTime;
/**
 * The synchronization time is the time the token spends in a place until the
 * transition becomes enabled (i.e., tm - tj).
 */
// public long syncTime;
/**
 * Creates a new time measurement data structure.
 * 
 * @param piName
 *            the processID for which this measurement has been taken
 * @param waitTime
 *            the waiting time is the time a token spends time in a place
 *            although the transition is already enabled (i.e., t - tm)
 * @param syncTime
 *            the synchronization time is the time the token spends in a place
 *            until the transition becomes enabled (i.e., tm - tj)
 */
// public TimeMeasurement(String piName, long waitTime, long syncTime) {
// this.piName = piName;
// this.waitTime = waitTime;
// this.syncTime = syncTime;
// }
/**
 * Creates a string of the measured values as an HTML table row.
 * 
 * @return a string containing the processID, the synchronization time, and then
 *         the waiting time
 */
// public String toString() {
// return "<tr><td>" + piName + "</td>" +
// "<td>" + syncTime + "</td>" +
// "<td>" + waitTime + "</td></tr>";
// }
/**
 * Make a deep copy of the object. Note that this method needs to be extended as
 * soon as there are attributes added to the class which are not primitive or
 * immutable.
 * 
 * @return Object The cloned object.
 */
// /public Object clone() {
// TimeMeasurement o = null;
// try {
// o = (TimeMeasurement)super.clone();
// } catch (CloneNotSupportedException e) {
// e.printStackTrace();
// }
// clone referenced objects to realize deep copy
// return o;
// }
// }
