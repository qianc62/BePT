package org.processmining.analysis.decisionmining;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.zip.GZIPOutputStream;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.ui.Message;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.mxml.writing.persistency.LogPersistencyStream;

/**
 * Class holding a decision point and a log, and providing a method to split up
 * the log in instances corresponding to loop occurrences (ignoring previous
 * instance divisions).
 */
public class DecisionPointPlusLog {

	protected DecisionPoint decisionPoint;
	protected LogReader log;

	public DecisionPointPlusLog(DecisionPoint aDecisionPoint, LogReader aLog) {
		decisionPoint = aDecisionPoint;
		log = aLog;
	}

	public DecisionPoint getDecisionPoint() {
		return decisionPoint;
	}

	/**
	 * Sets this object to hold the newly given decision point.
	 * 
	 * @param newDecisionPoint
	 *            the new decision point
	 */
	public void setDecisionPoint(DecisionPoint newDecisionPoint) {
		decisionPoint = newDecisionPoint;
	}

	/**
	 * Creates a new log where each process instances is split up into multiple
	 * instances if there are loops. <br>
	 * For each observed target category concept of the decision point a new
	 * instances is created (up to this point). This is useful in conjunction
	 * with the decision miner, which only interprets the last occurrence of a
	 * decision within an instance and thus ignores all previous occurrences of
	 * the same loop.
	 * 
	 * @return the log reader consisting of separate instances for each loop
	 *         occurrence
	 */
	public LogReader getLoopSeparatedLog() {
		// get all separating log events for corresponding decision categories
		HashSet<LogEvent> separatingEvents = new HashSet<LogEvent>();
		for (DecisionCategory category : decisionPoint.getTargetConcept()) {
			for (LogEvent event : category.getAssociatedLogEvents()) {
				separatingEvents.add(event);
			}
		}
		try {
			File outputFile = File.createTempFile("DecisionPointPlusLogTemp",
					".mxml.gz");
			FileOutputStream output = new FileOutputStream(outputFile);
			BufferedOutputStream out = new BufferedOutputStream(
					new GZIPOutputStream(output));
			LogPersistencyStream persistency = new LogPersistencyStream(out,
					false);
			Process process = log.getProcess(0);
			ProcessInstance instance = null;
			AuditTrailEntryList ateList = null;
			String name = process.getName();
			if (name == null || name.length() == 0) {
				name = "UnnamedProcess";
			}
			String description = process.getDescription();
			if (description == null || description.length() == 0) {
				description = name + " exported by MXMLib @ P-stable";
			}
			String source = log.getLogSummary().getSource().getName();
			if (source == null || source.length() == 0) {
				source = "UnknownSource";
			}
			persistency.startLogfile(name, description, source);
			for (int i = 0; i < log.numberOfProcesses(); i++) {
				process = log.getProcess(i);
				name = process.getName();
				if (name == null || name.length() == 0) {
					name = "UnnamedProcess";
				}
				description = process.getDescription();
				if (description == null || description.length() == 0) {
					description = name + " exported by MXMLib @ P-stable";
				}
				persistency.startProcess(name, description, process
						.getAttributes());
				for (int j = 0; j < process.size(); j++) {
					instance = process.getInstance(j);
					name = instance.getName();
					if (name == null || name.length() == 0) {
						name = "SplitInstance";
					}
					description = instance.getDescription();
					if (description == null || description.length() == 0) {
						description = name + " exported by MXMLib @ P-stable";
					}
					ateList = instance.getAuditTrailEntryList();
					persistency.startProcessInstance(name + "_0", description,
							instance.getAttributes());
					int lastSplit = -1;
					for (int k = 0; k < ateList.size(); k++) {
						String splitname = name + "_" + k;
						AuditTrailEntry ate = ateList.get(k);
						persistency
								.addAuditTrailEntry(promAte2mxmlibAte(ateList
										.get(k)));
						LogEvent lePendent = new LogEvent(ate.getElement(), ate
								.getType());
						if (separatingEvents.contains(lePendent) == true
								&& lastSplit < k) {
							persistency.endProcessInstance();
							persistency.startProcessInstance(splitname,
									description, instance.getAttributes());
							lastSplit = k;
							k = 0;
						}
					}
					persistency.endProcessInstance();
				}
				persistency.endProcess();
			}
			// clean up
			persistency.endLogfile();
			persistency.finish();
			// read back again
			LogFile logFile = LogFile.getInstance(outputFile.getAbsolutePath());
			LogReader loopSeparated = BufferedLogReader.createInstance(
					new DefaultLogFilter(DefaultLogFilter.INCLUDE), logFile);
			outputFile.deleteOnExit();
			return loopSeparated;
		} catch (Exception ex) {
			ex.printStackTrace();
			Message
					.add("Error occured while creating loop-separated log. Non-separated log exported instead.");
			return log;
		}
	}

	protected org.processmining.lib.mxml.AuditTrailEntry promAte2mxmlibAte(
			AuditTrailEntry promAte) {
		org.processmining.lib.mxml.AuditTrailEntry mxmlibAte = new org.processmining.lib.mxml.AuditTrailEntry();
		mxmlibAte.setWorkflowModelElement(promAte.getElement());
		mxmlibAte.setEventType(EventType.getType(promAte.getType()));
		mxmlibAte.setOriginator(promAte.getOriginator());
		if (promAte.getTimestamp() != null) {
			mxmlibAte.setTimestamp(promAte.getTimestamp());
		}
		mxmlibAte.setAttributes(promAte.getAttributes());
		return mxmlibAte;
	}

}
