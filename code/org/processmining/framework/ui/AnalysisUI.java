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

package org.processmining.framework.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.AnalysisPluginCollection;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.util.ToolTipComboBox;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class AnalysisUI extends JInternalFrame implements Provider {

	private static final long serialVersionUID = -2388596903790097044L;

	private Vector<AlgorithmListItem> algorithms;
	private JDesktopPane desktop;
	private JScrollPane inputsScrollPane;
	private JList algorithmList;
	private JComponent result = null;

	public AnalysisUI(JDesktopPane desktop) {
		super("Perform analysis", true, true, true, true);
		this.desktop = desktop;

		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public ProvidedObject[] getProvidedObjects() {
		return result != null && result instanceof Provider ? ((Provider) result)
				.getProvidedObjects()
				: new ProvidedObject[0];
	}

	private void showAnalysis() {
		final AnalysisInputItem[] configuredItems;
		final AlgorithmListItem algorithm;
		InputItem[] items;

		if (algorithmList.getSelectedValue() == null) {
			JOptionPane.showMessageDialog(this,
					"Please select an analysis algorithm first.", "Analysis",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		algorithm = (AlgorithmListItem) algorithmList.getSelectedValue();
		items = algorithm.getItems();
		AnalysisInputItem[] neededItems = ((AnalysisPlugin) algorithm
				.getAlgorithm()).getInputItems();

		configuredItems = new AnalysisInputItem[items.length];
		for (int i = 0; i < items.length; i++) {
			configuredItems[i] = items[i].getConfiguredItem();
			if (configuredItems[i] == null && (neededItems[i].getMinimum() > 0)) {
				JOptionPane.showMessageDialog(this,
						"Please choose inputs for all items.", "Analysis",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}
		UISettings.getInstance().setLastUsedAnalysis(
				algorithm.getAlgorithm().getName());

		MainUI.getInstance().addAction(algorithm.getAlgorithm(),
				LogStateMachine.START, configuredItems);

		SwingWorker w = new SwingWorker() {
			public Object construct() {
				result = ((AnalysisPlugin) algorithm.getAlgorithm())
						.analyse(configuredItems);
				return null;
			}

			public void finished() {

				MainUI.getInstance().addAction(
						algorithm.getAlgorithm(),
						LogStateMachine.COMPLETE,
						(result instanceof Provider) ? ((Provider) result)
								.getProvidedObjects() : null);

				if (result != null) {
					getContentPane().removeAll();
					getContentPane().add(result, BorderLayout.CENTER);
					setTitle(algorithm.getAlgorithm().getName());
					getContentPane().validate();
					getContentPane().repaint();
				}
			}

		};
		w.start();

	}

	private void frameActivated() {
		for (AlgorithmListItem algorithm : algorithms) {
			algorithm.refresh();
		}
		inputsScrollPane.validate();
		inputsScrollPane.repaint();
	}

	private void setSelectedAlgorithm(int index) {
		if (0 <= index && index < algorithms.size()) {
			JPanel upperLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

			upperLeftPanel.add(algorithms.get(index).getPanel(), null);
			inputsScrollPane.setViewportView(upperLeftPanel);
		} else {
			inputsScrollPane.setViewportView(new JPanel());
		}
		inputsScrollPane.validate();
		inputsScrollPane.repaint();
	}

	private void jbInit() {
		JPanel buttonsPanel, innerButtonsPanel, mainPanel;
		JButton nextButton, closeButton;
		JSplitPane splitPane;
		String lastUsedAnalysis = UISettings.getInstance()
				.getLastUsedAnalysis();
		AnalysisPluginCollection algorithmCollection = AnalysisPluginCollection
				.getInstance();
		int selection = -1;

		inputsScrollPane = new JScrollPane();

		// construct list of algorithms
		algorithms = new Vector<AlgorithmListItem>();
		for (int i = 0; i < algorithmCollection.size(); i++) {
			AnalysisPlugin algorithm = (AnalysisPlugin) algorithmCollection
					.get(i);

			if (algorithm != null) {
				AnalysisInputItem[] items = algorithm.getInputItems();
				InputItem[] inputItems = new InputItem[items.length];

				for (int j = 0; j < items.length; j++) {
					inputItems[j] = new InputItem(inputsScrollPane, items[j],
							desktop);
				}
				algorithms.add(new AlgorithmListItem(algorithm, inputItems));
			}
		}
		Collections.sort(algorithms);
		int i = 0;
		for (AlgorithmListItem algorithm : algorithms) {
			if (algorithm.getAlgorithm().getName().equals(lastUsedAnalysis)) {
				selection = i;
			}
			i++;
		}
		algorithmList = new JList(algorithms);
		algorithmList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if (selection >= 0) {
			algorithmList.setSelectedIndex(selection);
		}
		algorithmList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				setSelectedAlgorithm(algorithmList.getSelectedIndex());
			}
		});

		// build UI
		nextButton = new JButton("Next >>");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAnalysis();
			}
		});
		closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		innerButtonsPanel = new JPanel();
		innerButtonsPanel.add(nextButton);
		innerButtonsPanel.add(closeButton);
		buttonsPanel = new JPanel(new BorderLayout());
		buttonsPanel.add(innerButtonsPanel, BorderLayout.EAST);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(algorithmList), inputsScrollPane);
		splitPane.setDividerLocation(200);
		splitPane.setOneTouchExpandable(true);

		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(splitPane, BorderLayout.CENTER);
		mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameActivated(InternalFrameEvent e) {
				frameActivated();
			}
		});

		setSelectedAlgorithm(selection);
	}
}

