package org.processmining.analysis.socialsuccess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.log.InfoItem;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.rfb.LogSummaryFormatter;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.ui.Message;

public class PersonalityData extends LogSummaryFormatter {
	// html
	protected static final String FONT_TEXT_GOOD = "<font face=\"helvetica,arial,sans-serif\" color=\"green\" size=\"4\">";
	protected static final String FONT_TEXT_BAD = "<font face=\"helvetica,arial,sans-serif\" color=\"red\" size=\"4\">";

	public static final String NODE_ACTION_USER_ORIG = "ACTION+USER_ORIG";
	public static final String NODE_ACTION_REFER_ORIG = "ACTION+REFER_ORIG";
	public static final String USER_ACTION = "USER+ACTION";
	public static final String NODES_TAGS = "NODES+TAGS";
	public static final String USER_USER_RELATION = "USER+USER";
	private LogReader[] log;
	private OrgModel model;
	private LogSummary[] summary;
	private HashMap<String, Integer> processes; // NROfProcessInstances,
	// firstPInstance
	private HashSet<String> filteredProcess;
	private Date startTime = null, endTime = null;
	private int nrOfClusters = 10;
	private SimpleDateFormat fd = new SimpleDateFormat("yyyy-MM-dd");

	public PersonalityData(LogReader log0, LogReader log1, LogReader log2,
			LogReader log3, OrgModel model) {
		super();

		this.log = new LogReader[4];
		this.log[0] = log0;
		this.log[1] = log1;
		this.log[2] = log2;
		this.log[3] = log3;
		this.model = model;
		// haal de summaries op
		Message.add("SSA: Getting log summary");
		this.summary = new LogSummary[log.length];
		for (int i = 0; i < log.length; i++) {
			this.summary[i] = log[i].getLogSummary();
		}
		this.processes = new HashMap<String, Integer>();
		this.filteredProcess = new HashSet<String>();

		// koppel de processen aan het bestand waar ze in zitten.
		for (int i = 0; i < summary.length; i++) {
			InfoItem[] info = summary[i].getProcesses();
			for (int j = 0; j < info.length; j++) {
				this.processes.put(info[j].getName(), i);
			}
		}
		Iterator<String> it = processes.keySet().iterator();
		while (it.hasNext()) {
			String pName = it.next();
			if (pName.equals(NODE_ACTION_REFER_ORIG)
					|| pName.equals(NODE_ACTION_USER_ORIG)
					|| pName.equals(NODES_TAGS)
					|| pName.equals(USER_USER_RELATION)
					|| pName.equals(USER_ACTION)) {
				filteredProcess.add(pName);
			}
		}

		// initialiseer de datum velden
		this.getEndTime();
		this.getStartTime();
	}

	/**
	 * Returns (given a boolean) a html string containing <b>available</b> or
	 * <b>not available</b>
	 * 
	 * @param b
	 *            boolean input
	 * @return html string
	 */
	protected String booleanToString(boolean b) {
		if (b)
			return FONT_TEXT_GOOD + "available" + "</font>";
		else
			return FONT_TEXT_BAD + "not available" + "</font>";
	}

	/**
	 * This functions counts the number of unknown processes.
	 * 
	 * @return int the number of unknown processes
	 */
	protected int countUnknownProcesses() {
		return countProcesses() - filteredProcess.size();
	}

	protected int countProcesses() {
		return processes.size();
	}

	protected StringBuffer echoBoldString(StringBuffer s1, String s2, Object s3) {
		return echoString(s1, s2, "<b>" + s3 + "</b>");
	}

	protected StringBuffer echoString(StringBuffer s1, String s2, Object s3) {
		return s1.append(FONT_TEXT + s2 + ": " + s3 + "</font><br>");
	}

	public Date getStartTime() {
		if (startTime == null)
			startTime = getStartTimeFromData();
		return startTime;
	}

	public String getStartTimeString() {
		return fd.format(getStartTime());
	}

	public Date getEndTime() {
		if (endTime == null)
			endTime = getEndTimeFromData();
		return endTime;
	}

	public String getEndTimeString() {
		return fd.format(getEndTime());
	}

	public Date getStartTimeFromData() {
		Date startTime = null;
		for (int j = 0; j < log.length; j++) {
			for (int i = 0; i < log[j].numberOfProcesses(); i++) {
				Date timeStart = this.summary[j].getStartTime(log[j]
						.getProcess(i).getName());
				if (timeStart != null) {
					if (startTime != null) {
						if (timeStart.before(startTime))
							startTime = timeStart;
					} else {
						startTime = timeStart;
					}
				}
			}
		}
		return startTime;
	}

	public String getStartTimeFromDataString() {
		return fd.format(getStartTimeFromData());
	}

	public Date getEndTimeFromData() {
		Date endTime = null;
		for (int j = 0; j < log.length; j++) {
			for (int i = 0; i < log[j].numberOfProcesses(); i++) {
				Date timeEnd = this.summary[j].getEndTime(log[j].getProcess(i)
						.getName());
				if (timeEnd != null) {
					if (endTime != null) {
						if (timeEnd.after(endTime))
							endTime = timeEnd;
					} else {
						endTime = timeEnd;
					}
				}
			}
		}
		return endTime;
	}

	public String getEndTimeFromDataString() {
		return fd.format(getEndTimeFromData());
	}

