package org.processmining.analysis.mergesimmodels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.att.HLAttributeValue;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.models.hlprocess.visualization.HLVisualization;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyListRadio;
import org.processmining.framework.util.GuiPropertyStringTextarea;

/**
 * On the panel the names of the simulation models are shown which cover the
 * data perspective. For one of these simulation models, the user can select the
 * one for which the simulation information of the data perspective needs to be
 * copied to the final simulation model and which is subsequently shown on the
 * graph panel. <br/>
 * However, for the input simulation models already an initial selection is made
 * and copied to the final simulation model
 * 
 * @author rmans
 * @author arozinat
 */
public class DataTabUI extends JPanel {

	/**
	 * enumeration type for the perspectives that can be shown
	 */
	public enum DataPerspectives {
		INITVAL("Initial values"), VALRANGE("Value range"), ATTASKS(
				"Providing activities");

		private final String myName;

		/**
		 * Constructor for the enumeration type PerspectiveToShow
		 * 
		 * @param name
		 *            the name
		 */
		DataPerspectives(String name) {
			myName = name;
		}

		public String toString() {
			return myName;
		}
	}

	/** for the data perspective also default values can be chosen */
	private final String DEFAULT = "none (default information)";
	/** the final simulation model */
	private HLModel myFinalSimModel;
	/** the final simulation model with default information */
	private HLModel myFinalSimModelDefault;
	/** the available simulation models */
	private ArrayList<HLModel> mySimModels;
	/** the simulation model that is currently selected */
	/**
	 * the panel on which the graph of the final simulation model has to be
	 * located
	 */
	private JPanel myGraphPanel;
	/** the currently selected perspective to show */
	private DataPerspectives mySelectedPerspectiveToShow;
	/**
	 * The mapping from each perspective to the drop down box that contains the
	 * available simulation models for that perspective
	 */
	private HashMap<DataPerspectives, GUIPropertyListEnumeration> myMappingPerspectiveToAvailableSimModels = new HashMap<DataPerspectives, GUIPropertyListEnumeration>();

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
	 *            ArrayList the available simulation models
	 * @param gp
	 *            JPanel the panel on which the graph of the final simulation
	 *            model has to be located
	 */
	public DataTabUI(HLModel finalSimModel, ArrayList<HLModel> simModels,
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
	 * setting up the GUI.
	 */
	private void jbInit() {
		this.setLayout(new BorderLayout());
		// add user help text at the top of the tab
		String description = new String(
				"Here you can determine from which input simulation model the data attribute information should be copied to the output model. "
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
				"Data information for");
		perspectivePanel.setBorder(titledBorderChoicePanel);
		Border borderInputsPanel = BorderFactory.createEtchedBorder(
				Color.white, new Color(148, 145, 140));
		Border titledBorderInputsPanel = new TitledBorder(borderInputsPanel,
				"Get particular data information from");
		inputsPanel.setBorder(titledBorderInputsPanel);
		// create a radio list for the different perspectives
		ArrayList<String> valuesRadioList = new ArrayList<String>();
		DataPerspectives[] perspectives = DataPerspectives.values();
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
	 * Based on the initial selection of some input simulation model, data
	 * attribute values are copied to the final simulation model.
	 */
	private void initialSelection() {
		// change the name of the final simulation model with default values
		myFinalSimModelDefault.getHLProcess().getGlobalInfo().setName(DEFAULT);
		ArrayList<HLModel> simModelsWithPerspective = new ArrayList<HLModel>();
		simModelsWithPerspective = MergeUtilities
				.generateUniqueNameSimModel(MergeUtilities
						.getSimModelsWithPerspective(mySimModels,
								HLTypes.Perspective.DATA_AT_TASKS));
		simModelsWithPerspective.add(myFinalSimModelDefault);
		GUIPropertyListEnumeration inputSimModAtTasks = new GUIPropertyListEnumeration(
				"   Simulation model:  ",
				"Please select from which of the available input simulation models the attribute mappings at tasks should be copied",
				simModelsWithPerspective, new InputSimModelListener(), 400);
		myMappingPerspectiveToAvailableSimModels.put(DataPerspectives.ATTASKS,
				inputSimModAtTasks);
		simModelsWithPerspective = MergeUtilities
				.generateUniqueNameSimModel(MergeUtilities
						.getSimModelsWithPerspective(mySimModels,
								HLTypes.Perspective.DATA_INITIAL_VAL));
		simModelsWithPerspective.add(myFinalSimModelDefault);
		GUIPropertyListEnumeration inputSimModInitVal = new GUIPropertyListEnumeration(
				"   Simulation model:  ",
				"Please select from which of the available input simulation models the initial values should be copied",
				simModelsWithPerspective, new InputSimModelListener(), 400);
		myMappingPerspectiveToAvailableSimModels.put(DataPerspectives.INITVAL,
				inputSimModInitVal);
		simModelsWithPerspective = MergeUtilities
				.generateUniqueNameSimModel(MergeUtilities
						.getSimModelsWithPerspective(mySimModels,
								HLTypes.Perspective.DATA_VALUE_RANGE));
		simModelsWithPerspective.add(myFinalSimModelDefault);
		GUIPropertyListEnumeration inputSimModValRange = new GUIPropertyListEnumeration(
				"   Simulation model:  ",
				"Please select from which of the available input simulation models the possible values (value range) should be copied",
				simModelsWithPerspective, new InputSimModelListener(), 400);
		myMappingPerspectiveToAvailableSimModels.put(DataPerspectives.VALRANGE,
				inputSimModValRange);
		// initialize the first selection
		// based on the initial selection of the drop down box, you already copy
		// values to the final simulation model
		HLModel selectedAtTasks = (HLModel) myMappingPerspectiveToAvailableSimModels
				.get(DataPerspectives.ATTASKS).getValue();
		copyDataInfoForPerspective(selectedAtTasks, myFinalSimModel,
				HLTypes.Perspective.DATA_AT_TASKS);
		HLModel selectedInitVal = (HLModel) myMappingPerspectiveToAvailableSimModels
				.get(DataPerspectives.INITVAL).getValue();
		copyDataInfoForPerspective(selectedInitVal, myFinalSimModel,
				HLTypes.Perspective.DATA_INITIAL_VAL);
		HLModel selectedValRange = (HLModel) myMappingPerspectiveToAvailableSimModels
				.get(DataPerspectives.VALRANGE).getValue();
		copyDataInfoForPerspective(selectedValRange, myFinalSimModel,
				HLTypes.Perspective.DATA_VALUE_RANGE);
		// current selected Data Perspectives
		mySelectedPerspectiveToShow = DataPerspectives.INITVAL;
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
				.get(mySelectedPerspectiveToShow);
		HashSet<HLTypes.Perspective> selectedPerspective = new HashSet<HLTypes.Perspective>();
		// if (mySelectedPerspectiveToShow.equals(DataPerspectives.ATTASKS)) {
		selectedPerspective.add(HLTypes.Perspective.DATA_AT_TASKS);
		// }
		// else if
		// (mySelectedPerspectiveToShow.equals(DataPerspectives.INITVAL)) {
		// selectedPerspective.add(HLTypes.Perspective.DATA_INITIAL_VAL);
		// }
		// else if
		// (mySelectedPerspectiveToShow.equals(DataPerspectives.VALRANGE)) {
		// selectedPerspective.add(HLTypes.Perspective.DATA_VALUE_RANGE);
		// }
		HLVisualization viz = new HLVisualization(((HLModel) selectedListEnum
				.getValue()).getVisualization(selectedPerspective));
		return viz.getPanel();
	}

	/**
	 * Listener for the perspectives
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
					DataPerspectives.INITVAL.toString())) {
				inputSimModels = myMappingPerspectiveToAvailableSimModels
						.get(DataPerspectives.INITVAL);
				mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
				mySelectedPerspectives
						.add(HLTypes.Perspective.DATA_INITIAL_VAL);
				mySelectedPerspectiveToShow = DataPerspectives.INITVAL;
			} else if (myPerspectivesRadioList.getValue().equals(
					DataPerspectives.VALRANGE.toString())) {
				inputSimModels = myMappingPerspectiveToAvailableSimModels
						.get(DataPerspectives.VALRANGE);
				mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
				mySelectedPerspectives
						.add(HLTypes.Perspective.DATA_VALUE_RANGE);
				mySelectedPerspectiveToShow = DataPerspectives.VALRANGE;
			} else if (myPerspectivesRadioList.getValue().equals(
					DataPerspectives.ATTASKS.toString())) {
				inputSimModels = myMappingPerspectiveToAvailableSimModels
						.get(DataPerspectives.ATTASKS);
				mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
				mySelectedPerspectives.add(HLTypes.Perspective.DATA_AT_TASKS);
				mySelectedPerspectiveToShow = DataPerspectives.ATTASKS;
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
	 * listener for the simulation model that is selected on the DataTab
	 */
	class InputSimModelListener implements GuiNotificationTarget {

		public void updateGUI() {
			// get the name of the selected simulation model, dependent on the
			// selected perspective
			HLModel selectedSimModel = (HLModel) myMappingPerspectiveToAvailableSimModels
					.get(mySelectedPerspectiveToShow).getValue();
			HashSet<HLTypes.Perspective> perspectivesToShow = new HashSet<HLTypes.Perspective>();
			// copy the selected timing information from the input simulation
			// model to the final simulation model
			if (myPerspectivesRadioList.getValue().equals(
					DataPerspectives.ATTASKS.toString())) {
				copyDataInfoForPerspective(selectedSimModel, myFinalSimModel,
						HLTypes.Perspective.DATA_AT_TASKS);
				perspectivesToShow.add(HLTypes.Perspective.DATA_AT_TASKS);
			} else if (myPerspectivesRadioList.getValue().equals(
					DataPerspectives.INITVAL.toString())) {
				copyDataInfoForPerspective(selectedSimModel, myFinalSimModel,
						HLTypes.Perspective.DATA_INITIAL_VAL);
				perspectivesToShow.add(HLTypes.Perspective.DATA_INITIAL_VAL);
			} else if (myPerspectivesRadioList.getValue().equals(
					DataPerspectives.VALRANGE.toString())) {
				copyDataInfoForPerspective(selectedSimModel, myFinalSimModel,
						HLTypes.Perspective.DATA_VALUE_RANGE);
				perspectivesToShow.add(HLTypes.Perspective.DATA_VALUE_RANGE);
			}
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
	 * Copies the data information of the provided perspective from the source
	 * to the destination simulation model
	 * 
	 * @param source
	 *            HighLevelProcess the source simulation model
	 * @param destination
	 *            HighLevelProcess the destination simulation model
	 * @param p
	 *            Perspective the type of data information (initial values,
	 *            value range, data at tasks) that needs to be copied from the
	 *            source simulation model to the destination simulation model.
	 *            If another perspective is chosen, nothing will be copied.
	 */
	private void copyDataInfoForPerspective(HLModel source,
			HLModel destination, HLTypes.Perspective p) {
		// if data at tasks should be copied, remove all data references at
		// tasks first
		if (p.equals(HLTypes.Perspective.DATA_AT_TASKS)) {
			for (HLActivity hlAct : destination.getHLProcess().getActivities()) {
				for (HLID attID : hlAct.getOutputDataAttributeIDs()) {
					hlAct.removeOutputDataAttribute(attID);
				}
				for (HLID attID : hlAct.getInputDataAttributeIDs()) {
					hlAct.removeInputDataAttribute(attID);
				}

			}
		}
		// in any case first check whether all the data attributes from source
		// model are in destination model
		for (HLAttribute sourceAtt : source.getHLProcess().getAttributes()) {
			HLAttribute destinationAtt = destination.getHLProcess()
					.findAttributeByName(sourceAtt.getName());
			if (destinationAtt == null) {
				HLAttribute clonedAtt = (HLAttribute) sourceAtt.clone();
				destination.getHLProcess().addOrReplace(clonedAtt);
				destinationAtt = clonedAtt;
			}
			// copy only information relevant for perspective
			if (p.equals(HLTypes.Perspective.DATA_INITIAL_VAL)) {
				destinationAtt.setInitialValue((HLAttributeValue) sourceAtt
						.getInitialValue().clone());
			} else if (p.equals(HLTypes.Perspective.DATA_VALUE_RANGE)) {
				destinationAtt.setPossibleValues((HLAttributeValue) sourceAtt
						.getPossibleValues().clone());
			} else if (p.equals(HLTypes.Perspective.DATA_AT_TASKS)) {
				HLID destAttID = destinationAtt.getID();
				for (HLActivity hlAct : source.getHLProcess().getActivities()) {
					if (hlAct.hasOutputDataAttribute(sourceAtt.getID())) {
						// find the corresponding activity in target model and
						// add the destination attribute
						ModelGraphVertex actNode = source
								.findModelGraphVertexForActivity(hlAct.getID());
						HLActivity destAct = destination.findActivity(actNode);
						if (destAct != null) {
							destAct.addOutputDataAttribute(destAttID);
						} else {
							Message
									.add(
											"HLActivity could not be found in destination model when attempting to "
													+ "copy data attribute references at tasks.",
											Message.ERROR);
						}
					}
					if (hlAct.hasInputDataAttribute(sourceAtt.getID())) {
						// find the corresponding activity in target model and
						// add the destination attribute
						ModelGraphVertex actNode = source
								.findModelGraphVertexForActivity(hlAct.getID());
						HLActivity destAct = destination.findActivity(actNode);
						if (destAct != null) {
							destAct.addInputDataAttribute(destAttID);
						} else {
							Message
									.add(
											"HLActivity could not be found in destination model when attempting to "
													+ "copy data attribute references at tasks.",
											Message.ERROR);
						}
					}
				}
			}
		}
	}

}
