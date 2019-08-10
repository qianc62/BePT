/*
 * Created on Jun 10, 2005
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
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class TolerantAggregation implements AggregationMethod {

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.dmcscanning.aggregation.AggregationMethod#
	 * isCompliant(org.processmining.mining.dmcscanning.Admc,
	 * org.processmining.mining.dmcscanning.Dmc)
	 */
	public boolean isCompliant(Admc anAdmc, Dmc aDmc) {
		return anAdmc.footprint().equals(aDmc.footprint());
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
		if (anAdmc.footprint().equals(aDmc.footprint())) {
			int conflicts = 0;
			for (Dmc test : anAdmc.getDMCs()) {
				if (test.conflicts(aDmc)) {
					conflicts++;
				}
			}
			return ((double) (anAdmc.size() - conflicts) / (double) anAdmc
					.size());
		} else {
			return 0.0;
		}
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
		if (isCompliant(anAdmc, aDmc)) {
			anAdmc.addOverridingChecks(aDmc, anAdmc.footprint());
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
		int sizeBefore = anAdmcSet.size();
		for (Admc refAdmc : anAdmcSet.getAllCopy()) {
			if ((anAdmcSet.consolidationVictims() != null)
					&& (anAdmcSet.consolidationVictims().contains(refAdmc))) {
				continue; // avoid checking already victimized elements
			}
			for (Admc testAdmc : anAdmcSet.getAllCopy()) {
				if ((anAdmcSet.consolidationVictims() != null)
						&& (anAdmcSet.consolidationVictims().contains(testAdmc))) {
					continue; // avoid consolidating previous victims
				} else if ((testAdmc != refAdmc)
						&& (testAdmc.footprint().equals(refAdmc.footprint()))) {
					// merge non-identical ADMCs with same footprint
					for (Dmc toBeAdded : testAdmc.getDMCs()) {
						if (refAdmc.getDMCs().contains(toBeAdded) == false) {
							refAdmc.addOverridingChecks(toBeAdded, refAdmc
									.footprint());
						}
					}
					// make test ADMC consolidation victim
					anAdmcSet.makeConsolidationVictim(testAdmc.getIdString());
				}
			}
		}
		return ((double) anAdmcSet.size() / (double) sizeBefore);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.dmcscanning.aggregation.AggregationMethod#
	 * getDescription()
	 */
	public String getDescription() {
		return "Ignores conflicts between DMCs within an ADMC element; Consolidates ADMC accordingly.";
	}

	public String toString() {
		return "Tolerant aggregation";
	}

}
