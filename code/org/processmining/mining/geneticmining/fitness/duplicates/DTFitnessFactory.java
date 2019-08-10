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

package org.processmining.mining.geneticmining.fitness.duplicates;

import org.processmining.framework.log.LogReader;
import org.processmining.mining.geneticmining.fitness.Fitness;
import org.processmining.mining.geneticmining.util.ParameterValue;

/**
 * <p>
 * Title: Duplicate Tasks Fitness Measures
 * </p>
 * <p>
 * Description: This class provides objects of the interface
 * <code>Fitness</code>.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros.
 * @version 1.0
 */

public class DTFitnessFactory {

	public static double KAPPA = 0.025;
	public static double GAMMA = 0.025;

	public static int INDEX_KAPPA = 0;
	public static int INDEX_GAMMA = 1;

	public static double[] ALL_FITNESS_PARAMETERS = new double[] { KAPPA, GAMMA };

	public DTFitnessFactory() {
	}

	/**
	 * Returns an array containing the names of the supported fitness types.
	 */
	public static String[] getAllFitnessTypes() {
		return new String[] { "ProperCompletion",
				"ImprovedContinuousSemantics", "ExtraBehavior" };
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
		case 0:
			parametersNames = new ParameterValue[0];
			break;

		case 1:
			parametersNames = new ParameterValue[0];
			break;

		case 2:
			parametersNames = new ParameterValue[2];
			parametersNames[0] = new ParameterValue(
					"           Extra Behavior Punishment  ",
					0.025/* default */,
					0.000/* min */,
					1.000/* max */,
					0.001/* step size */,
					"Sets the amount of punishment (kappa) for the extra behavior that an individual allows for.");
			parametersNames[1] = new ParameterValue(
					"           Duplicates With Common In/Out Tasks Punishment  ",
					0.025/* default */,
					0.000/* min */,
					1.000/* max */,
					0.001/* step size */,
					"Sets the amount of punishment (gamma) for the amount of duplicates sharing input/output tasks that an individual has.");
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
		case 0:
			object = new DTProperCompletionFitness(log);
			break;
		case 1:
			object = new DTImprovedContinuousSemanticsFitness(log);
			break;
		case 2:
			object = new DTExtraBehaviorPunishmentFitness(log, parameters);
			break;
		}
		return object;
	}
}
