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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.ToolTipComboBox;
import org.processmining.mining.geneticmining.fitness.Fitness;
import org.processmining.mining.geneticmining.fitness.FitnessFactory;
import org.processmining.mining.geneticmining.fitness.duplicates.DTFitnessFactory;
import org.processmining.mining.heuristicsmining.HeuristicsNetResult;

public class CalculateFitnessUI extends JPanel implements Provider {

	private HeuristicsNet net = null;
	private LogReader log = null;

	private HeuristicsNetResult netResult = null;
	private JPanel fitnessSpace = null;

	private ToolTipComboBox fitnessType = null;
	private JButton calculate = null;

	CalculateFitnessUI panelObject = null;

	public CalculateFitnessUI(LogReader log, HeuristicsNet net) {
		this.net = net;
		this.log = log;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void jbInit() throws Exception {

		JLabel ftLabel = new JLabel("Type of Fitness ");
		netResult = new HeuristicsNetResult(net, log);
		fitnessSpace = new JPanel();

		fitnessType = new ToolTipComboBox(FitnessFactory.getAllFitnessTypes());
		calculate = new JButton("Calculate");
		fitnessSpace.add(ftLabel);
		fitnessSpace.add(fitnessType);
		fitnessSpace.add(calculate);

		panelObject = this;

		calculate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Fitness fitness = FitnessFactory.getFitness(fitnessType
						.getSelectedIndex(), log,
						DTFitnessFactory.ALL_FITNESS_PARAMETERS);
				HeuristicsNet[] result = fitness
						.calculate(new HeuristicsNet[] { net });

				JOptionPane
						.showMessageDialog(
								MainUI.getInstance(),
								"The fitness  for this individual is "
										+ result[0].getFitness(),
								"Fitness - "
										+ FitnessFactory.getAllFitnessTypes()[fitnessType
												.getSelectedIndex()],
								JOptionPane.INFORMATION_MESSAGE);
				Message.add("<Fitness "
						+ FitnessFactory.getAllFitnessTypes()[fitnessType
								.getSelectedIndex()] + "=\""
						+ result[0].getFitness() + "\">", Message.TEST);

				panelObject.removeAll();
				netResult = new HeuristicsNetResult(net, log, netResult
						.getShowSplitJoinSemantics());
				panelObject.add(netResult.getVisualization(),
						BorderLayout.CENTER);
				panelObject.add(fitnessSpace, BorderLayout.SOUTH);
				panelObject.repaint();
				panelObject.validate();
			}
		});

		this.setLayout(new BorderLayout());
		this.add(netResult.getVisualization(), BorderLayout.CENTER);
		this.add(fitnessSpace, BorderLayout.SOUTH);

	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = new ProvidedObject[] { new ProvidedObject(
				"Heuristics Net", new Object[] { net, log }) };
		return objects;
	}

}