class InputItem {

	private AnalysisInputItem item;
	private JDesktopPane desktop;
	private JComponent parent;
	private ArrayList combos;
	private JPanel comboPanel;
	private boolean incompleteInput;
	private boolean inputRequired;
	private JPanel panel;

	private static Icon delIcon = Utils.getStandardIcon("general/Delete16");
	private static Icon addIcon = Utils.getStandardIcon("general/Add16");

	public InputItem(JComponent parent, AnalysisInputItem item,
			JDesktopPane desktop) {
		this.item = item;
		this.desktop = desktop;
		this.parent = parent;
		incompleteInput = false;
		inputRequired = item.getMinimum() > 0;

		combos = new ArrayList();
		for (int i = 0; i < item.getMinimum(); i++) {
			combos.add(createComboBox());
		}
		comboPanel = new JPanel(new GridBagLayout());
		constructComboPanel();

		panel = new JPanel(new BorderLayout());
		panel.add(comboPanel, BorderLayout.WEST);
		panel.add(new JLabel(item.getCaption()), BorderLayout.NORTH);
	}

	public JPanel getPanel() {
		return panel;
	}

	public void refresh() {
		ProvidedObjectComboItem[] items = getComboItems();

		if (items.length == 0) {
			incompleteInput = true;
		} else {
			incompleteInput = false;
			for (int i = 0; i < combos.size(); i++) {
				ToolTipComboBox box = (ToolTipComboBox) combos.get(i);
				ProvidedObjectComboItem selected = (ProvidedObjectComboItem) box
						.getSelectedItem();

				box.setModel(new DefaultComboBoxModel(items));

				for (int j = 0; j < items.length; j++) {
					if (items[j].equals(selected)) {
						box.setSelectedIndex(j);
					}
				}
			}
		}

		constructComboPanel();
		if (parent != null) {
			parent.validate();
			parent.repaint();
		}
	}

	public AnalysisInputItem getConfiguredItem() {
		ProvidedObject[] result;

		if (incompleteInput) {
			return null;
		}

		result = new ProvidedObject[combos.size()];
		for (int i = 0; i < combos.size(); i++) {
			ToolTipComboBox box = (ToolTipComboBox) combos.get(i);
			ProvidedObjectComboItem selected = (ProvidedObjectComboItem) box
					.getSelectedItem();

			if (selected == null) {
				return null;
			}
			result[i] = selected.getObject();
		}
		item.setProvidedObjects(result);
		return item;
	}

