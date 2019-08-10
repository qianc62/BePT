package org.processmining.analysis.conformance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.processmining.framework.log.LogEvent;

/**
 * Data structure to represent a log event, i.e., the actual type of log event,
 * such as 'A complete' (i.e., including event type and opposed to the audit
 * trail entry, which is an occurrence of a log event).
 * 
 * @author anne
 */
public class DiagnosticLogEvent extends LogEvent {

	/**
	 * Contains all log events, which have occurred some time before this log
	 * event.
	 */
	private HashMap<DiagnosticLogEvent, Integer> preActivities = new HashMap<DiagnosticLogEvent, Integer>();

	/**
	 * Contains all log events, which have occurred some time after this log
	 * event.
	 */
	private HashMap<DiagnosticLogEvent, Integer> postActivities = new HashMap<DiagnosticLogEvent, Integer>();

	/**
	 * Constructor taking the model element and the event type for this log
	 * event.
	 * 
	 * @param name
	 *            the name of the task in the corresponding process
	 * @param type
	 *            the life-cycle stage of this log event with respect to the
	 *            specified activity
	 */
	public DiagnosticLogEvent(String name, String type) {
		super(name, type);
		// needs to be explicitly set to prevent side effects
		// when pre- or post activities are added to global matrix
		// before they are themselves considered for addition!
		occurenceCount = 0;
	}

	/**
	 * Increments the pre-occurrence status of the indicated log event by 1.
	 * 
	 * @param event
	 *            the log event which has been occurred some time ago
	 */
	public void addPreActivity(DiagnosticLogEvent event) {
		addPreActivity(event, 1);
	}

	/**
	 * Increments the pre-occurrence status of the indicated log event by the
	 * indicated value.
	 * 
	 * @param event
	 *            the log event which has been occurred some time ago
	 * @param value
	 *            the number of times this activity newly occurred before this
	 *            one
	 */
	public void addPreActivity(DiagnosticLogEvent event, int value) {
		if (preActivities.containsKey(event)) {
			int counter = preActivities.get(event).intValue();
			counter = counter + value;
			preActivities.put(event, new Integer(counter));
		} else {
			preActivities.put(event, new Integer(value));
		}
	}

	/**
	 * Gets the pre-occurrence status of the indicated log event.
	 * 
	 * @param event
	 *            the log event which status is requested
	 * @return the status of pre-occurrence
	 */
	public int getPreActivityStatus(DiagnosticLogEvent event) {
		if (preActivities.containsKey(event) == false) {
			return 0;
		} else {
			return preActivities.get(event).intValue();
		}
	}

	/**
	 * Retrieves all activities that have occurred before this one.
	 * 
	 * @return the set of activities that preceded this activity some time ago
	 */
	public Set<DiagnosticLogEvent> getPreActivities() {
		return preActivities.keySet();
	}

	/**
	 * Increments the post-occurrence status of the indicated log event by 1.
	 * 
	 * @param event
	 *            the log event which status is to be updated
	 */
	public void addPostActivity(DiagnosticLogEvent event) {
		addPostActivity(event, 1);
	}

	/**
	 * Increments the post-occurrence status of the indicated log event by the
	 * indicated value.
	 * 
	 * @param event
	 *            the log event which status is to be updated
	 * @param value
	 *            the number of times this activity newly occurred after this
	 *            one
	 */
	public void addPostActivity(DiagnosticLogEvent event, int value) {
		if (postActivities.containsKey(event)) {
			int counter = postActivities.get(event).intValue();
			counter = counter + value;
			postActivities.put(event, new Integer(counter));
		} else {
			postActivities.put(event, new Integer(value));
		}
	}

	/**
	 * Gets the post-occurrence status of the indicated log event.
	 * 
	 * @param event
	 *            the log event which status is requested
	 * @return the status of pre-occurrence
	 */
	public int getPostActivityStatus(DiagnosticLogEvent event) {
		if (postActivities.containsKey(event) == false) {
			return 0;
		} else {
			return postActivities.get(event).intValue();
		}
	}

	/**
	 * Retrieves all activities that have occurred after this one.
	 * 
	 * @return the set of activities that occurred after this activity
	 */
	public Set<DiagnosticLogEvent> getPostActivities() {
		return postActivities.keySet();
	}

	// ///// actually determine the Always, Never, and Sometimes relations

	/**
	 * Retrieves all those log events that always happened (some time) after
	 * this one.
	 * 
	 * @param threshold
	 *            the number of times that an activity may not have occurred
	 *            after this one while still being considered to be in
	 *            "always follows" relation
	 * @return the set of those activities that always followed. May be empty
	 */
	public Set<DiagnosticLogEvent> getAlwaysRelationsForwards(int threshold) {
		Set<DiagnosticLogEvent> result = new HashSet<DiagnosticLogEvent>();
		Iterator<DiagnosticLogEvent> it = getPostActivities().iterator();
		while (it.hasNext()) {
			DiagnosticLogEvent currentPost = it.next();
			if (getPostActivityStatus(currentPost) >= (occurenceCount - threshold)) {
				result.add(currentPost);
			}
		}
		return result;
	}

