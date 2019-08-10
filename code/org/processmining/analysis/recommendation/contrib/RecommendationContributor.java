/**
 *
 */
package org.processmining.analysis.recommendation.contrib;

import java.util.List;

import org.processmining.analysis.log.scale.ProcessInstanceScale;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.filter.AbstractLogFilter;
import org.processmining.framework.models.logabstraction.LogAbstraction;
import org.processmining.framework.models.logabstraction.LogAbstractionFactory;
import org.processmining.framework.models.recommendation.RecommendationQuery;
import org.processmining.framework.models.recommendation.RecommendationResult;
import org.processmining.framework.models.recommendation.compat.RecommendationQueryFilter;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.log.LogFilter;

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
public abstract class RecommendationContributor implements Plugin {

	public RecommendationContributor() {
		super();
	}

	/**
	 * The scale which is used to calculate the weight of this contributor based
	 * on the contained process instances' weights as their sum.
	 */
	protected ProcessInstanceScale scale;

	/**
	 * @return The scale used for weighing the contained process instances.
	 */
	public ProcessInstanceScale getScale() {
		return scale;
	}

	/**
	 * Sets the scale used for weighing this contributor based on the contained
	 * process instances.
	 * 
	 * @param aScale
	 *            a process instance scale.
	 */
	public void setScale(ProcessInstanceScale aScale) {
		scale = aScale;
	}

	private RecommendationQueryFilter filter;

	public void initialize(LogReader log, ProcessInstanceScale scale)
			throws NoFactoryException {
		if (logAbstractionFactory == null) {
			throw new NoFactoryException();
		} else {
			scale.initializeScale(log);
			logAbstractions = logAbstractionFactory.getAbstractions(log, scale);
		}
		this.scale = scale;
		LogFilter filter = log.getLogFilter();
		if (filter == null) {
			// no filtering, i.e. defaultLogFilter with INCLUDE as setting
			filter = new DefaultLogFilter(DefaultLogFilter.INCLUDE);
		}
		this.filter = new RecommendationQueryFilter(new AbstractLogFilter(
				"Filter for filtering queries", filter));
	}

	/**
	 * logAbstractor that takes care of the abstraction of the entire log, as
	 * well as any offered query
	 */
	protected List<LogAbstraction> logAbstractions;

	/**
	 * logAbstractor that takes care of the abstraction of the entire log, as
	 * well as any offered query
	 */
	protected LogAbstractionFactory logAbstractionFactory;

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
			RecommendationQuery query, String process) {
		return generateFilteredRecommendations(filter.filter(query, process),
				process);
	}

	public abstract RecommendationResult generateFilteredRecommendations(
			RecommendationQuery query, String process);

	/**
	 * Gets the name of this plugin. Implementing classes should use this method
	 * to return their own name.
	 * 
	 * @return the name of this plugin
	 */
	public abstract String getName();

	/**
	 * Gets a description of this plugin in HTML. The string returned by this
	 * method should only contain the contents of the body of the html page, so
	 * the <code>html</code>, <code>head</code> and <code>body</code> tags
	 * should <b>not</b> be used. This HTML page is displayed in the help system
	 * or as context sensitive help.
	 * 
	 * The HTML body can be stored in an external file in the
	 * /lib/documentation/ sub folder and in this case it can be obtained by
	 * calling <code>PluginDocumentationLoader.load(this)</code>
	 * 
	 * @return a description of this plugin in HTML
	 */
	public abstract String getHtmlDescription();

}
