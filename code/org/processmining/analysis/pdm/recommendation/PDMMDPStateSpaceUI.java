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

package org.processmining.analysis.pdm.recommendation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.Choice;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.math.BigDecimal;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.pdm.*;
import org.processmining.framework.models.pdm.PDMModel;
import org.processmining.framework.models.pdm.*;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.framework.models.*;
import java.util.Vector;

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
public class PDMMDPStateSpaceUI extends JPanel implements Provider {

	private PDMModel model;
	private PDMStateSpace statespace;
	private Vector strategy;

	private JButton calculateStateSpaceButton = new JButton(
			"Calculate statespace");
	private JButton calculateStrategyButton = new JButton(
			"Calculate MDP strategy");

	private JPanel pdmPanel; // Holds the panel with the PDM model and the
	// actions to be taken.
	private JScrollPane pdmContainer; // Holds the PDM.
	private JPanel actionContainer; // Holds the checkboxes and buttons for the
	// actions that can be taken.
	private JPanel checkboxContainer; // Holds the checkboxes
	private JPanel limitsContainer;
	private JPanel numberOfStatesContainer;
	private JPanel breadthOfTreeContainer;
	private JPanel buttonsContainer; // Holds all buttons
	private JPanel statespacePanel; // Holds the statespace container
	private JScrollPane statespaceContainer; // Holds the calculated statespace.
	private ModelGraphPanel gp; // Holds the PDM model
	private ModelGraphPanel sp; // Holds the PDM statespace
	private JCheckBox proceedAfterRootElement = new JCheckBox(
			"Proceed after root element", false);
	private JCheckBox failurePossible = new JCheckBox(
			"Failure of operations possible", true);
	private JCheckBox allInputEltsAvailable = new JCheckBox(
			"All input elements available", false);
	private JCheckBox coloredStatespace = new JCheckBox("Display colours", true);
	private Choice optimizationChoice = new Choice();
	private JFormattedTextField numberOfStates = new JFormattedTextField(
			new Integer("1000"));
	private JFormattedTextField breadthOfTree = new JFormattedTextField(
			new Integer("50"));

	private boolean proceed_after_root = false;
	private boolean failure_possible = false;
	private boolean all_inputs_available = false;
	private boolean colored_statespace = false;
	private int nStates;
	private int breadth;

