/**
 *
 */
package org.processmining.framework.models.recommendation.net;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class RecommendationQueryProtocol {

	public static final String PROTOCOL_VERSION = "2.0";

	public static final String HELO_SERVER_PROLOG = "HELO Welcome to recommendation service! (Protocol version "
			+ PROTOCOL_VERSION + ")";
	public static final String HELO_SERVER_EPILOG = "LIST The service will now take your questions.";

	public static final String CLIENT_REQUEST_XML_PROLOG = "RQOP Opening transmission of request.";
	public static final String CLIENT_REQUEST_XML_EPILOG = "RQCM Transmission of request completed.";

	public static final String SERVER_REQUEST_XML_RECEIVED = "RCVR Thank you, your request has been received.";

	public static final String SERVER_RESPONSE_NEGATIVE = "RRNK Your request could not be processed correctly.";
	public static final String SERVER_RESPONSE_POSITIVE = "RROK Your request has been processed correcty.";

	public static final String SERVER_RESULT_PROLOG = "RSOP Opening transmission of result.";
	public static final String SERVER_RESULT_EPILOG = "RSCM Transmission of result completed.";

	public static final String CLIENT_TRANSMIT_TRACE_PROLOG = "TRCO Opening transmission of trace.";
	public static final String CLIENT_TRANSMIT_TRACE_EPILOG = "TRCO Transmission of trace completed.";

	public static final String CLIENT_REQUEST_RESTART_EPILOG = "Starting request for a restart.";
	public static final String CLIENT_REQUEST_RESTART_PROLOG = "Finalizing request for a restart.";
	public static final String SERVER_REQUEST_RESTART_RECEIVED = "Thank you, your restart request has been received.";

	public static final String CLIENT_REQUEST_CLOSE = "Please kill yourself, you're useless anyway.";
	public static final String SERVER_REQUEST_CLOSE_RECEIVED = "OK, OK, I will kill myself shortly.";

	public static final String SERVER_TRANSMIT_TRACE_RECEIVED = "RCVT Thank you, your trace has been received.";

	public static final String CLIENT_GOODBYE = "CBYE Thank you. Goodbye!";
	public static final String SERVER_GOODBYE = "SBYE Goodbye!";

}
