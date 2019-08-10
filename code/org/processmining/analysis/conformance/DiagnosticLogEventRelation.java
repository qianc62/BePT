package org.processmining.analysis.conformance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.processmining.framework.ui.Message;

/**
 * Data structure for keeping track of the log event relations in a log trace or
 * an execution trace of a model. <br>
 * Note that this class can be used both to represent the relations between log
 * events in a trace or model execution path and to capture the whole behavioral
 * relation for the log or the process model.
 * 
 * TODO - consider making subclasses implementing these two different behaviors!
 * 
 * @see addDiagnosticLogEvent
 * @see updateLogEventRelation
 * 
 * @author arozinat
 */
public class DiagnosticLogEventRelation {

	/**
	 * Maps the name of each log event within the current trace on its
	 * diagnostic data structure (which includes their relations to other log
	 * events).
	 */
	private HashMap<String, DiagnosticLogEvent> logEvents = new HashMap<String, DiagnosticLogEvent>();

	/**
	 * Retrieves all diagnostic log events contained in this relation.
	 * 
	 * @return all diagnostic log events
	 */
	public Collection<DiagnosticLogEvent> getDiagnosticLogEvents() {
		return logEvents.values();
	}

	/**
	 * Gets the specified log event from the internal list of log events.
	 * 
	 * @param namePlusType
	 *            the string consisting of name and type specifies the log event
	 *            to be retrieved
	 * @return the diagnostic log event if found, null otherwise
	 */
	public DiagnosticLogEvent getDiagnosticLogEvent(String namePlusType) {
		return logEvents.get(namePlusType);
	}

	/**
	 * Determines whether the given elements are in Sometimes Follows relation.
	 * 
	 * @param first
	 *            the first element in the relation
	 * @param second
	 *            the second element in the relation
	 * @param threshold
	 *            the threshold for the sometimes relation
	 * @return whether they are in sometimes follows relation (according to
	 *         threshold)
	 */
	public boolean areInSFRelation(String first, String second, int threshold) {
		DiagnosticLogEvent firstLE = logEvents.get(first);
		DiagnosticLogEvent secondLE = logEvents.get(second);
		// check whether they are in SF relation
		if (firstLE != null) {
			Set<DiagnosticLogEvent> SF = firstLE
					.getSometimesRelationForwards(threshold);
			if (SF.contains(secondLE) == true) {
				return true;
			} else {
				return false;
			}
		} else {
			// firstLE has never been observed in the log (per definition Never
			// relation)
			// TODO check: should actually never happen because of external
			// normalization of log relation
			return false;
		}
	}

	/**
	 * Determines whether the given elements are in Sometimes Follows relation.
	 * 
	 * @param first
	 *            the first element in the relation
	 * @param second
	 *            the second element in the relation
	 * @return whether they are in sometimes follows relation (according to
	 *         threshold 0)
	 */
	public boolean areInSFRelation(String first, String second) {
		return areInSFRelation(first, second, 0);
	}

	/**
	 * Determines whether the given elements are in Sometimes Precedes relation.
	 * 
	 * @param first
	 *            the first element in the relation
	 * @param second
	 *            the second element in the relation
	 * @param threshold
	 *            the threshold for the sometimes relation
	 * @return whether they are in sometimes Precedes relation (according to
	 *         threshold)
	 */
	public boolean areInSBRelation(String first, String second, int threshold) {
		DiagnosticLogEvent firstLE = logEvents.get(first);
		DiagnosticLogEvent secondLE = logEvents.get(second);
		// check whether they are in SF relation
		if (firstLE != null) {
			Set<DiagnosticLogEvent> SB = firstLE
					.getSometimesRelationsBackwards(threshold);
			if (SB.contains(secondLE) == true) {
				return true;
			} else {
				return false;
			}
		} else {
			// firstLE has never been observed in the log (per definition Never
			// relation)
			// TODO check: should actually never happen because of external
			// normalization of log relation
			return false;
		}
	}

	/**
	 * Determines whether the given elements are in Sometimes Precedes relation.
	 * 
	 * @param first
	 *            the first element in the relation
	 * @param second
	 *            the second element in the relation
	 * @return whether they are in sometimes Precedes relation (according to
	 *         threshold 0)
	 */
	public boolean areInSBRelation(String first, String second) {
		return areInSBRelation(first, second, 0);
	}

