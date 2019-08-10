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

package org.processmining.analysis.summary;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.InfoItem;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;

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
public class ExtendedLogSummary implements LogSummary {

	// HTML styles
	private static final String H1 = "h1";
	private static final String H2 = "h2";
	private static final String H3 = "h3";
	private static final String BOLD = "b";

	private static final String PAR = "p";

	// HTML tables
	private static final String TR = "tr";
	private static final String TH = "th";
	private static final String TD = "td";

	private InfoItem workflow;
	private InfoItem source;
	private ArrayList processes;
	private HashSet eventTypes;
	private HashSet modelElements;
	private HashSet originators;
	private int numAuditTrailEntries;
	private int numProcessInstances;
	private LogEvents logEvents;

	private HashMap mapPIs = new HashMap();
	private HashMap mapStartAtes = new HashMap();
	private HashMap mapEndAtes = new HashMap();
	private HashMap mapAtes = new HashMap();
	private HashMap mapAtes2numberPIs = new HashMap();
	private HashMap mapOriginators = new HashMap();

	private HashMap startTimes = new HashMap();
	private HashMap endTimes = new HashMap();
	private boolean loadedOntologies = false;
	private OntologyCollection ontologies = null;

	public ExtendedLogSummary() {
		workflow = new InfoItem("", "", new DataSection(), null);
		source = new InfoItem("", "", new DataSection(), null);
		processes = new ArrayList();
		eventTypes = new HashSet();
		modelElements = new HashSet();
		originators = new HashSet();
		logEvents = new LogEvents();
		numAuditTrailEntries = 0;
		numProcessInstances = 0;
	}

	public LogEvents getLogEvents() {
		return logEvents;
	}

	public InfoItem getWorkflowLog() {
		return workflow;
	}

	public InfoItem getSource() {
		return source;
	}

	public InfoItem[] getProcesses() {
		return (InfoItem[]) processes.toArray(new InfoItem[0]);
	}

	public int getNumberOfAuditTrailEntries() {
		return numAuditTrailEntries;
	}

	public int getNumberOfProcessInstances() {
		return numProcessInstances;
	}

	public int getNumberOfUniqueProcessInstances() {
		return mapPIs.size();
	}

	public HashMap getMappingAtesToNumberPIs() {
		return this.mapAtes2numberPIs;
	}

	// guaranteed to be alphabetically sorted, so binary search can be used
	public String[] getEventTypes() {
		String[] s = (String[]) eventTypes.toArray(new String[0]);

		Arrays.sort(s);
		return s;
	}

	// guaranteed to be alphabetically sorted, so binary search can be used
	public String[] getModelElements() {
		String[] s = (String[]) modelElements.toArray(new String[0]);

		Arrays.sort(s);
		return s;
	}

	// guaranteed to be alphabetically sorted, so binary search can be used
	public String[] getOriginators() {
		String[] s = (String[]) originators.toArray(new String[0]);

		Arrays.sort(s);
		return s;
	}

	public HashMap getOriginatorMap() {
		return mapOriginators;
	}

	public void setWorkflowLog(InfoItem workflow) {
		this.workflow = workflow;
	}

	public void setSource(InfoItem source) {
		this.source = source;
	}

	public void addProcess(InfoItem process) {
		processes.add(process);
	}

