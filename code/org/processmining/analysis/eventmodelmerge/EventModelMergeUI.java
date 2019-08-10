package org.processmining.analysis.eventmodelmerge;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.HLTypes.EventType;
import org.processmining.framework.models.hlprocess.hlmodel.HLActivitySet;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.hlprocess.hlmodel.HLYAWL;
import org.processmining.framework.models.hlprocess.visualization.HLVisualization;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyStringTextarea;

/**
 * A process model that contains simulation information and that implements the
 * interfaces that are given in the hlprocess package in
 * org.processmining.framework.models is merged from an event model into an
 * activity model. This means that the provided simulation model contains
 * activities that refer to an event (schedule, start, complete), and which
 * information is in most cases obtained from a log, is now merged into a model
 * in which the activities only refer to an <i>activity</i> (so without
 * schedule, start and complete). <br>
 * <br>
 * For the timing, organizational and data perspective it can be decided how
 * information from the events need to be merged into an activity and which
 * ultimately results in a model that only contains activities with the merged
 * information.
 * 
 * @author rmans
 */
public class EventModelMergeUI extends JPanel implements Provider {

	/** the final simulation model that is provided by this analysis plugin */
	private HLModel myFinalSimModel = null;

	/**
	 * the input simulation model that needs to be merged into an activity model
	 */
	private HLModel myInputSimModel = null;

	/**
	 * the mapping from the highlevel activities in the final activity model
	 * (finalSimModel) to the highlevel activities in the provided simulation
	 * model that have been merged into that activity in the final simulation
	 * model. If for an activity no highlevel activities have been merged, then
	 * there is only a mapping from the highlevelactivity in the final
	 * simulation model to the highlevelactivity in the old process model.
	 */
	// private HashMap<HLActivity, ArrayList<HLActivity>> myMapping = new
	// HashMap<HLActivity,ArrayList<HLActivity>>();
	private HashMap<HLID, ArrayList<HLID>> myMapping = new HashMap<HLID, ArrayList<HLID>>();

	/**
	 * indicates which types of low-level activity where there before the
	 * merging
	 */
	private HashSet<EventType> eventTypes = null;

	/** visualisation */
	private JPanel myGraphPanel = new JPanel(new BorderLayout());

	/** GUI related fields */
	private JSplitPane mySplitPanel = new JSplitPane();

	private JTabbedPane myTabsPanel = new JTabbedPane();
	/** the panel for the timing perspective */
	private TimingTabUI myTimingPanel;
	/** the panel for the organizational perspective */
	private OrganizationalTabUI myOrganizationalPanel;
	/** the panel for the data perspective */
	private DataTabUI myDataPanel;