	/**
	 * Determines whether the given elements are in Always Follows relation.
	 * 
	 * @param first
	 *            the first element in the relation
	 * @param second
	 *            the second element in the relation
	 * @param threshold
	 *            the threshold for the Always relation
	 * @return whether they are in Always follows relation (according to
	 *         threshold)
	 */
	public boolean areInAFRelation(String first, String second, int threshold) {
		DiagnosticLogEvent firstLE = logEvents.get(first);
		DiagnosticLogEvent secondLE = logEvents.get(second);
		// check whether they are in SF relation
		if (firstLE != null) {
			Set<DiagnosticLogEvent> AF = firstLE
					.getAlwaysRelationsForwards(threshold);
			if (AF.contains(secondLE) == true) {
				return true;
			} else {
				return false;
			}
		} else {
			// firstLE has never been observed in the log (per definition Never
			// relation)
			// TODO check: should actually never happen because of external
			// normalization of log relation
			return false;
		}
	}

	/**
	 * Determines whether the given elements are in Always Follows relation.
	 * 
	 * @param first
	 *            the first element in the relation
	 * @param second
	 *            the second element in the relation
	 * @return whether they are in Always follows relation (according to
	 *         threshold 0)
	 */
	public boolean areInAFRelation(String first, String second) {
		return areInAFRelation(first, second, 0);
	}

	/**
	 * Determines whether the given elements are in Always Precedes relation.
	 * 
	 * @param first
	 *            the first element in the relation
	 * @param second
	 *            the second element in the relation
	 * @param threshold
	 *            the threshold for the Always relation
	 * @return whether they are in Always Precedes relation (according to
	 *         threshold)
	 */
	public boolean areInABRelation(String first, String second, int threshold) {
		DiagnosticLogEvent firstLE = logEvents.get(first);
		DiagnosticLogEvent secondLE = logEvents.get(second);
		// check whether they are in SF relation
		if (firstLE != null) {
			Set<DiagnosticLogEvent> AB = firstLE
					.getAlwaysRelationsBackwards(threshold);
			if (AB.contains(secondLE) == true) {
				return true;
			} else {
				return false;
			}
		} else {
			// firstLE has never been observed in the log (per definition Never
			// relation)
			// TODO check: should actually never happen because of external
			// normalization of log relation
			return false;
		}
	}

	/**
	 * Determines whether the given elements are in Always Precedes relation.
	 * 
	 * @param first
	 *            the first element in the relation
	 * @param second
	 *            the second element in the relation
	 * @return whether they are in Always Precedes relation (according to
	 *         threshold 0)
	 */
	public boolean areInABRelation(String first, String second) {
		return areInABRelation(first, second, 0);
	}

	/**
	 * Determines whether the given elements are in Never Follows relation.
	 * 
	 * @param first
	 *            the first element in the relation
	 * @param second
	 *            the second element in the relation
	 * @param threshold
	 *            the threshold for the Never relation
	 * @return whether they are in Never follows relation (according to
	 *         threshold)
	 */
	public boolean areInNFRelation(String first, String second, int threshold) {
		DiagnosticLogEvent firstLE = logEvents.get(first);
		DiagnosticLogEvent secondLE = logEvents.get(second);
		// check whether they are in SF relation
		if (firstLE != null) {
			Set<DiagnosticLogEvent> NF = firstLE
					.getNeverRelationsForwards(threshold);
			if (NF.contains(secondLE) == true) {
				return true;
			} else {
				return false;
			}
		} else {
			// firstLE has never been observed in the log
			// although this is per definition Never relation we do not want to
			// show
			// relations where the first element did not occur
			// TODO check: should actually never happen because of external
			// normalization of log relation
			return false;
		}
	}

	/**
	 * Determines whether the given elements are in Never Follows relation.
	 * 
	 * @param first
	 *            the first element in the relation
	 * @param second
	 *            the second element in the relation
	 * @return whether they are in Never follows relation (according to
	 *         threshold 0)
	 */
	public boolean areInNFRelation(String first, String second) {
		return areInNFRelation(first, second, 0);
	}

