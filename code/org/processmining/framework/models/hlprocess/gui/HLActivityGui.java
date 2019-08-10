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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.slickerbox.util.SlickerSwingUtils;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.HLTypes.TransformationType;
import org.processmining.framework.models.hlprocess.gui.att.dist.HLDistributionGui;
import org.processmining.framework.models.hlprocess.gui.att.dist.HLDistributionGuiManager;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiDisplayable;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyObjectSet;

/**
 * Gui class for high level activity with input and output data attributes. <br>
 * Allows to view and edit the activity characteristics through a graphical user
 * interface.
 * 
 * @see HLProcessGui
 */
public class HLActivityGui implements GuiDisplayable, GuiNotificationTarget {

	// The actual activity object reflected by this Gui object
	protected HLActivity hlActivity;
	// GUI attributes
	protected JPanel resultPanel;
	protected GuiPropertyObjectSet inputAttributesProperty;
	protected GuiPropertyObjectSet outputAttributesProperty;
	protected GUIPropertyListEnumeration groupProperty;

	/**
	 * Creates a graphical version of the given HLActivity.
	 * 
	 * @param act
	 *            the high level activity to be made Gui-displayable
	 */
	public HLActivityGui(HLActivity act) {
		hlActivity = act;
	}

	/**
	 * Retrieves the underlying high level activity object.
	 * 
	 * @return the high level activity for this gui object
	 */
	public HLActivity getHLActivity() {
		return hlActivity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
	 */
	public JPanel getPanel() {
		if (resultPanel == null) {
			resultPanel = new JPanel();
			resultPanel.setOpaque(false);
			resultPanel.setLayout(new BoxLayout(resultPanel,
					BoxLayout.LINE_AXIS));
		}
		resultPanel.removeAll();
		createPanel();
		resultPanel.validate();
		resultPanel.repaint();
		return resultPanel;
	}

	private void createPanel() {
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));

		JPanel heading = new JPanel();
		heading.setLayout(new BoxLayout(heading, BoxLayout.LINE_AXIS));
		heading.add(new JLabel("Options available for activity:   "
				+ toString()));
		heading.add(Box.createHorizontalGlue());
		content.add(heading);
		content.add(Box.createRigidArea(new Dimension(0, 30)));

		// input data attributes
		//
		// Fill myAttributesResampleOrReuse for indicating whether a data
		// attribute has to
		// be resampled or reused.
		// When clicking on the add button the selected value in the combobox of
		// myAttributesResampleOrReuse
		// determines whether the added data attribute has to be resampled or
		// reused
		ArrayList<HLTypes.TransformationType> modType = new ArrayList<HLTypes.TransformationType>();
		modType.add(HLTypes.TransformationType.Resample);
		modType.add(HLTypes.TransformationType.Reuse);
		// get the init values for the property of the data attributes
		HashMap<HLAttribute, TransformationType> inputInitValPropDatAttr = new HashMap<HLAttribute, TransformationType>();
		Iterator inputAttrs = hlActivity.getInputDataAttributes().iterator();
		while (inputAttrs.hasNext()) {
			HLAttribute input = (HLAttribute) inputAttrs.next();
			inputInitValPropDatAttr.put(input, hlActivity
					.getTransformationType(input.getID()));
		}

		Iterator<HLAttribute> availableInputAttributes = hlActivity
				.getHLProcess().getAttributes().iterator();
		Set<HLAttribute> inputSet = new HashSet<HLAttribute>();
		while (availableInputAttributes.hasNext()) {
			HLAttribute inAtt = availableInputAttributes.next();
			inputSet.add(inAtt);
		}

		inputAttributesProperty = new GuiPropertyObjectSet(
				"Input data attributes",
				"Specifies the input data attributes that are provided by this activity",
				hlActivity.getInputDataAttributes(), inputSet, modType,
				inputInitValPropDatAttr, this);
		content.add(inputAttributesProperty.getPropertyPanel());
		content.add(Box.createRigidArea(new Dimension(0, 30)));

		// output data attributes
		//
		// get the init values for the property of the data attributes
		HashMap<HLAttribute, TransformationType> outputInitValPropDatAttr = new HashMap<HLAttribute, TransformationType>(); // java.util.HashMap
		// does
		// not
		// take
		// parameters.
		Iterator outputAttrs = hlActivity.getOutputDataAttributes().iterator();
		while (outputAttrs.hasNext()) {
			HLAttribute output = (HLAttribute) outputAttrs.next();
			outputInitValPropDatAttr.put(output, hlActivity
					.getTransformationType(output.getID()));
		}

		Iterator<HLAttribute> availableOutputAttributes = hlActivity
				.getHLProcess().getAttributes().iterator();
		Set<HLAttribute> outputSet = new HashSet<HLAttribute>();
		while (availableOutputAttributes.hasNext()) {
			HLAttribute outAtt = availableOutputAttributes.next();
			outputSet.add(outAtt);
		}

		outputAttributesProperty = new GuiPropertyObjectSet(
				"Output data attributes",
				"Specifies the output data attributes that are provided by this activity",
				hlActivity.getOutputDataAttributes(), outputSet, modType,
				outputInitValPropDatAttr, this);
		content.add(outputAttributesProperty.getPropertyPanel());
		content.add(Box.createRigidArea(new Dimension(0, 30)));

		// take current group into account and put as the first element (i.e.
		// initial value)
		// in the list before creating the GUI property
		// hide the resource (group for all resource) group
		ArrayList<HLGroup> groups = new ArrayList<HLGroup>();
		HLGroup currentGroup = hlActivity.getGroup();
		if (currentGroup != null) {
			groups.add(currentGroup);
		}

