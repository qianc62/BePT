package org.processmining.mining.fsm;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;

import java.awt.BorderLayout;
import javax.swing.border.TitledBorder;
import javax.swing.SpinnerListModel;
import javax.swing.ComboBoxEditor;

/**
 * <p>
 * Title: FsmHorizonGui
 * </p>
 * 
 * <p>
 * Description: GUI for changing a horizon setting.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 * 
 *          Code rating: Read
 * 
 *          Review rating: Red
 */
public class FsmHorizonGui extends JPanel {

	// Check box to indicate whether these settings should be used.
	private JCheckBox useCheckBox;
	// Spinner for the number of steps to take.
	private JSpinner horizonSpinner;
	// Combo box for selecting the abstraction.
	private JComboBox abstractionComboBox;
	// List for the filter.
	private JList filterList;
	// Spinner for the numbe rof filtered steps to take.
	private JSpinner filteredHorizonSpinner;

	/**
	 * Creates the GUI.
	 * 
	 * @param settings
	 *            FsmHorizonSettings the settings to display.
	 * @param tabbedPane
	 *            JTabbedPane the parent.
	 * @param title
	 *            String the title.
	 */
	public FsmHorizonGui(FsmHorizonSettings settings, JTabbedPane tabbedPane,
			String title) {
		String[] spinnerStrings = new String[101];
		spinnerStrings[0] = "Infinite horizon";
		for (int i = 1; i < 101; i++) {
			spinnerStrings[i] = String.valueOf(i);
		}

		// Create a main panel and add it to the tabbed pane.
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		panel.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		tabbedPane.addTab(title, panel);

		Object abstractions[] = { "Off", "Sequence", "Multiset", "Set" };
		abstractionComboBox = new JComboBox(abstractions);
		abstractionComboBox
				.setBorder(new TitledBorder("Select an abstraction"));
		abstractionComboBox.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		abstractionComboBox.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		abstractionComboBox.setEditable(true); // Turn on the Editor (to get
		// white background)
		abstractionComboBox.getEditor().getEditorComponent().setBackground(
				FsmGuiColorScheme.FIELDBACKGROUNDCOLOR);
		abstractionComboBox.getEditor().getEditorComponent()
				.setFocusable(false); // But denied focus (thus, user can only
		// select from dropdown)
		panel.add("North", abstractionComboBox);

		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		southPanel.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		panel.add("Center", southPanel);

		// Create a filter panel.
		JPanel filteredPanel = new JPanel(new BorderLayout());
		filteredPanel.setBorder(new TitledBorder(
				"Only filtered items will count for the filtered horizon"));
		filteredPanel.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		filteredPanel.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		southPanel.add("Center", filteredPanel);

		horizonSpinner = new JSpinner(new SpinnerListModel(spinnerStrings));
		horizonSpinner.setBorder(new TitledBorder("Select a general horizon"));
		horizonSpinner.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		horizonSpinner.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) horizonSpinner
				.getEditor();
		editor.getTextField().setBackground(
				FsmGuiColorScheme.FIELDBACKGROUNDCOLOR);
		editor.getTextField().setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		southPanel.add("North", horizonSpinner);

		filteredHorizonSpinner = new JSpinner(new SpinnerListModel(
				spinnerStrings));
		filteredHorizonSpinner.setBorder(new TitledBorder(
				"Select a filtered horizon"));
		filteredHorizonSpinner.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		filteredHorizonSpinner.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		editor = (JSpinner.DefaultEditor) filteredHorizonSpinner.getEditor();
		editor.getTextField().setBackground(
				FsmGuiColorScheme.FIELDBACKGROUNDCOLOR);
		editor.getTextField().setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		filteredPanel.add("North", filteredHorizonSpinner);

		// Add the filter list to the center of the main panel.
		Object elements[] = settings.getFilterArray();
		filterList = new JList(elements);
		filterList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		filterList.setVisibleRowCount(-1);
		filterList.setSelectionInterval(0, elements.length - 1);
		filterList.setBackground(FsmGuiColorScheme.FIELDBACKGROUNDCOLOR);
		filterList.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		JPanel filterPanel = new JPanel(new BorderLayout());
		filterPanel.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		filterPanel.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		filterPanel.setBorder(new TitledBorder("Select filtered items"));
		filterPanel.add("Center", filterList);
		filteredPanel.add("Center", filterPanel);
	}

	/**
	 * Gets the settings from the GUI.
	 * 
	 * @return FsmHorizonSettings
	 */
	public FsmHorizonSettings GetSettings() {
		FsmHorizonSettings settings = new FsmHorizonSettings();
		String value;

		settings.setUse(abstractionComboBox.getSelectedIndex() > 0);
		value = horizonSpinner.getValue().toString();
		if (value.equals("Infinite horizon")) {
			value = "-1";
		}
		settings.setHorizon(Integer.valueOf(value));
		// Abstraction count starts at 1.
		settings.setAbstraction(abstractionComboBox.getSelectedIndex());
		settings.clearFilter();
		settings.addAllToFilter(filterList.getSelectedValues());
		value = filteredHorizonSpinner.getValue().toString();
		if (value.equals("Infinite horizon")) {
			value = "-1";
		}
		settings.setFilteredHorizon(Integer.valueOf(value));

		return settings;
	}
}