	/**
	 * Retrieves all those log events that never happened (some time) after this
	 * one. <br>
	 * Note that when calling this method it is assumed that 0 relations are
	 * contained explicitly (rather than being indicated via the absence of that
	 * particular pre- or post-activity).
	 * 
	 * @param threshold
	 *            the number of times that an activity may have occurred after
	 *            this one while still being considered to be in "never follows"
	 *            relation
	 * @return the set of those activities that never followed. May be empty
	 */
	public Set<DiagnosticLogEvent> getNeverRelationsForwards(int threshold) {
		Set<DiagnosticLogEvent> result = new HashSet<DiagnosticLogEvent>();
		Iterator<DiagnosticLogEvent> it = getPostActivities().iterator();
		while (it.hasNext()) {
			DiagnosticLogEvent currentPost = it.next();
			if (getPostActivityStatus(currentPost) <= threshold) {
				result.add(currentPost);
			}
		}
		return result;
	}

	/**
	 * Retrieves all those log events that sometimes happened (some time) after
	 * this one. <br>
	 * Note that when calling this method it is assumed that 0 relations are
	 * contained explicitly (rather than being indicated via the absence of that
	 * particular pre- or post-activity).
	 * 
	 * @param threshold
	 *            the number of times that an activity may not have occurred
	 *            after this one while still being considered to be in
	 *            "always follows" relation and the number of times that an
	 *            activity may have occurred after this one while still being
	 *            considered to be in "never follows" relation as sometimes :=
	 *            all - (always + never)
	 * @return the set of those activities that sometimes followed. May be empty
	 */
	public Set<DiagnosticLogEvent> getSometimesRelationForwards(int threshold) {
		Set<DiagnosticLogEvent> result = new HashSet<DiagnosticLogEvent>();
		Set<DiagnosticLogEvent> alwaysRelation = getAlwaysRelationsForwards(threshold);
		Set<DiagnosticLogEvent> neverRelation = getNeverRelationsForwards(threshold);
		Iterator<DiagnosticLogEvent> it = getPostActivities().iterator();
		while (it.hasNext()) {
			DiagnosticLogEvent currentPost = it.next();
			if (alwaysRelation.contains(currentPost) == false
					&& neverRelation.contains(currentPost) == false) {
				result.add(currentPost);
			}
		}
		return result;
	}

	/**
	 * Retrieves all those log events that always happened (some time) before
	 * this one.
	 * 
	 * @param threshold
	 *            the number of times that an activity may not have occurred
	 *            before this one while still being considered to be in
	 *            "always precedes" relation
	 * @return the set of those activities that always preceeded. May be empty
	 */
	public Set<DiagnosticLogEvent> getAlwaysRelationsBackwards(int threshold) {
		Set<DiagnosticLogEvent> result = new HashSet<DiagnosticLogEvent>();
		Iterator<DiagnosticLogEvent> it = getPreActivities().iterator();
		while (it.hasNext()) {
			DiagnosticLogEvent currentPre = it.next();
			if (getPreActivityStatus(currentPre) >= (occurenceCount - threshold)) {
				result.add(currentPre);
			}
		}
		return result;
	}

	/**
	 * Retrieves all those log events that never happened (some time) before
	 * this one. <br>
	 * Note that when calling this method it is assumed that 0 relations are
	 * contained explicitly (rather than being indicated via the absence of that
	 * particular pre- or post-activity).
	 * 
	 * @param threshold
	 *            the number of times that an activity may have occurred before
	 *            this one while still being considered to be in
	 *            "never precedes" relation
	 * @return the set of those activities that never preceeded. May be empty
	 */
	public Set<DiagnosticLogEvent> getNeverRelationsBackwards(int threshold) {
		Set<DiagnosticLogEvent> result = new HashSet<DiagnosticLogEvent>();
		Iterator<DiagnosticLogEvent> it = getPreActivities().iterator();
		while (it.hasNext()) {
			DiagnosticLogEvent currentPre = it.next();
			if (getPreActivityStatus(currentPre) <= threshold) {
				result.add(currentPre);
			}
		}
		return result;
	}

	/**
	 * Retrieves all those log events that sometimes happened (some time) before
	 * this one. <br>
	 * Note that when calling this method it is assumed that 0 relations are
	 * contained explicitly (rather than being indicated via the absence of that
	 * particular pre- or post-activity).
	 * 
	 * @param threshold
	 *            the number of times that an activity may not have occurred
	 *            before this one while still being considered to be in
	 *            "always precedes" relation and the number of times that an
	 *            activity may have occurred before this one while still being
	 *            considered to be in "never precedes" relation as sometimes :=
	 *            all - (always + never)
	 * @return the set of those activities that sometimes preceeded. May be
	 *         empty
	 */
	public Set<DiagnosticLogEvent> getSometimesRelationsBackwards(int threshold) {
		Set<DiagnosticLogEvent> result = new HashSet<DiagnosticLogEvent>();
		Set<DiagnosticLogEvent> alwaysRelation = getAlwaysRelationsBackwards(threshold);
		Set<DiagnosticLogEvent> neverRelation = getNeverRelationsBackwards(threshold);
		Iterator<DiagnosticLogEvent> it = getPreActivities().iterator();
		while (it.hasNext()) {
			DiagnosticLogEvent currentPre = it.next();
			if (alwaysRelation.contains(currentPre) == false
					&& neverRelation.contains(currentPre) == false) {
				result.add(currentPre);
			}
		}
		return result;
	}
}
