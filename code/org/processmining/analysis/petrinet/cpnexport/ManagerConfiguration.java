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
package org.processmining.analysis.petrinet.cpnexport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyListRadio;
import org.processmining.framework.util.GuiUtilities;

/**
 * The Configuration Manager class has to keep track of which simulation
 * information exactly has to be exported to the cpn file (data, resources,
 * timing information, logging functions). Furthermore, it should ensure that
 * there are conflicts with regard to the exported simulation model (e.g. that
 * there resources included in the model and that at the same time waiting times
 * are included in the model).
 * 
 * @author arozinat
 * @author rmans
 */
public class ManagerConfiguration {

	/**
	 * the gui elements that are put on the panel of the configuration manager
	 */
	private GUIPropertyBoolean myDataPerspectiveSelected = new GUIPropertyBoolean(
			"  Data Perspective  ",
			"Includes simulation information about data attributes and choices based on data",
			false);
	private GUIPropertyBoolean myTimePerspectiveSelected = new GUIPropertyBoolean(
			"  Time Perspective  ",
			"Includes simulation information about the timing of activities and the case generation scheme",
			false, new TimePerspectiveListener());
	private GUIPropertyBoolean myResourcePerspectiveSelected = new GUIPropertyBoolean(
			"  Resource Perspective  ",
			"Includes simulation information about groups and resources",
			false, new ResourcePerspectiveListener());
	private GuiPropertyListRadio timeOptionProperty;
	private GUIPropertyInteger myWaitingRatio;
	/**
	 * The year offset from 1970 used for logging - default is 38 (i.e., 2008).
	 */
	protected GUIPropertyInteger myYearOffset = new GUIPropertyInteger(
			" Year Offset (starting from 1970):",
			"The year offset that will be used for logging", 38, 0, 1000, null,
			100, true);
	private GuiPropertyListRadio PushPullProperty;
	private GUIPropertyBoolean myActivityLoggingMonitor = new GUIPropertyBoolean(
			" MXML logging",
			"Generates monitors that create execution logs during the simulation of the CPN model",
			false, new LoggingOptionListener());
	private GUIPropertyBoolean myCurrentStateOption = new GUIPropertyBoolean(
			" Current state support",
			"Generates CPN model with inscriptions for loading a current state file",
			false, null);
	private GUIPropertyBoolean throughputTimeMonitor = new GUIPropertyBoolean(
			" Throughput time monitor ",
			"Generates a monitor for monitoring the throughput time", false,
			null);
	private GUIPropertyBoolean resourcesMonitor = new GUIPropertyBoolean(
			" Resource availability monitor ",
			"Generates a monitor for monitoring the availability of resources",
			false, null);
	private static ManagerConfiguration myInstance = new ManagerConfiguration();
	private JPanel outmostLayer = null;
	private static final String textExecutionTimeOption = "execution time";
	private static final String textWaitingTimeAndExecutionTimeOption = "waiting time + execution time";
	private static final String textSojournTimeOption = "sojourn time";
	private static final String textPush = "Push work distribution";
	private static final String textPull = "Pull work distribution";
	private static Color bgColor = new Color(190, 190, 190);

	/**
	 * Private constructor to prevent the creation of more than one
	 * configuration manager object (Singleton pattern). Use
	 * {@link #getInstance()} instead to retrieve the single object available.
	 */
	private ManagerConfiguration() {
		ArrayList<String> timeOptions = new ArrayList<String>();
		timeOptions.add(textExecutionTimeOption);
		timeOptions.add(textWaitingTimeAndExecutionTimeOption);
		timeOptions.add(textSojournTimeOption);
		timeOptionProperty = new GuiPropertyListRadio("", null, timeOptions,
				new TimeOptionsPerspectiveListener());
		// initially time dimension is deselected (and therefore all sub-options
		// too)
		timeOptionProperty.disable(textExecutionTimeOption);
		timeOptionProperty.disable(textWaitingTimeAndExecutionTimeOption);
		timeOptionProperty.disable(textSojournTimeOption);
		myWaitingRatio = new GUIPropertyInteger(
				"  percent of the waiting time", 100, 0, 100);
		// define the guipropertylistradio for chosing between push and pull
		// when the waiting+execution time option and the
		// resource option is selected
		ArrayList<String> distMech = new ArrayList<String>();
		distMech.add("Push work distribution");
		distMech.add("Pull work distribution");
		PushPullProperty = new GuiPropertyListRadio("", "", distMech, null);
		PushPullProperty.setSelected(textPull, true);
	}

	/**
	 * Retrieves the only instance of this configuration manager (Singleton
	 * pattern).
	 * 
	 * @return the global configuration manager
	 */
	public static ManagerConfiguration getInstance() {
		return myInstance;
	}

