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

package org.processmining.mining.geneticmining.util;

/**
 * 
 * <p>
 * Title: Parameters and Value
 * </p>
 * 
 * <p>
 * Description: This class stores an object that has a parameter name, its
 * double value and its description.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class ParameterValue {
	private String parameter = "";
	private double defaultValue = 0.0d;
	private double minValue = 0.0d;
	private double maxValue = 1.0d;
	private String description = "";
	private double stepSize = 0.1d;

	/**
	 * Creates the object.
	 * 
	 * @param parameterName
	 *            String - name of the parameter.
	 * @param defaultvalue
	 *            double - value of the parameter.
	 * @param minValue
	 *            double - minimum value of the parameter.
	 * @param maxValue
	 *            double - maximum value of the parameter.
	 * @param stepSize
	 *            double - size of the increment for this value. (Useful for
	 *            interfaces)
	 * @param parameterDescription
	 *            String - the parameter description.
	 */

	public ParameterValue(String parameterName, double defaultvalue,
			double minValue, double maxValue, double stepSize,
			String parameterDescription) {
		parameter = parameterName;
		this.defaultValue = defaultvalue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		description = parameterDescription;
		this.stepSize = stepSize;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public double getStepSize() {
		return stepSize;
	}

	public String getParameter() {
		return parameter;
	}

	public double getDefaultValue() {
		return defaultValue;
	}

	public String getDescription() {
		return description;
	}
}
