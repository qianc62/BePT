/**
 * Project: ProM HPLR
 * File: LighweightLogSummary.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Apr 28, 2006, 12:55:24 AM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the
 *      names of its contributors may be used to endorse or promote
 *      products derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.framework.log.rfb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.InfoItem;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;

/**
 * This class implements the log summary data structure in a lightweight manner.
 * This means, best effort has been taken to ensure minimal memory footprint and
 * runtime overhead. The implementation is fully compliant to the LogSummary
 * interface, with significantly decreased runtime costs.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class LightweightLogSummary implements LogSummary {

	/**
	 * UNIX timestamp for a date, which is significantly far into the future.
	 */
	protected static long future = System.currentTimeMillis() + 6048000000l;

	/**
	 * A simple record for storing a process's start and end date.
	 * 
	 * @author Christian W. Guenther (christian at deckfour dot org)
	 */
	protected class ProcessBoundary {

		/**
		 * process's first ATE date
		 */
		public Date start = null;
		/**
		 * process's last ATE date
		 */
		public Date end = null;

		/**
		 * Creates an initialized boundary instance
		 */
		public ProcessBoundary() {
			start = new Date(future); // later than all sane events
			end = new Date(0); // earlier than all sane events
		}

		/**
		 * Adjusts the internal start and end boundary markers using the given
		 * date object.
		 * 
		 * @param date
		 */
		public void update(Date date) {
			if (date.before(start)) {
				// adjust start time boundary
				start.setTime(date.getTime());
			}
			if (date.after(end)) {
				// adjust end time boundary
				end.setTime(date.getTime());
			}
		}
	}

	/**
	 * The total number of audit trail entries contained in a log.
	 */
	protected int numberOfAuditTrailEntries = 0;
	/**
	 * The total number of process instances contained in a log.
	 */
	protected int numberOfProcessInstances = 0;
	/**
	 * The number of unique process instances contained in a log.
	 */
	protected int numberOfUniqueProcessInstances = 0;
	/**
	 * Alphabetically ordered set containing as strings the log's event types.
	 */
	protected TreeSet<String> eventTypes = null;
	/**
	 * Mapping from process instance occurrences to the actual process instance
	 * identifiers.
	 */
	protected HashMap<Integer, Set<String>> processInstancesOccurrences = null;
	/**
	 * Mapping from log events that start a process instance to the amount of
	 * times that these log events actually start a process instance
	 */
	protected HashMap<LogEvent, Integer> startingLogEvents = null;
	/**
	 * Mapping from log events that end a process instance to the amount of
	 * times that these log events actually end a process instance
	 */
	protected HashMap<LogEvent, Integer> endingLogEvents = null;
	/**
	 * Alphabetically ordered set containing as strings the log's originators.
	 */
	protected TreeSet<String> originators = null;
	/**
	 * Alphabetically ordered set containing as strings the log's model
	 * elements.
	 */
	protected TreeSet<String> modelElements = null;
	/**
	 * Extended array list containing the log's events, i.e. unique element/type
	 * combinations.
	 */
	protected LogEvents logEvents = null;
	/**
	 * Mapping from log events to the set of originator strings which have been
	 * found to execute this log event.
	 */
	protected HashMap<String, HashMap<LogEvent, Integer>> mapOriginatorsToEvents = null;
	/**
	 * Mapping from log events to process instances in which the respective
	 * event occurs.
	 */
	protected HashMap<LogEvent, Set<ProcessInstance>> mapEventToProcessInstances = null;
	/**
	 * The boundaries, i.e. dates of first and last event, of each process,
	 * referenced by the process's name.
	 */
	protected HashMap<String, ProcessBoundary> processBoundaries = null;
	/**
	 * List of info items containing meta information about the set of
	 * processes.
	 */
	protected ArrayList<InfoItem> processInfoItems = null;
	/**
	 * Meta information about the source log.
	 */
	protected InfoItem wfLog = null;
	/**
	 * Meta information about the source system.
	 */
	protected InfoItem source = null;
	/**
	 * A set of all concept URIs used in the log.
	 */
	private Map<String, Set<String>> conceptsAndInstances = null;
	private boolean loadedOntologies;
	private OntologyCollection ontologies;

	/**
	 * Creates a new lightweight log summary.
	 * 
	 * @param logName
	 *            Name (e.g. file name) of the summarized log.
	 * @param logDescription
	 *            Description of the summarized log.
	 * @param logData
	 *            Set of attributes, as key-value pairs, associated with the
	 *            summarized log.
	 * @param sourceProgram
	 *            Name of the program from which the summarized log has been
	 *            acquired.
	 * @param sourceData
	 *            Set of attributes, as key-value pairs, associated with the
	 *            program from which the summarized log has been acquired.
	 */
	public LightweightLogSummary(String logName, String logDescription,
			DataSection logData, List<String> logModelReferences,
			String sourceProgram, DataSection sourceData,
			List<String> sourceModelReferences) {
		// initialize data structures.
		wfLog = new InfoItem(logName, logDescription, logData,
				logModelReferences);
		source = new InfoItem(sourceProgram, "", sourceData,
				sourceModelReferences);
		numberOfAuditTrailEntries = 0;
		numberOfProcessInstances = 0;
		numberOfUniqueProcessInstances = 0;
		eventTypes = new TreeSet<String>();
		processInstancesOccurrences = new HashMap<Integer, Set<String>>();
		startingLogEvents = new HashMap<LogEvent, Integer>();
		endingLogEvents = new HashMap<LogEvent, Integer>();
		originators = new TreeSet<String>();
		modelElements = new TreeSet<String>();
		logEvents = new LogEvents();
		processBoundaries = new HashMap<String, ProcessBoundary>();
		processInfoItems = new ArrayList<InfoItem>();
		mapOriginatorsToEvents = new HashMap<String, HashMap<LogEvent, Integer>>();
		mapEventToProcessInstances = new HashMap<LogEvent, Set<ProcessInstance>>();
		conceptsAndInstances = new HashMap<String, Set<String>>();
		addModelReferences(logModelReferences, logName);
		addModelReferences(logData);
		addModelReferences(sourceModelReferences, sourceProgram);
		addModelReferences(sourceData);
	}

	/**
	 * Creates a new, empty and initialized lightweight log summary instance.
	 */
	public LightweightLogSummary() {
		this("", "", new DataSection(), null, "", new DataSection(), null);
	}

	/**
	 * Adds, or registers, a new process contained in the summarized log.
	 * 
	 * @param name
	 *            Name, or ID, of the process.
	 * @param description
	 *            Description of the process.
	 * @param data
	 *            Set of attributes, as key-value pairs, associated with the
	 *            process.
	 */
	public void addProcess(String name, String description, DataSection data,
			List<String> modelReferences) {
		// create new info item for process, and add to list.
		processInfoItems.add(new InfoItem(name, description, data,
				modelReferences));
		processBoundaries.put(name, new ProcessBoundary());
		addModelReferences(data);
		addModelReferences(modelReferences, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogSummary#getProcessInstancesOccurrences
	 * ()
	 */
	public HashMap<Integer, Set<String>> getProcessInstancesOccurrences() {
		return processInstancesOccurrences;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogSummary#getStartingLogEvents()
	 */
	public HashMap<LogEvent, Integer> getStartingLogEvents() {
		return startingLogEvents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogSummary#getEndingLogEvents()
	 */
	public HashMap<LogEvent, Integer> getEndingLogEvents() {
		return endingLogEvents;
	}

	/**
	 * Removes information about the given process from the summary.
	 * 
	 * @param name
	 *            Name of the process to remove.
	 */
	public void removeProcess(String name) {
		// delete from the set of info items.
		InfoItem tmpProcess = null;
		for (Iterator it = processInfoItems.iterator(); it.hasNext();) {
			tmpProcess = (InfoItem) it.next();
			if (tmpProcess.getName().equals(name)) {
				it.remove();
			}
		}
		// remove boundary information
		processBoundaries.remove(name);
	}

	/**
	 * Adds, or registers, a new process instance contained in the summarized
	 * log. This method will also add all audit trail entries contained in the
	 * provided process instance (by calling <code>addAuditTrailEntry()</code>).
	 * 
	 * @param instance
	 *            The process instance to be added.
	 * @throws IndexOutOfBoundsException
	 * @throws IOException
	 */
	public void addProcessInstance(ProcessInstance instance)
			throws IndexOutOfBoundsException, IOException {
		// increment the number of process instances, taking into account
		// grouped process instances (i.e. instances representing multiple
		// occurrences)
		int numberOfSimilarInstances = MethodsForWorkflowLogDataStructures
				.getNumberSimilarProcessInstances(instance);
		if (processInstancesOccurrences.containsKey(numberOfSimilarInstances)) {
			Set ts = processInstancesOccurrences.get(numberOfSimilarInstances);
			ts.add(instance.getName());
		} else {
			TreeSet ts = new TreeSet();
			ts.add(instance.getName());
			processInstancesOccurrences.put(numberOfSimilarInstances, ts);
		}
		numberOfProcessInstances += numberOfSimilarInstances;
		numberOfUniqueProcessInstances++;
		// add all contained audit trail entries.
		AuditTrailEntryList ateList = instance.getAuditTrailEntryList();

		if (ateList.size() > 0) {
			// getting the starting elements of this ATE
			addToStartingEndingLogEvents(new LogEvent(ateList.get(0)
					.getElement(), ateList.get(0).getType()),
					numberOfSimilarInstances, startingLogEvents);

			// getting the ending element of this ATE
			addToStartingEndingLogEvents(new LogEvent(ateList.get(
					ateList.size() - 1).getElement(), ateList.get(
					ateList.size() - 1).getType()), numberOfSimilarInstances,
					endingLogEvents);
		}

		for (int i = 0; i < ateList.size(); i++) {
			addAuditTrailEntry(ateList.get(i), instance);
			// adjust number of audit trail entries (take into account grouped
			// instances)
			numberOfAuditTrailEntries += numberOfSimilarInstances;
		}

		addModelReferences(instance.getDataAttributes());
		addModelReferences(instance.getModelReferences(), instance.getName());
	}

	private void addToStartingEndingLogEvents(LogEvent le,
			int numberOfSimilarInstances, HashMap<LogEvent, Integer> mapping) {
		if (mapping.containsKey(le)) {
			mapping.put(le, mapping.get(le) + numberOfSimilarInstances);
		} else {
			mapping.put(le, numberOfSimilarInstances);
		}
	}

	/**
	 * Adds, or registers, a new audit trail entry contained in the summarized
	 * log.
	 * 
	 * @param ate
	 *            The audit trail entry to be registered.
	 * @param instance
	 *            The process instance in which the audit trail entry occurs.
	 */
	public void addAuditTrailEntry(AuditTrailEntry ate, ProcessInstance instance) {
		// register all information that is available
		// in the provided audit trail entry.
		String type = ate.getType();
		String element = ate.getElement();
		Date timestamp = ate.getTimestamp();
		eventTypes.add(type);
		modelElements.add(element);
		// retrieve respective log event, if present
		LogEvent event = logEvents.findLogEvent(element, type);
		if (event == null) {
			// not present: create new log event.
			event = new LogEvent(element, type,
					MethodsForWorkflowLogDataStructures
							.getNumberSimilarProcessInstances(instance));
			logEvents.add(event);
			mapEventToProcessInstances.put(event,
					new HashSet<ProcessInstance>());
		} else {
			// present: increase counter for log event.
			event.incOccurrenceCount(MethodsForWorkflowLogDataStructures
					.getNumberSimilarProcessInstances(instance));
		}
		// update process instance information
		mapEventToProcessInstances.get(event).add(instance);
		// update originator information, if available
		String originator = ate.getOriginator();
		if (originator != null && originator.length() > 0) {
			originators.add(originator);
			// adding to events
			if (mapOriginatorsToEvents.containsKey(originator)) {
				// checking if event is already in the hashmap
				if (mapOriginatorsToEvents.get(originator).containsKey(event)) {
					// event is already there, just increment counter
					Integer occurrencesForThisOriginator = mapOriginatorsToEvents
							.get(originator).get(event);
					occurrencesForThisOriginator += MethodsForWorkflowLogDataStructures
							.getNumberSimilarProcessInstances(instance);
					mapOriginatorsToEvents.get(originator).put(event,
							occurrencesForThisOriginator);
				} else {
					// event is not there yet
					mapOriginatorsToEvents
							.get(originator)
							.put(
									event,
									new Integer(
											MethodsForWorkflowLogDataStructures
													.getNumberSimilarProcessInstances(instance)));
				}

			} else {

				HashMap hm = new HashMap();
				hm.put(event, new Integer(MethodsForWorkflowLogDataStructures
						.getNumberSimilarProcessInstances(instance)));
				mapOriginatorsToEvents.put(originator, hm);

			}
		}
		// update the respective process boundaries using the
		// event's timestamp, if available.
		if (timestamp != null) {
			((ProcessBoundary) processBoundaries.get(instance.getProcess()))
					.update(timestamp);
		}

		addModelReferences(ate.getDataAttributes());
		addModelReferences(ate.getElementModelReferences(), ate.getElement());
		addModelReferences(ate.getTypeModelReferences(), ate.getType());
		addModelReferences(ate.getOriginatorModelReferences(), ate
				.getOriginator());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogSummary#getStartTime(java.lang.String)
	 */
	public Date getStartTime(String process) {
		// refer to the respective process boundaries.
		Date start = ((ProcessBoundary) processBoundaries.get(process)).start;
		// do not return the default start time
		if (start.getTime() == future) {
			return null;
		} else {
			return start;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogSummary#getEndTime(java.lang.String)
	 */
	public Date getEndTime(String process) {
		// refer to the respective process boundaries.
		Date end = ((ProcessBoundary) processBoundaries.get(process)).end;
		// do not return the default end time
		if (end.getTime() == 0) {
			return null;
		} else {
			return end;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogSummary#getEventTypes()
	 */
	public String[] getEventTypes() {
		return (String[]) eventTypes.toArray(new String[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogSummary#getLogEvents()
	 */
	public LogEvents getLogEvents() {
		return logEvents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogSummary#getModelElements()
	 */
	public String[] getModelElements() {
		return (String[]) modelElements.toArray(new String[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogSummary#getNumberOfAuditTrailEntries()
	 */
	public int getNumberOfAuditTrailEntries() {
		return numberOfAuditTrailEntries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogSummary#getNumberOfProcessInstances()
	 */
	public int getNumberOfProcessInstances() {
		return numberOfProcessInstances;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogSummary#getNumberOfUniqueProcessInstances
	 * ()
	 */
	public int getNumberOfUniqueProcessInstances() {
		return numberOfUniqueProcessInstances;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogSummary#getOriginators()
	 */
	public String[] getOriginators() {
		return (String[]) originators.toArray(new String[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogSummary#getProcesses()
	 */
	public InfoItem[] getProcesses() {
		return (InfoItem[]) processInfoItems.toArray(new InfoItem[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogSummary#getSource()
	 */
	public InfoItem getSource() {
		return source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogSummary#getWorkflowLog()
	 */
	public InfoItem getWorkflowLog() {
		return wfLog;
	}

	/**
	 * Sets the name of the summarized log.
	 * 
	 * @param logName
	 *            The name of the summarized log.
	 */
	public void setLogName(String logName) {
		wfLog.setName(logName);
	}

	/**
	 * Sets the description of the summarized log.
	 * 
	 * @param logDescription
	 *            The description of the summarized log.
	 */
	public void setLogDescription(String logDescription) {
		wfLog.setDescription(logDescription);
	}

	/**
	 * Adds an attribute to the summarized log.
	 * 
	 * @param key
	 *            Key of the attribute to add.
	 * @param value
	 *            Value of the attribute to add.
	 */
	public void addLogAttribute(String key, String value) {
		wfLog.addAttribute(key, value);
	}

	/**
	 * Sets the name of the log's source program.
	 * 
	 * @param sourceProgram
	 *            The name of the log's source program.
	 */
	public void setSourceProgram(String sourceProgram) {
		source.setName(sourceProgram);
	}

	/**
	 * Adds an attribute to the log's source program.
	 * 
	 * @param key
	 *            Key of the attribute to add.
	 * @param value
	 *            Value of the attribute to add.
	 */
	public void addSourceAttribute(String key, String value) {
		source.addAttribute(key, value);
	}

	public String toString() {
		// FIXME: implement this properly!
		// return "lightweight log summary";
		return LogSummaryFormatter.format(this);
	}

	public int hashCode() {
		return numberOfAuditTrailEntries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogSummary#getEventsForInstance(org.
	 * processmining.framework.log.ProcessInstance)
	 */
	public Set<LogEvent> getEventsForInstance(ProcessInstance instance) {
		Set<LogEvent> result = new HashSet<LogEvent>();
		Set<ProcessInstance> instances = null;
		for (LogEvent key : mapEventToProcessInstances.keySet()) {
			instances = mapEventToProcessInstances.get(key);
			if (instances.contains(instance)) {
				result.add(key);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogSummary#getEventsForOriginator(java
	 * .lang.String)
	 */
	public HashMap<LogEvent, Integer> getEventsForOriginator(String originator) {
		return mapOriginatorsToEvents.get(originator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogSummary#getInstancesForEvent(org.
	 * processmining.framework.log.LogEvent)
	 */
	public Set<ProcessInstance> getInstancesForEvent(LogEvent event) {
		return mapEventToProcessInstances.get(event);
	}

	public Map<String, Set<String>> getConceptsAndInstances() {
		return Collections.unmodifiableMap(conceptsAndInstances);
	}

	private void addModelReferences(List<String> modelRefs, String instance) {
		if (modelRefs != null) {
			for (String uri : modelRefs) {
				Set<String> instances = conceptsAndInstances.get(uri);

				if (instances == null) {
					instances = new HashSet<String>();
					conceptsAndInstances.put(uri, instances);
				}
				instances.add(instance);
			}
		}
	}

	private void addModelReferences(DataSection data) {
		if (data != null) {
			for (String attributeName : data.keySet()) {
				addModelReferences(data.getModelReferences(attributeName), data
						.get(attributeName));
			}
		}
	}

	public OntologyCollection getOntologies() {
		if (!loadedOntologies) {
			loadedOntologies = true;
			ontologies = new OntologyCollection(this);
		}
		return ontologies;
	}
}
