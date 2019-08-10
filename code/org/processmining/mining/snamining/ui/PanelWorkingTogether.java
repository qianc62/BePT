package org.processmining.mining.snamining.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class PanelWorkingTogether extends JPanel {

	private GridBagLayout gridBagLayout3 = new GridBagLayout();
	private JRadioButton wtSimultaneousAppearance = new JRadioButton();
	private JRadioButton wtDistanceWithCausality = new JRadioButton();
	private JRadioButton wtDistanceWithoutCausality = new JRadioButton();

	public PanelWorkingTogether() {
		init();
	}

	private void jbInit() throws Exception {

	}

	private void init() {

		ButtonGroup workingTogetherGroup = new ButtonGroup();

		this.setLayout(gridBagLayout3);
		wtSimultaneousAppearance.setText("Simultaneous appearance ratio");
		wtDistanceWithCausality.setText("Consider distance with causality");
		wtDistanceWithoutCausality
				.setText("Consider distance without causality (beta=0.5)");
		wtSimultaneousAppearance.setSelected(true);
		this.add(wtSimultaneousAppearance, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(wtDistanceWithCausality, new GridBagConstraints(0, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(wtDistanceWithoutCausality, new GridBagConstraints(0, 2, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		workingTogetherGroup.add(wtSimultaneousAppearance);
		workingTogetherGroup.add(wtDistanceWithCausality);
		workingTogetherGroup.add(wtDistanceWithoutCausality);

	}

	public boolean getDistanceWithCausality() {
		return wtDistanceWithCausality.isSelected();
	}

	public boolean getSimultaneousAppearance() {
		return wtSimultaneousAppearance.isSelected();
	}

	public boolean getDistanceWithoutCausality() {
		return wtDistanceWithoutCausality.isSelected();
	}

}
