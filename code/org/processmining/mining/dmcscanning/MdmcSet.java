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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;

/**
 * MDMC, the Minimal set of DMCs This class is maintaining a set of all
 * conflict-free ADMCs.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class MdmcSet {

	protected HashSet<Admc> admcs = null;

	/**
	 * Constructor
	 */
	public MdmcSet() {
		admcs = new HashSet<Admc>();
	}

	/**
	 * Tries to add an ADMC to MDMC. This only succeeds in case all conflicting
	 * ADMCs that are already contained are smaller in size (regarding DMCs
	 * contained).
	 * 
	 * @param candidate
	 *            the ADMC that is tried to be added
	 * @return whether the ADMC was inserted into MDMC
	 */
	public boolean addADMC(Admc candidate) {
		if (admcs.contains(candidate)) {
			return false;
		}
		ArrayList<Admc> conflicting = new ArrayList<Admc>();
		// check, if candidate conflicts with any contained ADMC
		for (Admc other : admcs) {
			if (candidate.conflicts(other)) {
				if (candidate.size() < other.size()) {
					return false; // reject smaller candidates
				} else { // add to conflict set
					conflicting.add(other);
				}
			}
		}
		// candidate is larger than all conflicting, remove them
		for (Admc cnf : conflicting) {
			admcs.remove(cnf);
		}
		admcs.add(candidate);
		return true;
	}

	/**
	 * @return the number of ADMCs contained
	 */
	public int size() {
		return admcs.size();
	}

	/**
	 * @return an iterator on the MDMC set
	 */
	public Iterator<Admc> iterator() {
		return admcs.iterator();
	}

	/**
	 * @return ArrayList of all contained ADMC instances
	 */
	public Collection<Admc> getAll() {
		return admcs;
	}

	/**
	 * Retrieves all ADMCs in MDMC within a specific process instance
	 * 
	 * @param processId
	 *            id of the process to look up
	 * @param processInstanceId
	 *            id of the process instance to look up
	 * @return ArrayList containing all matching ADMCs
	 */
	public HashSet<Admc> getAllInProcessInstance(String processId,
			String processInstanceId) {
		HashSet<Admc> resultList = new HashSet<Admc>();
		for (Admc tgtAdmc : admcs) {
			for (Dmc tgtDmc : tgtAdmc.getDMCs()) {
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
	public Iterator<Admc> allInProcessInstanceIterator(String processId,
			String processInstanceId) {
		return getAllInProcessInstance(processId, processInstanceId).iterator();
	}

	/**
	 * Convenience method. Tries to add each element of a supplied set of ADMCs
	 * (in the given order) to build a consolidated minimal MDMC set.
	 * 
	 * @param admcSet
	 *            set of ADMCs to build MDMC from
	 * @param progress
	 *            the progress indicator used for status feedback
	 * @return the newly derived set of MDMC
	 */
	public static MdmcSet buildMDMC(AdmcSet admcSet, Progress progress) {
		progress.setNote("Deriving MDMC from ADMC...");
		progress.setMinMax(0, admcSet.size());
		int counter = 0;
		MdmcSet mdmc = new MdmcSet();
		for (Iterator it = admcSet.iterator(); it.hasNext();) {
			mdmc.addADMC((Admc) it.next());
			counter++;
			progress.setProgress(counter);
		}
		return mdmc;
	}

	/**
	 * provides the required test output in pseudo-xml format to Message.TEST
	 */
	public void testOutput() {
		Message.add("<MdmcSet size=\"" + this.admcs.size() + "\">",
				Message.TEST);
		long hash = 0;
		for (Admc admc : this.admcs) {
			hash += admc.size();
		}
		Message.add("\t<Hash>" + Long.toHexString(hash) + "</Hash>",
				Message.TEST);
		Message.add("</MdmcSet>", Message.TEST);
	}

}
