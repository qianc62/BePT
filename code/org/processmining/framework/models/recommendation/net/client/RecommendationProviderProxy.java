/**
 *
 */
package org.processmining.framework.models.recommendation.net.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.recommendation.Recommendation;
import org.processmining.framework.models.recommendation.RecommendationProvider;
import org.processmining.framework.models.recommendation.RecommendationQuery;
import org.processmining.framework.models.recommendation.RecommendationResult;
import org.processmining.framework.models.recommendation.net.RecommendationCompletedTraceMarshal;
import org.processmining.framework.models.recommendation.net.RecommendationQueryMarshal;
import org.processmining.framework.models.recommendation.net.RecommendationQueryProtocol;
import org.processmining.framework.models.recommendation.net.RecommendationResultMarshal;
import org.processmining.framework.models.recommendation.net.RecommendationRestartMarshal;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class RecommendationProviderProxy implements RecommendationProvider {

	protected Socket socket;
	protected String host;
	protected int port;
	protected RecommendationQueryMarshal queryMarshal;
	protected RecommendationResultMarshal resultMarshal;
	protected RecommendationCompletedTraceMarshal traceMarshal;
	protected RecommendationRestartMarshal restartMarshal;

	/**
	 *
	 */
	public RecommendationProviderProxy(String aHost, int aPort) {
		host = aHost;
		port = aPort;
		socket = null;
		queryMarshal = new RecommendationQueryMarshal();
		resultMarshal = new RecommendationResultMarshal();
		traceMarshal = new RecommendationCompletedTraceMarshal();
		restartMarshal = new RecommendationRestartMarshal();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.recommendation.RecommendationProvider
	 * #getRecommendation
	 * (org.processmining.framework.models.recommendation.RecommendationQuery)
	 */
	public RecommendationResult getRecommendation(RecommendationQuery query)
			throws Exception, IOException {
		// connect to socket
		socket = new Socket(host, port);
		socket.setKeepAlive(true);
		socket.setSoTimeout(0);
		// connect reader and writer
		BufferedReader in = new BufferedReader(new InputStreamReader(socket
				.getInputStream()));
		PrintWriter out = new PrintWriter(socket.getOutputStream());
		String line = in.readLine();
		if (line.equals(RecommendationQueryProtocol.HELO_SERVER_PROLOG) == false) {
			throw new Exception(
					"Fatal error: Remote is no recommendation server! (HELO mismatch!)");
		}
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.HELO_SERVER_EPILOG) == false) {
			// wait for server HELO epilog so we can start request
		}
		// write request
		String request = queryMarshal.marshal(query);
		writeLine(out, RecommendationQueryProtocol.CLIENT_REQUEST_XML_PROLOG);
		writeLine(out, request);
		writeLine(out, RecommendationQueryProtocol.CLIENT_REQUEST_XML_EPILOG);
		// wait for receipt confirmation
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.SERVER_REQUEST_XML_RECEIVED) == false) {
			// wait for receipt
		}
		// wait for outcome
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.SERVER_RESPONSE_POSITIVE) == false) {
			if (line
					.equalsIgnoreCase(RecommendationQueryProtocol.SERVER_RESPONSE_NEGATIVE)) {
				// error. print message to stderr...
				String errorMessage = "Remote server error:\n";
				System.err.println("Remote server at " + host + ":" + port
						+ " reported a fatal error:");
				while ((line = in.readLine())
						.equalsIgnoreCase(RecommendationQueryProtocol.SERVER_GOODBYE) == false) {
					System.err.println(line);
					errorMessage += line + "\n";
				}
				socket.close();
				throw new Exception(errorMessage);
			}
		}
		// ok, it seems to have worked..
		// wait for response prolog
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.SERVER_RESULT_PROLOG) == false) {
			// wait for result prolog
		}
		// collect result message
		StringBuilder resultStringBuilder = new StringBuilder();
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.SERVER_RESULT_EPILOG) == false) {
			resultStringBuilder.append(line);
			resultStringBuilder.append('\n'); // fix newline char
		}
		// send goodbye
		writeLine(out, RecommendationQueryProtocol.CLIENT_GOODBYE);
		// wait for server goodbye
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.SERVER_GOODBYE) == false) {
			// wait for server goodbye
		}
		// close connection
		socket.close();
		socket = null;
		// unmarshal result and return it
		RecommendationResult result = resultMarshal
				.unmarshal(resultStringBuilder.toString());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.recommendation.RecommendationProvider
	 * #signalPickedResult
	 * (org.processmining.framework.models.recommendation.RecommendationResult,
	 * int)
	 */
	public void signalPickedResult(RecommendationResult result, int index) {
		// ignore for now
	}

	/**
	 * safe and convenient writing to socket character streams; internal helper
	 * method
	 * 
	 * @param out
	 * @param line
	 */
	protected void writeLine(PrintWriter out, String line) {
		out.write(line + "\r\n");
		out.flush();
	}

	public void signalPickedResult(RecommendationResult result,
			Recommendation picked) {
		signalPickedResult(result, (picked == null ? -1 : result
				.indexOf(picked)));
	}

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
			throws Exception {
		// connect to socket
		socket = new Socket(host, port);
		socket.setKeepAlive(true);
		socket.setSoTimeout(0);
		// connect reader and writer
		BufferedReader in = new BufferedReader(new InputStreamReader(socket
				.getInputStream()));
		PrintWriter out = new PrintWriter(socket.getOutputStream());
		String line = in.readLine();
		if (line.equals(RecommendationQueryProtocol.HELO_SERVER_PROLOG) == false) {
			throw new Exception(
					"Fatal error: Remote is no recommendation server! (HELO mismatch!)");
		}
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.HELO_SERVER_EPILOG) == false) {
			// wait for server HELO epilog so we can start request
		}
		// write completed trace
		String reqXML = restartMarshal.marshal(contributor, scale);
		writeLine(out,
				RecommendationQueryProtocol.CLIENT_REQUEST_RESTART_PROLOG);
		writeLine(out, reqXML);
		writeLine(out,
				RecommendationQueryProtocol.CLIENT_REQUEST_RESTART_EPILOG);
		// wait for receipt confirmation
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.SERVER_REQUEST_RESTART_RECEIVED) == false) {
			// wait for receipt
		}
		// send goodbye
		writeLine(out, RecommendationQueryProtocol.CLIENT_GOODBYE);
		// wait for server goodbye
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.SERVER_GOODBYE) == false) {
			// wait for server goodbye
		}
		// close connection
		socket.close();
		socket = null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.recommendation.RecommendationProvider
	 * #transmitCompletedExecution
	 * (org.processmining.framework.log.ProcessInstance)
	 */
	public void handleCompletedExecution(ProcessInstance instance)
			throws Exception {
		// connect to socket
		socket = new Socket(host, port);
		socket.setKeepAlive(true);
		socket.setSoTimeout(0);
		// connect reader and writer
		BufferedReader in = new BufferedReader(new InputStreamReader(socket
				.getInputStream()));
		PrintWriter out = new PrintWriter(socket.getOutputStream());
		String line = in.readLine();
		if (line.equals(RecommendationQueryProtocol.HELO_SERVER_PROLOG) == false) {
			throw new Exception(
					"Fatal error: Remote is no recommendation server! (HELO mismatch!)");
		}
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.HELO_SERVER_EPILOG) == false) {
			// wait for server HELO epilog so we can start request
		}
		// write completed trace
		String traceXml = traceMarshal.marshal(instance);
		writeLine(out, RecommendationQueryProtocol.CLIENT_TRANSMIT_TRACE_PROLOG);
		writeLine(out, traceXml);
		writeLine(out, RecommendationQueryProtocol.CLIENT_TRANSMIT_TRACE_EPILOG);
		// wait for receipt confirmation
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.SERVER_TRANSMIT_TRACE_RECEIVED) == false) {
			// wait for receipt
		}
		// send goodbye
		writeLine(out, RecommendationQueryProtocol.CLIENT_GOODBYE);
		// wait for server goodbye
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.SERVER_GOODBYE) == false) {
			// wait for server goodbye
		}
		// close connection
		socket.close();
		socket = null;
	}

	public void requestClose() throws Exception {
		socket = new Socket(host, port);
		socket.setKeepAlive(true);
		socket.setSoTimeout(0);
		// connect reader and writer
		BufferedReader in = new BufferedReader(new InputStreamReader(socket
				.getInputStream()));
		PrintWriter out = new PrintWriter(socket.getOutputStream());
		String line = in.readLine();
		if (line.equals(RecommendationQueryProtocol.HELO_SERVER_PROLOG) == false) {
			throw new Exception(
					"Fatal error: Remote is no recommendation server! (HELO mismatch!)");
		}
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.HELO_SERVER_EPILOG) == false) {
			// wait for server HELO epilog so we can start request
		}
		// write completed trace
		writeLine(out, RecommendationQueryProtocol.CLIENT_REQUEST_CLOSE);
		// wait for receipt confirmation
		while ((line = in.readLine())
				.equalsIgnoreCase(RecommendationQueryProtocol.SERVER_REQUEST_CLOSE_RECEIVED) == false) {
			// wait for receipt
		}
		// goodbye will not be received.
		// close connection
		socket.close();
		socket = null;

	}

}
