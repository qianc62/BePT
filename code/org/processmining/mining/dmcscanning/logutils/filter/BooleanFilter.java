/*
 * Created on Jun 3, 2005
 *
 * (c) 2005 Christian W. Guenther, all rights reserved.
 * 
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source 
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */
package org.processmining.mining.dmcscanning.logutils.filter;

import org.processmining.framework.log.AuditTrailEntry;

/**
 * BooleanFilter. composite event filter class, provides both boolean constants
 * as TRUE or FALSE, as allows for the successive composition of complex boolean
 * filter statements.
 * 
 * @author christian
 * 
 *         Christian W. Guenther (christian@deckfour.com)
 * 
 */
public class BooleanFilter implements EventFilter {

	static final int OP_AND = 0;
	static final int OP_OR = 1;
	static final int OP_XOR = 2;
	static final int OP_NEG = 3;
	static final int OP_TRUE = 4;
	static final int OP_FALSE = 5;

	int operator = 0;
	EventFilter[] operands = null;

	/**
	 * 
	 */
	protected BooleanFilter() {
		// no standard constructor - use factory methods!
	}

	/**
	 * protected constructor -> use factory methods for construction!
	 */
	protected BooleanFilter(int theOperator, EventFilter[] theOperands) {
		operator = theOperator;
		operands = theOperands;
	}

	/**
	 * factory methods
	 * 
	 * @return
	 */
	public static BooleanFilter constFalse() {
		return new BooleanFilter(BooleanFilter.OP_FALSE, null);
	}

	public static BooleanFilter constTrue() {
		return new BooleanFilter(BooleanFilter.OP_TRUE, null);
	}

	public static BooleanFilter negate(EventFilter x) {
		EventFilter set[] = { x };
		return new BooleanFilter(BooleanFilter.OP_NEG, set);
	}

	public static BooleanFilter xor(EventFilter a, EventFilter b) {
		EventFilter set[] = { a, b };
		return new BooleanFilter(BooleanFilter.OP_XOR, set);
	}

	public static BooleanFilter and(EventFilter ops[]) {
		return new BooleanFilter(BooleanFilter.OP_AND, ops);
	}

	public static BooleanFilter and(EventFilter a, EventFilter b) {
		EventFilter ops[] = { a, b };
		return new BooleanFilter(BooleanFilter.OP_AND, ops);
	}

	public static BooleanFilter or(EventFilter ops[]) {
		return new BooleanFilter(BooleanFilter.OP_OR, ops);
	}

	public static BooleanFilter or(EventFilter a, EventFilter b) {
		EventFilter ops[] = { a, b };
		return new BooleanFilter(BooleanFilter.OP_OR, ops);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.abstractlog.EventFilter#matches(
	 * org.processmining.framework.log.AuditTrailEntry)
	 */
	public boolean matches(AuditTrailEntry entry) {
		switch (operator) {
		case (BooleanFilter.OP_TRUE):
			return true;
		case (BooleanFilter.OP_FALSE):
			return false;
		case (BooleanFilter.OP_NEG):
			return !(operands[0].matches(entry));
		case (BooleanFilter.OP_XOR):
			return (operands[0].matches(entry) ^ operands[1].matches(entry));
		case (BooleanFilter.OP_AND):
			for (int i = 0; i < operands.length; i++) {
				if (operands[i].matches(entry) == false) {
					return false;
				}
			}
			return true;
		case (BooleanFilter.OP_OR):
			for (int i = 0; i < operands.length; i++) {
				if (operands[i].matches(entry) == true) {
					return true;
				}
			}
			return false;
		default:
			return true;
		}
	}

}