	/**
	 * Basic constructor. Provides the GUI for presenting how information from
	 * the events can be merged into activities.
	 * 
	 * @param eventModel
	 *            HighLevelProcess the highlevelprocess that contains
	 *            information about events and that needs to be merged into an
	 *            activity model
	 */
	public EventModelMergeUI(HLModel eventModel) {
		myInputSimModel = eventModel;
		// print the original simulation model
		Message.add("<EventModelMergePlugin>", Message.TEST);
		eventModel.getHLProcess().getGlobalInfo().writeToTestLog();

		// the input simulation model needs to be converted to a activity model
		if (eventModel instanceof HLPetriNet) {
			// first clone the eventModel
			HLPetriNet clonedEventModel = (HLPetriNet) eventModel.clone();
			// take the cloned model as the input model (otherwise no match
			// between global
			// and activity-wise data attributes)
			myInputSimModel = clonedEventModel;
			MergeHLPetriNetIntoActivityModel actMod = new MergeHLPetriNetIntoActivityModel(
					clonedEventModel);
			myFinalSimModel = actMod.mergeHLPetriNetIntoActivityModel();
			myMapping = actMod.getMapping();
		}
		if (eventModel instanceof HLActivitySet) {
			// first clone the eventModel
			HLActivitySet clonedEventModel = (HLActivitySet) eventModel.clone();
			myInputSimModel = clonedEventModel;
			MergeActivitySetIntoActivityModel actMod = new MergeActivitySetIntoActivityModel(
					(HLActivitySet) clonedEventModel);
			myFinalSimModel = actMod.mergeActivitySetIntoActivityModel();
			myMapping = actMod.getMapping();
		}
		if (eventModel instanceof HLYAWL) {
			// TODO Anne: check whether this would be needed also for YAWL
			// high-level models at some point in time
			// (currently we do not mine the models, but import them - so there
			// should not be this issue)
			// -> only adapted to see the visualization so far
			myFinalSimModel = myInputSimModel;
		}
		// if there is another implementation of the highlevelprocess interface
		// and the
		// highlevelactivity interface then that the call for converting that
		// implementation
		// to a activity model should be here

		// determine which types of low-level activity where there before the
		// merging
		eventTypes = findDifferentEventTypes(myMapping);

		// the perspectives that were covered in the input simulation model must
		// also be covered in
		// the output simulation model
		HLTypes.Perspective[] possiblePerspectives = HLTypes.Perspective
				.values(); // all perspectives
		Set perspectivesInInputModel = myInputSimModel.getHLProcess()
				.getGlobalInfo().getPerspectives();
		for (int i = 0; i < possiblePerspectives.length; i++) {
			if (perspectivesInInputModel.contains(possiblePerspectives[i])) {
				myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
						possiblePerspectives[i]);
			}
		}
		Message.add("Model after merging", Message.TEST);
		// actually build the GUI
		jbInit();
		// output the simulation model that only consists of activities
		myFinalSimModel.getHLProcess().getGlobalInfo().writeToTestLog();
		// finish printing the initial data for the plugin
		Message.add("<EventModelMergePlugin/>", Message.TEST);
	}

	/**
	 * Setting up the GUI.
	 */
	private void jbInit() {
		// layout for the main panel
		this.setLayout(new BorderLayout());
		// initialize the grappaPanel (which contains the Petri net UI)
		myGraphPanel.removeAll();
		// visualize the graph of the simulation model
		// HLVisualization viz = new
		// HLVisualization(myFinalSimModel.getProcessModel());
		HLVisualization viz = new HLVisualization(myFinalSimModel
				.getVisualization(new HashSet()));
		myGraphPanel.add(viz.getPanel());
		myGraphPanel.validate();
		myGraphPanel.repaint();
		// add the split panel
		this.add(mySplitPanel, BorderLayout.CENTER);

		// fill the split panel with the components and set some options
		mySplitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mySplitPanel.setDividerLocation(250);
		mySplitPanel.setTopComponent(myTabsPanel);
		mySplitPanel.setBottomComponent(myGraphPanel);

		// build tab views (show them for covered perspectives only -->
		// usability!)
		Set<HLTypes.Perspective> coveredPerspectives = myInputSimModel
				.getHLProcess().getGlobalInfo().getPerspectives();
		if (coveredPerspectives.contains(HLTypes.Perspective.TIMING_EXECTIME)
				|| coveredPerspectives
						.contains(HLTypes.Perspective.TIMING_WAITTIME)
				|| coveredPerspectives
						.contains(HLTypes.Perspective.TIMING_SOJTIME)) {
			// generate the layout on the timing panel
			myTimingPanel = new TimingTabUI(myInputSimModel, myFinalSimModel,
					myMapping, myGraphPanel, eventTypes);
			myTabsPanel.addTab("Timing", null, myTimingPanel, "");
		}
		if (coveredPerspectives.contains(HLTypes.Perspective.ROLES_AT_TASKS)) {
			myOrganizationalPanel = new OrganizationalTabUI(myInputSimModel,
					myFinalSimModel, myMapping, myGraphPanel);
			myTabsPanel.addTab("Organizational", null, myOrganizationalPanel,
					"");
		}
		if (coveredPerspectives.contains(HLTypes.Perspective.DATA_AT_TASKS)) {
			myDataPanel = new DataTabUI(myInputSimModel, myFinalSimModel,
					myMapping, myGraphPanel);
			myTabsPanel.addTab("Data", null, myDataPanel, "");
		}

		// now check whether any of these tabs has been added - otherwise give
		// helpful message
		if (myTabsPanel.getComponentCount() == 0) {
			String description = new String(
					"Several activities may have been merged into one, higher level activity. "
							+ "Since there were no data attributes, or resource information, or time information attached to the former low-level activities, no further merging steps need to be carried out with respect to this simulation model.");
			GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
					description);
			myTabsPanel.addTab("Merging Finished", null, helpText
					.getPropertyPanel(), "");
		} else {
			// redraw the current visualization. For the initial selection of
			// the selected tab, the corresponding
			// graph is represented on the graph Panel.
			((GuiNotificationTarget) myTabsPanel.getSelectedComponent())
					.updateGUI();
		}

		myTabsPanel.setMinimumSize(new Dimension(22, 100));
		myTabsPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// redraw visualization view for new tab
				((GuiNotificationTarget) myTabsPanel.getSelectedComponent())
						.updateGUI();
			}
		});

		// copy all the global information to the target simulation model
		// TODO - check whether this depends on something done while creating
		// the tab
		// contents (and pre-selections). If not, this should happen before
		// actually creating the GUI
		copyGlobalInformation();
	}

	/**
	 * Copies all the global information from the input model to the output
	 * simulation model (since only activity-related information will be treated
	 * in the different tabs).
	 */
	private void copyGlobalInformation() {
		// copy case generation scheme information only if present
		if (myInputSimModel.getHLProcess().getGlobalInfo().getPerspectives()
				.contains(HLTypes.Perspective.CASE_GEN_SCHEME)) {
			// copy the case generation scheme to the final simulation model
			myFinalSimModel.getHLProcess().getGlobalInfo()
					.setCaseGenerationScheme(
							myInputSimModel.getHLProcess().getGlobalInfo()
									.getCaseGenerationScheme());
		}
		// copy the time unit
		myFinalSimModel.getHLProcess().getGlobalInfo().setTimeUnit(
				myInputSimModel.getHLProcess().getGlobalInfo().getTimeUnit());
		// copy choice-related information
		updateChoices();
		// give the final simulation model a name
		myFinalSimModel.getHLProcess().getGlobalInfo().setName(
				"Activity-based "
						+ myInputSimModel.getHLProcess().getGlobalInfo()
								.getName());
	}

	/**
	 * Ensures that the highlevelchoices that are present in the simulation
	 * model that is based on events are copied to the simulation model that
	 * only contains activities. It is taken into account that nodes in the
	 * process model are merged into activities.
	 */
	private void updateChoices() {
		Iterator<HLChoice> choicesInput = myInputSimModel.getHLProcess()
				.getChoices().iterator();
		while (choicesInput.hasNext()) {
			HLChoice choiceInput = choicesInput.next();
			// get the same choicenode in myFinalSimModel
			// myInputSimModel.findModelGraphVertexForChoice(choiceID)

			HLChoice choiceOutput = myFinalSimModel.getHLProcess().getChoice(
					choiceInput.getID());

			if (choiceOutput != null) {
				Iterator<HLID> oldTargets = choiceInput.getChoiceTargetIDs()
						.iterator();
				while (oldTargets.hasNext()) {
					HLID oldTargetID = oldTargets.next();
					// check whether old target activity has been merged (and is
					// now represented by another activity)
					Iterator<Entry<HLID, ArrayList<HLID>>> entryIt = myMapping
							.entrySet().iterator();
					while (entryIt.hasNext()) {
						Entry<HLID, ArrayList<HLID>> entry = entryIt.next();
						ArrayList<HLID> list = entry.getValue();
						if (list.contains(oldTargetID)) {
							HLID newTargetID = entry.getKey();
							// cloned choice still points to the (potentially)
							// wrong act ID after merging
							choiceOutput.replaceChoiceTarget(oldTargetID,
									newTargetID);
						}
					}
				}
			} else {
				Message.add(
						"Error during merging low-level activities into high-level activities. Choice "
								+ choiceInput.getName()
								+ " not found in output model", Message.ERROR);
			}

			// Iterator<HLChoice> choicesFinal =
			// myFinalSimModel.getHLProcess().getChoices().iterator();
			// while (choicesFinal.hasNext()) {
			// HLChoice choiceFinal = choicesFinal.next();
			// //if
			// (choiceInput.getChoiceNode().getIdentifier().startsWith(choiceFinal.getChoiceNode().getIdentifier()))
			// {
			// if (choiceInput.getChoiceNode().getIdKey() ==
			// choiceFinal.getChoiceNode().getIdKey()) {
			// // choices based on data
			// Iterator<HighLevelDataDependency> dataDepsInput =
			// choiceInput.getDataDependencies().iterator();
			// while (dataDepsInput.hasNext()) {
			// HighLevelDataDependency dataDepInput = dataDepsInput.next();
			// // find the corresponding data dependency for the final
			// simulation model
			// Iterator<HighLevelDataDependency> dataDepsFinal =
			// choiceFinal.getDataDependencies().iterator();
			// while (dataDepsFinal.hasNext()) {
			// HighLevelDataDependency dataDepFinal = dataDepsFinal.next();
			// //if
			// (dataDepInput.getTargetNode().getIdentifier().startsWith(dataDepFinal.getTargetNode().getIdentifier())
			// &&
			// if (dataDepInput.getTargetNode().getIdKey() ==
			// dataDepFinal.getTargetNode().getIdKey() &&
			// myInputSimModel.getHLProcess().getGlobalInfo().getPerspectives().contains(HighLevelTypes.Perspective.CHOICE_DATA))
			// {
			// // found the corresponding data dependency, copy the expression
			// dataDepFinal.setExpression(dataDepInput.getExpression());
			// break;
			// }
			// }
			// }
			// // choices based on probabilities
			// Iterator<HighLevelProbabilityDependency> probDepsInput =
			// choiceInput.getProbabilityDependencies().iterator();
			// while (probDepsInput.hasNext()) {
			// HighLevelProbabilityDependency probDepInput =
			// probDepsInput.next();
			// // find the corresponding probability dependency for the final
			// simulation model
			// Iterator<HighLevelProbabilityDependency> probDepsFinal =
			// choiceFinal.getProbabilityDependencies().iterator();
			// while (probDepsFinal.hasNext()) {
			// HighLevelProbabilityDependency probDepFinal =
			// probDepsFinal.next();
			// //if
			// (probDepInput.getTargetNode().getIdentifier().startsWith(probDepFinal.getTargetNode().getIdentifier())
			// &&
			// if (probDepInput.getTargetNode().getIdKey() ==
			// probDepFinal.getTargetNode().getIdKey() &&
			// myInputSimModel.getHLProcess().getGlobalInfo().getPerspectives().contains(HighLevelTypes.Perspective.CHOICE_PROB))
			// {
			// // found the corresponding probability dependency, copy the
			// probability value
			// probDepFinal.setProbability(probDepInput.getProbability());
			// break;
			// }
			// }
			// }
			// // choices based on frequencies
			// Iterator<HLActivity> freqDepsInput =
			// choiceInput.getFrequencyDependencies().iterator();
			// while (freqDepsInput.hasNext()) {
			// HLActivity freqDepInput = freqDepsInput.next();
			// ModelGraphVertex freqDepInputMGV =
			// myInputSimModel.findModelGraphVertex(freqDepInput);
			// // check voor null
			// // find the corresponding frequency dependency for the final
			// simulation model
			// Iterator<HLActivity> freqDepsFinal =
			// choiceFinal.getFrequencyDependencies().iterator();
			// while (freqDepsFinal.hasNext()) {
			// HLActivity freqDepFinal = freqDepsFinal.next();
			// ModelGraphVertex freqDepFinalMGV =
			// myFinalSimModel.findModelGraphVertex(freqDepFinal);
			// //if (freqDepInput.getName().startsWith(freqDepFinal.getName())
			// &&
			// if (freqDepInputMGV != null && freqDepFinalMGV != null &&
			// freqDepInputMGV.getIdKey() == freqDepFinalMGV.getIdKey() &&
			// myInputSimModel.getHLProcess().getGlobalInfo().getPerspectives().contains(HighLevelTypes.Perspective.CHOICE_FREQ))
			// {
			// // found the corresponding frequency dependency, copy the
			// fequency value
			// freqDepFinal.setFrequencyDependency(freqDepInput.getFrequencyDependency());
			// break;
			// }
			// }
			// }
			// break;
			// }
			// }
		}
	}

	/**
	 * Retrieves the different event types that are present in the
	 * highlevelprocess of which the activities refer to events.
	 * 
	 * @param mapping
	 *            HashMap the mapping in which it can be found which
	 *            highlevelactivities that refer to events are merged into a
	 *            highlevelactivity that refers to an activity.
	 * @return HashSet the different event types that are found
	 */
	private HashSet<EventType> findDifferentEventTypes(
			HashMap<HLID, ArrayList<HLID>> mapping) {
		HashSet<EventType> returnList = new HashSet<EventType>();
		Iterator<Entry<HLID, ArrayList<HLID>>> it = mapping.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<HLID, ArrayList<HLID>> entry = it.next();
			ArrayList<HLID> values = entry.getValue();
			Iterator<HLID> valuesIt = values.iterator();
			while (valuesIt.hasNext()) {
				HLID valueID = valuesIt.next();
				HLActivity value = myInputSimModel.getHLProcess().getActivity(
						valueID);
				EventType[] evtTypes = EventType.values();
				for (int i = 0; i < evtTypes.length; i++) {
					if (value.getName().endsWith(evtTypes[i].toString())
							|| value.getName().endsWith(
									"(" + evtTypes[i].toString() + ")")) {
						returnList.add(evtTypes[i]);
						break;
					}
				}
			}
		}
		return returnList;
	}

	/**
	 * Specifiy provided objects of the analysis that can be further used to,
	 * e.g., export an item.
	 * 
	 * @return An Array containing provided objects
	 */
	public ProvidedObject[] getProvidedObjects() {
		if (myFinalSimModel != null) {
			if (myFinalSimModel instanceof HLPetriNet) {
				ProvidedObject[] objects = {
						new ProvidedObject("Activity-based "
								+ myInputSimModel.getHLProcess()
										.getGlobalInfo().getName(),
								new Object[] { myFinalSimModel }),
						new ProvidedObject("Selected Petri net",
								new Object[] { ((HLPetriNet) myFinalSimModel)
										.getProcessModel() }) };
				return objects;
			} else {
				ProvidedObject[] objects = { new ProvidedObject(
						"Activity-based "
								+ myInputSimModel.getHLProcess()
										.getGlobalInfo().getName(),
						new Object[] { myFinalSimModel }) };
				return objects;
			}
		} else {
			return null;
		}
	}
}
