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
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLResource;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.models.hlprocess.visualization.HLVisualization;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyListRadio;
import org.processmining.framework.util.GuiPropertyStringTextarea;

/**
 * On the panel the names of the simulation models are shown which cover the
 * organizational perspective. For one of these simulation models, the user can
 * select the one for which the simulation information of the organizational
 * perspective needs to be copied to the final simulation model and which is
 * subsequently shown on the graph panel. <br/>
 * However, for the input simulation models already an initial selection is made
 * and copied to the final simulation model
 * 
 * @author rmans
 * @author arozinat
 */
public class OrganizationalTabUI extends JPanel {

	/**
	 * enumeration type for the perspectives that can be shown
	 */
	public enum OrgPerspectives {
		ORGMOD("Organizational model"), ROLEASS("Role assignments");

		private final String myName;

		/**
		 * Constructor for the enumeration type PerspectiveToShow
		 * 
		 * @param name
		 *            the name
		 */
		OrgPerspectives(String name) {
			myName = name;
		}

		public String toString() {
			return myName;
		}
	}

	/** for the organizational perspective also default values can be chosen */
	private final String DEFAULT = "none (default information)";
	/** the final simulation model */
	private HLModel myFinalSimModel;
	/** the final simulation model with default information */
	private HLModel myFinalSimModelDefault;
	/** the available simulation models */
	private ArrayList<HLModel> mySimModels;
	/**
	 * the panel on which the graph of the final simulation model has to be
	 * located
	 */
	private JPanel myGraphPanel;
	/** the currently selected perspective to show */
	private OrgPerspectives mySelectedPerspectiveToShow;
	/**
	 * The mapping from each perspective to the drop down box that contains the
	 * available simulation models for that perspective
	 */
	private HashMap<OrgPerspectives, GUIPropertyListEnumeration> myMappingPerspectiveToAvailableSimModels = new HashMap<OrgPerspectives, GUIPropertyListEnumeration>();
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
	public OrganizationalTabUI(HLModel finalSimModel,
			ArrayList<HLModel> simModels, JPanel gp) {
		super();
		myFinalSimModel = finalSimModel;
		myFinalSimModelDefault = (HLModel) myFinalSimModel.clone();
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
				"Here you can determine from which input simulation model the organizational information should be copied to the output model. "
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
				"Organizational information");
		perspectivePanel.setBorder(titledBorderChoicePanel);
		Border borderInputsPanel = BorderFactory.createEtchedBorder(
				Color.white, new Color(148, 145, 140));
		Border titledBorderInputsPanel = new TitledBorder(borderInputsPanel,
				"Get organizational information from");
		inputsPanel.setBorder(titledBorderInputsPanel);
		// create a radio list for the different perspectives
		ArrayList<String> valuesRadioList = new ArrayList<String>();
		OrgPerspectives[] perspectives = OrgPerspectives.values();
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

