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
package org.processmining.analysis.petrinet.cpnexport;

import javax.swing.tree.DefaultMutableTreeNode;

import org.processmining.framework.models.hlprocess.att.HLBooleanDistribution;
import org.processmining.framework.models.hlprocess.att.HLNominalDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLBernoulliDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLBinomialDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLChiSquareDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLConstantDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLDiscreteDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLErlangDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLExponentialDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLNormalDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLPoissonDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLStudentDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLUniformDistribution;
import org.processmining.framework.models.hlprocess.expr.HLDataExpression;
import org.processmining.framework.models.hlprocess.expr.HLExpressionElement;
import org.processmining.framework.models.hlprocess.expr.operand.HLAttributeOperand;
import org.processmining.framework.models.hlprocess.expr.operand.HLValueOperand;
import org.processmining.framework.models.hlprocess.expr.operator.HLAndOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLEqualOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLGreaterEqualOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLGreaterOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLNotEqualOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLNotOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLOrOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLSmallerEqualOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLSmallerOperator;
import org.processmining.framework.ui.Message;

/**
 * Contains static utility methods needed for generating CPN models.
 */
public class CpnUtils {

	/**
	 * Returns a cpn valid name.
	 * 
	 * @param name
	 *            String the original name
	 * @return String a name which only may contain letters, numbers, the _
	 *         (underscore) character, the ' (apostrophe) character and which
	 *         has to start with a letter.
	 */
	public static String getCpnValidName(String name) {
		if (name != null && name.equals("") == false) {
			// CPN does not allow names to start with a number
			name = replaceStartNumbers(name);
			// CPN does not allow names to contain spaces, and other special
			// characters
			name = replaceSpecialCharacters(name);
			// remove the last _ncomplete
			name = name.replaceAll("_ncomplete", "");
			return name;
		} else
			return "";
	}

	/**
	 * Replaces every character other than a letter, a number, an apostrophe, or
	 * an underscore with an underscore.
	 * 
	 * @param name
	 *            the string to be replaced
	 * @return the modified string
	 */
	public static String replaceSpecialCharacters(String name) {
		if (name != null && name.equals("") == false) {
			name = name.replaceAll("[^a-zA-Z0-9'_]", "_");
			return name;
		} else
			return "";
	}

	/**
	 * Inserts a character at the beginning of the given string if it starts by
	 * a number.
	 * 
	 * @param name
	 *            the string to be replaced
	 * @return the modified string
	 */
	public static String replaceStartNumbers(String name) {
		if (name != null && name.equals("") == false) {
			String firstChar = name.substring(0, 1);
			String remainder = name.substring(1, name.length());
			// if firstchar is not a letter then put n before it.
			if (firstChar.matches("[^a-zA-Z]")) {
				name = "n" + firstChar + remainder;
			}
			return name;
		} else
			return "";
	}

	/**
	 * The scientific notation of a double with E and a minus sign can not be
	 * directly used in cpn. The trick in which you read such a double from a
	 * string is allowed.
	 * 
	 * @param number
	 *            double the number for which we want to check whether it
	 *            contains an E with a minus sign
	 * @return String In case that the number contains an E with a minus sign, a
	 *         string is returned so that in cpn the value is read from a
	 *         string. Otherwise, the string representation of the number is
	 *         returned.
	 */
	public static String negativeEnotationCheck(double number) {
		String returnString = "";
		String numberToString = Double.toString(number);
		// valOf(Real.fromString("5e-9"))
		if (numberToString.contains("E-")) {
			returnString = "valOf(Real.fromString(\"" + numberToString + "\"))";
		} else {
			returnString = numberToString;
		}
		return returnString;
	}

