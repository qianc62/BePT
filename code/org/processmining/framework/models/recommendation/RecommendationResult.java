/**
 *
 */
package org.processmining.framework.models.recommendation;

import java.util.ArrayList;

import org.processmining.framework.models.recommendation.net.RecommendationResultMarshal;

/**
 * Data structure for storing a result for requesting recommendation for
 * scheduling in a process management system. (based on an array list of
 * Recommendation objects)
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class RecommendationResult extends ArrayList<Recommendation> {

	protected String queryId;
	private double partialTraceValue;

	public double getPartialTraceValue() {
		return partialTraceValue;
	}

	/**
	 * Creates a new, empty recommendation result for the identified query
	 * 
	 * @param queryId
	 *            the unique ID of the query to which this is a result.
	 */
	public RecommendationResult(String queryId, double partialTraceValue) {
		this.queryId = queryId;
		this.partialTraceValue = partialTraceValue;
	}

	/**
	 * @return the unique ID of the query to which this is a result
	 */
	public String getQueryId() {
		return queryId;
	}

	public boolean add(Recommendation rec) {
		Recommendation cur;
		for (int i = this.size(); i > 0; i--) {
			cur = this.get(i - 1);
			if (rec.compareTo(cur) >= 0) {
				super.add(i, rec);
				return true;
			}
		}
		super.add(0, rec);
		return true;
	}

	public String toString() {
		try {
			return (new RecommendationResultMarshal()).marshal(this);
		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR";
		}
	}

}