	/**
	 * Retrieves whether the data perspective is enabled or not
	 * 
	 * @return boolean <code>true</code> when the data perspective is enabled
	 *         and <code>false</code> when the data perspective is disabled
	 */
	public boolean isDataPerspectiveEnabled() {
		return myDataPerspectiveSelected.getValue();
	}

	/**
	 * Retrieves whether the time perspective is enabled or not
	 * 
	 * @return boolean <code>true</code> when the time perspective is enabled
	 *         and <code>false</code> when the time perspective is disabled
	 */
	public boolean isTimePerspectiveEnabled() {
		return myTimePerspectiveSelected.getValue();
	}

	/**
	 * Retrieves whether only the execution time is enabled.
	 * 
	 * @return boolean
	 */
	public boolean isOnlyExecutionTimeEnabled() {
		return myTimePerspectiveSelected.getValue()
				&& timeOptionProperty.getValue()
						.equals(textExecutionTimeOption);
	}

	/**
	 * Retrieves whether the only the waiting time and the execution time are
	 * enabled
	 * 
	 * @return boolean
	 */
	public boolean isOnlyWaitingAndExecutionTimeEnabled() {
		return myTimePerspectiveSelected.getValue()
				&& timeOptionProperty.getValue().equals(
						textWaitingTimeAndExecutionTimeOption);
	}

	/**
	 * Retrieves whether only the sojourn time is enabled
	 * 
	 * @return boolean
	 */
	public boolean isOnlySojournTimeEnabled() {
		return myTimePerspectiveSelected.getValue()
				&& timeOptionProperty.getValue().equals(textSojournTimeOption);
	}

	/**
	 * Retrieves the year offset that indicates when the process starts. <br>
	 * For example used for logging timestamps during simulation. The generated
	 * logging monitors would start to log at the Unix zero timestamp (i.e.
	 * 1970).
	 * 
	 * @return the offset in years to 1970
	 */
	public int getYearOffset() {
		return myYearOffset.getValue();
	}

	/**
	 * Retrieves whether the resource perspective is enabled or not
	 * 
	 * @return boolean <code>true</code> when the resource perspective is
	 *         enabled and <code>false</code> when the resource perspective is
	 *         disabled
	 */
	public boolean isResourcePerspectiveEnabled() {
		return myResourcePerspectiveSelected.getValue();
	}

	/**
	 * Retrives the value of waiting ratio
	 * 
	 * @return the waiting ratio
	 */
	public int getWaitingRatio() {
		return myWaitingRatio.getValue();
	}

	/**
	 * Retrieves whether the logging is enabled or not
	 * 
	 * @return boolean <code>true</code> when the logging is enabled and
	 *         <code>false</code> when the logging is disabled
	 */
	public boolean isActivityLoggingEnabled() {
		return myActivityLoggingMonitor.getValue();
	}

	public boolean isCurrentStateSelected() {
		return myCurrentStateOption.getValue();
	}

	public boolean isPushEnabled() {
		return PushPullProperty.isEnabled(textPush)
				&& PushPullProperty.getValue().equals(textPush);
	}

	public boolean isPullEnabled() {
		return PushPullProperty.isEnabled(textPull)
				&& PushPullProperty.getValue().equals(textPull);
	}

	public boolean isThroughputTimeMonitorEnabled() {
		return throughputTimeMonitor.isEnabled()
				&& throughputTimeMonitor.getValue();
	}

	public boolean isResourcesMonitorEnabled() {
		return resourcesMonitor.isEnabled() && resourcesMonitor.getValue();
	}

	/**
	 * Set the configuration options in redesign mode.
	 */
	public void setRedesignConfiguration() {
		myTimePerspectiveSelected.setSelected(true);
		myResourcePerspectiveSelected.setSelected(true);
		throughputTimeMonitor.setSelected(true);
	}

