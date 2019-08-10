package org.processmining.analysis.performance.componentstate.model;

/*
 * Created on July. 02, 2007
 *
 * Author: Minseok Song
 * (c) 2006 Technische Universiteit Eindhoven, Minseok Song
 * all rights reserved
 *
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */
/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;
import java.util.Arrays;
import java.util.TreeSet;
import java.awt.Color;

import org.processmining.analysis.performance.componentstate.logutil.AbstractLogUnit;
import org.processmining.analysis.performance.componentstate.logutil.LogUnitList;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.LogReader;

import org.processmining.analysis.performance.componentstate.ui.*;

public class ComponentStateModel {

	public static final String STR_NONE = "None";
	public static final String ST_ORIG = "Originator";
	public static final String ST_TASK = "Task ID";
	public static final String ST_INST = "Instance ID";
	public static final String ST_EVEN = "Event";
	public static final String ST_DATA = "Data";

	public static final String STATISTICS_OVERALL = "Overall";

	// //// define standard states for all components:
	// states displayed in graphics:
	public static final String STATE_RUNNING = "Running";
	public static final String STATE_ASSIGNED = "Assigned";
	public static final String STATE_SUSPENDED = "Suspended";
	public static final String STATE_COMPLETED = "Completed";
	public static final String STATE_MANUALSKIP = "ManualSkipped";
	public static final String STATE_ABORTED = "Aborted";

	// activity states ignored in graphics
	public static final String STATE_INITIAL = "Initial";
	public static final String STATE_READY = "Ready";

	// originator states, plus "Assigned" state defined already above
	public static final String STATE_WORKING = "Working";
	public static final String STATE_UNASSIGNED = "Unassigned";

	// data states
	public static final String STATE_UNDEFINED = "Undefined";
	public static final String STATE_DEFINED = "Defined";
	public static final String STATE_UNCONFIRMED = "Unconfirmed";

	// state shapes:
	public static final String STATE_SHAPE_LANE = "Lane";
	public static final String STATE_SHAPE_SEPARATOR = "Separator";

	public static final String[] instanceStatesList = new String[] {
			STATE_RUNNING, STATE_ASSIGNED, STATE_SUSPENDED, STATE_COMPLETED,
			STATE_MANUALSKIP, STATE_ABORTED };// , STATE_INITIAL, STATE_READY =
	// out
	public static final String[] taskStatesList = new String[] { STATE_RUNNING,
			STATE_ASSIGNED, STATE_SUSPENDED, STATE_COMPLETED, STATE_MANUALSKIP,
			STATE_ABORTED };// , STATE_INITIAL, STATE_READY = out
	public static final String[] originatorStatesList = new String[] {
			STATE_WORKING, STATE_ASSIGNED, STATE_UNASSIGNED };// ,STATE_UNASSIGNED
	// = out, as it
	// not
	// determined by
	// a specific
	// event
	public static final String[] dataStatesList = new String[] { STATE_DEFINED,
			STATE_UNCONFIRMED, STATE_UNDEFINED };

	private String typeHashMap = ST_TASK;

	// temporal maps with states for selected instances or events
	private HashMap<AbstractLogUnit, ArrayList> assignedUsersMap = new HashMap<AbstractLogUnit, ArrayList>();

	private ArrayList instancesList = new ArrayList();
	private ArrayList assignedUsersList = new ArrayList();

	// temporal objects
	private HashMap<String, LogUnitList> taskMap = new HashMap<String, LogUnitList>();
	private HashMap<String, LogUnitList> instanceMap = new HashMap<String, LogUnitList>();
	private HashMap<String, LogUnitList> originatorMap = new HashMap<String, LogUnitList>();
	private HashMap<String, LogUnitList> eventMap = new HashMap<String, LogUnitList>();
	private HashMap<String, LogUnitList> dataMap = new HashMap<String, LogUnitList>();

	// temporal objects
	private HashMap<String, Date> taskDateMap = new HashMap<String, Date>();
	private HashMap<String, Date> instanceDateMap = new HashMap<String, Date>();
	private HashMap<String, Date> originatorDateMap = new HashMap<String, Date>();
	private HashMap<String, Date> eventDateMap = new HashMap<String, Date>();
	private HashMap<String, Date> dataDateMap = new HashMap<String, Date>();

	// temporal objects
	private HashMap<String, Date> taskEndDateMap = new HashMap<String, Date>();
	private HashMap<String, Date> instanceEndDateMap = new HashMap<String, Date>();
	private HashMap<String, Date> originatorEndDateMap = new HashMap<String, Date>();
	private HashMap<String, Date> eventEndDateMap = new HashMap<String, Date>();
	private HashMap<String, Date> dataEndDateMap = new HashMap<String, Date>();

	protected Date logBoundaryLeft = null;
	protected Date logBoundaryRight = null;

	protected LogReader inputLog;
	protected ArrayList<String> eventTypeToKeep;

	// maps with list of selected events for each element type
	// <event, state>
	protected HashMap<String, String> instanceSelectedEvents = new HashMap<String, String>();
	protected HashMap<String, String> taskSelectedEvents = new HashMap<String, String>();
	protected HashMap<String, String> originatorSelectedEvents = new HashMap<String, String>();
	protected HashMap<String, String> dataSelectedEvents = new HashMap<String, String>();

