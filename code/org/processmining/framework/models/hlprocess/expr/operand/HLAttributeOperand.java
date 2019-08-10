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
package org.processmining.framework.models.hlprocess.expr.operand;

import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLProcess;
import org.processmining.framework.models.hlprocess.att.HLAttributeValue;
import org.processmining.framework.models.hlprocess.expr.HLExpressionOperand;

/**
 * An operand representing a placeholder rather than a fixed value.
 * <p>
 * For example, in the expression <code>Amount > 500</code> "Amount" would be a
 * placeholder referring to some attribute in the process (i.e., correspond to
 * an attribute operand) while "500" would be a value operand and ">" would be
 * the operator linking the two.
 * <p>
 * This implies that to actually evaluate an expression containing attribute
 * placeholders, concrete values need to be available for these attributes
 * (rather than aggregate information such as value range etc.).
 * 
 * @see HLValueOperand
 */
public class HLAttributeOperand extends HLExpressionOperand {

	/**
	 * The attribue from the high-level process that this placeholder operand is
	 * referring to.
	 */
	protected HLID attID;
	/**
	 * The high level process from which the actual attribute object can be
	 * obtained.
	 */
	protected HLProcess process;

	/**
	 * Creates an attribute operand based on the given attribute.
	 * 
	 * @param att
	 *            the attribute that serves as a placeholder in the expression
	 */
	public HLAttributeOperand(HLID theAttID, HLProcess hlProcess) {
		attID = theAttID;
		process = hlProcess;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLExpressionOperand
	 * #getValue()
	 */
	public HLAttributeValue getValue() {
		HLAttribute att = getAttribute();
		if (att != null) {
			return att.getCurrentValue();
		} else {
			return null;
		}
	}

	/**
	 * Retrieves the attribute associated to this operand.
	 * 
	 * @return the high level attribute
	 */
	public HLAttribute getAttribute() {
		return process.getAttribute(attID);
	}

	/**
	 * Replaces the current attribute ID by the new ID. Only if the given oldID
	 * matches the current ID (no effect otherwise).
	 * 
	 * @param oldID
	 *            the ID that needs to match the current ID
	 * @param newID
	 *            the new ID that should be assigned
	 */
	public void replaceAttribute(HLID oldID, HLID newID) {
		if (attID.equals(oldID)) {
			attID = newID;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLDataExpressionElement
	 * #toString()
	 */
	public String toString() {
		HLAttribute att = getAttribute();
		if (att != null) {
			return att.getName();
		} else {
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.expr.HLExpressionElement
	 * #evaluateToString()
	 */
	public String evaluateToString() {
		HLAttribute att = getAttribute();
		if (att != null) {
			return getAttribute().getName();
		} else {
			return "null";
		}
	}
}
