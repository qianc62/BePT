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
 * Simple merging method to start from. A DMC is merged with an ADMC if, and
 * only if, their footprints match exactly, i.e. the same set of data objects is
 * being modified (regardless of order) in all DMCs of an ADMC.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class SimpleAggregation implements AggregationMethod {

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.dmcscanning.aggregation.AggregationMethod#
	 * isCompliant(org.processmining.mining.dmcscanning.ADMC,
	 * org.processmining.mining.dmcscanning.DMC)
	 */
	public boolean isCompliant(Admc anAdmc, Dmc aDmc) {
		return anAdmc.footprint().equals(aDmc.footprint());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.aggregation.AggregationMethod#compliance
	 * (org.processmining.mining.dmcscanning.ADMC,
	 * org.processmining.mining.dmcscanning.DMC)
	 */
	public double compliance(Admc anAdmc, Dmc aDmc) {
		return isCompliant(anAdmc, aDmc) ? 1.0 : 0.0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.aggregation.AggregationMethod#merge
	 * (org.processmining.mining.dmcscanning.ADMC,
	 * org.processmining.mining.dmcscanning.DMC)
	 */
	public Admc merge(Admc anAdmc, Dmc aDmc) {
		if (isCompliant(anAdmc, aDmc) && (anAdmc.conflicts(aDmc) == false)) {
			anAdmc.addOverridingChecks(aDmc, anAdmc.footprint());
			return anAdmc;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.aggregation.AggregationMethod#getName
	 * ()
	 */
	public String toString() {
		return "Simple merge";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.dmcscanning.aggregation.AggregationMethod#
	 * getDescription()
	 */
	public String getDescription() {
		return "A DMC is merged into an ADMC iff their footprints match exactly.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.dmcscanning.aggregation.AggregationMethod#
	 * consolidate(org.processmining.mining.dmcscanning.ADMCSet)
	 */
	public double consolidate(AdmcSet anAdmcSet) {
		// this simple implementation does not support consolidation
		return 0;
	}

}