	/**
	 * Determines whether the given elements are in Never Precedes relation.
	 * 
	 * @param first
	 *            the first element in the relation
	 * @param second
	 *            the second element in the relation
	 * @param threshold
	 *            the threshold for the Never relation
	 * @return whether they are in Never Precedes relation (according to
	 *         threshold)
	 */
	public boolean areInNBRelation(String first, String second, int threshold) {
		DiagnosticLogEvent firstLE = logEvents.get(first);
		DiagnosticLogEvent secondLE = logEvents.get(second);
		// check whether they are in SF relation
		if (firstLE != null) {
			Set<DiagnosticLogEvent> NB = firstLE
					.getNeverRelationsBackwards(threshold);
			if (NB.contains(secondLE) == true) {
				return true;
			} else {
				return false;
			}
		} else {
			// firstLE has never been observed in the log (per definition Never
			// relation)
			// although this is per definition Never relation we do not want to
			// show
			// relations where the first element did not occur
			// TODO check: should actually never happen because of external
			// normalization of log relation
			return false;
		}
	}

	/**
	 * Determines whether the given elements are in Never Precedes relation.
	 * 
	 * @param first
	 *            the first element in the relation
	 * @param second
	 *            the second element in the relation
	 * @return whether they are in Never Precedes relation (according to
	 *         threshold 0)
	 */
	public boolean areInNBRelation(String first, String second) {
		return areInNBRelation(first, second, 0);
	}

	// ///////////////////// LOCAL USAGE ONLY METHODS
	// ////////////////////////////////////

	/**
	 * Registers a new activity for current log trace. <br>
	 * Call this method in order to add a newly observed log event for a log
	 * trace or possible execution trace in the model.
	 * 
	 * @param name
	 *            the name of the log event
	 * @param type
	 *            the event type (such as start or complete)
	 */
	public void addDiagnosticLogEvent(String name, String type) {
		DiagnosticLogEvent newActivity = null;
		// checkk whether exists already
		if (logEvents.containsKey(name + type)) {
			newActivity = logEvents.get(name + type);
		} else {
			newActivity = new DiagnosticLogEvent(name, type);
		}
		// update activity counter
		newActivity.incOccurrenceCount();
		// update the activity relations
		Iterator<String> it = logEvents.keySet().iterator();
		while (it.hasNext()) {
			DiagnosticLogEvent predecessor = logEvents.get(it.next());
			newActivity.addPreActivity(predecessor);
			predecessor.addPostActivity(newActivity);
		}
		logEvents.put(name + type, newActivity);
	}

	// ////////////////////////////// GLOBAL USAGE ONLY METHODS
	// ///////////////////////////