	private void constructComboPanel() {
		comboPanel.removeAll();
		if (incompleteInput && inputRequired) {
			comboPanel.add(new JLabel(
					"There is no input available for this item."),
					new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.NORTHWEST,
							GridBagConstraints.NONE, new Insets(0, 20, 0, 0),
							0, 0));
		} else if (incompleteInput && !inputRequired) {
			comboPanel.add(new JLabel(
					"There is no input required for this item."),
					new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.NORTHWEST,
							GridBagConstraints.NONE, new Insets(0, 20, 0, 0),
							0, 0));
		} else {
			for (int i = 0; i < combos.size(); i++) {
				final int index = i;
				boolean hasDelButtons = combos.size() > item.getMinimum();

				comboPanel.add((Component) combos.get(i),
						new GridBagConstraints(hasDelButtons ? 1 : 0, i, 2, 1,
								0.0, 0.0, GridBagConstraints.NORTHWEST,
								GridBagConstraints.NONE, new Insets(0,
										hasDelButtons ? 5 : 20, 0, 0), 0, 0));

				if (hasDelButtons) {
					JButton delButton = new JButton(delIcon);

					delButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							deleteInput(index);
						}
					});
					comboPanel.add(delButton, new GridBagConstraints(0, i, 1,
							1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
							GridBagConstraints.NONE, new Insets(0, 20, 0, 0),
							0, 0));
				}
			}
			if (combos.size() < item.getMaximum()) {
				JButton addButton = new JButton(addIcon);

				addButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						addInput();
					}
				});
				comboPanel
						.add(addButton, new GridBagConstraints(0,
								combos.size(), 1, 1, 0.0, 0.0,
								GridBagConstraints.NORTHWEST,
								GridBagConstraints.NONE,
								new Insets(5, 20, 0, 0), 0, 0));
				JLabel label = new JLabel(
						"Click this button to add more inputs (if required).");
				label.setEnabled(false);
				comboPanel
						.add(label, new GridBagConstraints(1, combos.size(), 1,
								1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
								GridBagConstraints.NONE,
								new Insets(10, 2, 0, 0), 0, 0));
			}
		}
	}

	private void deleteInput(int index) {
		combos.remove(index);
		constructComboPanel();

		if (parent != null) {
			parent.validate();
			parent.repaint();
		}
	}

	private void addInput() {
		ToolTipComboBox box;
		int selIndex = -1;
		if (combos.size() > 0) {
			box = (ToolTipComboBox) combos.get(combos.size() - 1);
			selIndex = box.getSelectedIndex();
		}

		box = createComboBox();
		box.setSelectedIndex((selIndex + 1) % box.getItemCount());
		combos.add(box);

		constructComboPanel();

		if (parent != null) {
			parent.validate();
			parent.repaint();
		}
	}

	private ToolTipComboBox createComboBox() {
		ProvidedObjectComboItem[] items = getComboItems();

		if (items.length == 0) {
			incompleteInput = true;
			return new ToolTipComboBox();
		} else {
			return new ToolTipComboBox(items);
		}
	}

	private ProvidedObjectComboItem[] getComboItems() {
		JInternalFrame[] frames = desktop.getAllFrames();
		ArrayList accepted = new ArrayList();

		// Iterate all frames to check for provided objects
		for (int j = 0; j < frames.length; j++) {
			if (frames[j] instanceof Provider) {
				ProvidedObject[] po = ((Provider) frames[j])
						.getProvidedObjects();

				for (int i = 0; i < po.length; i++) {
					if (item.accepts(po[i])) {
						accepted.add(new ProvidedObjectComboItem(frames[j]
								.getTitle(), po[i]));
					}
				}
			}
		}
		ProvidedObject[] po = MainUI.getInstance().getGlobalProvidedObjects();

		for (int i = 0; i < po.length; i++) {
			if (item.accepts(po[i])) {
				accepted.add(new ProvidedObjectComboItem("ProM Global Objects",
						po[i]));
			}
		}
		return (ProvidedObjectComboItem[]) accepted
				.toArray(new ProvidedObjectComboItem[0]);
	}
}
