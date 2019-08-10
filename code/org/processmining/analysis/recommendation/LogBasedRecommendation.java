package org.processmining.analysis.recommendation;

import java.io.*;

import javax.swing.*;

import org.processmining.framework.log.*;
import org.processmining.framework.log.rfb.*;
import org.processmining.framework.models.recommendation.*;
import org.processmining.framework.ui.*;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class LogBasedRecommendation extends SingleProcessAcceptor implements
		RecommendationProvider {

	private String process;
	private LogBasedRecommendationUI ui;
	private BufferedLogReader log;

	public LogBasedRecommendation() {
	}

	/**
	 * analyse
	 * 
	 * @param log
	 *            LogReader
	 * @param startServerImmediately
	 *            boolean
	 * @return JComponent
	 * @todo Implement this
	 *       org.processmining.analysis.recommendation.basic.SingleProcessAcceptor
	 *       method
	 */
	protected JComponent analyse(BufferedLogReader log,
			boolean startServerImmediately) {
		this.process = log.getProcess(0).getName();
		this.log = log;
		ui = new LogBasedRecommendationUI(log, this, startServerImmediately);
		return ui;
	}

	public String getHtmlDescription() {
		return "Generates recommendations, based on a selected process instance scale and recommendation contributor";
	}

	public String getName() {
		return "Log based recommendations";
	}

	public RecommendationResult getRecommendation(RecommendationQuery query)
			throws IOException, Exception {
		ui.writeQuery(query);
		// Message.add("received query with id " + query.getId());
		RecommendationResult result = ui.getContributor()
				.generateRecommendations(query, process);
		result = RecommendationFilter.filter(query, result);
		// Message.add("query " + query.getId() + " answered:");
		ui.writeResult(result);
		// return RecommendationFilter.filter(query,result);
		return result;
	}

	public void signalPickedResult(RecommendationResult recommendationResult,
			int _int) {
	}

	public void signalPickedResult(RecommendationResult result,
			Recommendation picked) {
		signalPickedResult(result, (picked == null ? -1 : result
				.indexOf(picked)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.recommendation.RecommendationProvider
	 * #handleCompletedExecution
	 * (org.processmining.framework.log.ProcessInstance)
	 */
	public void handleCompletedExecution(ProcessInstance instance)
			throws Exception {

		ProcessInstanceImpl pi = ((ProcessInstanceImpl) instance);
		pi.setProcess(log.getProcess(0).getName());
		if (log.getLogData().addProcessInstance(log.getProcess(0), pi)) {
			// successfully added this process instance to the log
			ui.getContributor().initialize(log, ui.getContributor().getScale());
		}

		Message.add("Added instance " + instance.getProcess() + "."
				+ instance.getName() + " to log, which now contains "
				+ log.numberOfInstances() + " instances");
		// System.err.println("TODO: implement handling completed process instances / traces in LogBasedRecommendation class!");
	}

	public void requestRestart(String contributor, String scale)
			throws Exception {
		ui.requestRestart(contributor, scale);
	}

	public void requestClose() throws Exception {
		ui.closeDown();
		if (ui.shouldKillProM()) {
			// Added due to interface change. This method can be called by a
			// recommendation requestor and the result should
			// be that the application closes down.
			// write a message to both the message bar and to the system prompt
			Message
					.add("Closed the application as requested by the recommendation service.");
			System.out
					.println("Closed the application as requested by the recommendation service.");
			// Ask ProM to quit
			MainUI.getInstance().quit();
		}
	}

}
