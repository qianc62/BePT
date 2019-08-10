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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
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

import org.processmining.converting.ConvertingPlugin;
import org.processmining.converting.ConvertingPluginCollection;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.plugin.DoNotCreateNewInstance;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.util.StopWatch;
import org.processmining.framework.util.ToolTipComboBox;
import org.processmining.mining.MiningResult;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class ConversionUI extends JInternalFrame implements Provider {

	private ConvertAlgorithm[] algorithms;
	private JDesktopPane desktop;
	private JScrollPane inputsScrollPane;
	private JList algorithmList;
	private ToolTipComboBox combo;
	private ArrayList comboItems;
	private MiningResult result = null;

	public ConversionUI(JDesktopPane desktop) {
		super("Perform conversion", true, true, true, true);
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

	private void showConversion() {
		if (combo == null) {
			return;
		}
		JPanel mainPanel = new JPanel();
		if (algorithmList.getSelectedValue() == null) {
			JOptionPane.showMessageDialog(this,
					"Please select a conversion algorithm first.",
					"Conversion", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		final ConvertingPlugin algorithm = ((ConvertAlgorithm) algorithmList
				.getSelectedValue()).getPlugin();
		if ((combo.getSelectedIndex() < 0)
				|| (combo.getSelectedIndex() >= combo.getItemCount())) {
			JOptionPane.showMessageDialog(this,
					"Please choose inputs for all items.", "Conversion",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		UISettings.getInstance().setLastUsedConversion(algorithm.getName());

		MainUI.getInstance().addAction(algorithm, LogStateMachine.START,
				new Object[] { comboItems.get(combo.getSelectedIndex()) });

		SwingWorker worker = new SwingWorker() {
			MiningResult result;
			StopWatch timer = new StopWatch();

			public Object construct() {
				Message.add("Start conversion.");
				timer.start();
				try {
					if (algorithm instanceof DoNotCreateNewInstance) {
						result = algorithm.convert((ProvidedObject) comboItems
								.get(combo.getSelectedIndex()));
					} else {
						result = ((ConvertingPlugin) algorithm.getClass()
								.newInstance())
								.convert((ProvidedObject) comboItems.get(combo
										.getSelectedIndex()));
					}
				} catch (IllegalAccessException ex) {
					Message.add("No new instantiation of "
							+ algorithm.getName() + " could be made, using"
							+ " old instance instead", Message.ERROR);
					result = algorithm.convert((ProvidedObject) comboItems
							.get(combo.getSelectedIndex()));
				} catch (InstantiationException ex) {
					Message.add("No new instantiation of "
							+ algorithm.getName() + " could be made, using"
							+ " old instance instead", Message.ERROR);
					result = algorithm.convert((ProvidedObject) comboItems
							.get(combo.getSelectedIndex()));
				}
				return result;
			}

			public void finished() {
				timer.stop();
				Message.add("Conversion duration: " + timer.formatDuration());
				MainUI.getInstance().addAction(
						algorithm,
						LogStateMachine.COMPLETE,
						(result instanceof Provider) ? ((Provider) result)
								.getProvidedObjects() : null);

				getContentPane().removeAll();
				getContentPane().add(result.getVisualization(),
						BorderLayout.CENTER);
				getContentPane().validate();
				getContentPane().repaint();
			}
		};
		worker.start();

	}

	private void frameActivated() {
		inputsScrollPane.validate();
		inputsScrollPane.repaint();
	}

	private void setSelectedAlgorithm(int index) {

		if (index >= 0) {

			JButton docsButton = new JButton("Plugin documentation...");

			docsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MainUI.getInstance().showReference(
							((ConvertAlgorithm) algorithmList
									.getSelectedValue()).getPlugin());
				}
			});

			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

			JPanel upperLeftPanel = new JPanel(new GridBagLayout());

			upperLeftPanel.add(docsButton, new GridBagConstraints(0, 1, 1, 1,
					0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.NONE, new Insets(30, 5, 5, 5), 0, 0));

			combo = new ToolTipComboBox(
					getComboItems(((ConvertAlgorithm) algorithmList
							.getSelectedValue()).getPlugin()));
			if (combo.getItemCount() == 0) {
				upperLeftPanel.add(new JLabel(
						"There is no input available for this item."),
						new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.NORTHWEST,
								GridBagConstraints.BOTH,
								new Insets(0, 5, 5, 0), 0, 0));
				combo = null;
			} else {
				upperLeftPanel.add(combo, new GridBagConstraints(0, 0, 1, 1,
						0.0, 0.0, GridBagConstraints.NORTHWEST,
						GridBagConstraints.BOTH, new Insets(0, 5, 5, 0), 0, 0));
			}
			panel.add(upperLeftPanel, null);
			inputsScrollPane.setViewportView(panel);
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
		String lastUsedConversion = UISettings.getInstance()
				.getLastUsedConversion();

		ConvertingPluginCollection algorithmCollection = ConvertingPluginCollection
				.getInstance();
		int selection = -1;

		inputsScrollPane = new JScrollPane();

		// construct list of algorithms
		algorithms = new ConvertAlgorithm[algorithmCollection.size()];
		for (int i = 0; i < algorithmCollection.size(); i++) {
			ConvertingPlugin algorithm = ((ConvertingPlugin) algorithmCollection
					.get(i));

			algorithms[i] = new ConvertAlgorithm(algorithm);

			if (algorithm.getName().equals(lastUsedConversion)) {
				selection = i;
			}
		}
		Arrays.sort(algorithms);

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
				showConversion();
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

	private ProvidedObjectComboItem[] getComboItems(ConvertingPlugin plugin) {
		JInternalFrame[] frames = desktop.getAllFrames();
		ArrayList accepted = new ArrayList();
		comboItems = new ArrayList();

		for (int j = 0; j < frames.length; j++) {
			if (frames[j] instanceof Provider) {
				ProvidedObject[] po = ((Provider) frames[j])
						.getProvidedObjects();

				for (int i = 0; i < po.length; i++) {
					if (plugin.accepts(po[i])) {
						accepted.add(new ProvidedObjectComboItem(frames[j]
								.getTitle(), po[i]));
						comboItems.add(po[i]);
					}
				}
			}
		}
		ProvidedObject[] po = MainUI.getInstance().getGlobalProvidedObjects();

		for (int i = 0; i < po.length; i++) {
			if (plugin.accepts(po[i])) {
				accepted.add(new ProvidedObjectComboItem("ProM Global Objects",
						po[i]));
				comboItems.add(po[i]);
			}
		}
		return (ProvidedObjectComboItem[]) accepted
				.toArray(new ProvidedObjectComboItem[0]);
	}

}

class ConvertAlgorithm implements Comparable {
	private ConvertingPlugin p;

	public ConvertAlgorithm(ConvertingPlugin plugin) {
		p = plugin;
	}

	public ConvertingPlugin getPlugin() {
		return p;
	}

	public String toString() {
		return p.getName();
	}

	public int compareTo(Object o) {
		if (o == null) {
			return -1;
		}
		return -((ConvertAlgorithm) o).getPlugin().getName().toLowerCase()
				.compareTo(p.getName().toLowerCase());
	}

}