		Iterator<HLGroup> groupsIt = hlActivity.getHLProcess().getAllGroups()
				.iterator();
		while (groupsIt.hasNext()) {
			HLGroup groupIt = groupsIt.next();
			if (groupIt != currentGroup) {
				groups.add(groupIt);
			}
		}
		groupProperty = new GUIPropertyListEnumeration(
				"Group",
				"Indicates which group of available resources may perform this activity",
				groups, this, 200);
		content.add(groupProperty.getPropertyPanel());
		content.add(Box.createRigidArea(new Dimension(0, 30)));
		HLDistributionGui distGui;
		// execution time
		if (hlActivity.getExecutionTime() != null) {
			JPanel etPanel = new JPanel();
			etPanel.setLayout(new BoxLayout(etPanel, BoxLayout.LINE_AXIS));
			JLabel etLabel = new JLabel("Execution time:");
			etPanel.add(etLabel);
			etPanel.add(Box.createHorizontalGlue());
			content.add(etPanel);
			content.add(Box.createRigidArea(new Dimension(0, 10)));
			distGui = HLDistributionGuiManager.getDistributionGui(hlActivity
					.getExecutionTime(), this);
			content.add(distGui.getPanel());
			content.add(Box.createRigidArea(new Dimension(0, 30)));
		}
		// waiting time
		if (hlActivity.getWaitingTime() != null) {
			JPanel wtPanel = new JPanel();
			wtPanel.setLayout(new BoxLayout(wtPanel, BoxLayout.LINE_AXIS));
			JLabel wtLabel = new JLabel("Waiting time:");
			wtPanel.add(wtLabel);
			wtPanel.add(Box.createHorizontalGlue());
			content.add(wtPanel);
			content.add(Box.createRigidArea(new Dimension(0, 10)));
			distGui = HLDistributionGuiManager.getDistributionGui(hlActivity
					.getWaitingTime(), this);
			content.add(distGui.getPanel());
			content.add(Box.createRigidArea(new Dimension(0, 30)));
		}
		// waiting time + execution time
		if (hlActivity.getSojournTime() != null) {
			JPanel stPanel = new JPanel();
			stPanel.setLayout(new BoxLayout(stPanel, BoxLayout.LINE_AXIS));
			JLabel stLabel = new JLabel("Sojourn time:");
			stPanel.add(stLabel);
			stPanel.add(Box.createHorizontalGlue());
			content.add(stPanel);
			content.add(Box.createRigidArea(new Dimension(0, 10)));
			distGui = HLDistributionGuiManager.getDistributionGui(hlActivity
					.getSojournTime(), this);
			content.add(distGui.getPanel());
			content.add(Box.createRigidArea(new Dimension(0, 30)));
		}
		resultPanel.add(Box.createHorizontalGlue());
		resultPanel.add(content);
		resultPanel.add(Box.createHorizontalGlue());
		SlickerSwingUtils.injectTransparency(resultPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiNotificationTarget#updateGUI()
	 */
	public void updateGUI() {
		// updates the list of input data attributes for this activity
		// (as might have been both added or removed via GUI panel)
		// Together with each input data attribute it is saved whether it needs
		// to be
		// resampled or reused
		Iterator<HLAttribute> inAttIt = hlActivity.getInputDataAttributes()
				.iterator();
		while (inAttIt.hasNext()) {
			HLAttribute inAtt = inAttIt.next();
			hlActivity.removeInputDataAttribute(inAtt.getID());
		}
		HashMap<HLAttribute, TransformationType> newInputData = inputAttributesProperty
				.getAllValuesWithProperty();
		Iterator<Entry<HLAttribute, TransformationType>> allInputs = newInputData
				.entrySet().iterator();
		while (allInputs.hasNext()) {
			Entry<HLAttribute, TransformationType> current = allInputs.next();
			HLAttribute att = current.getKey();
			TransformationType type = current.getValue();
			hlActivity.addInputDataAttribute(att.getID());
			hlActivity.setTransformationType(att.getID(), type);
		}

		// updates the list of output data attributes for this activity
		// (as might have been both added or removed via GUI panel)
		// Together with each output data attribute it is saved whether it needs
		// to be
		// resampled or reused
		Iterator<HLAttribute> outAttIt = hlActivity.getOutputDataAttributes()
				.iterator();
		while (outAttIt.hasNext()) {
			HLAttribute outAtt = outAttIt.next();
			hlActivity.removeOutputDataAttribute(outAtt.getID());
		}

		HashMap<HLAttribute, TransformationType> newOutputData = outputAttributesProperty
				.getAllValuesWithProperty();
		Iterator<Entry<HLAttribute, TransformationType>> allOutputs = newOutputData
				.entrySet().iterator();
		while (allOutputs.hasNext()) {
			Entry<HLAttribute, TransformationType> current = allOutputs.next();
			HLAttribute att = current.getKey();
			TransformationType type = current.getValue();
			hlActivity.addOutputDataAttribute(att.getID());
			hlActivity.setTransformationType(att.getID(), type);
		}

		// For the selected data attribute, check whether it needs to be
		// redistributed
		// or resampled
		// updates the group of this data attribute
		// (as might have been changed)
		HLGroup group = (HLGroup) groupProperty.getValue();
		if (group != null) {
			hlActivity.setGroup(group.getID());
		}

		// also update the gui as, for example, distribution type has changed
		// and resulted
		// in differently sized sub panels
		resultPanel.removeAll();
		createPanel();
		resultPanel.validate();
		resultPanel.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return hlActivity.getName();
	}

}