		// add the inputs panel to the main panel
		outmostLayer.add(inputsPanel);
		this.add(outmostLayer, BorderLayout.CENTER);
	}

	/**
	 * Based on the initial selection of some input simulation model, groups and
	 * resources are copied to the final simulation model.
	 */
	private void initialSelection() {
		// change the name of the final simulation model with default values
		myFinalSimModelDefault.getHLProcess().getGlobalInfo().setName(DEFAULT);
		ArrayList<HLModel> simModelsWithPerspective = new ArrayList<HLModel>();
		simModelsWithPerspective = MergeUtilities
				.generateUniqueNameSimModel(MergeUtilities
						.getSimModelsWithPerspective(mySimModels,
								HLTypes.Perspective.ORGANIZATIONAL_MODEL));
		simModelsWithPerspective.add(myFinalSimModelDefault);
		GUIPropertyListEnumeration inputSimModAtTasks = new GUIPropertyListEnumeration(
				"   Simulation model:  ",
				"Please select from which of the available input simulation models the organizational model should be copied",
				simModelsWithPerspective, new InputSimModelListener(), 400);
		myMappingPerspectiveToAvailableSimModels.put(OrgPerspectives.ORGMOD,
				inputSimModAtTasks);
		simModelsWithPerspective = MergeUtilities
				.generateUniqueNameSimModel(MergeUtilities
						.getSimModelsWithPerspective(mySimModels,
								HLTypes.Perspective.ROLES_AT_TASKS));
		simModelsWithPerspective.add(myFinalSimModelDefault);
		GUIPropertyListEnumeration inputSimModInitVal = new GUIPropertyListEnumeration(
				"   Simulation model:  ",
				"Please select from which of the available input simulation models the role assignments at tasks should be copied",
				simModelsWithPerspective, new InputSimModelListener(), 400);
		myMappingPerspectiveToAvailableSimModels.put(OrgPerspectives.ROLEASS,
				inputSimModInitVal);
		// initialize the first selection
		// based on the initial selection of the drop down box, you already copy
		// values to the final simulation model
		HLModel selectedAtTasks = (HLModel) myMappingPerspectiveToAvailableSimModels
				.get(OrgPerspectives.ORGMOD).getValue();
		copyOrgInfoForPerspective(selectedAtTasks, myFinalSimModel,
				HLTypes.Perspective.ORGANIZATIONAL_MODEL);
		HLModel selectedInitVal = (HLModel) myMappingPerspectiveToAvailableSimModels
				.get(OrgPerspectives.ROLEASS).getValue();
		copyOrgInfoForPerspective(selectedInitVal, myFinalSimModel,
				HLTypes.Perspective.ROLES_AT_TASKS);
		// current selected Data Perspectives
		mySelectedPerspectiveToShow = OrgPerspectives.ORGMOD;
		// update the gui
		myPerspectivesRadioList.notifyTarget();
	}

	// /**
	// * In the case that simulation models have the same names, unique names
	// will be generated, so that each simulation
	// * model in the list has an unique name
	// * @param simModels ArrayList list with simulation models
	// * @return ArrayList list with simulation models in which each model has
	// an unique name
	// */
	// private ArrayList<HLModel> generateUniqueNameSimModel(ArrayList<HLModel>
	// simModels) {
	// ArrayList<HLModel> returnModels = new ArrayList<HLModel>();
	// // check whether there are some names of the simModels the same
	// HashSet<String> usedNames = new HashSet<String>();
	// Iterator<HLModel> simModelNames = simModels.iterator();
	// while (simModelNames.hasNext()) {
	// HLModel simModel = simModelNames.next();
	// if
	// (usedNames.contains(simModel.getHLProcess().getGlobalInfo().getName())) {
	// // find another name for this simModel
	// int counter = 2;
	// while
	// (usedNames.contains(simModel.getHLProcess().getGlobalInfo().getName() +
	// counter)) {
	// // increment the counter
	// counter++;
	// }
	// // unique name found, add it to the used names list
	// usedNames.add(simModel.getHLProcess().getGlobalInfo().getName() +
	// counter);
	// // add set it as name for the respective simulation model
	// simModel.getHLProcess().getGlobalInfo().setName(simModel.getHLProcess().getGlobalInfo().getName()
	// + counter);
	// // add it to the models that have to be returned
	// returnModels.add(simModel);
	// }
	// else {
	// // add to the used names list
	// usedNames.add(simModel.getHLProcess().getGlobalInfo().getName());
	// // add it to the models that have to be returned
	// returnModels.add(simModel);
	// }
	// }
	// return returnModels;
	// }

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
		selectedPerspective.add(HLTypes.Perspective.ROLES_AT_TASKS);
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

	// /**
	// * Retrieves the simulation models in the list which cover the given
	// perspective
	// * @param simModels ArrayList list with simulation models
	// * @param p Perspective the perspective
	// * @return ArrayList the simulation models in the list which cover the
	// given perspective
	// */
	// private ArrayList<HLModel> getSimModelsWithPerspective(ArrayList<HLModel>
	// simModels, HLTypes.Perspective p) {
	// ArrayList<HLModel> returnSimModels = new ArrayList<HLModel>();
	// Iterator<HLModel> simModelsIt = simModels.iterator();
	// while (simModelsIt.hasNext()) {
	// HLModel simModel = simModelsIt.next();
	// if
	// (simModel.getHLProcess().getGlobalInfo().getPerspectives().contains(p)) {
	// returnSimModels.add(simModel);
	// }
	// }
	// return returnSimModels;
	// }

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
					OrgPerspectives.ORGMOD.toString())) {
				inputSimModels = myMappingPerspectiveToAvailableSimModels
						.get(OrgPerspectives.ORGMOD);
				mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
				mySelectedPerspectives
						.add(HLTypes.Perspective.ORGANIZATIONAL_MODEL);
				mySelectedPerspectiveToShow = OrgPerspectives.ORGMOD;
			} else if (myPerspectivesRadioList.getValue().equals(
					OrgPerspectives.ROLEASS.toString())) {
				inputSimModels = myMappingPerspectiveToAvailableSimModels
						.get(OrgPerspectives.ROLEASS);
				mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
				mySelectedPerspectives.add(HLTypes.Perspective.ROLES_AT_TASKS);
				mySelectedPerspectiveToShow = OrgPerspectives.ROLEASS;
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
	 * listener for the simulation model that is selected on the TimingTab
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
					OrgPerspectives.ORGMOD.toString())) {
				copyOrgInfoForPerspective(selectedSimModel, myFinalSimModel,
						HLTypes.Perspective.ORGANIZATIONAL_MODEL);
				perspectivesToShow
						.add(HLTypes.Perspective.ORGANIZATIONAL_MODEL);
			} else if (myPerspectivesRadioList.getValue().equals(
					OrgPerspectives.ROLEASS.toString())) {
				copyOrgInfoForPerspective(selectedSimModel, myFinalSimModel,
						HLTypes.Perspective.ROLES_AT_TASKS);
				perspectivesToShow.add(HLTypes.Perspective.ROLES_AT_TASKS);
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
	 * Copies the organizational information of the provided perspective from
	 * the source to the destination simulation model
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
	private void copyOrgInfoForPerspective(HLModel source, HLModel destination,
			HLTypes.Perspective p) {
		if (p.equals(HLTypes.Perspective.ORGANIZATIONAL_MODEL)) {
			// first remove all
			for (HLGroup hlGrp : destination.getHLProcess().getGroups()) {
				destination.getHLProcess()
						.removeGroupWithoutAffectingAssigmnets(hlGrp.getID());
			}
			for (HLResource hlRes : destination.getHLProcess().getResources()) {
				destination.getHLProcess().removeResource(hlRes.getID());
			}
			// now add groups and resources to destination model
			for (HLGroup hlGrp : source.getHLProcess().getGroups()) {
				HLGroup clonedGrp = (HLGroup) hlGrp.clone();
				destination.getHLProcess().addOrReplace(clonedGrp);
			}
			for (HLResource hlRes : source.getHLProcess().getResources()) {
				HLResource clonedRes = (HLResource) hlRes.clone();
				destination.getHLProcess().addOrReplace(clonedRes);
			}
		} else if (p.equals(HLTypes.Perspective.ROLES_AT_TASKS)) {
			for (HLActivity hlAct : destination.getHLProcess().getActivities()) {
				// find the corresponding activity in target model and add the
				// destination attribute
				ModelGraphVertex actNode = destination
						.findModelGraphVertexForActivity(hlAct.getID());
				HLActivity srcAct = source.findActivity(actNode);
				if (srcAct != null) {
					hlAct.setGroup(srcAct.getGroupID());
				} else {
					Message.add(
							"HLActivity could not be found in destination model when attempting to "
									+ "copy role assignments at tasks.",
							Message.ERROR);
				}
			}
		}
	}

}
