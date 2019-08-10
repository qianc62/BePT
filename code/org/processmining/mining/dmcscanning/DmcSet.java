/*
 * Created on May 20, 2005
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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;
import org.processmining.mining.dmcscanning.equivalence.ObjectEquivalence;
import org.processmining.mining.dmcscanning.logutils.LogItem;

/**
 * DMCSet provides an ordered and structured way to store and access a set of
 * DMCs. Typical usage is intended to cover the management of initially scanned
 * DMCs in a way that provides for automatically created structure, to ease
 * later access and search.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class DmcSet implements LogItem {

	/**
	 * the arraylist allDMCs is to contain references to all DMCs contained in
	 * the order of their having been added. The hashmap processes contains a
	 * set of hashmaps that are keyed by the present process ids/names. The
	 * value hashmaps inside contain as key the process instance ids and as
	 * value arraylists that contain references to the dmcs, belonging to this
	 * process instance. Such, the structure is: process->processInstance->DMCs
	 */
	protected TreeSet<Dmc> allDMCs = null;
	protected HashMap<String, HashMap> processes = null;

	protected Date leftBoundaryTime = null;
	protected Date rightBoundaryTime = null;

	protected long dmcCounter = 0;

	/**
	 * constructor
	 */
	public DmcSet() {
		allDMCs = new TreeSet<Dmc>();
		processes = new HashMap<String, HashMap>();
		dmcCounter = 0;
	}

	/**
	 * inserts a new DMC into the data structure (which is built transparently)
	 * 
	 * @param aDmc
	 */
	public void insert(Dmc aDmc) {
		allDMCs.add(aDmc);
		HashMap processInstanceMap = null;
		// retrieve map referring to process instances first, create and insert
		// if not present
		if (processes.containsKey(aDmc.getProcessInstance().getProcess())) {
			processInstanceMap = (HashMap) processes.get(aDmc
					.getProcessInstance().getProcess());
		} else {
			processInstanceMap = new HashMap();
			processes.put(aDmc.getProcessInstance().getProcess(),
					processInstanceMap);
		}
		// retrieve arraylist of DMCs, create and insert if not present
		TreeSet dmcList = null;
		if (processInstanceMap.containsKey(aDmc.getProcessInstance().getName())) {
			dmcList = (TreeSet) processInstanceMap.get(aDmc
					.getProcessInstance().getName());
		} else {
			dmcList = new TreeSet();
			processInstanceMap
					.put(aDmc.getProcessInstance().getName(), dmcList);
		}
		// insert new DMC to appropriate list
		dmcList.add(aDmc);
		// adjust time boundaries
		if ((leftBoundaryTime == null)
				|| aDmc.getLeftBoundaryTimestamp().before(leftBoundaryTime)) {
			leftBoundaryTime = aDmc.getLeftBoundaryTimestamp();
		}
		if ((rightBoundaryTime == null)
				|| aDmc.getRightBoundaryTimestamp().after(rightBoundaryTime)) {
			rightBoundaryTime = aDmc.getRightBoundaryTimestamp();
		}
		dmcCounter++;
	}

	/**
	 * @return an ArrayList with all DMCs contained
	 */
	public SortedSet getAll() {
		return allDMCs;
	}

	/**
	 * @return Iterator on all DMCs contained
	 */
	public Iterator iterator() {
		return allDMCs.iterator();
	}

	/**
	 * Retrieves a collection of all DMCs stored that belong to the specified
	 * process
	 * 
	 * @param processName
	 * @return
	 */
	public SortedSet getAllInProcess(String processName) {
		TreeSet result = new TreeSet();
		for (Iterator it = ((HashMap) processes.get(processName)).values()
				.iterator(); it.hasNext();) {
			result.addAll((SortedSet) it.next());
		}
		return result;
	}

	/**
	 * Convenience method, directly returning an iterator on a collection of all
	 * DMCs that belong to the specified process
	 * 
	 * @param processName
	 * @return
	 */
	public Iterator allInProcessIterator(String processName) {
		return getAllInProcess(processName).iterator();
	}

	/**
	 * Returns a list of all DMCs in a given process instance
	 * 
	 * @param processName
	 *            name of the process
	 * @param processInstanceName
	 *            id of process instance
	 * @return ArrayList of all specified DMCS
	 */
	public SortedSet getDMCs(String processName, String processInstanceName) {
		return (TreeSet) ((HashMap) processes.get(processName))
				.get(processInstanceName);
	}

	/**
	 * Convenience method, directly delivering an iterator on all specified DMCs
	 * 
	 * @param processName
	 *            name of the process
	 * @param processInstanceName
	 *            id of the process instance
	 * @return Iterator on all specified DMCs
	 */
	public Iterator getDMCsIterator(String processName,
			String processInstanceName) {
		return getDMCs(processName, processInstanceName).iterator();
	}

	/**
	 * Returns a list of all DMCs in a given process instance
	 * 
	 * @param processInstance
	 *            reference to a ProcessInstance
	 * @return ArrayList of all specified DMCS
	 */
	public SortedSet getDMCs(ProcessInstance processInstance) {
		return getDMCs(processInstance.getProcess(), processInstance.getName());
	}

	/**
	 * Convenience method, directly delivering an iterator on all specified DMCs
	 * 
	 * @param processInstance
	 *            reference to a ProcessInstance
	 * @return Iterator on all specified DMCs
	 */
	public Iterator getDMCsIterator(ProcessInstance processInstance) {
		return getDMCs(processInstance).iterator();
	}

	/**
	 * @return Iterator on the set of all process IDs/names contained
	 */
	public Iterator processesIterator() {
		return processes.keySet().iterator();
	}

	/**
	 * @param processName
	 *            ID/name of respective process
	 * @return iterator on the set of all process instance IDs contained for
	 *         that process
	 */
	public Iterator processInstancesIterator(String processName) {
		return ((HashMap) processes.get(processName)).keySet().iterator();
	}

	/**
	 * @return the total number of contained DMCs
	 */
	public int size() {
		return allDMCs.size();
	}

	/**
	 * @return names/ids of all process instances represented by any DMC within
	 *         the set
	 */
	public SortedSet getProcessInstances() {
		TreeSet result = new TreeSet();
		for (Iterator it = processes.values().iterator(); it.hasNext();) {
			HashMap instances = (HashMap) it.next();
			for (Iterator it2 = instances.keySet().iterator(); it2.hasNext();) {
				result.add(it2.next());
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.LogItem#getLeftBoundaryPosition()
	 */
	public long getPosition() {
		return ((Dmc) allDMCs.first()).getPosition();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.LogItem#getLeftBoundaryTimestamp()
	 */
	public Date getLeftBoundaryTimestamp() {
		return leftBoundaryTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.LogItem#getRightBoundaryTimestamp()
	 */
	public Date getRightBoundaryTimestamp() {
		return rightBoundaryTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		DmcSet other = (DmcSet) arg0;
		return (int) (this.getPosition() - other.getPosition());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.dmcscanning.LogItem#isAtomic()
	 */
	public boolean isAtomic() {
		return false;
	}

	/**
	 * Debug convenience method: check consistency of all contained DMCs.
	 * Returns the consistency ratio: 1.0==completely consistent, 0.0==forget
	 * it!
	 * 
	 * @param equiv
	 * @return
	 */
	public double checkConsistency(ObjectEquivalence equiv) {
		int faults = 0;
		Dmc test = null;
		for (Iterator it = this.iterator(); it.hasNext();) {
			test = (Dmc) it.next();
			if (!test.isConsistent(equiv)) {
				faults++;
				System.out.println("DMC #" + test.getIdNumber()
						+ " found inconsistent!");
			}
		}
		return ((double) faults / (double) this.size());
	}

	/**
	 * provides the required test output in pseudo-xml format to Message.TEST
	 */
	public void testOutput() {
		Message.add("<DmcSet size=\"" + this.allDMCs.size() + "\">",
				Message.TEST);
		long hash = 0;
		for (Dmc dmc : this.allDMCs) {
			hash += dmc.size();
		}
		Message.add("\t<Hash>" + Long.toHexString(hash) + "</Hash>",
				Message.TEST);
		Message.add("</DmcSet>", Message.TEST);
	}

}