	public PDMMDPStateSpaceUI(PDMModel model) {
		this.model = model;
		this.statespace = new PDMStateSpace();

		pdmPanel = new JPanel();
		statespacePanel = new JPanel();
		pdmContainer = new JScrollPane();
		actionContainer = new JPanel();
		checkboxContainer = new JPanel();
		limitsContainer = new JPanel();
		numberOfStatesContainer = new JPanel();
		breadthOfTreeContainer = new JPanel();
		buttonsContainer = new JPanel();
		statespaceContainer = new JScrollPane();

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {

		calculateStateSpaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// calculate the statespace using the given features
				proceed_after_root = proceedAfterRootElement.isSelected();
				failure_possible = failurePossible.isSelected();
				all_inputs_available = allInputEltsAvailable.isSelected();
				colored_statespace = coloredStatespace.isSelected();

				Integer nn = (Integer) numberOfStates.getValue();
				nStates = nn.intValue();

				Integer nn2 = (Integer) breadthOfTree.getValue();
				breadth = nn2.intValue();

				statespace = model.calculateSimpleStateSpace(
						proceed_after_root, failure_possible,
						all_inputs_available, colored_statespace, nStates,
						breadth);

				printTestOutput();

				ModelGraphPanel sp2 = statespace.getGrappaVisualization();
				statespaceContainer = new JScrollPane(sp2);
				statespacePanel.removeAll();
				statespacePanel.setLayout(new BorderLayout());
				statespacePanel.add(statespaceContainer, BorderLayout.CENTER);

				buttonsContainer.remove(calculateStateSpaceButton);
				checkboxContainer.remove(proceedAfterRootElement);
				checkboxContainer.remove(failurePossible);
				checkboxContainer.remove(allInputEltsAvailable);
				checkboxContainer.remove(coloredStatespace);
				actionContainer.remove(limitsContainer);

				buttonsContainer.add(calculateStrategyButton);
				checkboxContainer.add(optimizationChoice);
				optimizationChoice.add("Minimize cost");
				optimizationChoice.add("Minimize processing time");

				invalidate();
				repaint();
				validate();
			}
		});

		calculateStrategyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (getSelectedStrategy().equals("Minimize cost")) {
					strategy = statespace.calculateStrategyMinCost();
				}
				if (getSelectedStrategy().equals("Minimize processing time")) {
					strategy = statespace.calculateStrategyMinTime();
				}

				printTestOutput();

				ModelGraphPanel sp2 = statespace.getGrappaVisualization();
				statespaceContainer = new JScrollPane(sp2);
				statespacePanel.removeAll();
				statespacePanel.setLayout(new BorderLayout());
				statespacePanel.add(statespaceContainer, BorderLayout.CENTER);

				buttonsContainer.remove(calculateStrategyButton);
				checkboxContainer.remove(optimizationChoice);

				String str = writeStrategyToString(strategy);
				JTextArea textArea = new JTextArea(str);
				JScrollPane scrollPane = new JScrollPane(textArea);
				textArea.setEditable(false);
				actionContainer.setPreferredSize(new Dimension(450, 200));
				actionContainer.add(scrollPane, BorderLayout.CENTER);

				invalidate();
				repaint();
				validate();
			}
		});

		buttonsContainer.add(calculateStateSpaceButton);

		gp = model.getGrappaVisualization();
		pdmContainer = new JScrollPane(gp);
		sp = statespace.getGrappaVisualization();
		statespaceContainer = new JScrollPane(sp);

		numberOfStatesContainer.setLayout(new GridLayout(1, 1));
		numberOfStatesContainer.add(new JLabel("Max. number of states:"));
		numberOfStatesContainer.add(numberOfStates);

		breadthOfTreeContainer.setLayout(new GridLayout(1, 1));
		breadthOfTreeContainer.add(new JLabel("Breadth of statespace:"));
		breadthOfTreeContainer.add(breadthOfTree);

		checkboxContainer.setLayout(new GridLayout(2, 2));
		checkboxContainer.add(proceedAfterRootElement);
		checkboxContainer.add(failurePossible);
		checkboxContainer.add(allInputEltsAvailable);
		checkboxContainer.add(coloredStatespace);

		limitsContainer.setLayout(new GridLayout(2, 1));
		limitsContainer.add(breadthOfTreeContainer);
		limitsContainer.add(numberOfStatesContainer);

		buttonsContainer.setLayout(new BorderLayout());
		buttonsContainer.add(calculateStateSpaceButton, BorderLayout.NORTH);

		actionContainer.setLayout(new BorderLayout());
		actionContainer.add(checkboxContainer, BorderLayout.NORTH);
		actionContainer.add(limitsContainer, BorderLayout.CENTER);
		actionContainer.add(buttonsContainer, BorderLayout.SOUTH);

		pdmPanel.setLayout(new BorderLayout());
		pdmPanel.add(pdmContainer, BorderLayout.NORTH);
		pdmPanel.add(actionContainer, BorderLayout.SOUTH);

		statespacePanel.setLayout(new BorderLayout());
		statespacePanel.add(statespaceContainer, BorderLayout.CENTER);

		this.setLayout(new GridLayout(1, 1));
		this.add(pdmPanel);
		this.add(statespacePanel);
	}

	public ProvidedObject[] getProvidedObjects() {
		ArrayList objects = new ArrayList();
		if (model != null) {
			objects
					.add(new ProvidedObject("PDM model", new Object[] { model }));
		}
		if (statespace != null) {
			objects.add(new ProvidedObject("PDM MDP Statespace",
					new Object[] { statespace }));
		}
		if (strategy != null) {
			objects.add(new ProvidedObject("PDM MDP Strategy",
					new Object[] { strategy }));
		}

		ProvidedObject[] res = new ProvidedObject[objects.size()];
		for (int i = 0; i < objects.size(); i++) {
			res[i] = (ProvidedObject) objects.get(i);
		}
		return res;
	}

	public String getSelectedStrategy() {
		String selectedStrategy;
		selectedStrategy = optimizationChoice.getSelectedItem();
		return selectedStrategy;
	}

	public Vector getStrategy() {
		return strategy;
	}

	public String writeStrategyToString(Vector strategy) {
		Vector vector1 = (Vector) strategy.clone();
		String result = "Strategy \n(";
		int size = vector1.size();
		for (int i = 0; i < size; i++) {
			result = result + "state" + i + ": ";
			Vector map = (Vector) vector1.firstElement();
			Vector map2 = (Vector) map.clone();
			int map2size = map2.size();
			if (map2size > 0) {
				for (int j = 0; j < map2size; j++) {
					Vector dec = (Vector) map2.get(j);
					String key = (String) dec.get(0);
					Double c = (Double) dec.get(1);
					// Round up the value for cost
					int decimalPlace = 2;
					BigDecimal bd = new BigDecimal(c);
					bd = bd.setScale(decimalPlace, BigDecimal.ROUND_UP);
					double cc = bd.doubleValue();
					result = result + key + " (" + cc + "), ";
				}
			} else {
				result = result + "-, ";
			}
			result = result + "_";
			result = result.replace(", _", "\n");
			// result = result + "\n";
			vector1.remove(0);
		}
		vector1.clear();
		// result = result + "_";
		result = result + ")";
		// result = result.replace(",_", ")");
		return result;
	}

	protected void printTestOutput() {
		// Message.add("<PDMAnalysis>", Message.TEST);
		// Message.add("</PDMAnalysis>", Message.TEST);
	}
}
