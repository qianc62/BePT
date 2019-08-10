/*
 * Created on Jun 3, 2005
 *
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * (c) 2005 Technische Universiteit Eindhoven, Christian W. Guenther
 * all rights reserved
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
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class OriginatorFilter implements EventFilter {

	boolean regex = false;
	boolean negated = true;
	String pattern = null;

	protected OriginatorFilter() {
		// disable instantiation without factory
	}

	/**
	 * constructor
	 * 
	 * @param anOriginator
	 *            the pattern to match
	 * @param isRegex
	 *            whether the pattern is to be fully matched (false) or in fact
	 *            a regular expression (true)
	 */
	protected OriginatorFilter(String anOriginator, boolean isRegex,
			boolean isNegated) {
		pattern = anOriginator;
		regex = isRegex;
		negated = isNegated;
	}

	/**
	 * static factory method; retrieves a filter matching the complete string
	 * 
	 * @param anOriginator
	 * @return
	 */
	public static OriginatorFilter equal(String anOriginator) {
		return new OriginatorFilter(anOriginator, false, false);
	}

	/**
	 * static factory method; retrieves a filter matching the given regular
	 * expression
	 * 
	 * @param aPattern
	 * @return
	 */
	public static OriginatorFilter equalRegex(String aPattern) {
		return new OriginatorFilter(aPattern, true, false);
	}

	/**
	 * static factory method; retrieves a filter matching the complete string
	 * 
	 * @param anOriginator
	 * @return
	 */
	public static OriginatorFilter notEqual(String anOriginator) {
		return new OriginatorFilter(anOriginator, false, true);
	}

	/**
	 * static factory method; retrieves a filter matching the given regular
	 * expression
	 * 
	 * @param aPattern
	 * @return
	 */
	public static OriginatorFilter notEqualRegex(String aPattern) {
		return new OriginatorFilter(aPattern, true, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.dmcscanning.abstractlog.EventFilter#matches(
	 * org.processmining.framework.log.AuditTrailEntry)
	 */
	public boolean matches(AuditTrailEntry entry) {
		if (regex == true) {
			return (entry.getOriginator().matches(pattern) ^ negated);
		} else {
			return (entry.getOriginator().equals(pattern) ^ negated);
		}
	}

}
