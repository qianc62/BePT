package org.processmining.analysis.pdm.recommendation;

import java.util.*;

import org.processmining.analysis.log.scale.*;
import org.processmining.framework.log.*;
import org.processmining.framework.log.filter.*;
import org.processmining.framework.models.logabstraction.*;
import org.processmining.framework.models.recommendation.*;
import org.processmining.framework.models.recommendation.compat.*;
import org.processmining.framework.plugin.*;
import org.processmining.analysis.recommendation.*;
import org.processmining.framework.models.pdm.*;

/**
 * An instance abstraction is an entity which, based on a specific subset of
 * information available to it, can calculate a set of recommendations for a
 * provided query.
 * <p>
 * Typically a recommendation contributor will cluster a set of somewhat
 * comparable process instances, and return recommendations based on their
 * common characteristics.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */

// I am not going to use contributors to keep it simple at first!
public class PDMRandomSelectionContributor implements Plugin {

	public PDMRandomSelectionContributor() {
		super();
	}

	/**
	 * This method calculates this instance's contributions to a specified
	 * query, i.e. the recommendations provided by this contributor.
	 * Recommendations returned by this method are supposed to be internally
	 * ordered, i.e. have respective confidences assigned which reflect their
	 * relative importance as intended by this contributor.
	 * 
	 * @param query
	 *            The query to which recommendations are inquired.
	 * @param process
	 *            The process name the query refers to
	 * @return A set of recommendations provided to the specified query, in a
	 *         <code>RecommendationResult</code> container.
	 */
	public RecommendationResult generateRecommendations(
			RecommendationQuery query, PDMModel model) {
		RecommendationResult result = null;
		return result;
	}

	/**
	 * Gets the name of this plugin. Implementing classes should use this method
	 * to return their own name.
	 * 
	 * @return the name of this plugin
	 */
	public String getName() {
		return "Random Selection";
	}

	/**
	 * 
	 * @return String
	 */
	public String getHtmlDescription() {
		return "This recommendation plugin randomly selects a PDM operation from the list of enabled operations.";
	}
}
