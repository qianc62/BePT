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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.processmining.analysis.conformance.MaximumSearchDepthDiagnosis;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisGUI;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;
import org.processmining.framework.ui.MainUI;

/**
 * This class is needed to allow the user to set options, such as the boundaries
 * of bottleneck levels, i.e. the waiting time-levels, and colors corresponding
 * to these levels that can be high, medium or low can be set and given a color.
 * As well as the timesort and the number of decimals to use.
 * 
 * @see PerformanceAnalysisSettings
 * @see PerformanceAnalysisGUI
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */

public class PerformanceConfiguration extends JPanel {

	/**
	 * The results needed to initialize the GUI (only used for top level
	 * categories). A set of AnalysisResult objects.
	 */
	private HashSet myResultObjects = new HashSet();

	/** used by top level configuration objects to store the analysis result GUI */
	private AnalysisGUI myResultPanel;

	// variables to store color for each level
	private Color lowLevelColor = Color.BLUE;
	private Color mediumLevelColor = Color.YELLOW;
	private Color highLevelColor = Color.MAGENTA;
	// variables to store timesort
	private String timeSort = "seconds";
	private long timeDivider = 1000;
	// variables to store bound values
	private double lowUpper = 0.0;
	private double mediumUpper = 0.0;
	// variable to store number of decimal places
	private int decimalPlaces = 2;
	// variable to store advanced settings
	// (last parameter determines whether to restrict the search depth for the
	// log replay.
	// setting it to -1 disables the restriction)
	private int[] advancedSettings = { 1, 1, 1, 1, 1 };
	// selected place (if any)
	private ExtendedPlace selectedPlace;

	// GUI related components
	private JPanel radioPanel = new JPanel(); // Panel containing radiobuttons
	private JPanel placePanel = new JPanel(); // Panel containing other
	// radiobuttons
	// Panel containing the manualPanel and the "Manual settings:"-label
	private JPanel settingsPanel = new JPanel();
	// Panels to make UI look better
	private JPanel topPanel = new JPanel();
	private JPanel bottomPanel = new JPanel();
	private JPanel generalPanel = new JPanel();
	private JPanel bottleneckPanel = new JPanel();
	// Panels for each classification
	private JPanel lowPanel = new JPanel();
	private JPanel mediumPanel = new JPanel();
	private JPanel highPanel = new JPanel();
	// Panels which are used to display the currently selected color with
	private JPanel lowColorPanel = new JPanel();
	private JPanel mediumColorPanel = new JPanel();
	private JPanel highColorPanel = new JPanel();
	// radio buttons + group to allow the user to select auto or manual settings
	private ButtonGroup buttonGroup1 = new ButtonGroup();
	private JRadioButton autoButton = new JRadioButton(
			"Use auto bottleneck settings "
					+ " (let program choose bound values)", true);
	private JRadioButton ownButton = new JRadioButton(
			"Use manual bottleneck settings " + " (specify below)");

	// radio buttons + group to allow the user to select if the entered values
	// should be set for all places or only for the currently selected (if any)
	// one
	private ButtonGroup buttonGroup2 = new ButtonGroup();
	private JRadioButton allPlacesButton = new JRadioButton("All places", true);
	private JRadioButton thisPlaceButton = new JRadioButton(
			"The selected place only", false);

	// array of "time" strings required to fill the timeBox combobox with
	private String[] timeSorts = { "milliseconds", "seconds", "minutes",
			"hours", "days", "weeks", "months", "years" };
	private JComboBox timeBox = new JComboBox(timeSorts);

