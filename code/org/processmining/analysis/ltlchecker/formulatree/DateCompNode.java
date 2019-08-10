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
import org.processmining.framework.ui.Message;

/**
 * DateCompNode is a node class of the formula tree denoting comparator
 * operators for dates: ==, !=, <=, >=, < and >.
 * 
 * @version 0.2
 * @author HT de Beer
 */
public class DateCompNode extends CompNode {

	// FIELDS

	/** Lefthandside attribute of this comparison. */
	DateAttribute attribute;

	/**
	 * The righthandside expressiong of this comparison, that is in this case
	 * and StringValue.
	 */
	DateValueNode value;

	/**
	 * The operator of this node one of the constants defined in {@see CompNode}
	 * .*
	 */
	int op;

	// CONSTRUCTORS

	public DateCompNode(int op) {
		super();
		this.op = op;
	}

	// METHODS

	/**
	 * Set the attribute of this node.
	 * 
	 * @param attr
	 *            The attribute to set.
	 */
	public void setAttribute(DateAttribute attr) {
		this.attribute = attr;
	}

	/**
	 * Set the righthand side value of this node.
	 * 
	 * @param val
	 *            The value of this node.
	 */
	public void setValue(DateValueNode val) {
		this.value = val;
	}

	/**
	 * Compute the value of this node, that is te value of this node given the
	 * i-th ate of pi, computed by calling the value method of the children.
	 * 
	 * @param pi
	 *            The current process instance.
	 * @param ate
	 *            The current audit trail entry of pi.
	 * 
	 * @return The value of this node computed by calling the value method of
	 *         the children applied to the operator of this node.
	 */
	public boolean value(ProcessInstance pi, LinkedList ates, int ateNr) {
		if (ateNr < ates.size()) {
			boolean result = false;
			Date d = null;
			try {
				nr = ateNr;

				d = attribute.value(pi, ates, ateNr);

				switch (this.op) {
				case CompNode.EQUAL: {
					result = (attribute.value(pi, ates, ateNr).compareTo(
							value.value(pi, ates, ateNr)) == 0);
				}
					;
					break;

				case CompNode.NOTEQUAL: {
					result = (!(attribute.value(pi, ates, ateNr).compareTo(
							value.value(pi, ates, ateNr)) == 0));
				}
					;
					break;

				case CompNode.LESSEREQUAL: {
					result = (attribute.value(pi, ates, ateNr).compareTo(
							value.value(pi, ates, ateNr)) <= 0);
				}
					;
					break;

				case CompNode.BIGGEREQUAL: {
					result = (attribute.value(pi, ates, ateNr).compareTo(
							value.value(pi, ates, ateNr)) >= 0);
				}
					;
					break;

				case CompNode.LESSER: {
					result = (attribute.value(pi, ates, ateNr).compareTo(
							value.value(pi, ates, ateNr)) < 0);
				}
					;
					break;

				case CompNode.BIGGER: {
					result = (attribute.value(pi, ates, ateNr).compareTo(
							value.value(pi, ates, ateNr)) > 0);
				}
					;
					break;
				}
				;

				return result;
			} catch (ParseAttributeException pae) {
				// Unable to parse the attribute or value, so return
				// false.
				Message.add(pae.getMessage(), Message.ERROR);
				return false;
			} catch (AttributeNoValueException anve) {
				// No value so false
				return false;
			}
		} else {
			// out of bounds ...
			return false;
		}
	}

	public String toString() {
		return "(" + attribute.toString() + opAsString(op) + value.toString()
				+ ")";
	}
}
