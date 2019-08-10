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

package org.processmining.framework.log;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.processmining.framework.models.ontology.OntologyCollection;

/**
 * A summary of a workflow log.
 * 
 * @author Peter van den Brand
 * @author Christian W. Guenther (christian at deckfour dot org)
 * @version 1.1
 */

public interface LogSummary {
	/**
	 * Returns information about the workflow log itself. Provides the
	 * 'description' attribute of the 'workflow' tag and the contents of the
	 * 'data' section.
	 * 
	 * @return information about the workflow log itself
	 */
	public InfoItem getWorkflowLog();

	/**
	 * Returns information about the source. Provides 'program' attribute of the
	 * 'source' tag and the contents of the 'data' section.
	 * 
	 * @return information about the source
	 */
	public InfoItem getSource();

	/**
	 * Returns information about all the processes in the log. Provides an array
	 * with one entry per process. Each entry contains the 'id' and
	 * 'description' attributes of the 'process' tag and the contents of the
	 * 'data' section.
	 * 
	 * @return information about the processes
	 */
	public InfoItem[] getProcesses();

	/**
	 * Returns the earliest timestamp in all audittrailentries of the given
	 * process
	 * 
	 * @param process
	 *            Name of the process
	 * @return Date null in case the log does not contain any timestamp for this
	 *         process
	 */
	public Date getStartTime(String process);

	/**
	 * Returns the latest timestamp in all audittrailentries of the given
	 * process
	 * 
	 * @param process
	 *            Name of the process
	 * @return Date null in case the log does not contain any timestamp for this
	 *         process
	 */
	public Date getEndTime(String process);

	/**
	 * Returns the total number of audit trail entries in the log.
	 * 
	 * @return the total number of audit trail entries in the log
	 */
	public int getNumberOfAuditTrailEntries();

	/**
	 * Returns the total number of process instances in the log.
	 * 
	 * @return the total number of process instances in the log
	 */
	public int getNumberOfProcessInstances();

	/**
	 * Returns the number of different process instances in the log.
	 * 
	 * @return the number different of process instances in the log
	 */
	public int getNumberOfUniqueProcessInstances();

	/**
	 * Returns an alphabetically sorted list of all event types in the log.
	 * 
	 * @return an alphabetically sorted list of all event types in the log
	 */
	public String[] getEventTypes();

	/**
	 * Returns an alphabetically sorted list of all model element names in the
	 * log.
	 * 
	 * @return an alphabetically sorted list of all model element names in the
	 *         log
	 */
	public String[] getModelElements();

	/**
	 * Returns an alphabetically sorted list of all originators in the log.
	 * 
	 * @return an alphabetically sorted list of all originators in the log
	 */
	public String[] getOriginators();

	/**
	 * Returns a list of all log events (unique model element name and event
	 * type combinations) in the log.
	 * 
	 * @return a list of all log events in the log
	 */
	public LogEvents getLogEvents();

	/**
	 * Returns a set of (references to) process instances, in which the provided
	 * log event occurs.
	 * 
	 * @param event
	 *            Log event to find instances for.
	 * @return A set of process instances.
	 */
	public Set<ProcessInstance> getInstancesForEvent(LogEvent event);

	/**
	 * Returns the set of log events occurring in the specified process
	 * instance.
	 * 
	 * @param instance
	 *            Process instance to find log events for.
	 * @return A set of log events.
	 */
	public Set<LogEvent> getEventsForInstance(ProcessInstance instance);

	/**
	 * Returns a Map, whose keys are the events which have been executed by the
	 * specified originator. The values for each key indicate how often the
	 * originator has performed the task.
	 * 
	 * @param originator
	 *            Originator as string.
	 * @return A hashmap with log events executed by the specified originator,
	 *         and the number of times that the originator performed each event.
	 */
	public Map<LogEvent, Integer> getEventsForOriginator(String originator);

	/**
	 * Returns the mapping between process instances occurrences and the names
	 * of the process instances that occurred a certain amount of times.
	 * 
	 * @return A mapping between process intance occurrences.
	 */
	public Map<Integer, Set<String>> getProcessInstancesOccurrences();

	/**
	 * Returns the mapping between the starting log events and the number of
	 * times they start a process instance.
	 * 
	 * @return A mapping between starting log events occurrence.
	 */
	public Map<LogEvent, Integer> getStartingLogEvents();

	/**
	 * Returns the mapping between the ending log events and the number of times
	 * they end a process instance.
	 * 
	 * @return A mapping between starting log events occurrence.
	 */
	public Map<LogEvent, Integer> getEndingLogEvents();

	/**
	 * Returns a set of all concept URIs used in the log.
	 * 
	 * @return a set of concept URIs
	 */
	public Map<String, Set<String>> getConceptsAndInstances();

	/**
	 * Returns the ontologies referenced in this log.
	 * 
	 * @return the ontologies referenced in this log.
	 */
	public OntologyCollection getOntologies();
}
