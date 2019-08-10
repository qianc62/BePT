/**
 *
 */
package org.processmining.framework.models.recommendation;

import java.io.IOException;

import org.processmining.framework.log.ProcessInstance;

/**
 * This abstract class provides a clean high-level interface for obtaining
 * recommendations for task scheduling in a process-aware information system.
 * Encoding of queries and results is performed in dedicated data container
 * classes.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public interface RecommendationProvider {

	/**
	 * <p>
	 * This is the main query method, which can be used to retrieve
	 * recommendations for specific queries. All communication and encoding of
	 * parameters can be assumed to be performed in subclasses implementing this
	 * interface.
	 * </p>
	 * <p>
	 * <b>Note:</b> This method may block for an unspecified time, including
	 * network transport and resolving the query on the provider side. Use this
	 * method in a separate thread if you wish your application to remain
	 * responsive.<br>
	 * Also note that the query can fail, both due to transmission errors (which
	 * will yield an <code>IOException</code>, and due to the provider's
	 * inability to resolve the query (i.e., an error occurred while handling
	 * the issued request, signaled by an ordinary <code>Exception</code> being
	 * thrown; or, the query did not yield any usable result, which results in a
	 * <code>null</code> response value.
	 * </p>
	 * 
	 * @param query
	 *            The query for recommendations.
	 * @return The resulting set of recommendations (correlated with the issued
	 *         query), or <code>null</code>, of no result could be obtained (may
	 *         also be empty result list).
	 * @throws Exception
	 *             Signals an error in the recommendation provider
	 *             implementation.
	 * @throws IOException
	 *             Signals an error due to network transport problems.
	 */
	public RecommendationResult getRecommendation(RecommendationQuery query)
			throws Exception, IOException;

	/**
	 * <p>
	 * This method may be used to let the recommendation provider know, which of
	 * the provided results has been picked by the requesting party finally. The
	 * provider will use this information for logging purposes, or to improve
	 * future results.
	 * </p>
	 * 
	 * @param result
	 *            The result set which has been delivered earlier on.
	 * @param index
	 *            Index of the result which has been picked for usage in the
	 *            result set. If the given index is negative, the semantics are
	 *            that the requesting party has picked neither of the provided
	 *            recommendations, i.e. ignored them or found them not useful.
	 */
	public void signalPickedResult(RecommendationResult result, int index);

	/**
	 * <p>
	 * This method may be used to let the recommendation provider know, which of
	 * the provided results has been picked by the requesting party finally. The
	 * provider will use this information for logging purposes, or to improve
	 * future results.
	 * </p>
	 * 
	 * @param result
	 *            The result set which has been delivered earlier on.
	 * @param pcked
	 *            The result which has been picked for usage from the result
	 *            set. If the given result is <code>null</code>, the semantics
	 *            are that the requesting party has picked neither of the
	 *            provided recommendations, i.e. ignored them or found them not
	 *            useful.
	 */
	public void signalPickedResult(RecommendationResult result,
			Recommendation picked);

	/**
	 * <p>
	 * This method transmits a completed process instance to the recommendation
	 * provider, so that it may update its internal data structures accordingly
	 * (providing it is log-based recommendation).
	 * 
	 * @param instance
	 *            the completed process instance to transmit
	 */
	public void handleCompletedExecution(ProcessInstance instance)
			throws Exception;

	/**
	 * <p>
	 * This method transmits a request to the server to restart with a new
	 * contributor and a new scale
	 * </p>
	 * 
	 * @param contributor
	 *            is the full classname of the contributor, as shown in the
	 *            recommendations.ini file of ProM
	 * @param scale
	 *            is the full classname of the contributor, as shown in the
	 *            scales.ini file of ProM
	 */
	public void requestRestart(String contributor, String scale)
			throws Exception;

	/**
	 * <p>
	 * This method transmits a request to the server to close.
	 * </p>
	 */
	public void requestClose() throws Exception;

}