	// maps with list of selected colors for each state
	// <state, color>
	protected HashMap<String, Color> instanceColorMap = new HashMap<String, Color>();
	protected HashMap<String, Color> taskColorMap = new HashMap<String, Color>();
	protected HashMap<String, Color> origColorMap = new HashMap<String, Color>();
	protected HashMap<String, Color> dataColorMap = new HashMap<String, Color>();

	// maps with list of selected priority for each state
	// <state, priority>
	protected HashMap<String, Integer> instancePriorityMap = new HashMap<String, Integer>();
	protected HashMap<String, Integer> taskPriorityMap = new HashMap<String, Integer>();
	protected HashMap<String, Integer> origPriorityMap = new HashMap<String, Integer>();
	protected HashMap<String, Integer> dataPriorityMap = new HashMap<String, Integer>();

	private ArrayList instanceIDs;

	public ComponentStateModel(LogReader aInputLog) {
		inputLog = aInputLog;
		construct();
	}

	public ComponentStateModel(LogReader aInputLog,
			ArrayList<String> aEventTypeToKeep, ArrayList anInstanceIDs) {
		eventTypeToKeep = aEventTypeToKeep;
		instanceIDs = anInstanceIDs;
		inputLog = aInputLog;
	}

	public LogReader getLogReader() {
		return inputLog;
	}

	public HashMap<String, LogUnitList> getItemMap() {
		if (typeHashMap.equals(ST_INST)) {
			// return instanceMap;
			return instanceMap;
		} else if (typeHashMap.equals(ST_ORIG)) {
			// return originatorMap;
			return originatorMap;
		} else if (typeHashMap.equals(ST_TASK)) {
			// return taskMap;
			return taskMap;
		} else if (typeHashMap.equals(ST_EVEN)) {
			// return eventMap;
			return eventMap;
		} else if (typeHashMap.equals(ST_DATA)) {
			// return dataMap;
			return dataMap;
		}
		return null;
	}

	public HashMap<String, LogUnitList> getSortedItemMap() {
		if (typeHashMap.equals(ST_INST)) {
			// return instanceMap;
			return getSortedMap(instanceMap);
		} else if (typeHashMap.equals(ST_ORIG)) {
			// return originatorMap;
			return getSortedMap(originatorMap);
		} else if (typeHashMap.equals(ST_TASK)) {
			// return taskMap;
			return getSortedMap(taskMap);
		} else if (typeHashMap.equals(ST_EVEN)) {
			// return eventMap;
			return getSortedMap(eventMap);
		} else if (typeHashMap.equals(ST_DATA)) {
			// return dataMap;
			return getSortedMap(dataMap);
		}
		return null;
	}

	public HashMap<String, LogUnitList> getItemMap(String type) {
		if (type.equals(ST_INST)) {
			// return instanceMap;
			return getSortedMap(instanceMap);
		} else if (type.equals(ST_ORIG)) {
			// return originatorMap;
			return getSortedMap(originatorMap);
		} else if (type.equals(ST_TASK)) {
			// return taskMap;
			return getSortedMap(taskMap);
		} else if (type.equals(ST_EVEN)) {
			// return eventMap;
			return getSortedMap(eventMap);
		} else if (type.equals(ST_DATA)) {
			// return dataMap;
			return getSortedMap(dataMap);
		}
		return null;
	}

	public HashMap<String, Date> getStartDateMap(String type) {
		if (type.equals(ST_INST)) {
			return instanceDateMap;
		} else if (type.equals(ST_ORIG)) {
			return originatorDateMap;
		} else if (type.equals(ST_TASK)) {
			return taskDateMap;
		} else if (type.equals(ST_EVEN)) {
			return eventDateMap;
		} else if (type.equals(ST_DATA)) {
			return dataDateMap;
		}
		return null;
	}

	public HashMap<String, Date> getEndDateMap(String type) {
		if (type.equals(ST_INST)) {
			return instanceEndDateMap;
		} else if (type.equals(ST_ORIG)) {
			return originatorEndDateMap;
		} else if (type.equals(ST_TASK)) {
			return taskEndDateMap;
		} else if (type.equals(ST_EVEN)) {
			return eventEndDateMap;
		} else if (type.equals(ST_DATA)) {
			return dataEndDateMap;
		}
		return null;
	}

	public void setTypeHashMap(String typeMap) {
		typeHashMap = typeMap;
	}

	public String getTypeHashMap() {
		return typeHashMap;
	}

	public Date getLogBoundaryLeft() { // if(logBoundaryLeft!=null)
		return logBoundaryLeft;
		// return new Date(0);
	}

	public Date getLogBoundaryRight() { // if(logBoundaryRight!=null)
		return logBoundaryRight;
		// return new Date(0);
	}

	public ArrayList<String> getEventTypeToKeep() {
		return eventTypeToKeep;
	}

	public void setEventTypeToKeep(ArrayList<String> aEventTypeToKeep) {
		this.eventTypeToKeep = aEventTypeToKeep;
	}

