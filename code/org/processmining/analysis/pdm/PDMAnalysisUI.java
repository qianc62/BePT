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

package org.processmining.analysis.pdm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.pdm.PDMDesign;
import org.processmining.framework.models.pdm.PDMModel;
import org.processmining.framework.models.pdm.algorithms.PDMDesignReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;

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
 * @author Irene Vanderfeesten
 * @version 1.0
 */
public class PDMAnalysisUI extends JPanel implements Provider {

	private PDMModel model;
	private PDMDesign design;

	private JButton showPDMButton = new JButton("Show PDM model");
	private JButton loadDesignButton = new JButton("Load design");
	private JButton calculateCouplingCohesionButton = new JButton(
			"Calculate Coupling and Cohesion");
	private JButton makePetriNetButton = new JButton("Make PetriNet");

	private JPanel textContainer; // Holds the panel with CoCoMo analysis
	// results.
	private JScrollPane netContainer; // Holds the net and visualizes selected
	// analysis results.
	private JScrollPane tableContainer; // Holds the table with values.
	private JPanel containersPanel; // Holds both containers
	private JPanel buttonsPanel; // Holds all buttons
	private JPanel analysisPanel; // Holds the textContainer and tableContainer.

	private ModelGraphPanel gp;
	private JTable activities;

	// private HashMap mapping;

	public PDMAnalysisUI(PDMModel model) {
		this.model = model;

		textContainer = new JPanel();
		textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
		// tableContainer = new JPanel();
		containersPanel = new JPanel();
		analysisPanel = new JPanel();
		buttonsPanel = new JPanel();
		activities = new JTable();

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	void jbInit() throws Exception {

		showPDMButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gp.unSelectAll();
				remove(netContainer);
				netContainer = new JScrollPane(gp);
				add(netContainer, BorderLayout.CENTER);
				buttonsPanel.add(showPDMButton);
				buttonsPanel.add(loadDesignButton);
				buttonsPanel.add(calculateCouplingCohesionButton);
				validate();
				repaint();
			}

		});
		loadDesignButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser filechooser = new JFileChooser();
				int returnVal = filechooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						FileInputStream fis = new FileInputStream(filechooser
								.getSelectedFile());
						BufferedInputStream bis = new BufferedInputStream(fis);
						PDMDesignReader reader = new PDMDesignReader(model);
						try {
							reader.read(bis, model);
							design = reader.getDesign();
						} catch (Exception exp) {
							exp.printStackTrace();
						}
					} catch (FileNotFoundException exp) {
						exp.printStackTrace();
						// FileNotFoundException("The file you are trying to open does not exist.");
					}
					;

					buttonsPanel.add(calculateCouplingCohesionButton);
					// buttonsPanel.add(makePetriNetButton);
					validate();
					repaint();

				}

			}
		});
		calculateCouplingCohesionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textContainer.removeAll();
				textContainer.add(new JLabel("Coupling and Cohesion Analysis"));

				Double coh, cpl, ratio;

				if (design != null) {
					coh = design.calculateProcessCohesion();
					cpl = design.calculateProcessCoupling();
					ratio = design.calculateProcessRatio();
				} else {
					System.out.println("design is null");
					coh = 100.00;
					cpl = 100.00;
					ratio = 1000.00;
				}

				String str2 = "The Coupling/Cohesion ratio is:  "
						+ ratio.toString();
				textContainer.add(new JLabel(str2));

				String str1 = "The Process Coupling is:         "
						+ cpl.toString();
				textContainer.add(new JLabel(str1));

				String str = "The Process Cohesion is:         "
						+ coh.toString();
				textContainer.add(new JLabel(str));

				// Fill activities table
				String[] columnNames = design.getTableHeader();
				Object[][] data = design.getTableContent();
				JTable activitiesTable = new JTable(data, columnNames);
				activities.setModel(activitiesTable.getModel());

				printTestOutput();

				/*
				 * TableSorter sorter = new TableSorter(new MyTableModel());
				 * //ADDED THIS //JTable table = new JTable(new MyTableModel());
				 * //OLD JTable table = new JTable(sorter); //NEW
				 * sorter.setTableHeader(table.getTableHeader()); //ADDED THIS
				 */

				validate();
				repaint();

			}
		});
		/*
		 * makePetriNetButton.addActionListener(new ActionListener() { public
		 * void actionPerformed(ActionEvent e) { PDMDesignToPNPlugin plugin =
		 * new PDMDesignToPNPlugin(); plugin.convert(design,model); } });
		 */

		buttonsPanel.add(loadDesignButton);

		gp = model.getGrappaVisualization();
		// mapping = new HashMap();
		// buildGraphMapping(mapping, gp.getSubgraph());

		netContainer = new JScrollPane(gp);

		tableContainer = new JScrollPane(activities);
		activities.setPreferredScrollableViewportSize(new Dimension(200, 70));

		analysisPanel.setLayout(new BoxLayout(analysisPanel, BoxLayout.Y_AXIS));
		analysisPanel.add(textContainer);
		// analysisPanel.add(Box.createRigidArea(new Dimension(5,0)));
		analysisPanel.add(tableContainer);

		containersPanel.setLayout(new GridLayout(1, 1));
		containersPanel.add(netContainer);
		containersPanel.add(analysisPanel);

		this.setLayout(new BorderLayout());
		this.add(containersPanel, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.SOUTH);

		// add test results

	}

	public ProvidedObject[] getProvidedObjects() {
		ArrayList objects = new ArrayList();
		if (model != null) {
			objects
					.add(new ProvidedObject("PDM model", new Object[] { model }));
		}
		if (design != null) {
			objects.add(new ProvidedObject("PDM design",
					new Object[] { design }));
		}
		ProvidedObject[] res = new ProvidedObject[objects.size()];
		for (int i = 0; i < objects.size(); i++) {
			res[i] = (ProvidedObject) objects.get(i);
		}
		return res;
	}

	protected void printTestOutput() {
		Message.add("<PDMAnalysis>", Message.TEST);
		Message.add("<Number of activities = " + design.getActivities().size()
				+ " >", Message.TEST);
		Message.add("<Coupling Cohesion Ratio = "
				+ design.calculateProcessRatio() + " >", Message.TEST);
		Message.add("</PDMAnalysis>", Message.TEST);
	}

}
