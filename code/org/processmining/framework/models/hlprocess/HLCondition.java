/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.hlprocess;

import org.processmining.framework.models.hlprocess.expr.HLDataExpression;

/**
 * Container class for enabling conditions in the context of a high level
 * choice. <br>
 * Will be related to some high level activity in the process and describes
 * pre-conditions that need to be fulfilled to activate the activity (e.g.,
 * based on data or probabilistic values).
 * 
 * @author Anne Rozinat
 */
public class HLCondition implements Cloneable {

	/** Parent choice object */
	protected HLChoice choice;
	/** Data dependency to enable the activity */
	protected HLDataExpression expression = new HLDataExpression();
	/** Probability to enable the activity */
	protected double probability = 0;
	/** Relative frequency to enable the activity */
	protected int frequency = 1;

	public HLCondition(HLChoice parent) {
		choice = parent;
	}

	/**
	 * Retrieves the activity that is the target for this condition.
	 * 
	 * @return the activity that is associated to this condition.
	 *         <code>Null</code> if not found
	 */
	public HLActivity getTarget() {
		// TODO Anne: check whether ID and retrieval via HLProcess needed
		// instead
		return choice.getTarget(this);
	}

	/**
	 * Retrieves the choice this condition is related to.
	 * 
	 * @return the choice object that provides the context for this condition
	 */
	public HLChoice getChoice() {
		return choice;
	}

	/**
	 * Retrieves the current data dependency for this condition.
	 * 
	 * @return the data dependency that must be fulfilled to enable the
	 *         associated activity
	 */
	public HLDataExpression getExpression() {
		return expression;
	}

	/**
	 * Provides a new data dependency for this condition.
	 * 
	 * @param expression
	 *            the new data dependency that must be fulfilled to enable the
	 *            associated activity
	 */
	public void setExpression(HLDataExpression expression) {
		this.expression = expression;
	}

	public void setExpression(String expression) {
		this.expression.setExpression(expression);
	}

	/**
	 * Retrieves the relative frequency to enable the activity (to be seen in
	 * the context of other enabling targets of the choice).
	 * 
	 * @return the relative frequency to enable the associated activity
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * Specifies a new relative frequency to enable the activity (to be seen in
	 * the context of other enabling targets of the choice).
	 * 
	 * @param frequency
	 *            the new relative frequency to enable the associated activity
	 */
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	/**
	 * Retrieves the probability to enable the activity.
	 * 
	 * @return the current probability to enable the associated activity
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * Specifies a new probability to enable the activity.
	 * 
	 * @param probability
	 *            the new probability to enable the associated activity
	 */
	public void setProbability(double probability) {
		this.probability = probability;
	}

	/**
	 * Makes a deep copy of this object. <br>
	 * Note that this method needs to be extended as soon as there are
	 * attributes added to the class which are not primitive or immutable.
	 */
	public Object clone() {
		HLCondition o = null;
		try {
			o = (HLCondition) super.clone();
			o.expression = (HLDataExpression) o.expression.clone();
			return o;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	// TODO Anne: find better place for this (should be in GUI package?)
	/**
	 * Replaces every "\n" by a "\\n" (needed for DOT visualiztion).
	 * 
	 * @return the backslash-escaped data expression
	 */
	public String getEscapedExpression() {
		String result = getExpression().toString();
		// replace new lines into backslash-escaped new lines
		result = result.replaceAll("\\n", "\\\\n");
		return result;
	}

	// /**
	// * Only evaluates the order rank of the associated expression element.
	// * <p>Be careful as this must not necessarily be set (default is -1) and
	// * there are also default expressions.
	// */
	// public int compareTo(Object o) {
	// // needs to compare only conditions
	// if (o instanceof HLCondition == false) {
	// return 0;
	// } else if (((HLCondition) o).getExpression() != null && getExpression()
	// != null &&
	// ((HLCondition) o).getExpression().getOrderRank() != -1 &&
	// getExpression().getOrderRank() != -1 &&
	// ((HLCondition) o).getExpression().getOrderRank() >
	// getExpression().getOrderRank()) {
	// // order rank must be provided for both expressions and the provided one
	// needs to be higher
	// // than this one
	// return 1;
	// } else {
	// return 0;
	// }
	// }

}
