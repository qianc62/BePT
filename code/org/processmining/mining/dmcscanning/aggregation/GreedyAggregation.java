/*
 * Created on Jun 7, 2005
 *
 * (c) 2005 Christian W. Guenther, all rights reserved.
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
 * @author christian
 * 
 *         Christian W. Guenther (christian@deckfour.com)
 * 
 */
public class GreedyAggregation implements AggregationMethod {

	/**
	 * 
	 */
	public GreedyAggregation() {
		super();
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.dmcscanning.aggregation.AggregationMethod#
	 * isCompliant(org.processmining.mining.dmcscanning.Admc,
	 * org.processmining.mining.dmcscanning.Dmc)
	 */
	public boolean isCompliant(Admc anAdmc, Dmc aDmc) {
		return (aDmc.footprint().isSubsetOf(anAdmc.footprint()) || anAdmc
				.footprint().isSubsetOf(aDmc.footprint()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.aggregation.AggregationMethod#compliance
	 * (org.processmining.mining.dmcscanning.Admc,
	 * org.processmining.mining.dmcscanning.Dmc)
	 */
	public double compliance(Admc anAdmc, Dmc aDmc) {
		return anAdmc.footprint().overlap(aDmc.footprint());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.aggregation.AggregationMethod#merge
	 * (org.processmining.mining.dmcscanning.Admc,
	 * org.processmining.mining.dmcscanning.Dmc)
	 */
	public Admc merge(Admc anAdmc, Dmc aDmc) {
		if (aDmc.footprint().isSubsetOf(anAdmc.footprint())) {
			// simply add and retain previous ADMC's footprint
			anAdmc.addOverridingChecks(aDmc, anAdmc.footprint());
			return anAdmc;
		} else if (anAdmc.footprint().isSubsetOf(aDmc.footprint())) {
			// add and retain new DMC's footprint
			anAdmc.addOverridingChecks(aDmc, aDmc.footprint());
			return anAdmc;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.dmcscanning.aggregation.AggregationMethod#
	 * consolidate(org.processmining.mining.dmcscanning.AdmcSet)
	 */
	public double consolidate(AdmcSet anAdmcSet) {
		// TODO not implemented
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.dmcscanning.aggregation.AggregationMethod#
	 * getDescription()
	 */
	public String getDescription() {
		return "A DMC is added to an ADMC in case one's footprint is a subset of the other's";
	}

	public String toString() {
		return "Greedy aggregation";
	}

}
