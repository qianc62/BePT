/**
 *
 */
package org.processmining.framework.models.recommendation.compat;

import org.processmining.framework.log.LogFilter;
import org.processmining.framework.models.recommendation.RecommendationQuery;

/**
 * This class provides a wrapper around a ProM log filter, which enables it to
 * be applied to recommendation queries as well.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class RecommendationQueryFilter {

	protected LogFilter filter;

	/**
	 * Creates a new log filter wrapper
	 * 
	 * @param aFilter
	 *            the log filter to be wrapped
	 */
	public RecommendationQueryFilter(LogFilter aFilter) {
		filter = aFilter;
	}

	/**
	 * Filters the audit trail inside a recommendation query with the wrapped
	 * log filter. This method modifies the recommendation query parameter
	 * itself!
	 * 
	 * @param aQuery
	 *            recommendation query to be filtered
	 * @return the parameter recommendation query, filtered
	 */
	public RecommendationQuery filter(RecommendationQuery aQuery, String process) {
		filter.filter(aQuery.toProcessInstance(process));
		return aQuery;
	}

}
