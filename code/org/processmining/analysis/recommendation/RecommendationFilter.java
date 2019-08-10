/**
 *
 */
package org.processmining.analysis.recommendation;

import org.processmining.framework.models.recommendation.Recommendation;
import org.processmining.framework.models.recommendation.RecommendationQuery;
import org.processmining.framework.models.recommendation.RecommendationResult;

/**
 * Provides a static method for filtering a recommendation result to only
 * contain those recommendations that satisfy a recommendation filter provided
 * by the recommendation query.
 * <p>
 * Empty query filters, or filter fields, are ignored gracefully.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class RecommendationFilter {

	/**
	 * Filters a recommendation result based on the filter attributes provided
	 * in the respective recommendation query.
	 * 
	 * @param query
	 *            The recommendation query containing the filter specification.
	 * @param result
	 *            The recommendation result to be filtered.
	 * @return The filtered recommendation result (containing only
	 *         recommendations which have passed the query's filter).
	 */
	public static RecommendationResult filter(RecommendationQuery query,
			RecommendationResult result) {
		RecommendationResult filteredResult = new RecommendationResult(query
				.getId(), result.getPartialTraceValue());
		for (Recommendation rec : result) {
			if (RecommendationFilter.accept(query, rec) == true) {
				filteredResult.add(rec);
			}
		}
		return filteredResult;
	}

	/**
	 * Helper method which decides for one recommendation, whether it passes the
	 * query's filter.
	 * 
	 * @param query
	 *            The query containing the filter attributes.
	 * @param rec
	 *            Recommendation to be filtered.
	 * @return Whether the recommendation provided passes the filter.
	 */
	public static boolean accept(RecommendationQuery query, Recommendation rec) {
		// apply task name filter
		if (query.getFilterTasks() != null
				&& query.getFilterTasks().contains(rec.getLogEvent()) == false) {
			return false;
		}
		// if no users,roles or groups are specified in the recommendation,
		// then let everything trough
		if ((rec.getGroups() == null || rec.getGroups().isEmpty())
				&& (rec.getRoles() == null || rec.getRoles().isEmpty())
				&& (rec.getUsers() == null || rec.getUsers().isEmpty())) {
			return true;
		}

		// Resource filters: these are OR-concatenated, i.e. either at least one
		// group, role, or user specified must overlap between query and
		// recommendation
		// for the latter to be accepted by the filter.
		//
		// check groups filter
		if (query.getFilterGroups() != null && rec.getGroups() != null) {
			for (String group : rec.getGroups()) {
				if (query.getFilterGroups().contains(group)) {
					// one of the recommended groups should go through the
					// filter
					return true; // resource filter satisfied!
				}
			}
		}
		// check roles filter
		if (query.getFilterRoles() != null && rec.getRoles() != null) {
			for (String role : rec.getRoles()) {
				if (query.getFilterRoles().contains(role)) {
					// one of the recommended roles should go through the filter
					return true; // resource filter satisfied!
				}
			}
		}
		// check users filter
		if (query.getFilterUsers() != null && rec.getUsers() != null) {
			for (String user : rec.getUsers()) {
				if (query.getFilterUsers().contains(user)) {
					// one of the recommended users should go through the filter
					return true; // resource filter satisfied!
				}
			}
		}
		// resource filter not satisfied, i.e. rejected
		return false;
	}

}
