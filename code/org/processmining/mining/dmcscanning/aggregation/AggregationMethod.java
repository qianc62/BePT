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
package org.processmining.mining.dmcscanning.aggregation;

import org.processmining.mining.dmcscanning.Admc;
import org.processmining.mining.dmcscanning.AdmcSet;
import org.processmining.mining.dmcscanning.Dmc;

/**
 * This interface is used by the DMC Scanner to aggregate initially scanned DMCs
 * into more high-level clusters, i.e. ADMCs. Deriving methods to achieve this
 * from this common interface makes the whole procedure
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public interface AggregationMethod {

	/**
	 * Checks for compliance of a DMC with an ADMC. Compliance in this context
	 * means, that the DMC can positively be merged with the ADMC.
	 * 
	 * @param anAdmc
	 *            the ADMC to test compliance with
	 * @param aDmc
	 *            the DMC to check compliance
	 * @return whether the parameters are compliant
	 */
	public boolean isCompliant(Admc anAdmc, Dmc aDmc);

	/**
	 * In case 'isCompliant()' returns false, this method can be used to check
	 * for to which degree the two parameters are compliant. More fuzzy merging
	 * algorithms could use this to merge not completely compliant ADMCs/DMCs
	 * anyway.
	 * 
	 * @param anAdmc
	 *            the ADMC to test compliance with
	 * @param aDmc
	 *            the DMC to check compliance
	 * @return to which degree parameters are compliant (within [0,1], 1=fully
	 *         compliant)
	 */
	public double compliance(Admc anAdmc, Dmc aDmc);

	/**
	 * Merges the provided DMC with the provided ADMC. Notice: This method is
	 * intended to modify the original ADMC, i.e. the returned reference is to
	 * equal the supplied ADMC parameter!
	 * 
	 * @param anAdmc
	 *            the ADMC to merge with
	 * @param aDmc
	 *            the DMC to merge into
	 * @return the result of merging, i.e. a new ADMC.
	 */
	public Admc merge(Admc anAdmc, Dmc aDmc);

	/**
	 * Consolidates a set of ADMCs. The implementation of this method is up to
	 * the specific aggregation method, the rationale behind it is to strip down
	 * a set of ADMCs to the most fundamental elements. As such, the method is
	 * expected to merge similar ADMCs together or to delete less significant
	 * elements, according to its intent and implementation.
	 * 
	 * @param anAdmcSet
	 *            the set of ADMCs to be consolidated
	 * @return value within [0,1]; 0=all elements removed; 1=nothing removed
	 */
	public double consolidate(AdmcSet anAdmcSet);

	/**
	 * @return the name of this aggregation method
	 */
	public String toString();

	/**
	 * @return a short description of how merging and checking compliance is
	 *         performed (1-3 sentences)
	 */
	public String getDescription();

}