	// buttons for changing color
	private JButton lowButton = new JButton("Change");
	private JButton mediumButton = new JButton("Change");
	private JButton highButton = new JButton("Change");
	// button for advanced settings
	private JButton advancedButton = new JButton("Advanced Settings");
	// Textfields where the user can fill in the values of bounds
	private JTextField lowUpperField = new JTextField();
	private JTextField mediumUpperField = new JTextField();
	// Textfield where the user can fill in the number of decimals
	private JTextField decimalField = new JTextField("2");
	// various labels
	private JLabel selectLabel = new JLabel("Settings: ");
	private JLabel timesortLabel = new JLabel("Times measured in: ");
	private JLabel decimalLabel = new JLabel("Decimal number:");
	private JLabel upperLowLabel = new JLabel("Upper bound: ");
	private JLabel upperMediumLabel = new JLabel("Upper bound: ");
	private JLabel useSettingsLabel = new JLabel(
			"Use bottleneck settings for: ");
	private JLabel lowColorLabel = new JLabel("Color: ");
	private JLabel lowTimeLabel = new JLabel();
	private JLabel mediumColorLabel = new JLabel("Color: ");
	private JLabel mediumTimeLabel = new JLabel();
	private JLabel highColorLabel = new JLabel("Color: ");
	private HashSet advancedSettingsFrames = new HashSet();

