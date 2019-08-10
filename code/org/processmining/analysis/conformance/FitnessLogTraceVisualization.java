/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.analysis.conformance;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.Message;

/**
 * Overrides the default visualization methods to show the selected log traces
 * instead of the model.
 * 
 * @author arozinat
 */
public class FitnessLogTraceVisualization extends DiagnosticPetriNet {

	/**
	 * Represents the visualization option of indicating the log events that
	 * have failed execution in the log trace. The public attribute is necessary
	 * because the writeToDot() method has a fixed interface.
	 */
	public boolean failedEventsOption = false;

	/**
	 * Create a diagnostic Petri net with log trace visualization.
	 * 
	 * @param net
	 *            the Petri net to be passed to super
	 * @param caseIDs
	 *            the trace IDs to be passed to super
	 */
	public FitnessLogTraceVisualization(PetriNet net, ArrayList caseIDs,
			DiagnosticLogReader replayedLog) {
		super(net, caseIDs, replayedLog);
	}

	/**
	 * Create a diagnostic Petri net with log trace visualization. The copy
	 * constructor is used to change the visualization but to keep all the
	 * diagnostic results.
	 * 
	 * @param copyTemplate
	 *            The Petri net containing all the diagnostic information that
	 *            should be preserved (to be passed to super).
	 */
	public FitnessLogTraceVisualization(DiagnosticPetriNet copyTemplate) {
		super(copyTemplate);
	}

	// /////// VISUALIZATION SECTION //////////

	/**
	 * Overrides the default visualization to show the log traces instead of the
	 * model (the default visualization of process instances is used
	 * internally).
	 * 
	 * @param bw
	 *            The writer used by the framework to create the temporary dot
	 *            file.
	 * @throws IOException
	 *             If writing to the writer fails.
	 * */
	public void writeToDot(Writer bw) throws IOException {
		try {
			// for one single trace being selected
			if (myReplayedLog.getSizeOfLog() == 1) {
				// if (currentlySelectedInstances.size() == 1) {
				writeDefaultVisualization(bw);
			}
			// for more than one trace being selected
			else {
				if (failedEventsOption == true) {
					writeDiagnosticLogVisualization(bw);
				} else {
					writeLogVisualization(bw);
				}
			}
		} catch (Exception ex) {
			Message.add("FitnessLogTraceVisualization has failed.\n"
					+ ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * Write the standard log trace visualization like implemented for the class
	 * ProcessInstance. Note that this is only used for a single trace being
	 * selected.
	 * 
	 * @param bw
	 *            The writer used by the framework to create the temporary dot
	 *            file.
	 * @throws IOException
	 *             If writing to the writer fails.
	 */
	private void writeDefaultVisualization(Writer bw) throws IOException {
		bw.write("digraph G {\nfontsize=\"8\"; \nremincross=true;\n");
		bw.write("fontname=\"Arial\";\nrankdir=\"TB\";\n");
		bw
				.write("edge [arrowsize=\"0.7\",fontname=\"Arial\",fontsize=\"8\"];\n");
		bw
				.write("node [height=\".2\",width=\".2\",fontname=\"Arial\",fontsize=\"8\"];\n");
		// get the only element from the list

		this.myReplayedLog.reset();
		DiagnosticLogTrace visualizedInstance = (DiagnosticLogTrace) this.myReplayedLog
				.next();
		// DiagnosticLogTrace visualizedInstance = (DiagnosticLogTrace)
		// currentlySelectedInstances.get(0);

		// get audit trail list as arraylist to cast the entries to
		// diagnostic audit trail entries afterwards
		Iterator currentATEListIterator = visualizedInstance
				.getProcessInstance().getAuditTrailEntryList().iterator();
		// write header
		bw
				.write("pidata [shape=\"box\",rank=\"source\",label=\"Process Instance Data:");
		Iterator it = visualizedInstance.getProcessInstance().getAttributes()
				.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			bw.write("\\n"
					+ key
					+ " = "
					+ (String) visualizedInstance.getProcessInstance()
							.getAttributes().get(key));
		}
		bw.write("\"];\n");
		// write log events
		int i = 0;
		while (currentATEListIterator.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) currentATEListIterator
					.next();
			DiagnosticAuditTrailEntry ateDiag = visualizedInstance
					.getAteDiagnostic(ate);
			// only mark failed events if respective option was chosen by user
			if (failedEventsOption == true
					&& (ateDiag.getFailedExecutionValue() == true)) {
				bw
						.write("ate"
								+ i
								+ " [shape=\"box\",style=\"filled\",fillcolor=\"orange1\",label=\""
								+ ate.getElement()
								+ "\\n"
								+ ate.getType()
								+ "\\n"
								+ (ate.getTimestamp() == null ? "" : ate
										.getTimestamp().toString()) + "\"];\n");
			} else {
				// write normally
				bw.write("ate"
						+ i
						+ " [shape=\"box\",label=\""
						+ ate.getElement()
						+ "\\n"
						+ ate.getType()
						+ "\\n"
						+ (ate.getTimestamp() == null ? "" : ate.getTimestamp()
								.toString()) + "\"];\n");
			}
			// include log event data
			bw.write("atedata" + i + " [shape=\"box\",label=\"");
			bw.write("Originator = " + ate.getOriginator() + "\\n");
			it = ate.getAttributes().keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				bw.write(key + " = " + (String) ate.getAttributes().get(key)
						+ "\\n");
			}
			bw.write("\"];\n");
			bw.write("ate" + i + " -> atedata" + i + ";");
			if (i > 0) {
				bw.write("ate" + (i - 1) + " -> ate" + i + ";");
			}
			bw.write("{ rank = same; " + "ate" + i + ";atedata" + i + ";}");
			i++;
		}
		bw.write("pidata -> ate0 [color=\"white\"];\n");
		// close top graph
		bw.write("}\n");
	}