	/**
	 * Formats information contained in a LogSummary in HTML string format.
	 * 
	 * @param includeHeader
	 *            include general information about the log
	 * @param includeDataAvailability
	 * @param includeActions
	 * @param includeAttributeInformation
	 * @param includeAvailableProcesses
	 * @return HTML-formatted description string
	 */
	public String format(boolean includeHeader,
			boolean includeDataAvailability, boolean includeActions,
			boolean includeAttributeInformation,
			boolean includeAvailableProcesses) {

		StringBuffer sumString = new StringBuffer();
		writeDocumentHeader(sumString);

		if (includeDataAvailability) {
			sumString.append(PART_START);
			heading(sumString, "Social Success Data Availability");
			echoBoldString(sumString, "Number of processes", countProcesses());
			echoBoldString(sumString, "Nodes with actions",
					isProcessAvailableStr(NODE_ACTION_USER_ORIG));
			echoBoldString(sumString, "Users with their actions",
					isProcessAvailableStr(USER_ACTION));
			echoBoldString(sumString, "Nodes bundeled with their tags",
					isProcessAvailableStr(NODES_TAGS));
			echoBoldString(sumString, "Relations between users",
					isProcessAvailableStr(USER_USER_RELATION));
			echoBoldString(sumString, "Unknown processes",
					countUnknownProcesses());
			sumString.append(PART_STOP);
		}

		if (isProcessAvailable(NODE_ACTION_USER_ORIG)
				&& includeAvailableProcesses) {
			sumString.append(PART_START);
			heading(sumString, "Nodes with Actions (user=orig)");
			echoBoldString(sumString, "Number of nodes",
					getNumberOfProcessInstances(NODE_ACTION_USER_ORIG));
			sumString.append(PART_STOP);
		}

		if (isProcessAvailable(NODE_ACTION_REFER_ORIG)
				&& includeAvailableProcesses) {
			sumString.append(PART_START);
			heading(sumString, "Nodes with Actions (refer=orig)");
			echoBoldString(sumString, "Number of nodes",
					getNumberOfProcessInstances(NODE_ACTION_REFER_ORIG));
			sumString.append(PART_STOP);
		}

		if (isProcessAvailable(USER_ACTION) && includeAvailableProcesses) {
			sumString.append(PART_START);
			heading(sumString, "Users with their actions");
			echoBoldString(sumString, "Number of users",
					getNumberOfProcessInstances(USER_ACTION));
			sumString.append(PART_STOP);
		}

		if (isProcessAvailable(NODES_TAGS) && includeAvailableProcesses) {
			sumString.append(PART_START);
			heading(sumString, "Nodes with their tags");
			echoBoldString(sumString, "Number of tagged nodes",
					getNumberOfProcessInstances(NODES_TAGS));
			sumString.append(PART_STOP);
		}

		writeDocumentFooter(sumString);
		return sumString.toString();
	}

	protected String getHTMLListFromIterator(Iterator<String> it) {
		StringBuffer r = new StringBuffer();
		r.append("<ul>");
		while (it.hasNext()) {
			r.append("<li>" + it.next() + "</li>");
		}
		r.append("</ul>");
		return r.toString();
	}

	public LogReader[] getLog() {
		return log;
	}

	protected int getNumberOfProcessInstances(String processName) {
		if (this.getProcess(processName) != null) {
			return this.getProcess(processName).numberOfInstances();
		} else {
			return 0;
		}
	}

	public OrgModel getOrgModel() {
		return model;
	}

	public HashSet<String> getUsers() {
		HashSet<String> users = new HashSet<String>();
		if (isProcessAvailable(USER_ACTION)) {
			LogReader l = getProcess(USER_ACTION);
			Iterator<ProcessInstance> it = l.getInstances().iterator();
			while (it.hasNext()) {
				ProcessInstance p = (ProcessInstance) it.next();
				users.add(p.getName());
			}
		}
		return users;
	}

	public LogReader getProcess(String processName) {
		if (processes.get(processName) != null)
			return this.log[processes.get(processName)];
		else
			return null;
		/*
		 * if (isProcessAvailable(processName)) { if
		 * (!filteredProcess.containsKey(processName)) { DefaultLogFilter filter
		 * = new DefaultLogFilter( DefaultLogFilter.DISCARD);
		 * filter.setProcess(processName); String[] list =
		 * summary.getEventTypes(); for (int i = 0; i < list.length; i++) {
		 * filter.filterEventType(list[i], DefaultLogFilter.DISCARD); } try { //
		 * TODO fix this LogReader filteredLog =
		 * LogReaderFactory.createInstance(filter, log); // fL.data = null heeft
		 * iets met asynchroon te maken. filteredProcess.put(processName,
		 * filteredLog); return filteredLog; } catch (Exception e) { // TODO
		 * Auto-generated catch block
		 * System.out.println("Unable to filter log"); e.printStackTrace();
		 * return null; } } else { return filteredProcess.get(processName); } }
		 * else { return null; } //
		 */

	}

	/**
	 * Function which checks if a process with the name <i>processName</i> is
	 * available.
	 * 
	 * @param processName
	 *            name of the process to be checked
	 * @return boolean availability of the process
	 */
	public boolean isProcessAvailable(String processName) {
		return processes.containsKey(processName);
	}

	protected String isProcessAvailableStr(String processName) {
		return booleanToString(isProcessAvailable(processName));
	}

	/**
	 * This functions returns the HTML string representation of the summary of
	 * the input files
	 * 
	 * @return returns an HTML string containing the summary
	 */
	@Override
	public String toString() {
		return format(true, true, true, true, true);
	}

	public int getNrOfClusters() {
		return nrOfClusters;
	}

	public void setNrOfClusters(int nrOfClusters) {
		if (nrOfClusters > 0 && nrOfClusters < 100)
			this.nrOfClusters = nrOfClusters;
	}

	public void setStartTime(String startTime) {
		try {
			this.startTime = fd.parse(startTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setEndTime(String endTime) {
		try {
			this.endTime = fd.parse(endTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
