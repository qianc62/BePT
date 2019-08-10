/*
 * Created on Jun 7, 2005
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
package org.processmining.mining.dmcscanning.equivalence;

/**
 * Equivalence relation implementation for data logs extracted from the Case
 * Handling system 'FLOWer' (Pallas Athena)
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class FlowerEquivalence implements ObjectEquivalence {

	public String toString() {
		return "FLOWer Data Log Equivalence";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.equivalence.ObjectEquivalence#matches
	 * (java.lang.String)
	 */
	public boolean matches(String aSystemName) {
		return aSystemName.matches("(.*)FLOWer(.*)");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.dmcscanning.equivalence.ObjectEquivalence#
	 * getCanonicalName(java.lang.String)
	 */
	public String getCanonicalName(String aWfmElement) {
		String canonical = aWfmElement
				.replaceAll("_plan(\\d+)_id(\\d+)\\z", "");
		return canonical.trim();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.equivalence.ObjectEquivalence#equivalent
	 * (java.lang.String, java.lang.String)
	 */
	public boolean equivalent(String wfmElementOne, String wfmElementTwo) {
		return getCanonicalName(wfmElementOne).equals(
				getCanonicalName(wfmElementTwo));
	}

}