	/**
	 * Updates the global log event relations with respect to the given log
	 * event. <br>
	 * Call this method in order to update the log relations for the whole log
	 * or process model.
	 * 
	 * @param newEvent
	 * @param similarInst
	 */
	public void updateLogEventRelation(DiagnosticLogEvent newEvent,
			int similarInst) {
		String namePlusType = newEvent.getModelElementName()
				+ newEvent.getEventType();
		DiagnosticLogEvent eventInGlobalRelation = null;

		// find or create log event in global relation
		if (logEvents.containsKey(namePlusType)) {
			eventInGlobalRelation = logEvents.get(namePlusType);
		} else {
			// note that occurrence count is initialized with 0 (in contrast to
			// super class)
			eventInGlobalRelation = new DiagnosticLogEvent(newEvent
					.getModelElementName(), newEvent.getEventType());
			// add to global relation!
			logEvents.put(namePlusType, eventInGlobalRelation);
		}

		// increment occurrence counter for this log event (occurrence means
		// occurrence per trace)
		// note that Always, Never and Sometimes relations must be calculated
		// with respect to
		// occurrence of the log event
		eventInGlobalRelation.incOccurrenceCount(similarInst);

		// increment pre-activity counter
		Iterator<DiagnosticLogEvent> preActivities = newEvent
				.getPreActivities().iterator();
		while (preActivities.hasNext()) {
			DiagnosticLogEvent currentPre = preActivities.next();
			// check whether pre-activity is already contained in relation
			// matrix
			String currentPreString = currentPre.getModelElementName()
					+ currentPre.getEventType();
			DiagnosticLogEvent preInGlobalRelation = null;
			if (logEvents.containsKey(currentPreString)) {
				preInGlobalRelation = logEvents.get(currentPreString);
			} else {
				preInGlobalRelation = new DiagnosticLogEvent(currentPre
						.getModelElementName(), currentPre.getEventType());
				// add to global relation!
				logEvents.put(currentPreString, preInGlobalRelation);
			}
			// now increment pre-actvity counter
			eventInGlobalRelation.addPreActivity(preInGlobalRelation,
					similarInst);
		}

		// increment post-activity counter
		Iterator<DiagnosticLogEvent> postActivities = newEvent
				.getPostActivities().iterator();
		while (postActivities.hasNext()) {
			DiagnosticLogEvent currentPost = postActivities.next();
			// check whether pre-activity is already contained in relation
			// matrix
			String currentPreString = currentPost.getModelElementName()
					+ currentPost.getEventType();
			DiagnosticLogEvent postInGlobalRelation = null;
			if (logEvents.containsKey(currentPreString)) {
				postInGlobalRelation = logEvents.get(currentPreString);
			} else {
				postInGlobalRelation = new DiagnosticLogEvent(currentPost
						.getModelElementName(), currentPost.getEventType());
				// add to global relation!
				logEvents.put(currentPreString, postInGlobalRelation);
			}
			// now increment pre-actvity counter
			eventInGlobalRelation.addPostActivity(postInGlobalRelation,
					similarInst);
		}
	}

	/**
	 * Walks through all the local log event relations and adds a zero entry for
	 * those log events that are not present in the local relation. This is
	 * needed in order to calculate the correct Never (and Sometimes) relations.
	 */
	public void completeRelationByZeroEntries() {
		Iterator<DiagnosticLogEvent> temp = logEvents.values().iterator();
		while (temp.hasNext()) {
			DiagnosticLogEvent current = temp.next();
			// update all log events that do not contain current with zero
			// relation
			Iterator<DiagnosticLogEvent> all = logEvents.values().iterator();
			while (all.hasNext()) {
				DiagnosticLogEvent toBeUpdated = all.next();
				if (toBeUpdated.getPreActivities().contains(current) == false) {
					// add zero entry
					toBeUpdated.addPreActivity(current, 0);
				}
				if (toBeUpdated.getPostActivities().contains(current) == false) {
					// add zero entry
					toBeUpdated.addPostActivity(current, 0);
				}
			}
		}
	}

	/**
	 * Walks through all the local log event relations and adds a zero entry for
	 * those log events that are not present in the local relation. This is
	 * needed in order to calculate the correct Never (and Sometimes) relations.
	 * 
	 * @param the
	 *            set of log events that should be covered by the relation
	 */
	public void completeRelationByExternalZeroEntries(
			Collection<DiagnosticLogEvent> referenceEvents) {
		Iterator<DiagnosticLogEvent> temp = referenceEvents.iterator();
		while (temp.hasNext()) {
			DiagnosticLogEvent currentInModel = temp.next();
			DiagnosticLogEvent currentInLog = getDiagnosticLogEvent(currentInModel
					.getModelElementName()
					+ currentInModel.getEventType());
			if (currentInLog == null) {
				DiagnosticLogEvent neverOccurred = new DiagnosticLogEvent(
						currentInModel.getModelElementName(), currentInModel
								.getEventType());

				// put dummy log event entry (in order to find it in isInNF()
				// and isInNB() relation methods)
				// but don't keep any relations to other log events
				logEvents.put(neverOccurred.getModelElementName()
						+ neverOccurred.getEventType(), neverOccurred);

				// there is no such log event contained in this relation yet -->
				// normalize!
				// update all log events that do not contain current with zero
				// relation
				Iterator<DiagnosticLogEvent> all = logEvents.values()
						.iterator();
				while (all.hasNext()) {
					DiagnosticLogEvent toBeUpdated = all.next();
					// add zero entry
					toBeUpdated.addPreActivity(neverOccurred, 0);
					toBeUpdated.addPostActivity(neverOccurred, 0);
				}
			}
		}
	}

