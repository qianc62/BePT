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
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.slickerbox.util.SlickerSwingUtils;
import org.processmining.framework.models.hlprocess.HLGlobal;
import org.processmining.framework.models.hlprocess.HLTypes.TimeUnit;
import org.processmining.framework.models.hlprocess.gui.att.dist.HLDistributionGui;
import org.processmining.framework.models.hlprocess.gui.att.dist.HLDistributionGuiManager;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GUIPropertyString;
import org.processmining.framework.util.GuiDisplayable;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyStringTextarea;
import org.processmining.framework.util.GuiUtilities;

/**
 * Creates a gui representation for the global properties of the high level
 * process. <br>
 * Allows to view and edit the global characteristics through a graphical user
 * interface.
 * 
 * @see HLProcessGui
 */
public class HLGlobalGui implements GuiDisplayable, GuiNotificationTarget {

	/** the global process information represented by this gui object */
	protected HLGlobal global;

	// Gui attributes
	protected JPanel outmostPanel;
	protected JPanel caseGenerationPanel;
	protected GUIPropertyListEnumeration listEnumForTimeUnit;
	protected GUIPropertyString name;

	/**
	 * Creates a new gui object based on the underlying high level information.
	 * 
	 * @param glob
	 *            the global process to be represented by this gui object
	 */
	public HLGlobalGui(HLGlobal glob) {
		global = glob;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
	 */
	public JPanel getPanel() {
		if (outmostPanel == null) {
			outmostPanel = new JPanel(new BorderLayout());
		}
		createPanel();
		return outmostPanel;
	}

	private void createPanel() {
		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.LINE_AXIS));
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		String description = new String(
				"Here you can specify global properties of the process. "
						+ "For example, the case generation scheme determines in which distribution new cases "
						+ "(that is, new process instances) arrive in the process.");
		GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
				description);
		outmostPanel.add(BorderLayout.NORTH, helpText.getPropertyPanel());
		name = new GUIPropertyString("Process Name",
				"Specifies the name of this process", global.getName(), this,
				200);
		content.add(Box.createVerticalGlue());
		content.add(name.getPropertyPanel());
		createCaseGenerationPanel();
		content.add(caseGenerationPanel);
		// add the time unit to the panel
		listEnumForTimeUnit = new GUIPropertyListEnumeration("Time unit:",
				"Time unit used in the simulation model", TimeUnit
						.getAllTypes(), new TimeUnitChangeListener());
		// set the current time unit as selected value for the combobox
		listEnumForTimeUnit.setValue(global.getTimeUnit());
		content.add(listEnumForTimeUnit.getPropertyPanel());
		content.add(Box.createVerticalGlue());
		resultPanel.add(Box.createHorizontalGlue());
		resultPanel.add(content);
		resultPanel.add(Box.createHorizontalGlue());
		outmostPanel.add(GuiUtilities.getSimpleScrollable(resultPanel,
				HLProcessGui.bgColor), BorderLayout.CENTER);
		outmostPanel.setBackground(HLProcessGui.bgColor);
		SlickerSwingUtils.injectTransparency(outmostPanel);
	}

	private void createCaseGenerationPanel() {
		if (caseGenerationPanel == null) {
			caseGenerationPanel = new JPanel();
			caseGenerationPanel.setLayout(new BoxLayout(caseGenerationPanel,
					BoxLayout.PAGE_AXIS));
			caseGenerationPanel.setOpaque(false);
		}
		caseGenerationPanel.removeAll();
		JPanel stPanel = new JPanel();
		stPanel.setLayout(new BoxLayout(stPanel, BoxLayout.LINE_AXIS));
		JLabel stLabel = new JLabel("Case generation scheme:");
		stPanel.add(stLabel);
		stPanel.add(Box.createHorizontalGlue());
		caseGenerationPanel.add(Box.createVerticalGlue());
		caseGenerationPanel.add(stPanel);
		caseGenerationPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		HLDistributionGui distGui = HLDistributionGuiManager
				.getDistributionGui(global.getCaseGenerationScheme(), this);
		caseGenerationPanel.add(distGui.getPanel());
		caseGenerationPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		caseGenerationPanel.validate();
		caseGenerationPanel.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiNotificationTarget#updateGUI()
	 */
	public void updateGUI() {
		// update attributes that might have changed
		global.setTimeUnit((TimeUnit) listEnumForTimeUnit.getValue());
		global.setName(name.getValue());
		// also update the gui as, for example, the distribution type might have
		// changed
		createCaseGenerationPanel();
		outmostPanel.validate();
		outmostPanel.repaint();
		SlickerSwingUtils.injectTransparency(outmostPanel);
	}

	/**
	 * Invokes the conversion of all time-related values and issues a redraw of
	 * the global GUI.
	 */
	protected void updateTimeUnit() {
		global.changeTimeUnit((TimeUnit) listEnumForTimeUnit.getValue());
		updateGUI();
	}

	/**
	 * Listener for time unit change as this leads to a re-calculation of
	 * time-related properties (values need to be converted to match new time
	 * unit).
	 */
	class TimeUnitChangeListener implements GuiNotificationTarget {

		public void updateGUI() {
			if (global.getTimeUnit() != listEnumForTimeUnit.getValue()) {
				updateTimeUnit();
			}
		}
	}
}