	/**
	 * Creates GUI panel containg the configuration options, ready to display in
	 * some settings dialog.
	 * 
	 * @return the graphical panel representing the configuration options
	 */
	public JPanel getPanel() {
		if (outmostLayer == null) {
			outmostLayer = new JPanel(new BorderLayout());
			outmostLayer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			outmostLayer.setOpaque(false);
			// configure perspectives
			JPanel leftPanel = new JPanel(new BorderLayout());
			leftPanel.setBackground(bgColor);
			leftPanel.setBorder(BorderFactory.createEmptyBorder());
			JPanel configurationOptionsInnerLayer = new JPanel();
			configurationOptionsInnerLayer.setLayout(new BoxLayout(
					configurationOptionsInnerLayer, BoxLayout.Y_AXIS));
			configurationOptionsInnerLayer.setBorder(BorderFactory
					.createEmptyBorder(10, 10, 10, 10));
			configurationOptionsInnerLayer.setBackground(bgColor);
			configurationOptionsInnerLayer.add(myDataPerspectiveSelected
					.getPropertyPanel());
			configurationOptionsInnerLayer.add(Box
					.createRigidArea(new Dimension(0, 15)));
			configurationOptionsInnerLayer.add(myTimePerspectiveSelected
					.getPropertyPanel());
			configurationOptionsInnerLayer.add(timeOptionProperty
					.getPropertyPanel());
			// continue with the remaining boolean properties
			configurationOptionsInnerLayer.add(Box
					.createRigidArea(new Dimension(0, 15)));
			configurationOptionsInnerLayer.add(myResourcePerspectiveSelected
					.getPropertyPanel());
			configurationOptionsInnerLayer.add(Box
					.createRigidArea(new Dimension(0, 5)));
			configurationOptionsInnerLayer.add(myWaitingRatio
					.getPropertyPanel());
			// add the radio buttons for selecting push and pull
			configurationOptionsInnerLayer.add(PushPullProperty
					.getPropertyPanel());
			myWaitingRatio.disable(); // per default disabled
			PushPullProperty.disable(textPull);
			PushPullProperty.disable(textPush);

			// add checkboxes for creation of the three different monitors
			JPanel rightPanel = new JPanel(new BorderLayout());
			rightPanel.setBackground(bgColor);
			rightPanel.setBorder(BorderFactory.createEmptyBorder());
			JPanel extrasOptionsInnerLayer = new JPanel();
			extrasOptionsInnerLayer.setLayout(new BoxLayout(
					extrasOptionsInnerLayer, BoxLayout.Y_AXIS));
			extrasOptionsInnerLayer.setBorder(BorderFactory.createEmptyBorder(
					10, 10, 10, 10));
			extrasOptionsInnerLayer.setBackground(bgColor);
			extrasOptionsInnerLayer
					.add(myCurrentStateOption.getPropertyPanel());
			extrasOptionsInnerLayer.add(Box
					.createRigidArea(new Dimension(0, 15)));
			extrasOptionsInnerLayer.add(myActivityLoggingMonitor
					.getPropertyPanel());
			JPanel loggingOptions = new JPanel();
			loggingOptions.setLayout(new BoxLayout(loggingOptions,
					BoxLayout.X_AXIS));
			loggingOptions.setOpaque(false);
			loggingOptions.add(Box.createRigidArea(new Dimension(5, 0)));
			loggingOptions.add(myYearOffset.getPropertyPanel());
			extrasOptionsInnerLayer.add(loggingOptions);
			extrasOptionsInnerLayer.add(Box
					.createRigidArea(new Dimension(0, 15)));
			myYearOffset.disable(); // per default disabled
			extrasOptionsInnerLayer.add(throughputTimeMonitor
					.getPropertyPanel());
			extrasOptionsInnerLayer.add(resourcesMonitor.getPropertyPanel());
			leftPanel.add(configurationOptionsInnerLayer, BorderLayout.CENTER);
			rightPanel.add(extrasOptionsInnerLayer, BorderLayout.CENTER);

			JPanel jointPanel = new JPanel();
			jointPanel.setOpaque(false);
			jointPanel.setBorder(BorderFactory.createEmptyBorder());
			jointPanel.setLayout(new GridLayout(1, 2));
			jointPanel
					.add(GuiUtilities
							.configureAnyScrollable(
									leftPanel,
									"Perspectives",
									"Select which perspectives should be included in the simulation model.",
									bgColor));
			jointPanel
					.add(GuiUtilities
							.configureAnyScrollable(
									rightPanel,
									"Extras",
									"The following additional functions can be generated as a supplement.",
									bgColor));
			outmostLayer.add(jointPanel, BorderLayout.CENTER);
		}

		return outmostLayer;
	}

	/**
	 * Class handling the selection state changes due to activity logging
	 * option.
	 */
	class LoggingOptionListener implements GuiNotificationTarget {

		public void updateGUI() {
			if (myActivityLoggingMonitor.getValue() == true) {
				myYearOffset.enable();
			} else {
				myYearOffset.disable();
			}
		}
	}

	/**
	 * Class handling the selection state change of the time check button.
	 */
	class TimePerspectiveListener implements GuiNotificationTarget {

