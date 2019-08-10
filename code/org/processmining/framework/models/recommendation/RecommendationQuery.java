/**
 *
 */
package org.processmining.framework.models.recommendation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.classic.ProcessInstanceClassic;
import org.processmining.framework.models.recommendation.net.RecommendationQueryMarshal;
import org.processmining.framework.log.LogEvent;

/**
 * Data structure for storing a query requesting recommendation for scheduling
 * in a process management system.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class RecommendationQuery {

	// Host ID management
	protected static String hostId = "UNKNOWN_HOST";
	static {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostId = addr.getCanonicalHostName() + "(" + addr.getHostAddress()
					+ ")";
		} catch (UnknownHostException e) {
		}
	}

	protected String id;
	protected String processId;
	protected String processInstanceId;
	protected Map<String, String> processInstanceData;
	protected TreeSet<String> filterUsers;
	protected TreeSet<String> filterRoles;
	protected TreeSet<String> filterGroups;
	protected TreeSet<LogEvent> filterTasks;
	protected TreeSet<String> availableUsers;
	protected TreeSet<LogEvent> availableTasks;
	protected ArrayList<AuditTrailEntry> auditTrail;

	/**
	 * Creates an empty recommendation query with a known ID
	 */
	public RecommendationQuery(String queryId, String aProcessId,
			String aProcessInstanceId) {
		id = queryId;
		processId = aProcessId;
		processInstanceId = aProcessInstanceId;
		filterUsers = null;
		filterRoles = null;
		filterGroups = null;
		filterTasks = null;
		availableUsers = null;
		availableTasks = null;
		auditTrail = new ArrayList<AuditTrailEntry>();
		processInstanceData = null;
	}

	/**
	 * Creates a new, empty recommendation query
	 */
	public RecommendationQuery(String aProcessId, String aProcessInstanceId) {
		this("tempId", aProcessId, aProcessInstanceId);
		id = "QUERY_" + RecommendationQuery.hostId + "_"
				+ Long.toHexString(System.currentTimeMillis());
	}

	/**
	 * @param data
	 *            the data, i.e. attribute, map of the instance which the query
	 *            is based on.
	 */
	public void setProcessInstanceData(Map<String, String> data) {
		processInstanceData = data;
	}

	/**
	 * Adds a new attribute to the instance which the query is based on
	 * 
	 * @param key
	 *            Key of the attribute to be set
	 * @param value
	 *            Value of the attribute to be set
	 */
	public void setProcessInstanceAttribute(String key, String value) {
		if (processInstanceData == null) {
			processInstanceData = new HashMap<String, String>();
		}
		processInstanceData.put(key, value);
	}

	/**
	 * Adds a user for which results should be included
	 * 
	 * @param user
	 *            string representation of a user
	 */
	public void addFilterUser(String user) {
		if (filterUsers == null) {
			filterUsers = new TreeSet<String>();
		}
		filterUsers.add(user);
	}

	/**
	 * Adds a role for which results should be included
	 * 
	 * @param role
	 *            string representation of a role
	 */
	public void addFilterRole(String role) {
		if (filterRoles == null) {
			filterRoles = new TreeSet<String>();
		}
		filterRoles.add(role);
	}

	/**
	 * Adds a group for which results should be included
	 * 
	 * @param group
	 *            string representation of a group
	 */
	public void addFilterGroup(String group) {
		if (filterGroups == null) {
			filterGroups = new TreeSet<String>();
		}
		filterGroups.add(group);
	}

	/**
	 * Adds a task for which results should be included
	 * 
	 * @param task
	 *            string representation of a task
	 */
	public void addFilterTask(String task, String eventType) {
		if (filterTasks == null) {
			filterTasks = new TreeSet<LogEvent>();
		}
		filterTasks.add(new LogEvent(task, eventType));
	}

	/**
	 * Adds a user to the set of available resources to be used for
	 * recommendation.
	 * 
	 * @param user
	 *            string representation of a user
	 */
	public void addAvailableUser(String user) {
		if (availableUsers == null) {
			availableUsers = new TreeSet<String>();
		}
		availableUsers.add(user);
	}

	/**
	 * Adds a task to the set of available tasks to be used for recommendation.
	 * 
	 * @param task
	 *            string representation of a task
	 */
	public void addAvailableTask(String task, String eventType) {
		if (availableTasks == null) {
			availableTasks = new TreeSet<LogEvent>();
		}
		availableTasks.add(new LogEvent(task, eventType));
	}

	/**
	 * Adds an audit trail entry, or event, to the current incomplete audit
	 * trail of the process instance for which the query is being issued.
	 * 
	 * @param ate
	 *            an audit trail entry (event).
	 */
	public void addAuditTrailEntry(AuditTrailEntry ate) {
		auditTrail.add(ate);
	}

	/**
	 * @return A string containing the unique ID of this query (used for
	 *         correlation)
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the unique ID of the process this query refers to
	 */
	public String getProcessId() {
		return processId;
	}

	/**
	 * @return the unique ID of the process instance this query refers to
	 */
	public String getProcessInstanceId() {
		return processInstanceId;
	}

	/**
	 * @return A sorted set of strings specifying the users on which the result
	 *         should be filtered
	 */
	public SortedSet<String> getFilterUsers() {
		return filterUsers;
	}

	/**
	 * @return A sorted set of strings specifying the roles on which the result
	 *         should be filtered
	 */
	public SortedSet<String> getFilterRoles() {
		return filterRoles;
	}

	/**
	 * @return A sorted set of strings specifying the groups on which the result
	 *         should be filtered
	 */
	public SortedSet<String> getFilterGroups() {
		return filterGroups;
	}

	/**
	 * @return A sorted set of strings specifying the tasks on which the result
	 *         should be filtered
	 */
	public SortedSet<LogEvent> getFilterTasks() {
		return filterTasks;
	}

	/**
	 * @return A sorted set of strings specifying the users currently available
	 *         for scheduling
	 */
	public SortedSet<String> getAvailableUsers() {
		return availableUsers;
	}

	/**
	 * @return A sorted set of strings specifying the tasks currently available
	 *         for scheduling
	 */
	public SortedSet<LogEvent> getAvailableTasks() {
		return availableTasks;
	}

	/**
	 * @return The current, incomplete, audit trail (i.e. sequence of events) of
	 *         the process instance for which the query is issued
	 */
	public List<AuditTrailEntry> getAuditTrail() {
		return auditTrail;
	}

	/**
	 * @return The data, i.e. attribute, map of the process instance this query
	 *         refers to
	 */
	public Map<String, String> getProcessInstanceData() {
		return processInstanceData;
	}

	public String toString() {
		try {
			return (new RecommendationQueryMarshal()).marshal(this);
		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR";
		}
	}

	public ProcessInstance toProcessInstance(String process) {
		return new ProcessInstanceClassic(process, "undefined", "undefined",
				new HashMap<String, String>(),
				(ArrayList<AuditTrailEntry>) (getAuditTrail()));
	}

}
