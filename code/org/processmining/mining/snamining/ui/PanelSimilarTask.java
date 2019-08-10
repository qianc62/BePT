package org.processmining.mining.snamining.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class PanelSimilarTask extends JPanel {

	private GridBagLayout gridBagLayout4 = new GridBagLayout();
	private JRadioButton stEuclidianDistance = new JRadioButton();
	private JRadioButton stCorrelationCoefficient = new JRadioButton();
	private JRadioButton stSimilarityCoefficient = new JRadioButton();
	private JRadioButton stHammingDistance = new JRadioButton();

	public PanelSimilarTask() {
		init();
	}

	private void jbInit() throws Exception {

	}

	private void init() {
		ButtonGroup similarTaskGroup = new ButtonGroup();

		// ----------- Similar task -----------------------------------

		this.setLayout(gridBagLayout4);
		stEuclidianDistance.setText("Euclidian distance");
		stCorrelationCoefficient.setText("Correlation coefficient");
		stSimilarityCoefficient.setText("Similarity coefficient");
		stHammingDistance.setText("Hamming distance");
		stEuclidianDistance.setSelected(true);
		this.add(stEuclidianDistance, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(stCorrelationCoefficient, new GridBagConstraints(0, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(stSimilarityCoefficient, new GridBagConstraints(0, 2, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(stHammingDistance, new GridBagConstraints(0, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		similarTaskGroup.add(stEuclidianDistance);
		similarTaskGroup.add(stCorrelationCoefficient);
		similarTaskGroup.add(stSimilarityCoefficient);
		similarTaskGroup.add(stHammingDistance);
	}

	public boolean getEuclidianDistance() {
		return stEuclidianDistance.isSelected();
	}

	public boolean getCorrelationCoefficient() {
		return stCorrelationCoefficient.isSelected();
	}

	public boolean getSimilarityCoefficient() {
		return stSimilarityCoefficient.isSelected();
	}
}
