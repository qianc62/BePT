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

import java.util.Date;
import java.util.LinkedList;

import org.processmining.analysis.ltlchecker.parser.AttributeNoValueException;
import org.processmining.analysis.ltlchecker.parser.DateAttribute;
import org.processmining.analysis.ltlchecker.parser.ParseAttributeException;
import org.processmining.framework.log.ProcessInstance;

/**
 * DateValueNode is a representation of a string literal or attribute.
 * 
 * @version 0.2
 * @author HT de Beer
 */
public class DateValueNode extends ValueNode {

	// FIELDS

	/** It is an attribute of this ate. */
	public static final int VALUE = 0;

	/**
	 * If this node is an attributevalue, this field contains the attribute.
	 */
	private DateAttribute value;

	/** The `type' of this node, either attribute or literal. */
	private int type;

	// CONSTRUCTORS
	public DateValueNode(int type) {
		this.type = type;
	}

	// METHODS

	/**
	 * Set the value.
	 * 
	 * @param val
	 *            The value to set.
	 */
	public void setValue(DateAttribute val) {
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
	 * @return The date of this node.
	 */
	public Date value(ProcessInstance pi, LinkedList ates, int ateNr)
			throws AttributeNoValueException, ParseAttributeException {
		nr = ateNr;
		Date result = null;

		if (this.type == VALUE) {
			try {
				result = this.value.value(pi, ates, this.getBinder().getNr());
			} catch (ParseAttributeException pae) {
				throw pae;
			} catch (AttributeNoValueException anve) {
				throw anve;
			}
		}
		return result;
	}

	public String toString() {
		return value.toString();
	}

	@Override
	public String asParseableDefaultValue() {
		assert (value.isLiteral());
		return "\"" + value.getValue() + "\"";
	}
}
