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
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLCondition;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.models.hlprocess.visualization.HLVisualization;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyListRadio;
import org.processmining.framework.util.GuiPropertyStringTextarea;

/**
 * Panel on which the user can select a choice perspective (data dependencies,
 * probability dependencies, frequency dependencies) and then the names of the
 * simulation models are shown which the selected perspective. For one of these
 * simulation models, the user can select the one for which the simulation
 * information for the selected perspective needs to be copied to the final
 * simulation model and which is subsequently shown on the graph panel.
 * 
 * @author rmans
 */
public class ChoicesTabUI extends JPanel {

	/**
	 * enumeration type for the choices
	 * 
	 * @author rmans
	 */
	public enum Choice {
		DATA("Data attributes"), PROB("Probabilities"), FREQ("Frequencies"), ;

		private final String myName;

		/**
		 * Constructor for the enumeration type Choice
		 */
		Choice(String name) {
			this.myName = name;
		}

		public String toString() {
			return myName;
		}
	}

	/** for the different choice perspectives also default values can be chosen */
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
	private Choice mySelectedPerspectiveToShow;
	/**
	 * the mapping from each perspective to the drop down box that contains the
	 * available simulation models for that perspective
	 */
	private HashMap<Choice, GUIPropertyListEnumeration> myMappingPerspectiveToAvailableSimModels = new HashMap<Choice, GUIPropertyListEnumeration>();

	/* GUI properties */
	private JPanel choicePanel;
	private JPanel inputsPanel;
	private GuiPropertyListRadio myChoicesRadioList;

	/**
	 * Basic constructor
	 * 
	 * @param finalSimModel
	 *            HighLevelProcess the final simulation that is going to be
	 *            exported
	 * @param simModels
	 *            ArrayList the available simulation models
	 * @param gp
	 *            JPanel the panel on which the graph of the final simulation
	 *            model needs to be shown
	 */
	public ChoicesTabUI(HLModel finalSimModel, ArrayList<HLModel> simModels,
			JPanel gp) {
		super();
		myFinalSimModel = finalSimModel;
		myFinalSimModelDefault = (HLModel) finalSimModel.clone();
		myFinalSimModelDefault.reset();
		mySimModels = simModels;
		myGraphPanel = gp;
		jbInit();
	}

	/** constructs the GUI */
	private void jbInit() {

		this.setLayout(new BorderLayout());
		// add user help text at the top of the tab
		String description = new String(
				"For each of the choice perspectives below you can determine from which input simulation model the information should be copied to the output model. "
						+ "If none of the input models has been chosen (or none provides the corresponding information), default information will be used instead. ");
		GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
				description);
		this.add(BorderLayout.NORTH, helpText.getPropertyPanel());

