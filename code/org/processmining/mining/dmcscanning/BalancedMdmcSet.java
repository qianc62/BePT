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
package org.processmining.mining.dmcscanning;

import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;

/**
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class BalancedMdmcSet extends MdmcSet {

	protected double balance = 0.0;
	protected ArrayList<Admc> conflicting = null;

	public BalancedMdmcSet(double ratio) {
		super();
		balance = ratio;
		conflicting = new ArrayList<Admc>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.MdmcSet#addADMC(org.processmining
	 * .mining.dmcscanning.Admc)
	 */
	public boolean addADMC(Admc candidate) {
		conflicting.clear(); // reuse conflicting list
		// check, if candidate conflicts with any contained ADMC
		for (Admc other : admcs) {
			if (candidate.conflicts(other)) {
				// make a balanced decision
				if (chooseCandidate(candidate, other) == other) {
					return false;
				} else {
					conflicting.add(other);
				}
			}
		}
		// candidate is larger than all conflicting, remove them
		for (Iterator cnf = conflicting.iterator(); cnf.hasNext();) {
			admcs.remove(cnf.next());
		}
		admcs.add(candidate);
		return true;
	}

	/**
	 * create balanced value for decision
	 * 
	 * @param candidate
	 * @return
	 */
	protected double getDecisionValue(Admc candidate) {
		return ((candidate.size() * balance) + (candidate.footprint().size() * (1.0 - balance)));
	}

	/**
	 * This method should always choose for the same candidate of the two
	 * provided and thus prevent 'flattering' of choices in multiple iterations.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	protected Admc chooseCandidate(Admc a, Admc b) {
		double vA = getDecisionValue(a);
		double vB = getDecisionValue(b);
		// if the balanced decision is clear, choose fitter ADMC
		if (vA > vB) {
			return a;
		} else if (vB > vA) {
			return b;
		} else {
			// otherwise let the higher balanced value decide
			int decision = 0;
			if (balance > 0.5) {
				decision = a.footprint().size() - b.footprint().size();
			} else {
				decision = a.size() - b.size();
			}
			// see if we now got a clear decision
			if (decision > 0) {
				return a;
			} else if (decision < 0) {
				return b;
			} else {
				// still no clear decision - resort to order!
				if (a.compareTo(b) > 0) {
					return a;
				} else {
					return b;
				}
			}
		}
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
	public static MdmcSet buildMDMC(AdmcSet admcSet, double balance,
			int iterations, Progress progress) {
		if (iterations <= 0) {
			iterations = 1;
		}
		progress
				.setNote("Deriving minimal conflict-free set from aggregated set...");
		progress.setMinMax(0, (admcSet.size() * iterations));
		Message
				.add("Balanced iterated derivation of minimal conflict-free set..");
		int counter = 0;
		int turnover = 0;
		BalancedMdmcSet mdmc = new BalancedMdmcSet(balance);
		for (int iteration = 1; iteration <= iterations; iteration++) {
			progress
					.setNote("Deriving minimal conflict-free set from aggregated set: Iteration #"
							+ iteration);
			turnover = 0;
			for (Admc candidate : admcSet.getAll()) {
				if (mdmc.admcs.contains(candidate) == false) {
					if (mdmc.addADMC(candidate) == true) {
						turnover++;
						/*
						 * debug output if(turnover % 50 == 0) {
						 * System.out.println("MDMC: added " + turnover +
						 * " ADMCs."); }
						 */
					}
				}
				counter++;
				progress.setProgress(counter);
			}
			int turnoverPct = (int) (((double) turnover / (double) mdmc.size()) * 100.0);
			Message.add("Iteration #" + iteration + " completed. Turnover: "
					+ turnover + " (" + turnoverPct + "%)");
		}
		return mdmc;
	}
}