	public ArrayList getInstanceTypeToKeep() {
		return instanceIDs;
	}

	public void setInstanceTypeToKeep(ArrayList anInstanceIDs) {
		this.instanceIDs = anInstanceIDs;
	}

	// =============================================================================
	// methods for element STATE

	public HashMap getSelectedEventsForStates(String type) {

		if (type.equals(ST_INST)) {
			return instanceSelectedEvents;
		} else if (type.equals(ST_ORIG)) {
			return originatorSelectedEvents;
		} else if (type.equals(ST_TASK)) {
			return taskSelectedEvents;
		} else if (type.equals(ST_DATA)) {
			return dataSelectedEvents;
		}
		return null;
	}

	public void setSelectedEventsForStates(String type,
			HashMap selectedEventsMap) {

		if (type.equals(ST_INST)) {
			this.instanceSelectedEvents = selectedEventsMap;
		} else if (type.equals(ST_ORIG)) {
			this.originatorSelectedEvents = selectedEventsMap;
		} else if (type.equals(ST_TASK)) {
			this.taskSelectedEvents = selectedEventsMap;
		} else if (type.equals(ST_DATA)) {
			this.dataSelectedEvents = selectedEventsMap;
		}
	}

	public String[] getElementStatesList(String type) {
		if (type.equals(ST_INST)) {
			return instanceStatesList;
		} else if (type.equals(ST_ORIG)) {
			return originatorStatesList;
		} else if (type.equals(ST_TASK)) {
			return taskStatesList;
		} else if (type.equals(ST_DATA)) {
			return dataStatesList;
		}
		return null;
	}

	public ArrayList getHelpListElement(LogUnitList elementList) {

		ArrayList helpList = new ArrayList();
		// make copy after LogUnitList that contains all events of the element,
		// to use it for finding the End event for a Lane
		for (Iterator iter = elementList.iterator(); iter.hasNext();) {
			AbstractLogUnit item = (AbstractLogUnit) iter.next();
			helpList.add(item);
		}
		return helpList;
	}

	public HashMap getStateColorMap(String type) {

		if (type.equals(ST_INST)) {
			return instanceColorMap;
		} else if (type.equals(ST_ORIG)) {
			return origColorMap;
		} else if (type.equals(ST_TASK)) {
			return taskColorMap;
		} else if (type.equals(ST_DATA)) {
			return dataColorMap;
		}
		return null;
	}

	public void setStateColorMap(String type, HashMap selectedColorMap) {

		if (type.equals(ST_INST)) {
			this.instanceColorMap = selectedColorMap;
		} else if (type.equals(ST_ORIG)) {
			this.origColorMap = selectedColorMap;
		} else if (type.equals(ST_TASK)) {
			this.taskColorMap = selectedColorMap;
		} else if (type.equals(ST_DATA)) {
			this.dataColorMap = selectedColorMap;
		}
	}

	public HashMap getStatePriorityMap(String type) {

		if (type.equals(ST_INST)) {
			return instancePriorityMap;
		} else if (type.equals(ST_ORIG)) {
			return origPriorityMap;
		} else if (type.equals(ST_TASK)) {
			return taskPriorityMap;
		} else if (type.equals(ST_DATA)) {
			return dataPriorityMap;
		}
		return null;
	}

	public void setStatePriorityMap(String type, HashMap selectedPriorityMap) {

		if (type.equals(ST_INST)) {
			this.instancePriorityMap = selectedPriorityMap;
		} else if (type.equals(ST_ORIG)) {
			this.origPriorityMap = selectedPriorityMap;
		} else if (type.equals(ST_TASK)) {
			this.taskPriorityMap = selectedPriorityMap;
		} else if (type.equals(ST_DATA)) {
			this.dataPriorityMap = selectedPriorityMap;
		}
	}

	public HashMap getAssignedUsersMap() {
		return assignedUsersMap;
	}

	/**
	 * construct component state model
	 */

