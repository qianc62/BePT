package org.processmining.analysis.eventmodelmerge;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.HLTypes.EventType;
import org.processmining.framework.models.hlprocess.distribution.HLDistribution;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.models.hlprocess.visualization.HLVisualization;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyListRadio;
import org.processmining.framework.util.GuiPropertyStringTextarea;

/**
 * The timing tab of the EventModelMerge plugin presents possibilities to the
 * user for which event type he wants to select the execution, waiting and
 * sojourn time from.
 * 
 * @author rmans
 */
public class TimingTabUI extends JPanel implements GuiNotificationTarget {

	/**
	 * enumeration type for the perspectives that can be shown
	 * 
	 * @author rmans
	 */
	public enum PerspectiveToShow {
		EXECTIME("Execution time"), WAITTIME("Waiting time"), SOJTIME(
				"Sojourn time");

		private final String myName;

		/**
		 * Constructor for the enumeration type PerspectiveToShow
		 */
		PerspectiveToShow(String name) {
			myName = name;
		}

		public String toString() {
			return myName;
		}
	}

	/** for choosing the default timing information */
	private final String DEFAULT = "none (default information)";

	/** the input simulation model */
	private HLModel myInputSimModel;

	/** the final simulation model */
	private HLModel myFinalSimModel;

	/** the final simulation model with default values */
	private HLModel myFinalSimModelDefault;

	/**
	 * The mapping from the highlevelactivities in the final simulation model to
	 * the highlevelactivities in the input simulation model that are merged
	 */
	private HashMap<HLID, ArrayList<HLID>> myMapping;

	private HashSet<EventType> eventTypes;

	/** the panel on which the graph is located */
	private JPanel myGraphPanel;

	/* GUI properties */
	private JPanel perspectivePanel;
	private GuiPropertyListRadio myPerspectivesRadioList;
	private JPanel eventsPanel;
	private HashMap<PerspectiveToShow, GUIPropertyListEnumeration> perspectivesToEventSelection = new HashMap<PerspectiveToShow, GUIPropertyListEnumeration>();
	private GUIPropertyListEnumeration currentEventsEnumerationList = null; // TODO

	// -
	// maybe
	// remove
	// as
	// actually
	// redundant
	// information
	// (could
	// be
	// retrieved
	// from
	// the
	// list
	// directly)

	/**
	 * Constructor for the timing tab
	 * 
	 * @param inputSimModel
	 *            HighLevelProcess the highlevelprocess of which activities
	 *            refer to events
	 * @param finalSimModel
	 *            HighLevelProcess the highlevelprocess of which activities
	 *            refer to activities
	 * @param mapping
	 *            HashMap the mapping for which highlevelactivities of
	 *            inputSimModel are merged into one activity in the
	 *            finalSimModel
	 * @param graphPanel
	 *            JPanel the panel on which the activity model (the
	 *            finalSimModel) is presented to the user
	 * @param eventTypes
	 *            the types of low-level activities that have been found in the
	 *            model during merging
	 */
	public TimingTabUI(HLModel inputSimModel, HLModel finalSimModel,
			HashMap<HLID, ArrayList<HLID>> mapping, JPanel graphPanel,
			HashSet<EventType> types) {
		super();
		myInputSimModel = inputSimModel;
		myFinalSimModel = finalSimModel;
		myFinalSimModelDefault = (HLModel) myFinalSimModel.clone();
		myFinalSimModelDefault.reset();
		myGraphPanel = graphPanel;
		myMapping = mapping;
		eventTypes = types;
		jbInit();
	}

	/**
	 * Setting up the GUI.
	 */
	private void jbInit() {

		this.setLayout(new BorderLayout());
		// add user help text at the top of the tab
		String description = new String(
				"Several activities may have been merged into one, higher level activity. "
						+ "Here you can determine from which of the former low-level activities the timing information will be copied to the output model. "
						+ "Note that if timing information was obtained from the 'Performance Analysis with Petri net' plug-in, "
						+ "each of the low-level activities already carries the (same) information with respect to the higher level activity -- so, it makes no difference which one you choose.");
		GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
				description);
		this.add(BorderLayout.NORTH, helpText.getPropertyPanel());

