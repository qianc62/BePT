/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.mining.geneticmining.analysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.mining.geneticmining.fitness.Fitness;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class Performance {

	LoadPopulation[] populations = null;
	private double MBF = Double.NaN; // Mean Best Fitness
	private double BFE = Double.NaN; // Best Fitness Ever
	private double WFE = Double.NaN; // Worst Fitness Ever
	private double numRunsWFE = 0;
	private double numRunsBFE = 0;
	private StringBuffer bestIndividuals = null;

	public Performance(Fitness fit, HashSet pop) {

		Iterator it = null;
		HeuristicsNet[] auxPop = null;
		double numRuns = pop.size();

		it = pop.iterator();
		populations = new LoadPopulation[pop.size()];

		for (int i = 0; i < populations.length; i++) {
			try {
				populations[i] = (LoadPopulation) it.next();

				// copying populations
				populations[i].setPopulation(fit.calculate(populations[i]
						.getPopulation()));
				auxPop = new HeuristicsNet[populations[i].getPopulation().length];
				for (int j = 0; j < auxPop.length; j++) {
					auxPop[j] = populations[i].getPopulation()[j].copyNet();
				}
				Arrays.sort(auxPop);

				// System.out.println(" pop[n-1] = " +
				// populations[i][populations[i].length - 1].getFitness());

				if (Double.isNaN(MBF)) {
					MBF = auxPop[auxPop.length - 1].getFitness();
				} else {
					MBF += auxPop[auxPop.length - 1].getFitness();
				}

				if (Double.isNaN(BFE)
						|| BFE < auxPop[auxPop.length - 1].getFitness()) {
					BFE = auxPop[auxPop.length - 1].getFitness();
					numRunsBFE = 1;
				} else if (BFE == auxPop[auxPop.length - 1].getFitness()) {
					numRunsBFE++;
				}

				if (Double.isNaN(WFE)
						|| WFE > auxPop[auxPop.length - 1].getFitness()) {
					WFE = auxPop[auxPop.length - 1].getFitness();
					numRunsWFE = 1;
				} else if (WFE == auxPop[auxPop.length - 1].getFitness()) {
					numRunsWFE++;
				}

			} catch (NullPointerException npe) {
				numRuns--;
				System.err.println("Individual equals NULL!");
				npe.printStackTrace();
			}

		}

		if (!Double.isNaN(MBF)) {
			MBF /= numRuns;
		}

		// identify best individuals
		this.bestIndividuals = identifyIndividuals(BFE);

	}

	private StringBuffer identifyIndividuals(double fitness) {
		HeuristicsNet[] currentPopulation = null;
		StringBuffer sb = new StringBuffer();
		if (!Double.isNaN(fitness)) {
			for (int i = 0; i < populations.length; i++) {
				currentPopulation = populations[i].getPopulation();
				try {
					for (int j = 0; j < currentPopulation.length; j++) {
						if (currentPopulation[j].getFitness() == fitness) {
							sb.append(populations[i].getIndividualsPath()[j])
									.append(";");
						}
					}
				} catch (NullPointerException npe) {
					// just proceed...
				}

			}
		}

		return sb;

	}

	public double getNumRunsBFE() {
		return numRunsBFE;
	}

	public double getNumRunsWFE() {
		return numRunsWFE;
	}

	public double getMBF() {
		return MBF;
	}

	public double getBFE() {
		return BFE;
	}

	public double getWFE() {
		return WFE;
	}

	public StringBuffer getBestIndividuals() {
		return bestIndividuals;
	}

}