	public void construct() {
		// initialize list that is used to keep track of the data-elements that
		// appear in the process log
		inputLog.reset();
		LogUnitList.resetIdCounter();
		AbstractLogUnit event = null;

		// originators
		String[] originators = inputLog.getLogSummary().getOriginators();
		ArrayList originatorList = new ArrayList(Arrays.asList(originators));
		LogUnitList[] logUnitList = new LogUnitList[originators.length];
		for (int i = 0; i < originators.length; i++) {
			logUnitList[i] = new LogUnitList();
		}

		// tasks
		String[] tasks = inputLog.getLogSummary().getModelElements();
		ArrayList taskList = new ArrayList(Arrays.asList(tasks));
		LogUnitList[] logUnitforTaskList = new LogUnitList[tasks.length];
		for (int i = 0; i < tasks.length; i++) {
			logUnitforTaskList[i] = new LogUnitList();
		}

		// events
		String[] events = inputLog.getLogSummary().getEventTypes();
		ArrayList eventList = new ArrayList(Arrays.asList(events));
		LogUnitList[] logUnitforEventList = new LogUnitList[events.length];
		for (int i = 0; i < events.length; i++) {
			logUnitforEventList[i] = new LogUnitList();
		}

		// list to store events that have already an external event for
		// originators
		// ArrayList evListForExternalEvents = null;
		// HashMap <String, ArrayList> origExternalMap = new HashMap<String,
		// ArrayList>();

		Iterator it = inputLog.instanceIterator();

		while (it.hasNext()) {
			ProcessInstance pi = (ProcessInstance) it.next();
			if (pi.getName() != null) {
				instancesList.add(pi.getName());
			}

			Iterator ates = pi.getAuditTrailEntryList().iterator();
			LogUnitList logUnitListforInstance = new LogUnitList();
			while (ates.hasNext()) {
				AuditTrailEntry ate = (AuditTrailEntry) ates.next();
				if (ate.getTimestamp() == null)
					continue;
				event = AbstractLogUnit.create(pi, ate);

				// for eventTYPES, populate the list will all related events for
				// each event type
				if (eventDateMap.get(event.getType()) == null
						|| eventDateMap.get(event.getType()).after(
								ate.getTimestamp())) {
					eventDateMap.remove(event.getType());
					eventDateMap.put(event.getType(), ate.getTimestamp());
				}
				if (eventEndDateMap.get(event.getType()) == null
						|| eventEndDateMap.get(event.getType()).before(
								ate.getTimestamp())) {
					eventEndDateMap.remove(event.getType());
					eventEndDateMap.put(event.getType(), ate.getTimestamp());
				}
				logUnitforEventList[eventList.indexOf(event.getType())]
						.addEvent(event);

				// if there is a specific VARIABLE/DATA event, don't add to task
				// list
				// propose these as specific data events!!!
				if (event.getType().equalsIgnoreCase("update")
						|| event.getType().equalsIgnoreCase("define")
						|| event.getType().equalsIgnoreCase("rollback")
						|| event.getType().equalsIgnoreCase("confirm")) {
					// these are DATA specific events
					// do we need varDateMap & varEndDateMap?????

					if (dataMap.containsKey(event.getElement())) {
						((LogUnitList) dataMap.get(event.getElement()))
								.addEvent(event);
					} else {
						LogUnitList tempLogUnitList = new LogUnitList(event
								.getElement());
						tempLogUnitList.addEvent(event);
						dataMap.put(event.getElement(), tempLogUnitList);
					}
				}

				// get task/instance/originator events
				if (!event.getType().equalsIgnoreCase("update")
						&& !event.getType().equalsIgnoreCase("define")
						&& !event.getType().equalsIgnoreCase("rollback")
						&& !event.getType().equalsIgnoreCase("confirm")) {

					// ===========================================================================
					// for a process INSTANCE, add all events related...should
					// we keep only PI specific events??
					// or, in case there is no PI specific event, only TASK
					// ATEs...and exclude the DATA ate...

					if (instanceDateMap.get(event.getProcessInstance()
							.getName()) == null
							|| instanceDateMap.get(
									event.getProcessInstance().getName())
									.after(ate.getTimestamp())) {
						// instanceDateMap.remove(event.getProcessInstance().getName());
						instanceDateMap.put(event.getProcessInstance()
								.getName(), ate.getTimestamp());

					}
					if (instanceEndDateMap.get(event.getProcessInstance()
							.getName()) == null
							|| instanceEndDateMap.get(
									event.getProcessInstance().getName())
									.before(ate.getTimestamp())) {
						// instanceEndDateMap.remove(event.getProcessInstance().getName());
						instanceEndDateMap.put(event.getProcessInstance()
								.getName(), ate.getTimestamp());

					}
					logUnitListforInstance.addEvent(event);

					// get TASK events, that may have also process events
					if (taskDateMap.get(event.getElement()) == null
							|| taskDateMap.get(event.getElement()).after(
									ate.getTimestamp())) {
						taskDateMap.remove(event.getElement());
						taskDateMap.put(event.getElement(), ate.getTimestamp());
					}
					if (taskEndDateMap.get(event.getElement()) == null
							|| taskEndDateMap.get(event.getElement()).before(
									ate.getTimestamp())) {
						taskEndDateMap.remove(event.getElement());
						taskEndDateMap.put(event.getElement(), ate
								.getTimestamp());
					}

					logUnitforTaskList[taskList.indexOf(event.getElement())]
							.addEvent(event);

					// for ORIGINATOR

					if (originatorList.indexOf(event.getOriginator()) >= 0) {
						if (originatorDateMap.get(event.getOriginator()) == null
								|| originatorDateMap.get(event.getOriginator())
										.after(ate.getTimestamp())) {
							originatorDateMap.remove(event.getOriginator());
							originatorDateMap.put(event.getOriginator(), ate
									.getTimestamp());
						}
						if (originatorEndDateMap.get(event.getOriginator()) == null
								|| originatorEndDateMap.get(
										event.getOriginator()).before(
										ate.getTimestamp())) {
							originatorEndDateMap.remove(event.getOriginator());
							originatorEndDateMap.put(event.getOriginator(), ate
									.getTimestamp());
						}
						logUnitList[originatorList.indexOf(event
								.getOriginator())].addEvent(event);

					}

					// if
					// ((event.getType().equalsIgnoreCase("assign"))||(event.getType().equalsIgnoreCase("reassign"))){

					// must add the assigned users to the list of originators,
					// with the related assign (or ANY other) event
					// assigned users may be set as "assignedUser" attributes
					// for the ATE

					Iterator iter = ate.getAttributes().keySet().iterator();
					// run through attributes
					while (iter.hasNext()) {
						String assignedUser = (String) iter.next();
						String assignedUserName = (String) ate.getAttributes()
								.get(assignedUser);

						if ((assignedUserName != null)
								&& (assignedUserName != "")
								&& (assignedUser.contains("assignedUser"))) {
							if (originatorDateMap.get(assignedUserName) == null
									|| originatorDateMap.get(assignedUserName)
											.after(ate.getTimestamp())) {
								originatorDateMap.remove(assignedUserName);
								originatorDateMap.put(assignedUserName, ate
										.getTimestamp());
							}
							if (originatorEndDateMap.get(assignedUserName) == null
									|| originatorEndDateMap.get(
											assignedUserName).before(
											ate.getTimestamp())) {
								originatorEndDateMap.remove(assignedUserName);
								originatorEndDateMap.put(assignedUserName, ate
										.getTimestamp());
							}

							// add orig to assigned users list
							ArrayList users = new ArrayList();
							if (assignedUsersMap.containsKey(event)) {
								users = assignedUsersMap.get(event);
							}
							users.add(assignedUserName);
							assignedUsersMap.put(event, users);

							if (originatorList.contains(assignedUserName)
									&& (originatorList
											.indexOf(assignedUserName) < logUnitList.length)) {
								// add the assign event to the originator events
								// list
								logUnitList[originatorList
										.indexOf(assignedUserName)]
										.addEvent(event);
							} else { // add user to originator list if it is not
								// equal to the empty String
								originatorList.add(assignedUserName);
								LogUnitList tempLogUnitList = new LogUnitList(
										assignedUserName);
								tempLogUnitList.addEvent(event);
								// assignedUser not added to "originators" array
								// that has a fixed size, only to
								// originatorList!
								originatorMap.put(assignedUserName,
										tempLogUnitList);

							}
						}

						// ============ originator external
						// events===================

						// look for all originators related to the Task & PI of
						// the new event,
						// and add this event to their list, if it doesn't exist
						// already
						LogUnitList userEventsList = null;

						// System.out.println(" csModel:  look for external Orig event");
						for (Iterator iter2 = originatorList.iterator(); iter2
								.hasNext();) {
							String userName = (String) iter2.next();
							// System.out.println(" csModel:  look for external Orig event for ORIG= "+userName);

							if (originatorList.contains(userName)
									&& (originatorList.indexOf(userName) < logUnitList.length)) {
								userEventsList = logUnitList[originatorList
										.indexOf(userName)];
							} else if (originatorMap.containsKey(userName)) {
								userEventsList = (LogUnitList) originatorMap
										.get(userName);
							}

							// System.out.println(" csModel:  userEventsList = "+
							// userEventsList.size());
							if ((userEventsList != null)
									&& (userEventsList.size() >= 1)) {

								// System.out.println(" csModel:  ORIG = "+userName+" has userEventsList not empty!!!!!");
								if (!userEventsList.containsEvent(event)) {

									// System.out.println(" csModel:  userEventsList not includes event= "+event.getType()
									// );
									AbstractLogUnit item = null;

									boolean addExternal = false;
									for (Iterator ite = userEventsList
											.iterator(); ite.hasNext();) {
										item = (AbstractLogUnit) ite.next();
										if (item.getElement().equals(
												event.getElement())
												&& item
														.getProcessInstance()
														.equals(
																event
																		.getProcessInstance())) {

											addExternal = true;
											// System.out.println(" csModel:  userEventsList has events related to event= "+event.getType()
											// );
										}
									}

									// add event only if it relates to same
									// Element&PI, and its the First external
									// event for this task
									// we don't know which one is the FIRST
									// SELECTED external event!!!!!!..so we
									// include all of them
									if (addExternal) {
										userEventsList.addEvent(event);
										// System.out.println(" EXTERNAL related event = "+event.getType()+" added to originatorMap list of orig = "+userName);
									}

								}// else
								// System.out.println("Orig related event already in userEventsList");
							}

							else {
								// System.out.println("userEventsList is empty or null!!");
							}

						}

					}
				}

			}
			instanceMap.put(pi.getName(), logUnitListforInstance);
		}
		for (int i = 0; i < tasks.length; i++) {
			taskMap.put(tasks[i], logUnitforTaskList[i]);
		}
		for (int i = 0; i < events.length; i++) {
			eventMap.put(events[i], logUnitforEventList[i]);
		}
		for (int i = 0; i < originators.length; i++) {
			originatorMap.put(originators[i], logUnitList[i]);
			// the assignedUsers were added to the originatorList, and already
			// put into the originatorMap
		}
	}

