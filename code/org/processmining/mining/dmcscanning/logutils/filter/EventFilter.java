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
 * Generic filter template for extracting subsets of event logs
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public interface EventFilter {

	/**
	 * Determines, whether a given AuditTrailEntry matches this filter's
	 * conditions
	 * 
	 * @param entry
	 * @return
	 */
	public boolean matches(AuditTrailEntry entry);

}
