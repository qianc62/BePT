/*
 * Created on May 19, 2005
 *
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * (c) 2005 Technische Universiteit Eindhoven, Christian W. Guenther
 * all rights reserved
 * 
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source 
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */
package org.processmining.mining.dmcscanning;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.processmining.framework.log.ProcessInstance;
import org.processmining.mining.dmcscanning.equivalence.ObjectEquivalence;
import org.processmining.mining.dmcscanning.logutils.AbstractEvent;
import org.processmining.mining.dmcscanning.logutils.LogItem;

/**
 * This class represents a Data Modification Cluster (DMC). Data modification
 * clusters extracted in the initial scan pass.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class Dmc implements LogItem {

	/**
	 * static counter value for providing unique id numbers
	 */
	protected static long id_counter = 0;

	/**
	 * Static facility to provide unique, ascending id numbers
	 * 
	 * @return a unique id number
	 */
	protected static long nextId() {
		long nextId = id_counter;
		if (id_counter >= Long.MAX_VALUE) {
			id_counter = 0;
		} else {
			id_counter++;
		}
		return nextId;
	}

	/**
	 * manually resets the static id counter to zero
	 */
	public static void resetIdCounter() {
		id_counter = 0;
	}

	protected ArrayList<AbstractEvent> events = null;
	protected Date startDate = null;
	protected Date endDate = null;
	protected Footprint footprint = null; // cache footprint for speedup
	protected long id_nr = 0;
	protected String id_string = null;

	/*
	 * by definition, all events within a DMC are from the same process
	 * instance. This property is cached to provide uniform access.
	 */
	protected ProcessInstance processInstance = null;

	/**
	 * creates a new DMC object
	 */
	public Dmc() {
		events = new ArrayList<AbstractEvent>();
		startDate = null;
		endDate = null;
		footprint = new Footprint();
		processInstance = null;
		id_nr = nextId();
		id_string = "DMC." + id_nr;
	}

	/**
	 * constructor
	 * 
	 * @param id
	 *            a custom id string
	 */
	public Dmc(String id) {
		events = new ArrayList<AbstractEvent>();
		startDate = null;
		endDate = null;
		footprint = new Footprint();
		processInstance = null;
		id_nr = nextId();
		id_string = id;
	}

	/**
	 * copy constructor
	 * 
	 * @param copy
	 *            the original DMC to copy
	 */
	public Dmc(Dmc copy) {
		events = new ArrayList<AbstractEvent>(copy.events);
		startDate = new Date(copy.getLeftBoundaryTimestamp().getTime());
		endDate = new Date(copy.getRightBoundaryTimestamp().getTime());
		footprint = new Footprint(copy.footprint);
		processInstance = copy.processInstance;
		id_nr = copy.id_nr;
		id_string = copy.id_string;
	}

	/**
	 * Adds a new event to this DMC
	 * 
	 * @param entry
	 */
	public void addEvent(AbstractEvent entry) {
		if (processInstance == null) { // initialize process instance
			processInstance = entry.getProcessInstance();
		}
		events.add(entry);
		footprint.add(entry.getElement());
		// adjust start and end date
		if ((startDate == null)
				|| (startDate.after(entry.getLeftBoundaryTimestamp()))) {
			startDate = new Date(entry.getLeftBoundaryTimestamp().getTime());
		}
		if ((endDate == null)
				|| (endDate.before(entry.getRightBoundaryTimestamp()))) {
			endDate = new Date(entry.getRightBoundaryTimestamp().getTime());
		}
	}

	/**
	 * @return the set of events contained within this DMC
	 */
	public ArrayList<AbstractEvent> getEvents() {
		return events;
	}

	/**
	 * @return iterator over DMCs contained
	 */
	public Iterator<AbstractEvent> iterator() {
		return events.iterator();
	}

	/**
	 * @return number of events contained
	 */
	public int size() {
		return events.size();
	}

	/**
	 * Retrieves the minimal list of modified data objects in this DMC
	 * 
	 * @return
	 */
	public Footprint footprint() {
		return footprint;
	}

	/**
	 * checks whether this DMC is in conflict with another one, i.e. they share
	 * at least one event.
	 * 
	 * @param other
	 *            DMC to test conflict with
	 * @return
	 */
	public boolean conflicts(Dmc other) {
		for (AbstractEvent event : events) {
			if (other.containsEvent(event)) {
				return true; // at least one event shared
			}
		}
		return false; // conflict free
	}

	/**
	 * Tests for similarity with another DMC. Two DMCs are considered similar if
	 * their footprints match.
	 * 
	 * @param other
	 * @return
	 */
	public boolean isSimilarTo(Dmc other) {
		return footprint.equals(other.footprint());
	}

	/**
	 * tests with equality to another DMC WARNING: As 'AuditTrailEntry' does so
	 * far not override the 'equals()' method this method does, as well, not
	 * check for real value equality. This needs, if necessary, to be fixed in
	 * AuditTrailEntry.
	 * 
	 * @param obj
	 * @return
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Dmc) {
			Dmc other = (Dmc) obj;
			if (size() != other.size()) {
				return false;
			} else {
				return events.equals(other.getEvents());
			}
		}
		return false;
	}

	/**
	 * deep check whether a given AbstractEvent is contained within this DMC's
	 * event set
	 * 
	 * @param entry
	 * @return
	 */
	public boolean containsEvent(AbstractEvent entry) {
		return events.contains(entry);
	}

	/**
	 * Converts all data object identifiers in the footprint to canonical
	 * representations of the data objects, following the rules implemented in
	 * the given equivalence relation implementation.
	 * 
	 * @param equiv
	 *            equivalence relation implementation used for conversion
	 */
	public void makeFootprintCanonical(ObjectEquivalence equiv) {
		footprint.convertToCanonical(equiv);
	}

	/**
	 * @return the position index of the leftmost event in this DMC
	 */
	public long getPosition() {
		return startDate.getTime();
	}

	/**
	 * @return the timestamp of the leftmost event in this DMC
	 */
	public Date getLeftBoundaryTimestamp() {
		return startDate;
	}

	/**
	 * @return the timestamp of the rightmost event in this DMC
	 */
	public Date getRightBoundaryTimestamp() {
		return endDate;
	}

	/**
	 * @return reference to the ProcessInstance all events in this DMC stem from
	 */
	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	/**
	 * @return the id sequence number of this DMC
	 */
	public long getIdNumber() {
		return id_nr;
	}

	/**
	 * @return the id string of this DMC
	 */
	public String getIdString() {
		return id_string;
	}

	/**
	 * @return the id string of this DMC
	 */
	public String toString() {
		return getIdString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		Dmc other = (Dmc) arg0;
		return (int) (this.getIdNumber() - other.getIdNumber());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.dmcscanning.LogItem#isAtomic()
	 */
	public boolean isAtomic() {
		return (getLeftBoundaryTimestamp().equals(getRightBoundaryTimestamp()));
	}

	/**
	 * checks the inner consistency of a DMC, i.e. whether for every data object
	 * contained in the footprint a corresponding event exists
	 * 
	 * @param equiv
	 * @return
	 */
	public boolean isConsistent(ObjectEquivalence equiv) {
		if (footprint.size() > events.size()) {
			return false;
		}
		// check thoroughly, whether for every footprint element
		// there exists a correspondig event
		for (String dataTest : footprint.getData()) {
			boolean isCovered = false;
			for (AbstractEvent evt : events) {
				if ((evt.getElement().equals(dataTest))
						|| (equiv.equivalent(evt.getElement(), dataTest))) {
					isCovered = true;
					break;
				}
			}
			if (isCovered == false) {
				return false;
			}
		}
		return true;
	}

}