	public void addProcessInstance(ProcessInstance pi) {
		int numSimilarInstances = 1;
		AuditTrailEntries ates = pi.getAuditTrailEntries();
		String processName = pi.getProcess();
		Iterator it = processes.iterator();
		InfoItem process = null;
		while (it.hasNext()
				&& ((process == null) || (!process.getName()
						.equals(processName)))) {
			process = (InfoItem) it.next();
		}

		numSimilarInstances = MethodsForWorkflowLogDataStructures
				.getNumberSimilarProcessInstances(pi);
		Date start = ates.first().getTimestamp();
		Date end = ates.last().getTimestamp();
		while (ates.hasNext()) {
			AuditTrailEntry ate = ates.next();
			if (ate.getTimestamp() != null) {
				if (start == null) {
					start = ate.getTimestamp();
				} else {
					if (ate.getTimestamp().before(start)) {
						start = ate.getTimestamp();
					}
				}
			}

			if (ate.getTimestamp() != null) {
				if (end == null) {
					end = ate.getTimestamp();
				} else {
					if (ate.getTimestamp().after(end)) {
						end = ate.getTimestamp();
					}
				}

			}

			LogEvent e;

			eventTypes.add(ate.getType());
			modelElements.add(ate.getElement());
			originators.add(ate.getOriginator());

			e = logEvents.findLogEvent(ate.getElement(), ate.getType());
			if (e == null) {
				// add LogEvent
				logEvents.add(new LogEvent(ate.getElement(), ate.getType(),
						numSimilarInstances));
			} else {
				e.incOccurrenceCount(numSimilarInstances);
			}

			numAuditTrailEntries += numSimilarInstances;

		}
		numProcessInstances += numSimilarInstances;

		int piFrequency = MethodsForWorkflowLogDataStructures
				.getNumberSimilarProcessInstances(pi);

		insert(mapPIs, pi.getName(), piFrequency);

		if (ates.size() > 0) {
			insert(mapStartAtes, getElementEvent(ates.first()), piFrequency);
			insert(mapEndAtes, getElementEvent(ates.last()), piFrequency);
		}
		HashSet uniqueAtes = new HashSet();
		for (int i = 0; i < ates.size(); i++) {
			uniqueAtes.add(getElementEvent(ates.getEntry(i)));
			insert(mapAtes, getElementEvent(ates.getEntry(i)), piFrequency);
			insert(mapOriginators, ates.getEntry(i).getOriginator(),
					piFrequency);
		}

		Iterator it2 = uniqueAtes.iterator();
		while (it2.hasNext()) {
			insert(mapAtes2numberPIs, (String) it2.next(), piFrequency);
		}

		if (start != null) {
			if (!startTimes.containsKey(process)
					|| startTimes.get(process) == null) {
				startTimes.put(process, start);
			} else {
				Date s = (Date) startTimes.get(process);
				if (start.before(s)) {
					startTimes.put(process, start);
				}
			}
		}
		if (end != null) {
			if (!endTimes.containsKey(process) || endTimes.get(process) == null) {
				endTimes.put(process, end);
			} else {
				Date s = (Date) endTimes.get(process);
				if (end.after(s)) {
					endTimes.put(process, end);
				}
			}
		}
	}

	private String getElementEvent(AuditTrailEntry ate) {
		return ate.getElement() + " (" + ate.getType() + ")";
	}

