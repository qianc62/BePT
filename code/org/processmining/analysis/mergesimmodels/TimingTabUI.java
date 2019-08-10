package org.processmining.analysis.mergesimmodels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.models.hlprocess.visualization.HLVisualization;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyListRadio;
import org.processmining.framework.util.GuiPropertyStringTextarea;

/**
 * Panel on which the user can select a timing perspective (case generation
 * scheme, execution time, waiting time or sojourn time) and then the names of
 * the simulation models are shown which cover the selected perspective. For one
 * of these simulation models, the user can select the one for which the
 * simulation information for the selected perspective needs to be to the final
 * simulation model and which is subsequently shown on the graph panel.
 * 
 * @author rmans
 */
public class TimingTabUI extends JPanel {

	/**
	 * enumeration type for the perspectives that can be shown
	 * 
	 * @author rmans
	 */
	public enum TimingPerspectives {
		EXECTIME("Execution time"), WAITTIME("Waiting time"), SOJTIME(
				"Sojourn time"), CASEGENSCHEME("Case generation scheme");

		private final String myName;

		/**
		 * Constructor for the enumeration type PerspectiveToShow
		 * 
		 * @param name
		 *            the name
		 */
		TimingPerspectives(String name) {
			myName = name;
		}

		public String toString() {
			return myName;
		}
	}

	/** for the timing perspective also default values can be chosen */
	private final String DEFAULT = "none (default information)";
	/** the final simulation model that is going to be exported */
	private HLModel myFinalSimModel;
	/** the final simulation model with default values */
	private HLModel myFinalSimModelDefault;
	/** the available simulation models */
	private ArrayList<HLModel> mySimModels;
	/**
	 * the panel on which the graph of the final simulation model needs to be
	 * shown
	 */
	private JPanel myGraphPanel;
	/** the currently selected perspective to show */
	private TimingPerspectives mySelectedTimingPerspectives;

	/**
	 * the mapping from each perspective to the drop down box that contains the
	 * available simulation models for that perspective
	 */
	private HashMap<TimingPerspectives, GUIPropertyListEnumeration> myMappingPerspectiveToAvailableSimModels = new HashMap<TimingPerspectives, GUIPropertyListEnumeration>();

	/* GUI properties */
	private JPanel inputsPanel;
	private JPanel perspectivePanel;
	private GuiPropertyListRadio myPerspectivesRadioList;

	/**
	 * Default constructor
	 * 
	 * @param finalSimModel
	 *            HighLevelProcess the final simulation model
	 * @param simModels
	 *            ArrayList the input simulation models
	 * @param gp
	 *            JPanel the panel on which the graph of the final simulation
	 *            model is located
	 */
	public TimingTabUI(HLModel finalSimModel, ArrayList<HLModel> simModels,
			JPanel gp) {
		super();
		myFinalSimModel = finalSimModel;
		myFinalSimModelDefault = (HLModel) finalSimModel.clone();
		myFinalSimModelDefault.reset();
		mySimModels = simModels;
		myGraphPanel = gp;
		jbInit();
	}

	/**
	 * Constructs the ui for the timing perspective tab
	 */
	private void jbInit() {

		this.setLayout(new BorderLayout());
		// add user help text at the top of the tab
		String description = new String(
				"For each of the timing perspectives below you can determine from which input simulation model the information should be copied to the output model. "
						+ "If none of the input models has been chosen (or none provides the corresponding information), default information will be used instead. ");
		GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
				description);
		this.add(BorderLayout.NORTH, helpText.getPropertyPanel());

