package org.processmining.analysis.performance.componentstate.logutil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import org.processmining.framework.log.ProcessInstance;
import java.util.TreeSet;

/**
 * This class represents a Data Modification Cluster (LogUnitList). Data
 * modification clusters extracted in the initial scan pass.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class LogUnitList {

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

	protected ArrayList<AbstractLogUnit> events = null;
	// protected Date startDate = null;
	// protected Date endDate = null;
	protected Date startRelativeDate = null;
	protected Date endRelativeDate = null;
	protected long id_nr = 0;
	protected String id_string = null;

	/**
	 * creates a new LogUnitList object
	 */
	public LogUnitList() {
		events = new ArrayList<AbstractLogUnit>();
		startRelativeDate = null;
		endRelativeDate = null;
		id_nr = nextId();
		id_string = "LogUnitList." + id_nr;
	}

	/**
	 * constructor
	 * 
	 * @param id
	 *            a custom id string
	 */
	public LogUnitList(String id) {
		events = new ArrayList<AbstractLogUnit>();
		startRelativeDate = null;
		endRelativeDate = null;
		id_nr = nextId();
		id_string = id;
	}

	/**
	 * Adds a new event to this LogUnitList
	 * 
	 * @param entry
	 */
	public void addEvent(AbstractLogUnit entry) {
		events.add(entry);
	}

	/**
	 * Remove a event from this LogUnitList
	 */
	public void removeEvent(AbstractLogUnit entry) {
		events.remove(entry);
	}

	/**
	 * @return the set of events contained within this LogUnitList
	 */
	public ArrayList<AbstractLogUnit> getEvents() {
		return events;
	}

	/**
	 * @return iterator over LogUnitLists contained
	 */
	public Iterator<AbstractLogUnit> iterator() {
		return events.iterator();
	}

	/**
	 * @return number of events contained
	 */
	public int size() {
		return events.size();
	}

	/**
	 * @return number of events contained
	 */
	public int size(ArrayList<String> eventTypeToKeep, ArrayList instanceIDs) {
		int num = 0;
		for (Iterator itr = events.iterator(); itr.hasNext();) {
			AbstractLogUnit abstLogUnit = (AbstractLogUnit) itr.next();
			if (eventTypeToKeep.contains(abstLogUnit.getType())
					&& instanceIDs.contains(abstLogUnit.getProcessInstance()
							.getName()))
				num++;
		}
		return num;
	}

	/**
	 * tests with equality to another LogUnitList WARNING: As 'AuditTrailEntry'
	 * does so far not override the 'equals()' method this method does, as well,
	 * not check for real value equality. This needs, if necessary, to be fixed
	 * in AuditTrailEntry.
	 * 
	 * @param obj
	 * @return
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof LogUnitList) {
			LogUnitList other = (LogUnitList) obj;
			if (size() != other.size()) {
				return false;
			} else {
				return events.equals(other.getEvents());
			}
		}
		return false;
	}

	/**
	 * deep check whether a given AbstractLogUnit is contained within this
	 * LogUnitList's event set
	 * 
	 * @param entry
	 * @return
	 */
	public boolean containsEvent(AbstractLogUnit entry) {
		return events.contains(entry);
	}

	/**
	 * @return the timestamp of the leftmost event in this LogUnitList
	 */
	public Date getLeftBoundaryTimestamp(ArrayList<String> eventTypeToKeep,
			ArrayList instanceIDs) {
		ArrayList<AbstractLogUnit> abst = getEvents();
		Date startDate = null;

		for (Iterator itr = abst.iterator(); itr.hasNext();) {
			AbstractLogUnit abstLogUnit = (AbstractLogUnit) itr.next();
			if (startDate == null) {
				startDate = abstLogUnit.getCurrentTimeStamp();
				continue;
			}
			if (eventTypeToKeep.contains(abstLogUnit.getType())
					&& instanceIDs.contains(abstLogUnit.getProcessInstance()
							.getName())
					&& abstLogUnit.getCurrentTimeStamp().before(startDate)) {
				startDate = abstLogUnit.getCurrentTimeStamp();
			}
		}
		return startDate;
	}

	/**
	 * public Date getLeftBoundaryTimestampRelativeTime(ArrayList<String>
	 * eventTypeToKeep, ArrayList instanceIDs) { ArrayList<AbstractLogUnit> abst
	 * = getEvents(); Date startDate = new Date(Long.MAX_VALUE); boolean flag =
	 * false;
	 * 
	 * for (Iterator itr = abst.iterator(); itr.hasNext(); ) { AbstractLogUnit
	 * abstLogUnit = (AbstractLogUnit) itr.next(); if
	 * (eventTypeToKeep.contains(abstLogUnit.getType()) &&
	 * instanceIDs.contains(abstLogUnit.getProcessInstance().getName()) &&
	 * abstLogUnit.getRelativeTimestamp().before(startDate)) { startDate =
	 * abstLogUnit.getRelativeTimestamp(); flag = true; } } return
	 * (flag)?startDate:null; }
	 * 
	 * public Date getLeftBoundaryTimestampRelativeRatio(ArrayList<String>
	 * eventTypeToKeep, ArrayList instanceIDs) { ArrayList<AbstractLogUnit> abst
	 * = getEvents(); Date startDate = new Date(Long.MAX_VALUE); boolean flag =
	 * false;
	 * 
	 * for (Iterator itr = abst.iterator(); itr.hasNext(); ) { AbstractLogUnit
	 * abstLogUnit = (AbstractLogUnit) itr.next(); if
	 * (eventTypeToKeep.contains(abstLogUnit.getType()) &&
	 * instanceIDs.contains(abstLogUnit.getProcessInstance().getName()) &&
	 * abstLogUnit.getRelativeRatio().before(startDate)) { startDate =
	 * abstLogUnit.getRelativeRatio(); flag = true; } } return
	 * (flag)?startDate:null; }
	 * 
	 * public Date getRightBoundaryTimestampRelativeRatio(ArrayList<String>
	 * eventTypeToKeep, ArrayList instanceIDs) { ArrayList<AbstractLogUnit> abst
	 * = getEvents(); Date endDate = new Date(0); boolean flag = false;
	 * 
	 * for (Iterator itr = abst.iterator(); itr.hasNext(); ) { AbstractLogUnit
	 * abstLogUnit = (AbstractLogUnit) itr.next(); if
	 * (eventTypeToKeep.contains(abstLogUnit.getType()) &&
	 * instanceIDs.contains(abstLogUnit.getProcessInstance().getName()) &&
	 * abstLogUnit.getRelativeRatio().after(endDate)) { endDate =
	 * abstLogUnit.getRelativeRatio(); flag = true; } } return (flag) ? endDate
	 * : null; }
	 * 
	 * public Date getRightBoundaryTimestampRelativeTime(ArrayList<String>
	 * eventTypeToKeep, ArrayList instanceIDs) { ArrayList<AbstractLogUnit> abst
	 * = getEvents(); Date endDate = new Date(0); boolean flag = false;
	 * 
	 * for (Iterator itr = abst.iterator(); itr.hasNext(); ) { AbstractLogUnit
	 * abstLogUnit = (AbstractLogUnit) itr.next(); if
	 * (eventTypeToKeep.contains(abstLogUnit.getType()) &&
	 * instanceIDs.contains(abstLogUnit.getProcessInstance().getName()) &&
	 * abstLogUnit.getRelativeTimestamp().after(endDate)) { endDate =
	 * abstLogUnit.getRelativeTimestamp(); flag = true; } } return (flag) ?
	 * endDate : null;
	 * 
	 * }
	 */
	/**
	 * @return the timestamp of the rightmost event in this LogUnitList
	 */
	public Date getRightBoundaryTimestamp(ArrayList<String> eventTypeToKeep,
			ArrayList instanceIDs) {
		ArrayList<AbstractLogUnit> abst = getEvents();
		Date endDate = null;

		for (Iterator itr = abst.iterator(); itr.hasNext();) {
			AbstractLogUnit abstLogUnit = (AbstractLogUnit) itr.next();
			if (endDate == null) {
				endDate = abstLogUnit.getCurrentTimeStamp();
				// endDate = abstLogUnit.getRelativeTimestamp();
				// System.out.println("relative ==========================  "+abstLogUnit.getRelativeTimestamp());
				continue;
			}
			if (eventTypeToKeep.contains(abstLogUnit.getType())
					&& instanceIDs.contains(abstLogUnit.getProcessInstance()
							.getName())
					&& abstLogUnit.getCurrentTimeStamp().after(endDate)) {
				endDate = abstLogUnit.getCurrentTimeStamp();

				// System.out.println("enddate right boundary ==========================  "+endDate);
				// System.out.println("relative right boundary ==========================  "+abstLogUnit.getRelativeTimestamp());
				// >>>>>>> .r1816
			}
		}
		return endDate;

	}

	public void resetPositionOfItems() {
		for (int i = 0; i < events.size(); i++)
			((AbstractLogUnit) events.get(i)).resetPosition();
	}

	public void resetRelativePositionOfItems() {
		for (int i = 0; i < events.size(); i++)
			((AbstractLogUnit) events.get(i)).resetRelativePosition();
	}

	/**
	 * @return the id sequence number of this LogUnitList
	 */
	public long getIdNumber() {
		return id_nr;
	}

	/**
	 * @return the id string of this LogUnitList
	 */
	public String getIdString() {
		return id_string;
	}

	/**
	 * @return the id string of this LogUnitList
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
		LogUnitList other = (LogUnitList) arg0;
		return (int) (this.getIdNumber() - other.getIdNumber());
	}
}