	private void insert(HashMap hash, String key, int frequency) {
		if (hash.containsKey(key)) {
			int i = ((Integer) hash.get(key)).intValue();
			i += frequency;
			hash.put(key, new Integer(i));
		} else {
			hash.put(key, new Integer(frequency));
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("<html><body>");

		// process instances (PIs)
		sb.append(tag(" Process Instances (PIs)", H1));
		sb.append(tag(tag("Number of PIs: ", BOLD)
				+ getNumberOfProcessInstances(), PAR));
		sb.append(tag(tag("Number of different PIs: ", BOLD)
				+ getNumberOfUniqueProcessInstances(), PAR));
		sb.append(tag("", PAR));
		sb.append("<table border=\"1\">");
		sb.append(tag((tag("Frequency", TH) + tag("Percentage", TH) + tag(
				"PIs with this frequency/percentage", TH)), TR));
		sb.append(convertToOrderedString(getReverseMapping(mapPIs),
				getNumberOfProcessInstances()));
		sb.append("</table>");

		// audit trail entries (ATEs)
		sb.append(tag(" Audit Trail Entries (ATEs)", H1));
		sb.append(tag(tag("Number of ATEs: ", BOLD)
				+ getNumberOfAuditTrailEntries(), PAR));
		sb.append(tag(tag("Number of different ATEs: ", BOLD) + mapAtes.size(),
				PAR));
		sb.append(tag(tag("ATEs: ", BOLD) + orderSet(mapAtes.keySet(), false),
				PAR));
		sb.append(tag("", PAR));
		sb.append("<table border=\"1\">");
		sb.append(tag((tag("Frequency", TH) + tag("Percentage", TH) + tag(
				"ATEs with this frequency/percentage", TH)), TR));
		sb.append(convertToOrderedString(getReverseMapping(mapAtes),
				getNumberOfAuditTrailEntries()));
		sb.append("</table>");

		// start ATEs
		sb.append(tag("Start ATEs", H1));
		sb.append(tag(tag("Number of different start ATEs: ", BOLD)
				+ mapStartAtes.size(), PAR));
		sb.append(tag(tag("Start ATEs: ", BOLD)
				+ orderSet(mapStartAtes.keySet(), false), PAR));
		sb.append(tag("", PAR));
		sb.append("<table border=\"1\">");
		sb.append(tag((tag("Frequency", TH) + tag("Percentage", TH) + tag(
				"Start ATEs with this frequency/percentage", TH)), TR));
		sb.append(convertToOrderedString(getReverseMapping(mapStartAtes),
				getNumberOfProcessInstances()));
		sb.append("</table>");

		// end ATEs
		sb.append(tag("End ATEs", H1));
		sb.append(tag(tag("Number of different end ATEs: ", BOLD)
				+ mapEndAtes.size(), PAR));
		sb.append(tag(tag("End ATEs: ", BOLD)
				+ orderSet(mapEndAtes.keySet(), false), PAR));
		sb.append(tag("", PAR));
		sb.append("<table border=\"1\">");
		sb.append(tag((tag("Frequency", TH) + tag("Percentage", TH) + tag(
				"End ATEs with this frequency/percentage", TH)), TR));
		sb.append(convertToOrderedString(getReverseMapping(mapEndAtes),
				getNumberOfProcessInstances()));
		sb.append("</table>");

		// Originators

		sb.append(tag("Originators", H1));
		sb.append(tag(tag("Number of different originators: ", BOLD)
				+ mapOriginators.size(), PAR));
		sb.append(tag(tag("Originators: ", BOLD)
				+ orderSet(mapOriginators.keySet(), false), PAR));
		sb.append(tag("", PAR));
		sb.append("<table border=\"1\">");
		sb.append(tag((tag("Frequency", TH) + tag("Percentage", TH) + tag(
				"Originators with this frequency/percentage", TH)), TR));
		sb.append(convertToOrderedString(getReverseMapping(mapOriginators),
				getNumberOfAuditTrailEntries()));
		sb.append("</table>");

		sb.append("</body></html>");
		return sb.toString();
	}

	private String convertToOrderedString(HashMap hash, double sum) {

		StringBuffer string = new StringBuffer();

		// Ordering the keys.
		int[] keys = new int[hash.size()];
		Iterator iterator = hash.keySet().iterator();
		for (int i = 0; i < hash.size(); i++) {
			keys[i] = ((Integer) iterator.next()).intValue();
		}
		Arrays.sort(keys);

		// Building the ordered string representation.
		// The string has two columns.

		NumberFormat percentFormatter = NumberFormat.getPercentInstance();
		percentFormatter.setMinimumFractionDigits(4);
		percentFormatter.setMaximumFractionDigits(4);

		for (int i = 0; i < keys.length; i++) {
			double percent = (keys[i] / sum);
			string.append(tag(tag(keys[i] + "", TD)
					+ tag(percentFormatter.format(percent), TD)
					+ tag((String) hash.get(new Integer(keys[i])), TD), TR));
		}

		return string.toString();
	}

	private HashMap getReverseMapping(HashMap hash) {
		HashMap reverseTemp = new HashMap();
		HashMap reverse = new HashMap();

		// filling in the reverseTemp variable
		// I use this order the set of tasks
		// that have the same frequency
		Set keys = hash.keySet();
		Iterator iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			Integer hashValue = (Integer) hash.get(key);
			if (reverseTemp.containsKey(hashValue)) {
				TreeSet reverseValue = (TreeSet) reverseTemp.get(hashValue);
				reverseValue.add(key);
				reverseTemp.put(hashValue, reverseValue);
			} else {
				TreeSet set = new TreeSet();
				set.add(key);
				reverseTemp.put(hashValue, set);
			}

		}

		// filling in the actual return variable
		iterator = reverseTemp.keySet().iterator();
		while (iterator.hasNext()) {
			Object key = iterator.next();
			String value = orderSet((TreeSet) reverseTemp.get(key), true);
			reverse.put(key, value);
		}

		return reverse;

	}