		JPanel outmostLayer = new JPanel();

		// create perspective and inputs panel
		perspectivePanel = new JPanel();
		BorderLayout borderLayoutPerspectivePanel = new BorderLayout();
		perspectivePanel.setLayout(borderLayoutPerspectivePanel);
		eventsPanel = new JPanel();
		BorderLayout borderLayoutInputsPanel = new BorderLayout();
		eventsPanel.setLayout(borderLayoutInputsPanel);
		// set the layout for this panel
		outmostLayer.setLayout(new BoxLayout(outmostLayer, BoxLayout.X_AXIS));
		// create titled borders for the perspective and the events panel
		Border borderChoicePanel = BorderFactory.createEtchedBorder(
				Color.white, new Color(148, 145, 140));
		Border titledBorderChoicePanel = new TitledBorder(borderChoicePanel,
				"Timing information");
		perspectivePanel.setBorder(titledBorderChoicePanel);
		Border borderInputsPanel = BorderFactory.createEtchedBorder(
				Color.white, new Color(148, 145, 140));
		Border titledBorderInputsPanel = new TitledBorder(borderInputsPanel,
				"Get particular timing information from");
		eventsPanel.setBorder(titledBorderInputsPanel);
		// create a radio list for the different perspectives: execution,
		// waiting and sojourn time
		ArrayList<String> valuesRadioList = new ArrayList<String>();
		PerspectiveToShow[] perspectives = PerspectiveToShow.values();
		for (int i = 0; i < perspectives.length; i++) {
			valuesRadioList.add(perspectives[i].toString());
		}
		myPerspectivesRadioList = new GuiPropertyListRadio("", "",
				valuesRadioList, new PerspectiveSimModelListener());
		perspectivePanel.add(myPerspectivesRadioList.getPropertyPanel());
		// create a radio list for the event types that are present in the input
		// simulation model
		ArrayList<String> evtTypesString = new ArrayList<String>();
		Iterator<EventType> it = eventTypes.iterator();
		while (it.hasNext()) {
			EventType evtType = it.next();
			evtTypesString.add(evtType.toString());
		}
		evtTypesString.add(DEFAULT);

		// create separate event list properties for every perspective
		perspectivesToEventSelection
				.put(
						PerspectiveToShow.EXECTIME,
						new GUIPropertyListEnumeration(
								"   Low-level activity:  ",
								"Please select from which of the former low-level activities the execution times should be copied",
								evtTypesString, new EventTypeListener(), 400));
		perspectivesToEventSelection
				.put(
						PerspectiveToShow.WAITTIME,
						new GUIPropertyListEnumeration(
								"   Low-level activity:  ",
								"Please select from which of the former low-level activities the waiting times should be copied",
								evtTypesString, new EventTypeListener(), 400));
		perspectivesToEventSelection
				.put(
						PerspectiveToShow.SOJTIME,
						new GUIPropertyListEnumeration(
								"   Low-level activity:  ",
								"Please select from which of the former low-level activities the sojourn times should be copied",
								evtTypesString, new EventTypeListener(), 400));
		currentEventsEnumerationList = perspectivesToEventSelection
				.get(PerspectiveToShow.EXECTIME);
		eventsPanel.add(currentEventsEnumerationList.getPropertyPanel(),
				BorderLayout.LINE_START);