	/**
	 * Write a log visualization incorporating all the selected traces. Note
	 * that this is used for more than one traces being selected and the
	 * failedEventsOption not being specified.
	 * 
	 * @param bw
	 *            The writer used by the framework to create the temporary dot
	 *            file.
	 * @throws IOException
	 *             If writing to the writer fails.
	 */
	public void writeLogVisualization(Writer bw) throws IOException {
		// (note that this visualization does not include evaluation of the
		// failedEventsOption, since the record shape does not allow a single
		// part of it to deviate in fill color or the like;
		// In the case of failedEventsOption being selected, an alternative
		// method
		// called writeDiagnosticLogVisualization is called as a temporary
		// solution)
		bw.write("digraph G {\nfontsize=\"8\"; \nremincross=true;\n");
		bw.write("fontname=\"Arial\"; \nrankdir=\"LR\"; \n");
		bw
				.write("edge [arrowsize=\"0.7\",fontname=\"Arial\",fontsize=\"8\"];\n");
		bw
				.write("node [shape=\"box\",height=\".2\",width=\".2\",fontname=\"Arial\",fontsize=\"8\"];\n");
		// traverse arraylist manually instead of using an iterator
		// to ensure the right order of the traces on the screen (from top to
		// bottom)

		myReplayedLog.reset();
		int traceCount = myReplayedLog.getSizeOfLog();
		// int listSize = currentlySelectedInstances.size();
		// int traceCount = listSize - 1;

		// write a whole graph for every log trace
		// while (traceCount >= 0) {
		while (myReplayedLog.hasNext()) {
			// DiagnosticLogTrace currentTrace = (DiagnosticLogTrace)
			// currentlySelectedInstances.get(traceCount);
			DiagnosticLogTrace currentTrace = (DiagnosticLogTrace) myReplayedLog
					.next();

			// write the name of the trace
			bw.write("traceID_" + traceCount
					+ " [color=\"white\",label=\"Log Trace "
					+ currentTrace.getName() + "\"];\n");
			// get audit trail list as arraylist to cast the entries to
			// diagnostic audit trail entries afterwards
			Iterator currentATEListIterator = currentTrace.getProcessInstance()
					.getAuditTrailEntryList().iterator();
			// TODO - remove counter
			int eventCount = 0;
			// start trace record
			bw
					.write("trace_" + traceCount
							+ " [shape=\"record\",label=\"{<s> ");
			while (currentATEListIterator.hasNext()) {
				AuditTrailEntry currentATE = (AuditTrailEntry) currentATEListIterator
						.next();
				// DiagnosticAuditTrailEntry ateDiag =
				// currentTrace.getAteDiagnostic(currentATE);
				// write box for current audit trail entry (ATE)
				bw
						.write(currentATE.getElement() + "\\n"
								+ currentATE.getType());
				// prepare next entry only if is not the last element of trace
				if (eventCount != currentTrace.getProcessInstance()
						.getAuditTrailEntryList().size() - 1) {
					bw.write(" | ");
				}
				eventCount++;
			}
			// finish trace record
			bw.write(" }\"];\n");
			// connect head with trace
			bw.write("traceID" + "_" + traceCount + " -> trace_" + traceCount
					+ ":s [color=\"white\"];");
			traceCount--;
		}
		// close graph
		bw.write("}\n");
	}

