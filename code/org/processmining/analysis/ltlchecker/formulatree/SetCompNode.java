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
import java.util.List;

import org.processmining.analysis.ltlchecker.parser.AttributeNoValueException;
import org.processmining.analysis.ltlchecker.parser.ParseAttributeException;
import org.processmining.analysis.ltlchecker.parser.SetAttribute;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;

/**
 * SetCompNode is a node class of the formula tree denoting comparator operators
 * for sets: ==, != and in.
 * 
 * @version 0.2
 * @author HT de Beer
 */
public class SetCompNode extends CompNode {

	// FIELDS

	/**
	 * Lefthandside attribute of this comparison. Either a SetAttribute or a
	 * ConceptSetAttribute.
	 */
	private SetAttribute attribute;

	/**
	 * The righthandside expressiong of this comparison, that is in this case
	 * and StringValue.
	 */
	private SetValueNode value;

	/**
	 * The operator of this node one of the constants defined in {@see CompNode}
	 * .*
	 */
	private int op;

	// CONSTRUCTORS

	public SetCompNode(int op) {
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
	public void setAttribute(SetAttribute attr) {
		this.attribute = attr;
	}

	/**
	 * Set the righthand side value of this node.
	 * 
	 * @param val
	 *            The value of this node.
	 */
	public void setValue(SetValueNode val) {
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
		boolean result = false;
		if (ateNr < ates.size()) {
			try {
				nr = ateNr;

				if (value.getType() == SetValueNode.MODEL_REFERENCE_SET) {
					List<String> refs = attribute.modelReferences(pi, ates,
							ateNr);
					switch (this.op) {
					case CompNode.EQUAL:
					case CompNode.IN:
						result = value.hasInstanceOf(refs);
						break;
					case CompNode.NOTEQUAL:
						result = !value.hasInstanceOf(refs);
						break;
					default:
						throw new RuntimeException("cannot use operator "
								+ opAsString(this.op) + " on a set of concepts");
					}
				} else if (value.getType() == SetValueNode.SET) {
					String attrValue = attribute.value(pi, ates, ateNr);

					switch (this.op) {
					case CompNode.EQUAL:
					case CompNode.IN:
						result = value.in(attrValue);
						break;
					case CompNode.NOTEQUAL:
						result = !value.in(attrValue);
						break;
					default:
						throw new RuntimeException("cannot use operator "
								+ opAsString(this.op) + " on a set of values");
					}
				} else {
					String attrValue = attribute.value(pi, ates, ateNr);
					String exprValue = value.value(pi, ates, ateNr);

					switch (this.op) {
					case CompNode.EQUAL:
						result = (exprValue.equals(attrValue));
						break;
					case CompNode.NOTEQUAL:
						result = (!exprValue.equals(attrValue));
						break;
					case CompNode.LESSEREQUAL:
						result = (exprValue.compareTo(attrValue) >= 0);
						break;
					case CompNode.BIGGEREQUAL:
						result = (exprValue.compareTo(attrValue) <= 0);
						break;
					// case CompNode.REGEXPEQUAL :
					// result = (attrValue.matches( exprValue ));
					// break;
					case CompNode.LESSER:
						result = (exprValue.compareTo(attrValue) > 0);
						break;
					case CompNode.BIGGER:
						result = (exprValue.compareTo(attrValue) < 0);
						break;
					default:
						throw new RuntimeException("cannot use operator "
								+ opAsString(this.op) + " on a single value");
					}
				}

				return result;

			} catch (ParseAttributeException pae) {
				// Value or attribute van not be parsed, So a comparison
				// always result in false;
				Message.add(pae.getMessage(), Message.ERROR);
				return false;
			} catch (AttributeNoValueException anve) {
				// No vaule so the comparison is false.
				return false;
			}
		} else {
			// Out of range ... so the value is false.
			return false;
		}
	}

	public String toString() {
		return attribute.toString() + opAsString(op) + value.toString();
	}
}