	/**
	 * Constructor to build the GUI
	 */
	public PerformanceConfiguration(PetriNet net) {
		try {
			// initialize maximum search depth based on net structure
			int maxSearchDepth = MaximumSearchDepthDiagnosis
					.determineMaximumSearchDepth(net);
			advancedSettings[4] = maxSearchDepth;
			// build configuration frame
			jbInit();
			registerGuiActionListener();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Just as the normal constructor, but also initializes the options, with
	 * the parameter-values
	 * 
	 * @param levelColors
	 *            ArrayList: contains the colors corresponding to the different
	 *            time levels
	 * @param bounds
	 *            ArrayList: contains the bounds
	 * @param sort
	 *            String: contains the timeSort in which is measured
	 * @param decimals
	 *            int: number of decimal places
	 * @param settings
	 *            int[]: the advanced options used
	 */
	public PerformanceConfiguration(ArrayList levelColors, ArrayList bounds,
			String sort, int decimals, int[] settings) {
		try {
			lowLevelColor = (Color) levelColors.get(0);
			mediumLevelColor = (Color) levelColors.get(1);
			highLevelColor = (Color) levelColors.get(2);
			Double temp = (Double) bounds.get(0);
			lowUpper = temp.doubleValue();
			temp = (Double) bounds.get(1);
			mediumUpper = temp.doubleValue();
			timeSort = sort;
			decimalPlaces = decimals;
			timeDivider = determineTimeNumber(timeSort);
			advancedSettings = settings;
			allPlacesButton.setSelected(true);
			radioPanel.setVisible(false);
			settingsPanel.setVisible(true);
			jbInit();
			registerGuiActionListener();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Initializes the GUI-panel in which the user can specify settings, e.g.
	 * level boundaries and colors
	 * 
	 * @throws Exception
	 */
	private void jbInit() throws Exception {
		// build GUI
		buttonGroup1.add(autoButton);
		buttonGroup1.add(ownButton);
		radioPanel.setLayout(new GridBagLayout());
		GridBagConstraints con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = 0;
		con.insets = new Insets(10, 10, 0, 0);
		con.anchor = GridBagConstraints.NORTHWEST;
		radioPanel.add(selectLabel, con);
		con.gridx = 0;
		con.gridy = 1;
		con.gridwidth = 2;
		radioPanel.add(generalPanel, con);
		con.gridx = 0;
		con.gridy = 2;
		radioPanel.add(autoButton, con);
		con.gridx = 0;
		con.gridy = 3;
		con.insets = new Insets(0, 10, 0, 0);
		radioPanel.add(ownButton, con);

		buttonGroup2.add(allPlacesButton);
		buttonGroup2.add(thisPlaceButton);
		placePanel.setLayout(new BoxLayout(placePanel, BoxLayout.Y_AXIS));
		placePanel.add(useSettingsLabel);
		placePanel.add(allPlacesButton);
		placePanel.add(thisPlaceButton);
		decimalField.setText(decimalPlaces + "");
		lowUpperField.setText(lowUpper + "");
		lowUpperField.setHorizontalAlignment(SwingConstants.TRAILING);
		mediumUpperField.setHorizontalAlignment(SwingConstants.LEADING);

		lowTimeLabel.setText(timeSort);
		mediumTimeLabel.setText(timeSort);
		lowColorPanel.setBackground(lowLevelColor);
		lowColorPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		mediumColorPanel.setBackground(mediumLevelColor);
		mediumColorPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		highColorPanel.setBackground(highLevelColor);
		highColorPanel.setBorder(BorderFactory.createRaisedBevelBorder());

		timeBox.setSelectedItem(timeSort);
		mediumUpperField.setText(mediumUpper + "");
		mediumUpperField.setHorizontalAlignment(SwingConstants.TRAILING);

		lowButton.setBorder(BorderFactory.createRaisedBevelBorder());
		mediumButton.setBorder(BorderFactory.createRaisedBevelBorder());
		highButton.setBorder(BorderFactory.createRaisedBevelBorder());

		// initialize the panel that contains low level settings
		lowPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(
				Color.white, new Color(148, 145, 140)), "Low level settings",
				0, 0, new Font("SansSerif", Font.BOLD, 12)));
		lowPanel.setLayout(new GridBagLayout());
		lowPanel.setMinimumSize(new Dimension(400, 120));
		con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = 0; // column and row
		con.gridwidth = 1;
		con.insets = new Insets(0, 5, 5, 10);
		lowPanel.add(upperLowLabel, con);
		con.gridx = 1;
		con.gridy = 0;
		con.gridwidth = 2;
		lowUpperField.setPreferredSize(new Dimension(140, 20));
		lowPanel.add(lowUpperField, con);
		con.gridx = 3;
		con.gridy = 0;
		con.gridwidth = 1;
		lowPanel.add(lowTimeLabel, con);
		con.gridx = 0;
		con.gridy = 1;
		lowPanel.add(lowColorLabel, con);
		con.gridx = 1;
		con.gridy = 1;
		lowColorPanel.setPreferredSize(new Dimension(40, 20));
		lowPanel.add(lowColorPanel, con);
		con.gridx = 2;
		con.gridy = 1;
		lowButton.setPreferredSize(new Dimension(80, 20));
		lowPanel.add(lowButton, con);
		// initialize the panel that contains medium level settings
		mediumPanel.setBorder(new TitledBorder(BorderFactory
				.createEtchedBorder(Color.white, new Color(148, 145, 140)),
				"Medium level settings", 0, 0, new Font("SansSerif", Font.BOLD,
						12)));
		mediumPanel.setLayout(new GridBagLayout());
		mediumPanel.setMinimumSize(new Dimension(400, 120));
		con.gridx = 0;
		con.gridy = 0;
		mediumPanel.add(upperMediumLabel, con);
		con.gridx = 1;
		con.gridy = 0;
		con.gridwidth = 2;
		mediumUpperField.setPreferredSize(new Dimension(140, 20));
		mediumPanel.add(mediumUpperField, con);
		con.gridx = 3;
		con.gridy = 0;
		con.gridwidth = 1;
		mediumPanel.add(mediumTimeLabel, con);
		con.gridx = 0;
		con.gridy = 1;
		con.anchor = GridBagConstraints.NORTHWEST;
		mediumPanel.add(mediumColorLabel, con);
		con.gridx = 1;
		con.gridy = 1;
		mediumColorPanel.setPreferredSize(new Dimension(40, 20));
		mediumPanel.add(mediumColorPanel, con);
		con.gridx = 2;
		con.gridy = 1;
		mediumButton.setPreferredSize(new Dimension(80, 20));
		mediumPanel.add(mediumButton, con);
		// initialize the panel that contains high level settings
		highPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(
				Color.white, new Color(148, 145, 140)), "High level settings",
				0, 0, new Font("SansSerif", Font.BOLD, 12)));
		highPanel.setLayout(new GridBagLayout());
		highPanel.setMinimumSize(new Dimension(400, 120));
		con.gridx = 0;
		con.gridy = 0;
		con.gridwidth = 2;
		con.insets = new Insets(0, 5, 5, 53);
		con.anchor = GridBagConstraints.NORTHWEST;
		highPanel.add(highColorLabel, con);
		con.gridx = 2;
		con.gridy = 0;
		con.gridwidth = 1;
		con.insets = new Insets(0, 5, 5, 15);
		highColorPanel.setPreferredSize(new Dimension(40, 20));
		highPanel.add(highColorPanel, con);
		con.gridx = 3;
		con.gridy = 0;
		con.insets = new Insets(0, 0, 5, 55);
		highButton.setPreferredSize(new Dimension(80, 20));
		highPanel.add(highButton, con);
		// fill general panel
		generalPanel.setLayout(new GridBagLayout());
		con = new GridBagConstraints();
		con.gridy = 0;
		con.gridx = 0;
		con.insets = new Insets(0, 10, 0, 0);
		con.anchor = GridBagConstraints.NORTHWEST;
		generalPanel.add(timesortLabel, con);
		con.gridy = 0;
		con.gridx = 1;
		con.insets = new Insets(0, 10, 5, 70);
		con.anchor = GridBagConstraints.NORTHWEST;
		timeBox.setPreferredSize(new Dimension(120, 20));
		timeBox.setBackground(Color.WHITE);
		generalPanel.add(timeBox, con);
		con.gridy = 1;
		con.gridx = 0;
		con.insets = new Insets(0, 10, 0, 0);
		con.anchor = GridBagConstraints.NORTHWEST;
		generalPanel.add(decimalLabel, con);
		con.gridy = 1;
		con.gridx = 1;
		con.insets = new Insets(0, 10, 0, 0);
		con.anchor = GridBagConstraints.NORTHWEST;
		decimalField.setPreferredSize(new Dimension(30, 20));
		decimalField.setHorizontalAlignment(SwingConstants.TRAILING);
		generalPanel.add(decimalField, con);
		con.gridy = 2;
		con.gridx = 0;
		con.insets = new Insets(5, 10, 5, 0);
		con.anchor = GridBagConstraints.NORTHWEST;
		con.gridwidth = 2;
		generalPanel.add(advancedButton, con);
		generalPanel
				.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(
						Color.white, new Color(148, 145, 140)),
						"General settings", 0, 0, new Font("SansSerif",
								Font.BOLD, 13)));
		// fill bottleneck panel
		bottleneckPanel.setLayout(new GridBagLayout());
		con = new GridBagConstraints();
		bottleneckPanel.add(lowPanel);
		bottleneckPanel.add(mediumPanel);
		bottleneckPanel.add(highPanel);
		bottleneckPanel.add(placePanel);
		con.gridy = 0;
		con.gridx = 0;
		con.gridwidth = 5;
		con.insets = new Insets(0, 10, 0, 0);
		con.anchor = GridBagConstraints.NORTHWEST;
		bottleneckPanel.add(lowPanel, con);
		con.gridy = 1;
		con.gridx = 0;
		con.gridwidth = 5;
		bottleneckPanel.add(mediumPanel, con);
		con.gridy = 2;
		con.gridx = 0;
		con.gridwidth = 5;
		bottleneckPanel.add(highPanel, con);
		con.gridy = 3;
		con.gridx = 0;
		bottleneckPanel.add(placePanel, con);
		bottleneckPanel.setBorder(new TitledBorder(BorderFactory
				.createEtchedBorder(Color.white, new Color(148, 145, 140)),
				"Bottleneck settings", 0, 0, new Font("SansSerif", Font.BOLD,
						13)));

		// use bottomPanel to make the GUI look better
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(bottleneckPanel, BorderLayout.WEST);
		settingsPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		settingsPanel.setLayout(new BorderLayout());
		settingsPanel.add(bottomPanel, BorderLayout.NORTH);
		// hide the settingsPanel
		settingsPanel.setVisible(false);

		// set fonts
		autoButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
		ownButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
		selectLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		useSettingsLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		timesortLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		decimalLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		decimalField.setFont(new Font("SansSerif", Font.PLAIN, 13));
		upperLowLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		upperMediumLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		lowColorLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mediumColorLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		highColorLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		lowTimeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mediumTimeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		timeBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mediumUpperField.setFont(new Font("SansSerif", Font.PLAIN, 13));
		lowButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mediumButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
		highButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
		lowUpperField.setFont(new Font("SansSerif", Font.PLAIN, 13));
		allPlacesButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
		thisPlaceButton.setFont(new Font("SansSerif", Font.PLAIN, 12));

		// set tooltips
		lowButton.setToolTipText("Change color for places with a low waiting"
				+ " time.");
		mediumButton.setToolTipText("Change color for places with a medium "
				+ " waiting time.");
		highButton.setToolTipText("Change color for places with a high waiting"
				+ " time.");
		lowUpperField.setToolTipText("Enter the upper bound value of the"
				+ " low waiting time level.");
		mediumUpperField.setToolTipText("Enter the upper bound value of the"
				+ " medium waiting time level.");
		decimalField.setToolTipText("Enter the maximum number of places behind"
				+ " the comma here (at most 20).");
		placePanel.setVisible(false);
		// use topPanel to make the GUI look better
		topPanel.setLayout(new BorderLayout());
		topPanel.add(radioPanel, BorderLayout.WEST);
		this.setLayout(new BorderLayout());
		this.add(settingsPanel, BorderLayout.CENTER);
		this.add(topPanel, BorderLayout.NORTH);
		this.validate();
		this.repaint();
	}

	/**
	 * Connects GUI elements with functionality that allows for interaction
	 */
	private void registerGuiActionListener() {
		ownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// display the settings panel
				settingsPanel.setVisible(true);
			}
		});
		autoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// hide the settings panel
				settingsPanel.setVisible(false);
			}
		});
		timeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// change labels when the user has selected a timesort
				timeSort = timeBox.getSelectedItem().toString();
				timeDivider = determineTimeNumber(timeSort);
				mediumTimeLabel.setText(timeSort);
				lowTimeLabel.setText(timeSort);
			}
		});
		decimalField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// set the number of decimal places to the number specified by
				// the user
				String temp = decimalField.getText();
				temp = temp.trim();
				try {
					decimalPlaces = Integer.parseInt(temp);
				} catch (NumberFormatException ex) {
					// illegal value was filled in, restore to old value
					JOptionPane.showMessageDialog(MainUI.getInstance(),
							"Illegal upper bound entered at low level!");
					decimalField.setText(decimalPlaces + "");
				}
			}
		});

		decimalField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			};

			public void focusLost(FocusEvent e) {
				// set the number of decimal places to the number specified by
				// the user
				String temp = decimalField.getText();
				temp = temp.trim();
				try {
					decimalPlaces = Integer.parseInt(temp);
				} catch (NumberFormatException ex) {
					// illegal value was filled in, restore to old value
					JOptionPane.showMessageDialog(MainUI.getInstance(),
							"Illegal upper bound entered at low level!");
					decimalField.setText(decimalPlaces + "");
				}
			}
		});
		lowButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// open dialog in which the user can choose a new color
				Color tempColor = JColorChooser.showDialog(
						MainUI.getInstance(), "Select low level color",
						lowLevelColor);
				if (tempColor != null) {
					lowLevelColor = tempColor;
				}
				lowColorPanel.setBackground(lowLevelColor);
			}
		});
		mediumButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// open dialog in which the user can choose a new color
				Color tempColor = JColorChooser.showDialog(
						MainUI.getInstance(), "Select medium level color",
						mediumLevelColor);
				if (tempColor != null) {
					mediumLevelColor = tempColor;
				}
				mediumColorPanel.setBackground(mediumLevelColor);
			}
		});
		highButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// open dialog in which the user can choose a new color
				Color tempColor = JColorChooser.showDialog(
						MainUI.getInstance(), "Select high level color",
						highLevelColor);
				if (tempColor != null) {
					highLevelColor = tempColor;
				}
				highColorPanel.setBackground(highLevelColor);
			}
		});

		// ActionListener and FocusListener at input fields
		lowUpperField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// set lowUpper to the value filled in at lowUpperField (if
				// valid)
				String temp = lowUpperField.getText();
				temp = temp.trim();
				try {
					lowUpper = Double.parseDouble(temp);
				} catch (NumberFormatException ex) {
					// illegal value filled in, restore to previous value
					JOptionPane.showMessageDialog(MainUI.getInstance(),
							"Illegal upper bound entered at low level!");
					lowUpperField.setText("" + lowUpper);
				}
			}
		});
		lowUpperField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			};

			public void focusLost(FocusEvent e) {
				// set lowUpper to the value filled in at lowUpperField (if
				// valid)
				String temp = lowUpperField.getText();
				temp = temp.trim();
				try {
					lowUpper = Double.parseDouble(temp);
				} catch (NumberFormatException ex) {
					// illegal value filled in, restore to previous value
					JOptionPane.showMessageDialog(MainUI.getInstance(),
							"Illegal upper bound entered at low level!");
					lowUpperField.setText("" + lowUpper);
				}
			}
		});

		mediumUpperField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String temp = mediumUpperField.getText();
				temp = temp.trim();
				try {
					mediumUpper = Double.parseDouble(temp);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(MainUI.getInstance(),
							"Illegal upper bound entered at medium level!");
					mediumUpperField.setText("" + mediumUpper);
				}
			}
		});
		mediumUpperField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			};

			public void focusLost(FocusEvent e) {
				String temp = mediumUpperField.getText();
				temp = temp.trim();
				try {
					mediumUpper = Double.parseDouble(temp);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(MainUI.getInstance(),
							"Illegal upper bound entered at medium level!");
					mediumUpperField.setText("" + mediumUpper);
				}
			}
		});
		advancedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// show the panel that contains the advanced settings
				AdvancedOptions advancedPanel = new AdvancedOptions(
						advancedSettings.clone(), getConfiguration());
				MainUI.getInstance().createFrame(
						"Advanced Performance Analysis options", advancedPanel);
				JInternalFrame advancedSettingsFrame = MainUI.getInstance()
						.getDesktop().getSelectedFrame();
				advancedSettingsFrame.setMinimumSize(new Dimension(600, 500));
				advancedSettingsFrame.setSize(new Dimension(600, 500));
				advancedSettingsFrames.add(advancedSettingsFrame);
			}
		});
	}

	/**
	 * Disables all buttons, comboboxes and input fields
	 */
	public void disableAll() {
		autoButton.setEnabled(false);
		ownButton.setEnabled(false);
		timeBox.setEnabled(false);
		lowButton.setEnabled(false);
		mediumButton.setEnabled(false);
		highButton.setEnabled(false);
		lowUpperField.setEnabled(false);
		mediumUpperField.setEnabled(false);
		decimalField.setEnabled(false);
		advancedButton.setEnabled(false);
		closeAdvancedFrames();
	}

	/**
	 * Enables all buttons, comboboxes and input fields
	 */
	public void enableAll() {
		autoButton.setEnabled(true);
		ownButton.setEnabled(true);
		timeBox.setEnabled(true);
		lowButton.setEnabled(true);
		mediumButton.setEnabled(true);
		highButton.setEnabled(true);
		lowUpperField.setEnabled(true);
		mediumUpperField.setEnabled(true);
		decimalField.setEnabled(true);
		advancedButton.setEnabled(true);
	}

	/**
	 * Hides the radioPanel and displays the settingsPanel. Furthermore, this
	 * method checks whether a place was selected and displays the placePanel if
	 * this is the case. If the place has its 'own'-settings, i.e. the user
	 * filled in settings specific to this place before, then the settings that
	 * were filled in by the user are displayed.
	 * 
	 * @param selected
	 *            ExtendedPlace: the selected place
	 */
	public void changeDisplay(ExtendedPlace selected) {
		allPlacesButton.setSelected(true);
		topPanel.setVisible(true);
		settingsPanel.setVisible(true);
		settingsPanel.setBorder(BorderFactory.createEtchedBorder());
		radioPanel.setVisible(true);
		autoButton.setVisible(false);
		ownButton.setVisible(false);
		if (selected != null) {
			// a place was selected -> show the placePanel
			placePanel.setVisible(true);
			selectedPlace = selected;
			thisPlaceButton.setText("The selected place ("
					+ selectedPlace.getIdentifier() + ") only");
			if (selectedPlace.hasSettings()) {
				// the selected place has its own settings, initialize the
				// configuration with these
				thisPlaceButton.setSelected(true);
				lowLevelColor = (Color) selected.getColors().get(0);
				mediumLevelColor = (Color) selected.getColors().get(1);
				highLevelColor = (Color) selected.getColors().get(2);
				lowColorPanel.setBackground(lowLevelColor);
				mediumColorPanel.setBackground(mediumLevelColor);
				highColorPanel.setBackground(highLevelColor);
				Double temp = (Double) selected.getBounds().get(0);
				lowUpper = temp.doubleValue();
				lowUpperField.setText(lowUpper + "");
				temp = (Double) selected.getBounds().get(1);
				mediumUpper = temp.doubleValue();
				mediumUpperField.setText(mediumUpper + "");
			}

		}
	}

	/**
	 * Takes as input the timesort (seconds, minutes etc.) and returns the
	 * number through which the measurements have to be divided to get the time
	 * in the right timesort (normally time is measured in milliseconds)
	 * 
	 * @param time
	 *            String: the timesort
	 * @return long
	 */
	private long determineTimeNumber(String time) {
		if (time.equals("milliseconds")) {
			return 1;
		} else if (time.equals("seconds")) {
			return 1000;
		} else if (time.equals("minutes")) {
			return (60 * 1000L);
		} else if (time.equals("hours")) {
			return (60 * 60 * 1000L);
		} else if (time.equals("days")) {
			return (24 * 60 * 60 * 1000L);
		} else if (time.equals("weeks")) {
			return (7 * 24 * 60 * 60 * 1000L);
		} else if (time.equals("months")) {
			// assume 30 days in a month
			return (30 * 24 * 60 * 60 * 1000L);
		} else if (time.equals("years")) {
			// assume 365 days in a year
			return (365 * 24 * 60 * 60 * 1000L);
		} else {
			// incorrect input, assume milliseconds
			return 1;
		}
	}

	/**
	 * Closes all advanced settings frames that were opened from this
	 * performance configuration
	 */
	public void closeAdvancedFrames() {
		try {
			// close all advanced settings screens
			Iterator it = advancedSettingsFrames.iterator();
			while (it.hasNext()) {
				JInternalFrame frame = (JInternalFrame) it.next();
				frame.doDefaultCloseAction();
			}
			advancedSettingsFrames.clear();
		} catch (Exception ex) {
		}
	}

	// ///////////////// GET SET AND ADD METHODS
	// /////////////////////////////////

	/**
	 * Adds another result object to the list of analysis results held by this
	 * category. This only used for top level categories as they get a seperate
	 * tab in the results frame, and need to initialize their GUI classes with
	 * the analysis results obtained.
	 * 
	 * @param result
	 *            the new result object
	 */
	public synchronized void addAnalysisResult(AnalysisResult result) {
		myResultObjects.add(result);
	}

	/**
	 * Assigns the GUI containing the results for that main analysis category.
	 * It serves as an intermediate storage to be packed genericly in the
	 * {@link AnalyisResults AnalysisResults} class.
	 * 
	 * @param panel
	 *            the results panel
	 */
	public void setResultPanel(AnalysisGUI panel) {
		myResultPanel = panel;
	}

	/**
	 * Gets the results needed to initialize the GUI for this top level
	 * category.
	 * 
	 * @return a list of AnalysisResult objects
	 */
	public synchronized HashSet getResultObjects() {
		return myResultObjects;
	}

	/**
	 * Sets the actual result contents of the panel representing the belonging
	 * kind of analysis perspective (only used for man categories, that is,
	 * top-level objects in the tree). It serves as an intermediate storage to
	 * be packed genericly in the {@link AnalyisResults AnalysisResults} class.
	 * 
	 * @see #setResultPanel
	 * @return the result panel if assigned before, an empty JPanel otherwise
	 */
	public JPanel getResultPanel() {
		if (myResultPanel == null) {
			return new JPanel();
		} else {
			return myResultPanel;
		}
	}

	/**
	 * Returns the number of
	 * 
	 * @return long
	 */
	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	/**
	 * Returns the timesort in which time metrics should be displayed
	 * 
	 * @return String
	 */
	public String getTimesort() {
		return timeSort;
	}

	/**
	 * Returns the timeDivider corresponding to the timeSort, that is, the
	 * number through which milliseconds have to be divided to get the right
	 * timesort
	 * 
	 * @return long
	 */
	public long getTimeDivider() {
		return timeDivider;
	}

	/**
	 * Retrieves the list of bounds between waiting time levels, starting with
	 * the lowest bound
	 * 
	 * @return ArrayList
	 */
	public ArrayList getBoundaries() {
		ArrayList tempList = new ArrayList();
		tempList.add(Double.valueOf(lowUpper));
		tempList.add(Double.valueOf(mediumUpper));
		return tempList;
	}

	/**
	 * Retrieves the list of colors used (starting with lowest waiting time
	 * level)
	 * 
	 * @return ArrayList
	 */
	public ArrayList getColors() {
		ArrayList tempList = new ArrayList();
		tempList.add(lowLevelColor);
		tempList.add(mediumLevelColor);
		tempList.add(highLevelColor);
		return tempList;
	}

	/**
	 * Returns an array containing the advanced settings
	 * 
	 * @return int[]
	 */
	public int[] getAdvancedSettings() {
		return advancedSettings;
	}

	/**
	 * Returns true if manual settings have been selected, else (use
	 * auto-settings) false.
	 * 
	 * @return boolean
	 */
	public boolean getManualSelected() {
		if (ownButton.isSelected()) {
			return (true);
		} else {
			return (false);
		}
	}

	/**
	 * Returns true if the radioButton allPlacesButton has been selected, false
	 * if thisPlaceButton has been selected instead.
	 * 
	 * @return boolean
	 */
	public boolean getAllSelected() {
		if (allPlacesButton.isSelected()) {
			return (true);
		} else {
			return (false);
		}
	}

	/**
	 * returns the selected place
	 * 
	 * @return ExtendedPlace
	 */
	public ExtendedPlace getSelectedPlace() {
		return (selectedPlace);
	}

	/**
	 * Returns this class
	 * 
	 * @return PerformanceConfiguration
	 */
	private PerformanceConfiguration getConfiguration() {
		return this;
	}

	/**
	 * adjusts the advanced settings and closes all advanced settings frames
	 * 
	 * @param settings
	 *            int[]: the settings to which the advanced settings are to be
	 *            set
	 */
	public void setAdvancedSettings(int[] settings) {
		// set settings
		advancedSettings = settings;
	}
}
