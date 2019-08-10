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

package org.processmining.mining.geneticmining;

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

public class GeneticMinerConstants {

	public static final String OR = "|";
	public static final String AND = "&";

	public static final int INITIAL_POPULATION_TYPE = 0;
	public static final int POPULATION_SIZE = 100;
	public static final double ELITISM_RATE = 0.02;
	public static final int MAX_GENERATION = 200;
	public static final int FITNESS_TYPE = 4;
	public static final int SELECTION_TYPE = 1;
	public static final double CROSSOVER_RATE = 0.8;
	public static final int CROSSOVER_TYPE = 3;
	public static final double MUTATION_RATE = 0.2;
	public static final int MUTATION_TYPE = 2;
	public static final long POWER = 1;

	public static final long SEED = 1;

	public static final String FN = "File Name";
	public static final String BF = "Best Fitness";
	public static final String AF = "Average Fitness";
	public static final String SD = "Standard Deviation";
	public static final String PC = "Proper Completion";
	public static final String PS = "Population Size";
	public static final String GN = "Generation Number";
	public static final String FT = "Fitness Type";
	public static final String TT = "Tournament Type";
	public static final String MR = "Maximum Runs";
	public static final String S = "Seed";
	public static final String ST = "Selection Type";
	public static final String UGO = "Use Genetic Operators";
	public static final String MRt = "Mutation Rate";
	public static final String MTp = "Mutation Type";
	public static final String CRt = "Crossover Rate";
	public static final String CTp = "Crossover Type";
	public static final String IPT = "Initial Population Type";
	public static final String POW = "Power";
	public static final String ELI = "Elitism Rate";
	public static final String ET = "Elapsed Time(ms)";
	public static final String FP = "Fitness Parameters";

	public static final String[] logLine = { FN, BF, AF, SD, PC, PS, GN, FT,
			MR, S, ST, UGO, MTp, MRt, CTp, CRt, IPT, POW, ELI, ET, FP };

	/**
	 * Returns the index of a certain constant in the logLine array.
	 * 
	 * @param constant
	 *            String constant value
	 * @return int the index of this constant. If the vector does not contain
	 *         the constant, -1 is returned.
	 */
	public static int getIndexConstantInLogLine(String constant) {
		for (int i = 0; i < logLine.length; i++) {
			if (logLine[i].compareTo(constant) == 0) {
				return i;
			}
		}
		return -1;
	}

}
