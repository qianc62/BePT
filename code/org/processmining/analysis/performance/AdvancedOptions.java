/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

/*
 * Copyright (c) 2006 Eindhoven University of Technology
 * All rights reserved.
 */

package org.processmining.analysis.performance;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GuiNotificationTarget;

/**
 * This class is needed to allow the user to set advanced settings, which are
 * settings that influence how measurements are affected by non-conformance
 * 
 * @see PerformanceAnalysisGUI
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class AdvancedOptions extends JPanel implements GuiNotificationTarget {
	private int[] advancedSettings;
	private PerformanceConfiguration config;
	// panel containing the settings
	private JPanel advancedPanel = new JPanel();
	// comboboxes that contain values of the settings
	private JComboBox processBox = new JComboBox();
	private JComboBox placeBox = new JComboBox();
	private JComboBox transitionsBox = new JComboBox();
	private JComboBox activityBox = new JComboBox();
	// the save time options
	private GUIPropertyBoolean isRestricted = new GUIPropertyBoolean(
			"Restrict search depth for invisible tasks",
			"Restricts search for sequences of invisible tasks that might enable another task during log replay",
			true, this);
	private GUIPropertyInteger restrictedDepth = new GUIPropertyInteger(
			"Maximum depth: ",
			"Corresponds to the maximum length of a sequence of invisible tasks that can be found",
			1, 0, 100);
	// various labels
	private JLabel advancedLabel = new JLabel("Advanced Settings");
	private JLabel conformanceLabel = new JLabel(
			"<html>Values of various performance"
					+ " metrics can be influenced by"
					+ " non-conformance.<br>These are the"
					+ " settings that are currently in use:</html>");
	private JLabel processLabel = new JLabel("Process metrics are based on:");
	private JLabel placeLabel = new JLabel("Place metrics are based on:");
	private JLabel transitionsLabel = new JLabel(
			"Times in between two transitions" + " are based on:");
	private JLabel activityLabel = new JLabel("Activity metrics based on:");
	private JLabel saveTimeLabel0 = new JLabel("Saving time:");
	private JLabel saveTimeLabel1 = new JLabel(
			"Log replay can be a very time-consuming method, especially when the used Petri net contains");
	private JLabel saveTimeLabel2 = new JLabel(
			"invisible transitions. Therefore, you can restrict this search to sequences of a certain length.");
	private JLabel saveTimeLabel3 = new JLabel(
			"Note, however, that some performance metrics (e.g. place metrics) might be influenced by this");
	private JLabel saveTimeLabel4 = new JLabel(
			"option. Please refer to the help file for further details.");
	// buttons
	private JButton applyButton = new JButton("Apply changes");
	private JButton cancelButton = new JButton("Cancel");

	/**
	 * Constructs the panel that contains advanced options
	 * 
	 * @param settings
	 *            int[]: the initial settings
	 * @param pc
	 *            PerformanceConfiguration: the Performance Configuration object
	 *            from which this constructor was called.
	 */
	public AdvancedOptions(int[] settings, PerformanceConfiguration pc) {
		advancedSettings = settings;
		config = pc;
		try {
			jbInit();
			registerGuiActionListener();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Actually builds the panel that contains the advanced options
	 */
	private void jbInit() {
		// initialize the panel that contains the advanced settings
		advancedPanel.setLayout(new GridBagLayout());
		GridBagConstraints con = new GridBagConstraints();
		con.gridy = 0;
		con.gridx = 0;
		con.insets = new Insets(0, 0, 5, 0);
		con.anchor = GridBagConstraints.NORTHWEST;
		advancedPanel.add(advancedLabel, con);
		con.gridy = 1;
		con.gridx = 0;
		con.gridwidth = 3;
		con.insets = new Insets(5, 0, 5, 0);
		advancedPanel.add(conformanceLabel, con);
		con.gridy = 2;
		con.gridx = 0;
		con.gridwidth = 1;
		con.insets = new Insets(5, 0, 0, 0);
		advancedPanel.add(processLabel, con);
		con.gridy = 3;
		con.gridx = 0;
		con.gridwidth = 3;
		con.insets = new Insets(0, 0, 5, 0);
		processBox.setMaximumSize(new Dimension(550, 22));
		processBox.setPreferredSize(new Dimension(550, 22));
		processBox.addItem("All measurements taken");
		processBox.addItem("All measurements taken in fitting traces");
		processBox.setSelectedIndex(advancedSettings[0]);
		advancedPanel.add(processBox, con);
		con.gridy = 4;
		con.gridx = 0;
		con.insets = new Insets(5, 0, 0, 0);
		advancedPanel.add(placeLabel, con);
		con.gridy = 5;
		con.gridx = 0;
		con.insets = new Insets(0, 0, 5, 0);
		placeBox.setMaximumSize(new Dimension(550, 22));
		placeBox.setPreferredSize(new Dimension(550, 22));
		placeBox.addItem("All measurements taken");
		placeBox
				.addItem("All measurements taken in each trace before the trace fails");
		placeBox
				.addItem("All measurements taken in each trace where no related transition has failed execution");
		placeBox.addItem("All measurements taken in fitting traces");
		placeBox.setSelectedIndex(advancedSettings[1]);
		advancedPanel.add(placeBox, con);
		con.gridy = 6;
		con.gridx = 0;
		con.insets = new Insets(5, 0, 0, 0);
		advancedPanel.add(transitionsLabel, con);
		con.gridy = 7;
		con.gridx = 0;
		con.insets = new Insets(0, 0, 5, 0);
		transitionsBox.setMaximumSize(new Dimension(550, 22));
		transitionsBox.setPreferredSize(new Dimension(550, 22));
		transitionsBox.addItem("All measurements taken");
		transitionsBox
				.addItem("All measurements taken in each trace before the trace fails");
		transitionsBox
				.addItem("All measurements taken in traces where both transitions do not fail");
		transitionsBox.addItem("All measurements taken in fitting traces");
		transitionsBox.setSelectedIndex(advancedSettings[2]);
		advancedPanel.add(transitionsBox, con);
		con.gridy = 8;
		con.gridx = 0;
		con.insets = new Insets(5, 0, 0, 0);
		advancedPanel.add(activityLabel, con);
		con.gridy = 9;
		con.gridx = 0;
		con.insets = new Insets(0, 0, 5, 0);
		activityBox.setMaximumSize(new Dimension(550, 22));
		activityBox.setPreferredSize(new Dimension(550, 22));
		activityBox.addItem("All measurements taken");
		activityBox
				.addItem("All measurements taken in each trace before the trace fails");
		activityBox
				.addItem("All measurements taken in traces where no transition corresponding to the activity fails");
		activityBox
				.addItem("All measurements taken where no transition corresponding to a metric of the activity fails");
		activityBox.addItem("All measurements taken in fitting traces");
		activityBox.setSelectedIndex(advancedSettings[3]);
		advancedPanel.add(activityBox, con);
		advancedPanel
				.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		con.gridy = 10;
		con.gridx = 0;
		con.gridwidth = 3;
		advancedPanel.add(saveTimeLabel0, con);
		con.gridy = 11;
		con.gridx = 0;
		advancedPanel.add(saveTimeLabel1, con);
		con.gridy = 12;
		con.gridx = 0;
		advancedPanel.add(saveTimeLabel2, con);
		con.gridy = 13;
		con.gridx = 0;
		advancedPanel.add(saveTimeLabel3, con);
		con.gridy = 14;
		con.gridx = 0;
		advancedPanel.add(saveTimeLabel4, con);

		/*
		 * NOTE: modified by Anne in order to support restricted search depth
		 * for invisible tasks also from the Performance Analysis plug-in. Now
		 * the binary choice of time saving option or not translates into an
		 * integer as follows: x < 0 --> the whole state space will be built
		 * (i.e., no time saving option) x = 0 --> no state space will be built
		 * at all (i.e., time saving option as it used to be) x > 0 --> improved
		 * time saving option building a partial state space in order to still
		 * detect sequences of invisible tasks of the length x (and therefore
		 * increase fitness of log and model).
		 */
		if (advancedSettings[4] >= 0) {
			restrictedDepth = new GUIPropertyInteger(
					"Maximum depth: ",
					"Corresponds to the maximum length of a sequence of invisible tasks that can be found",
					advancedSettings[4], 0, 100);
		} else {
			isRestricted = new GUIPropertyBoolean(
					"Restrict search depth for invisible tasks",
					"Restricts search for sequences of invisible tasks that might enable another task during log replay",
					false, this);
			restrictedDepth = new GUIPropertyInteger(
					"Maximum depth: ",
					"Corresponds to the maximum length of a sequence of invisible tasks that can be found",
					1, 0, 100);
			// TODO Bugfix Anne: does not work because property object not built
			// yet (result of memory problems
			// in CPN export - look into further possibilities to solve this
			// issue)
			// restrictedDepth.disable();
		}

		con.gridy = 15;
		con.gridx = 0;
		con.gridwidth = 1;
		advancedPanel.add(isRestricted.getPropertyPanel(), con);
		con.gridy = 15;
		con.gridx = 2;
		con.gridwidth = 1;
		con.anchor = GridBagConstraints.NORTHEAST;
		advancedPanel.add(restrictedDepth.getPropertyPanel(), con);

		// now that the property panels are created, we can disable the
		// enumeration property if necessary
		// (see bugfix note above)
		if (advancedSettings[4] < 0) {
			restrictedDepth.disable();
		}

		con.anchor = GridBagConstraints.SOUTHWEST;
		con.gridy = 16;
		con.gridx = 0;
		con.insets = new Insets(0, 0, 0, 0);
		con.anchor = GridBagConstraints.NORTHWEST;
		advancedPanel.add(cancelButton, con);
		con.gridy = 16;
		con.gridx = 2;
		con.gridwidth = 1;
		con.anchor = GridBagConstraints.NORTHEAST;
		advancedPanel.add(applyButton, con);
		// set fonts
		advancedLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
		processBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
		placeBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
		transitionsBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
		activityBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
		conformanceLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		saveTimeLabel1.setFont(new Font("SansSerif", Font.PLAIN, 13));
		saveTimeLabel2.setFont(new Font("SansSerif", Font.PLAIN, 13));
		saveTimeLabel3.setFont(new Font("SansSerif", Font.PLAIN, 13));
		saveTimeLabel4.setFont(new Font("SansSerif", Font.PLAIN, 13));
		// fill panel
		this.setLayout(new BorderLayout());
		this.add(advancedPanel, BorderLayout.CENTER);
		this.validate();
		this.repaint();
	}

	/**
	 * Ties GUI-objects with listener methods
	 */
	private void registerGuiActionListener() {
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// pass settings to bottleneck configuration
				advancedSettings[0] = processBox.getSelectedIndex();
				advancedSettings[1] = placeBox.getSelectedIndex();
				advancedSettings[2] = transitionsBox.getSelectedIndex();
				advancedSettings[3] = activityBox.getSelectedIndex();

				/*
				 * NOTE modified by Anne in order to support restricted search
				 * depth (see above)
				 */
				if (isRestricted.getValue() == false) {
					advancedSettings[4] = -1;
				} else {
					advancedSettings[4] = restrictedDepth.getValue();
				}
				config.setAdvancedSettings(advancedSettings.clone());
				config.closeAdvancedFrames();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// pass input settings back to bottleneck configuration
				processBox.setSelectedIndex(advancedSettings[0]);
				placeBox.setSelectedIndex(advancedSettings[1]);
				transitionsBox.setSelectedIndex(advancedSettings[2]);
				activityBox.setSelectedIndex(advancedSettings[3]);

				/*
				 * NOTE modified by Anne in order to support restricted search
				 * depth (see above)
				 */
				if (advancedSettings[4] >= 0) {
					isRestricted.setSelected(true);
					restrictedDepth = new GUIPropertyInteger(
							"Maximum depth: ",
							"Corresponds to the maximum length of a sequence of invisible tasks that can be found",
							advancedSettings[4], 0, 100);
				} else {
					isRestricted.setSelected(false);
					restrictedDepth.disable();
				}
				// close the frames
				config.closeAdvancedFrames();
			}
		});
	}

	/**
	 * Will be called as soon the user restricts or unrestricts the search depth
	 * for invisible tasks during log replay. <br>
	 * Per default the restriction is selected and a depth value can be
	 * provided. However, as soon as the user deselects the depth limitation the
	 * corresponding spinner will be disabled (and it will be enabled as soon as
	 * the restriction is selected again).
	 */
	public void updateGUI() {
		if (restrictedDepth.isEnabled() == true) {
			restrictedDepth.disable();
		} else {
			restrictedDepth.enable();
		}
	}
}