		public void updateGUI() {
			if (myTimePerspectiveSelected.getValue()) {
				if (myResourcePerspectiveSelected.getValue()) {
					timeOptionProperty.enable(textExecutionTimeOption);
					timeOptionProperty
							.enable(textWaitingTimeAndExecutionTimeOption);
					timeOptionProperty.disable(textSojournTimeOption);
					// select the execution time option
					timeOptionProperty.setSelected(textExecutionTimeOption,
							true);
					myWaitingRatio.disable();
					// disable push and pull
					PushPullProperty.disable(textPush);
					PushPullProperty.disable(textPull);
				} else {
					timeOptionProperty.enable(textExecutionTimeOption);
					timeOptionProperty
							.enable(textWaitingTimeAndExecutionTimeOption);
					timeOptionProperty.enable(textSojournTimeOption);
					// select the execution time option
					timeOptionProperty.setSelected(textExecutionTimeOption,
							true);
				}
			} else {
				// disable the separate timing options
				timeOptionProperty.disable(textExecutionTimeOption);
				timeOptionProperty
						.disable(textWaitingTimeAndExecutionTimeOption);
				timeOptionProperty.disable(textSojournTimeOption);
				myWaitingRatio.disable();
				// disable push and pull
				PushPullProperty.disable(textPush);
				PushPullProperty.disable(textPull);
				// again enable the resource perspective in the case that this
				// had
				// been disabled
				if (!myResourcePerspectiveSelected.isEnabled()) {
					myResourcePerspectiveSelected.enable();
				}
			}
		}
	}

	/**
	 * Class handling the selection state change of the resource check button.
	 */
	class ResourcePerspectiveListener implements GuiNotificationTarget {

		public void updateGUI() {
			if (myResourcePerspectiveSelected.isEnabled()) {
				if (myResourcePerspectiveSelected.getValue()) {
					// disable the waiting time + execution time and sojourn
					// time radio buttons
					timeOptionProperty.disable(textSojournTimeOption);
					// in the case that the timing perspective is enabled make
					// sure that that the
					// execution time radio button is selected
					if (myTimePerspectiveSelected.getValue()
							&& timeOptionProperty.getValue().equals(
									textSojournTimeOption)) {
						timeOptionProperty.setSelected(textExecutionTimeOption,
								true);
					}
					if (myTimePerspectiveSelected.getValue()
							&& timeOptionProperty.getValue().equals(
									textWaitingTimeAndExecutionTimeOption)) {
						myWaitingRatio.enable();
						// enable push and pull
						PushPullProperty.enable(textPush);
						PushPullProperty.enable(textPull);
					}
				} else {
					// first check whether the time perspective is enabled
					if (myTimePerspectiveSelected.getValue()) {
						// enable the waiting time + execution time and sojourn
						// time radio buttons
						String str = timeOptionProperty.getValue();
						timeOptionProperty.enable(textSojournTimeOption);
						timeOptionProperty.setSelected(str, true);
						// select execution time
						myWaitingRatio.disable();
						// disable push and pull
						PushPullProperty.disable(textPush);
						PushPullProperty.disable(textPull);
					}
				}
			}
		}
	}

	/**
	 * Class handling the selection state change of the radio buttons for the
	 * time options
	 */
	class TimeOptionsPerspectiveListener implements GuiNotificationTarget {

		public void updateGUI() {
			if ((timeOptionProperty.getValue().equals(textSojournTimeOption) && timeOptionProperty
					.isEnabled(textSojournTimeOption))) {
				// disable and deselect the resource perspective
				myResourcePerspectiveSelected.setSelected(false);
				myResourcePerspectiveSelected.disable();
				myWaitingRatio.disable();
				// disable push and pull
				PushPullProperty.disable(textPull);
				PushPullProperty.disable(textPush);
			}
			// in the case that time option execution time is selected, enable
			// the resource perspective if this had been disabled
			if (timeOptionProperty.getValue().equals(textExecutionTimeOption)
					&& timeOptionProperty.isEnabled(textExecutionTimeOption)) {
				myWaitingRatio.disable();
				// disable push and pull
				PushPullProperty.disable(textPull);
				PushPullProperty.disable(textPush);
				if (!myResourcePerspectiveSelected.isEnabled())
					myResourcePerspectiveSelected.enable();
			}
			if (timeOptionProperty.getValue().equals(
					textWaitingTimeAndExecutionTimeOption)
					&& timeOptionProperty
							.isEnabled(textWaitingTimeAndExecutionTimeOption)) {
				if (!myResourcePerspectiveSelected.isEnabled()) {
					myResourcePerspectiveSelected.enable();
				}
				if (myResourcePerspectiveSelected.getValue()) {
					myWaitingRatio.enable();
					// enable push and pull
					PushPullProperty.enable(textPush);
					PushPullProperty.enable(textPull);
				} else {
					myWaitingRatio.disable();
					// disable push and pull
					PushPullProperty.disable(textPull);
					PushPullProperty.disable(textPush);
				}
			}
		}
	}
}
