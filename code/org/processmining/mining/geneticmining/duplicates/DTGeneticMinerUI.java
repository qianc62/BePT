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

package org.processmining.mining.geneticmining.duplicates;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.framework.log.LogSummary;
import org.processmining.framework.util.GUIPropertyDouble;
import org.processmining.framework.util.ToolTipComboBox;
import org.processmining.mining.geneticmining.GeneticMinerSettings;
import org.processmining.mining.geneticmining.fitness.duplicates.DTFitnessFactory;
import org.processmining.mining.geneticmining.geneticoperations.duplicates.DTCrossoverFactory;
import org.processmining.mining.geneticmining.geneticoperations.duplicates.DTMutationFactory;
import org.processmining.mining.geneticmining.population.duplicates.DTInitialPopulationFactory;
import org.processmining.mining.geneticmining.selection.duplicates.DTSelectionMethodFactory;
import org.processmining.mining.geneticmining.util.ParameterValue;

/**
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class DTGeneticMinerUI extends JPanel {

	private JPanel mainPanel = new JPanel();
	private JPanel genOpPanel = new JPanel();
	private JPanel usingGenOpPanel;
	private JPanel notUsingGenOpPanel;

	private JPanel fitnessPanel = new JPanel();
	private JCheckBox showAdvancedFitnessFeatures = new JCheckBox();
	private JPanel showingAdvancedFitnessFeatures;
	private JPanel notShowingAdvancedFitnessFeatures;
	private GUIPropertyDouble[] advancedFitnessFeaturesProperties;

	private JLabel initialPopulationTypeLabel = new JLabel();
	private ToolTipComboBox initialPopulationType = new ToolTipComboBox(
			DTInitialPopulationFactory.getInitialPopulationTypes());
	private JPanel otherOptionsPanel = new JPanel();
	private JPanel geneticOptionsPanel = new JPanel();

	private JCheckBox useGenOp = new JCheckBox();

	private JLabel popSizeLabel = new JLabel();
	private JTextField popSize = new JTextField();
	private JLabel maxGenerationLabel = new JLabel();
	private JTextField maxGeneration = new JTextField();
	private JLabel elitismLabel = new JLabel();
	private JTextField elitismRate = new JTextField();
	private JLabel seedLabel = new JLabel();
	private JTextField seed = new JTextField();
	// private JCheckBox properCompletion = new JCheckBox();
	// private JCheckBox useContinuousSemantics = new JCheckBox();

	private JLabel fitnessTypeLabel = new JLabel();
	private ToolTipComboBox fitnessType = new ToolTipComboBox(DTFitnessFactory
			.getAllFitnessTypes());

	private JTextField crossoverRate = new JTextField();
	private JLabel crossoverLabel = new JLabel();
	private JLabel selectionMethodTypeLabel = new JLabel();
	private ToolTipComboBox selectionMethodType = new ToolTipComboBox(
			DTSelectionMethodFactory.getAllSelectionMethodsTypes());
	private JLabel crossoverTypeLabel = new JLabel();
	private ToolTipComboBox crossoverType = new ToolTipComboBox(
			DTCrossoverFactory.getAllCrossoverTypes());
	private JLabel mutationTypeLabel = new JLabel();
	private ToolTipComboBox mutationType = new ToolTipComboBox(
			DTMutationFactory.getAllMutationTypes());
	private JTextField mutationRate = new JTextField();
	private JLabel mutationLabel = new JLabel();

	private JTextField power = new JTextField();
	private JLabel powerLabel = new JLabel();

	public DTGeneticMinerUI(LogSummary summary) {

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public GeneticMinerSettings getSettings() {
		return new GeneticMinerSettings(getPopulationSize(),
				getMaxNumGenerations(), getMutationRate(), getCrossoverType(),
				getCrossoverRate(), getSeed(), getFitnessType(),
				getSelectionMethodType(), getElitismRate(), getMutationType(),
				getPower(), getInitialPopulationType(),
				getUseGeneticOperations(), getFitnessParametersValues());
	}

	private double[] getFitnessParametersValues() {
		if (advancedFitnessFeaturesProperties == null) {
			updateTheAdvanceFitnessFeaturesComponent();
		}
		double[] fitnessParameterValues = new double[advancedFitnessFeaturesProperties.length];
		for (int i = 0; i < fitnessParameterValues.length; i++) {
			fitnessParameterValues[i] = advancedFitnessFeaturesProperties[i]
					.getValue();
		}
		return fitnessParameterValues;
	}

	private long getSeed() {
		try {
			Long.parseLong(seed.getText());
		} catch (Exception e) {
			seed.setText(Long.toString(DTGeneticMinerConstants.SEED));
		}

		return Long.parseLong(seed.getText());
	}

	private int getPopulationSize() {
		try {
			if (Integer.parseInt(popSize.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			popSize.setText(Integer
					.toString(DTGeneticMinerConstants.POPULATION_SIZE));
		}

		return Integer.parseInt(popSize.getText());
	}

	private int getMaxNumGenerations() {
		try {
			if (Integer.parseInt(maxGeneration.getText()) < 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			maxGeneration.setText(Integer
					.toString(DTGeneticMinerConstants.MAX_GENERATION));
		}

		return Integer.parseInt(maxGeneration.getText());

	}

	private double getElitismRate() {
		double e = DTGeneticMinerConstants.ELITISM_RATE;
		try {
			e = Double.parseDouble(mutationRate.getText());
			if (e > 1.0) {
				e = 1.0;
				throw new NumberFormatException();
			}
		} catch (NumberFormatException nfe) {
			elitismRate.setText(Double.toString(e));
		}

		return Double.parseDouble(elitismRate.getText());
	}

	private double getMutationRate() {
		double m = DTGeneticMinerConstants.MUTATION_RATE;
		try {
			m = Double.parseDouble(mutationRate.getText());
			if (m > 1.0) {
				m = 1.0;
				throw new NumberFormatException();
			}
		} catch (NumberFormatException nfe) {
			mutationRate.setText(Double.toString(m));
		}

		return m;
	}

	private long getPower() {
		long p = DTGeneticMinerConstants.POWER;
		try {
			p = Long.parseLong(power.getText());
		} catch (NumberFormatException nfe) {
			power.setText(Long.toString(p));
		}

		if ((p % 2) == 0) {
			p += 1; // making the power odd!
			power.setText(Long.toString(p));
		}

		return p;
	}

	private int getInitialPopulationType() {
		return initialPopulationType.getSelectedIndex();
	}

	private int getCrossoverType() {
		return crossoverType.getSelectedIndex();
	}

	private int getMutationType() {
		return mutationType.getSelectedIndex();
	}

	private int getSelectionMethodType() {
		return selectionMethodType.getSelectedIndex();
	}

	private double getCrossoverRate() {
		double d = DTGeneticMinerConstants.CROSSOVER_RATE;

		try {
			d = Double.parseDouble(crossoverRate.getText());
			if (d > 1.0) {
				d = 1.0;
				throw new NumberFormatException();
			}
		} catch (NumberFormatException nfe) {
			crossoverRate.setText(Double.toString(d));
		}

		return Double.parseDouble(crossoverRate.getText());
	}

	private boolean getUseGeneticOperations() {
		return useGenOp.isSelected();
	}

	private int getFitnessType() {
		return fitnessType.getSelectedIndex();
	}

	private void jbInit() throws Exception {

		int x = 0;
		int y = 0;

		// ---- options for genetic operations
		// -----------------------------------------
		selectionMethodTypeLabel.setText("Selection method type");
		// selectionMethodTypeLabel.setToolTipText("Sets the kind of tournament to be used while selecting parents to undergo crossover and mutation.");

		selectionMethodType
				.setSelectedIndex(DTGeneticMinerConstants.SELECTION_TYPE);
		crossoverLabel.setText("Crossover rate");
		crossoverRate.setPreferredSize(new Dimension(50, 21));
		crossoverRate.setText(Double
				.toString(DTGeneticMinerConstants.CROSSOVER_RATE));
		crossoverTypeLabel.setText("Crossover type");
		crossoverType.setSelectedIndex(DTGeneticMinerConstants.CROSSOVER_TYPE);
		mutationTypeLabel.setText("Mutation type");
		mutationLabel.setText("Mutation rate");
		mutationRate.setPreferredSize(new Dimension(50, 21));
		mutationRate.setText(Double
				.toString(DTGeneticMinerConstants.MUTATION_RATE));
		mutationType.setSelectedIndex(DTGeneticMinerConstants.MUTATION_TYPE);

		usingGenOpPanel = new JPanel(new GridBagLayout());

		usingGenOpPanel.add(selectionMethodTypeLabel, new GridBagConstraints(0,
				x + y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 33, 0, 0), 0, 0));
		usingGenOpPanel.add(selectionMethodType, new GridBagConstraints(1, x
				+ y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));

		usingGenOpPanel.add(crossoverTypeLabel, new GridBagConstraints(0, x
				+ (++y), 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 33, 0, 0), 0, 0));
		usingGenOpPanel.add(crossoverType, new GridBagConstraints(1, x + y, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));
		usingGenOpPanel.add(crossoverLabel, new GridBagConstraints(0,
				x + (++y), 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 33, 0, 0), 0, 0));
		usingGenOpPanel.add(crossoverRate, new GridBagConstraints(1, x + y, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));

		usingGenOpPanel.add(mutationTypeLabel, new GridBagConstraints(0, x
				+ (++y), 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 33, 0, 0), 0, 0));

		usingGenOpPanel.add(mutationType, new GridBagConstraints(1, x + y, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));

		usingGenOpPanel.add(mutationLabel, new GridBagConstraints(0, x + (++y),
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 33, 0, 0), 0, 0));
		usingGenOpPanel.add(mutationRate, new GridBagConstraints(1, x + y, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));

		notUsingGenOpPanel = new JPanel(new GridBagLayout());
		useGenOp.setText("Use genetic operators");
		useGenOp.setSelected(true);

		useGenOp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				usingGenOpPanel.setVisible(useGenOp.isSelected());
				notUsingGenOpPanel.setVisible(!useGenOp.isSelected());
			}
		});

		genOpPanel.setLayout(new BorderLayout());
		genOpPanel.add(useGenOp, BorderLayout.NORTH);
		genOpPanel.add(usingGenOpPanel, BorderLayout.CENTER);
		genOpPanel.add(notUsingGenOpPanel, BorderLayout.SOUTH);
		notUsingGenOpPanel.setVisible(false);

		// ----- Options for the fitness measure -----//

		showAdvancedFitnessFeatures.setText("Show Advanced Fitness Parameters");
		showAdvancedFitnessFeatures.setSelected(false);

		showingAdvancedFitnessFeatures = new JPanel(new GridLayout(0, 1));
		showingAdvancedFitnessFeatures.setVisible(showAdvancedFitnessFeatures
				.isSelected());

		notShowingAdvancedFitnessFeatures = new JPanel(new GridBagLayout());
		notShowingAdvancedFitnessFeatures
				.setVisible(!showAdvancedFitnessFeatures.isSelected());

		showAdvancedFitnessFeatures.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (showAdvancedFitnessFeatures.isSelected()) {
					updateTheAdvanceFitnessFeaturesComponent();
				}
				showingAdvancedFitnessFeatures
						.setVisible(showAdvancedFitnessFeatures.isSelected());
				notShowingAdvancedFitnessFeatures
						.setVisible(!showAdvancedFitnessFeatures.isSelected());
				fitnessPanel.updateUI();
			}
		});

		fitnessPanel.setLayout(new BorderLayout());
		fitnessPanel.add(showAdvancedFitnessFeatures, BorderLayout.NORTH);
		fitnessPanel.add(showingAdvancedFitnessFeatures, BorderLayout.CENTER);
		fitnessPanel.add(notShowingAdvancedFitnessFeatures, BorderLayout.SOUTH);

		// ------ other options
		// --------------------------------------------------------
		popSizeLabel.setText("Population size");
		popSize.setPreferredSize(new Dimension(50, 21));
		popSize.setText(Integer
				.toString(DTGeneticMinerConstants.POPULATION_SIZE));
		initialPopulationTypeLabel.setText("Initial population type");
		initialPopulationType
				.setSelectedIndex(DTGeneticMinerConstants.INITIAL_POPULATION_TYPE);
		maxGenerationLabel.setText("Max number generations");
		maxGeneration.setPreferredSize(new Dimension(50, 21));
		maxGeneration.setText(Integer
				.toString(DTGeneticMinerConstants.MAX_GENERATION));
		seedLabel.setText("Seed");
		seed.setPreferredSize(new Dimension(50, 21));
		seed.setText(Long.toString(DTGeneticMinerConstants.SEED));
		powerLabel.setText("Power value");
		power.setPreferredSize(new Dimension(50, 21));
		power.setText(Long.toString(DTGeneticMinerConstants.POWER));

		fitnessTypeLabel.setText("Fitness type");
		fitnessType.setSelectedIndex(DTGeneticMinerConstants.FITNESS_TYPE);

		fitnessType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (DTFitnessFactory
						.getAdvancedFitnessParametersNames(fitnessType
								.getSelectedIndex()).length > 0) {
					fitnessPanel.setVisible(true);
				} else {
					fitnessPanel.setVisible(false);
				}
			}
		});

		elitismLabel.setText("Elitism rate");
		elitismRate.setPreferredSize(new Dimension(50, 21));
		elitismRate.setText(Double
				.toString(DTGeneticMinerConstants.ELITISM_RATE));

		otherOptionsPanel.setLayout(new GridBagLayout());
		y = 0;

		// generation size
		otherOptionsPanel.add(popSizeLabel, new GridBagConstraints(0, x + y, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 5, 0, 0), 0, 0));
		otherOptionsPanel.add(popSize, new GridBagConstraints(1, x + y, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));

		// initial population type
		otherOptionsPanel.add(initialPopulationTypeLabel,
				new GridBagConstraints(0, x + (++y), 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 5, 0, 0), 0, 0));
		otherOptionsPanel.add(initialPopulationType, new GridBagConstraints(1,
				x + y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));

		// max runs
		otherOptionsPanel.add(maxGenerationLabel, new GridBagConstraints(0, x
				+ (++y), 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
		otherOptionsPanel.add(maxGeneration, new GridBagConstraints(1, x + y,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
		// seed
		otherOptionsPanel.add(seedLabel, new GridBagConstraints(0, x + (++y),
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
		otherOptionsPanel.add(seed, new GridBagConstraints(1, x + y, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));

		// power
		otherOptionsPanel.add(powerLabel, new GridBagConstraints(0, x + (++y),
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
		otherOptionsPanel.add(power, new GridBagConstraints(1, x + y, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));

		// elitism rate
		otherOptionsPanel.add(elitismLabel, new GridBagConstraints(0,
				x + (++y), 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
		otherOptionsPanel.add(elitismRate, new GridBagConstraints(1, x + y, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));

		// firing semantics

		otherOptionsPanel.add(fitnessTypeLabel, new GridBagConstraints(0, x
				+ (++y), 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
		otherOptionsPanel.add(fitnessType, new GridBagConstraints(1, x + y, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));

		// ------ putting main panel together
		// ------------------------------------------

		geneticOptionsPanel.setLayout(new GridBagLayout());
		y = 0;
		geneticOptionsPanel.add(otherOptionsPanel, new GridBagConstraints(0, x
				+ y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		geneticOptionsPanel.add(fitnessPanel, new GridBagConstraints(0, x
				+ (++y), 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		geneticOptionsPanel.add(genOpPanel, new GridBagConstraints(0,
				x + (++y), 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		mainPanel.add(geneticOptionsPanel, null);

		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.NORTH);
	}

	private void updateTheAdvanceFitnessFeaturesComponent() {

		ParameterValue[] parameterNames = DTFitnessFactory
				.getAdvancedFitnessParametersNames(fitnessType
						.getSelectedIndex());
		// we need to update the parameter list
		if (advancedFitnessFeaturesProperties == null
				|| advancedFitnessFeaturesProperties.length != parameterNames.length) {
			advancedFitnessFeaturesProperties = new GUIPropertyDouble[parameterNames.length];
			for (int i = 0; i < parameterNames.length; i++) {
				GUIPropertyDouble dp = new GUIPropertyDouble(parameterNames[i]
						.getParameter(), parameterNames[i].getDescription(),
						parameterNames[i].getDefaultValue(), parameterNames[i]
								.getMinValue(),
						parameterNames[i].getMaxValue(), parameterNames[i]
								.getStepSize());
				advancedFitnessFeaturesProperties[i] = dp;
			}
		}

		// inserting the parameter list in the interface
		showingAdvancedFitnessFeatures.removeAll();
		for (int i = 0; i < parameterNames.length; i++) {
			showingAdvancedFitnessFeatures
					.add(advancedFitnessFeaturesProperties[i]
							.getPropertyPanel());
		}

	}
}
