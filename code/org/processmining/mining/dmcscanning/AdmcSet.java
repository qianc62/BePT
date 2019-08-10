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
 * 
 */
package org.processmining.mining.dmcscanning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.mining.dmcscanning.aggregation.AggregationMethod;

/**
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class AdmcSet {

	protected HashMap<String, Admc> admcs = null;
	protected ArrayList<Admc> consolidationVictims = null;
	protected AggregationMethod method = null;

	/**
	 * constructor
	 * 
	 * @param aMethod
	 *            the AggregationMethod instance to be used for DMC->ADMC
	 *            aggregation (comparison and merging)
	 */
	public AdmcSet(AggregationMethod aMethod) {
		admcs = new HashMap<String, Admc>();
		consolidationVictims = null;
		method = aMethod;
	}

	/**
	 * Attempts to merge a DMC with all included ADMCs. If none succeeds, a new
	 * ADMC is being created and inserted, based on the provided DMC.
	 * 
	 * @param aDmc
	 *            the DMC to add to ADMC (set).
	 * @return the number of ADMCs the DMC was merged with. Zero, if a new ADMC
	 *         was created.
	 */
	public int add(Dmc aDmc) {
		int addNr = 0;
		Admc theAdmc = null;
		// attempt to merge with present ADMCs
		for (String theKey : admcs.keySet()) {
			theAdmc = admcs.get(theKey);
			if (method.isCompliant(theAdmc, aDmc)) {
				Admc merged = method.merge(theAdmc, aDmc);
				if (merged != null) { // play it safe
					admcs.put(theKey, merged);
					addNr++;
				}
			}
		}
		if (addNr == 0) { // create and insert new ADMC
			theAdmc = new Admc();
			theAdmc.addIfMatches(aDmc);
			admcs.put(theAdmc.getIdString(), theAdmc);
		}
		return addNr;
	}

	/**
	 * Retrieve an ADMC by its string id
	 * 
	 * @param key
	 *            id string of ADMC to retrieve
	 * @return
	 */
	public Admc get(String key) {
		return (Admc) admcs.get(key);
	}

	/**
	 * Removes the specified ADMC from the set by id key
	 * 
	 * @param key
	 *            id string of ADMC to remove
	 * @return the removed ADMC, null if not contained
	 */
	public Admc remove(String key) {
		Admc removed = admcs.remove(key);
		if (consolidationVictims != null) {
			consolidationVictims.remove(removed);
		}
		return removed;
	}

	/**
	 * Retrieves an ADMC according to its footprint
	 * 
	 * @param footprint
	 *            the footprint to match
	 * @return the first ADMC within the set matching the specified footprint
	 */
	public Admc get(Footprint footprint) {
		for (Admc theAdmc : admcs.values()) {
			if (footprint.equals(theAdmc.footprint())) {
				return theAdmc;
			}
		}
		return null;
	}

	/**
	 * @return collection interface to all ADMCs contained
	 */
	public Collection<Admc> getAll() {
		return admcs.values();
	}

	public Collection<Admc> getAllCopy() {
		return new HashSet<Admc>(admcs.values());
	}

	/**
	 * @return sorted set of all ADMCs contained
	 */
	public SortedSet<Admc> getAllSorted() {
		TreeSet<Admc> sorted = new TreeSet<Admc>();
		sorted.addAll(admcs.values());
		return sorted;
	}

	/**
	 * @return iterator on the set of contained ADMCs
	 */
	public Iterator iterator() {
		if (consolidationVictims == null) {
			return admcs.values().iterator();
		} else {
			HashSet<Admc> consolidatedSet = new HashSet<Admc>();
			for (Admc toTest : admcs.values()) {
				if (consolidationVictims.contains(toTest) == false) {
					consolidatedSet.add(toTest);
				}
			}
			return consolidatedSet.iterator();
		}
	}

	/**
	 * @return the number of contained ADMCs
	 */
	public int size() {
		if (consolidationVictims == null) {
			return admcs.size();
		} else {
			return (admcs.size() - consolidationVictims.size());
		}
	}

	/**
	 * consolidates the ADMC set. The implementation of this method is up to the
	 * specific aggregation method, the rationale behind it is to strip down a
	 * set of ADMCs to the most fundamental elements. As such, the method is
	 * expected to merge similar ADMCs together or to delete less significant
	 * elements, according to its intent and implementation.
	 * 
	 * @return value within [0,1]; 0=all elements removed; 1=nothing removed
	 */
	public double consolidate() {
		return method.consolidate(this);
	}

	/**
	 * Makes the specified ADMC a victim of consolidation. This results in the
	 * specific ADMC no longer being part of the 'active' (or, consolidated) set
	 * of ADMCs within the ADMC set. However, it is being preserved in a list of
	 * consolidation victims that can be accessed further on.
	 * 
	 * @param anAdmcId
	 *            id string of the ADMC to victimize.
	 */
	public void makeConsolidationVictim(String anAdmcId) {
		Admc victim = (Admc) admcs.remove(anAdmcId);
		if (consolidationVictims == null) {
			consolidationVictims = new ArrayList<Admc>();
		}
		consolidationVictims.add(victim);
	}

	/**
	 * @return whether this set of ADMCs has already been consolidated
	 */
	public boolean isConsolidated() {
		return (consolidationVictims != null);
	}

	/**
	 * @return the number of ADMCs that have been victim to consolidation
	 */
	public int consolidationVictimsSize() {
		if (consolidationVictims != null) {
			return consolidationVictims.size();
		} else {
			return 0;
		}
	}

	/**
	 * @return iterator on the set of victims of consolidation.
	 */
	public Iterator consolidationVictimsIterator() {
		return consolidationVictims.iterator();
	}

	/**
	 * @return the set of consolidation victims (may be null, if not yet
	 *         consolidated)
	 */
	public ArrayList consolidationVictims() {
		return consolidationVictims;
	}

	/**
	 * @return an iterator on all ADMCs, both actively contained and
	 *         consolidation victims
	 */
	public Iterator allAdmcIterator() {
		if (consolidationVictims == null) {
			return iterator();
		} else {
			ArrayList<Admc> allMembers = new ArrayList<Admc>();
			allMembers.addAll(admcs.values());
			allMembers.addAll(consolidationVictims);
			return allMembers.iterator();
		}
	}

	/**
	 * Retrieves all ADMCs within a specific process instance
	 * 
	 * @param processId
	 *            id of the process to look up
	 * @param processInstanceId
	 *            id of the process instance to look up
	 * @return ArrayList containing all matching ADMCs
	 */
	public ArrayList getAllInProcessInstance(String processId,
			String processInstanceId) {
		ArrayList<Admc> resultList = new ArrayList<Admc>();
		Admc tgtAdmc = null;
		Dmc tgtDmc = null;
		for (Iterator itAdmc = iterator(); itAdmc.hasNext();) {
			tgtAdmc = (Admc) itAdmc.next();
			for (Iterator itDmc = tgtAdmc.iterator(); itDmc.hasNext();) {
				tgtDmc = (Dmc) itDmc.next();
				if (tgtDmc.getProcessInstance().getProcess().equals(processId)
						&& tgtDmc.getProcessInstance().getName().equals(
								processInstanceId)) {
					resultList.add(tgtAdmc);
				}
			}
		}
		return resultList;
	}

	/**
	 * @param processId
	 *            id of the process to look up
	 * @param processInstanceId
	 *            id of the process instance to look up
	 * @return Iterator on all ADMCs within a specific process instance
	 */
	public Iterator allInProcessInstanceIterator(String processId,
			String processInstanceId) {
		return getAllInProcessInstance(processId, processInstanceId).iterator();
	}

	/**
	 * provides the required test output in pseudo-xml format to Message.TEST
	 */
	public void testOutput() {
		Message.add("<AdmcSet size=\"" + this.admcs.size() + "\">",
				Message.TEST);
		long hash = 0;
		for (Admc admc : admcs.values()) {
			hash += admc.size();
		}
		Message.add("\t<Hash>" + Long.toHexString(hash) + "</Hash>",
				Message.TEST);
		Message.add("</AdmcSet>", Message.TEST);
	}

	/**
	 * Convenience method. Builds, from a DMCSet of initially scanned DMCs
	 * together with an AggregationMethod, the set of ADMCs.
	 * 
	 * @param aDmcSet
	 *            set of initially scanned DMCs
	 * @param aMethod
	 *            method to be used for ADMC aggregation
	 * @param progress
	 *            the progress indicator used for status feedback
	 * @return the set of ADMCs, according to the supplied method
	 */
	public static AdmcSet buildAdmcSet(DmcSet aDmcSet,
			AggregationMethod aMethod, Progress progress) {
		progress.setNote("Deriving aggregated set of clusters...");
		int counter = 0;
		progress.setMinMax(0, aDmcSet.size());
		progress.setProgress(0);
		AdmcSet resultSet = new AdmcSet(aMethod);
		for (Iterator it = aDmcSet.iterator(); it.hasNext();) {
			resultSet.add((Dmc) it.next());
			counter++;
			progress.setProgress(counter);
		}
		return resultSet;
	}
}
