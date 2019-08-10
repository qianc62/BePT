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
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.deckfour.slickerbox.util.SlickerSwingUtils;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLCondition;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLProcess;
import org.processmining.framework.models.hlprocess.HLTypes.ChoiceEnum;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiDisplayable;
import org.processmining.framework.util.GuiNotificationTarget;

/**
 * Gui class for high-level choice objects. <br>
 * Allows to view and edit the choice characteristics through a graphical user
 * interface.
 * 
 * @see HLProcessGui
 */
public class HLChoiceGui implements GuiDisplayable, GuiNotificationTarget {

	// The actual choice object reflected by this Gui object
	protected HLChoice hlChoice;
	// maps activity ids onto condition guis
	protected HashMap<HLID, HLConditionGui> conditions;
	// GUI attributes
	protected JPanel myPanel;
	protected GUIPropertyListEnumeration configuration;

	/**
	 * Creates a new Gui object that allows to view and manipulate the choice
	 * object.
	 * 
	 * @param choice
	 *            the choice object reflected by this gui object
	 */
	public HLChoiceGui(HLChoice choice, HLProcess proc) {
		hlChoice = choice;
		// configuration
		ChoiceEnum current = choice.getChoiceConfiguration();
		configuration = new GUIPropertyListEnumeration(
				"Choice based on",
				"Indicates which information is used to choose an alternative path",
				ChoiceEnum.getAllTypes(), this, 200);
		configuration.setValue(current);
		// target guis
		conditions = new HashMap<HLID, HLConditionGui>();
		for (HLID actID : hlChoice.getChoiceTargetIDs()) {
			HLCondition cond = hlChoice.getCondition(actID);
			HLActivity hlAct = proc.getActivity(actID);
			conditions.put(actID, new HLConditionGui(cond, hlAct));
		}
	}

	/**
	 * Retrieves the underlying high level choice object.
	 * 
	 * @return the high level choice object for this gui object
	 */
	public HLChoice getHLChoice() {
		return hlChoice;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
	 */
	public JPanel getPanel() {
		if (myPanel == null) {
			myPanel = new JPanel();
			myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.LINE_AXIS));
			updateGUI();
		}
		return myPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiNotificationTarget#updateGUI()
	 */
	public void updateGUI() {
		// the configuration might have been updated
		hlChoice.setChoiceConfiguration((ChoiceEnum) configuration.getValue());

		// update gui too
		myPanel.removeAll();
		JPanel content = new JPanel();
		// configuration
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		content.add(configuration.getPropertyPanel());
		content.add(Box.createRigidArea(new Dimension(0, 15)));
		// condition targets
		Iterator<HLConditionGui> condIt = conditions.values().iterator();
		while (condIt.hasNext()) {
			HLConditionGui condGui = condIt.next();
			GuiDisplayable condObj = condGui.getConditionView(hlChoice
					.getChoiceConfiguration());
			if (condObj != null) {
				content.add(condObj.getPanel());
			}
		}
		myPanel.add(Box.createHorizontalGlue());
		myPanel.add(content);
		myPanel.add(Box.createHorizontalGlue());
		SlickerSwingUtils.injectTransparency(myPanel);
		myPanel.validate();
		myPanel.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return hlChoice.getName();
	}

}