	private String orderSet(Set set, boolean breakLine) {
		StringBuffer value;
		String separator = ", ";

		if (!(set instanceof TreeSet)) {
			// order the set
			TreeSet tempSet = new TreeSet();
			Iterator i = set.iterator();
			while (i.hasNext()) {
				tempSet.add(i.next());
			}
			set = tempSet;
			tempSet = null;
		}

		value = new StringBuffer();
		if (!set.isEmpty()) {
			// Building the text...
			// The break lines "<br>" are included to limit the cell width.
			// Otherwise,
			// the exception
			// "javax.swing.text.StateInvariantError: infinite loop in formatting"
			// will be raised by the "FlowView" class when the result has to be
			// shown.
			Object[] o = new Object[set.size()];
			o = set.toArray(o);
			for (int i = 0; i < set.size(); i++) {
				value.append(o[i]).append(separator);
				if (breakLine) {
					value.append("<br>");
				}
			}
			value = new StringBuffer(value.substring(0, value
					.lastIndexOf(separator)));
		}

		return value.toString();
	}

	private String tag(String s, String tag) {
		return "<" + tag + ">" + s + "</" + tag + ">";
	}

	public Date getStartTime(String process) {
		InfoItem proc = null;
		Iterator it = processes.iterator();
		while (it.hasNext()
				&& ((proc == null) || (!proc.getName().equals(process)))) {
			proc = (InfoItem) it.next();
		}
		return (Date) startTimes.get(proc);
	}

	public Date getEndTime(String process) {
		InfoItem proc = null;
		Iterator it = processes.iterator();
		while (it.hasNext()
				&& ((proc == null) || (!proc.getName().equals(process)))) {
			proc = (InfoItem) it.next();
		}
		return (Date) endTimes.get(proc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogSummary#getEventsForInstance(org.
	 * processmining.framework.log.ProcessInstance)
	 */
	public Set<LogEvent> getEventsForInstance(ProcessInstance instance) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogSummary#getInstancesForEvent(org.
	 * processmining.framework.log.LogEvent)
	 */
	public Set<ProcessInstance> getInstancesForEvent(LogEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<Integer, Set<String>> getProcessInstancesOccurrences() {
		return null;
	}

	public Map<LogEvent, Integer> getStartingLogEvents() {
		return new HashMap<LogEvent, Integer>();
	}

	public Map<LogEvent, Integer> getEndingLogEvents() {
		return new HashMap<LogEvent, Integer>();
	}

	public HashMap getEventsForOriginator(String originator) {
		return new HashMap();
	}

	public Map<String, Set<String>> getConceptsAndInstances() {
		return Collections.unmodifiableMap(new HashMap<String, Set<String>>(0));
	}

	public OntologyCollection getOntologies() {
		if (!loadedOntologies) {
			loadedOntologies = true;
			ontologies = new OntologyCollection(this);
		}
		return ontologies;
	}
}
