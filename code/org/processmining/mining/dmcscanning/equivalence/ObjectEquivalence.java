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
 * Interface for pluggable equivalence relation/determination between data
 * objects. The objective of derived classes is to, for a given system,
 * determine whether two workflow model element identifiers in a log file
 * correspond to actually the same data object. This serves the purpose to make
 * workflow model elements that have, e.g., the process instance id appended,
 * recognizable among multiple processes or process instances.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public interface ObjectEquivalence {

	/**
	 * @return the name of the respective equivalence relation (i.e. the system
	 *         or kind of log it is applicable for)
	 */
	public String toString();

	/**
	 * Checks for a given system name (of e.g. a WFMS software) whether this
	 * equivalence relation is applicable
	 * 
	 * @param aSystemName
	 * @return
	 */
	public boolean matches(String aSystemName);

	/**
	 * Returns for a given workflow model element name the corresponding
	 * canonical name of the object referred
	 * 
	 * @param aWfmElement
	 * @return
	 */
	public String getCanonicalName(String aWfmElement);

	/**
	 * Checks for two given workflow model element identifiers, whether they
	 * refer to actually the same (abstract) object
	 * 
	 * @param wfmElementOne
	 * @param wfmElementTwo
	 * @return
	 */
	public boolean equivalent(String wfmElementOne, String wfmElementTwo);

}