		// add the perspective panel and the inputs panel to the main panel
		outmostLayer.add(perspectivePanel);
		outmostLayer.add(eventsPanel);
		this.add(outmostLayer, BorderLayout.CENTER);
		// initial selection
		initialSelection();
	}

	/*
	 * Only redraws the visualization panel (triggered by changed tab pane on
	 * global GUI or by changed user selection)
	 */
	public void updateGUI() {
		// repaint the graphpanel
		myPerspectivesRadioList.notifyTarget();
	}

	/**
	 * Initial settings that are choosen and which are copied to the final
	 * simulation model
	 */
	private void initialSelection() {
		// based on the provided timing perspectives and the event types that
		// are available in the log, copy some timing information
		if (eventTypes.size() > 0) {
			ArrayList<EventType> eventTypesList = new ArrayList<EventType>(
					eventTypes);
			Iterator<Entry<HLID, ArrayList<HLID>>> it = myMapping.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<HLID, ArrayList<HLID>> entry = it.next();
				HLID actID = entry.getKey();
				HLActivity hlActivity = myFinalSimModel.getHLProcess()
						.getActivity(actID);
				// HLActivity hlActivity = entry.getKey();
				// check whether the first element of eventTypes can be found in
				// the value list of entry
				Iterator<HLID> activitiesIt = entry.getValue().iterator();
				while (activitiesIt.hasNext()) {
					HLID actValueID = activitiesIt.next();
					HLActivity activityValue = myInputSimModel.getHLProcess()
							.getActivity(actValueID);
					// HLActivity activityValue = activitiesIt.next();
					if (activityValue.getName().endsWith(
							eventTypesList.get(0).toString())
							|| activityValue.getName().endsWith(
									"(" + eventTypesList.get(0).toString()
											+ ")")) {
						// copy to the hlActivity
						if (myInputSimModel.getHLProcess().getGlobalInfo()
								.getPerspectives().contains(
										HLTypes.Perspective.TIMING_EXECTIME)) {
							hlActivity
									.setExecutionTime((HLDistribution) activityValue
											.getExecutionTime().clone());
							// myFinalSimModel.addPerspective(Perspective.TIMING_EXECTIME);
							// a hack
							// for hltransitions in the finalSimModel with the
							// same name as hlActivity but not the
							// same object, copy the timing info
							Iterator<HLActivity> simModelHlActs = myFinalSimModel
									.getHLProcess().getActivities().iterator();
							while (simModelHlActs.hasNext()) {
								HLActivity simModelHlAct = simModelHlActs
										.next();
								if (simModelHlAct.getName().equals(
										hlActivity.getName())
										&& simModelHlAct != hlActivity) {
									simModelHlAct.setExecutionTime(hlActivity
											.getExecutionTime());
								}
							}
						}
						if (myInputSimModel.getHLProcess().getGlobalInfo()
								.getPerspectives().contains(
										HLTypes.Perspective.TIMING_WAITTIME)) {
							hlActivity
									.setWaitingTime((HLDistribution) activityValue
											.getWaitingTime().clone());
							// myFinalSimModel.addPerspective(Perspective.TIMING_WAITTIME);
							// a hack
							// for hltransitions in the finalSimModel with the
							// same name as hlActivity but not the
							// same object, copy the timing info
							Iterator<HLActivity> simModelHlActs = myFinalSimModel
									.getHLProcess().getActivities().iterator();
							while (simModelHlActs.hasNext()) {
								HLActivity simModelHlAct = simModelHlActs
										.next();
								if (simModelHlAct.getName().equals(
										hlActivity.getName())
										&& simModelHlAct != hlActivity) {
									simModelHlAct.setWaitingTime(hlActivity
											.getWaitingTime());
								}
							}

						}
						if (myInputSimModel.getHLProcess().getGlobalInfo()
								.getPerspectives().contains(
										HLTypes.Perspective.TIMING_SOJTIME)) {
							hlActivity
									.setSojournTime((HLDistribution) activityValue
											.getSojournTime().clone());
							// myFinalSimModel.addPerspective(Perspective.TIMING_SOJTIME);
							// a hack
							// for hltransitions in the finalSimModel with the
							// same name as hlActivity but not the
							// same object, copy the timing info
							Iterator<HLActivity> simModelHlActs = myFinalSimModel
									.getHLProcess().getActivities().iterator();
							while (simModelHlActs.hasNext()) {
								HLActivity simModelHlAct = simModelHlActs
										.next();
								if (simModelHlAct.getName().equals(
										hlActivity.getName())
										&& simModelHlAct != hlActivity) {
									simModelHlAct.setSojournTime(hlActivity
											.getSojournTime());
								}
							}
						}
						break;
					}
				}
			}
		}
		// visualize initial selection
		myPerspectivesRadioList.notifyTarget();
	}

	/**
	 * For each element in the enumeration type <code>PerspectiveToShow</code>
	 * the corresponding element of enumeration type <code>Perspective</code> is
	 * returned
	 * 
	 * @param p
	 *            PerspectiveToShow
	 * @return Perspective
	 */
	private HLTypes.Perspective getCorrespondingPerspective(PerspectiveToShow p) {
		if (p.equals(PerspectiveToShow.EXECTIME)) {
			return HLTypes.Perspective.TIMING_EXECTIME;
		} else if (p.equals(PerspectiveToShow.WAITTIME)) {
			return HLTypes.Perspective.TIMING_WAITTIME;
		} else if (p.equals(PerspectiveToShow.SOJTIME)) {
			return HLTypes.Perspective.TIMING_SOJTIME;
		} else {
			return null;
		}
	}

	/**
	 * Listener for which perspective is selected. If the case generation scheme
	 * perspective is selected then the case generation scheme has to be copied
	 * to the final simulation model
	 * 
	 * @author rmans
	 */
	class PerspectiveSimModelListener implements GuiNotificationTarget {

		public void updateGUI() {
			PerspectiveToShow[] perspectivesToShow = PerspectiveToShow.values();
			for (int i = 0; i < perspectivesToShow.length; i++) {
				if (myPerspectivesRadioList.getValue().equals(
						perspectivesToShow[i].toString())) {
					// update visualization
					HashSet<HLTypes.Perspective> perspectives = new HashSet<HLTypes.Perspective>();
					perspectives
							.add(getCorrespondingPerspective(perspectivesToShow[i]));
					myGraphPanel.removeAll();
					// HLVisualization viz = new
					// HLVisualization(myFinalSimModel.getProcessModel());
					HLVisualization viz = new HLVisualization(myFinalSimModel
							.getVisualization(perspectives));
					myGraphPanel.add(viz.getPanel());
					myGraphPanel.validate();
					myGraphPanel.repaint();
					// repaint the events panel
					eventsPanel.removeAll();
					currentEventsEnumerationList = perspectivesToEventSelection
							.get(perspectivesToShow[i]);
					eventsPanel.add(currentEventsEnumerationList
							.getPropertyPanel(), BorderLayout.LINE_START);
					eventsPanel.validate();
					eventsPanel.repaint();
					break;
				}
			}
		}
	}

	/**
	 * A listener for which event type and which timing perspective (execution,
	 * waiting or sojourn time) is selected by the user.
	 * 
	 * @author rmans
	 */
	class EventTypeListener implements GuiNotificationTarget {
		public void updateGUI() {
			HashSet<HLTypes.Perspective> perspectives = new HashSet<HLTypes.Perspective>();
			// execution time perspective
			PerspectiveToShow[] perspectivesToShow = PerspectiveToShow.values();
			for (int j = 0; j < perspectivesToShow.length; j++) {
				if (myPerspectivesRadioList.getValue().equals(
						perspectivesToShow[j].toString())) {
					if (currentEventsEnumerationList.getValue().equals(DEFAULT)) {
						// copy the default execution time to the final
						// simulation model
						Iterator<HLID> activitiesIt = myMapping.keySet()
								.iterator();
						while (activitiesIt.hasNext()) {
							HLID activityID = activitiesIt.next();
							HLActivity activity = myFinalSimModel
									.getHLProcess().getActivity(activityID);
							// HLActivity activity = activitiesIt.next();
							// find the activity with the same name in the
							// default simulation model
							HLActivity defActivity = findHLActivity(
									myFinalSimModelDefault, activity.getName());
							if (defActivity != null) {
								// for each perspective a specific method needs
								// to be called
								// execution time
								if (perspectivesToShow[j]
										.equals(PerspectiveToShow.EXECTIME)) {
									activity
											.setExecutionTime((HLDistribution) defActivity
													.getExecutionTime().clone());
								}
								// waiting time
								else if (perspectivesToShow[j]
										.equals(PerspectiveToShow.WAITTIME)) {
									activity
											.setWaitingTime((HLDistribution) defActivity
													.getWaitingTime().clone());
								}
								// sojourn time
								else if (perspectivesToShow[j]
										.equals(PerspectiveToShow.SOJTIME)) {
									activity
											.setSojournTime((HLDistribution) defActivity
													.getSojournTime().clone());
								}
								perspectives
										.add(getCorrespondingPerspective(perspectivesToShow[j]));
							}
						}
					} else {
						EventType[] evtTypes = EventType.values();
						for (int i = 0; i < evtTypes.length; i++) {
							if (currentEventsEnumerationList.getValue().equals(
									evtTypes[i].toString())) {
								// copy the execution time of the schedule event
								// to the final simulation model
								Iterator<Entry<HLID, ArrayList<HLID>>> it = myMapping
										.entrySet().iterator();
								while (it.hasNext()) {
									Entry<HLID, ArrayList<HLID>> entry = it
											.next();
									HLID hlActKeyID = entry.getKey();
									HLActivity hlActKey = myFinalSimModel
											.getHLProcess().getActivity(
													hlActKeyID);
									// HLActivity hlActKey = entry.getKey();
									Iterator<HLID> hlValuesIt = entry
											.getValue().iterator();
									while (hlValuesIt.hasNext()) {
										HLID hlValueID = hlValuesIt.next();
										HLActivity hlValue = myInputSimModel
												.getHLProcess().getActivity(
														hlValueID);
										// HLActivity hlValue =
										// hlValuesIt.next();
										// for each perspective a specific
										// method needs to be called
										if (hlValue.getName().endsWith(
												evtTypes[i].toString())) {
											// execution time
											if (perspectivesToShow[j]
													.equals(PerspectiveToShow.EXECTIME)) {
												hlActKey
														.setExecutionTime((HLDistribution) hlValue
																.getExecutionTime()
																.clone());
											}
											// waiting time
											else if (perspectivesToShow[j]
													.equals(PerspectiveToShow.WAITTIME)) {
												hlActKey
														.setWaitingTime((HLDistribution) hlValue
																.getWaitingTime()
																.clone());
											}
											// sojourn time
											else if (perspectivesToShow[j]
													.equals(PerspectiveToShow.SOJTIME)) {
												hlActKey
														.setSojournTime((HLDistribution) hlValue
																.getSojournTime()
																.clone());
											}
											perspectives
													.add(getCorrespondingPerspective(perspectivesToShow[j]));
											break;
										}
									}
								}
								break;
							}
						}
					}
				}
			}
			// update the picture of the graph
			myGraphPanel.removeAll();
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
	 * Finds a highlevelactivity in a process, based on the name
	 * 
	 * @param process
	 *            HighLevelProcess the highlevelprocess in which some highlevel
	 *            activity needs to be found
	 * @param name
	 *            String the name of the highlevelactivity that needs to be
	 *            found
	 * @return HLActivity the highlevelactivity object in process that satisfied
	 *         the given name. <code>null</code> otherwise.
	 */
	private HLActivity findHLActivity(HLModel process, String name) {
		HLActivity returnActivity = null;
		Iterator<HLActivity> activitiesIt = process.getHLProcess()
				.getActivities().iterator();
		while (activitiesIt.hasNext()) {
			HLActivity activity = activitiesIt.next();
			if (activity.getName().equals(name)) {
				returnActivity = activity;
				break;
			}
		}
		return returnActivity;
	}

}
