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
 * 
 */
package org.processmining.mining.dmcscanning;

import java.util.Iterator;
import java.util.HashSet;

import org.processmining.framework.log.ProcessInstance;

/**
 * Aggregate Data Modification Cluster (ADMC) Contains a set of DMCs all
 * modifying the same set of data objects (footprint), regardless of order and
 * modification type etc.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class Admc implements Comparable {

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

	/**
	 * member attributes
	 */
	protected HashSet<Dmc> dmcs = null;
	protected Footprint footprint = null;
	protected long id_nr = 0;
	protected String id_string = null;

	/**
	 * constructor
	 */
	public Admc() {
		dmcs = new HashSet<Dmc>();
		footprint = null;
		id_nr = nextId();
		id_string = "ADMC." + id_nr;
	}

	/**
	 * constructor
	 * 
	 * @param id
	 *            custom id string for this ADMC
	 */
	public Admc(String id) {
		this();
		id_string = id;
	}

	/**
	 * copy constructor
	 * 
	 * @param other
	 */
	public Admc(Admc other) {
		dmcs = new HashSet<Dmc>(other.dmcs);
		footprint = new Footprint(other.footprint);
		id_nr = other.id_nr;
		id_string = other.id_string;
	}

	/**
	 * @return DMCS contained within this ADMC
	 */
	public HashSet<Dmc> getDMCs() {
		return dmcs;
	}

	/**
	 * @return iterator of DMCs contained
	 */
	public Iterator<Dmc> iterator() {
		return dmcs.iterator();
	}

	/**
	 * @return number of DMCs contained
	 */
	public int size() {
		return dmcs.size();
	}

	/**
	 * @return this ADMCs footprint, i.e. the set of modified data objects
	 */
	public Footprint footprint() {
		return footprint;
	}

	/**
	 * Attempts to add a DMC to this aggregate set if footprints match.
	 * 
	 * @param dmc
	 *            the DMC to add
	 * @return whether DMC was inserted
	 */
	public boolean addIfMatches(Dmc aDmc) {
		if (footprint == null) {
			// initial element
			footprint = new Footprint(aDmc.footprint());
			dmcs.add(aDmc);
			return true;
		} else if (footprint.equals(aDmc.footprint())
				&& (conflicts(aDmc) == false)) {
			// TODO find a better, i.e. more generic, criterium than matching
			// footprints
			// TODO check whether the 'conflicts' relation on DMCs is really
			// correct
			dmcs.add(aDmc);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Adds a DMC to the ADMC set regardless of its content and footprint.
	 * WARNING: This method is intended for use by more sophisticated
	 * AggregationMethods. It can be used in case the ADMC as is does not
	 * naiively match the footprint of the DMC to be added. It is up to the
	 * AggregationMethod to take care of the following necessities, as these are
	 * not provided within the implementation of this method: - test for and
	 * resolve potential conflicts between DMCs - test overall compatibility
	 * with all contained DMCs and the ADMC itself - potentially adjust the DMC
	 * to be added - potentially adjust previously contained DMCs - derive the
	 * ADMC's new footprint and supply it
	 * 
	 * @param aDmc
	 *            the new DMC to be added
	 * @param aFootprint
	 *            the new footprint of the ADMC, calculated externally!
	 */
	public void addOverridingChecks(Dmc aDmc, Footprint aFootprint) {
		dmcs.add(aDmc);
		footprint = new Footprint(aFootprint);
	}

	/**
	 * Checks the set of DMCs contained within this ADMC for conflict with
	 * another DMC.
	 * 
	 * @param aDmc
	 * @return
	 */
	public boolean conflicts(Dmc aDmc) {
		for (Dmc dmc : dmcs) {
			if (dmc.conflicts(aDmc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks for a conflict between this ADMC and another ADMC. A conflict
	 * between two ADMCs is present, if any DMC from one ADMC is in conflict
	 * with any DMC from the other ADMC, thus this test is performed
	 * recursively.
	 * 
	 * @param anAdmc
	 * @return
	 */
	public boolean conflicts(Admc anAdmc) {
		for (Dmc dmc : anAdmc.dmcs) {
			if (conflicts(dmc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the id sequence number of this ADMC
	 */
	public long getIdNumber() {
		return id_nr;
	}

	/**
	 * @return the id string of this ADMC
	 */
	public String getIdString() {
		return id_string;
	}

	/**
	 * @return the id string of this ADMC
	 */
	public String toString() {
		return getIdString();
	}

	/**
	 * Retrieves all DMCs in this ADMC within a specific process instance
	 * 
	 * @param processId
	 *            id of the process to look up
	 * @param processInstanceId
	 *            id of the process instance to look up
	 * @return ArrayList containing all matching ADMCs
	 */
	public HashSet<Dmc> getAllInProcessInstance(String processId,
			String processInstanceId) {
		HashSet<Dmc> resultList = new HashSet<Dmc>();
		for (Dmc targetDmc : dmcs) {
			if (targetDmc.getProcessInstance().getProcess().equals(processId)
					&& targetDmc.getProcessInstance().getName().equals(
							processInstanceId)) {
				resultList.add(targetDmc);
			}
		}
		return resultList;
	}

	/**
	 * @param processId
	 *            id of the process to look up
	 * @param processInstanceId
	 *            id of the process instance to look up
	 * @return Iterator on all DMCs within a specific process instance
	 */
	public Iterator<Dmc> allInProcessInstanceIterator(String processId,
			String processInstanceId) {
		return getAllInProcessInstance(processId, processInstanceId).iterator();
	}

	/**
	 * @return a sorted set of all process instances (as references) the
	 *         contained DMCs stem from
	 */
	public HashSet<ProcessInstance> coveredProcessInstances() {
		HashSet<ProcessInstance> processInstances = new HashSet<ProcessInstance>();
		for (Dmc dmc : dmcs) {
			processInstances.add(dmc.getProcessInstance());
		}
		return processInstances;
	}

	/**
	 * overriding Object's equal method
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Admc) {
			Admc other = (Admc) obj;
			if (footprint.equals(other.footprint()) && (size() == other.size())) {
				for (Dmc dmc : other.getDMCs()) {
					if (dmcs.contains(dmc) == false) {
						return false;
					}
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		Admc other = (Admc) arg0;
		return (int) (((this.size() * 10000) + this.getIdNumber()) - ((other
				.size() * 10000) + other.getIdNumber()));
	}
}
