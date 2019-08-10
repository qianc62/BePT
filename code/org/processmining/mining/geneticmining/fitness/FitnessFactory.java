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

package org.processmining.mining.geneticmining.fitness;

import org.processmining.framework.log.LogReader;
import org.processmining.mining.geneticmining.fitness.duplicates.DTContinuousSemanticsFitness;
import org.processmining.mining.geneticmining.fitness.duplicates.DTExtraBehaviorPunishmentFitness;
import org.processmining.mining.geneticmining.fitness.duplicates.DTExtraBehaviorPunishmentSplittedLogFitness;
import org.processmining.mining.geneticmining.fitness.duplicates.DTImprovedContinuousSemanticsFitness;
import org.processmining.mining.geneticmining.fitness.duplicates.DTProperCompletionFitness;
import org.processmining.mining.geneticmining.fitness.duplicates.DTStopSemanticsFitness;
import org.processmining.mining.geneticmining.util.ParameterValue;
import org.processmining.mining.heuristicsmining.fitness.ContinuousParsingMeasure;

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

public class FitnessFactory {

	public static final int PROPER_COMPLETION_INDEX = 0;
	public static final int STOP_SEMANTICS_INDEX = 1;
	public static final int CONTINUOUS_SEMANTICS_INDEX = 2;
	public static final int IMPROVED_CONTINUOUS_SEMANTICS_INDEX = 3;
	public static final int EXTRA_BEHAVIOR_PUNISHMENT_INDEX = 4;
	public static final int CONTINUOUS_PARSING_MEASURE_INDEX = 5;

	public FitnessFactory() {
	}

	public static String[] getAllFitnessTypes() {
		return new String[] { "ProperCompletion", "StopSemantics",
				"ContinuousSemantics", "ImprovedContinuousSemantics",
				"ExtraBehaviorPunishment", "ContinuousParsingMeasure" }; // ,
		// "ExtraBehaviorPunishmentSplittedLog"};
	}

	/**
	 * This methods returns the names of the parameters used for a certain
	 * fitness types. When the fitness does not have a special parameter (e.g.
	 * kappa ou gama), the returned array is empty.
	 * 
	 * @param fitnessIndex
	 *            int index of the fitness type.
	 * @return String[] Array containing the names of the parameters. If the
	 *         fitness does not require any special parameter, an empty array is
	 *         returned.
	 */
	public static ParameterValue[] getAdvancedFitnessParametersNames(
			int fitnessIndex) {
		ParameterValue[] parametersNames = null;
		switch (fitnessIndex) {
		case PROPER_COMPLETION_INDEX:
			parametersNames = new ParameterValue[0];
			break;

		case STOP_SEMANTICS_INDEX:
			parametersNames = new ParameterValue[0];
			break;

		case CONTINUOUS_SEMANTICS_INDEX:
			parametersNames = new ParameterValue[0];
			break;

		case IMPROVED_CONTINUOUS_SEMANTICS_INDEX:
			parametersNames = new ParameterValue[0];
			break;

		case EXTRA_BEHAVIOR_PUNISHMENT_INDEX:
			parametersNames = new ParameterValue[1];
			parametersNames[0] = new ParameterValue(
					"           Extra Behavior Punishment  ",
					0.025/* default */,
					0.000/* min */,
					1.000/* max */,
					0.001/* step size */,
					"Sets the amount of punishment (kappa) for the extra behavior that an individual allows for.");
			break;

		case CONTINUOUS_PARSING_MEASURE_INDEX:
			parametersNames = new ParameterValue[0];
			break;
		}

		return parametersNames;
	}

	/**
	 * Creates the specified fitness type object.
	 * 
	 * @param indexFitnessType
	 *            int Fitness type.
	 * @param log
	 *            LogReader Log to be parsed by the created fitness type object.
	 * @param parameters
	 *            double[] the parameters to be used by the specificied fitness
	 *            type.
	 * @return Fitness Object of the selected fitness type.
	 */

	public static Fitness getFitness(int indexFitnessType, LogReader log,
			double[] parameters) {
		Fitness object = null;
		switch (indexFitnessType) {
		case PROPER_COMPLETION_INDEX:
			object = new DTProperCompletionFitness(log);
			break;
		case STOP_SEMANTICS_INDEX:
			object = new DTStopSemanticsFitness(log);
			break;
		case CONTINUOUS_SEMANTICS_INDEX:
			object = new DTContinuousSemanticsFitness(log);
			break;
		case IMPROVED_CONTINUOUS_SEMANTICS_INDEX:
			object = new DTImprovedContinuousSemanticsFitness(log);
			break;
		case EXTRA_BEHAVIOR_PUNISHMENT_INDEX:
			object = new DTExtraBehaviorPunishmentFitness(log, parameters);
			break;
		case CONTINUOUS_PARSING_MEASURE_INDEX:
			object = new ContinuousParsingMeasure(log);
			break;
		// case 6:
		// object = new DTExtraBehaviorPunishmentSplittedLogFitness(log);
		// break;

		}
		return object;
	}

}
