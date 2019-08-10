package org.processmining.analysis.eventmodelmerge;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLTypes.Perspective;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.models.hlprocess.visualization.HLVisualization;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyStringTextarea;

/**
 * The data tab of the EventModelMerge plugin presents possibilities to the user
 * for in which way he wants to merge information about data attributes in the
 * case that activities that refer to events are merged into activities that
 * only refer to activities.
 * 
 * @author rmans
 */
public class DataTabUI extends JPanel implements GuiNotificationTarget {

	/** the input simulation model */
	private HLModel myInputSimModel;
	/** the final simulation model */
	private HLModel myFinalSimModel;

	/**
	 * the mapping from the highlevelactivities in the final simulation model to
	 * the highlevelactivities in the input simulation model that are merged
	 */
	private HashMap<HLID, ArrayList<HLID>> myMapping;

	/** the panel on which the graph is located */
	private JPanel myGraphPanel;

	/**
	 * constructor
	 * 
	 * @param inputSimModel
	 *            HighLevelProcess the simulation model from which the
	 *            information of the data attributes has to be obtained
	 * @param finalSimModel
	 *            HighLevelProcess the simulation model to which the information
	 *            about the data attributes has to be copied to.
	 * @param mapping
	 *            HashMap the mapping from a highlevelactivity in the final
	 *            simulation model to the highlevelactivities in the input
	 *            simulation model that have been merged into that activity
	 * @param graphPanel
	 *            JPanel the panel on which the graph of the final simulation
	 *            model needs to be visualized.
	 */
	public DataTabUI(HLModel inputSimModel, HLModel finalSimModel,
			HashMap<HLID, ArrayList<HLID>> mapping, JPanel graphPanel) {
		super();
		myInputSimModel = inputSimModel;
		myFinalSimModel = finalSimModel;
		myMapping = mapping;
		myGraphPanel = graphPanel;
		// initially copy the super set information (default case)
		initDefaults();
		// buld GUI
		jbInit();
	}

	/**
	 * At plugin startup the superset of all low-level activities will be used
	 * for the target model (user may deselect certain activity types later on).
	 */
	private void initDefaults() {
		// copy the data attribute objects first..
		Iterator<HLAttribute> attIt = myInputSimModel.getHLProcess()
				.getAttributes().iterator();
		while (attIt.hasNext()) {
			HLAttribute att = attIt.next();
			HLAttribute clonedAtt = (HLAttribute) att.clone();
			myFinalSimModel.getHLProcess().addOrReplace(clonedAtt);
		}
		// .. now set the references to attributes at the activities
		Iterator<Entry<HLID, ArrayList<HLID>>> it = myMapping.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<HLID, ArrayList<HLID>> entry = it.next();
			ArrayList<HLID> values = entry.getValue();
			// search for a highlevelactivity in values to which data attributes
			// are attached
			Iterator<HLID> valuesIt = values.iterator();
			while (valuesIt.hasNext()) {
				HLID origActID = valuesIt.next();
				HLActivity origAct = myInputSimModel.getHLProcess()
						.getActivity(origActID);
				// HLActivity origAct = valuesIt.next();
				if (origAct.getOutputDataAttributes().size() > 0) {
					HLID finalActID = entry.getKey();
					HLActivity finalAct = myFinalSimModel.getHLProcess()
							.getActivity(finalActID);
					// HLActivity finalAct = entry.getKey();
					Iterator<HLAttribute> dataAttrs = origAct
							.getOutputDataAttributes().iterator();
					while (dataAttrs.hasNext()) {
						HLAttribute dataAttr = dataAttrs.next();
						finalAct.addOutputDataAttribute(dataAttr.getID());
					}
					break;
				}
				if (origAct.getInputDataAttributes().size() > 0) {
					HLID finalActID = entry.getKey();
					HLActivity finalAct = myFinalSimModel.getHLProcess()
							.getActivity(finalActID);
					// HLActivity finalAct = entry.getKey();
					Iterator<HLAttribute> dataAttrs = origAct
							.getInputDataAttributes().iterator();
					while (dataAttrs.hasNext()) {
						HLAttribute dataAttr = dataAttrs.next();
						finalAct.addInputDataAttribute(dataAttr.getID());
					}
					break;
				}
			}
		}
	}

	/**
	 * setting up the GUI.
	 */
	private void jbInit() {
		this.setLayout(new BorderLayout());
		// add user help text at the top of the tab
		String description = new String(
				"Several activities may have been merged into one, higher level activity. "
						+ "Here you can determine from which of the former low-level activities the associated data attributes will be copied to the output model.");
		GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
				description);
		this.add(BorderLayout.NORTH, helpText.getPropertyPanel());

		JPanel outmostLayer = new JPanel();
		// to be done..
		JLabel message = new JLabel(
				"<html><br><br>Different strategies still need to be implemented. <br><br>Currently, only the default strategy is available: the super set of the attributes from all low-level activities is taken.</html>");
		outmostLayer.add(message);

		this.add(outmostLayer, BorderLayout.CENTER);
	}

	/*
	 * Only redraws the visualization panel (triggered by changed tab pane on
	 * global GUI or by changed user selection)
	 */
	public void updateGUI() {
		// repaint the graphpanel
		myGraphPanel.removeAll();
		HashSet<Perspective> perspectives = new HashSet<Perspective>();
		perspectives.add(Perspective.DATA_AT_TASKS);
		perspectives.add(Perspective.CHOICE_DATA);
		// HLVisualization viz = new
		// HLVisualization(myFinalSimModel.getProcessModel());
		HLVisualization viz = new HLVisualization(myFinalSimModel
				.getVisualization(perspectives));
		myGraphPanel.add(viz.getPanel());
		myGraphPanel.validate();
		myGraphPanel.repaint();
	}
}