	/**
	 * Writes the log event relations to the Message Console.
	 * 
	 * @param threshold
	 *            the barrier telling when something counts as happened "always"
	 *            or "never".
	 */
	public void printRelationsToMessageConsole(int threshold) {
		// print the log event relations
		Iterator<DiagnosticLogEvent> it = logEvents.values().iterator();
		while (it.hasNext()) {
			DiagnosticLogEvent logEvent = it.next();
			// print log event name
			Message.add("\n" + logEvent.toString() + ": "
					+ logEvent.getOccurrenceCount());

			// /// FORWARD RELATIONS
			// print always follows relation
			Message.add("Actvities in Always-follows relation: ");
			Iterator<DiagnosticLogEvent> print = logEvent
					.getAlwaysRelationsForwards(threshold).iterator();
			while (print.hasNext()) {
				DiagnosticLogEvent current = print.next();
				Message.add(current.getModelElementName()
						+ current.getEventType());
			}
			// print never follows relation
			Message.add("Actvities in Never-follows relation: ");
			print = logEvent.getNeverRelationsForwards(threshold).iterator();
			while (print.hasNext()) {
				DiagnosticLogEvent current = print.next();
				Message.add(current.getModelElementName()
						+ current.getEventType());
			}
			// print sometimes follows relation
			Message.add("Actvities in Sometimes-follows relation: ");
			print = logEvent.getSometimesRelationForwards(threshold).iterator();
			while (print.hasNext()) {
				DiagnosticLogEvent current = print.next();
				Message.add(current.getModelElementName()
						+ current.getEventType());
			}

			// /// BACKWARDS RELATIONS
			// print always precedes relation
			Message.add("Actvities in Always-precedes relation: ");
			print = logEvent.getAlwaysRelationsBackwards(threshold).iterator();
			while (print.hasNext()) {
				DiagnosticLogEvent current = print.next();
				Message.add(current.getModelElementName()
						+ current.getEventType());
			}
			// print never precedes relation
			Message.add("Actvities in Never-precedes relation: ");
			print = logEvent.getNeverRelationsBackwards(threshold).iterator();
			while (print.hasNext()) {
				DiagnosticLogEvent current = print.next();
				Message.add(current.getModelElementName()
						+ current.getEventType());
			}
			// print sometimes precedes relation
			Message.add("Actvities in Sometimes-precedes relation: ");
			print = logEvent.getSometimesRelationsBackwards(threshold)
					.iterator();
			while (print.hasNext()) {
				DiagnosticLogEvent current = print.next();
				Message.add(current.getModelElementName()
						+ current.getEventType());
			}
		}
	}

	/**
	 * Prints how often any log event has occurred some time before or after
	 * each log event.
	 */
	public void printOccurrenceCountsToMessageConsole() {
		// print the log event relations
		Iterator<DiagnosticLogEvent> it = logEvents.values().iterator();
		while (it.hasNext()) {
			DiagnosticLogEvent logEvent = it.next();
			// print pre-activities including occurrence count
			Message.add("Pre-activities: ");
			Iterator<DiagnosticLogEvent> preActivities = logEvent
					.getPreActivities().iterator();
			while (preActivities.hasNext()) {
				DiagnosticLogEvent currentPre = preActivities.next();
				int occurrenceCount = logEvent.getPreActivityStatus(currentPre);
				Message.add(currentPre.getModelElementName()
						+ currentPre.getEventType() + " occurred in "
						+ occurrenceCount
						+ " traces some time before current activity.");
			}

			// print post-activities including occurrence count
			Message.add("Post-activities: ");
			Iterator<DiagnosticLogEvent> postActivities = logEvent
					.getPostActivities().iterator();
			while (postActivities.hasNext()) {
				DiagnosticLogEvent currentPost = postActivities.next();
				int occurrenceCount = logEvent
						.getPostActivityStatus(currentPost);
				Message.add(currentPost.getModelElementName()
						+ currentPost.getEventType() + " occurred in "
						+ occurrenceCount
						+ " traces some time after current activity.");
			}
		}
	}
}