	/**
	 * Generates a CPN Tools compliant distribution string based on the given
	 * distribution.
	 * 
	 * @param dist
	 *            the distribution for which the CPN distribution is requested
	 * @return the CPN string that can be used to represent the given
	 *         distribution
	 */
	public static String getCpnDistributionFunction(HLDistribution dist) {
		String result = "";
		if (dist instanceof HLBernoulliDistribution) {
			result = "bernoulli("
					+ Double.toString(((HLBernoulliDistribution) dist)
							.getProbability()) + ")";
		} else if (dist instanceof HLBinomialDistribution) {
			result = "binomial("
					+ ((HLBinomialDistribution) dist).getNumberOfExperiments()
					+ "," + ((HLBinomialDistribution) dist).getProbability()
					+ ")";
		} else if (dist instanceof HLChiSquareDistribution) {
			result = "round(chisq("
					+ ((HLChiSquareDistribution) dist).getDegreesFreedom()
					+ "))";
		} else if (dist instanceof HLConstantDistribution) {
			result = "round(" + ((HLConstantDistribution) dist).getConstant()
					+ ")";
		} else if (dist instanceof HLDiscreteDistribution) {
			result = "discrete(" + ((HLDiscreteDistribution) dist).getMin()
					+ "," + ((HLDiscreteDistribution) dist).getMax() + ")";
		} else if (dist instanceof HLErlangDistribution) {
			result = "round(erlang("
					+ ((HLErlangDistribution) dist).getEmergenceOfEvents()
					+ ","
					+ negativeEnotationCheck(((HLErlangDistribution) dist)
							.getIntensity()) + "))";
		} else if (dist instanceof HLExponentialDistribution) {
			result = "round(exponential("
					+ negativeEnotationCheck(((HLExponentialDistribution) dist)
							.getIntensity()) + "))";
		} else if (dist instanceof HLGeneralDistribution) {
			result = getCpnDistributionFunction(((HLGeneralDistribution) dist)
					.getBestDistribution());
		} else if (dist instanceof HLNormalDistribution) {
			result = "round(normal("
					+ negativeEnotationCheck(((HLNormalDistribution) dist)
							.getMean())
					+ ","
					+ negativeEnotationCheck(((HLNormalDistribution) dist)
							.getVariance()) + "))";
		} else if (dist instanceof HLPoissonDistribution) {
			result = "poisson(1.0 / "
					+ negativeEnotationCheck(((HLPoissonDistribution) dist)
							.getIntensity()) + ")";
		} else if (dist instanceof HLStudentDistribution) {
			result = "round(student("
					+ ((HLStudentDistribution) dist).getDegreesFreedom() + "))";
		} else if (dist instanceof HLUniformDistribution) {
			result = "round(uniform("
					+ negativeEnotationCheck(((HLUniformDistribution) dist)
							.getMin())
					+ ","
					+ negativeEnotationCheck(((HLUniformDistribution) dist)
							.getMax()) + "))";
		} else {
			Message
					.add("Distribution type not found while creating CPN Tools compliant distribution string");
		}
		return result;
	}

	/**
	 * Generates a CPN Tools compliant distribution string based on the given
	 * boolean distribution.
	 * 
	 * @param dist
	 *            the boolean distribution for which the CPN distribution is
	 *            requested
	 * @param attName
	 *            the name of the boolean attribute for which the function is
	 *            generated
	 * @return the CPN string that can be used to represent the given
	 *         distribution
	 */
	public static String getCpnDistributionFunction(HLBooleanDistribution dist,
			String attName) {
		String result = "";
		result = "get" + attName + "Value(bernoulli("
				+ dist.getProbability()
				+ ")) and "
				+
				// translate 1 and 0 value into boolean true and false,
				// respectively
				"get" + attName
				+ "Value(i:int) = if (i=1) then true else false";
		return result;
	}

	/**
	 * Generates a CPN Tools compliant distribution string based on the given
	 * nominal distribution.
	 * 
	 * @param dist
	 *            the nominal distribution for which the CPN distribution is
	 *            requested
	 * @param attName
	 *            the name of the nominal attribute for which the function is
	 *            generated
	 * @return the CPN string that can be used to represent the given
	 *         distribution
	 */
	public static String getCpnDistributionFunction(HLNominalDistribution dist,
			String attName) {
		String result = "";
		result = "get" + attName + "Value(discrete(1,"
				+ dist.getSumOfAllFrequencies() + ")) and " +
				// defines a second function retrieving the nominal value for
				// the random int value in the same row
				"get" + attName + "Value(i:int) = ";
		int accumulatedFreq = 1;
		int counter = 1;
		for (String val : dist.getValues()) {
			int freqVal = dist.getFrequencyPossibleValueNominal(val);
			if (counter < dist.getValues().size()) {
				result = result + "\nif (" + accumulatedFreq
						+ "<=i andalso i<=" + (accumulatedFreq + freqVal - 1)
						+ ") then " + val + " else (";
			} else {
				// last nested element -> no further nesting needed
				result = result + val;
			}
			accumulatedFreq = accumulatedFreq + freqVal;
			counter = counter + 1;
		}
		// now close the brackets of all the nested if else statements
		for (int i = 1; i < dist.getValues().size(); i++) {
			result = result + ")";
		}
		return result;
	}

