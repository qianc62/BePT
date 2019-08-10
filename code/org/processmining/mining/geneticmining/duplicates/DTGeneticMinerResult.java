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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.DoubleClickTable;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningResult;
import org.processmining.mining.geneticmining.util.MethodsOverIndividuals;
import org.processmining.mining.heuristicsmining.HeuristicsNetResult;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Ana Karla A. de Medeiros.
 * @version 1.0
 */

public class DTGeneticMinerResult extends JPanel implements MiningResult,
		Provider {

	public HeuristicsNet[] population = null;

	private LogReader log = null;

	private JTable table = null;
	private JPanel graphPanel = null;
	private JCheckBox showSplitJoinSemantics = null;

	private HeuristicsNetResult result = null;

	public DTGeneticMinerResult(HeuristicsNet[] pop, LogReader log) {
		this.population = pop;
		this.log = log;

		MethodsOverIndividuals.decreasinglyOrderPopulation(population);

		population = MethodsOverIndividuals.removeDuplicates(population);
		try {
			jbInit();
			// if the table has a single element, we already show it!
			if (table.getRowCount() == 1) {
				table.changeSelection(0, 0, false, false);
				updateGraph();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Message.add("<GeneticAlgorithmPlugins nofIndividuals=\""
				+ population.length + "\" bestFitness=\""
				+ population[0].getFitness() + "\" worstFitness=\""
				+ population[population.length - 1].getFitness() + "\">",
				Message.TEST);
	}

	public JComponent getVisualization() {
		return this;
	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = new ProvidedObject[table
				.getSelectedRowCount()];
		if (table.getSelectedRowCount() > 0) {

			for (int i = 0; i < table.getSelectedRows().length; i++) {
				int j = table.getSelectedRows()[i];
				objects[i] = new ProvidedObject("Individual " + j
						+ ", fitness " + population[j].getFitness(),
						new Object[] { population[j], log });
			}
		}
		return objects;
	}

	public LogReader getLogReader() {
		return log;
	}

	private void updateGraph() {

		if (table.getSelectedRowCount() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please select a process instance first.");
			return;
		}

		graphPanel.removeAll();
		result = new HeuristicsNetResult(population[table.getSelectedRow()],
				log, showSplitJoinSemantics.isSelected());
		// providedObjects = result.getProvidedObjects();

		graphPanel.add(result.getVisualization(), BorderLayout.CENTER);
		graphPanel.validate();
		graphPanel.repaint();

	}

	private void jbInit() throws Exception {
		JSplitPane splitPane;
		JPanel bottomPanel = new JPanel(new BorderLayout());
		JPanel buttonsPanel = new JPanel();
		JPanel messagePanel = new JPanel();
		JButton updateGraphButton = new JButton("Update graph");
		showSplitJoinSemantics = new JCheckBox("Display split/join semantics");

		messagePanel
				.add(new JLabel(
						"Please select one or more process instances and press 'Update graph'."));
		graphPanel = new JPanel(new BorderLayout());
		graphPanel.add(messagePanel, BorderLayout.CENTER);

		table = new DoubleClickTable(new IndividualTableModel(population),
				updateGraphButton);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		updateGraphButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateGraph();
			}
		});

		showSplitJoinSemantics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateGraph();
			}
		});

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(table), graphPanel);

		splitPane.setDividerLocation(250);
		splitPane.setOneTouchExpandable(true);

		buttonsPanel.add(updateGraphButton);
		bottomPanel.add(buttonsPanel, BorderLayout.WEST);
		bottomPanel.add(showSplitJoinSemantics, BorderLayout.EAST);

		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
	}

}

class IndividualTableModel extends AbstractTableModel {

	private HeuristicsNet[] data;

	public IndividualTableModel(HeuristicsNet[] data) {
		this.data = data;
	}

	public String getColumnName(int col) {
		return "Population";
	}

	public int getRowCount() {
		return data.length;
	}

	public int getColumnCount() {
		return 1;
	}

	public Object getValueAt(int row, int column) {
		return "Individual " + row + ", fitness " + data[row].getFitness();
	}

}