		JPanel outmostLayer = new JPanel();
		// create choice panel and inputs panel
		choicePanel = new JPanel();
		inputsPanel = new JPanel();
		BorderLayout borderLayoutInputsPanel = new BorderLayout();
		inputsPanel.setLayout(borderLayoutInputsPanel);
		// set the layout for this panel
		outmostLayer.setLayout(new BoxLayout(outmostLayer, BoxLayout.X_AXIS));
		// create titled borders for the choice and the inputs panel
		Border borderChoicePanel = BorderFactory.createEtchedBorder(
				Color.white, new Color(148, 145, 140));
		Border titledBorderChoicePanel = new TitledBorder(borderChoicePanel,
				"Choice based on");
		choicePanel.setBorder(titledBorderChoicePanel);
		Border borderInputsPanel = BorderFactory.createEtchedBorder(
				Color.white, new Color(148, 145, 140));
		Border titledBorderInputsPanel = new TitledBorder(borderInputsPanel,
				"Get particular choice information from");
		inputsPanel.setBorder(titledBorderInputsPanel);
		// create a radio list for the different choices: probabilities,
		// frequencies and choice based on data
		ArrayList<String> valuesRadioList = new ArrayList<String>();
		Choice[] choices = Choice.values();
		for (int i = 0; i < choices.length; i++) {
			valuesRadioList.add(choices[i].toString());
		}
		myChoicesRadioList = new GuiPropertyListRadio("", "", valuesRadioList,
				new ChoiceSimModelListener());
		choicePanel.add(myChoicesRadioList.getPropertyPanel());
		// add the choice panel and the inputs panel to the main panel
		outmostLayer.add(choicePanel);
		outmostLayer.add(inputsPanel);
		this.add(outmostLayer, BorderLayout.CENTER);
		// initial selection
		initialSelection();
	}

	/**
	 * Based on the initial selection of some input simulation model, groups and
	 * resources are copied to the final simulation model.
	 */
	private void initialSelection() {
		// change the name of the final simulation model with default values
		myFinalSimModelDefault.getHLProcess().getGlobalInfo().setName(DEFAULT);
		ArrayList<HLModel> simModelsWithPerspective = new ArrayList<HLModel>();
		// choice based on data
		simModelsWithPerspective = MergeUtilities
				.generateUniqueNameSimModel(MergeUtilities
						.getSimModelsWithPerspective(mySimModels,
								HLTypes.Perspective.CHOICE_DATA));
		simModelsWithPerspective.add(myFinalSimModelDefault);
		GUIPropertyListEnumeration inputSimModChoiceData = new GUIPropertyListEnumeration(
				"   Simulation model:  ",
				"Please select from which of the available input simulation models the data-based decision rule information should be copied",
				simModelsWithPerspective, new InputSimModelListener(), 400);
		myMappingPerspectiveToAvailableSimModels.put(Choice.DATA,
				inputSimModChoiceData);
		// choice based on probabilities
		simModelsWithPerspective = MergeUtilities
				.generateUniqueNameSimModel(MergeUtilities
						.getSimModelsWithPerspective(mySimModels,
								HLTypes.Perspective.CHOICE_PROB));
		simModelsWithPerspective.add(myFinalSimModelDefault);
		GUIPropertyListEnumeration inputSimModChoiceProb = new GUIPropertyListEnumeration(
				"   Simulation model:  ",
				"Please select from which of the available input simulation models the probability-based decision rule information should be copied",
				simModelsWithPerspective, new InputSimModelListener(), 400);
		myMappingPerspectiveToAvailableSimModels.put(Choice.PROB,
				inputSimModChoiceProb);
		// choice based on frequencies
		simModelsWithPerspective = MergeUtilities
				.generateUniqueNameSimModel(MergeUtilities
						.getSimModelsWithPerspective(mySimModels,
								HLTypes.Perspective.CHOICE_FREQ));
		simModelsWithPerspective.add(myFinalSimModelDefault);
		GUIPropertyListEnumeration inputSimModChoiceFreq = new GUIPropertyListEnumeration(
				"   Simulation model:  ",
				"Please select from which of the available input simulation models the frequency-based decision rule information should be copied",
				simModelsWithPerspective, new InputSimModelListener(), 400);
		myMappingPerspectiveToAvailableSimModels.put(Choice.FREQ,
				inputSimModChoiceFreq);
		// initialize the first selection
		// based on the initial selection of the drop down box, you already copy
		// values to the final simulation model
		// choice based on data
		HLModel selectedChoiceData = (HLModel) myMappingPerspectiveToAvailableSimModels
				.get(Choice.DATA).getValue();
		copyChoicesForPerspective(selectedChoiceData, myFinalSimModel,
				HLTypes.Perspective.CHOICE_DATA);
		// waiting time
		HLModel selectedChoiceProb = (HLModel) myMappingPerspectiveToAvailableSimModels
				.get(Choice.PROB).getValue();
		copyChoicesForPerspective(selectedChoiceProb, myFinalSimModel,
				HLTypes.Perspective.CHOICE_PROB);
		// sojourn time
		HLModel selectedChoiceFreq = (HLModel) myMappingPerspectiveToAvailableSimModels
				.get(Choice.FREQ).getValue();
		copyChoicesForPerspective(selectedChoiceFreq, myFinalSimModel,
				HLTypes.Perspective.CHOICE_FREQ);
		// current selected PerspectiveToShow
		mySelectedPerspectiveToShow = Choice.DATA;
		// update the gui
		myChoicesRadioList.notifyTarget();
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
		if (mySelectedPerspectiveToShow.equals(Choice.DATA)) {
			selectedPerspective.add(HLTypes.Perspective.CHOICE_DATA);
		} else if (mySelectedPerspectiveToShow.equals(Choice.PROB)) {
			selectedPerspective.add(HLTypes.Perspective.CHOICE_PROB);
		} else if (mySelectedPerspectiveToShow.equals(Choice.FREQ)) {
			selectedPerspective.add(HLTypes.Perspective.CHOICE_FREQ);
		}
		HLVisualization viz = new HLVisualization(((HLModel) selectedListEnum
				.getValue()).getVisualization(selectedPerspective));
		return viz.getPanel();
	}

	/**
	 * Class handling the selection state change of the radio buttons for the
	 * choices (decision points) in the simulation model
	 */
	class ChoiceSimModelListener implements GuiNotificationTarget {

		public void updateGUI() {
			// dependent on the selected perspective, show the corresponding
			// drop down box on the right side panel
			GUIPropertyListEnumeration inputSimModels = null;
			HashSet<HLTypes.Perspective> mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
			if (myChoicesRadioList.getValue().equals(Choice.DATA.toString())) {
				inputSimModels = myMappingPerspectiveToAvailableSimModels
						.get(Choice.DATA);
				mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
				mySelectedPerspectives.add(HLTypes.Perspective.CHOICE_DATA);
				mySelectedPerspectiveToShow = Choice.DATA;
			} else if (myChoicesRadioList.getValue().equals(
					Choice.PROB.toString())) {
				inputSimModels = myMappingPerspectiveToAvailableSimModels
						.get(Choice.PROB);
				mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
				mySelectedPerspectives.add(HLTypes.Perspective.CHOICE_PROB);
				mySelectedPerspectiveToShow = Choice.PROB;
			} else if (myChoicesRadioList.getValue().equals(
					Choice.FREQ.toString())) {
				inputSimModels = myMappingPerspectiveToAvailableSimModels
						.get(Choice.FREQ);
				mySelectedPerspectives = new HashSet<HLTypes.Perspective>();
				mySelectedPerspectives.add(HLTypes.Perspective.CHOICE_FREQ);
				mySelectedPerspectiveToShow = Choice.FREQ;
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
	 * listener for the simulation model that is selected on the choicesTab. In
	 * the case that a simulation model is selected by the user, the information
	 * that belongs to the selected perspective (data, frequencies or
	 * probabilities) is copied to the final simulation model and shown to the
	 * user.
	 */
	class InputSimModelListener implements GuiNotificationTarget {
		public void updateGUI() {
			// get the name of the selected simulation model
			HLModel selectedSimModel = (HLModel) myMappingPerspectiveToAvailableSimModels
					.get(mySelectedPerspectiveToShow).getValue();
			HashSet<HLTypes.Perspective> perspectives = new HashSet<HLTypes.Perspective>();
			if (myChoicesRadioList.getValue().equals(Choice.DATA.toString())) {
				copyChoicesForPerspective(selectedSimModel, myFinalSimModel,
						HLTypes.Perspective.CHOICE_DATA);
				perspectives.add(HLTypes.Perspective.CHOICE_DATA);
			} else if (myChoicesRadioList.getValue().equals(
					Choice.PROB.toString())) {
				copyChoicesForPerspective(selectedSimModel, myFinalSimModel,
						HLTypes.Perspective.CHOICE_PROB);
				perspectives.add(HLTypes.Perspective.CHOICE_PROB);
			} else if (myChoicesRadioList.getValue().equals(
					Choice.FREQ.toString())) {
				copyChoicesForPerspective(selectedSimModel, myFinalSimModel,
						HLTypes.Perspective.CHOICE_FREQ);
				perspectives.add(HLTypes.Perspective.CHOICE_FREQ);
			}
			// visualize the choices based on data
			myGraphPanel.removeAll();
			HLVisualization viz = new HLVisualization(myFinalSimModel
					.getVisualization(perspectives));
			myGraphPanel.add(viz.getPanel());
			myGraphPanel.validate();
			myGraphPanel.repaint();
		}
	}

	/**
	 * Copies all the information about the choices from the source
	 * highlevelprocess to the destination highlevelprocess. That is, all the
	 * data dependencies, probability dependencies, frequency dependencies are
	 * copied from the source highlevelprocess to the destination
	 * highlevelprocess. <br>
	 * It is assumed that the graph of both highlevelprocesses is the same.
	 * 
	 * @param source
	 *            HighLevelProcess the source highlevelprocess
	 * @param destination
	 *            HighLevelProcess the destination highlevelprocess
	 * @param p
	 *            Perspective the type of dependency that needs to be copied
	 *            from the source highlevelprocess to the destination
	 *            highlevelprocess. Available options are
	 *            Perspective.CHOICE_DATA, Perspective.CHOICE_PROB and
	 *            Perspective.CHOICE_FREQ. If another perspective is chosen,
	 *            nothing will be copied.
	 */
	private void copyChoicesForPerspective(HLModel source, HLModel destination,
			HLTypes.Perspective p) {
		Iterator<HLChoice> choicesInput = source.getHLProcess().getChoices()
				.iterator();
		while (choicesInput.hasNext()) {
			HLChoice choiceInput = choicesInput.next();
			// since it is assumed that the underlying model is the same - the
			// choices can be
			// obtained via the choice node from the high level process
			ModelGraphVertex choiceNode = source
					.findModelGraphVertexForChoice(choiceInput.getID());
			HLChoice destChoice = destination.findChoice(choiceNode);
			Iterator<HLID> choiceTargetIt = destChoice.getChoiceTargetIDs()
					.iterator();
			while (choiceTargetIt.hasNext()) {
				HLID targetID = choiceTargetIt.next();
				ModelGraphVertex actNode = destination
						.findModelGraphVertexForActivity(targetID);
				HLActivity sourceAct = source.findActivity(actNode);
				HLCondition sourceCond = choiceInput.getCondition(sourceAct
						.getID());
				HLCondition destCond = destChoice.getCondition(targetID);
				if (sourceCond != null && destCond != null) {
					// data attributes
					if (p.equals(HLTypes.Perspective.CHOICE_DATA)) {
						destCond.setExpression(sourceCond.getExpression());
					}
					// probability dependencies
					else if (p.equals(HLTypes.Perspective.CHOICE_PROB)) {
						destCond.setProbability(sourceCond.getProbability());
					}
					// frequency dependencies
					else if (p.equals(HLTypes.Perspective.CHOICE_FREQ)) {
						destCond.setFrequency(sourceCond.getFrequency());
					}
				}
			}
		}
	}

}