	/*
	 * public ArrayList getExternalEvents(String userName){
	 * 
	 * LogUnitList userEventsList = null; ArrayList externalEvents = new
	 * ArrayList();
	 * 
	 * // list to store events that have already an external event for
	 * originators ArrayList evListForExternalEvents = new ArrayList(); //
	 * HashMap <String, ArrayList> origExternalMap = new HashMap<String,
	 * ArrayList>();
	 * 
	 * 
	 * if (originatorMap.containsKey(userName)){ userEventsList =
	 * (LogUnitList)originatorMap.get(userName); }
	 * 
	 * ///// ==============we have to verify for each event if its an external
	 * event, then to see if its the first :(( // not good !!1
	 * 
	 * }
	 */

	// =================================== SORT
	// MAPS=======================================================
	public HashMap<String, LogUnitList> getSortedMap(
			HashMap<String, LogUnitList> aMap) {

		ArrayList<AbstractLogUnit> arrayList = new ArrayList<AbstractLogUnit>();
		HashMap<String, LogUnitList> sortedMap = new HashMap<String, LogUnitList>();
		AbstractLogUnit item, ev;
		String key = null;

		// add items to the tree set
		for (Iterator itSets = aMap.keySet().iterator(); itSets.hasNext();) {
			key = (String) itSets.next();

			LogUnitList tempLogUnitList = (LogUnitList) aMap.get(key);
			tempLogUnitList.resetPositionOfItems();

			arrayList.clear();

			// first add items to a HashSet
			for (Iterator itItm = tempLogUnitList.iterator(); itItm.hasNext();) {
				item = (AbstractLogUnit) itItm.next();

				if (arrayList.size() != 0) {

					if (arrayList.get(arrayList.size() - 1)
							.getCurrentTimeStamp().before(
									item.getCurrentTimeStamp())) {
						arrayList.add(item);
						continue;
					} else if (!arrayList.get(arrayList.size() - 1)
							.getCurrentTimeStamp().after(
									item.getCurrentTimeStamp())) {
						arrayList.add(item);
						continue;
					}
					if (arrayList.get(0).getCurrentTimeStamp().after(
							item.getCurrentTimeStamp())) {
						arrayList.add(0, item);
						continue;
					} else if (!arrayList.get(0).getCurrentTimeStamp().before(
							item.getCurrentTimeStamp())) {
						arrayList.add(0, item);
						continue;
					}

					int x_min = 0;
					int x_max = arrayList.size();
					int x_mean;

					while (true) {
						int x_temp;
						x_mean = (x_min + x_max) / 2;
						if (arrayList.get(x_mean).getCurrentTimeStamp().before(
								item.getCurrentTimeStamp())) {
							if (x_min == (x_mean + x_max) / 2) {
								arrayList.add(x_min + 1, item);
								break;
							}
							x_min = x_mean;
						} else if (arrayList.get(x_mean).getCurrentTimeStamp()
								.after(item.getCurrentTimeStamp())) {
							if (x_min == (x_min + x_mean) / 2) {
								arrayList.add(x_min + 1, item);
								break;
							}
							x_max = x_mean;
						} else {
							arrayList.add(x_mean + 1, item);
							break;
						}

					}
				} else {
					arrayList.add(item);
				}
			}

			// make the sorted list and put it in the HashMap
			LogUnitList sortedList = new LogUnitList();
			for (Iterator itr = arrayList.iterator(); itr.hasNext();) {
				ev = (AbstractLogUnit) itr.next();
				sortedList.addEvent(ev);
			}
			sortedMap.put(key, sortedList);

		}

		return sortedMap;

	}

