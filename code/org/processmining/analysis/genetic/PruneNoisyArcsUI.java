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

package org.processmining.analysis.genetic;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.ToolTipComboBox;
import org.processmining.mining.geneticmining.fitness.Fitness;
import org.processmining.mining.geneticmining.fitness.FitnessFactory;
import org.processmining.mining.geneticmining.fitness.duplicates.DTFitnessFactory;
import org.processmining.mining.heuristicsmining.HeuristicsNetResult;

public class PruneNoisyArcsUI extends JPanel implements Provider {

	private HeuristicsNet originalNet = null;
	private HeuristicsNet currentNet = null;
	private LogReader log = null;

	private HeuristicsNetResult netResult = null;
	private JPanel fitnessAndNoisePercentageSpace = null;

	private ToolTipComboBox fitnessType = null;
	private JButton prune = null;
	SpinnerModel model = null;
	private JButton showUnprunedNet = null;

	PruneNoisyArcsUI panelObject = null;

	public PruneNoisyArcsUI(LogReader log, HeuristicsNet net) {
		this.originalNet = net;
		this.currentNet = this.originalNet;
		this.log = log;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void jbInit() throws Exception {

		JLabel ftLabel = new JLabel("Type of Fitness ");
		netResult = new HeuristicsNetResult(originalNet, log);
		fitnessAndNoisePercentageSpace = new JPanel();

		fitnessType = new ToolTipComboBox(FitnessFactory.getAllFitnessTypes());

		JLabel pruneLabel = new JLabel(" Arc pruning in % ");
		model = new SpinnerNumberModel(5.0, 0.0, 100.0, 1.0);
		JSpinner pruningPercentage = new JSpinner(model);

		prune = new JButton("Prune");
		showUnprunedNet = new JButton("Show Unpruned Individual");

		fitnessAndNoisePercentageSpace.add(ftLabel);
		fitnessAndNoisePercentageSpace.add(fitnessType);
		fitnessAndNoisePercentageSpace.add(pruneLabel);
		fitnessAndNoisePercentageSpace.add(pruningPercentage);
		fitnessAndNoisePercentageSpace.add(prune);
		fitnessAndNoisePercentageSpace.add(showUnprunedNet);

		panelObject = this;

		prune.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Fitness fitness = FitnessFactory.getFitness(fitnessType
						.getSelectedIndex(), log,
						DTFitnessFactory.ALL_FITNESS_PARAMETERS);
				currentNet = fitness
						.calculate(new HeuristicsNet[] { originalNet.copyNet() })[0];

				currentNet.disconnectArcsUsedBelowThreshold(PruneNoisyArcs
						.calculatePruningThreshold(currentNet, ((Double) model
								.getValue()).doubleValue()));

				panelObject.removeAll();
				netResult = new HeuristicsNetResult(currentNet, log, netResult
						.getShowSplitJoinSemantics());
				panelObject.add(netResult.getVisualization(),
						BorderLayout.CENTER);
				panelObject.add(fitnessAndNoisePercentageSpace,
						BorderLayout.SOUTH);
				panelObject.repaint();
				panelObject.validate();

				Message.add("<PruneArcs numberOfUsedArcs=\""
						+ currentNet.getArcUsage().cardinality() + "\">",
						Message.TEST);

			}
		});

		showUnprunedNet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Fitness fitness = FitnessFactory.getFitness(fitnessType
						.getSelectedIndex(), log,
						DTFitnessFactory.ALL_FITNESS_PARAMETERS);
				currentNet = fitness
						.calculate(new HeuristicsNet[] { originalNet })[0];

				// JOptionPane.showMessageDialog(MainUI.getInstance(),
				// "The fitness  for this individual is " +
				// result[0].getFitness(),
				// "Fitness - " +
				// FitnessFactory.getAllFitnessTypes()[fitnessType.getSelectedIndex()],
				// JOptionPane.INFORMATION_MESSAGE);

				panelObject.removeAll();
				netResult = new HeuristicsNetResult(currentNet, log, netResult
						.getShowSplitJoinSemantics());
				panelObject.add(netResult.getVisualization(),
						BorderLayout.CENTER);
				panelObject.add(fitnessAndNoisePercentageSpace,
						BorderLayout.SOUTH);
				panelObject.repaint();
				panelObject.validate();

				Message.add("<PruneArcs numberOfUsedArcs=\""
						+ currentNet.getArcUsage().cardinality() + "\">",
						Message.TEST);

			}
		});

		this.setLayout(new BorderLayout());
		this.add(netResult.getVisualization(), BorderLayout.CENTER);
		this.add(fitnessAndNoisePercentageSpace, BorderLayout.SOUTH);

	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = new ProvidedObject[] { new ProvidedObject(
				"Heuristics Net", new Object[] { currentNet, log }) };
		return objects;
	}

}
