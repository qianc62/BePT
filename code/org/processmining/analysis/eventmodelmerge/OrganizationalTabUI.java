package org.processmining.analysis.eventmodelmerge;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLResource;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.models.hlprocess.visualization.HLVisualization;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyStringTextarea;

/**
 * The organizational tab of the EventModelMerge plugin presents possibilities
 * to the user for in which way he wants to merge organizational information
 * when activities that refer to events are merged into activities that only
 * refer to activities.
 * 
 * @author rmans
 */
public class OrganizationalTabUI extends JPanel implements
		GuiNotificationTarget {

	/** the input simulation model */
	private HLModel myInputSimModel;

	/** the final simulation model */
	private HLModel myFinalSimModel;

	/**
	 * the mapping from the highlevelactivities in the final simulation model to
	 * the highlevelactivities in the input simulation model that are merged
	 */
	private HashMap<HLID, ArrayList<HLID>> myMapping;
	// private HashSet<EventType> eventTypes;

	/** the panel on which the graph is located */
	private JPanel myGraphPanel;

	/* GUI properties */
	private GUIPropertyListEnumeration currentOptionEnumerationList = null;
	// private GuiPropertyListRadio myUnionRadioList;
	// private JPanel eventsPanel;
	private final String DEFAULT_STRATEGY = "default strategy ('complete' activities)";
	private final String UNION_ALL_STRATEGY = "union of originators (all events)";
	private final String UNION_SELECTED_STRATEGY = "union of originators (selected events)";
	private EventCheckBoxList eventCheckBoxList = new EventCheckBoxList();

	public OrganizationalTabUI(HLModel inputSimModel, HLModel finalSimModel,
			HashMap<HLID, ArrayList<HLID>> mapping, JPanel graphPanel) {
		super();
		myInputSimModel = inputSimModel;
		myFinalSimModel = finalSimModel;
		myMapping = mapping;
		myGraphPanel = graphPanel;
		// initially copy the super set information (default case)
		initDefaults();
		// build GUI
		jbInit();
	}

	/**
	 * At plug-in startup the information from the complete low-level activity
	 * will be used for the target model only (user may choose different
	 * strategy later on).
	 */
	private void initDefaults() {
		removeGroupsFromFinalSimModel();
		Iterator<Entry<HLID, ArrayList<HLID>>> it = myMapping.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<HLID, ArrayList<HLID>> entry = it.next();
			ArrayList<HLID> values = entry.getValue();
			if (values != null && values.size() > 0) {
				for (int k = 0; k < values.size(); k++) {
					HLID actID = values.get(k);
					HLActivity act = myInputSimModel.getHLProcess()
							.getActivity(actID);
					if (act.getName().endsWith("complete")) {
						HLID targetActID = entry.getKey();
						HLActivity targetAct = myFinalSimModel.getHLProcess()
								.getActivity(targetActID);
						HLGroup group = act.getGroup();
						if (!group.getID().equals(
								myInputSimModel.getHLProcess()
										.getNobodyGroupID())) {
							HLGroup clonedGroup = (HLGroup) group.clone();
							// remove "complete" from a group name
							if (clonedGroup.getName().endsWith("complete")) {
								int lastIndex = clonedGroup.getName()
										.lastIndexOf("complete");
								clonedGroup.setName((clonedGroup.getName()
										.substring(0, lastIndex - 1)).trim());
							}
							targetAct.setGroup(clonedGroup.getID());
							myFinalSimModel.getHLProcess().addOrReplace(
									clonedGroup);
						}
					} else {
						myFinalSimModel.getHLProcess().removeActivity(actID);
					}
				}
			}
		}
		// copy the resources on the process level to the final simulation model
		Iterator<HLResource> resources = myInputSimModel.getHLProcess()
				.getResources().iterator();
		while (resources.hasNext()) {
			HLResource resource = resources.next();
			HLResource clonedResource = (HLResource) resource.clone();
			myFinalSimModel.getHLProcess().addOrReplace(clonedResource);
		}
	}

	private void removeGroupsFromFinalSimModel() {
		// initialize
		Iterator<HLGroup> groups = myFinalSimModel.getHLProcess().getGroups()
				.iterator();
		while (groups.hasNext()) {
			HLGroup grp = groups.next();
			myFinalSimModel.getHLProcess().removeGroup(grp.getID());
		}
	}

	private void unionAll() {

		removeGroupsFromFinalSimModel();
		Iterator<Entry<HLID, ArrayList<HLID>>> it = myMapping.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<HLID, ArrayList<HLID>> entry = it.next();
			ArrayList<HLID> values = entry.getValue();
			if (values != null && values.size() > 0) {
				HashMap<HLID, HLGroup> groups = new HashMap<HLID, HLGroup>();
				for (int k = 0; k < values.size(); k++) {
					HLID actID = values.get(k);
					HLActivity act = myInputSimModel.getHLProcess()
							.getActivity(actID);
					if (!groups.containsKey(act.getGroup().getID())) {
						groups.put(act.getGroup().getID(), act.getGroup());
					}
				}
				handleGroups(groups, entry);
			}
		}
	}

	private void unionSelected() {
		removeGroupsFromFinalSimModel();

		Iterator<Entry<HLID, ArrayList<HLID>>> it = myMapping.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<HLID, ArrayList<HLID>> entry = it.next();
			ArrayList<HLID> values = entry.getValue();
			if (values != null && values.size() > 0) {
				HashMap<HLID, HLGroup> groups = new HashMap<HLID, HLGroup>();
				for (int k = 0; k < values.size(); k++) {
					HLID actID = values.get(k);
					HLActivity act = myInputSimModel.getHLProcess()
							.getActivity(actID);
					boolean flag = false;
					if (eventCheckBoxList.is_checkBox_schedule()
							&& act.getName().endsWith("schedule")) {
						flag = true;
					} else if (eventCheckBoxList.is_checkBox_start()
							&& act.getName().endsWith("start")) {
						flag = true;
					} else if (eventCheckBoxList.is_checkBox_complete()
							&& act.getName().endsWith("complete")) {
						flag = true;
					}
					if (flag && !groups.containsKey(act.getGroup().getID())) {
						groups.put(act.getGroup().getID(), act.getGroup());
					}
				}
				handleGroups(groups, entry);

			}
		}
	}

	/*
	 * private void unionAll_ignore() { Iterator<Entry<HLID, ArrayList<HLID>>>
	 * it = myMapping.entrySet().iterator(); while (it.hasNext()) { Entry<HLID,
	 * ArrayList<HLID>> entry = it.next(); ArrayList<HLID> values =
	 * entry.getValue(); if (values != null && values.size() > 0) {
	 * HashMap<HLID, HLGroup> groups = new HashMap<HLID, HLGroup>(); for (int k
	 * = 0; k < values.size(); k++) { HLID actID = values.get(k); HLActivity act
	 * = myInputSimModel.getHLProcess().getActivity(actID); if
	 * (!groups.containsKey(act.getGroup().getID())) {
	 * groups.put(act.getGroup().getID(), act.getGroup()); } //HLActivity act =
	 * values.get(k); // if(!groups.containsKey(act.getGroup().getName())) //
	 * groups.put(act.getGroup().getName(), act.getGroup()); } if (groups.size()
	 * == 1) { HLID targetID = entry.getKey(); HLActivity act =
	 * myFinalSimModel.getHLProcess().getActivity(targetID);
	 * act.setGroup(groups.keySet().iterator().next());
	 * //entry.getKey().setGroup
	 * (groups.get((String)groups.keySet().iterator().next())); } else { HLGroup
	 * newGroup = new HLGroup("", myFinalSimModel.getHLProcess()); //HLGroup
	 * newGroup = new HLGroup(""); Set<String> newNameSet = new
	 * HashSet<String>(); // make a new name of group String[] key = (String[])
	 * groups.keySet().toArray(new String[groups.keySet().size()]); for (int i =
	 * 0; i < key.length; i++) { HLGroup tempGroup = groups.get(key[i]);
	 * String[] tempString = tempGroup.getName().split(":"); for (int j=0;
	 * j<tempString.length; j++) { if(!newNameSet.contains(tempString[j])) {
	 * newNameSet.add(tempString[j]); } } } ArrayList<String> tempArrayList =
	 * new ArrayList<String>(); tempArrayList.addAll(newNameSet);
	 * 
	 * HLID targetID = entry.getKey(); HLActivity act =
	 * myFinalSimModel.getHLProcess().getActivity(targetID);
	 * act.setGroup(newGroup.getID()); }
	 * 
	 * } } }
	 */
	private void handleGroups(HashMap<HLID, HLGroup> groups,
			Entry<HLID, ArrayList<HLID>> entry) {
		// if only one group -> no merging required
		if (groups.size() == 1) {
			HLID targetID = entry.getKey();
			HLActivity targetAct = myFinalSimModel.getHLProcess().getActivity(
					targetID);
			HLGroup group = (HLGroup) groups.get(groups.keySet().iterator()
					.next());
			if (group != null) {
				HLGroup clonedGroup = (HLGroup) group.clone();
				targetAct.setGroup(clonedGroup.getID());
				myFinalSimModel.getHLProcess().addOrReplace(clonedGroup);
			}
		}
		// if more than one group -> merge resources from all groups into super
		// group
		else {
			Set<HLResource> newSetResource = new HashSet<HLResource>();
			Set<String> newNameSet = new HashSet<String>();

			// make a new name of group
			Iterator<HLID> itr = groups.keySet().iterator();
			while (itr.hasNext()) {
				HLID hlID = itr.next();
				HLGroup tempGroup = myInputSimModel.getHLProcess().getGroup(
						hlID);
				String[] tempString = tempGroup.getName().split(":");
				for (int j = 0; j < tempString.length; j++)
					newNameSet.add(tempString[j]);
			}
			// sort names alphabetically and make a new name
			ArrayList<String> tempArrayList = new ArrayList<String>();
			tempArrayList.addAll(newNameSet);
			Collections.sort(tempArrayList);
			String listNames = "";
			for (int i = 0; i < tempArrayList.size(); i++) {
				listNames += tempArrayList.get(i);
				if (i < tempArrayList.size() - 1)
					listNames += ":";
			}
			// Look for the group from InputSimModel by name
			boolean flag = false;

			Iterator<HLGroup> existingGroups = myInputSimModel.getHLProcess()
					.getGroups().iterator();
			while (existingGroups.hasNext()) {
				HLGroup group = existingGroups.next();
				if (group.getName().equals(listNames)) {
					// exist in the inputSimModel
					HLGroup clonedGroup = (HLGroup) group.clone();
					HLActivity targetAct = myFinalSimModel.getHLProcess()
							.getActivity(entry.getKey());
					targetAct.setGroup(clonedGroup.getID());
					myFinalSimModel.getHLProcess().addOrReplace(clonedGroup);
					flag = true;
					break;
				}
			}

			// The group exists in myFinalSimModel
			if (!flag) {
				existingGroups = myFinalSimModel.getHLProcess().getGroups()
						.iterator();
				while (existingGroups.hasNext()) {
					HLGroup group = existingGroups.next();
					if (group.getName().equals(listNames)) {
						// exist in the inputSimModel
						HLActivity targetAct = myFinalSimModel.getHLProcess()
								.getActivity(entry.getKey());
						targetAct.setGroup(group.getID());
						flag = true;
						break;
					}
				}
			}
			// The group doesn't exist in myInputSimModel and myFinalSimModel
			if (!flag && !listNames.equals("")) {
				HLGroup newGroup = new HLGroup(listNames, myFinalSimModel
						.getHLProcess());
				existingGroups = myInputSimModel.getHLProcess().getGroups()
						.iterator();
				while (existingGroups.hasNext()) {
					HLGroup group = existingGroups.next();
					// Doesn't the group consist of sub-groups? (if yes, skip)
					if (!group.getName().contains(":")) {
						// Is the group name in the new group name?
						if (listNames.indexOf(group.getName()) > 0) {
							// copy resources from old group to new group
							Iterator<HLResource> existingResources = group
									.getResources().iterator();
							while (existingResources.hasNext()) {
								HLResource resource = existingResources.next();
								HLResource clonedResource = (HLResource) resource
										.clone();
								myFinalSimModel.getHLProcess().addOrReplace(
										clonedResource);
								newGroup.addResource(clonedResource);
							}
						}
					}
				}
				HLActivity targetAct = myFinalSimModel.getHLProcess()
						.getActivity(entry.getKey());
				targetAct.setGroup(newGroup.getID());
			}
		}
	}

	private void jbInit() {

		this.setLayout(new BorderLayout());
		// add user help text at the top of the tab
		String description = new String(
				"Several activities may have been merged into one, higher level activity. "
						+ "Here you can determine how organizational information from the former low-level activities will be combined in the output model.");
		GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
				description);
		this.add(BorderLayout.NORTH, helpText.getPropertyPanel());

		JPanel outmostLayer = new JPanel();
		outmostLayer
				.setLayout(new BoxLayout(outmostLayer, BoxLayout.PAGE_AXIS));

		// initialize currentOptionEnumerationList
		ArrayList<String> values = new ArrayList<String>();
		values.add(DEFAULT_STRATEGY);
		values.add(UNION_ALL_STRATEGY);
		values.add(UNION_SELECTED_STRATEGY);
		currentOptionEnumerationList = new GUIPropertyListEnumeration(
				"Merging strategies: ", null, values, new StrategyListener(),
				250);
		JPanel innerLayer = new JPanel();
		innerLayer.add(currentOptionEnumerationList.getPropertyPanel());
		outmostLayer.add(innerLayer);
		outmostLayer.add(eventCheckBoxList);
		this.add(outmostLayer, BorderLayout.CENTER);

	}

	/*
	 * Only redraws the visualization panel (triggered by changed tab pane on
	 * global GUI or by changed user selection)
	 */
	public void updateGUI() {
		// repaint the graphpanel
		myGraphPanel.removeAll();
		HashSet<HLTypes.Perspective> perspectives = new HashSet<HLTypes.Perspective>();
		perspectives.add(HLTypes.Perspective.ROLES_AT_TASKS);
		// HLVisualization viz = new
		// HLVisualization(myFinalSimModel.getProcessModel());
		HLVisualization viz = new HLVisualization(myFinalSimModel
				.getVisualization(perspectives));
		myGraphPanel.add(viz.getPanel());
		myGraphPanel.validate();
		myGraphPanel.repaint();
	}

	/**
	 * Listener for which strategy is selected. (e.g.
	 * "default strategy ('complete' activities)",
	 * "union of originators (all events)" )
	 * 
	 * @author msong
	 */
	class StrategyListener implements GuiNotificationTarget {

		public void updateGUI() {
			if (currentOptionEnumerationList.getValue()
					.equals(DEFAULT_STRATEGY)) {
				initDefaults();
			} else if (currentOptionEnumerationList.getValue().equals(
					UNION_ALL_STRATEGY)) {
				unionAll();
			} else if (currentOptionEnumerationList.getValue().equals(
					UNION_SELECTED_STRATEGY)) {
				unionSelected();
			}

			myGraphPanel.removeAll();
			HashSet<HLTypes.Perspective> perspectives = new HashSet<HLTypes.Perspective>();
			perspectives.add(HLTypes.Perspective.ROLES_AT_TASKS);
			// HLVisualization viz = new
			// HLVisualization(myFinalSimModel.getProcessModel());
			HLVisualization viz = new HLVisualization(myFinalSimModel
					.getVisualization(perspectives));
			myGraphPanel.add(viz.getPanel());
			myGraphPanel.validate();
			myGraphPanel.repaint();
		}
	}

	/**
	 * A standard CheckBox for event type
	 * 
	 * @author Minseok Song
	 * @version 1.0
	 */
	public class EventCheckBoxList extends JPanel implements
			GuiNotificationTarget {

		private GUIPropertyBoolean checkBox_schedule = new GUIPropertyBoolean(
				"schedule", true, this);
		private GUIPropertyBoolean checkBox_start = new GUIPropertyBoolean(
				"start", true, this);
		private GUIPropertyBoolean checkBox_complete = new GUIPropertyBoolean(
				"complete", true, this);

		public EventCheckBoxList() {
			jbInit();
		}

		private void jbInit() {
			this.add(checkBox_schedule.getPropertyPanel());
			this.add(checkBox_start.getPropertyPanel());
			this.add(checkBox_complete.getPropertyPanel());
		}

		public boolean is_checkBox_schedule() {
			return checkBox_schedule.getValue();
		}

		public boolean is_checkBox_start() {
			return checkBox_start.getValue();
		}

		public boolean is_checkBox_complete() {
			return checkBox_complete.getValue();
		}

		public void updateGUI() {
			if (currentOptionEnumerationList.getValue().equals(
					UNION_SELECTED_STRATEGY)) {
				unionSelected();
				myGraphPanel.removeAll();
				HashSet<HLTypes.Perspective> perspectives = new HashSet<HLTypes.Perspective>();
				perspectives.add(HLTypes.Perspective.ROLES_AT_TASKS);
				HLVisualization viz = new HLVisualization(myFinalSimModel
						.getVisualization(perspectives));
				myGraphPanel.add(viz.getPanel());
				myGraphPanel.validate();
				myGraphPanel.repaint();
			}
		}
	}

}
