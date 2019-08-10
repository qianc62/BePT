/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/
package org.processmining.framework.models.hlprocess.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.deckfour.slickerbox.components.SlickTabbedPane;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.util.SlickerSwingUtils;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLProcess;
import org.processmining.framework.models.hlprocess.att.HLNominalAttribute;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.models.hlprocess.visualization.HLVisualization;
import org.processmining.framework.util.GenericTableModelPanel;
import org.processmining.framework.util.GuiDisplayable;
import org.processmining.framework.util.GuiNotificationTargetStateful;

/**
 * Creates a Gui representation of the given high level process. High level
 * information can be manipulated via Gui elements and the changes are instantly
 * reported back to the underlying data structure.
 * <p>
 * Depending on whether was initialized by a plain high level process or by a
 * process model type implementation (e.g., Petri net or YAWL) a graphical
 * visualization of affected graph nodes can be provided.
 * 
 * @see HLActivityGui
 * @see HLAttributeGui
 * @see HLChoiceGui
 * @see HLConditionGui
 * @see HLGlobalGui
 */
public class HLProcessGui implements GuiDisplayable,
		GuiNotificationTargetStateful {

	protected HLModel hlProcessImpl = null;
	protected HLProcess hlProcess;
	protected HLVisualization hlVisualization;
	// GUI attributes
	protected JPanel processPanel = new JPanel();
	protected JPanel attributesPanel = new JPanel();
	protected GenericTableModelPanel attTable;
	protected JPanel visualizationPanel = new JPanel(new BorderLayout());
	protected SlickTabbedPane tabPane = new SlickTabbedPane();
	protected JButton addAttributeButton = new SlickerButton("Add Attribute");
	/** adds a new data attribute */
	protected JButton removeAttributeButton = new SlickerButton(
			"Remove Selected");
	/** removes existing data attribute */
	public static Color bgColor = new Color(190, 190, 190);

	/**
	 * Constructor based on high level process implementation (such as
	 * HLPetriNet or HLYawl). <br>
	 * Visualization options depend on the concrete model type and its high
	 * level implementation.
	 * 
	 * @param proc
	 *            the high level process to be displayed
	 */
	public HLProcessGui(HLModel proc) {
		this(proc.getHLProcess());
		hlProcessImpl = proc;
		hlVisualization = new HLVisualization(hlProcessImpl.getProcessModel());
	}

	/**
	 * Constructor based on the plain high level information (not related to a
	 * concrete process model).
	 * 
	 * @param proc
	 *            the high level information to be displayed
	 */
	public HLProcessGui(HLProcess proc) {
		hlProcess = proc;
	}

	/**
	 * Returns a list of gui-displayable Activities, i.e., each activity in this
	 * list can be displayed in a list of a gui property. Input and output data
	 * attributes are displayed separately. Without specification of input and
	 * output data, all data attributes are displayed as output data attributes.
	 * 
	 * @return the list of activity guis
	 */
	public List getActivityGuiDisplayables() {
		List<HLActivityGui> result = new ArrayList<HLActivityGui>();
		List<HLActivity> activities;
		if (hlProcessImpl != null) {
			// respect filter if provided
			activities = hlProcessImpl.getSelectedActivities();
		} else {
			activities = hlProcess.getActivities();
		}
		for (HLActivity act : activities) {
			HLActivityGui actGui = new HLActivityGui(act);
			result.add(actGui);
		}
		return result;
	}

	/**
	 * Returns a list of gui-displayable Choices, i.e., each choice in this list
	 * can be displayed in a list of a gui property.
	 * 
	 * @return the list of choice guis
	 */
	public List getChoiceGuiDisplayables() {
		List<HLChoiceGui> result = new ArrayList<HLChoiceGui>();
		for (HLChoice choice : hlProcess.getChoices()) {
			HLChoiceGui chGui = new HLChoiceGui(choice, hlProcess);
			result.add(chGui);
		}
		return result;
	}

	/**
	 * Creates a GUI panel containing a list of all those attributes that are
	 * contained in this simulation model. <br>
	 * Note that the panel can be readily displayed and modifications to the
	 * displayed attributes are propagated to the internally maintained values.
	 * 
	 * @return the GUI panel containing the data attributes available
	 */
	public JPanel getDataAttributesPanel() {
		JPanel resultPanel = new JPanel();
		resultPanel.setOpaque(false);
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
		resultPanel.add(Box.createVerticalGlue());
		List<GuiDisplayable> dataAttrHL = new ArrayList<GuiDisplayable>();
		Iterator<HLAttribute> it = hlProcess.getAttributes().iterator();
		while (it.hasNext()) {
			HLAttribute attrib = it.next();
			HLAttributeGui attGui = new HLAttributeGui(attrib);
			dataAttrHL.add(attGui);
		}
		attTable = new GenericTableModelPanel(
				dataAttrHL,
				"Attributes",
				"Here you can adjust existing data attributes to your needs. You can create new attributes by pressing the button 'Add attribute' at the lower left, "
						+ "and select attributes for deletion at the lower right of this window.  "
						+ "Furthermore, you can assign existing data attributes to activities in the 'Activities' tab, and "
						+ "create decision rules based on these attributes in the 'Choices' tab. ",
				this, bgColor);
		resultPanel.add(attTable);
		resultPanel.add(Box.createVerticalGlue());
		return resultPanel;
	}

	/**
	 * Creates a gui object for the global characteristics of this high level
	 * process.
	 * 
	 * @return the global gui displayable
	 */
	public HLGlobalGui getGlobalGui() {
		return new HLGlobalGui(hlProcess.getGlobalInfo());
	}

	/**
	 * Creates a gui object for the organizational characteristics of this high
	 * level process.
	 * 
	 * @return the gui displayable containing the organizational characteristics
	 */
	public HLOrganizationalGui getOrganizationalGui() {
		return new HLOrganizationalGui(hlProcess);
	}

	// ///////////////////////////////// Integrated view on all the high level
	// data ///////////////////

	/**
	 * Creates a panel representing the overall high level process. <br>
	 * The different types of process characteristics will be nested in tabs.
	 * 
	 * @return the GUI panel with the gui representation of the high level
	 *         process
	 */
	public JPanel getPanel() {

		JPanel result = new JPanel(new BorderLayout());
		addAttributeButton
				.setToolTipText("Press button to add a new data attribute");
		removeAttributeButton
				.setToolTipText("Select the attribute to be removed from the combo box at the left and press button");

		// build data attribute view
		processPanel
				.setLayout(new BoxLayout(processPanel, BoxLayout.PAGE_AXIS));
		processPanel.setBackground(bgColor);
		attributesPanel.setLayout(new BoxLayout(attributesPanel,
				BoxLayout.PAGE_AXIS));
		attributesPanel.setBackground(bgColor);
		attributesPanel.add(getDataAttributesPanel());
		SlickerSwingUtils.injectTransparency(attributesPanel);
		SlickerSwingUtils.injectTransparency(processPanel);
		processPanel.add(attributesPanel);
		JPanel newAttPanel = new JPanel();
		newAttPanel.setOpaque(false);
		newAttPanel.setLayout(new BoxLayout(newAttPanel, BoxLayout.LINE_AXIS));
		newAttPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		newAttPanel.add(addAttributeButton);
		newAttPanel.add(Box.createHorizontalGlue());
		newAttPanel.add(removeAttributeButton);
		processPanel.add(newAttPanel);

		// build tab views
		tabPane.addTab("Global", getGlobalGui().getPanel());
		tabPane.addTab("Attributes", processPanel);
		tabPane
				.addTab(
						"Activities",
						new GenericTableModelPanel(
								getActivityGuiDisplayables(),
								"Activities",
								"Here you can configure the settings for the activities in the process. For each activity, you can specify timing information, "
										+ "provided data attributes, and require a resource from a specific group for the execution of this activity.",
								this, bgColor));
		tabPane
				.addTab(
						"Choices",
						new GenericTableModelPanel(
								getChoiceGuiDisplayables(),
								"Choices",
								"Here you can specify on which basis the decision for an alternative branch at each of these choice points is determined in the process: "
										+ "decision rules (based on data attributes), probabilities, frequencies, or random selection.",
								this, bgColor));
		tabPane.addTab("Resources", getOrganizationalGui());

		if (hlVisualization != null) {
			// initialize visualization
			visualizationPanel.add(hlVisualization.getPanel());
			// set up split pane between visualization and actual settings tab
			// pane
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					tabPane, visualizationPanel);
			splitPane.setDividerLocation(300);
			splitPane.setOneTouchExpandable(true);
			splitPane.setDividerSize(3);
			result.add(splitPane, BorderLayout.CENTER);
		} else {
			result.add(tabPane, BorderLayout.CENTER);
		}
		registerGuiActionListener();
		return result;
	}

	/**
	 * Connect GUI elements with functionality to create interaction.
	 */
	protected void registerGuiActionListener() {
		// specify button actions
		addAttributeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// add new data attribute to simulation model
				HLAttribute newAtt = new HLNominalAttribute("New Attribute",
						hlProcess);
				// redraw GUI
				updateAttributesPanel();
			}
		});
		removeAttributeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (GuiDisplayable attGui : attTable.getSelected()) {
					HLAttribute att = ((HLAttributeGui) attGui)
							.getHLAttribute();
					hlProcess.removeAttribute(att.getID());
				}
				// redraw GUI
				updateAttributesPanel();
			}
		});
	}

	protected void updateAttributesPanel() {
		// redraw attributes section
		attributesPanel.removeAll();
		attributesPanel.add(getDataAttributesPanel());
		attributesPanel.validate();
		attributesPanel.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.util.GuiNotificationTargetStateful#
	 * stateHasChanged(java.lang.Object)
	 */
	public void stateHasChanged(Object involvedObj) {
		// only updated visualization if model graph is available
		if (hlProcessImpl != null && hlVisualization != null) {
			ArrayList<ModelGraphVertex> vertices = new ArrayList<ModelGraphVertex>();
			// in case of new activity has been double-clicked in 'Activities'
			// view
			if (involvedObj instanceof HLActivityGui) {
				ModelGraphVertex trans = hlProcessImpl
						.findModelGraphVertexForActivity(((HLActivityGui) involvedObj)
								.getHLActivity().getID());
				vertices.add(trans);
				hlVisualization.highLightNodesInVisualization(vertices);
			}
			// in case a new choice being double-clicked
			else if (involvedObj instanceof HLChoiceGui) {
				ModelGraphVertex place = hlProcessImpl
						.findModelGraphVertexForChoice(((HLChoiceGui) involvedObj)
								.getHLChoice().getID());
				vertices.add(place);
				// highlight target activities too
				HLChoice ch = ((HLChoiceGui) involvedObj).getHLChoice();
				Iterator<HLID> actIt = ch.getChoiceTargetIDs().iterator();
				while (actIt.hasNext()) {
					HLID actID = actIt.next();
					ModelGraphVertex trans = hlProcessImpl
							.findModelGraphVertexForActivity(actID);
					vertices.add(trans);
				}
				hlVisualization.highLightNodesInVisualization(vertices);
			}
			// in case an attribute was double-clicked - the activities should
			// be highlighted
			else if (involvedObj instanceof HLAttributeGui) {
				HLAttribute att = ((HLAttributeGui) involvedObj)
						.getHLAttribute();
				Iterator<HLActivity> actIt = hlProcess
						.getActivitiesForAttribute(att.getID()).iterator();
				while (actIt.hasNext()) {
					HLActivity act = actIt.next();
					ModelGraphVertex trans = hlProcessImpl
							.findModelGraphVertexForActivity(act.getID());
					vertices.add(trans);
				}
				hlVisualization.highLightNodesInVisualization(vertices);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return hlProcess.getGlobalInfo().getName();
	}

}