	public Date getEndPITimestamp(String aInstance) {
		LogUnitList sortedPIList = new LogUnitList();
		AbstractLogUnit item = null;

		sortedPIList = (LogUnitList) getItemMap(ST_INST).get(aInstance);
		if (sortedPIList != null && sortedPIList.size() > 0) {
			for (Iterator it = sortedPIList.iterator(); it.hasNext();) {
				item = (AbstractLogUnit) it.next();
			}
		}

		if (item != null)
			return item.getCurrentTimeStamp();
		else
			return null;

	}

	public void calculateCurrentTimeLogical() {
		ArrayList<AbstractLogUnit> arrayList = new ArrayList<AbstractLogUnit>();
		AbstractLogUnit item;
		String key = null;

		// add items to the tree set
		for (Iterator itSets = getItemMap().keySet().iterator(); itSets
				.hasNext();) {
			key = (String) itSets.next();
			if (typeHashMap.equals(ST_INST) && !instanceIDs.contains(key))
				continue;
			LogUnitList tempLogUnitList = (LogUnitList) getItemMap().get(key);
			tempLogUnitList.resetPositionOfItems();
			// first add items to a HashSet
			for (Iterator itItm = tempLogUnitList.iterator(); itItm.hasNext();) {
				item = (AbstractLogUnit) itItm.next();
				// todo
				if (eventTypeToKeep != null && (// !eventTypeToKeep.contains(item.getType())
						// ||
						!instanceIDs.contains(item.getProcessInstance()
								.getName())))
					continue;
				if (arrayList.size() != 0) {

					if (arrayList.get(arrayList.size() - 1)
							.getActualTimeStamp().before(
									item.getActualTimeStamp())) {
						arrayList.add(item);
						continue;
					} else if (!arrayList.get(arrayList.size() - 1)
							.getActualTimeStamp().after(
									item.getActualTimeStamp())) {
						arrayList.add(item);
						continue;
					}
					if (arrayList.get(0).getActualTimeStamp().after(
							item.getActualTimeStamp())) {
						arrayList.add(0, item);
						continue;
					} else if (!arrayList.get(0).getActualTimeStamp().before(
							item.getActualTimeStamp())) {
						arrayList.add(0, item);
						continue;
					}

					int x_min = 0;
					int x_max = arrayList.size();
					int x_mean;

					while (true) {
						int x_temp;
						x_mean = (x_min + x_max) / 2;
						if (arrayList.get(x_mean).getActualTimeStamp().before(
								item.getActualTimeStamp())) {
							if (x_min == (x_mean + x_max) / 2) {
								arrayList.add(x_min + 1, item);
								break;
							}
							x_min = x_mean;
						} else if (arrayList.get(x_mean).getActualTimeStamp()
								.after(item.getActualTimeStamp())) {
							if (x_min == (x_min + x_mean) / 2) {
								arrayList.add(x_min + 1, item);
								break;
							}
							x_max = x_mean;
						} else {
							arrayList.add(x_mean + 1, item);
							break;
						}

					}
				} else {
					arrayList.add(item);
				}
			}
		}

		// find the position of a event
		if (arrayList.size() > 0) {
			arrayList.get(0).setPosition(0);
			arrayList.get(0).setCurrentTimeStampLogical();
			for (int i = 1; i < arrayList.size(); i++) {
				AbstractLogUnit abs = (AbstractLogUnit) arrayList.get(i);
				AbstractLogUnit abs0 = (AbstractLogUnit) arrayList.get(i - 1);
				if (!abs0.getActualTimeStamp().before(abs.getActualTimeStamp())) {
					abs.setPosition(abs0.getPosition());
				} else
					abs.setPosition(i);
				abs.setCurrentTimeStampLogical();
			}
		}
	}