		JPanel outmostLayer = new JPanel();
		// create perspective and inputs panel
		perspectivePanel = new JPanel();
		BorderLayout borderLayoutPerspectivePanel = new BorderLayout();
		perspectivePanel.setLayout(borderLayoutPerspectivePanel);
		inputsPanel = new JPanel();
		BorderLayout borderLayoutInputsPanel = new BorderLayout();
		inputsPanel.setLayout(borderLayoutInputsPanel);
		// set the layout for this panel
		outmostLayer.setLayout(new BoxLayout(outmostLayer, BoxLayout.X_AXIS));
		// create titled borders for the perspective and the inputs panel
		Border borderChoicePanel = BorderFactory.createEtchedBorder(
				Color.white, new Color(148, 145, 140));
		Border titledBorderChoicePanel = new TitledBorder(borderChoicePanel,
				"Timing information for");
		perspectivePanel.setBorder(titledBorderChoicePanel);
		Border borderInputsPanel = BorderFactory.createEtchedBorder(
				Color.white, new Color(148, 145, 140));
		Border titledBorderInputsPanel = new TitledBorder(borderInputsPanel,
				"Get particular timing information from");
		inputsPanel.setBorder(titledBorderInputsPanel);
		// create a radio list for the different perspectives: execution,
		// waiting and sojourn time
		ArrayList<String> valuesRadioList = new ArrayList<String>();
		TimingPerspectives[] perspectives = TimingPerspectives.values();
		for (int i = 0; i < perspectives.length; i++) {
			valuesRadioList.add(perspectives[i].toString());
		}
		myPerspectivesRadioList = new GuiPropertyListRadio("", "",
				valuesRadioList, new PerspectiveSimModelListener());
		perspectivePanel.add(myPerspectivesRadioList.getPropertyPanel());
		// add the perspective panel and the inputs panel to the main panel
		outmostLayer.add(perspectivePanel);
		outmostLayer.add(inputsPanel);
		this.add(outmostLayer, BorderLayout.CENTER);
		// make initial selection
		initialSelection();
	}

	/**
	 * Based on the initial selection of some input simulation model, timing
	 * information is copied to the final simulation model.
	 */
	private void initialSelection() {
		// change the name of the final simulation model with default values
		myFinalSimModelDefault.getHLProcess().getGlobalInfo().setName(DEFAULT);
		ArrayList<HLModel> simModelsWithPerspective = new ArrayList<HLModel>();
		// execution time
		simModelsWithPerspective = MergeUtilities
				.generateUniqueNameSimModel(MergeUtilities
						.getSimModelsWithPerspective(mySimModels,
								HLTypes.Perspective.TIMING_EXECTIME));
		simModelsWithPerspective.add(myFinalSimModelDefault);
		GUIPropertyListEnumeration inputSimModExecTime = new GUIPropertyListEnumeration(
				"   Simulation model:  ",
				"Please select from which of the available input simulation models the execution times should be copied",
				simModelsWithPerspective, new InputSimModelListener(), 400);
		myMappingPerspectiveToAvailableSimModels.put(
				TimingPerspectives.EXECTIME, inputSimModExecTime);
		// waiting time
		simModelsWithPerspective = MergeUtilities
				.generateUniqueNameSimModel(MergeUtilities
						.getSimModelsWithPerspective(mySimModels,
								HLTypes.Perspective.TIMING_WAITTIME));
		simModelsWithPerspective.add(myFinalSimModelDefault);
		GUIPropertyListEnumeration inputSimModWaitTime = new GUIPropertyListEnumeration(
				"   Simulation model:  ",
				"Please select from which of the available input simulation models the waiting times should be copied",
				simModelsWithPerspective, new InputSimModelListener(), 400);
		myMappingPerspectiveToAvailableSimModels.put(
				TimingPerspectives.WAITTIME, inputSimModWaitTime);
		// sojourn time
		simModelsWithPerspective = MergeUtilities
				.generateUniqueNameSimModel(MergeUtilities
						.getSimModelsWithPerspective(mySimModels,
								HLTypes.Perspective.TIMING_SOJTIME));
		simModelsWithPerspective.add(myFinalSimModelDefault);
		GUIPropertyListEnumeration inputSimModSojTime = new GUIPropertyListEnumeration(
				"   Simulation model:  ",
				"Please select from which of the available input simulation models the sojourn times should be copied",
				simModelsWithPerspective, new InputSimModelListener(), 400);
		myMappingPerspectiveToAvailableSimModels.put(
				TimingPerspectives.SOJTIME, inputSimModSojTime);
		// case generation scheme
		simModelsWithPerspective = MergeUtilities
				.generateUniqueNameSimModel(MergeUtilities
						.getSimModelsWithPerspective(mySimModels,
								HLTypes.Perspective.CASE_GEN_SCHEME));
		simModelsWithPerspective.add(myFinalSimModelDefault);
		GUIPropertyListEnumeration inputSimModCaseGen = new GUIPropertyListEnumeration(
				"   Simulation model:  ",
				"Please select from which of the available input simulation models the case generation scheme should be copied",
				simModelsWithPerspective, new InputSimModelListener(), 400);
		myMappingPerspectiveToAvailableSimModels.put(
				TimingPerspectives.CASEGENSCHEME, inputSimModCaseGen);
		// initialize the first selection
		// based on the initial selection of the drop down box, you already copy
		// values to the final simulation model
		// execution time
		HLModel selectedExecTime = (HLModel) myMappingPerspectiveToAvailableSimModels
				.get(TimingPerspectives.EXECTIME).getValue();
		copyTimingInfoForPerspective(selectedExecTime, myFinalSimModel,
				HLTypes.Perspective.TIMING_EXECTIME);
		// waiting time
		HLModel selectedWaitTime = (HLModel) myMappingPerspectiveToAvailableSimModels
				.get(TimingPerspectives.WAITTIME).getValue();
		copyTimingInfoForPerspective(selectedWaitTime, myFinalSimModel,
				HLTypes.Perspective.TIMING_WAITTIME);
		// sojourn time
		HLModel selectedSojTime = (HLModel) myMappingPerspectiveToAvailableSimModels
				.get(TimingPerspectives.SOJTIME).getValue();
		copyTimingInfoForPerspective(selectedSojTime, myFinalSimModel,
				HLTypes.Perspective.TIMING_SOJTIME);
		// case generation scheme
		HLModel selectedCaseGenScheme = (HLModel) myMappingPerspectiveToAvailableSimModels
				.get(TimingPerspectives.CASEGENSCHEME).getValue();
		copyTimingInfoForPerspective(selectedCaseGenScheme, myFinalSimModel,
				HLTypes.Perspective.CASE_GEN_SCHEME);
		// copy the timeunit and the year offset
		myFinalSimModel.getHLProcess().getGlobalInfo().setTimeUnit(
				selectedCaseGenScheme.getHLProcess().getGlobalInfo()
						.getTimeUnit());

		// current selected TimingPerspectives
		mySelectedTimingPerspectives = TimingPerspectives.EXECTIME;
		// update the gui
		myPerspectivesRadioList.notifyTarget();
	}

	/**
	 * Get the visualization for the input simulation model that is currently
	 * selected.
	 * 
	 * @return JPanel
	 */
	public JPanel getVisualizationForSelection() {
		GUIPropertyListEnumeration selectedListEnum = (GUIPropertyListEnumeration) myMappingPerspectiveToAvailableSimModels
				.get(mySelectedTimingPerspectives);
		HashSet<HLTypes.Perspective> selectedPerspective = new HashSet<HLTypes.Perspective>();
		if (mySelectedTimingPerspectives.equals(TimingPerspectives.EXECTIME)) {
			selectedPerspective.add(HLTypes.Perspective.TIMING_EXECTIME);
		} else if (mySelectedTimingPerspectives
				.equals(TimingPerspectives.WAITTIME)) {
			selectedPerspective.add(HLTypes.Perspective.TIMING_WAITTIME);
		} else if (mySelectedTimingPerspectives
				.equals(TimingPerspectives.SOJTIME)) {
			selectedPerspective.add(HLTypes.Perspective.TIMING_SOJTIME);
		} else if (mySelectedTimingPerspectives
				.equals(TimingPerspectives.CASEGENSCHEME)) {
			selectedPerspective.add(HLTypes.Perspective.CASE_GEN_SCHEME);
		}
		HLVisualization viz = new HLVisualization(((HLModel) selectedListEnum
				.getValue()).getVisualization(selectedPerspective));
		return viz.getPanel();
	}

	/**
	 * Listener for the perspectives (case generation scheme, execution time,
	 * waiting time, sojourn time)
	 * 
	 * @author rmans
	 */
	class PerspectiveSimModelListener implements GuiNotificationTarget {

		public void updateGUI() {
			// dependent on the selected perspective, show the corresponding
			// drop down box on the right side panel
			GUIPropertyListEnumeration inputSimModels = null;
			HashSet<HLTypes.Perspective> mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
			if (myPerspectivesRadioList.getValue().equals(
					TimingPerspectives.EXECTIME.toString())) {
				inputSimModels = myMappingPerspectiveToAvailableSimModels
						.get(TimingPerspectives.EXECTIME);
				mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
				mySelectedPerspectives.add(HLTypes.Perspective.TIMING_EXECTIME);
				mySelectedTimingPerspectives = TimingPerspectives.EXECTIME;
			} else if (myPerspectivesRadioList.getValue().equals(
					TimingPerspectives.WAITTIME.toString())) {
				inputSimModels = myMappingPerspectiveToAvailableSimModels
						.get(TimingPerspectives.WAITTIME);
				mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
				mySelectedPerspectives.add(HLTypes.Perspective.TIMING_WAITTIME);
				mySelectedTimingPerspectives = TimingPerspectives.WAITTIME;
			} else if (myPerspectivesRadioList.getValue().equals(
					TimingPerspectives.SOJTIME.toString())) {
				inputSimModels = myMappingPerspectiveToAvailableSimModels
						.get(TimingPerspectives.SOJTIME);
				mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
				mySelectedPerspectives.add(HLTypes.Perspective.TIMING_SOJTIME);
				mySelectedTimingPerspectives = TimingPerspectives.SOJTIME;
			} else if (myPerspectivesRadioList.getValue().equals(
					TimingPerspectives.CASEGENSCHEME.toString())) {
				inputSimModels = myMappingPerspectiveToAvailableSimModels
						.get(TimingPerspectives.CASEGENSCHEME);
				mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
				mySelectedPerspectives.add(HLTypes.Perspective.CASE_GEN_SCHEME);
				mySelectedTimingPerspectives = TimingPerspectives.CASEGENSCHEME;
			}
			// put it on the panel where the input simulation models for the
			// selected perspective are presented
			inputsPanel.removeAll();
			inputsPanel.add(inputSimModels.getPropertyPanel(),
					BorderLayout.LINE_START);
			// repaint the panel
			inputsPanel.validate();
			inputsPanel.repaint();
			// also update the graph
			myGraphPanel.removeAll();
			HLVisualization viz = new HLVisualization(((HLModel) inputSimModels
					.getValue()).getVisualization(mySelectedPerspectives));
			myGraphPanel.add(viz.getPanel());
			// repaint the graph panel
			myGraphPanel.validate();
			myGraphPanel.repaint();
		}
	}

	/**
	 * listener for the simulation model that is selected on the TimingTab.
	 * Dependent on the perspective that is selected the corresponding
	 * information for that perspective is copied to the final simulation model
	 * and shown to the user.
	 */
	class InputSimModelListener implements GuiNotificationTarget {

		public void updateGUI() {
			// get the name of the selected simulation model, dependent on the
			// selected perspective
			HLModel selectedSimModel = (HLModel) myMappingPerspectiveToAvailableSimModels
					.get(mySelectedTimingPerspectives).getValue();
			HashSet<HLTypes.Perspective> perspectivesToShow = new HashSet<HLTypes.Perspective>();
			// check whether the case generation scheme has been selected
			if (myPerspectivesRadioList.getValue().equals(
					TimingPerspectives.CASEGENSCHEME.toString())) {
				myFinalSimModel.getHLProcess().getGlobalInfo()
						.setCaseGenerationScheme(
								selectedSimModel.getHLProcess().getGlobalInfo()
										.getCaseGenerationScheme());
				// add perspective
				myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
						HLTypes.Perspective.CASE_GEN_SCHEME);
				perspectivesToShow.add(HLTypes.Perspective.CASE_GEN_SCHEME);
			} else {
				// copy the selected timing information from the input
				// simulation model to the final simulation model
				if (myPerspectivesRadioList.getValue().equals(
						TimingPerspectives.EXECTIME.toString())) {
					copyTimingInfoForPerspective(selectedSimModel,
							myFinalSimModel,
							HLTypes.Perspective.TIMING_EXECTIME);
					perspectivesToShow.add(HLTypes.Perspective.TIMING_EXECTIME);
				}
				if (myPerspectivesRadioList.getValue().equals(
						TimingPerspectives.WAITTIME.toString())) {
					copyTimingInfoForPerspective(selectedSimModel,
							myFinalSimModel,
							HLTypes.Perspective.TIMING_WAITTIME);
					perspectivesToShow.add(HLTypes.Perspective.TIMING_WAITTIME);
				}
				if (myPerspectivesRadioList.getValue().equals(
						TimingPerspectives.SOJTIME.toString())) {
					copyTimingInfoForPerspective(selectedSimModel,
							myFinalSimModel, HLTypes.Perspective.TIMING_SOJTIME);
					perspectivesToShow.add(HLTypes.Perspective.TIMING_SOJTIME);
				}
			}
			// copy the timeunit and the year offset
			myFinalSimModel.getHLProcess().getGlobalInfo().setTimeUnit(
					selectedSimModel.getHLProcess().getGlobalInfo()
							.getTimeUnit());
			// repaint the petri net on the UI.
			// dependent of the selected perspective
			myGraphPanel.removeAll();
			HLVisualization viz = new HLVisualization(myFinalSimModel
					.getVisualization(perspectivesToShow));
			myGraphPanel.add(viz.getPanel());
			myGraphPanel.validate();
			myGraphPanel.repaint();
		}
	}

	/**
	 * Copies the timing information of the provided perspective from the source
	 * to the destination simulation model
	 * 
	 * @param source
	 *            HighLevelProcess the source simulation model
	 * @param destination
	 *            HighLevelProcess the destination simulation model
	 * @param p
	 *            Perspective the type of timing information (execution,
	 *            waiting, sojourn time or case generation scheme) that needs to
	 *            be copied from the source simulation model to the destination
	 *            simulation model. If another perspective is chosen, nothing
	 *            will be copied.
	 */
	private void copyTimingInfoForPerspective(HLModel source,
			HLModel destination, HLTypes.Perspective p) {
		// when the case generation scheme is selected, it is not needed to
		// iterate over all the nodes
		if (p.equals(HLTypes.Perspective.CASE_GEN_SCHEME)) {
			destination.getHLProcess().getGlobalInfo().setCaseGenerationScheme(
					source.getHLProcess().getGlobalInfo()
							.getCaseGenerationScheme());
		} else {
			Iterator<ModelGraphVertex> graphNodes = source.getGraphNodes()
					.iterator();
			while (graphNodes.hasNext()) {
				ModelGraphVertex graphNode = graphNodes.next();
				// check whether a highlevelactivity belongs to the graphnode
				HLActivity actDestSimModel = destination
						.findActivity(graphNode);
				if (actDestSimModel != null) {
					// find the corresponding highlevelactivity in the selected
					// simulation model
					HLActivity actSourceSimModel = source
							.findActivity(graphNode);
					if (actSourceSimModel != null) {
						if (p.equals(HLTypes.Perspective.TIMING_EXECTIME)) {
							actDestSimModel.setExecutionTime(actSourceSimModel
									.getExecutionTime());
						} else if (p
								.equals(HLTypes.Perspective.TIMING_WAITTIME)) {
							actDestSimModel.setWaitingTime(actSourceSimModel
									.getWaitingTime());
						} else if (p.equals(HLTypes.Perspective.TIMING_SOJTIME)) {
							actDestSimModel.setSojournTime(actSourceSimModel
									.getSojournTime());
						}
					}
				}
			}
		}
	}

}
