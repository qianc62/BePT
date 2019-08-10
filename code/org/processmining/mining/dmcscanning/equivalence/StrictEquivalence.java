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
 * Implements the strict equivalence relation, i.e. string equality makes
 * equivalent
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class StrictEquivalence implements ObjectEquivalence {

	public String toString() {
		return "Strict equivalence";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.equivalence.ObjectEquivalence#matches
	 * (java.lang.String)
	 */
	public boolean matches(String aSystemName) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.dmcscanning.equivalence.ObjectEquivalence#
	 * getCanonicalName(java.lang.String)
	 */
	public String getCanonicalName(String aWfmElement) {
		return aWfmElement.trim();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.equivalence.ObjectEquivalence#equivalent
	 * (java.lang.String, java.lang.String)
	 */
	public boolean equivalent(String wfmElementOne, String wfmElementTwo) {
		return wfmElementOne.trim().equals(wfmElementTwo.trim());
	}

}