	public void calculateCurrentTimeLogical_Relative() {

		String key = null;

		for (Iterator itSets = getItemMap().keySet().iterator(); itSets
				.hasNext();) {
			key = (String) itSets.next();
			if (typeHashMap.equals(ST_INST) && !instanceIDs.contains(key))
				continue;
			LogUnitList tempLogUnitList = (LogUnitList) getItemMap().get(key);
			tempLogUnitList.resetRelativePositionOfItems();
			// first add items to a HashSet
			ArrayList<AbstractLogUnit> arrayList = new ArrayList<AbstractLogUnit>();
			AbstractLogUnit item;
			for (Iterator itItm = tempLogUnitList.iterator(); itItm.hasNext();) {
				item = (AbstractLogUnit) itItm.next();

				if (eventTypeToKeep != null && (// !eventTypeToKeep.contains(item.getType())
						// ||
						!instanceIDs.contains(item.getProcessInstance()
								.getName())))
					continue;
				if (arrayList.size() != 0) {

					if (arrayList.get(arrayList.size() - 1)
							.getActualTimeStamp().before(
									item.getActualTimeStamp())) {
						arrayList.add(item);
						continue;
					} else if (!arrayList.get(arrayList.size() - 1)
							.getActualTimeStamp().after(
									item.getActualTimeStamp())) {
						arrayList.add(item);
						continue;
					}
					if (arrayList.get(0).getActualTimeStamp().after(
							item.getActualTimeStamp())) {
						arrayList.add(0, item);
						continue;
					} else if (!arrayList.get(0).getActualTimeStamp().before(
							item.getActualTimeStamp())) {
						arrayList.add(0, item);
						continue;
					}

					int x_min = 0;
					int x_max = arrayList.size();
					int x_mean;

					while (true) {
						x_mean = (x_min + x_max) / 2;
						if (arrayList.get(x_mean).getActualTimeStamp().before(
								item.getActualTimeStamp())) {
							if (x_min == (x_mean + x_max) / 2) {
								arrayList.add(x_min + 1, item);
								break;
							}
							x_min = x_mean;
						} else if (arrayList.get(x_mean).getActualTimeStamp()
								.after(item.getActualTimeStamp())) {
							if (x_min == (x_min + x_mean) / 2) {
								arrayList.add(x_min + 1, item);
								break;
							}
							x_max = x_mean;
						} else {
							arrayList.add(x_mean + 1, item);
							break;
						}

					}
				} else {
					arrayList.add(item);
				}
			}
			// find the position of a event
			if (arrayList.size() > 0) {
				arrayList.get(0).setRelativePosition(0);
				arrayList.get(0).setCurrentTimeStampLogicalRelative();
				for (int i = 1; i < arrayList.size(); i++) {
					AbstractLogUnit abs = (AbstractLogUnit) arrayList.get(i);
					AbstractLogUnit abs0 = (AbstractLogUnit) arrayList
							.get(i - 1);
					if (!abs0.getActualTimeStamp().before(
							abs.getActualTimeStamp())) {
						abs.setRelativePosition(abs0.getRelativePosition());
					} else
						abs.setRelativePosition(i);
					abs.setCurrentTimeStampLogicalRelative();
				}
			}

		}
	}