	/**
	 * Generates a CPN-compliant string for the given data dependency, which can
	 * be directly set as guard for the respective CPN transition.
	 * <p>
	 * This means that the expression is logically composed of AND (CPN:
	 * andalso) and OR (CPN: orelse) connectors over data attribute statements
	 * (CPN: #attributeName data).
	 * 
	 * @param expr
	 *            the high level expression to be converted
	 * @return the CPN-compliant string for this expression
	 */
	public static String getCpnExpression(HLDataExpression expr) {
		HLExpressionElement root = expr.getRootExpressionElement();
		return getCpnExpression(root);
	}

	/*
	 * Dispatches the expression elements based on their type. TODO: check how
	 * overloading can be put to work or refactor (overwrite evaluateToString()
	 * method of operators instead)
	 */
	private static String getCpnExpression(HLExpressionElement elem) {
		if (elem instanceof HLAttributeOperand) {
			return getCpnExpression((HLAttributeOperand) elem);
		} else if (elem instanceof HLValueOperand) {
			return getCpnExpression((HLValueOperand) elem);
		} else if (elem instanceof HLAndOperator) {
			return getCpnExpression((HLAndOperator) elem);
		} else if (elem instanceof HLOrOperator) {
			return getCpnExpression((HLOrOperator) elem);
		} else if (elem instanceof HLGreaterOperator) {
			return getCpnExpression((HLGreaterOperator) elem);
		} else if (elem instanceof HLGreaterEqualOperator) {
			return getCpnExpression((HLGreaterEqualOperator) elem);
		} else if (elem instanceof HLSmallerOperator) {
			return getCpnExpression((HLSmallerOperator) elem);
		} else if (elem instanceof HLSmallerEqualOperator) {
			return getCpnExpression((HLSmallerEqualOperator) elem);
		} else if (elem instanceof HLNotEqualOperator) {
			return getCpnExpression((HLNotEqualOperator) elem);
		} else if (elem instanceof HLEqualOperator) {
			return getCpnExpression((HLEqualOperator) elem);
		} else if (elem instanceof HLNotOperator) {
			return getCpnExpression((HLNotOperator) elem);
		} else {
			return "";
		}
	}

	/*
	 * Attribute values will be accessed in the record type of the data
	 * variable, e.g., "#Amount data".
	 */
	private static String getCpnExpression(HLAttributeOperand op) {
		return "#" + op.getAttribute().getName() + " data";
	}

	/*
	 * Values and attributes are expected to be leaf nodes in expressions, i.e.,
	 * no other expressions need to be evaluated below.
	 */
	private static String getCpnExpression(HLValueOperand op) {
		return op.getValue().toString();
	}

	/*
	 * Evaluates an AND operator into "(..) andalso (..) andalso (..)", whereas
	 * the content within the brackets is determined by the sub expressions.
	 */
	private static String getCpnExpression(HLAndOperator op) {
		String result = "";
		if (op.getExpressionNode().getChildCount() == 0) {
			return result;
		} else if (op.getExpressionNode().getChildCount() == 1) {
			HLExpressionElement childExpr = (HLExpressionElement) ((DefaultMutableTreeNode) op
					.getExpressionNode().getChildAt(0)).getUserObject();
			return childExpr.evaluateToString();
		} else {
			for (int i = 0; i < op.getExpressionNode().getChildCount(); i++) {
				HLExpressionElement childExpr = (HLExpressionElement) ((DefaultMutableTreeNode) op
						.getExpressionNode().getChildAt(i)).getUserObject();
				if (result == "") {
					result = result + "(" + getCpnExpression(childExpr) + ")";
				} else {
					result = result + " andalso " + "("
							+ getCpnExpression(childExpr) + ")";
				}
			}
			return result;
		}
	}

	/*
	 * Evaluates an OR operator into "(..) \n orelse\n (..) \n orelse\n (..)",
	 * whereas the content within the brackets is determined by the sub
	 * expressions.
	 */
	private static String getCpnExpression(HLOrOperator op) {
		String result = "";
		if (op.getExpressionNode().getChildCount() == 0) {
			return result;
		} else if (op.getExpressionNode().getChildCount() == 1) {
			HLExpressionElement childExpr = (HLExpressionElement) ((DefaultMutableTreeNode) op
					.getExpressionNode().getChildAt(0)).getUserObject();
			return childExpr.evaluateToString();
		} else {
			for (int i = 0; i < op.getExpressionNode().getChildCount(); i++) {
				HLExpressionElement childExpr = (HLExpressionElement) ((DefaultMutableTreeNode) op
						.getExpressionNode().getChildAt(i)).getUserObject();
				if (result == "") {
					result = result + "(" + getCpnExpression(childExpr) + ")";
				} else {
					result = result + " orelse " + "("
							+ getCpnExpression(childExpr) + ")";
				}
			}
			return result;
		}
	}

