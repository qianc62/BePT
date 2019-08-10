package org.processmining.analysis.history;

import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;

/**
 * Enhances the input log with history information, i.e., which events were
 * executed how many times in the current state of the process. This information
 * is recorded in dedicated data attributes for each audit trail entry. <br>
 * The purpose of this enhancement is enable the analysis of the execution log
 * to history-based formalisms.
 * 
 * @author Anne Rozinat
 */
public class HistoryAnalysisPlugin implements AnalysisPlugin {

	/**
	 * Specify the name of the plugin.
	 * 
	 * @return The name of the plugin.
	 */
	public String getName() {
		return "Enhance Log with History";
	}

	/**
	 * Provide user documentation for the plugin.
	 * 
	 * @return The Html body of the documentation page.
	 */
	public String getHtmlDescription() {
		return ("<h1>"
				+ getName()
				+ "</h1>"
				+ "This plugin adds counting measures to the data part of each audit trail entry. "
				+ "For example, if a log trace contains the sequence ABAC, then there will be a new data attribute \"#A\" = \"1\", \"#B\" = \"0\", and \"#C\" = \"0\" for log event B, "
				+ "two new data attributes \"#A\" = \"1\", \"#B\" = \"1\", and \"#C\" = \"0\" for the second log event A, and "
				+ "two new data attributes \"#A\" = \"2\", \"#B\" = \"1\", and \"#C\" = \"0\" for log event C etc. "
				+ "So, the history is captured without including the current log event itself. <BR>" + "The purpose of this plugin is to enable the analysis of the history via data mining techniques, e.g., in the Decision Miner.");
	}

	/**
	 * Define the input items necessary for the application of the plugin to
	 * offer its functionality to the user only in the right context. The
	 * ConformanceChecker analysis plugin requires a Petri net and a log file to
	 * evaluate their correspondence.
	 * 
	 * @return An array with an AnalysisInputItem that accepts a ProvidedObject
	 *         having a LogReader and a PetriNet.
	 */
	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = {
		/* newly define an analysis input item .. */
		new AnalysisInputItem("Log") {
			// .. including the accepts method, which actually evaluates
			// the validity of the context provided
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasLogReader = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof LogReader) {
						hasLogReader = true;
					}
				}
				// context needs to provid log
				return hasLogReader;
			}
		} };
		return items;
	}

	/**
	 * Define the procedure that is called automatically as soon as the plugin
	 * is invoked by the ProM tool.
	 * 
	 * @param analysisInputItemArray
	 *            The list of input items necessary to carry out the analysis
	 *            provided by the plugin.
	 * @return The JComponent to be displayed to the user within the plugin
	 *         window.
	 */
	public JComponent analyse(AnalysisInputItem[] analysisInputItemArray) {
		AnalysisInputItem PNLog = analysisInputItemArray[0];

		Object[] o = PNLog.getProvidedObjects()[0].getObjects();
		LogReader logReader = null;
		// since the plugin can only be invoked by the ProM tool, if the accepts
		// method
		// of its AnalysisInputItem (deliverable by getInputItems) evaluates to
		// true,
		// we can be sure that all the required arguments are passed over
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				logReader = (LogReader) o[i];
			}
		}

		// write history data attributes
		// int[] emptyMask = new int[0]; // no instances should be discarded
		// LogReader historyLog = logReader.clone(emptyMask);
		// LogEvents allATEs = historyLog.getLogSummary().getLogEvents();
		LogEvents allATEs = logReader.getLogSummary().getLogEvents();

		// Iterator<ProcessInstance> allInstances =
		// historyLog.instanceIterator();
		Iterator<ProcessInstance> allInstances = logReader.instanceIterator();
		while (allInstances.hasNext()) {
			ProcessInstance currentPI = allInstances.next();
			AuditTrailEntryList entries = currentPI.getAuditTrailEntryList();
			int index = 0;
			AuditTrailEntry current;

			// init counting measures
			HashMap<String, Integer> countingMeasures = new HashMap<String, Integer>();
			Iterator<LogEvent> allAteIt = allATEs.iterator();
			while (allAteIt.hasNext()) {
				LogEvent currentATE = allAteIt.next();
				countingMeasures.put(currentATE.getModelElementName()
						+ currentATE.getEventType(), 0);
			}

			try {
				// walk through the process instance and perform the actual
				// filtering task
				while (index < entries.size()) {
					// get ate at current index position
					current = entries.get(index);

					// write current counting measures to data part
					Iterator<String> soFarCounted = countingMeasures.keySet()
							.iterator();
					while (soFarCounted.hasNext()) {
						String ateName = soFarCounted.next();
						int ateCount = countingMeasures.get(ateName);
						current.setAttribute("#" + ateName, "" + ateCount);
					}

					// replace to make change persistent
					entries.replace(current, index);

					// update counting measures for next ATE
					String name = current.getName();
					String type = current.getType();
					if (countingMeasures.containsKey(name + type) == true) {
						// just increment if this ate was already counted at
						// least once
						int count = countingMeasures.get(name + type);
						count = count + 1;
						countingMeasures.put(name + type, count);
					} else {
						// should never happen as counting measures are
						// initialized
					}

					// move to next event in log trace
					index = index + 1;
				}
			} catch (Exception ex) {
				Message
						.add("An error occurred during filtering process instance "
								+ currentPI.getName());
				ex.printStackTrace();
			}
		}

		// return new HistoryAnalyisResult(this, analysisInputItemArray,
		// historyLog);
		return new HistoryAnalyisResult(this, analysisInputItemArray, logReader);
	}
}