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

package org.processmining.analysis.ltlchecker.formulatree;

import java.util.LinkedList;

import org.processmining.analysis.ltlchecker.parser.AttributeNoValueException;
import org.processmining.analysis.ltlchecker.parser.NumberAttribute;
import org.processmining.analysis.ltlchecker.parser.ParseAttributeException;
import org.processmining.framework.log.ProcessInstance;

/**
 * NumberValueNode is a representation of a number literal or attribute.
 * 
 * @version 0.2
 * @author HT de Beer
 */
public class NumberValueNode extends ValueNode {

	// FIELDS

	/** It is an attribute of this ate. */
	public static final int VALUE = 0;

	/** Plus operator */
	public static final int PLUS = 2;

	/** unary Min operator */
	public static final int UNMIN = 3;

	/** Min operator */
	public static final int MIN = 4;

	/** Mult operator */
	public static final int MULT = 5;

	/** Div operator */
	public static final int DIV = 6;

	/**
	 * If this node is an attributevalue or an literal, this field contains the
	 * attribute.
	 */
	private NumberAttribute value;

	/**
	 * The `type' of this node, either attribute or literal or an numerical
	 * operator like plus.
	 */
	private int type;

	/**
	 * The left child if any, a Number too.
	 */
	private NumberValueNode leftChild;

	/**
	 * The right child if any, a Number too.
	 */
	private NumberValueNode rightChild;

	// CONSTRUCTORS
	public NumberValueNode(int type) {
		this.type = type;
	}

	// METHODS

	/**
	 * Set the left child.
	 * 
	 * @param child
	 *            The left child.
	 */
	public void setLeftChild(NumberValueNode child) {
		this.leftChild = child;
	}

	/**
	 * Set the right hild.
	 * 
	 * @param child
	 *            The right child.
	 */
	public void setRightChild(NumberValueNode child) {
		this.rightChild = child;
	}

	/**
	 * Set the Value.
	 * 
	 * @param val
	 *            The value to set.
	 */
	public void setValue(NumberAttribute val) {
		this.value = val;
	}

	/**
	 * Compute the value of this node, either getting the string representation
	 * fo the attribute or giving the literal.
	 * 
	 * @param pi
	 *            The current process instance.
	 * @param ate
	 *            The current audit trail entry of this pi.
	 * 
	 * @return The string of this node.
	 */
	public float value(ProcessInstance pi, LinkedList ates, int ateNr)
			throws AttributeNoValueException, ParseAttributeException {

		nr = ateNr;
		float result = Float.NaN;

		switch (this.type) {
		case VALUE:

			// Try to get or compute the value. If this does
			// not succeed, attend the comparison node, so
			// that it can take the appropiate action. That
			// is, giving back false and write to a log.
			try {
				result = this.value.value(pi, ates, this.getBinder().getNr());
				break;
				// If this value is to be computed, use the binded
				// ateNr because it may be bound by a
				// formulacall...........................
			} catch (ParseAttributeException pae) {
				throw pae;
			} catch (AttributeNoValueException anve) {
				throw anve;
			}
		case UNMIN:
			result = -leftChild.value(pi, ates, ateNr);
			break;

		case PLUS:
			result = leftChild.value(pi, ates, ateNr)
					+ rightChild.value(pi, ates, ateNr);
			break;

		case MIN:
			result = leftChild.value(pi, ates, ateNr)
					- rightChild.value(pi, ates, ateNr);
			break;

		case MULT:
			result = leftChild.value(pi, ates, ateNr)
					* rightChild.value(pi, ates, ateNr);
			break;

		case DIV:
			result = leftChild.value(pi, ates, ateNr)
					/ rightChild.value(pi, ates, ateNr);
			break;
		}
		;

		return result;
	}

	public String toString() {
		switch (this.type) {
		case VALUE:
			return value.toString();
		case UNMIN:
			return "( -" + leftChild.toString() + ")";
		case PLUS:
			return "(" + leftChild.toString() + " + " + rightChild.toString()
					+ ")";

		case MIN:
			return "(" + leftChild.toString() + " - " + rightChild.toString()
					+ ")";

		case MULT:
			return "(" + leftChild.toString() + " * " + rightChild.toString()
					+ ")";

		case DIV:
			return "(" + leftChild.toString() + " / " + rightChild.toString()
					+ ")";
		}
		;
		return "";
	}

	@Override
	public String asParseableDefaultValue() {
		return toString();
	}
}