	/*
	 * Will be evaluated to ".. > .." (without any brackets and assuming exactly
	 * two sub expressions).
	 */
	private static String getCpnExpression(HLGreaterOperator op) {
		HLExpressionElement first = (HLExpressionElement) ((DefaultMutableTreeNode) op
				.getExpressionNode().getChildAt(0)).getUserObject();
		HLExpressionElement second = (HLExpressionElement) ((DefaultMutableTreeNode) op
				.getExpressionNode().getChildAt(1)).getUserObject();
		return getCpnExpression(first) + " > " + getCpnExpression(second);
	}

	/*
	 * Will be evaluated to ".. >= .." (without any brackets and assuming
	 * exactly two sub expressions).
	 */
	private static String getCpnExpression(HLGreaterEqualOperator op) {
		HLExpressionElement first = (HLExpressionElement) ((DefaultMutableTreeNode) op
				.getExpressionNode().getChildAt(0)).getUserObject();
		HLExpressionElement second = (HLExpressionElement) ((DefaultMutableTreeNode) op
				.getExpressionNode().getChildAt(1)).getUserObject();
		return getCpnExpression(first) + " >= " + getCpnExpression(second);
	}

	/*
	 * Will be evaluated to ".. < .." (without any brackets and assuming exactly
	 * two sub expressions).
	 */
	private static String getCpnExpression(HLSmallerOperator op) {
		HLExpressionElement first = (HLExpressionElement) ((DefaultMutableTreeNode) op
				.getExpressionNode().getChildAt(0)).getUserObject();
		HLExpressionElement second = (HLExpressionElement) ((DefaultMutableTreeNode) op
				.getExpressionNode().getChildAt(1)).getUserObject();
		return getCpnExpression(first) + " < " + getCpnExpression(second);
	}

	/*
	 * Will be evaluated to ".. <= .." (without any brackets and assuming
	 * exactly two sub expressions).
	 */
	private static String getCpnExpression(HLSmallerEqualOperator op) {
		HLExpressionElement first = (HLExpressionElement) ((DefaultMutableTreeNode) op
				.getExpressionNode().getChildAt(0)).getUserObject();
		HLExpressionElement second = (HLExpressionElement) ((DefaultMutableTreeNode) op
				.getExpressionNode().getChildAt(1)).getUserObject();
		return getCpnExpression(first) + " <= " + getCpnExpression(second);
	}

	/*
	 * Will be evaluated to ".. <> .." (without any brackets and assuming
	 * exactly two sub expressions).
	 */
	private static String getCpnExpression(HLNotEqualOperator op) {
		HLExpressionElement first = (HLExpressionElement) ((DefaultMutableTreeNode) op
				.getExpressionNode().getChildAt(0)).getUserObject();
		HLExpressionElement second = (HLExpressionElement) ((DefaultMutableTreeNode) op
				.getExpressionNode().getChildAt(1)).getUserObject();
		return getCpnExpression(first) + " <> " + getCpnExpression(second);
	}

	/*
	 * Will be evaluated to ".. = .. = .." (without any brackets but allowing
	 * for multiple sub expressions to be compared).
	 */
	private static String getCpnExpression(HLEqualOperator op) {
		String result = "";
		for (int i = 0; i < op.getExpressionNode().getChildCount(); i++) {
			HLExpressionElement childExpr = (HLExpressionElement) ((DefaultMutableTreeNode) op
					.getExpressionNode().getChildAt(i)).getUserObject();
			if (result == "") {
				result = result + getCpnExpression(childExpr);
			} else {
				result = result + " = " + getCpnExpression(childExpr);
			}
		}
		return result;
	}

	/*
	 * Will be evaluated to ".. = .. = .." (without any brackets but allowing
	 * for multiple sub expressions to be compared).
	 */
	private static String getCpnExpression(HLNotOperator op) {
		String result = "";
		if (op.getExpressionNode().getChildCount() == 0) {
			return result;
		} else {
			HLExpressionElement first = (HLExpressionElement) ((DefaultMutableTreeNode) op
					.getExpressionNode().getChildAt(0)).getUserObject();
			return "not(" + getCpnExpression(first) + ")";
		}
	}

}
