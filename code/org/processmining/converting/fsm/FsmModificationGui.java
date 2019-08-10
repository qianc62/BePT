package org.processmining.converting.fsm;

import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JTabbedPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import javax.swing.border.TitledBorder;
import org.processmining.mining.fsm.FsmGuiColorScheme;

/**
 * <p>
 * Title: FsmModificationGui
 * </p>
 * 
 * <p>
 * Description: GUI for the FSM Modification plug-in
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class FsmModificationGui extends JPanel {
	private JCheckBox useKillSelfLoopsCheckBox;
	private JCheckBox useExtendCheckBox;
	private JCheckBox useMergeByOutputCheckBox;
	private JCheckBox useMergeByInputCheckBox;

	public FsmModificationGui(FsmModificationSettings settings,
			JTabbedPane tabbedPane, String title) {
		GridBagLayout layout = new GridBagLayout();
		JPanel panel = new JPanel(layout);
		panel.setBackground(FsmGuiColorScheme.FIELDBACKGROUNDCOLOR);
		panel.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add("Center", panel);
		panel2.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		panel2.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add("North", panel2);
		mainPanel.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		mainPanel.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		tabbedPane.addTab(title, mainPanel);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;

		useKillSelfLoopsCheckBox = new JCheckBox("Remove self loops", settings
				.getUse(FsmModificationSettings.KILLSELFLOOPSTRATEGY));
		useKillSelfLoopsCheckBox
				.setBackground(FsmGuiColorScheme.FIELDBACKGROUNDCOLOR);
		useKillSelfLoopsCheckBox
				.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		constraints.gridy = 0;
		constraints.anchor = constraints.WEST;
		layout.setConstraints(useKillSelfLoopsCheckBox, constraints);
		panel.add(useKillSelfLoopsCheckBox);

		useExtendCheckBox = new JCheckBox("Improve diamond structure", settings
				.getUse(FsmModificationSettings.EXTENDSTRATEGY));
		useExtendCheckBox.setBackground(FsmGuiColorScheme.FIELDBACKGROUNDCOLOR);
		useExtendCheckBox.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		constraints.gridy = 1;
		layout.setConstraints(useExtendCheckBox, constraints);
		panel.add(useExtendCheckBox);

		useMergeByOutputCheckBox = new JCheckBox(
				"Merge states with identical outputs", settings
						.getUse(FsmModificationSettings.MERGYBYOUTPUTSTRATEGY));
		useMergeByOutputCheckBox
				.setBackground(FsmGuiColorScheme.FIELDBACKGROUNDCOLOR);
		useMergeByOutputCheckBox
				.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		constraints.gridy = 2;
		layout.setConstraints(useMergeByOutputCheckBox, constraints);
		panel.add(useMergeByOutputCheckBox);

		useMergeByInputCheckBox = new JCheckBox(
				"Merge states with identical inputs", settings
						.getUse(FsmModificationSettings.MERGYBYINPUTSTRATEGY));
		useMergeByInputCheckBox
				.setBackground(FsmGuiColorScheme.FIELDBACKGROUNDCOLOR);
		useMergeByInputCheckBox
				.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		constraints.gridy = 3;
		layout.setConstraints(useMergeByInputCheckBox, constraints);
		panel.add(useMergeByInputCheckBox);

		panel2.setBorder(new TitledBorder(
				"Select the appropriate post-mine FSM modifications"));
	}

	public FsmModificationSettings getSettings() {
		FsmModificationSettings settings = new FsmModificationSettings();
		settings.setUse(FsmModificationSettings.KILLSELFLOOPSTRATEGY,
				useKillSelfLoopsCheckBox.isSelected());
		settings.setUse(FsmModificationSettings.EXTENDSTRATEGY,
				useExtendCheckBox.isSelected());
		settings.setUse(FsmModificationSettings.MERGYBYOUTPUTSTRATEGY,
				useMergeByOutputCheckBox.isSelected());
		settings.setUse(FsmModificationSettings.MERGYBYINPUTSTRATEGY,
				useMergeByInputCheckBox.isSelected());
		return settings;
	}
}