	/**
	 * Write a log visualization incorporating all the selected traces. Note
	 * that this is used for more than one traces being selected and the
	 * failedEventsOption being specified.
	 * 
	 * @param bw
	 *            The writer used by the framework to create the temporary dot
	 *            file.
	 * @throws IOException
	 *             If writing to the writer fails.
	 */
	public void writeDiagnosticLogVisualization(Writer bw) throws IOException {
		// (note that this visualization does include evaluation of the
		// failedEventsOption, since the record shape used in the default method
		// writeLogVisualization does not allow a single
		// part of it to deviate in fill color or the like)
		bw.write("digraph G {\nfontsize=\"8\"; \nremincross=true;\n");
		bw.write("fontname=\"Arial\"; \nrankdir=\"LR\"; \n");
		bw
				.write("edge [arrowsize=\"0.7\",fontname=\"Arial\",fontsize=\"8\"];\n");
		bw
				.write("node [shape=\"box\",height=\".2\",width=\".2\",fontname=\"Arial\",fontsize=\"8\"];\n");
		// traverse arraylist manually instead of using an iterator
		// to ensure the right order of the traces on the screen (from top to
		// bottom)
		// int listSize = currentlySelectedInstances.size();
		// int traceCount = listSize - 1;
		myReplayedLog.reset();
		int traceCount = myReplayedLog.getSizeOfLog();

		// write a whole graph for every log trace
		// while (traceCount >= 0) {
		// DiagnosticLogTrace currentTrace = (DiagnosticLogTrace)
		// currentlySelectedInstances.get(
		// traceCount);
		while (myReplayedLog.hasNext()) {
			// DiagnosticLogTrace currentTrace = (DiagnosticLogTrace)
			// currentlySelectedInstances.get(traceCount);
			DiagnosticLogTrace currentTrace = (DiagnosticLogTrace) myReplayedLog
					.next();

			// write the name of the trace
			bw.write("traceID_" + traceCount
					+ " [color=\"white\",label=\"Log Trace "
					+ currentTrace.getName() + "\"];\n");
			// get audit trail list as arraylist to cast the entries to
			// diagnostic audit trail entries afterwards
			Iterator currentATEListIterator = currentTrace.getProcessInstance()
					.getAuditTrailEntryList().iterator();
			int eventCount = 0;
			while (currentATEListIterator.hasNext()) {
				AuditTrailEntry currentATE = (AuditTrailEntry) currentATEListIterator
						.next();
				DiagnosticAuditTrailEntry ateDiag = currentTrace
						.getAteDiagnostic(currentATE);
				// mark current log event
				if (ateDiag.getFailedExecutionValue() == true) {
					bw
							.write("logEvent_"
									+ traceCount
									+ "_"
									+ eventCount
									+ " [shape=\"box\",style=\"filled\",fillcolor=\"orange1\",label=\""
									+ currentATE.getElement() + "\\n"
									+ currentATE.getType() + "\"];\n");
				}
				// write normally
				else {
					bw.write("logEvent_" + traceCount + "_" + eventCount
							+ " [shape=\"box\",label=\""
							+ currentATE.getElement() + "\\n"
							+ currentATE.getType() + "\"];\n");
				}
				// connect with header
				if (eventCount == 0) {
					bw.write("traceID" + "_" + traceCount + " -> logEvent_"
							+ traceCount + "_" + eventCount
							+ "[color=\"white\"];");
				}
				// connect with previous log event
				else {
					bw.write("logEvent_" + traceCount + "_" + (eventCount - 1)
							+ " -> logEvent_" + traceCount + "_" + eventCount
							+ ";");
				}
				eventCount++;
			}
			traceCount--;
		}
		// close graph
		bw.write("}\n");
	}
}
