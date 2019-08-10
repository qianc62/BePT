package org.processmining.analysis.mergesimmodels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.models.hlprocess.visualization.HLVisualization;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;

/**
 * Provides the GUI with which the user can indicate for each perspective from
 * which of the provided simulation models the specific information for that
 * perspective, provided by the selected simulation model, needs to be copied to
 * the final simulation model. The simulation information that is copied will be
 * visualized in a separate panel.
 * 
 * @author rmans
 */
public class MergeSimModelsUI extends JPanel implements Provider {

	/**
	 * the simulation models that are provided by the different analysis plugins
	 */
	private ArrayList<HLModel> mySimModels = null;

	/** the final simulation model that is provided by this analysis plugin */
	private HLModel myFinalSimModel = null;

	/** visualisation */
	private JPanel myGraphPanel = new JPanel(new BorderLayout());

	/**
	 * If the decision miner is used multiple times, each time the simulation
	 * model to be provided will have an incremented number (in order to
	 * distinguish them later when they e.g., need to be joined)
	 */
	private static int simulationModelCounter = 0;

	/** GUI related fields */
	private JSplitPane mySplitPanel = new JSplitPane();

	private JTabbedPane myTabsPanel = new JTabbedPane();
	private TimingTabUI myTimingPanel;
	private OrganizationalTabUI myOrganizationalPanel;
	private DataTabUI myDataPanel;
	private ChoicesTabUI myChoicesPanel;

	/**
	 * Basic constructor
	 * 
	 * @param simModels
	 *            ArrayList list of simulation models that need to be merged
	 */
	public MergeSimModelsUI(ArrayList<HLModel> simModels) {
		mySimModels = simModels;
		// create the final simulation model which has default simulation
		// information
		myFinalSimModel = (HLModel) simModels.get(0).clone();
		myFinalSimModel.reset();
		jbInit();
	}

	/**
	 * Construct the user interface for the merge simulation models analysis
	 * plugin.
	 */
	private void jbInit() {
		// increment static sim model counter for this plugin
		simulationModelCounter = simulationModelCounter + 1;
		// layout for the main panel
		this.setLayout(new BorderLayout());
		// initialize the grappaPanel (which contains the Petri net UI)
		myGraphPanel.removeAll();
		// visualize the petri net of the simulation model
		HLVisualization viz = new HLVisualization(myFinalSimModel
				.getVisualization(new HashSet()));
		myGraphPanel.add(viz.getPanel());
		myGraphPanel.validate();
		myGraphPanel.repaint();
		// add the split panel
		this.add(mySplitPanel, BorderLayout.CENTER);

		// fill the split panel with the components and set some options
		mySplitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mySplitPanel.setTopComponent(myTabsPanel);
		mySplitPanel.setBottomComponent(myGraphPanel);
		mySplitPanel.setDividerLocation(250);

		// generate the layout on the timing panel
		myTimingPanel = new TimingTabUI(myFinalSimModel, mySimModels,
				myGraphPanel);
		myDataPanel = new DataTabUI(myFinalSimModel, mySimModels, myGraphPanel);
		myOrganizationalPanel = new OrganizationalTabUI(myFinalSimModel,
				mySimModels, myGraphPanel);
		myChoicesPanel = new ChoicesTabUI(myFinalSimModel, mySimModels,
				myGraphPanel);
		// build tab views
		myTabsPanel.addTab("Timing", myTimingPanel);
		myTabsPanel.addTab("Organizational", myOrganizationalPanel);
		myTabsPanel.addTab("Data", myDataPanel);
		myTabsPanel.addTab("Choices", myChoicesPanel);
		myTabsPanel.setMinimumSize(new Dimension(22, 100));

		// set up listener for JTabbedPane object
		myTabsPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int index = myTabsPanel.getSelectedIndex();
				JPanel p = null;
				if (index == 0) {
					p = myTimingPanel.getVisualizationForSelection();
				} else if (index == 1) {
					p = myOrganizationalPanel.getVisualizationForSelection();
				} else if (index == 2) {
					p = myDataPanel.getVisualizationForSelection();
				} else if (index == 3) {
					p = myChoicesPanel.getVisualizationForSelection();
				}
				if (p != null) {
					myGraphPanel.removeAll();
					myGraphPanel.add(p);
					myGraphPanel.validate();
					myGraphPanel.repaint();
				}
			}
		});
		// visualize the selection on the timing tab
		myGraphPanel.removeAll();
		myGraphPanel.add(myTimingPanel.getVisualizationForSelection());
		myGraphPanel.validate();
		myGraphPanel.repaint();
		// add the perspectives to the final simulation model
		myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.TIMING_EXECTIME);
		myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.TIMING_WAITTIME);
		myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.TIMING_SOJTIME);
		myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.CASE_GEN_SCHEME);
		myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.ORGANIZATIONAL_MODEL);
		myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.ROLES_AT_TASKS);
		myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.DATA_AT_TASKS);
		myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.DATA_INITIAL_VAL);
		myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.DATA_VALUE_RANGE);
		myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.CHOICE_DATA);
		myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.CHOICE_PROB);
		myFinalSimModel.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.CHOICE_FREQ);
		myFinalSimModel.getHLProcess().getGlobalInfo().setName(
				"Merged Simulation Model (" + simulationModelCounter + ")");
		Message.add("<MergeSimModels>", Message.TEST);
		myFinalSimModel.getHLProcess().getGlobalInfo().writeToTestLog();
		Message.add("<MergeSimModels/>", Message.TEST);
	}

	/**
	 * Specifiy provided objects of the analysis that can be further used to,
	 * e.g., export an item.
	 * 
	 * @return An Array containing provided objects
	 */
	public ProvidedObject[] getProvidedObjects() {
		if (myFinalSimModel != null) {
			ProvidedObject[] objects = { new ProvidedObject(
					"Merged Simulation Model (" + simulationModelCounter + ")",
					new Object[] { myFinalSimModel }) };
			return objects;
		} else {
			return null;
		}
	}
}
