/**
 *
 */
package org.processmining.framework.models.recommendation.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.recommendation.RecommendationProvider;
import org.processmining.framework.models.recommendation.RecommendationQuery;
import org.processmining.framework.models.recommendation.RecommendationResult;
import org.processmining.framework.remote.ServiceHandler;
import org.processmining.framework.ui.Message;
import org.processmining.framework.models.recommendation.net.client.RestartRequest;
import org.processmining.framework.ui.MainUI;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class RecommendationServiceHandler implements ServiceHandler {

	protected static String hostId;
	protected RecommendationQueryMarshal queryMarshal;
	protected RecommendationResultMarshal resultMarshal;
	protected RecommendationCompletedTraceMarshal traceMarshal;
	protected RecommendationRestartMarshal restartMarshal;

	static {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			// Get IP Address
			byte[] ipAddr = addr.getAddress();
			// Get hostname
			String hostname = addr.getHostName();
			// Assemble host ID
			hostId = "";
			for (int i = 0; i < ipAddr.length; i++) {
				hostId += ipAddr[i];
				if ((i + 1) < ipAddr.length) {
					hostId += ".";
				}
			}
			hostId += " (" + hostname + ")";
		} catch (UnknownHostException e) {
		}
	}

	protected RecommendationProvider localProvider;

	public RecommendationServiceHandler(RecommendationProvider provider) {
		localProvider = provider;
		queryMarshal = new RecommendationQueryMarshal();
		resultMarshal = new RecommendationResultMarshal();
		traceMarshal = new RecommendationCompletedTraceMarshal();
		restartMarshal = new RecommendationRestartMarshal();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.remote.ServiceHandler#handleRequest(java.
	 * io.Reader, java.io.Writer)
	 */
	public void handleRequest(BufferedReader in, PrintWriter out)
			throws IOException {
		// some helpers and var initializations
		String received = null;
		// initiate session
		writeLine(out, RecommendationQueryProtocol.HELO_SERVER_PROLOG);
		writeLine(out, "RecommendationQueryService on host " + hostId);
		writeLine(out, RecommendationQueryProtocol.HELO_SERVER_EPILOG);
		while ((received = in.readLine()) != null) {
			// wait for client request prolog
			if (received
					.equals(RecommendationQueryProtocol.CLIENT_REQUEST_XML_PROLOG) == true) {
				// handle recommendation request from client
				handleRecommendationRequest(in, out);
				// exit to proceed to goodbye
				break;
			} else if (received
					.equals(RecommendationQueryProtocol.CLIENT_TRANSMIT_TRACE_PROLOG) == true) {
				// handle recommendation request from client
				handleCompletedExecution(in, out);
				// exit to proceed to goodbye
				break;
			} else if (received
					.equals(RecommendationQueryProtocol.CLIENT_REQUEST_RESTART_PROLOG) == true) {
				// handle recommendation request from client
				handleRestartRequest(in, out);
				// exit to proceed to goodbye
				break;
			} else if (received
					.equals(RecommendationQueryProtocol.CLIENT_REQUEST_CLOSE) == true) {
				// handle recommendation request from client
				handleCloseRequest(in, out);
				// exit to proceed to goodbye
				break;
			}

		}
		// wait for goodbye (i.e. all right received)
		while ((received = in.readLine())
				.equals(RecommendationQueryProtocol.CLIENT_GOODBYE) == false) {
			// wait for client goodbye
		}
		// send own goodbye and terminate
		writeLine(out, RecommendationQueryProtocol.SERVER_GOODBYE);
		// done.
	}

	protected void handleRecommendationRequest(BufferedReader in,
			PrintWriter out) throws IOException {
		String received = null;
		StringBuilder queryXml = new StringBuilder();
		String resultXml = null;
		// receive query XML string
		while ((received = in.readLine())
				.equals(RecommendationQueryProtocol.CLIENT_REQUEST_XML_EPILOG) == false) {
			queryXml.append(received);
			queryXml.append('\n'); // add proper newline terminator
		}
		// signal receipt
		writeLine(out, RecommendationQueryProtocol.SERVER_REQUEST_XML_RECEIVED);
		// process query
		RecommendationQuery query;
		RecommendationResult result;
		// unmarshal query from XML
		try {
			query = queryMarshal.unmarshal(queryXml.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Message.add("Exception thrown when unmarshalling your query:"
					+ e.toString() + " ", Message.ERROR);
			sendError(out, "Exception thrown when unmarshalling your query:", e);
			return; // give up
		}
		// process query
		try {
			result = localProvider.getRecommendation(query);
		} catch (Exception e) {
			e.printStackTrace();
			Message.add("Exception thrown processing your query:"
					+ e.toString(), Message.ERROR);
			sendError(out, "Exception thrown processing your query:", e);
			return; // give up
		}
		// marshal result to XML
		try {
			resultXml = resultMarshal.marshal(result);
		} catch (Exception e) {
			e.printStackTrace();
			Message.add(
					"Exception thrown when marshalling the result to your query:"
							+ e.toString(), Message.ERROR);
			sendError(
					out,
					"Exception thrown when marshalling the result to your query:",
					e);
			return; // give up
		}
		// signal success
		writeLine(out, RecommendationQueryProtocol.SERVER_RESPONSE_POSITIVE);
		// send result (sandwiched in prolog & epilog)
		writeLine(out, RecommendationQueryProtocol.SERVER_RESULT_PROLOG);
		writeLine(out, resultXml);
		writeLine(out, RecommendationQueryProtocol.SERVER_RESULT_EPILOG);
	}

	protected void handleCompletedExecution(BufferedReader in, PrintWriter out)
			throws IOException {
		String received = null;
		StringBuilder traceXml = new StringBuilder();
		// receive completed trace XML string
		while ((received = in.readLine())
				.equals(RecommendationQueryProtocol.CLIENT_TRANSMIT_TRACE_EPILOG) == false) {
			traceXml.append(received);
			traceXml.append('\n'); // add proper newline terminator
		}
		// signal receipt
		writeLine(out,
				RecommendationQueryProtocol.SERVER_TRANSMIT_TRACE_RECEIVED);
		// unmarshal and handle completed trace in
		// local recommendation provider implementation
		try {
			ProcessInstance trace = traceMarshal.unmarshal(traceXml.toString());
			localProvider.handleCompletedExecution(trace);
		} catch (Exception e) {
			sendError(
					out,
					"Exception thrown when unmarshalling the completed trace which has been transmitted:",
					e);
			e.printStackTrace();
		}
	}

	protected void handleRestartRequest(BufferedReader in, PrintWriter out)
			throws IOException {
		String received = null;
		StringBuilder traceXml = new StringBuilder();
		// receive completed trace XML string
		while ((received = in.readLine())
				.equals(RecommendationQueryProtocol.CLIENT_REQUEST_RESTART_EPILOG) == false) {
			traceXml.append(received);
			traceXml.append('\n'); // add proper newline terminator
		}
		// signal receipt
		writeLine(out,
				RecommendationQueryProtocol.SERVER_REQUEST_RESTART_RECEIVED);
		// unmarshal and handle completed trace in
		// local recommendation provider implementation
		try {
			RestartRequest request = restartMarshal.unmarshal(traceXml
					.toString());
			localProvider.requestRestart(request.getContributor(), request
					.getScale());
		} catch (Exception e) {
			sendError(
					out,
					"Exception thrown when unmarshalling the completed trace which has been transmitted:",
					e);
			e.printStackTrace();
		}
	}

	protected void handleCloseRequest(BufferedReader in, PrintWriter out)
			throws IOException {
		String received = null;
		StringBuilder traceXml = new StringBuilder();
		// receive completed trace XML string
		// signal receipt
		writeLine(out,
				RecommendationQueryProtocol.SERVER_REQUEST_CLOSE_RECEIVED);
		// unmarshal and handle completed trace in
		// local recommendation provider implementation
		try {
			localProvider.requestClose();
		} catch (Exception e) {
			sendError(
					out,
					"Exception thrown when unmarshalling the completed trace which has been transmitted:",
					e);
			e.printStackTrace();
		}
	}

	protected void sendError(PrintWriter out, String reason, Exception e)
			throws IOException {
		writeLine(out, RecommendationQueryProtocol.SERVER_RESPONSE_NEGATIVE);
		writeLine(out, reason);
		writeLine(out, e.getMessage());
		writeLine(out, e.toString());
		e.printStackTrace(out);
		writeLine(out, " ...giving up and closing connection!");
		writeLine(out, RecommendationQueryProtocol.SERVER_GOODBYE);
	}

	protected void writeLine(Writer out, String line) throws IOException {
		out.write(line + "\r\n");
		out.flush();
	}

}