	public void setLogicalRelativeTime() {
		// paint items
		if (getItemMap().size() <= 0)
			return;

		String key = null;
		AbstractLogUnit item = null;

		// iterate through sets
		int index = -1;
		for (Iterator itSets = getItemMap().keySet().iterator(); itSets
				.hasNext();) {
			index++;
			key = (String) itSets.next();

			if (typeHashMap.equals(ST_INST) && !instanceIDs.contains(key))
				continue;

			LogUnitList tempLogUnit = (LogUnitList) getItemMap().get(key);

			for (Iterator itItm = tempLogUnit.iterator(); itItm.hasNext();) {
				item = (AbstractLogUnit) itItm.next();
				// todo
				if (eventTypeToKeep != null
						&& (!eventTypeToKeep.contains(item.getType()) || !instanceIDs
								.contains(item.getProcessInstance().getName())))
					continue;

				item.setCurrentTimeStampLogicalRelative();
			}
		}
	}

	public void setRelativeTime() {
		// paint items
		if (getItemMap().size() <= 0)
			return;

		String key = null;
		AbstractLogUnit item = null;

		// iterate through sets
		int index = -1;
		for (Iterator itSets = getItemMap().keySet().iterator(); itSets
				.hasNext();) {
			index++;
			key = (String) itSets.next();

			if (typeHashMap.equals(ST_INST) && !instanceIDs.contains(key))
				continue;

			LogUnitList tempLogUnit = (LogUnitList) getItemMap().get(key);

			for (Iterator itItm = tempLogUnit.iterator(); itItm.hasNext();) {
				item = (AbstractLogUnit) itItm.next();
				// todo
				if (eventTypeToKeep != null
						&& (!eventTypeToKeep.contains(item.getType()) || !instanceIDs
								.contains(item.getProcessInstance().getName())))
					continue;

				item.setCurrentTimeStampRelative();
			}
		}
	}

	public void setLogicalTime() {
		// paint items
		if (getItemMap().size() <= 0)
			return;

		String key = null;
		AbstractLogUnit item = null;

		// iterate through sets
		int index = -1;
		for (Iterator itSets = getItemMap().keySet().iterator(); itSets
				.hasNext();) {
			index++;
			key = (String) itSets.next();

			if (typeHashMap.equals(ST_INST) && !instanceIDs.contains(key))
				continue;

			LogUnitList tempLogUnit = (LogUnitList) getItemMap().get(key);

			for (Iterator itItm = tempLogUnit.iterator(); itItm.hasNext();) {
				item = (AbstractLogUnit) itItm.next();
				// todo
				if (eventTypeToKeep != null
						&& (!eventTypeToKeep.contains(item.getType()) || !instanceIDs
								.contains(item.getProcessInstance().getName())))
					continue;

				item.setCurrentTimeStampLogical();
			}
		}
	}

	/**
	 * consolidates the global log viewing area. Iterates through all contained
	 * subsets and compares their boundaries to the current log boundaries set.
	 * If necessary, global log boundaries are adjusted to the outside (i.e.,
	 * only extended)
	 */
	public void adjustLogBoundaries(String timeOption) {
		LogUnitList aLogUnitList = null;
		Date dStart = null;
		Date dEnd = null;
		logBoundaryLeft = null;
		logBoundaryRight = null;
		if (timeOption.equals(ComponentStatePanel.TIME_RELATIVE_RATIO)) {
			logBoundaryLeft = new Date(0);
			logBoundaryRight = new Date(10000);
		} else {
			for (Iterator it = getItemMap().values().iterator(); it.hasNext();) {
				aLogUnitList = (LogUnitList) it.next();
				dStart = aLogUnitList.getLeftBoundaryTimestamp(eventTypeToKeep,
						instanceIDs);
				dEnd = aLogUnitList.getRightBoundaryTimestamp(eventTypeToKeep,
						instanceIDs);

				if (logBoundaryLeft == null
						|| (dStart != null && logBoundaryLeft.after(dStart))) {
					logBoundaryLeft = dStart;
				}
				if (logBoundaryRight == null
						|| (dEnd != null && logBoundaryRight.before(dEnd))) {
					logBoundaryRight = dEnd;
				}
			}

			// / if there is no event to display, make the boundary for 5 days =
			// default
			if (logBoundaryLeft == null)
				logBoundaryLeft = new Date(
						(new Date().getTime() - (3600 * 24 * 5)));
			if (logBoundaryRight == null)
				logBoundaryRight = new Date();
		}
	}

	public Date getStartDateofLogUniList(String key) {
		return ((LogUnitList) getItemMap().get(key)).getLeftBoundaryTimestamp(
				eventTypeToKeep, instanceIDs);
	}

	public Date getEndDateofLogUniList(String key) {
		return ((LogUnitList) getItemMap().get(key)).getRightBoundaryTimestamp(
				eventTypeToKeep, instanceIDs);
	}

	public int getNumberOfLogUnits(String key) {
		return ((LogUnitList) getItemMap().get(key)).size(eventTypeToKeep,
				instanceIDs);
	}
}
