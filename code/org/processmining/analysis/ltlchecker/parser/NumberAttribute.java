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

package org.processmining.analysis.ltlchecker.parser;

import java.util.LinkedList;
import java.util.Map;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.ProcessInstance;

/**
 * NumberAttribute is a specialised {@see Attribute} class implementing an
 * value method, used to compute the value of an attribute given the context.
 *
 * @version 0.1
 * @author HT de Beer
 */
public class NumberAttribute extends Attribute {

	public NumberAttribute(String value, int scope, int type, int kind) {
		super(value, scope, type, kind);
	}

	public NumberAttribute(String value, int kind, Attribute type) {
		super(value, kind, type);
	}

	/**
	 * The value method. Given an processinstance, a list of audit trail
	 * entries and the number of the `current' audit trail entry, give the
	 * value of this attribute as a number.
	 *
	 * @param pi The current process instance.
	 * @param ates The list with audit trail entries of pi.
	 * @param ateNr The number of the current ate in ates.
	 *
	 * @return The value of this attribute given the context. That is, te
	 * value of the literal or the value of the attribute of the current ate.
	 *
	 * @throw AttributeNoValueException
	 * @throw ParseAttributeException
	 */
	public float value(ProcessInstance pi, LinkedList ates,
			int ateNr) throws AttributeNoValueException, ParseAttributeException {

		float result = 0;
		String stringValue = "";

		// For the emty list, the value is 0. In principle, this value is
		// never computed because the formula results in some standard
		// value for the empty list.

		if (ateNr < ates.size()) {
			// If the list is not empty, the attribute can be `valuated'.

			switch (this.getKind()) {

				case Attribute.LITERAL: {
					// Just a constant, it should be parseble because it is
					// pared by the LTLParser, but nonetheless, it is tried.

					try {

						stringValue = this.getValue();
						result = (new Float(stringValue)).floatValue();

					} catch (NumberFormatException e) {

						throw new ParseAttributeException(stringValue,
								this);

					}
					;
				}
				break;

				case Attribute.ATTRIBUTE: {
					// Get the value of this attribute given the context

					if (this.getScope() == Attribute.PI) {
						// Get the value out of pi

						Map data = pi.getAttributes();

						if (data.containsKey(
								this.getAttributeId().substring(3))) {

							try {

								stringValue = (String) data.get(
											  this.getAttributeId().substring(3));
								result = (new Float(stringValue)).floatValue();

							} catch (NumberFormatException e) {

								throw new ParseAttributeException(
										stringValue, this);

							}
							;

						} else {
							// No dataelement

							throw new AttributeNoValueException(pi, ateNr,
									this);
						}
						;
					} else {
						// scope is ATE, so get the value of the ate

						AuditTrailEntry ate = (AuditTrailEntry) ates.get(ateNr);
						String attrName = this.getAttributeId().substring(4);

						if (attrName.equals("WorkflowModelElement")) {

							stringValue = ate.getElement();

						} else if (attrName.equals("EventType")) {

							stringValue = ate.getType();

						} else if (attrName.equals("Originator")) {

							stringValue = ate.getOriginator();

						} else if (attrName.equals("Timestamp")) {
							if (ate.getTimestamp() == null) {
								throw new AttributeNoValueException(pi, ateNr,
										this);
							} else {
								stringValue = ate.getTimestamp().toString();
							}

						} else {
							// No standard element, get the data

							Map data = ate.getAttributes();

							if (data.containsKey(attrName)) {

								stringValue = (String) data.get(
											  attrName);

							} else {
								// No dataelement

								throw new AttributeNoValueException(pi, ateNr,
										this);
							}
							;
						}
						;

						try {

							result = (new Float(stringValue)).floatValue();

						} catch (NumberFormatException e) {

							throw new ParseAttributeException(
									stringValue, this);

						}
						;
						} ;
					}
					; break;
				}
				;
			}
			;

			return result;
		}

	}
